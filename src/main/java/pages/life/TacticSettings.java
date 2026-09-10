package pages.life;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import factory.DriverFactory;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import utils.WaitUtility;

public class TacticSettings {
    private final Page page;
    private final Locator VERIFY_TACTIC_SETTINGS_PAGE;
    private final Locator SELECT_CHANNEL;
    private final Locator SEARCH_RULE_TYPE;
    private final Locator SELECT_RULE_TYPE;
    private final Locator SELECT_TARGETING;
    private final Locator SELECT_OPTION;
    private final Locator RULE_TYPE_OK_BUTTON;
    private final Locator RULE_TYPE_CLOSE;
    private final Locator SAVE_TACTIC_SETTINGS;
    private final Locator TACTIC_SETTINGS_SUCCESS;
    private final Locator SEARCH_RULE_OPTION;
    private final Locator RULE_POSTAL_CODES_TEXTBOX;
    private final Locator RULE_DEVICE_BLOCK;
    private final Locator RULE_LEGAL_POPULATIONS_HOUSEHOLD_TAB;
    private final Locator VERIFY_NPI;
    private final Locator FETCH_TARGET_RULE_TYPES;
    private final Locator FETCH_TARGET_RULE_OPTIONS;
    private final Locator EXPAND_TARGETING_ICONS;
    private final Locator TARGET_CATEGORY_NAME;
    private final Locator PERSON_TAB;
    private final Locator HOUSEHOLD_TAB;
    private final Locator HOUSEHOLD_IP_TAB;
    private final Locator HEALTH_POPULATIONS_TREATMENTS_OPTION;
    private final Locator RULE_INDIVIDUAL_KEYWORDS_OPTION;
    private final Locator KEYWORD_POPULATIONS_TEXTBOX;
    private final Locator GEO_TARGETS_BULK_UPLOAD;
    private final Locator GEO_TARGETS_UPLOAD_BUTTON;
    private final Locator GEO_TARGETS_TEXTBOX;
    private final Locator BRAND_SAFETY_PROFILE_SEGMENT_ID;
    private final Locator RULE_APP_BUNDLES_LISTS_OPTION;
    private final Locator VIEW_ABILITY_PERCENTAGE_BOX;
    private final Locator KEYWORDS_TEXTBOX;
    private final Locator GEO_RADIUS_ADD_POINT;
    private final Locator GEO_RADIUS_LAT;
    private final Locator GEO_RADIUS_LONG;
    private final Locator GEO_RADIUS_DISTANCE;
    private final Locator GEO_RADIUS_POINT_NAME;
    private final Locator GEO_RADIUS_SAVE;
    private final Locator TOTAL_NPI_COUNT;
    private final Locator SELECTED_LIST;
    private final Locator SHOW_MATCHED_NPI_BUTTON;
    private final Locator MATCHED_NPI_COUNT;
    private final Locator TARGETING_RULES_PANEL_TITLE;
    private final Locator KEYWORD_CUSTOM_LIST;
    private final Locator KEYWORD_SELECTED_LIST;
    private final Locator SHOW_MORE_BUTTON;
    private final Locator BLOCK_TARGETING;
    private final Locator HOUSEHOLD_IP_ICON;
    private final Locator PRACTICE_IP_ICON;
    private final Locator SELECTED_ONLY_TAB;
    private final Locator PRACTICE_IP;
    private final Locator TACTIC_MAX_BID_PRICE;
    private final Locator TACTIC_BASE_BID_PRICE;
    private final Locator TACTIC_NAME;
    private final Locator DISPLAY_TACTIC_NAME;
    private final Locator SELECTED_TARGET;
    private final Locator BLOCKED_TARGET;
    private final Locator SELECTED_TARGET_HP;
    private final Locator BLOCKED_TARGET_HP;
    private final Locator TARGET;
    private final Locator BLOCK;
    private final Locator NEW_TACTIC;
    private final Locator BASE_BID_ERROR;
    private final Locator CANCEL_BUTTON;
    private final Locator NPI_TREE_VIEW_NODE;
    private final Locator NPI_FACILITY_AFFILIATION_SEARCH;
    private final Locator APP_TREE_VIEW_NODE;
    private final Locator TARGETING_SEGMENT;
    private final Locator TARGETING_PANEL_TEXTAREA;
    private final Locator MANAGEMENT_FEE_LABEL_VALUE;
    private final Locator MANAGEMENT_FEE_OVERRIDE;
    private final Locator MANAGEMENT_FEE_OPTIONS;
    private final Locator NEW_TARGETING_RULE_BUTTON;
    private final Locator ADD_BID_MULTIPLIER;
    private final Locator BEHAVIOUR_SEGMENT;
    private final Locator BEHAVIOUR_SEGMENT_ERROR;
    private final Locator BID_PANEL_CANCEL_BUTTON;
    private final Locator NPI_BID;
    private final Locator NPI_ERROR;
    private final Locator INACTIVE_PANEL;
    private final Locator ADD_TARGETING;
    private final Locator BID_MULTIPLIER_CATEGORY_NAME;
    private final Locator FETCH_BID_MULTIPLIER_RULE_TYPES;
    private final Locator FETCH_BID_MULTIPLIER_RULE_OPTIONS;
    private final Locator CREATIVE_TAB;
    private final Locator PERCENT_TYPE_FEE_INPUT;
    private final Locator DOLLAR_TYPE_FEE_INPUT;
    private final Locator BID_PANEL;
    public final Set<String> ACTUAL_TARGET_RULE = new HashSet<>();
    public final Set<String> EXPECTED_TARGET_RULE = new HashSet<>();
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());
    List<Object> ruleTypes;
    List<Object> ruleOptions;

    public TacticSettings(Page page) {
        this.page = page;
        this.VERIFY_TACTIC_SETTINGS_PAGE = page.locator("//div[text()='Bid Strategy']");
        this.SELECT_CHANNEL =
                page.locator("//label[text()='Channel']/following-sibling::div//div[@id='billingTypeDropdown']");
        this.SEARCH_RULE_TYPE = page.locator("//input[@name='search']");
        this.SELECT_RULE_TYPE = page.locator("(//a[@classname='target-tooltip'])[1]");
        this.SELECT_OPTION = page.locator("(//div[contains(@class,'include-default')])[1]");
        this.RULE_TYPE_OK_BUTTON =
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ok").setExact(true));
        this.RULE_TYPE_CLOSE = page.locator("//div[contains(@class,'close_icon')]");
        this.SAVE_TACTIC_SETTINGS = page.locator("//app-ds-button-wrapper[@label='Save']");
        this.TACTIC_SETTINGS_SUCCESS = page.locator("//div[@aria-label='Success!']");
        this.SEARCH_RULE_OPTION =
                page.locator("//input[contains(@placeholder,'Search') and contains(@class,'panel-search')]");
        this.RULE_POSTAL_CODES_TEXTBOX = page.locator("//div[@id='targetedItemsTA']");
        this.RULE_DEVICE_BLOCK = page.locator(
                "//sui-radio-button[contains(@class,'ui radio checkbox')]//label[text()='Block Selected']");
        this.RULE_LEGAL_POPULATIONS_HOUSEHOLD_TAB = page.locator("//button[normalize-space(text())='Household']");
        this.VERIFY_NPI = page.locator("//span[contains(@class,'target-item')]/span[normalize-space(text())='NPI']");
        this.FETCH_TARGET_RULE_TYPES = page.locator("//span[contains(@class,'target-item__label')]");
        this.FETCH_TARGET_RULE_OPTIONS = page.locator("//app-ds-pill-wrapper");
        this.EXPAND_TARGETING_ICONS = page.locator("//i[@class='dropdown icon gaExpandTargeting']");
        this.TARGET_CATEGORY_NAME = page.locator("//div[contains(@class,'targetCategoryName')]");
        this.PERSON_TAB = page.locator("//button[normalize-space(text())='Person']");
        this.HOUSEHOLD_TAB = page.locator("//button[normalize-space(text())='Household']");
        this.HOUSEHOLD_IP_TAB = page.locator("//button[normalize-space(text())='Household IP']");
        this.HEALTH_POPULATIONS_TREATMENTS_OPTION =
                page.locator("//div[contains(@class,'vertical-tab')]//a[contains(text(),'Treatments')]");
        this.RULE_INDIVIDUAL_KEYWORDS_OPTION =
                page.locator("//div[contains(@class,'vertical-tab')]//a[contains(text(),'Individual Keywords')]");
        this.KEYWORD_POPULATIONS_TEXTBOX = page.locator("//div[contains(@class,'keywordContainer')]//textarea");
        this.GEO_TARGETS_BULK_UPLOAD = page.locator("//span[text()='Bulk Upload']");
        this.GEO_TARGETS_UPLOAD_BUTTON = page.locator("//button[normalize-space()='Upload']");
        this.GEO_TARGETS_TEXTBOX = page.locator("//textarea[@id='geotargetedItemsTA']");
        this.BRAND_SAFETY_PROFILE_SEGMENT_ID = page.locator(
                "//span[contains(text(),'DoubleVerify Authentic Brand Suitability ID')]/parent::div/following-sibling::div//input[@type='text']");
        this.RULE_APP_BUNDLES_LISTS_OPTION =
                page.locator("//div[contains(@class,'vertical-tab')]//a[contains(text(),'App Bundles Lists')]");
        this.VIEW_ABILITY_PERCENTAGE_BOX = page.locator(
                "//div[contains(@class, 'rightLabel')]//input[contains(@class, 'form-control-percent-mini-right')]");
        this.KEYWORDS_TEXTBOX = page.locator("//div[contains(@class,'text-area-container')]//textarea");
        this.GEO_RADIUS_ADD_POINT =
                page.locator("//div[normalize-space()='Add Point' and contains(@class,'semi-bold')]");
        this.GEO_RADIUS_LAT =
                page.locator("//tr[contains(@class,'geopointRowInEdit')]//input[@formcontrolname='latitude']");
        this.GEO_RADIUS_LONG =
                page.locator("//tr[contains(@class,'geopointRowInEdit')]//input[@formcontrolname='longitude']");
        this.GEO_RADIUS_DISTANCE =
                page.locator("//tr[contains(@class,'geopointRowInEdit')]//input[@formcontrolname='distance']");
        this.GEO_RADIUS_POINT_NAME =
                page.locator("//tr[contains(@class,'geopointRowInEdit')]//input[@formcontrolname='name']");
        this.GEO_RADIUS_SAVE = page.locator("(//div[@title='Save' and contains(@class,'saveGeoPtButton')])[1]");
        this.TOTAL_NPI_COUNT = page.locator("//div[@class='supportedNPIsNumber']");
        this.SELECTED_LIST = page.locator("//span[contains(text(),'Selected Only')]");
        this.SHOW_MATCHED_NPI_BUTTON = page.locator("//span[contains(text(),'show')]");
        this.MATCHED_NPI_COUNT = page.locator("//div[@class='supportedNPIsNumber']/span[@class='supportedNPIsNumber']");
        this.TARGETING_RULES_PANEL_TITLE =
                page.locator("//div[text()='HCP Direct Match Targeting' or text()='Targeting Rule']");
        this.KEYWORD_CUSTOM_LIST =
                page.locator("//div[contains(@class,'vertical-tab')]//a[contains(text(),'Custom Lists')]");
        this.KEYWORD_SELECTED_LIST = page.locator(
                "//span[contains(text(),'Custom Keyword')]/following-sibling::span[contains(text(),'Selected Only')]");
        this.SHOW_MORE_BUTTON = page.locator("//button[contains(@class,'show-more-button')]");
        this.SELECT_TARGETING = page.locator("//div[@title='Target'] | //button[@title='Target']");
        this.BLOCK_TARGETING = page.locator("//div[@title='Block'] | //button[@title='Block']");
        this.PRACTICE_IP_ICON = page.locator("//span[contains(@class,'practiceIp')]");
        this.HOUSEHOLD_IP_ICON = page.locator("//span[contains(@class,'householdIp')]");
        this.PRACTICE_IP = page.locator("//button[normalize-space(text())='Practice IP']");
        this.TACTIC_MAX_BID_PRICE =
                page.locator("//input[@type='text' and @formcontrolname='maxBidPrice' and @id='maxBidPrice']");
        this.TACTIC_BASE_BID_PRICE = page.locator("//input[@type='text' and @formcontrolname='cost' and @id='maxBod']");
        this.TACTIC_NAME = page.locator("#lidcBody div")
                .filter(new Locator.FilterOptions())
                .first();
        this.SELECTED_ONLY_TAB = page.locator("//span[contains(text(),'Selected Only')]");
        this.BLOCKED_TARGET = page.locator("//div[contains(@class,'danger')]");
        this.SELECTED_TARGET = page.locator("//div[contains(@class,'success')]");
        this.SELECTED_TARGET_HP = page.locator("//div[contains(@class,'targetGreen')]");
        this.BLOCKED_TARGET_HP = page.locator("//div[contains(@class,'targetRed')]");
        this.TARGET = page.locator("//div[contains(@class,'text-target')]");
        this.BLOCK = page.locator("//div[contains(@class,'text-block')]");
        this.DISPLAY_TACTIC_NAME = page.locator("//div[@class='tactic-main-details']");
        this.NEW_TACTIC = page.locator("app-icon-lable-link")
                .filter(new Locator.FilterOptions().setHasText("New Tactic"))
                .locator("img");
        this.BASE_BID_ERROR = page.locator(
                "//div[contains(normalize-space(@aria-label), 'Base Bid Price can not exceed Max Bid Price') or contains(normalize-space(@aria-label), 'Your Account Manager has limited Max Bid')]");
        this.CANCEL_BUTTON = page.locator("//div[contains(@class,'gaCancel')]");
        this.NPI_TREE_VIEW_NODE = page.locator("//div[@class='npi-list']//div[contains(@class,'treeviewNode')]");
        this.NPI_FACILITY_AFFILIATION_SEARCH = page.locator("//input[contains(@id,'searchTextId')]");
        this.APP_TREE_VIEW_NODE = page.locator("//app-treeview//div[contains(@class,'treeviewNode')]");
        this.TARGETING_SEGMENT = page.locator("//div[contains(text(),'Targeting Segments')]");
        this.TARGETING_PANEL_TEXTAREA = page.locator("//div[contains(@class,'editableTextarea')]");
        this.MANAGEMENT_FEE_LABEL_VALUE = page.locator("//span[contains(@class,'fee-value')]");
        this.MANAGEMENT_FEE_OVERRIDE = page.locator("//label[contains(text(),'Override')]");
        this.MANAGEMENT_FEE_OPTIONS = page.locator("//div[contains(@class,'management-fee-contanier')]//div//button");
        this.NEW_TARGETING_RULE_BUTTON = page.locator("//span[text()='New Targeting Rule']");
        this.ADD_BID_MULTIPLIER =
                page.locator("//div[contains(@class,'no_content_center')]//span[text()='Add Bid Multiplier']");
        this.BEHAVIOUR_SEGMENT =
                page.locator("//div[contains(@class,'behavior') and contains(@class,'bidMultiplierImages')]");
        this.BEHAVIOUR_SEGMENT_ERROR = page.locator("//div[contains(@class,'noDataMessageHeader')]");
        this.BID_PANEL_CANCEL_BUTTON = page.locator("//button[contains(@class,'cancelbtn')]");
        this.NPI_BID = page.locator("//div[contains(@class,'npi') and contains(@class,'bidMultiplierImages')]");
        this.NPI_ERROR = page.locator("//div[contains(@class,'noDataMessageHeader')]");
        this.INACTIVE_PANEL =
                page.locator("//div[contains(@class, 'right iconSprite icons_20-close pointer close_icon')]");
        this.ADD_TARGETING = page.locator("//span[text()='Add Targeting Rule']");
        this.BID_MULTIPLIER_CATEGORY_NAME =
                page.locator("//app-rightpanel//div[contains(@class,'bidMultiplierCategoryName')]");
        this.FETCH_BID_MULTIPLIER_RULE_TYPES =
                page.locator("//div[contains(@class,'bidMultiplierData')]//div[contains(@class,'bold')]");
        this.FETCH_BID_MULTIPLIER_RULE_OPTIONS = page.locator("//div[contains(@class,'bmtName')]");
        this.CREATIVE_TAB = page.locator("(//div[contains(@class,'navbar')]//a[contains(@class,'gaTabCreatives')])[1]");
        this.PERCENT_TYPE_FEE_INPUT = page.locator(
                "//div[contains(@class,'management-fee-container')]//input[contains(@class,'percent-img')]");
        this.DOLLAR_TYPE_FEE_INPUT = page.locator(
                "//div[contains(@class,'management-fee-container')]//input[contains(@class,'doller-img')]");
        this.BID_PANEL = page.locator("//div[@class='bidMultiplierCategoryName ng-star-inserted' and contains(text(), 'AUDIENCE ATTRIBUTE')]");

    }

    public String verifyTacticSettingsText() {
        return VERIFY_TACTIC_SETTINGS_PAGE.innerText();
    }

    public void selectChannel(String channel) {
        page.waitForLoadState(LoadState.LOAD);
        if (SELECT_CHANNEL.isVisible()) {
            SELECT_CHANNEL.click();
            SELECT_CHANNEL.locator("text=" + channel).first().click();
        }
    }

    public void selectRuleType(String ruleType) {
        searchAndSelectRuleType(ruleType);
        SELECT_OPTION.click();
        clickOk();
        clickClose();
    }

    public int selectRuleType(String ruleType, String ruleOption) {
        searchAndSelectRuleType(ruleType);
        SEARCH_RULE_OPTION.fill(ruleOption);

        String pixelXpath;
        switch (ruleType) {
            case "Retargeting Pixels":
                pixelXpath = String.format(
                        "//div[@title='%s']/preceding-sibling::div[contains(@class,'iconsWrapper')]//div[contains(@class,'include-default')]",
                        ruleOption);
                isElementVisible(pixelXpath);
                break;
            case "NPI":
                pixelXpath = String.format(
                        "(//mark[contains(text(), '%s')]/ancestor::div[contains(@class, 'npilist-itemWrapper')]//div[contains(@class, 'include-default')])[1]",
                        ruleOption);
                isElementVisible(pixelXpath);
                break;
            case "Converters":
                pixelXpath = String.format(
                        "//span[contains(normalize-space(),'%s')]/parent::div/preceding-sibling::div[contains(@class,'targetBlockIcons')]//div[@title='Target']",
                        ruleOption);
                isElementVisible(pixelXpath);
                break;
        }
        int selectCount = fetchSelectedListCountFromTargetingPanel();
        clickRuleTypeOkButton();
        closeRuleTypePanel();

        return selectCount;
    }

    public void saveTacticSettings() {
        SAVE_TACTIC_SETTINGS.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void searchAndSelectRuleType(String ruleType) {
        SEARCH_RULE_TYPE.fill(ruleType);
        SEARCH_RULE_TYPE.press("Enter");
        SELECT_RULE_TYPE.click();
    }

    public boolean isManagementFeeSectionVisible() {
        return MANAGEMENT_FEE_LABEL_VALUE.isVisible();
    }

    public String fetchDisplayedManagementFeeValue() {
        return MANAGEMENT_FEE_LABEL_VALUE.innerText().trim();
    }

    public boolean isManagementFeeOverrideVisible() {
        return MANAGEMENT_FEE_OVERRIDE.isVisible();
    }

    public String fetchSelectedManagementFeeOption() {
        for (int i = 0; i < MANAGEMENT_FEE_OPTIONS.count(); i++) {
            String classAttr = MANAGEMENT_FEE_OPTIONS.nth(i).getAttribute("class");
            if (classAttr != null && classAttr.contains("active")) {
                return MANAGEMENT_FEE_OPTIONS.nth(i).innerText().trim();
            }
        }
        return "";
    }

    public List<String> fetchEnteredManagementFeeValues() {
        List<String> values = new ArrayList<>();
        if (PERCENT_TYPE_FEE_INPUT.isVisible())
            values.add(PERCENT_TYPE_FEE_INPUT.inputValue().trim());
        if (DOLLAR_TYPE_FEE_INPUT.isVisible())
            values.add(DOLLAR_TYPE_FEE_INPUT.inputValue().trim());
        return values;
    }

    public void addTargetingRules(String ruleType) {
        searchAndSelectRuleType(ruleType);
        switch (ruleType) {
            case "Health Population":
                HOUSEHOLD_IP_TAB.click();
                break;
            case "NPI":
                PRACTICE_IP.click();
                SHOW_MORE_BUTTON.click();
                SHOW_MORE_BUTTON.click();
                break;
            case "Behavioral Segment":
                HOUSEHOLD_IP_TAB.click();
                SHOW_MORE_BUTTON.click();
                break;
        }
        performTargetingActions(ruleType);
    }

    private void performTargetingActions(String ruleType) {
        SELECT_TARGETING.first().click();
        if (ruleType.equals("Behavioral Segment") || ruleType.equals("NPI")) {
            SHOW_MORE_BUTTON.click();
        }
        BLOCK_TARGETING.last().click();
        SELECTED_ONLY_TAB.click();
        EXPECTED_TARGET_RULE.add(
                (ruleType.equals("Health Population") ? SELECTED_TARGET_HP : SELECTED_TARGET.last()).innerText());
        EXPECTED_TARGET_RULE.add(
                (ruleType.equals("Health Population") ? BLOCKED_TARGET_HP : BLOCKED_TARGET.last()).innerText());
        clickOk();
        clickClose();
        waitUtility.waitForLocatorVisible(ruleType.equals("NPI") ? PRACTICE_IP_ICON : HOUSEHOLD_IP_ICON);
        String selectedTargets = TARGET.first()
                .innerText()
                .replaceAll("\\s*\\(\\d+\\)\\s*", "")
                .replaceAll("[\\r\\n]+", "")
                .replaceAll("\\u00A0", "")
                .trim();
        String blockedTargets = BLOCK.first()
                .innerText()
                .trim()
                .replaceAll("\\s*\\(\\d+\\)\\s*", "")
                .replaceAll("[\\r\\n]+", "")
                .replaceAll("\\u00A0", "")
                .trim();
        ACTUAL_TARGET_RULE.add(blockedTargets);
        ACTUAL_TARGET_RULE.add(selectedTargets);
    }

    public List<String> getExpectedTargetRules() {
        return new ArrayList<>(EXPECTED_TARGET_RULE);
    }

    public List<String> getActualTargetRules() {
        return new ArrayList<>(ACTUAL_TARGET_RULE);
    }

    public String tacticSettingsSuccess() {
        return TACTIC_SETTINGS_SUCCESS.innerText();
    }

    public void selectMultipleRuleTypes(String ruleType, List<String> ruleValues) {
        SEARCH_RULE_TYPE.clear();
        SEARCH_RULE_TYPE.type(ruleType);
        if (SELECT_RULE_TYPE.isVisible()) {
            SELECT_RULE_TYPE.click();
            waitUtility.waitUntilSpinnerHidden();

            switch (ruleType) {
                case "Behavioral Segment":
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        String xpath = String.format(
                                "(//span[contains(text(), '%s')]/ancestor::div[contains(@class, 'segmentname')]/preceding-sibling::div[contains(@class, 'iconsWrapper')]//div[contains(@class, 'include-default')])[1]",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "NPI":
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        String xpath = String.format(
                                "(//mark[contains(text(), '%s')]/ancestor::div[contains(@class, 'npilist-itemWrapper')]//div[contains(@class, 'include-default')])[1]",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "HCP by Specialty", "Venue Type":
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        String xpath = String.format(
                                "(//mark[contains(text(), '%s')]/ancestor::div[contains(@class, 'left name-icon')]/preceding-sibling::div[contains(@class,'left targetBlockIcons')]/div[@title='Target'])[1]",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Health Populations", "Health Populations+":
                    HOUSEHOLD_IP_TAB.click();
                    HEALTH_POPULATIONS_TREATMENTS_OPTION.click();
                    waitUtility.waitForLocatorVisible(APP_TREE_VIEW_NODE.last());
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        String xpath = String.format(
                                "(//mark[contains(text(), '%s')]/ancestor::div[contains(@class, 'left name-icon')]/preceding-sibling::div[contains(@class,'left targetBlockIcons')]/button[@title='Target'])[1]",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Keyword Populations":
                    PERSON_TAB.click();
                    RULE_INDIVIDUAL_KEYWORDS_OPTION.click();
                    for (String val : ruleValues) {
                        KEYWORD_POPULATIONS_TEXTBOX.type(val.trim());
                        KEYWORD_POPULATIONS_TEXTBOX.press("Enter");
                        page.waitForLoadState(LoadState.LOAD);
                    }
                    break;
                case "Practice Staff":
                    HOUSEHOLD_TAB.click();
                    waitUtility.waitForLocatorVisible(NPI_TREE_VIEW_NODE.first());
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        String xpath = String.format(
                                "(//span[contains(text(), '%s')]/ancestor::div[contains(@class, 'itemWrapper')]//div[contains(@class, 'include-default')])[1]",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "IP":
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        String xpath = String.format(
                                "(//div[contains(text(), '%s')]/ancestor::div[contains(@class, 'left cliptext')]/preceding-sibling::div[contains(@class, 'left iconsWrapper')]//div[contains(@class, 'include-default')])[1]",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "In Condition":
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        String xpath = String.format(
                                "//mark[contains(text(), '%s')]/ancestor::div[contains(@class, 'left name-icon')]/preceding-sibling::div[contains(@class,'left targetBlockIcons')]/button[@title='Target']",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Age", "Browser", "Invalid Traffic", "Ethnicity", "Gender":
                    for (String val : ruleValues) {
                        String xpath = String.format("//label[contains(text(),'%s')]", val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Health Pages":
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.first().fill(val);
                        String xpath = String.format(
                                "//span[contains(text(),'%s')]/ancestor::div[contains(@class,'left name-icon')]/preceding-sibling::div[contains(@class,'left targetBlockIcons')]/div[@title='Target']",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Keywords":
                    RULE_INDIVIDUAL_KEYWORDS_OPTION.click();
                    for (String val : ruleValues) {
                        KEYWORDS_TEXTBOX.type(val.trim());
                        KEYWORDS_TEXTBOX.press("Enter");
                        page.waitForLoadState(LoadState.LOAD);
                    }
                    break;
                case "Endemics":
                    for (String val : ruleValues) {
                        String xpath = String.format("//sui-radio-button//label[normalize-space(text())='%s']", val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Geo Targets":
                    GEO_TARGETS_BULK_UPLOAD.click();
                    GEO_TARGETS_TEXTBOX.click();
                    for (String val : ruleValues) {
                        GEO_TARGETS_TEXTBOX.type(val.trim());
                        GEO_TARGETS_TEXTBOX.press("Enter");
                        page.waitForLoadState(LoadState.LOAD);
                    }
                    GEO_TARGETS_UPLOAD_BUTTON.click();
                    break;
                case "Geo Radius":
                    GEO_RADIUS_ADD_POINT.click();
                    for (String val : ruleValues) {
                        String[] parts = val.split("::");
                        String latitude = parts[0].trim();
                        String longitude = parts[1].trim();
                        String distance = parts[2].trim();
                        String pointName = parts.length > 3 ? parts[3].trim() : "";
                        GEO_RADIUS_LAT.fill(latitude);
                        GEO_RADIUS_LONG.fill(longitude);
                        GEO_RADIUS_DISTANCE.fill(distance);
                        if (!pointName.isEmpty()) {
                            GEO_RADIUS_POINT_NAME.fill(pointName);
                        }
                        GEO_RADIUS_SAVE.click();
                    }
                    break;
                case "Postal Codes":
                    TARGETING_PANEL_TEXTAREA.click();
                    for (String val : ruleValues) {
                        TARGETING_PANEL_TEXTAREA.type(val.trim());
                        TARGETING_PANEL_TEXTAREA.press("Enter");
                        page.waitForLoadState(LoadState.LOAD);
                    }
                    break;
                case "Weather Signals":
                    waitUtility.waitForLocatorVisible(APP_TREE_VIEW_NODE.last());
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        String xpath = String.format(
                                "(//mark[contains(text(),'%s')]/ancestor::div[contains(@class,'treeviewNode')]//div[contains(@class,'include-default')])[1]",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Brand Safety Profile":
                    String segmentID = ruleValues.get(0);
                    BRAND_SAFETY_PROFILE_SEGMENT_ID.fill(segmentID);
                    page.locator("body").click();
                    break;
                case "Brand Suitability":
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        String xpath = String.format(
                                "//mark[contains(text(),'%s')]/ancestor::div[contains(@class,'left name-icon')]/preceding-sibling::div[contains(@class,'custom_checkbox')]",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Device", "Operating System":
                    RULE_DEVICE_BLOCK.click();
                    for (String val : ruleValues) {
                        String xpath = String.format("//label[contains(text(),'%s')]", val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Domains/Apps":
                    RULE_APP_BUNDLES_LISTS_OPTION.click();
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        String xpath = String.format(
                                "//span[text()='%s']/ancestor::div[@class='cliptext']/preceding-sibling::div[@class='target_icon h-20']//div[contains(@class,'include-default')]",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Inventory Source":
                    for (String val : ruleValues) {
                        String xpath = String.format(
                                "//span[text()='%s']/ancestor::div[contains(@class,'name-icon')]/preceding-sibling::div[contains(@class,'targetBlockIcons')]//div[contains(@class,'include-default')]",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Viewability":
                    String percent = ruleValues.get(0);
                    VIEW_ABILITY_PERCENTAGE_BOX.fill(percent);
                    break;
                case "Legal Pages":
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        String xpath = String.format(
                                "//mark[text()='%s']/ancestor::div[contains(@class,'treeviewNode')]//button[contains(@class,'include-default')]",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Legal Populations":
                    RULE_LEGAL_POPULATIONS_HOUSEHOLD_TAB.click();
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        String xpath = String.format(
                                "//span/mark[contains(text(), '%s')]/ancestor::div[contains(@class, 'left name-icon')]/preceding-sibling::div[contains(@class, 'left targetBlockIcons')]//button[contains(@class, 'include-default')]",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "NPI Facility Affiliation":
                    for (String val : ruleValues) {
                        NPI_FACILITY_AFFILIATION_SEARCH.fill(val);
                        page.keyboard().press("Enter");
                        String xpath = String.format(
                                "//mark[contains(text(),'%s')]/parent::div[contains(@style,'display:')]/preceding-sibling::div//div[@title='Target']",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Retargeting Pixels":
                    PERSON_TAB.click();
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        String xpath = String.format(
                                "(//div[contains(@title,'%s')]/preceding-sibling::div//div[@title='Target'])[1]", val);
                        isElementVisible(xpath);
                    }
                    break;
                case "OTC Populations":
                    HOUSEHOLD_IP_TAB.click();
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        String xpath = String.format(
                                "(//mark[contains(text(),'%s')]/ancestor::div//preceding-sibling::div//div[@title='Target'])[1]",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Clickers":
                    waitUtility.waitForLocatorVisible(APP_TREE_VIEW_NODE.last());
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        String xpath = String.format(
                                "(//mark[contains(text(),'%s')]/ancestor::div//preceding-sibling::div//div[@title='Target'])[1]",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Email":
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        String xpath = String.format(
                                "(//div[contains(text(),'%s')]/parent::div//preceding-sibling::div//div[@title='Target'])[1]",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Sensitive Areas":
                    waitUtility.waitForLocatorVisible(APP_TREE_VIEW_NODE.last());
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        page.keyboard().press("Enter");
                        String xpath = String.format(
                                "//mark[text()='%s']/ancestor::div/preceding-sibling::div//button[@title='Target' and not(@hidden)]",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "IAB Categories", "IAB Categories New", "Language":
                    waitUtility.waitForLocatorVisible(APP_TREE_VIEW_NODE.last());
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        page.keyboard().press("Enter");
                        String xpath = String.format(
                                "//mark[text()='%s']/ancestor::div[contains(@class,'name-icon')]/preceding-sibling::div//div[@title='Target']",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "IAS Context Control":
                    for (String val : ruleValues) {
                        TARGETING_SEGMENT.click();
                        waitUtility.waitForLocatorVisible(APP_TREE_VIEW_NODE.last());
                        SEARCH_RULE_OPTION.fill(val);
                        page.keyboard().press("Enter");
                        String xpath = String.format(
                                "(//mark[text()='%s']/ancestor::div[contains(@class,'name-icon')]/preceding-sibling::div//div[@title='Target'])[1]",
                                val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Inventory Type", "Video Placement":
                    for (String val : ruleValues) {
                        String xpath = String.format("//sui-checkbox//label[text()='%s']", val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Video Size":
                    for (String val : ruleValues) {
                        String xpath = String.format(
                                "//span[text()='%s']/parent::div/preceding-sibling::div//div[@title='Target']", val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Video Skipping":
                    for (String val : ruleValues) {
                        String xpath = String.format("//sui-radio-button//label[text()='%s']", val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Search Keywords", "Area Codes", "Custom Targeting Bundle":
                    for (String val : ruleValues) {
                        TARGETING_PANEL_TEXTAREA.type(val.trim());
                        TARGETING_PANEL_TEXTAREA.press("Enter");
                    }
                    break;
                case "Bespoke":
                    for (String val : ruleValues) {
                        SEARCH_RULE_OPTION.fill(val);
                        String xpath = String.format(
                                "(//div[contains(text(), '%s')]/preceding-sibling::div/div[@title='Target'])[1]", val);
                        isElementVisible(xpath);
                    }
                    break;
                case "Audience Multiplier":
                    if (ruleValues.size() == 1) {
                        String rangeValue = ruleValues.get(0).trim();
                        String[] minMax = rangeValue.split("-");

                        if (minMax.length != 2) {
                            throw new IllegalArgumentException(
                                    "Audience Multiplier requires a range format like 'Min-Max'. Found: " + rangeValue);
                        }

                        String minValue = minMax[0].trim();
                        String maxValue = minMax[1].trim();

                        Locator minHandle = page.locator("span.ngx-slider-pointer-min");
                        Locator maxHandle = page.locator("span.ngx-slider-pointer-max");
                        Locator minTarget = page.locator(String.format(
                                "//span[contains(@class, 'ngx-slider-tick-legend') and normalize-space()='%s']",
                                minValue));
                        Locator maxTarget = page.locator(String.format(
                                "//span[contains(@class, 'ngx-slider-tick-legend') and normalize-space()='%s']",
                                maxValue));

                        minHandle.dragTo(minTarget);
                        Locator minSelected = page.locator(String.format(
                                "//span[contains(@class, 'ngx-slider-selected') and normalize-space()='%s']",
                                minValue));
                        waitUtility.waitForLocatorVisible(minSelected);

                        maxHandle.dragTo(maxTarget);
                        Locator maxSelected = page.locator(String.format(
                                "//span[contains(@class, 'ngx-slider-selected') and normalize-space()='%s']",
                                maxValue));
                        waitUtility.waitForLocatorVisible(maxSelected);
                    } else {
                        throw new IllegalArgumentException(
                                "Audience Multiplier rule requires exactly 2 values (minimum and maximum). Found: "
                                        + ruleValues.size() + " values.");
                    }
                    break;
            }
            clickRuleTypeOkButton();
        }
    }

    public void isElementVisible(String xpath) {
        Locator locator = page.locator(xpath);
        boolean visible = false;
        for (int i = 0; i < 5; i++) {
            if (locator.isVisible()) {
                page.waitForTimeout(1000);
                visible = true;
                break;
            }
            page.waitForTimeout(1000);
        }
        if (visible) {
            locator.scrollIntoViewIfNeeded();
            locator.click(new Locator.ClickOptions().setForce(true));
            page.waitForLoadState(LoadState.LOAD);
        }
    }

    public void clickRuleTypeOkButton() {
        if (RULE_TYPE_OK_BUTTON.isEnabled()) RULE_TYPE_OK_BUTTON.click();
        page.waitForLoadState(LoadState.LOAD);
    }

    public void closeRuleTypePanel() {
        RULE_TYPE_CLOSE.click();
    }

    public List<Object> fetchRulesTypes() {
        ruleTypes = new ArrayList<>();
        FETCH_TARGET_RULE_TYPES.first().waitFor();
        for (int i = 0; i < FETCH_TARGET_RULE_TYPES.count(); i++) {
            String text = FETCH_TARGET_RULE_TYPES
                    .nth(i)
                    .innerText()
                    .replaceAll("\\s*\\(\\d+\\)", "")
                    .trim();
            ruleTypes.add(text);
        }
        return ruleTypes;
    }

    public void fetchRulesTypesCount(int expectedCount) {
        waitUtility.waitUntilSpinnerHidden();
        FETCH_TARGET_RULE_TYPES.nth(expectedCount - 1).waitFor();
    }

    public List<Object> fetchRuleOptions() {
        ruleOptions = new ArrayList<>();
        Locator ruleOptionLocator = FETCH_TARGET_RULE_OPTIONS.locator("ds-typography");
        for (int i = 0; i < ruleOptionLocator.count(); i++) {
            ruleOptionLocator.nth(i).scrollIntoViewIfNeeded();
            String text = ruleOptionLocator.nth(i).innerText();
            text = text.replaceAll("≥", "").trim();
            ruleOptions.add(text);
        }
        return ruleOptions;
    }

    public void selectTargetingRule(String ruleType, String listName) {
        SEARCH_RULE_TYPE.clear();
        SEARCH_RULE_TYPE.type(ruleType);
        if (SELECT_RULE_TYPE.isVisible()) {
            SELECT_RULE_TYPE.click();
            waitUtility.waitForLocatorVisible(TARGETING_RULES_PANEL_TITLE);
            Map<String, Locator> ruleOptionMap = new HashMap<>();
            ruleOptionMap.put("AppBundle", RULE_APP_BUNDLES_LISTS_OPTION);
            ruleOptionMap.put("Keyword", KEYWORD_CUSTOM_LIST);
            for (Map.Entry<String, Locator> entry : ruleOptionMap.entrySet()) {
                if (listName.contains(entry.getKey())) {
                    entry.getValue().click();
                    break;
                }
            }
            Locator searchInput = listName.contains("Keyword") ? SEARCH_RULE_OPTION.nth(1) : SEARCH_RULE_OPTION;
            searchInput.fill(listName);
            page.waitForLoadState();
            searchInput.press("Enter");
        }
    }

    public void clickTarget(String listName) {
        Locator locator = page.locator(String.format(
                "//div[@title='%s']/preceding-sibling::div/div[@title='Target'] | "
                        + "//span[@title='%s']/ancestor::div/preceding-sibling::div/div[@title='Target'] | "
                        + "//div[text()='%s']/ancestor::div/preceding-sibling::div/div[@title='Target']",
                listName, listName, listName));
        locator.click();
    }

    public void clickOk() {
        RULE_TYPE_OK_BUTTON.click();
    }

    public void clickClose() {
        RULE_TYPE_CLOSE.click();
    }

    public void clickCancel() {
        CANCEL_BUTTON.click();
    }

    public String verifyNPIRule() {
        return VERIFY_NPI.innerText();
    }

    /*Roshani Sherkar
     * 01-07-2025*/
    public boolean fetchAndVerifyTargetCategoryName(List<String> targetCategoryList) {
        List<String> actualCategories = new ArrayList<>();
        int count = TARGET_CATEGORY_NAME.count();
        for (int i = 0; i < count; i++) {
            String text = TARGET_CATEGORY_NAME.nth(i).innerText().trim();
            actualCategories.add(text);
        }
        return new HashSet<>(targetCategoryList).containsAll(actualCategories);
    }

    public List<String> getTargetTypesForCategory(String key) {
        TARGET_CATEGORY_NAME.first().waitFor();
        int categoryCount = TARGET_CATEGORY_NAME.count();
        for (int i = 0; i < categoryCount; i++) {
            String categoryText = TARGET_CATEGORY_NAME.nth(i).innerText().trim();

            if (categoryText.contains(key)) {
                String xpathString = String.format(
                        "//div[contains(@class,'targetCategoryName') and contains(text(),'%s')]/following-sibling::div//span[1]",
                        key);
                Locator categoryItems = page.locator(xpathString);
                List<String> actualValues = new ArrayList<>();
                for (int j = 0; j < categoryItems.count(); j++) {
                    categoryItems.nth(j).scrollIntoViewIfNeeded();
                    actualValues.add(categoryItems.nth(j).innerText().trim());
                }
                return actualValues;
            }
        }
        return Collections.emptyList();
    }

    /*Roshani Sherkar
     * 20-08-2025
     * Open NPI list created in new browser tab */
    public String fetchTotalNPICountFromNewTab(String listName) {
        Page originalPage = DriverFactory.getPage();
        Page newTab = DriverFactory.getContext().waitForPage(() -> {
            DriverFactory.getPage()
                    .locator(String.format("//span[@title='%s']/ancestor::div/following-sibling::span", listName))
                    .click();
        });
        newTab.bringToFront();
        DriverFactory.threadLocalDriver.set(newTab);
        newTab.waitForLoadState();
        newTab.waitForTimeout(3000);
        NPIAttributesList npiAttributesList = new NPIAttributesList(newTab);
        String npiCount = npiAttributesList.fetchTotalNPIListCount(listName);
        newTab.close();
        originalPage.bringToFront();
        return npiCount;
    }

    public String fetchNPICountFromTargetingPanel() {
        return TOTAL_NPI_COUNT.first().innerText().trim();
    }

    public boolean isListAvailableInTargetingPanel(String listName) {
        Locator locator = page.locator(String.format(
                "//span[@title='%s'] | //div[@title='%s'] | //div[contains(@class,'text-cls') and contains(text(),'%s')]",
                listName, listName, listName));
        locator.scrollIntoViewIfNeeded();
        return locator.isVisible();
    }

    public int fetchSelectedListCountFromTargetingPanel() {
        Locator locator = SELECTED_LIST.count() > 1 ? KEYWORD_SELECTED_LIST : SELECTED_LIST;
        String text = locator.innerText();
        return Integer.parseInt(text.replaceAll("^.*\\((\\d+)\\).*$", "$1").trim());
    }

    public String verifyIfRuleIsAdded() {
        return FETCH_TARGET_RULE_TYPES.innerText().trim();
    }

    public String fetchSelectedListCountFromTactic(String ruleType) {
        Locator targetCount = FETCH_TARGET_RULE_TYPES
                .locator(String.format("xpath=./span[text()='%s']/following-sibling::span", ruleType))
                .first();
        return targetCount.innerText().trim();
    }

    public boolean isSelectedListPresentInTactic(String npiName) {
        FETCH_TARGET_RULE_OPTIONS.filter(new Locator.FilterOptions().setHasText(npiName)).scrollIntoViewIfNeeded();
        return FETCH_TARGET_RULE_OPTIONS.filter(new Locator.FilterOptions().setHasText(npiName)).isVisible();
    }

    public String fetchSelectedListItemCountFromTactic(String npiName) {
        Locator targetCount =
                FETCH_TARGET_RULE_OPTIONS.filter(new Locator.FilterOptions().setHasText(npiName)).locator("ds-typography").nth(1);
        if (!targetCount.isVisible()) return "";
        return targetCount.innerText().trim();
    }

    public String fetchMatchedNPICountFromTargetingPanel() {
        if (SHOW_MATCHED_NPI_BUTTON.isVisible()) {
            SHOW_MATCHED_NPI_BUTTON.click();
            waitUtility.waitForLocatorVisible(MATCHED_NPI_COUNT);
        }
        return MATCHED_NPI_COUNT.innerText().trim();
    }

    public String verifyRuleType() {
        return FETCH_TARGET_RULE_TYPES
                .innerText()
                .replaceAll("\\s*\\(\\d+\\)", "")
                .trim();
    }

    public String verifyRuleOption() {
        return FETCH_TARGET_RULE_OPTIONS.locator("ds-typography").innerText();
    }

    public BigDecimal getTacticBaseBidPrice() {
        return new BigDecimal(TACTIC_BASE_BID_PRICE.evaluate("el => el.value").toString());
    }

    public BigDecimal getTacticMaxBidPrice() {
        return new BigDecimal(TACTIC_MAX_BID_PRICE.evaluate("el => el.value").toString());
    }

    public void updateBaseBidPrice(BigDecimal updatedBaseBidPrice) {
        TACTIC_BASE_BID_PRICE.fill(String.valueOf(updatedBaseBidPrice));
        SAVE_TACTIC_SETTINGS.click();
    }

    public void updateMaxBidPrice(BigDecimal updatedMaxBidPrice) {
        TACTIC_MAX_BID_PRICE.fill(String.valueOf(updatedMaxBidPrice));
        SAVE_TACTIC_SETTINGS.click();
    }

    public String getBidErrorText() {
        return BASE_BID_ERROR.innerText();
    }

    public String getTacticName() {
        return DISPLAY_TACTIC_NAME.innerText();
    }

    public String verifyTacticName() {
        return TACTIC_NAME.innerText();
    }

    public void clickNewTactic() {
        NEW_TACTIC.click();
    }

    public String checkErrorOfBidMultiplierSegmentRule() {
        ADD_BID_MULTIPLIER.click();
        BEHAVIOUR_SEGMENT.click();
        return BEHAVIOUR_SEGMENT_ERROR.innerText();
    }

    public String checkErrorOfBidMultiplierNPIRule() {
        BID_PANEL_CANCEL_BUTTON.click();
        NPI_BID.click();
        return NPI_ERROR.innerText();
    }

    public void navigateToAddTargeting() {
        BID_PANEL_CANCEL_BUTTON.click();
        INACTIVE_PANEL.click();
        ADD_TARGETING.click();
    }

    public boolean fetchAndVerifyBidCategoryName(List<String> bidCategoryList) {
        List<String> actualCategories = new ArrayList<>();
        int count = BID_MULTIPLIER_CATEGORY_NAME.count();
        for (int i = 0; i < count; i++) {
            String text = BID_MULTIPLIER_CATEGORY_NAME.nth(i).innerText();
            if (text != null) actualCategories.add(text.trim());
        }
        return new HashSet<>(actualCategories).containsAll(
                bidCategoryList.stream().filter(Objects::nonNull).map(String::trim).collect(Collectors.toSet())
        );
    }

    public void clickBidMultipliers() {
        ADD_BID_MULTIPLIER.click();
    }

    public List<String> getBidTypesForCategory(String key) {
        BID_MULTIPLIER_CATEGORY_NAME.first().waitFor();
        int categoryCount = BID_MULTIPLIER_CATEGORY_NAME.count();
        for (int i = 0; i < categoryCount; i++) {
            String categoryText =
                    BID_MULTIPLIER_CATEGORY_NAME.nth(i).innerText().trim();
            if (categoryText.contains(key)) {
                String xpathString = String.format(
                        "//div[contains(@class,'bidMultiplierCategoryName') and contains(text(),'%s')]/following-sibling::div//div[contains(@class,'content')]",
                        key);
                Locator categoryItems = page.locator(xpathString);
                List<String> actualValues = new ArrayList<>();
                for (int j = 0; j < categoryItems.count(); j++) {
                    categoryItems.nth(j).scrollIntoViewIfNeeded();
                    actualValues.add(categoryItems.nth(j).innerText().trim());
                }
                return actualValues;
            }
        }
        return Collections.emptyList();
    }

    public void selectMultipleBidRuleTypes(String ruleType, List<String> ruleValues, String fillValue) {

        String ruleTypeXpath = String.format(
                "//div[contains(@class,'content ng-star-inserted') and contains(text(),'%s')]", ruleType);
        page.locator(ruleTypeXpath).click();

        switch (ruleType) {
            case "Behavioral Segment", "NPI", "Day of The Week", "Practitioner Type", "Age", "Gender", "Browser",
                 "Device", "Inventory Source":
                fillRuleValues("//div[contains(text(),'%s')]/ancestor::td/preceding-sibling::td//input",
                        ruleValues, fillValue);
                break;

            case "Speciality", "Geo Targets":
                fillRuleValues("//div[contains(@class,'bmtTreeNodeName') and normalize-space(text())='%s']/preceding-sibling::div//input[@bidmultiplierconverter]",
                        ruleValues, fillValue);
                page.waitForTimeout(1000);
                break;

            case "Operating Systems", "Creative Size":
                fillRuleValues("//tr[td[@class='name-column' and normalize-space(text())='%s']]/td[contains(@class,'bmtPriceCol')]/input",
                        ruleValues, fillValue);
                break;

            case "Domains and Apps":
                fillRuleValues("//div[contains(@class,'cliptext') and @title='%s']/preceding-sibling::div[contains(@class,'left')]/input",
                        ruleValues, fillValue);
                break;
        }

        clickRuleTypeOkButton();
    }

    private void fillRuleValues(String xpathTemplate, List<String> ruleValues, String fillValue) {
        for (String value : ruleValues) {
            String cleanedValue = value.replace("[", "").replace("]", "");
            String BidValuexpath = String.format(xpathTemplate, cleanedValue);
            page.locator(BidValuexpath).fill(fillValue);
        }
    }

    public void fetchBidRulesTypesCount(int expectedBidCount) {
        Locator target = FETCH_BID_MULTIPLIER_RULE_TYPES.nth(expectedBidCount - 1);
        waitUtility.waitForLocatorVisible(target);
    }

    public List<Object> fetchBidRulesTypes() {
        ruleTypes = new ArrayList<>();
        FETCH_BID_MULTIPLIER_RULE_TYPES.first().waitFor();
        for (int i = 0; i < FETCH_BID_MULTIPLIER_RULE_TYPES.count(); i++) {
            String text = FETCH_BID_MULTIPLIER_RULE_TYPES
                    .nth(i)
                    .innerText()
                    .replaceAll("\\s*\\(\\d+\\)", "")
                    .trim();
            ruleTypes.add(text);
        }
        return ruleTypes;
    }

    public List<Object> fetchBidRuleOptions() {
        ruleOptions = new ArrayList<>();
        for (int i = 0; i < FETCH_BID_MULTIPLIER_RULE_OPTIONS.count(); i++) {
            String text = FETCH_BID_MULTIPLIER_RULE_OPTIONS.nth(i).innerText();
            text = text.replaceAll("≥", "").trim();
            ruleOptions.add(text);
        }
        return ruleOptions;
    }

    public void clickCreativeTab() {
        CREATIVE_TAB.click();
    }

    public void expandAllTargetingRules() {
        waitUtility.waitForLocatorVisible(NEW_TARGETING_RULE_BUTTON);
        int safetyLimit = EXPAND_TARGETING_ICONS.count();
        int expanded = 0;

        while (EXPAND_TARGETING_ICONS.count() > 0 && expanded < safetyLimit) {
            Locator icon = EXPAND_TARGETING_ICONS.first();
            icon.scrollIntoViewIfNeeded();
            icon.click();
            expanded++;
        }
    }

    public void waitForBidPanel() {
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(BID_PANEL);

    }
}
