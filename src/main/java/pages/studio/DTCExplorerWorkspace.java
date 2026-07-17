package pages.studio;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import utils.WaitUtility;

public class DTCExplorerWorkspace {

    private final Page page;
    private final FrameLocator WORKSPACE_FRAME;
    private final FrameLocator DASHBOARD_FRAME;
    private final Locator SAVE_WORKSPACE;
    private final Locator UNIQUE_CONSUMER_TEXT;
    private final Locator UNIQUE_CONSUMER_COUNT;
    private final Locator SUBMIT_ICON;
    private final Locator SUBMIT_BUTTON;
    private final Locator AUDIENCE_SUBMIT_VERIFICATION;
    private final Locator WORKSPACE_SUBMIT_TOAST;
    private final Locator REQUEST_SUBMIT_TOAST;
    private final WaitUtility waitUtility;

    public DTCExplorerWorkspace(Page page) {
        this.page = page;
        this.waitUtility = new WaitUtility(page);
        this.WORKSPACE_FRAME = page.frameLocator("iframe").frameLocator("iframe");
        this.DASHBOARD_FRAME = WORKSPACE_FRAME.locator("#extension-root iframe").contentFrame();
        this.SAVE_WORKSPACE = WORKSPACE_FRAME.locator("[data-tour-id*='save-workspace-button']");
        this.UNIQUE_CONSUMER_TEXT = DASHBOARD_FRAME.locator("//h3[contains(text(), 'Unique Consumers')]");
        this.UNIQUE_CONSUMER_COUNT = DASHBOARD_FRAME.locator(
                "//h3[normalize-space()='Unique Consumers']/ancestor::div[contains(@class,'single-value-visualization')]//span");
        this.SUBMIT_ICON = WORKSPACE_FRAME
                .locator("//div[contains(@class, 'sc-cXPBUD')]//div[contains(@class, 'Icon-sc')]")
                .first();
        this.SUBMIT_BUTTON = WORKSPACE_FRAME.locator("//button[.//div[text()='Submit request']]");
        this.AUDIENCE_SUBMIT_VERIFICATION = WORKSPACE_FRAME.locator("//p[text()='Your Audience is being processed']");
        this.WORKSPACE_SUBMIT_TOAST = WORKSPACE_FRAME.locator("//p[normalize-space(.)='Workspace saved successfully']");
        this.REQUEST_SUBMIT_TOAST = WORKSPACE_FRAME.locator("//p[normalize-space(.)='Request submitted successfully']");
    }

    public void waitForDashboardLoad() {
        waitUtility.waitForLocatorVisible(UNIQUE_CONSUMER_TEXT);
    }

    public void saveDTCExplorerWorkspace() {
        SAVE_WORKSPACE.click();
    }

    public String getUniqueConsumerCount() {
        waitUtility.waitForLocatorVisible(UNIQUE_CONSUMER_TEXT);
        return UNIQUE_CONSUMER_COUNT.textContent().replace(",", "");
    }

    public void clickSubmitButton() {
        SUBMIT_ICON.click();
        SUBMIT_BUTTON.click();
    }

    public String getDialogMessage() {
        SUBMIT_ICON.click();
        waitUtility.waitForLocatorVisible(AUDIENCE_SUBMIT_VERIFICATION);
        return AUDIENCE_SUBMIT_VERIFICATION.innerText().trim();
    }

    public void verifyDTCExplorerWorkspaceConfirmationToast() {
        waitUtility.waitForLocatorVisible(WORKSPACE_SUBMIT_TOAST);
        waitUtility.waitForLocatorVisible(REQUEST_SUBMIT_TOAST);
    }
}
