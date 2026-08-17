package pages.life;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;
import factory.DriverFactory;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import pages.Navigation;
import utils.CommonUtils;
import utils.WaitUtility;

public class TacticDetails {
    private final Page page;
    public final Locator TARGETING_RULES_ICON;
    private final Locator VERIFY_TACTIC_DETAILS_PAGE;
    private final Locator TACTIC_NAME;
    private final Locator SAVE_TACTIC_DETAILS;
    private final Locator TACTIC_DETAILS_SUCCESS;
    private final Locator IMPORT_TEMPLATE_ICON;
    private final Locator TEMPLATE_SEARCH_BOX;
    private final Locator IMPORT_TEMPLATE_DIALOG;
    private final Locator IMPORT_BUTTON;
    private final Locator OVERRIDE_DIALOG;
    private final Locator REPLACE_BUTTON;
    private final Locator TEMPLATE_IMPORT_ALERT;
    private final Locator IMPORTED_TARGET_TEMPLATE;
    private final Locator TACTIC_SETTINGS_TAB;
    private final Locator SAVE_TEMPLATE_BUTTON;
    private final Locator SAVE_TEMPLATE_DIALOG;
    private final Locator TEMPLATE_NAME_TEXT;
    private final Locator SAVE_BUTTON;
    private final Locator TEMPLATE_SAVED_SUCCESS_ALERT;
    private final Locator NEW_TACTIC_BUTTON;
    private final Locator SAVED_TACTICS;
    private final Locator DETAILS_TAB;
    private final Locator NEW_TACTIC_TABS;
    private final Locator SAVED_TACTIC_TABS;
    private final Locator TACTIC_STATUS;
    private final Locator TACTIC_DETAILS_TAB;
    private final Locator ADD_CUSTOM_FIELD;
    private final Locator ADD_CUSTOM_FIELD_INPUT;
    private final Locator SAVE_CUSTOM_FIELD_BUTTON;
    private final Locator FIELD_CREATE_SUCCESS;
    private final Locator CUSTOM_FIELD_TEXT;
    private final Locator DELETE_BUTTON;
    private final Locator CONFIRM_DELETE;
    private final Locator DELETE_SUCCESS;
    private final Locator CUSTOM_FIELD;
    private final Locator TACTIC_OPTIONS;
    private final Locator TACTIC_DELETE_BUTTON;
    private final Locator TACTIC_REMOVE_BUTTON;
    private final Locator EXIT_BULK_MODE;
    private final Locator ENABLE_TACTIC;
    private final Locator DISABLE_TACTIC;
    private final Locator BULK_ACTION;
    private final Locator TACTIC_GLOBAL_SEARCH_TEXT;
    private final Locator TACTIC_TAB;
    private final Locator CLOSE_GLOBAL_SEARCH;
    private final Locator OPEN_GLOBAL_SEARCH;
    private final Locator GLOBAL_SEARCH_INPUT_FIELD;
    private final Locator HEADER_COMMENT;
    private final Locator NAVIGATION_COMMENT;
    private final Locator COMMENT_TEXT_BOX;
    private final Locator COMMENT_SUCCESS_ALERT;
    private final Locator TACTIC_COPY_NAME_FIELD;
    private final Locator DUPLICATE_BUTTON;
    private final Locator CREATIVE_NAME;
    private final Locator TACTIC_CHANNEL;
    private final Locator TACTIC_PRIORITY;
    private final Locator GLOBAL_TACTIC_TAB;
    private final Locator TACTIC_MEDIA_OPTIMIZATION;
    private final Locator DATA_COST_CPM;
    private final Locator HUMAN_COST_CPM;
    private final Locator COPY_SUCCESS_ALERT;
    private final Locator TARGETING_RULE_CONFIRMATION_DIALOG;
    private final Locator CONTINUE_BUTTON;
    private final Locator CLICK_REFRESH_BUTTON;
    private final Locator NO_TARGETING_RULES;
    private final Locator FORECAST_AVAILS_NUMBER;
    private final Locator SHOW_EXPRESSION_BUTTON;
    private final Locator CONNECTION_LOCATOR;
    private final Locator VALUE_LOCATOR;
    private final Locator PERCENT_TYPE_FEE_INPUT;
    private final Locator DOLLAR_TYPE_FEE_INPUT;
    private final Locator CANCEL_BUTTON;
    private List<String> showExpressionRawValues;
    private List<String> showExpressionValues;
    Campaigns campaigns = new Campaigns(DriverFactory.getPage());
    LineItemDetails lineItemDetails = new LineItemDetails(DriverFactory.getPage());
    NPISmartList npiSmartList = new NPISmartList(DriverFactory.getPage());
    TargetingTemplate targetingTemplate = new TargetingTemplate(DriverFactory.getPage());
    Navigation navigation = new Navigation(DriverFactory.getPage());
    TacticSettings tacticSettings = new TacticSettings(DriverFactory.getPage());
    TacticCreatives tacticCreatives = new TacticCreatives(DriverFactory.getPage());
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());

    public TacticDetails(Page page) {
        this.page = page;
        this.VERIFY_TACTIC_DETAILS_PAGE = page.locator("//div[text()='New Tactic' or text()='New Ad Group']");
        this.TACTIC_NAME = page.locator("//input[@placeholder='Tactic Name' or @placeholder='Ad Group Name']");
        this.SAVE_TACTIC_DETAILS = page.locator("//span[text()='Save']");
        this.TACTIC_DETAILS_SUCCESS = page.locator("//div[@aria-label='Success!']/following-sibling::div[@role='alert' and contains(text(),'Tactic')]");
        this.IMPORT_TEMPLATE_ICON = page.locator("//app-icon-lable-link[contains(@text,'Import Template')]/div");
        this.IMPORT_TEMPLATE_DIALOG = page.locator("//div[contains(text(),'Import Template')]");
        this.TEMPLATE_SEARCH_BOX = page.locator("//input[contains(@placeholder,'type in to search...')]");
        this.IMPORT_BUTTON = page.locator("//button[contains(text(),'Import')]");
        this.OVERRIDE_DIALOG = page.locator("//div[contains(text(),'Override Targeting Rules?')]");
        this.REPLACE_BUTTON = page.locator("//button[contains(text(),'Replace Targeting')]");
        this.TEMPLATE_IMPORT_ALERT = page.locator("//div[contains(text(),'Template Imported Successfully')]");
        this.IMPORTED_TARGET_TEMPLATE = page.locator("//div[@class='targets-list']");
        this.TACTIC_SETTINGS_TAB = page.locator("//a[contains(@class,'gaTabSettings')]");
        this.SAVE_TEMPLATE_BUTTON = page.locator("//app-icon-lable-link[contains(@text,'Save as Template')]/div");
        this.SAVE_TEMPLATE_DIALOG = page.locator("//div[contains(text(),'Save as Template')]");
        this.TEMPLATE_NAME_TEXT = page.locator("//input[contains(@placeholder,'Template Name')]");
        this.SAVE_BUTTON = page.locator("//button[contains(@class,'okButton')]");
        this.TEMPLATE_SAVED_SUCCESS_ALERT = page.locator("//div[contains(text(),'Saved as Template Successfully')]");
        this.TARGETING_RULES_ICON = page.locator("//span[contains(text(),'Targeting Rule')]");
        this.NEW_TACTIC_BUTTON = page.locator("//span[normalize-space(text())='New Tactic']");
        this.SAVED_TACTICS = page.locator("//div[contains(@class,'tactic-main-details')]");
        this.DETAILS_TAB = page.locator("//a[contains(text(),'Detail')]");
        this.NEW_TACTIC_TABS = page.locator("//a[@disabled='disabled']");
        this.SAVED_TACTIC_TABS = page.locator("//a[contains(@class,'gaTab')]");
        this.TACTIC_STATUS = page.locator("//span[contains(@class, 'status-label')]/span");
        this.TACTIC_DETAILS_TAB = page.locator("//a[normalize-space()='Details']");
        this.ADD_CUSTOM_FIELD = page.locator("//span[contains(text(),'Add Custom Field')]");
        this.ADD_CUSTOM_FIELD_INPUT = page.locator("//input[@placeholder='Field Name']");
        this.SAVE_CUSTOM_FIELD_BUTTON = page.locator("//button[normalize-space()='Save']");
        this.FIELD_CREATE_SUCCESS = page.locator("//div[@role='alert' and contains(text(),'Successfully created custom Field')]");
        this.CUSTOM_FIELD_TEXT = page.locator("//input[contains(@class,'gaName')]");
        this.DELETE_BUTTON = page.locator("//app-icon-lable-link[contains(@class,'delete-field')]");
        this.CONFIRM_DELETE = page.locator("//span[contains(text(),'Delete Field')]");
        this.DELETE_SUCCESS = page.locator("//div[contains(text(),'Successfully deleted the Field')]");
        this.CUSTOM_FIELD = page.locator("(//label[contains(@class,'cmp-form-label')])[1]");
        this.TACTIC_TAB = page.locator("//div[@class='tactic item-details']");
        this.TACTIC_OPTIONS = page.locator("//div[contains(@class, 'tactic-app-action')]//span[@title='options']");
        this.TACTIC_DELETE_BUTTON = page.getByText("Delete");
        this.TACTIC_REMOVE_BUTTON = page.getByText("Remove");
        this.EXIT_BULK_MODE = page.locator("//button[normalize-space()='Exit Bulk edit mode']");
        this.ENABLE_TACTIC = page.locator("//div[@class='bulk-icon addBulkOpActive']").first();
        this.DISABLE_TACTIC = page.locator("//div[contains(text(),'Disable Tactics')]");
        this.BULK_ACTION = page.locator("//span[@class='pointer inlineDiv iconSprite bulkEdit']");
        this.TACTIC_GLOBAL_SEARCH_TEXT = page.locator("//div[contains(text(),'Nothing found...')]");
        this.CLOSE_GLOBAL_SEARCH = page.locator("//div[@class='ui image close-white-40 pointer']");
        this.OPEN_GLOBAL_SEARCH = page.locator("//div[@class='iconSprite search-overlay-lens']");
        this.GLOBAL_SEARCH_INPUT_FIELD = page.locator("//input[@id='global_search_input']");
        this.HEADER_COMMENT = page.locator("//div[@class='notes-dashboard left']");
        this.NAVIGATION_COMMENT = page.locator("//span[@class='notes-dark-icon-empty'] | //span[@class='notes-dark-icon-provided']");
        this.COMMENT_TEXT_BOX = page.locator("//textarea[@id='notesId']");
        this.COMMENT_SUCCESS_ALERT = page.locator("//div[contains(text(),'Notes saved successfully')]");
        this.TACTIC_COPY_NAME_FIELD = page.locator("//input[contains(@class, \"multi-line-input-box\")and @placeholder=\"Enter New Tactic Name\"]");
        this.DUPLICATE_BUTTON = page.locator("//button[contains(@class, \"okButton\") and normalize-space(text())=\"Duplicate\"]");
        this.CREATIVE_NAME = page.locator("//td[contains(@class,'semi-bold')]//span[contains(@class,'crt-name')]");
        this.TACTIC_CHANNEL = page.locator("(//div[@id='billingTypeDropdown'])[1]");
        this.TACTIC_PRIORITY = page.locator("//div[contains(@class, 'labeled input')] //input[@id='priority']");
        this.GLOBAL_TACTIC_TAB = page.locator("//a[contains(text(),'Tactics')]");
        this.TACTIC_MEDIA_OPTIMIZATION = page.locator("(//div[@id='billingTypeDropdown'])[2]");
        this.DATA_COST_CPM = page.locator("(//span[contains(@class, 'cost-override')])[1]");
        this.HUMAN_COST_CPM = page.locator("(//span[contains(@class, 'cost-override')])[2]");
        this.COPY_SUCCESS_ALERT = page.locator("//div[@id='toast-container' and contains(., 'Tactic(s)') and contains(., 'copied successfully')]");
        this.TARGETING_RULE_CONFIRMATION_DIALOG = page.locator("//div[contains(@class,'confirm-modal header-title')]");
        this.CONTINUE_BUTTON = page.locator("//span[text()='Continue']");
        this.CLICK_REFRESH_BUTTON = page.locator("//button[contains(@class,'refresh')]");
        this.NO_TARGETING_RULES = page.locator("//div[contains(text(),'No Targeting Rules set yet')]");
        this.FORECAST_AVAILS_NUMBER = page.locator("//div[@class='forecast-metrics']//div[@class='availsNumber']");
        this.SHOW_EXPRESSION_BUTTON = page.locator("//span[contains(text(),'Show Expression')]");
        this.VALUE_LOCATOR = page.locator("//span[@class='targetGreen keyword text-target']");
        this.CONNECTION_LOCATOR = page.locator("//span[@class='inlineDiv connector']");
        this.PERCENT_TYPE_FEE_INPUT = page.locator("//div[contains(@class,'management-fee-container')]//input[contains(@class,'percent-img')]");
        this.DOLLAR_TYPE_FEE_INPUT = page.locator("//div[contains(@class,'management-fee-container')]//input[contains(@class,'doller-img')]");
        this.CANCEL_BUTTON = page.locator("//span[contains(@class,'cancleButton')]");
    }

    public void clickNewTactic() {
        NEW_TACTIC_BUTTON.click();
    }

    public Locator customFieldValue(String customFieldName) {
        return page.locator(String.format("//span[@class='cmp-form-label-text' and text()='%s']/parent::label/following-sibling::input", customFieldName));
    }

    public List<String> getAllTactics() {
        return SAVED_TACTICS.allInnerTexts();
    }

    public void verifyDetailsTab() {
        DETAILS_TAB.isVisible();
    }

    public List<String> newTacticTabs() {
        return NEW_TACTIC_TABS.allInnerTexts();
    }

    public void clickFirstTacticTab() {
        SAVED_TACTICS.first().click();
    }

    public void clearCustomFieldText(String customFieldName) {
        Locator FIELD_OPTIONS = page.locator(String.format("//span[@class='cmp-form-label-text' and text()='%s']/parent::label/following-sibling::input", customFieldName));
        FIELD_OPTIONS.clear();
        SAVE_TACTIC_DETAILS.click();
    }

    public String validateComment(String entryPoint) {
        if (entryPoint.contains("header")) {
            HEADER_COMMENT.click();
        } else if (entryPoint.contains("navigation")) {
            NAVIGATION_COMMENT.click();
        }
        return COMMENT_TEXT_BOX.inputValue();
    }

    public void clearComment(){
        COMMENT_TEXT_BOX.clear();
        SAVE_BUTTON.click();
        waitUtility.waitForLocatorHidden(COMMENT_SUCCESS_ALERT.first());
        waitUtility.waitForElementVisible("//span[@class='notes-icon-empty-dashboard']");
    }

    public void clickCancelButton(){
        CANCEL_BUTTON.click();
    }

    public List<String> getShowExpressionRawValues() {
        return showExpressionRawValues;
    }

    public void clickSettingsTab() {
        TACTIC_SETTINGS_TAB.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void clickShowExpressionButton() {
        SHOW_EXPRESSION_BUTTON.click();
        waitUtility.waitUntilPreLoaderHidden();
    }

    public void fetchShowExpressionValues() {
        waitUtility.waitForLocatorVisible(VALUE_LOCATOR.first());
        int valueCount = VALUE_LOCATOR.count();
        int connectorCount = CONNECTION_LOCATOR.count();
        List<String> values = new ArrayList<>(valueCount * 2);

        for (int i = 0; i < valueCount; i++) {
            if (i < connectorCount) {
                values.add(CONNECTION_LOCATOR.nth(i).innerText().trim());
            } else {
                values.add("");
            }
            values.add(VALUE_LOCATOR.nth(i).innerText().trim());
        }
        showExpressionRawValues = new ArrayList<>(values); // preserve raw for connector assertion
        // Keep first occurrence order, remove duplicates, blanks, and logical connectors.
        showExpressionValues = values.stream().filter(v -> !v.isBlank() && !v.equalsIgnoreCase("AND") && !v.equalsIgnoreCase("OR")).distinct().collect(Collectors.toList());
    }

    public boolean assertShowExpressionConnectorLogic(List<String> rawValues) {
        // Keywords are at odd indices (1, 3, 5, ...), connectors at even indices (2, 4, 6, ...)
        boolean allValid = true;
        for (int i = 1; i + 2 < rawValues.size(); i += 2) {
            String leftKeyword = rawValues.get(i);
            String connector = rawValues.get(i + 1);
            String rightKeyword = rawValues.get(i + 2);
            String expectedConnector = leftKeyword.equals(rightKeyword) ? "OR" : "AND";
            if (!connector.equals(expectedConnector)) {
                allValid = false;
                break;
            }
        }
        return allValid;
    }

    public boolean ruleMappingWithShowExpressionValues(Map<String, List<String>> ruleMap, String defaultExpression) {
        List<String> ruleTypes = new ArrayList<>(ruleMap.keySet());
        List<String> ruleTypeExpressions = new ArrayList<>(ruleTypes.size());
        ruleTypeExpressions.add(defaultExpression);
        for (String ruleType : ruleTypes) {
            switch (ruleType) {
                case "Behavioral Segment":
                    ruleTypeExpressions.add("Behavioral");
                    break;
                case "Health Populations":
                    ruleTypeExpressions.add("CONDITION");
                    break;
                case "Age":
                    ruleTypeExpressions.add("AGE");
                    break;
                default:
                    ruleTypeExpressions.add(ruleType);
                    break;
            }
        }
        return showExpressionValues.equals(ruleTypeExpressions);
    }

    public void removeTargetingRule(String ruleType) {
        waitUtility.waitUntilSpinnerHidden();
        String ruleLocator = String.format("//span[text()='%s']/parent::label//following-sibling::div//div[contains(@title,'delete')]", ruleType);
        page.locator(ruleLocator).click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public boolean isForecastDataAvailable() {
        CLICK_REFRESH_BUTTON.click();
        waitUtility.waitUntilSpinnerHidden();
        List<String> forecastData = FORECAST_AVAILS_NUMBER.allInnerTexts();
        return forecastData.getLast().contains("$");
    }

    public boolean isTargetingRuleMissing() {
        waitUtility.waitForLocatorVisible(targetingTemplate.ADD_TARGETING_RULE_BUTTON);
        return NO_TARGETING_RULES.isVisible();
    }

    public void addComment(String entryPoint, String comment) {
        if (entryPoint.contains("header")) {
            HEADER_COMMENT.click();
        } else if (entryPoint.contains("navigation")) {
            NAVIGATION_COMMENT.click();
        }
        COMMENT_TEXT_BOX.fill(comment);
        SAVE_BUTTON.click();
        waitUtility.waitForLocatorHidden(COMMENT_SUCCESS_ALERT.first());
        waitUtility.waitForElementVisible("//span[@class='notes-icon-provided-dashboard']");
    }

    public List<String> allTacticsUnderLI() {
        return SAVED_TACTIC_TABS.allInnerTexts();
    }

    public String verifyTacticState() {
        return TACTIC_STATUS.innerText();
    }

    public void clickDetailsTab() {
        TACTIC_DETAILS_TAB.click();
    }

    public void addCustomField(String fieldName) {
        ADD_CUSTOM_FIELD.click();
        ADD_CUSTOM_FIELD_INPUT.fill(fieldName);
        SAVE_CUSTOM_FIELD_BUTTON.click();
        waitUtility.waitForLocatorVisible(FIELD_CREATE_SUCCESS);
        CUSTOM_FIELD_TEXT.last().fill(CommonUtils.generateRandomString());
        SAVE_TACTIC_DETAILS.click();
    }

    public String verifyCustomField(String fieldName) {
        Locator customField = page.locator(String.format("//span[contains(text(),'%s')]", fieldName));
        return customField.innerText().trim();
    }

    public void clickTactic(String tacticName) {
        TACTIC_TAB.locator("text=" + tacticName).click();
    }

    public void clickLastTactic() {
        TACTIC_TAB.last().click();
    }

    public String deleteCustomField(String customFieldName) {
        waitUtility.waitForLocatorVisible(page.locator("//app-life-custom-field-setting//label[contains(@class,'form-label')]").last());
        Locator FIELD_OPTIONS = page.locator(String.format("//span[@class='cmp-form-label-text' and text()='%s']/following-sibling::div//img[@class='three-dots']", customFieldName));
        FIELD_OPTIONS.click();
        DELETE_BUTTON.last().click();
        CONFIRM_DELETE.click();
        String text = DELETE_SUCCESS.innerText();
        waitUtility.waitForLocatorVisible(DELETE_SUCCESS);
        return text;
    }

    public void clickTargetingRuleIcon() {
        waitUtility.waitForLocatorVisible(TARGETING_RULES_ICON);
        TARGETING_RULES_ICON.click();
    }

    public String verifyTacticDetailsText() {
        return VERIFY_TACTIC_DETAILS_PAGE.innerText();
    }

    public void enterTacticName(String tacticName) {
        waitUtility.waitForLocatorVisible(ADD_CUSTOM_FIELD);
        TACTIC_NAME.fill(tacticName);
    }

    public void saveTacticDetails() {
        waitUtility.waitForLocatorVisible(SAVE_TACTIC_DETAILS);
        SAVE_TACTIC_DETAILS.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public String tacticDetailsSuccess() {
        String successMessage = TACTIC_DETAILS_SUCCESS.innerText().trim();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorHidden(TACTIC_DETAILS_SUCCESS);
        return successMessage;
    }

    public boolean createTacticWithLineItemsAndImport(List<String> lineItemTypeList, String advertiser, String campaignName, String campaignType, String budget, String lineItemName, String lineBudget, String tacticName, List<String> templateNameList, List<Map<String, String>> ruleCountAndValueList) {
        List<Map<String, String>> labelCountMapList = new ArrayList<>();
        for (String lineItemType : lineItemTypeList) {
            navigation.clickSubMenu();
            navigation.clickCampaigns();
            campaigns.campaignDashboard();
            // Campaign, Line Item and Tactic creation
            createCampaign(advertiser, campaignName + "_" + CommonUtils.timeStampCalculation(), campaignType, budget);
            createLineItem(lineItemName + "_" + CommonUtils.timeStampCalculation(), lineItemType.trim(), lineBudget);
            createTactic(tacticName + "_" + CommonUtils.timeStampCalculation());
            Map<String, String> labelCountMap = importTargetingTemplate(lineItemType.trim(), templateNameList);
            labelCountMapList.add(labelCountMap);
            saveTacticDetails();
            tacticDetailsSuccess();
        }
        ruleCountAndValueList.sort(Comparator.comparing(Object::toString));
        labelCountMapList.sort(Comparator.comparing(Object::toString));
        return ruleCountAndValueList.equals(labelCountMapList);
    }

    public List<String> createTacticWithLineItemsAndTargetingRules(List<String> lineItemTypeList, String advertiser, String campaignName, String campaignType, String budget, String lineItemName, String lineBudget, String tacticName, Map<String, List<String>> rulesMap) {
        List<String> templateNameList = new ArrayList<>();
        for (String lineItemType : lineItemTypeList) {
            navigation.clickSubMenu();
            navigation.clickCampaigns();
            campaigns.campaignDashboard();

            createCampaign(advertiser, campaignName + "_" + CommonUtils.timeStampCalculation(), campaignType, budget);
            createLineItem(lineItemName + "_" + CommonUtils.timeStampCalculation(), lineItemType.trim(), lineBudget);
            createTactic(tacticName + "_" + CommonUtils.timeStampCalculation());

            targetingTemplate.addTargetingRules(rulesMap);
            saveTacticDetails();
            waitUtility.waitUntilSpinnerHidden();
            templateNameList.add(saveTargetingTemplate(lineItemType.trim()));
        }
        return templateNameList;
    }

    public void selectManagementFeeOptionAndEnterData(String managementFeeOption, String percent, String amount, String expectedFeeValue) {
        String optionXPath = String.format("//div[contains(@class,'management-fee-contanier')]//div//button[normalize-space(text())='%s']", managementFeeOption);
        page.locator(optionXPath).click();
        switch (managementFeeOption) {
            case "Percentage" -> PERCENT_TYPE_FEE_INPUT.fill(percent);
            case "CPM", "Fixed CPM" -> DOLLAR_TYPE_FEE_INPUT.fill(amount);
            case "% + CPM" -> {
                PERCENT_TYPE_FEE_INPUT.fill(percent);
                DOLLAR_TYPE_FEE_INPUT.fill(amount);
            }
            default -> throw new IllegalArgumentException("Unexpected fee type: " + managementFeeOption);
        }
        saveTacticDetails();
        waitUtility.waitForElementVisible("//span[contains(@class,'strike-text')]");
        clickFirstTacticTab();
        clickSettingsTab();
        waitUtility.waitForElementVisible(String.format("//span[contains(text(),'%s')]", expectedFeeValue));
    }

    private void createCampaign(String advertiser, String campaignName, String campaignType, String budget) {
        campaigns.createCampaign();
        campaigns.verifyCampaignText();
        campaigns.selectAdvertiser(advertiser);
        campaigns.enterCampaignName(campaignName);
        campaigns.setCampaignType(campaignType);
        campaigns.enterBudget(budget);
        campaigns.saveCampaign();
        waitUtility.waitUntilSpinnerHidden();
        campaigns.campaignSuccess();
    }

    private void createLineItem(String lineItemName, String lineItemType, String lineBudget) {
        lineItemDetails.enterLineItemName(lineItemName);
        lineItemDetails.selectLineItemType(lineItemType);
        lineItemDetails.clickAddFlightButton();
        lineItemDetails.enterLineItemBudget(lineBudget);
        lineItemDetails.enableLineItem();
        lineItemDetails.saveLineItem();
        waitUtility.waitUntilSpinnerHidden();
        lineItemDetails.lineItemSuccess();
    }

    public void createTactic(String tacticName) {
        try {
            enterTacticName(tacticName);
            saveTacticDetails();
            waitUtility.waitUntilSpinnerHidden();
            tacticDetailsSuccess();
        } catch (PlaywrightException e) {
            saveTacticDetails();
            tacticDetailsSuccess();
        }
    }

    private Map<String, String> importTargetingTemplate(String lineItemType, List<String> templateNameList) {
        Map<String, String> labelCountMap = new LinkedHashMap<>();
        for (String templateName : templateNameList) {
            if (templateName.startsWith(lineItemType.trim() + "_")) {
                IMPORT_TEMPLATE_ICON.click();
                IMPORT_TEMPLATE_DIALOG.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                TEMPLATE_SEARCH_BOX.fill(templateName);
                page.locator(String.format("//div[contains(@class,'item text-truncate') and contains(text(), '%s')]", templateName)).click();
                IMPORT_BUTTON.click();
                if (OVERRIDE_DIALOG.isVisible()) REPLACE_BUTTON.click();
                waitUtility.waitUntilSpinnerHidden();
                TEMPLATE_IMPORT_ALERT.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
                labelCountMap = targetingTemplate.fetchTargetingRulesCountFromTargeting();
                IMPORTED_TARGET_TEMPLATE.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                break;
            }
        }
        return labelCountMap;
    }

    public String saveTargetingTemplate(String lineItemType) {
        String templateName = lineItemType + "_Template_" + CommonUtils.timeStampCalculation();
        TACTIC_SETTINGS_TAB.click();
        waitUtility.waitUntilSpinnerHidden();
        tacticSettings.verifyTacticSettingsText();
        SAVE_TEMPLATE_BUTTON.click();
        if (TARGETING_RULE_CONFIRMATION_DIALOG.isVisible()) CONTINUE_BUTTON.click();
        waitUtility.waitForLocatorVisible(SAVE_TEMPLATE_DIALOG);
        TEMPLATE_NAME_TEXT.fill(templateName);
        SAVE_BUTTON.click();
        waitUtility.waitForLocatorHidden(TEMPLATE_SAVED_SUCCESS_ALERT);
        return templateName;
    }

    public boolean createTacticWithLineItemsAndAssignCreative(String lineItemType, String advertiser, String campaignName, String campaignType, String budget, String lineItemName, String lineBudget, String tacticName, String CreativeName) {
        npiSmartList.clickPulsepointIcon();
        campaigns.campaignDashboard();

        createCampaign(advertiser, campaignName + "_" + CommonUtils.timeStampCalculation(), campaignType, budget);
        createLineItem(lineItemName + "_" + CommonUtils.generateRandomString(), lineItemType.trim(), lineBudget);
        createTactic(tacticName + "_" + CommonUtils.generateRandomString());

        tacticCreatives.clickCreativeTab();
        tacticCreatives.clickAssignCreatives();
        tacticCreatives.assignCreatives(CreativeName);
        saveTacticDetails();
        waitUtility.waitUntilSpinnerHidden();
        return tacticCreatives.verifyCreativeAssigned(CreativeName);
    }

    public void deleteTactic(String tacticName) {
        Locator tacticNameXpath = page.getByText(tacticName);
        tacticNameXpath.click();
        TACTIC_OPTIONS.click();
        TACTIC_DELETE_BUTTON.click();
        TACTIC_REMOVE_BUTTON.click();
    }

    public void bulkEnableTactics(String tacticName) {
        Locator tacticNameXpath = page.locator(String.format("//div[@class='tactic-main-details' and text()='%s']/ancestor::div[contains(@class,'tactic-list')]/preceding-sibling::div/sui-checkbox", tacticName));
        BULK_ACTION.click();
        tacticNameXpath.click();
        ENABLE_TACTIC.click();
        EXIT_BULK_MODE.click();
    }

    public void bulkDisableTactics(String tacticName) {
        Locator tacticNameXpath = page.locator(String.format("//div[@class='tactic-main-details' and text()='%s']/ancestor::div[contains(@class,'tactic-list')]/preceding-sibling::div/sui-checkbox", tacticName));
        BULK_ACTION.click();
        tacticNameXpath.click();
        DISABLE_TACTIC.click();
        EXIT_BULK_MODE.click();
    }

    public boolean getToggleClass(String tacticName) {
        Locator TACTIC_TOGGLE_CLASS = page.locator(String.format("//div[@class='tactic-main-details' and contains(text(), '%s')]/ancestor::div[contains(@class,'item-list-wrapper tactic-list')]//div[contains(@class,'item-list-control-toggle')]", tacticName));
        return TACTIC_TOGGLE_CLASS.getAttribute("class").contains("toggle-enabled");
    }

    public boolean getToggleIcon(String tacticName) {
        Locator TACTIC_NAME = page.locator(String.format("//div[@class='tactic-main-details' and contains(text(), '%s')]", tacticName));
        TACTIC_NAME.click();
        Locator TACTIC_TOGGLE = page.locator(("//label[normalize-space()='Enabled']/preceding-sibling::input"));
        return TACTIC_TOGGLE.isChecked();
    }

    public boolean getDisabledToggleClass(String tacticName) {
        Locator TACTIC_TOGGLE_CLASS = page.locator(String.format("//div[@class='tactic-main-details' and contains(text(), '%s')]/ancestor::div[contains(@class,'item-list-wrapper tactic-list')]//div[contains(@class,'item-list-control-toggle')]", tacticName));
        String cls = TACTIC_TOGGLE_CLASS.getAttribute("class");
        return !cls.contains("toggle-enabled");
    }

    public boolean getToggleDisabledIcon(String tacticName) {
        Locator TACTIC_NAME = page.locator(String.format("//div[@class='tactic-main-details' and contains(text(), '%s')]", tacticName));
        TACTIC_NAME.click();
        Locator TACTIC_TOGGLE = page.locator(("//*[@id='actionComponent']/div/div/span/sui-checkbox"));
        String cls = TACTIC_TOGGLE.getAttribute("class");
        return !cls.contains("checked");
    }

    public void globalSearchDeletedTactic(String tacticName) {
        navigation.refreshPage();
        OPEN_GLOBAL_SEARCH.click();
        GLOBAL_TACTIC_TAB.click();
        GLOBAL_SEARCH_INPUT_FIELD.fill(tacticName);
        GLOBAL_SEARCH_INPUT_FIELD.press("Enter");
    }

    public String getSearchText() {
        return TACTIC_GLOBAL_SEARCH_TEXT.innerText();
    }

    public void closeGlobalSearch() {
        CLOSE_GLOBAL_SEARCH.click();
    }

    public boolean verifyTacticAvailable(String tacticName) {
        Locator tacticItem = page.locator(String.format("//div[@class='tactic-main-details' and text()='%s']", tacticName));
        tacticItem.scrollIntoViewIfNeeded();
        return tacticItem.isVisible();
    }

    public void clickTacticOptions(String option) {
        TACTIC_OPTIONS.click();
        Locator optionXpath = page.locator(String.format("//div[contains(@class,'menu-items-popover')]/div/app-icon-lable-link[@title='%s']", option));
        waitUtility.waitForLocatorVisible(optionXpath);
        optionXpath.click();
    }

    public String createACopyOfTactic(String name) {
        TACTIC_COPY_NAME_FIELD.click();
        TACTIC_COPY_NAME_FIELD.fill(name);
        DUPLICATE_BUTTON.click();
        String text = COPY_SUCCESS_ALERT.innerText().trim();
        waitUtility.waitForLocatorHidden(COPY_SUCCESS_ALERT);
        return text;
    }

    public void navigateToTacticDetails(String tacticName) {
        page.locator(String.format("//div[@class='tactic-main-details' and text()='%s']", tacticName)).click();
    }

    public List<String> fetchTacticDetails() {
        List<String> originalTacticDetails = new ArrayList<>();
        originalTacticDetails.add(TACTIC_CHANNEL.locator("div.text").textContent().trim());
        originalTacticDetails.add(TACTIC_PRIORITY.evaluate("el => el.value").toString().trim());
        originalTacticDetails.add(TACTIC_MEDIA_OPTIMIZATION.locator("div.text").textContent().trim());
        page.waitForCondition(() -> {
            String text = DATA_COST_CPM.nth(0).textContent().trim();
            return !text.equals("—") && !text.isEmpty();
        });
        originalTacticDetails.add(DATA_COST_CPM.nth(0).textContent().trim());
        originalTacticDetails.add(HUMAN_COST_CPM.textContent().trim());
        return originalTacticDetails;
    }

    public List<String> fetchTacticCreative() {
        CREATIVE_NAME.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        return CREATIVE_NAME.allInnerTexts();
    }

    public void clickNewTacticForLineItem(String name) {
        String xpath = String.format("//div[text()='%s']/ancestor::div[contains(@class,'lineitem-list-wrapper')]//app-icon-lable-link[@class='tactic-new-button']//div", name);
        page.locator(xpath).click();
    }

    public boolean isCustomFieldAvailable(String customFieldName) {
        return page.locator(String.format("//label[contains(text(),'%s')]", customFieldName)).isVisible();
    }

    public void createLineItemsWithTacticsAndTargetingRules(List<Map<String, String>> rows, String creative, Consumer<Map<String, List<String>>> perTacticVerification) {
        String currentLiName = null;

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            String liType = row.get("LI_TYPE");
            String liName = row.get("LI_NAME");
            String liBudget = row.get("LI_BUDGET");
            String tacticName = row.get("TACTIC_NAME");
            String channel = row.get("CHANNEL");

            if (!liName.equals(currentLiName)) {
                if (currentLiName != null) {
                    lineItemDetails.selectNewLineItem();
                }
                lineItemDetails.enterLineItemName(liName);
                lineItemDetails.selectLineItemType(liType);
                lineItemDetails.clickAddFlightButton();
                lineItemDetails.enterLineItemBudget(liBudget);
                lineItemDetails.enableLineItem();
                lineItemDetails.saveLineItem();
                waitUtility.waitUntilSpinnerHidden();
                lineItemDetails.lineItemSuccess();
                currentLiName = liName;
            }

            createTactic(tacticName);
            tacticSettings.selectChannel(channel);
            navigation.clickOnIcon("Add Targeting Rule");

            Map<String, List<String>> perTacticRules = new LinkedHashMap<>();
            for (int j = 1; row.containsKey("RULE_" + j); j++) {
                String rule = row.get("RULE_" + j);
                String values = row.get("VALUES_" + j);
                if (rule != null && !rule.isEmpty()) {
                    List<String> parsedValues = CommonUtils.parseCommaSeparatedString(values);
                    tacticSettings.selectMultipleRuleTypes(rule, parsedValues);
                    perTacticRules.put(rule, parsedValues);
                }
            }

            tacticSettings.closeRuleTypePanel();

            if (perTacticVerification != null) {
                perTacticVerification.accept(perTacticRules);
            }

            saveTacticDetails();
            navigation.clickOnIcon("Assign Existing Creatives");
            tacticCreatives.assignCreatives(creative);
            tacticCreatives.enableCreative();
            tacticCreatives.saveTacticCreatives();
            waitUtility.waitUntilSpinnerHidden();

            if (i + 1 < rows.size()) {
                String nextLiName = rows.get(i + 1).get("LI_NAME");
                if (nextLiName.equals(liName)) {
                    addNewTactic();
                }
            }
        }
    }

    public void addNewTactic() {
        NEW_TACTIC_BUTTON.click();
        waitUtility.waitForLocatorVisible(CUSTOM_FIELD);
    }
}
