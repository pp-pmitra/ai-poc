package pages.studio;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.Pattern;
import utils.WaitUtility;

public class ExpansionWorkspace {
    private final Page page;
    private final Locator HCP_AUDIENCEEXP;
    private final Locator ADVERTISER_DROPDOWN;
    private final Locator SELECT_ADVERTISER;
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
    private final Locator ENTER_MY_PLAYGROUND;
    private final Locator POPUP_CLOSE;
    private final Locator DROPDOWN_CARE_TEAM;
    private final Locator DROPDOWN_CARETEAM_VALUE;
    private final Locator NPILIST_SEARCH;
    private final Locator EXPANDED_AUDIENCE_COUNT;
    private final Locator DRAFT_PRIVATE;
    private final Locator DRAFT_PUBLIC;
    private final FrameLocator FRAME;
    WaitUtility waitUtility;

    public ExpansionWorkspace(Page page) {
        this.page = page;
        this.waitUtility = new WaitUtility(page);
        // The HCP Audience Expansion builder is rendered inside nested iframes (Studio -> Looker embed ->
        // looker-extension). The iframes no longer carry title="overview", so resolve them positionally the same
        // way Workspace.java does. All builder locators hang off this shared FrameLocator.
        this.FRAME = page.frameLocator("iframe").frameLocator("iframe");
        this.HCP_AUDIENCEEXP = FRAME.getByRole(AriaRole.IMG).nth(2);
        this.ADVERTISER_DROPDOWN = FRAME.getByPlaceholder("Select Advertiser");
        this.SELECT_ADVERTISER =
                FRAME.getByRole(AriaRole.OPTION, new FrameLocator.GetByRoleOptions().setName("Abbvie"));
        this.SOURCE_AUDIENCE = FRAME.getByRole(
                AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Studio Workspace Extend"));
        this.NPILIST = FRAME.locator("//div[@class='styles__StyledIcon-sc-d00f7j-2 QkzJU']");
        this.SELECT_SOURCE_AUDIENCE = FRAME.getByText("My playground");
        this.ENTER_MY_PLAYGROUND =
                FRAME.getByRole(AriaRole.TEXTBOX, new FrameLocator.GetByRoleOptions().setName("Search"));
        this.EXPAND_CARE_TEAM = FRAME.getByText("Expand With Care Team");
        this.EXPAND_AFF_GRAPH = FRAME.getByText("Expand With Affiliation Graph");
        this.ADD_FILTER = FRAME.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Add Filters"));
        this.NPI_AGE = FRAME.locator("div")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^NPI Age$")))
                .nth(3);
        this.BELOW_25 = FRAME.getByLabel("Below");
        this.OK_FILTER = FRAME.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Ok"));
        this.SAVE = FRAME.locator(".styles__StyledResetUpdate-sc-njp72g-0 > button:nth-child(2)");
        this.POPUP_CLOSE = FRAME.locator("div")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Select Filter$")))
                .getByRole(AriaRole.BUTTON);
        this.WORKSPACE_NAME = FRAME.getByRole(AriaRole.TEXTBOX).nth(3);
        this.DROPDOWN_CARE_TEAM = FRAME.getByRole(AriaRole.COMPLEMENTARY)
                .getByRole(AriaRole.COMBOBOX, new Locator.GetByRoleOptions().setName("undefined combobox"))
                .getByRole(AriaRole.TEXTBOX);
        this.DROPDOWN_CARETEAM_VALUE =
                FRAME.getByRole(AriaRole.OPTION, new FrameLocator.GetByRoleOptions().setName("Basic"));
        this.NPILIST_SEARCH = FRAME.getByRole(AriaRole.TEXTBOX, new FrameLocator.GetByRoleOptions().setName("Search"));
        this.EXPANDED_AUDIENCE_COUNT = FRAME.locator(
                "//span[contains(@class,'count')] | //*[contains(text(),'Expanded')]//following-sibling::*[contains(@class,'count')]");
        this.DRAFT_PRIVATE = FRAME.getByRole(AriaRole.RADIO, new FrameLocator.GetByRoleOptions().setName("Private"));
        this.DRAFT_PUBLIC = FRAME.getByRole(AriaRole.RADIO, new FrameLocator.GetByRoleOptions().setName("Public"));
    }

    public void clickAdvertiserDropdown(String advertiser) {
        ADVERTISER_DROPDOWN.click();
        ADVERTISER_DROPDOWN.fill(advertiser);
        FRAME.getByRole(AriaRole.OPTION, new FrameLocator.GetByRoleOptions().setName(advertiser))
                .click();
    }

    public void selectSourceAudience(String string) {
        page.waitForLoadState();
        if (string.equals("Studio Workspace")) {
            SOURCE_AUDIENCE.click();
            ENTER_MY_PLAYGROUND.fill("My playground");
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

    public void selectSourceAudienceWithOptions(String sourceAudience, String options) {
        page.waitForLoadState();
        if (sourceAudience.equals("Studio Workspace")) {
            SOURCE_AUDIENCE.click();
            ENTER_MY_PLAYGROUND.fill(options);
            FRAME.getByText(options).first().click();
        } else if (sourceAudience.equals("NPI List")) {
            NPILIST.click();
            NPILIST_SEARCH.fill(options);
            FRAME.getByText(options).first().click();
        }
    }

    public void selectExpandedAudience(String expandedAudience) {
        page.waitForLoadState();
        for (String type : expandedAudience.split(",\\s*")) {
            String trimmed = type.trim();
            if (trimmed.equalsIgnoreCase("Expand with Care Team")) {
                selectExpandCareTeam();
            } else if (trimmed.equalsIgnoreCase("Expand with Affiliation Graph")) {
                selectExpandAffGraph();
            } else {
                FRAME.getByRole(AriaRole.CHECKBOX, new FrameLocator.GetByRoleOptions().setName(trimmed))
                        .check();
            }
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
