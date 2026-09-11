package pages.admin;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.ArrayList;
import java.util.List;
import factory.DriverFactory;
import pages.life.Campaigns;
import utils.WaitUtility;

public class Accounts {
    private final Page page;
    private final Locator SUB_MENU;
    private final Locator ADMINISTRATION;
    private final Locator ACCOUNTS_TAB;
    private final Locator SEARCH_ACCOUNT;
    private final Locator SEARCH_ICON;
    private final Locator SELECT_ACCOUNT;
    private final Locator STUDIO_TOGGLE_BUTTON;
    private final Locator EXPANSION_TOGGLE;
    private final Locator STUDIO_SETTINGS_SAVE;
    private final Locator STUDIO_MENU;
    private final Locator DISABLE_STUDIO_OK_BUTTON;
    private final Locator PULSEPOINT_ICON;
    private final Locator SWITCH_ACCOUNT;
    private final Locator SWITCH_SEARCH_ACCOUNT;
    private final Locator SWITCH_CLICK_ACCOUNT;
    private final Locator WORKSPACE_NAME;
    private final Locator ACCOUNTS_TAB_TEXT;
    private final Locator ADVERTISER_TAB;
    private final Locator ACCOUNT_DROPDOWN;
    private final Locator ACCOUNT_DROPDOWN_TEXTAREA;
    private final Locator SEARCH_BUTTON;
    private final Locator ADVERTISER_LIST;
    private final Locator REPORTING_TAB;
    private final Locator CUSTOM_DESTINATION_SECTION;
    private final Locator ADD_DESTINATION_BUTTON;
    private final Locator ENTER_DESTINATION_NAME;
    private final Locator DESTINATION_TYPE_DROPDOWN;
    private final Locator HOSTNAME;
    private final Locator USERNAME;
    private final Locator PASSWORD;
    private final Locator PORT;
    private final Locator TEST_CONNECTION_LINK;
    private final Locator CONNECTION_CONFIRMATION_TEXT;
    private final Locator OK_BUTTON;
    private final Locator SELECT_USER_TAB;
    private final Locator STUDIO_USER_TAB;
    private final Locator STUDIO_TOGGLE_EXTERNAL_USER_ENABLED;
    private final Locator STUDIO_TOGGLE_EXTERNAL_USER_DISABLED;
    private final Locator ACCOUNT_ADVERTISER_TAB;
    private final Locator GLOBAL_SIGNALS_TAB;
    private final Locator ADVERTISER_PERMISSION_SAVE_BUTTON;
    private final Locator ACCOUNT_USER_TAB;
    private final Locator USER_SIGNAL_TAB;
    private final Locator USER_PERMISSIONS_SAVE_BUTTON;
    private final Locator USER_PROFILE_ICON;
    private final Locator LOGOUT_BUTTON;
    private final Locator MOMENTS_CHECKBOX;
    private final Locator IBHEALTH_CHECKBOX;
    private final Locator CLAIMSDATA_CHECKBOX;
    private final Locator ACCOUNTS_ADVERTISER_TAB;
    private final Locator SUCCESS_ALERT;
    private final Locator CLIENT_VALUE;
    private final Locator STUDIO_SETTINGS_ICON;
    private final Locator STUDIO_SETTINGS_PANEL;
    private final Locator WORKSPACE_PERMISSION_TOGGLE_BUTTON;
    private final Locator STUDIO_SETTINGS_PANEL_CANCEL_BUTTON;
    private final Locator ERROR_ALERT;
    private final Locator ALERT;
    private final Locator EXPORT_OPTIONS_DIALOG;
    private final Locator RUN_BUTTON;
    private final Locator TEST_ACCESS_FAILED_TEXT;
    private final Locator STUDIO_TOGGLE_ACTIVE;
    private final Locator DETAILS_TAB;
    private final Locator LIFE_SETTINGS;
    private final Locator PULSEPOINT_DATA_FEE;
    private final Locator NPI_TARGETING_GROSS_CPM;
    private final Locator LIFE_SETTINGS_PANEL_CANCEL_BUTTON;
    private final Locator CUSTOM_FIELD_DELETION_POP_UP;
    private final Locator CUSTOM_FIELD_CANNOT_DELETE_POPUP;
    private final Locator CUSTOM_FIELD_DELETE_BUTTON;
    private final Locator CAMPAIGN_LINK_FROM_CUSTOM_FIELD_POPUP;
    private final Locator CUSTOM_FIELD_REMOVAL_POPUP;
    private final Locator CUSTOM_DESTINATION_ROW;
    private final Locator CONNECTING_TEXT;
    WaitUtility waitUtility;

    public Accounts(Page page) {
        this.page = page;
        this.waitUtility = new WaitUtility(page);
        this.SUB_MENU = page.locator("span").first();
        this.ADMINISTRATION = page.getByText("Administration", new Page.GetByTextOptions().setExact(true));
        this.ACCOUNTS_TAB = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Accounts"));
        this.SEARCH_ACCOUNT = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Search"));
        this.SEARCH_ICON = page.locator(".ui > .iconSprite");
        this.SELECT_ACCOUNT = page.getByRole(AriaRole.CELL, new Page.GetByRoleOptions().setName("ACCOUNT_NAME"));
        this.SELECT_USER_TAB = page.locator("//a[@routerlink='users']");
        this.STUDIO_USER_TAB = page.locator("//button[.//span[text()='Studio']]");
        this.STUDIO_TOGGLE_EXTERNAL_USER_ENABLED = page.locator("//span[contains(@class,'icons-24-checkmark-green')]");
        this.STUDIO_TOGGLE_EXTERNAL_USER_DISABLED = page.locator("//span[contains(@class,'icons-24-checkmark-white')]");
        this.STUDIO_TOGGLE_BUTTON =
                page.locator("//span[@class='btn-label' and text()='Studio']/following-sibling::span//span");
        this.EXPANSION_TOGGLE =
                page.locator("tr:nth-child(3) > td:nth-child(2) > .toggle-wrapper-withLabel > .toggle > label");
        this.STUDIO_SETTINGS_SAVE = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));
        this.STUDIO_MENU = page.getByText("Studio").nth(4);
        this.DISABLE_STUDIO_OK_BUTTON = page.locator("//div[contains(@class,'approveButtonText')]");
        this.PULSEPOINT_ICON = page.locator("//app-buyer-logo/div[@class='logo-holder']");
        this.SWITCH_ACCOUNT = page.locator(".left > div:nth-child(2)").first();
        this.SWITCH_SEARCH_ACCOUNT = page.getByPlaceholder("Search");
        this.SWITCH_CLICK_ACCOUNT = page.locator("#accountSwitcher").getByText("100Plus");
        this.WORKSPACE_NAME = page.locator("iframe[title=\"overview\"]")
                .contentFrame()
                .locator("iframe")
                .contentFrame()
                .getByText("HCP Audience Expansion");
        this.ACCOUNTS_TAB_TEXT = page.locator("//span[text()='Account Management']");
        this.ADVERTISER_TAB = page.locator("//a[@routerlink='/advertisers/list']");
        this.ACCOUNT_DROPDOWN = page.locator("//app-single-select-dropdown[@placeholder='Any Account']");
        this.ACCOUNT_DROPDOWN_TEXTAREA = page.locator("//input[@placeholder='Any Account']");
        this.SEARCH_BUTTON = page.locator("//span[text()='Search']");
        this.ADVERTISER_LIST = page.locator("//td[contains(@class,'gaTableRow')]//div");
        this.REPORTING_TAB = page.locator("//a[@routerlink='reporting']");
        this.DETAILS_TAB = page.locator("//a[@routerlink='details']");
        this.CUSTOM_DESTINATION_SECTION = page.locator("//div[@id='custom-destinations']");
        this.ADD_DESTINATION_BUTTON = page.locator("//app-icon-lable-link[@text='Add Destination']//div");
        this.ENTER_DESTINATION_NAME = page.locator("//input[@placeholder='Enter Destination Name']");
        this.DESTINATION_TYPE_DROPDOWN = page.locator("//label[text()='Destination Type']/following-sibling::select");
        this.HOSTNAME = page.locator("//input[@placeholder='Enter Host Name']");
        this.USERNAME = page.locator("//input[@placeholder='Enter User Name']");
        this.PASSWORD = page.locator("//input[contains(@placeholder,'Password')]");
        this.PORT = page.locator("//input[@placeholder='Enter Port Number']");
        this.TEST_CONNECTION_LINK = page.locator("//span[text()='Test Connection' or text()='Test Access']");
        this.CONNECTION_CONFIRMATION_TEXT = page.locator(
                "//app-icon-lable-link[@text='Connection confirmed']/div | //span[contains(text(),'Access test successful')]");
        this.OK_BUTTON = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));
        this.ACCOUNT_ADVERTISER_TAB = page.locator("//a[@routerlink='advertisers']");
        this.GLOBAL_SIGNALS_TAB = page.locator("//button[@class='signal']");
        this.ADVERTISER_PERMISSION_SAVE_BUTTON = page.locator("//button[@class='ui primary button okButton']");
        this.ACCOUNT_USER_TAB = page.locator("//a[@routerlink='users']");
        this.USER_SIGNAL_TAB = page.locator("//button[normalize-space(.)='Signal']");
        this.USER_PERMISSIONS_SAVE_BUTTON = page.locator("//button[@class='ui primary button okButton']");
        this.USER_PROFILE_ICON = page.locator("//div[@class='accountname']");
        this.LOGOUT_BUTTON = page.locator("//div[text()='Sign Out'] | //span[text()='Sign Out']");
        this.MOMENTS_CHECKBOX = page.locator("//*[@id='44_0' and not(contains(@class, 'checked'))]");
        this.IBHEALTH_CHECKBOX = page.locator("//*[@id='45_0' and not(contains(@class, 'checked'))]");
        this.CLAIMSDATA_CHECKBOX = page.locator("//*[@id='43_0' and not(contains(@class, 'checked'))]");
        this.ACCOUNTS_ADVERTISER_TAB = page.locator("//a[@routerlink='advertisers']");
        this.SUCCESS_ALERT =
                page.locator("//div[@role='alert' and contains(text(),'Advertisers updated successfully')]");
        this.CLIENT_VALUE = page.locator("//app-single-select-dropdown[@placeholder='Client']//span");
        this.STUDIO_SETTINGS_ICON =
                page.locator("//span[@class='header-name' and text()='Studio']/following-sibling::span");
        this.STUDIO_SETTINGS_PANEL =
                page.locator("//div[@class='bsHeaderContainer']//div[contains(text(),'Studio Settings')]");
        this.WORKSPACE_PERMISSION_TOGGLE_BUTTON = page.locator(
                "//div[@class='secondtablewrapper']//div[contains(@class,'toggle-wrapper-withLabel')]//sui-checkbox");
        this.STUDIO_SETTINGS_PANEL_CANCEL_BUTTON = page.locator(
                "//app-genomestudio-workspace//button[contains(@class,'cancelbtn') and contains(text(),'Cancel')]");
        this.LIFE_SETTINGS_PANEL_CANCEL_BUTTON =
                page.locator("//app-account-fee//button[contains(@class,'cancelbtn') and contains(text(),'Cancel')]");
        this.ERROR_ALERT = page.locator("//div[@aria-label='Error while saving.']");
        this.ALERT = page.locator("//div[@role='alert']");
        this.EXPORT_OPTIONS_DIALOG = page.locator("//div[text()='Choose file size to test access']");
        this.RUN_BUTTON = page.locator("//app-ds-button-wrapper[@label='Run']");
        this.TEST_ACCESS_FAILED_TEXT = page.locator("//span[text()='Access Test Failed']");
        this.STUDIO_TOGGLE_ACTIVE = page.locator("//div[contains(@class,'button-active')]//span[text()='Studio']");
        this.LIFE_SETTINGS = page.locator("//span[text()='Life Platform']/following-sibling::span");
        this.PULSEPOINT_DATA_FEE = page.locator("//input[@name='ppDataMargin']");
        this.NPI_TARGETING_GROSS_CPM = page.locator("//input[@name='NPITargeting']");
        this.CUSTOM_FIELD_DELETION_POP_UP = page.locator("//div[contains(text(),'Delete Custom Field Settings')]");
        this.CUSTOM_FIELD_CANNOT_DELETE_POPUP = page.locator("//div[contains(text(),'Custom Field Can’t Be Removed')]");
        this.CUSTOM_FIELD_DELETE_BUTTON = page.locator("//div[contains(@class,'approveButtonText')]//span[contains(text(),'Delete')]");
        this.CAMPAIGN_LINK_FROM_CUSTOM_FIELD_POPUP = page.locator("//a[contains(@class, 'bullet_list')]");
        this.CUSTOM_FIELD_REMOVAL_POPUP = page.locator("//div[contains(@class,'confirm-modal header')]");
        this.CUSTOM_DESTINATION_ROW = page.locator("//div[@class='customDestination-row']");
        this.CONNECTING_TEXT = page.getByText("Connecting...");
    }

    public void clickAdministration() {
        waitUtility.waitForLocatorVisible(ADMINISTRATION);
        ADMINISTRATION.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void selectAccountsTab() {
        while (!ACCOUNTS_TAB_TEXT.isVisible()) {
            ACCOUNTS_TAB.click();
            waitUtility.waitUntilSpinnerHidden();
        }
    }

    public void searchAccount(String accountName) {
        SEARCH_ACCOUNT.fill(accountName);
        SEARCH_ICON.click();
        Locator selectAccount = page.locator(String.format("//div[@title='%s']", accountName));
        waitUtility.waitForLocatorVisible(selectAccount);
        selectAccount.click();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(CLIENT_VALUE);
    }

    public void enableStudio() {
        STUDIO_TOGGLE_BUTTON.click();
    }

    public Locator studioToggleActive() {
        return STUDIO_TOGGLE_ACTIVE;
    }

    public void workSpaceSettings() {
        EXPANSION_TOGGLE.click();
    }

    public void saveStudioSettings() {
        STUDIO_SETTINGS_SAVE.click();
    }

    public void disableStudioForAccount(String accountName) {
        ADMINISTRATION.click();
        ACCOUNTS_TAB.click();
        waitUtility.waitForLocatorVisible(ACCOUNTS_TAB_TEXT);
        SEARCH_ACCOUNT.fill(accountName);
        SEARCH_ICON.click();
        Locator selectAccount = page.getByRole(AriaRole.CELL, new Page.GetByRoleOptions().setName(accountName));
        selectAccount.click();
        STUDIO_TOGGLE_BUTTON.click();
        DISABLE_STUDIO_OK_BUTTON.click();
        page.reload();
    }

    public void switchAccount(String accountName) {
        SWITCH_ACCOUNT.click();
        SWITCH_SEARCH_ACCOUNT.fill(accountName);
        SWITCH_CLICK_ACCOUNT.click();
    }

    public String verifyWorkspacePermission() {
        WORKSPACE_NAME.waitFor();
        return WORKSPACE_NAME.textContent();
    }

    public void verifyStudioMenu() {
        PULSEPOINT_ICON.click();
        SUB_MENU.click();
        assertThat(STUDIO_MENU).isHidden();
    }

    public void clickAdvertiserTab() {
        waitUtility.waitForLocatorVisible(ADVERTISER_TAB);
        ADVERTISER_TAB.click();
        waitUtility.waitForLocatorVisible(ADVERTISER_LIST.first());
    }

    public void selectAccount(String account) {
        ACCOUNT_DROPDOWN.click();
        ACCOUNT_DROPDOWN_TEXTAREA.fill(account);
        ACCOUNT_DROPDOWN.getByText(account).click();
        SEARCH_BUTTON.click();
    }

    public List<String> fetchAdvertiserList() {
        waitUtility.waitForLocatorVisible(ADVERTISER_LIST.last());
        return ADVERTISER_LIST.allInnerTexts();
    }

    public boolean isDetailsTabDisplayed() {
        return DETAILS_TAB.isVisible();
    }

    public void clickReportingTab() {
        REPORTING_TAB.click();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(CUSTOM_DESTINATION_SECTION);
    }

    public void clickAddDestination() {
        ADD_DESTINATION_BUTTON.click();
        waitUtility.waitForLocatorVisible(ENTER_DESTINATION_NAME.last());
    }

    public void enterDestinationName(String metricName) {
        ENTER_DESTINATION_NAME.last().fill(metricName);
    }

    public void selectDestinationType(String destinationType) {
        DESTINATION_TYPE_DROPDOWN.last().selectOption(destinationType);
    }

    public void enterHostName(String hostName) {
        HOSTNAME.last().fill(hostName);
    }

    public void enterPortName(String port) {
        PORT.last().fill(port);
    }

    public void enterUserName(String demoUser) {
        USERNAME.last().fill(demoUser);
    }

    public void enterPassword(String demoPassword) {
        PASSWORD.last().fill(demoPassword);
    }

    public String clickTestConnection() {
        TEST_CONNECTION_LINK.last().click();
        if (EXPORT_OPTIONS_DIALOG.isVisible()) RUN_BUTTON.click();
        waitUtility.waitForLocatorHidden(CONNECTING_TEXT);
        if (TEST_ACCESS_FAILED_TEXT.isVisible())
            return TEST_ACCESS_FAILED_TEXT.textContent().trim();
        else {
            waitUtility.waitForLocatorVisible(CONNECTION_CONFIRMATION_TEXT);
            return CONNECTION_CONFIRMATION_TEXT.textContent().trim();
        }
    }

    public boolean clickOKButton() {
        OK_BUTTON.click();
        waitUtility.waitForLocatorVisible(ALERT);
        return !ERROR_ALERT.isVisible();
    }

    public void isPulsePointIconEnabled() {
        while (!PULSEPOINT_ICON.isVisible() && !PULSEPOINT_ICON.isEnabled()) {
            page.waitForTimeout(5000);
        }
        page.waitForTimeout(
                10000); // needed this hard wait as page remains un-interactive even after element is visible
    }

    public void selectUserTab() {
        SELECT_USER_TAB.click();
        page.waitForLoadState();
    }

    public void selectExternalUser(String externalUser) {
        Locator searchUser = page.locator("(//input[@placeholder='Search'])[2]");
        waitUtility.waitForLocatorVisible(searchUser);
        searchUser.click();
        searchUser.fill(externalUser);
        SEARCH_ICON.click();
        Locator externalUserVisibility = page.locator(String.format("//div[contains(text(),'%s')]", externalUser));
        waitUtility.waitForLocatorVisible(externalUserVisibility);
        STUDIO_USER_TAB.click();
    }

    public boolean turnStudioToggleForExternalUser() {
        page.waitForLoadState();
        if (STUDIO_TOGGLE_EXTERNAL_USER_ENABLED.isVisible()) {
            return true;
        } else if (STUDIO_TOGGLE_EXTERNAL_USER_DISABLED.isVisible()) {
            STUDIO_TOGGLE_EXTERNAL_USER_DISABLED.click();
            waitUtility.waitForLocatorVisible(STUDIO_TOGGLE_EXTERNAL_USER_ENABLED);
            return STUDIO_TOGGLE_EXTERNAL_USER_ENABLED.isVisible();
        }
        return false;
    }

    public void clickAccountAdvertiserTab() {
        ACCOUNT_ADVERTISER_TAB.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void clickGlobalSignalsTab() {
        GLOBAL_SIGNALS_TAB.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void enableAdvertiserPermission(String advertiserName, String advertiserPermission) {
        Locator permissionCheckbox = page.locator(String.format(
                "//tr[td[normalize-space(.)='%s']]/td[position() = count(ancestor::table//th[normalize-space(.)='%s']/preceding-sibling::th) + 1]//sui-checkbox[not(contains(@class, 'checked'))]",
                advertiserName, advertiserPermission));
        switch (advertiserPermission) {
            case "MOMENTS", "IB HEALTH", "CLAIMS DATA":
                if (!permissionCheckbox.isHidden()) {
                    permissionCheckbox.click();
                    ADVERTISER_PERMISSION_SAVE_BUTTON.click();
                }
                break;
        }
        waitUtility.waitUntilSpinnerHidden();
    }

    public void navigateToUserTab() {
        ACCOUNT_USER_TAB.click();
        waitUtility.waitForLocatorVisible(USER_SIGNAL_TAB);
    }

    public void externalUserPermissions(String studioPermissions, String accountName) {
        USER_SIGNAL_TAB.click();
        switch (studioPermissions) {
            case "MOMENTS":
                if (!MOMENTS_CHECKBOX.isHidden()) {
                    MOMENTS_CHECKBOX.click();
                }
                break;
            case "IB HEALTH":
                if (!IBHEALTH_CHECKBOX.isHidden()) {
                    IBHEALTH_CHECKBOX.click();
                }
                break;
            case "CLAIMS DATA":
                if (!CLAIMSDATA_CHECKBOX.isHidden()) {
                    CLAIMSDATA_CHECKBOX.click();
                }
                break;
        }
        if (USER_PERMISSIONS_SAVE_BUTTON.isVisible()) {
            USER_PERMISSIONS_SAVE_BUTTON.click();
        }
    }

    public void internalUserLogout() {
        USER_PROFILE_ICON.click();
        LOGOUT_BUTTON.click();
    }

    public boolean isAccountsAdvertiserTabDisplayed() {
        return ACCOUNTS_ADVERTISER_TAB.isVisible();
    }

    public void clickAccountsAdvertiserTab() {
        ACCOUNTS_ADVERTISER_TAB.click();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForElementVisible("//div[@class='advertiser-tabs']");
    }

    public void clickAdvertisersSubTab(String tabName) {
        Locator tabXpath = page.locator(String.format(
                "//div[@class='advertiser-tabs']//button[contains(normalize-space(text()),'%s')]", tabName));
        tabXpath.click();
    }

    public boolean checkHCPPermissionForAdvertiser(String checkboxStatus, String advertiser) {
        boolean flag;
        Locator permissionXpath = page.locator(String.format(
                "//td[contains(normalize-space(text()),'%s')]/following-sibling::td[contains(@class,'hcp365Col')]//sui-checkbox",
                advertiser));
        Locator disabledTextXpath = page.locator(String.format(
                "//td[contains(normalize-space(text()),'%s')]/following-sibling::td//span[contains(@class,'disabled-text' )and contains(text(),'HCP365 is disabled for this Advertiser')]",
                advertiser));
        waitUtility.waitForLocatorVisible(permissionXpath);
        flag = switch (checkboxStatus) {
            case "Disabled" -> {
                if (!disabledTextXpath.isVisible()) {
                    permissionXpath.click();
                }
                yield true;
            }
            case "Enabled" -> {
                if (disabledTextXpath.isVisible()) {
                    permissionXpath.click();
                }
                yield true;
            }
            default -> false;
        };
        return flag;
    }

    public void saveAccountsAdvertiserTab() {
        if (OK_BUTTON.isVisible()) {
            OK_BUTTON.click();
            waitUtility.waitForLocatorHidden(SUCCESS_ALERT);
        }
    }

    public String fetchClientValue() {
        waitUtility.waitForLocatorVisible(CLIENT_VALUE);
        return CLIENT_VALUE.textContent().trim();
    }

    public boolean isStudioSettingsButtonVisible() {
        return STUDIO_SETTINGS_ICON.isVisible();
    }

    public void clickStudioSettingsButton() {
        STUDIO_SETTINGS_ICON.click();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(STUDIO_SETTINGS_PANEL);
    }

    public List<String> fetchWorkspacesWithPermission() {
        List<String> workspaceNameList = new ArrayList<>();
        for (int i = 0; i < WORKSPACE_PERMISSION_TOGGLE_BUTTON.count(); i++) {
            if (WORKSPACE_PERMISSION_TOGGLE_BUTTON.nth(i).getAttribute("class").contains("checked")) {
                int rowIndex = (int) WORKSPACE_PERMISSION_TOGGLE_BUTTON
                        .nth(i)
                        .evaluate("(el) => Array.from(el.closest('tbody').children).indexOf(el.closest('tr')) + 1");
                Locator workspaceNameLocator = page.locator(
                        String.format("//div[contains(@class,'firsttablewrapper')]//tbody//tr[%d]//div", rowIndex));
                workspaceNameList.add(workspaceNameLocator.textContent().trim());
            }
        }
        return workspaceNameList;
    }

    public void clickCancelButtonFromSettingsPanel() {
        STUDIO_SETTINGS_PANEL_CANCEL_BUTTON.click();
    }

    public void clickLifeSettings() {
        LIFE_SETTINGS.click();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(PULSEPOINT_DATA_FEE);
    }

    public double fetchPulsePointDataFees() {
        return Double.parseDouble(PULSEPOINT_DATA_FEE.inputValue().trim());
    }

    public double fetchNPITargetingGrossCPM() {
        return Double.parseDouble(NPI_TARGETING_GROSS_CPM.inputValue().trim());
    }

    public void clickLifeCancelButtonFromSettingsPanel() {
        LIFE_SETTINGS_PANEL_CANCEL_BUTTON.click();
    }

    public void externalUserSwitchAccount(String accountName) {
        USER_PROFILE_ICON.click();
        Locator SelectAccount =
                page.locator(String.format("//*[@id='accSwitchContainer']/ul/li/span[text()='%s]", accountName));
        waitUtility.waitForLocatorVisible(SelectAccount);
        SelectAccount.click();
    }

    public List<String> fetchCustomFieldsStartingWith(String prefix){
        Locator customFieldName = page.locator(String.format("//td[@id='fieldName']//div[starts-with(normalize-space(), '%s')]", prefix));
        return customFieldName.allTextContents().stream().map(String::trim).toList();
    }

    public String deleteCustomField(String fieldName) {
        Locator deleteIcon = page.locator(String.format("//td[@id='fieldName']//div[starts-with(normalize-space(), '%s')]/ancestor::tr//td[@id='action']//i[contains(@class,'delete')]", fieldName));
        deleteIcon.click();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(CUSTOM_FIELD_REMOVAL_POPUP);
        if (CUSTOM_FIELD_CANNOT_DELETE_POPUP.isVisible()) {
            waitUtility.waitForLocatorVisible(CUSTOM_FIELD_CANNOT_DELETE_POPUP);
            for (int i = 0; i < CAMPAIGN_LINK_FROM_CUSTOM_FIELD_POPUP.count(); i++) {
                Locator currentLink = CAMPAIGN_LINK_FROM_CUSTOM_FIELD_POPUP.nth(i);
                Page originalTab = DriverFactory.getPage();
                Page newTab = DriverFactory.getContext().waitForPage(() -> {
                    currentLink.click(new Locator.ClickOptions().setForce(true));
                });
                newTab.bringToFront();
                DriverFactory.threadLocalDriver.set(newTab);
                Campaigns campaigns = new Campaigns(newTab);
                campaigns.clearCustomFieldFromCampaign(fieldName);
                newTab.close();
                DriverFactory.threadLocalDriver.set(originalTab);
                originalTab.bringToFront();
            }
            deleteIcon.click();
            waitUtility.waitUntilSpinnerHidden();
            waitUtility.waitForLocatorVisible(CUSTOM_FIELD_REMOVAL_POPUP);
        }
        waitUtility.waitForLocatorVisible(CUSTOM_FIELD_DELETION_POP_UP);
        CUSTOM_FIELD_DELETE_BUTTON.click();
        return ALERT.textContent().trim();
    }

    public List<String> deleteCustomDefinitionRow(String username) {
        List<String> deletedEntries = new ArrayList<>();
        waitUtility.waitForLocatorVisible(CUSTOM_DESTINATION_ROW.last());
        int totalCount = CUSTOM_DESTINATION_ROW.count();
        for (int i = 0; i < totalCount; i++) {
            Locator currentRow = CUSTOM_DESTINATION_ROW.nth(i);
            currentRow.scrollIntoViewIfNeeded();
            Locator expandBtn = currentRow.locator("xpath=.//div[contains(@class,'collapsed-thin')]").first();
            if (expandBtn.isVisible()) {
                expandBtn.click();
            }
            Locator destinationName = currentRow.locator("xpath=.//div[label[text()='Name']]//input").first();
            Locator userInput = currentRow.locator("xpath=.//div[label[text()='User Name']]//input").first();
            Locator deleteIcon = currentRow.locator("xpath=.//app-icon-lable-link[contains(@text,'Delete')]//div").first();
            try {
                waitUtility.waitForLocatorVisible(userInput, 100);
                String currentUsername = userInput.inputValue().trim();
                if (currentUsername.equalsIgnoreCase(username)) {
                    deletedEntries.add(destinationName.inputValue().trim());
                    deleteIcon.click();
                }
            } catch (com.microsoft.playwright.TimeoutError e) {
            }
        }
        saveAccountsAdvertiserTab();
        return deletedEntries;
    }

    public boolean isDeletedEntryAvailable(String destinationName) {
        for (int i = 0; i < ENTER_DESTINATION_NAME.count(); i++) {
            if (!ENTER_DESTINATION_NAME.nth(i).inputValue().trim().equalsIgnoreCase(destinationName)) {
                return true;
            }
        }
        return false;
    }
}
