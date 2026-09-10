package pages.life;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import factory.DriverFactory;
import utils.WaitUtility;

public class TacticCreatives {
    private final Page page;
    private final Locator VERIFY_TACTIC_CREATIVES_PAGE;
    private final Locator SEARCH_CREATIVE;
    private final Locator CLICK_SEARCH;
    private final Locator ASSIGN_CREATIVE_OK_BUTTON;
    private final Locator ENABLE_CREATIVE;
    private final Locator SAVE_TACTIC_CREATIVES;
    private final Locator TACTIC_CREATIVE_SUCCESS;
    private final Locator NAVIGATE_TO_CAMPAIGN_DASHBOARD;
    private final Locator CAMPAIGN_STATUS;
    private final Locator ASSIGN_CREATIVE_TITLE;
    private final Locator CREATIVE_TAB;
    private final Locator ASSIGN_EXISTING_CREATIVE;
    private final Locator SHOW_MORE_BUTTON;
    private final Locator CREATIVE_STATUS;
    private final Locator CLEAR_SEARCH_BOX;
    private final Locator CREATIVES_TABLE;
    private final Locator CAMPAIGN_APPROVAL_STATUS;
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());

    public TacticCreatives(Page page) {
        this.page = page;
        this.VERIFY_TACTIC_CREATIVES_PAGE = page.locator("//div[text()='Creative(s)']");
        this.SEARCH_CREATIVE = page.locator("//input[contains(@class, 'gaTableSearch')]");
        this.CLICK_SEARCH = page.locator("//app-ds-button-wrapper[contains(@class,'gaTableSearchBtn')]");
        this.ASSIGN_CREATIVE_OK_BUTTON =
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ok").setExact(true));
        this.ENABLE_CREATIVE =
                page.locator("//sui-checkbox[@class='toggle ui checkbox ng-untouched ng-pristine ng-valid']");
        this.SAVE_TACTIC_CREATIVES = page.locator("//app-ds-button-wrapper[@label='Save']");
        this.TACTIC_CREATIVE_SUCCESS = page.locator("//div[@aria-label='Success!']");
        this.NAVIGATE_TO_CAMPAIGN_DASHBOARD = page.locator("//div[contains(@class,'campaign-tile')]");
        this.CAMPAIGN_STATUS = page.locator("span.control ds-status");
        this.ASSIGN_CREATIVE_TITLE = page.locator("//div[contains(text(),'Assign Creatives')]");
        this.CREATIVE_TAB = page.locator("//a[contains(text(),'Creatives')]");
        this.ASSIGN_EXISTING_CREATIVE = page.locator("//span[contains(text(),'Assign Existing Creatives')]");
        this.SHOW_MORE_BUTTON = page.locator("//button[contains(text(),'Show More')]");
        this.CREATIVE_STATUS = page.locator("//td[contains(@class,'status-label')]");
        this.CLEAR_SEARCH_BOX = page.locator("//div[contains(@class,'clear-search-close')]");
        this.CREATIVES_TABLE = page.locator("//div[@id='parentTable']");
        this.CAMPAIGN_APPROVAL_STATUS = page.locator("div.approval-status ds-status");
    }

    public String verifyTacticCreativesText() {
        return VERIFY_TACTIC_CREATIVES_PAGE.innerText();
    }

    public void assignCreatives(String creative) {
        Locator selectCreative = page.locator(String.format(
                "//div[contains(text(),'%s')]/ancestor::td/preceding-sibling::td/sui-checkbox", creative));
        ASSIGN_CREATIVE_TITLE.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        SEARCH_CREATIVE.fill(creative);
        CLICK_SEARCH.click();
        waitUtility.waitForLocatorVisible(CREATIVES_TABLE);
        if (!selectCreative.first().isVisible()) {
            CLEAR_SEARCH_BOX.click();
            waitUtility.waitForLocatorVisible(CREATIVES_TABLE);
            for (int i = 0; i < CREATIVE_STATUS.count(); i++) {
                if (CREATIVE_STATUS.nth(i).innerText().equalsIgnoreCase("Approved")) {
                    selectCreative = page.locator(String.format(
                            "//div[contains(@class,'firsttablewrapper')]//tbody//tr[%d]//sui-checkbox", i + 1));
                    break;
                }
            }
        }
        selectCreative.first().scrollIntoViewIfNeeded();
        if (!selectCreative.first().getAttribute("class").contains("checked"))
            selectCreative.first().click();
        ASSIGN_CREATIVE_OK_BUTTON.click();
    }

    public void enableCreative() {
        ENABLE_CREATIVE.click();
    }

    public void saveTacticCreatives() {
        SAVE_TACTIC_CREATIVES.click();
    }

    public String tacticCreativesSuccess() {
        return TACTIC_CREATIVE_SUCCESS.innerText();
    }

    public String getCampaignStatus() {
        waitUtility.waitForLocatorVisible(CAMPAIGN_STATUS);
        return CAMPAIGN_STATUS.innerText();
    }

    public String getCampaignApprovalStatus() {
        waitUtility.waitForLocatorVisible(CAMPAIGN_APPROVAL_STATUS);
        return CAMPAIGN_APPROVAL_STATUS.innerText();
    }

    public void navigateToCampaignDashboard() {
        NAVIGATE_TO_CAMPAIGN_DASHBOARD.click();
    }

    public void clickCreativeTab() {
        CREATIVE_TAB.click();
    }

    public void clickAssignCreatives() {
        ASSIGN_EXISTING_CREATIVE.click();
    }

    public boolean verifyCreativeAssigned(String CreativeName) {
        waitUtility.waitForElementVisible(String.format("//td[@title='%s']", CreativeName));
        return page.locator(String.format("//td[@title='%s']", CreativeName)).isVisible();
    }

    public void selectAndAssignCreativeByStatus(String status) {
        int maxAttempts = 3;
        waitUtility.waitForLocatorVisible(CREATIVES_TABLE);
        for (int i = 0; i < maxAttempts; i++) {
            Locator statusLocator =
                    page.locator(String.format("//div[@class='secondtablewrapper']//tr[td//div[@title='%s']]", status));
            if (statusLocator.first().isVisible()) {
                int rowIndex = (int)
                        statusLocator.first().evaluate("(el) => Array.from(el.parentElement.children).indexOf(el) + 1");
                Locator selectCreative = page.locator(String.format(
                        "//div[contains(@class,'firsttablewrapper')]//tbody//tr[%d]//sui-checkbox", rowIndex));
                selectCreative.click();
                ASSIGN_CREATIVE_OK_BUTTON.click();
                return;
            }
            if (!SHOW_MORE_BUTTON.isVisible() || !SHOW_MORE_BUTTON.isEnabled()) {
                break;
            }
            SHOW_MORE_BUTTON.scrollIntoViewIfNeeded();
            SHOW_MORE_BUTTON.click();
            waitUtility.waitForLocatorVisible(SEARCH_CREATIVE);
        }
    }
}
