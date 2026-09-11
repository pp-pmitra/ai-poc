package pages.life;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import factory.DriverFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import utils.CommonUtils;
import utils.WaitUtility;

public class NPIStaticList {
    private final Page page;
    private final Locator LIST_NAME;
    private final Locator SEARCH_ADVERTISER;
    private final Locator SELECT_ADVERTISER;
    private final Locator NPI_NUMBER;
    private final Locator AVAILABLE_IN;
    private final Locator SAVE_BUTTON;
    private final Locator LIST_SUCCESS;
    private final Locator LIST_NAME_ERROR;
    private final Locator ADVERTISER_NAME_ERROR;
    private final Locator BACK_TO_NPI_LISTS;
    private final Locator DELETE_LIST_ICON;
    private final Locator DELETE_LIST_BUTTON;
    private final Locator DELETE_SUCCESS;
    private final Locator DOWNLOAD_ICON;
    private final Locator NPI_COUNT_FROM_LIST_INFO;
    private final Locator EDIT_NPI_LIST_ICON;
    private final Locator LIST_NAME_FROM_HEADER;
    private final Locator ADVERTISER_NAME_FROM_HEADER;
    private final Locator FETCH_SELECTED_ADVERTISER;
    private final Locator FETCH_SELECTED_AVAILABLE_IN;
    private final Locator TOTAL_NPI;
    private final Locator FETCH_DATA_COST;
    private final Locator DELETE_CONFIRMATION_POPUP;
    private final Locator OK_BUTTON;
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());
    NPISmartList npiSmartList = new NPISmartList(DriverFactory.getPage());
    NPILists npiLists = new NPILists(DriverFactory.getPage());

    public NPIStaticList(Page page) {
        this.page = page;
        this.LIST_NAME = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("List Name"));
        this.SEARCH_ADVERTISER = page.locator("//ng-select[@placeholder='Select Advertiser']//input");
        this.SELECT_ADVERTISER = page.locator("//div[contains(@class,'dropdown-items ng-star-inserted')]");
        this.NPI_NUMBER =
                page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("NPI Numbers (one number per"));
        this.AVAILABLE_IN = page.locator("//div[contains(@class,'npiGroupAvailableSettingContainer')]//span");
        this.SAVE_BUTTON = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));
        this.LIST_SUCCESS = page.locator("//div[contains(@aria-label,'NPI list created')]");
        this.LIST_NAME_ERROR = page.locator("//div[contains(text(),'List Name is required')]");
        this.ADVERTISER_NAME_ERROR = page.locator("//div[contains(text(),'Advertiser is required')]");
        this.BACK_TO_NPI_LISTS =
                page.locator("//img[@alt='BackButton_NPI_Lists'  and contains(@src,'BackButton_NPI_Lists.svg')]");
        this.DELETE_LIST_ICON = page.locator("//app-icon-lable-link[@icon='icons_20-delete.svg']");
        this.DELETE_LIST_BUTTON = page.locator("//span[text()='Delete']");
        this.DELETE_SUCCESS = page.locator("//div[contains(text(),'Deleted Successfully')]");
        this.DOWNLOAD_ICON = page.locator("//span[contains(@class,'image download')]");
        this.NPI_COUNT_FROM_LIST_INFO = page.locator("//div[text()='Total NPI']/preceding-sibling::div[1]");
        this.EDIT_NPI_LIST_ICON = page.locator("//img[@alt='edit'  and contains(@src,'edit-inline.svg')]");
        this.LIST_NAME_FROM_HEADER = page.locator("//div[contains(@class,'header-name')]");
        this.ADVERTISER_NAME_FROM_HEADER = page.locator("//span[@class='header-adv']/span");
        this.FETCH_SELECTED_ADVERTISER = page.locator("//span[@class='ng-value-label']");
        this.FETCH_SELECTED_AVAILABLE_IN =
                page.locator("//div[contains(text(),'Available In ')]//following-sibling::div//mat-checkbox");
        this.TOTAL_NPI = page.locator("//span[contains(text(),'Total NPI')]/preceding-sibling::span");
        this.FETCH_DATA_COST = page.locator("//div[contains(@class,'data-cost')]");
        this.DELETE_CONFIRMATION_POPUP = page.locator("//mat-dialog-container[contains(@class,'mat-dialog-container')]");
        this.OK_BUTTON = page.locator("//button[contains(@class,'confirm-button')]//span[text()='Ok']");
    }

    public void enterListName(String npiListName) {
        LIST_NAME.fill(npiListName);
    }

    public void selectAdvertiser(String advertiser) {
        SEARCH_ADVERTISER.click();
        SELECT_ADVERTISER.locator("text=" + advertiser).click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void enterNPINumber(String npiNumber) {
        NPI_NUMBER.fill(npiNumber);
    }

    public void selectProduct(String platformName) {
        CommonUtils.selectAndClickElement(AVAILABLE_IN, Collections.singletonList(platformName));
    }

    public void saveList() {
        SAVE_BUTTON.click();
    }

    public String fetchSuccessAlert() {
        String alertMessage = LIST_SUCCESS.innerText();
        waitUtility.waitForLocatorHidden(LIST_SUCCESS);
        return alertMessage;
    }

    public String listNameError() {
        return LIST_NAME_ERROR.innerText();
    }

    public String advertiserError() {
        return ADVERTISER_NAME_ERROR.innerText();
    }

    public void uploadStaticListFile(String fileName) {
        Locator fileInput = page.locator("input[type='file']"); // will remove the hardcoding
        CommonUtils.uploadFile(fileInput, fileName);
    }

    public void clickBackToNPILists() {
        waitUtility.waitForLocatorDetached(LIST_SUCCESS);
        BACK_TO_NPI_LISTS.scrollIntoViewIfNeeded();
        BACK_TO_NPI_LISTS.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void editListName(String newListName) {
        waitUtility.waitForLocatorVisible(EDIT_NPI_LIST_ICON);
        EDIT_NPI_LIST_ICON.click();
        LIST_NAME.fill(newListName);
    }

    public void deleteList() {
        waitUtility.waitUntilSpinnerHidden();
        DELETE_LIST_ICON.click();
        waitUtility.waitForLocatorVisible(DELETE_CONFIRMATION_POPUP);
        if (!DELETE_LIST_BUTTON.isVisible()) {
            npiLists.removeNPILinkage();
            waitUtility.waitUntilSpinnerHidden();
            waitUtility.waitForLocatorVisible(DELETE_CONFIRMATION_POPUP);
            OK_BUTTON.click();
            DELETE_LIST_ICON.click();
            waitUtility.waitForLocatorVisible(DELETE_CONFIRMATION_POPUP);
        }
        DELETE_LIST_BUTTON.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public String deleteSuccess() {
        return DELETE_SUCCESS.innerText();
    }

    public Path clickDownloadIcon() throws IOException {
        Download download = page.waitForDownload(DOWNLOAD_ICON.first()::click);
        return CommonUtils.downloadFileAndMoveToSystemFolder(download);
    }

    public String fetchSharedListCountFromUI() {
        String itemCountText = NPI_COUNT_FROM_LIST_INFO.first().innerText().trim();
        return itemCountText.replaceAll("[^0-9]", "");
    }

    public List<String> retrieveEnteredData() {
        List<String> enteredData = new ArrayList<>();
        if (LIST_NAME.isVisible()) enteredData.add(LIST_NAME.inputValue());
        else if (LIST_NAME_FROM_HEADER.isVisible())
            enteredData.add(LIST_NAME_FROM_HEADER.textContent().trim());
        if (!EDIT_NPI_LIST_ICON.isVisible() && FETCH_SELECTED_ADVERTISER.first().isVisible()) {
            for (int i = 0; i < FETCH_SELECTED_ADVERTISER.count(); i++) {
                enteredData.add(FETCH_SELECTED_ADVERTISER.nth(i).textContent());
            }
        } else if (ADVERTISER_NAME_FROM_HEADER.first().isVisible()) {
            String text = ADVERTISER_NAME_FROM_HEADER
                    .first()
                    .textContent()
                    .replace("Advertiser: ", "")
                    .trim();
            if (text.contains(",")) {
                String[] parts = text.split(",");
                for (String part : parts) {
                    enteredData.add(part.trim());
                }
            } else {
                enteredData.add(text);
            }
        }
        if (NPI_NUMBER.isVisible()) enteredData.add(NPI_NUMBER.inputValue().trim());
        npiSmartList.getValuesByClassAttribute(
                FETCH_SELECTED_AVAILABLE_IN,
                "mat-checkbox-checked",
                "xpath=//span[@class='mat-checkbox-label']",
                enteredData);
        return enteredData;
    }

    public int getNPICountFromListDetails() {
        return Integer.parseInt(TOTAL_NPI.textContent().trim());
    }

    public int getNPICountFromListItems(String listName) {
        Locator npiCount = page.locator(String.format(
                "//div[@title='%s']/parent::div/following-sibling::div[contains(@class,'list-item-counter')]",
                listName));
        return Integer.parseInt(npiCount.textContent().trim());
    }

    public int getNPICountFromListInfo() {
        return Integer.parseInt(NPI_COUNT_FROM_LIST_INFO.textContent().trim());
    }

    public String fetchDisplayedDataCost() {
        String fullText = FETCH_DATA_COST.textContent().trim();
        Pattern pattern = Pattern.compile("\\$\\d+(\\.\\d+)?");
        Matcher matcher = pattern.matcher(fullText);
        String price = "";
        if (matcher.find()) {
            price = matcher.group();
        }
        return price;
    }
}
