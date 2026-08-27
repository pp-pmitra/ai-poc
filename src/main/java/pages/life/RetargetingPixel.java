package pages.life;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import factory.DriverFactory;
import utils.WaitUtility;

public class RetargetingPixel {
    private final Page page;
    private final Locator PIXEL_NAME;
    private final Locator SEARCH_ADVERTISER;
    private final Locator SELECT_ADVERTISER;
    private final Locator PIXEL_NAME_ERROR;
    private final Locator ADVERTISER_NAME_ERROR;
    private final Locator JAVASCRIPT_PIXEL_TYPE;
    private final Locator IMAGE_PIXEL_TYPE;
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());

    public RetargetingPixel(Page page) {
        this.page = page;
        this.PIXEL_NAME = page.locator("//input[@placeholder='Pixel Name']");
        this.SEARCH_ADVERTISER =
                page.locator("//input[contains(@class,'dropdown-search') and @placeholder='Advertisers']");
        this.SELECT_ADVERTISER = page.locator("//div[contains(@class,'item text-truncate')]");
        this.PIXEL_NAME_ERROR = page.locator("//div[contains(text(),'Pixel Name is required')]");
        this.ADVERTISER_NAME_ERROR = page.locator("//div[contains(text(),'Advertiser is required')]");
        this.JAVASCRIPT_PIXEL_TYPE = page.locator("//sui-radio-button[@name='pixelType']//label[text()='JavaScript']");
        this.IMAGE_PIXEL_TYPE = page.locator("//sui-radio-button[@name='pixelType']//label[text()='Image']");
    }

    public void enterPixelName(String pixelName) {
        PIXEL_NAME.fill(pixelName);
    }

    public void selectAdvertiser(String advertiser) {
        SEARCH_ADVERTISER.click();
        SELECT_ADVERTISER.locator("text=" + advertiser).click();
    }

    public void clearPixelName() {
        PIXEL_NAME.clear();
    }

    public String getPixelNameError() {
        String pixelNameError = PIXEL_NAME_ERROR.innerText().trim();
        waitUtility.waitForLocatorDetached(PIXEL_NAME_ERROR);
        return pixelNameError;
    }

    public String getAdvertiserError() {
        String advertiserError = ADVERTISER_NAME_ERROR.innerText().trim();
        waitUtility.waitForLocatorDetached(ADVERTISER_NAME_ERROR);
        return advertiserError;
    }

    public void selectPixelType(String pixelType) {
        if (pixelType.equals("JavaScript")) {
            JAVASCRIPT_PIXEL_TYPE.click();
        } else if (pixelType.equals("Image")) {
            IMAGE_PIXEL_TYPE.click();
        }
    }
}
