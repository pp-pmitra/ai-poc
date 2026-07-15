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
    private final Locator NPI_LIST;
    private final Locator SOURCE_SEARCH;
    private final FrameLocator WORKSPACE_FRAME;
    private final Locator LISTBOX;
    private final Locator WORKSCPACE_FRAME_OVERVIEW;
    WaitUtility waitUtility;


    public ExpansionWorkspace(Page page) {
        this.page = page;
        this.waitUtility = new WaitUtility(page);
        WORKSCPACE_FRAME_OVERVIEW = page.locator("iframe[title='overview']").contentFrame().locator("iframe");
        this.WORKSPACE_FRAME = page.frameLocator("iframe").frameLocator("iframe");
        this.HCP_AUDIENCEEXP = WORKSCPACE_FRAME_OVERVIEW.contentFrame().locator("iframe").contentFrame().getByRole(AriaRole.IMG).nth(2);
        this.ADVERTISER_DROPDOWN = WORKSPACE_FRAME.locator("//input[@placeholder='Select Advertiser']");
        this.SELECT_ADVERTISER = WORKSCPACE_FRAME_OVERVIEW.contentFrame().locator("iframe").contentFrame().getByRole(AriaRole.OPTION, new FrameLocator.GetByRoleOptions().setName("Abbvie"));
        this.SOURCE_AUDIENCE = WORKSPACE_FRAME.locator("button").filter(new Locator.FilterOptions().setHasText("Studio Workspace"));;
        this.NPILIST = page.locator("//div[@class='styles__StyledIcon-sc-d00f7j-2 QkzJU']");
        this.SELECT_SOURCE_AUDIENCE = WORKSCPACE_FRAME_OVERVIEW.contentFrame().locator("iframe").contentFrame().getByText("My playground");
        this.ENTER_MY_PLAYGROUND = WORKSCPACE_FRAME_OVERVIEW.contentFrame().locator("iframe").contentFrame().getByRole(AriaRole.TEXTBOX, new FrameLocator.GetByRoleOptions().setName("Search"));
        this.EXPAND_CARE_TEAM = WORKSCPACE_FRAME_OVERVIEW.contentFrame().locator("iframe").contentFrame().getByText("Expand With Care Team");
        this.EXPAND_AFF_GRAPH = WORKSCPACE_FRAME_OVERVIEW.contentFrame().locator("iframe").contentFrame().getByText("Expand With Affiliation Graph");
        this.ADD_FILTER = WORKSCPACE_FRAME_OVERVIEW.contentFrame().locator("iframe").contentFrame().getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Add Filters"));
        this.NPI_AGE = WORKSCPACE_FRAME_OVERVIEW.contentFrame().locator("iframe").contentFrame().locator("div").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^NPI Age$"))).nth(3);
        this.BELOW_25 = WORKSCPACE_FRAME_OVERVIEW.contentFrame().locator("iframe").contentFrame().getByLabel("Below");
        this.OK_FILTER = WORKSCPACE_FRAME_OVERVIEW.contentFrame().locator("iframe").contentFrame().getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Ok"));
        this.SAVE = WORKSPACE_FRAME.locator("//button[contains(@data-tour-id,'save-workspace-button')]//div[contains(text(),'Save')]");
        this.POPUP_CLOSE = WORKSCPACE_FRAME_OVERVIEW.contentFrame().locator("iframe").contentFrame().locator("div").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Select Filter$"))).getByRole(AriaRole.BUTTON);
        this.WORKSPACE_NAME = WORKSCPACE_FRAME_OVERVIEW.contentFrame().locator("iframe").contentFrame().getByRole(AriaRole.TEXTBOX).nth(3);
        this.DROPDOWN_CARE_TEAM = WORKSCPACE_FRAME_OVERVIEW.contentFrame().locator("iframe").contentFrame().getByRole(AriaRole.COMPLEMENTARY).getByRole(AriaRole.COMBOBOX, new Locator.GetByRoleOptions().setName("undefined combobox")).getByRole(AriaRole.TEXTBOX);
        this.DROPDOWN_CARETEAM_VALUE = WORKSCPACE_FRAME_OVERVIEW.contentFrame().locator("iframe").contentFrame().getByRole(AriaRole.OPTION, new FrameLocator.GetByRoleOptions().setName("Basic"));
        this.NPI_LIST = WORKSPACE_FRAME.locator("button").filter(new Locator.FilterOptions().setHasText("NPI List"));
        this.SOURCE_SEARCH = WORKSPACE_FRAME.getByPlaceholder("Search");
        this.LISTBOX=WORKSPACE_FRAME.locator("ul[role='listbox']");
    }

    public void clickAdvertiserDropdown(String advertiser) {
        ADVERTISER_DROPDOWN.click();
        ADVERTISER_DROPDOWN.fill(advertiser);
        Locator advertiserOption = WORKSPACE_FRAME.locator(String.format("//span[text()='%s']", advertiser));
        waitUtility.waitForLocatorVisible(advertiserOption);
        advertiserOption.click();
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
        SAVE.click();
        page.waitForLoadState();
    }

    public void renameExpansion(String workspaceName) {
        WORKSPACE_NAME.click();
        WORKSPACE_NAME.click();
        WORKSPACE_NAME.fill(workspaceName);
    }

    private void dismissOpenListbox() {
        if (LISTBOX.isVisible()) {
            page.keyboard().press("Escape");
            waitUtility.waitForLocatorHidden(LISTBOX);
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
        WORKSPACE_FRAME.getByText(options, new FrameLocator.GetByTextOptions().setExact(true)).first().click();
    }

}
