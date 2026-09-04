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
    private final Locator AUDIENCE_ICON;
    private final Locator SUBMIT_REQUEST_BUTTON;
    private final Locator AUDIENCE_SUBMIT_VERIFICATION;
    private final Locator WORKSPACE_SUBMIT_ALERT;
    private final Locator REQUEST_SUBMIT_ALERT;
    private final Locator OK_BUTTON;
    WaitUtility waitUtility;

    public DTCExplorerWorkspace(Page page) {
        this.page = page;
        this.waitUtility = new WaitUtility(page);
        this.WORKSPACE_FRAME = page.frameLocator("iframe").frameLocator("iframe");
        this.DASHBOARD_FRAME = WORKSPACE_FRAME.locator("#extension-root iframe").contentFrame();
        this.SAVE_WORKSPACE = WORKSPACE_FRAME.locator("[data-tour-id*='save-workspace-button']");
        this.UNIQUE_CONSUMER_TEXT = DASHBOARD_FRAME.locator("//h3[contains(text(), 'Unique Consumers')]");
        this.UNIQUE_CONSUMER_COUNT = DASHBOARD_FRAME.locator(
                "//h3[normalize-space()='Unique Consumers']/ancestor::div[contains(@class,'kpi-visualization')]//span");
        this.AUDIENCE_ICON = WORKSPACE_FRAME
                .locator("//div[@role='group']/following-sibling::div");
        this.SUBMIT_REQUEST_BUTTON = WORKSPACE_FRAME.locator("//button[.//div[text()='Submit request']]");
        this.AUDIENCE_SUBMIT_VERIFICATION = WORKSPACE_FRAME.locator("//ds-typography[text()='Your Audience is being processed']");
        this.WORKSPACE_SUBMIT_ALERT = WORKSPACE_FRAME.locator("//p[normalize-space(.)='Workspace saved successfully']");
        this.REQUEST_SUBMIT_ALERT = WORKSPACE_FRAME.locator("//p[normalize-space(.)='Request submitted successfully']");
        this.OK_BUTTON = WORKSPACE_FRAME.locator("//button/div[text()='Ok']");
    }

    public void waitForDashboardLoad() {
        waitUtility.waitForLocatorVisible(UNIQUE_CONSUMER_TEXT);
    }

    public void saveDTCExplorerWorkspace() {
        SAVE_WORKSPACE.click();
    }

    public String getUniqueConsumerCount(String countType) {
        waitUtility.waitForLocatorVisible(DASHBOARD_FRAME.locator(String.format("//h3[contains(text(), '%s')]", countType)));
        return DASHBOARD_FRAME.locator(String.format("//h3[normalize-space()='%s']/ancestor::div[contains(@class,'kpi-visualization')]//span", countType)).textContent().replace(",", "");
    }

    public void clickAudienceIcon(){
        AUDIENCE_ICON.click();
    }

    public void clickSubmitButton() {
        SUBMIT_REQUEST_BUTTON.click();
    }

    public String getDialogMessage() {
        waitUtility.waitForLocatorVisible(AUDIENCE_SUBMIT_VERIFICATION);
        return AUDIENCE_SUBMIT_VERIFICATION.innerText().trim();
    }

    public String getDTCExplorerWorkspaceSubmissionAlert() {
        waitUtility.waitForLocatorVisible(WORKSPACE_SUBMIT_ALERT);
        String text = WORKSPACE_SUBMIT_ALERT.textContent().trim();
        waitUtility.waitForLocatorHidden(WORKSPACE_SUBMIT_ALERT);
        return text;
    }

    public String getDTCExplorerWorkspaceRequestSubmitAlert() {
        waitUtility.waitForLocatorVisible(REQUEST_SUBMIT_ALERT);
        String text = REQUEST_SUBMIT_ALERT.textContent().trim();
        waitUtility.waitForLocatorHidden(REQUEST_SUBMIT_ALERT);
        return text;
    }
}
