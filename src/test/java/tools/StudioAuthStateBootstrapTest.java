package tools;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import utils.ConfigReader;

public class StudioAuthStateBootstrapTest {
    private static final Path AUTH_STATE_PATH = Path.of(".claude/auth/studio-prerelease-state.json");
    private static final Path READY_MARKER = Path.of(".claude/auth/studio-cdp-ready");
    private static final Path DONE_MARKER = Path.of(".claude/auth/studio-cdp-done");
    private static final String TARGET_ACCOUNT = "PP engineering test";

    /**
     * By default this behaves exactly as before: manual local run, visible browser,
     * saves storage state, closes immediately. Set {@code -Dauth.holdForCdp=true} to
     * instead hold the browser open on a fixed CDP port for an unattended
     * {@code claude -p "/analyze-failure"} run to attach to (see
     * {@link LifeAuthStateBootstrapTest} for the Life-app equivalent) — use this variant
     * for Studio-flavored features, since plain Life login lands on the wrong account
     * and never reaches Studio.
     */
    @Test
    public void saveStudioPreReleaseAuthState() throws Exception {
        boolean headless = Boolean.parseBoolean(System.getProperty("auth.headless", "true"));
        boolean holdForCdp = Boolean.parseBoolean(System.getProperty("auth.holdForCdp", "false"));
        int cdpPort = Integer.parseInt(System.getProperty("auth.cdpPort", "9224"));
        int holdSeconds = Integer.parseInt(System.getProperty("auth.holdSeconds", "1800"));

        Files.deleteIfExists(READY_MARKER);

        try (Playwright playwright = Playwright.create()) {
            AuthHoldUtil.HeldCdpBrowser heldBrowser = null;
            Browser browser = null;
            try {
                BrowserContext context;
                if (holdForCdp) {
                    heldBrowser = AuthHoldUtil.launchChromiumForCdp(playwright, cdpPort, headless, "studio");
                    browser = heldBrowser.browser();
                    context = AuthHoldUtil.firstContext(browser);
                } else {
                    browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
                    context = browser.newContext();
                }
                Page page = AuthHoldUtil.firstPage(context);

                page.navigate(ConfigReader.getProperty("preReleaseURL"));
                page.locator("#UserName").fill(ConfigReader.getInternalPreReleaseUsername());
                page.locator("#Password").fill(ConfigReader.getInternalPreReleasePassword());
                page.locator(".loginLabel").click();
                page.waitForURL(url -> url.contains("Buyer") || url.contains("Life") || url.contains("Admin"));

                selectBuyingPlatformIfPresent(page);
                navigateToLife(page);
                clickPulsePointLogo(page);
                page.waitForTimeout(2000);
                openHamburgerMenuAndSelect(page, "Administration");
                switchAccountIfPresent(page, TARGET_ACCOUNT);
                checkStudioPermissionsFromBackground(page);
                page.waitForTimeout(2000);
                navigateToStudioFromMenu(page);

                context.storageState(new BrowserContext.StorageStateOptions().setPath(AUTH_STATE_PATH));

                if (holdForCdp) {
                    AuthHoldUtil.writeReadyMarker(READY_MARKER, cdpPort);
                    System.out.println("Studio authenticated. CDP endpoint: http://localhost:" + cdpPort);
                    AuthHoldUtil.waitForDoneSignalOrTimeout(DONE_MARKER, holdSeconds);
                }
            } finally {
                if (heldBrowser != null) {
                    heldBrowser.close();
                } else if (browser != null) {
                    browser.close();
                }
            }
        } finally {
            Files.deleteIfExists(READY_MARKER);
        }

        System.out.println("Saved Studio auth state to " + AUTH_STATE_PATH);
    }

    private static void switchAccountIfPresent(Page page, String account) {
        Locator accountName = page.locator("//div[@class='accountname']");
        if (accountName.count() == 0) {
            System.out.println("Account switcher was not visible before saving state. Current URL: " + page.url());
            return;
        }

        String currentAccount = accountName.textContent(new Locator.TextContentOptions().setTimeout(10000));
        if (currentAccount != null && currentAccount.contains(account)) {
            return;
        }

        accountName.click();
        Locator accountSearch = page.locator("//div[@id='accountSwitcher']/input[@placeholder='Search']");
        if (accountSearch.count() == 0) {
            System.out.println("Account search was not visible before saving state. Current URL: " + page.url());
            return;
        }

        accountSearch.fill(account);
        page.waitForTimeout(1500);
        page.locator("//div[@id='accountSwitcher']//div[@class='item']").first().click();
        page.waitForLoadState();
        page.waitForTimeout(3000);
    }

    private static void selectBuyingPlatformIfPresent(Page page) {
        Locator buyingPlatform =
                page.locator("//div[contains(@class,'portalSelectionLabel') and contains(text(),'BUYING PLATFORM')]");
        if (buyingPlatform.count() > 0 && buyingPlatform.isVisible()) {
            buyingPlatform.click();
            page.waitForTimeout(3000);
        }
    }

    private static void navigateToLife(Page page) {
        Locator buyerLink = page.locator("//span[contains(@class,'buyerPortalLink')]");
        try {
            buyerLink.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        } catch (PlaywrightException e) {
            return;
        }
        buyerLink.click();
        page.waitForURL(url -> url.contains("Buyer"), new Page.WaitForURLOptions().setTimeout(30000));
        page.waitForTimeout(3000);
    }

    private static void openHamburgerMenuAndSelect(Page page, String menuItemText) {
        page.locator("//img[contains(@alt,'menu')]").click();
        page.waitForTimeout(500);
        page.getByText(menuItemText, new Page.GetByTextOptions().setExact(true)).first().click();
    }

    private static void navigateToStudioFromMenu(Page page) {
        clickPulsePointLogo(page);
        openHamburgerMenuAndSelect(page, "Studio");
        page.waitForURL(url -> url.contains("Studio"), new Page.WaitForURLOptions().setTimeout(30000));
        page.waitForTimeout(5000);
        closeAiPanelIfPresent(page);
    }

    private static void checkStudioPermissionsFromBackground(Page page) {
        page.waitForTimeout(3000);
        page.getByRole(com.microsoft.playwright.options.AriaRole.LINK, new Page.GetByRoleOptions().setName("Accounts"))
                .click();
        page.waitForTimeout(3000);

        page.getByRole(com.microsoft.playwright.options.AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Search"))
                .fill(TARGET_ACCOUNT);
        page.locator(".ui > .iconSprite").click();
        page.locator(String.format("//div[@title='%s']", TARGET_ACCOUNT)).click();
        page.waitForTimeout(3000);

        Locator studioSettingsIcon = page.locator("//a[contains(@class,'studio-settings') or contains(@class,'settings')]")
                .or(page.locator("//*[contains(@class,'studio') and contains(@class,'settings')]"));
        if (studioSettingsIcon.count() > 0 && studioSettingsIcon.first().isVisible()) {
            studioSettingsIcon.first().click();
            page.waitForTimeout(2000);
            Locator cancel = page.getByRole(
                    com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Cancel"));
            if (cancel.count() > 0 && cancel.first().isVisible()) {
                cancel.first().click();
            }
        } else {
            System.out.println("Studio settings icon was not found during auth bootstrap. Current URL: " + page.url());
        }
    }

    private static void clickPulsePointLogo(Page page) {
        Locator logo = page.locator("//div[contains(@class, 'dynamic-logo')] | //app-buyer-logo/div[@class='logo-holder']");
        if (logo.count() > 0 && logo.isVisible()) {
            logo.click();
            page.waitForTimeout(2000);
        }
    }

    private static void closeAiPanelIfPresent(Page page) {
        Locator aiPanel = page.locator("//div[@class='ai-assistant-panel open']");
        Locator closeButton = page.locator("//button[@aria-label='Close AI Assistant' and @class='ai-icon-btn']");
        if (aiPanel.count() > 0 && aiPanel.isVisible() && closeButton.count() > 0 && closeButton.isVisible()) {
            closeButton.click();
            page.waitForTimeout(1000);
        }
    }
}
