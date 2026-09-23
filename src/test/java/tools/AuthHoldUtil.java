package tools;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Shared "hold a just-authenticated browser open for CDP attach" logic used by both
 * {@link LifeAuthStateBootstrapTest} and {@link StudioAuthStateBootstrapTest} so an
 * unattended run (Jenkins, {@code claude -p}) can point the Playwright MCP server at an
 * already-logged-in session instead of having Claude perform the login itself.
 */
final class AuthHoldUtil {
    private AuthHoldUtil() {}

    static HeldCdpBrowser launchChromiumForCdp(Playwright playwright, int cdpPort, boolean headless, String profilePrefix)
            throws IOException, InterruptedException {
        Files.createDirectories(Path.of("target"));
        Path profileDir = Files.createTempDirectory(Path.of("target"), profilePrefix + "-cdp-profile-");
        Path browserLog = Path.of("target", profilePrefix + "-cdp-" + cdpPort + ".log");

        List<String> command = new ArrayList<>();
        command.add(playwright.chromium().executablePath());
        command.add("--remote-debugging-port=" + cdpPort);
        command.add("--user-data-dir=" + profileDir.toAbsolutePath());
        command.add("--no-first-run");
        command.add("--no-default-browser-check");
        if ("root".equals(System.getProperty("user.name"))) {
            // Chromium's sandbox refuses to initialize as root (e.g. inside a Docker
            // container running as root by default) unless explicitly disabled.
            command.add("--no-sandbox");
        }
        if (headless) {
            command.add("--headless=new");
            command.add("--disable-gpu");
        }

        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.appendTo(browserLog.toFile()))
                .start();

        try {
            waitForCdpEndpoint(cdpPort, process, browserLog);
            Browser browser = playwright.chromium().connectOverCDP("http://localhost:" + cdpPort);
            return new HeldCdpBrowser(browser, process, profileDir);
        } catch (IOException | InterruptedException | RuntimeException e) {
            if (process.isAlive()) {
                process.destroy();
            }
            deleteRecursively(profileDir);
            throw e;
        }
    }

    static BrowserContext firstContext(Browser browser) {
        List<BrowserContext> contexts = browser.contexts();
        return contexts.isEmpty() ? browser.newContext() : contexts.get(0);
    }

    static Page firstPage(BrowserContext context) {
        List<Page> pages = context.pages();
        return pages.isEmpty() ? context.newPage() : pages.get(0);
    }

    static void writeReadyMarker(Path readyMarker, int cdpPort) throws IOException {
        Files.createDirectories(readyMarker.getParent());
        Files.writeString(readyMarker, String.valueOf(cdpPort));
    }

    static void waitForDoneSignalOrTimeout(Path doneMarker, int holdSeconds) throws InterruptedException, IOException {
        long deadline = System.currentTimeMillis() + holdSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            if (Files.exists(doneMarker)) {
                Files.delete(doneMarker);
                return;
            }
            Thread.sleep(2000);
        }
        System.out.println("Hold timeout (" + holdSeconds + "s) elapsed without a done signal; closing.");
    }

    private static void waitForCdpEndpoint(int cdpPort, Process process, Path browserLog)
            throws IOException, InterruptedException {
        long deadline = System.currentTimeMillis() + 30000L;
        URI versionEndpoint = URI.create("http://localhost:" + cdpPort + "/json/version");
        IOException lastError = null;

        while (System.currentTimeMillis() < deadline) {
            if (!process.isAlive()) {
                throw new IOException("Chromium exited before CDP became ready. See " + browserLog);
            }

            try {
                HttpURLConnection connection = (HttpURLConnection) versionEndpoint.toURL().openConnection();
                connection.setConnectTimeout(500);
                connection.setReadTimeout(500);
                if (connection.getResponseCode() == 200) {
                    return;
                }
            } catch (IOException e) {
                lastError = e;
            }

            Thread.sleep(500);
        }

        throw new IOException("Timed out waiting for CDP endpoint on port " + cdpPort + ". See " + browserLog, lastError);
    }

    private static void deleteRecursively(Path path) {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder()).forEach(file -> {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException ignored) {
                    // Temporary browser profiles are best-effort cleanup.
                }
            });
        } catch (IOException ignored) {
            // Temporary browser profiles are best-effort cleanup.
        }
    }

    static final class HeldCdpBrowser implements AutoCloseable {
        private final Browser browser;
        private final Process process;
        private final Path profileDir;

        private HeldCdpBrowser(Browser browser, Process process, Path profileDir) {
            this.browser = browser;
            this.process = process;
            this.profileDir = profileDir;
        }

        Browser browser() {
            return browser;
        }

        @Override
        public void close() {
            try {
                browser.close();
            } catch (RuntimeException ignored) {
                // The browser may already be gone after an external CDP client closes it.
            }
            if (process.isAlive()) {
                process.destroy();
            }
            deleteRecursively(profileDir);
        }
    }
}
