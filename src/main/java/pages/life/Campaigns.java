package pages.life;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import factory.DriverFactory;
import java.util.ArrayList;
import java.util.List;
import utils.WaitUtility;

public class Campaigns {
    private final Page page;
    private final Locator CREATE_CAMPAIGN;
    private final Locator VERIFY_CAMPAIGN_PAGE;
    private final Locator SEARCH_ADVERTISER;
    private final Locator SELECT_ADVERTISER;
    private final Locator CAMPAIGN_NAME;
    private final Locator CAMPAIGN_TYPE;
    private final Locator BUDGET;
    private final Locator SAVE_CAMPAIGN;
    private final Locator CAMPAIGN_DASHBOARD;
    private final Locator CAMPAIGN_SUCCESS;
    private final Locator LIFE_TIME_FILTER;
    private final Locator CAMPAIGN_ENTRIES;
    private final Locator ADVERTISER_DROPDOWN_VALUES;
    private final Locator MANDATORY_FIELD_ERROR;
    private final Locator SEARCH_DRUG;
    private final Locator CAMPAIGN_DESCRIPTION;
    private final Locator BUDGET_STATUS;
    private final Locator MANAGEMENT_FEE;
    private final Locator MANAGEMENT_FEE_OPTIONS;
    private final Locator DOLLAR_TYPE_FEE_INPUT;
    private final Locator PERCENT_TYPE_FEE_INPUT;
    private final Locator ACTION_ITEM_MENU;
    private final Locator SELECTED_ADVERTISER;
    private final Locator SELECTED_DRUG;
    private final Locator CAMPAIGN_DETAILS_TAB;
    private final Locator CLIENT_DROPDOWN;
    private final Locator CLIENT_DROPDOWN_VALUE;
    private final Locator SELECTED_CLIENT_VALUE;
    private final Locator ADD_CUSTOM_FIELD_BUTTON;
    private final Locator CUSTOM_FIELD_INPUT;
    private final Locator SAVE_CUSTOM_FIELD;
    private final Locator CUSTOM_FIELD_SUCCESS_ALERT;
    private final Locator CUSTOM_FIELD_DELETE_ICON;
    private final Locator CUSTOM_FIELD_DELETE_CONFIRMATION_POP_UP;
    private final Locator CUSTOM_FIELD_DELETE_BUTTON;
    private final Locator COMMENT_BOX_FROM_TITLE;
    private final Locator COMMENT_BOX_FROM_DASHBOARD;
    private final Locator LINE_ITEM_TILE;
    private final Locator TACTIC_TILE;
    private final Locator CAMPAIGN_TILE;
    private final Locator TOOL_TIP;
    private final Locator TOGGLE_STATUS;
    private final Locator CAMPAIGN_TAB;
    private final Locator DETAILS_TAB;
    private final Locator TIMES_PER_DROPDOWN;
    private final Locator SCOPE_DROPDOWN;
    private final Locator FREQUENCY_CAP_VALUE;
    private final Locator CUSTOM_FIELD;
    private final Locator GET_FREQUENCY_CAP_TEXT;
    private final Locator LINE_ITEM_TAB;
    private final Locator FREQUENCY_CAP;
    private final Locator TIMES_PER_HOURS_VALUE;
    private final Locator CAMPAIGN_OPTIONS;
    private final Locator EXPORT_AUDIT_LOG;
    private final Locator EXPORT_AUDIT_LOG_POPUP;
    private final Locator EXPORT_AUDIT_LOG_POPUP_CONTENT;
    private final Locator EXPORT_AUDIT_LOG_POPUP_OK_BUTTON;
    private final Locator EXPORT_AUDIT_LOG_SUCCESS_ALERT;
    private final Locator EXPORT_CAMPAIGN_SETTINGS;
    private final Locator EXPORT_CAMPAIGN_SETTINGS_POPUP;
    private final Locator EXPORT_CAMPAIGN_SETTINGS_SELECT_ALL_BUTTON;
    private final Locator EXPORT_CAMPAIGN_SETTINGS_EXPORT_BUTTON;
    private final Locator EXPORT_CAMPAIGN_SETTINGS_SUCCESS_ALERT;
    private final Locator BUDGET_STATUS_EXTERNAL;
    private final Locator CAMPAIGN_APPROVAL_STATUS;
    private final Locator CAMPAIGN_STATUS_APPROVED_BUTTON;
    private final Locator FAVORITE_ONLY_CHECKBOX;
    private final Locator FREQUENCY_CAP_VALIDATION_ERROR;
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());
    TacticSettings tacticSettings = new TacticSettings(DriverFactory.getPage());

    public Campaigns(Page page) {
        this.page = page;
        this.CREATE_CAMPAIGN = page.locator("//button[text()='Create a Campaign']");
        this.VERIFY_CAMPAIGN_PAGE = page.locator("//div[text()='Create New Campaign']");
        this.SEARCH_ADVERTISER = page.locator("//label[text()='Advertiser']/following-sibling::div//input");
        this.SELECT_ADVERTISER = page.getByText("");
        this.CAMPAIGN_NAME = page.locator("//input[@placeholder='Campaign Name']");
        this.CAMPAIGN_TYPE = page.locator("//label[contains(text(),'Campaign Type')]/following-sibling::div//button");
        this.BUDGET = page.locator("//input[@id='budgetcap']");
        this.SAVE_CAMPAIGN = page.locator("//span[text()='Save']");
        this.CAMPAIGN_SUCCESS = page.locator(
                "//div[@aria-label='Success!']/following-sibling::div[@role='alert' and contains(text(),'Campaign')]");
        this.CAMPAIGN_DASHBOARD = page.locator("//span[@class='breadCrumbRoot']");
        this.LIFE_TIME_FILTER = page.locator("//button[normalize-space()='Lifetime']");
        this.CAMPAIGN_ENTRIES = page.locator("//tr[contains(@class,'cl-li-row')]");
        this.ADVERTISER_DROPDOWN_VALUES = page.locator(
                "//input[@placeholder='Select Advertiser']/following-sibling::div[@class='menu transition visible']//div");
        this.MANDATORY_FIELD_ERROR = page.locator("//div[contains(@class,'errorsWrapper')]//p");
        this.CAMPAIGN_DESCRIPTION = page.locator("//textarea[@placeholder='Description']");
        this.SEARCH_DRUG = page.locator("//input[@placeholder='Drug']");
        this.BUDGET_STATUS = page.locator(
                "//label[contains(text(),'Budget Status')]/following-sibling::div//button | //label[contains(text(),'Approval Status')]/following-sibling::div[contains(@class,'display-inlineBlock')]//button");
        this.MANAGEMENT_FEE = page.locator("//label[contains(text(),'Apply Management Fee')]//parent::sui-checkbox");
        this.MANAGEMENT_FEE_OPTIONS = page.locator("//button[contains(@class,'fee-type-button')]");
        this.PERCENT_TYPE_FEE_INPUT =
                page.locator("//div[contains(@class,'fee-inputs')]//input[contains(@class,'percent-img')]");
        this.DOLLAR_TYPE_FEE_INPUT =
                page.locator("//div[contains(@class,'fee-inputs')]//input[contains(@class,'doller-img')]");
        this.ACTION_ITEM_MENU = page.locator("//span[contains(@title,'options')]");
        this.SELECTED_ADVERTISER = page.locator(
                "//label[text()='Advertiser']/following-sibling::div//input//following-sibling::span | //label[text()='Advertiser']/following-sibling::span");
        this.SELECTED_DRUG = page.locator("//div[@id='drugLookup']/following-sibling::div//input");
        this.CAMPAIGN_DETAILS_TAB = page.locator("//a[contains(@class,'gaTabDetails') and contains(text(),'Details')]");
        this.CLIENT_DROPDOWN =
                page.locator("//label[text()='Client']/following-sibling::div[contains(@class,'dropdown')]");
        this.CLIENT_DROPDOWN_VALUE = page.locator(
                "//label[text()='Client']/following-sibling::div[contains(@class,'dropdown')]//div[@class='menu transition visible']/div");
        this.SELECTED_CLIENT_VALUE = page.locator(
                "//label[text()='Client']/following-sibling::div[contains(@class,'dropdown')]//div[@class='text']");
        this.ADD_CUSTOM_FIELD_BUTTON = page.locator("//span[text()='Add Custom Field']");
        this.CUSTOM_FIELD_INPUT = page.locator("//input[@placeholder='Field Name']");
        this.SAVE_CUSTOM_FIELD = page.locator("//button[normalize-space()='Save']");
        this.CUSTOM_FIELD_SUCCESS_ALERT = page.locator(
                "//div[contains(text(),'Successfully created custom Field :')] | //div[contains(text(),'Successfully updated custom Field :')] | //div[contains(text(),'Successfully deleted the Field :')]");
        this.CUSTOM_FIELD_DELETE_ICON =
                page.locator("//div[contains(@class,'campaign-height-popover')]//app-icon-lable-link[@text='Delete']");
        this.CUSTOM_FIELD_DELETE_CONFIRMATION_POP_UP = page.locator(
                "//div[contains(text(),'Custom Field Will Be Deleted') or normalize-space(text())=\"Custom Field Can't Be Removed\"]");
        this.CUSTOM_FIELD_DELETE_BUTTON =
                page.locator("//span[contains(text(),'Delete Field') or contains(text(),'Ok')]");
        this.COMMENT_BOX_FROM_TITLE = page.locator("//span[@class='notes-dark-icon-provided']");
        this.COMMENT_BOX_FROM_DASHBOARD = page.locator("//span[contains(@class,'notes-icon-provided-dashboard')]");
        this.LINE_ITEM_TILE = page.locator("//div[contains(@class,'listitembox')]");
        this.TACTIC_TILE = page.locator("//div[contains(@class,'tactic-container')]");
        this.CAMPAIGN_TILE = page.locator("//div[contains(@class,'campaign-tile')]");
        this.TOOL_TIP = page.locator("//div[contains(@class,'ng-tooltip-show')]");
        this.TOGGLE_STATUS = page.locator("//span[contains(@class,'gaEnable ')]//label");
        this.CAMPAIGN_TAB = page.locator("//div[@class='item-details']");
        this.CUSTOM_FIELD = page.locator("//label[contains(@class,'cmp-form-label')]");
        this.DETAILS_TAB = page.locator("//a[contains(text(),'Details')]");
        this.TIMES_PER_DROPDOWN = page.locator("//div[contains(@class,'dropdown-wrapper ui field noMargin')]");
        this.SCOPE_DROPDOWN = page.locator("//div[contains(@class,'crossDevice-dropdown')]");
        this.FREQUENCY_CAP_VALUE = page.locator("//input[@id='windowLimit' or @id='freqWindowLimit']");
        this.GET_FREQUENCY_CAP_TEXT = page.locator("//p[contains(@class,'display-block')]");
        this.LINE_ITEM_TAB = page.locator("//div[contains(@class,'listitembox')]");
        this.FREQUENCY_CAP =
                page.locator("//label[contains(text(),'Frequency Cap')]/following-sibling::div//sui-checkbox");
        this.TIMES_PER_HOURS_VALUE = page.locator("//input[@formcontrolname='hour']");
        this.CAMPAIGN_OPTIONS = page.locator("//div[contains(@class, 'action-items')]//span[@title='options']");
        this.EXPORT_AUDIT_LOG = page.locator("//span[text()='Export Audit Log']");
        this.EXPORT_AUDIT_LOG_POPUP = page.locator("//div[@class='popup-header' and text()='Export Audit Log']");
        this.EXPORT_AUDIT_LOG_POPUP_CONTENT = page.locator("//div[contains(@class,'popup-content')]/span");
        this.EXPORT_AUDIT_LOG_POPUP_OK_BUTTON = page.locator("//span[@class='text' and text()='Ok']");
        this.EXPORT_AUDIT_LOG_SUCCESS_ALERT =
                page.locator("//div[@role='alert' and contains(text(),'Audit Log request created')]");
        this.EXPORT_CAMPAIGN_SETTINGS =
                page.locator("//app-icon-lable-link[@title='Export Settings']//div[contains(@class,'icolink')]");
        this.EXPORT_CAMPAIGN_SETTINGS_POPUP =
                page.locator("//div[@class='rightPanelHeader2' and text()='Export Campaign Settings']");
        this.EXPORT_CAMPAIGN_SETTINGS_SELECT_ALL_BUTTON =
                page.locator("//app-icon-lable-link[@icon='20-select-all.svg']/div");
        this.EXPORT_CAMPAIGN_SETTINGS_EXPORT_BUTTON =
                page.locator("//button[contains(@class,'okButton') and contains(text(),'Export')]");
        this.EXPORT_CAMPAIGN_SETTINGS_SUCCESS_ALERT =
                page.locator("//div[@role='alert' and contains(text(),'The exported file will be sent')]");
        this.BUDGET_STATUS_EXTERNAL =
                page.locator("//label[contains(text(),'Budget Status')]/following-sibling::div//span");
        this.CAMPAIGN_APPROVAL_STATUS = page.locator("//label[contains(text(),'Approval Status')]");
        this.CAMPAIGN_STATUS_APPROVED_BUTTON = page.locator(
                "//label[contains(text(),'Approval Status')]/following-sibling::div[contains(@class,'display-inlineBlock')]//button[text()='Approved']");
        this.FAVORITE_ONLY_CHECKBOX = page.locator("//sui-checkbox[label[normalize-space()='Favorite Only']]");
        this.FREQUENCY_CAP_VALIDATION_ERROR = page.locator("//p[contains(@class,'ng-star-inserted')]");
    }

    public void createCampaign() {
        page.waitForTimeout(3000);
        CREATE_CAMPAIGN.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public boolean isCreateCampaignButtonVisible() {
        return CREATE_CAMPAIGN.isVisible();
    }

    public void selectCampaign() {
        CAMPAIGN_TAB.first().click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void clickLineItem() {
        LINE_ITEM_TAB.first().click();
        waitUtility.waitForElementVisible("//div[contains(@class, 'data-rangeSlider-container')]");
    }

    public boolean isFrequencyCapDisabled() {
        return FREQUENCY_CAP.getAttribute("class").contains("checked");
    }

    public void clickDetailsTab() {
        DETAILS_TAB.click();
    }

    public void addFrequencyCap(String level, String frequencyValue, String timesPer, String scope) {
        Locator TIMES_PER_OPTION = page.locator(String.format("//div[contains(text(),'%s')]", timesPer));
        Locator FREQUENCY_CAP_SCOPE = page.locator(String.format("//div[contains(text(),'%s')]", scope));
        if (level.contains("Campaign") | level.contains("Line Item")) {
            waitUtility.waitForLocatorVisible(CUSTOM_FIELD.first());
        }
        if (!FREQUENCY_CAP.getAttribute("class").contains("checked")) {
            FREQUENCY_CAP.click();
        }
        FREQUENCY_CAP_VALUE.fill(frequencyValue);
        TIMES_PER_DROPDOWN.click();
        TIMES_PER_OPTION.first().click();
        if (timesPer.contains("hour(s)")) {
            TIMES_PER_HOURS_VALUE.fill(frequencyValue);
        }
        SCOPE_DROPDOWN.click();
        FREQUENCY_CAP_SCOPE.click();
        SAVE_CAMPAIGN.click();
        waitUtility.waitForElementVisible("//div[@role='alert']");
    }

    public void addFrequencyCap(String level, String frequencyValue, String timesPer, String scope, String exceed) {
        Locator TIMES_PER_OPTION = page.locator(String.format("//div[contains(text(),'%s')]", timesPer));
        Locator FREQUENCY_CAP_SCOPE = page.locator(String.format("//div[contains(text(),'%s')]", scope));
        if (level.contains("Line Item")) {
            waitUtility.waitForLocatorVisible(CUSTOM_FIELD.first());
        }
        if (!FREQUENCY_CAP.first().getAttribute("class").contains("checked")) {
            FREQUENCY_CAP.click();
        }
        FREQUENCY_CAP_VALUE.fill(frequencyValue);
        TIMES_PER_DROPDOWN.click();
        TIMES_PER_OPTION.first().click();
        if (timesPer.contains("hour(s)")) {
            TIMES_PER_HOURS_VALUE.fill(frequencyValue);
        }
        SCOPE_DROPDOWN.click();
        FREQUENCY_CAP_SCOPE.first().click();
        SAVE_CAMPAIGN.click();
        waitUtility.waitForElementVisible("//div[contains(@aria-label,'can not exceed')]");
    }

    public String frequencyLimitExceedCheck(String level) {
        String errorMessage = "Error message not fetched yet";
        if (level.contains("Line Item")) {
            errorMessage = FREQUENCY_CAP_VALIDATION_ERROR.nth(1).innerText();
        } else if (level.contains("tactic"))
            errorMessage = FREQUENCY_CAP_VALIDATION_ERROR.nth(2).innerText();
        tacticSettings.clickCancel();
        return errorMessage;
    }

    public boolean getFrequencyCapState() {
        return FREQUENCY_CAP.first().getAttribute("class").contains("checked");
    }

    public String getSavedFrequencyCap(String level) {
        String frequencyCapValue =
                GET_FREQUENCY_CAP_TEXT.first().innerText().trim().toUpperCase();
        if (level.contains("Line Item")) {
            frequencyCapValue = GET_FREQUENCY_CAP_TEXT
                    .filter(new Locator.FilterOptions().setHasText("Line item"))
                    .innerText()
                    .trim()
                    .toUpperCase();
        }
        return frequencyCapValue;
    }

    public String campaignDashboard() {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        return this.page.title();
    }

    public String verifyCampaignText() {
        waitUtility.waitForLocatorVisible(VERIFY_CAMPAIGN_PAGE);
        return VERIFY_CAMPAIGN_PAGE.innerText();
    }

    public void selectAdvertiser(String advertiser) {
        SEARCH_ADVERTISER.fill(advertiser);
        SELECT_ADVERTISER.locator("text=" + advertiser).click();
    }

    public void enterCampaignName(String campaignNameRandom) {
        CAMPAIGN_NAME.fill(campaignNameRandom);
    }

    public void setCampaignType(String campaignType) {
        Locator campaignTypeButton = CAMPAIGN_TYPE.filter(new Locator.FilterOptions().setHasText(campaignType));
        campaignTypeButton.click();
    }

    public void enterBudget(String budget) {
        BUDGET.fill(budget);
        page.keyboard().press("Tab");
    }

    public void saveCampaign() {
        SAVE_CAMPAIGN.click();
    }

    public String campaignSuccess() {
        String successMessage = CAMPAIGN_SUCCESS.innerText().trim();
        waitUtility.waitForLocatorHidden(CAMPAIGN_SUCCESS);
        return successMessage;
    }

    public void navigateToCampaignDashboard() {
        CAMPAIGN_DASHBOARD.click();
        waitUtility.waitForLocatorVisible(CAMPAIGN_DASHBOARD);
        if (FAVORITE_ONLY_CHECKBOX.getAttribute("class").contains("checked")) {
            FAVORITE_ONLY_CHECKBOX.click();
        }
        if (LIFE_TIME_FILTER.getAttribute("class").contains("inactive")) {
            LIFE_TIME_FILTER.click();
            waitUtility.waitForLocatorVisible(CAMPAIGN_ENTRIES.last());
        }
    }

    public List<String> fetchAdvertiserList() {
        waitUtility.waitForLocatorVisible(SEARCH_ADVERTISER);
        SEARCH_ADVERTISER.click();
        return ADVERTISER_DROPDOWN_VALUES.allTextContents();
    }

    public List<String> fetchMandatoryFieldsError() {
        return MANDATORY_FIELD_ERROR.allTextContents();
    }

    public String fetchDefaultCampaignType() {
        return fetchDefaultValue(CAMPAIGN_TYPE);
    }

    public void selectDrug(String drugName) {
        SEARCH_DRUG.click();
        SEARCH_DRUG.fill(drugName);
        Locator option =
                SEARCH_DRUG.locator(String.format("//following-sibling::div//div[contains(@title,'%s')]", drugName));
        option.first().click();
    }

    public String fetchCampaignBudget(String budget) {
        enterBudget(budget);
        page.keyboard().press("Tab");
        return BUDGET.innerText();
    }

    public void enterCampaignDescription(String campaignDescription) {
        CAMPAIGN_DESCRIPTION.fill(campaignDescription);
    }

    public List<String> fetchBudgetStatus() {
        return BUDGET_STATUS.allTextContents();
    }

    public String fetchDefaultBudgetStatus() {
        return fetchDefaultValue(BUDGET_STATUS);
    }

    public String fetchDefaultValue(Locator locator) {
        for (int i = 0; i < locator.count(); i++) {
            if (locator.nth(i).getAttribute("class") != null
                    && locator.nth(i).getAttribute("class").contains("active"))
                return locator.nth(i).textContent().trim();
        }
        return "";
    }

    public boolean isManagementFeeAvailable() {
        return MANAGEMENT_FEE.isVisible();
    }

    public void clickManagementFee() {
        MANAGEMENT_FEE.click();
    }

    public List<String> fetchManagementFeeOptions() {
        return MANAGEMENT_FEE_OPTIONS.allTextContents();
    }

    public void clickManagementFeeOptionAndEnterData(String managementFeeOption, String percent, String amount) {
        MANAGEMENT_FEE_OPTIONS.locator("text=" + managementFeeOption).click();
        if (PERCENT_TYPE_FEE_INPUT.isVisible()) PERCENT_TYPE_FEE_INPUT.fill(percent);
        if (DOLLAR_TYPE_FEE_INPUT.isVisible()) DOLLAR_TYPE_FEE_INPUT.fill(amount);
    }

    public void clickActionItemMenu() {
        ACTION_ITEM_MENU.scrollIntoViewIfNeeded();
        ACTION_ITEM_MENU.click();
    }

    public String fetchSelectedManagementFeeOption() {
        return fetchDefaultValue(MANAGEMENT_FEE_OPTIONS);
    }

    public List<String> fetchEnteredManagementFeeValues() {
        List<String> values = new ArrayList<>();
        if (PERCENT_TYPE_FEE_INPUT.isVisible())
            values.add(PERCENT_TYPE_FEE_INPUT.inputValue().trim());
        if (DOLLAR_TYPE_FEE_INPUT.isVisible())
            values.add(DOLLAR_TYPE_FEE_INPUT.inputValue().trim());
        return values;
    }

    public boolean isGenerateReportOptionAvailable(String reportOption) {
        Locator reportOptionXpath =
                page.locator(String.format("//app-icon-lable-link[contains(@title,'%s')]//div", reportOption));
        waitUtility.waitForLocatorVisible(reportOptionXpath);
        return reportOptionXpath.isVisible()
                && !reportOptionXpath.getAttribute("class").contains("disabled");
    }

    public boolean isDeleteOptionAvailable(String deleteOption) {
        Locator reportOptionXpath =
                page.locator(String.format("//app-icon-lable-link[contains(@title,'%s')]//div", deleteOption));
        waitUtility.waitForLocatorVisible(reportOptionXpath);
        return reportOptionXpath.isVisible()
                && reportOptionXpath.getAttribute("class").contains("disabled");
    }

    public List<String> fetchCampaignDetails() {
        List<String> enteredData = new ArrayList<>();
        enteredData.add(SELECTED_ADVERTISER.textContent());
        enteredData.add(CAMPAIGN_NAME.inputValue());
        enteredData.add(fetchDefaultValue(CAMPAIGN_TYPE));
        enteredData.add(SELECTED_CLIENT_VALUE.textContent());
        enteredData.add(SELECTED_DRUG.inputValue());
        enteredData.add(BUDGET.inputValue());
        enteredData.add(CAMPAIGN_DESCRIPTION.inputValue());
        enteredData.add(fetchDefaultValue(BUDGET_STATUS));
        enteredData.add(fetchDefaultValue(MANAGEMENT_FEE_OPTIONS));
        if (PERCENT_TYPE_FEE_INPUT.isVisible()) enteredData.add(PERCENT_TYPE_FEE_INPUT.inputValue());
        if (DOLLAR_TYPE_FEE_INPUT.isVisible()) enteredData.add(DOLLAR_TYPE_FEE_INPUT.inputValue());
        return enteredData;
    }

    public void clickSavedCampaign(String campaignName) {
        Locator savedCampaignXpath = page.locator(
                String.format("//div[contains(@class,'campaign-title') and contains(text(),'%s')]", campaignName));
        savedCampaignXpath.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void clickCampaignDetailsTab() {
        waitUtility.waitForLocatorVisible(CAMPAIGN_DETAILS_TAB);
        CAMPAIGN_DETAILS_TAB.click();
        while (!SELECTED_ADVERTISER.isVisible()) {
            page.waitForTimeout(1000);
        }
    }

    public String verifyClientFieldEnabledOrDisabledBasedOnAccount(String clientName) {
        String state;
        boolean isDisabled = CLIENT_DROPDOWN.getAttribute("class").contains("disabled");
        if (!isDisabled) {
            CLIENT_DROPDOWN.scrollIntoViewIfNeeded();
            CLIENT_DROPDOWN.click();
            CLIENT_DROPDOWN_VALUE
                    .filter(new Locator.FilterOptions().setHasText(clientName))
                    .click();
            state = "Enabled";
        } else {
            state = "Disabled";
        }
        return state;
    }

    public boolean isAddCustomFieldButtonAvailable() {
        return ADD_CUSTOM_FIELD_BUTTON.isVisible();
    }

    public void clickAddCustomFieldButton() {
        ADD_CUSTOM_FIELD_BUTTON.click();
    }

    public void enterCustomFieldName(String metricName) {
        CUSTOM_FIELD_INPUT.fill(metricName);
    }

    public void saveCustomField() {
        SAVE_CUSTOM_FIELD.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public String fetchCustomFieldSuccessAlert() {
        String text = CUSTOM_FIELD_SUCCESS_ALERT.textContent().trim();
        waitUtility.waitForLocatorHidden(CUSTOM_FIELD_SUCCESS_ALERT);
        return text;
    }

    public boolean isAddedCustomFieldAvailable(String metricName) {
        return page.locator(String.format("//span[contains(normalize-space(.),'%s')]", metricName))
                .isVisible();
    }

    public void clickCustomFieldLabel(String metricName) {
        page.locator(String.format("//span[contains(normalize-space(.),'%s')]/ancestor::label//img", metricName))
                .click();
    }

    public void enterCustomFieldData(String customFieldName, String customFieldData) {
        Locator fieldRow = page.locator("app-life-custom-field-setting div")
                .filter(new Locator.FilterOptions().setHasText(customFieldName))
                .last();
        fieldRow.locator("input").fill(customFieldData);
    }

    public void navigateToCampaign(String campaignName) {
        page.locator(String.format("//div[normalize-space(text())='%s']", campaignName))
                .click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public String fetchCustomFieldData(String customFieldName) {
        Locator fieldRow = page.locator("app-life-custom-field-setting div, .form-group")
                .filter(new Locator.FilterOptions().setHasText(customFieldName))
                .last();
        return fieldRow.locator("input").inputValue();
    }

    public String deleteCustomField(String customFieldName) {
        page.locator(String.format("//span[contains(normalize-space(.),'%s')]/ancestor::label//img", customFieldName))
                .click();
        waitUtility.waitForLocatorVisible(CUSTOM_FIELD_DELETE_ICON);
        CUSTOM_FIELD_DELETE_ICON.click();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(CUSTOM_FIELD_DELETE_CONFIRMATION_POP_UP);
        String text = CUSTOM_FIELD_DELETE_CONFIRMATION_POP_UP.textContent().trim();
        CUSTOM_FIELD_DELETE_BUTTON.click();
        return text;
    }

    public void clickCampaignTile() {
        CAMPAIGN_TILE.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void clickLineItemTile() {
        LINE_ITEM_TILE.click();
        waitUtility.waitUntilPreLoaderHidden();
        waitUtility.waitForElementVisible("//div[contains(@class, 'data-rangeSlider-container')]");
    }

    public void clickTacticTile() {
        TACTIC_TILE.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public String fetchCommentFromCampaignLineItemTacticPanel() {
        COMMENT_BOX_FROM_TITLE.hover();
        return TOOL_TIP.innerText();
    }

    public String fetchCommentFromCampaignLineItemTacticDashboard() {
        do {
            COMMENT_BOX_FROM_DASHBOARD.hover();
        } while (!TOOL_TIP.isVisible());
        return TOOL_TIP.innerText();
    }

    public String fetchToggleStatus() {
        return TOGGLE_STATUS.textContent().trim();
    }

    public void clickCampaignOptions() {
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(CAMPAIGN_OPTIONS);
        CAMPAIGN_OPTIONS.click();
    }

    public void openExportAuditLogPopup() {
        waitUtility.waitForLocatorVisible(EXPORT_AUDIT_LOG);
        EXPORT_AUDIT_LOG.click();
        waitUtility.waitForLocatorVisible(EXPORT_AUDIT_LOG_POPUP);
    }

    public String fetchExportAuditLogPopupContent() {
        return EXPORT_AUDIT_LOG_POPUP_CONTENT.innerText();
    }

    public void clickConfirmExportAuditLog() {
        EXPORT_AUDIT_LOG_POPUP_OK_BUTTON.click();
    }

    public String fetchExportAuditLogSuccessAlert() {
        String text = EXPORT_AUDIT_LOG_SUCCESS_ALERT.innerText().trim();
        waitUtility.waitForLocatorHidden(EXPORT_AUDIT_LOG_SUCCESS_ALERT);
        return text;
    }

    public void exportCampaignSettings() {
        waitUtility.waitForLocatorVisible(EXPORT_CAMPAIGN_SETTINGS);

        page.evaluate("() => { " + "if (window.isPatched) return; " + "const o = Element.prototype.setAttribute; "
                + "Element.prototype.setAttribute = function(n, v) { " + "if (n !== ']') o.apply(this, arguments); "
                + "}; window.isPatched = true; }");

        EXPORT_CAMPAIGN_SETTINGS.evaluate("el => el.click()");
        waitUtility.waitForLocatorVisible(EXPORT_CAMPAIGN_SETTINGS_POPUP);

        long timeout = System.currentTimeMillis() + 5000;
        boolean isChecked = false;

        while (System.currentTimeMillis() < timeout) {
            EXPORT_CAMPAIGN_SETTINGS_SELECT_ALL_BUTTON.click(new Locator.ClickOptions().setForce(true));
            page.waitForTimeout(500);
            isChecked = (Boolean) page.locator("sui-checkbox input").first().evaluate("el => el.checked");

            if (isChecked) {
                break;
            }
        }

        if (!isChecked) {
            EXPORT_CAMPAIGN_SETTINGS_SELECT_ALL_BUTTON.dispatchEvent("click");
            page.waitForTimeout(1000);
        }

        EXPORT_CAMPAIGN_SETTINGS_EXPORT_BUTTON.click();
    }

    public String fetchExportCampaignSettingsSuccessAlert() {
        String text = EXPORT_CAMPAIGN_SETTINGS_SUCCESS_ALERT.innerText().trim();
        waitUtility.waitForLocatorHidden(EXPORT_CAMPAIGN_SETTINGS_SUCCESS_ALERT);
        return text;
    }

    public String getCampaignBudgetStatus() {
        return BUDGET_STATUS_EXTERNAL.innerText().trim();
    }

    public String checkBackgroundColorOfCampaignBudgetStatus() {
        return BUDGET_STATUS_EXTERNAL
                .evaluate("element => getComputedStyle(element).backgroundColor")
                .toString();
    }

    public int getCampaignBudgetStatusOptionsCount() {
        waitUtility.waitForLocatorVisible(BUDGET_STATUS_EXTERNAL);
        return page.locator("//label[contains(text(),'Budget Status')]/following-sibling::div//span")
                .count();
    }

    public void approveCampaign() {
        waitUtility.waitForLocatorVisible(CAMPAIGN_APPROVAL_STATUS);
        waitUtility.waitForLocatorVisible(CAMPAIGN_STATUS_APPROVED_BUTTON);
        CAMPAIGN_STATUS_APPROVED_BUTTON.click();
        SAVE_CAMPAIGN.click();
        waitUtility.waitUntilSpinnerHidden();
    }
}
