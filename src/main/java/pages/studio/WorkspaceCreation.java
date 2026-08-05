package pages.studio;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.Collections;
import java.util.List;
import utils.CommonUtils;
import utils.ConfigReader;
import utils.WaitUtility;

public class WorkspaceCreation {

    private final Page page;
    private final Locator CREATE_WORKSPACE;
    private final Locator HCP_EXPLORER;
    private final Locator HCP_EXPANSION;
    private final Locator BRAND_EXPLORER;
    private final Locator DTC_EXPLORER;
    private final Locator BACK_TO_WORKSPACE_DASHBOARD;
    private final Locator WORKSPACE_CREATED_ALERT;
    private final Locator MENU_ICON;
    private final FrameLocator WORKSPACE_FRAME;
    private final Locator WORKSPACE_TYPE_TITLE;
    private final Locator MORE_ACTION_DIALOG;
    private final Locator DELETE_BUTTON;
    private final Locator REMOVAL_CONFIRMATION_POPUP;
    private final Locator REMOVAL_CONFIRMATION_TEXT;
    private final Locator REMOVE_BUTTON;
    private final Locator WORKSPACE_ARCHIVAL_ALERT;
    private final Locator OUTER_FRAME;
    private final Locator RENAME_BUTTON;
    private final Locator RENAME_WORKSPACE_POPUP;
    private final Locator UPDATE_BUTTON;
    private final Locator RENAME_WORKSPACE_ALERT;
    private final Locator SEARCH_WORKSPACE;
    private final Locator DUPLICATE_BUTTON;
    private final Locator DUPLICATE_WORKSPACE_POPUP;
    private final Locator DUPLICATE_BUTTON_FROM_POPUP;
    private final Locator DUPLICATE_WORKSPACE_ALERT;
    private final Locator DUPLICATE_WORKSPACE_NAME;
    private final Locator DASHBOARD_RELOAD_ICON;
    private final Locator PAGINATION;
    private final Locator DEPENDENT_WORKSPACE_TEXT;
    private final Locator REMOVE_WORKSPACE_BUTTON;
    private final Locator DELETE_WORKSPACE_ERROR_TEXT;
    private final Locator WORKSPACE_TYPE_LIST;
    private final Locator WORKSPACE_ADVERTISER_DROPDOWN;
    private final Locator WORKSPACE_TYPE;
    private final Locator WORKSPACE_CREATED_BY_DROPDOWN;
    private final Locator DROPDOWN_LIST_ITEMS;
    private final Locator GET_WORKSPACE_NAME_FROM_DASHBOARD;
    private final Locator BACK_ARROW;
    private final Locator AI_PANEL;
    private final Locator AI_PANEL_CLOSE_BUTTON;
    private final Locator ABSENT_WORKSPACE;
    WaitUtility waitUtility;
    int counter = 0;

    public WorkspaceCreation(Page page) {
        this.page = page;
        this.waitUtility = new WaitUtility(page);
        this.WORKSPACE_FRAME = page.frameLocator("iframe").frameLocator("iframe");
        this.CREATE_WORKSPACE = WORKSPACE_FRAME.locator(
                "//div[text()='Create New Workspace' or contains(text(),'Open New Workspace')]");
        this.HCP_EXPLORER = WORKSPACE_FRAME.locator("//ds-typography[contains(text(),'HCP Explorer')]");
        this.HCP_EXPANSION = WORKSPACE_FRAME.locator("//ds-typography[contains(text(),'HCP Audience Expansion')]");
        this.BRAND_EXPLORER = WORKSPACE_FRAME.locator("//ds-typography[contains(text(),'Brand Explorer')]");
        this.DTC_EXPLORER = WORKSPACE_FRAME.locator("//ds-typography[contains(text(),'DTC Explorer')]");
        this.BACK_TO_WORKSPACE_DASHBOARD = WORKSPACE_FRAME.getByRole(AriaRole.BUTTON);
        this.WORKSPACE_CREATED_ALERT = WORKSPACE_FRAME.locator(
                "//p[contains(text(),'Workspace created successfully') or contains(text(),'Workspace saved successfully')]");
        this.MENU_ICON = page.locator("//img[contains(@class,'menu-icon')]");
        this.WORKSPACE_TYPE_TITLE = WORKSPACE_FRAME.locator("//ds-typography[text()='Workspace Type']");
        this.MORE_ACTION_DIALOG = WORKSPACE_FRAME.locator("//div[@role='dialog']");
        this.DELETE_BUTTON = WORKSPACE_FRAME.locator("//div[contains(text(),'Delete')]");
        this.REMOVAL_CONFIRMATION_POPUP = WORKSPACE_FRAME.locator("//h3[contains(text(),'Removal Confirmation')]");
        this.REMOVAL_CONFIRMATION_TEXT =
                WORKSPACE_FRAME.locator("//div[contains(text(),'You are trying to delete the workspace')]");
        this.REMOVE_BUTTON = WORKSPACE_FRAME.locator("//div[text()='Remove']");
        this.WORKSPACE_ARCHIVAL_ALERT =
                WORKSPACE_FRAME.locator("//p[contains(text(),'Workspace deleted successfully')]");
        this.OUTER_FRAME = page.frameLocator("iframe").locator("//section[@id='main-content']");
        this.RENAME_BUTTON = WORKSPACE_FRAME.locator("//div[contains(text(),'Rename')]");
        this.RENAME_WORKSPACE_POPUP = WORKSPACE_FRAME.locator("//h3[contains(text(),'Rename Workspace')]");
        this.UPDATE_BUTTON = WORKSPACE_FRAME.locator("//div[contains(text(),'Update')]");
        this.RENAME_WORKSPACE_ALERT = WORKSPACE_FRAME.locator("//p[contains(text(),'Workspace renamed successfully')]");
        this.SEARCH_WORKSPACE = WORKSPACE_FRAME.locator("//input[contains(@placeholder,'Search')]");
        this.DUPLICATE_BUTTON = WORKSPACE_FRAME.locator("//div[contains(text(),'Duplicate')]");
        this.DUPLICATE_WORKSPACE_POPUP = WORKSPACE_FRAME.locator("//h3[contains(text(),'Duplicate Workspace')]");
        this.DUPLICATE_BUTTON_FROM_POPUP = WORKSPACE_FRAME.locator(
                "//h3[contains(text(),'Duplicate Workspace')]/parent::header/following-sibling::footer//div[contains(text(),'Duplicate')]");
        this.DUPLICATE_WORKSPACE_ALERT =
                WORKSPACE_FRAME.locator("//p[contains(text(),'Workspace duplicated successfully')]");
        this.DUPLICATE_WORKSPACE_NAME = WORKSPACE_FRAME.locator("//span/b[starts-with(text(), 'Copy of')]");
        this.DASHBOARD_RELOAD_ICON = WORKSPACE_FRAME
                .locator("#extension-root iframe")
                .contentFrame()
                .locator("//div[contains(text(),'Reload')]");
        this.PAGINATION = WORKSPACE_FRAME.locator(
                "//table[@data-tour-id='workspaces-table']/parent::div/following-sibling::div");
        this.DEPENDENT_WORKSPACE_TEXT =
                WORKSPACE_FRAME.locator("//div[contains(text(),'Checking for dependent workspaces...')]");
        this.REMOVE_WORKSPACE_BUTTON = WORKSPACE_FRAME.locator("//button/div[text()='Remove']");
        this.DELETE_WORKSPACE_ERROR_TEXT = WORKSPACE_FRAME.locator(
                "//p[contains(text(),\"Deletion blocked by Life. Message: This list can't be deleted\")]");
        this.WORKSPACE_TYPE_LIST = WORKSPACE_FRAME.locator(
                "//p[contains(text(),'Workspace Type')]/following-sibling::div//p[not(@color)]");
        this.WORKSPACE_ADVERTISER_DROPDOWN =
                WORKSPACE_FRAME.locator("//div[@data-tour-id='workspaces-advertiser-filter']//input");
        this.WORKSPACE_TYPE = WORKSPACE_FRAME.locator(
                "//div[@data-tour-id='workspaces-types-filter']//div[@role='img']//following-sibling::span");
        this.WORKSPACE_CREATED_BY_DROPDOWN =
                WORKSPACE_FRAME.locator("//div[@data-tour-id='workspaces-created-by-filter']//input");
        this.DROPDOWN_LIST_ITEMS = WORKSPACE_FRAME.locator("//div[@role='dialog']//li//span");
        this.GET_WORKSPACE_NAME_FROM_DASHBOARD =
                WORKSPACE_FRAME.locator("//td[@role='gridcell' and contains(@id,'workspace_name')]//span");
        this.BACK_ARROW = WORKSPACE_FRAME.locator("//button[@color='textPrimary']");
        this.AI_PANEL = page.locator("//div[@class='ai-assistant-panel open']");
        this.AI_PANEL_CLOSE_BUTTON =
                page.locator("//button[@aria-label='Close AI Assistant' and @class='ai-icon-btn']");
        this.ABSENT_WORKSPACE = WORKSPACE_FRAME.locator("//p[text()='Nothing Found...']");
    }

    public String studioDashboard() {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        return this.page.title();
    }

    public List<String> fetchWorkspaceTypes() {
        return WORKSPACE_TYPE_LIST.allTextContents();
    }

    public String verifyHCPExplorer() {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        return HCP_EXPLORER.innerText();
    }

    public String verifyBrandExplorer() {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        return BRAND_EXPLORER.innerText();
    }

    public String verifyDTCExplorer() {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        return DTC_EXPLORER.innerText();
    }

    public String verifyHCPAudienceExpansion() {
        return HCP_EXPANSION.innerText();
    }

    public void clickHCPExplorerWorkspace() {
        HCP_EXPLORER.click();
    }

    public void clickBrandExplorerWorkspace() {
        BRAND_EXPLORER.click();
    }

    public void clickHCPAudienceExpansionWorkspace() {
        HCP_EXPANSION.click();
    }

    public void clickDTCExplorerWorkspace() {
        DTC_EXPLORER.click();
    }

    public String isWorkspaceCreationAlertDisplayed() {
        String text = WORKSPACE_CREATED_ALERT.innerText();
        waitUtility.waitForLocatorHidden(WORKSPACE_CREATED_ALERT);
        return text;
    }

    public void createWorkspace() {
        while (counter < 3) {
            retryCreateWorkspace(false);
        }
    }

    public void clickCreateStudioWorkspace() {
        CREATE_WORKSPACE.first().click();
        waitUtility.waitForLocatorVisible(WORKSPACE_TYPE_TITLE);
    }

    public void verifyStudioWorkspaceFrame() {
        OUTER_FRAME.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        CREATE_WORKSPACE.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        waitForStudioWorkspacePage(CREATE_WORKSPACE);
    }

    private void waitForStudioWorkspacePage(Locator locator) {
        long startTime = System.currentTimeMillis();
        long timeout = Long.parseLong(ConfigReader.getProperty("timeout"));
        while (System.currentTimeMillis() - startTime < timeout) {
            if (locator.count() > 0 && locator.first().isVisible()) {
                break;
            }
            MENU_ICON.click();
            page.keyboard().press("Escape");
        }
    }

    public void retryCreateWorkspace(boolean clickFlag) {
        {
            page.waitForTimeout(5000);
            CREATE_WORKSPACE.click();
            page.waitForLoadState();
            if (HCP_EXPANSION.isVisible()) {
                page.waitForLoadState();
                counter = 4;
                //                if (clickFlag) {
                //                    HCP_EXPLORER.click();
                //                }
            } else {
                counter++;
                BACK_TO_WORKSPACE_DASHBOARD.click();
                page.waitForLoadState();
            }
        }
    }

    public void clickMoreActionsMenu(String workspaceName) {
        waitUtility.waitForLocatorVisible(
                WORKSPACE_FRAME.locator(String.format("//ds-typography[contains(text(),'%s')]", workspaceName)));
        WORKSPACE_FRAME
                .locator(String.format("//td[contains(@id,'%s')]//button", workspaceName))
                .first()
                .click();
    }

    public void deleteWorkspace() {
        MORE_ACTION_DIALOG.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        DELETE_BUTTON.scrollIntoViewIfNeeded();
        DELETE_BUTTON.click();
        REMOVAL_CONFIRMATION_POPUP.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void performActionOnWorkspace(String actionName) {
        MORE_ACTION_DIALOG.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        switch (actionName) {
            case "Duplicate":
                DUPLICATE_BUTTON.scrollIntoViewIfNeeded();
                DUPLICATE_BUTTON.click();
                DUPLICATE_WORKSPACE_POPUP.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                break;
            case "Delete":
                DELETE_BUTTON.scrollIntoViewIfNeeded();
                DELETE_BUTTON.click();
                REMOVAL_CONFIRMATION_POPUP.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                break;
            case "Rename":
                RENAME_BUTTON.scrollIntoViewIfNeeded();
                RENAME_BUTTON.click();
                RENAME_WORKSPACE_POPUP.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                break;
        }
    }

    public String verifyDeletePopUp() {
        waitUtility.waitForLocatorHidden(DEPENDENT_WORKSPACE_TEXT);
        return REMOVAL_CONFIRMATION_TEXT.innerText();
    }

    public String deleteWorkspaceWithActiveWebhook() {
        String text = " ";
        REMOVE_BUTTON.click();
        text = WORKSPACE_ARCHIVAL_ALERT.innerText();
        WORKSPACE_ARCHIVAL_ALERT.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        return text;
    }

    public String renameWorkspaceName(String oldWorkspaceName, String newWorkspace) {
        WORKSPACE_FRAME
                .locator(String.format(
                        "//h3[text()='Rename Workspace']/parent::header/following-sibling::div//input[@value='%s']",
                        oldWorkspaceName))
                .fill(newWorkspace);
        if (!UPDATE_BUTTON.isEnabled()) page.waitForTimeout(2000);
        UPDATE_BUTTON.click();
        String text = RENAME_WORKSPACE_ALERT.innerText();
        waitUtility.waitForLocatorHidden(RENAME_WORKSPACE_ALERT);
        return text;
    }

    public boolean searchWorkspaceName(String workspaceName) {
        waitUtility.waitForLocatorVisible(SEARCH_WORKSPACE);
        page.waitForCondition(() -> SEARCH_WORKSPACE.filter().count() == 1);
        if (WORKSPACE_FRAME
                .locator(String.format("//ds-typography[contains(text(),'%s')]", workspaceName))
                .isVisible()) {
            return true;
        } else {
            SEARCH_WORKSPACE.fill(workspaceName);
            page.keyboard().press("Enter");
            waitUtility.waitForLocatorVisible(PAGINATION.first());
            waitUtility.waitForLocatorVisible(
                    WORKSPACE_FRAME.locator(String.format("//ds-typography[contains(text(),'%s')]", workspaceName)));
            return WORKSPACE_FRAME
                    .locator(String.format("//ds-typography[contains(text(),'%s')]", workspaceName))
                    .isVisible();
        }
    }

    public String fetchDuplicateWorkspaceName() {
        return DUPLICATE_WORKSPACE_NAME.innerText();
    }

    public String clickDuplicateButton() {
        DUPLICATE_BUTTON_FROM_POPUP.click();
        String text = DUPLICATE_WORKSPACE_ALERT.innerText();
        waitUtility.waitForLocatorHidden(DUPLICATE_WORKSPACE_ALERT);
        return text;
    }

    public void clickWorkspace(String workspaceName) {
        WORKSPACE_FRAME
                .locator(String.format("//ds-typography[contains(text(),'%s')]", workspaceName))
                .click();
    }

    public boolean navigateToWorkspace(String workspace) {
        DASHBOARD_RELOAD_ICON.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        return WORKSPACE_FRAME
                .locator(String.format("//h1[contains(text(),'%s')]", workspace))
                .isVisible();
    }

    public void selectMoreActionsMenu(String workspaceName) {
        Locator moreActionsButton = WORKSPACE_FRAME
                .locator(String.format("//td[contains(@id,'%s')]//button", workspaceName))
                .first();
        waitUtility.waitForLocatorVisible(moreActionsButton);
        moreActionsButton.click();
    }

    public void clickRemoveWorkspaceButton() {
        REMOVE_WORKSPACE_BUTTON.click();
    }

    public String verifyDeleteWorkspaceErrorMessage() {
        waitUtility.waitForLocatorVisible(REMOVAL_CONFIRMATION_POPUP);
        waitUtility.waitForLocatorVisible(DELETE_WORKSPACE_ERROR_TEXT);
        String errorText = DELETE_WORKSPACE_ERROR_TEXT.innerText();
        waitUtility.waitForLocatorHidden(REMOVAL_CONFIRMATION_POPUP);
        return errorText;
    }

    public void selectWorkspaceType(String workspaceType) {
        waitForStudioWorkspacePage(WORKSPACE_TYPE);
        waitUtility.waitForLocatorVisible(WORKSPACE_TYPE.last());
        CommonUtils.selectAndClickElement(WORKSPACE_TYPE, Collections.singletonList(workspaceType));
        waitUtility.waitForLocatorVisible(PAGINATION.first());
    }

    public void filterByWorkspaceTypeAndOpen(String workspaceType, String workspaceName) {
        waitForStudioWorkspacePage(WORKSPACE_TYPE);
        waitUtility.waitForLocatorVisible(WORKSPACE_TYPE.last());
        CommonUtils.selectAndClickElement(WORKSPACE_TYPE, Collections.singletonList(workspaceType));
        Locator workspaceRow = WORKSPACE_FRAME.locator(String.format("//span[contains(text(),'%s')]", workspaceName));
        waitUtility.waitForLocatorVisible(workspaceRow);
        workspaceRow.click();
    }

    public void selectWorkspaceAdvertiser(String advertiser) {
        WORKSPACE_ADVERTISER_DROPDOWN.click();
        waitUtility.waitForLocatorVisible(DROPDOWN_LIST_ITEMS.locator("text = " + advertiser));
        DROPDOWN_LIST_ITEMS.locator("text=" + advertiser).click();
        page.keyboard().press("Escape");
        waitUtility.waitForLocatorVisible(PAGINATION.first());
    }

    public void selectWorkspaceCreatedBy(String createdBy) {
        WORKSPACE_CREATED_BY_DROPDOWN.click();
        waitUtility.waitForLocatorVisible(DROPDOWN_LIST_ITEMS.locator("text = " + createdBy));
        DROPDOWN_LIST_ITEMS.locator("text=" + createdBy).click();
        page.keyboard().press("Escape");
        waitUtility.waitForLocatorVisible(PAGINATION.first());
    }

    public String fetchWorkspaceNameFromDashboard() {
        return GET_WORKSPACE_NAME_FROM_DASHBOARD.first().textContent().trim();
    }

    public void searchByWorkspaceName(String workspaceName) {
        SEARCH_WORKSPACE.fill(workspaceName);
        page.keyboard().press("Enter");
    }

    public void isWorkspacePresent(String workspaceName) {
        waitUtility.waitForLocatorVisible(PAGINATION.first());
        waitUtility.waitForLocatorVisible(
                WORKSPACE_FRAME.locator(String.format("//span[contains(text(),'%s')]", workspaceName)));
    }

    public void isWorkspaceAbsent() {
        waitUtility.waitForLocatorVisible(ABSENT_WORKSPACE);
    }

    public void clickBackArrowFromCreateNewWorkspace() {
        BACK_ARROW.click();
        CREATE_WORKSPACE.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        waitUtility.waitForLocatorVisible(PAGINATION.first());
    }

    public String getSelectedWorkspaceType() {
        Locator locator = WORKSPACE_TYPE.locator("xpath=/ancestor::label/preceding-sibling::div//input");
        for (int i = 0; i < WORKSPACE_TYPE.count(); i++) {
            if (locator.nth(i).getAttribute("aria-checked").equals("true")) {
                return WORKSPACE_TYPE.nth(i).innerText();
            }
        }
        return "";
    }

    public String getSelectedWorkspaceAdvertiser() {
        return WORKSPACE_ADVERTISER_DROPDOWN.getAttribute("value").trim();
    }

    public String getSelectedWorkspaceCreatedBy() {
        return WORKSPACE_CREATED_BY_DROPDOWN.getAttribute("value").trim();
    }

    public String getSearchedWorkspaceName() {
        return SEARCH_WORKSPACE.getAttribute("value").trim();
    }

    public void refreshPage() {
        page.reload();
        waitUtility.waitForLocatorVisible(CREATE_WORKSPACE.first());
        waitUtility.waitForLocatorVisible(PAGINATION.first());
    }

    public void closeAIPanel() {
        page.waitForTimeout(5000);
        if (AI_PANEL.isVisible() && AI_PANEL_CLOSE_BUTTON.isVisible()) {
            AI_PANEL_CLOSE_BUTTON.click();
            waitUtility.waitForLocatorDetached(AI_PANEL);
        }
    }

    public boolean isWorkspaceVisible(String workspaceName, String draftOption) {
        if (draftOption.equalsIgnoreCase("Public")) {
            return WORKSPACE_FRAME
                    .locator(String.format("//span[contains(text(),'%s')]", workspaceName))
                    .isVisible();
        } else {
            return ABSENT_WORKSPACE.isVisible();
        }
    }
}
