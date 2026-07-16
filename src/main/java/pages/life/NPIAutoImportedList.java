package pages.life;

import api.ApiActions;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.options.WaitForSelectorState;
import factory.DriverFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import utils.CommonUtils;
import utils.FileActions;
import utils.WaitUtility;

public class NPIAutoImportedList {

    private final Page page;
    private final Locator SETUP_IMPORT_BUTTON;
    private final Locator ERROR_MESSAGE;
    private final Locator IMPORT_SETTING_TITLE;
    private final Locator DESTINATION_DROPDOWN;
    private final Locator DESTINATION_DROPDOWN_VALUE;
    private final Locator FILE_PATH;
    private final Locator FILE_NAME;
    private final Locator NPI_COLUMN;
    private final Locator CHECK_FILE_BUTTON;
    private final Locator OK_BUTTON;
    private final Locator TOTAL_NPI_COUNT;
    private final Locator RELOAD_NOW_BUTTON;
    private final Locator RELOAD_SUCCESS_ALERT;
    private final Locator FILE_OKAY_TEXT;
    private final Locator IMPORT_SETTING_BUTTON;
    private final Locator NPI_LIST_TEXTAREA;
    private final Locator NPI_ATTRIBUTE_GRIDVIEW;
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());
    ApiActions apiActions = new ApiActions();

    public NPIAutoImportedList(Page page) {
        this.page = page;
        this.SETUP_IMPORT_BUTTON = page.locator("//span[contains(text(),'Setup Import')]");
        this.ERROR_MESSAGE = page.locator("//div[@aria-label='Advertiser is required']");
        this.IMPORT_SETTING_TITLE = page.locator("//div[contains(text(),'Import Settings')]");
        this.DESTINATION_DROPDOWN = page.locator("//ng-select[@placeholder='Select Destination']");
        this.DESTINATION_DROPDOWN_VALUE = page.locator("//ng-dropdown-panel//div//span");
        this.FILE_PATH = page.locator("//input[@placeholder='File Path']");
        this.FILE_NAME = page.locator("//input[@placeholder='File Name']");
        this.NPI_COLUMN = page.locator("//input[@formcontrolname='npiColumn']");
        this.CHECK_FILE_BUTTON = page.locator("//span[contains(text(),'Check File')]");
        this.OK_BUTTON = page.locator("//button[contains(@class,'saveButton')]");
        this.TOTAL_NPI_COUNT = page.locator("//div[text()='Total NPI']//preceding-sibling::div");
        this.RELOAD_NOW_BUTTON = page.locator("//span[contains(text(),'Reload Now')]/parent::div");
        this.RELOAD_SUCCESS_ALERT = page.locator("//div[@aria-label='File is reloaded']");
        this.FILE_OKAY_TEXT = page.locator("//span[contains(text(),'File is okay')]");
        this.IMPORT_SETTING_BUTTON = page.locator("//span[contains(text(),'Import Settings')]");
        this.NPI_LIST_TEXTAREA = page.locator("//textarea[@name='npilist']");
        this.NPI_ATTRIBUTE_GRIDVIEW = page.locator("//div[contains(@class,'npiattrsGridView')]");
    }

    public String verifyIfAutoImportPage() {
        SETUP_IMPORT_BUTTON.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        return SETUP_IMPORT_BUTTON.innerText().trim();
    }

    public String verifyErrorMessage() {
        String errorMessage = ERROR_MESSAGE.innerText().trim();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorHidden(ERROR_MESSAGE);
        return errorMessage;
    }

    public void clickSetupImportButton() {
        SETUP_IMPORT_BUTTON.scrollIntoViewIfNeeded();
        SETUP_IMPORT_BUTTON.click();
    }

    public void waitForImportSettingPanel() {
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(IMPORT_SETTING_TITLE);
    }

    public void enterFileDetails(String fileLocation, String filePath, String fileName) {
        DESTINATION_DROPDOWN.click();
        CommonUtils.selectAndClickElement(DESTINATION_DROPDOWN_VALUE, Collections.singletonList(fileLocation));
        FILE_PATH.fill(filePath);
        FILE_NAME.fill(fileName);
    }

    public void selectListType(String listType) {
        page.locator(String.format("//div[contains(text(),'%s')]/ancestor::mat-radio-button", listType))
                .click();
    }

    public void enterColumnName(String npiColumn, String columnName) {
        Locator locator = page.locator(
                String.format("//div[@class='selection' and contains(text(),'%s')]/parent::button", npiColumn));
        if (!locator.getAttribute("class").contains("active")) locator.click();
        NPI_COLUMN.fill(columnName);
    }

    public void selectImportType(String importType) {
        Locator locator =
                page.locator(String.format("//div[contains(text(),'%s')]/ancestor::mat-radio-button", importType));
        if (!locator.getAttribute("class").contains("mat-radio-checked")) locator.click();
    }

    public void clickCheckFile() {
        CHECK_FILE_BUTTON.click();
        waitUtility.waitForLocatorVisible(FILE_OKAY_TEXT);
    }

    public void clickOKButton() {
        OK_BUTTON.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public APIResponse runAPI(String baseURL, String endpointPath, HashMap<String, String> headers) {
        APIResponse response;
        response = apiActions.postRequestWithoutBody(baseURL, endpointPath, headers);
        return response;
    }

    public String fetchTotalNPICount() {
        waitUtility.waitForLocatorVisible(TOTAL_NPI_COUNT);
        return TOTAL_NPI_COUNT.innerText();
    }

    public String fetchNPIRecordFromTestFile(String fileName) throws IOException {
        int count = FileActions.fetchRowCountExcludeHeaderFromCSVAndTxt(fileName);
        return String.valueOf(count);
    }

    public boolean verifyReloadNowButton() {
        return RELOAD_NOW_BUTTON.isVisible();
    }

    public void clickReloadNowButton() {
        RELOAD_NOW_BUTTON.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public String verifyIfFileIsReloaded() {
        String text = RELOAD_SUCCESS_ALERT.innerText().trim();
        waitUtility.waitForLocatorHidden(RELOAD_SUCCESS_ALERT);
        waitUtility.waitUntilSpinnerHidden();
        return text;
    }

    public void verifyIfImportSettingButtonIsVisible() {
        waitUtility.waitForLocatorVisible(IMPORT_SETTING_BUTTON);
    }

    public String fetchToken(String backgroundUrl) {
        Request matchedRequest = page.waitForRequest(request -> request.url().contains(backgroundUrl), page::reload);
        return matchedRequest.headers().get("token");
    }

    public boolean refreshBrowser() {
        page.reload();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(TOTAL_NPI_COUNT);
        return NPI_ATTRIBUTE_GRIDVIEW.isVisible() || NPI_LIST_TEXTAREA.isVisible();
    }
}
