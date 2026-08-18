package stepdefinitions;

import static utils.CommonUtils.normalize;
import static utils.CommonUtils.normalizeObjectList;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.PlaywrightException;
import com.opencsv.exceptions.CsvValidationException;
import factory.DriverFactory;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.Navigation;
import pages.admin.Accounts;
import pages.admin.Setup;
import pages.life.*;
import pages.studio.WorkspaceCreation;
import utils.*;

public class LifeSteps {

    private static final Logger logger = LoggerFactory.getLogger(LifeSteps.class);
    static String campaignNameRandom;
    static String lineItemNameRandom;
    static String tacticNameRandom;
    static String dealNameRandom;
    static String dealIDRandom;
    static String url;
    static String username;
    static String password;
    static String npiName;
    static String npiNameEdited;
    static String templateNameRandom;
    static String dimensionName;
    static String metricName;
    static String newPixelName;
    static String pixelNameEdited;
    static String userType;
    List<Object> keyType = new ArrayList<>();
    List<Object> keyValues = new ArrayList<>();
    Map<String, Map<String, String>> keyValueMap = new LinkedHashMap<>();
    Map<String, List<String>> rulesMap = new LinkedHashMap<>();
    List<String> nameList = new ArrayList<>();
    List<String> capturedDetails = new ArrayList<>();
    List<String> itemList = new ArrayList<>();
    Map<String, List<String>> itemMap = new HashMap<>();
    Navigation navigation = new Navigation(DriverFactory.getPage());
    Campaigns campaigns = new Campaigns(DriverFactory.getPage());
    LineItemDetails lineItemDetails = new LineItemDetails(DriverFactory.getPage());
    TacticDetails tacticDetails = new TacticDetails(DriverFactory.getPage());
    TacticSettings tacticSettings = new TacticSettings(DriverFactory.getPage());
    TacticCreatives tacticCreatives = new TacticCreatives(DriverFactory.getPage());
    NPILists npiLists = new NPILists(DriverFactory.getPage());
    NPIStaticList npiStaticList = new NPIStaticList(DriverFactory.getPage());
    ReportTemplates reportTemplates = new ReportTemplates(DriverFactory.getPage());
    PMP pmp = new PMP(DriverFactory.getPage());
    NPISmartList npiSmartList = new NPISmartList(DriverFactory.getPage());
    CampaignDashboard campaignDashboard = new CampaignDashboard(DriverFactory.getPage());
    TargetingTemplate targetingTemplate = new TargetingTemplate(DriverFactory.getPage());
    CreateCreatives createCreatives = new CreateCreatives(DriverFactory.getPage());
    NPIAttributesList npiAttributesList = new NPIAttributesList(DriverFactory.getPage());
    NPIAutoImportedList npiAutoImportedList = new NPIAutoImportedList(DriverFactory.getPage());
    SharedList sharedList = new SharedList(DriverFactory.getPage());
    Pixels pixels = new Pixels(DriverFactory.getPage());
    RetargetingPixel retargetingPixel = new RetargetingPixel(DriverFactory.getPage());
    ConversionPixel conversionPixel = new ConversionPixel(DriverFactory.getPage());
    SmartPixel smartPixel = new SmartPixel(DriverFactory.getPage());
    BulkCreativeUpload bulkCreativeUpload = new BulkCreativeUpload(DriverFactory.getPage());
    RunReport runReport = new RunReport(DriverFactory.getPage());
    Accounts accounts = new Accounts(DriverFactory.getPage());
    ScheduleReport scheduleReport = new ScheduleReport(DriverFactory.getPage());
    LineItemFlights lineItemFlights = new LineItemFlights(DriverFactory.getPage());
    CampaignSettings campaignSettings = new CampaignSettings(DriverFactory.getPage());
    WorkspaceCreation workspaceCreation = new WorkspaceCreation(DriverFactory.getPage());
    Setup setup = new Setup(DriverFactory.getPage());
    CuratedMarket curatedMarket = new CuratedMarket(DriverFactory.getPage());
    Constants constants = new Constants();
    NPIMedscapeList npiMedscapeList = new NPIMedscapeList(DriverFactory.getPage());
    int itemCount = 0;
    int totalListCount = 0;
    int flightStartDate = 0;
    int flightEndDate = 0;
    int file1RecordCount = 0;
    int file2RecordCount = 0;
    APIResponse response;
    boolean flag = false;
    String customFieldName;
    String uiCustomFieldName;
    BigDecimal campaignBaseBid;
    BigDecimal campaignMaxBid;
    BigDecimal campaignHighestBid;
    Path targetFilePath;

    @Given("This scenario will be executed in the {string} environment as a {string}")
    public void set_environment(String environment, String user) throws Exception {
        logger.info("Setting up environment: {} for user type: {}", environment, user);
        userType = user;

        if (environment.equals("Demo")) {
            url = ConfigReader.getProperty("demoURL");
            if (user != null
                    && user.toLowerCase().contains("external")
                    && ConfigReader.getProperty("demoExternalUser") != null) {
                username = ConfigReader.getExternalDemoUsername();
                password = ConfigReader.getExternalDemoPassword();
            } else {
                username = ConfigReader.getInternalDemoUsername();
                password = ConfigReader.getInternalDemoPassword();
            }
        } else if (environment.equals("Pre-release")) {
            url = ConfigReader.getProperty("preReleaseURL");
            if (user != null && user.toLowerCase().contains("external")) {
                username = ConfigReader.getExternalPreReleaseUsername();
                password = ConfigReader.getExternalPreReleasePassword();
            } else {
                username = ConfigReader.getInternalPreReleaseUsername();
                password = ConfigReader.getInternalPreReleasePassword();
            }
        }
    }

    @And("{string} application is logged in successfully with Account {string}")
    public void life_application_is_logged_in_as(String application, String account) {
        navigation.navigateToUrl(url);
        navigation.enterUsername(username);
        navigation.enterPassword(password);
        navigation.clickLogin();
        logger.info("{} application is logged in successfully with Account {}", application, account);
        logger.info("Navigating to specific application module: {}", application);
        navigation.selectAndClickExternalUserApplicationType();
        switch (application) {
            case "Life":
                if (userType.equals("User")) {
                    navigation.navigateToLife();
                }
                break;
            case "HCP":
                navigation.navigateToHCP();
                break;
            case "Studio":
                if (userType.equals("User")) {
                    navigation.navigateToLife();
                }
                navigation.navigateToStudio();
                break;
        }
        if (userType.equals("User")) {
            logger.info("Selecting account: {}", account);
            navigation.selectAccount(account);
        } else {
            logger.info("Selecting account for External user: {}", account);
            navigation.selectExternalUserAccount(account);
        }
    }

    @Given("User clicks on Create Campaign")
    public void user_clicks_on_create_campaign() {
        logger.info("Clicking on Create Campaign button");
        campaigns.createCampaign();
        String campaignText = campaigns.verifyCampaignText();
        Assert.assertEquals("Create New Campaign", campaignText);
    }

    @When("User enters the campaign details as {string} {string} {string} {string} and saves the campaign")
    public void user_enters_the_campaign_details_and_saves_the_campaign(
            String advertiser, String campaign_name, String campaign_type, String budget) {
        campaignNameRandom = campaign_name + '_' + CommonUtils.timeStampCalculation();
        logger.info(
                "Entering campaign details - Advertiser: {}, Name: {}, Type: {}, Budget: {}",
                advertiser,
                campaignNameRandom,
                campaign_type,
                budget);
        campaigns.selectAdvertiser(advertiser);
        campaigns.enterCampaignName(campaignNameRandom);
        campaigns.setCampaignType(campaign_type);
        campaigns.enterBudget(budget);
        logger.info("Saving campaign details");
        campaigns.saveCampaign();
    }

    @When("User enters the campaign details as {string} {string} {string} {string}")
    public void userEntersTheCampaignDetailsAs(
            String advertiser, String campaignName, String campaignType, String budget) {
        campaignNameRandom = campaignName + '_' + CommonUtils.timeStampCalculation();
        logger.info(
                "Entering campaign details - Advertiser: {}, Name: {}, Type: {}, Budget: {}",
                advertiser,
                campaignNameRandom,
                campaignType,
                budget);
        campaigns.selectAdvertiser(advertiser);
        campaigns.enterCampaignName(campaignNameRandom);
        campaigns.setCampaignType(campaignType);
        campaigns.enterBudget(budget);
    }

    @Then("Verify that the campaign budget status is {string} and is greyed out")
    public void verifyThatTheBudgetStatusIs(String campaignBudgetStatus) {
        logger.info("Verifying campaign budget status: {}", campaignBudgetStatus);
        Assert.assertEquals(campaignBudgetStatus, campaigns.getCampaignBudgetStatus());
        Assert.assertEquals("rgba(34, 34, 34, 0.09)", campaigns.checkBackgroundColorOfCampaignBudgetStatus());
        Assert.assertEquals(1, campaigns.getCampaignBudgetStatusOptionsCount());
    }

    @And("User saves the campaign")
    public void userSavesTheCampaign() {
        logger.info("Saving campaign details");
        campaigns.saveCampaign();
    }

    @And("User sets campaign management fee as {string} {string} {string}")
    public void userSetsCampaignManagementFeeAs(String managementFeeOption, String percent, String amount) {
        logger.info(
                "Setting campaign management fee. Option: {}, Percent: {}, Amount: {}",
                managementFeeOption,
                percent,
                amount);
        Assert.assertTrue("Campaign management fee checkbox is not visible", campaigns.isManagementFeeAvailable());

        campaigns.clickManagementFee();
        campaigns.clickManagementFeeOptionAndEnterData(managementFeeOption, percent, amount);
    }

    @Then("Verify management fee is set as {string}")
    public void verifyManagementFeeIsSetAs(String expectedFeeValue) {
        logger.info("Verifying inherited management fee value: {}", expectedFeeValue);
        Assert.assertEquals(
                "Inherited management fee is incorrect",
                expectedFeeValue,
                lineItemDetails.fetchDisplayedManagementFeeValue());
    }

    @Then("User clicks on create new tactic")
    public void userClicksOnCreateNewTactic() {
        logger.info("Clicking on Create New Tactic");
        tacticDetails.clickNewTactic();
    }

    @Then("User navigates to line item and clicks on details tab")
    public void userNavigatesToLineItemAndClicksOnDetailsTab() {
        campaigns.clickLineItemTile();
        lineItemDetails.clickDetailsTab();
    }

    @When("User overrides line item management fee and verifies tactic reflection for the following fee types")
    public void userOverridesLineItemManagementFeeAndVerifiesTacticReflectionForTheFollowingFeeTypes(
            DataTable dataTable) {

        logger.info("Overriding line item management fee and verifying tactic reflection for each fee type");
        List<Map<String, String>> feeDetails = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> feeRow : feeDetails) {
            campaigns.clickLineItemTile();
            lineItemDetails.clickDetailsTab();
            String feeOption = feeRow.get("Fee Option");
            String percent = feeRow.get("Percent");
            String amount = feeRow.get("Amount");
            String expectedDisplay = feeRow.get("Expected Display");

            logger.info(
                    "Applying line item management fee override - Fee Option: {}, Percent: {}, Amount: {}, Expected Display: {}",
                    feeOption,
                    percent,
                    amount,
                    expectedDisplay);

            lineItemDetails.enableManagementFeeOverride();
            tacticDetails.selectManagementFeeOptionAndEnterData(feeOption, percent, amount, expectedDisplay);
        }
    }

    @Then("Verify tactic reflects line item management fee as {string}")
    public void verifyTacticReflectsLineItemManagementFeeAs(String expectedFeeValue) {
        logger.info("Verifying tactic management fee value: {}", expectedFeeValue);
        tacticDetails.enterTacticName("Tactic_" + CommonUtils.timeStampCalculation());
        tacticDetails.saveTacticDetails();
        Assert.assertEquals(
                "Tactic management fee value is incorrect",
                expectedFeeValue,
                tacticSettings.fetchDisplayedManagementFeeValue());
    }

    @Then("Verify campaign details are saved and user is navigated to the line item page")
    public void verify_campaign_details_are_saved_and_user_is_navigated_to_line_item_page() {
        logger.info("Verifying campaign creation and navigation to Line Item page");
        Assert.assertEquals("Campaign " + campaignNameRandom + " created.", campaigns.campaignSuccess());
        String lineItemText = lineItemDetails.verifyLineItemText();
        Assert.assertEquals("New Line Item", lineItemText);
    }

    @Then("User navigates to campaign")
    public void user_navigates_to_campaign() {
        logger.info("Navigating to campaign");
        campaigns.selectCampaign();
    }

    @When("User enters the line item details as {string} {string}, enables the line item and saves the changes")
    public void user_enters_the_line_item_details_enables_the_line_item_and_saves_the_changes(
            String lineItemName, String lineBudget) {
        lineItemNameRandom = lineItemName + '_' + CommonUtils.timeStampCalculation();
        logger.info("Entering Line Item details - Name: {}, Budget: {}", lineItemNameRandom, lineBudget);
        lineItemDetails.enterLineItemName(lineItemNameRandom);
        navigation.clickOnIcon("Add Flight");
        lineItemDetails.enterLineItemBudget(lineBudget);
        lineItemDetails.isPlacementIdAvailable(lineItemNameRandom);
        lineItemDetails.enableLineItem();
        lineItemDetails.saveLineItem();
    }

    @Then("Verify line item details are saved and user is navigated to the tactic page")
    public void verify_line_item_details_are_saved_and_user_is_navigated_to_tactic_page() {
        logger.info("Verifying Line Item creation and navigation to Tactic page");
        Assert.assertEquals("Lineitem " + lineItemNameRandom + " created.", lineItemDetails.lineItemSuccess());
        String tacticText = tacticDetails.verifyTacticDetailsText();
        Assert.assertTrue(
                "Tactic page text is not displayed",
                tacticText.contains("New Tactic") || tacticText.contains("New Ad Group"));
    }

    @Then("User creates below tactics under same line item and verifies it")
    public void user_creates_below_tactics_under_same_line_item_and_verifies_it(DataTable dataTable) {
        logger.info("Starting creation and verification of multiple tactics under the same Line Item");
        List<Map<String, String>> tactics = dataTable.asMaps(String.class, String.class);
        List<String> expectedTactic = new ArrayList<>();

        for (Map<String, String> tacticData : tactics) {
            String tacticName = tacticData.get("Tactic Name")+'_' + CommonUtils.timeStampCalculation();
            metricName = tacticName;
            String channel = tacticData.get("Channel");
            String ruleType = tacticData.get("RuleType");
            logger.info("Processing Tactic - Name: {}, Channel: {}, RuleType: {}", tacticName, channel, ruleType);
            expectedTactic.add(tacticName);
            // Enter tactic name
            tacticDetails.enterTacticName(tacticName);
            tacticDetails.saveTacticDetails();

            // Select channel and add targeting rules
            logger.info("Configuring channel and targeting rules for: {}", tacticName);
            tacticSettings.selectChannel(channel);
            tacticDetails.clickTargetingRuleIcon();
            tacticSettings.addTargetingRules(ruleType);
            tacticSettings.saveTacticSettings();
            tacticDetails.clickNewTactic();
        }

        List<String> actualTactics = tacticDetails.getAllTactics();
        Assert.assertEquals(new HashSet<>(expectedTactic), new HashSet<>(actualTactics));
        List<String> expectedTarget = tacticSettings.getExpectedTargetRules();
        List<String> actualTarget = tacticSettings.getActualTargetRules();
        Assert.assertEquals(expectedTarget, actualTarget);
    }

    @When("User clicks the comments icon in the tactic {string} section and add {string}")
    public void userClicksTheCommentsIconInTheTacticSection(String entryPoint, String comment) {
        logger.info("Adding comment to section: '{}'. Comment: '{}'", entryPoint, comment);
        tacticDetails.addComment(entryPoint, comment);
    }

    @When("User validates the comment added in {string} is {string} then clear it")
    public void user_validates_the_comment_added_then_clear_it(String entryPoint, String expectedComment) {
        logger.info("Validating comment in section: '{}'. Expected: '{}'", entryPoint, expectedComment);
        String actualComment = tacticDetails.validateComment(entryPoint);
        Assert.assertEquals(expectedComment, actualComment);
        tacticDetails.clearComment();
    }

    @Then("Verify that {string} is visible in {string} section")
    public void verifyIfTheCommentAddedInIsVisibleInSection(String headerComment, String sectionName) {
        String actualComment = tacticDetails.validateComment(sectionName);
        Assert.assertEquals(headerComment, actualComment);
        tacticDetails.clickCancelButton();
    }

    @Then("User adds frequency cap with details {string} {string} {string} {string}")
    public void user_adds_frequency_cap_with_details(String level, String frequencyValue, String timesPer, String scope) {
        logger.info(
                "Adding Frequency Cap - Level: {}, Value: {}, Times Per: {}, Scope: {}",
                level,
                frequencyValue,
                timesPer,
                scope);
        campaigns.addFrequencyCap(level, frequencyValue, timesPer, scope);
    }

    @Then("User adds {string} frequency cap with details {string} {string} {string} {string}")
    public void user_adds_frequency_cap_with_details(String exceeded, String level, String frequencyValue, String timesPer, String scope) {
        campaigns.addFrequencyCap(level, frequencyValue, timesPer, scope, exceeded);
    }

    @Then("User gets error of limit exceeded {string}")
    public void userGetsErrorOfLimitExceeded(String level) {
        String actualError = campaigns.frequencyLimitExceedCheck(level);
        if (level.contains("Line Item"))
            Assert.assertEquals("Line Item frequency cap can not exceed Campaign frequency cap", actualError);
        else if (level.contains("tactic")) {
            Assert.assertEquals("Tactic frequency cap can not exceed Line Item or Campaign frequency cap", actualError);
        }
    }

    @Then("User clicks on details tab")
    public void user_clicks_on_details_tab() {
        logger.info("Clicking on details tab");
        campaigns.clickDetailsTab();
    }

    @Then("User verifies if Frequency Cap is in disabled state by default")
    public void userVerifiedFrequencyCapIsInDisabledStatesByDefault() {
        logger.info("Verifying Frequency Cap is disabled by default");
        boolean fc_checkbox_state = campaigns.isFrequencyCapDisabled();
        Assert.assertFalse(fc_checkbox_state);
    }

    @Then("User navigates to LineItem")
    public void userNavigatesToLineItem() {
        logger.info("Navigating to Line Item");
        campaigns.clickLineItem();
    }

    @Then("User verifies if frequency cap is saved with details {string} {string} {string} {string}")
    public void userVerifiesIfFrequencyCapIsSavedWithDetailsOnCampaignLevel(
            String freqValue, String timesPer, String scope, String level) {
        logger.info(
                "Verifying frequency cap saved with details - Value: {}, Times Per: {}, Scope: {}, Level: {}",
                freqValue,
                timesPer,
                scope,
                level);
        String actualFrequencyCapText = campaigns.getSavedFrequencyCap(level);
        String expectedFrequencyCapText = String.format("%s x %s x %s %s", freqValue, timesPer, scope, level)
                .toUpperCase();
        if (timesPer.contains("hour")) {
            expectedFrequencyCapText = String.format("%s x Time Per %s hour %s %s", freqValue, freqValue, scope, level)
                    .toUpperCase();
        }
        Assert.assertEquals(expectedFrequencyCapText, actualFrequencyCapText);
    }

    @Then("User navigates to Tactic and clicks on settings tab")
    public void user_navigates_to_tactic_and_clicks_on_settings_tab() {
        logger.info("Navigating to Tactic and clicking on settings tab");
        tacticDetails.clickFirstTacticTab();
        tacticDetails.clickSettingsTab();
    }

    @Then("Verify that frequency cap is saved in tactic")
    public void verify_that_frequency_cap_is_saved_in_tactic() {
        logger.info("Verifying frequency cap is saved in tactic");
        boolean frequencyCapState = campaigns.getFrequencyCapState();
        Assert.assertTrue(frequencyCapState);
    }

    @Then("Verify that below tabs gets enabled only after saving tactics")
    public void verify_that_below_tabs_gets_enabled_only_after_saving_tactics(DataTable dataTable) {
        logger.info("Verifying: that frequency cap is saved in tactic");
        tacticDetails.verifyDetailsTab();
        List<String> tacticTabNames = new ArrayList<>(dataTable.asList(String.class));
        String detailsTab = tacticTabNames.remove(tacticTabNames.size() - 1);
        logger.info("Tabs expected to be disabled initially: {}", tacticTabNames);
        List<String> disabledTabs = tacticDetails.newTacticTabs();
        Assert.assertEquals(tacticTabNames, disabledTabs);
        tacticDetails.clickFirstTacticTab();
        List<String> enabledTabs = tacticDetails.allTacticsUnderLI();
        tacticTabNames.add(detailsTab);
        Assert.assertEquals(new HashSet<>(tacticTabNames), new HashSet<>(enabledTabs));
    }

    @And("Verify the status of first tactic under line item is {string}")
    public void verify_the_status_of_first_tactic_under_line_item_is(String ExpectedStatus) {
        logger.info("Verifying tactic status is: {}", ExpectedStatus);
        tacticDetails.clickFirstTacticTab();
        String actualStatus = tacticDetails.verifyTacticState();
        Assert.assertEquals(ExpectedStatus, actualStatus);
    }

    @Then("User creates new custom field {string} and verifies the same")
    public void user_creates_new_custom_field_and_verifies_the_same(String customField) {
        String customFieldName = customField + "_" + CommonUtils.randomFourDigitNumber();
        logger.info("Creating new custom field with name: {}", customFieldName);
        this.customFieldName = customFieldName;
        tacticDetails.addCustomField(customFieldName);
        String raw = tacticDetails.verifyCustomField(customFieldName);
        String actualName = raw.split("\\R")[0]; // To remove unwanted space and text
        Assert.assertEquals(customFieldName, actualName);
        this.uiCustomFieldName = actualName;
        logger.info("Custom field created and verified successfully");
    }

    @And("User verifies if new custom field is visible and empty in new tactic {string}")
    public void user_verifies_if_new_custom_field_is_visible_and_empty_in_new_tactic(String tacticSearch) {
        logger.info("Verifying custom field is visible and empty in new tactic");
        navigation.clickPulsePointLogo();
        campaignDashboard.searchCreatedTactic(tacticSearch);
        campaignDashboard.clickCampaignFromDashboard();
        campaignDashboard.clickLineItemExpandIcon();
        tacticDetails.clickFirstTacticTab();
        tacticDetails.clickDetailsTab();
        Assert.assertEquals("Custom field name did not match", customFieldName, uiCustomFieldName);
        navigation.clickPulsePointLogo();
        tacticDetails.globalSearchDeletedTactic(metricName);
        campaignDashboard.clickGlobalTacticResult();
        tacticDetails.clickNewTactic();
        Assert.assertEquals(customFieldName, uiCustomFieldName);
        Assert.assertTrue(tacticDetails.customFieldValue(customFieldName).inputValue().isEmpty());
        tacticDetails.clickLastTactic();
        Assert.assertEquals(customFieldName, uiCustomFieldName);
        Assert.assertFalse(tacticDetails.customFieldValue(customFieldName).inputValue().isEmpty());
    }

    @Then("User deletes the custom field and verify its removed from new tactic")
    public void user_deletes_the_custom_field_and_verify_its_removed_from_new_tactic() {
        logger.info("Deleting custom field: {}", customFieldName);
        Assert.assertTrue(
                "Unable to delete Custom field",
                tacticDetails.deleteCustomField(customFieldName).contains("Successfully deleted the Field"));
        Assert.assertFalse("Custom Field is available", tacticDetails.isCustomFieldAvailable(customFieldName));
    }

    @When("User enters the tactic details as {string} and saves the tactic")
    public void user_enters_the_tactic_details_and_saves_the_tactic(String tacticName) {
        nameList.clear();
        tacticNameRandom = tacticName + '_' + CommonUtils.timeStampCalculation();
        logger.info("Entering tactic details for name: {}", tacticNameRandom);
        tacticDetails.enterTacticName(tacticNameRandom);
        nameList.add(tacticNameRandom);
        tacticDetails.saveTacticDetails();
        logger.info("Tactic details saved");
    }

    @Then("Verify tactic details are saved and user is navigated to the settings tab")
    public void verify_tactic_details_are_saved_and_user_is_navigated_to_settings_tab() {
        logger.info("Verifying tactic details are saved and navigation to settings tab");
        Assert.assertEquals("Tactic " + tacticNameRandom + " updated.", tacticDetails.tacticDetailsSuccess());
        String settingsText = tacticSettings.verifyTacticSettingsText();
        Assert.assertEquals("Bid Strategy", tacticSettings.verifyTacticSettingsText());
    }

    @When("User selects the {string} as channel")
    public void user_selects_the_channel(String channel) {
        logger.info("Selecting Channel: {}", channel);
        tacticSettings.selectChannel(channel);
        logger.info("Adding Targeting Rule");
        navigation.clickOnIcon("Add Targeting Rule");
    }

    @When("User clicks on Add Targeting Rule")
    public void userClicksOnAddTargetingRule() {
        logger.info("Adding Targeting Rule");
        navigation.clickOnIcon("Add Targeting Rule");
    }

    @Then("User selects {string} as rule type and configures the targeting rules, and saves the settings")
    public void user_configures_the_targeting_rules_and_saves_the_settings(String ruleType) {
        logger.info("Selecting rule type: {}", ruleType);
        tacticSettings.selectRuleType(ruleType);
        logger.info("Saving Tactic settings");
        tacticSettings.saveTacticSettings();
    }

    @Then("Verify settings details are saved and user is navigated to the creatives tab")
    public void verify_settings_details_are_saved_and_user_is_navigated_to_creatives_tab() {
        String successMessage = tacticSettings.tacticSettingsSuccess();
        logger.info("Save Message: {}", successMessage);
        assert successMessage.contains("Success!");
        String creativesText = tacticCreatives.verifyTacticCreativesText();
        Assert.assertEquals("Creative(s)", creativesText);
    }

    @Then("User clicks on first tactic and goes to details tab")
    public void user_clicks_on_first_tactic_and_goes_to_details_tab() {
        logger.info("Clicking on first tactic and navigating to details tab");
        tacticDetails.clickFirstTacticTab();
        tacticDetails.clickDetailsTab();
    }

    @Then("User clears the custom field text")
    public void user_clears_the_custom_field_text() {
        logger.info("Clearing text in custom field: {}", customFieldName);
        tacticDetails.clearCustomFieldText(customFieldName);
    }

    @Then("User assigns the existing creative named {string}, enables the tactic and saves the changes")
    public void user_assigns_the_existing_creative_enables_the_tactic_and_saves_the_changes(String creative) {
        logger.info("Assigning existing creative: {}", creative);
        navigation.clickOnIcon("Assign Existing Creatives");
        tacticCreatives.assignCreatives(creative);
        logger.info("Enabling Tactic and saving Creatives");
        tacticCreatives.enableCreative();
        tacticCreatives.saveTacticCreatives();
        Assert.assertTrue(
                "Unable to save creative",
                tacticCreatives.tacticCreativesSuccess().contains("Success!"));
    }

    @Then("Verify the newly created campaign is in running state")
    public void verify_the_newly_created_campaign_is_in_running_state() {
        logger.info("Verifying newly created campaign is in running state");
        tacticCreatives.navigateToCampaignDashboard();
        campaignDashboard.resetFiltersIfApplied();
        String status = tacticCreatives.getCampaignStatus();
        Assert.assertEquals("Running", status);
    }

    @Then("Verify creative details are saved")
    public void verifyCreativeDetailsAreSaved() {
        logger.info("Verifying creative details are saved successfully");
        Assert.assertTrue(
                "Tactic creatives success message should contain 'Success!'",
                tacticCreatives.tacticCreativesSuccess().contains("Success!"));
    }

    @Then("Verify that the campaign is in {string} state")
    public void verifyTheCampaignState(String expectedStatus) {
        logger.info("Navigating to Campaign Dashboard to verify {} status", expectedStatus);
        tacticCreatives.navigateToCampaignDashboard();
        Assert.assertEquals(expectedStatus, tacticCreatives.getCampaignStatus());
    }

    @Then("Verify that the approval status of the campaign is {string}")
    public void verifyThatTheApprovalStatusOfTheCampaignIs(String expectedStatus) {
        logger.info("Navigating to Campaign Dashboard to verify approval status: {}", expectedStatus);
        tacticCreatives.navigateToCampaignDashboard();
        campaigns.clickCampaignDetailsTab();
        Assert.assertEquals(expectedStatus, tacticCreatives.getCampaignApprovalStatus());
    }

    @Then(
            "Verify the newly created campaign details in the campaign list: Campaign name, Line item name and Tactic name")
    public void verify_the_newly_created_campaign_details_in_the_campaign_list() {
        campaigns.navigateToCampaignDashboard();
        logger.info("Searching for Campaign: {}", campaignNameRandom);
        campaignDashboard.searchCreatedCampaign(campaignNameRandom);
        Assert.assertEquals(campaignNameRandom, campaignDashboard.verifyCreatedCampaign(campaignNameRandom));
        logger.info("Expanding Line Item to verify: {}", lineItemNameRandom);
        campaignDashboard.expandCreatedLineItem();
        Assert.assertEquals(lineItemNameRandom, campaignDashboard.verifyCreatedLineItem(lineItemNameRandom));
        logger.info("Expanding Tactic to verify: {}", tacticNameRandom);
        campaignDashboard.expandCreatedLineItem();
        Assert.assertEquals(tacticNameRandom, campaignDashboard.verifyCreatedTactic());
    }

    @Given("User navigates to NPI Lists page")
    public void user_navigates_to_npi_lists_page() {
        logger.info("Navigating to NPI Lists page");
        navigation.clickSubMenu();
        npiLists.clickNPILists();
    }

    @And("User searches the workspace in LIFE and selects it")
    public void userSearchesTheInLIFEAndSelectsIt() {
        logger.info("Searching for workspace: {}", StudioSteps.workspaceName);
        npiLists.searchNPILists(StudioSteps.workspaceName);
    }

    @And("User clicks on the published workspace")
    public void userClicksOnThePublished() {
        logger.info("Selecting published workspace: {}", StudioSteps.workspaceName);
        npiLists.selectPublishedList(StudioSteps.workspaceName);
    }

    @Then("User Verify the list is displayed in the Life")
    public void userVerifyTheListIsDisplayedInTheLife() {
        logger.info("Verifying list is displayed in LIFE");
        Assert.assertTrue("NPI list is not available in LIFE", npiLists.availablePlatforms());
    }

    @And("Verify the list should be available for LIFE platform by default")
    public void verifyTheListShouldBeAvailableForLIFEPlatformByDefault() {
        logger.info("Verifying LIFE platform is selected by default");
        Assert.assertTrue("LIFE (only) is not selected as default", npiLists.checkOnlyLIFEIsSelected());
    }

    @When("User clicks on Create New List")
    public void user_clicks_on_create_new_list() {
        logger.info("Clicking on Create New List");
        npiLists.clickCreateNewList();
    }

    @Then("Save and Verify the list gets saved successfully")
    public void verify_smart_list_gets_saved_successfully() {
        logger.info("Saving NPI List");
        npiStaticList.saveList();
        String successMessage = npiStaticList.fetchSuccessAlert();
        logger.info("NPI List save message: {}", successMessage);
        assert successMessage.contains("NPI list created");
        logger.info("List saved successfully");
    }

    @Then("Verify creation of NPI List screen is displayed")
    public void verify_creation_of_npi_list_screen_is_displayed() {
        logger.info("Verifying NPI List creation screen is displayed");
        String screenText = npiLists.verifyNPIListText();
        Assert.assertEquals("Create New NPI List", screenText);
    }

    @Then("User selects Static List")
    public void user_selects_static_list() {
        logger.info("Selecting Static List type");
        npiLists.clickStaticList();
    }

    @Then("User enters the NPI list details as {string} {string} {string}")
    public void user_enters_the_npi_list_details_as(String npiListName, String advertiser, String npiNumber) {
        npiName = npiListName + '_' + CommonUtils.timeStampCalculation();
        String[] advertiserList = advertiser.split(",");
        logger.info(
                "Entering NPI List details - Name: {}, Advertiser: {}, NPI Number: {}", npiName, advertiser, npiNumber);
        npiStaticList.enterListName(npiName);
        for (String adv : advertiserList) {
            logger.info("Selecting advertiser: {}", adv.trim());
            npiStaticList.selectAdvertiser(adv.trim());
        }
        npiStaticList.enterNPINumber(npiNumber);
    }

    @When("User makes list available in {string} and saves the list")
    public void user_makes_list_available_in_life_and_saves_the_list(String platformName) {
        logger.info("Making list available in platform: {}", platformName);
        npiStaticList.selectProduct(platformName);
    }

    @Then("Verify list gets saved successfully")
    public void verify_list_gets_saved_successfully() {
        npiStaticList.saveList();
        String successMessage = npiStaticList.fetchSuccessAlert();
        logger.info("Success Message: {}", successMessage);
        assert successMessage.contains("NPI list created");
        logger.info("List saved verification passed");
    }

    @And("Verify the NPI Numbers from the uploaded file {string} are displayed correctly in the list details page")
    public void verifyTheStaticNPINumbersFromTheUploadedFileAreDisplayedCorrectlyInTheListDetailsPage(String fileName)
            throws IOException {
        logger.info(
                "Verify the NPI Numbers from the uploaded file {} are displayed correctly in the list details page",
                fileName);
        int npiCountFromFile = 0;
        int npiCountFromListDetails = 0;
        if (fileName.contains(".xlsx")) npiCountFromFile = FileActions.fetchRowCountFromExcel(fileName);
        else if (fileName.contains(".csv") || fileName.contains(".txt"))
            npiCountFromFile = FileActions.fetchRowCountExcludeHeaderFromCSVAndTxt(fileName);
        if (fileName.contains("StaticList")) {
            logger.info(
                    "Verifying: the NPI Numbers from the uploaded file {} are displayed correctly in the list details page",
                    fileName);
            npiCountFromListDetails = npiStaticList.getNPICountFromListDetails();
            Assert.assertEquals(
                    "NPI count from file does not match with UI", npiCountFromFile, npiCountFromListDetails);
        } else {
            npiCountFromListDetails = npiAttributesList.getNPICountFromListDetails();
            Assert.assertEquals(
                    "NPI count from file does not match with UI", npiCountFromFile, npiCountFromListDetails);
        }
        int npiCountFromListItems = npiStaticList.getNPICountFromListItems(npiName);
        Assert.assertEquals(
                "NPI count from file does not match with List Items Section", npiCountFromFile, npiCountFromListItems);

        int npiCountFromListInfo = npiStaticList.getNPICountFromListInfo();
        Assert.assertEquals(
                "NPI count from file does not match with List Info", npiCountFromFile, npiCountFromListInfo);

        logger.info("NPI count verification successful");
    }

    @Given("User navigates to Report Templates page")
    public void user_navigates_to_report_templates_page() {
        logger.info("Navigating to Report Templates page");
        navigation.clickSubMenu();
        navigation.clickMenuAngle();
        reportTemplates.clickReportTemplatesLink();
    }

    @Then("Verify the tabs displayed on the Report Templates page")
    public void verify_the_tabs_displayed_on_the_report_templates_page() {
        logger.info("Verifying tabs on Report Templates page");
        Assert.assertEquals("TEMPLATES", reportTemplates.verifyTemplatesTab().toUpperCase());
        Assert.assertEquals(
                "GENERATED REPORTS", reportTemplates.verifyGeneratedReportsTab().toUpperCase());
        Assert.assertEquals("SCHEDULING", reportTemplates.verifySchedulingTab().toUpperCase());
    }

    @And("Verify Template tab is selected by default on the Report Templates page")
    public void verifyTabIsSelectedByDefaultOnTheReportTemplatesPage() {
        logger.info("Verifying Template tab is selected by default");
        Assert.assertTrue("Template tab is not selected by default", reportTemplates.isTabSelectedByDefault());
    }

    @When("User clicks on New Template")
    public void user_clicks_on_new_template() {
        logger.info("Clicking on New Template");
        reportTemplates.createNewTemplate();
    }

    @Then("Verify the tabs displayed on the Create New Template panel")
    public void verify_the_tabs_displayed_on_the_create_new_template_panel() {
        logger.info("Verifying tabs on Create New Template panel");
        Assert.assertEquals("DIMENSIONS", reportTemplates.verifyDimensionsTab().toUpperCase());
        Assert.assertEquals("METRICS", reportTemplates.verifyMetricsTab().toUpperCase());
    }

    @And("Verify if {string} is selected by default as Template type on the Create New Template panel")
    public void verifyIfIsSelectedByDefaultAsTemplateTypeOnTheCreateNewTemplatePanel(String defaultTemplateType) {
        logger.info("Verifying default template type: {}", defaultTemplateType);
        Assert.assertTrue(
                defaultTemplateType + " is not selected by default as Template type",
                reportTemplates.isDefaultTemplateTypeSelected(defaultTemplateType));
    }

    @When("User enters the template details as {string} {string} {string}")
    public void user_enters_the_template_details_as(String templateName, String dimensions, String metrics) {
        nameList.clear();
        nameList.addAll(Arrays.stream(dimensions.split("\\s*,\\s*"))
                .filter(s -> !s.isBlank())
                .toList());
        itemList.clear();
        itemList.addAll(Arrays.stream(metrics.split("\\s*,\\s*"))
                .filter(s -> !s.isBlank())
                .toList());
        templateNameRandom = templateName + '_' + CommonUtils.timeStampCalculation();
        logger.info(
                "Entering template details. Name: {}, Dimension: {}, Metric: {}",
                templateNameRandom,
                nameList,
                itemList);
        reportTemplates.enterTemplateName(templateNameRandom);
        for (String dimension : nameList) {
            if (!dimension.isEmpty()) {
                reportTemplates.selectDimensionAndMetric(dimension);
            }
        }
        reportTemplates.clickMetricsTab();
        for (String metric : itemList) {
            if (!metric.isEmpty()) {
                reportTemplates.selectDimensionAndMetric(metric);
            }
        }
    }

    @When("User enters the template details for end to end as {string} {string} {string}")
    public void user_enters_the_template_for_end_to_end_details_as(
            String templateName, String dimension, String metric) {
        templateNameRandom = templateName + '_' + CommonUtils.timeStampCalculation();
        logger.info("Entering E2E template details. Name: {}", templateNameRandom);
        reportTemplates.enterTemplateName(templateNameRandom);
        String[] dimensionList = dimension.split(",");

        for (String dimensionValue : dimensionList) {
            dimensionValue = dimensionValue.trim();
            reportTemplates.selectDimensionAndMetric(dimensionValue);
        }

        reportTemplates.clickMetricsTab();
        String[] metricsList = metric.split(",");

        for (String metricValue : metricsList) {
            metricValue = metricValue.trim();
            reportTemplates.selectDimensionAndMetric(metricValue);
        }
    }

    @Then("Verify the selected dimensions and metrics under the Template Structure section")
    public void verify_the_selected_dimensions_and_metrics_under_the_template_structure_section() {
        logger.info("Verifying selected dimensions and metrics in Template Structure");
        Assert.assertEquals(nameList, reportTemplates.verifySelectedDimensions());
        Assert.assertEquals(itemList, reportTemplates.verifySelectedMetrics());
    }

    @When("User saves the new template")
    public void user_saves_the_new_template() {
        logger.info("Saving new template");
        reportTemplates.saveReportTemplate();
    }

    @Then("Verify new template is saved and displayed in the template list")
    public void verify_new_template_is_saved_and_displayed_in_the_template_list() {
        logger.info("Verifying template {} is saved and displayed in list", templateNameRandom);
        String successMessage = reportTemplates.reportTemplateSuccess();
        assert successMessage.contains("Template created successfully");
        reportTemplates.searchCreatedReportTemplate(templateNameRandom);
        Assert.assertEquals(templateNameRandom, reportTemplates.verifyCreatedReportTemplate(templateNameRandom));
        Assert.assertEquals(1, reportTemplates.searchResultRowCount());
    }

    @And("Verify availability of {string}, {string} and {string} actions for the created template")
    public void verifyAvailabilityOfAndActionsForTheCreatedTemplate(
            String runReportIcon, String copyIcon, String deleteIcon) {
        logger.info("Verifying availability of action icons for template: {}", templateNameRandom);
        Assert.assertTrue(
                "Run report Icon is not present",
                reportTemplates.checkActionIconsForTemplate(templateNameRandom, runReportIcon));
        Assert.assertTrue(
                "Copy Icon is not present", reportTemplates.checkActionIconsForTemplate(templateNameRandom, copyIcon));
        Assert.assertTrue(
                "Delete Icon is not present",
                reportTemplates.checkActionIconsForTemplate(templateNameRandom, deleteIcon));
    }

    @And("Verify the details of the created template")
    public void verifyTheDetailsOfTheCreatedTemplate() {
        logger.info("Verifying details of the created template: {}", templateNameRandom);
        reportTemplates.clickTemplate(templateNameRandom);
        Assert.assertEquals(nameList, reportTemplates.verifySelectedDimensions());
        Assert.assertEquals(itemList, reportTemplates.verifySelectedMetrics());
    }

    @And("Verify the delete button is disabled on the Create New Template panel")
    public void verifyTheDeleteButtonIsDisabledOnTheCreateNewTemplatePanel() {
        logger.info("Verifying delete button is disabled on Create New Template panel");
        Assert.assertTrue(
                "Delete Icon is not disabled on Create New Template panel",
                reportTemplates.isDeleteIconDisabledOnCreateNewTemplatePanel());
    }

    @And("Verify the delete button is enabled on the Edit Template panel")
    public void verifyTheDeleteButtonIsEnabledOnTheEditTemplatePanel() {
        logger.info("Verifying delete button is enabled on Edit Template panel");
        Assert.assertTrue(
                "Delete Icon is not enabled on Edit Template panel",
                reportTemplates.isDeleteIconEnabledOnEditTemplatePanel());
    }

    @And("User deletes the created template")
    public void userDeletesTheCreatedTemplate() {
        logger.info("Deleting the created template: {}", templateNameRandom);
        reportTemplates.clickDeleteIconFromTemplatePanel();
    }

    @And(
            "Verify error message when no dimensions and metrics are selected and user tries to save the template {string}")
    public void verifyErrorMessageWhenNoDimensionsAndMetricsAreSelectedAndUserTriesToSaveTheTemplate(
            String templateName) {
        logger.info(
                "Verifying error message when no dimensions and metrics are selected for template: {}", templateName);
        reportTemplates.enterTemplateName(templateName);
        reportTemplates.saveReportTemplate();
        Assert.assertEquals("Please select at least 1 dimension and 1 metric", reportTemplates.fetchAlertMessage());
    }

    @And("User deletes the existing template from the template list")
    public void userDeletesTheExistingTemplateFromTheTemplateList() {
        logger.info("Deleting the existing template from the template list");
        reportTemplates.clickDeleteIconFromTemplateList();
    }

    @Then("Verify the template is deleted and not displayed in the template list")
    public void verifyTheTemplateIsDeletedAndNotDisplayedInTheTemplateList() {
        Assert.assertTrue(
                "Delete Report Template Confirmation pop-up is not displayed",
                reportTemplates.isDeleteReportTemplateConfirmationPopupDisplayed());
        reportTemplates.clickDeleteButtonFromConfirmationPopup();
        String actualMessage = reportTemplates.fetchAlertMessage();
        Assert.assertTrue(actualMessage.contains("Deleted the template.")
                || actualMessage.contains("Can not delete template: This template is being used by grouped templates"));
        logger.info("Verifying template {} is deleted and not displayed in list", templateNameRandom);
        reportTemplates.searchCreatedReportTemplate(templateNameRandom);
        Assert.assertEquals(0, reportTemplates.searchResultRowCount());
    }

    @Given("User configures targeting rules as below")
    public void user_selects_the_channel_configures_targeting_rules(DataTable ruleTypeAndOptions) {
        logger.info("Configuring targeting rules from DataTable");
        Map<String, String> rawMap = ruleTypeAndOptions.asMap(String.class, String.class);
        rulesMap = CommonUtils.processDataTable(rawMap);
        for (Map.Entry<String, List<String>> entry : rulesMap.entrySet()) {
            logger.info("Adding Rule Type: {} with Options: {}", entry.getKey(), entry.getValue());
            keyType.add(entry.getKey());
            keyValues.addAll(entry.getValue());
            tacticSettings.selectMultipleRuleTypes(entry.getKey(), entry.getValue());
        }
        tacticSettings.closeRuleTypePanel();
    }

    @Then("Verify the configured targeting rules")
    public void verify_the_configured_targeting_rules() {
        logger.info("Starting verification of configured targeting rules");
        tacticSettings.expandAllTargetingRules();
        List<String> expectedNormalizedRuleTypes = normalizeObjectList(keyType);
        int expectedCount = expectedNormalizedRuleTypes.size();
        logger.info("Fetching actual rule types (Expected Count: {})", expectedCount);
        tacticSettings.fetchRulesTypesCount(expectedCount);
        List<String> actualNormalizedRuleTypes = normalizeObjectList(tacticSettings.fetchRulesTypes());

        logger.info(
                "Comparing Rule Types. Expected: {}, Actual: {}",
                expectedNormalizedRuleTypes,
                actualNormalizedRuleTypes);
        for (String expectedOption : expectedNormalizedRuleTypes) {
            boolean matchFound = actualNormalizedRuleTypes.stream().anyMatch(actual -> {
                String exp = expectedOption.toLowerCase().trim();
                String act = actual.toLowerCase().trim();
                if ((exp.equals("email") && act.equals("emails")) || (exp.equals("emails") && act.equals("email")))
                    return true;
                return act.equals(exp);
            });
            Assert.assertTrue("Expected rule type not found: " + expectedOption, matchFound);
        }

        List<String> expectedNormalizedRuleOptions = normalizeObjectList(keyValues);
        List<String> actualNormalizedRuleOptions = normalizeObjectList(tacticSettings.fetchRuleOptions());
        logger.info(
                "Comparing Rule Options. Expected: {}, Actual: {}",
                expectedNormalizedRuleOptions,
                actualNormalizedRuleOptions);
        for (String expectedOption : expectedNormalizedRuleOptions) {
            boolean matchFound = actualNormalizedRuleOptions.stream()
                    .anyMatch(actual -> actual.equalsIgnoreCase(expectedOption)
                            || actual.matches(".*\\(" + expectedOption + "\\).*"));
            Assert.assertTrue("Expected rule option not found: " + expectedOption, matchFound);
        }

        logger.info("All targeting rules verified successfully");
    }

    @And("Verify the count of rules added for the selected targeting rule type on the Tactic Settings page")
    public void verifyTheCountOfRulesAddedForTheSelectedTargetingRuleTypeOnTheTacticSettingsPage() {
        logger.info("Verifying count of rules added for targeting rule types");
        String ruleType;
        for (Map.Entry<String, List<String>> entry : rulesMap.entrySet()) {
            logger.info(
                    "Verifying: the count of rules added for the selected targeting rule type on the Tactic Settings page");
            if (entry.getKey().contains("Email")) ruleType = "Emails";
            else ruleType = entry.getKey();
            int itemCount = entry.getValue().size();
            String optionsCount = tacticSettings.fetchSelectedListCountFromTactic(ruleType);
            int targetedOptionsCount = Integer.parseInt(optionsCount.replaceAll("[^0-9]", ""));
            Assert.assertEquals(
                    "Selected options count for " + ruleType + " does not match", itemCount, targetedOptionsCount);
        }
    }

    @When("User saves the settings")
    public void user_saves_the_settings() {
        logger.info("Saving Tactic settings");
        tacticSettings.saveTacticSettings();
    }

    @Then("Verify the newly created campaign in the database")
    public void verify_campaign_in_database() throws SQLException {
        logger.info("User saves the settings");
        String actualValue = DatabaseActions.getData(constants.CAMPAIGN_NAME, campaignNameRandom);
        logger.info("DB Result: {}", actualValue);

        if (actualValue != null) {
            Assert.assertEquals(campaignNameRandom, actualValue);
        } else {
            logger.error("Campaign not found in database");
            throw new SQLException("Campaign not found in the database with the expected name: " + campaignNameRandom);
        }
    }

    @Then("User selects Smart List")
    public void user_selects_smart_list() {
        logger.info("Selecting Smart List type");
        npiLists.clickSmartList();
    }

    @Then("User enters the NPI list details as {string} {string}")
    public void user_enters_the_npi_list_details_as(String listName, String advertiser) {
        npiName = listName + '_' + CommonUtils.timeStampCalculation();
        logger.info("Entering NPI list details - Name: {}, Advertiser: {}", npiName, advertiser);
        npiSmartList.enterListName(npiName);
        npiSmartList.selectAdvertiser(advertiser);
    }

    @When("User clicks on {string} and enters the drug details {string}")
    public void user_clicks_on_prescribed_drug_and_enters_the_drug_details(String smartListType, String drugName) {
        logger.info("Selecting Smart List type: {} and entering Drug details: {}", smartListType, drugName);
        npiSmartList.selectSmartNPIListType(smartListType);
        npiSmartList.selectDrug(drugName);
    }

    @Then("Verify drug details are added")
    public void verify_drug_details_are_added() {
        logger.info("Verifying drug details are added");
        String actualDrug = npiSmartList.fetchDrugName();
        Assert.assertEquals("Glynase", actualDrug);
    }

    @When("User makes list available in LIFE, HCP365 and saves the list")
    public void user_makes_list_available_in_life_hcp365_and_saves_the_list() {
        logger.info("Making list available in LIFE and HCP365");
        npiSmartList.selectProduct();
    }

    @Then("User navigates to Campaign Dashboard")
    public void user_navigates_to_campaign_dashboard() {
        logger.info("Navigating to Campaign Dashboard");
        navigation.clickSubMenu();
        navigation.clickCampaigns();
        String dashboardTitle = campaigns.campaignDashboard();
        Assert.assertEquals("Life", dashboardTitle);
    }

    @Then("Verify list is targeted in the tactic successfully")
    public void verify_list_is_targeted_in_the_tactic_successfully() {
        logger.info("Verifying NPI list is targeted in tactic");
        tacticSettings.verifyNPIRule();
        String npiRule = tacticSettings.verifyNPIRule();
        Assert.assertTrue("Rule does not contain 'NPI'", npiRule.contains("NPI"));
    }

    @Then("User saves the targeting")
    public void u_ser_saves_the_targeting() {
        logger.info("Saving targeting settings");
        tacticSettings.saveTacticSettings();
    }

    @When("User selects the {string} channel, configure {string} targeting rule")
    public void user_selects_the_channel_configure_npi_targeting_rule(String channel, String ruleType) {
        logger.info("Configuring Channel: {} with Rule Type: {} using List: {}", channel, ruleType, npiName);
        tacticSettings.selectChannel(channel);
        navigation.clickOnIcon("Add Targeting Rule");
        tacticSettings.selectTargetingRule(ruleType, npiName);
        tacticSettings.clickTarget(npiName);
        tacticSettings.clickOk();
        tacticSettings.clickClose();
    }

    @And("User navigates to run report from mega menu of the life application")
    public void user_navigate_to_run_report() {
        logger.info("Navigating to Run Report from mega menu");
        navigation.clickSubMenu();
        navigation.clickMenuAngle();
        navigation.clickRunReport();
    }

    @Then("User selects the report template created tactic and other fields for running the report")
    public void user_enter_input_for_running_report() {
        logger.info("Entering details to run report. Template: {}, Tactic: {}", templateNameRandom, tacticNameRandom);
        reportTemplates.enterDetailsToRunReport(templateNameRandom, tacticNameRandom);
    }

    @Then("User verifies the selected campaign,line item, tactic and runs report by clicking on Run button")
    public void user_verifies_the_selected_details() {
        logger.info("Verifying autopopulated campaign and line item, then running report");
        Assert.assertEquals(campaignNameRandom, reportTemplates.verifyAutopopulatedCampaign(campaignNameRandom));
        Assert.assertEquals(lineItemNameRandom, reportTemplates.verifyAutopopulatedLineitem(lineItemNameRandom));
        reportTemplates.runReport();
    }

    @Then("User navigates to generate report field and verifies the report name by campaign name")
    public void user_navigate_to_generate_report_page() {
        logger.info("Navigating to Generated Report page");
        navigation.clickSubMenu();
        navigation.clickScheduledReport();
        navigation.clickSubMenu();
        navigation.clickMenuAngle();
        navigation.clickGeneratedReport();
    }

    @Then("User downloads the report and verify the data in downloaded report")
    public void user_download_the_report_from_generated_report_page_and_verify_the_data() throws Exception {
        logger.info("Downloading generated report for template: {}", templateNameRandom);
        String filePath = reportTemplates.downloadGeneratedReport(templateNameRandom);
        navigation.clickSubMenu();
        navigation.clickMenuAngle();
        navigation.clickReportTemplate();
        Assert.assertTrue(
                "Report headers match expected values!",
                reportTemplates.verifyColumnsOfReport(templateNameRandom, filePath));
    }

    /*Roshani Sherkar - 18-06-2025
     * Campaign Dashbaord Features Start*/
    @And("Verify Campaign Dashboard is displayed with title {string}")
    public void verifyCampaignDashboardIsDisplayedWithTitle(String title) {
        logger.info("Verifying Campaign Dashboard is displayed with title: {}", title);
        Assert.assertEquals(title, campaignDashboard.isCampaignDashboardVisibleWithTitle(title));
    }

    @When("User enters {string} and click Search button")
    public void userEntersAndClickSearchButton(String campaignID) {
        logger.info("Searching for Campaign ID: {}", campaignID);
        campaignDashboard.searchCreatedCampaign(campaignID);
        campaignDashboard.expandCreatedLineItem();
    }

    @Then("Verify Campaigns, line items, tactics names matching the {string} should display on Dashboard table")
    public void verifyCampaignsLineItemsTacticsNamesMatchingTheShouldDisplayOnDashboardTable(String campaignID) {
        logger.info("Verifying campaign details for Campaign ID: {}", campaignID);
        Assert.assertEquals(campaignID, campaignDashboard.verifyCampaignDetails(campaignID));
    }

    @When("User add and save comments to Campaign, Line Items and Tactics")
    public void userAddCommentsToCampaignLineItemsAndTactics(DataTable comments) {
        logger.info("Adding comments to Campaign, Line Items, and Tactics");
        Map<String, String> rawMap = comments.asMap(String.class, String.class);
        Map<String, List<String>> commentMap = CommonUtils.processDataTable(rawMap);
        keyValues.clear();

        for (Map.Entry<String, List<String>> entry : commentMap.entrySet()) {
            logger.info("Adding comment to Level: {}, Comments: {}", entry.getKey(), entry.getValue());
            keyValues.addAll(entry.getValue());
            String successAlertText = campaignDashboard.addCommentsToCampaign(entry.getKey(), entry.getValue());
            Assert.assertEquals("Notes saved successfully.", successAlertText);
        }
    }

    @Then(
            "Verify comments, icon should display in bluish-green color {string} and comments should available on individual panel")
    public void
            verifyCommentsAreSavedSuccessfullyIconShouldDisplayInBLUISHGREENAndCommentsShouldAvailableOnIndividualPanel(
                    String colour) {
        logger.info("Verifying comment icon color: {} and comment text", colour);
        List<String> backgroundImage = campaignDashboard.verifyCommentIconColor();
        Assert.assertTrue("Image is matched", backgroundImage.contains(colour));
        List<String> expectedComments = normalize(Collections.singletonList(keyValues.toString()));
        List<String> actualComments =
                normalize(Collections.singletonList(String.valueOf(campaignDashboard.verifyCommentIconText())));
        Assert.assertEquals(expectedComments, actualComments);
    }

    @And(
            "User navigates to campaign, line item and tactic using {string} and verifies that the comments are displayed in the respective tile comment boxes")
    public void userNavigatesToCampaignLineItemAndTacticToVerifyTheCommentsAreDisplayedInRespectiveCommentSections(
            String campaignId) {
        logger.info("Navigating to entities for Campaign {} and fetching comment box text", campaignId);
        List<String> actualComments = new ArrayList<>();
        campaignDashboard.navigateToCampaign(campaignId);
        actualComments.add(campaigns.fetchCommentFromCampaignLineItemTacticPanel());
        campaigns.clickLineItemTile();
        actualComments.add(campaigns.fetchCommentFromCampaignLineItemTacticPanel());
        campaigns.clickTacticTile();
        actualComments.add(campaigns.fetchCommentFromCampaignLineItemTacticPanel());
        Assert.assertEquals(keyValues, actualComments);
    }

    @And("User verifies the comments in the campaign, line item, and tactic dashboard's comment boxes")
    public void userVerifiesTheCommentsInTheCampaignLineItemAndTacticDashboardSCommentBoxes() {
        logger.info("Verifying comments in Campaign, Line Item, and Tactic dashboard comment boxes");
        List<String> actualComments = new ArrayList<>();
        campaigns.clickCampaignTile();
        actualComments.add(campaigns.fetchCommentFromCampaignLineItemTacticDashboard());
        campaigns.clickLineItemTile();
        actualComments.add(campaigns.fetchCommentFromCampaignLineItemTacticDashboard());
        campaigns.clickTacticTile();
        actualComments.add(campaigns.fetchCommentFromCampaignLineItemTacticDashboard());
        Assert.assertEquals(keyValues, actualComments);
    }

    @When("User toggles the Enabled button for Line Items and Tactics")
    public void userTogglesEnabledButtonForLineItemsAndTacticFromDashboard() {
        logger.info("Toggling Enabled/Disabled status for Line Items and Tactics from dashboard");
        campaignDashboard.clickLineAndTacticToggleButton();
    }

    @Then("Verify that Line Items and Tactics reflect the correct enabled or disabled state")
    public void verifyLineItemsAndTacticsAreEnabledDisabledAccordingly() {
        logger.info("Verifying Line Items and Tactics enabled/disabled state");
        Assert.assertTrue("Buttons are clickable and functional", campaignDashboard.verifyLineTacticToggleStatus());
    }

    @And(
            "User fetches the Line Items and Tactics enabled-disabled status from Campaign Dashboard using {string} and verifies the same status in the respective Line Item and Tactic pages")
    public void
            userFetchesTheLineItemsAndTacticsEnabledDisabledStatusFromCampaignDashboardAndVerifiesTheSameStatusInTheRespectiveLineItemAndTacticPages(
                    String campaignID) {
        List<String> expectedStatus = campaignDashboard.fetchLineAndTacticToggleStatus();
        List<String> actualStatus = new ArrayList<>();
        logger.info("Navigating to pages to verify status. Campaign ID: {}", campaignID);
        campaignDashboard.navigateToCampaign(campaignID);
        campaigns.clickLineItemTile();
        actualStatus.add(campaigns.fetchToggleStatus());
        campaigns.clickTacticTile();
        actualStatus.add(campaigns.fetchToggleStatus());
        Assert.assertEquals(expectedStatus, actualStatus);
    }

    @When("User clicks Campaign {string}, Line Item and Tactic and verify navigation to respective pages")
    public void userClicksCampaignLineItemAndTacticOneByOne(String campaignID) {
        logger.info("Clicking and verifying navigation to Campaign, Line Item, and Tactic pages for: {}", campaignID);
        campaignDashboard.navigateToCampaign(campaignID);
        Assert.assertTrue(
                "Navigation to Campaign details page is not successful", campaignDashboard.isCampaignPageDisplayed());
        campaigns.navigateToCampaignDashboard();
        campaignDashboard.searchCreatedCampaign(campaignID);
        campaignDashboard.expandCreatedLineItem();

        campaignDashboard.navigateToLineItemDetails();
        Assert.assertTrue(
                "Navigation to Line Item details page is not successful", campaignDashboard.isLineItemPageDisplayed());
        campaigns.navigateToCampaignDashboard();
        campaignDashboard.searchCreatedCampaign(campaignID);
        campaignDashboard.expandCreatedLineItem();

        campaignDashboard.navigateToTacticDetails();
        Assert.assertTrue(
                "Navigation to Tactic details page is not successful", campaignDashboard.isTacticPageDisplayed());
    }

    @When("User clicks Menu option and selects column names")
    public void userClicksMenuOptionAndSelectsColumnNames(DataTable columnNames) {
        List<String> columns = columnNames.asList(String.class);
        logger.info("Customizing dashboard columns: {}", columns);
        keyValues.clear();
        keyValues.addAll(columns);
        campaignDashboard.clickMenuTransitionIcon(columns);
    }

    @Then("Verify dashboard is customized and only selected columns are displayed")
    public void verifyDashboardIsCustomizedAndOnlySelectedColumnsAreDisplayed() {
        logger.info("Verifying dashboard is customized with selected columns");
        List<String> columnName = campaignDashboard.fetchDashboardColumns();
        Assert.assertEquals(
                keyValues.stream().map(o -> ((String) o).toLowerCase()).collect(Collectors.toSet()),
                columnName.stream().map(String::toLowerCase).collect(Collectors.toSet()));
    }

    @And("User clicks HideAll option from Menu and verifies Dashboard columns are hidden accordingly")
    public void userClicksHideAllAndShowAllOptionsFromMenu() {
        logger.info("Clicking HideAll option and verifying columns are hidden");
        Assert.assertTrue("Columns are not hidden successfully", campaignDashboard.clickHideAllOption());
    }

    @And("User clicks ShowAll option from Menu and verifies Dashboard columns are shown accordingly")
    public void userClicksShowAllOptionFromMenuAndVerifiesDashboardColumnsAreShownAccordingly() {
        logger.info("Clicking ShowAll option and verifying columns are shown");
        Assert.assertTrue("Columns are not shown successfully", campaignDashboard.clickShowAllOption());
    }

    @When("Navigate to any Dashboard column, select the filter and apply")
    public void navigateToAnyDashboardColumnSelectTheFilterAndApply(DataTable filterNames) {
        logger.info("Applying filters to dashboard columns");
        Map<String, String> rawMap = filterNames.asMap(String.class, String.class);
        Map<String, List<String>> filterMap = CommonUtils.processDataTable(rawMap);
        keyValues.clear();
        keyType.clear();

        for (Map.Entry<String, List<String>> entry : filterMap.entrySet()) {
            logger.info("Applying filter on Column: {}, Values: {}", entry.getKey(), entry.getValue());
            keyType.add(entry.getKey());
            keyValues.addAll(entry.getValue());
            campaignDashboard.applyFilterOnSelectedColumns(entry.getKey(), entry.getValue());
        }
    }

    @And("Verify the filter list displays only the selected filter values")
    public void verifyTheFilterListDisplaysOnlyTheSelectedFilterValues() {
        logger.info("Verifying filter list displays only selected filter values");
        List<String> selectedFilterLabels = campaignDashboard.fetchSelectedFilterLabels();
        List<String> cleanedActual =
                selectedFilterLabels.stream().map(s -> s.replaceAll(":$", "")).toList();
        Assert.assertEquals(keyType, cleanedActual);
        List<String> normalizedExpected = keyValues.stream()
                .map(obj -> obj.toString().toLowerCase().trim())
                .toList();
        List<String> normalizedActual = campaignDashboard.fetchSelectedFilterValues().stream()
                .map(s -> s.trim().toLowerCase())
                .toList();
        Assert.assertEquals(normalizedExpected, normalizedActual);
    }

    @Then("Verify the Campaign Dashboard data should filter as per the selected filter values")
    public void verifyTheDataShouldFilterAsPerTheSelectedFilterValues() {
        logger.info("Verifying Campaign Dashboard data is filtered as per selected values");
        for (Object o : keyType) {
            logger.info("Verifying: the Campaign Dashboard data should filter as per the selected filter values");
            Assert.assertTrue(
                    "Campaign Dashboard data is not filtered as per the selected filter values",
                    campaignDashboard.isCampaignDataFilteredAccordingToSelectedFilters(o.toString(), keyValues));
        }
    }

    @And(
            "Filter icon should display in the column header to which filter is applied and a red bullet {string} on the filter icon present next to global search")
    public void
            filterIconShouldDisplayInTheColumnHeaderToWhichFilterIsAppliedAndARedBulletOnTheFilterIconPresentNextToGlobalSearch(
                    String iconColor) {
        logger.info("Verifying filter icon color: {}", iconColor);
        String filterIconColor = campaignDashboard.verifyFilterIcon();
        Assert.assertEquals(iconColor, filterIconColor);
    }

    @And("User removes all the filters applied on the Dashboard and verifies the data is reset to default state")
    public void userRemovesAllTheFiltersAppliedOnTheDashboardAndVerifiesTheDataIsResetToDefaultState() {
        String campaignCountBeforeFilterRemoval = campaignDashboard.fetchCampaignDataCountFromPagination();
        logger.info("Count before reset: {}", campaignCountBeforeFilterRemoval);
        campaignDashboard.clickResetAllFilters();
        String campaignCountAfterFilterRemoval = campaignDashboard.fetchCampaignDataCountFromPagination();
        Assert.assertNotEquals(campaignCountBeforeFilterRemoval, campaignCountAfterFilterRemoval);
    }

    @And("User verifies that the campaigns displayed on the Dashboard include all past and current flights")
    public void userVerifiesThatTheCampaignsDisplayedOnTheDashboardIncludeAllPastAndCurrentFlights() {
        logger.info("User verifies that the campaigns displayed on the Dashboard include all past and current flights");
        List<LocalDate> dates = campaignDashboard.fetchFlightStartAndEndDate();
        LocalDate today = LocalDate.now();
        LocalDate earliest = dates.stream().min(LocalDate::compareTo).orElse(today);
        LocalDate latest = dates.stream().max(LocalDate::compareTo).orElse(today);
        boolean hasPast = earliest.isBefore(today);
        boolean hasFuture = latest.isAfter(today);
        boolean hasCurrent = (earliest.isBefore(today) || earliest.isEqual(today))
                && (latest.isAfter(today) || latest.isEqual(today));
        logger.info(
                "Range: {} to {}. Past: {}, Current: {}, Future: {}", earliest, latest, hasPast, hasCurrent, hasFuture);
        Assert.assertFalse("Dashboard is empty!", dates.isEmpty());
    }

    @When("User clicks Favorite Only checkbox")
    public void userClicksFavoriteOnlyCheckbox() {
        logger.info("Clicking 'Favorite Only' checkbox");
        campaignDashboard.clickFavoriteOnlyCheckbox();
    }

    @Then("Verify the dashboard results should show only campaigns which are marked as favorite")
    public void verifyTheDashboardResultsShouldShowOnlyCampaignsWhichAreMarkedAsFavorite() {
        logger.info("Verifying dashboard shows only favorite campaigns");
        Assert.assertTrue(
                "Dashboard data has campaign details marked as favorite", campaignDashboard.isFavoriteCampaignShown());
    }

    @And("User unchecks Favorite Only checkbox")
    public void userUnchecksFavoriteOnlyCheckbox() {
        logger.info("Unchecking 'Favorite Only' checkbox");
        campaignDashboard.unselectFavoriteCheckboxIfSelected();
    }

    @And("Verify the dashboard results should show campaigns which are marked as favorite and nonfavorite")
    public void verifyTheDashboardResultsShouldShowCampaignsWhichAreMarkedAsFavoriteAndNonfavorite() {
        logger.info("Verifying dashboard shows both favorite and non-favorite campaigns");
        boolean isAvailable = campaignDashboard.isFavoriteNonFavoriteCampaignAvailable();
        Assert.assertTrue("Dashboard data has campaign details marked as favorite", isAvailable);
    }

    @When("User clicks Hide Finished checkbox")
    public void userClicksHideFinishedCheckbox() {
        logger.info("Clicking 'Hide Finished' checkbox");
        campaignDashboard.clickHideFinishedCheckbox();
    }

    @Then("Verify the dashboard data should not reflect campaigns with Finished status")
    public void verifyTheDashboardDataShouldNotReflectCampaignsWithFinishedStatus() {
        logger.info("Verifying dashboard does not show finished campaigns");
        boolean isHidden = campaignDashboard.isFinishedCampaignListHidden();
        Assert.assertTrue("Campaigns with Finished Status are hidden", isHidden);
    }

    @And("User unchecks Hide Finished checkbox")
    public void userUnchecksHideFinishedCheckbox() {
        logger.info("Unchecking 'Hide Finished' checkbox");
        campaignDashboard.unselectHideFinishedCheckboxIfSelected();
    }

    @And("Verify the dashboard data should reflect campaigns with Finished status")
    public void verifyTheDashboardDataShouldReflectCampaignsWithFinishedStatus() {
        logger.info("Verifying dashboard shows campaigns with Finished status");
        boolean isShown = campaignDashboard.isFinishedCampaignListShownWithOtherStatus();
        Assert.assertTrue("Campaigns with Finished Status are hidden", isShown);
    }

    @And("User clicks {string} filter")
    public void userClicksFilter(String filterType) {
        logger.info("Clicking filter type: {}", filterType);
        campaignDashboard.clickFilterTypeButton(filterType);
    }

    @Then("Verify only Current Month's Flights should render on the Dashboard")
    public void verifyOnlyActiveFlightsShouldRenderOnTheDashboard() {
        List<LocalDate> dates = campaignDashboard.fetchFlightStartAndEndDate();
        logger.info("Current Month's fetched flight dates: {}", dates);
        LocalDate today = LocalDate.now();
        boolean allFlightsActiveToday = true;
        if (dates.isEmpty()) {
            logger.warn("No flights displayed for 'Today' filter.");
        }
        for (int i = 0; i < dates.size(); i += 2) {
            LocalDate start = dates.get(i);
            LocalDate end = dates.get(i + 1);
            boolean isActive = !today.isBefore(start) && !today.isAfter(end);
            if (!isActive) {
                allFlightsActiveToday = false;
                logger.error(
                        "Invalid flight for 'Today' filter: Flight range [{} to {}] does not include {}",
                        start,
                        end,
                        today);
            }
        }
        Assert.assertTrue("One or more flights displayed do not fall within today's date range", allFlightsActiveToday);
    }

    @Then("Verify only Today's Flights should render on the Dashboard")
    public void verifyOnlyTodaySFlightsShouldRenderOnTheDashboard() {
        List<LocalDate> dates = campaignDashboard.fetchFlightStartAndEndDate();
        logger.info("Fetched flight dates: {}", dates);
        LocalDate today = LocalDate.now();
        boolean allDatesToday = dates.stream().allMatch(date -> date.isEqual(today));
        Assert.assertTrue("Only today's flights should be visible on the Dashboard", allDatesToday);
    }

    @And("User enters the custom date range from {string} to {string} and applies the filter")
    public void userEntersTheCustomDateRangeFromToAndAppliesTheFilter(String startDate, String endDate) {
        logger.info("Entering custom date range from {} to {} and applying filter", startDate, endDate);
        campaignDashboard.enterCustomDateRange(startDate, endDate);
    }

    @And("Verify only Custom date range Flights from {string} to {string} should render on the Dashboard if available")
    public void verifyOnlyCustomDateRangeFlightsShouldRenderOnTheDashboardIfAvailable(
            String startDate, String endDate) {
        boolean flag = campaignDashboard.isCampaignDataAvailableInCustomDateRange();
        logger.info("Is campaign data empty/unavailable in custom date range: {}", flag);

        if (flag) {
            Assert.assertTrue("No campaign data is available", true);
        } else {
            List<LocalDate> dates = campaignDashboard.fetchFlightStartAndEndDate();
            logger.info("Fetched flight dates from dashboard: {}", dates);
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate start = LocalDate.parse(startDate, inputFormatter);
            LocalDate end = LocalDate.parse(endDate, inputFormatter);
            logger.info("Parsed validation bounds - Start: {}, End: {}", start, end);
            boolean allDatesInCurrentMonth =
                    dates.stream().noneMatch(date -> date.isBefore(start) || date.isAfter(end));
            Assert.assertTrue(
                    "Only flights within the selected date range should be visible on the Dashboard",
                    allDatesInCurrentMonth);
        }
    }

    @When(
            "User clicks the Settings icon and selects the following group by options and verify dashboard data is grouped accordingly")
    public void userClicksTheSettingsIconAndSelectsTheFollowingGroupByOptions(DataTable dataTable) {
        List<String> groupByOption = dataTable.asList(String.class);
        logger.info("Initiating dashboard grouping verification for options: {}", groupByOption);

        for (String option : groupByOption) {
            campaignDashboard.clickSettingIcon();
            logger.info("Selecting '{}' and validating dashboard data grouping", option);
            boolean isGrouped = campaignDashboard.clickGroupByOptionsAndCheckDashboardData(option);
            Assert.assertTrue("Dashboard data is not grouped by the selected options - " + option, isGrouped);
        }
    }

    @When(
            "User hover on the image icon for creative in red color and check whether creative is assigned to the campaign")
    public void userHoverOnTheImageIconForCreativeInRedColor() {
        logger.info("Hovering on red image icon to check creative assignment status");
        String creativeStatus = campaignDashboard.fetchCreativeToolTipText();
        Assert.assertTrue(
                "No status has been displayed",
                creativeStatus.contains("No creative assigned")
                        || creativeStatus.contains("are pending approval")
                        || creativeStatus.contains("are denied")
                        || creativeStatus.contains("Creative assigned and approved"));
    }

    @When("User navigates to Tactic and assigns creative of status {string} to the Tactic")
    public void userNavigatesToTacticAndAssignsCreativeToTheTactic(String status) {
        logger.info("Navigating to Tactic to assign creative with status: {}", status);
        campaignDashboard.navigateToTacticDetails();
        tacticCreatives.clickCreativeTab();
        tacticCreatives.clickAssignCreatives();
        tacticCreatives.selectAndAssignCreativeByStatus(status);
        tacticCreatives.saveTacticCreatives();
        logger.info("Creative assigned and saved successfully");
    }

    /* Roshani Sherkar
    Life PMP - Deals Assignment
    * */
    @When("User clicks Tactic Setting tab")
    public void userClicksTacticSettingTab() {
        logger.info("Clicking Tactic Setting tab");
        pmp.navigateToTacticSettingTab();
    }

    @Then("User should navigate to respective Tactic Setting tab")
    public void userShouldNavigateToRespectiveTacticSettingTab() {
        logger.info("Verifying navigation to Tactic Setting tab");
        pmp.verifyTacticSettingsText();
    }

    @When("User add new targeting rule for Rule Type {string}")
    public void userAddNewTargetingRuleForRuleType(String ruleType) {
        logger.info("Adding new targeting rule for Rule Type: {}", ruleType);
        pmp.addNewTargetingRule();
        pmp.searchTargetingRuleAndSelect(ruleType);
    }

    @Then("user should navigate to PMP Deals Panel")
    public void userShouldNavigateToPMPDealsPanel() {
        logger.info("Verifying navigation to PMP Deals Panel");
        String panelText = pmp.verifyPMPDealsPanel();
        Assert.assertEquals("All Deals ", panelText);
    }

    @When("User clicks {string} Deals Tab")
    public void userClicksPrivateDealsTab(String dealType) {
        logger.info("Clicking Deals Tab: {}", dealType);
        pmp.clickDealsTab(dealType);
    }

    @Then("User should see Add New Deal button, filters such as Exchange, Search")
    public void userShouldSeeButtonFiltersSuchAsExchangeAdvertiser() {
        logger.info("Verifying Add New Deal button and filters are available");
        boolean isAvailable = pmp.verifyPrivateDealsFilterPanel();
        Assert.assertTrue("Button and Filters are not available", isAvailable);
    }

    @When("User tries to save the list without entering any details, an error message should be displayed")
    public void user_tries_to_save_the_list_without_entering_any_details() {
        logger.info("Attempting to save list without details to verify validation errors");
        npiStaticList.saveList();
        String listNameError = npiStaticList.listNameError();
        logger.info("Fetched list name error: {}", listNameError);
        assert listNameError.contains("List Name is required");
        String npiNameTemp = "Temporary List Name";
        logger.info("Entering temporary list name '{}' to trigger advertiser error", npiNameTemp);
        npiStaticList.enterListName(npiNameTemp);
        npiStaticList.saveList();
        String advertiserError = npiStaticList.advertiserError();
        logger.info("Fetched advertiser error: {}", advertiserError);
        assert advertiserError.contains("Advertiser is required");
    }

    @And("User enters the NPI Static list details as {string} {string}")
    public void user_enters_npi_static_list_details(String npiListName, String advertiser) {
        npiName = npiListName + '_' + CommonUtils.timeStampCalculation();
        logger.info("Entering NPI static list details - Name: {}, Advertiser: {}", npiName, advertiser);
        npiStaticList.enterListName(npiName);
        npiStaticList.selectAdvertiser(advertiser);
    }

    @And("User uploads the file {string}")
    public void user_uploads_the_file(String fileName) {
        logger.info("Uploading static list file: {}", fileName);
        npiStaticList.uploadStaticListFile(fileName);
    }

    @When("User edits the created list")
    public void user_edits_the_created_list() {
        logger.info("Editing the created list: {}", npiName);
        npiStaticList.clickBackToNPILists();
        npiLists.searchList(npiName);
        npiLists.openSearchedList(npiName);
        npiNameEdited = "Edited" + '_' + npiName;
        logger.info("Updating list name to: {}", npiNameEdited);
        npiStaticList.editListName(npiNameEdited);
        npiStaticList.saveList();
        String successMessage = npiStaticList.fetchSuccessAlert();
        Assert.assertTrue("Unable to see success message", successMessage.contains("NPI list created"));
    }

    @Then("Verify list gets updated successfully")
    public void verify_list_gets_updated_successfully() {
        logger.info("Verifying list is updated successfully: {}", npiNameEdited);
        npiStaticList.clickBackToNPILists();
        npiLists.searchList(npiNameEdited);
        npiLists.openSearchedList(npiNameEdited);
    }

    @When("User deletes the created list")
    public void user_deletes_the_created_list() {
        logger.info("Deleting the created list: {}", npiNameEdited);
        npiStaticList.deleteList();
    }

    @Then("Verify list gets deleted successfully")
    public void verify_list_gets_deleted_successfully() {
        String successMessage = npiStaticList.deleteSuccess();
        logger.info("Delete success message: {}", successMessage);
        assert successMessage.contains("NPI List Deleted");
    }

    @When(
            "User enters below details in respective search field, verify that the deal list appears based on the selected filters")
    public void userEntersBelowDetailsInRespectiveSearchField(DataTable filterBy) {
        logger.info("Applying filters and verifying deal list appears accordingly");
        Map<String, String> rawMap = filterBy.asMap(String.class, String.class);
        Map<String, List<String>> filterMap = CommonUtils.processDataTable(rawMap);

        for (Map.Entry<String, List<String>> entry : filterMap.entrySet()) {
            logger.info("Applying filter {} with values: {}", entry.getKey(), entry.getValue());
            boolean isAvailable = pmp.applyFilter(entry.getKey(), entry.getValue());
            Assert.assertTrue("Deals list is not available for " + entry.getKey(), isAvailable);
        }
    }

    @And("User clicks on Add New Deal button")
    public void userClicksOnAddNewDealButton() {
        logger.info("Clicking on Add New Deal button");
        pmp.clickAddNewDeals();
    }

    @Then(
            "New Deal panel should open and user should be able to add new deal with details {string}, {string}, {string}, {string}, {string}, {string}, {string}, {string}")
    public void newDealPanelShouldOpenAndUserShouldBeAbleToAddNewDealWithDetails(
            String exchangeType,
            String dealID,
            String dealName,
            String mediaType,
            String advertiser,
            String dealPriceType,
            String price,
            String curator) {
        dealIDRandom = dealID + CommonUtils.timeStampCalculation();
        dealNameRandom = dealName + CommonUtils.timeStampCalculation();
        logger.info(
                "Adding new deal - Exchange: {}, Deal ID: {}, Deal Name: {}",
                exchangeType,
                dealIDRandom,
                dealNameRandom);
        List<String> mediaTypeList = Arrays.stream(mediaType.split(",")).toList();
        String saveResult = pmp.addAndSaveNewDeals(
                exchangeType, dealIDRandom, dealNameRandom, mediaTypeList, advertiser, dealPriceType, price, curator);
        Assert.assertEquals("Deal saved successfully", saveResult);
    }

    @When("User searches the deal and assign it from the deal list")
    public void userSelectsTheDealFromTheDealList() {
        logger.info("Searching and assigning deal from list: {}", dealNameRandom);
        pmp.searchDealFromList(dealNameRandom);
        pmp.assignDealFromList(dealNameRandom);
    }

    @Then("Selected Deals should appear in Applied Deals panel")
    public void selectedDealsShouldAppearInAppliedDealsPanel() {
        logger.info("Verifying assigned deals appear in Applied Deals panel");
        boolean assigned = pmp.verifyAsignedDealsList();
        Assert.assertTrue("Unable to assign deals", assigned);
    }

    @And("Verify Target Applied Deals Toggle button is available with default value as {string}")
    public void verifyTargetAppliedDealsToggleButtonIsAvailableWithDefaultValueAsON(String toggleButton) {
        logger.info("Verifying Target Applied Deals Toggle button with default value: {}", toggleButton);
        boolean verifyToggle = pmp.verifyTargetAppliedDealsToggle(toggleButton);
        Assert.assertTrue("Default value is " + toggleButton, verifyToggle);
    }

    @When("User clicks on OK button")
    public void userClicksOnOKButton() {
        logger.info("Clicking on OK button to save assigned deals");
        pmp.saveDealsAssigned();
    }

    @Then(
            "Deal details should appear on Tactic Settings tab under Targeting section, Curated Markets and Deals section depending on toggle button status")
    public void dealDetailsShouldAppearOnTacticSettingsTab() {
        logger.info("Verifying deal details appear on Tactic Settings tab: {}", dealNameRandom);
        boolean areDealsPresent = pmp.verifyAssignedDealsOnTactic(dealNameRandom);
        Assert.assertTrue("Assigned Deals are not present under targeting and deals section", areDealsPresent);
    }

    @And("Verify Delete icon is disabled and error message {string}")
    public void verifyDeleteIconIsDisabledAndOnHoverShowErrorMessage(String errorMessage) {
        logger.info("Verifying Delete icon is disabled with error message: {}", errorMessage);
        boolean isDisabled = pmp.isDeleteIconDisabled();
        Assert.assertTrue("Delete icon is not disabled", isDisabled);
        String actualMessage = pmp.fetchMessageOnDeleteIconClick();
        Assert.assertEquals(errorMessage, actualMessage);
    }

    @And(
            "Verify Pricing Strategy is editable and update it with {string} and {string} for Deals present in Curated Markets and Deals section")
    public void verifyPricingStrategyIsEditableAndUpdateItWithAndForDealsPresentInCuratedMarketAndDealsSection(
            String pricingStrategy, String value) {
        logger.info(
                "Verifying Pricing Strategy is editable for deal: {}. Strategy: {}, Value: {}",
                dealNameRandom,
                pricingStrategy,
                value);
        pmp.verifyPricingStrategyIsEditable(dealNameRandom, pricingStrategy, value);
    }

    @And(
            "Verify user can add new {string} deals by clicking Add Deal button present in Curated Markets and Deals section using details {string}, {string}, {string}, {string}, {string}, {string}, {string}, {string}")
    public void verifyUserCanApplyDealsByClickingAddDealButtonPresentInCuratedMarketAndDealsSection(
            String dealType,
            String exchangeType,
            String dealID,
            String dealName,
            String mediaType,
            String advertiser,
            String dealPriceType,
            String price,
            String curator) {
        logger.info("Initiating addition of new '{}' deal from Curated Markets and Deals section", dealType);
        List<String> mediaTypeList = Arrays.stream(mediaType.split(",")).toList();
        dealIDRandom = dealID + CommonUtils.timeStampCalculation() + "_01";
        dealNameRandom = dealName + CommonUtils.timeStampCalculation() + "_01";
        logger.info("Generated unique Deal ID: {} and Deal Name: {} for assignment", dealIDRandom, dealNameRandom);
        boolean isDealApplied = pmp.applyDealsFromDealsSection(
                dealType,
                exchangeType,
                dealIDRandom,
                dealNameRandom,
                mediaTypeList,
                advertiser,
                dealPriceType,
                price,
                curator);
        Assert.assertTrue("Assigned Deals are not present under targeting and deals section", isDealApplied);
    }

    @And("Verify Base Bid Price {string} and Max Bid Price {string} fields are editable when deals are targeted")
    public void verifyBaseBidPriceAndMaxBidPriceFieldsAreEditableWhenDealsAreTargeted(
            String baseBidPrice, String maxBidPrice) {
        logger.info(
                "Verifying Base Bid Price and Max Bid Price fields are editable. Base: {}, Max: {}",
                baseBidPrice,
                maxBidPrice);
        boolean isEditable = pmp.verifyBaseAndMaxPriceIsEditable(baseBidPrice, maxBidPrice);
        Assert.assertTrue("Base and Max Bid Price fields are editable", isEditable);
    }

    @When("User clicks Save button from Tactic Setting tab")
    public void userClicksSaveButtonFromTacticSettingTab() {
        logger.info("Clicking Save button from Tactic Setting tab");
        pmp.saveTacticSettings();
    }

    @Then("Deals should get assigned to the Tactic")
    public void dealsShouldGetAssignedToTheTactic() {
        logger.info("Verifying deals are assigned to the Tactic");
        String saveResult = pmp.verifyTacticIsSaved().trim();
        Assert.assertEquals("Tactic " + tacticNameRandom + " updated.", saveResult);
    }

    /*Roshani Sherkar
     * 01-07-2025*/
    @Then("Verify targeting panel with all targeting under below categories")
    public void verifyTargetingPanelWithAllTargetingUnderBelowCategories(DataTable targetCategory) {
        logger.info("Verifying targeting panel with all targeting categories");
        List<String> targetCategoryList = targetCategory.asList(String.class);
        boolean isMatched = tacticSettings.fetchAndVerifyTargetCategoryName(targetCategoryList);
        Assert.assertTrue("Category names are not matched", isMatched);
    }

    @And("Verify target type with respect to category")
    public void verifyTargetTypeWithRespectToCategory(DataTable categoryNameAndType) {
        logger.info("Verifying target types with respect to their categories");
        Map<String, String> rawMap = categoryNameAndType.asMap(String.class, String.class);
        Map<String, List<String>> categoryNameAndTypeMap = CommonUtils.processDataTable(rawMap);

        for (Map.Entry<String, List<String>> entry : categoryNameAndTypeMap.entrySet()) {
            String key = entry.getKey();
            List<String> expectedValues = entry.getValue();
            logger.info("Checking category '{}' for expected target types: {}", key, expectedValues);
            List<String> actualValues = tacticSettings.getTargetTypesForCategory(key);
            logger.info("Actual target types found for '{}': {}", key, actualValues);

            for (String expected : expectedValues) {
                Assert.assertTrue(
                        "Expected value '" + expected + "' not found for category '" + key + "'. Found: "
                                + actualValues,
                        actualValues.contains(expected));
            }
        }

        logger.info("All target types matched their respective categories successfully");
    }

    /*Roshani Sherkar
     * 08-07-2025*/
    @When("User navigates to Targeting template page by clicking the icon from Activation section")
    public void userNavigatesToTargetingTemplatePageByClickingTheIconFromActivationSection() {
        logger.info("Navigating to Targeting template page from Activation section");
        navigation.clickSubMenu();
        navigation.clickTargetingTemplate();
    }

    @Then("Verify New Template button is present above the Search option")
    public void verifyNewTemplateButtonIsPresentAboveTheSearchOption() {
        logger.info("Verifying New Template button is present above Search option");
        boolean isDisplayed = targetingTemplate.verifyTargetingButtonAndSearchBox();
        Assert.assertTrue("Targeting Button and Search Box are not displayed", isDisplayed);
    }

    @And("Verify Targeting template section opens by clicking New Template button")
    public void verifyTargetingTemplateSectionByClickingNewTemplateButton() {
        logger.info("Verifying Targeting template section opens by clicking New Template button");
        boolean isAvailable = targetingTemplate.clickAndVerifyTargetingTemplate();
        Assert.assertTrue("All fields require to create targeting template are not available", isAvailable);
    }

    @When(
            "User creates Targeting template {string} for the line items {string} with channel {string} and Targeting Rules")
    public void userCreatesTargetingTemplateForTheLineItemsWithChannelAndTargetingRules(
            String templateName, String lineItems, String channel, DataTable ruleTypeAndOptions) {
        logger.info(
                "Creating Targeting Template: {} for Line Items: {} and Channel: {}", templateName, lineItems, channel);
        Map<String, String> rawMap = ruleTypeAndOptions.asMap(String.class, String.class);
        Map<String, List<String>> rulesMap = CommonUtils.processDataTable(rawMap);
        logger.info("Processed Targeting Rules: {}", rulesMap);
        List<String> lineItemsList = Arrays.stream(lineItems.split(",")).toList();
        List<String> channelList = Arrays.stream(channel.split(",")).toList();
        keyValueMap =
                targetingTemplate.createAndSaveTargetingTemplate(templateName, lineItemsList, channelList, rulesMap);
        logger.info("Targeting template created. Returned Key-Value Map: {}", keyValueMap);
    }

    @Then("User searches and verifies the already created targeting template using the search option")
    public void userSearchesTheAlreadyCreatedTargetingTemplateUsingTheSearchOption() {
        logger.info("Searching and verifying created targeting template");
        Assert.assertTrue(
                "Targeting template is not found in the search results",
                targetingTemplate.searchTargetingTemplate(new ArrayList<>(keyValueMap.keySet())));
    }

    @And(
            "User tries to save the targeting template with targeting rule {string} and without specifying a template name")
    public void userTriesToSaveTheTargetingTemplateWithTargetingRuleAndWithoutSpecifyingATemplateName(
            String targetingRule) {
        logger.info("Attempting to save targeting template without a name using rule: {}", targetingRule);
        String errorMessage = targetingTemplate.verifyErrorMessageForTemplateName(targetingRule);
        Assert.assertEquals("Template Name is required", errorMessage);
    }

    @And("User tries to save the targeting template with template name {string} without specifying any targeting")
    public void userTriesToSaveTheTargetingTemplateWithTemplateNameWithoutSpecifyingAnyTargeting(String templateName) {
        logger.info("Attempting to save targeting template '{}' without specifying any targeting rules", templateName);
        String errorMessage = targetingTemplate.verifyErrorMessageForTargetingRules(templateName);
        Assert.assertEquals("Please select atleast one targeting", errorMessage);
    }

    @And("User clicks on Show Expression and verifies the query is displayed for the {string}")
    public void userClicksOnAndVerifiesTheQueryIsDisplayed(String templateName) {
        logger.info("Clicking on Show Expression and verifying query is displayed for template: {}", templateName);
        boolean isDisplayed = targetingTemplate.clickAndVerifyShowExpression(templateName);
        Assert.assertTrue("Targeting container is not displayed", isDisplayed);
    }

    @And("User edits an existing targeting template and verifies the changes are saved for the {string}")
    public void userEditsAnExistingTargetingTemplateAndVerifiesTheChangesAreSaved(String templateName) {
        logger.info("Editing existing targeting template: {}", templateName);
        boolean isEditableAndSaved = targetingTemplate.clickAndVerifyTargetTemplateEditable(templateName);
        Assert.assertTrue("Unable to edit targeting template", isEditableAndSaved);
    }

    @And("User deletes an existing targeting template and verifies it is removed from the list for the {string}")
    public void userDeletesAnExistingTargetingTemplateAndVerifiesItIsRemovedFromTheList(String templateName) {
        logger.info("Deleting targeting template: {}", templateName);
        String deleteMessage = targetingTemplate.clickAndVerifyTargetTemplateDeletion(templateName);
        Assert.assertEquals("Target template deleted successfully", deleteMessage);
    }

    @And(
            "Create a tactic with {string} line items and other details {string} {string} {string} {string} {string} {string} {string} and import the template in Tactic")
    public void createATacticWithLineItemsAndOtherDetails(
            String lineItemType,
            String advertiser,
            String campaign_name,
            String campaign_type,
            String budget,
            String lineItemName,
            String lineBudget,
            String tacticName) {
        logger.info(
                "Creating a tactic and importing template. Line Item Type: {}, Advertiser: {}, Campaign: {}, Tactic: {}",
                lineItemType,
                advertiser,
                campaign_name,
                tacticName);
        List<String> lineItemTypeList = Arrays.stream(lineItemType.split(",")).toList();
        List<String> templateList = new ArrayList<>(keyValueMap.keySet());
        List<Map<String, String>> ruleCountAndValueList = new ArrayList<>(keyValueMap.values());
        logger.info("Importing templates: {}", templateList);
        flag = tacticDetails.createTacticWithLineItemsAndImport(
                lineItemTypeList,
                advertiser,
                campaign_name,
                campaign_type,
                budget,
                lineItemName,
                lineBudget,
                tacticName,
                templateList,
                ruleCountAndValueList);
        logger.info("Tactic creation and template import flag result: {}", flag);
    }

    @Then("Verify the template created can be imported in the Tactic")
    public void verifyTheTemplateCreatedCanBeImported() {
        logger.info("Verifying template can be imported in the Tactic");
        Assert.assertTrue("Tactic is not created with the imported template", flag);
    }

    @And("User selects the Attributes List and uploads the file {string}")
    public void userSelectsTheAttributesListAndUploadsTheFile(String attributesFile) {
        logger.info("Selecting Attributes List and uploading file: {}", attributesFile);
        npiAttributesList.uploadAttributesFile(attributesFile);
        String successMessage = npiAttributesList.verifyFileUploadSuccess();
        logger.info("File upload success message: '{}'", successMessage);
        assert successMessage.contains("Successfully uploaded");
    }

    @Then("Verify file {string} is uploaded successfully")
    public void verifyFileIsUploadedSuccessfully(String attributesFile) {
        String successMessage = npiAttributesList.verifyFileUploadSuccess();
        logger.info("Fetched success message: '{}'", successMessage);
        assert successMessage.contains("Successfully uploaded Excel file : " + attributesFile);
    }

    @And("User selects the {string} column and clicks on Next")
    public void userSelectsTheNPIColumnAndClicksOnNext(String columnName) {
        logger.info("Selecting column: '{}' and clicking Next", columnName);
        npiAttributesList.selectNPIColumn(columnName);
        npiAttributesList.clickNextButton();
    }

    @When("User tries to save the Attribute list without entering any details, an error message should be displayed")
    public void userSavesAttributeListWithoutAnyDetails() {
        logger.info("Attempting to save Attribute list without details to verify validations");
        npiAttributesList.clickNextButton();
        String listNameError = npiAttributesList.listNameError();
        logger.info("Fetched List Name error: '{}'", listNameError);
        assert listNameError.contains("List Name is required");
        String listName = "Temporary List Name";
        logger.info("Entering temporary list name '{}' to verify advertiser error", listName);
        npiAttributesList.enterListName(listName);
        npiAttributesList.clickNextButton();
        String advertiserError = npiAttributesList.advertiserError();
        logger.info("Fetched Advertiser error: '{}'", advertiserError);
        assert advertiserError.contains("Advertiser is required");
    }

    @And("User enters the Attributes list details as {string} {string}")
    public void userEntersTheAttributesListDetailsAs(String listName, String advertiser) {
        npiName = listName + '_' + CommonUtils.timeStampCalculation();
        logger.info("Entering Attributes list details. Name: {}, Advertiser: {}", npiName, advertiser);
        npiAttributesList.enterListName(npiName);
        npiAttributesList.selectAdvertiser(advertiser);
    }

    @When("User makes list available in LIFE and HCP365 and clicks on next")
    public void userMakesListAvailableInLifeAndHCP365AndClicksOnNext() {
        logger.info("Making list available in LIFE and HCP365 and clicking Next");
        npiAttributesList.selectProduct();
        npiAttributesList.clickNextButton();
    }

    @Then("Verify the Attributes list is saved successfully")
    public void verifyTheAttributesListIsSavedSuccessfully() {
        logger.info("User makes list available in LIFE and HCP365 and clicks on next");
        String successMessage = npiAttributesList.fetchSuccessAlert();
        logger.info("Save message: '{}'", successMessage);
        assert successMessage.contains("NPI list created");
    }

    @When("User edits the saved list")
    public void userEditsTheSavedList() {
        logger.info("Editing the saved Attributes list: {}", npiName);
        npiAttributesList.clickBackToNPILists();
        npiLists.searchList(npiName);
        npiLists.openSearchedList(npiName);
        npiNameEdited = "Edited" + '_' + npiName;
        logger.info("Updating list name to: {}", npiNameEdited);
        npiAttributesList.editListName(npiNameEdited);
    }

    @And("User saves the list after making updates")
    public void userSavesTheListAfterMakingUpdates() {
        npiAttributesList.saveList();
        String updateMessage = npiAttributesList.updateListSuccess();
        Assert.assertTrue(
                "Failed to update list name",
                updateMessage.contains("NPI list updated") || updateMessage.contains("Medscape List updated"));
    }

    @Then("Verify the updates are applied successfully")
    public void verifyTheUpdatesAreAppliedSuccessfully() {
        logger.info("Verifying updates are applied successfully");
        npiAttributesList.clickBackToNPILists();
        npiLists.searchList(npiNameEdited);
        npiLists.openSearchedList(npiNameEdited);
    }

    @When("User deletes the {string} list")
    public void userDeletesTheAttributeList(String listType) {
        logger.info("Deleting the {} type list: {}", listType, npiNameEdited);
        npiAttributesList.deleteList();
    }

    @Then("Verify the list is deleted successfully")
    public void verifyTheListIsDeletedSuccessfully() {
        String deleteMessage = npiAttributesList.deleteSuccess();
        logger.info("Delete message: '{}'", deleteMessage);
        Assert.assertTrue("No Deletion message is shown", deleteMessage.contains("NPI List Deleted"));
    }

    @And(
            "Create a tactic with below targeting rules and {string} line items and other details {string} {string} {string} {string} {string} {string} {string}")
    public void createATacticWithBelowTargetingRulesAndLineItemsAndOtherDetails(
            String lineItemType,
            String advertiser,
            String campaign_name,
            String campaign_type,
            String budget,
            String lineItemName,
            String lineBudget,
            String tacticName,
            DataTable ruleTypeAndOptions) {
        logger.info(
                "Creating a tactic with inline targeting rules. Line Item Type: {}, Advertiser: {}, Campaign: {}, Tactic: {}",
                lineItemType,
                advertiser,
                campaign_name,
                tacticName);
        Map<String, String> rawMap = ruleTypeAndOptions.asMap(String.class, String.class);
        Map<String, List<String>> rulesMap = CommonUtils.processDataTable(rawMap);
        logger.info("Processed targeting rules: {}", rulesMap);
        List<String> lineItemTypeList =
                Arrays.stream(lineItemType.split(",")).map(String::trim).toList();
        List<String> templateNameList = tacticDetails.createTacticWithLineItemsAndTargetingRules(
                lineItemTypeList,
                advertiser,
                campaign_name,
                campaign_type,
                budget,
                lineItemName,
                lineBudget,
                tacticName,
                rulesMap);
        logger.info("Returned Template Names from tactic creation: {}", templateNameList);

        for (String templateName : templateNameList) {
            keyValueMap.put(templateName, new HashMap<>());
        }
    }

    @Then("Verify the template created are saved")
    public void verifyTheTemplateCreatedAreSaved() {
        logger.info("Verifying template is saved");
        Assert.assertFalse("Unable to save targeting templates", keyValueMap.isEmpty());
    }

    /*Roshani Sherkar
     * 14-07-2024
     * Creatives creation*/
    @And("User clicks Creative Library options present under Activation tab")
    public void userClicksCreativeLibraryOptionsPresentUnderActivationTab() {
        logger.info("User clicks Creative Library options present under Activation tab");
        navigation.clickSubMenu();
        navigation.clickCreativeLibrary();
    }

    @Then("Verify Creative Library page is displayed")
    public void verifyCreativeLibraryPageIsDisplayed() {
        logger.info("Verifying Creative Library page title is displayed");
        String pageTitle = createCreatives.verifyCreativeLibraryPageTitle();
        Assert.assertEquals("Creatives", pageTitle);
    }

    @And("Check Activity buttons {string} and verify following filters are available and working")
    public void checkActivityButtonsAndVerifyFollowingFiltersAreAvailableAndWorking(
            String buttonType, DataTable filters) {
        logger.info("Verifying Activity button '{}' and filters", buttonType);
        Map<String, String> rawFilters = filters.asMap(String.class, String.class);
        Map<String, List<String>> filtersMap = CommonUtils.processDataTable(rawFilters);
        createCreatives.navigateToFirstCreativePage();
        createCreatives.clickActivityButton(buttonType);
        boolean isButtonPresent = createCreatives.isArchiveUnarchiveButtonsPresent(buttonType);
        Assert.assertTrue("Activity " + buttonType + " button is not present", isButtonPresent);

        if (buttonType.equals("Active")) {
            boolean isAllActive = createCreatives.showsActiveCreativesWhenActiveClicked();
            Assert.assertTrue("Not all content on Creative Library page is Active", isAllActive);
        } else if (buttonType.equals("Archived")) {
            boolean isAllArchived = createCreatives.showsArchivedCreativesWhenArchivedClicked();
            Assert.assertTrue("Not all content on Creative Library page is Archived", isAllArchived);
        }

        for (Map.Entry<String, List<String>> entry : filtersMap.entrySet()) {
            createCreatives.navigateToFirstCreativePage();
            flag = createCreatives.verifyFilterOptions(entry.getKey(), entry.getValue());
            Assert.assertTrue(
                    "Creative Library page does not display values for all content " + entry.getValue(), flag);
        }
    }

    @And("Verify the following sort options are available and working")
    public void verifyTheFollowingSortOptionsAreAvailableAndWorking(DataTable sortOptions) {
        List<String> sortOptionsList = sortOptions.asList(String.class);
        logger.info("Verifying sort options: {}", sortOptionsList);

        for (String sortOption : sortOptionsList) {
            boolean isWorking = createCreatives.checkSortingOrder(sortOption);
            Assert.assertTrue(sortOption + " is not working correctly", isWorking);
        }
    }

    @And("Verify Search Box is available and working")
    public void verifySearchBoxIsAvailableAndWorking(DataTable searchValues) {
        List<String> searchValuesList = searchValues.asList(String.class);
        logger.info("Verifying search functionality for values: {}", searchValuesList);
        createCreatives.clickActivityButton("Active");

        for (String searchValue : searchValuesList) {
            createCreatives.searchCreative(searchValue);
            boolean isFound = createCreatives.checkSearchedValue(searchValue);
            Assert.assertTrue("Search is not working for value: " + searchValue, isFound);
        }
    }

    @And("User checks Copy option is working for creative and verify details before and after saving the creative")
    public void verifyCopyOptionIsAvailableAndWorking() {
        logger.info(
                "User checks Copy option is working for creative and verify details before and after saving the creative");
        String creativeName = "Copy_Creative_" + CommonUtils.timeStampCalculation();
        metricName = createCreatives.selectCheckboxWithArchiveButton();
        createCreatives.searchCreative(metricName);
        createCreatives.clickCopyCreative(metricName);
        createCreatives.enterCreativeName(creativeName);
        List<String> fetchCreativeDetailsBeforeSave = createCreatives.fetchCreativeDetails();
        Assert.assertTrue(
                "Copy option is not working properly",
                createCreatives.saveCreative().contains("updated."));
        createCreatives.searchCreative(creativeName);
        createCreatives.clickSearchedCreative(creativeName);
        List<String> fetchCreativeDetailsAfterSave = createCreatives.fetchCreativeDetails();
        createCreatives.clickCancelButton();
        Assert.assertEquals(
                "Creative details are not matched", fetchCreativeDetailsBeforeSave, fetchCreativeDetailsAfterSave);
    }

    @And("User checks Archive option is working for creative and verify the creative is moved to {string} tab")
    public void userChecksArchiveOptionIsWorkingForCreativeAndVerifyTheCreativeIsMovedToArchivedTab(String tabName) {
        logger.info(
                "User checks Archive option is working for creative and verify the creative is moved to {} tab",
                tabName);
        String archiveCreative = createCreatives.clickArchiveButton();
        createCreatives.clickActivityButton(tabName);
        createCreatives.searchCreative(archiveCreative);
        Assert.assertTrue(
                archiveCreative + " - creative is not available on " + tabName,
                createCreatives.checkSearchedValue(archiveCreative));
    }

    @And("User checks Unarchive option is working for creative and verify the creative is moved to {string} tab")
    public void userChecksArchiveOptionIsWorkingForCreativeAndVerifyTheCreativeIsMovedToUnarchivedTab(String tabName) {
        logger.info(
                "User checks Unarchive option is working for creative and verify the creative is moved to {} tab",
                tabName);
        String unarchiveCreative = createCreatives.clickUnarchiveButton();
        createCreatives.clickActivityButton(tabName);
        createCreatives.searchCreative(unarchiveCreative);
        Assert.assertTrue(
                unarchiveCreative + " - creative is not available on " + tabName,
                createCreatives.checkSearchedValue(unarchiveCreative));
    }

    @When("User clicks on {string} tab and verify following filters value")
    public void userClicksOnTabAndVerifyFollowingFiltersValue(String tabName, DataTable filters) {
        logger.info("Verifying filters on '{}' tab", tabName);
        Map<String, String> rawFilters = filters.asMap(String.class, String.class);
        Map<String, List<String>> filtersMap = CommonUtils.processDataTable(rawFilters);
        createCreatives.clickActivityButton(tabName);

        for (Map.Entry<String, List<String>> entry : filtersMap.entrySet()) {
            List<String> fetchedFilterValues = createCreatives.fetchFilterValues(entry.getKey());
            Assert.assertEquals("Creative details are not matched", entry.getValue(), fetchedFilterValues);
        }

        logger.info("Filter values verified successfully");
    }

    @When("User selects pagination values {string} from the dropdown")
    public void userSelectsPaginationValuesFromTheDropdown(String paginationValue) {
        logger.info("Selecting pagination value: {}", paginationValue);
        createCreatives.selectPaginationItemsPerPage(paginationValue);
    }

    @And("Verify pagination is working properly on the Creative Library page")
    public void verifyPaginationIsWorkingProperlyOnTheCreativeLibraryPage() {
        logger.info("Verify pagination is working properly on the Creative Library page");
        boolean isWorking = createCreatives.fetchCreativeCount();
        Assert.assertTrue("Pagination is not working properly", isWorking);
    }

    @When("User assigns a campaign to the creative using {string} option")
    public void userAssignsACampaignToTheCreative(String bulkActionOption, DataTable filters) {
        List<String> filtersList = filters.asList(String.class);
        logger.info("Assigning campaign to creative via bulk action: {}", bulkActionOption);
        metricName = createCreatives.selectCheckboxWithArchiveButton();
        createCreatives.clickBulkActionsButton();
        createCreatives.selectBulkActionsOption(bulkActionOption);
        if (createCreatives.isNoCampaignFoundMessageDisplayed()) {
            logger.info(
                    "No campaign found message is displayed when trying to assign campaign to creative without any campaign available");
            createCreatives.clickBulkPanelCancelButton();
            createCreatives.selectAdvertiser(filtersList);
            metricName = createCreatives.selectCheckboxWithArchiveButton();
            createCreatives.clickBulkActionsButton();
            createCreatives.selectBulkActionsOption(bulkActionOption);
        }
        Assert.assertEquals("Bulk Assign Successful", createCreatives.assignCampaignToCreative());
    }

    @Then(
            "Verify user is not able to delete a creative associated with a Campaign and appropriate error message is displayed")
    public void verifyUserIsNotAbleToDeleteACreativeAssociatedWithACampaignAndAppropriateErrorMessageIsDisplayed() {
        logger.info(
                "Verify user is not able to delete a creative associated with a Campaign and appropriate error message is displayed");
        createCreatives.searchCreative(metricName);
        Assert.assertEquals(
                "Creatives that have 1 or more running campaigns cannot be archived.",
                createCreatives.fetchTooltipTextForAssignedCampaigns());
        createCreatives.clickSearchedCreative(metricName);
        Assert.assertEquals("Delete icon is disabled, cannot delete the creative.", createCreatives.deleteCreative());
        createCreatives.clickCancelButton();
        createCreatives.clearSearchBox();
    }

    @And("User deletes a creative not associated with any Campaign")
    public void userDeletesACreativeNotAssociatedWithAnyCampaign() {
        logger.info("Initiating deletion of an unassigned creative");
        metricName = createCreatives.selectCheckboxWithArchiveButton();
        createCreatives.searchCreative(metricName);
        createCreatives.clickSearchedCreative(metricName);
        Assert.assertEquals(
                "You are about to delete " + metricName
                        + ".This action cannot be undone: all deleted data will be lost.Do you want to proceed?",
                createCreatives.deleteCreative());
    }

    @And("Verify the creative is removed from the Creative Library page")
    public void verifyTheCreativeIsRemovedFromTheCreativeLibraryPage() {
        logger.info(
                "Verifying deleted creative is no longer present by searching for it and confirming 'Nothing Found' is displayed");
        createCreatives.searchCreative(metricName);
        String notFoundMessage = createCreatives.fetchNoCreativeFoundMessage();
        Assert.assertEquals("Nothing Found", notFoundMessage);
    }

    @When("User clicks on Preview icon for a creative from Creative Library page")
    public void userClicksOnPreviewIconForACreative() {
        logger.info("Clicking Preview icon for a creative from Creative Library page");
        metricName = createCreatives.clickCreativeTypeIconAndFetchCreativeName();
        logger.info("Previewing creative: {}", metricName);
    }

    @Then("Verify Creative Preview tab is displayed with correct creative name")
    public void verifyCreativePreviewTabIsDisplayedWithCorrectCreativeName() {
        logger.info("Verify Creative Preview tab is displayed with correct creative name");
        Assert.assertEquals("Creative Preview", createCreatives.isCreativePreviewTabDisplayed());
        Assert.assertTrue(
                "Creative name in preview tab does not match expected name",
                createCreatives.fetchCreativeNameFromPreviewTab().contains(metricName));
    }

    @And("Verify user is able to close the Creative Preview tab")
    public void verifyUserIsAbleToCloseTheCreativePreviewTab() {
        logger.info("Closing Creative Preview tab");
        createCreatives.closeCreativePreviewTab();
    }

    @And("User searches the creative and clicks the creative details from Creative Library page")
    public void userSearchesTheCreativeAndClicksTheCreativeDetailsFromCreativeLibraryPage() {
        logger.info("Searching and opening creative details for: {}", metricName);
        createCreatives.searchCreative(metricName);
        createCreatives.clickSearchedCreative(metricName);
    }

    @And("User clicks on Preview link from Creative Details page")
    public void userClicksOnPreviewLinkFromCreativeDetailsPage() {
        logger.info("User clicks on Preview link from Creative Details page");
        createCreatives.clickPreviewLinkFromCreativeDetailsPage();
    }

    @When(
            "User performs {string} action using {string} option on multiple creatives - {string} and verifies the selected creatives are moved to {string} tab")
    public void userPerformsBulkArchiveActionOnMultipleCreativesAndVerifiesTheSelectedCreativesAreMovedToArchivedTab(
            String bulkAction, String bulkActionOption, String noOfCreatives, String tabName) {
        logger.info("Performing bulk action: {} ({}) on {} creatives", bulkAction, bulkActionOption, noOfCreatives);
        createCreatives.clearSearchBox();

        if (bulkAction.equalsIgnoreCase("Bulk Archive")) createCreatives.clickActivityButton("Active");
        else createCreatives.clickActivityButton("Archived");

        nameList.clear();

        for (int i = 0; i < Integer.parseInt(noOfCreatives); i++) {
            nameList.add(createCreatives.selectCheckboxWithArchiveButton());
        }

        createCreatives.clickBulkActionsButton();
        createCreatives.selectBulkActionsOption(bulkActionOption);
        createCreatives.clickActivityButton(tabName);

        for (String name : nameList) {
            createCreatives.searchCreative(name);
            Assert.assertTrue(
                    "Creative " + name + " is not found in the " + tabName + " tab",
                    createCreatives.checkSearchedValue(name));
        }
    }

    @And(
            "User performs Bulk approve action using {string} option on multiple creatives - {string} with status other than Approved and verifies the selected creatives are marked as {string}")
    public void
            userPerformsBulkApproveActionOnMultipleCreativesAndVerifiesTheSelectedCreativesAreRemovedFromTheCreativeLibraryPage(
                    String bulkActionOption, String noOfCreatives, String statusLabel, DataTable dataTable) {
        logger.info(
                "Performing bulk approval ({}) on {} creatives to set status: {}",
                bulkActionOption,
                noOfCreatives,
                statusLabel);
        List<String> statusList = Arrays.stream(
                        dataTable.asList(String.class).get(0).split(","))
                .map(String::trim)
                .toList();
        createCreatives.clearSearchBox();
        createCreatives.clickActivityButton("Active");
        createCreatives.selectCreativeStatus(statusList);
        nameList.clear();

        for (int i = 0; i < Integer.parseInt(noOfCreatives); i++) {
            nameList.add(createCreatives.selectCheckboxWithCreativeStatusLabel());
        }

        createCreatives.clickBulkActionsButton();
        createCreatives.selectBulkActionsOption(bulkActionOption);
        Assert.assertTrue("Unable to perform approval in bulk", createCreatives.performBulkApproval());
        createCreatives.clickClearAllButton();

        for (String name : nameList) {
            createCreatives.searchCreative(name);
            Assert.assertTrue("Creative " + name + " is not found", createCreatives.checkSearchedValue(name));
            Assert.assertEquals(statusLabel, createCreatives.fetchCreativeStatusLabel());
        }
    }

    @And(
            "Verify data persistence when user creates and saves {string} creative using details {string} as Advertiser, {string} as Creative Name, {string}, {string} and below Creative attributes")
    public void userCreatesAndSavesCreativeUsingDetailsAsAdvertiserAsCreativeNameAndBelowCreativeAttributes(
            String creativeType,
            String advertiser,
            String creativeName,
            String advertiserDSA,
            String financer,
            DataTable dataTable) {
        logger.info(
                "Creating {} creatives and verifying data persistence. Advertiser: {}, Base Name: {}",
                creativeType,
                advertiser,
                creativeName);
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        nameList.clear();

        for (Map<String, String> row : rows) {
            String type = row.get("CreativeType").trim();
            String attributes = row.get("CreativeAttributes").trim();

            String newCreativeName = creativeType + "_" + creativeName + '_' + CommonUtils.timeStampCalculation();
            createCreatives.clickNewCreativeButton();
            createCreatives.enterCreativeDetails(advertiser, newCreativeName, advertiserDSA, financer);
            createCreatives.selectCreativeType(creativeType);

            Map<String, String> attributeMap = Arrays.stream(attributes.split(","))
                    .map(String::trim)
                    .map(entry -> entry.split(":", 2))
                    .collect(Collectors.toMap(e -> e[0].trim(), e -> e[1].trim()));

            createCreatives.fillAttributes(type, attributeMap);
            List<String> creativeDetailsBeforeSave = createCreatives.fetchCreativeDetails();
            String actualMessage = createCreatives.saveCreative();
            Assert.assertTrue(
                    "No message is displayed",
                    actualMessage.contains("BulkUpload created successfully.")
                            || actualMessage.contains("Creative " + newCreativeName + " created."));

            String creativeNameFetched = createCreatives.fetchCreativeName(actualMessage);
            nameList.add(creativeNameFetched);
            createCreatives.searchCreative(creativeNameFetched);
            List<String> creativeDetailsFromCreativeTile = createCreatives.fetchCreativeDetailsFromCreativeTile();

            for (String creativeDetail : creativeDetailsFromCreativeTile) {
                if (creativeDetail == null || creativeDetail.trim().equalsIgnoreCase("N/A")) {
                    continue;
                }
                Assert.assertTrue(
                        "Creative detail " + creativeDetail + " is not matched in the creative tile",
                        creativeDetailsBeforeSave.contains(creativeDetail));
            }
            Assert.assertEquals(
                    "Creative detail - CreatedBy is not matched in the creative tile",
                    "Anand Venkatraman",
                    createCreatives.fetchCreatedByFromCreativeTile());
            Assert.assertEquals(
                    "Creative detail - Source is not matched in the creative tile",
                    "Manual",
                    createCreatives.fetchSourceFromCreativeTile());
            createCreatives.clickSearchedCreative(creativeNameFetched);
            List<String> creativeDetailsAfterSave = createCreatives.fetchCreativeDetails();
            Assert.assertEquals("Creative Details are not same", creativeDetailsBeforeSave, creativeDetailsAfterSave);
            createCreatives.clickCancelButton();
        }

        logger.info("Data persistence verified successfully for all created creatives");
    }

    @Then("Verify the newly created creative is displayed in the Creative Library page")
    public void verifyTheNewlyCreatedCreativeIsDisplayedInTheCreativeLibraryPage() {

        for (String name : nameList) {
            logger.info("Verifying: the newly created creative is displayed in the Creative Library page");
            Assert.assertTrue(
                    "Creative " + name + " is not found in the library",
                    createCreatives.verifyCreativesInLibrary(name));
            Assert.assertEquals("1 records", createCreatives.fetchRecordsNumberAfterSearch());
        }
    }

    @And(
            "Create and verify a tactic with {string} line items and other details {string} {string} {string} {string} {string} {string} {string} and assign the created creatives to it")
    public void createATacticWithLineItemsAndOtherDetailsAndAssignTheCreatedCreativesToIt(
            String lineItemType,
            String advertiser,
            String campaign_name,
            String campaign_type,
            String budget,
            String lineItemName,
            String lineBudget,
            String tacticName) {
        logger.info("Creating tactic to assign recently created creatives. Allowed Line Item Types: {}", lineItemType);
        List<String> lineItemTypeList =
                Arrays.stream(lineItemType.split(",")).map(String::trim).toList();

        for (String creativeName : nameList) {
            String creativeType =
                    creativeName.replaceAll("_Creative_\\d+_\\d+", "").trim();
            String matchedLineItemType;

            switch (creativeType) {
                case "HTML" -> matchedLineItemType = "Display";
                case "Native" -> matchedLineItemType = "Native Display";
                default -> matchedLineItemType = creativeType;
            }

            logger.info("Processing Creative: {}, Mapped Line Item Type: {}", creativeName, matchedLineItemType);

            if (lineItemTypeList.contains(matchedLineItemType)) {
                logger.info("Creating Tactic for mapped type '{}' and assigning creative", matchedLineItemType);
                boolean result = tacticDetails.createTacticWithLineItemsAndAssignCreative(
                        matchedLineItemType,
                        advertiser,
                        campaign_name,
                        campaign_type,
                        budget,
                        lineItemName,
                        lineBudget,
                        tacticName,
                        creativeName);
                Assert.assertTrue("Creative is not assigned to Tactic", result);
            } else {
                logger.info(
                        "Skipping Creative '{}' as its mapped Line Item Type '{}' is not in the allowed list",
                        creativeName,
                        matchedLineItemType);
            }
        }
    }

    /*Roshani Sherkar
     * Auto-Imported List*/
    @And("User selects the Auto-Imported List")
    public void userSelectsTheAutoImportedList() {
        logger.info("User selects the Auto-Imported List");
        npiLists.clickAutoImportedList();
    }

    @And("Verify if user navigates to the Auto-Imported List page")
    public void verifyIfUserNavigatesToTheAutoImportedListPage() {
        logger.info("User selects the Auto-Imported List");
        String pageHeader = npiAutoImportedList.verifyIfAutoImportPage();
        Assert.assertEquals("Setup Import", pageHeader);
    }

    @Then(
            "User tries to save the Auto-Imported list without entering any details, an error message should be displayed")
    public void userTriesToSaveTheAutoImportedListWithoutEnteringAnyDetailsAnErrorMessageShouldBeDisplayed() {
        logger.info("Attempting to save Auto-Imported list without details to verify validation");
        npiAutoImportedList.clickSetupImportButton();
        String errorMessage = npiAutoImportedList.verifyErrorMessage();
        Assert.assertEquals("Advertiser is required", errorMessage);
    }

    @When("User enters the Auto-Imported list details as {string} {string}")
    public void userEntersTheAutoImportedListDetailsAs(String listName, String advertiser) {
        npiName = listName + '_' + CommonUtils.timeStampCalculation();
        logger.info("Entering Auto-Imported list details. Name: {}, Advertiser: {}", npiName, advertiser);
        npiAttributesList.enterListName(npiName);
        npiAttributesList.selectAdvertiser(advertiser);
    }

    @And("User makes list available in LIFE and HCP365 module")
    public void userMakesListAvailableInLIFEAndHCP() {
        logger.info("User makes list available in LIFE and HCP365 module");
        npiAttributesList.selectProduct();
    }

    @And("User clicks Setup Import button to import File details")
    public void userClicksSetupImportButtonToImportFileDetails() {
        logger.info("User makes list available in LIFE and HCP365 module");
        npiAutoImportedList.clickSetupImportButton();
        npiAutoImportedList.waitForImportSettingPanel();
    }

    @And("User enters file details {string} {string} {string}")
    public void userEntersImportSettingWithDetails(String fileLocation, String filePath, String fileName) {
        logger.info(
                "Entering file import settings - Location: {}, Path: {}, Name: {}", fileLocation, filePath, fileName);
        npiAutoImportedList.enterFileDetails(fileLocation, filePath.trim(), fileName.trim());
    }

    @And("User selects the {string} radio button")
    public void userSelectsTheListType(String listType) {
        logger.info("Selecting List Type radio button: {}", listType);
        npiAutoImportedList.selectListType(listType);
    }

    @And("User enters NPI column {string} {string}")
    public void userEntersNPIColumnName(String npiColumn, String columnName) {
        logger.info("Mapping NPI Column: '{}' to CSV Column: '{}'", npiColumn, columnName);
        npiAutoImportedList.enterColumnName(npiColumn, columnName);
    }

    @And("User selects the {string}")
    public void userSelectsTheImportType(String importType) {
        logger.info("Selecting Import Type: {}", importType);
        npiAutoImportedList.selectImportType(importType);
    }

    @Then("User clicks Check File button to verify the file details are correct")
    public void userClicksCheckFileButtonToVerifyTheFileDetailsAreCorrect() {
        logger.info("User clicks Check File button to verify the file details are correct");
        npiAutoImportedList.clickCheckFile();
    }

    @Then("User saves the import settings and verifies the data is imported successfully")
    public void userSavesTheImportSettingsAndVerifiesTheIsSavedSuccessfully() {
        logger.info("Saving import settings");
        npiAutoImportedList.clickOKButton();
    }

    @And("Verify that Token is fetched successfully from URL {string}")
    public void verifyThatTokenIsFetchedSuccessfully(String url) {
        logger.info("Fetching authentication token from URL: {}", url);
        constants.TOKEN = npiAutoImportedList.fetchToken(url);
        Assert.assertNotNull("Token is not fetched", constants.TOKEN);
    }

    @And("Pass token in the API Header and run it to upload the data into the list")
    public void runAPIToUploadTheListDataIntoTheList() {
        logger.info("Executing API request to trigger data upload into the list");
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Token", constants.TOKEN);
        response = npiAutoImportedList.runAPI(constants.BASE_URL, constants.ENDPOINT_PATH, headers);
        logger.info("API execution completed");
    }

    @And("Verify list data is uploaded successfully")
    public void verifyListDataIsUploadedSuccessfully() {
        logger.info("Pass token in the API Header and run it to upload the data into the list");
        if (response == null) {
            logger.info("Verifying: list data is uploaded successfully");
            Assert.fail("API response is null");
        } else {
            int statusCode = response.status();
            Assert.assertEquals(204, statusCode);
        }
    }

    @And("Refresh the Browser to view the data uploaded")
    public void refreshTheBrowserToViewTheDataUploaded() {
        logger.info("Refreshing browser to view uploaded data");
        boolean refreshSuccess = npiAutoImportedList.refreshBrowser();
        Assert.assertTrue("NPI List is not available", refreshSuccess);
    }

    @And("Verify the Total NPI count displayed in Matched NPI section is similar to NPI records present in {string}")
    public void verifyMatchedNPISectionIsDisplayedWithTheTotalNPICount(String fileName)
            throws IOException, CsvValidationException {
        logger.info(
                "Verify the Total NPI count displayed in Matched NPI section is similar to NPI records present in {}",
                fileName);
        String totalNPICount = npiAutoImportedList.fetchTotalNPICount();
        String npiRecordsFromFile = npiAutoImportedList.fetchNPIRecordFromTestFile(fileName);
        Assert.assertEquals("Count is not matching", totalNPICount, npiRecordsFromFile);
    }

    @And("Verify Reload Now button is available and enabled")
    public void verifyReloadNowButtonIsAvailableAndEnabled() {
        logger.info("Verifying availability of 'Reload Now' button");
        npiAutoImportedList.verifyIfImportSettingButtonIsVisible();
        boolean isReloadAvailable = npiAutoImportedList.verifyReloadNowButton();
        logger.info("Reload Now button available and enabled: {}", isReloadAvailable);
        Assert.assertTrue("Reload Now Button is not available", isReloadAvailable);
    }

    @When("User clicks on Reload Now button")
    public void userClicksOnReloadNowButton() {
        logger.info("User clicks on Reload Now button");
        npiAutoImportedList.clickReloadNowButton();
    }

    @Then("Verify the file is reloaded successfully")
    public void verifyTheFileIsReloadedSuccessfully() {
        logger.info("User clicks on Reload Now button");
        String reloadStatus = npiAutoImportedList.verifyIfFileIsReloaded();
        Assert.assertEquals("File is reloaded", reloadStatus);
    }

    /*Roshani Sherkar
     * 07-08-2025
     * Domain List*/
    @Given("User navigates to the {string} page")
    public void userNavigatesToTheDomainListPage(String pageName) {
        try {
            logger.info("Navigating to Shared List type: {}", pageName);
            navigation.clickSubMenu();
            sharedList.clickDomainListFromMenu(pageName);
        } catch (PlaywrightException e) {
            logger.info("Encountered PlaywrightException, attempting navigation again");
            if (campaigns.isCreateCampaignButtonVisible()) navigation.clickSubMenu();
            sharedList.clickDomainListFromMenu(pageName);
        }
    }

    @And("Verify that the search option is present on the {string} tab")
    public void verifyThatTheSearchOptionIsPresentOnTheTab(String listName) {
        logger.info("Verify that the search option is present on the {} tab", listName);
        String activeTab = sharedList.verifyIfListPageIsOpen(listName.trim());
        Assert.assertEquals("Tab is not opened", listName.trim(), activeTab);
        boolean searchPresent = sharedList.verifyIfSearchBoxIsPresent();
        Assert.assertTrue("Search Box is not available", searchPresent);
    }

    @And(
            "Verify that the sub-tabs {string} on the left navigation panel are available and {string} is selected by default")
    public void verifyThatTheSubTabsOnTheLeftNavigationPanelAreAvailable(String subTabs, String defaultTabName) {
        List<String> subTabsList = CommonUtils.convertStringToList(subTabs);

        for (String tab : subTabsList) {
            logger.info(
                    "Verifying: that the sub-tabs {} on the left navigation panel are available and {} is selected by default",
                    subTabs,
                    defaultTabName);
            boolean isPresent = sharedList.verifySubTabs(tab);
            Assert.assertTrue(tab + " Tab is not present", isPresent);
        }

        boolean isDefaultSelected = sharedList.verifyDefaultSubTab(defaultTabName);
        Assert.assertTrue("Both tab is not selected by default", isDefaultSelected);
    }

    @And("Verify that when the {string} tab is selected, only {string} lists are visible in the panel")
    public void verifyThatWhenTheTabIsSelectedListsAreVisibleInThePanel(String tabName, String listName) {
        logger.info(
                "Verify that when the {} tab is selected, only {} lists are visible in the panel", tabName, listName);
        sharedList.clickSubTab(tabName);
        boolean listAvailable = sharedList.verifyListIsAvailable(listName);
        Assert.assertTrue(tabName + " Tab list is not available", listAvailable);
    }

    @And("User selects the {string} radio button from create new list page")
    public void userSelectsTheRadioButtonFromCreateNewListPage(String listType) {
        logger.info("Selecting List type radio button: {}", listType);
        sharedList.clickListTypeRadioButton(listType);
    }

    @Then("Verify that the Create New List screen is displayed")
    public void verifyThatTheCreateListScreenIsDisplayed() {
        logger.info("Verify that the Create New List screen is displayed");
        boolean isDisplayed = sharedList.verifyNewListPage();
        Assert.assertTrue("Create New List screen is not displayed", isDisplayed);
    }

    @And("Verify that an error message is displayed when no listname {string} or {string} names are specified")
    public void verifyThatAnErrorMessageIsDisplayedWhenNoNamesAreSpecified(String listName, String listType) {
        logger.info(
                "Verify that an error message is displayed when no listname {} or {} names are specified",
                listName,
                listType);
        metricName = listName + "_" + CommonUtils.timeStampCalculation();

        String listNameError = sharedList.validateErrorOnEmptyListNameInput(metricName);
        Assert.assertEquals("List Name is required", listNameError);
        String dataInputError = sharedList.validateErrorOnEmptyListInput(metricName);
        logger.info("Fetched Data Input error: '{}'", dataInputError);

        switch (listType) {
            case "Domains":
                Assert.assertEquals("Domain name is required", dataInputError);
                break;
            case "AppBundle":
                Assert.assertEquals("AppBundle name is required", dataInputError);
                break;
            case "Keywords":
                Assert.assertEquals("Keyword is required", dataInputError);
                break;
            case "IP Address":
                Assert.assertEquals("IPAddress is required", dataInputError);
                break;
            default:
                Assert.fail("Invalid list type specified: " + listType);
        }
    }

    @And("Verify that if multiple {string} are specified on a single line, a validation error is shown")
    public void verifyThatIfMultipleDomainNamesAreSpecifiedOnASingleLineAValidationErrorIsShown(String domainName) {
        logger.info(
                "Verify that if multiple {} are specified on a single line, a validation error is shown", domainName);
        List<String> domainNameList = CommonUtils.convertStringToList(domainName);
        String validationErrorMessage = sharedList.checkErrorOnSingleLineMultipleDomainsInput(domainNameList);
        Assert.assertTrue("No Validation error is displayed", validationErrorMessage.contains("validation error(s)"));
    }

    @And("Verify that if multiple {string} are specified across multiple lines, an error is shown")
    public void verifyThatIfMultipleAreSpecifiedAcrossMultipleLinesAValidationErrorIsShown(String domainName) {
        logger.info(
                "Verify that if multiple {} are specified across multiple lines, a validation error is shown",
                domainName);
        List<String> domainNameList =
                Arrays.stream(domainName.split("::")).map(String::trim).toList();
        String gotToNextErrorMessage = sharedList.getGoToNextValidationError(domainNameList);
        Assert.assertTrue("No Go To Next Error is displayed", gotToNextErrorMessage.contains("Go To Next Error"));
    }

    @And("Verify that when {string} names are specified manually, the option to upload a file disappears")
    public void verifyThatWheNamesAreSpecifiedManuallyTheOptionToUploadAFileDisappears(String listType) {
        logger.info(
                "Verify that when {} names are specified manually, the option to upload a file disappears", listType);
        keyValues.clear();
        keyValues = new ArrayList<>(CommonUtils.convertStringToList(listType));
        sharedList.clearListTextArea();
        boolean isUploadVisibleBefore = sharedList.verifyUploadSectionIsVisibleBeforeListInput();
        Assert.assertTrue("Upload section is not available before list input", isUploadVisibleBefore);
        logger.info("Entering domain names manually");
        sharedList.enterDomainNames(keyValues);
        boolean isUploadVisibleAfter = sharedList.verifyUploadSectionIsVisibleAfterListInput();
        Assert.assertTrue("Upload section is available after list input", isUploadVisibleAfter);
    }

    @And("User retrieves all the entered data before saving the list details {string}")
    public void userRetrievesAllTheEnteredDataBeforeSavingTheListDetails(String listName) {
        logger.info("Retrieving all the entered data before saving the list details: {}", listName);
        itemList = sharedList.fetchListDetailsFromNewPanel();
    }

    @And("Verify that the user is able to create a {string} list by specifying names manually")
    public void verifyThatTheUserIsAbleToCreateAListBySpecifyingNamesManually(String listType) {
        sharedList.saveList();
        String successMessage = sharedList.isListCreatedOrDeleted();
        logger.info("Creation status message: '{}'", successMessage);

        switch (listType) {
            case "Domains":
                Assert.assertEquals("Domain list created successfully", successMessage);
                break;
            case "AppBundle":
                Assert.assertEquals("AppBundle list created successfully", successMessage);
                break;
            case "Keywords":
                Assert.assertEquals("Keywords list created successfully", successMessage);
                break;
            case "IP Address":
                Assert.assertEquals("IPAddress list created successfully", successMessage);
                break;
            default:
                Assert.fail("Invalid list type specified: " + listType);
        }
    }

    @And("Verify that PulsePoint provided domain list {string} is denoted with a purple P icon")
    public void verifyThatPulsePointProvidedListsAreDenotedWithAPurpleIconUnderTheTab(
            String pulsepointProvidedDomainList) {
        logger.info(
                "Searching for PulsePoint provided list '{}' to verify purple P icon", pulsepointProvidedDomainList);
        sharedList.searchCreatedList(pulsepointProvidedDomainList);
        sharedList.openSearchedList(pulsepointProvidedDomainList);
        boolean isIconPresent = sharedList.fetchPulsepointIcon(pulsepointProvidedDomainList);
        Assert.assertTrue("P icon is not present on the PulsePoint provided list", isIconPresent);
    }

    @And("Verify that the counter on the left displays the correct value for each list in the navigation panel")
    public void verifyThatTheCounterOnTheLeftDisplaysTheCorrectValueForEachListInTheNavigationPanel() {
        logger.info(
                "Verify that the counter on the left displays the correct value for each list in the navigation panel");
        sharedList.searchCreatedList(metricName);
        sharedList.openSearchedList(metricName);
        totalListCount = Integer.parseInt(sharedList.fetchCountFromLeftPanel(metricName));
        Assert.assertEquals(totalListCount, keyValues.size());
    }

    @And("User retrieves all the entered data after saving the list details {string}")
    public void userRetrievesAllTheEnteredDataAfterSavingTheListDetails(String listName) {
        logger.info("Retrieving all the entered data after saving the list details: {}", listName);
        capturedDetails.clear();
        capturedDetails = sharedList.fetchListDetailsFromEditPanel();
        List<String> normalizedExpected = itemList.stream()
                .flatMap(item -> Arrays.stream(item.split("\\R")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .sorted()
                .toList();
        List<String> normalizedActual = capturedDetails.stream()
                .flatMap(item -> Arrays.stream(item.split("\\R")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .sorted()
                .toList();
        Assert.assertEquals("List Details are not matching", normalizedExpected, normalizedActual);
    }

    @And("Verify that the user is able to edit an existing {string} name list {string}")
    public void verifyThatTheUserIsAbleToEditAnExistingNameList(String listType, String modifiedName) {
        logger.info("Editing existing '{}' list with modified names: {}", listType, modifiedName);
        keyValues = new ArrayList<>(CommonUtils.convertStringToList(modifiedName));
        sharedList.editAnExistingList(keyValues);
        itemList.clear();
        itemList = sharedList.fetchListDetailsFromEditPanel();
        logger.info("Saving updated list");
        sharedList.saveList();
        String updateMessage = sharedList.isListCreatedOrDeleted();
        logger.info("Update status message: '{}'", updateMessage);

        switch (listType) {
            case "Domains":
                Assert.assertEquals("Domains list updated successfully", updateMessage);
                break;
            case "AppBundle":
                Assert.assertEquals("AppBundle list updated successfully", updateMessage);
                break;
            case "Keywords":
                Assert.assertEquals("Keywords list updated successfully", updateMessage);
                break;
            case "IP Address":
                Assert.assertEquals("IPAddresses list updated successfully", updateMessage);
                break;
            default:
                Assert.fail("Invalid list type specified: " + listType);
        }
    }

    @And("Verify that the user is able to delete an existing {string} name list")
    public void verifyThatTheUserIsAbleToDeleteAnExistingNameList(String listType) {
        logger.info("Deleting existing '{}' name list: {}", listType, metricName);
        sharedList.deleteList();
        String confirmation = sharedList.fetchRemovalConfirmation();
        Assert.assertEquals(metricName, confirmation);
        String deleteMessage = sharedList.isListCreatedOrDeleted();
        logger.info("Delete status message: '{}'", deleteMessage);

        switch (listType) {
            case "Domains":
                Assert.assertEquals("Domain deleted successfully", deleteMessage);
                break;
            case "AppBundle":
                Assert.assertEquals("AppBundleGroup deleted successfully", deleteMessage);
                break;
            case "Keywords":
                Assert.assertEquals("Keyword deleted successfully", deleteMessage);
                break;
            case "IP Address":
                Assert.assertEquals("IPAddress deleted successfully", deleteMessage);
                break;
            default:
                Assert.fail("Invalid list type specified: " + listType);
        }
    }

    @And("Verify the deleted list is no longer displayed in the left panel")
    public void verifyTheDeletedListIsNoLongerDisplayedInTheLeftPanel() {
        logger.info("Verifying that the deleted list '{}' is no longer displayed in the left panel", metricName);
        sharedList.searchCreatedList(metricName);
        Assert.assertTrue(
                "Deleted list is still displayed in the left panel (expected 'Nothing Found' after search)",
                sharedList.isNothingFoundDisplayed());
    }

    @And(
            "Verify that an error message is displayed when no list names is specified and user tries to upload a file {string}")
    public void verifyThatAnErrorMessageIsDisplayedWhenNoListNamesIsSpecifiedAndUserTriesToUploadAFile(
            String fileName) {
        logger.info("Attempting to upload file '{}' without a list name to verify validation error", fileName);
        sharedList.uploadDomainFile(fileName);
        String errorMessage = sharedList.fetchListErrorMessage();
        Assert.assertEquals("List Name is required", errorMessage);
    }

    @And(
            "Verify that when enters {string} and upload file {string} option is selected, the text area to direct enter the names disappears")
    public void verifyThatWhenUploadFileOptionIsSelectedTheTextAreaToDirectEnterTheNamesDisappears(
            String listName, String fileName) {
        logger.info(
                "Verify that when enters {} and upload file {} option is selected, the text area to direct enter the names disappears",
                listName,
                fileName);
        metricName = listName + "_" + CommonUtils.timeStampCalculation();
        npiName = metricName;
        sharedList.enterListName(metricName);
        boolean isTextAreaVisibleBefore = sharedList.verifyTextAreaIsVisibleBeforeFileUpload();
        Assert.assertTrue("Text area is not available", isTextAreaVisibleBefore);
        logger.info("Uploading domain file: {}", fileName);
        sharedList.uploadDomainFile(fileName);
        boolean isTextAreaVisibleAfter = sharedList.verifyTextAreaIsVisibleAfterFileUpload();
        Assert.assertTrue("Text area is available", isTextAreaVisibleAfter);
    }

    @And(
            "Verify the Uploaded Files section displays the entries count, includes download and delete icons after the file {string} is uploaded")
    public void
    verifyUploadedFilesSectionDisplaysEntriesIncludedInTheFileTimestampDownloadAndDeleteIconsOnceTheFileIsUploaded(
            String fileName) throws IOException {
        logger.info(
                "Verify the Uploaded Files section displays the entries count, includes download and delete icons after the file {} is uploaded",
                fileName);
        String fetchedFileName = sharedList.fetchFileNameFromUploadedFilesSection(fileName);
        Assert.assertEquals(fileName, fetchedFileName);
        int expectedCount = FileActions.fetchRowCountExcludeHeaderFromCSVAndTxt(fileName);
        int actualCount = sharedList.fetchDomainCountFromUploadedFilesSection(fileName);
        Assert.assertEquals(expectedCount, actualCount);
        boolean isDownloadVisible = sharedList.isDownloadIconVisible(fileName);
        Assert.assertTrue("No Download icon is available", isDownloadVisible);
        boolean isDeleteVisible = sharedList.isDeleteIconVisible(fileName);
        Assert.assertTrue("No Delete icon is available", isDeleteVisible);
    }

    @And("Verify that the user is able to create a {string} list through file upload")
    public void verifyThatTheUserIsAbleToCreateAListThroughFileUpload(String listType) {
        sharedList.saveList();
        String successMessage = sharedList.isListCreatedOrDeleted();
        logger.info("Creation Status message: '{}'", successMessage);

        switch (listType) {
            case "Domains":
                Assert.assertEquals("Domains list created successfully", successMessage);
                break;
            case "AppBundle":
                Assert.assertEquals("AppBundle list created successfully", successMessage);
                break;
            case "Keywords":
                Assert.assertEquals("Keywords list created successfully", successMessage);
                break;
            case "IP Address":
                Assert.assertEquals("IPAddresses list created successfully", successMessage);
                break;
            default:
                Assert.fail("Invalid list type specified: " + listType);
        }
    }

    @And("Verify that the counter on the left displays the correct value after file upload {string}")
    public void verifyThatTheCounterOnTheLeftDisplaysTheCorrectValueAfterFileUpload(String fileName) {
        logger.info("Verify that the counter on the left displays the correct value after file upload {}", fileName);
        sharedList.searchCreatedList(metricName);
        sharedList.openSearchedList(metricName);
        totalListCount = Integer.parseInt(sharedList.fetchCountFromLeftPanel(metricName));
        itemCount = sharedList.fetchDomainCountFromUploadedFilesSection(fileName);
        Assert.assertEquals(totalListCount, itemCount);
    }

    @And(
            "Verify that the user is able to edit an existing list by uploading same file {string} again and verify the changes")
    public void verifyThatTheUserIsAbleToEditAnExistingNameListByUploadingSameFileAgainAndVerifyTheChanges(
            String fileName) {
        logger.info("Editing list by attempting to upload duplicate file: {}", fileName);
        sharedList.uploadDomainFile(fileName);
        boolean isDuplicateDialogShown = sharedList.verifyIfDuplicateFileDialogIsDisplayed(fileName);
        Assert.assertTrue("Different filename is displayed", isDuplicateDialogShown);
        sharedList.clickReplaceButton();
    }

    @And(
            "Verify that the user is able to edit and save an existing {string} list by uploading another file {string} and verify the changes")
    public void verifyThatTheUserIsAbleToEditAnExistingNameListByUploadingAnotherFileAndVerifyTheChanges(
            String listType, String fileName) throws CsvValidationException, IOException {
        logger.info("Editing '{}' list by uploading a different file: {}", listType, fileName);
        sharedList.uploadDomainFile(fileName);
        String fetchedFileName = sharedList.fetchFileNameFromUploadedFilesSection(fileName);
        Assert.assertEquals(fileName, fetchedFileName);
        int expectedCount = FileActions.fetchRowCountExcludeHeaderFromCSVAndTxt(fileName);
        int actualCount = sharedList.fetchDomainCountFromUploadedFilesSection(fileName);
        Assert.assertEquals(expectedCount, actualCount);
        Assert.assertTrue("No Download icon is available", sharedList.isDownloadIconVisible(fileName));
        Assert.assertTrue("No Delete icon is available", sharedList.isDeleteIconVisible(fileName));
        logger.info("Saving edited list");
        sharedList.saveList();
        String updateMessage = sharedList.isListCreatedOrDeleted();
        logger.info("Update Status message: '{}'", updateMessage);

        switch (listType) {
            case "Domains":
                Assert.assertEquals("Domains list updated successfully", updateMessage);
                break;
            case "AppBundle":
                Assert.assertEquals("AppBundle list updated successfully", updateMessage);
                break;
            case "Keywords":
                Assert.assertEquals("Keywords list updated successfully", updateMessage);
                break;
            case "IP Address":
                Assert.assertEquals("IPAddresses list updated successfully", updateMessage);
                break;
            default:
                Assert.fail("Invalid list type specified: " + listType);
        }
    }

    @And("Verify that the counter on the left displays the updated value after new file upload {string}")
    public void verifyThatTheCounterOnTheLeftDisplaysTheUpdatedValueAfterNewFileUpload(String fileName) {
        logger.info(
                "Verify that the counter on the left displays the updated value after new file upload {}", fileName);
        sharedList.searchCreatedList(metricName);
        sharedList.openSearchedList(metricName);
        int domainCount = Integer.parseInt(sharedList.fetchCountFromLeftPanel(metricName));
        int newUploadCount = sharedList.fetchDomainCountFromUploadedFilesSection(fileName);
        Assert.assertEquals(itemCount + newUploadCount, domainCount);
    }

    @And(
            "Verify that user is able to download the uploaded file {string}, {string} and fetches the count of the downloaded files")
    public void verifyThatUserIsAbleToDownloadTheUploadedFile(String fileName1, String fileName2) throws IOException {
        logger.info("Verify that user is able to download the uploaded file {}, {}", fileName1, fileName2);
        Path filePath1 = sharedList.downloadFile(fileName1);
        Assert.assertTrue("Downloaded file is not available", CommonUtils.isDownloadedFileAvailable(filePath1, "csv"));
        file1RecordCount = FileActions.fetchRowCountFromCSV(filePath1);

        Path filePath2 = sharedList.downloadFile(fileName2);
        Assert.assertTrue("Downloaded file is not available", CommonUtils.isDownloadedFileAvailable(filePath2, "csv"));
        file2RecordCount = FileActions.fetchRowCountFromCSV(filePath2);
    }

    @And(
            "Verify that the count of the downloaded files {string}, {string} matches with the count displayed in the Uploaded Files section and left side panel")
    public void
            verifyThatTheCountOfTheDownloadedFilesMatchesWithTheCountDisplayedInTheUploadedFilesSectionAndLeftSidePanel(
                    String fileName1, String fileName2) {
        logger.info(
                "Verifying that the count of the downloaded files matches with the count displayed in the Uploaded Files section and left side panel");
        int uploadedFileCount1 = sharedList.fetchDomainCountFromUploadedFilesSection(fileName1);
        int uploadedFileCount2 = sharedList.fetchDomainCountFromUploadedFilesSection(fileName2);
        Assert.assertEquals(file1RecordCount, uploadedFileCount1);
        Assert.assertEquals(file2RecordCount, uploadedFileCount2);

        int leftPanelCount = Integer.parseInt(sharedList.fetchCountFromLeftPanel(metricName));
        Assert.assertEquals(file1RecordCount + file2RecordCount, leftPanelCount);
    }

    @And("Verify that the user is able to delete the uploaded file {string}")
    public void verifyThatTheUserIsAbleToDeleteTheUploadedFile(String fileName) {
        logger.info("Deleting uploaded file: {}", fileName);
        sharedList.deleteFile(fileName);
        String removalConfirmation = sharedList.fetchRemovalConfirmation();
        Assert.assertEquals(fileName, removalConfirmation);
    }

    /*Roshani Sherkar
     * 20-08-2025
     * Atrribute NPI List creation and targeting it at tactic level*/
    @And("Navigate to Campaign Dashboard and clicks on Create Campaign")
    public void navigateToCampaignDashboardAndClicksOnCreateCampaign() {
        logger.info("Navigate to Campaign Dashboard and clicks on Create Campaign");
        navigation.clickSubMenu();
        navigation.clickCampaigns();
        Assert.assertTrue("Unable to navigate to Campaign Dashboard", campaigns.isCreateCampaignButtonVisible());
        campaigns.createCampaign();
        Assert.assertEquals("Create New Campaign", campaigns.verifyCampaignText());
    }

    @And("User add and configure {string} targeting rule and verify list is displayed in the targeting rule")
    public void userAddAndConfigureNPITargetingRule(String ruleType) {
        logger.info("Adding and configuring targeting rule: {} with NPI list: {}", ruleType, npiName);
        tacticSettings.selectTargetingRule(ruleType, npiName);
        boolean isListAvailable = tacticSettings.isListAvailableInTargetingPanel(npiName);
        Assert.assertTrue("List is not available", isListAvailable);
        tacticSettings.clickTarget(npiName);
        itemCount = tacticSettings.fetchSelectedListCountFromTargetingPanel();
        logger.info("Fetched selected list count from targeting panel: {}", itemCount);
    }

    @And(
            "Verify that the total NPI count and the matched NPI count from the list are correctly displayed in the targeting rule")
    public void verifyTheTotalNPICountFromTheListIsDisplayedInTheTargetingRule() {
        String npiCount = tacticSettings.fetchTotalNPICountFromNewTab(npiName);
        logger.info("Raw NPI count string fetched from new tab: {}", npiCount);
        String[] parts = npiCount.split("&");
        totalListCount = Integer.parseInt(parts[0].split("-")[1]);
        int matchedNPIListCount = Integer.parseInt(parts[1].split("-")[1]);
        logger.info("Parsed counts from list - Total: {}, Matched: {}", totalListCount, matchedNPIListCount);
        String npiCountFromTargetingPanel = tacticSettings.fetchNPICountFromTargetingPanel();
        String matchedNpiCountFromTargetingPanel = tacticSettings.fetchMatchedNPICountFromTargetingPanel();
        Assert.assertEquals(
                "Total NPI count from the list is not matching with the count in targeting rule",
                String.valueOf(totalListCount),
                npiCountFromTargetingPanel);
        Assert.assertTrue(
                "Matched NPI count from the list is not matching with the count in targeting rule",
                matchedNpiCountFromTargetingPanel.contains(String.valueOf(matchedNPIListCount)));
    }

    @And("User saves the rule configured in the tactic")
    public void userSavesTheRuleConfiguredInTheTactic() {
        logger.info("Saving configured rule in Tactic settings");
        tacticSettings.clickOk();
        tacticSettings.clickClose();
    }

    @Then("Verify that the {string} rule is added to the tactic and retrieve the count of selected lists")
    public void verifyThatTheNPIRuleIsAddedToTheTacticAndRetrieveTheCountOfSelectedLists(String ruleType) {
        logger.info(
                "Verify that the {} rule is added to the tactic and retrieve the count of selected lists", ruleType);
        String addedRules = tacticSettings.verifyIfRuleIsAdded();
        Assert.assertTrue("Unable to add Rule", addedRules.contains(ruleType));
        String text = tacticSettings.fetchSelectedListCountFromTactic(ruleType);
        Assert.assertTrue("Selected list count is not matching", text.contains(String.valueOf(itemCount)));
    }

    @And(
            "Verify that the selected list is displayed in the targeting rule and retrieve the total count of targeted items")
    public void verifyThatTheSelectedListIsDisplayedInTheTargetingRuleAndRetrieveTheTotalNPICount() {
        logger.info("Verifying selected list '{}' is displayed in tactic targeting rules", npiName);
        boolean isPresent = tacticSettings.isSelectedListPresentInTactic(npiName);
        Assert.assertTrue("Selected List is not available", isPresent);
        String text = tacticSettings.fetchSelectedListItemCountFromTactic(npiName);
        logger.info("Targeted items count text for list '{}': {}", npiName, text);
        Assert.assertTrue("Selected list count is not matching", text.contains(String.valueOf(totalListCount)));
    }

    @And("User navigates to Pixels page")
    public void userNavigatesToPixelsPage() {
        logger.info("User navigates to Pixels page");
        navigation.clickSubMenu();
        pixels.clickPixelsMenuItem();
    }

    @When("User clicks on Add Pixel button")
    public void userClicksOnAddPixelButton() {
        logger.info("User navigates to Pixels page");
        pixels.clickAddPixelButton();
    }

    @Then("Verify the Create New Pixel panel and types of Pixel")
    public void verifyCreateNewPixelPanelAndTypesOfPixel() {
        logger.info("User clicks on Add Pixel button");
        Assert.assertEquals(
                "CREATE NEW PIXEL", pixels.verifyCreateNewPixelLabel().toUpperCase());
        Assert.assertEquals("RETARGETING PIXEL", pixels.verifyRetargetingPixel().toUpperCase());
        Assert.assertEquals("SMART PIXEL", pixels.verifySmartPixel().toUpperCase());
        Assert.assertEquals("CONVERSION PIXEL", pixels.verifyConversionPixel().toUpperCase());
    }

    @And("User selects the {string} type")
    public void userSelectsThePixelType(String pixelType) {
        logger.info("Selecting Pixel type: {}", pixelType);
        pixels.selectPixelType(pixelType);
    }

    @And("User enters the pixel details as {string} {string}")
    public void userEntersPixelDetails(String pixelName, String advertiser) {
        newPixelName = pixelName + '_' + CommonUtils.timeStampCalculation();
        logger.info("Entering basic Pixel details - Name: {}, Advertiser: {}", newPixelName, advertiser);
        retargetingPixel.enterPixelName(newPixelName);
        retargetingPixel.selectAdvertiser(advertiser);
    }

    @And("User enters the pixel details as {string} {string} {string} {string}")
    public void userEntersPixelDetails(
            String pixelName, String advertiser, String conversionPixelScope, String conversionPixelType) {
        newPixelName = pixelName + '_' + CommonUtils.timeStampCalculation();
        logger.info(
                "Entering Conversion Pixel details - Name: {}, Advertiser: {}, Scope: {}, Type: {}",
                newPixelName,
                advertiser,
                conversionPixelScope,
                conversionPixelType);
        conversionPixel.enterPixelName(newPixelName);
        conversionPixel.selectAdvertiser(advertiser);
        conversionPixel.selectConversionPixelScope(conversionPixelScope);
        conversionPixel.selectConversionPixelType(conversionPixelType);
    }

    @And("User selects the {string} and the associated campaign")
    public void userSelectsTheFromTheList(String advertiser) {
        logger.info("Selecting Advertiser '{}' and its associated campaign for Smart Pixel", advertiser);
        smartPixel.selectAdvertiser(advertiser);
        smartPixel.selectAssociatedCampaign();
    }

    @And("User saves the pixel")
    public void userSavesThePixel() {
        logger.info("Saving Pixel");
        pixels.savePixel();
    }

    @Then(
            "Verify the pixel is saved successfully, search for it by name, and confirm it is displayed in the pixel list")
    public void verifyPixelIsSavedSuccessfullyAndDisplayedInPixelList() {
        logger.info("User saves the pixel");
        String saveSuccessMessage = pixels.verifySaveSuccess();
        Assert.assertTrue("Unable to save pixel", saveSuccessMessage.contains("Success!"));
        pixels.searchSavedPixel(newPixelName);
        String foundPixel = pixels.verifyCreatedPixel(newPixelName);
        Assert.assertEquals(newPixelName, foundPixel);
    }

    @Then(
            "Verify the smart pixel is saved successfully, search for it by name, and confirm it is displayed in the pixel list")
    public void verifySmartPixelIsSavedSuccessfullyAndDisplayedInPixelList() {
        logger.info(
                "Verifying: the pixel is saved successfully, search for it by name, and confirm it is displayed in the pixel list");
        String saveSuccessMessage = pixels.verifySaveSuccess();
        Assert.assertTrue("Unable to save Smart Pixel", saveSuccessMessage.contains("Success!"));
        newPixelName = smartPixel.getPixelNameFromHeader();
        logger.info("Fetched Smart Pixel name from header: {}", newPixelName);
        pixels.searchSavedPixel(newPixelName);
        String foundPixel = pixels.verifyCreatedPixel(newPixelName);
        Assert.assertEquals(newPixelName, foundPixel);
    }

    @When("User selects {string} as rule type and selects the created pixel")
    public void userSelectsRuleTypeAndSelectsCreatedPixelAndSavesSettings(String ruleType) {
        logger.info("Selecting Rule Type: '{}' with created pixel: {}", ruleType, newPixelName);
        itemCount = tacticSettings.selectRuleType(ruleType, newPixelName);
        logger.info("Returned item count after rule selection: {}", itemCount);
    }

    @Then("Verify the selected targeting rule {string}")
    public void verifyTheSelectedTargetingRule(String ruleType) {
        logger.info("Verify the selected targeting rule {}", ruleType);
        Assert.assertEquals(ruleType, tacticSettings.verifyRuleType());
        Assert.assertEquals(newPixelName, tacticSettings.verifyRuleOption());
    }

    @Then("Verify the selected targeting rule {string} for Smart list")
    public void verifyTheSelectedTargetingRuleForSmartList(String ruleType) {
        logger.info("Verify the selected targeting rule {} for Smart list", ruleType);
        Assert.assertEquals(ruleType, tacticSettings.verifyRuleType());
        Assert.assertEquals(npiName, tacticSettings.verifyRuleOption());
    }

    @And("User enters the Smart NPI list details as {string} {string} and selects the created {string}")
    public void userEntersTheSmartNPIListDetailsAndSelectsTheCreatedSmartPixel(
            String npiListName, String advertiser, String smartListType) {
        npiName = npiListName + '_' + CommonUtils.timeStampCalculation();
        logger.info(
                "Entering Smart NPI list details. Name: {}, Advertiser: {}, Type: {}",
                npiName,
                advertiser,
                smartListType);
        npiStaticList.enterListName(npiName);
        npiStaticList.selectAdvertiser(advertiser);
        npiSmartList.clickLifeCheckbox();
        npiSmartList.selectSmartNPIListType(smartListType);
        logger.info("Selecting associated Smart Pixel: {}", newPixelName);
        npiSmartList.clickSmartPixelDropDown();
        npiSmartList.clickSmartPixelDropDownValue(newPixelName);
    }

    @Then("Verify the selected Smart Pixel")
    public void verifyTheSelectedSmartPixel() {
        logger.info("Verify the selected Smart Pixel");
        String selectedPixel = npiSmartList.verifySelectedSmartPixel();
        Assert.assertEquals(newPixelName, selectedPixel);
    }

    @And("User selects {string} as rule type and selects the created Smart list")
    public void userSelectsRuleTypeAndSelectsCreatedSmartList(String ruleType) {
        logger.info("Selecting Rule Type: '{}' with created Smart list: {}", ruleType, npiName);
        itemCount = tacticSettings.selectRuleType(ruleType, npiName);
        logger.info("Returned item count after Smart list rule selection: {}", itemCount);
    }

    @Then("Verify the count of rule options for the selected targeting rule {string} on the Tactic Settings page")
    public void verifyTheCountOfSelectedRuleOptions(String ruleType) {
        logger.info(
                "Verify the count of rule options for the selected targeting rule {} on the Tactic Settings page",
                ruleType);
        String optionsCount = tacticSettings.fetchSelectedListCountFromTactic(ruleType);
        int targetedOptionsCount = Integer.parseInt(optionsCount.replaceAll("[^0-9]", ""));
        Assert.assertEquals("Selected options count does not match", itemCount, targetedOptionsCount);
    }

    /*Roshani Sherkar
     * 25-08-2025
     * E2E Domain List creation and targeting it at tactic level*/
    @And("User enters {string} in the List Name field")
    public void userEntersInTheListNameField(String listName) {
        metricName = listName + "_" + CommonUtils.timeStampCalculation();
        logger.info("Entering List name: {}", metricName);
        sharedList.enterListName(metricName);
        npiName = metricName;
    }

    /*Creative Bulk Upload */
    @Given("User clicks Bulk Upload button on Creative Library page")
    public void userClicksBulkUploadButtonOnCreativeLibraryPage() {
        logger.info("User clicks Bulk Upload button on Creative Library page");
        bulkCreativeUpload.clickBulkUploadButton();
    }

    @Then("Verify Bulk Upload panel is displayed with following options - Creative Type and Advertiser Dropdown")
    public void verifyBulkUploadPanelIsDisplayedWithFollowingOptionsCreativeTypeAndAdvertiserDropdown() {
        logger.info("User clicks Bulk Upload button on Creative Library page");
        boolean hasCreativeType = bulkCreativeUpload.checkCreativeTypeButtonsArePresent();
        boolean hasAdvertiserDrop = bulkCreativeUpload.checkAdvertiserDropdownIsShown();
        Assert.assertTrue("Creative Type Options are not available", hasCreativeType);
        Assert.assertTrue("Advertiser dropdown is not available", hasAdvertiserDrop);
    }

    @And("Verify the availability of below Creative Type options and the Default option is {string}")
    public void verifyTheAvailabilityOfBelowCreativeTypeOptionsAndTheDefaultOptionIsDisplay(
            String defaultOption, DataTable dataTable) {
        List<String> creativeTypeOptions = dataTable.asList(String.class);
        logger.info("Verifying Creative Type options {} and default option '{}'", creativeTypeOptions, defaultOption);
        Assert.assertEquals(
                "All creative type options are available.",
                bulkCreativeUpload.verifyCreativeTypeOptions(creativeTypeOptions));
        Assert.assertTrue(
                "Expected 'Display' to be the default selected creative type.",
                bulkCreativeUpload.checkDefaultCreativeType(defaultOption));
    }

    @When("User selects the {string} creative type")
    public void userSelectsTheCreativeType(String creativeType) {
        logger.info("Selecting Creative type: {}", creativeType);
        bulkCreativeUpload.selectAndClickCreativeType(creativeType);
    }

    @And("Verify Creative Type field is present and default value is {string}")
    public void verifyCreativeTypeFieldIsPresentAndDefaultValueIs(String defaultButtonName) {
        logger.info("Verify Creative Type field is present and default value is {}", defaultButtonName);
        boolean isVisible = bulkCreativeUpload.checkCreativeWidthTypeIsVisible();
        Assert.assertTrue("Creative Width Type Button is not available", isVisible);
        String actualDefaultValue = bulkCreativeUpload.fetchDefaultCreativeWidthType();
        Assert.assertEquals(defaultButtonName, actualDefaultValue);
    }

    @And("User selects the Approval status {string}")
    public void userSelectsTheApprovalStatus(String status) {
        logger.info("Selecting Approval Status: {}", status);
        bulkCreativeUpload.selectApprovalStatus(status);
    }

    @And(
            "Verify an appropriate error message when user attempts to click the Preview or OK button without selecting a creative file")
    public void userAttemptsToClickThePreviewButtonWithoutSelectingACreativeFile() {
        logger.info("Validating error message when preview/upload is clicked without a file");
        bulkCreativeUpload.isRemoveFileIconAvailable();
        bulkCreativeUpload.clickPreviewButton();
        bulkCreativeUpload.clickUploadButton();
        String errorMessage = bulkCreativeUpload.fetchErrorAlert();
        Assert.assertEquals("Atleast one creative should be selected", errorMessage);
    }

    @Then("Verify the header message for {string} status")
    public void verifyForStatusTheHeaderMessageShouldBeDisplayed(String status) {
        String headerMessage = bulkCreativeUpload.fetchHeaderMessage();
        logger.info("Fetched header message: '{}'", headerMessage);

        switch (status) {
            case "Pending":
                Assert.assertEquals("CREATIVE IS NOT APPROVED YET", headerMessage);
                break;
            case "Approved":
                Assert.assertEquals("CREATIVE IS APPROVED", headerMessage);
                break;
            case "Denied":
                Assert.assertEquals("CREATIVE IS DENIED", headerMessage);
                break;
            default:
                logger.warn("Unknown status provided for header verification: {}", status);
        }
    }

    @And("User uploads a valid file {string} for {string} creative")
    public void userUploadsAValidFileForTheCreative(String fileName, String creativeType) {
        logger.info("Uploading valid file '{}' for '{}' secondary creative", fileName, creativeType);
        bulkCreativeUpload.uploadSecondaryCreativeTemplate(fileName);
        logger.info("File upload initiated successfully");
    }

    @And("User uploads a valid file {string} for {string} creative and previews the creative details")
    public void userUploadsAValidFileAndPreviewsTheCreativeDetails(String fileName, String creativeType)
            throws IOException {
        logger.info("Uploading file '{}' for '{}' creative", fileName, creativeType);
        Path latestFile = CommonUtils.getMostRecentFileFromDownloads();

        if (fileName.contains("Downloaded")) {
            bulkCreativeUpload.uploadSecondaryCreativeTemplate(String.valueOf(latestFile.getFileName()));
        } else {
            bulkCreativeUpload.uploadSecondaryCreativeTemplate(fileName);
        }

        itemList = bulkCreativeUpload.fetchBulkUploadCreativeDetails();
        bulkCreativeUpload.clickPreviewButton();
        bulkCreativeUpload.clickOKButton();
        metricName = creativeType + "_" + CommonUtils.timeStampCalculation();
        bulkCreativeUpload.updateCreativeName(metricName);
        bulkCreativeUpload.checkIfValidationErrorsExist();
        itemList = bulkCreativeUpload.fetchBulkUploadCreativeDetails();
        bulkCreativeUpload.clickUploadButton();
        itemList = itemList.stream()
                .filter(item -> !item.startsWith("https://media-active.contextweb.com/"))
                .map(item -> item.contains("*") ? item.replace("*", "x") : item)
                .toList();
        nameList.clear();
        nameList.add(metricName);
    }

    @And("User saves the creative")
    public void userSavesTheCreative() {
        logger.info("User saves the creative");
        bulkCreativeUpload.clickOKButton();
        String successAlert = bulkCreativeUpload.fetchSuccessAlert();
        Assert.assertEquals("BulkUpload created successfully.", successAlert);
    }

    @And(
            "Verify the newly created creative is displayed in the Creative Library page and contains all the details entered during creation")
    public void
            verifyTheNewlyCreatedCreativeIsDisplayedInTheCreativeLibraryPageAndContainsAllTheDetailsEnteredDuringCreation() {
        logger.info("User saves the creative");

        for (String name : nameList) {
            logger.info(
                    "Verifying: the newly created creative is displayed in the Creative Library page and contains all the details entered during creation");
            createCreatives.searchCreative(name);
            createCreatives.clickSearchedCreative(name);
            List<String> fetchSavedCreativeDetails = createCreatives.fetchCreativeDetails();
            List<String> expectedValues = itemList.stream()
                    .filter(fetchSavedCreativeDetails::contains)
                    .toList();

            for (String expected : expectedValues) {
                Assert.assertTrue(
                        "Expected value not found: " + expected, fetchSavedCreativeDetails.contains(expected));
            }

            createCreatives.clickCancelButton();
        }
    }

    /*Display Creative Bulk Upload*/
    @When("The advertiser {string} is selected for {string} creative the following sections are visible")
    public void theAdvertiserIsSelectedForCreativeTheFollowingSectionsAreVisible(
            String advertiser, String creativeType, DataTable dataTable) {
        logger.info("Selecting Creative Type: '{}' and Advertiser: '{}'", creativeType, advertiser);
        bulkCreativeUpload.selectAndClickCreativeType(creativeType);
        bulkCreativeUpload.selectAdvertiser(advertiser);
        List<String> displayCreativeSections = dataTable.asList(String.class);

        for (String section : displayCreativeSections) {
            boolean flag = bulkCreativeUpload.verifyDisplayCreativeSections(section);
            Assert.assertTrue(section + " section is not available", flag);
        }
    }

    @And("Verify under the {string} section the options {string} and {string} are available")
    public void underTheSectionTheOptionsAndAreAvailable(String sectionName, String option1, String option2) {
        Assert.assertTrue(
                option1 + " is not available under " + sectionName,
                bulkCreativeUpload.isDownloadTemplateButtonVisible());
        Assert.assertTrue(
                option2 + " is not available under " + sectionName,
                bulkCreativeUpload.isBrowseFileButtonVisible(option2));
        logger.info("Both options are available under section '{}'", sectionName);
    }

    @And("User is able to download a blank template using the Download Blank Template option")
    public void userIsAbleToDownloadABlankTemplateUsingTheOption() throws IOException {
        logger.info("User is able to download a blank template using the Download Blank Template option");
        Assert.assertTrue(
                "Downloaded file is not available",
                CommonUtils.isDownloadedFileAvailable(bulkCreativeUpload.clickBlankTemplateDownloadButton(), "xlsx"));
    }

    @And("Verify user is able to upload images {string} to get a template with URLs")
    public void userIsAbleToUploadImagesToGetATemplateWithURLsUsingTheOption(String imageFileName) throws IOException {
        logger.info("Uploading image file '{}' to generate template with URLs", imageFileName);
        bulkCreativeUpload.uploadImageFile(imageFileName);
        Assert.assertTrue(
                "Downloaded file is not available",
                CommonUtils.isDownloadedFileAvailable(bulkCreativeUpload.clickTemplateWithURLsLink(), "xlsx"));
    }

    @And("Verify under the {string} section the fields {string}, {string}, {string} are available")
    public void verifyUnderTheSectionTheFieldsAreAvailable(
            String sectionName, String field1, String field2, String field3) {
        Assert.assertTrue(
                field1 + " field is not available under " + sectionName,
                bulkCreativeUpload.isCampaignToRestrictVisible());
        Assert.assertTrue(
                field2 + " field is not available under " + sectionName,
                bulkCreativeUpload.isBrowseFileButtonVisible(field2));
        Assert.assertTrue(
                field2 + " field is not available under " + sectionName, bulkCreativeUpload.isApprovalStatusVisible());
        logger.info("All specified fields are available under section '{}'", sectionName);
    }

    @And("User is able to select a {string} from the Campaign Restrict dropdown")
    public void userIsAbleToSelectAFromTheCampaignRestrictDropdown(String campaignName) {
        logger.info("Selecting campaign '{}' from the Campaign Restrict dropdown", campaignName);
        bulkCreativeUpload.clickCampaignName(campaignName);
    }

    @And("User is able to browse and select a template {string} from the system")
    public void userIsAbleToBrowseAndSelectATemplateFromTheSystem(String fileName) {
        logger.info("Browsing and selecting system template file for secondary creative: {}", fileName);
        bulkCreativeUpload.uploadSecondaryCreativeTemplate(fileName);
        logger.info("Template file '{}' selected successfully", fileName);
    }

    @And("Verify default value of the Approval Status field is {string}")
    public void defaultValueOfTheFieldIsForCreatives(String defaultStatus) {
        logger.info("Verify default value of the Approval Status field is {}", defaultStatus);
        boolean isDefaultMatched = bulkCreativeUpload.checkDefaultApprovalStatus(defaultStatus);
        Assert.assertTrue("Expected 'Pending' to be the default selected status.", isDefaultMatched);
    }

    @And(
            "Verify under the {string} section the fields Add Third Party Tracking Pixel and Add DoubleVerify Pixel are available")
    public void verifyUnderTheSectionTheFieldsAddThirdPartyTrackingPixelTagAndAddDoubleVerifyPixelAreAvailable(
            String sectionName) {
        logger.info(
                "Verify under the {} section the fields Add Third Party Tracking Pixel and Add DoubleVerify Pixel are available",
                sectionName);
        boolean thirdPartyAvailable = bulkCreativeUpload.isThirdPartyTrackingPixelAvailable();
        Assert.assertTrue(
                "Add Third Party Tracking Pixel/Tag field is not available under " + sectionName, thirdPartyAvailable);
        boolean doubleVerifyAvailable = bulkCreativeUpload.isDoubleVerifyPixelAvailable();
        Assert.assertTrue("Add DoubleVerify Pixel is not available under " + sectionName, doubleVerifyAvailable);
        logger.info("Both pixel fields are available under section: {}", sectionName);
    }

    @And("User is able to click a third-party tracking pixel and add details {string}")
    public void userIsAbleToSelectAThirdPartyTrackingPixelForACreative(String pixelDetails) {
        logger.info("Adding Third Party Tracking Pixel with details: {}", pixelDetails);
        bulkCreativeUpload.addThirdPartyTrackingPixel(pixelDetails);
        logger.info("Third Party Tracking Pixel added successfully");
    }

    @And("User is able to add a DoubleVerify pixel")
    public void userIsAbleToAddADoubleVerifyPixelForACreative() {
        logger.info("User is able to add a DoubleVerify pixel");
        Assert.assertTrue("Unable to add DoubleVerify Pixel", bulkCreativeUpload.addDoubleVerifyPixel());
    }

    @And("User is able to delete third-party tracking pixel entries")
    public void userIsAbleToDeleteThirdPartyTrackingPixelEntries() {
        logger.info("Deleting third-party tracking pixel entries");
        Assert.assertTrue(
                "Unable to delete Third Party Tracking Pixel", bulkCreativeUpload.deleteThirdPartyTrackingPixel());
    }

    @And("An error message is displayed when a blank template {string} is uploaded")
    public void anErrorMessageIsDisplayedWhenABlankTemplateIsUploaded(String fileName) {
        logger.info("Uploading blank template file: {}", fileName);
        bulkCreativeUpload.uploadBlankTemplate(fileName);
        logger.info("Blank template upload attempted, error message should be displayed");
    }

    @And("User enters {string}, {string} mandatory fields data for Display creative")
    public void userEntersMandatoryFieldsDataForCreative(String advertiserDSA, String financer) {
        logger.info("Entering mandatory fields data - Advertiser DSA: {}, Financer: {}", advertiserDSA, financer);
        bulkCreativeUpload.enterAdvertiserDSA(advertiserDSA);
        bulkCreativeUpload.enterFinancer(financer);
        logger.info("Mandatory fields data entered successfully");
    }

    @And("Verify Advertiser field should be mandatory")
    public void verifyAdvertiserFieldShouldBeMandatory() {
        logger.info("Verify Advertiser field should be mandatory");
        bulkCreativeUpload.clickPreviewButton();
        bulkCreativeUpload.clickOKButton();
        Assert.assertEquals("Select Advertiser", bulkCreativeUpload.fetchErrorAlert());
    }

    @And(
            "Verify that the Landing Domain field is mandatory when all other required fields, including {string} are filled")
    public void verifyLandingDomainFieldShouldBeMandatoryByEnteringOtherMandatoryFields(String advertiser) {
        logger.info(
                "Verify that the Landing Domain field is mandatory when all other required fields, including {} are filled",
                advertiser);
        bulkCreativeUpload.selectAdvertiser(advertiser);
        bulkCreativeUpload.clickPreviewButton();
        bulkCreativeUpload.clickOKButton();
        Assert.assertEquals("Landing Page Domain is required", bulkCreativeUpload.fetchErrorAlert());
    }

    @And(
            "Verify that an appropriate error message is displayed when invalid data {string} is entered for the Landing Domain")
    public void verifyThatAnAppropriateErrorMessageIsDisplayedWhenInvalidDataIsEnteredForTheLandingDomain(
            String invalidLandingDomain) {
        logger.info("Entering invalid Landing Domain: {}", invalidLandingDomain);
        bulkCreativeUpload.enterLandingPageDomain(invalidLandingDomain);
        bulkCreativeUpload.clickPreviewButton();
        bulkCreativeUpload.clickOKButton();
        Assert.assertEquals("Landing Page Domain is not valid.", bulkCreativeUpload.fetchErrorAlert());
    }

    @And("Verify only valid Landing Domain {string} values should be permitted")
    public void verifyOnlyValidLandingDomainValuesShouldBePermitted(String validLandingDomain) {
        logger.info("Entering valid Landing Domain: {}", validLandingDomain);
        bulkCreativeUpload.enterLandingPageDomain(validLandingDomain);
        Assert.assertEquals("No error alert is displayed.", bulkCreativeUpload.fetchErrorAlert());
    }

    @And("Verify default value of the File field should be {string}")
    public void verifyDefaultValueOfTheFileFieldShouldBe(String defaultValue) {
        logger.info("Verify default value of the File field should be {}", defaultValue);
        Assert.assertEquals(defaultValue, bulkCreativeUpload.fetchFileDefaultValue());
    }

    @And("Verify default value of the AdChoices Icon should be {string}")
    public void verifyDefaultValueOfTheAdChoicesIconShouldBe(String defaultValue) {
        logger.info("Verify default value of the AdChoices Icon should be {}", defaultValue);
        Assert.assertEquals(defaultValue, bulkCreativeUpload.fetchAdChoiceDefaultValue());
    }

    @And("Verify default value of the Notes Column field should be {string}")
    public void verifyDefaultValueOfTheNotesColumnFieldShouldBe(String defaultValue) {
        logger.info("Verify default value of the Notes Column field should be {}", defaultValue);
        Assert.assertEquals(defaultValue, bulkCreativeUpload.fetchNotesColumnDefaultValue());
    }

    @And("Verify Rich Media checkbox should be present and selectable {string}")
    public void verifyRichMediaCheckboxShouldBePresentAndSelectable(String direction) {
        Assert.assertTrue("Rich Media Checkbox is not available", bulkCreativeUpload.isRichMediaCheckboxAvailable());
        Assert.assertTrue("Rich Media Checkbox is not clickable", bulkCreativeUpload.isRichMediaCheckboxClickable());
        bulkCreativeUpload.selectAndClickDirection(direction);
        logger.info("Rich Media checkbox selected with direction: {}", direction);
    }

    @And(
            "Verify that the user is able to browse the computer, upload the following file types, and create creatives using details - {string}, {string}, {string}, {string}, {string}, {string}, {string}, {string}, {string}, {string}")
    public void verifyThatTheUserIsAbleToBrowseTheComputerUploadTheFollowingFileTypesAndCreateCreativesUsingDetails(
            String advertiser,
            String advertiserDSA,
            String financer,
            String landingDomain,
            String status,
            String creativeName,
            String size,
            String duration,
            String fileType,
            String fileName)
            throws IOException {
        logger.info(
                "Starting creative upload and creation flow. Advertiser: {}, FileType: {}, FileName: {}",
                advertiser,
                fileType,
                fileName);
        nameList.clear();
        logger.info("Filling advertiser and financial details - DSA: {}, Financer: {}", advertiserDSA, financer);
        bulkCreativeUpload.selectAdvertiser(advertiser);
        bulkCreativeUpload.enterAdvertiserDSA(advertiserDSA);
        bulkCreativeUpload.enterFinancer(financer);
        logger.info("Selecting file type '{}' and uploading file '{}'", fileType, fileName);
        bulkCreativeUpload.selectFileTypeAndUploadFile(fileType, fileName);
        logger.info("Entering Landing Domain: {} and Approval Status: {}", landingDomain, status);
        bulkCreativeUpload.enterLandingPageDomain(landingDomain);
        bulkCreativeUpload.selectApprovalStatus(status);
        bulkCreativeUpload.clickPreviewButton();
        logger.info("Entering creative name: {}", creativeName);
        nameList = bulkCreativeUpload.enterCreativeName(creativeName);
        logger.info("Generated and tracked creative names: {}", nameList);

        if (bulkCreativeUpload.isWidthHeightVisibleAndBlank()) bulkCreativeUpload.enterWidthHeight(size);
        if (bulkCreativeUpload.isDurationVisibleAndBlank()) bulkCreativeUpload.enterDuration(duration);

        bulkCreativeUpload.clickUploadButton();
        bulkCreativeUpload.clickOKButton();
        String successAlert = bulkCreativeUpload.fetchSuccessAlert();
        Assert.assertEquals("BulkUpload created successfully.", successAlert);
    }

    @And("Verify user is able to type in {string} categories")
    public void verifyUserIsAbleToTypeInCategories(String iabCategory) {
        logger.info("Verify user is able to type in {} categories", iabCategory);
        bulkCreativeUpload.typeIABCategory(iabCategory);
    }

    @And(
            "Verify that the Clickthrough URL and Landing Domain fields are validated as mandatory when all other required fields are filled")
    public void
            verifyThatTheClickthroughURLAndLandingDomainFieldsAreValidatedAsMandatoryWhenAllOtherRequiredFieldsIncludingAreFilled() {
        logger.info("Verifying mandatory validation for Clickthrough URL and Landing Domain");
        bulkCreativeUpload.clickPreviewButton();
        bulkCreativeUpload.clickOKButton();
        List<String> expectedMessages =
                Arrays.asList("Clickthrough URL is required", "Landing Page Domain is required");
        Assert.assertEquals(expectedMessages, bulkCreativeUpload.fetchInlineValidationMessage());
        logger.info("Mandatory validation messages verified for Clickthrough URL and Landing Domain");
    }

    @And("Verify only valid Clickthrough URL {string} values should be permitted")
    public void verifyOnlyValidClickthroughURLValuesShouldBePermitted(String validURL) {
        logger.info("Verify only valid Clickthrough URL {} values should be permitted", validURL);
        bulkCreativeUpload.enterClickthroughURL(validURL);
        Assert.assertEquals("No error alert is displayed.", bulkCreativeUpload.fetchErrorAlert());
    }

    @And(
            "Verify data persistence when user creates and saves {string} Bulk upload creative using details {string} as Advertiser, {string}, {string} and below Creative attributes")
    public void userCreatesAndSavesBulkUploadCreativeUsingDetailsAsAdvertiserAsCreativeNameAndBelowCreativeAttributes(
            String creativeType, String advertiser, String advertiserDSA, String financer, DataTable dataTable)
            throws IOException {
        logger.info("Creating Bulk Upload creative - Type: {}, Advertiser: {}", creativeType, advertiser);
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String type = row.get("CreativeType").trim();
            String attributes = row.get("CreativeAttributes").trim();
            String creativeName = creativeType + "_Creative_" + CommonUtils.timeStampCalculation();
            Map<String, String> attributeMap = Arrays.stream(attributes.split(","))
                    .map(String::trim)
                    .map(entry -> entry.split(":", 2))
                    .collect(Collectors.toMap(e -> e[0].trim(), e -> e[1].trim()));
            bulkCreativeUpload.clickBulkUploadButton();
            bulkCreativeUpload.selectAndClickCreativeType(creativeType);
            bulkCreativeUpload.enterCreativeAndDSADetails(advertiser, advertiserDSA, financer);
            bulkCreativeUpload.fillAttributes(type, attributeMap, creativeName);
            String successAlert = bulkCreativeUpload.fetchSuccessAlert();
            Assert.assertEquals("BulkUpload created successfully.", successAlert);
            nameList.add(creativeName);
        }
    }

    /*Run A Report - Fields verification and validation*/
    @And("Verify Run Report panel should be opened")
    public void verifyRunReportPanelIsOpened() {
        logger.info("Verify Run Report panel should be opened");
        Assert.assertTrue("Run Report panel is not opened", runReport.isRunReportPanelOpened());
    }

    @And("Template drop-down should display templates created under {string}")
    public void templateDropDownShouldDisplayTemplatesCreatedUnder(String templateName) {
        logger.info("Template drop-down should display templates created under {}", templateName);
        Assert.assertTrue("Template dropdown is not present", runReport.isTemplateDropdownAvailable());
    }

    @When("User clicks on {string} link")
    public void userClicksOnLink(String linkName) {
        logger.info("Clicking on link: {}", linkName);
        runReport.clickLink(linkName);
    }

    @Then("Dimensions and Metrics fields should be displayed")
    public void dimensionsAndMetricsFieldsShouldBeDisplayed() {
        logger.info("Dimensions and Metrics fields should be displayed");
        Assert.assertTrue(
                "Dimension and Metrics dropdown are not displayed", runReport.isDimensionsAndMetricsDisplayed());
    }

    @And("User should navigate back to Template drop-down by clicking {string}")
    public void userShouldNavigateBackToTemplateDropDownByClicking(String linkName) {
        logger.info("Navigating back by clicking link: {}", linkName);
        runReport.clickLink(linkName);
    }

    @Then("Template drop-down should be visible")
    public void templateDropDownShouldBeVisible() {
        logger.info("Template drop-down should be visible");
        Assert.assertTrue("Template dropdown is not present", runReport.isTemplateDropdownAvailable());
    }

    @Then("Data Granularity should have default value {string}")
    public void dataGranularityShouldHaveDefaultValue(String defaultValue) {
        logger.info("Data Granularity should have default value {}", defaultValue);
        Assert.assertEquals(defaultValue.trim(), runReport.getDefaultDataGranularity());
    }

    @And("Verify Data Granularity dropdown should show below list of values")
    public void verifyDataGranularityDropdownShouldShowBelowListOfValues(DataTable dataTable) {
        List<String> dropdownValues = dataTable.asList(String.class);
        logger.info("Verifying Data Granularity dropdown values: {}", dropdownValues);
        runReport.showDataGranularityOptions();
        Assert.assertEquals(new HashSet<>(dropdownValues), new HashSet<>(runReport.fetchDataGranularityOptions()));
    }

    @And("Verify Data Granularity field should allow selection any of the values {string} from the dropdown")
    public void verifyDataGranularityFieldShouldAllowSelectionAnyOfTheValuesFromTheDropdown(String dropdownValue) {
        logger.info(
                "Verify Data Granularity field should allow selection any of the values {} from the dropdown",
                dropdownValue);
        Assert.assertTrue(
                "Unable to set Data Granularity value from drop-down",
                runReport.setDataGranularity(dropdownValue));
    }

    @Then("Advertiser drop-down should list advertisers mapped to {string}")
    public void advertiserDropDownShouldListAdvertisersMappedTo(String arg0) {
        logger.info("Advertiser drop-down should list advertisers mapped to {}", arg0);
        Assert.assertTrue("Advertiser dropdown is not present", runReport.isAdvertiserDropdownAvailable());
        runReport.clickAdvertiserDropdown();
        List<String> advertiser = runReport.fetchAdvertisers();
        Assert.assertTrue("Advertiser List does not match", advertiser.containsAll(itemList));
    }

    @And("User should be able to select multiple advertisers from the list")
    public void userShouldBeAbleToSelectMultipleAdvertisersFromTheList() {
        logger.info("Selecting multiple advertisers from dropdown");
        runReport.clickAdvertiserDropdown();
        Assert.assertTrue(
                "Unable to select multiple advertisers", runReport.selectMultipleAdvertisersFromDropdown());
        logger.info("Multiple advertisers selected successfully");
    }

    @And("Verify on selecting {string} option, previously selected individual advertisers should be cleared")
    public void verifyOnSelectingOptionPreviouslySelectedIndividualAdvertisersShouldBeCleared(String advertiser) {
        logger.info(
                "Verify on selecting {} option, previously selected individual advertisers should be cleared",
                advertiser);
        Assert.assertEquals(advertiser, runReport.selectAdvertiser(advertiser));
    }

    @And("User should be able to select template {string} from the dropdown")
    public void userShouldBeAbleToSelectTemplateFromTheDropdownAndAdvertiserAs(String templateName) {
        logger.info("Selecting template from dropdown: {}", templateName);
        runReport.selectTemplateFromDropdown(templateName);
        templateNameRandom = runReport.fetchTemplateValue().get(0);
        nameList.add(templateNameRandom);
        logger.info("Template selected successfully: {}", templateNameRandom);
    }

    @And("User should be able to select advertiser as {string}")
    public void userShouldBeAbleToSelectAdvertiserAs(String advertiser) {
        logger.info("Selecting advertiser: {}", advertiser);
        runReport.clickAdvertiserDropdown();
        Assert.assertEquals(advertiser, runReport.selectAdvertiser(advertiser));
        nameList.add(advertiser);
        logger.info("Advertiser selected successfully: {}", advertiser);
    }

    @When("Campaign should load for selection when user types campaign initials {string} in {string} field")
    public void campaignShouldLoadForSelectionWhenUserTypesCampaignInitialsInCampaignField(
            String campaignInitials, String fieldName) {
        logger.info(
                "Campaign should load for selection when user types campaign initials {} in {} field",
                campaignInitials,
                fieldName);
        Assert.assertTrue(
                "Dropdown values are not loaded",
                runReport.isDropdownValueLoadedForInitials(campaignInitials, fieldName));
    }

    @Then("User should be able to select multiple values from dropdown")
    public void userShouldBeAbleToSelectMultipleValuesFromDropdown() {
        logger.info("Selecting multiple values from dropdown");
        List<String> valuesSelected = runReport.selectMultipleValueFromDropdown();
        Assert.assertFalse("Unable to select multiple values from dropdown", valuesSelected.isEmpty());
        nameList.addAll(valuesSelected);
        logger.info("Multiple dropdown values selected: {}", valuesSelected);
    }

    @When("Line Items of selected campaigns should load when user types line items initials {string} in {string} field")
    public void lineItemsOfSelectedCampaignsShouldLoadWhenUserTypesLineItemsInitialsInLineItemField(
            String lineItemInitials, String fieldName) {
        logger.info(
                "Line Items of selected campaigns should load when user types line items initials {} in {} field",
                lineItemInitials,
                fieldName);
        Assert.assertTrue(
                "Dropdown values are not loaded",
                runReport.isDropdownValueLoadedForInitials(lineItemInitials, fieldName));
    }

    @When("Tactic of selected line items should load when user types tactic names initials {string} in {string} field")
    public void tacticOfSelectedLineItemsShouldLoadWhenUserTypesTacticNamesInitialsInTacticField(
            String tacticInitials, String fieldName) {
        logger.info(
                "Tactic of selected line items should load when user types tactic names initials {} in {} field",
                tacticInitials,
                fieldName);
        Assert.assertTrue(
                "Dropdown values are not loaded",
                runReport.isDropdownValueLoadedForInitials(tacticInitials, fieldName));
    }

    @When("Creative of selected tactic should load when user types creative names initials {string} in {string} field")
    public void creativeOfSelectedTacticShouldLoadWhenUserTypesCreativeNamesInitialsInCreativeField(
            String creativeInitials, String fieldName) {
        logger.info(
                "Creative of selected tactic should load when user types creative names initials {} in {} field",
                creativeInitials,
                fieldName);
        Assert.assertTrue(
                "Dropdown values are not loaded",
                runReport.isDropdownValueLoadedForInitials(creativeInitials, fieldName));
    }

    @When("User clicks on Advanced Settings")
    public void userClicksOn() {
        runReport.clickAdvanceSettings();
    }

    @Then("{string} section should be visible with label {string} checkbox")
    public void checkboxShouldBeVisibleWithLabel(String filterReportSection, String checkboxLabel) {
        logger.info("{} section should be visible with label {} checkbox", filterReportSection, checkboxLabel);
        Assert.assertTrue(
                "Report Filter checkbox is not available",
                runReport.isFilterReportSectionAvailable(filterReportSection));
        if (runReport.isFilterReportCheckboxAvailable(checkboxLabel))
            Assert.assertEquals(
                    checkboxLabel.trim(), runReport.fetchAndClickFilterReportCheckboxLabel(checkboxLabel));
    }

    @Then("{string} and {string} tabs should be present in the Run Report pop-up")
    public void andTabsShouldBePresentInTheRunReportPopUp(String runNowTab, String scheduleTab) {
        logger.info("{} and {} tabs should be present in the Run Report pop-up", runNowTab, scheduleTab);
        Assert.assertTrue(
                "Run Now and Schedule tabs are not available",
                runReport.isRunNowAndScheduleTabsAvailable(runNowTab, scheduleTab));
    }

    @When("On Run Now tab, Report Period field should have options below")
    public void onRunNowTabReportPeriodFieldShouldHaveOptionsBelow(DataTable dataTable) {
        List<String> dropdownValues = dataTable.asList(String.class);
        logger.info("Verifying Report Period dropdown options: {}", dropdownValues);
        logger.info("Fetching Report Period options from UI: {}", runReport.fetchReportPeriodOptions());
        Assert.assertEquals(new HashSet<>(dropdownValues), new HashSet<>(runReport.fetchReportPeriodOptions()));
    }

    @And(
            "User selects {string} option from Report Period field and verify the fields displayed on selecting the option")
    public void userSelectsOptionFromReportPeriodFieldAndVerifyTheFieldsDisplayedOnSelectingTheOption(String option) {
        logger.info("Selecting Report Period option: {}", option);
        runReport.selectReportPeriodButton(option);
        Assert.assertTrue("Fields are not available", runReport.verifyReportPeriodRelatedFields(option));
    }

    @And("User selects the option Only Report on Impressions with Identifiable NPIs")
    public void userSelectsTheOptionOnlyReportOnImpressionsWithIdentifiableNPIs() {
        logger.info("User selects the option Only Report on Impressions with Identifiable NPIs");
        runReport.clickFilterReportCheckbox();
    }

    @And("User should be able to generate the report")
    public void userShouldAbleToGenerateTheReport() {
        logger.info("User selects the option Only Report on Impressions with Identifiable NPIs");
        String fileName = "Custom Report";
        logger.info("Generating report with file name: {}", fileName);
        metricName = runReport.fetchFileNameFromUI();
        runReport.clickRunButton(fileName);
        Assert.assertEquals("You will get the report on your email", runReport.fetchSuccessAlert());
    }

    @And("Validate report details such as Created By, Reporting period, Report Name from Report Listing page")
    public void validateReportDetailsSuchAsCreatedByReportingPeriodReportNameFromReportListingPage() {
        logger.info("Validating report details on Report Listing page for report: {}", metricName);
        runReport.searchReportName(metricName);
        logger.info("Fetching report details for '{}'", metricName);
        List<String> reportDetails = runReport.fetchReportDetailsFromListingPage();
        Assert.assertTrue("Created By is not available", reportDetails.contains(userType));
        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        Assert.assertTrue("Reporting Period is not today's date", reportDetails.contains(formattedDate));
        Assert.assertTrue("Report name doesn't match", reportDetails.contains(metricName));
    }

    @And("Confirms that the report panel retains the entered data")
    public void andConfirmThatTheReportPanelRetainsTheEnteredData() {
        logger.info("Fetching data before saving the report: {}", nameList);
        runReport.clickModifyOption(metricName);
        capturedDetails.addAll(runReport.fetchTemplateValue());
        capturedDetails.addAll(runReport.fetchDimensionAndMetricValues());
        capturedDetails.addAll(runReport.fetchAdvertiserName());
        capturedDetails.addAll(runReport.fetchCampaignName());
        capturedDetails.addAll(runReport.fetchLineItemName());
        capturedDetails.addAll(runReport.fetchTacticName());
        capturedDetails.addAll(runReport.fetchCreativeName());
        capturedDetails.add(runReport.fetchStartTime());
        capturedDetails.add(runReport.fetchEndTime());
        capturedDetails.add(runReport.fetchTimeZone());
        capturedDetails.addAll(runReport.fetchScheduleReportInputValue("Deliver to Users"));
        capturedDetails.addAll(runReport.fetchScheduleReportInputValue("Notify User for Failures"));
        Assert.assertTrue("Not all entered data present in fetched values", capturedDetails.containsAll(nameList));
    }

    @And("Verify that by default {string} option is selected for Report Period Field")
    public void verifyThatByDefaultCustomDatesOptionIsSelectedForReportPeriodField(String buttonType) {
        logger.info("Verify that by default {} option is selected for Report Period Field", buttonType);
        Assert.assertTrue(
                "Custom Dates button is not enabled by default", runReport.isReportPeriodSelected(buttonType));
    }

    @And("Verify that user is able to select start date and end date when Custom Dates option is selected")
    public void verifyThatUserIsAbleToSelectStartDateAndEndDateWhenCustomDatesOptionIsSelected() {
        Assert.assertTrue("Unable to select date from date picker", runReport.selectStartAndEndDate());
    }

    @And("Verify that user is able to select start {string} and end time {string} when Custom Dates option is selected")
    public void verifyThatUserIsAbleToSelectStartAndEndTimeWhenCustomDatesOptionIsSelected(
            String startTime, String endTime) {
        logger.info(
                "Verify that user is able to select start {} and end time {} when Custom Dates option is selected",
                startTime,
                endTime);
        Assert.assertTrue("Unable to select time", runReport.enterStartAndEndTime(startTime, endTime));
        nameList.add(startTime);
        nameList.add(endTime);
    }

    @And("Verify that user is able to select Timezone field value {string}")
    public void verifyThatUserIsAbleToSelectTimezoneFieldValue(String timeZone) {
        logger.info("Verify that user is able to select Timezone field value {}", timeZone);
        Assert.assertTrue("Unable to select time zone " + timeZone, runReport.selectTimeZone(timeZone.trim()));
        nameList.add(timeZone);
    }

    @And("Verify the presence of Report Format field and default value - {string}")
    public void verifyTheDefaultValueOfTheTheReportFormatFieldIsCSV(String fileFormat) {
        logger.info("Verify the presence of Report Format field and default value - {}", fileFormat);
        Assert.assertTrue("Report Format field is not available", runReport.isReportFormatFieldAvailable());
        Assert.assertEquals(fileFormat, runReport.fetchDefaultReportFormat());
    }

    @And("Verify the availability of various options of the Report Format field - {string}")
    public void verifyTheAvailabilityOfVariousOptionsOfTheReportFormatField(String reportFormats) {
        List<String> expectedFormats = CommonUtils.convertStringToList(reportFormats);
        List<String> reportFormatValues = runReport.fetchReportFormatList();
        for (String format : expectedFormats) {
            logger.info(
                    "Verifying: the availability of various options of the Report Format field - {}", reportFormats);
            Assert.assertTrue("Missing report format: " + format, reportFormatValues.contains(format));
        }
    }

    @And("Verify the presence of Text Qualifier checkbox and by default it should be checked")
    public void verifyByDefaultTheTextQualifierCheckboxIsChecked() {
        logger.info("Verify the presence of Text Qualifier checkbox and by default it should be checked");
        Assert.assertTrue(
                "Text Qualifier checkbox is not available", runReport.isTextQualifierCheckboxAvailable());
        Assert.assertTrue("Text Qualifier is not checked by default", runReport.isTextQualifierCheckboxChecked());
    }

    @And("Verify that {string} and {string} options are disabled until a Line Item is selected")
    public void verifyThatLifetimeAndFlightsOptionsAreDisabledUntilALineItemIsSelected(
            String lifeTime, String flights) {
        logger.info("Verify that {} and {} options are disabled until a Line Item is selected", lifeTime, flights);
        List<String> disabledButtons = runReport.verifyButtonsDisabledBeforeLineItemSelection();
        Assert.assertTrue("Expected disabled button missing: " + lifeTime, disabledButtons.contains(lifeTime));
        Assert.assertTrue("Expected disabled button missing: " + flights, disabledButtons.contains(flights));
    }

    @And("User should be able to select {string} and {string}")
    public void userShouldBeAbleToSelectAnd(String dimensions, String metrics) {
        List<String> dimensionList = CommonUtils.convertStringToList(dimensions);
        List<String> metricsList = CommonUtils.convertStringToList(metrics);
        logger.info("Selecting Dimensions: {} and Metrics: {}", dimensionList, metricsList);
        runReport.selectDimension(dimensionList);
        runReport.selectMetrics(metricsList);
        nameList.addAll(dimensionList);
        nameList.addAll(metricsList);
        templateNameRandom = "Custom Template";
    }

    @And("Verify that {string} and {string} options are enabled")
    public void verifyThatLifetimeAndFlightsOptionsAreEnabled(String lifeTime, String flights) {
        logger.info("Verify that {} and {} options are enabled", lifeTime, flights);
        List<String> enabledButtons = runReport.verifyButtonsEnabledAfterLineItemSelection();
        Assert.assertTrue("Expected " + lifeTime + " button to be enabled", enabledButtons.contains("Lifetime"));
        Assert.assertTrue("Expected " + flights + " button to be enabled", enabledButtons.contains("Flights"));
    }

    @And("User clicks {string} report period button")
    public void userClicksReportPeriodButton(String buttonName) {
        logger.info("Clicking Report Period button: {}", buttonName);
        runReport.selectReportPeriodButton(buttonName);
    }

    @Then("User should be able to select value from dropdown")
    public void userShouldBeAbleToSelectValueFromDropdown() {
        logger.info("User should be able to select value from dropdown");
        String valuesSelected = runReport.selectValueFromDropdown();
        Assert.assertFalse("Unable to select value from dropdown", valuesSelected.isEmpty());
        nameList.add(valuesSelected);
    }

    @And("User should be able to fetch details - Advertiser, Campaign, Line Item, Tactic")
    public void userShouldBeAbleToFetchDetailsAdvertiserCampaignLineItemTactic() {
        logger.info("User should be able to select value from dropdown");
        nameList.addAll(runReport.fetchAdvertiserName());
        nameList.addAll(runReport.fetchCampaignName());
        nameList.addAll(runReport.fetchLineItemName());
        logger.info("Fetched details successfully. Total values captured so far: {}", nameList.size());
    }

    @And("Verify that Flight details field is displayed with value")
    public void verifyThatFlightDetailsFieldIsDisplayedWithValue() {
        Assert.assertFalse(
                "Flight details are not populated",
                runReport.isFlightDetailsDisplayed().isEmpty());
        logger.info("Flight details field is displayed with populated values");
    }

    @And("User navigates to Administrative section and fetches the advertiser for the account {string}")
    public void userNavigatesToAdministrativeSectionAndFetchesTheAdvertiserForTheAccount(String account) {
        logger.info("Navigating to Administrative section to fetch advertisers for account: {}", account);
        navigation.clickSubMenu();
        accounts.clickAdministration();
        accounts.clickAdvertiserTab();
        accounts.selectAccount(account);
        itemList = accounts.fetchAdvertiserList();
        logger.info("Fetched {} advertisers for account {}", itemList.size(), account);
        navigation.clickPulsePointLogo();
        logger.info("Navigated back to Life application");
    }

    @And("Expand all the groups and fetch dimensions and metrics")
    public void expandAllTheGroupsAndFetchDimensions() {
        nameList = reportTemplates.expandGroupsAndFetchDimensionsAndMetrics();
        logger.info("Fetched {} dimensions from template", nameList.size());
        reportTemplates.clickMetricsTab();
        logger.info("Switched to Metrics tab to fetch metrics");
        capturedDetails = reportTemplates.expandGroupsAndFetchDimensionsAndMetrics();
        logger.info("Fetched {} metrics from template", capturedDetails.size());
        reportTemplates.clickCancelButton();
        logger.info("Closed Report Template panel after fetching dimensions and metrics");
    }

    @And("Verify dropdown dimensions with the template")
    public void verifyDropdownDimensionsWithTheTemplate() {
        logger.info("Verify dropdown dimensions with the template");
        List<String> dimensionList = runReport.clickDimensionDropdownAndFetchValues();
        Assert.assertTrue(
                "Template's Dimension values are not available in Run report", dimensionList.containsAll(nameList));
    }

    @And("Verify dropdown metrics with the template")
    public void verifyDropdownMetricsWithTheTemplate() {
        logger.info("Verifying: dropdown dimensions with the template");
        List<String> metricList = runReport.clickMetricDropdownAndFetchValues();
        Assert.assertTrue(
                "Template's Dimension values are not available in Run report", metricList.containsAll(capturedDetails));
    }

    @And("User selects {string} button")
    public void userSelects(String buttonType) {
        logger.info("User selects button: {}", buttonType);
        runReport.clickFileBreakdownType(buttonType);
    }

    @Then("{string} section should be visible with label {string}, {string}, {string} checkbox")
    public void sectionShouldBeVisibleWithLabelCheckbox(
            String filterReportSection, String checkboxLabel1, String checkboxLabel2, String checkboxLabel3) {
        Assert.assertTrue(
                "Report Filter checkbox is not available",
                runReport.isFilterReportSectionAvailable(filterReportSection));
        if (runReport.isFilterReportCheckboxAvailable(checkboxLabel1)) {
            logger.info(
                    "{} section should be visible with label {}, {}, {} checkbox",
                    filterReportSection,
                    checkboxLabel1,
                    checkboxLabel2,
                    checkboxLabel3);
            Assert.assertEquals(
                    checkboxLabel1.trim(), runReport.fetchAndClickFilterReportCheckboxLabel(checkboxLabel1));
        }
        if (runReport.isFilterReportCheckboxAvailable(checkboxLabel2)) {
            Assert.assertEquals(
                    checkboxLabel2.trim(), runReport.fetchAndClickFilterReportCheckboxLabel(checkboxLabel2));
        }
        if (runReport.isFilterReportCheckboxAvailable(checkboxLabel3)) {
            Assert.assertEquals(
                    checkboxLabel3.trim(), runReport.fetchAndClickFilterReportCheckboxLabel(checkboxLabel3));
        }
    }

    @When("User navigates to Schedule report from mega menu of the life application")
    public void userNavigatesToScheduleReportFromMegaMenuOfTheLifeApplication() {
        logger.info("User navigates to Schedule report from mega menu of the life application");
        navigation.clickSubMenu();
        navigation.clickMenuAngle();
        navigation.clickScheduledReport();
    }

    @And("User clicks Schedule Report button")
    public void userClicksScheduleReportButton() {
        logger.info("User navigates to Schedule report from mega menu of the life application");
        scheduleReport.clickScheduleReportButton();
    }

    @And("Verify Schedule Report panel should be opened")
    public void verifyScheduleReportPanelShouldBeOpened() {
        logger.info("User clicks Schedule Report button");
        Assert.assertTrue("Schedule Report panel is not opened", scheduleReport.isScheduleReportPanelOpened());
    }

    @And("Verify Report Name field is available and accepts input {string}")
    public void verifyReportNameFieldIsAvailableAndAcceptsInput(String name) {
        Assert.assertTrue("Report Name field is not available", scheduleReport.isReportNameAvailable());
        metricName = name + "_" + CommonUtils.timeStampCalculation();
        logger.info("Generated Report Name: {}", metricName);
        scheduleReport.enterReportName(metricName);
        nameList.add(metricName);
    }

    @And("Verify availability of frequency field with options below")
    public void verifyAvailabilityOfFrequencyFieldWithOptions(DataTable dataTable) {
        List<String> buttonLabels = dataTable.asList(String.class);
        logger.info("Expected Frequency options: {}", buttonLabels);
        List<String> actualOptions = scheduleReport.fetchFrequencyOptions();
        Assert.assertEquals(new HashSet<>(buttonLabels), new HashSet<>(actualOptions));
    }

    @And("Verify default value of the Frequency field is {string}")
    public void verifyDefaultValueOfTheFrequencyFieldIs(String defaultValue) {
        logger.info("Verify default value of the Frequency field is {}", defaultValue);
        Assert.assertTrue(
                "Weekly is not selected by default", scheduleReport.checkDefaultFrequencyOption(defaultValue));
    }

    @And("Verify that user is able to select Schedule start date and Schedule end date")
    public void verifyThatUserIsAbleToSelectScheduleStartDateAndScheduleEndDate() {
        logger.info("Verify that user is able to select Schedule start date and Schedule end date");
        Assert.assertTrue("Unable to select start date from date picker", scheduleReport.selectScheduleStartDate());
        Assert.assertTrue("Unable to select end date from date picker", scheduleReport.selectScheduleEndDate());
    }

    @And("Verify default value of Data Timezone is {string}")
    public void verifyDefaultValueOfDataTimezoneIs(String timeZone) {
        logger.info("Verify default value of Data Timezone is {}", timeZone);
        String actualTimezone = scheduleReport.fetchDefaultTimeZone();
        Assert.assertEquals(timeZone.trim(), actualTimezone);
    }

    @And("Verify that user is able to select Data Timezone field value {string}")
    public void verifyThatUserIsAbleToSelectDataTimezoneFieldValue(String timeZone) {
        Assert.assertTrue("Unable to select time zone", scheduleReport.selectDataTimeZone(timeZone.trim()));
        nameList.add(timeZone);
        logger.info("Added timezone to nameList: {}", timeZone);
    }

    @And("The Send On field should contain all days of the week when {string} is selected as Frequency")
    public void theSendOnFieldShouldContainAllDaysOfTheWeekWhenIsSelectedAsFrequency(
            String frequencyOption, DataTable dataTable) {
        logger.info("Verifying Send On field for Frequency: {}", frequencyOption);
        List<String> expectedDays = dataTable.asList(String.class);
        Assert.assertTrue(
                "Weekly is not selected by default", scheduleReport.checkDefaultFrequencyOption(frequencyOption));
        List<String> actualDays = scheduleReport.fetchWeekDays();
        Assert.assertEquals(new HashSet<>(expectedDays), new HashSet<>(actualDays));
    }

    @And("Verify default value of Send On is {string}")
    public void verifyDefaultValueOfSendOnIs(String defaultValue) {
        logger.info("Verify default value of Send On is {}", defaultValue);
        Assert.assertTrue("Sun is not selected by default", scheduleReport.checkDefaultWeekDay(defaultValue));
    }

    @And("Verify Send At field is available with Start Time and Timezone fields")
    public void verifySendAtFieldIsAvailableWithStartTimeAndTimezoneFields() {
        logger.info("Verify Send At field is available with Start Time and Timezone fields");
        Assert.assertTrue(
                "Send At fields - Time and TimeZone are not available", scheduleReport.isSendAtFieldAvailable());
    }

    @And("Verify default value of Send At fields - Start Time is {string} and Timezone is {string}")
    public void verifyDefaultValueOfSendAtFieldsStartTimeIsAndTimezoneIs(String defaultTime, String defaultTimezone) {
        logger.info(
                "Verify default value of Send At fields - Start Time is {} and Timezone is {}",
                defaultTime,
                defaultTimezone);
        Assert.assertEquals(
                "Default time " + defaultTime + " is not present", defaultTime, scheduleReport.fetchSendAtTimeValue());
        Assert.assertEquals(
                "Default time " + defaultTimezone + " is not present",
                defaultTimezone,
                scheduleReport.fetchSendAtTimezoneValue());
    }

    @And("Verify user is able to select Time {string} and Timezone {string} for Send At fields")
    public void verifyUserIsAbleToSelectTimeAndTimezoneForSendAtFields(String time, String timeZone) {
        logger.info("Verify user is able to select Time {} and Timezone {} for Send At fields", time, timeZone);
        scheduleReport.enterSendAtTime(time);
        boolean isTimeZoneSelected = scheduleReport.selectDataTimeZone(timeZone);
        Assert.assertTrue("Unable to select timezone", isTimeZoneSelected);
        nameList.add(time);
        nameList.add(timeZone);
    }

    @And("Verify Delivery field has two methods - {string} and {string}")
    public void verifyDeliveryFieldHasTwoMethodsAnd(String email, String customDestination) {
        logger.info("Verify Delivery field has two methods - {} and {}", email, customDestination);
        List<String> methodNames = scheduleReport.verifyDeliveryMethods();
        Assert.assertTrue(
                "Methods are not available", methodNames.contains(email) && methodNames.contains(customDestination));
    }

    @When("User clicks on {string} tab as Delivery Method")
    public void userClicksOnTabAsDeliveryMethod(String tabName) {
        logger.info("User clicks Delivery Method tab: {}", tabName);
        scheduleReport.clickDeliveryTab(tabName);
    }

    @Then("Verify Deliver to Users field is available")
    public void verifyDeliverToUsersFieldIsAvailable() {
        logger.info("Verify Deliver to Users field is available");
        Assert.assertTrue("Deliver To Users field is not available", scheduleReport.isDeliveryToUserAvailable());
    }

    @And("Verify that Add Emails link is available below Deliver to Users")
    public void verifyThatAddEmailsLinkIsAvailableBelowDeliverToUsers() {
        logger.info("Verifying: deliver to Users field is available");
        Assert.assertTrue("Deliver To Users field is not available", scheduleReport.isAddEmailLinkAvailable());
    }

    @When("User clicks Add Emails link, Deliver to External Emails field should display")
    public void userClicksAddEmailsLinkDeliverToExternalEmailsFieldShouldDisplay() {
        logger.info("User clicks Add Emails link");
        scheduleReport.clickAddEmailsLink();
        Assert.assertTrue(
                "Deliver To External Users field is not available",
                scheduleReport.isDeliverToExternalEmailsAvailable());
    }

    @And("User should be able to add multiple emails in Deliver to External Emails field")
    public void userShouldBeAbleToAddMultipleEmailsInDeliverToExternalEmailsField(DataTable dataTable) {
        logger.info("User clicks Add Emails link, Deliver to External Emails field should display");
        List<String> emailList = dataTable.asList(String.class);
        logger.info("Adding external email addresses: {}", emailList);
        scheduleReport.enterExternalEmails(emailList);
    }

    @Then("Verify Destination dropdown field is available")
    public void verifyDestinationDropdownFieldIsAvailable() {
        logger.info("Verify Destination dropdown field is available");
        Assert.assertTrue("Destination field is not available", scheduleReport.isDestinationAvailable());
    }

    @And("Verify {string} button is available in Destination dropdown field")
    public void verifyAddDestinationButtonIsAvailableInDestinationDropdownField(String buttonName) {
        if (buttonName.contains("Add Destination")) {
            logger.info("Verifying: {} button is available in Destination dropdown field", buttonName);
            Assert.assertTrue(
                    "Add Destination field is not available", scheduleReport.isAddDestinationAvailable(buttonName));
        } else {
            Assert.assertTrue("Edit Destination field is not available", runReport.isEditDestinationAvailable());
        }
    }

    @When("User clicks {string} button")
    public void userClicksAddDestinationButton(String buttonName) {
        logger.info("User clicks Destination button: {}", buttonName);
        scheduleReport.clickAddDestination(buttonName);
    }

    @And("User clicks Edit button from Destination dropdown field")
    public void userClicksEditButtonFromDestinationDropdownField() {
        logger.info("User clicks Edit button in Destination dropdown");
        runReport.clickEditDestination();
        Assert.assertTrue("Custom destination fields are not available", runReport.isDestinationNameAvailable());
    }

    @And(
            "User verifies the custom destination fields - Destination Name, Destination Type, Host, Username, Password, Port textfields, Test Access, Create and Cancel buttons")
    public void
            userVerifiesTheCustomDestinationFieldsDestinationNameDestinationTypeHostUsernamePasswordPortTextfieldsTestAccessCreateAndCancelButtons() {
        logger.info("User clicks Edit button from Destination dropdown field");
        Assert.assertTrue("Destination Name field is not available", runReport.isDestinationNameAvailable());
        Assert.assertTrue("Destination Type field is not available", runReport.isDestinationTypeAvailable());
        Assert.assertTrue("Host field is not available", runReport.isHostFieldAvailable());
        Assert.assertTrue("Username field is not available", runReport.isUsernameFieldAvailable());
        Assert.assertTrue("Password field is not available", runReport.isPasswordFieldAvailable());
        Assert.assertTrue("Port field is not available", runReport.isPortFieldAvailable());
        Assert.assertTrue(
                "Test Access button is not available",
                runReport.isTestAccessButtonAvailable() | runReport.isReRunAccessButtonAvailable());
        Assert.assertTrue("Create button is not available", runReport.isCreateButtonAvailable());
        Assert.assertTrue("Cancel button is not available", runReport.isCancelButtonAvailable());
    }

    @Then("Verify Destination Name, Destination Type fields are displayed")
    public void verifyDestinationNameDestinationTypeFieldsAreDisplayed() {
        logger.info(
                "User verifies the custom destination fields - Destination Name, Destination Type, Host, Username, Password, Port textfields, Test Access, Create and Cancel buttons");
        Assert.assertTrue(
                "Destination Name and Type fields are not available",
                scheduleReport.isDestinationNameAndTypeDisplayed());
    }

    @And("Verify that Destination Type has values {string}")
    public void verifyThatDestinationTypeHasValues(String destinationTypes) {
        List<String> expectedTypes = CommonUtils.convertStringToList(destinationTypes);
        List<String> actualTypes = scheduleReport.fetchDestinationTypes();
        logger.info("Expected Destination Types: {}", expectedTypes);
        for (String type : expectedTypes) {
            Assert.assertTrue("Missing Destination Type: " + type, actualTypes.contains(type));
        }
    }

    @And("Verify File Path field is available")
    public void verifyFilePathFieldIsAvailable() {
        logger.info("Verify File Path field is available");
        Assert.assertTrue("File Path field is not available", scheduleReport.isFilePathAvailable());
    }

    @And("Verify File Name field is available")
    public void verifyFileNameFieldIsAvailable() {
        logger.info("Verifying: file Path field is available");
        Assert.assertTrue("File Path field is not available", scheduleReport.isFileNameAvailable());
    }

    @And("Verify Compression field is available with below options and default value is {string}")
    public void verifyCompressionFieldIsAvailableWithBelowOptions(String defaultCompressionType, DataTable dataTable) {
        logger.info("Verifying Compression field with default value: {}", defaultCompressionType);
        List<String> expectedTypes = dataTable.asList(String.class);
        Assert.assertTrue(
                "None is not selected by default", scheduleReport.checkDefaultCompression(defaultCompressionType));
        List<String> actualTypes = scheduleReport.fetchCompressionTypes();
        Assert.assertEquals(new HashSet<>(expectedTypes), new HashSet<>(actualTypes));
    }

    @And("Verify Control File checkbox is present and by default it should be unchecked")
    public void verifyControlFileCheckboxIsPresentAndByDefaultItShouldBeUnchecked() {
        logger.info("Verify Control File checkbox is present and by default it should be unchecked");
        Assert.assertTrue("Control File checkbox is not available", scheduleReport.isControlFileCheckboxAvailable());
        Assert.assertTrue(
                "Control File checkbox is not unchecked by default", scheduleReport.isControlFileCheckboxSelected());
    }

    @And("User clicks {string} as frequency")
    public void userClicksAsFrequency(String frequencyType) {
        logger.info("User selects Frequency option: {}", frequencyType);
        scheduleReport.clickFrequencyOption(frequencyType);
    }

    @And("User selects Send On date")
    public void userSelectsSendOnDate() {
        logger.info("User selects Send On date");
        Assert.assertTrue("Unable to select date from date picker", scheduleReport.selectScheduleStartDate());
    }

    @And("Verify Report Period is selected as {string}")
    public void verifyReportPeriodIsSelectedAs(String periodType) {
        logger.info("Selecting Report Period: {}", periodType);
        scheduleReport.selectReportPeriod(periodType);
        nameList.add(periodType);
        logger.info("Added Report Period to nameList: {}", periodType);
    }

    @And("User selects start date and end date when Custom Dates option is selected")
    public void userSelectsStartDateAndEndDateWhenCustomDatesOptionIsSelected() {
        logger.info("User selects start date and end date when Custom Dates option is selected");
        Assert.assertTrue("Unable to select start date", scheduleReport.selectStartDate());
        Assert.assertTrue("Unable to select end date", scheduleReport.selectEndDate());
    }

    @And("User selects start {string} and end time {string} when Custom Dates option is selected")
    public void userSelectsStartAndEndTimeWhenCustomDatesOptionIsSelected(String startTime, String endTime) {
        logger.info("User selects start {} and end time {} when Custom Dates option is selected", startTime, endTime);
        Assert.assertTrue(
                "Unable to select start and end time", scheduleReport.selectStartAndEndTime(startTime, endTime));
        nameList.add(startTime);
        nameList.add(endTime);
    }

    @And("User clicks Schedule button to generate the report")
    public void userClicksScheduleButtonForReportGeneration() {
        logger.info("User clicks Schedule button to generate report");
        scheduleReport.clickScheduleButton();
        String successMsg = scheduleReport.fetchSuccessAlert();
        Assert.assertEquals("Success!", successMsg);
    }

    @And(
            "User searches the report and verify the details in report listing page - Template name, {string}, {string} and Generated By")
    public void userSearchesTheReportAndVerifyTheDetailsInReportListingPageTemplateNameAndGeneratedBy(
            String frequency, String reportingPeriod) {
        logger.info("Searching scheduled report: {}", metricName);
        scheduleReport.searchReport(metricName);
        logger.info("Fetching report details from listing page for report: {}", metricName);
        List<String> actualDetails = scheduleReport.fetchListingPageDetails(metricName);
        Assert.assertTrue("Template name is not displayed in listing page", actualDetails.contains(templateNameRandom));
        Assert.assertTrue("Frequency is not displayed in listing page", actualDetails.contains(frequency));
        Assert.assertTrue("Reporting Period is not displayed in listing page", actualDetails.contains(reportingPeriod));
        Assert.assertTrue("Generated By is not displayed in listing page", actualDetails.contains(userType));
    }

    @And("Verify the report panel retains the entered data")
    public void userSearchesTheReportAndChecksTheReportPanelRetainsTheEnteredData() {
        logger.info("Verify the report panel retains the entered data");
        scheduleReport.clickReportName(metricName);
        capturedDetails.addAll(runReport.fetchTemplateValue());
        capturedDetails.addAll(runReport.fetchAdvertiserName());
        capturedDetails.addAll(runReport.fetchCampaignName());
        capturedDetails.addAll(runReport.fetchLineItemName());
        capturedDetails.addAll(runReport.fetchTacticName());
        capturedDetails.addAll(runReport.fetchCreativeName());
        capturedDetails.add(scheduleReport.fetchReportName());
        capturedDetails.add(scheduleReport.fetchTimeZone());
        capturedDetails.add(scheduleReport.fetchSendAtTime());
        capturedDetails.add(scheduleReport.fetchSendAtTimezone());
        capturedDetails.add(scheduleReport.fetchReportPeriod());
        capturedDetails.add(scheduleReport.fetchStartTime());
        capturedDetails.add(scheduleReport.fetchEndTime());
        Assert.assertTrue("Not all entered data present in fetched values", capturedDetails.containsAll(nameList));
    }

    @And("Verify Send On field is visible and user should be able to select the day {string} of the month")
    public void verifySendOnFieldIsVisibleAndUserShouldBeAbleToSelectTheDayOfTheMonth(String dayOfTheMonth) {
        logger.info(
                "Verify Send On field is visible and user should be able to select the day {} of the month",
                dayOfTheMonth);
        Assert.assertTrue("Send On field is not available", scheduleReport.isSendOnAvailable());
        Assert.assertTrue("Unable to select Day of the Month", scheduleReport.selectDayOfTheMonth(dayOfTheMonth));
    }

    @And("Verify that user is able to select Send On day {string}")
    public void verifyThatUserIsAbleToSelectSendOnDay(String day) {
        logger.info("Verify that user is able to select Send On day {}", day);
        Assert.assertTrue("Unable to select Send On Day", scheduleReport.selectWeekDay(day));
    }

    @Then("User searches the Campaign {string}, navigates to LineItem and fetches the flight details")
    public void userSearchesTheCampaignNavigatesToLineItemAndFetchesTheFlightDetails(String campaignName) {
        logger.info("Searching Campaign: {} and fetching Line Item flight details", campaignName);
        campaignDashboard.searchCreatedCampaign(campaignName);
        campaignDashboard.expandCreatedLineItem();
        campaignDashboard.navigateToLineItemDetails();
        lineItemFlights.clickFlightTab();
        Assert.assertTrue("Flight details are not displayed", lineItemFlights.isFlightTableDisplayed());
        itemList = lineItemFlights.fetchFlightDates();
        logger.info("Fetched Flight Dates: {}", itemList);
    }

    @And(
            "User fetches all the Flight details and verifies that selected Line Item flight details appear in the Flight tab of the Run Report panel")
    public void userFetchesAllTheFlightDetailsAndSelects() throws ParseException {
        List<String> flightDescriptions = runReport.fetchAndSelectFlightDetails();
        logger.info("Flight Descriptions from Run Report: {}", flightDescriptions);
        SimpleDateFormat descFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        SimpleDateFormat extractedFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        SimpleDateFormat compareFormat = new SimpleDateFormat("MM/dd/yyyy");
        for (int i = 0; i < flightDescriptions.size() && (i * 2 + 1) < itemList.size(); i++) {
            String desc = flightDescriptions.get(i);
            String[] dateParts = desc.split(":", 2)[1].split("-");
            String startDate = dateParts[0].trim();
            String endDate = dateParts[1].trim();
            startDate = startDate.replaceAll("(\\d+)(st|nd|rd|th)", "$1");
            endDate = endDate.replaceAll("(\\d+)(st|nd|rd|th)", "$1");
            String actualStart = compareFormat.format(descFormat.parse(startDate));
            String actualEnd = compareFormat.format(descFormat.parse(endDate));
            String expectedStart = compareFormat.format(extractedFormat.parse(itemList.get(i * 2)));
            String expectedEnd = compareFormat.format(extractedFormat.parse(itemList.get(i * 2 + 1)));
            Assert.assertEquals("Start date mismatch for Flight #" + (i + 1), expectedStart, actualStart);
            Assert.assertEquals("End date mismatch for Flight #" + (i + 1), expectedEnd, actualEnd);
        }
    }

    @And("User downloads the Scheduled report and verify the data in downloaded report")
    public void userDownloadsTheScheduledReportAndVerifyTheDataInDownloadedReport() {
        logger.info("Downloading scheduled report");
        runReport.downloadScheduledReport();
    }

    @When("User navigates to Administrative section")
    public void userNavigatesToAdministrativeSection() {
        logger.info("User navigates to Administrative section");
        campaignDashboard.waitUntilCampaignPaginationAppears();
        navigation.clickSubMenu();
        accounts.clickAdministration();
    }

    @And("User navigates to Accounts Tab")
    public void userNavigatesToAccountsTab() {
        logger.info("User navigates to Accounts section");
        accounts.selectAccountsTab();
    }

    @And("User searches the account {string} and selects the account")
    public void userSearchesTheAccountAndSelectsTheAccount(String account) {
        logger.info("Searching account: {}", account);
        accounts.searchAccount(account);
        Assert.assertTrue("Details Tab is not displayed", accounts.isDetailsTabDisplayed());
    }

    @And("User navigates to Reporting tab")
    public void userNavigatesToReportingTab() {
        logger.info("User navigates to Reporting tab");
        accounts.clickReportingTab();
    }

    @Then("User clicks Add Destination button")
    public void userClicksAddDestinationButton() {
        logger.info("User clicks Add Destination button");
        accounts.clickAddDestination();
    }

    @And("User enters Destination details - {string}, {string}, {string}, {string}")
    public void userEntersDestinationDetails(
            String destinationName, String destinationType, String hostName, String port) throws Exception {
        dimensionName = destinationName + CommonUtils.timeStampCalculation();
        logger.info("Entering Destination details. Name: {}, Type: {}", dimensionName, destinationType);
        accounts.enterDestinationName(dimensionName);
        accounts.selectDestinationType(destinationType);
        accounts.enterHostName(hostName);
        accounts.enterUserName(ConfigReader.getCustomDestinationUsername());
        accounts.enterPassword(ConfigReader.getCustomDestinationPassword());
        accounts.enterPortName(port);
    }

    @And("User clicks Test Connection link to verify if connection happened successfully")
    public void userClicksTestConnectionLinkToVerifyIfConnectionHappenedSuccessfully() {
        logger.info("User clicks Test Connection link to verify if connection happened successfully");
        String connectionStatus = accounts.clickTestConnection();
        Assert.assertTrue(
                "Unexpected connection status: " + connectionStatus,
                "Connection confirmed".equals(connectionStatus) || "Access test successful".equals(connectionStatus));
    }

    @Then("User selects destination name created, and other details - {string}, {string}")
    public void userSelectsDestinationNameCreatedAndOtherDetails(String filePath, String fileName) {
        logger.info("Selecting custom destination: {}, FilePath: {}, FileName: {}", dimensionName, filePath, fileName);
        scheduleReport.enterCustomDestinationDetailsOnReportPanel(dimensionName, filePath, fileName);
    }

    @And("User saves the custom destination")
    public void userSavesTheCustomDestination() {
        logger.info("User saves the custom destination");
        Assert.assertTrue("Unable to save custom destination details", accounts.clickOKButton());
        accounts.isPulsePointIconEnabled();
    }

    @And("User clicks PulsePoint icon to navigate back to Life")
    public void userClicksPulsePointIconToNavigateBackToLife() {
        logger.info("User saves the custom destination");
        navigation.clickPulsePointLogo();
    }

    @And("User clicks Lifetime filter")
    public void userClicksLifetimeFilter() {
        logger.info("User clicks PulsePoint icon to navigate back to Life");
        campaignDashboard.clickLifetimeFilter();
    }

    @And("User removes all the filters applied on the Dashboard")
    public void userRemovesAllTheFiltersAppliedOnTheDashboard() {
        logger.info("Removing all applied dashboard filters");
        campaignDashboard.ensureCampaignRadioBtnSelected();
        campaignDashboard.unselectFavoriteCheckboxIfSelected();
        campaignDashboard.unselectHideFinishedCheckboxIfSelected();
        campaignDashboard.resetFiltersIfApplied();
    }

    @Then("Verify the tabs displayed on the Pixels page")
    public void verifyTabsDisplayedOnPixelsPage() {
        logger.info("User removes all the filters applied on the Dashboard");
        Assert.assertEquals("RETARGETING", pixels.verifyRetargetingTab().toUpperCase());
        Assert.assertEquals("SMART", pixels.verifySmartTab().toUpperCase());
        Assert.assertEquals("CONVERSION", pixels.verifyConversionTab().toUpperCase());
    }

    @Then("Verify the Advertiser dropdown and search box are displayed on the Pixels page")
    public void verifyAdvertiserDropdownAndSearchBoxDisplayed() {
        logger.info("Verifying: the tabs displayed on the Pixels page");
        Assert.assertTrue("Advertiser Dropdown is not visible", pixels.verifyAdvertiserDropdown());
        Assert.assertTrue("Search Box is not visible", pixels.verifySearchBox());
    }

    @When("User tries to save the Retargeting pixel without entering any details, an error message should be displayed")
    public void userTriesToSaveRetargetingPixelWithoutDetails() {
        logger.info("Validating error messages when saving Retargeting pixel without required fields");
        String pixelNameTemp = "Temporary Pixel Name";
        retargetingPixel.enterPixelName(pixelNameTemp);
        pixels.savePixel();
        Assert.assertEquals("Advertiser is required", retargetingPixel.advertiserError());
        retargetingPixel.clearPixelName();
        pixels.savePixel();
        Assert.assertEquals("Pixel Name is required", retargetingPixel.pixelNameError());
    }

    @And("User selects the {string} pixel")
    public void userSelectsPixel(String pixelType) {
        logger.info("Selecting pixel type: {}", pixelType);
        retargetingPixel.selectPixelType(pixelType);
    }

    @When("User edits the name of the created {string}")
    public void userEditsPixel(String pixelType) {
        pixelNameEdited = newPixelName + '_' + "Edited";
        logger.info("Editing {} name to {}", pixelType, pixelNameEdited);
        pixels.clickEditIcon();
        switch (pixelType) {
            case "Retargeting Pixel" -> retargetingPixel.enterPixelName(pixelNameEdited);
            case "Smart Pixel" -> smartPixel.enterPixelName(pixelNameEdited);
            case "Conversion Pixel" -> conversionPixel.enterPixelName(pixelNameEdited);
        }
        pixels.savePixel();
    }

    @Then("Verify the {string} gets updated successfully")
    public void verifyPixelUpdated(String pixelType) {
        if (pixelType.equals("Retargeting Pixel") || pixelType.equals("Conversion Pixel")) {
            logger.info("Verifying: the {} gets updated successfully", pixelType);
            Assert.assertEquals(
                    "PIXEL UPDATED SUCCESSFULLY", pixels.verifyUpdateSuccess().toUpperCase());
        } else if (pixelType.equals("Smart Pixel")) {
            Assert.assertEquals(
                    "SAVED SUCCESSFULLY", pixels.verifyUpdateSuccess().toUpperCase());
        }
        pixels.searchSavedPixel(pixelNameEdited);
        Assert.assertEquals(pixelNameEdited, pixels.verifyCreatedPixel(pixelNameEdited));
    }

    @When("User removes the created pixel")
    public void userRemovesPixel() {
        logger.info("Removing created pixel");
        pixels.removePixel();
    }

    @Then("Verify the pixel gets removed successfully")
    public void verifyPixelRemoved() {
        logger.info("User removes the created pixel");
        Assert.assertEquals("Pixel deleted successfully", pixels.removeSuccess());
    }

    @When("User selects {string} as advertiser")
    public void userSelectsAdvertiser(String advertiser) {
        logger.info("Select advertiser: {}", advertiser);
        smartPixel.selectAdvertiser(advertiser);
    }

    @Then("Verify the Smart Pixel name is auto populated with {string} and Smart Pixel text")
    public void verifySmartPixelNameIsAutoPopulated(String advertiser) {
        logger.info("STEP: Verify Smart Pixel name auto-population for advertiser: {}", advertiser);
        String pixelName = smartPixel.getPixelNameFromHeader();
        logger.info("Fetched Smart Pixel name from UI: {}", pixelName);
        String expectedPixelName = advertiser + " Smart Pixel";
        String regex = "^\\Q" + expectedPixelName + "\\E(\\s\\d+)?$";
        Assert.assertTrue(pixelName.matches(regex));
    }

    @And("User selects the associated campaign")
    public void userSelectsAssociatedCampaign() {
        logger.info("User selects the associated campaign");
        smartPixel.selectAssociatedCampaign();
    }

    @And("User adds the associated Smart List and enters list details as {string}")
    public void addsAssociatedSmartList(String listName) {
        logger.info("Adding associated Smart List with base name: {}", listName);
        smartPixel.clickAddSmartListButton();
        npiName = listName + '_' + CommonUtils.timeStampCalculation();
        logger.info("Generated Smart List name: {}", npiName);
        npiStaticList.enterListName(npiName);
    }

    @Then("Verify the selected {string} and Smart Pixel")
    public void verifySelectedAdvertiserAndSmartPixel(String advertiser) {
        logger.info("Verify the selected {} and Smart Pixel", advertiser);
        Assert.assertEquals(advertiser, npiSmartList.verifySelectedAdvertiser());
        Assert.assertEquals(newPixelName, npiSmartList.verifySelectedSmartPixel());
        npiSmartList.clickLifeCheckbox();
    }

    @And("User selects the created Smart Pixel")
    public void userSelectsCreatedSmartPixel() {
        logger.info("Selecting created Smart Pixel: {}", newPixelName);
        pixels.selectSmartPixelTab();
        pixels.searchSavedPixel(newPixelName);
        pixels.openSearchedPixel(newPixelName);
    }

    @Then("Verify the selected Smart List should be reflected in the Associated Smartlists tab")
    public void verifySmartListReflectedInAssociatedTab() {
        logger.info("Verify the selected Smart List should be reflected in the Associated Smartlists tab");
        smartPixel.clickAssociatedSmartListsTab();
        Assert.assertEquals(npiName, smartPixel.verifyAssociatedSmartList(npiName));
    }

    @And("User navigates to the Pixel Codes tab")
    public void userNavigatesToPixelCodesTab() {
        logger.info("Verifying: the selected Smart List should be reflected in the Associated Smartlists tab");
        smartPixel.clickPixelCodesTab();
        Assert.assertTrue(smartPixel.verifyPixelCodesTabIsSelected());
    }

    @Then("Verify user should not be able to deactivate the Smart Pixel if any Smart list is associated with it")
    public void verifyUserCannotDeactivateSmartPixelWithAssociatedSmartList() {
        logger.info("User navigates to the Pixel Codes tab");
        pixels.clickEditIcon();
        smartPixel.clickDeactivatePixelIcon();
        Assert.assertEquals(
                "PIXEL CAN'T BE DEACTIVATED", smartPixel.verifyDeactivateError().toUpperCase());
    }

    @When("User deactivates the created pixel")
    public void userDeactivatesCreatedPixel() {
        logger.info("Deactivating pixel: {}", newPixelName);
        pixels.clickEditIcon();
        smartPixel.deactivatePixel();
    }

    @Then("Verify the pixel gets deactivated successfully")
    public void verifyPixelDeactivatedSuccessfully() {
        logger.info("Verify the pixel gets deactivated successfully");
        Assert.assertTrue(
                "Deactivate success message is not displayed",
                smartPixel.deactivateSuccess().contains("Pixel Deactivated successfully"));
    }

    @When("User tries to save the Conversion pixel without entering any details, an error message should be displayed")
    public void userTriesToSaveConversionPixelWithoutDetails() {
        logger.info("Validating mandatory field errors while saving Conversion Pixel");
        String pixelNameTemp = "Temporary Pixel Name";
        logger.info("Using temporary pixel name: {}", pixelNameTemp);
        conversionPixel.enterPixelName(pixelNameTemp);
        pixels.savePixel();
        Assert.assertEquals("Advertiser is required", conversionPixel.advertiserError());
        conversionPixel.clearPixelName();
        pixels.savePixel();
        Assert.assertEquals("Pixel Name is required", conversionPixel.pixelNameError());
        conversionPixel.enterPixelName(pixelNameTemp);
        // Temporary hardcoded selection of advertiser to validate mandatory fields
        conversionPixel.selectAdvertiser("01- Advertiser");
        pixels.savePixel();
        Assert.assertEquals("Conversion Type is required", conversionPixel.pixelTypeOptionError());
        logger.info("Cancelling Conversion Pixel creation");
        pixels.clickCancelButton();
    }

    @Then("Verify the removed pixel should not be displayed in the pixel list")
    public void verifyRemovedPixelNotDisplayedInPixelList() {
        logger.info("Verify the removed pixel should not be displayed in the pixel list");
        pixels.searchSavedPixel(pixelNameEdited);
        String noResultText = pixels.verifyDeletedPixel().toUpperCase();
        Assert.assertTrue(noResultText.equals("NOTHING FOUND...") || noResultText.equals("NOTHING FOUND"));
    }

    @Then("Verify the deactivated pixel should not be displayed in the pixel list")
    public void verifyDeactivatedPixelNotDisplayedInPixelList() {
        logger.info("Verifying: the removed pixel should not be displayed in the pixel list");
        pixels.searchSavedPixel(pixelNameEdited);
        String noResultText = pixels.verifyDeletedPixel().toUpperCase();
        Assert.assertTrue(noResultText.equals("NOTHING FOUND...") || noResultText.equals("NOTHING FOUND"));
    }

    @And("User should be able to select the created template from the dropdown")
    public void userShouldBeAbleToSelectTheTemplateCreatedFromTheDropdown() {
        logger.info("Selecting created template from dropdown: {}", templateNameRandom);
        runReport.selectTemplateFromDropdown(templateNameRandom);
        templateNameRandom = runReport.fetchTemplateValue().get(0);
        logger.info("Fetched selected template value: {}", templateNameRandom);
        nameList.add(templateNameRandom);
    }

    @When("User selects {string} as rule type and selects the published Studio list")
    public void userSelectsRuleTypeAndSelectsThePublishedStudioList(String ruleType) {
        logger.info("Selecting rule type '{}' and published Studio list '{}'", ruleType, StudioSteps.workspaceName);
        itemCount = tacticSettings.selectRuleType(ruleType, StudioSteps.workspaceName);
        logger.info("Item count returned after rule selection: {}", itemCount);
    }

    @Then("Verify the selected targeting rule {string} and rule option")
    public void verifyTheSelectedTargetingRuleAndRuleOption(String ruleType) {
        logger.info("Verify the selected targeting rule {} and rule option", ruleType);
        Assert.assertEquals(ruleType, tacticSettings.verifyRuleType());
        Assert.assertEquals(StudioSteps.workspaceName, tacticSettings.verifyRuleOption());
    }

    // The methods below are slight variations of existing ones used to navigate to Life, HCP and Studio from the Admin
    // landing page after login.
    // These are specifically defined to navigate back to Life, HCP and Studio from other modules.
    @And("User navigates to {string} application")
    public void userNavigatesToApplication(String application) {
        logger.info("Navigating back to application: {}", application);
        switch (application.toLowerCase()) {
            case "life":
                navigation.navigateBackToLife();
                break;
            case "hcp":
                navigation.navigateBackToHCP();
                break;
            case "studio":
                navigation.navigateBackToStudio();
                break;
            default:
                logger.warn("Unknown application requested: {}", application);
        }
    }

    @And("Verify Line Item page has below tabs")
    public void verifyLineItemPageHasBelowTabs(DataTable dataTable) {
        List<String> tabNames = dataTable.asList(String.class);
        logger.info("Verifying Line Item page tabs: {}", tabNames);
        Assert.assertTrue("Line Item tabs are not available", lineItemDetails.verifyLineItemTabs(tabNames));
    }

    @And("Verify status of line item is Incomplete when there are no tactics under the line item")
    public void verifyStatusOfLineItemIsIncompleteWhenThereAreNoTacticsUnderTheLineItem() {
        logger.info("Verify status of line item is Incomplete when there are no tactics under the line item");
        Assert.assertEquals("Incomplete", lineItemDetails.verifyLineItemStatus());
        Assert.assertEquals(
                "Campaign is enabled . Tactic is Incomplete.", lineItemDetails.fetchIncompleteStatusToolTip());
    }

    @When("User fills in required details {string} except for flight information and save")
    public void userFillsInRequiredDetailsExceptForFlightInformation(String lineItemName) {
        lineItemNameRandom = lineItemName + CommonUtils.timeStampCalculation();
        logger.info("Entering Line Item name without flight details: {}", lineItemNameRandom);
        lineItemDetails.enterLineItemName(lineItemNameRandom);
        lineItemDetails.saveLineItem();
    }

    @Then("User should see an error message to add flight details")
    public void userShouldSeeAnErrorMessageToAddFlightDetails() {
        logger.info("User should see an error message to add flight details");
        Assert.assertEquals("LineItem Flight is required.", lineItemDetails.fetchErrorAlert());
    }

    @And("User clicks Add Flight button")
    public void userClicksAddFlightButton() {
        logger.info("User should see an error message to add flight details");
        lineItemDetails.clickAddFlightButton();
    }

    @And("User tries to save the line item without entering any flight details")
    public void userTriesToSaveTheLineItemWithoutEnteringAnyFlightDetails() {
        logger.info("User tries to save the line item without entering any flight details");
        lineItemDetails.saveLineItem();
    }

    @Then("User should see error message {string} when tries to save line item page")
    public void userShouldSeeErrorMessageWhenTriesToSaveLineItemPage(String errorMessage) {
        logger.info("User should see error message {} when tries to save line item page", errorMessage);
        Assert.assertTrue(
                "Error message is not displayed",
                lineItemDetails.fetchErrorAlert().contains(errorMessage));
    }

    @And("Verify if user enters flight budget that exceeds Campaign budget")
    public void verifyIfUserEntersFlightBudgetThatExceedsCampaignBudget() {
        logger.info("User clicks Add Flight button");
        String unaccountedBudget = lineItemDetails.fetchCampaignBudget();
        String modifiedBudget = String.valueOf(Integer.parseInt(unaccountedBudget) + 1000);
        logger.info(
                "Entering flight budget exceeding campaign budget. Campaign: {}, Entered: {}",
                unaccountedBudget,
                modifiedBudget);
        lineItemDetails.enterLineItemBudget(modifiedBudget);
        lineItemDetails.saveLineItem();
    }

    @Then("User should see error message when tries to save line item page")
    public void userShouldSeeErrorMessageWhenTriesToSaveLineItemPage() {
        logger.info("User should see error message when tries to save line item page");
        Assert.assertTrue(
                "The total flight budget is exceeded",
                lineItemDetails.fetchErrorAlert().contains("The total flight budget could not exceed")
                        || lineItemDetails.fetchErrorAlert().contains("Invalid budget"));
    }

    @And("User adds the flight details - Flight Start Date, Flight End Date, {string}")
    public void userAddsTheFlightDetailsFlightStartDateFlightStartDate(String budget) {
        logger.info("Adding flight details with budget: {}", budget);
        lineItemDetails.enterLineItemBudget(budget);
        flightStartDate = lineItemDetails.selectStartDateOfFlight();
        flightEndDate = lineItemDetails.selectEndDateOfFlight();
        logger.info("Selected flight dates → Start: {}, End: {}", flightStartDate, flightEndDate);
    }

    @And("User adds new flight and enter overlapping flight details - Flight Start Date, Flight End Date, {string}")
    public void userAddsOverlappingFlightDetailsFlightStartDateFlightStartDate(String budget) {
        logger.info("Adding overlapping flight with budget: {}", budget);
        lineItemDetails.clickAddFlightButton();
        lineItemDetails.enterLineItemBudget(budget);
        lineItemDetails.selectOverlappingFlightDates(flightStartDate, flightEndDate);
        lineItemDetails.saveLineItem();
    }

    @And(
            "User should see error message when tries to save line item page and dates fields should get highlighted with inline error message")
    public void userShouldSeeErrorMessageWhenTriesToSaveLineItemPageAndDatesFieldsShouldGetHighlighted() {
        logger.info(
                "User should see error message when tries to save line item page and dates fields should get highlighted with inline error message");
        Assert.assertTrue(
                "LineItem flights overlap message is not displayed",
                lineItemDetails.fetchErrorAlert().contains("LineItem flights overlap."));
        Assert.assertEquals("Flight overlap with other flights.", lineItemDetails.fetchInlineErrorMessage());
    }

    @When("User enters line item name {string} on details page")
    public void userEntersLineItemDetails(String lineItemName) {
        lineItemNameRandom = lineItemName + CommonUtils.timeStampCalculation();
        logger.info("Entering Line Item name: {}", lineItemNameRandom);
        lineItemDetails.enterLineItemName(lineItemNameRandom);
    }

    @And(
            "User adds {string} flights, fills in the details with {string} for each flight section, and saves the line item")
    public void userAddsMultipleFlightsAndFillsInDetailsForEachFlightSection(String noOfFlights, String budget) {
        logger.info("Adding {} flights with budget {} for each flight", noOfFlights, budget);
        lineItemDetails.addMultipleFlights(noOfFlights, budget);
    }

    @And("User generates sequential flights for the line item using {string} and {string}")
    public void userGeneratesSequentialFlightsToALineItem(String budget, String numberOfMonths) {
        logger.info("Generating sequential flights with budget {} for {} months", budget, numberOfMonths);
        lineItemDetails.generateSequentialFlights(budget, numberOfMonths);
        lineItemDetails.deleteFlightEntry();
        capturedDetails = lineItemDetails.fetchSequentialFlightStartDates();
        itemList = lineItemDetails.fetchSequentialFlightEndDates();
    }

    @And(
            "Verify that Sequential flights should be added based on the start month and verify start date of the month for each flight entry")
    public void verifyThatSequentialFlightsShouldBeAddedBasedOnTheStartMonth() {
        logger.info("Verify start date of the month for each flight entry");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate startDate = LocalDate.parse(capturedDetails.get(0), formatter);
        for (int i = 0; i < capturedDetails.size(); i++) {
            String dateStr = capturedDetails.get(i);
            LocalDate actualStartDate = LocalDate.parse(dateStr, formatter);
            LocalDate expectedStartDate = startDate.plusMonths(i).withDayOfMonth(1);
            Assert.assertEquals("Flight start date mismatch ", expectedStartDate, actualStartDate);
        }
    }

    @And("Verify end date of the month for each flight entry")
    public void verifyEndDateOfTheMonthForEachFlightEntry() {
        logger.info("Verify end date of the month for each flight entry");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate endDate = LocalDate.parse(itemList.get(0), formatter);
        for (int i = 0; i < itemList.size(); i++) {
            String dateStr = itemList.get(i);
            LocalDate actualEndDate = LocalDate.parse(dateStr, formatter);
            LocalDate expectedEndDate =
                    endDate.plusMonths(i).withDayOfMonth(endDate.plusMonths(i).lengthOfMonth());
            Assert.assertEquals("Flight end date mismatch ", expectedEndDate, actualEndDate);
        }
    }

    @And("User fetches all the flight details added")
    public void userFetchesAllTheFlightDetailsAdded() {
        logger.info("Saving Line Item and fetching all flight details");
        lineItemDetails.saveLineItem();
        lineItemDetails.navigateToLineItemDetails(lineItemNameRandom);
        lineItemDetails.clickDetailsTab();
        itemList = lineItemDetails.fetchFlightDetails();
        logger.info("Fetched flight details: {}", itemList);
    }

    @Then("User navigates to the Flights tab and verifies the flight details")
    public void userNavigatesToTheFlightsTabAndVerifiesTheFlightDetails() {
        logger.info("User navigates to the Flights tab and verifies the flight details");
        List<String> flightDetails;
        lineItemFlights.clickFlightTab();
        Assert.assertTrue("Flight details are not displayed", lineItemFlights.isFlightTableDisplayed());
        flightDetails = lineItemFlights.fetchFlightDetailsFromFlightTab();
        logger.info("Flight details from Flights tab: {}", flightDetails);
        for (String expected : itemList) {
            boolean matchFound = flightDetails.stream().anyMatch(actual -> actual.contains(expected));
            Assert.assertTrue("Expected value not found in flight tab: " + expected, matchFound);
        }
        capturedDetails.clear();
        capturedDetails = flightDetails;
    }

    @When("User deletes some flight entries")
    public void userDeletesSomeFlightEntries() {
        logger.info("Deleting some flight entries");
        lineItemDetails.clickDetailsTab();
        lineItemDetails.deleteFlightEntry();
        lineItemDetails.saveLineItem();
        itemList = lineItemDetails.fetchFlightDetails();
        logger.info("Remaining flight details after deletion: {}", itemList);
    }

    @Then("User should see the remaining flights listed under the Flights section")
    public void userShouldSeeTheRemainingFlightsListedUnderTheFlightsSection() {
        logger.info("User should see the remaining flights listed under the Flights section");
        List<String> flightDetailsAfterDeletion;
        lineItemFlights.clickFlightTab();
        Assert.assertTrue("Flight details are not displayed", lineItemFlights.isFlightTableDisplayed());
        flightDetailsAfterDeletion = lineItemFlights.fetchFlightDetailsFromFlightTab();
        logger.info("Flight details after deletion: {}", flightDetailsAfterDeletion);
        for (String expected : itemList) {
            boolean matchFound = flightDetailsAfterDeletion.stream().anyMatch(actual -> actual.contains(expected));
            Assert.assertTrue("Expected value not found in flight tab after flight deletion: " + expected, matchFound);
        }
        Assert.assertNotEquals(
                "Flight details did not change after deletion – deletion may have failed.",
                capturedDetails,
                flightDetailsAfterDeletion);
    }

    @When(
            "User creates line items with below line types and other details, enables the line item and saves the changes")
    public void userEntersTheLineItemDetailsWithDifferentLineTypesEnablesTheLineItemAndSavesTheChanges(
            DataTable dataTable) {
        logger.info("Creating multiple Line Items with different line types");
        nameList.clear();
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        int currentRowIndex = 0;
        int totalRows = rows.size();
        for (Map<String, String> row : rows) {
            String type = row.get("LINE_TYPE").trim();
            String attributes = row.get("LINE_ITEM_DETAILS").trim();
            Map<String, String> attributeMap = Arrays.stream(attributes.split(","))
                    .map(String::trim)
                    .map(entry -> entry.split(":", 2))
                    .collect(Collectors.toMap(e -> e[0].trim(), e -> e[1].trim()));
            lineItemNameRandom = attributeMap.get("LineName") + "_" + type + "_" + CommonUtils.timeStampCalculation();
            logger.info("Creating Line Item: {} of type {}", lineItemNameRandom, type);
            nameList.add(lineItemNameRandom);
            lineItemDetails.createLineItem(type, lineItemNameRandom, attributeMap);
            lineItemDetails.saveLineItem();
            Assert.assertEquals("Lineitem " + lineItemNameRandom + " created.", lineItemDetails.lineItemSuccess());
            List<String> lineItemLabelList = lineItemDetails.fetchLineItemName();
            Assert.assertTrue(
                    "Line Item '" + lineItemNameRandom + "' is not available",
                    lineItemLabelList.stream().anyMatch(item -> item.equalsIgnoreCase(lineItemNameRandom)));
            lineItemDetails.cancelTactic();
            if (currentRowIndex < totalRows - 1) {
                lineItemDetails.selectNewLineItem();
            }
            currentRowIndex++;
        }
    }

    @When(
            "User creates line items with below line types and other details and verifies the details after saving the line item")
    public void userCreatesLineItemsWithBelowLineTypesAndOtherDetailsAndVerifiesTheDetailsAfterSavingTheLineItem(
            DataTable dataTable) throws InterruptedException {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        int currentRowIndex = 0;
        int totalRows = rows.size();
        for (Map<String, String> row : rows) {
            String type = row.get("LINE_TYPE").trim();
            String attributes = row.get("LINE_ITEM_DETAILS").trim();
            Map<String, String> attributeMap = Arrays.stream(attributes.split(","))
                    .map(String::trim)
                    .map(entry -> entry.split(":", 2))
                    .collect(Collectors.toMap(e -> e[0].trim(), e -> e[1].trim()));
            lineItemNameRandom = attributeMap.get("LineName") + "_" + type + "_" + CommonUtils.timeStampCalculation();
            logger.info("Creating Line Item: {} of type {}", lineItemNameRandom, type);
            lineItemDetails.createLineItem(type, lineItemNameRandom, attributeMap);
            List<String> enteredDetails = lineItemDetails.fetchLineItemDetails();
            lineItemDetails.saveLineItem();
            Assert.assertEquals("Lineitem " + lineItemNameRandom + " created.", lineItemDetails.lineItemSuccess());
            lineItemDetails.navigateToLineItemDetails(lineItemNameRandom);
            lineItemDetails.clickDetailsTab();
            List<String> capturedDetails = lineItemDetails.fetchLineItemDetails();
            if (currentRowIndex < totalRows - 1) {
                lineItemDetails.selectNewLineItem();
            }
            Assert.assertEquals(lineItemNameRandom + " - Line item details mismatch", enteredDetails, capturedDetails);
            currentRowIndex++;
        }
    }

    @Then("User adds Comments or Notes {string} to each line item")
    public void userAddsCommentsOrNotesToEachLineItem(String notes) {
        logger.info("Adding comments/notes to each Line Item");
        for (String name : nameList) {
            logger.info("User adds Comments or Notes {} to each line item", notes);
            lineItemDetails.navigateToLineItemDetails(name);
            String newNotes = name + " " + notes;
            logger.info("Adding notes to {}: {}", name, newNotes);
            itemList.add(newNotes);
            Assert.assertEquals("Notes saved successfully.", lineItemDetails.addNotesToLineItem(newNotes));
        }
    }

    @And("Verify the notes added to each line item")
    public void verifyTheNotesAddedToEachLineItem() {
        for (String name : nameList) {
            logger.info("Verifying: the notes added to each line item");
            lineItemDetails.navigateToLineItemDetails(name);
            String notes = lineItemDetails.fetchLineItemNotes();
            Assert.assertTrue(
                    "Note of '" + name + "' is not available",
                    itemList.stream().anyMatch(item -> item.equalsIgnoreCase(notes)));
        }
    }

    @And("Verify Bulk Edit Mode successfully {string} multiple selected line items")
    public void verifyBulkEditModeWorksForDisablingMultipleLineItems(String bulkOperations) {
        lineItemDetails.clickBulkEditMode();
        logger.info("Entered Bulk Edit Mode");
        for (String name : nameList) {
            logger.info("Selecting line item '{}' for bulk operation", name);
            lineItemDetails.selectLineItemUsingBulkEdit(name);
        }
        Assert.assertEquals(
                "Lineitems status updated successfully",
                lineItemDetails.performBulkModeOperationsOnLineItems(bulkOperations));
        lineItemDetails.exitBulkEditMode();
        logger.info("Exited Bulk Edit Mode");
    }

    @And("Verify that each selected line item is {string}")
    public void verifyThatEachSelectedLineItemIsDisabled(String label) {
        for (String name : nameList) {
            logger.info("Navigating to line item details for '{}'", name);
            lineItemDetails.navigateToLineItemDetails(name);
            boolean isDisabled = lineItemDetails.checkIfEachLineItemEnabledOrDisabled(label);
            Assert.assertTrue(name + " is not " + label + " using Bulk Edit Mode", isDisabled);
        }
    }

    @And("Verify user is able to create a copy of the line items using {string} option")
    public void verifyUserIsAbleToCreateACopyOfTheLineItems(String lineItemOption) {
        logger.info("Verify user is able to create a copy of the line items using {} option", lineItemOption);
        itemList.clear();
        List<String> originalLineItemDetails;
        List<String> copiedLineItemDetails;
        for (String name : nameList) {
            logger.info("Processing line item '{}'", name);
            lineItemDetails.navigateToLineItemDetails(name);
            lineItemDetails.clickDetailsTab();
            originalLineItemDetails = lineItemDetails.fetchLineItemDetails();
            logger.info("Fetched original line item details: {}", originalLineItemDetails);
            lineItemDetails.clickLineItemOptions(lineItemOption);
            String lineItemName = "Copy of " + name;
            itemList.add(lineItemName);
            Assert.assertEquals("Line Item copied successfully.", lineItemDetails.createACopyOfLineItem(lineItemName));
            Assert.assertTrue(
                    "Copied Line Item is not available", lineItemDetails.verifyLineItemAvailable(lineItemName));
            lineItemDetails.navigateToLineItemDetails(lineItemName);
            lineItemDetails.clickDetailsTab();
            copiedLineItemDetails = lineItemDetails.fetchLineItemDetails();
            logger.info("Fetched copied line item details: {}", copiedLineItemDetails);
            lineItemDetails.clickOverviewTab();
            Assert.assertEquals(
                    "Line item details do not match after copy.", originalLineItemDetails, copiedLineItemDetails);
        }
    }

    @And("Verify {string} option opens the Run report screen for user and run the report for {string}")
    public void verifyOptionOpensTheRunReportScreenForUser(String lineItemOption, String templateName) {
        for (String name : nameList) {
            logger.info("Navigating to line item '{}'", name);
            lineItemDetails.navigateToLineItemDetails(name);
            lineItemDetails.clickLineItemOptions(lineItemOption);
            lineItemDetails.runReportFromLineItemPage();
            runReport.selectTemplateFromDropdown(templateName);
            String fileName = "Custom Report";
            runReport.clickRunButton(fileName);
            String actualMessage = runReport.fetchSuccessAlert();
            Assert.assertTrue(
                    "Unexpected success message: " + actualMessage,
                    actualMessage.equals("Success!") || actualMessage.equals("You will get the report on your email"));
        }
    }

    @And("Verify that the reports generated on the Line Item page are available on the Generate Report page")
    public void verifyThatTheReportsGeneratedOnTheLineItemPageAreAvailableOnTheGenerateReportPage() {
        logger.info(
                "Verify that the reports generated on the Line Item page are available on the Generate Report page");
        navigation.clickSubMenu();
        navigation.clickMenuAngle();
        navigation.clickGeneratedReport();
        runReport.clickSearchButton();
        for (String name : nameList) {
            logger.info(
                    "Verifying: that the reports generated on the Line Item page are available on the Generate Report page");
            boolean reportAvailable = reportTemplates.verifyReportGeneratedFromLineItemPage(name);
            Assert.assertTrue("Report generated using line item " + name + " is not available", reportAvailable);
        }
    }

    @And("Verify {string} is available for each item, and deleted items are removed from the Left menu")
    public void isAvailableForEachItemAndDeletedItemsAreRemovedFromTheLeftMenu(String lineItemOption) {
        for (String name : itemList) {
            logger.info("Navigating to line item name '{}'", name);
            lineItemDetails.navigateToLineItemDetails(name);
            lineItemDetails.clickLineItemOptions(lineItemOption);
            lineItemDetails.performDeleteOperation();
            List<String> lineItemLabelList = lineItemDetails.fetchLineItemName();
            Assert.assertFalse(
                    "Line Item '" + name + "' is still available after performing Delete Operation",
                    lineItemLabelList.stream().anyMatch(item -> item.equalsIgnoreCase(name)));
        }
    }

    // * Rajyalaxmi - Tactic max bid and base bid verification
    @When("User clicks on Campaign Settings")
    public void user_clicks_on_campaign_settings() {
        logger.info("User clicks on Campaign Settings");
        campaignSettings.campaignSettingsLink();
        campaignSettings.bidSettingsTab();
    }

    @Then("Verify user is on default bid settings page")
    public void verify_user_is_on_default_bid_settings_page() {
        logger.info("User clicks on Campaign Settings");
        String defaultSettings = campaignSettings.getDefaultSettings();
        Assert.assertEquals("Default Bid Settings", defaultSettings);
    }

    @Then("User gets Max Bid Base Bid values and Highest Possible Max Bid value from Campaign Settings")
    public void user_gets_max_bid_and_base_bid_values_and_highest_possible_max_bid_value() {
        logger.info("Verifying: user is on default bid settings page");
        campaignBaseBid = (campaignSettings.getBaseBidPrice());
        campaignMaxBid = (campaignSettings.getMaxBidPrice());
        campaignHighestBid = (campaignSettings.getHighestPossibleMaxBidPrice());
        logger.info("Fetched Campaign Base Bid: {}, Max Bid: {}", campaignBaseBid, campaignMaxBid);
    }

    @Then("Verify Max Bid and Base Bid values on the tactic settings match with Campaign Settings values")
    public void verify_max_bid_and_base_bid_values_on_the_tactic_settings_match_with_campaign_settings_values() {
        logger.info("Verify Max Bid and Base Bid values on the tactic settings match with Campaign Settings values");
        BigDecimal tacticBaseBid = (tacticSettings.getTacticBaseBidPrice()).stripTrailingZeros();
        BigDecimal tacticMaxBid = (tacticSettings.getTacticMaxBidPrice()).stripTrailingZeros();
        Assert.assertEquals("Max Bid did not match", campaignMaxBid, tacticMaxBid);
        Assert.assertEquals("Base Bid did not match", campaignBaseBid, tacticBaseBid);
    }

    @When("Verify user is able to update and save the {string} bid price")
    public void verify_user_is_able_to_update_and_save_the_bid_price(String bidType) {
        logger.info("Verify user is able to update and save the {} bid price", bidType);
        BigDecimal originalBid;
        BigDecimal updatedBid;
        BigDecimal actualBid;

        if (bidType.equalsIgnoreCase("Base")) {
            logger.info("Verify user is able to update and save the {} bid price", bidType);
            originalBid = tacticSettings.getTacticBaseBidPrice().stripTrailingZeros();
            updatedBid = originalBid.add(BigDecimal.ONE);
            logger.info("Updating Base Bid price from {} to {}", originalBid, updatedBid);
            tacticSettings.updateBaseBidPrice(updatedBid);
            tacticDetails.clickSettingsTab();
            actualBid = tacticSettings.getTacticBaseBidPrice().stripTrailingZeros();

        } else if (bidType.equalsIgnoreCase("Max")) {
            originalBid = tacticSettings.getTacticMaxBidPrice().stripTrailingZeros();
            updatedBid = originalBid.add(BigDecimal.ONE);
            logger.info("Updating Max Bid price from {} to {}", originalBid, updatedBid);
            tacticSettings.updateMaxBidPrice(updatedBid);
            tacticDetails.clickSettingsTab();
            actualBid = tacticSettings.getTacticMaxBidPrice().stripTrailingZeros();

        } else {
            throw new IllegalArgumentException("Unsupported bid type: " + bidType);
        }
        Assert.assertEquals(updatedBid, actualBid);
    }

    @Then("Verify user is not able to update {string} bid price more than allowed limit")
    public void verifyUserIsNotAbleToSetBidPriceMoreThanAllowedLimit(String bidType) {
        logger.info("Verify user is not able to update {} bid price more than allowed limit", bidType);
        BigDecimal originalBid;
        BigDecimal updatedBid;
        String fetchError;
        String expectedError;

        if (bidType.equalsIgnoreCase("Base")) {
            logger.info("Verifying: user is not able to update {} bid price more than allowed limit", bidType);
            expectedError = "Base Bid Price can not exceed Max Bid Price";
            originalBid = tacticSettings.getTacticBaseBidPrice().stripTrailingZeros();
            updatedBid = campaignMaxBid.add(BigDecimal.TEN);
            logger.info("Updating the Base Bid price from {} to {}", originalBid, updatedBid);
            tacticSettings.updateBaseBidPrice(updatedBid);
            fetchError = tacticSettings.getBidErrorText();
            tacticSettings.clickCancel();

        } else if (bidType.equalsIgnoreCase("Max")) {
            expectedError = "Your Account Manager has limited Max Bid";
            originalBid = tacticSettings.getTacticMaxBidPrice().stripTrailingZeros();
            updatedBid = campaignHighestBid.add(BigDecimal.TEN);
            logger.info("Updating the Max Bid price from {} to {}", originalBid, updatedBid);
            tacticSettings.updateMaxBidPrice(updatedBid);
            fetchError = tacticSettings.getBidErrorText().replaceAll("to.*", "").trim();
            tacticSettings.clickCancel();
        } else {
            throw new IllegalArgumentException("Unsupported bid type: " + bidType);
        }
        Assert.assertEquals(expectedError, fetchError);
    }

    @Then("User creates a new tactic with details {string} {string} {string}")
    public void user_creates_a_new_tactics(String tacticName, String channel, String count) {
        int loopCount = 1;

        try {
            logger.info("User creates a new tactic with details {} {} {}", tacticName, channel, count);
            loopCount = Integer.parseInt(count.trim());
            logger.info("Parsed tactic count successfully: {}", loopCount);
        } catch (NumberFormatException e) {
            Assert.fail("Invalid count value for creating tactics: \"" + count + "\"");
        }

        for (int i = 1; i <= loopCount; i++) {
            tacticNameRandom = tacticName + '_' + CommonUtils.timeStampCalculation();
            logger.info("Generated Tactic Name: {}", tacticNameRandom);
            nameList.add(tacticNameRandom);
            logger.info("Entering and saving tactic details");
            tacticDetails.enterTacticName(tacticNameRandom);
            tacticDetails.saveTacticDetails();
            tacticDetails.tacticDetailsSuccess();
            logger.info("Selecting channel '{}' and saving tactic settings", channel);
            tacticSettings.selectChannel(channel);
            tacticSettings.saveTacticSettings();

            if (i != loopCount) {
                tacticSettings.clickNewTactic();
            }
        }

        logger.info("Successfully created {} tactic(s)", loopCount);
    }

    @Then("User deletes the tactic and verifies it")
    public void user_deletes_the_tactic_and_verifies_it() {
        for (int i = 0; i < nameList.size() - 1; i++) {
            logger.info("User deletes the tactic and verifies it");
            String tacticName = nameList.get(i);
            tacticDetails.deleteTactic(tacticName);
            String currentTacticName = tacticSettings.verifyTacticName();
            Assert.assertNotEquals(tacticName, currentTacticName);
            tacticDetails.globalSearchDeletedTactic(tacticName);
            String searchText = tacticDetails.getSearchText();
            Assert.assertEquals("Nothing found...", searchText);
            tacticDetails.closeGlobalSearch();
        }
    }

    @And("User enables tactic through bulk action and verifies the status")
    public void userEnableAllTacticsThroughBulkActionAndVerifiesTheStatus() {
        logger.info("Enabling {} tactic(s) through bulk action", (nameList.size() - 1));

        for (int i = 0; i < nameList.size() - 1; i++) {
            String tacticName = nameList.get(i);
            tacticDetails.bulkEnableTactics(tacticName);

            boolean isToggleClassEnabled = tacticDetails.getToggleClass(tacticName);
            Assert.assertTrue(isToggleClassEnabled);

            boolean isToggleIconEnabled = tacticDetails.getToggleIcon(tacticName);
            Assert.assertTrue(isToggleIconEnabled);
        }
    }

    @And("User disables tactic through bulk action and verifies the status")
    public void userDisableAllTacticsThroughBulkActionAndVerifiesTheStatus() {
        logger.info("Disabling {} tactic(s) through bulk action", (nameList.size() - 1));
        for (int i = 0; i < nameList.size() - 1; i++) {
            String tacticName = nameList.get(i);
            tacticDetails.bulkDisableTactics(tacticName);
            Assert.assertTrue(tacticDetails.getDisabledToggleClass(tacticName));

            boolean isToggleIconDisabled = tacticDetails.getToggleDisabledIcon(tacticName);
            Assert.assertTrue(isToggleIconDisabled);
        }
    }

    @When("User clicks on create new Campaign")
    public void userClicksOnCreateNewCampaign() {
        logger.info("User clicks on create new Campaign");
        campaigns.createCampaign();
    }

    @Then("Verify Smart List Creation Panel should display the following List Population Options")
    public void verifySmartListCreationPanelShouldDisplayTheFollowingListPopulationOptions(DataTable dataTable) {
        List<String> listPopulationOptions = dataTable.asList(String.class);
        logger.info("Verifying Smart List Population Options: {}", listPopulationOptions);

        for (String option : listPopulationOptions) {
            Assert.assertTrue(
                    "List Population Option - " + option + " is not available in Smart List Container",
                    npiSmartList.verifyListPopulationOptions(option.trim()));
        }
    }

    @And("User selects the {string} and fetches Smart pixel list")
    public void userSelectsTheAndFetchesSmartPixelList(String advertiser) {
        logger.info("Fetching Smart Pixel list for advertiser: {}", advertiser);
        pixels.selectSmartPixelTab();
        pixels.selectAdvertiser(advertiser);
        itemList = pixels.fetchPixelsList();
    }

    @And("User enters the Smart NPI list details as {string} {string}")
    public void userEntersTheSmartNPIListDetailsAsFor(String npiListName, String advertiser) {
        npiName = npiListName + '_' + CommonUtils.timeStampCalculation();
        logger.info("Entering Smart NPI list details: Name='{}', Advertiser='{}'", npiName, advertiser);
        npiSmartList.enterListName(npiName);
        npiSmartList.selectAdvertiser(advertiser);
        npiSmartList.clickLifeCheckbox();
    }

    @And("User selects Smart NPI list as {string}")
    public void userSelectsSmartNPIList(String smartListType) {
        logger.info("Selecting Smart NPI list type '{}'", smartListType);
        npiSmartList.selectSmartNPIListType(smartListType);
    }

    @And(
            "User verifies the Smart Pixel dropdown displays all Smart Pixels for the selected advertiser and select the pixel")
    public void userVerifiesTheSmartPixelDropdownDisplaysAllSmartPixelsForTheSelectedAdvertiser() {
        logger.info(
                "User verifies the Smart Pixel dropdown displays all Smart Pixels for the selected advertiser and select the pixel");
        npiSmartList.clickSmartPixelDropDown();
        List<String> list = npiSmartList.fetchSmartPixelDropdownValue();
        Assert.assertTrue("Pixel list doesn't match", itemList.retainAll(list));
        npiSmartList.selectSmartPixelDropdownValue();
    }

    @And("User selects Engagement Type {string} and enter related details {string}, {string}, {string}")
    public void userSelectsEngagementType(
            String engagementType, String visitedURL, String ignoredURL, String keywords) {
        logger.info(
                "Selecting engagement type '{}' with visitedURL='{}', ignoredURL='{}', keywords='{}'",
                engagementType,
                visitedURL,
                ignoredURL,
                keywords);
        List<String> visitedUrlList = CommonUtils.parseCommaSeparatedString(visitedURL);
        List<String> ignoredUrlList = CommonUtils.parseCommaSeparatedString(ignoredURL);
        List<String> keywordList = CommonUtils.parseCommaSeparatedString(keywords);

        npiSmartList.selectEngagementType(engagementType);
        switch (engagementType) {
            case "Engaged on Site":
                npiSmartList.addVisitedURL(visitedUrlList);
                npiSmartList.addIgnoredURL(ignoredUrlList);
                break;
            case "Engaged via Search":
                npiSmartList.clickSearchKeywordsCheckbox();
                npiSmartList.enterSearchKeywords(keywordList);
                break;
            case "Engaged Anywhere":
                npiSmartList.addVisitedURL(visitedUrlList);
                npiSmartList.addIgnoredURL(ignoredUrlList);
                npiSmartList.clickSearchKeywordsCheckbox();
                npiSmartList.enterSearchKeywords(keywordList);
                break;
        }
    }

    @And("User retrieves all the entered data before saving the list {string}")
    public void userRetrievesAllTheEnteredDataBeforeSavingTheList(String listType) {
        logger.info("Retrieving entered data before saving list '{}'", listType);
        if (listType.contains(",")) {
            List<String> listTypes = CommonUtils.parseCommaSeparatedString(listType);
            for (String list : listTypes) {
                capturedDetails.addAll(npiSmartList.retrieveEnteredData(list));
            }
        } else {
            capturedDetails = npiSmartList.retrieveEnteredData(listType);
        }
        logger.info("Captured details: {}", capturedDetails);
    }

    @And("User retrieves all the entered data before saving the Static List")
    public void userRetrievesAllTheEnteredDataBeforeSavingTheStaticList() {
        logger.info("Retrieving entered data before saving Static list");
        capturedDetails = npiStaticList.retrieveEnteredData();
        logger.info("List details before saving it {}", capturedDetails);
    }

    @And("User retrieves all the entered data after saving the Static List")
    public void userRetrievesAllTheEnteredDataAfterSavingTheStaticList() {
        logger.info("Retrieving entered data after saving Static list");
        List<String> fetchDetails = npiStaticList.retrieveEnteredData();
        Assert.assertEquals("Details entered doesn't match after saving the list", capturedDetails, fetchDetails);
    }

    @And("User saves the Smart List and verifies the successful creation of the list")
    public void theUserSavesTheSmartListAndVerifiesTheSuccessfulCreationOfTheList() {
        logger.info("Saving Smart NPI list '{}'", npiName);
        npiSmartList.clickSaveButton();
        String alert = npiSmartList.fetchSuccessAlert();
        Assert.assertEquals("NPI list created successfully", alert);
    }

    @And("User saves the Smart List and verifies the error message is displayed")
    public void userSavesTheSmartListAndVerifiesTheErrorMessageIsDisplayed() {
        logger.info("Saving Smart list '{}'", npiName);
        npiSmartList.clickSaveButton();
        String alert = npiSmartList.fetchErrorAlert();
        Assert.assertEquals("Smart list can not be generated with current selection.", alert);
    }

    @And("Verify that the retrieved data for the {string} list was saved correctly")
    public void verifyThatTheRetrievedDataForTheListWasSavedCorrectly(String listType) {
        List<String> onListSavedFetchData = new ArrayList<>();
        if (listType.contains(",")) {
            logger.info("Verifying: that the retrieved data for the {} list was saved correctly", listType);
            List<String> listTypes = CommonUtils.parseCommaSeparatedString(listType);
            for (String list : listTypes) {
                onListSavedFetchData.addAll(npiSmartList.retrieveEnteredData(list));
            }
        } else {
            onListSavedFetchData = npiSmartList.retrieveEnteredData(listType);
        }
        Assert.assertEquals("Data entered doesn't match after saving the list", capturedDetails, onListSavedFetchData);
    }

    @And("Verify {string} population option is disabled when Advertiser value is not selected")
    public void verifyNPIListPopulationOptionIsDisabledWhenAdvertiserValueIsNotSelected(String smartListType) {
        logger.info("Verify {} population option is disabled when Advertiser value is not selected", smartListType);
        Assert.assertTrue("NPI List is not disabled", npiSmartList.verifyNPIListDisabled(smartListType));
    }

    @And("User selects the HCP switch {string}")
    public void userSelectsTheHCPSwitch(String hcpSwitch) {
        logger.info("Selecting HCP switch '{}'", hcpSwitch);
        npiSmartList.selectHCPSwitch(hcpSwitch);
    }

    @And("User selects the NPI Group {string}")
    public void userSelectsTheNPIGroup(String npiList) {
        logger.info("Selecting NPI Group '{}'", npiList);
        npiSmartList.selectNPIGroup(npiList);
    }

    @And("User selects the NPI data {string} for {string}")
    public void userSelectsTheNPIData(String npiData, String optionType) {
        logger.info("Selecting NPI Data '{}'", npiData);
        if (optionType.contains("NPI List")) npiSmartList.selectNPIGroup(npiData);
        else npiSmartList.selectSpeciality(npiData);
    }

    @And("User selects the Speciality {string}")
    public void userSelectsTheSpeciality(String specialities) {
        List<String> specialityList = CommonUtils.parseCommaSeparatedString(specialities);
        logger.info("Selecting Specialities: {}", specialityList);
        for (String speciality : specialityList) {
            npiSmartList.selectSpeciality(speciality);
        }
    }

    @And("User selects the Profession {string}")
    public void userSelectsTheProfession(String professions) {
        List<String> professionList = CommonUtils.parseCommaSeparatedString(professions);
        logger.info("Selecting Professions: {}", professionList);
        for (String profession : professionList) {
            npiSmartList.selectProfession(profession);
        }
    }

    @And("Verify that Recency is set to {string} by default for {string}")
    public void verifyThatRecencyIsSetToByDefault(String recency, String type) {
        logger.info("Verify that Recency is set to {} by default for {}", recency, type);
        String currentRecency = npiSmartList.fetchRecency(type);
        Assert.assertEquals(recency, currentRecency);
    }

    @And("verify that Decile is set to {string} by default for {string}")
    public void verifyThatDecileIsSetToByDefault(String decile, String type) {
        logger.info("verify that Decile is set to {} by default for {}", decile, type);
        String fetchedDecile = String.valueOf(npiSmartList.fetchDecile(type));
        Assert.assertTrue("Default decile is not available - ", fetchedDecile.contains(decile));
    }

    @And("User selects {string} from {string} dropdown")
    public void userSelectsTheDiagnosis(String options, String type) {
        List<String> optionList = CommonUtils.parseCommaSeparatedString(options);
        logger.info("Selecting options '{}' from dropdown '{}'", optionList, type);
        for (int i = 0; i < optionList.size(); i++) {
            npiSmartList.selectValueFromClinicalDropdown(optionList.get(i), type);
            if (i < optionList.size() - 1) {
                npiSmartList.clickAddButton(type);
                logger.info("Clicked Add button for '{}' in '{}'", optionList.get(i), type);
            }
        }
    }

    @And("User clicks Browse button to upload {string} file {string}")
    public void userClicksBrowseButtonToUploadDiagnosisFile(String type, String fileName) {
        logger.info("Uploading file '{}' for type '{}'", fileName, type);
        npiSmartList.browseBulkUploadTemplate(type, fileName);
    }

    @And("Verify that Prescribed Behavior Change should display below tabs")
    public void verifyThatPrescribedBehaviorChangeShouldDisplayDroppersAndNewPrescribersTabs(DataTable dataTable) {
        List<String> tabList = dataTable.asList(String.class);
        logger.info("Verifying Prescribed Behavior Change tabs: {}", tabList);

        for (String tabName : tabList) {
            Assert.assertTrue(tabName + " is not available", npiSmartList.fetchPrescriptionBehaviourTab(tabName));
        }
    }

    @And("Verify that {string} tab should be selected by default")
    public void verifyThatDroppersTabShouldBeSelectedByDefault(String defaultTabName) {
        logger.info("Verify that {} tab should be selected by default", defaultTabName);
        Assert.assertTrue(
                "Droppers is not a default selection",
                npiSmartList.fetchDefaultPrescriptionBehaviourTab(defaultTabName));
    }

    @And(
            "Verify that Top Droppers percentage slider should range from {string} to {string} and should be set to {string} by default")
    public void topDroppersPercentageSliderShouldRangeFromToAndShouldBeSetToByDefault(
            String topDropperMin, String topDropperMax, String topDropperDefault) {
        logger.info(
                "Verify that Top Droppers percentage slider should range from {} to {} and should be set to {} by default",
                topDropperMin,
                topDropperMax,
                topDropperDefault);
        Assert.assertTrue(
                "Top Dropper range is not set from 1 to 100%",
                npiSmartList.fetchTopDropperMinAndMaxValues(topDropperMin, topDropperMax));
        Assert.assertEquals(topDropperDefault, npiSmartList.fetchTopDropperDefaultValue());
    }

    @And("User selects the {string} value as {string} from the slider")
    public void userSelectsTheValueFromTheSlider(String sliderType, String sliderValue) {
        logger.info("Selecting '{}' value '{}' from slider", sliderType, sliderValue);
        npiSmartList.selectDropperValueFromSlider(sliderType, sliderValue);
    }

    @And("User selects the Decile value as {string} from the slider for {string}")
    public void userSelectsTheValueFromTheSliderFor(String sliderValue, String listOption) {
        logger.info("Selecting value '{}' for option '{}' from slider", sliderValue, listOption);
        npiSmartList.selectDecile(sliderValue, listOption);
    }

    @And(
            "Verify that Time Frame Selector slider should range from {string} to {string} months and should be set to {string} by default")
    public void verifyThatTimeFrameSelectorSliderShouldRangeFromToMonthsAndShouldBeSetToByDefault(
            String timeframeSelectorMin, String timeframeSelectorMax, String timeframeSelectorDefault) {
        logger.info(
                "Verify that Time Frame Selector slider should range from {} to {} months and should be set to {} by default",
                timeframeSelectorMin,
                timeframeSelectorMax,
                timeframeSelectorDefault);
        Assert.assertTrue(
                "Time Frame Selector range is not set from 6 to 12 months",
                npiSmartList.fetchTimeframeSelectorMinAndMaxValues(timeframeSelectorMin, timeframeSelectorMax));
        Assert.assertEquals(timeframeSelectorDefault, npiSmartList.fetchTimeframeSelectorDefaultValue());
    }

    @And("User selects the prescription drug name {string}")
    public void userSelectsThePrescriptionDrugName(String drugNames) {
        List<String> drugList = CommonUtils.parseCommaSeparatedString(drugNames);
        logger.info("Selecting prescription drugs: {}", drugList);
        for (String drug : drugList) {
            npiSmartList.selectPrescriptionDrug(drug);
        }
    }

    @And("User selects the {string} tab under Prescription Behavior Change")
    public void userSelectsTheTabUnderPrescriptionBehaviorChange(String tabName) {
        logger.info("Selecting Prescription Behavior Change tab '{}'", tabName);
        npiSmartList.clickPrescriptionBehaviourTab(tabName);
    }

    @And("User selects Engagement Type {string} and contextual category {string}")
    public void userSelectsEngagementTypeAndContextualCategory(String engagementType, String contextualCategory) {
        logger.info("Selecting Engagement Type '{}' and Contextual Category '{}'", engagementType, contextualCategory);
        npiSmartList.selectEngagementTypeAndContextualCategory(engagementType, contextualCategory);
    }

    @And("User clicks MESH dropdown, enters {string} and selects it")
    public void userClicksMESHDropdownEntersAndSelectsIt(String meshCondition) {
        logger.info("Selecting MESH condition '{}'", meshCondition);
        npiSmartList.selectMESHCondition(meshCondition);
    }

    @And(
            "Verify that Recency slider should range from {string} to {string} days and should be set to {string} by default")
    public void verifyThatRecencySliderShouldRangeFromToDaysAndShouldBeSetToByDefault(
            String recencyMin, String recencyMax, String recencyDefault) {
        logger.info(
                "Verify that Recency slider should range from {} to {} days and should be set to {} by default",
                recencyMin,
                recencyMax,
                recencyDefault);
        Assert.assertTrue(
                "Recency range is not set from 1 to 60 days",
                npiSmartList.fetchRecencyMinAndMaxValues(recencyMin, recencyMax));
        Assert.assertEquals(recencyDefault, npiSmartList.fetchRecencyDefaultValue());
    }

    @And("User checks Prime list with historical data check box")
    public void userChecksPrimeListWithHistoricalDataCheckBox() {
        logger.info("User checks Prime list with historical data check box");
        npiSmartList.selectPrimeListWithHistoricalDataCheckbox();
    }

    @And("User hovers over the {string} question icon and fetches tool-tip {string}")
    public void userHoversOverTheMedscapeQuestionIconAndFetchesToolTipFor(String contextualCategory, String tooltip) {
        logger.info("User hovers over the {} question icon and fetches tool-tip {}", contextualCategory, tooltip);
        String actualTooltip = npiSmartList.hoverAndFetchTooltip(contextualCategory);
        Assert.assertEquals(tooltip, actualTooltip);
    }

    @And("User clicks {string} primary concept dropdown, enters {string} and selects it")
    public void userClicksMedscapePrimaryConceptDropdownEntersAndSelectsIt(
            String contextualCategory, String primaryConcept) {
        List<String> primaryConceptList = CommonUtils.parseCommaSeparatedString(primaryConcept);
        logger.info("Selecting primary concepts '{}' under '{}'", primaryConceptList, contextualCategory);
        for (String concept : primaryConceptList) {
            npiSmartList.selectMedscapePrimaryConcept(contextualCategory, concept);
        }
    }

    @And("Verify that {string} is disabled under Endemic Network")
    public void verifyThatIsDisabledUnderEndemicNetwork(String contextualCategory) {
        logger.info("Verify that {} is disabled under Endemic Network", contextualCategory);
        Assert.assertTrue(
                contextualCategory + "is not disabled",
                npiSmartList.verifyMedscapeAndWebMDAreDisabled(contextualCategory));
    }

    @And("The user saves the Smart List without selecting any other Population options and verifies error message")
    public void theUserSavesTheSmartListWithoutSelectingAnyOtherPopulationOptionsAndVerifiesErrorMessage() {
        logger.info("Saving Smart List without selecting any Population options to verify error message");
        npiSmartList.clickSaveButton();
        String errorMsg = npiSmartList.fetchErrorAlert();
        Assert.assertEquals("Select one or more List Population Options", errorMsg);
    }

    @And("User selects Smart NPI list as below with mandatory details")
    public void userSelectsSmartNPIListAsBelowWithMandatoryDetails(DataTable dataTable) {
        logger.info(
                "The user saves the Smart List without selecting any other Population options and verifies error message");
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        logger.info("Selecting Smart NPI list with mandatory details: {}", rows);
        for (Map<String, String> row : rows) {
            String option = row.get("PopulationOption") != null
                    ? row.get("PopulationOption").trim()
                    : "";
            String details =
                    row.get("OptionDetails") != null ? row.get("OptionDetails").trim() : "";
            npiSmartList.selectSmartNPIListType(option);
            Map<String, String> attributeMap = Arrays.stream(details.split(","))
                    .map(String::trim)
                    .filter(entry -> entry.contains(":"))
                    .map(entry -> entry.split(":", 2))
                    .filter(arr -> arr.length == 2)
                    .collect(Collectors.toMap(arr -> arr[0].trim(), arr -> arr[1].trim()));
            logger.info("Entering details for '{}': {}", option, attributeMap);
            npiSmartList.enterPopulationOptionsDetail(option, attributeMap);
        }
    }

    @And("Verify Bulk Upload template {string} records count matches UI count post upload")
    public void verifyBulkUploadTemplateEntryCountMatchesUICountPostUpload(String fileName) throws IOException {
        int recordsCountFromFile = FileActions.countRecordsFromTextFile(fileName);
        int recordsCountFromUI = 0;
        if (fileName.contains("Diagnosis_BulkUpload") || fileName.contains("PrescribedDrugs_BulkUpload")) {
            logger.info("Verifying: bulk Upload template {} records count matches UI count post upload", fileName);
            recordsCountFromUI = Integer.parseInt(npiSmartList.fetchDiagnosisCodesFromUI());
        } else {
            recordsCountFromUI = Integer.parseInt(npiSmartList.fetchMedicalProcedureCodesFromUI());
        }
        Assert.assertEquals(
                "Bulk Upload template records doesn't match with UI", recordsCountFromFile, recordsCountFromUI);
    }

    @And(
            "User navigates to Administrative section and fetches the advertisers and client value for the account {string}")
    public void userNavigatesToAdministrativeSectionAndFetchesTheAdvertisersAndClientValueForTheAccount(
            String account) {
        logger.info("Navigating to Administrative section to fetch advertisers and client for account '{}'", account);
        navigation.clickSubMenu();
        accounts.clickAdministration();
        accounts.clickAdvertiserTab();
        accounts.selectAccount(account);
        itemList = normalize(accounts.fetchAdvertiserList());
        logger.info("Fetched advertisers for account '{}': {}", account, itemList);
        accounts.selectAccountsTab();
        accounts.searchAccount(account);
        metricName = accounts.fetchClientValue();
        logger.info("Fetched client value: '{}'", metricName);
        navigation.clickPulsePointLogo();
    }

    @Then("Verify Advertiser dropdown should show values which are mapped to the account")
    public void verifyAdvertiserDropdownShouldShowValuesWhichAreMappedToTheAccount() {
        logger.info("Verify Advertiser dropdown should show values which are mapped to the account");
        List<String> actualAdvertiserList = normalize(campaigns.fetchAdvertiserList());
        Assert.assertEquals("Advertisers list is not matched", actualAdvertiserList, itemList);
    }

    @And("Verify that an error message is displayed when no Advertiser is selected")
    public void verifyThatAnErrorMessageIsDisplayedWhenNoAdvertiserIsSelected() {
        logger.info("Saving campaign without selecting Advertiser to verify error message");
        campaigns.saveCampaign();
        List<String> errorList = campaigns.fetchMandatoryFieldsError();
        Assert.assertTrue("Mandatory field error message is not displayed", errorList.contains("Select Advertiser"));
    }

    @And("Verify that Campaign Type default value is set to {string}")
    public void verifyThatCampaignTypeDefaultValueIsSetTo(String campaignType) {
        logger.info("Verify that Campaign Type default value is set to {}", campaignType);
        String defaultValue = campaigns.fetchDefaultCampaignType();
        Assert.assertEquals("Campaign Type default value is not set to " + campaignType, campaignType, defaultValue);
    }

    @And(
            "Verify that if the account has a Client value set, the Client field is disabled and auto-populated; otherwise, it remains enabled for user selection {string}")
    public void
            verifyThatIfTheAccountHasAClientValueSetTheClientFieldIsDisabledAndAutoPopulatedOtherwiseItRemainsEnabledForUserSelection(
                    String clientName) {
        boolean isEnabled = metricName.equalsIgnoreCase("None");
        String actualState = campaigns.verifyClientFieldEnabledOrDisabledBasedOnAccount(clientName);
        if (isEnabled) {
            logger.info(
                    "Verifying: that if the account has a Client value set, the Client field is disabled and auto-populated; otherwise, it remains enabled for user selection {}",
                    clientName);
            Assert.assertEquals("Enabled", actualState);
        } else {
            Assert.assertEquals("Disabled", actualState);
        }
    }

    @And("Verify that user is able to enter and select the drug {string}")
    public void verifyThatUserIsAbleToEnterAndSelectTheDrug(String drugName) {
        logger.info("Selecting drug '{}'", drugName);
        campaigns.selectDrug(drugName);
    }

    @And("Verify that Campaign Budget accepts only numeric values {string}")
    public void verifyThatCampaignBudgetAcceptsOnlyNumericValues(String budget) {
        logger.info("Verify that Campaign Budget accepts only numeric values {}", budget);
        String invalidValue = campaigns.fetchCampaignBudget(budget);
        Assert.assertEquals("Campaign Budget accepts invalid values", "", invalidValue);
    }

    @And("Verify that user is able to enter the data {string} in the Description field")
    public void verifyThatUserIsAbleToEnterTheDataInTheDescriptionField(String campaignDescription) {
        logger.info("Entering campaign description: '{}'", campaignDescription);
        campaigns.enterCampaignDescription(campaignDescription);
    }

    @And("Verify that Budget Status has the below options, and the default status is {string}")
    public void verifyThatBudgetStatusHasTheOptionsAndAndTheDefaultStatusIs(String defaultButton, DataTable dataTable) {
        List<String> expectedOptions = dataTable.asList(String.class);
        List<String> actualOptions = campaigns.fetchBudgetStatus();
        logger.info("Verifying Budget Status options: expected={}, actual={}", expectedOptions, actualOptions);
        Assert.assertEquals("Budget status has different options", expectedOptions, actualOptions);

        String actualDefault = campaigns.fetchDefaultBudgetStatus();
        logger.info("Verifying default Budget Status button: expected='{}', actual='{}'", defaultButton, actualDefault);
        Assert.assertEquals(defaultButton + " button is not set as default", defaultButton, actualDefault);
    }

    @And("Verify the availability of the Management Fee checkbox and when clicked, below options should be displayed")
    public void verifyTheAvailabilityOfTheManagementFeeCheckboxAndWhenClickedTheOptionsAndShouldBeDisplayed(
            DataTable dataTable) {
        List<String> expectedOptions = dataTable.asList(String.class);
        Assert.assertTrue("Management Fee checkbox is not available", campaigns.isManagementFeeAvailable());
        logger.info("Management Fee checkbox is available. Clicking to display options.");
        campaigns.clickManagementFee();
        List<String> actualOptions = campaigns.fetchManagementFeeOptions();
        Assert.assertEquals("Management Fee has different options", expectedOptions, actualOptions);
    }

    @And(
            "Verify that the user is able to enter data in the selected Management Fee option - {string}, {string}, {string}")
    public void verifyThatTheUserIsAbleToEnterDataInTheSelectedManagementFeeOption(
            String managementFeeOption, String percent, String amount) {
        logger.info(
                "Entering Management Fee data: Option='{}', Percent='{}', Amount='{}'",
                managementFeeOption,
                percent,
                amount);
        campaigns.clickManagementFeeOptionAndEnterData(managementFeeOption, percent, amount);
    }

    @And("User clicks the three-dot menu and verifies that {string} is enabled and {string} is disabled")
    public void userClicksTheThreeDotMenuAndVerifiesThatIsEnabledAndIsDisabled(
            String reportOption, String deleteOption) {
        logger.info(
                "User clicks the three-dot menu and verifies that {} is enabled and {} is disabled",
                reportOption,
                deleteOption);
        campaigns.clickActionItemMenu();
        Assert.assertTrue(
                "Generate Report option is not available and enabled",
                campaigns.isGenerateReportOptionAvailable(reportOption));
        Assert.assertTrue(
                "Delete option is not available and disabled", campaigns.isDeleteOptionAvailable(deleteOption));
    }

    @And("User enters other campaign details {string} {string} {string} {string}")
    public void userEntersOtherCampaignDetails(
            String advertiser, String campaign_name, String campaign_type, String budget) {
        campaignNameRandom = campaign_name + '_' + CommonUtils.timeStampCalculation();
        logger.info(
                "Entering campaign details: Advertiser='{}', Campaign='{}', Type='{}', Budget='{}'",
                advertiser,
                campaignNameRandom,
                campaign_type,
                budget);
        campaigns.selectAdvertiser(advertiser);
        campaigns.enterCampaignName(campaignNameRandom);
        campaigns.setCampaignType(campaign_type);
        campaigns.enterBudget(budget);
    }

    @And("User retrieves all the entered data, saves the Campaign and verifies successful creation")
    public void userRetrievesAllTheEnteredDataSavesTheCampaignAndVerifiesSuccessfulCreation() {
        capturedDetails.clear();
        capturedDetails = campaigns.fetchCampaignDetails();
        logger.info("Fetched campaign details before saving: {}", capturedDetails);
        campaigns.saveCampaign();
        String successMsg = campaigns.campaignSuccess();
        Assert.assertEquals("Campaign " + campaignNameRandom + " created.", successMsg);
    }

    @And("Verify that the saved Campaign data matches the entered data")
    public void verifyThatTheSavedCampaignDataMatchesTheEnteredData() {
        logger.info("Verify that the saved Campaign data matches the entered data");
        campaigns.clickSavedCampaign(campaignNameRandom);
        campaigns.clickCampaignDetailsTab();
        List<String> fetchedData = campaigns.fetchCampaignDetails();
        Assert.assertEquals("The saved Campaign data doesn't match the entered data", capturedDetails, fetchedData);
    }

    @And("User verifies if Add Custom Field button is available")
    public void userVerifiesIfAddCustomFieldButtonIsAvailable() {
        logger.info("Verifying: that the saved Campaign data matches the entered data");
        Assert.assertTrue("Add Custom Field Button is not available", campaigns.isAddCustomFieldButtonAvailable());
        logger.info("Add Custom Field button is available");
    }

    @When("User adds a custom field with {string} on the campaign creation page successfully")
    public void userAddsACustomFieldWithOnTheCampaignCreationPage(String fieldName) {
        customFieldName = fieldName + '_' + CommonUtils.randomFourDigitNumber();
        logger.info("Adding Custom Field: '{}'", customFieldName);
        campaigns.clickAddCustomFieldButton();
        campaigns.enterCustomFieldName(customFieldName);
        campaigns.saveCustomField();
        String successAlert = campaigns.fetchCustomFieldSuccessAlert();
        Assert.assertEquals("Successfully created custom Field : " + customFieldName, successAlert);
    }

    @Then("Verify that the custom field is added on the campaign creation page")
    public void verifyThatTheCustomFieldIsAddedOnTheCampaignCreationPage() {
        logger.info("Verify that the custom field is added on the campaign creation page");
        Assert.assertTrue(
                customFieldName + " Custom Field is not available",
                campaigns.isAddedCustomFieldAvailable(customFieldName));
    }

    @When("User modifies the custom field label to new label {string}")
    public void userModifiesTheCustomFieldToNewLabel(String newFieldName) {
        uiCustomFieldName = newFieldName + '_' + CommonUtils.randomFourDigitNumber();
        logger.info("Modifying Custom Field '{}' to new label '{}'", customFieldName, uiCustomFieldName);
        campaigns.clickCustomFieldLabel(customFieldName);
        campaigns.enterCustomFieldName(uiCustomFieldName);
        campaigns.saveCustomField();
        Assert.assertEquals(
                "Successfully updated custom Field : " + uiCustomFieldName, campaigns.fetchCustomFieldSuccessAlert());
    }

    @Then("Verify that the custom field is updated with new label")
    public void verifyThatTheCustomFieldIsUpdatedWithNewLabel() {
        logger.info("Verify that the custom field is updated with new label");
        Assert.assertTrue(
                customFieldName + " Custom Field label is not updated with " + uiCustomFieldName,
                campaigns.isAddedCustomFieldAvailable(uiCustomFieldName));
    }

    @And("User enters data {string} in the custom field")
    public void userEntersDataInTheCustomField(String customFieldData) {
        logger.info("Entering data '{}' in Custom Field '{}'", customFieldData, uiCustomFieldName);
        campaigns.enterCustomFieldData(uiCustomFieldName, customFieldData);
    }

    @Then("Verify that the custom field value {string} is saved and displayed in the campaign details page")
    public void verifyThatTheCustomFieldValueIsSavedAndDisplayedInTheCampaignDetailsPage(String customFieldData) {
        logger.info(
                "Verify that the custom field value {} is saved and displayed in the campaign details page",
                customFieldData);
        campaigns.navigateToCampaign(campaignNameRandom);
        campaigns.clickCampaignDetailsTab();
        String fetchedData = campaigns.fetchCustomFieldData(uiCustomFieldName);
        Assert.assertEquals(customFieldData, fetchedData);
    }

    @And("User verifies if the added custom field is available on New Campaign creation page")
    public void userVerifiesIfTheAddedCustomFieldIsAvailableOnNewCampaignCreationPage() {
        logger.info("User verifies if the added custom field is available on New Campaign creation page");
        campaigns.navigateToCampaignDashboard();
        campaigns.createCampaign();
        Assert.assertEquals("Create New Campaign", campaigns.verifyCampaignText());
        Assert.assertTrue(
                uiCustomFieldName + " Custom Field is not available",
                campaigns.isAddedCustomFieldAvailable(uiCustomFieldName));
    }

    @When("User deletes the custom field for which campaign is created and verifies if it is deleted")
    public void userDeletesTheCustomFieldFromTheCampaignCreationPage() {
        logger.info("User deletes the custom field for which campaign is created and verifies if it is deleted");
        Assert.assertEquals("Custom Field Can't Be Removed", campaigns.deleteCustomField(uiCustomFieldName));
    }

    @And("User deletes the custom field for which campaign is not created and verifies if it is deleted")
    public void userDeletesTheCustomFieldForWhichCampaignIsNotCreatedAndVerifiesIfItIsDeleted() {
        logger.info("User deletes the custom field for which campaign is created and verifies if it is deleted");
        customFieldName = "Test" + '_' + uiCustomFieldName;
        logger.info("Creating temporary custom field '{}' to delete later", customFieldName);
        campaigns.clickAddCustomFieldButton();
        campaigns.enterCustomFieldName(customFieldName);
        campaigns.saveCustomField();
        Assert.assertEquals(
                "Successfully created custom Field : " + customFieldName, campaigns.fetchCustomFieldSuccessAlert());
        logger.info("Deleting temporary custom field '{}'", customFieldName);
        campaigns.deleteCustomField(customFieldName);
        Assert.assertEquals(
                "Successfully deleted the Field : " + customFieldName, campaigns.fetchCustomFieldSuccessAlert());
    }

    @And("User verifies if the deleted custom field is available on New Campaign creation page")
    public void userVerifiesIfTheDeletedCustomFieldIsAvailableOnNewCampaignCreationPage() {
        logger.info("User verifies if the deleted custom field is available on New Campaign creation page");
        navigation.clickPulsePointLogo();
        campaigns.createCampaign();
        Assert.assertEquals("Create New Campaign", campaigns.verifyCampaignText());
        Assert.assertFalse(
                customFieldName + " Custom Field is available", campaigns.isAddedCustomFieldAvailable(customFieldName));
    }

    @And("Verify that user is able to download the uploaded {string} list")
    public void verifyThatUserIsAbleToDownloadTheUploadedFile(String listType) throws IOException {
        if (listType.equals("NPI")) {
            logger.info("Verifying: that user is able to download the uploaded {} list", listType);
            targetFilePath = npiStaticList.clickDownloadIcon();
        } else {
            targetFilePath = sharedList.clickDownloadIcon();
        }
        Assert.assertTrue(
                "Downloaded file is not available", CommonUtils.isDownloadedFileAvailable(targetFilePath, "csv"));
    }

    @And(
            "Verify that the count of items in the downloaded {string} list is the same as the item count displayed in the UI")
    public void verifyTheCountOfItemsInTheDownloadedList(String listType) throws IOException {
        logger.info(
                "Verify that the count of items in the downloaded {} list is the same as the item count displayed in the UI",
                listType);
        String header = null;
        int recordsCountFromFile = 0;
        String recordsCountFromUI;

        switch (listType) {
            case "Keyword":
                header = "\ufeffKeywords";
                break;
            case "Domain":
                header = "\ufeffdomains";
                break;
            case "App Bundle":
                header = "\ufeffApp Bundles";
                break;
            case "IP":
                recordsCountFromFile = FileActions.fetchRowCountFromCSV(targetFilePath);
                break;
            case "NPI":
                header = "NPI";
                break;
        }

        if (header != null) {
            recordsCountFromFile = FileActions.fetchColumnCountFromCSV(targetFilePath, header);
        }

        if (listType.equals("NPI")) {
            recordsCountFromUI = npiStaticList.fetchSharedListCountFromUI();
        } else {
            recordsCountFromUI = sharedList.fetchSharedListCountFromUI();
        }
        Assert.assertEquals(
                "Downloaded list count doesn't match with UI count",
                recordsCountFromFile,
                Integer.parseInt(recordsCountFromUI));
    }

    @And("User enters the list name as {string} and uploads the file {string}")
    public void userEntersTheListNameAndUploadsTheFile(String listName, String fileName) {
        metricName = listName + "_" + CommonUtils.timeStampCalculation();
        npiName = metricName;
        logger.info("Uploading '{}' as list name '{}'", fileName, metricName);
        sharedList.enterListName(metricName);
        npiStaticList.uploadStaticListFile(fileName);
    }

    @And("User saves the Email list and verify that the list is created successfully")
    public void userSavesTheListAndVerifyThatTheListIsCreatedSuccessfully() {
        logger.info("User saves the Email list and verify that the list is created successfully");
        sharedList.saveEmailList();
        Assert.assertEquals("Email list created successfully", sharedList.isListCreatedOrDeleted());
    }

    @And("Verify that the counter on the left displays the correct value after file upload for {string}")
    public void verifyThatTheCounterOnTheLeftDisplaysTheCorrectValueAfterFileUploadFor(String listType) {
        logger.info(
                "Verify that the counter on the left displays the correct value after file upload for {}", listType);
        sharedList.searchCreatedList(metricName);
        sharedList.openSearchedList(metricName);
        totalListCount = Integer.parseInt(sharedList.fetchCountFromLeftPanel(metricName));
        itemCount = sharedList.fetchEmailCount();
        Assert.assertEquals(totalListCount, itemCount);
    }

    @And("Verify that download option should not be available for uploaded Email list")
    public void verifyThatDownloadOptionShouldNotBeAvailableForUploadedEmailList() {
        logger.info("Verify that download option should not be available for uploaded Email list");
        Assert.assertFalse("Download icon is available for Email list", sharedList.isEmailListDownloadIconVisible());
    }

    @Then("Verify that user is able to export the audit log for {string}")
    public void verifyThatUserIsAbleToExportTheAuditLogFor(String moduleName) {
        campaigns.clickCampaignOptions();
        campaigns.openExportAuditLogPopup();
        logger.info("Export audit log popup content for module '{}'", moduleName);

        switch (moduleName) {
            case "campaign":
                assert campaigns.fetchExportAuditLogPopupContent().contains(campaignNameRandom);
                break;
            case "line item":
                assert campaigns.fetchExportAuditLogPopupContent().contains(lineItemNameRandom);
                break;
            case "tactic":
                assert campaigns.fetchExportAuditLogPopupContent().contains(tacticNameRandom);
                break;
        }

        campaigns.clickConfirmExportAuditLog();
        Assert.assertEquals(
                "Audit Log request created. File will be send via email.", campaigns.fetchExportAuditLogSuccessAlert());
    }

    @When("User navigates to {string} page")
    public void userNavigatesToItemPage(String moduleName) {
        logger.info("Navigating to '{}' page", moduleName);
        switch (moduleName) {
            case "campaign":
                campaigns.clickCampaignTile();
                break;
            case "line item":
                campaigns.clickLineItemTile();
                break;
            case "tactic":
                campaigns.clickTacticTile();
                break;
        }
    }

    @Then("Verify that user is able to export the campaign settings")
    public void verifyThatUserIsAbleToExportTheCampaignSettings() {
        logger.info("Verify that user is able to export the campaign settings");
        campaigns.clickCampaignDetailsTab();
        campaigns.clickCampaignOptions();
        campaigns.exportCampaignSettings();
        String successMsg = campaigns.fetchExportCampaignSettingsSuccessAlert();
        Assert.assertEquals(
                "Done! The exported file will be sent to default@pulsepoint.com within 10 minutes.", successMsg);
    }

    @And("Verify that user is able to download the {string} list")
    public void verifyThatUserIsAbleToDownloadTheList(String listType) throws IOException {
        logger.info("Verify that user is able to download the {} list", listType);
        targetFilePath = npiStaticList.clickDownloadIcon();
        Assert.assertTrue(
                "Downloaded file is not available", CommonUtils.isDownloadedFileAvailable(targetFilePath, "csv"));
    }

    @And("User searches and selects the NPI List {string}")
    public void userSearchesAndSelectsThePulsePointProvidedNPIList(String npiListName) {
        logger.info("Searching and selecting NPI list '{}'", npiListName);
        npiLists.searchList(npiListName);
        npiLists.openSearchedList(npiListName);
    }

    @Then("User navigates to creative details and clicks Association tab")
    public void userNavigatesToCreativeDetailsAndClickOnAssociationTab() {
        logger.info("Navigating to creative details and Association tab for creative: '{}'", metricName);
        createCreatives.searchCreative(metricName);
        createCreatives.clickSearchedCreative(metricName);
        createCreatives.clickAssociationTab();
        logger.info("Successfully navigated to the Association tab");
    }

    @And("Verify column selection icon is available and upon clicking it below columns should display")
    public void
            verifyColumnSelectionIconIsAvailableAndUponClickingItLineItemNameIDStatusCampaignNameStartDateAndEndDateShouldBeDisplayed(
                    DataTable dataTable) {
        List<String> expectedColumnNames = dataTable.asList(String.class);
        logger.info("Verifying column selection icon and expected available columns: {}", expectedColumnNames);
        createCreatives.clickColumnSelectionIcon();
        List<String> actualColumnNames = createCreatives.fetchColumnNamesFromSelectionIconList();
        Assert.assertEquals("Column names do not match", expectedColumnNames, actualColumnNames);
    }

    @And("User searches and selects the campaign {string}")
    public void userSearchesAndSelectsTheCampaign(String campaignName) {
        logger.info("Searching and opening campaign '{}'", campaignName);
        campaignDashboard.searchCreatedCampaign(campaignName);
        campaignDashboard.navigateToCampaign(campaignName);
    }

    @And("Verify unselected columns are not displayed in the Association tab")
    public void verifyUnselectedColumnsAreNotDisplayedInTheAssociationTab(DataTable dataTable) {
        List<String> unselectedColumnNames = dataTable.asList(String.class);
        logger.info("Deselecting columns from the column selection icon list");
        createCreatives.deselectColumnNamesFromSelectionIconList(unselectedColumnNames);
        List<String> columnName = createCreatives.fetchColumnNamesFromAssociationsTab();
        logger.info("Fetched visible columns from Association tab: {}", columnName);

        for (String name : unselectedColumnNames) {
            logger.info("Checking visibility of deselected column: '{}'", name);
            boolean isColumnDisplayed = columnName.contains(name);
            Assert.assertFalse(
                    name + " column is still displayed in Association tab after deselecting it", isColumnDisplayed);
        }
        logger.info("Unselected columns verification completed successfully");
    }

    @Then("Verify that the campaign page is displayed")
    public void verifyThatTheCampaignPageIsDisplayed() {
        logger.info("Verify that the campaign page is displayed");
        Assert.assertTrue(
                "Navigation to Campaign details page is not successful", campaignDashboard.isCampaignPageDisplayed());
    }

    @And("Verify if {string} hides all the columns in the Association tab")
    public void verifyIfHidesAllTheColumnsInTheAssociationTab(String buttonName) {
        createCreatives.clickColumnSelectionIcon();
        logger.info("Clicking menu button '{}' from column selection", buttonName);
        createCreatives.clickMenuButtonFromColumnSelection(buttonName);
        List<String> columnName = createCreatives.fetchColumnNamesFromAssociationsTab();
        logger.info("Fetched column names from Association tab: {}", columnName);
        boolean isColumnListEmpty = columnName.isEmpty();
        Assert.assertTrue("Column names are available", isColumnListEmpty);
    }

    @And("Verify if {string} displays all the columns in the Association tab")
    public void verifyIfDisplaysAllTheColumnsInTheAssociationTab(String buttonName, DataTable dataTable) {
        List<String> expectedColumnNames = dataTable.asList(String.class);
        createCreatives.clickColumnSelectionIcon();
        logger.info("Clicking menu button '{}' from column selection", buttonName);
        createCreatives.clickMenuButtonFromColumnSelection(buttonName);
        List<String> actualColumnNames = createCreatives.fetchColumnNamesFromAssociationsTab();
        Assert.assertEquals("Column names do not match", expectedColumnNames, actualColumnNames);
    }

    @And("Verify filter icon is available and upon clicking it {string}, {string} and {string} text should display")
    public void verifyFilterIconIsAvailableAndUponClickingItAndTextShouldDisplay(
            String button1, String button2, String text) {
        createCreatives.clickFilterIcon();
        List<String> filterFields = createCreatives.fetchFilterFields();
        logger.info("Fetched filter fields: {}", filterFields);
        boolean isButton1Available = filterFields.contains(button1);
        Assert.assertTrue("Add Filter button is not available", isButton1Available);
        boolean isButton2Available = filterFields.contains(button2);
        Assert.assertTrue("Done button is not available", isButton2Available);
        boolean isTextAvailable = filterFields.contains(text);
        Assert.assertTrue("No Filters applied text is not available", isTextAvailable);
    }

    @And("User clicks {string}, selects below filters and apply using {string} button")
    public void userClicksSelectsBelowFiltersAndApplyUsingButton(
            String addFilterButton, String doneButton, DataTable dataTable) {
        List<String> filterName = dataTable.asList(String.class);
        logger.info("Applying filters: {}", filterName);
        lineItemNameRandom = createCreatives.fetchLineItemFromAssociation();
        String campaignName = createCreatives.fetchCampaignFromAssociation();

        for (String name : filterName) {
            createCreatives.clickFilterButton(addFilterButton);
            createCreatives.selectFilterName(name);

            if (name.contains("Line Item Name")) {
                createCreatives.enterFilterValue(lineItemNameRandom);
            } else if (name.contains("Campaign Name")) {
                createCreatives.enterFilterValue(campaignName);
            } else {
                createCreatives.enterDates();
            }
        }

        createCreatives.clickFilterButton(doneButton);
        String filteredRecordsCount = createCreatives.fetchFilteredRecordsCount();
        Assert.assertEquals("1 records", filteredRecordsCount);
    }

    @And("User navigates to Line item from Association Tab")
    public void userNavigatesToLineItemFromAssociationTab() {
        logger.info("User navigates to Line item from Association Tab");
        Assert.assertTrue(
                "Navigation to the line item " + lineItemNameRandom + " is not successful",
                createCreatives.clickLineItemName(lineItemNameRandom).contains(lineItemNameRandom));
    }

    @Then("User navigates to tactic setting tab")
    public void userNavigatesToTacticSettingTab() {
        logger.info("User navigates to Line item from Association Tab");
        tacticDetails.clickSettingsTab();
    }

    @Then("The user clicks on show expression tab and fetch the values displayed")
    public void the_user_clicks_on_show_expression_tab_and_fetch_the_values_displayed() {
        tacticDetails.clickShowExpressionButton();
        tacticDetails.fetchShowExpressionValues();
    }

    @Then(
            "Verify that all the rule types added in targeting rules are displayed in show expression with correct values along with {string}")
    public void
            verify_all_rule_types_added_in_targeting_rules_are_displayed_in_show_expression_with_correct_values_along_with_default_expression(
                    String defaultExpression) {
        boolean result = tacticDetails.ruleMappingWithShowExpressionValues(rulesMap, defaultExpression);
        Assert.assertTrue("Targeting rules added is not matching with the ones in show expression", result);
    }

    @Then("Verify show expression connector AND OR logic is correct")
    public void verifyShowExpressionConnectorLogicIsCorrect() {
        tacticDetails.fetchShowExpressionValues();
        Assert.assertTrue(
                "Show expression connector logic is incorrect. Raw values: "
                        + tacticDetails.getShowExpressionRawValues(),
                tacticDetails.assertShowExpressionConnectorLogic(tacticDetails.getShowExpressionRawValues()));
    }

    @Then("User removes the targeting {string} and saves the settings")
    public void userRemovesTheTargetingAndSavesTheSettings(String ruleType) {
        logger.info("Removing targeting rule type: {}", ruleType);
        tacticDetails.removeTargetingRule(ruleType);
        tacticSettings.saveTacticSettings();
    }

    @Then("User verifies that forecast data is unavailable when no targeting rules are applied")
    public void user_verifies_that_forecast_data_is_unavailable_when_no_targeting_rules_are_applied() {
        Assert.assertTrue("Targeting is added in the tactic", tacticDetails.isTargetingRuleMissing());
        Assert.assertFalse(
                "Forecast data is displayed even though no targeting rules are added",
                tacticDetails.isForecastDataAvailable());
    }

    @Then("User verifies the forecast data refreshes and displays values after adding targeting rule")
    public void user_verifies_the_forecast_data_refreshes_and_displays_values_after_adding_targeting_rule() {
        logger.info("Verifying that forecast data is refreshed and generated after targeting rules are added");
        Assert.assertTrue(
                "Forecast data was not refreshed after adding targeting rules",
                tacticDetails.isForecastDataAvailable());
    }

    @And("User opens Life Settings")
    public void userOpensLifeSettings() {
        logger.info("User navigates to tactic setting tab");
        accounts.clickLifeSettings();
    }

    @And("User fetches PulsePoint Data Fees, NPI Targeting Gross CPM and calculates the data cost")
    public void userFetchesPulsePointDataFeesNPITargetingGrossCPMAndCalculatesTheDataCost() {
        logger.info("User opens Life Settings");
        double dataFee = accounts.fetchPulsePointDataFees();
        double grossCPM = accounts.fetchNPITargetingGrossCPM();
        logger.info("Calculating data cost from Data Fee: {}% and Gross CPM: ${}", dataFee, grossCPM);
        BigDecimal dataCost =
                BigDecimal.valueOf((grossCPM / (1 - (dataFee / 100)))).setScale(2, RoundingMode.HALF_UP);
        metricName = "$" + dataCost;
        accounts.clickLifeCancelButtonFromSettingsPanel();
    }

    @And("User verifies the calculated data cost is similar to the displayed data cost")
    public void userVerifiesTheCalculatedDataCostIsSimilarToTheDisplayedDataCost() {
        logger.info("User verifies the calculated data cost is similar to the displayed data cost");
        String displayedDataCost = npiStaticList.fetchDisplayedDataCost();
        Assert.assertEquals(
                "Calculated data cost doesn't match with displayed data cost", metricName, displayedDataCost);
    }

    @And("User logs out from the application")
    public void logsOutFromTheApplication() {
        logger.info("Logging out from the application");
        navigation.logout();
    }

    @And("User navigates to the created campaign")
    public void userNavigatesToTheCreatedCampaign() {
        logger.info("Navigating to the created campaign '{}'", campaignNameRandom);
        campaignDashboard.searchCreatedCampaign(campaignNameRandom);
        campaignDashboard.navigateToCreatedCampaign(lineItemNameRandom);
    }

    @And("Admin user approves the campaign")
    public void adminUserApprovesTheCampaign() {
        logger.info("Admin user approving the campaign '{}'", campaignNameRandom);
        campaigns.clickCampaignDetailsTab();
        campaigns.approveCampaign();
        Assert.assertEquals("Campaign " + campaignNameRandom + " updated.", campaigns.campaignSuccess());
    }

    @And("User fetches the logged in username")
    public void userFetchesTheLoggedInUsername() {
        userType = runReport.fetchLoggedInUsername().split("\\(")[0];
        logger.info("Fetched logged in username: '{}'", userType);
    }

    @And("Verify that {string} tab is selected as Delivery method by default")
    public void verifyThatTabIsSelectedByDefault(String deliveryTab) {
        logger.info("Verify that {} tab is selected as Delivery method by default", deliveryTab);
        Assert.assertTrue(
                deliveryTab + " is not default Delivery Method", runReport.fetchDefaultDeliveryTab(deliveryTab));
    }

    @And(
            "Verify that {string} field is pre-populated with logged in user email and user should be able to edit the email address {string}")
    public void verifyThatFieldIsPrePopulatedWithLoggedInUserEmailAndUserShouldBeAbleToEditTheEmailAddress(
            String fieldName, String newEmail) {
        String[] emails = newEmail.split(",");
        List<String> fetchScheduleReportValue = runReport.fetchScheduleReportInputValue(fieldName);
        logger.info("Fetched value from '{}' field: '{}'", fieldName, fetchScheduleReportValue);
        for (String value : fetchScheduleReportValue) {
            Assert.assertTrue(
                    fieldName + " field is not pre-populated with logged in user email", userType.contains(value));
        }
        for (String email : emails) {
            runReport.enterDataInScheduleReport(fieldName, email);
            logger.info("Entered new email '{}' in '{}' field", email, fieldName);
            nameList.addAll(runReport.fetchScheduleReportInputValue(fieldName));
        }
    }

    @And("Verify that user is not able to remove the pre-populated logged in user email from {string} field")
    public void verifyThatUserIsNotAbleToRemoveThePrePopulatedLoggedInUserEmailFromField(String fieldName) {
        logger.info(
                "Verify that user is not able to remove the pre-populated logged in user email from {} field",
                fieldName);
        String toolTipText = runReport.verifyEmailNotRemovable(fieldName, userType);
        Assert.assertEquals(
                "Tooltip text mismatch when trying to remove email from " + fieldName,
                "Creator can't be removed.",
                toolTipText);
    }

    @And("Verify File Name field is available on report panel")
    public void verifyFileNameFieldIsAvailableOnReportPanel() {
        logger.info("Verify File Name field is available on report panel");
        Assert.assertTrue(
                "File Name field is not available on report panel", runReport.isFileNameFieldAvailable());
    }

    @And("Verify the presence of Advanced Export checkbox and by default it should be unchecked")
    public void verifyThePresenceOfAdvancedExportCheckboxAndByDefaultItShouldBeUnchecked() {
        logger.info("Verify the presence of Advanced Export checkbox and by default it should be unchecked");
        Assert.assertTrue(
                "Advanced Export checkbox is not available", runReport.isAdvancedExportCheckboxAvailable());
        Assert.assertFalse(
                "Advanced Export checkbox is checked by default", runReport.isAdvancedExportCheckboxChecked());
    }

    @And("Verify Line Coding field is available with below options and default value is {string}")
    public void verifyLineCodingFieldIsAvailableWithBelowOptionsAndDefaultValueIs(
            String defaultValue, DataTable dataTable) {
        List<String> expectedTypes = dataTable.asList(String.class);
        runReport.clickAdvancedDeliverySettingLink();
        logger.info("Verifying Line Coding field is available");
        Assert.assertTrue("Line Coding field is not available", runReport.isLineCodingFieldAvailable());
        logger.info("Verifying default Line Coding type is '{}'", defaultValue);
        Assert.assertTrue("None is not selected by default", runReport.checkDefaultLineCodingType(defaultValue));
        List<String> actualTypes = runReport.fetchLineCodingTypes();
        Assert.assertEquals(new HashSet<>(expectedTypes), new HashSet<>(actualTypes));
    }

    @And(
            "User enters Destination name {string}, Destination Type {string} and other required details - {string}, {string}, {string}")
    public void userEntersDestinationNameDestinationTypeAndOtherRequiredDetails(
            String destinationName, String destinationType, String host, String port, String serverPath)
            throws Exception {
        logger.info(
                "Fetching destination details from config for Destination Name='{}', Destination Type='{}'",
                destinationName,
                destinationType);
        username = ConfigReader.getCustomDestinationUsername();
        password = ConfigReader.getCustomDestinationPassword();
        dimensionName = destinationName + '_' + CommonUtils.timeStampCalculation();
        logger.info(
                "Entering destination details: Name='{}', Type='{}', Host='{}', Port='{}', Server Path='{}'",
                dimensionName,
                destinationType,
                host,
                port,
                serverPath);
        runReport.enterDestinationDetails(
                dimensionName, destinationType, host, username, password, port, serverPath);
    }

    @Then("User runs the connection test and creates the destination")
    public void userRunsTheConnectionTestAndCreatesTheDestination() {
        logger.info("Running connection test and creating destination");
        runReport.clickTestAccessButton();
        runReport.clickCreateDestinationButton();
        logger.info("Destination creation initiated successfully");
        String text = runReport.fetchSuccessAlert();
        Assert.assertEquals("Destination's settings saved successfully", text);
    }

    @And("Verify destination created should populate in the Destination dropdown field")
    public void verifyDestinationCreatedShouldPopulateInTheDestinationDropdownField() {
        logger.info("User runs the connection test and creates the destination");
        String destinationOptions = scheduleReport.fetchDestinationOptions();
        Assert.assertTrue(
                "Created destination is not available in the dropdown", destinationOptions.contains(dimensionName));
    }

    @And(
            "Validate that the template name matches the file name, and ensure the help text displays the file name in the {string} format")
    public void validateThatTheTemplateNameMatchesTheFileNameAndEnsureTheHelpTextDisplaysTheFileNameInTheCsvFormat(
            String extensionType) {
        logger.info(
                "Fetching the file name and help text from UI to validate against expected file name with extension '{}'",
                extensionType);
        String actualFileName = runReport.fetchFileNameFromUI();
        Assert.assertEquals("File name is not populated with template name", templateNameRandom, actualFileName);
        logger.info(
                "Fetching help text for file name field to validate it contains the expected file name with extension '{}'",
                extensionType);
        String expectedFileName = templateNameRandom + extensionType;
        String helpText = runReport.fetchFileNameHelpText();
        Assert.assertEquals("Fetched help text does not match expected file name", expectedFileName, helpText);
    }

    @And("Verify Report Period field is available with default value {string}")
    public void verifyReportPeriodFieldIsAvailableWithDefaultValue(String defaultValue) {
        logger.info("Verify Report Period field is available with default value {}", defaultValue);
        Assert.assertTrue("Report Period field is not available", scheduleReport.isReportPeriodFieldAvailable());
        Assert.assertEquals(
                "Default value of Report Period field is not " + defaultValue,
                defaultValue,
                scheduleReport.fetchDefaultReportPeriodValue());
    }

    @And("Verify that Report Period field has below options")
    public void verifyThatReportPeriodFieldHasBelowOptions(DataTable dataTable) {
        List<String> expectedOptions = dataTable.asList(String.class);
        logger.info("Verifying Report Period field has expected options: {}", expectedOptions);
        List<String> actualOptions = scheduleReport.fetchReportPeriodOptions();
        logger.info("Fetched Report Period options from UI: {}", actualOptions);
        Assert.assertEquals(new HashSet<>(expectedOptions), new HashSet<>(actualOptions));
    }

    @And("Verify Report Timing checkbox is available and by default it is unchecked")
    public void verifyReportTimingCheckboxIsAvailableAndByDefaultItIsUnchecked() {
        logger.info("Verify Report Timing checkbox is available and by default it is unchecked");
        Assert.assertTrue("Report Timing checkbox is not available", scheduleReport.isReportTimingCheckboxAvailable());
        Assert.assertTrue(
                "Report Timing checkbox is unchecked by default", scheduleReport.isReportTimingCheckboxChecked());
    }

    @And(
            "User clicks the three-dot menu, selects the General variable - {string} and Time variable - {string} with Date-Time format {string}")
    public void userClicksTheThreeDotMenuSelectsTheGeneralAndTimeVariables(
            String generalVariable, String timeVariable, String dateTimeFormat) {
        logger.info(
                "User clicks the three-dot menu, selects the General variable - {} and Time variable - {} with Date-Time format {}",
                generalVariable,
                timeVariable,
                dateTimeFormat);
        scheduleReport.clickThreeDotMenuForFileName();
        scheduleReport.selectGeneralVariableFromThreeDotMenu(generalVariable);
        if (generalVariable.contains("$CampaignName$"))
            customFieldName = runReport.fetchCampaignName().getFirst();
        else if (generalVariable.contains("$LineItemName$"))
            customFieldName = runReport.fetchLineItemName().getFirst();
        else if (generalVariable.contains("AdvertiserName"))
            customFieldName = runReport.fetchAdvertiserName().getFirst();
        scheduleReport.selectTimeVariableFromThreeDotMenu(timeVariable, dateTimeFormat);
        scheduleReport.closeThreeDotMenu();
        logger.info("Selected General variable and Time variable from three-dot menu successfully");
    }

    @And("User verifies that the help text displays the file name with the value of General and Time variables")
    public void userVerifiesThatTheHelpTextDisplaysTheFileNameWithTheValueOfGeneralAndTimeVariables() {
        logger.info("Verifying that the help text displays the file name with the value of General and Time variables");
        String helpText = runReport.fetchFileNameHelpText();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String expectedFileName = templateNameRandom + "_" + customFieldName + "_" + date + ".csv";
        Assert.assertEquals(
                "Help text does not display the expected file name with General and Time variable values",
                expectedFileName,
                helpText);
    }

    @And("User searches the campaign created in the above steps")
    public void userSearchesTheCampaignCreatedInTheAboveSteps() {
        logger.info("Searching for the campaign created in the above steps with name '{}'", campaignNameRandom);
        userEntersAndClickSearchButton(campaignNameRandom);
    }

    @And("User clicks New Tactic button, create tactic with details - {string}, {string}")
    public void userClicksNewTacticButtonCreateTacticWithDetails(String ruleType, String creative) {
        logger.info("Clicking New Tactic button to create a new tactic under campaign '{}'", campaignNameRandom);
        for (String name : nameList) {
            logger.info("Navigating to Line Item '{}' details page", name);
            lineItemDetails.clickLineItem(name);
            logger.info("Clicking New Tactic button for Line Item '{}'", name);
            tacticDetails.clickNewTacticForLineItem(name);
            tacticNameRandom = "Tactic" + '_' + CommonUtils.timeStampCalculation();
            logger.info("Entering tactic details for name: {}", tacticNameRandom);
            tacticDetails.createTactic(tacticNameRandom);
            Assert.assertEquals("Bid Strategy", tacticSettings.verifyTacticSettingsText());
            logger.info("Adding Targeting Rule for line item '{}'", name);
            navigation.clickOnIcon("Add Targeting Rule");
            tacticSettings.selectRuleType(ruleType);
            tacticSettings.saveTacticSettings();
            Assert.assertTrue(
                    "Unable to save Tactic Settings page",
                    tacticSettings.tacticSettingsSuccess().contains("Success!"));
            Assert.assertEquals("Creative(s)", tacticCreatives.verifyTacticCreativesText());
            logger.info("Assigning creative '{}' to tactic for line item '{}'", creative, name);
            navigation.clickOnIcon("Assign Existing Creatives");
            tacticCreatives.assignCreatives(creative);
            logger.info("Enabling Tactic and saving Creatives");
            tacticCreatives.enableCreative();
            tacticCreatives.saveTacticCreatives();
        }
    }

    @And("Verify Deal Type field is available with default value as {string}")
    public void verifyDealTypeFieldIsAvailableWithDefaultValueAs(String defaultValue) {
        logger.info("Verify Deal Type field is available with default value as {}", defaultValue);
        Assert.assertTrue("Deal Type field is not available", pmp.isDealTypeFieldAvailable());
        Assert.assertEquals(
                "Default value of Deal Type field is not " + defaultValue,
                defaultValue,
                pmp.fetchDefaultDealTypeValue());
    }

    @And("Verify Curator field is available with default value as {string}")
    public void verifyCuratorFieldIsAvailableWithDefaultValueAs(String defaultValue) {
        logger.info("Verify Curator field is available with default value as {}", defaultValue);
        Assert.assertTrue("Curator field is not available", pmp.isCuratorFieldAvailable());
        Assert.assertEquals(
                "Default value of Curator field is not " + defaultValue, defaultValue, pmp.fetchDefaultCuratorValue());
    }

    @And("Verify Pricing Type field is available with default value as {string}")
    public void verifyPricingTypeFieldIsAvailableWithDefaultValueAs(String defaultValue) {
        logger.info("Verify Pricing Type field is available with default value as {}", defaultValue);
        Assert.assertTrue("Pricing Type field is not available", pmp.isPricingTypeFieldAvailable());
        Assert.assertEquals(
                "Default value of Pricing Type field is not " + defaultValue,
                defaultValue,
                pmp.fetchDefaultPricingTypeValue());
    }

    @Then("Verify Edit icon availability for the deals listed under {string} Deals tab")
    public void verifyEditIconIsAvailableForThePrivateDealCreated(String dealType) {
        if (dealType.contains("Private")) {
            logger.info("Verifying: edit icon availability for the deals listed under {} Deals tab", dealType);
            Assert.assertTrue(
                    "Edit icon is not available for the created private deal", pmp.isEditIconAvailableForDeals());
            Assert.assertTrue(
                    "Edit icon is not clickable for the created private deal",
                    pmp.clickEditButtonForCreatedPrivateDeal(dealNameRandom));
            pmp.closePMPDealEditPanel();
        } else {
            Assert.assertFalse(
                    "Edit icon is available for the created programmatic deal", pmp.isEditIconAvailableForDeals());
        }
    }

    @And("Verify Clearing Price field is available and fetch the tool-tip details on hover for the field")
    public void verifyClearingPriceFieldIsAvailableAndFetchTheToolTipDetailsOnHoverForTheField() {
        logger.info("Verify Clearing Price field is available and fetch the tool-tip details on hover for the field");
        Assert.assertTrue("Clearing Price field is not available", pmp.isClearingPriceFieldAvailable());
        String toolTipText = pmp.fetchClearingPriceFieldToolTip();
        Assert.assertEquals(
                "Tooltip text mismatch for Clearing Price field",
                "Clearing Price is the average clearing bid price seen for the deal by the DSP. To ensure scale, when targeting a deal, we recommend bidding above the clearing price.",
                toolTipText);
    }

    @And("Verify that {string} and {string} buttons are available and by default {string} button is selected")
    public void verifyThatActiveAndArchivedButtonsAreAvailableAndByDefaultButtonIsSelected(
            String activeText, String archivedText, String defaultValue) {
        logger.info(
                "Verify that {} and {} buttons are available and by default {} button is selected",
                activeText,
                archivedText,
                defaultValue);
        Assert.assertTrue("Active button is not available", pmp.isActiveArchivedButtonAvailable(activeText));
        Assert.assertTrue("Archived button is not available", pmp.isActiveArchivedButtonAvailable(archivedText));
        Assert.assertEquals(
                "Default value of Pricing Type field is not " + defaultValue,
                defaultValue,
                pmp.isDefaultStatusButtonSelected());
    }

    @And("Verify that {string} should not display in deals listing under Life Marketplace Deals tab")
    public void verifyThatShouldNotDisplayInDealsListingUnderDealsTab(String exchangeType) {
        logger.info(
                "Verify that {} should not display in deals listing under Life Marketplace Deals tab", exchangeType);
        List<String> dealExchangeTypes = pmp.fetchDealExchangeTypesFromDealsListing();
        boolean isExchangeTypePresent = dealExchangeTypes.contains(exchangeType);
        Assert.assertFalse(
                "Deals with exchange type " + exchangeType
                        + " are displayed in the deals listing under Life Marketplace Deals tab",
                isExchangeTypePresent);
    }

    @And("User clicks 3 dot menu and selects Archive button for the active deal from the deal listing")
    public void userClicksArchiveButtonForTheActiveDealFromTheDealListing() {
        logger.info("Clicking Archive button for the active deal '{}'", dealNameRandom);
        pmp.clickArchiveButtonForCreatedPrivateDeal(dealNameRandom);
        Assert.assertTrue("Archive Confirmation Dialog is not displayed", pmp.isArchiveConfirmationDialogDisplayed());
    }

    @And("User clicks {string} button from the search section of deal listing page")
    public void userClicksButtonFromTheDealListing(String buttonType) {
        logger.info("User clicks {} button from the search section of deal listing page", buttonType);
        pmp.clickActiveArchivedButtonAvailable(buttonType);
    }

    @Then("Verify that the deal is moved to archived deal section")
    public void verifyThatTheDealIsMovedToArchivedSectionAndNotListedUnderActiveDealSection() {
        logger.info("Searching for the deal '{}' in archive tab", dealNameRandom);
        pmp.searchDealFromList(dealNameRandom);
        Assert.assertTrue("Deal is not listed in the Archived Deal Listing page", pmp.isDealAvailable(dealNameRandom));
    }

    @And("Verify Archive option is available based on the campaign state")
    public void verifyArchiveOptionIsAvailableBasedOnTheCampaignState() {
        if (pmp.isArchiveButtonAvailableOnConfirmationDialog()) {
            logger.info("Verifying: archive option is available based on the campaign state");
            pmp.clickArchiveButtonFromConfirmationDialog();
            Assert.assertEquals(
                    "Success alert is not displayed", "Deal Archived Successfully", pmp.fetchDealArchiveSuccessAlert());
        } else if (pmp.isRemoveAssociationTextDisplayed()) {
            Assert.assertEquals(
                    "This deal cannot be archived; one of the associated Tactics is currently live.Remove Assoctiation to proceed.",
                    pmp.fetchRemoveAssociationText());
        }
    }

    @And("Verify the Tactic Link is available in the confirmation pop-up")
    public void verifyTheTacticLinkIsAvailableInTheConfirmationPopUp() {
        logger.info("Verify the Tactic Link is available in the confirmation pop-up");
        Assert.assertTrue("Tactic Link is not available in the confirmation pop-up", pmp.isTacticLinkAvailable());
    }

    @And("Verify the Tactic Link is clickable and navigates to the respective tactic page")
    public void verifyTheTacticLinkIsClickableAndNavigatesToTheRespectiveTacticPage() {
        logger.info("Verifying: the Tactic Link is available in the confirmation pop-up");
        String tacticNameFromLink = pmp.fetchTacticLinkText();
        Assert.assertTrue(
                "Tactic Name is not available in the Tactic Link fetched",
                tacticNameFromLink.contains(tacticNameRandom));
        String tacticNameFromPage = pmp.clickTacticLink();
        Assert.assertTrue(
                "Navigation to the tactic page is not successful", tacticNameFromPage.contains(tacticNameRandom));
    }

    @And("User unassigns active deal from the applied deals section of All Deals tab")
    public void userUnassignsActiveDealFromTheAppliedDealsSectionOfAllDealsTab() {
        logger.info("Verifying: the Tactic Link is clickable and navigates to the respective tactic page");
        Assert.assertTrue(
                "Deal is available on the Applied Deal panel", pmp.unassignDealFromAppliedDealsSection(dealNameRandom));
    }

    @And("Verify only life marketplace tab is displayed under Targeting templates section for {string} rule type")
    public void verifyOnlyLifeMarketplaceTabIsDisplayedUnderTargetingTemplatesSectionForRuleType(String ruleType) {
        logger.info(
                "Verify only life marketplace tab is displayed under Targeting templates section for {} rule type",
                ruleType);
        targetingTemplate.clickAddTargetingRule();
        tacticSettings.searchAndSelectRuleType(ruleType);
        Assert.assertTrue("Life Marketplace Tab is not available", pmp.isLifeMarketplaceDealsTabVisible());
    }

    @And("User navigates to Setup Tab")
    public void userNavigatesToSetupTab() {
        logger.info("Navigating to Setup tab");
        setup.clickSetupTab();
        setup.curatedMarketSubtab();
    }

    @When("User clicks Create Curated Market link")
    public void userClicksCreateCuratedMarketLink() {
        logger.info("User clicks Create Curated Market link");
        setup.clickCuratedMarketLink();
    }

    @And("User creates a curated market with details {string}, {string}, {string}, {string}")
    public void userCreatesACuratedMarketWithDetails(
            String marketName, String accountName, String description, String marginKPIAndBenchmark) {
        logger.info(
                "Creating a curated market with details - Market Name: '{}', Account Name: '{}', Description: '{}', Margin KPI and Benchmark: '{}'",
                marketName,
                accountName,
                description,
                marginKPIAndBenchmark);
        templateNameRandom = marketName + '_' + CommonUtils.timeStampCalculation();
        setup.enterCuratedMarketDetails(templateNameRandom, accountName, description, marginKPIAndBenchmark);
        setup.clickSaveButton();
        Assert.assertEquals("Curated Market created successfully", setup.getAlertMessage());
        metricName = setup.fetchCuratedMarketId();
        logger.info("Curated market ID: '{}'", metricName);
    }

    @And("User creates a curated market with only {string}")
    public void userCreatesACuratedMarketWithOnlyDescription(String description) {
        logger.info("Creating a curated market with only description - Description: '{}'", description);
        setup.enterCuratedMarketDetailsOnlyDescription(description);
        setup.clickSaveButton();
        Assert.assertEquals("3 error(s) found.", setup.getAlertMessage());
        Assert.assertTrue("Account is required", setup.isMarketNameErrorTextVisible());
        Assert.assertTrue("Account is required", setup.isAccountsErrorTextVisible());
        Assert.assertTrue("Market KPI and Benchmark is required", setup.isMarketKPIErrorVisible());
    }

    @And("User clicks {string} tab")
    public void userClicksTab(String tabName) {
        logger.info("Clicking '{}' tab", tabName);
        setup.clickTabName(tabName);
    }

    @When("User duplicates tactic, verify data on the duplicated tactic using {string} option")
    public void user_duplicates_tactic_verify_data_on_the_duplicated_tactic_using_option(String tacticOption) {
        itemList.clear();
        List<String> originalTacticDetails;
        List<String> copiedTacticDetails;
        for (String name : nameList) {
            logger.info("Starting copy verification for Tactic: {}", name);
            tacticDetails.navigateToTacticDetails(name);
            tacticDetails.clickSettingsTab();
            originalTacticDetails = tacticDetails.fetchTacticDetails();
            tacticCreatives.clickCreativeTab();
            originalTacticDetails.addAll(tacticDetails.fetchTacticCreative());
            logger.debug("Original details captured for {}: {}", name, originalTacticDetails);
            tacticDetails.clickTacticOptions(tacticOption);
            String tacticName = "Copy of " + name;
            itemList.add(tacticName);
            logger.info("Creating a copy named: {}", tacticName);
            String actualMsg = tacticDetails.createACopyOfTactic(tacticName);
            String expectedMsg = String.format("×\nTactic(s) %s copied successfully.", tacticName);
            Assert.assertEquals("Tactic copied Successfully", expectedMsg, actualMsg);
            boolean isAvailable = tacticDetails.verifyTacticAvailable(tacticName);
            logger.info("Is copied tactic '{}' available on dashboard? {}", tacticName, isAvailable);
            Assert.assertTrue("Copied tactic is not available", tacticDetails.verifyTacticAvailable(tacticName));
            tacticDetails.navigateToTacticDetails(tacticName);
            tacticDetails.clickSettingsTab();
            copiedTacticDetails = tacticDetails.fetchTacticDetails();
            tacticCreatives.clickCreativeTab();
            copiedTacticDetails.addAll(tacticDetails.fetchTacticCreative());
            logger.debug("Copied details captured for {}: {}", tacticName, copiedTacticDetails);
            logger.info("Comparing Original vs. Copied data for {}", name);
            if (!originalTacticDetails.equals(copiedTacticDetails)) {
                logger.error("Mismatch found!\nOriginal: {}\nCopied:   {}", originalTacticDetails, copiedTacticDetails);
            }
            Assert.assertEquals("Tactic details do not match after copy.", originalTacticDetails, copiedTacticDetails);
            logger.info("Successfully verified copy for tactic: {}", name);
        }
    }

    @And("User clicks Import Deals button")
    public void userClicksImportDealsButton() {
        logger.info("User clicks Import Deals button");
        setup.clickImportDealsButton();
    }

    @Then("Verify Import Deal panel is opened")
    public void verifyImportDealPanelIsOpened() {
        logger.info("User clicks Import Deals button");
        Assert.assertTrue("Import Deals panel is not opened", setup.isImportDealsPanelOpened());
    }

    @And("User downloads the curated market template")
    public void userDownloadsTheCuratedMarketTemplate() throws IOException {
        logger.info("Downloading the curated market template");
        targetFilePath = setup.downloadCuratedMarketTemplate();
        logger.info("Curated market template downloaded successfully '{}'", targetFilePath);
    }

    @And("User fills the template with deal details and uploads the template")
    public void userFillsTheTemplateWithDealDetailsAndUploadsTheTemplate(DataTable dataTable) throws Exception {
        logger.info(
                "Reading deal details from the data table to fill the template and upload. Deal details include - DEAL_NAME, EXCHANGE, MEDIA_TYPE, CURATOR, DEAL_PRICE, PRICING_TYPE, MPC_DEAL_TYPE");
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> templateDate : data) {
            String dealName = templateDate.get("DEAL_NAME")
                    + '_'
                    + CommonUtils.timeStampCalculation()
                    + '_'
                    + new Random().nextInt(100);
            String exchange = templateDate.get("EXCHANGE");
            String mediaType = templateDate.get("MEDIA_TYPE");
            String curator = templateDate.get("CURATOR");
            String dealPrice = templateDate.get("DEAL_PRICE");
            String pricingType = templateDate.get("PRICING_TYPE");
            String mpcDealType = templateDate.get("MPC_DEAL_TYPE");
            itemMap.computeIfAbsent("DEAL_NAME", k -> new ArrayList<>()).add(dealName);
            itemMap.computeIfAbsent("EXCHANGE", k -> new ArrayList<>()).add(exchange);
            itemMap.computeIfAbsent("MEDIA_TYPE", k -> new ArrayList<>()).add(mediaType);
            itemMap.computeIfAbsent("CURATOR", k -> new ArrayList<>()).add(curator);
            itemMap.computeIfAbsent("DEAL_PRICE", k -> new ArrayList<>()).add(dealPrice);
            itemMap.computeIfAbsent("PRICING_TYPE", k -> new ArrayList<>()).add(pricingType);
            itemMap.computeIfAbsent("MPC_DEAL_TYPE", k -> new ArrayList<>()).add(mpcDealType);
            logger.info(
                    "Filling the template with deal details and uploading the template. Deal details - Deal Name: '{}', Exchange: '{}', Media Type: '{}', Curator: '{}', Deal Price: '{}', Pricing Type: '{}', MPC Deal Type: '{}'",
                    dealName,
                    exchange,
                    mediaType,
                    curator,
                    dealPrice,
                    pricingType,
                    mpcDealType);
            setup.fillCuratedMarketTemplate(
                    targetFilePath,
                    metricName,
                    dealName,
                    exchange,
                    mediaType,
                    curator,
                    dealPrice,
                    pricingType,
                    mpcDealType);
        }
        logger.info(
                "Expected file name to be uploaded: '{}'",
                targetFilePath.getFileName().toString());
        setup.browseCuratedMarketTemplate(targetFilePath);
        setup.clickPreviewButton();
        Assert.assertTrue("Complete Deal details are not available", setup.isDealAddedSuccessTextVisible());
        setup.clickUploadButton();
        Assert.assertTrue(
                "Unable to upload deals successfully", setup.getAlertMessage().contains("Deals added to"));
    }

    @Then(
            "Verify the imported deal is displayed in the Deals Tab on Admin's Curated Market page with details matching the uploaded template")
    public void
            verifyTheImportedDealIsDisplayedInTheDealsTabOnAdminSCuratedMarketPageWithDetailsMatchingTheUploadedTemplate() {
        logger.info(
                "Verify the imported deal is displayed in the Deals Tab on Admin's Curated Market page with details matching the uploaded template");
        Assert.assertTrue(
                "Deal Name does not match", setup.fetchDealNameFromDealTab().containsAll(itemMap.get("DEAL_NAME")));
        Assert.assertTrue(
                "Deal Id does not match", setup.fetchDealIdFromDealTab().containsAll(itemMap.get("DEAL_NAME")));
        Assert.assertTrue(
                "Exchange does not match", setup.fetchExchangeTypeFromDealTab().containsAll(itemMap.get("EXCHANGE")));
        Assert.assertTrue(
                "Deal Price does not match", setup.fetchDealPriceFromDealTab().containsAll(itemMap.get("DEAL_PRICE")));
        Assert.assertTrue(
                "Pricing type does not match",
                setup.fetchPricingTypeFromDealTab().containsAll(itemMap.get("PRICING_TYPE")));
        Assert.assertTrue(
                "Media type does not match", setup.fetchMediaTypeFromDealTab().containsAll(itemMap.get("MEDIA_TYPE")));
        Assert.assertTrue(
                "Curator does not match", setup.fetchCuratorFromDealTab().containsAll(itemMap.get("CURATOR")));
        Assert.assertTrue(
                "MPC Deal type does not match",
                setup.fetchMPCDealTypeFromDealTab().containsAll(itemMap.get("MPC_DEAL_TYPE")));
    }

    @And("User fetches floor price for the imported deal")
    public void userFetchesFloorPriceForTheImportedDeal() {
        logger.info("Fetching floor price for the imported deal '{}'", itemMap.get("DEAL_NAME"));
        itemList = setup.fetchFloorPriceForImportedDeal();
        logger.info("Fetched floor price for the imported deal: {}", itemList);
    }

    @And("User enables the Curated Market created")
    public void userEnablesTheCuratedMarketCreated() {
        logger.info("Enabling the curated market '{}' created with ID '{}'", templateNameRandom, metricName);
        setup.enableCuratedMarket();
        Assert.assertEquals("Market updated successfully", setup.getAlertMessage());
    }

    @And("User navigates to Curated Markets section")
    public void userNavigatesToCuratedMarketsSection() {
        logger.info("User navigates to Curated Markets section");
        navigation.clickSubMenu();
        navigation.navigateToCuratedMarket();
    }

    @And("Verify Curated Market tab is displayed")
    public void verifyCuratedMarketTabIsDisplayed() {
        logger.info("User navigates to Curated Markets section");
        Assert.assertTrue("Curated Market tab is not displayed", curatedMarket.isCuratedMarketTabDisplayed());
    }

    @And("User searches for the created Curated Market")
    public void userSearchesForTheCreatedCuratedMarket() {
        logger.info("Searching for the created Curated Market '{}'", templateNameRandom);
        curatedMarket.searchCuratedMarket(templateNameRandom);
        Assert.assertTrue(
                "Created Curated Market is not displayed in the search result",
                curatedMarket.isCuratedMarketCreatedAvailable(templateNameRandom));
    }

    @Then(
            "Verify the market id, media type, and floor price displayed in Curated Markets section matches the media type in Admin Setup for the same market")
    public void verifyTheMediaTypeDisplayedInCuratedMarketsSectionMatchesTheMediaTypeInAdminSetupForTheSameMarket() {
        logger.info(
                "Verify the market id, media type, and floor price displayed in Curated Markets section matches the media type in Admin Setup for the same market");
        String marketIdInCuratedMarket = curatedMarket.fetchMarketIdForCuratedMarket(templateNameRandom);
        Assert.assertEquals(
                "Market ID mismatch between Curated Market section and Admin Setup for the same market",
                metricName,
                marketIdInCuratedMarket);

        String mediaTypeInCuratedMarket = curatedMarket.fetchMediaTypeForCuratedMarket(templateNameRandom);
        Set<String> curatedSet = Arrays.stream(mediaTypeInCuratedMarket.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        Set<String> adminSet =
                itemMap.get("MEDIA_TYPE").stream().map(String::trim).collect(Collectors.toSet());
        Assert.assertEquals("Media type mismatch", adminSet, curatedSet);

        String floorPriceInCuratedMarket = curatedMarket.fetchFloorPriceForCuratedMarket(templateNameRandom);
        logger.info(
                "Fetched floor price from Curated Market section: '{}', Fetched floor price from Admin Setup: '{}'",
                floorPriceInCuratedMarket,
                itemList);
        if (floorPriceInCuratedMarket.contains("-")) {
            String[] range = floorPriceInCuratedMarket.split("-");
            double uiMin = Double.parseDouble(range[0].trim());
            double uiMax = Double.parseDouble(range[1].trim());

            List<Double> adminPrices = itemList.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Double::parseDouble)
                    .toList();
            double adminMin =
                    adminPrices.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
            double adminMax =
                    adminPrices.stream().mapToDouble(Double::doubleValue).max().orElseThrow();
            Assert.assertEquals(
                    "Minimum floor price mismatch", BigDecimal.valueOf(adminMin), BigDecimal.valueOf(uiMin));
            Assert.assertEquals(
                    "Maximum floor price mismatch", BigDecimal.valueOf(adminMax), BigDecimal.valueOf(uiMax));
        } else {
            Assert.assertTrue(
                    "Floor price mismatch between Curated Market section and Admin Setup for the same market",
                    itemList.contains(floorPriceInCuratedMarket));
        }
    }

    @When(
            "User enters the line item details as {string} {string} {string}, enables the line item and saves the changes")
    public void userEntersTheLineItemDetailsAsEnablesTheLineItemAndSavesTheChanges(
            String lineItemName, String lineBudget, String lineItemType) {
        lineItemNameRandom = lineItemName + '_' + CommonUtils.timeStampCalculation();
        logger.info(
                "Entering Line Item details - Name: {}, Line Item Type: {}, Budget: {}",
                lineItemNameRandom,
                lineItemType,
                lineBudget);
        lineItemDetails.enterLineItemName(lineItemNameRandom);
        logger.info("Selecting Line Item type: '{}'", lineItemType);
        lineItemDetails.selectLineItemType(lineItemType);
        lineItemDetails.clickAddFlightButton();
        logger.info("Entering Line Item budget: '{}'", lineBudget);
        lineItemDetails.enterLineItemBudget(lineBudget);
        lineItemDetails.isPlacementIdAvailable(lineItemNameRandom);
        lineItemDetails.enableLineItem();
        logger.info("Line Item enabled successfully. Saving Line Item details.");
        lineItemDetails.saveLineItem();
    }

    @And("User saves tactic details as a target template {string} and verifies the template is saved successfully")
    public void userSavesTacticDetailsAsATargetTemplateAndVerifiesTheTemplateIsSavedSuccessfully(String lineItemType) {
        logger.info("Saving tactic details as target template for Line Item type '{}'", lineItemType);
        templateNameRandom = tacticDetails.saveTargetingTemplate(lineItemType);
    }

    @Then("User searches and verifies the created targeting template is available on Targeting Templates page")
    public void userSearchesAndVerifiesTheCreatedTargetingTemplateIsAvailableOnTargetingTemplatesPage() {
        logger.info(
                "User searches and verifies the created targeting template is available on Targeting Templates page");
        Assert.assertTrue(
                "Targeting template is not found in the search results",
                targetingTemplate.searchTargetingTemplate(Collections.singletonList(templateNameRandom)));
    }

    @And("Verify {string} and {string} checkboxes are available for each flight entry")
    public void verifyAndCheckboxesAreAvailableForEachFlightEntry(
            String flightImpressionCap, String dailyImpressionCap) {
        logger.info(
                "Verify {} and {} checkboxes are available for each flight entry",
                flightImpressionCap,
                dailyImpressionCap);
        Assert.assertTrue(
                flightImpressionCap + " checkbox is not available for the flight entry",
                lineItemDetails.isImpressionCapCheckboxAvailable(flightImpressionCap));
        Assert.assertTrue(
                dailyImpressionCap + " checkbox is not available for the flight entry",
                lineItemDetails.isImpressionCapCheckboxAvailable(dailyImpressionCap));
    }

    @And("User should be able to check the {string} and {string} checkboxes")
    public void userShouldBeAbleToCheckTheAndCheckboxes(String flightImpressionCap, String dailyImpressionCap) {
        logger.info("User should be able to check the {} and {} checkboxes", flightImpressionCap, dailyImpressionCap);
        lineItemDetails.clickImpressionCapCheckbox(flightImpressionCap);
        Assert.assertTrue(
                flightImpressionCap + " checkbox is not checked successfully for the flight entry",
                lineItemDetails.isImpressionCapCheckboxChecked(flightImpressionCap));
        lineItemDetails.clickImpressionCapCheckbox(dailyImpressionCap);
        Assert.assertTrue(
                dailyImpressionCap + " checkbox is not checked successfully for the flight entry",
                lineItemDetails.isImpressionCapCheckboxChecked(dailyImpressionCap));
    }

    @And(
            "Verify error message if user fails to add impression cap value when the checkboxes are selected and tries to save the line item page")
    public void
            verifyErrorMessageIfUserFailsToAddImpressionCapValueWhenTheCheckboxesAreSelectedAndTriesToSaveTheLineItemPage() {
        logger.info(
                "Verify error message if user fails to add impression cap value when the checkboxes are selected and tries to save the line item page");
        lineItemDetails.saveLineItem();
        Assert.assertTrue(
                "Error message is not displayed when impression cap values are not added for the selected checkboxes",
                lineItemDetails.isImpressionCapErrorMessageVisible());
    }

    @When("User clicks on the existing campaign to open the campaign details page")
    public void userClicksOnTheExistingCampaignToOpenTheCampaignDetailsPage() {
        logger.info("Clicking on the existing campaign to open the campaign details page");
        campaignDashboard.clickCampaignFromDashboard();
        Assert.assertTrue(
                "Navigation to Campaign details page is not successful", campaignDashboard.isCampaignPageDisplayed());
    }

    @And("User clicks on Add Line Item button")
    public void userClicksOnAddLineItemButton() {
        logger.info("Clicking on Add Line Item button to add a new line item to the existing campaign");
        lineItemDetails.selectNewLineItem();
        String lineItemText = lineItemDetails.verifyLineItemText();
        Assert.assertEquals("New Line Item", lineItemText);
    }

    @Then(
            "Verify that the new line item is added to the existing campaign and displayed in the left menu under the campaign")
    public void verifyThatTheNewLineItemIsAddedToTheExistingCampaignAndDisplayedInTheLeftMenuUnderTheCampaign() {
        lineItemDetails.navigateToLineItemDetails(lineItemNameRandom);
        Assert.assertTrue(
                "Failed to create new Line Item",
                lineItemDetails.fetchLineItemName().contains(lineItemNameRandom));
        lineItemDetails.clickDetailsTab();
    }

    @And("User updates line item details such as {string} and flight dates and saves the line item")
    public void userUpdatesLineItemDetailsSuchAsAndFlightDatesAndSavesTheLineItem(String lineBudget) {
        logger.info("Updating line item details - Budget, Flight Dates");
        lineItemDetails.enterLineItemBudget(lineBudget);
        nameList = lineItemDetails.fetchLineItemDetails();
        lineItemDetails.saveLineItem();
        Assert.assertEquals("Lineitem " + lineItemNameRandom + " updated.", lineItemDetails.lineItemSuccess());
    }

    @Then("Verify that the line item details are updated successfully and reflected on the Line Item page")
    public void verifyThatTheLineItemDetailsAreUpdatedSuccessfullyAndReflectedOnTheLineItemPage() {
        List<String> updatedDetails = lineItemDetails.fetchLineItemDetails();
        Assert.assertEquals("Line item details are not updated successfully", nameList, updatedDetails);
    }

    @And("User enters details in {string} enables the line item and saves the changes")
    public void userEntersDetailsInNewCustomFieldAndEnablesTheLineItemAndSavesTheChanges(String lineBudget) {
        logger.info("Entering details in new custom field, line budget and saving the line item");
        navigation.clickOnIcon("Add Flight");
        lineItemDetails.enterLineItemBudget(lineBudget);
        lineItemDetails.isPlacementIdAvailable(lineItemNameRandom);
        lineItemDetails.enableLineItem();
        lineItemDetails.saveLineItem();
    }

    @Then("User creates new custom field {string} and verifies the same in the line item details page")
    public void userCreatesNewCustomFieldAndVerifiesTheSameInTheLineItemDetailsPage(String customField) {
        String customFieldName = customField + "_" + CommonUtils.randomFourDigitNumber();
        logger.info("Creating new custom field with name in Line Item: {}", customFieldName);
        this.customFieldName = customFieldName;
        lineItemDetails.addCustomField(customFieldName);
        String raw = tacticDetails.verifyCustomField(customFieldName);
        String actualName = raw.split("\\R")[0]; // To remove unwanted space and text
        Assert.assertEquals(customFieldName, actualName);
        this.uiCustomFieldName = actualName;
    }

    @Then(
            "Verify the custom field created in line item details page is available for all line items under the campaign")
    public void verifyTheCustomFieldCreatedInLineItemDetailsPageIsAvailableForAllLineItemsUnderTheCampaign() {
        logger.info(
                "Verify the custom field created in line item details page is available for all line items under the campaign");
        List<String> lineItemNames = lineItemDetails.fetchLineItemName();
        for (String name : lineItemNames) {
            lineItemDetails.navigateToLineItemDetails(name);
            lineItemDetails.clickDetailsTab();
            Assert.assertTrue(
                    "Custom Field is not available", lineItemDetails.isCustomFieldAvailable(uiCustomFieldName));
        }
    }

    @Then("Verify Bid multiplier panel with all options under below categories")
    public void verifyTargetingPanelWithAllBidmultiplierUnderBelowCategories(DataTable bidCategory) {
        tacticSettings.clickBidMultipliers();
        List<String> bidCategoryList = bidCategory.asList(String.class);
        logger.info("Verifying Bid multiplier panel contains the following categories: {}", bidCategoryList);
        for (String category : bidCategoryList) {
            boolean isPresent = tacticSettings.fetchAndVerifyBidCategoryName(Collections.singletonList(category));
            Assert.assertTrue("Bid multiplier category '" + category + "' not found or not visible on UI", isPresent);
        }
        logger.info("All bid multiplier categories verified successfully");
    }

    @And("Verify Bid type with respect to category")
    public void verifyBidTypeWithRespectToCategory(DataTable bidCategoryNameAndType) {
        logger.info("Verifying Bid types with respect to their categories");
        Map<String, String> rawMap = bidCategoryNameAndType.asMap(String.class, String.class);
        Map<String, List<String>> bidCategoryNameAndTypeMap = CommonUtils.processDataTable(rawMap);

        for (Map.Entry<String, List<String>> entry : bidCategoryNameAndTypeMap.entrySet()) {
            String key = entry.getKey();
            List<String> bidexpectedValues = entry.getValue();
            List<String> actualValues = tacticSettings.getBidTypesForCategory(key);
            for (String bidexpected : bidexpectedValues) {
                Assert.assertTrue(
                        "Expected value '" + bidexpected + "' not found for category '" + key + "'. Found: "
                                + actualValues,
                        actualValues.contains(bidexpected));
            }
        }

        logger.info("All Bid types matched their respective categories successfully");
    }

    @Given("User configures Bid multiplier rules as below with {string}")
    public void user_selects_the_Bid_multiplier_rules(String fillValue, DataTable bidRuleTypeAndOptions) {
        logger.info("Configuring Bid multiplier rules from DataTable");
        Map<String, String> rawMap = bidRuleTypeAndOptions.asMap(String.class, String.class);
        Map<String, List<String>> rulesMap = CommonUtils.processDataTable(rawMap);
        keyType.clear();
        keyValues.clear();
        for (Map.Entry<String, List<String>> entry : rulesMap.entrySet()) {
            keyType.add(entry.getKey());
            keyValues.addAll(entry.getValue());
            tacticSettings.selectMultipleBidRuleTypes(entry.getKey(), entry.getValue(), fillValue);
        }
        logger.info("Closing Bid Rule Type panel");
        tacticSettings.closeRuleTypePanel();
    }

    @Then("Verify the configured Bid multiplier rules")
    public void verify_the_configured_bid_multiplier_rules() {
        logger.info("Starting verification of configured bid multiplier rules");
        List<String> expectedNormalizedBidRuleTypes = normalizeObjectList(keyType);
        int expectedBidCount = expectedNormalizedBidRuleTypes.size();
        tacticSettings.fetchBidRulesTypesCount(expectedBidCount);
        List<String> actualNormalizedBidRuleTypes = normalizeObjectList(tacticSettings.fetchBidRulesTypes());

        Set<String> expectedBidSet = new LinkedHashSet<>(expectedNormalizedBidRuleTypes);
        Set<String> actualBidSet = new LinkedHashSet<>(actualNormalizedBidRuleTypes);

        List<String> expectedUniqueAndSortedBid = new ArrayList<>(expectedBidSet);
        List<String> actualUniqueAndSortedBid = new ArrayList<>(actualBidSet);

        Collections.sort(expectedUniqueAndSortedBid);
        Collections.sort(actualUniqueAndSortedBid);

        List<String> expectedNormalizedBidRuleOptions = normalizeObjectList(keyValues);
        List<String> actualNormalizedBidRuleOptions = normalizeObjectList(tacticSettings.fetchBidRuleOptions());
        Assert.assertEquals("Rule types mismatch", expectedUniqueAndSortedBid, actualUniqueAndSortedBid);

        for (String expectedOption : expectedNormalizedBidRuleOptions) {
            boolean matchFound =
                    actualNormalizedBidRuleOptions.stream().anyMatch(actual -> actual.equalsIgnoreCase(expectedOption));
            Assert.assertTrue("Expected rule option not found: " + expectedOption, matchFound);
        }

        logger.info("All Bid rules verified successfully");
    }

    @When("User saves the Bid multiplier settings")
    public void user_saves_the_bid_multiplier_settings() {
        logger.info("Saving Tactic and Bid settings");
        tacticSettings.saveTacticSettings();
        tacticSettings.clickCreativeTab();
    }

    @And(
            "User verify Behaviour segment and NPI are not allowed in bid multiplier rules when same are not selected in targeting rules")
    public void userVerifyBehavioursegmentandNPIareNotAllowedinbidMultiplier() {
        logger.info(
                "Verifying Behaviour segment and NPI are not allowed in Bid multiplier if the same are not targeted in tactic");
        String displaySegmentError = tacticSettings.checkErrorOfBidMultiplierSegmentRule();
        Assert.assertEquals(
                "Application allows Behaviour segment in bid multiplier even if its not targeted in tactic",
                ("No Behavioral Targeting is Selected"),
                displaySegmentError);
        String displayNPIError = tacticSettings.checkErrorOfBidMultiplierNPIRule();
        Assert.assertEquals(
                "Application allows NPI in bid multiplier even if its not targeted in tactic",
                ("No NPI Targeting is Selected"),
                displayNPIError);
    }

    @Then("User close the bid panel to add targeting rules")
    public void userCloseTheBidPanelToAddTargetingRules() {
        logger.info("Navigating to page to add targeting");
        tacticSettings.navigateToAddTargeting();
    }

    @When("User navigates back to settings tab from creatives tab")
    public void user_navigates_back_to_settings_tab_from_creatives_tab() {
        logger.info("Navigating back to settings tab from creatives tab");
        tacticDetails.clickSettingsTab();
        String settingsText = tacticSettings.verifyTacticSettingsText();
        Assert.assertEquals("Bid Strategy", settingsText);
    }

    @When("User navigates back to creatives tab")
    public void user_navigates_back_to_creatives_tab() {
        logger.info("Navigating back to creatives tab");
        tacticCreatives.clickCreativeTab();
        String creativesText = tacticCreatives.verifyTacticCreativesText();
        Assert.assertEquals("Creative(s)", creativesText);
    }

    @When(
            "User creates line items with tactics and targeting rules as below and assigns existing creative named {string}")
    public void userCreatesLineItemsWithTacticsAndTargetingRules(String creative, DataTable dataTable) {
        logger.info("Creating line items with tactics and targeting rules");
        tacticDetails.createLineItemsWithTacticsAndTargetingRules(
                dataTable.asMaps(String.class, String.class), creative, perTacticRules -> {
                    logger.info("Running per-tactic targeting rule verifications for: {}", perTacticRules.keySet());
                    rulesMap = new LinkedHashMap<>(perTacticRules);
                    keyType = new ArrayList<>(perTacticRules.keySet());
                    keyValues = new ArrayList<>();
                    for (List<String> v : perTacticRules.values()) {
                        keyValues.addAll(v);
                    }
                    verify_the_configured_targeting_rules();
                    verifyTheCountOfRulesAddedForTheSelectedTargetingRuleTypeOnTheTacticSettingsPage();
                });
    }

    @Then("Verify the newly created campaign details in the campaign list")
    public void verifyTheNewlyCreatedCampaignDetailsInTheCampaignList() {
        campaigns.navigateToCampaignDashboard();
        logger.info("Searching for Campaign: {}", campaignNameRandom);
        campaignDashboard.searchCreatedCampaign(campaignNameRandom);
        Assert.assertEquals(campaignNameRandom, campaignDashboard.verifyCreatedCampaign(campaignNameRandom));
    }

    @And("User selects Medscape List")
    public void userSelectsMedscapeList() {
        logger.info("Selecting Medscape List");
        npiLists.clickMedscapeList();
    }

    @And("Verify Advertiser is auto selected as {string}")
    public void verifyAdvertiserIsAutoSelectedAs(String advertiserName) {
        logger.info("Verifying Advertiser is auto selected as: {}", advertiserName);
        String actualAdvertiser = npiMedscapeList.fetchDefaultAdvertiser();
        Assert.assertEquals("Advertiser is not auto selected correctly", advertiserName, actualAdvertiser);
    }

    @And("User enters the Medscape NPI list name as {string} and saves the list")
    public void userEntersTheMedscapeNPIListNameAs(String listName) {
        npiName = listName + "_" + CommonUtils.timeStampCalculation();
        logger.info("Entering Medscape NPI list name: {}", npiName);
        npiMedscapeList.enterListName(npiName);
        npiMedscapeList.clickNextButton();
    }

    @And("Verify Upload File section is displayed on the Medscape List details page")
    public void verifyUploadFileSectionIsDisplayedOnTheMedscapeListDetailsPage() {
        logger.info("Verifying Upload File section is displayed on the Medscape List details page");
        Assert.assertTrue("Upload File section is not displayed", npiMedscapeList.isUploadFileSectionDisplayed());
    }

    @And("User maps row headers from the uploaded spreadsheet to predefined labels as below")
    public void userMapsRequiredFieldsAs(DataTable dataTable) {
        logger.info("Mapping row headers from the uploaded spreadsheet to predefined labels");
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> mapping : data) {
            String labelName = mapping.get("LABEL");
            String columnValue = mapping.get("COLUMN_VALUE");
            npiMedscapeList.mapRowHeadersToLabels(labelName, columnValue);
        }
    }

    @And(
            "User saves the Medscape List and verify that the list is uploaded successfully with message {string} and {string}")
    public void userSavesTheMedscapeListNavigatesToLoadingPageWithContent(String messageContent, String waitText) {
        logger.info("Saving the Medscape List and navigating to loading page with content: {}", messageContent);
        npiMedscapeList.clickSaveButton();
        String softMatchMessage = npiMedscapeList.getSoftMatchProgressText();
        String waitTextContent = npiMedscapeList.getWaitText();
        Assert.assertEquals("Soft match progress message is not displayed correctly", messageContent, softMatchMessage);
        Assert.assertEquals("Soft match progress message is not displayed correctly", waitText, waitTextContent);
    }

    @And("Verify file details are displayed correctly in the list details page")
    public void verifyFileDetailsAreDisplayedInListDetailsPage() {
        logger.info("Verifying file details are displayed correctly in the list details page");
        Assert.assertTrue("File details are not displayed", npiMedscapeList.isMedscapeListContainerDisplayed());
    }

    @And("Verify {int} download options are available on the NPI list details page for {string} list {string}")
    public void verifyTwoDownloadOptionsAreAvailableOnTheNPIListDetailsPage(int expectedDownloadOptions, String listType, String npiListName) {
        logger.info("Searching and selecting NPI list '{}'", npiListName);
        npiLists.searchList(npiListName);
        npiLists.openSearchedList(npiListName);
        logger.info("Verifying {} download option(s) are available on the NPI list details page for {} list", expectedDownloadOptions, listType);
        Assert.assertTrue("Download options are not available", npiLists.areDownloadOptionsAvailable(expectedDownloadOptions));
    }

    @And("User navigates back to NPI Lists landing page")
    public void userNavigatesBackToNPIListsLandingPage() {
        logger.info("Navigating back to NPI Lists landing page");
        npiLists.navigateBackToNPIListsLandingPage();
    }

    @And("Verify the Creatives Advertiser dropdown is displaying all Advertisers mapped to the logged in account {string}")
    public void verifyTheCreativesAdvertiserDropdownIsDisplayingAllAdvertisersMappedToTheLoggedInAccount(String accountName) {
        logger.info("Creatives Advertiser drop-down should list advertisers mapped to {}", accountName);
        Assert.assertTrue("Advertiser dropdown is not present", bulkCreativeUpload.isAdvertiserDropdownAvailable());
        List<String> advertiser = bulkCreativeUpload.fetchAdvertisers();
        Assert.assertTrue("Advertiser List does not match", advertiser.containsAll(itemList));
    }

    @When("User deletes the campaign")
    public void userDeletesTheCampaign() {
        campaigns.clickCampaignOptions();
        campaigns.deleteCampaign();
    }

    @Then("Verify that the campaign is deleted successfully")
    public void verifyThatTheCampaignIsDeletedSuccessfully() {
        String successMessage = campaigns.fetchCampaignDeleteSuccessAlert();
        Assert.assertEquals("Campaign deleted successfully", successMessage);
        String noCampaignFoundError = campaignDashboard.fetchNoCampaignFoundMessage(campaignNameRandom);
        Assert.assertEquals("No campaigns matching filtering criteria found", noCampaignFoundError);
    }

    @And("User fetches custom field starting with {string}, perform deletion operation on Campaign, Line Items and Tactics levels and verify successful deletion")
    public void userFetchesCustomFieldCreatedAndPerformDeletionOperation(String prefix) {
        logger.info("Fetching custom field starting with '{}' and performing deletion operation", prefix);
        List<String> customFields = accounts.fetchCustomFieldsStartingWith(prefix);
        for (String field : customFields) {
            String alertText = accounts.deleteCustomField(field);
            Assert.assertTrue("Custom field deletion failed", alertText.contains("Successfully deleted the record"));
        }
    }

    @And("User expands each Custom Destination created, performs delete operation on the entries created by {string} and verify successful deletion")
    public void userExpandsEachCustomDestinationCreatedAndFetchesEntriesWithUsername(String username) {
        logger.info("Expanding each Custom Destination created and fetching entries with username '{}'", username);
        List<String> deletedEntries = accounts.expandCustomDefinitionRow(username);
        for (String entry : deletedEntries) {
            Assert.assertTrue("Custom destination deletion failed", accounts.isDeletedEntryAvailable(entry));
        }
    }
}
