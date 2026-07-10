package pages.studio;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.ArrayList;
import java.util.List;
import utils.CommonUtils;
import utils.WaitUtility;

public class ExplorerWorkspace {
    private final Page page;
    private final Locator WORKSPACE_NAME;
    private final Locator SEARCH_ADVERTISER_IN_EDIT_WORKSPACE;
    private final Locator DASHBOARD_CONTENT;
    private final Locator DASHBOARD_ELEMENT;
    private final Locator DASHBOARD_RELOAD_ICON;
    private final Locator ADD_FILTER;
    private final Locator SEARCH_FILTER;
    private final Locator ADVERTISER_SELECTED;
    private final Locator FILTER_OK_BUTTON;
    private final Locator FILTER_CLOSE_BUTTON;
    private final Locator APPLIED_FILTER;
    private final Locator APPLIED_FILTER_OPTION;
    private final Locator SAVE_WORKSPACE;
    private final Locator EXPLORER_WORKSPACE_SUCCESS;
    private final FrameLocator WORKSPACE_FRAME;
    private final Locator SAVE_WORKSPACE_NAME;
    private final Locator TO_YEAR;
    private final Locator FROM_YEAR;
    private final Locator REACHABLE_AUDIENCE;
    private final Locator TAB_PANEL_SEARCH;
    private final Locator SELECT_DESELECT_ALL;
    private final Locator AI_CONFIGURATOR_BTN;
    private final Locator AUDIENCE_DESCRIPTION_TEXTAREA;
    private final Locator BUILD_AUDIENCE_BTN;
    private final Locator TRY_ANOTHER_PROMPT_BTN;
    private final Locator FILTER_HEADER_TITLE;
    private final Locator MAP_TOOL_TIP;
    private final Locator DELETE_FILTER;
    private final Locator CAMERA_CONTROL_ICON;
    private final Locator ZOOM_OUT;
    private final Locator MAP_CONTENT;
    private final Locator DASHBOARD_FILTER_TITLE;
    private final Locator MERGED_TEXT;
    private final Locator DASHBOARD_FILTERS;
    private final Locator MOMENTS;
    private final Locator IBHEALTH;
    private final Locator MOMENTS_WIDGET;
    private final Locator CLAIMS_WIDGET;
    private final Locator OWNED_AND_OPERATED_SECTION;
    private final Locator WORKSPACE_EDIT_BUTTON;
    private final Locator WORKSPACE_HEADER;
    private final Locator ADVERTISER_LIST;
    private final Locator SEARCH_ADVERTISER;
    private final Locator ADVERTISER_BUTTON;
    private final Locator SPECIALITY_PANEL;
    private final Locator INCLUDE_EXCLUDE_CHECK;
    private final Locator ALERT;
    WaitUtility waitUtility;

    public ExplorerWorkspace(Page page) {
        this.page = page;
        this.waitUtility = new WaitUtility(page);
        this.WORKSPACE_FRAME = page.frameLocator("iframe").frameLocator("iframe");
        this.WORKSPACE_NAME = WORKSPACE_FRAME.locator("//p[text()='Workspace Name']/following-sibling::div//input");
        this.SEARCH_ADVERTISER_IN_EDIT_WORKSPACE = WORKSPACE_FRAME.locator("input[id^='listbox-input']");
        this.DASHBOARD_CONTENT = WORKSPACE_FRAME
                .locator("#extension-root iframe")
                .contentFrame()
                .getByRole(AriaRole.REGION, new FrameLocator.GetByRoleOptions().setName("Dashboard Content"));
        this.DASHBOARD_ELEMENT = WORKSPACE_FRAME
                .locator("#extension-root iframe")
                .contentFrame()
                .locator("(//p[contains(@class,'TextBase-sc')])[1]");
        this.DASHBOARD_RELOAD_ICON = WORKSPACE_FRAME
                .locator("#extension-root iframe")
                .contentFrame()
                .locator("//div[contains(text(),'Reload')]");
        this.ADD_FILTER = WORKSPACE_FRAME.locator("//div[contains(text(),'Add Filter')]");
        this.SEARCH_FILTER =
                WORKSPACE_FRAME.getByRole(AriaRole.TEXTBOX, new FrameLocator.GetByRoleOptions().setName("Search"));
        this.ADVERTISER_SELECTED = WORKSPACE_FRAME.locator(
                "//div[@data-tour-id='workspace-back-button']/following-sibling::div//p[contains(text(),'Advertiser:')]");
        this.FILTER_OK_BUTTON =
                WORKSPACE_FRAME.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Ok"));
        this.FILTER_CLOSE_BUTTON =
                WORKSPACE_FRAME.locator("//h1[contains(text(),'Select Filter')]/following-sibling::button");
        this.APPLIED_FILTER = WORKSPACE_FRAME.locator(
                "//div[contains(@data-tour-id,'filters-container')]//button/preceding-sibling::p");
        this.APPLIED_FILTER_OPTION = WORKSPACE_FRAME.locator("//div[contains(@class,'style__FilterExpression-sc')]");
        this.SAVE_WORKSPACE = WORKSPACE_FRAME.locator(
                "//button[contains(@data-tour-id,'save-workspace-button')]//div[contains(text(),'Save')]");
        this.EXPLORER_WORKSPACE_SUCCESS = WORKSPACE_FRAME
                .locator("[id=\"\\32 \"] div")
                .filter(new Locator.FilterOptions().setHasText("Workspace managementWorkspace"))
                .nth(2);
        this.SAVE_WORKSPACE_NAME = WORKSPACE_FRAME.locator(
                "//button[contains(@data-tour-id,'save-workspace-details-button')]//div[contains(text(),'Save')]");
        this.TAB_PANEL_SEARCH = WORKSPACE_FRAME.locator("//div[@role='tabpanel']//input[@placeholder='Search']");
        this.TO_YEAR = WORKSPACE_FRAME.locator("//input[@data-testid='bi-slider-input-0']");
        this.FROM_YEAR = WORKSPACE_FRAME.locator("//input[@data-testid='bi-slider-input-1']");
        this.REACHABLE_AUDIENCE =
                WORKSPACE_FRAME.locator("//input[@type='checkbox' and contains(@class,'PrivateSwitchBase-input')]");
        this.SELECT_DESELECT_ALL = WORKSPACE_FRAME.locator("//div[contains(text(),'Select/Deselect All')]");
        this.AI_CONFIGURATOR_BTN = WORKSPACE_FRAME.locator("//p[contains(text(),'AI Configurator')]");
        this.AUDIENCE_DESCRIPTION_TEXTAREA =
                WORKSPACE_FRAME.locator("//textarea[@placeholder='Describe your Audience']");
        this.BUILD_AUDIENCE_BTN = WORKSPACE_FRAME.locator("//div[text()='Build Audience']");
        this.TRY_ANOTHER_PROMPT_BTN = WORKSPACE_FRAME.locator("//div[text()='Try another prompt']");
        this.FILTER_HEADER_TITLE = WORKSPACE_FRAME.locator("//div[contains(@data-tour-id, 'filters-container')]");
        this.MAP_TOOL_TIP = WORKSPACE_FRAME
                .locator("#extension-root iframe")
                .contentFrame()
                .locator(
                        "//div[contains(@class,'MapTooltip')]/div/div/div[text()='Identified NPIs']/following-sibling::div");
        this.DELETE_FILTER = WORKSPACE_FRAME.locator("//div[contains(@data-tour-id, 'filters-container')]//button");
        this.CAMERA_CONTROL_ICON = WORKSPACE_FRAME
                .locator("#extension-root iframe")
                .contentFrame()
                .locator("//button[@title='Map camera controls']");
        this.ZOOM_OUT = WORKSPACE_FRAME
                .locator("#extension-root iframe")
                .contentFrame()
                .locator("//div[@class='gmnoprint']//button[@title='Zoom out' and @class='gm-control-active']");
        this.MAP_CONTENT = WORKSPACE_FRAME
                .locator("#extension-root iframe")
                .contentFrame()
                .locator("div[aria-label='Dashboard Content']");
        this.DASHBOARD_FILTER_TITLE = WORKSPACE_FRAME.locator("//p[contains(text(),'Dashboard Filters')]");
        this.MERGED_TEXT = WORKSPACE_FRAME.locator("//p[contains(text(),'Merged with Primary after Save')]");
        this.DASHBOARD_FILTERS = WORKSPACE_FRAME.locator(
                "//p[contains(text(),'Dashboard Filters')]/ancestor::div/following-sibling::div//p");
        this.MOMENTS = WORKSPACE_FRAME.locator("//div[@data-tour-id='filters-drawer']//p[normalize-space(.)='IAB']");
        this.IBHEALTH = WORKSPACE_FRAME.locator("//div[@data-tour-id='filters-drawer']//p[normalize-space(.)='WebMD']");
        this.MOMENTS_WIDGET =
                WORKSPACE_FRAME.getByText("Contextual", new FrameLocator.GetByTextOptions().setExact(true));
        this.CLAIMS_WIDGET = WORKSPACE_FRAME.getByText("Clinical", new FrameLocator.GetByTextOptions().setExact(true));
        this.OWNED_AND_OPERATED_SECTION = WORKSPACE_FRAME
                .locator("#extension-root iframe")
                .contentFrame()
                .locator("//span[text()='Owned & Operated']");
        this.WORKSPACE_EDIT_BUTTON = WORKSPACE_FRAME.locator("//button//div[text()='Edit']");
        this.WORKSPACE_HEADER =
                WORKSPACE_FRAME.locator("//div[@data-tour-id='workspace-back-button']/following-sibling::div//h1");
        this.ADVERTISER_LIST = WORKSPACE_FRAME.locator("//p[text()='Advertisers']");
        this.SEARCH_ADVERTISER = WORKSPACE_FRAME.locator("//input[@placeholder='Search']");
        this.ADVERTISER_BUTTON = WORKSPACE_FRAME.locator("//button[contains(@data-tour-id,'workspace-advertiser')]");
        this.SPECIALITY_PANEL = WORKSPACE_FRAME.locator("//div[@id='panel-speciality_all']");
        this.INCLUDE_EXCLUDE_CHECK = WORKSPACE_FRAME.locator("//button[@data-testid='bi-include-exclude-check']");
        this.ALERT = WORKSPACE_FRAME.locator("//div[contains(@class, 'Toastify')]//div[@role='alert']//p");
    }

    public void enterWorkspaceName(String workspaceName) {
        WORKSPACE_NAME.clear();
        WORKSPACE_NAME.fill(workspaceName);
    }

    public void selectAdvertiser(String advertiser) {
        waitUtility.waitForLocatorVisible(ADVERTISER_LIST);
        SEARCH_ADVERTISER.fill(advertiser);
        assertThat(ADVERTISER_BUTTON).hasCount(1);

        for (int i = 0; i < ADVERTISER_BUTTON.count(); i++) {
            if (ADVERTISER_BUTTON.nth(i).getAttribute("data-tour-id").contains(advertiser)) {
                ADVERTISER_BUTTON.nth(i).click();
                break;
            }
        }
    }

    public void selectAdvertiserFromWorkspaceEdit(String advertiser) {
        SEARCH_ADVERTISER_IN_EDIT_WORKSPACE.click();
        SEARCH_ADVERTISER_IN_EDIT_WORKSPACE.fill(advertiser);
        SEARCH_ADVERTISER_IN_EDIT_WORKSPACE.press("ArrowDown");
        SEARCH_ADVERTISER_IN_EDIT_WORKSPACE.press("Enter");
    }

    public void saveWorkspaceName() {
        SAVE_WORKSPACE_NAME.click();
    }

    public void waitUntilAlertDisappears() {
        waitUtility.waitForLocatorHidden(ALERT);
    }

    public void waitForDashboardLoad() {
        DASHBOARD_CONTENT.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        DASHBOARD_ELEMENT.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        DASHBOARD_RELOAD_ICON.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void clickAddFilter() {
        ADD_FILTER.click();
    }

    public void selectFilter(String filter, List<String> options) {
        SEARCH_FILTER.clear();
        SEARCH_FILTER.fill(filter);
        WORKSPACE_FRAME
                .locator(String.format("//span[contains(text(),'%s')]", filter))
                .first()
                .click();
        switch (filter) {
            case "NPI List Name",
                    "Medical School",
                    "Profession",
                    "Specialty",
                    "State",
                    "Facility Name",
                    "Patient Facility",
                    "Prescriptions",
                    "Prescribing behavior",
                    "Diagnoses",
                    "Procedures",
                    "IAB",
                    "MeSH":
                if (filter.equals("Specialty")) {
                    WORKSPACE_FRAME
                            .locator("//span[contains(text(),'All Specialties')]")
                            .click();
                    waitUtility.waitForLocatorVisible(SPECIALITY_PANEL);
                }
                for (String option : options) {
                    waitUtility.waitForLocatorVisible(INCLUDE_EXCLUDE_CHECK.last());
                    Locator locator = WORKSPACE_FRAME.locator(String.format(
                            "//p[contains(text(),'%s')]/preceding-sibling::div/button[@data-testid='bi-include-exclude-check']",
                            option.trim()));
                    TAB_PANEL_SEARCH.fill(option.trim());
                    waitUtility.waitForLocatorVisible(locator.first());
                    page.waitForTimeout(1000);
                    if (SELECT_DESELECT_ALL.isVisible()) SELECT_DESELECT_ALL.click();
                    else locator.first().click();
                }
                break;
            case "NPI Gender",
                    "NPI Age",
                    "Years Practiced",
                    "Number of Patients",
                    "Patient Age",
                    "Patient Gender",
                    "Gender":
                for (String option : options) {
                    WORKSPACE_FRAME
                            .locator(String.format("//label[contains(text(),'%s')]", option.trim()))
                            .click();
                }
                break;
            case "Site", "Search":
                for (String option : options) {
                    WORKSPACE_FRAME
                            .locator(String.format("//span[contains(text(),'%s')]", option.trim()))
                            .click();
                }
                break;
            case "Graduation Year":
                for (String option : options) {
                    String[] array = option.split("-");
                    TO_YEAR.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                    page.waitForTimeout(1000);
                    TO_YEAR.fill(array[0]);
                    FROM_YEAR.fill(array[1]);
                }
                break;
            case "Net Worth":
                for (String option : options) {
                    WORKSPACE_FRAME
                            .locator(String.format("//label[contains(text(),'%s')]", option.trim()))
                            .first()
                            .click();
                }
                break;
            case "Reachable Audience":
                REACHABLE_AUDIENCE.click();
                break;
        }
    }

    public void clickFilterOKButton() {
        FILTER_OK_BUTTON.click();
    }

    public void applyFilter() {
        FILTER_CLOSE_BUTTON.click();
    }

    public List<String> verifyAllSelectedFilters() {
        DASHBOARD_RELOAD_ICON.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        return APPLIED_FILTER.allInnerTexts();
    }

    public List<String> verifyAllSelectedOptions() {
        return APPLIED_FILTER_OPTION.allInnerTexts();
    }

    public void saveExplorerWorkspace() {
        waitForDashboardLoad();
        SAVE_WORKSPACE.first().click();
    }

    public String workspaceSuccess() {
        EXPLORER_WORKSPACE_SUCCESS.waitFor();
        return EXPLORER_WORKSPACE_SUCCESS.innerText();
    }

    public void clickAIConfigurator() {
        AI_CONFIGURATOR_BTN.click();
    }

    public boolean describeAudience(String aiPrompt) {
        AUDIENCE_DESCRIPTION_TEXTAREA.fill(aiPrompt);
        BUILD_AUDIENCE_BTN.click();
        TRY_ANOTHER_PROMPT_BTN.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        return FILTER_HEADER_TITLE.isVisible();
    }

    public List<String> checkNPIDetails() {
        DASHBOARD_RELOAD_ICON.waitFor(
                new Locator.WaitForOptions().setTimeout(240000).setState(WaitForSelectorState.VISIBLE));
        return DASHBOARD_ELEMENT.allInnerTexts();
    }

    public void deleteFilter() {
        DELETE_FILTER.first().click();
    }

    public void hoverOverNPIVisualsIcon(List<String> npiVisualList) {
        for (String s : npiVisualList) {
            String visual = s.trim();
            boolean isInView;
            switch (s.trim()) {
                case "NPI Geographic Location", "NPI Facilities Geography", "NPI ZIP Codes":
                    Locator MAP_TILE = WORKSPACE_FRAME
                            .locator("#extension-root iframe")
                            .contentFrame()
                            .locator(String.format(
                                    "//h2[@data-title='%s']/parent::div/following-sibling::div//div[contains(@style,'z-index: 3;')]",
                                    visual));
                    isInView = CommonUtils.scrollElementIntoView(MAP_CONTENT, CAMERA_CONTROL_ICON, 1000, 100, page);
                    if (!isInView) {
                        continue;
                    }
                    if (s.contains("NPI Facilities Geography")) {
                        ZOOM_OUT.first().click();
                    } else if (s.contains("NPI ZIP Codes")) {
                        ZOOM_OUT.nth(1).click();
                    }

                    BoundingBox box = MAP_TILE.boundingBox();
                    CommonUtils.hoverAndClick(page, box, MAP_TOOL_TIP);
                    break;
                case "Top 20 Market Areas",
                        "Top 20 Professions",
                        "Top 20 Specialties",
                        "Top 20 Insurance Providers",
                        "Top 20 Prescriptions",
                        "Top 20 Diagnoses",
                        "Top 20 Procedures",
                        "Top 20 MeSH Categories",
                        "Top 20 IAB Categories":
                    Locator TOP_ENTITIES = WORKSPACE_FRAME
                            .locator("#extension-root iframe")
                            .contentFrame()
                            .locator(String.format(
                                    "//h2[@data-title='%s']/parent::div/following-sibling::div//div[@class='ag-center-cols-container']/div",
                                    visual));
                    isInView = CommonUtils.scrollElementIntoView(MAP_CONTENT, TOP_ENTITIES, 1000, 100, page);
                    if (!isInView) {
                        continue;
                    }
                    if (TOP_ENTITIES.count() > 0) {
                        TOP_ENTITIES.first().click();
                    }
                    break;
                case "NPI Age Range",
                        "NPI Gender",
                        "Patient Age Range",
                        "Patient Gender",
                        "Net Worth",
                        "Years Practiced",
                        "Patient Distribution":
                    Locator NPI_PATIENT_ENTITIES = WORKSPACE_FRAME
                            .locator("#extension-root iframe")
                            .contentFrame()
                            .locator(String.format(
                                    "//h2[@data-title='%s']/parent::div/following-sibling::div//*[name()='path' and contains(@class, 'highcharts-point')]",
                                    visual));
                    isInView = CommonUtils.scrollElementIntoView(MAP_CONTENT, NPI_PATIENT_ENTITIES, 1000, 100, page);
                    if (!isInView) {
                        continue;
                    }
                    if (NPI_PATIENT_ENTITIES.count() > 0) {
                        NPI_PATIENT_ENTITIES.first().click();
                    }
                    break;
            }
            DASHBOARD_RELOAD_ICON.waitFor(
                    new Locator.WaitForOptions().setTimeout(240000).setState(WaitForSelectorState.VISIBLE));
        }
    }

    public boolean verifyCrossFiltersDisplayed() {
        return DASHBOARD_FILTER_TITLE.isVisible() && MERGED_TEXT.isVisible() && DASHBOARD_FILTERS.count() > 0;
    }

    public List<String> fetchMergedFilters() {
        List<String> mergeFilterName = new ArrayList<>();
        for (String dashboardFilter : DASHBOARD_FILTERS.allInnerTexts()) {
            String[] parts = dashboardFilter.split(":");
            if (parts.length > 0) {
                mergeFilterName.add(parts[0].trim()); // Extract value before colon and trim whitespace
            }
        }
        return mergeFilterName;
    }

    public boolean isOwnedAndOperatedSectionAvailable() {
        return OWNED_AND_OPERATED_SECTION.isVisible();
    }

    public boolean verifyPermissionFilters(String permissions) {
        boolean isFilterVisible = true;
        switch (permissions) {
            case "MOMENTS":
                ADD_FILTER.click();
                waitUtility.waitForLocatorVisible(MOMENTS);
                isFilterVisible = MOMENTS.isVisible();
                FILTER_CLOSE_BUTTON.click();
                break;
            case "IB HEALTH":
                ADD_FILTER.click();
                waitUtility.waitForLocatorVisible(IBHEALTH);
                isFilterVisible = IBHEALTH.isVisible();
                FILTER_CLOSE_BUTTON.click();
                break;
        }

        return isFilterVisible;
    }

    public boolean verifyWidgets(String visualization) {
        return switch (visualization) {
            case "MOMENTS", "IB HEALTH" -> {
                waitUtility.waitForLocatorVisible(MOMENTS_WIDGET);
                yield MOMENTS_WIDGET.isVisible();
            }
            case "CLAIMS DATA" -> {
                waitUtility.waitForLocatorVisible(CLAIMS_WIDGET);
                yield CLAIMS_WIDGET.isVisible();
            }
            default -> false;
        };
    }

    public String isAdvertiserDisabled() {
        return ADVERTISER_SELECTED.evaluate("el => getComputedStyle(el).color").toString();
    }

    public void clickEditWorkspace() {
        WORKSPACE_EDIT_BUTTON.click();
        waitUtility.waitForLocatorVisible(WORKSPACE_NAME);
    }

    public String fetchWorkspaceHeader() {
        return WORKSPACE_HEADER.textContent().trim();
    }

    public void selectRecency(String recency) {
        WORKSPACE_FRAME
                .locator("//p[normalize-space()='Recency']/following-sibling::div//label[normalize-space()='" + recency
                        + "']")
                .click();
    }

    public String fetchRecencyValue(String filterType) {
        Locator recencyLocator = WORKSPACE_FRAME.locator(String.format(
                "//p[normalize-space()='%s Recency']/parent::div//following-sibling::div//p", filterType));
        return recencyLocator.textContent().trim();
    }

    public void selectDraftOption(String DraftOption) {
        Locator DRAFT_OPTION = WORKSPACE_FRAME.locator(String.format("//button/div[text()='%s']", DraftOption));
        DRAFT_OPTION.click();
    }
}
