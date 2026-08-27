package pages.life;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import factory.DriverFactory;
import utils.WaitUtility;

public class SmartPixel {
    private final Page page;
    private final Locator SELECT_ADVERTISER;
    private final Locator INACTIVE_CAMPAIGNS_BUTTON;
    private final Locator ASSOCIATED_CAMPAIGN;
    private final Locator PIXEL_NAME;
    private final Locator PIXEL_HEADER_NAME;
    private final Locator ADD_SMARTLIST_BUTTON;
    private final Locator ASSOCIATED_SMARTLISTS_TAB;
    private final Locator PIXEL_CODES_TAB;
    private final Locator DEACTIVATE_PIXEL_ICON;
    private final Locator DEACTIVATE_PIXEL_BUTTON;
    private final Locator DEACTIVATE_ERROR;
    private final Locator DEACTIVATE_SUCCESS;
    private final Locator UPDATE_SUCCESS;
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());

    public SmartPixel(Page page) {
        this.page = page;
        this.SELECT_ADVERTISER = page.locator("//div[@class='advertiser']/div[contains(@class,'left')]");
        this.INACTIVE_CAMPAIGNS_BUTTON = page.locator("//button[contains(text(),'Show Inactive Campaigns')]");
        this.ASSOCIATED_CAMPAIGN =
                page.locator("//sui-checkbox[contains(@class,'checkbox')]/label/span[contains(@class,'camp-name')]");
        this.PIXEL_NAME = page.locator("//input[@placeholder='Pixel Name']");
        this.ADD_SMARTLIST_BUTTON = page.locator("//app-icon-lable-link[@icon='20-add.svg' and @text='Add Smartlist']");
        this.ASSOCIATED_SMARTLISTS_TAB = page.locator("//span[contains(text(),'ASSOCIATED SMARTLISTS')]");
        this.PIXEL_CODES_TAB = page.locator("//span[contains(text(),'PIXEL CODES')]");
        this.DEACTIVATE_PIXEL_ICON = page.locator("//app-icon-lable-link[@icon='20-clear.svg']");
        this.DEACTIVATE_PIXEL_BUTTON = page.locator("//span[text()='Deactivate']");
        this.DEACTIVATE_ERROR = page.locator("//div[contains(@class,'confirm-modal header')]");
        this.DEACTIVATE_SUCCESS = page.locator("//div[@role='alert' and text()='Pixel Deactivated successfully']");
        this.UPDATE_SUCCESS = page.locator("//div[@role='alert' and text()='Saved successfully']");
        this.PIXEL_HEADER_NAME = page.locator("//div[contains(@class,'header-name')]");
    }

    public void selectAdvertiser(String advertiser) {
        waitUtility.waitUntilSpinnerHidden();
        SELECT_ADVERTISER.locator("text=" + advertiser).click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void selectAssociatedCampaign() {
        waitUtility.waitUntilSpinnerHidden();
        if (ASSOCIATED_CAMPAIGN.first().isVisible()) {
            ASSOCIATED_CAMPAIGN.first().click();
        } else {
            INACTIVE_CAMPAIGNS_BUTTON.click();
            ASSOCIATED_CAMPAIGN.first().click();
        }
    }

    public String getPixelNameFromHeader() {
        waitUtility.waitForLocatorVisible(PIXEL_HEADER_NAME);
        return PIXEL_HEADER_NAME.textContent().trim();
    }

    public void clickAddSmartListButton() {
        ADD_SMARTLIST_BUTTON.click();
        page.waitForTimeout(3000);
        waitUtility.waitUntilSpinnerHidden();
    }

    public void clickAssociatedSmartListsTab() {
        ASSOCIATED_SMARTLISTS_TAB.click();
    }

    public void clickPixelCodesTab() {
        PIXEL_CODES_TAB.click();
    }

    public String getAssociatedSmartList(String listName) {
        String smartListNameXpath = String.format("//div[contains(text(),'%s')]", listName);
        waitUtility.waitForLocatorVisible(page.locator(smartListNameXpath));
        return page.locator(smartListNameXpath).innerText();
    }

    public void enterPixelName(String pixelName) {
        PIXEL_NAME.fill(pixelName);
    }

    public Boolean isPixelCodesTabSelected() {
        String classAttribute = PIXEL_CODES_TAB.getAttribute("class");
        return classAttribute.contains("selectedTab");
    }

    public String getDeactivateError() {
        return DEACTIVATE_ERROR.innerText();
    }

    public void clickDeactivatePixelIcon() {
        DEACTIVATE_PIXEL_ICON.click();
    }

    public void deactivatePixel() {
        waitUtility.waitForLocatorDetached(UPDATE_SUCCESS);
        DEACTIVATE_PIXEL_ICON.click();
        DEACTIVATE_PIXEL_BUTTON.click();
    }

    public String getDeactivateSuccessMessage() {
        String text = DEACTIVATE_SUCCESS.innerText();
        waitUtility.waitUntilSpinnerHidden();
        return text;
    }
}
