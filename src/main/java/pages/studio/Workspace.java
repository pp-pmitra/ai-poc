package pages.studio;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import factory.DriverFactory;
import utils.CommonUtils;
import utils.WaitUtility;

public class Workspace {

    private final Page page;
    private final Locator STUDIO_CLICK;
    private final Locator FLY_PAGE_BUTTON;
    private final Locator PUBLISH_NPI;
    private final Locator PUBLISHED_NPI;
    private final Locator STATIC_LIST;
    private final Locator LIVE_LIST;
    private final Locator SELECT_HCP;
    private final Locator SELECT_LIFE;
    private final Locator PUBLISH_BUTTON;
    private final FrameLocator WORKSPACE_FRAME;
    private final Locator WORKSPACE_CREATED_ALERT;
    private final Locator WEBHOOK_ICON;
    private final Locator WEBHOOK_TOGGLE_BUTTON;
    private final Locator WEBHOOK_PANEL_TITLE;
    private final Locator WEBHOOK_CANCEL_BUTTON;
    private final Locator URL_TEXTAREA;
    private final Locator BODY_TEXTAREA;
    private final Locator PARAM;
    private final Locator WEBHOOK_BUTTONS;
    private final Locator WEBHOOK_SUCCESS_ALERT;
    private final Locator WEBHOOK_SAVE_BUTTON;
    private final Locator INLINE_ERROR_MESSAGE;
    private final Locator WEBHOOK_ERROR_ALERT;
    private final Locator CREATE_WORKSPACE;
    private final Locator BEFORE_YOU_LEAVE_DIALOG;
    private final Locator EXIT_BUTTON;
    private final Locator BACK_ARROW;
    private final Locator DOWNLOAD_NPI_BUTTON;
    private final Locator DOWNLOAD_BUTTON;
    private final Locator DOWNLOAD_SUCCESS_ALERT;
    private final Locator IDENTIFIED_NPI_COUNT;
    private final Locator RETROFIT_CHECKBOX;
    private final Locator NPI_ENGAGING_TEXT;
    private final Locator PUBLISH_LOADER;
    private final Locator HCP_WORKSPACE_FILTER_CHECKBOX;
    private final Locator EXISTING_WORKSPACE;
    private final Locator ADVERTISER_SELECTOR_DROPDOWN;
    private final Locator NPI_LIST_PULLOUT_MENU;
    private final Locator OK_BUTTON;
    private final Locator NPI_PUBLISH_ALERT;
    private final Locator SAVE_WORKSPACE;
    WaitUtility waitUtility;
    BrandExplorerWorkspace brandExplorerWorkspace = new BrandExplorerWorkspace(DriverFactory.getPage());
    ExplorerWorkspace explorerWorkspace = new ExplorerWorkspace(DriverFactory.getPage());

    public Workspace(Page page) {
        this.page = page;
        this.waitUtility = new WaitUtility(page);
        this.WORKSPACE_FRAME = page.frameLocator("iframe").frameLocator("iframe");
        this.STUDIO_CLICK = page.locator("#megamenu div")
                .filter(new Locator.FilterOptions().setHasText("Studio"))
                .nth(3);
        this.FLY_PAGE_BUTTON = WORKSPACE_FRAME
                .locator("//div[@role='group']/following-sibling::div//button")
                .first();
        this.PUBLISH_NPI = WORKSPACE_FRAME.getByRole(
                AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Publish NPI List"));
        this.PUBLISHED_NPI = WORKSPACE_FRAME.getByRole(
                AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Published NPI List"));
        this.STATIC_LIST =
                WORKSPACE_FRAME.getByText("Static List",
                        new FrameLocator.GetByTextOptions().setExact(true));
        this.LIVE_LIST =
                WORKSPACE_FRAME.getByText("Live List",
                        new FrameLocator.GetByTextOptions().setExact(true));
        this.SELECT_HCP =
                WORKSPACE_FRAME.locator("ds-checkbox", new FrameLocator.LocatorOptions().setHasText("HCP365"));
        this.SELECT_LIFE =
                WORKSPACE_FRAME.locator("ds-checkbox", new FrameLocator.LocatorOptions().setHasText("Life"));
        this.PUBLISH_BUTTON =
                WORKSPACE_FRAME.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Publish"));
        this.WORKSPACE_CREATED_ALERT = WORKSPACE_FRAME.locator(
                "//p[contains(text(),'Workspace created successfully') or contains(text(),'Workspace saved successfully')]");
        this.WEBHOOK_ICON = WORKSPACE_FRAME.locator(
                "(//div[@role='group']/following-sibling::div//button)[3]"); // no unique identifier is available hence
        // index needs to be provided
        this.WEBHOOK_TOGGLE_BUTTON = WORKSPACE_FRAME.locator("//ds-typography[contains(text(),'Webhook')]/parent::div/following-sibling::div//span[contains(@class,'MuiButtonBase-root')]");
        this.WEBHOOK_PANEL_TITLE = WORKSPACE_FRAME.locator("//div[@role='dialog']//ds-typography[contains(text(),'Webhook') and @role='heading']");
        this.WEBHOOK_CANCEL_BUTTON = WORKSPACE_FRAME.locator("//button[@type='button']/div[contains(text(),'Cancel')]");
        this.URL_TEXTAREA = WORKSPACE_FRAME.locator("//textarea[@name='url']");
        this.BODY_TEXTAREA = WORKSPACE_FRAME.locator("//textarea[@name='body']");
        this.PARAM = WORKSPACE_FRAME.locator("//ul[contains(@role,'menu')]//ds-typography");
        this.WEBHOOK_BUTTONS = WORKSPACE_FRAME.locator("//button[contains(@class,'ButtonItem-sc')]");
        this.WEBHOOK_SUCCESS_ALERT = WORKSPACE_FRAME.locator("//p[contains(text(),'Webhook setup successfully')]");
        this.WEBHOOK_SAVE_BUTTON = WORKSPACE_FRAME.locator("//button[@type='submit']");
        this.INLINE_ERROR_MESSAGE = WORKSPACE_FRAME.locator(
                " //label[contains(@class,'FieldLabel')]/following-sibling::div/div[contains(@class,'ValidationMessage')]");
        this.WEBHOOK_ERROR_ALERT =
                WORKSPACE_FRAME.locator("//div[contains(@class, 'Toastify')]//p[contains(text(),'Webhook')]");
        this.CREATE_WORKSPACE = WORKSPACE_FRAME.locator(
                "//div[text()='Create New Workspace' or contains(text(),'Open New Workspace')]");
        this.BEFORE_YOU_LEAVE_DIALOG = WORKSPACE_FRAME.locator("//h3[contains(text(),'Before you leave')]");
        this.EXIT_BUTTON = WORKSPACE_FRAME.locator("//div[contains(text(),'Yes, Exit')]");
        this.BACK_ARROW = WORKSPACE_FRAME.locator("//div[contains(@style,'cursor: pointer')]");
        this.DOWNLOAD_NPI_BUTTON = WORKSPACE_FRAME.locator("//div[contains(text(),'Download NPIs')]");
        this.DOWNLOAD_BUTTON = WORKSPACE_FRAME.locator("//div[text()='Download']");
        this.DOWNLOAD_SUCCESS_ALERT = WORKSPACE_FRAME.locator("//p[text()='Download completed successfully']");
        this.IDENTIFIED_NPI_COUNT = WORKSPACE_FRAME
                .locator("#extension-root iframe")
                .contentFrame()
                .locator(
                        "//h3[contains(text(),'Identified NPIs')]/ancestor::div[contains(@class,'kpi-visualization')]//span");
        this.RETROFIT_CHECKBOX = WORKSPACE_FRAME.getByRole(AriaRole.CHECKBOX, new FrameLocator.GetByRoleOptions().setName("Retrofit NPIs"));
        this.NPI_ENGAGING_TEXT = WORKSPACE_FRAME.locator("//p[contains(text(),'NPIs engaging on or')]");
        this.HCP_WORKSPACE_FILTER_CHECKBOX = WORKSPACE_FRAME.getByRole(
                AriaRole.CHECKBOX, new FrameLocator.GetByRoleOptions().setName("HCP Explorer"));
        this.EXISTING_WORKSPACE = WORKSPACE_FRAME.locator("//tr[1]/td[1]//span");
        this.ADVERTISER_SELECTOR_DROPDOWN = WORKSPACE_FRAME.getByRole(
                AriaRole.TEXTBOX, new FrameLocator.GetByRoleOptions().setName("All Advertisers"));
        this.PUBLISH_LOADER = WORKSPACE_FRAME.locator(
                "//div[contains(@data-tour-id,'hcp-workspace-actions-container')]/div[contains(@data-testid, 'loading-spinner')]");
        this.NPI_LIST_PULLOUT_MENU = WORKSPACE_FRAME.locator("//ul[@data-tour-id='npi-list-pullout-menu']");
        this.OK_BUTTON = WORKSPACE_FRAME.locator("//div[contains(text(),'OK')]");
        this.NPI_PUBLISH_ALERT = WORKSPACE_FRAME.locator("//p[contains(text(), 'NPI list published successfully')]");
        this.SAVE_WORKSPACE = WORKSPACE_FRAME.locator("//button[contains(@data-tour-id,'save-workspace-button')]");
    }

    public void studio() {
        STUDIO_CLICK.click();
        page.reload();
    }

    public void clickFlyOrPageButton() {
        if (NPI_PUBLISH_ALERT.isVisible()) waitUtility.waitForLocatorHidden(NPI_PUBLISH_ALERT);
        if (PUBLISH_LOADER.isVisible()) waitUtility.waitForLocatorHidden(PUBLISH_LOADER);
        if (NPI_LIST_PULLOUT_MENU.isVisible()) {
            OK_BUTTON.click();
        } else {
            FLY_PAGE_BUTTON.click();
        }
    }

    public void clickPublishNPI() {
        PUBLISH_NPI.click();
    }

    public void publishNPI(String listType) {
        if (listType.equals("Static")) {
            STATIC_LIST.click();
        } else {
            LIVE_LIST.click();
        }
    }

    public void hcp() {
        String ariaChecked = SELECT_HCP.locator("button").getAttribute("aria-checked");
        if ("false".equals(ariaChecked)) {
            SELECT_HCP.click();
        }
    }

    public void life() {
        String ariaChecked = SELECT_LIFE.locator("button").getAttribute("aria-checked");
        if ("false".equals(ariaChecked)) {
            SELECT_LIFE.click();
        }
    }

    public void clickPublish() {
        PUBLISH_BUTTON.click();
    }

    public String verifyPublishedNpi() {
        return PUBLISHED_NPI.innerText();
    }

    public void waitTillWorkspaceAlertHide() {
        waitUtility.waitForLocatorHidden(WORKSPACE_CREATED_ALERT);
    }

    public void waitTillWorkspaceSaveButtonIsDisabled(String workspaceType) {
        // Make workspace save verification type-aware so HCP Explorer can rely on the successful save toast
        // while other workspace types continue waiting for the Save button to become disabled.
        if (!"Brand Explorer".equalsIgnoreCase(workspaceType)) {
            return;
        }
        page.waitForCondition(SAVE_WORKSPACE::isDisabled);
    }

    public void clickWebhookIcon() {
        WEBHOOK_ICON.click();
    }

    public String verifyWebhookToggleButton() {
        WEBHOOK_PANEL_TITLE.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        if (WEBHOOK_TOGGLE_BUTTON.getAttribute("class").contains("Mui-disabled")) {
            return "Disabled";
        } else {
            WEBHOOK_TOGGLE_BUTTON.click();
            if (WEBHOOK_TOGGLE_BUTTON.getAttribute("class").contains("Mui-checked")) return "Enabled";
        }
        return " ";
    }

    public void closeWebhookPanel() {
        WEBHOOK_CANCEL_BUTTON.click();
    }

    public void clickRequestOrContentButton(String buttonName) {
        for (int i = 0; i < WEBHOOK_BUTTONS.count(); i++) {
            if (WEBHOOK_BUTTONS.nth(i).innerText().contains(buttonName)
                    && WEBHOOK_BUTTONS.nth(i).getAttribute("aria-pressed").contains("true")) {
                break;
            } else if (WEBHOOK_BUTTONS.nth(i).innerText().contains(buttonName)) {
                WEBHOOK_BUTTONS.nth(i).click();
                break;
            }
        }
    }

    public void addURL(String url) {
        URL_TEXTAREA.fill(url);
    }

    public String verifyMacrosAppendedToURL() {
        return URL_TEXTAREA.inputValue();
    }

    public void addBody(String jsonFile) throws IOException {
        BODY_TEXTAREA.fill(CommonUtils.readJsonTestDataFile(jsonFile));
    }

    public void addMacros(String textType, String param, List<String> macrosList) {
        Locator fieldBlock = WORKSPACE_FRAME.locator("label")
                .filter(new Locator.FilterOptions().setHasText(textType))
                .locator("xpath=../..");
        for (String macro : macrosList) {
            Locator macroChip = fieldBlock.locator("ds-chip").filter(new Locator.FilterOptions().setHasText(macro));
            if (macroChip.isVisible()) {
                macroChip.click();
                for (int j = 0; j < PARAM.count(); j++) {
                    if (PARAM.nth(j).innerText().contains(param)) {
                        PARAM.nth(j).click();
                        page.keyboard().press("Escape");
                        break;
                    }
                }
            }
        }
    }

    public void saveWebhookSetup() {
        WEBHOOK_SAVE_BUTTON.click();
    }

    public String verifyWebhookCreationIsSuccess() {
        String alert = WEBHOOK_SUCCESS_ALERT.innerText().trim();
        WEBHOOK_SUCCESS_ALERT.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        return alert;
    }

    public String verifyInlineErrorMessage(String invalidData) {
        String error = " ";
        URL_TEXTAREA.fill(invalidData);
        if (BODY_TEXTAREA.isVisible()) BODY_TEXTAREA.fill(invalidData);
        WEBHOOK_SAVE_BUTTON.click();
        INLINE_ERROR_MESSAGE.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        if (INLINE_ERROR_MESSAGE.count() > 0) {
            for (int i = 0; i < INLINE_ERROR_MESSAGE.count(); i++) {
                error = error + INLINE_ERROR_MESSAGE.nth(i).innerText();
            }
        }
        return error.trim();
    }

    public String verifyErrorMsgWhenAPIFailed(List<String> mediaTypeList) throws IOException {
        String alert = " ";
        URL_TEXTAREA.fill(mediaTypeList.get(0));
        if (BODY_TEXTAREA.isVisible()) {
            String file = mediaTypeList.get(1).trim();
            BODY_TEXTAREA.fill(CommonUtils.readJsonTestDataFile(file));
        }
        WEBHOOK_SAVE_BUTTON.click();
        alert = WEBHOOK_ERROR_ALERT.innerText();
        waitUtility.waitForLocatorHidden(WEBHOOK_ERROR_ALERT);
        return alert;
    }

    public String verifyMacrosAppendedToBody() {
        return BODY_TEXTAREA.inputValue();
    }

    public String checkBackgroundColorOfWebhookIcon() {
        return WEBHOOK_ICON
                .evaluate("element => getComputedStyle(element).backgroundColor")
                .toString();
    }

    public void goToWorkspaceList() {
        BACK_ARROW.click();
        BEFORE_YOU_LEAVE_DIALOG.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        EXIT_BUTTON.click();
        CREATE_WORKSPACE.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void clickDownloadNPI() {
        DOWNLOAD_NPI_BUTTON.click();
    }

    public void selectFileExtension(String fileExtension) {
        WORKSPACE_FRAME.locator("ds-radio").filter(new Locator.FilterOptions().setHasText(fileExtension)).click();;
    }

    public Path clickDownloadButton() throws IOException {
        Download download = page.waitForDownload(DOWNLOAD_BUTTON::click);
        waitUtility.waitForLocatorVisible(DOWNLOAD_SUCCESS_ALERT);
        return CommonUtils.downloadFileAndMoveToSystemFolder(download);
    }

    public String checkNPIDownloadComplete() {
        String text = DOWNLOAD_SUCCESS_ALERT.innerText();
        waitUtility.waitForLocatorHidden(DOWNLOAD_SUCCESS_ALERT);
        return text;
    }

    public String fetchIdentifiedNPICount() {
        waitUtility.waitForLocatorVisible(IDENTIFIED_NPI_COUNT);
        return IDENTIFIED_NPI_COUNT.textContent().replace(",", "");
    }

    public String checkBackgroundColorOfDownloadIcon() {
        return FLY_PAGE_BUTTON
                .evaluate("element => getComputedStyle(element).backgroundColor")
                .toString();
    }

    public boolean isRetrofitCheckboxSelected() {
        if ("false".equals(RETROFIT_CHECKBOX.getAttribute("aria-checked"))) {
            RETROFIT_CHECKBOX.click();
        }
        return "true".equals(RETROFIT_CHECKBOX.getAttribute("aria-checked"));
    }

    public void clickNPIRetentionOption(String option) {
        Locator retentionXpath = WORKSPACE_FRAME.locator(String.format("//button[contains(@value,'%s')]", option));
        retentionXpath.click();
    }

    public void clickPublishedButton() {
        PUBLISHED_NPI.click();
    }

    public boolean verifyListTypeAfterPublished(String listType) {
        return WORKSPACE_FRAME
                .locator(String.format("//p[contains(text(),'%s')]", listType))
                .isVisible();
    }

    public String verifyNPIsEngagingText() {
        return NPI_ENGAGING_TEXT.innerText().trim();
    }

    public String fetchNPIListPublishAlertDisplayed() {
        try {
            String text = NPI_PUBLISH_ALERT.innerText();
            waitUtility.waitForLocatorHidden(NPI_PUBLISH_ALERT);
            return text;
        } catch (PlaywrightException e) {
            return "";
        }
    }

    public void waitForDashboardLoad(String workspaceType) {
        if (workspaceType.contains("Brand Explorer")) {
            brandExplorerWorkspace.waitForDashboardLoad();
        } else {
            explorerWorkspace.waitForDashboardLoad();
        }
    }
}
