package pages.life;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import factory.DriverFactory;
import java.util.List;
import utils.WaitUtility;

public class NPILists {
    private final Page page;
    private final Locator NPI_LISTS;
    private final Locator NPI_LISTS_HEADER;
    private final Locator CREATE_NPI_LIST;
    private final Locator STATIC_LIST;
    private final Locator AVAILABLE_IN_CHECKBOX;
    private final Locator AVAILABLE_IN_CONTAINER;
    private final Locator SEARCH_NPI_LISTS;
    private final Locator PARENT_LIST_LABEL;
    private final Locator CREATE_NEW_LIST;
    private final Locator SEARCH_BOX;
    private final Locator SMART_LIST;
    private final Locator AUTO_IMPORTED_LIST;
    private final Locator EDIT_ICON;
    private final Locator MEDSCAPE_LIST;
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());

    public NPILists(Page page) {
        this.page = page;
        this.NPI_LISTS = page.locator("//div[contains(@class, 'menu') and contains(text(),'NPI Lists')]");
        this.NPI_LISTS_HEADER =
                page.locator("//span[contains(@class,'header-title') and contains(text(),'NPI Lists')]");
        this.CREATE_NPI_LIST = page.getByText("Create New NPI List");
        this.STATIC_LIST = page.getByText("Plain static list of NPI");
        this.SEARCH_NPI_LISTS = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Search"));
        this.AVAILABLE_IN_CHECKBOX = page.locator(
                "//input[contains(@class,'mat-checkbox-input') and @aria-checked='true']/ancestor::div/following-sibling::span");
        this.AVAILABLE_IN_CONTAINER = page.locator("//div[contains(@class,'npiGroupAvailableSettingContainer')]");
        this.PARENT_LIST_LABEL = page.locator("//span[contains(@class,'parentListLabel')]");
        this.CREATE_NEW_LIST = page.locator("//span[normalize-space(text())='Create New List']");
        this.SEARCH_BOX = page.locator("//input[@placeholder='Search']");
        this.SMART_LIST = page.getByText("Dynamic list of NPI");
        this.AUTO_IMPORTED_LIST = page.locator("//app-npilisttype[@listtypename='Auto-Imported List']");
        this.EDIT_ICON = page.locator("//img[@alt='edit' and contains(@src,'edit-inline.svg')]");
        this.MEDSCAPE_LIST = page.locator("//app-npilisttype[@listtypename='Medscape List']");
    }

    public void clickNPILists() {
        waitUtility.waitForLocatorVisible(NPI_LISTS);
        NPI_LISTS.click();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(NPI_LISTS_HEADER);
    }

    public void clickNPIListsStg() {
        NPI_LISTS.click();
    }

    public void clickCreateNewList() {
        CREATE_NEW_LIST.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public String verifyNPIListText() {
        waitUtility.waitForLocatorVisible(CREATE_NPI_LIST);
        return CREATE_NPI_LIST.innerText();
    }

    public void clickStaticList() {
        STATIC_LIST.click();
    }

    public void clickSmartList() {
        SMART_LIST.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void searchNPILists(String workspaceName) {
        waitUtility.waitForLocatorVisible(SEARCH_NPI_LISTS);
        SEARCH_NPI_LISTS.fill(workspaceName);
        SEARCH_NPI_LISTS.press("Enter");
    }

    public void selectPublishedList(String listname) {
        page.locator(String.format("//div[contains(text(),'%s')]", listname)).click();
        waitUtility.waitForLocatorVisible(EDIT_ICON);
        EDIT_ICON.click();
        waitUtility.waitForLocatorVisible(PARENT_LIST_LABEL);
        waitUtility.waitUntilSpinnerHidden();
    }

    public boolean availablePlatforms() {
        waitUtility.waitForLocatorVisible(AVAILABLE_IN_CONTAINER);
        AVAILABLE_IN_CONTAINER.scrollIntoViewIfNeeded();
        List<String> allTexts = AVAILABLE_IN_CHECKBOX.allInnerTexts();
        boolean containsLife = allTexts.stream().anyMatch(text -> text.contains("Life"));
        boolean containsHCP = allTexts.stream().anyMatch(text -> text.contains("HCP"));
        return containsLife && containsHCP;
    }

    public boolean checkOnlyLIFEIsSelected() {
        waitUtility.waitForLocatorVisible(AVAILABLE_IN_CONTAINER);
        AVAILABLE_IN_CONTAINER.scrollIntoViewIfNeeded();
        List<String> allTexts = AVAILABLE_IN_CHECKBOX.allInnerTexts();
        return allTexts.size() == 1 && allTexts.stream().anyMatch(text -> text.contains("Life"));
    }

    public void searchList(String listName) {
        waitUtility.waitForLocatorVisible(CREATE_NEW_LIST);
        SEARCH_BOX.first().fill(listName);
        SEARCH_BOX.first().press("Enter");
        waitUtility.waitUntilSpinnerHidden();
    }

    public void openSearchedList(String listName) {
        String listNameXpath = String.format("//div[contains(text(),'%s')]", listName);
        waitUtility.waitForElementVisible(listNameXpath);
        page.locator(listNameXpath).first().click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void clickAutoImportedList() {
        AUTO_IMPORTED_LIST.click();
    }

    public void clickMedscapeList() {
        MEDSCAPE_LIST.click();
        waitUtility.waitUntilSpinnerHidden();
    }
}
