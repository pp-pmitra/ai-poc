package pages.life;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import factory.DriverFactory;
import utils.WaitUtility;

public class NPIMedscapeList {
    private final Page page;
    private final Locator LIST_NAME;
    private final Locator FETCH_ADVERTISER;
    private final Locator NEXT_BUTTON;
    private final Locator BROWSE_BUTTON;
    private final Locator COLUMN_LIST_SECTION;
    private final Locator SAVE_BUTTON;
    private final Locator SOFT_MATCH_PROGRESS_TEXT;
    private final Locator WAIT_TEXT;
    private final Locator MEDSCAPE_LIST_CONTAINER;
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());

    public NPIMedscapeList(Page page) {
        this.page = page;
        this.LIST_NAME = page.locator("//input[contains(@placeholder,'List Name')]");
        this.FETCH_ADVERTISER =
                page.locator("//div[contains(text(),'Select Advertiser')]/following-sibling::div//span");
        this.NEXT_BUTTON = page.locator("//button[contains(@class,'mat-flat-button')]//span[text()='Next']");
        this.BROWSE_BUTTON = page.locator("//a[@id='upload_link' and contains(text(),'browse computer')]");
        this.COLUMN_LIST_SECTION = page.locator("//div[contains(@class,'med-col-list-section')]");
        this.SAVE_BUTTON = page.locator("//button[contains(@class,'saveButton')]");
        this.SOFT_MATCH_PROGRESS_TEXT = page.locator("//div[contains(@class,'warning-header')]/span");
        this.WAIT_TEXT = page.locator("//div[contains(@class,'warning-header')]/following-sibling::div");
        this.MEDSCAPE_LIST_CONTAINER = page.locator("//div[contains(@class,'medscape-list-container')]");
    }

    public String fetchDefaultAdvertiser() {
        return FETCH_ADVERTISER.textContent().trim();
    }

    public void enterListName(String listName) {
        waitUtility.waitUntilSpinnerHidden();
        LIST_NAME.fill(listName);
    }

    public void clickNextButton() {
        NEXT_BUTTON.click();
    }

    public boolean isUploadFileSectionDisplayed() {
        return BROWSE_BUTTON.isVisible();
    }

    public void mapRowHeadersToLabels(String labelName, String columnValue) {
        Locator listColumnDropdown = page.locator(
                String.format("//span[text()='%s']/parent::div/following-sibling::div//select", labelName));
        waitUtility.waitForLocatorVisible(COLUMN_LIST_SECTION);
        COLUMN_LIST_SECTION
                .locator(String.format(
                        "//span[text()='%s']/preceding-sibling::span//div[contains(@class,'mat-checkbox-inner-container')]",
                        labelName))
                .click();
        waitUtility.waitForLocatorVisible(listColumnDropdown);
        listColumnDropdown.click();
        listColumnDropdown.selectOption(new SelectOption().setLabel(columnValue));
    }

    public void clickSaveButton() {
        SAVE_BUTTON.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public String getSoftMatchProgressText() {
        return SOFT_MATCH_PROGRESS_TEXT.textContent().trim();
    }

    public String getWaitText() {
        return WAIT_TEXT.textContent().trim();
    }

    public boolean isMedscapeListContainerDisplayed() {
        return MEDSCAPE_LIST_CONTAINER.isVisible();
    }
}
