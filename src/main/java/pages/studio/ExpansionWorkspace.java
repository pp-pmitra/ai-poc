package pages.studio;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import utils.WaitUtility;

public class ExpansionWorkspace {
    private final Page page;
    private final Locator HCP_AUDIENCEEXP;
    private final Locator ADVERTISER_DROPDOWN;
    private final Locator SOURCE_AUDIENCE;
    private final Locator NPILIST;
    private final Locator SELECT_SOURCE_AUDIENCE;
    private final Locator EXPAND_CARE_TEAM;
    private final Locator EXPAND_AFF_GRAPH;
    private final Locator ADD_FILTER;
    private final Locator NPI_AGE;
    private final Locator BELOW_25;
    private final Locator OK_FILTER;
    private final Locator SAVE;
    private final Locator WORKSPACE_NAME;
    private final Locator SOURCE_SEARCH;
    private final Locator POPUP_CLOSE;
    private final Locator DROPDOWN_CARE_TEAM;
    private final Locator DROPDOWN_CARETEAM_VALUE;
    private final Locator EXPANDED_AUDIENCE_COUNT;
    private final Locator DRAFT_PRIVATE;
    private final Locator DRAFT_PUBLIC;
    private final FrameLocator WORKSPACE_FRAME;
    WaitUtility waitUtility;

    public ExpansionWorkspace(Page page) {
        this.page = page;
        this.waitUtility = new WaitUtility(page);
        this.WORKSPACE_FRAME = page.frameLocator("iframe").frameLocator("iframe");
        this.HCP_AUDIENCEEXP = WORKSPACE_FRAME.getByRole(AriaRole.IMG).nth(2);
        this.ADVERTISER_DROPDOWN = WORKSPACE_FRAME.getByPlaceholder("Select Advertiser");
        this.SOURCE_AUDIENCE =
                WORKSPACE_FRAME.locator("button").filter(new Locator.FilterOptions().setHasText("Studio Workspace"));
        this.NPILIST = WORKSPACE_FRAME.locator("button").filter(new Locator.FilterOptions().setHasText("NPI List"));
        this.SELECT_SOURCE_AUDIENCE = WORKSPACE_FRAME.getByText("My playground");
        this.SOURCE_SEARCH = WORKSPACE_FRAME.getByPlaceholder("Search");
        this.EXPAND_CARE_TEAM = WORKSPACE_FRAME.getByText("Expand With Care Team");
        this.EXPAND_AFF_GRAPH = WORKSPACE_FRAME.getByText("Expand With Affiliation Graph");
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
        this.DROPDOWN_CARETEAM_VALUE =
                WORKSPACE_FRAME.getByRole(AriaRole.OPTION, new FrameLocator.GetByRoleOptions().setName("Basic"));
        this.EXPANDED_AUDIENCE_COUNT = WORKSPACE_FRAME.getByText("Expanded with");
        this.DRAFT_PRIVATE = WORKSPACE_FRAME.getByRole(AriaRole.RADIO, new FrameLocator.GetByRoleOptions().setName("Private"));
        this.DRAFT_PUBLIC = WORKSPACE_FRAME.getByRole(AriaRole.RADIO, new FrameLocator.GetByRoleOptions().setName("Public"));
    }

    public void clickAdvertiserDropdown(String advertiser) {
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
        DROPDOWN_CARETEAM_VALUE.click();
    }

    public void selectExpandAffGraph() {
        EXPAND_AFF_GRAPH.click();
    }

    public void addFilter() {
        page.waitForLoadState();
        ADD_FILTER.click();
        NPI_AGE.click();
        BELOW_25.click();
        OK_FILTER.click();
        POPUP_CLOSE.click();
    }

    public void saveExpansion() {
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
            NPILIST.click();
        }
        // The picker's list/search loads asynchronously after the source card is selected.
        waitUtility.waitForLocatorVisible(SOURCE_SEARCH);
        // Filter by name, then click the row whose name matches exactly. Exact matches sort first, so the leading
        // result is the intended workspace and not a PB_Test_* sibling.
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
        // "Expand with Care Team" means the Care Team checkbox with its default type (Basic); the Basic / Exact
        // Diagnosis / Extended / Professions / Specialities values are options of the "Care Team Type" dropdown.
        List<String> careTeamTypes = tokens.stream()
                .filter(t -> !t.equalsIgnoreCase("Expand with Affiliation Graph"))
                .map(t -> t.equalsIgnoreCase("Expand with Care Team") ? "Basic" : t)
                .toList();
        if (!careTeamTypes.isEmpty()) {
            EXPAND_CARE_TEAM.click();
            // "Care Team Type" is a single-select dropdown, so when several types are supplied each selection
            // replaces the previous one (the last wins). Wait for the count to settle between selections, otherwise
            // the dashboard recompute spinner intercepts the dropdown.
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

    public void verifyExpandedAudienceCount() {
        page.waitForLoadState();
        waitUtility.waitForLocatorVisible(EXPANDED_AUDIENCE_COUNT.first());
    }

    public void selectDraftOption(String draftOption) {
        page.waitForLoadState();
        if (draftOption.equalsIgnoreCase("Private")) {
            DRAFT_PRIVATE.click();
        } else {
            DRAFT_PUBLIC.click();
        }
    }
}
