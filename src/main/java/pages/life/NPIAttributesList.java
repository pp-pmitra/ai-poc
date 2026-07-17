package pages.life;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import factory.DriverFactory;
import utils.CommonUtils;
import utils.WaitUtility;

public class NPIAttributesList {
    private final Page page;
    private final Locator FILE_INPUT;
    private final Locator FILE_UPLOAD_SUCCESS;
    private final Locator LIST_NAME;
    private final Locator SEARCH_ADVERTISER;
    private final Locator SELECT_ADVERTISER;
    private final Locator NPI_COLUMN_DROPDOWN;
    private final Locator NEXT_BUTTON;
    private final Locator LIST_NAME_ERROR;
    private final Locator ADVERTISER_NAME_ERROR;
    private final Locator AVAILABLE_IN_LIFE;
    private final Locator AVAILABLE_IN_HCP365;
    private final Locator LIST_SUCCESS;
    private final Locator BACK_TO_NPI_LISTS;
    private final Locator SAVE_BUTTON;
    private final Locator LIST_UPDATE_SUCCESS;
    private final Locator DELETE_LIST_ICON;
    private final Locator DELETE_LIST_BUTTON;
    private final Locator DELETE_SUCCESS;
    private final Locator TOTAL_NPI_LIST_COUNT;
    private final Locator MATCH_NPI_COUNT;
    private final Locator EDIT_NPI_LIST_ICON;
    private final Locator NPI_RECORDS;
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());

    public NPIAttributesList(Page page) {
        this.page = page;
        this.FILE_INPUT = page.locator("input[type='file']");
        this.FILE_UPLOAD_SUCCESS = page.locator("//div[contains(@aria-label,'Successfully uploaded')]");
        this.LIST_NAME = page.locator("//input[contains(@placeholder,'List Name')]");
        this.SEARCH_ADVERTISER = page.locator("//div[contains(text(),'Select Advertiser')]");
        this.SELECT_ADVERTISER = page.locator("//div[contains(@class,'dropdown-items ng-star-inserted')]");
        this.NPI_COLUMN_DROPDOWN =
                page.locator("//ng-select[contains(@class,'hcpDropdown') and contains(@placeholder,'NPI Column')]");
        this.NEXT_BUTTON = page.locator("//button[contains(@class,'saveButton')]");
        this.LIST_NAME_ERROR = page.locator("//div[contains(@aria-label,'List Name is required')]");
        this.ADVERTISER_NAME_ERROR = page.locator("//div[contains(@aria-label,'Advertiser is required')]");
        this.AVAILABLE_IN_LIFE =
                page.locator("(//span[@class='mat-checkbox-label' and normalize-space(text())='Life'])[1]");
        this.AVAILABLE_IN_HCP365 =
                page.locator("(//span[@class='mat-checkbox-label' and normalize-space(text())='HCP365'])[1]");
        this.LIST_SUCCESS = page.locator("//div[contains(@aria-label,'NPI list created')]");
        this.BACK_TO_NPI_LISTS =
                page.locator("//img[@alt='BackButton_NPI_Lists' and contains(@src,'BackButton_NPI_Lists.svg')]");
        this.SAVE_BUTTON = page.locator("//span[text()='Save']");
        this.LIST_UPDATE_SUCCESS = page.locator(
                "//div[contains(@aria-label,'NPI list updated')] | //div[contains(@aria-label,'Medscape List updated')]");
        this.DELETE_LIST_ICON = page.locator("//app-icon-lable-link[@icon='icons_20-delete.svg']");
        this.DELETE_LIST_BUTTON = page.locator("//span[text()='Delete']");
        this.DELETE_SUCCESS = page.locator("//div[contains(text(),'Deleted Successfully')]");
        this.TOTAL_NPI_LIST_COUNT = page.locator("//div[@class='label' and text()='Total NPI']/preceding-sibling::div");
        this.MATCH_NPI_COUNT = page.locator("//div[@class='label' and text()='Matched NPI']/preceding-sibling::div");
        this.EDIT_NPI_LIST_ICON = page.locator("//img[@alt='edit'  and contains(@src,'edit-inline.svg')]");
        this.NPI_RECORDS = page.locator("//div[contains(@class,'total-record')]");
    }

    public void uploadAttributesFile(String attributesFile) {
        CommonUtils.uploadFile(FILE_INPUT, attributesFile);
        waitUtility.waitUntilSpinnerHidden();
    }

    public String verifyFileUploadSuccess() {
        return FILE_UPLOAD_SUCCESS.innerText();
    }

    public void enterListName(String listName) {
        LIST_NAME.scrollIntoViewIfNeeded();
        LIST_NAME.fill(listName);
    }

    public void selectAdvertiser(String advertiser) {
        SEARCH_ADVERTISER.click();
        SELECT_ADVERTISER.locator("text=" + advertiser).click();
    }

    public void selectNPIColumn(String columnName) {
        waitUtility.waitForLocatorDetached(FILE_UPLOAD_SUCCESS);
        NPI_COLUMN_DROPDOWN.click();
        Locator columnOption =
                page.locator("//div[contains(@class,'ng-option') and normalize-space()='" + columnName + "']");
        waitUtility.waitForLocatorVisible(columnOption);
        columnOption.click();
    }

    public void clickNextButton() {
        NEXT_BUTTON.click();
    }

    public String listNameError() {
        String text = LIST_NAME_ERROR.innerText();
        waitUtility.waitForLocatorHidden(LIST_NAME_ERROR);
        return text;
    }

    public String advertiserError() {
        String text = ADVERTISER_NAME_ERROR.innerText();
        waitUtility.waitForLocatorHidden(ADVERTISER_NAME_ERROR);
        return text;
    }

    public void selectProduct() {
        AVAILABLE_IN_LIFE.check();
        AVAILABLE_IN_HCP365.check();
    }

    public String fetchSuccessAlert() {
        String text = LIST_SUCCESS.innerText();
        waitUtility.waitForLocatorHidden(LIST_SUCCESS);
        return text;
    }

    public void clickBackToNPILists() {
        BACK_TO_NPI_LISTS.scrollIntoViewIfNeeded();
        BACK_TO_NPI_LISTS.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void editListName(String newListName) {
        page.waitForTimeout(5000);
        waitUtility.waitForLocatorVisible(EDIT_NPI_LIST_ICON);
        EDIT_NPI_LIST_ICON.click();
        LIST_NAME.fill(newListName);
    }

    public void saveList() {
        SAVE_BUTTON.click();
    }

    public String updateListSuccess() {
        String text = LIST_UPDATE_SUCCESS.innerText();
        waitUtility.waitForLocatorHidden(LIST_UPDATE_SUCCESS);
        return text;
    }

    public void deleteList() {
        waitUtility.waitForLocatorDetached(LIST_SUCCESS);
        DELETE_LIST_ICON.click();
        DELETE_LIST_BUTTON.click();
    }

    public String deleteSuccess() {
        return DELETE_SUCCESS.innerText();
    }

    public String fetchTotalNPIListCount(String listName) {
        String npiCount = "";
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(TOTAL_NPI_LIST_COUNT);
        waitUtility.waitForLocatorVisible(MATCH_NPI_COUNT);
        if (listName.contains(
                page.locator(String.format("//div[contains(@class,'header-name') and contains(@title,'%s')]", listName))
                        .innerText()))
            npiCount = "Total-" + TOTAL_NPI_LIST_COUNT.innerText().trim() + "&" + "Matched-"
                    + MATCH_NPI_COUNT.innerText().trim();
        return npiCount;
    }

    public int getNPICountFromListDetails() {
        return Integer.parseInt(NPI_RECORDS.textContent().trim().replaceAll("[^0-9]", ""));
    }
}
