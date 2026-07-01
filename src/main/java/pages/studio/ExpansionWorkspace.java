package pages.studio;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import utils.CommonUtils;
import utils.WaitUtility;

public class ExpansionWorkspace {
    private final Page page;
    private final FrameLocator WORKSPACE_FRAME;
    private final Locator ADVERTISER_DROPDOWN;
    private final Locator SOURCE_AUDIENCE;
    private final Locator NPI_LIST;
    private final Locator SELECT_SOURCE_AUDIENCE;
    private final Locator EXPAND_CARE_TEAM;
    private final Locator EXPAND_AFFILIATION_GRAPH;
    private final Locator ADD_FILTER;
    private final Locator NPI_AGE;
    private final Locator BELOW_25;
    private final Locator OK_FILTER;
    private final Locator SAVE;
    private final Locator WORKSPACE_NAME;
    private final Locator SOURCE_SEARCH;
    private final Locator POPUP_CLOSE;
    private final Locator DROPDOWN_CARE_TEAM;
    private final Locator DROPDOWN_CARE_TEAM_VALUE;
    private final Locator EXPANDED_AUDIENCE_COUNT;
    private final Locator DRAFT_PRIVATE;
    private final Locator DRAFT_PUBLIC;
    private final Locator SELECT_HCP;
    private final Locator SELECT_LIFE;
    private final Locator TOTAL_NPI_COUNT;
    private final Locator PUBLISH_BUTTON;
    private final Locator NPI_PUBLISH_ALERT;
    private final Locator SELECT_AUDIENCE_MANAGER;
    private final Locator SCHEDULE_NPI_BUTTON;
    private final Locator SCHEDULE_SAVE_BUTTON;
    private final Locator DOWNLOAD_REPORT_BUTTON;
    private final Locator REPORT_NAME_INPUT;
    private final Locator SCHEDULE_REPORT_BUTTON;
    WaitUtility waitUtility;

    public ExpansionWorkspace(Page page) {
        this.page = page;
        this.waitUtility = new WaitUtility(page);
        this.WORKSPACE_FRAME = page.frameLocator("iframe").frameLocator("iframe");
        this.ADVERTISER_DROPDOWN = WORKSPACE_FRAME.getByPlaceholder("Select Advertiser");
        this.SOURCE_AUDIENCE =
                WORKSPACE_FRAME.locator("button").filter(new Locator.FilterOptions().setHasText("Studio Workspace"));
        this.NPI_LIST = WORKSPACE_FRAME.locator("button").filter(new Locator.FilterOptions().setHasText("NPI List"));
        this.SELECT_SOURCE_AUDIENCE = WORKSPACE_FRAME.getByText("My playground");
        this.SOURCE_SEARCH = WORKSPACE_FRAME.getByPlaceholder("Search");
        this.EXPAND_CARE_TEAM = WORKSPACE_FRAME.getByText("Expand With Care Team");
        this.EXPAND_AFFILIATION_GRAPH = WORKSPACE_FRAME.getByText("Expand With Affiliation Graph");
        this.ADD_FILTER = WORKSPACE_FRAME.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Add Filters"));
        this.NPI_AGE = WORKSPACE_FRAME.locator("div")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^NPI Age$")))
                .nth(3);
        this.BELOW_25 = WORKSPACE_FRAME.getByLabel("Below");
        this.OK_FILTER = WORKSPACE_FRAME.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Ok"));
        this.SAVE = WORKSPACE_FRAME.locator(".styles__StyledResetUpdate-sc-njp72g-0 > button:nth-child(2)");
        this.POPUP_CLOSE = WORKSPACE_FRAME.locator("div")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Select Filter$")))
                .getByRole(AriaRole.BUTTON);
        this.WORKSPACE_NAME = WORKSPACE_FRAME.getByRole(AriaRole.TEXTBOX).nth(3);
        this.DROPDOWN_CARE_TEAM = WORKSPACE_FRAME.getByRole(AriaRole.COMPLEMENTARY)
                .getByRole(AriaRole.COMBOBOX, new Locator.GetByRoleOptions().setName("undefined combobox"))
                .getByRole(AriaRole.TEXTBOX);
        this.DROPDOWN_CARE_TEAM_VALUE =
                WORKSPACE_FRAME.getByRole(AriaRole.OPTION, new FrameLocator.GetByRoleOptions().setName("Basic"));
        this.EXPANDED_AUDIENCE_COUNT = WORKSPACE_FRAME.getByText("Expanded with");
        this.DRAFT_PRIVATE = WORKSPACE_FRAME.locator("//button[@type='button']/div[text()='Private']");
        this.DRAFT_PUBLIC = WORKSPACE_FRAME.locator("//button[@type='button']/div[text()='Public']");
        this.SELECT_HCP =
                WORKSPACE_FRAME.locator(" //span[contains(text(),'HCP')]/parent::label/preceding-sibling::div//input");
        this.SELECT_LIFE =
                WORKSPACE_FRAME.locator(" //span[contains(text(),'Life')]/parent::label/preceding-sibling::div//input");
        this.TOTAL_NPI_COUNT = WORKSPACE_FRAME.locator(
                "//h3[text()='Total NPIs']"
                        + "/ancestor::div[@data-testid='test-single-value-container']"
                        + "//span[contains(@class,'StyleSpan')]");
        this.PUBLISH_BUTTON =
                WORKSPACE_FRAME.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Publish"));
        this.NPI_PUBLISH_ALERT = WORKSPACE_FRAME.locator("//p[contains(text(), 'Workspace saved successfully')]");
        this.SELECT_AUDIENCE_MANAGER = WORKSPACE_FRAME.locator(
                "//span[contains(text(),'Audience Manager')]/parent::label/preceding-sibling::div//input");
        this.SCHEDULE_NPI_BUTTON =
                WORKSPACE_FRAME.locator("//div[contains(text(),'Schedule NPIs') or contains(text(),'Schedule NPI')]");
        this.SCHEDULE_SAVE_BUTTON =
                WORKSPACE_FRAME.locator("(//button[contains(@class,'ButtonBase')]/div[text()='Save'])[2]");
        this.DOWNLOAD_REPORT_BUTTON =
                WORKSPACE_FRAME.locator("//div[contains(text(),'Download Report')]");
        this.REPORT_NAME_INPUT = WORKSPACE_FRAME.locator(
                "//input[contains(@placeholder,'Report Name') or contains(@name,'reportName') or contains(@id,'reportName')]");
        this.SCHEDULE_REPORT_BUTTON = WORKSPACE_FRAME.locator("//div[normalize-space()='Schedule Report']");
    }

    public void clickAdvertiserDropdown(String advertiser) {
        page.waitForLoadState();
        ADVERTISER_DROPDOWN.click();
        Locator listbox = WORKSPACE_FRAME.locator("ul[role='listbox']");
        listbox.locator("li")
                .filter(new Locator.FilterOptions().setHasText(advertiser))
                .first()
                .click();
        page.keyboard().press("Escape");
        waitUtility.waitForLocatorHidden(listbox);
    }

    public void selectSourceAudience(String string) {
        page.waitForLoadState();
        if (string.equals("Studio Workspace")) {
            SOURCE_AUDIENCE.click();
            SOURCE_SEARCH.fill("My playground");
            SELECT_SOURCE_AUDIENCE.click();
        } else {
            System.out.println("ok");
        }
    }

    public void selectExpandCareTeam() {
        page.waitForLoadState();
        EXPAND_CARE_TEAM.click();
        DROPDOWN_CARE_TEAM.click();
        DROPDOWN_CARE_TEAM_VALUE.click();
    }

    public void selectExpandAffGraph() {
        EXPAND_AFFILIATION_GRAPH.click();
    }

    public void addFilter() {
        page.waitForLoadState();
        ADD_FILTER.click();
        NPI_AGE.click();
        BELOW_25.click();
        OK_FILTER.click();
        POPUP_CLOSE.click();
    }

    public void saveExpansionWorkspace() {
        page.waitForLoadState();
        SAVE.click();
        page.waitForLoadState();
    }

    public void renameExpansion(String workspaceName) {
        WORKSPACE_NAME.click();
        WORKSPACE_NAME.click();
        WORKSPACE_NAME.fill(workspaceName);
    }

    /** The advertiser combobox can intermittently re-open; its modal-root option list then intercepts clicks. */
    private void dismissOpenListbox() {
        Locator listbox = WORKSPACE_FRAME.locator("ul[role='listbox']");
        if (listbox.isVisible()) {
            page.keyboard().press("Escape");
            waitUtility.waitForLocatorHidden(listbox);
        }
    }

    public void selectSourceAudienceWithOptions(String sourceAudience, String options) {
        page.waitForLoadState();
        dismissOpenListbox();

        if (sourceAudience.equals("Studio Workspace")) {
            SOURCE_AUDIENCE.click();
        } else if (sourceAudience.equals("NPI List")) {
            NPI_LIST.click();
        }

        waitUtility.waitForLocatorVisible(SOURCE_SEARCH);
        SOURCE_SEARCH.fill(options);
        page.waitForTimeout(2000);
        WORKSPACE_FRAME.getByText(options, new FrameLocator.GetByTextOptions().setExact(true))
                .first()
                .click();
    }

    public void selectExpandedAudience(String expandedAudience) {
        page.waitForLoadState();
        List<String> tokens = Arrays.stream(expandedAudience.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        boolean wantsAffiliationGraph =
                tokens.stream().anyMatch(t -> t.equalsIgnoreCase("Expand with Affiliation Graph"));
        List<String> careTeamTypes = tokens.stream()
                .filter(t -> !t.equalsIgnoreCase("Expand with Affiliation Graph"))
                .map(t -> t.equalsIgnoreCase("Expand with Care Team") ? "Basic" : t)
                .toList();

        if (!careTeamTypes.isEmpty()) {
            EXPAND_CARE_TEAM.click();

            for (String careTeamType : careTeamTypes) {
                waitUtility.waitForLocatorVisible(DROPDOWN_CARE_TEAM);
                DROPDOWN_CARE_TEAM.click();
                WORKSPACE_FRAME.getByRole(AriaRole.OPTION, new FrameLocator.GetByRoleOptions().setName(careTeamType))
                        .click();
                waitUtility.waitForLocatorVisible(EXPANDED_AUDIENCE_COUNT);
            }
        }

        if (wantsAffiliationGraph) {
            selectExpandAffGraph();
        }
    }

    public void selectDraftOption(String draftOption) {
        page.waitForLoadState();
        if (draftOption.equalsIgnoreCase("Private")) {
            DRAFT_PRIVATE.click();
        } else if (draftOption.equalsIgnoreCase("Public")) {
            DRAFT_PUBLIC.click();
        } else {
            throw new IllegalArgumentException("Unsupported draft option: " + draftOption);
        }
    }

    public String fetchTotalNPICount() {
        waitUtility.waitForLocatorVisible(TOTAL_NPI_COUNT);
        return TOTAL_NPI_COUNT.textContent().replace(",", "");
    }

    public String fetchTotalNPICountAfterExpansion() {
        page.waitForLoadState();
        waitUtility.waitForLocatorVisible(EXPANDED_AUDIENCE_COUNT.first());
        return TOTAL_NPI_COUNT.textContent().replace(",", "");
    }

    public void clickPublish() {
        PUBLISH_BUTTON.click();
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

    public void selectPublishPlatforms(List<String> platforms) {
        for (String platform : platforms) {
            String trimmed = platform.trim();
            switch (trimmed) {
                case "Life" -> {
                    if (SELECT_LIFE.getAttribute("aria-checked").contains("false")) SELECT_LIFE.click();
                }
                case "HCP365" -> {
                    if (SELECT_HCP.getAttribute("aria-checked").contains("false")) SELECT_HCP.click();
                }
                case "Audience Manager" -> {
                    if (SELECT_AUDIENCE_MANAGER.getAttribute("aria-checked").contains("false"))
                        SELECT_AUDIENCE_MANAGER.click();
                }
                default -> throw new IllegalArgumentException("Unsupported publish platform: " + trimmed);
            }
        }
    }

    public void clickScheduleNPIButton() {
        SCHEDULE_NPI_BUTTON.click();
    }

    public void enterScheduleDataAndSave() {
        SCHEDULE_SAVE_BUTTON.click();
    }

    public void clickDownloadReport() {
        DOWNLOAD_REPORT_BUTTON.click();
    }

    public void enterReportName() {
        REPORT_NAME_INPUT.fill("Report_" + CommonUtils.timeStampCalculation());
    }

    public void clickScheduleReport() {
        SCHEDULE_REPORT_BUTTON.click();
    }
}
