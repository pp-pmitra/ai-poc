package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import pages.life.CampaignDashboard;
import utils.WaitUtility;

public class Navigation {

    public final Locator USERNAME;
    private final Locator PASSWORD;
    private final Locator LOGIN_BUTTON;
    private final Page page;
    private final Locator LIFE;
    private final Locator SIGNAL;
    private final Locator STUDIO;
    private final Locator SUB_MENU;
    private final Locator RUN_REPORT;
    private final Locator GENERATED_REPORT;
    private final Locator SCHEDULED_REPORT;
    private final Locator REPORT_TEMPLATE;
    private final Locator ACCOUNT_NAME;
    private final Locator ACCOUNT_SEARCH;
    private final Locator ACCOUNT_ITEM;
    private final Locator STUDIO_TITLE;
    private final Locator TARGETING_TEMPLATE_ICON;
    private final Locator CAMPAIGNS;
    private final Locator CREATIVE_LIBRARY_ICON;
    private final Locator MENU_ANGLE;
    private final Locator PULSEPOINT_LOGO;
    private final Locator TARGETING_TEMPLATE_HEADER;
    private final Locator LOGOUT_BUTTON;
    private final Locator CURATED_MARKET;
    private final Locator BUYING_PLATFORM;
    private final Locator STUDIO_PRIMARY_MENU_TEXT;
    WaitUtility waitUtility;
    CampaignDashboard campaignDashboard;

    public Navigation(Page page) {
        this.page = page;
        this.waitUtility = new WaitUtility(page);
        this.campaignDashboard = new CampaignDashboard(page);
        this.USERNAME = page.locator("#UserName");
        this.PASSWORD = page.locator("#Password");
        this.LOGIN_BUTTON = page.locator(".loginLabel");
        this.LIFE = page.locator("//span[contains(@class,'buyerPortalLink')]");
        this.SIGNAL = page.getByText("Signal");
        this.STUDIO = page.locator("//div[contains(@class,'genomeMenuItem')]");
        this.RUN_REPORT = page.getByText("Run a Report");
        this.GENERATED_REPORT = page.locator("#megamenu").getByText("Generated Reports");
        this.SCHEDULED_REPORT = page.locator("#megamenu").getByText("Scheduled Reports");
        this.REPORT_TEMPLATE = page.locator("#megamenu").getByText("Report Templates");
        this.SUB_MENU = page.locator("//img[contains(@alt,'menu')]");
        this.ACCOUNT_NAME = page.locator("//div[@class='accountname']");
        this.ACCOUNT_SEARCH = page.locator("//div[@id='accountSwitcher']/input[@placeholder='Search']");
        this.ACCOUNT_ITEM = page.locator("//div[@id='accountSwitcher']//div[@class='item']");
        this.STUDIO_PRIMARY_MENU_TEXT = page.locator("//div[contains(@class,'primaryMenuText') and text()='Studio']");
        this.STUDIO_TITLE = page.locator("//div[contains(@class,'breadcrumbWrapper') and text()='Studio']");
        this.TARGETING_TEMPLATE_ICON = page.locator("//div[contains(@class,'targetTemplateIcon')]");
        this.CAMPAIGNS =
                page.locator("//div[contains(@class,'pull-left primaryMenuText') and contains(text(),'Campaigns')]");
        this.CREATIVE_LIBRARY_ICON = page.locator("//div[contains(@class,'crtlibIcon')]");
        this.MENU_ANGLE = page.locator(
                "//div[text()='Campaign Reporting']/following-sibling::i[contains(@class,'parentMenuFaAngle')]");
        this.TARGETING_TEMPLATE_HEADER =
                page.locator("//div[contains(text(),'Targeting Templates') and contains(@class,'section-name')]");
        this.PULSEPOINT_LOGO =
                page.locator("//div[contains(@class, 'dynamic-logo')] | //app-buyer-logo/div[@class='logo-holder']");
        this.LOGOUT_BUTTON = page.locator("//div[text()='Sign Out']");
        this.CURATED_MARKET =
                page.locator("//div[contains(@class, 'menuLabel') and contains(text(), 'Curated Markets')]");
        this.BUYING_PLATFORM =
                page.locator("//div[contains(@class,'portalSelectionLabel') and contains(text(),'BUYING PLATFORM')]");
    }

    public void navigateToUrl(String url) {
        this.page.navigate(url);
    }

    public void enterUsername(String userName) {
        USERNAME.fill(userName);
    }

    public void enterPassword(String password) {
        PASSWORD.fill(password);
    }

    public void clickLogin() {
        LOGIN_BUTTON.click();
    }

    public void clickOnIcon(String iconName) {
        this.page.getByText(iconName.trim()).click();
    }

    public void navigateToLife() {
        LIFE.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void navigateToHCP() {
        SIGNAL.click();
    }

    public void navigateToStudio() {
        waitUtility.waitForLocatorVisible(SUB_MENU);
        SUB_MENU.click();
        STUDIO.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        STUDIO.click();
        waitUtility.waitForLocatorVisible(STUDIO_TITLE);
    }

    public boolean isStudioAvailableInMegaMenu() {
        return STUDIO_PRIMARY_MENU_TEXT.isVisible();
    }

    public String verifyStudioTitle() {
        waitUtility.waitForLocatorVisible(STUDIO_TITLE);
        return STUDIO_TITLE.innerText();
    }

    public void selectAccount(String account) {
        if (ACCOUNT_NAME.innerText().contains("buyer2")) {
            ACCOUNT_NAME.click();
            ACCOUNT_SEARCH.fill(account);
            page.waitForLoadState(LoadState.LOAD);
            ACCOUNT_ITEM.click();
        }
        waitUtility.waitUntilSpinnerHidden();
    }

    public void refreshPage() {
        page.reload();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void clickSubMenu() {
        waitUtility.waitForLocatorVisible(SUB_MENU);
        SUB_MENU.click();
    }

    public void clickMenuAngle() {
        if (MENU_ANGLE.isVisible()) {
            if (MENU_ANGLE.getAttribute("class").contains("fa-angle-left")) {
                MENU_ANGLE.click();
            }
        }
    }

    public void clickRunReport() {
        RUN_REPORT.click();
    }

    public void clickGeneratedReport() {
        page.waitForTimeout(5000);
        GENERATED_REPORT.click();
    }

    public void clickScheduledReport() {
        SCHEDULED_REPORT.click();
    }

    public void clickReportTemplate() {
        REPORT_TEMPLATE.click();
    }

    public void clickTargetingTemplate() {
        TARGETING_TEMPLATE_ICON.click();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(TARGETING_TEMPLATE_HEADER);
    }

    public void clickCampaigns() {
        CAMPAIGNS.click();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForElementVisible("//div[@class='page-title' and text()='Campaigns']");
    }

    public void clickCreativeLibrary() {
        waitUtility.waitForLocatorVisible(CREATIVE_LIBRARY_ICON);
        CREATIVE_LIBRARY_ICON.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void clickPulsePointLogo() {
        PULSEPOINT_LOGO.click();
        waitUtility.waitForLocatorVisible(SUB_MENU);
    }

    // The methods below are slight variations of existing ones used to navigate to Life, HCP and Studio from the Admin
    // landing page after login.
    // These are specifically defined to navigate back to Life, HCP and Studio from other modules.
    public void navigateBackToLife() {
        waitUtility.waitForLocatorVisible(SUB_MENU);
        SUB_MENU.click();
        LIFE.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        LIFE.click();
    }

    public void navigateBackToHCP() {
        waitUtility.waitForLocatorVisible(SUB_MENU);
        SUB_MENU.click();
        SIGNAL.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        SIGNAL.click();
    }

    public void navigateBackToStudio() {
        waitUtility.waitForLocatorVisible(SUB_MENU);
        SUB_MENU.click();
        STUDIO.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        STUDIO.click();
    }

    public void logout() {
        waitUtility.waitForLocatorVisible(ACCOUNT_NAME);
        ACCOUNT_NAME.click();
        LOGOUT_BUTTON.click();
        waitUtility.waitForLocatorVisible(USERNAME);
    }

    public void selectAndClickExternalUserApplicationType() {
        page.waitForURL(url -> url.contains("AdminDashBoard.aspx")
                || url.contains("LifePortalSelector.aspx")
                || url.contains("Buyer/#/campaign"));
        if (page.url().contains("LifePortalSelector.aspx")) {
            BUYING_PLATFORM.click();
            waitUtility.waitUntilSpinnerHidden();
        }
    }

    public void navigateToCuratedMarket() {
        CURATED_MARKET.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void selectExternalUserAccount(String account) {
        if (!ACCOUNT_NAME.innerText().contains(account)) {
            ACCOUNT_NAME.click();
            String accountXpath = String.format(
                    "//div[contains(@id, 'accSwitchContainer')]//span[contains(@class, 'account-name') and normalize-space(text())='%s']",
                    account);
            page.locator(accountXpath).click();
            page.waitForLoadState(LoadState.LOAD);
        }
        waitUtility.waitUntilSpinnerHidden();
    }
}
