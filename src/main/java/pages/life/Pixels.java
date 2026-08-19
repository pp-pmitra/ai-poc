package pages.life;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import factory.DriverFactory;
import java.util.List;
import utils.WaitUtility;

public class Pixels {
    private final Page page;
    private final Locator PIXELS_MENU_ITEM;
    private final Locator ADD_PIXEL_BUTTON;
    private final Locator CREATE_NEW_PIXEL_LABEL;
    private final Locator RETARGETING_PIXEL;
    private final Locator SMART_PIXEL;
    private final Locator CONVERSION_PIXEL;
    private final Locator SEARCH_BOX;
    private final Locator SAVE_BUTTON;
    private final Locator SAVE_SUCCESS;
    private final Locator RETARGETING_TAB;
    private final Locator SMART_TAB;
    private final Locator CONVERSION_TAB;
    private final Locator ADVERTISER_DROPDOWN;
    private final Locator UPDATE_SUCCESS;
    private final Locator REMOVE_PIXEL_ICON;
    private final Locator REMOVE_PIXEL_BUTTON;
    private final Locator REMOVE_SUCCESS;
    private final Locator CANCEL_BUTTON;
    private final Locator NO_RESULTS_FOUND;
    private final Locator PIXEL_LIST;
    private final Locator EDIT_ICON;
    private final Locator CLEAR_SEARCH_ICON;
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());

    public Pixels(Page page) {
        this.page = page;
        this.PIXELS_MENU_ITEM = page.locator(
                "//div[text()='Pixels' and (contains(@class,'pull-left menuLabel') or contains(@class,'pull-left menuText'))]");
        this.ADD_PIXEL_BUTTON = page.locator("//span[normalize-space(text())='Add Pixel']");
        this.CREATE_NEW_PIXEL_LABEL = page.locator("//div[@class='createNewPixel']/div[@class='heading-wrapper']");
        this.RETARGETING_PIXEL = page.locator("//span[text()='Retargeting Pixel']");
        this.SMART_PIXEL = page.locator("//span[text()='Smart Pixel']");
        this.CONVERSION_PIXEL = page.locator("//span[text()='Conversion Pixel']");
        this.SEARCH_BOX = page.locator("//input[@placeholder='Search' and contains(@class,'search icon')]");
        this.SAVE_BUTTON = page.locator("//button[text()='Save']");
        this.SAVE_SUCCESS = page.locator("//div[contains(@aria-label,'Success!')]");
        this.RETARGETING_TAB = page.locator("//button[text()='Retargeting']");
        this.SMART_TAB = page.locator("//button[text()='Smart']");
        this.CONVERSION_TAB = page.locator("//button[text()='Conversion']");
        this.ADVERTISER_DROPDOWN = page.locator("//app-multi-select[@placeholder='Any Advertiser']");
        this.UPDATE_SUCCESS = page.locator(
                "//div[@role='alert' and (text()='Pixel updated successfully' or text()='Saved successfully')]");
        this.REMOVE_PIXEL_ICON = page.locator("//app-icon-lable-link[@icon='20-delete.svg']");
        this.REMOVE_PIXEL_BUTTON = page.locator("//span[text()='Remove']");
        this.REMOVE_SUCCESS = page.locator("//div[@role='alert' and text()='Pixel deleted successfully']");
        this.CANCEL_BUTTON = page.locator(
                "//button[contains(@class,'cancel secondary button') and normalize-space(text())='Cancel']");
        this.NO_RESULTS_FOUND =
                page.locator("//div[contains(text(),'Nothing found') or contains(text(),'Nothing Found')]");
        this.PIXEL_LIST =
                page.locator("//div[contains(@class,'pixel-list-wrapper')]//div[contains(@class,'main-details')]");
        this.EDIT_ICON = page.locator("//img[@alt='edit' and contains(@class,'header-edit-icon')]");
        this.CLEAR_SEARCH_ICON = page.locator("//div[@id='searchPixel']/div[contains(@class,'clear-search-close')]");
    }

    public void clickPixelsMenuItem() {
        PIXELS_MENU_ITEM.click();
    }

    public void clickAddPixelButton() {
        ADD_PIXEL_BUTTON.click();
    }

    public String verifyCreateNewPixelLabel() {
        return CREATE_NEW_PIXEL_LABEL.innerText();
    }

    public String verifyRetargetingPixel() {
        return RETARGETING_PIXEL.innerText();
    }

    public String verifySmartPixel() {
        return SMART_PIXEL.innerText();
    }

    public String verifyConversionPixel() {
        return CONVERSION_PIXEL.innerText();
    }

    public void selectPixelType(String pixelType) {
        switch (pixelType) {
            case "Retargeting Pixel":
                RETARGETING_PIXEL.click();
                break;
            case "Smart Pixel":
                SMART_PIXEL.click();
                waitUtility.waitUntilSpinnerHidden();
                break;
            case "Conversion Pixel":
                CONVERSION_PIXEL.click();
                break;
        }
    }

    public void selectPixelTab(String pixelTab) {
        if (pixelTab.startsWith("Retargeting")) {
            RETARGETING_TAB.click();
        } else if (pixelTab.startsWith("Smart")) {
            SMART_TAB.click();
            waitUtility.waitUntilSpinnerHidden();
        } else if (pixelTab.startsWith("Conversion")) {
            CONVERSION_TAB.click();
        }
    }

    public void savePixel() {
        waitUtility.waitForLocatorVisible(SAVE_BUTTON);
        SAVE_BUTTON.click();
    }

    public String verifySaveSuccess() {
        String successMessage = SAVE_SUCCESS.innerText();
        waitUtility.waitForLocatorHidden(SAVE_SUCCESS);
        waitUtility.waitUntilSpinnerHidden();
        return successMessage;
    }

    public void searchSavedPixel(String pixelName) {
        waitUtility.waitUntilSpinnerHidden();

        if (CLEAR_SEARCH_ICON.isVisible()) {
            CLEAR_SEARCH_ICON.click();
        }

        SEARCH_BOX.fill(pixelName);
        SEARCH_BOX.press("Enter");
    }

    public String verifyCreatedPixel(String pixelName) {
        String createdPixelXpath = String.format("//div[contains(@title,'%s')]", pixelName);
        waitUtility.waitForLocatorVisible(page.locator(createdPixelXpath));
        return page.locator(createdPixelXpath).innerText();
    }

    public String verifyRetargetingTab() {
        return RETARGETING_TAB.innerText();
    }

    public String verifySmartTab() {
        return SMART_TAB.innerText();
    }

    public String verifyConversionTab() {
        return CONVERSION_TAB.innerText();
    }

    public Boolean verifyAdvertiserDropdown() {
        waitUtility.waitForLocatorVisible(ADVERTISER_DROPDOWN);
        return ADVERTISER_DROPDOWN.isVisible();
    }

    public Boolean verifySearchBox() {
        waitUtility.waitForLocatorVisible(SEARCH_BOX);
        return SEARCH_BOX.isVisible();
    }

    public String verifyUpdateSuccess() {
        String updateSuccessMessage = UPDATE_SUCCESS.innerText();
        waitUtility.waitForLocatorDetached(UPDATE_SUCCESS);
        return updateSuccessMessage;
    }

    public void removePixel() {
        if (UPDATE_SUCCESS.isVisible()) {
            waitUtility.waitForLocatorDetached(UPDATE_SUCCESS);
        }

        waitUtility.waitForLocatorVisible(REMOVE_PIXEL_ICON);
        REMOVE_PIXEL_ICON.click();
        try {
            waitUtility.waitForLocatorVisible(REMOVE_PIXEL_BUTTON, 5000);
        } catch (PlaywrightException e) {
            // Sometimes the first click doesn't register, so click again.
            REMOVE_PIXEL_ICON.click();
            waitUtility.waitForLocatorVisible(REMOVE_PIXEL_BUTTON);
        }
        REMOVE_PIXEL_BUTTON.click();
    }

    public String removeSuccess() {
        return REMOVE_SUCCESS.innerText().trim();
    }

    public void clickCancelButton() {
        CANCEL_BUTTON.click();
    }

    public void selectSmartPixelTab() {
        SMART_TAB.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void openSearchedPixel(String pixelName) {
        String pixelNameXpath = String.format("//div[contains(text(),'%s')]", pixelName);
        waitUtility.waitForLocatorVisible(page.locator(pixelNameXpath));
        page.locator(pixelNameXpath).click();
    }

    public String verifyDeletedPixel() {
        waitUtility.waitForLocatorVisible(NO_RESULTS_FOUND);
        return NO_RESULTS_FOUND.innerText();
    }

    public void selectAdvertiser(String advertiser) {
        ADVERTISER_DROPDOWN.click();
        ADVERTISER_DROPDOWN
                .locator("xpath=//span[contains(text(),'" + advertiser + "')]")
                .click();
        page.keyboard().press("Escape");
        waitUtility.waitForLocatorVisible(PIXEL_LIST.last());
    }

    public List<String> fetchPixelsList() {
        return PIXEL_LIST.allInnerTexts();
    }

    public void clickEditIcon() {
        EDIT_ICON.click();
    }
}
