package stepdefinitions;

import factory.DriverFactory;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.Navigation;
import pages.admin.Accounts;
import pages.life.NPILists;
import pages.studio.*;
import utils.CommonUtils;
import utils.ConfigReader;
import utils.Constants;
import utils.FileActions;

public class StudioSteps {
    private static final Logger logger = LoggerFactory.getLogger(StudioSteps.class);
    static String workspaceName;
    static String newWorkspaceName;
    static String draftOption;
    Boolean flag = true;
    Boolean isOverwritten = false;
    List<String[]> fileContent;
    List<String> fileContentData;
    Accounts accounts = new Accounts(DriverFactory.getPage());
    Navigation navigation = new Navigation(DriverFactory.getPage());
    WorkspaceCreation workspaceCreation = new WorkspaceCreation(DriverFactory.getPage());
    ExpansionWorkspace expansionWorkspace = new ExpansionWorkspace(DriverFactory.getPage());
    ExplorerWorkspace explorerWorkspace = new ExplorerWorkspace(DriverFactory.getPage());
    Workspace workspace = new Workspace(DriverFactory.getPage());
    BrandExplorerWorkspace brandExplorerWorkspace = new BrandExplorerWorkspace(DriverFactory.getPage());
    NPILists npiLists = new NPILists(DriverFactory.getPage());
    List<String> appliedFilterEntries = new ArrayList<>();
    List<String> appliedFilterValues = new ArrayList<>();
    List<String> previousNpiDetails = null;
    List<String> metricNames = new ArrayList<>();
    List<String> fetchedMetricNames = new ArrayList<>();
    String npiCount;
    String totalNpiCount;
    Path targetFilePath;

    @When("the user clicks on Create New Workspace")
    public void the_user_clicks_on_create_new_workspace() {
        logger.info("User clicks on Create New Workspace");
        workspaceCreation.clickCreateStudioWorkspace();
    }

    @And("the User navigate to studio")
    public void theUserNavigateToStudio() {
        logger.info("Navigating to Studio");
        navigation.navigateToLife();
        navigation.navigateToStudio();
    }

    @When("the user sees the types of workspaces they have permissions for")
    public void the_user_sees_the_types_of_workspaces_they_have_permissions_for() {
        String actualHCPExplorer = workspaceCreation.verifyHCPExplorer();
        logger.info("HCP Explorer: {}", actualHCPExplorer);
        Assert.assertEquals("HCP Explorer", actualHCPExplorer);
        String actualHCPEAudienceExpansion = workspaceCreation.verifyHCPAudienceExpansion();
        logger.info("HCP Audience Expansion: {}", actualHCPEAudienceExpansion);
        Assert.assertEquals("HCP Audience Expansion", actualHCPEAudienceExpansion);
    }

    @Then("the user selects the advertiser {string}")
    public void the_user_selects_the_advertiser(String advertiser) {
        logger.info("Selecting advertiser: {}", advertiser);
        DriverFactory.getPage().waitForLoadState();
        expansionWorkspace.clickAdvertiserDropdown(advertiser);
        DriverFactory.getPage().waitForLoadState();
    }

    @Then("the user selects Source Audience {string}")
    public void the_user_selects_source_audience(String sourceAudience) {
        logger.info("Selecting source audience: {}", sourceAudience);
        expansionWorkspace.selectSourceAudience(sourceAudience);
    }

    @Then("the user selects Expand With Care Team or Expand With Affiliation Graph and selects the value")
    public void the_user_selects_expand_with_care_team_or_expand_with_affiliation_graph_and_selects_the_value() {
        logger.info("Selecting Expand With Care Team and Expand With Affiliation Graph options");
        expansionWorkspace.selectExpandCareTeam();
        expansionWorkspace.selectExpandAffGraph();
    }

    @Then("the filters should be applied to the workspace")
    public void the_user_applies_the_following_filters() {
        logger.info("Applying filters to the workspace");
        expansionWorkspace.addFilter();
    }

    @Then("the user renames the workspace to {string}")
    public void the_user_renames_the_workspace_to_(String wName) {
        workspaceName = wName + CommonUtils.timeStampCalculation();
        logger.info("Renaming workspace to: {}", workspaceName);
        expansionWorkspace.renameExpansion(workspaceName);
    }

    @Then("the user saves the workspace and check the workspace is Saved")
    public void the_user_saves_the_workspace_and_check_the_workspace_is_Saved() {
        logger.info("Saving the workspace");
        expansionWorkspace.saveExpansionWorkspace();
        String workspaceSuccessMsg = explorerWorkspace.workspaceSuccess();
        logger.info("Workspace save message: {}", workspaceSuccessMsg);
        Assert.assertTrue("Unable to save workspace", workspaceSuccessMsg.contains("Workspace saved"));
    }

    @And("User enables the studio for {string} account")
    public void user_enables_the_studio_for_an_account(String accountName) {
        logger.info("Enabling Studio for account: {}", accountName);
        navigation.clickSubMenu();
        accounts.clickAdministration();
        accounts.selectAccountsTab();
        accounts.searchAccount(accountName);
    }

    @And("User navigates to workspace permissions")
    public void User_navigates_to_workspace_permissions() {
        if (accounts.studioToggleActive().isVisible()) {
            logger.info("Studio toggle already active");
        } else {
            logger.info("Enabling Studio toggle");
            accounts.enableStudio();
        }
    }

    @When("User selects the workspace types and saves the settings")
    public void user_selects_the_workspace_types_and_saves_the_settings() {
        if (accounts.studioToggleActive().isVisible()) {
            logger.info("Studio toggle active, skipping workspace settings");
        } else {
            logger.info("Configuring workspace settings");
            accounts.workSpaceSettings();
        }
    }

    // Removed this line from scenario: "enable Studio for an Account for internal users" since permission sync takes
    // 5-7 mins, causing script fail.
    @And("User should be able to see the enabled workspaces for {string} account under Studio")
    public void userShouldBeAbleToSeeTheEnabledWorkspacesForThatAccountUnderStudio(String accountName) {
        logger.info("Verifying enabled workspaces under Studio for account: {}", accountName);
        accounts.switchAccount(accountName);
        navigation.navigateToStudio();
        workspaceCreation.createWorkspace();
        String permission = accounts.verifyWorkspacePermission();
        logger.info("Workspace permission: {}", permission);
        Assert.assertEquals("HCP Explorer", permission);
    }

    @Then("Studio should be enabled for that account")
    public void Studio_should_be_enabled_for_that_account() {
        if (accounts.studioToggleActive().isVisible()) {
            logger.info("Studio toggle active, settings already saved");
        } else {
            logger.info("Saving Studio settings");
            accounts.saveStudioSettings();
        }
    }

    @And("User verifies if Studio appears in submenu for {string} account")
    public void userVerifiesIfStudioAppearsInSubmenuForAccount(String accountName) {
        logger.info("Verifying if studio option is visible in submenu for account: {}", accountName);
        navigation.clickPulsePointLogo();
        navigation.refreshPage();
        navigation.clickSubMenu();
        Assert.assertTrue(navigation.isStudioTitleVisible());
    }

    @And("User disables the studio permission for {string} account")
    public void userDisablesTheStudioPermissionForAnAccount(String accountName) {
        logger.info("Disabling Studio permission for account: {}", accountName);
        accounts.disableStudioForAccount(accountName);
    }

    @Then("User should not be able to see the studio permission for that account")
    public void userShouldNotBeAbleToSeeTheStudioPermissionForThatAccount() {
        logger.info("Verifying Studio permission is not visible for the account");
        accounts.verifyStudioMenu();
    }

    @When("User clicks on Create New Workspace")
    public void user_clicks_on_create_new_workspace() {
        String dashboard = workspaceCreation.studioDashboard();
        logger.info("Dashboard: {}", dashboard);
        Assert.assertEquals("Unable to click on Create New Workspace button", "Studio", dashboard);
        workspaceCreation.verifyStudioWorkspaceFrame();
        workspaceCreation.clickCreateStudioWorkspace();
    }

    @Then("User sees the types of workspaces they have permissions for")
    public void user_sees_the_types_of_workspaces_they_have_permissions_for() {
        fetchedMetricNames = workspaceCreation.fetchWorkspaceTypes();
        logger.info("Workspace permissions - Fetched: {}, Admin: {}", fetchedMetricNames, metricNames);
        Assert.assertTrue("Admin and Studio permissions don't match", metricNames.containsAll(fetchedMetricNames));
    }

    @And("User clicks on {string} workspace")
    public void userClicksOnWorkspace(String workspaceType) {
        logger.info("Selecting {} workspace", workspaceType);

        if (fetchedMetricNames.contains(workspaceType)) {
            String explorer =
                    switch (workspaceType) {
                        case "HCP Explorer" -> workspaceCreation.verifyHCPExplorer();
                        case "Brand Explorer" -> workspaceCreation.verifyBrandExplorer();
                        case "HCP Audience Expansion" -> workspaceCreation.verifyHCPAudienceExpansion();
                        default -> throw new IllegalArgumentException(
                                "Unsupported workspace verification: " + workspaceType);
                    };

            logger.info("{} permission: {}", workspaceType, explorer);
            Assert.assertEquals(workspaceType, explorer);
        }

        switch (workspaceType) {
            case "HCP Explorer" -> workspaceCreation.clickHCPExplorerWorkspace();
            case "Brand Explorer" -> workspaceCreation.clickBrandExplorerWorkspace();
            case "HCP Audience Expansion" -> workspaceCreation.clickHCPAudienceExpansionWorkspace();
            default -> throw new IllegalArgumentException("Unsupported workspace click: " + workspaceType);
        }
    }

    @And("User selects the advertiser {string}")
    public void userSelectsTheAdvertiser(String advertiser) {
        logger.info("Selecting advertiser: {}", advertiser);
        explorerWorkspace.selectAdvertiser(advertiser);
        // As confirmed by Nikhil ,Studio workspace creation alert is removed, so commenting the below code and
        // assertion for now.
        //        String alertText = workspaceCreation.isWorkspaceCreationAlertDisplayed();
        //        logger.info("Alert: {}", alertText);
        //        Assert.assertEquals("Workspace created successfully", alertText);
    }

    @And("User selects the advertiser {string} for HCP Audience Expansion workspace")
    public void userSelectsTheAdvertiserForHCPAudienceExpansionWorkspace(String advertiser) {
        logger.info("Selecting advertiser for HCP Audience Expansion workspace: {}", advertiser);
        expansionWorkspace.clickAdvertiserDropdown(advertiser);
    }

    @And("User updates the workspace name as {string}")
    public void userUpdatesTheWorkspaceNameAs(String wName) {
        workspaceName = wName + '_' + CommonUtils.timeStampCalculation();
        logger.info("Adding workspace name: {}", workspaceName);
        explorerWorkspace.waitForDashboardLoad();
        explorerWorkspace.clickEditWorkspace();
        explorerWorkspace.enterWorkspaceName(workspaceName);
        explorerWorkspace.saveWorkspaceName();
        explorerWorkspace.waitUntilAlertDisappears();
        explorerWorkspace.waitForDashboardLoad();
    }

    @When("User applies the filter and selects option")
    public void user_applies_the_filters_as_gender_and_age(DataTable dataTable) {
        logger.info("Applying filters to HCP Explorer workspace");
        explorerWorkspace.clickAddFilter();
        List<Map<String, String>> filters = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> row : filters) {
            String filterName = row.get("FilterName").trim();
            String filterOption = row.get("Option").trim();
            logger.info("Processing filter: {} with option: {}", filterName, filterOption);

            if (!filterOption.isEmpty()) {
                appliedFilterEntries.add(filterName);
                appliedFilterValues.add(filterOption);
                List<String> filterOptionList = CommonUtils.parseCommaSeparatedString(filterOption);
                explorerWorkspace.selectFilter(filterName, filterOptionList);
            }
            explorerWorkspace.clickFilterOKButton();
        }
    }

    @Then("User clicks on Ok and closes the filter popup")
    public void user_clicks_on_ok_and_closes_the_filter_popup() {
        logger.info("Applying filters and closing filter popup");
        explorerWorkspace.applyFilter();
    }

    @Then("Verify that the applied filters are displayed correctly")
    public void verify_that_the_applied_filters_are_displayed_correctly() {
        logger.info("Verifying applied filters are displayed correctly");
        List<String> displayedFilters = explorerWorkspace.verifyAllSelectedFilters();
        logger.info("Displayed filters from UI: {}", displayedFilters);
        for (String appliedFilter : appliedFilterEntries) {
            logger.info("Validating displayed filter for: {}", appliedFilter);
            String appliedNorm =
                    appliedFilter.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim();
            boolean matchFound = displayedFilters.stream().anyMatch(displayed -> {
                String displayedNorm =
                        displayed.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim();
                boolean exactMatch = displayedNorm.equals(appliedNorm);
                boolean singularPlural = displayedNorm.startsWith(appliedNorm.replaceAll("s$", ""));
                boolean wordMatch = Arrays.stream(appliedNorm.split(" "))
                        .anyMatch(word -> word.length() > 3 && displayedNorm.contains(word));
                boolean prescriptionRoot = appliedNorm.contains("prescri") && displayedNorm.contains("prescri");
                boolean diagnosisRoot = appliedNorm.contains("diagnos") && displayedNorm.contains("diagnos");

                if (exactMatch || singularPlural || wordMatch || prescriptionRoot || diagnosisRoot) {
                    logger.info(
                            "Match found → Applied: '{}' | Displayed: '{}' | Rules [exact={}, plural={}, word={}, prescriptionRoot={}, diagnosisRoot={}]",
                            appliedNorm,
                            displayedNorm,
                            exactMatch,
                            singularPlural,
                            wordMatch,
                            prescriptionRoot,
                            diagnosisRoot);
                    return true;
                }

                return false;
            });
            logger.info("Filter '{}' match found: {}", appliedFilter, matchFound);
            Assert.assertTrue("Applied filter not displayed: " + appliedFilter, matchFound);
        }
    }

    @Then("User saves the {string} workspace")
    public void user_saves_the_workspace(String workspaceType) {
        logger.info("Saving {} workspace", workspaceType);

        switch (workspaceType) {
            case "HCP Explorer":
                explorerWorkspace.saveExplorerWorkspace();
                break;
            case "Brand Explorer":
                brandExplorerWorkspace.saveBrandExplorerWorkspace();
                break;
            case "HCP Audience Expansion":
                expansionWorkspace.saveExpansionWorkspace();
                break;
        }
    }

    @Then("Verify the {string} Workspace is saved")
    public void verify_the_hcp_explorer_workspace_is_saved(String workspaceType) {
        String actualMessage = workspaceCreation.isWorkspaceCreationAlertDisplayed();
        logger.info("Save alert for {} workspace: {}", workspaceType, actualMessage);
        boolean isValid = actualMessage.equals("Workspace created successfully")
                || actualMessage.equals("Workspace saved successfully")
                || actualMessage.equals(
                        "Sent for asynchronous processing, forced by upstream dependencies - need to refresh upstream workspaces first");
        Assert.assertTrue("Unexpected message for " + workspaceType + " workspace: " + actualMessage, isValid);
        workspace.waitTillWorkspaceAlertHide();
    }

    @And("User clicks Edit button and updates workspace name to {string}")
    public void userClicksEditButtonAndUpdatesWorkspaceNameTo(String editedName) {
        explorerWorkspace.clickEditWorkspace();
        workspaceName = editedName + CommonUtils.timeStampCalculation();
        logger.info("Updating workspace name to: {}", workspaceName);
        explorerWorkspace.enterWorkspaceName(workspaceName);
        explorerWorkspace.saveWorkspaceName();
        explorerWorkspace.waitForDashboardLoad();
    }

    @Then("Verify the Workspace is updated with edited name")
    public void verifyTheHCPExplorerWorkspaceIsUpdated() {
        String header = explorerWorkspace.fetchWorkspaceHeader();
        logger.info("Workspace header: {}", header);
        Assert.assertEquals(workspaceName, header);
    }

    @And("Verify that advertiser field is disabled and displayed in {string} after saving the workspace")
    public void verifyIfAdvertiserIsDisabledAfterSavingTheWorkspace(String textColor) {
        String color = explorerWorkspace.isAdvertiserDisabled();
        logger.info("Advertiser field color: {}", color);
        Assert.assertEquals(textColor, color);
    }

    @Then("verify the file content")
    public void verify_the_file_content() {
        logger.info("Reading downloaded CSV file content");
        fileContent = FileActions.readAllDataAtOnce(ConfigReader.getProperty("csvFilePath"));
        fileContentData = new ArrayList<>();
        // To display the data from csv- Separate logic
        /*for (int i = 1; i < fileContent.size(); i++) {
           // System.out.println("Row " + i + ": " + String.join(", ", fileContent.get(i)));
            String data= Arrays.toString(fileContent.get(i));
            fileContentData.add(data);
        }*/
        for (String[] row : fileContent) {
            fileContentData.add(String.join(", ", row));
        }
        logger.info("CSV File Content: {}", fileContentData);
    }

    @When("Studio platform is available")
    public void studio_platform_is_available() {
        logger.info("Checking Studio platform availability");
        workspace.studio();
    }

    @And("User searches the {string} and selects it")
    public void userSearchesTheAndSelectsIt(String workspace) {
        logger.info("Verifying Studio workspace frame is visible");
        workspaceCreation.verifyStudioWorkspaceFrame();
        logger.info("Searching and opening workspace: {}", workspaceName);
        workspaceCreation.searchWorkspaceName(workspace);
        workspaceCreation.clickWorkspace(workspace);
        boolean navigated = workspaceCreation.navigateToWorkspace(workspace);
        logger.info("Workspace navigation result: {}", navigated);
        Assert.assertTrue("Unable to navigate to workspace", navigated);
    }

    @And("User fetches the Identified NPI count from the workspace")
    public void userFetchesTheIdentifiedNPICountFromTheWorkspace() {
        logger.info("Fetching Identified NPI count from workspace");
        npiCount = workspace.fetchIdentifiedNPICount();
        logger.info("Identified NPI count fetched: {}", npiCount);
    }

    @And("User fetches the Total NPI count from the workspace")
    public void userFetchesTheTotalNPICountFromTheWorkspace() {
        logger.info("Fetching Total NPI count from workspace");
        totalNpiCount = expansionWorkspace.fetchTotalNPICount();
        logger.info("Total NPI count fetched: {}", totalNpiCount);
    }

    @And("Download button is enabled to the user")
    public void download_button_is_enabled_to_the_user() {
        logger.info("Verifying Download button is enabled");
        workspace.clickFlyOrPageButton();
    }

    @And("User clicks Download NPI option")
    public void userClicksDownloadNPIOption() {
        logger.info("User clicks Download NPI option");
        workspace.clickDownloadNPI();
    }

    @And("User selects download format as {string} and clicks Download button")
    public void userSelectsFileTypeAsAndClicksDownloadButton(String fileExtension) throws IOException {
        logger.info("Downloading NPI file with format: {}", fileExtension);
        workspace.selectFileExtension(fileExtension);
        targetFilePath = workspace.clickDownloadButton();
        logger.info("Downloaded file path: {}", targetFilePath);
        String downloadMsg = workspace.checkNPIDownloadComplete();
        logger.info("Download message: {}", downloadMsg);
        Assert.assertEquals("Download completed successfully", downloadMsg);
    }

    @And("User verifies the total Identified {string} count in the downloaded file - {string}")
    public void userVerifiesTheFileContent(String npiHeader, String fileExtension) throws IOException {
        logger.info("Verifying NPI count in downloaded {} file for header: {}", fileExtension, npiHeader);
        int npiCountFromFile = 0;

        if (fileExtension.equalsIgnoreCase("CSV"))
            npiCountFromFile = FileActions.fetchColumnCountFromCSV(targetFilePath, npiHeader);
        else if (fileExtension.equalsIgnoreCase("XLSX"))
            npiCountFromFile = FileActions.fetchColumnCountFromExcel(targetFilePath, npiHeader);
        logger.info("NPI count from UI: {}, NPI count from file: {}", npiCount, npiCountFromFile);
        Assert.assertEquals("NPI count is not matching", Integer.parseInt(npiCount), npiCountFromFile);
    }

    @When("User clicks on Publish NPI List")
    public void user_clicks_on_publish_npi_list() {
        logger.info("User clicks on Publish NPI List");
        workspace.clickPublishNPI();
    }

    @And("User selects publish {string}")
    public void userSelectsPublish(String listType) {
        logger.info("User selects publish type: {}", listType);
        workspace.publishNPI(listType);
    }

    @When("User select the platform to publish the list")
    public void user_select_the_system_to_publish_the_list() {
        logger.info("Selecting platforms to publish NPI list");
        workspace.hcp();
        workspace.life();
    }

    @Then("Verify list is published")
    public void verify_list_is_published() {
        logger.info("Publishing NPI list");
        workspace.clickPublish();
        String alertMsg = workspace.fetchNPIListPublishAlertDisplayed();
        if (!alertMsg.isEmpty()) {
            logger.info("Publish alert message: {}", alertMsg);
            Assert.assertEquals("NPI list published successfully", alertMsg);
        } else {
            Assert.assertFalse("Publish alert is not displayed", false);
        }
        logger.info("Verifying published NPI list");
        workspace.clickFlyOrPageButton();
        String publishedNpi = workspace.verifyPublishedNpi();
        logger.info("Published NPI: {}", publishedNpi);
        Assert.assertEquals("Published NPI List", publishedNpi);
    }

    @Then("Verify HCP Audience Expansion list is published")
    public void verifyHCPAudienceExpansionListIsPublished() {
        logger.info("Publishing HCP Audience Expansion list");
        expansionWorkspace.clickPublish();
        String alertMsg = expansionWorkspace.fetchNPIListPublishAlertDisplayed();
        if (!alertMsg.isEmpty()) {
            logger.info("Publish alert message: {}", alertMsg);
            Assert.assertEquals("Workspace saved successfully", alertMsg);
        } else {
            Assert.assertFalse("Publish alert is not displayed", false);
        }
        logger.info("Verifying published NPI list");
        workspace.clickFlyOrPageButton();
        String publishedNpi = workspace.verifyPublishedNpi();
        logger.info("Published NPI: {}", publishedNpi);
        Assert.assertEquals("Published NPI List", publishedNpi);
    }

    @And("Check the Download icon is highlighted in green color")
    public void checkTheDownloadIconIsHighlightedInGreenColor() {
        logger.info("Verifying Download icon is highlighted in green");
        String color = workspace.checkBackgroundColorOfDownloadIcon();
        logger.info("Download icon color: {}", color);
        Assert.assertEquals("rgb(0, 136, 136)", color);
    }

    @And("Verify Webhook panel is disabled before applying filters")
    public void verifyWebhookPanelIsDisabledBeforeApplyingFilters() {
        logger.info("Verifying Webhook panel is disabled before applying filters");
        workspace.clickWebhookIcon();
        String toggleStatus = workspace.verifyWebhookToggleButton();
        logger.info("Webhook toggle status: {}", toggleStatus);
        Assert.assertEquals("Disabled", toggleStatus);
        workspace.closeWebhookPanel();
    }

    @Then("Verify Webhook panel is enabled after applying engagement filters")
    public void verifyWebhookPanelIsEnabledAfterApplyingFilters() {
        logger.info("Verifying Webhook panel is enabled after applying filters");
        workspace.clickWebhookIcon();
        String toggleStatus = workspace.verifyWebhookToggleButton();
        logger.info("Webhook toggle status: {}", toggleStatus);
        Assert.assertEquals("Enabled", toggleStatus);
    }

    @When("User clicks {string} request method")
    public void userClicksRequestMethod(String requestType) {
        logger.info("User clicks request method: {}", requestType);
        workspace.clickRequestOrContentButton(requestType);
    }

    @And("User adds valid URL and append Macros with {string} to the {string} as follow")
    public void userAddsURLAndMacrosWithToTheURLAsFollow(String param, String textType, DataTable macros) {
        logger.info("Adding webhook URL and appending macros for param: {}", param);
        List<String> macrosList = macros.asList(String.class);
        workspace.addURL(Constants.WEBHOOK_URL);
        workspace.addMacros(textType, param, macrosList);
    }

    @Then("Verify if Macros Appended to the URL")
    public void verifyIfMacrosAppendedToTheURL() {
        logger.info("Verifying macros are appended to webhook URL");
        String text = workspace.verifyMacrosAppendedToURL();
        Assert.assertTrue(
                "Macros are not correctly appended to the URL",
                text.matches(Pattern.quote(Constants.WEBHOOK_URL) + "%%NPI%%%%URL%%%%Channel%%%%PARAM\\d+%%"));
    }

    @And("User selects content type {string}")
    public void userSelectsContentType(String contentType) {
        logger.info("Selecting content type: {}", contentType);
        workspace.clickRequestOrContentButton(contentType);
    }

    @And("User adds valid body {string} and append Macros with {string} to the {string} as follow")
    public void userAddsBodyAndAppendMacrosToTheBodyAsFollow(
            String jsonFile, String param, String textType, DataTable macros) throws IOException {
        logger.info("Adding webhook body from file: {}", jsonFile);
        List<String> macrosList = macros.asList(String.class);
        workspace.addBody(jsonFile);
        workspace.addMacros(textType, param, macrosList);
    }

    @Then("Verify if Macros Appended to the Body {string}")
    public void verifyIfMacrosAppendedToTheBody(String body) throws IOException {
        logger.info("Verifying macros appended to webhook body");
        String text = workspace.verifyMacrosAppendedToBody();
        String[] parts = text.split("%%NPI%%%%URL%%%%Channel%%%%PARAM\\d+%%");
        Assert.assertTrue("Macro suffix not found in text", parts.length == 1 || parts.length == 2);
        String actualJson = parts[0].replaceAll("\\s+", "");
        String expectedJsonNormalized = CommonUtils.readJsonTestDataFile(body).replaceAll("\\s+", "");
        Assert.assertEquals("JSON part does not match", expectedJsonNormalized, actualJson);
        workspace.addBody(body);
    }

    @When("User saves the webhook setup")
    public void userSavesTheWebhookSetup() {
        logger.info("Saving webhook setup");
        workspace.saveWebhookSetup();
    }

    @Then("Check that the success message appears once the webhook is successfully created")
    public void checkThatTheSuccessMessageAppearsOnceTheWebhookIsSuccessfullyCreated() {
        logger.info("Verifying webhook creation success message");
        Assert.assertEquals("Webhook setup successfully", workspace.verifyWebhookCreationIsSuccess());
    }

    @Then("Verify inline error message for the invalid webhook entries {string}")
    public void verifyInlineErrorMessageForTheInvalidWebhookEntries(String invalidData) {
        logger.info("Verifying inline error message for invalid webhook data");
        String inlineError = String.valueOf(workspace.verifyInlineErrorMessage(invalidData));
        Assert.assertTrue(
                "Inline error message is not displayed",
                (inlineError.contains("URL is invalidBody is invalid") || inlineError.contains("URL is invalid")));
    }

    @And("Verify error message when webhook setup is failed using {string}")
    public void verifyErrorMessageWhenWebhookSetupIsFailed(String errorData) throws IOException {
        logger.info("Verifying webhook failure error message");
        List<String> mediaTypeList = CommonUtils.parseCommaSeparatedString(errorData);
        String errorMessage = workspace.verifyErrorMsgWhenAPIFailed(mediaTypeList);
        Assert.assertTrue("Error message is not displayed", errorMessage.contains("Webhook setup error. Try again."));
    }

    @Then("Check the webhook icon is highlighted in green color")
    public void checkTheWebhookIconIsHighlightedInGreenColor() {
        logger.info("Verifying webhook icon is highlighted in green");
        String color = workspace.checkBackgroundColorOfWebhookIcon();
        logger.info("Webhook icon color: {}", color);
        Assert.assertEquals("rgb(0, 136, 136)", color);
    }

    @When("User tries to delete the workspace associated with active webhook from the workspace list")
    public void userDeletesTheWebhookFromTheWorkspaceList() throws InterruptedException {
        logger.info("Attempting to delete workspace with active webhook: {}", workspaceName);
        workspace.goToWorkspaceList();
        workspaceCreation.clickMoreActionsMenu(workspaceName);
        workspaceCreation.deleteWorkspace();
    }

    @Then("Verify user receives a warning when attempting to delete a workspace with an active webhook")
    public void verifyUserReceivesAWarningWhenAttemptingToDeleteAWorkspaceWithAnActiveWebhook() {
        logger.info("Verifying delete warning for workspace with active webhook");
        String actual = workspaceCreation
                .verifyDeletePopUp()
                .replaceAll("\r\n|\r|\n", "\n")
                .replaceAll(" +", " ")
                .trim();
        logger.info("Delete popup message: {}", actual);
        Assert.assertTrue(
                "Message should warn about deleting the workspace",
                actual.contains("You are trying to delete the workspace " + workspaceName + "."));
        Assert.assertTrue(
                "Message should mention that webhooks are enabled",
                actual.contains("Webhooks are enabled for this workspace."));
        Assert.assertTrue(
                "Message should mention that deletion is irreversible and will delete the webhook",
                actual.contains(
                        "Deleting the workspace will delete the webhook as well. This action cannot be undone."));
        Assert.assertTrue("Message should confirm user wants to proceed", actual.contains("Do you want to proceed?"));
        String deleteMsg = workspaceCreation.deleteWorkspaceWithActiveWebhook().trim();
        logger.info("Delete workspace message: {}", deleteMsg);
        Assert.assertEquals("Workspace deleted successfully", deleteMsg);
    }

    /*Roshani Sherkar
     * 21-07-2025*/
    @And("User clicks on AI Configurator and build audience using the AIPrompt {string}")
    public void userClicksOnAIConfiguratorAndBuildAudienceUsingTheAIprompt(String aiPrompt) {
        logger.info("Building audience using AI prompt");
        explorerWorkspace.clickAIConfigurator();
        flag = explorerWorkspace.describeAudience(aiPrompt);
        logger.info("AI audience build status: {}", flag);
    }

    @Then("Verify the filter is applied correctly {string}")
    public void verifyTheFilterIsAppliedCorrectly(String primaryFilter) {
        logger.info("Verifying AI-applied filters");
        if (flag) {
            List<String> appliedFilters = explorerWorkspace.verifyAllSelectedFilters();
            List<String> expectedFilters = CommonUtils.parseCommaSeparatedString(primaryFilter);

            for (String expected : expectedFilters) {
                boolean isFilterApplied = appliedFilters.stream().anyMatch(filter -> filter.equalsIgnoreCase(expected));
                Assert.assertTrue("The filter '" + expected + "' is not applied correctly", isFilterApplied);
            }
            isOverwritten = true;
        } else {
            Assert.fail("AI is unable to build audience");
        }
    }

    @And("User applies the following filters one by one and checks that NPI details are refined after each filter:")
    public void userAppliesBelowFilterWithOptionAndCheckNPIDetails(DataTable dataTable) {
        logger.info("Applying filters one by one and verifying NPI refinement");
        List<Map<String, String>> filters = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> row : filters) {
            explorerWorkspace.clickAddFilter();
            String filterName = row.get("FilterName").trim();
            String filterOption = row.get("Option").trim();
            logger.info("Applying filter: {} with option: {}", filterName, filterOption);

            if (!filterOption.isEmpty()) {
                List<String> filterOptionList = CommonUtils.parseCommaSeparatedString(filterOption);
                explorerWorkspace.selectFilter(filterName, filterOptionList);
            }
            explorerWorkspace.clickFilterOKButton();
            explorerWorkspace.applyFilter();
            List<String> currentNpiDetails = explorerWorkspace.checkNPIDetails();
            // explorerWorkspace.deleteFilter();
            if (previousNpiDetails != null) {
                if (currentNpiDetails.equals(previousNpiDetails)) {
                    Assert.assertEquals(
                            "Filter '" + filterName + "' with options '" + filterOption
                                    + "' did not refine the audience (NPI details remained the same)",
                            currentNpiDetails,
                            previousNpiDetails);
                } else {
                    Assert.assertTrue(
                            "Filter '" + filterName + "' refined the audience as expected",
                            currentNpiDetails.size() <= previousNpiDetails.size());
                }
            }
            previousNpiDetails = currentNpiDetails;
        }
    }

    @And("Verify after adding ai prompt filter selected manually should be overwritten")
    public void verifyAfterAddingAiPromptManuallySelectedFilterShouldBeOverwritten() {
        logger.info("Verifying AI filter overwrote manual filter");
        Assert.assertTrue("AI filter did not overwrite manual filter", isOverwritten);
    }

    @And("Fetch and verify that NPI details are refined")
    public void verifyThatNPIDetailsAreRefinedAfterEachFilter() {
        logger.info("Verifying NPI details refinement");
        List<String> currentNpiDetails = explorerWorkspace.checkNPIDetails();
        if (previousNpiDetails != null) {
            if (currentNpiDetails.equals(previousNpiDetails)) {
                Assert.assertEquals(
                        "Filter with options did not refine the audience (NPI details remained the same)",
                        currentNpiDetails,
                        previousNpiDetails);
            } else {
                Assert.assertTrue(
                        "Filter refined the audience as expected",
                        currentNpiDetails.size() <= previousNpiDetails.size());
            }
        }
        previousNpiDetails = currentNpiDetails;
    }

    @Then("Delete the filter")
    public void deleteTheFilter() {
        logger.info("Deleting applied filter");
        explorerWorkspace.deleteFilter();
    }

    /*Roshani Sherkar
     * 24-07-2024*/
    @And("User hovers over the dashboard filters, selects the region with maximum NPIs and clicks on it")
    public void userHoversOverTheNPIVisualsIconAndClicksOnIt(DataTable dataTable) {
        List<String> npiVisualList = dataTable.asList(String.class);
        logger.info("Hovering over dashboard filters and selecting region with maximum NPIs");
        logger.info("Dashboard filter options: {}", npiVisualList);
        explorerWorkspace.hoverOverNPIVisualsIcon(npiVisualList);
    }

    @And("Verify that dashboard filters are displayed correctly in Filter section")
    public void verifyThatCrossFiltersAreDisplayedCorrectlyInFilterSection() {
        logger.info("Verifying dashboard filters are displayed in Filter section");
        Assert.assertTrue("Filters were not added", explorerWorkspace.verifyCrossFiltersDisplayed());
        appliedFilterEntries = explorerWorkspace.fetchMergedFilters();
        logger.info("Merged dashboard filters: {}", appliedFilterEntries);
    }

    @And("Verify dashboard filters are merged with Primary filters")
    public void verifyDashboardFiltersAreMergedWithPrimaryFilters() {
        logger.info("Verifying dashboard filters are merged with primary filters");
        List<String> displayedFilters = explorerWorkspace.verifyAllSelectedFilters();
        for (String mergedFilter : appliedFilterEntries) {
            boolean matchFound =
                    displayedFilters.stream().anyMatch(displayed -> displayed.equalsIgnoreCase(mergedFilter));
            Assert.assertTrue("Merged filter not displayed: " + mergedFilter, matchFound);
        }
    }

    @And("Navigate to workspace dashboard")
    public void navigateToWorkspaceDashboardAndSearchTheWorkspaceCreated() {
        logger.info("Navigating to workspace dashboard");
        workspace.goToWorkspaceList();
    }

    @And("User searches the workspace created to perform Actions from More menu")
    public void userSearchesTheWorkspaceCreatedToPerformDuplicateOperation() throws InterruptedException {
        workspaceCreation.closeAIPanel();
        workspaceCreation.verifyStudioWorkspaceFrame();
        logger.info("Searching workspace: {}", workspaceName);
        workspaceCreation.searchWorkspaceName(workspaceName);
        workspaceCreation.clickMoreActionsMenu(workspaceName);
    }

    @And("User selects the {string} option by clicking More Actions menu")
    public void userSelectsTheDeleteOptionByClickingMoreActionsButton(String actionName) {
        logger.info("Performing '{}' action from More Actions menu", actionName);
        workspaceCreation.performActionOnWorkspace(actionName);
    }

    @And("Verify user is able to rename the workspace as {string}")
    public void verifyUserIsAbleToRenameTheWorkspace(String newWorkspace) {
        newWorkspaceName = newWorkspace + CommonUtils.timeStampCalculation();
        logger.info("Renaming workspace from {} to {}", workspaceName, newWorkspaceName);
        String renameMsg = workspaceCreation.renameWorkspaceName(workspaceName, newWorkspaceName);
        logger.info("Rename workspace message: {}", renameMsg);
        Assert.assertEquals("Workspace renamed successfully", renameMsg);
        workspaceName = newWorkspaceName;
    }

    @And("Verify user is able to duplicate the workspace")
    public void verifyUserIsAbleToDuplicateTheWorkspace() {
        logger.info("Duplicating workspace: {}", workspaceName);
        workspaceName = workspaceCreation.fetchDuplicateWorkspaceName();
        String duplicateMsg = workspaceCreation.clickDuplicateButton();
        logger.info("Duplicate workspace message: {}", duplicateMsg);
        Assert.assertEquals("Workspace duplicated successfully", duplicateMsg);
    }

    @And("User is able to search the workspace after performing operation - {string}")
    public void userIsAbleToSearchTheWorkspaceWithTheNewName(String operationName) {
        logger.info("Searching workspace after {} operation: {}", operationName, workspaceName);
        boolean searched = workspaceCreation.searchWorkspaceName(workspaceName);
        logger.info("Workspace search result: {}", searched);
        Assert.assertTrue("Unable to search workspace after performing " + operationName + " operation", searched);
    }

    @And("Verify user is able to delete the workspace")
    public void verifyUserIsAbleToDeleteTheWorkspace() {
        logger.info("Verifying delete confirmation for workspace: {}", workspaceName);
        String text = workspaceCreation.verifyDeletePopUp().trim();
        logger.info("Delete popup message: {}", text);
        Assert.assertTrue(
                "Message should contain warning about deleting workspace",
                text.contains("You are trying to delete the workspace " + workspaceName));
        Assert.assertTrue(
                "Message should mention irreversible deletion",
                text.contains("This action cannot be undone – all deleted data will be lost."));
        Assert.assertTrue("Message should ask for confirmation", text.contains("Do you want to proceed?"));
        Assert.assertEquals(
                "Workspace deleted successfully",
                workspaceCreation.deleteWorkspaceWithActiveWebhook().trim());
    }

    @And("User searches the created workspace")
    public void userSearchesTheCreatedWorkspace() {
        workspaceCreation.closeAIPanel();
        logger.info("Searching created workspace: {}", workspaceName);
        workspaceCreation.searchWorkspaceName(workspaceName);
        workspaceCreation.selectMoreActionsMenu(workspaceName);
    }

    @When("User navigates to administration tab")
    public void user_navigates_to_administration_tab() {
        logger.info("Navigating to Administration tab");
        accounts.verifyStudioMenu();
        accounts.clickAdministration();
    }

    @When("User clicks on accounts tab")
    public void user_clicks_on_accounts_tab() {
        logger.info("Clicking on Accounts tab");
        accounts.selectAccountsTab();
    }

    @Then("Verify that the workspace cannot be deleted and appropriate message is displayed to the user")
    public void verifyThatTheWorkspaceCannotBeDeletedAndAppropriateMessageIsDisplayedToTheUser() {
        logger.info("Verifying workspace deletion is blocked");
        workspaceCreation.clickRemoveWorkspaceButton();
        Assert.assertTrue(workspaceCreation
                .verifyDeleteWorkspaceErrorMessage()
                .contains("Deletion blocked by Life. Message: This list can't be deleted"));
    }

    @When("Locate an account {string} with external user permission and select it")
    public void locate_an_account_with_external_user_permission_and_select_it(String externalAccount) {
        logger.info("Locating account with external user permission: {}", externalAccount);
        accounts.searchAccount(externalAccount);
    }

    @When("Go to users tab and search {string} and select studio tab")
    public void go_to_users_tab_and_search_and_select_studio_tab(String selectUser) {
        logger.info("Selecting external user and opening Studio tab: {}", selectUser);
        accounts.selectUserTab();
        accounts.selectExternalUser(selectUser);
    }

    @Then("User turns on studio toggle for external users and verifies that it is enabled")
    public void user_turns_on_studio_toggle_for_external_users_and_verifies_that_it_is_enabled() {
        logger.info("Enabling Studio toggle for external user");
        Assert.assertTrue(
                "Studio toggle for external user was not turned on", accounts.turnStudioToggleForExternalUser());
        accounts.internalUserLogout();
    }

    @And("Verify the Retrofit checkbox is selected")
    public void verifyTheRetrofitCheckboxIsSelected() {
        logger.info("Verifying Retrofit checkbox is selected");
        boolean retrofitSelected = workspace.isRetrofitCheckboxSelected();
        logger.info("Retrofit checkbox selected: {}", retrofitSelected);
        Assert.assertTrue("Retrofit Checkbox is not selected", retrofitSelected);
    }

    @And("User selects {string} as Keep NPIs on the list option")
    public void userSelectsAsKeepNPIsOnTheList(String option) {
        logger.info("Selecting NPI retention option: {}", option);
        workspace.clickNPIRetentionOption(option);
    }

    @And("User clicks on Published button, verifies the {string} and {string} text are displayed")
    public void userClicksOnPublishedButtonAndVerifiesThe(String listType, String engagingText) {
        logger.info("Verifying Published NPI list details");
        workspace.clickPublishedButton();
        Assert.assertTrue(
                listType + " is not displayed", workspace.verifyListTypeAfterPublished(listType.toLowerCase()));
        String text = workspace.verifyNPIsEngagingText();
        Assert.assertTrue("NPIs engaging text is not available", text.contains(engagingText));
    }

    @And("User searches and selects the account {string}")
    public void userSearchesAndSelectsTheAccount(String accountName) {
        logger.info("Searching and selecting account: {}", accountName);
        accounts.searchAccount(accountName);
    }

    @And("User navigates to advertisers page under the selected account")
    public void userNavigatesToAdvertisersPageUnderTheSelectedAccount() {
        logger.info("Navigating to Advertisers page under selected account");
        accounts.clickAccountAdvertiserTab();
        accounts.clickGlobalSignalsTab();
    }

    @And("User enables the {string} permission for the {string} advertiser")
    public void userEnablesThePermissionForTheAdvertiser(String advertiserPermissions, String advertiserName) {
        logger.info("Enabling advertiser permission '{}' for advertiser '{}'", advertiserPermissions, advertiserName);
        if (!advertiserPermissions.isBlank()) {
            accounts.enableAdvertiserPermission(advertiserName, advertiserPermissions);
        }
    }

    @And("User navigates to users page under the selected account")
    public void userNavigatesToUsersPageUnderTheSelectedAccount() {
        logger.info("Navigating to Users page under selected account");
        accounts.navigateToUserTab();
    }

    @And("User selects the {string} external user")
    public void userSelectsTheExternalUser(String externalUser) {
        logger.info("Selecting external user: {}", externalUser);
        accounts.selectExternalUser(externalUser);
    }

    @And("User enables the {string} permission for the {string} for an external user")
    public void userEnablesThePermissionForTheForAnExternalUser(String studioPermissions, String accountName) {
        logger.info(
                "Enabling studio permission '{}' for external user on account '{}'", studioPermissions, accountName);
        accounts.externalUserPermissions(studioPermissions, accountName);
        accounts.internalUserLogout();
    }

    @And("External user selects the workspace")
    public void externalUserSelectsTheWorkspace() {
        logger.info("External user selects existing workspace");
        workspace.selectExistingWorkspace();
    }

    @Then("External user should be able to see the {string} permission in the workspace")
    public void externalUserShouldBeAbleToSeeThePermissionInTheWorkspace(String permissions) {
        logger.info("Verifying external user permission in workspace: {}", permissions);
        if (permissions.equals("MOMENTS") || permissions.equals("IB HEALTH") || permissions.equals("CLAIMS DATA")) {
            Assert.assertTrue(explorerWorkspace.verifyPermissionFilters(permissions));
            Assert.assertTrue(explorerWorkspace.verifyWidgets(permissions));
        }
    }

    @And("User searches the account {string} for which permission to be checked")
    public void userSearchesTheAccountForWhichPermissionToBeChecked(String account) {
        logger.info("Verifying Studio access for account: {}", account);
        accounts.searchAccount(account);
        Assert.assertTrue("Advertiser Tab is not displayed", accounts.isAccountsAdvertiserTabDisplayed());
    }

    @And("User navigates to Advertisers tab")
    public void userNavigatesToAdvertisersTab() {
        logger.info("Navigating to Advertisers tab");
        accounts.clickAccountsAdvertiserTab();
    }

    @And("User clicks on {string} tab present under Advertisers tab")
    public void userClicksToTabPresentUnderAdvertisersTab(String tabName) {
        logger.info("Clicking '{}' tab under Advertisers section", tabName);
        accounts.clickAdvertisersSubTab(tabName);
    }

    @And("User sets the HCP365 permission {string} for {string} and saves the changes")
    public void userChecksTheHCPPermissionAndItFor(String checkboxStatus, String advertiser) {
        logger.info("Setting HCP365 permission '{}' for advertiser '{}'", checkboxStatus, advertiser);
        boolean permissionModified = accounts.checkHCPPermissionForAdvertiser(checkboxStatus, advertiser);
        logger.info("HCP365 permission modified: {}", permissionModified);
        Assert.assertTrue("HCP365 permission is not modified correctly", permissionModified);
        accounts.saveAccountsAdvertiserTab();
        logger.info("HCP365 permission saved for advertiser: {}", advertiser);
    }

    @And("Verify Owned And Operated section is {string} within the Cross-Filter section of the Workspace")
    public void verifyOwnedAndOperatedSectionWithinTheCrossFilterSectionOfTheWorkspace(String visibilityFlag) {
        logger.info("Verifying Owned And Operated section is {}", visibilityFlag);
        boolean flag = explorerWorkspace.isOwnedAndOperatedSectionAvailable();
        logger.info("Owned And Operated section availability: {}", flag);
        switch (visibilityFlag.toLowerCase()) {
            case "present":
                Assert.assertTrue("Expected 'Owned and Operated' section to be present, but it was absent.", flag);
                break;
            case "absent":
                Assert.assertFalse("Expected 'Owned and Operated' section to be absent, but it was present.", flag);
                break;
            default:
                Assert.fail("Invalid input for visibility: " + visibilityFlag);
        }
    }

    @And("User searches the account {string} and checks Studio permissions")
    public void userSearchesTheAccountInWhichPermissionToBeChecked(String account) {
        logger.info("Searching account '{}' and checking Studio permissions", account);
        accounts.searchAccount(account);
        Assert.assertTrue("Studio permissions setting icon is not displayed", accounts.isStudioSettingsButtonVisible());
        accounts.clickStudioSettingsButton();
        metricNames = accounts.fetchWorkspacesWithPermission();
        logger.info("Workspace permissions fetched for account '{}': {}", account, metricNames);
        accounts.clickCancelButtonFromSettingsPanel();
    }

    @And("User navigates to Studio application")
    public void userNavigatesToStudioApplication() {
        logger.info("Navigating to Studio application");
        navigation.navigateToStudio();
        workspaceCreation.closeAIPanel();
    }

    @And(
            "User applies {string} filter, selects filter options as below and verifies the clinical recency filter is updated correctly")
    public void
            userAppliesClinicalFilterSelectsFilterOptionsAsBelowAndVerifiesTheClinicalRecencyFilterIsUpdatedCorrectly(
                    String filterType, DataTable dataTable) {
        logger.info("Applying '{}' filter and verifying clinical recency values", filterType);
        List<Map<String, String>> filters = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> row : filters) {
            String filterName = row.get("FilterName").trim();
            String filterOption = row.get("Option").trim();
            String recency = row.getOrDefault("Recency", "").trim();
            logger.info("Applying filter '{}' with options '{}' and recency '{}'", filterName, filterOption, recency);
            explorerWorkspace.clickAddFilter();
            // Apply option values
            if (!filterOption.isEmpty()) {
                List<String> filterOptionList = CommonUtils.parseCommaSeparatedString(filterOption);
                explorerWorkspace.selectFilter(filterName, filterOptionList);
                if (!recency.isEmpty()) {
                    explorerWorkspace.selectRecency(recency);
                }
                explorerWorkspace.clickFilterOKButton();
            }
            explorerWorkspace.applyFilter();
            logger.info(
                    "Expected recency: '{}' | Actual recency: '{}'",
                    recency,
                    explorerWorkspace.fetchRecencyValue(filterType));
            Assert.assertEquals(
                    filterType + " recency value is not matched",
                    recency,
                    explorerWorkspace.fetchRecencyValue(filterType));
        }
    }

    @Then("User should be able to see Studio for that account")
    public void userShouldBeAbleToSeeStudioForThatAccount() {
        logger.info("Verifying Studio is visible for the account");
        String studioTitle = navigation.verifyStudioTitle();
        logger.info("Studio title: {}", studioTitle);
        Assert.assertEquals("Studio", studioTitle);
    }

    @When("User selects the workspace type {string}")
    public void userSelectsTheWorkspaceType(String workspaceType) {
        logger.info("Verifying Studio Workspace frame is displayed");
        workspaceCreation.verifyStudioWorkspaceFrame();
        logger.info("Selecting workspace type: {}", workspaceType);
        workspaceCreation.selectWorkspaceType(workspaceType);
    }

    @And("User selects {string} from the Studio Workspace Advertiser dropdown")
    public void userSelectsFromTheStudioWorkspaceAdvertiserDropdown(String workspaceAdvertiser) {
        logger.info("Selecting workspace advertiser: {}", workspaceAdvertiser);
        workspaceCreation.selectWorkspaceAdvertiser(workspaceAdvertiser);
    }

    @And("User selects {string} from the Studio Workspace Created By dropdown")
    public void userSelectsFromTheStudioWorkspaceCreatedByDropdown(String createdBy) {
        logger.info("Selecting workspace created by: {}", createdBy);
        workspaceCreation.selectWorkspaceCreatedBy(createdBy);
    }

    @And("User searches for a workspace by name using the search box on the Workspace Details page")
    public void userSearchesForAWorkspaceByNameUsingTheSearchBoxOnTheWorkspaceDetailsPage() {
        logger.info("Fetching workspace name from dashboard to search");
        workspaceName = workspaceCreation.fetchWorkspaceNameFromDashboard();
        logger.info("Searching workspace by name: {}", workspaceName);
        workspaceCreation.searchByWorkspaceName(workspaceName);
    }

    @And("User navigates to another page within Studio and then returns to the workspace list page")
    public void userNavigatesToAnotherPageWithinStudioAndThenReturnsToTheWorkspaceListPage() {
        logger.info("Navigating to another page within Studio and then returning to the workspace list page");
        workspaceCreation.clickCreateStudioWorkspace();
        logger.info("Navigated back to the workspace list page after visiting the create new workspace page");
        workspaceCreation.clickBackArrowFromCreateNewWorkspace();
    }

    @Then(
            "User verifies that the selected filters, dropdown values, and search input remain persistent unless they are manually deselected or cleared - {string}, {string}, {string}")
    public void
            userVerifiesThatTheSelectedFiltersDropdownValuesAndSearchInputRemainPersistentUnlessTheyAreManuallyDeselectedOrCleared(
                    String expectedWorkspaceType, String expectedAdvertiser, String expectedCreatedBy) {
        String actualWorkspaceType = workspaceCreation.getSelectedWorkspaceType();
        logger.info("Workspace type: {}", actualWorkspaceType);
        Assert.assertEquals("Selected workspace type is not persistent", expectedWorkspaceType, actualWorkspaceType);
        String actualWorkspaceAdvertiser = workspaceCreation.getSelectedWorkspaceAdvertiser();
        logger.info("Workspace advertiser: {}", actualWorkspaceAdvertiser);
        Assert.assertEquals(
                "Selected workspace advertiser is not persistent", expectedAdvertiser, actualWorkspaceAdvertiser);
        String actualWorkspaceCreatedBy = workspaceCreation.getSelectedWorkspaceCreatedBy();
        logger.info("Created by: {}", actualWorkspaceCreatedBy);
        Assert.assertEquals(
                "Selected workspace created by is not persistent", expectedCreatedBy, actualWorkspaceCreatedBy);
        String actualWorkspaceName = workspaceCreation.getSearchedWorkspaceName();
        logger.info("Workspace name: {}", actualWorkspaceName);
        Assert.assertEquals("Search input value is not persistent", workspaceName, actualWorkspaceName);
    }

    @And("Verify on refresh of the page, the filters are reset and search input is cleared")
    public void verifyOnRefreshOfThePageTheFiltersAreResetAndSearchInputIsCleared() {
        logger.info("Refreshing the page to verify filters are reset and search input is cleared");
        workspaceCreation.refreshPage();
        logger.info("Verifying filters are reset and search input is cleared after page refresh");
        Assert.assertTrue(
                "Workspace Type checkbox is not reset",
                workspaceCreation.getSelectedWorkspaceType().isEmpty());
        Assert.assertTrue(
                "Workspace advertiser dropdown is not reset",
                workspaceCreation.getSelectedWorkspaceAdvertiser().isEmpty());
        Assert.assertTrue(
                "Workspace created by dropdown is not reset",
                workspaceCreation.getSelectedWorkspaceCreatedBy().isEmpty());
        Assert.assertTrue(
                "Search box is not cleared",
                workspaceCreation.getSearchedWorkspaceName().isEmpty());
    }

    @And("User selects the Draft option as {string}")
    public void userSelectsTheDraftOptionAs(String draftOption) {
        explorerWorkspace.selectDraftOption(draftOption);
    }

    @And("Internal user logs out from the application")
    public void internalUserLogsOutFromTheApplication() {
        accounts.internalUserLogout();
    }

    @And("External User switches the {string}account in Studio application")
    public void externalUserSwitchesTheAccountInStudioApplication(String accountName) {
        accounts.externalUserSwitchAccount(accountName);
        logger.info("Switching account in Studio application to: {}", accountName);
    }

    @When("External user searches the workspace name in studio application with {string} draft option")
    public void externalUserSearchesTheWorkspaceNameInStudioApplicationWithDraftOption(String draftOption) {
        logger.info("External user searching and selecting the workspace: {}", workspaceName);
        workspaceCreation.searchByWorkspaceName(workspaceName);
        if (draftOption.equals("Public")) {
            workspaceCreation.isWorkspacePresent(workspaceName);
            Assert.assertTrue("Workspace is present for external user with draft option: " + draftOption, true);
        } else {
            workspaceCreation.isWorkspaceAbsent();
            Assert.assertTrue("Workspace is absent for external user with draft option: " + draftOption, true);
        }
    }

    @Then("External user verifies whether the workspace with {string} is visible in workspace management page")
    public void externalUserVerifiesWhetherTheWorkspaceWithIsVisibleInWorkspaceManagementPage(String draftOption) {
        logger.info("External user verifying workspace visibility with draft option: {}", draftOption);
        boolean isWorkspaceVisible = workspaceCreation.isWorkspaceVisible(workspaceName, draftOption);
        logger.info("Is workspace visible for external user: {}", isWorkspaceVisible);
        Assert.assertTrue(
                "Workspace is not visible for external user with draft option: " + draftOption, isWorkspaceVisible);
    }

    @And("User edits the workspace name as {string}")
    public void userEditsTheWorkspaceNameAs(String wName) {
        workspaceName = wName + '_' + CommonUtils.timeStampCalculation();
        logger.info("Adding workspace name: {}", workspaceName);
        brandExplorerWorkspace.waitForDashboardLoad();
        explorerWorkspace.clickEditWorkspace();
        explorerWorkspace.enterWorkspaceName(workspaceName);
        explorerWorkspace.saveWorkspaceName();
        explorerWorkspace.waitUntilAlertDisappears();
        brandExplorerWorkspace.waitForDashboardLoad();
    }

    @Then("Verify Dimension {string} and Metric {string} are selected by default in the workspace")
    public void verifyDefaultDimensionAndMetricInTheWorkspace(String dimension, String metric) {
        logger.info("Verifying default Dimension and Metric in the workspace");
        String actualDimension = brandExplorerWorkspace.getDefaultDimensions(dimension);
        logger.info("Default Dimension: {}", actualDimension);
        Assert.assertEquals("Default Dimension is not as expected", dimension, actualDimension);
        String actualMetric = brandExplorerWorkspace.getDefaultMetrics(metric);
        logger.info("Default Metric: {}", actualMetric);
        Assert.assertEquals("Default Metric is not as expected", metric, actualMetric);
    }

    @Then("Verify Time Frame is selected as {string} by default in the workspace")
    public void verifyDefaultTimeFrameInTheWorkspace(String timeFrame) {
        logger.info("Verifying default Time Frame in the workspace");
        String actualTimeFrame = brandExplorerWorkspace.getDefaultTimeFrame();
        logger.info("Default Time Frame: {}", actualTimeFrame);
        Assert.assertEquals("Default Time Frame is not as expected", timeFrame, actualTimeFrame);
    }

    @When("User clicks the TimeFrame selector")
    public void userClicksTimeframeSelector() {
        logger.info("Clicking the TimeFrame selector");
        brandExplorerWorkspace.clickTimeFrameSelector();
    }

    @Then("All 9 preset timeframe options are visible in the dropdown with correct labels")
    public void allPresetOptionsAreVisibleInDropdownWithCorrectLabels(DataTable dataTable) {
        List<String> expected = dataTable.asList(String.class);
        logger.info("Expected timeframe options: {}", expected);
        List<String> actual = brandExplorerWorkspace.getTimeFrameOptions();
        logger.info("Actual timeframe options: {}", actual);
        Assert.assertEquals("Timeframe option count mismatch", expected.size(), actual.size());
        Assert.assertTrue(
                "Actual timeframe options do not match expected timeframe options", actual.containsAll(expected));
    }

    @When("User selects the timeframe preset {string}")
    public void userSelectsTimeframePreset(String timeFrame) {
        logger.info("Selecting timeframe preset: {}", timeFrame);
        brandExplorerWorkspace.selectTimeFramePreset(timeFrame);
    }

    @Then("Verify the chart and table update immediately to reflect {string} data")
    public void verifyChartAndTableUpdateForSelectedTimeFrame(String timeFrame) {
        logger.info("Verifying chart and table updated for timeframe: {}", timeFrame);
        String actual = brandExplorerWorkspace.getSelectedTimeFrameAfterUpdate();
        logger.info("Time Frame input after selection: {}", actual);
        Assert.assertTrue(
                "Chart and table did not update for selected timeframe: " + timeFrame,
                actual.equalsIgnoreCase(timeFrame));
    }

    @Then("User selects Source Audience details as {string}, {string}")
    public void userSelectsSourceAudienceDetailsAs(String sourceAudience, String options) {
        logger.info("Selecting source audience: {} with options: {}", sourceAudience, options);
        expansionWorkspace.selectSourceAudienceWithOptions(sourceAudience, options);
    }

    @And("User selects {string}")
    public void userSelectsExpandedAudience(String expandedAudience) {
        logger.info("Selecting expanded audience: {}", expandedAudience);
        expansionWorkspace.selectExpandedAudience(expandedAudience);
    }

    @Then("User verifies the expanded audience count")
    public void userVerifiesTheExpandedAudienceCount() {
        logger.info("Verifying the expanded audience count");
        String expandedCount = expansionWorkspace.fetchTotalNPICountAfterExpansion();
        int beforeCount = Integer.parseInt(totalNpiCount);
        int afterCount = Integer.parseInt(expandedCount);
        Assert.assertTrue(
                "Expected expanded audience count (" + afterCount
                        + ") to be greater than Total NPI count ("
                        + beforeCount + ")",
                afterCount > beforeCount);
    }

    @Then("Verify the workspace is visible in workspace management page")
    public void verifyTheWorkspaceIsVisibleInWorkspaceManagementPage() {
        logger.info("Verifying workspace is visible in workspace management page: {}", workspaceName);
        workspace.goToWorkspaceList();
        boolean isVisible = workspaceCreation.searchWorkspaceName(workspaceName);
        Assert.assertTrue("Workspace is not visible in workspace management page: " + workspaceName, isVisible);
    }

    @And("User select the {string} to publish the list")
    public void userSelectThePlatformToPublishTheList(String platform) {
        logger.info("Selecting platforms to publish: {}", platform);
        List<String> platforms = CommonUtils.parseCommaSeparatedString(platform);
        expansionWorkspace.selectPublishPlatforms(platforms);
    }

    @And("User searches the workspace in {string} and selects it")
    public void userSearchesTheWorkspaceInPlatformAndSelectsIt(String platform) {
        logger.info("Searching workspace in {} platform: {}", platform, workspaceName);
        if (platform.equalsIgnoreCase("Life") || platform.equalsIgnoreCase("LIFE")) {
            npiLists.searchNPILists(workspaceName);
        } else {
            logger.warn("Platform '{}' navigation not yet implemented", platform);
            throw new PendingException();
        }
    }

    @Then("User Verify the list is displayed in the LIFE")
    public void userVerifyTheListIsDisplayedInTheLIFE() {
        logger.info("Verifying list is displayed in LIFE");
        Assert.assertTrue("NPI list is not available in LIFE", npiLists.availablePlatforms());
    }

    @Then("User selects Draft option as {string}")
    public void userSelectsDraftOptionAs(String draft) {
        logger.info("Selecting draft option: {}", draft);
        expansionWorkspace.selectDraftOption(draft);
    }

    @And("Internal User is able to view {string} in workspace management page")
    public void internalUserIsAbleToViewInWorkspaceManagementPage(String wsName) {
        logger.info("Verifying internal user can view workspace: {}", workspaceName);
        workspaceCreation.verifyStudioWorkspaceFrame();
        boolean isVisible = workspaceCreation.searchWorkspaceName(workspaceName);
        Assert.assertTrue("Internal user cannot view workspace: " + workspaceName, isVisible);
    }

    @And("{string} logs out from the {string} application")
    public void logsOutFromTheApplication(String userType, String appName) {
        logger.info("{} logs out from the {} application", userType, appName);
        accounts.internalUserLogout();
    }

    @And("Verify the workspace status is updated to {string}")
    public void statusIsUpdatedToPublic(String status) {
        logger.info("Verifying workspace status is updated to {} after publish", status);
        Assert.assertTrue(
                "Workspace status is not updated to " + status,
                workspaceCreation.isWorkspaceStatusPublic(workspaceName, status));
    }

    @And("User clicks Schedule NPI button")
    public void userClicksScheduleNPIButton() {
        logger.info("User clicks Schedule NPI button");
        expansionWorkspace.clickScheduleNPIButton();
    }

    @And("User enters data and clicks Save button")
    public void userEntersDataAndClicksSaveButton() {
        logger.info("User enters schedule data and clicks Save button");
        expansionWorkspace.enterScheduleDataAndSave();
    }

    @And("Report button is enabled to the user")
    public void reportButtonIsEnabledToTheUser() {
        logger.info("Verifying Report button is enabled");
        workspace.clickFlyOrPageButton();
    }

    @And("User clicks on Download Report")
    public void userClicksOnDownloadReport() {
        logger.info("User clicks on Download Report");
        expansionWorkspace.clickDownloadReport();
    }

    @And("User enters the Report Name")
    public void userEntersTheReportName() {
        logger.info("User enters the report name");
        expansionWorkspace.enterReportName();
    }

    @And("User clicks on Schedule Report button")
    public void userClicksOnScheduleReportButton() {
        logger.info("User clicks Schedule Report button");
        expansionWorkspace.clickScheduleReport();
    }

    @And("Verify the Day column shows {int} dates in ascending order")
    public void verifyDayColumnShowsDatesInAscendingOrder(int expectedDays) {
        logger.info("Verifying Day column shows {} dates in ascending order", expectedDays);
        List<String> dates = brandExplorerWorkspace.getTableDates(expectedDays);
        logger.info("Dates from Day column: {}", dates);
        Assert.assertEquals("Expected " + expectedDays + " rows in the Day column", expectedDays, dates.size());
        for (int i = 0; i < dates.size() - 1; i++) {
            Assert.assertTrue(
                    "Dates are not in ascending order: " + dates.get(i) + " is not before " + dates.get(i + 1),
                    dates.get(i).compareTo(dates.get(i + 1)) < 0);
        }
        String expectedEnd = java.time.LocalDate.now().minusDays(1).toString();
        String expectedStart = java.time.LocalDate.now().minusDays(expectedDays).toString();
        logger.info("Expected date range: {} to {}", expectedStart, expectedEnd);
        Assert.assertEquals("Last date in table does not match yesterday", expectedEnd, dates.get(dates.size() - 1));
        Assert.assertEquals("First date in table does not match expected start", expectedStart, dates.get(0));
    }
}
