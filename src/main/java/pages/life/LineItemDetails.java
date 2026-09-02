package pages.life;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import factory.DriverFactory;
import java.time.LocalDateTime;
import java.util.*;
import utils.WaitUtility;

public class LineItemDetails {
    private final Page page;
    private final Locator LINE_ITEM_PAGE;
    private final Locator LINE_ITEM_NAME;
    private final Locator LINE_ITEM_BUDGET;
    private final Locator ENABLE_LINE_ITEM;
    private final Locator SAVE_LINE_ITEM;
    private final Locator LINE_ITEM_SUCCESS;
    private final Locator LINE_ITEM_TYPE_DROPDOWN;
    private final Locator LINE_ITEM_TYPE_VALUE;
    private final Locator ADD_FLIGHT_BUTTON;
    private final Locator TAB_NAMES;
    private final Locator TACTIC_ITEM_DETAILS;
    private final Locator LINE_ITEM_STATUS;
    private final Locator TOOL_TIP;
    private final Locator ERROR_ALERT;
    private final Locator UNACCOUNTED_BUDGET;
    private final Locator FLIGHT_CONTAINER;
    private final Locator FLIGHT_START_DATE;
    private final Locator FLIGHT_END_DATE;
    private final Locator CALENDER_TITLE;
    private final Locator CALENDER_PREV_BUTTON;
    private final Locator CALENDER_NEXT_BUTTON;
    private final Locator CALENDER_CURRENT_YEAR;
    private final Locator CALENDER_CURRENT_MONTH;
    private final Locator CALENDER_DATE;
    private final Locator FLIGHT_OVERLAP_INLINE_ERROR;
    private final Locator MAX_DAILY_SPEND;
    private final Locator DELETE_FLIGHT;
    private final Locator GENERATE_SEQUENTIAL_FLIGHT;
    private final Locator GENERATE_FLIGHTS;
    private final Locator SEQUENTIAL_START_MONTH;
    private final Locator NUMBER_OF_MONTHS;
    private final Locator CANCEL_TACTIC;
    private final Locator NEW_LINE_ITEM;
    private final Locator LINE_ITEM_PANEL_NAME;
    private final Locator NOTES_ICON;
    private final Locator NOTES_TEXTAREA;
    private final Locator NOTES_OK_BUTTON;
    private final Locator NOTES_SUCCESS_ALERT;
    private final Locator LINE_ITEMS_OPTIONS;
    private final Locator BULK_EDIT_MODE;
    private final Locator BULK_EDIT_CHECKBOX;
    private final Locator BULK_DISABLE_LINE_ITEM;
    private final Locator BULK_ENABLE_LINE_ITEM;
    private final Locator BULK_EDIT_SUCCESS_ALERT;
    private final Locator COPY_LINE_ITEM_DIALOG;
    private final Locator COPY_INPUT;
    private final Locator COPY_BUTTON;
    private final Locator COPY_SUCCESS_ALERT;
    private final Locator TEMPLATE_NAME_LABEL;
    private final Locator REMOVAL_CONFIRMATION_DIALOG;
    private final Locator REMOVE_BUTTON;
    private final Locator MAIN_DETAILS_LABEL;
    private final Locator CAMPAIGN_HEADER;
    private final Locator EXIT_BULK_EDIT_MODE;
    private final Locator COST_MODEL;
    private final Locator BUDGET_DISTRIBUTION;
    private final Locator PACING_MODE;
    private final Locator FLAT_CPM;
    private final Locator PACING_MODE_INPUT;
    private final Locator PLACEMENT_ID;
    private final Locator MANAGEMENT_FEE_LABEL_VALUE;
    private final Locator MANAGEMENT_FEE_OVERRIDE;
    private final Locator IMPRESSION_ERROR_MESSAGE;
    private final Locator ADD_CUSTOM_FIELD;
    private final Locator ADD_CUSTOM_FIELD_INPUT;
    private final Locator SAVE_CUSTOM_FIELD_BUTTON;
    private final Locator FIELD_CREATE_SUCCESS;
    private final Locator FETCH_LINE_ITEM_NAME;
    private final Locator LINE_ITEM_DETAILS_TAB;
    private final Locator LINE_ITEM_OVERVIEW_TAB;
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());
    Calendar calendar = Calendar.getInstance();
    LocalDateTime currentDateTime = LocalDateTime.now();
    int currentYear = currentDateTime.getYear();
    int startDay = currentDateTime.getDayOfMonth();
    int endDay;
    int daysInCurrentMonth = calendar.getActualMaximum(Calendar.DATE);

    public LineItemDetails(Page page) {

        if (startDay == daysInCurrentMonth) {
            endDay = 5;
        } else {
            endDay = startDay + 5;
        }
        this.page = page;
        this.LINE_ITEM_PAGE = page.locator("//div[text()='New Line Item' and @class='left lineitem-name']");
        this.LINE_ITEM_NAME = page.locator("//input[@placeholder='Line Item Name']");
        this.LINE_ITEM_BUDGET = page.locator("//input[contains(@class,'gaFlightBudget')]");
        this.ENABLE_LINE_ITEM =
                page.locator("//sui-checkbox[@class='toggle ui checkbox ng-untouched ng-pristine ng-valid']");
        this.SAVE_LINE_ITEM = page.locator("//app-ds-button-wrapper[@label='Save']");
        this.LINE_ITEM_SUCCESS = page.locator(
                "//div[@aria-label='Success!']/following-sibling::div[@role='alert' and contains(text(),'Lineitem')]");
        this.LINE_ITEM_TYPE_DROPDOWN = page.locator("//div[contains(@class,'lineItemType')]");
        this.LINE_ITEM_TYPE_VALUE = page.locator("//div[contains(@class,'gaCostType')]/div");
        this.ADD_FLIGHT_BUTTON = page.locator("//app-icon-lable-link[contains(@text,'Add Flight')]");
        this.TAB_NAMES = page.locator("//div[@id='lineItemPageWrapper']//a[contains(@class, 'nav-item')]");
        this.TACTIC_ITEM_DETAILS = page.locator("//div[contains(@class,'tactic item-details pointer')]");
        this.LINE_ITEM_STATUS = page.locator("//span[contains(@class,'status-label')]/span");
        this.TOOL_TIP = page.locator("//div[contains(@class,'ng-tooltip-show')]");
        this.ERROR_ALERT = page.locator(
                "//div[contains(@aria-label, 'The total flight budget could not exceed') or contains(@aria-label, 'LineItem Flight is required.') or contains(@aria-label, 'LineItem flights overlap.') or contains(@aria-label, 'Invalid budget')]");
        this.UNACCOUNTED_BUDGET = page.locator("//span[contains(text(), 'Use Unaccounted Budget')]");
        this.FLIGHT_CONTAINER = page.locator("//div[contains(@class,'flight-container')]");
        this.FLIGHT_START_DATE = page.locator("//input[contains(@class,'gaFlightStartDate')]");
        this.FLIGHT_END_DATE = page.locator("//input[contains(@class,'gaFlightEndDate')]");
        this.CALENDER_TITLE = page.locator("//sui-calendar-view-title/span[@class='title link']");
        this.CALENDER_PREV_BUTTON = page.locator("//sui-calendar-view-title/span[@class='prev link']");
        this.CALENDER_NEXT_BUTTON = page.locator("//sui-calendar-view-title/span[@class='next link']");
        this.CALENDER_CURRENT_YEAR = page.locator("//sui-calendar-year-view//tbody//td[contains(@class,'today')]");
        this.CALENDER_CURRENT_MONTH = page.locator("//sui-calendar-month-view//tbody//td[contains(@class,'today')]");
        this.CALENDER_DATE = page.locator(
                "//sui-calendar-date-view//tbody//td[contains(@class,'today') or contains(@class,'link')]");
        this.FLIGHT_OVERLAP_INLINE_ERROR = page.locator("//p[text()='Flight overlap with other flights.']");
        this.MAX_DAILY_SPEND = page.locator("//label[contains(@class, 'daily-spent-label')]");
        this.DELETE_FLIGHT = page.locator("//div[@title='delete']/img");
        this.GENERATE_SEQUENTIAL_FLIGHT = page.locator("//app-icon-lable-link[@text='Generate Sequential Flights']");
        this.GENERATE_FLIGHTS = page.locator("//span[text()='Generate Flights']");
        this.SEQUENTIAL_START_MONTH = page.locator(
                "//label[text()='Start Month']/following-sibling::div//input[contains(@placeholder,'Start Date')]");
        this.NUMBER_OF_MONTHS = page.locator("//label[text()='Number of Months']/following-sibling::div//sui-select");
        this.LINE_ITEM_PANEL_NAME = page.locator("//div[@class='item-detials']/div[@class='main-details']");
        this.CANCEL_TACTIC = page.locator("#lidcBody").getByText("Cancel");
        this.NEW_LINE_ITEM = page.locator("//app-icon-lable-link[@text='New Line Item']//div");
        this.NOTES_ICON = page.locator("//div[contains(@class,'notes-dashboard')]//span");
        this.NOTES_TEXTAREA = page.locator("//textarea[@placeholder='Notes']");
        this.NOTES_OK_BUTTON = page.locator("//span[@class='okText']");
        this.NOTES_SUCCESS_ALERT = page.locator("//div[text()='Notes saved successfully.']");
        this.LINE_ITEMS_OPTIONS =
                page.locator("//div[contains(@class, 'lineItem-app-action')]//span[@title='options']");
        this.BULK_EDIT_MODE = page.locator("//span[contains(@tooltip, 'Bulk Edit Mode')]");
        this.BULK_EDIT_CHECKBOX = page.locator("//div[contains(@class, 'bulkEditModeWrapper')]//sui-checkbox");
        this.BULK_DISABLE_LINE_ITEM = page.locator("//div[contains(text(), 'Disable Line Items')]");
        this.BULK_ENABLE_LINE_ITEM = page.locator("//div[contains(text(), 'Enable Line Items')]");
        this.BULK_EDIT_SUCCESS_ALERT = page.locator("//div[text()='Lineitems status updated successfully']");
        this.COPY_LINE_ITEM_DIALOG = page.locator("//div[contains(text(),'Copy Line Item')]");
        this.COPY_INPUT = page.locator("//input[contains(@class, 'copyInput')]");
        this.COPY_BUTTON = page.locator("//button[contains(text(), 'Copy')]");
        this.COPY_SUCCESS_ALERT = page.locator("//div[contains(text(), 'Line Item copied successfully.')]");
        this.TEMPLATE_NAME_LABEL = page.locator("//label[text()='Template']");
        this.REMOVAL_CONFIRMATION_DIALOG = page.locator("//div[contains(text(),'Removal Confirmation')]");
        this.REMOVE_BUTTON = page.locator("//span[contains(text(),'Remove')]");
        this.MAIN_DETAILS_LABEL = page.locator("//div[@class='main-details']");
        this.CAMPAIGN_HEADER = page.locator("//div[@class='campaign-header']");
        this.EXIT_BULK_EDIT_MODE = page.locator("//button[contains(text(),'Exit Bulk edit mode')]");
        this.COST_MODEL = page.locator("//div[@id='billingTypeDropdown']/parent::div");
        this.BUDGET_DISTRIBUTION = page.locator("//label[contains(text(),'Budget Distribution')]/parent::div");
        this.PACING_MODE = page.locator("//sui-select[@placeholder='PacingMode']");
        this.FLAT_CPM = page.locator("//input[@formcontrolname='flatCPM']");
        this.PACING_MODE_INPUT = page.locator("//input[contains(@class,'pacing-mode-input')]");
        this.PLACEMENT_ID = page.locator("//label[span[normalize-space()='PlacementId']]/following-sibling::input");
        this.MANAGEMENT_FEE_LABEL_VALUE = page.locator("//span[contains(@class, 'fee-value')]");
        this.MANAGEMENT_FEE_OVERRIDE = page.locator(
                "//div[contains(@class,'management-fee')]//span/following-sibling::span//label[contains(text(),'Override')]");
        this.IMPRESSION_ERROR_MESSAGE = page.locator("//p[contains(text(),'Invalid Impression Cap')]");
        this.ADD_CUSTOM_FIELD = page.locator("//span[contains(text(),'Add Custom Field')]");
        this.ADD_CUSTOM_FIELD_INPUT = page.locator("//input[@placeholder='Field Name']");
        this.SAVE_CUSTOM_FIELD_BUTTON = page.locator("//button[normalize-space()='Save']");
        this.FIELD_CREATE_SUCCESS =
                page.locator("//div[@role='alert' and contains(text(),'Successfully created custom Field')]");
        this.FETCH_LINE_ITEM_NAME = page.locator("//div[contains(@class,'item-detials')]/div[@class='main-details']");
        this.LINE_ITEM_DETAILS_TAB = page.locator("//a[contains(@class,'gaTabDetails') and contains(text(),'Details')]");
        this.LINE_ITEM_OVERVIEW_TAB = page.locator("//a[contains(@class,'gaTabOverview') and contains(text(),'Overview')]");
    }

    public String verifyLineItemText() {
        return LINE_ITEM_PAGE.innerText();
    }

    public void enterLineItemName(String lineItemName) {
        LINE_ITEM_NAME.fill(lineItemName);
    }

    public void enterLineItemBudget(String lineBudget) {
        int count = LINE_ITEM_BUDGET.count();
        LINE_ITEM_BUDGET.nth(count - 1).fill(lineBudget);
    }

    public void enableLineItem() {
        ENABLE_LINE_ITEM.click();
    }

    public void saveLineItem() {
        SAVE_LINE_ITEM.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public String lineItemSuccess() {
        String successMessage = LINE_ITEM_SUCCESS.innerText().trim();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorHidden(LINE_ITEM_SUCCESS);
        return successMessage;
    }

    public void selectLineItemType(String lineItemType) {
        LINE_ITEM_TYPE_DROPDOWN.click();
        int count = LINE_ITEM_TYPE_VALUE.count();
        for (int i = 0; i < count; i++) {
            if (LINE_ITEM_TYPE_VALUE.nth(i).innerText().equals(lineItemType)) {
                LINE_ITEM_TYPE_VALUE.nth(i).scrollIntoViewIfNeeded();
                LINE_ITEM_TYPE_VALUE.nth(i).click();
                break;
            }
        }
    }

    public void cancelTactic() {
        CANCEL_TACTIC.click();
    }

    public void selectNewLineItem() {
        waitUtility.waitForLocatorVisible(NEW_LINE_ITEM);
        NEW_LINE_ITEM.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public String verifyLineItemPanelName() {
        return LINE_ITEM_PANEL_NAME.innerText();
    }

    public void clickAddFlightButton() {
        ADD_FLIGHT_BUTTON.click();
    }

    public boolean verifyLineItemTabs(List<String> tabNames) {
        Set<String> actualTabs = new HashSet<>();
        for (int i = 0; i < TAB_NAMES.count(); i++) {
            actualTabs.add(TAB_NAMES.nth(i).innerText().trim().toLowerCase());
        }
        for (String tab : tabNames) {
            if (!actualTabs.contains(tab.trim().toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    public String fetchDisplayedManagementFeeValue() {
        return MANAGEMENT_FEE_LABEL_VALUE.textContent().trim();
    }

    public void enableManagementFeeOverride() {
        Locator checkboxInput = page.locator(
                "//div[contains(@class,'management-fee')]//span/following-sibling::span//label[contains(text(),'Override')]/preceding-sibling::input");

        if (!checkboxInput.isChecked()) {
            MANAGEMENT_FEE_OVERRIDE.click();
        }
    }

    public String verifyLineItemStatus() {
        String status = " ";
        if (TACTIC_ITEM_DETAILS.count() == 0) {
            status = LINE_ITEM_STATUS.innerText();
        }
        return status;
    }

    public String fetchIncompleteStatusToolTip() {
        LINE_ITEM_STATUS.hover();
        return TOOL_TIP.innerText();
    }

    public String fetchErrorAlert() {
        String text = ERROR_ALERT.innerText().trim();
        waitUtility.waitForLocatorHidden(ERROR_ALERT);
        return text;
    }

    public String fetchCampaignBudget() {
        String fullText = UNACCOUNTED_BUDGET.textContent();
        String number = fullText.replaceAll("[^0-9]", "");
        number = number.substring(0, number.length() - 2);
        return number;
    }

    public int selectStartDateOfFlight() {
        selectFlightDates(FLIGHT_START_DATE.first(), startDay);
        waitUtility.waitForLocatorHidden(CALENDER_DATE.first());
        return startDay;
    }

    public int selectEndDateOfFlight() {
        selectFlightDates(FLIGHT_END_DATE.first(), endDay);
        waitUtility.waitForLocatorHidden(CALENDER_DATE.first());
        return endDay;
    }

    public void selectFlightDates(Locator locator, int day) {
        locator.click();
        while (CALENDER_TITLE.first().isVisible()
                && !CALENDER_TITLE.first().innerText().trim().equals(String.valueOf(currentYear))) {
            CALENDER_TITLE.first().click();
            Locator yearLocator =
                    page.locator(String.format("//sui-calendar-year-view//td[contains(text(),'%s')]", currentYear));
            if (yearLocator.isVisible() && yearLocator.getAttribute("class").contains("today")) {
                CALENDER_TITLE.first().click();
                CALENDER_PREV_BUTTON.click();
                if (CALENDER_TITLE.first().innerText().trim().equals(String.valueOf(currentYear)))
                    CALENDER_TITLE.first().click();
            }
        }
        if (CALENDER_CURRENT_MONTH.isVisible()) CALENDER_CURRENT_MONTH.click();
        if (!CALENDER_DATE.locator("text = " + day).first().isVisible()) {
            CALENDER_NEXT_BUTTON.click();
        }
        CALENDER_DATE.locator("text = " + day).first().click();
    }

    public void selectOverlappingFlightDates(int flightStartDate, int flightEndDate) {
        startDay = flightStartDate + 1;
        endDay = flightEndDate - 1;
        if (FLIGHT_CONTAINER.count() > 1) {
            selectFlightDates(FLIGHT_START_DATE.nth(1), startDay);
            selectFlightDates(FLIGHT_END_DATE.nth(1), endDay);
            waitUtility.waitForLocatorHidden(CALENDER_DATE.first());
        }
    }

    public String fetchInlineErrorMessage() {
        waitUtility.waitForLocatorVisible(FLIGHT_OVERLAP_INLINE_ERROR);
        return FLIGHT_OVERLAP_INLINE_ERROR.innerText().trim();
    }

    public void addMultipleFlights(String noOfFlights, String budget) {
        int flightCount = Integer.parseInt(noOfFlights);
        for (int i = 0; i < flightCount; i++) {
            clickAddFlightButton();
            enterLineItemBudget(budget);
        }
    }

    public List<String> fetchFlightDetails() {
        List<String> flightDetail = new ArrayList<>();
        int flightCount = FLIGHT_START_DATE.count();
        for (int i = 0; i < flightCount; i++) {
            flightDetail.add(extractValue(FLIGHT_START_DATE, i));
            flightDetail.add(extractValue(FLIGHT_END_DATE, i));
            flightDetail.add("$" + extractValue(LINE_ITEM_BUDGET, i));
            flightDetail.add(MAX_DAILY_SPEND.nth(i).innerText().replaceAll(".*(\\$[\\d,]+\\.\\d{2}).*", "$1"));
        }
        return flightDetail;
    }

    private String extractValue(Locator locator, int index) {
        String value = locator.nth(index).evaluate("el => el.value").toString();
        return (value != null && !value.isEmpty()) ? value : "";
    }

    public void navigateToLineItemDetails(String lineItemName) {
        page.locator(String.format("//div[@class='main-details' and text()='%s']", lineItemName))
                .click();
        page.locator("//div[contains(@class, 'data-rangeSlider-container')]")
                .or(ADD_FLIGHT_BUTTON)
                .first()
                .waitFor();
    }

    public void clickDetailsTab() {
        TAB_NAMES.locator("text=Details").click();
        waitUtility.waitUntilPreLoaderHidden();
    }

    public void clickOverviewTab() {
        TAB_NAMES.locator("text=Overview").click();
        waitUtility.waitUntilPreLoaderHidden();
    }

    public void deleteFlightEntry() {
        DELETE_FLIGHT.first().click();
    }

    public void generateSequentialFlights(String budget, String numberOfMonths) {
        GENERATE_SEQUENTIAL_FLIGHT.click();
        NUMBER_OF_MONTHS.click();
        page.locator(String.format(
                        "//label[text()='Number of Months']/following-sibling::div//sui-select//div[@class='menu transition visible']/sui-select-option[@title='%s']",
                        numberOfMonths))
                .click();
        enterLineItemBudget(budget);
        GENERATE_FLIGHTS.click();
    }

    public List<String> fetchSequentialFlightStartDates() {
        List<String> sequentialFlights = new ArrayList<>();
        int totalFlightCount = FLIGHT_CONTAINER.count();
        for (int i = 1; i <= totalFlightCount; i++) {
            String flightStartDate = page.locator(String.format(
                            "//div[@class='flight-number' and contains(text(),'%d')]/following-sibling::div//input[contains(@class,'gaFlightStartDate')]",
                            i))
                    .evaluate("el => el.value")
                    .toString();
            sequentialFlights.add(flightStartDate);
        }
        return sequentialFlights;
    }

    public List<String> fetchSequentialFlightEndDates() {
        List<String> sequentialFlights = new ArrayList<>();
        int totalFlightCount = FLIGHT_CONTAINER.count();
        for (int i = 1; i <= totalFlightCount; i++) {
            String flightEndDate = page.locator(String.format(
                            "//div[@class='flight-number' and contains(text(),'%d')]/following-sibling::div//input[contains(@class,'gaFlightEndDate')]",
                            i))
                    .evaluate("el => el.value")
                    .toString();
            sequentialFlights.add(flightEndDate);
        }
        return sequentialFlights;
    }

    public String addNotesToLineItem(String notes) {
        NOTES_ICON.click();
        waitUtility.waitForLocatorVisible(NOTES_TEXTAREA);
        NOTES_TEXTAREA.fill(notes);
        NOTES_OK_BUTTON.click();
        String text = NOTES_SUCCESS_ALERT.innerText();
        waitUtility.waitForLocatorHidden(NOTES_SUCCESS_ALERT);
        return text;
    }

    public void clickBulkEditMode() {
        BULK_EDIT_MODE.evaluate(
                "el => { const container = el.closest('.tacticListContainer'); if (container) { container.scrollTop = el.offsetTop - container.offsetTop; } }");
        BULK_EDIT_MODE.click();
        waitUtility.waitForLocatorVisible(BULK_EDIT_CHECKBOX.first());
    }

    public void selectLineItemUsingBulkEdit(String lineItemName) {
        Locator lineItemXpath = page.locator(String.format(
                "//div[@class='main-details' and text()='%s']/ancestor::div[contains(@class,'li-list item-list-wrapper')]/preceding-sibling::div/sui-checkbox",
                lineItemName));
        if (!lineItemXpath.getAttribute("class").contains("checked")) {
            lineItemXpath.click();
            waitUtility.waitUntilSpinnerHidden();
        }
    }

    public String performBulkModeOperationsOnLineItems(String bulkOperations) {
        if (bulkOperations.equalsIgnoreCase("Disables")) {
            waitUtility.waitForLocatorVisible(BULK_DISABLE_LINE_ITEM);
            BULK_DISABLE_LINE_ITEM.click();
        } else if (bulkOperations.equalsIgnoreCase("Enables")) {
            waitUtility.waitForLocatorVisible(BULK_ENABLE_LINE_ITEM);
            BULK_ENABLE_LINE_ITEM.click();
        }
        String text = BULK_EDIT_SUCCESS_ALERT.innerText().trim();
        waitUtility.waitForLocatorHidden(BULK_EDIT_SUCCESS_ALERT);
        return text;
    }

    public void clickLineItemOptions(String option) {
        LINE_ITEMS_OPTIONS.scrollIntoViewIfNeeded();
        LINE_ITEMS_OPTIONS.click();
        Locator optionXpath = page.locator(String.format(
                "//div[contains(@class,'menu-items-popover')]/div/app-icon-lable-link[@title='%s']", option));
        waitUtility.waitForLocatorVisible(optionXpath);
        optionXpath.click();
    }

    public String createACopyOfLineItem(String name) {
        waitUtility.waitForLocatorVisible(COPY_LINE_ITEM_DIALOG);
        COPY_INPUT.fill(name);
        COPY_BUTTON.click();
        String text = COPY_SUCCESS_ALERT.innerText().trim();
        waitUtility.waitForLocatorHidden(COPY_SUCCESS_ALERT);
        return text;
    }

    public boolean verifyLineItemAvailable(String lineItemName) {
        Locator lineItem = page.locator(String.format("//div[@class='main-details' and text()='%s']", lineItemName));
        lineItem.scrollIntoViewIfNeeded();
        return lineItem.isVisible();
    }

    public void runReportFromLineItemPage() {
        waitUtility.waitUntilSpinnerHidden();
        page.waitForCondition(TEMPLATE_NAME_LABEL::isVisible);
    }

    public void performDeleteOperation() {
        waitUtility.waitForLocatorVisible(REMOVAL_CONFIRMATION_DIALOG);
        REMOVE_BUTTON.click();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(CAMPAIGN_HEADER);
    }

    public List<String> fetchLineItemName() {
        waitUtility.waitForLocatorVisible(FETCH_LINE_ITEM_NAME.last());
        return FETCH_LINE_ITEM_NAME.allTextContents();
    }

    public void exitBulkEditMode() {
        EXIT_BULK_EDIT_MODE.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public boolean checkIfEachLineItemEnabledOrDisabled(String label) {
        Locator xpath = page.locator(String.format("//label[contains(text(),'%s')]", label));
        return xpath.innerText().trim().contains(label);
    }

    public String fetchLineItemNotes() {
        NOTES_ICON.hover();
        return TOOL_TIP.innerText().trim();
    }

    public List<String> fetchLineItemDetails() {
        List<String> originalLineItemDetails = new ArrayList<>();
        originalLineItemDetails.add(COST_MODEL
                .locator("xpath=./descendant::div[contains(@class, 'text')]")
                .textContent());
        if (FLAT_CPM.isVisible()) originalLineItemDetails.add(FLAT_CPM.inputValue());
        Locator budgetXpath = BUDGET_DISTRIBUTION.locator("xpath=//button");
        for (int i = 0; i < budgetXpath.count(); i++) {
            if (budgetXpath.nth(i).getAttribute("class").contains("active")) {
                originalLineItemDetails.add(
                        budgetXpath.nth(i).innerText().trim().split(" ")[0]);
                break;
            }
        }
        originalLineItemDetails.add(FLIGHT_START_DATE.inputValue());
        originalLineItemDetails.add(FLIGHT_END_DATE.inputValue());
        if (LINE_ITEM_BUDGET.isVisible()) originalLineItemDetails.add(LINE_ITEM_BUDGET.inputValue());
        originalLineItemDetails.add(PACING_MODE
                .locator("xpath=./descendant::div[contains(@class, 'text')]/span[2]")
                .textContent()
                .trim());
        if (PACING_MODE_INPUT.isVisible()) originalLineItemDetails.add(PACING_MODE_INPUT.inputValue());
        return originalLineItemDetails;
    }

    public void createLineItem(String type, String lineItemName, Map<String, String> attributeMap) {
        enterLineItemName(lineItemName);
        selectLineItemType(type);
        selectCostModel(attributeMap.get("CostModel"), attributeMap.get("CPMAmount"));
        selectBudgetDistribution(attributeMap.get("BudgetDistribution"));
        clickAddFlightButton();
        enterLineItemBudget(attributeMap.get("LineBudget"));
        selectPacingMode(attributeMap.get("PacingMode"), attributeMap.get("PacingPercentage"));
        enableLineItem();
    }

    public void selectCostModel(String costModel, String cpm) {
        String optionXPath =
                String.format("//div[contains(@class, 'gaCostType') and normalize-space(text())='%s']", costModel);
        if (!COST_MODEL.getAttribute("class").contains("disabled")) {
            COST_MODEL.click();
            COST_MODEL.locator(optionXPath).click();
            if (FLAT_CPM.isVisible()) FLAT_CPM.fill(cpm);
        }
    }

    private void selectBudgetDistribution(String budgetDistribution) {
        if (!BUDGET_DISTRIBUTION.getAttribute("class").contains("disabled")) {
            Locator xpath =
                    BUDGET_DISTRIBUTION.locator("xpath=//button[normalize-space(.)='" + budgetDistribution + "']");
            xpath.click();
        }
    }

    public void selectPacingMode(String pacingMode, String pacingPercentage) {
        PACING_MODE.click();
        String optionXPath = String.format("//sui-select-option//span[text()='%s']", pacingMode);
        PACING_MODE.locator(optionXPath).click();
        if (PACING_MODE_INPUT.isVisible()) PACING_MODE_INPUT.fill(pacingPercentage);
    }

    public void isPlacementIdAvailable(String lineItemNameRandom) {
        if (PLACEMENT_ID.isVisible()) {
            PLACEMENT_ID.fill(lineItemNameRandom);
        }
    }

    public void clickLineItem(String name) {
        String xpath = String.format("//div[text()='%s']", name);
        page.locator(xpath).click();
        waitUtility.waitForElementVisible("//div[contains(@class, 'data-rangeSlider-container')]");
    }

    public boolean isImpressionCapCheckboxAvailable(String impressionCap) {
        Locator impressionCapLocator = page.locator(String.format("//label[contains(text(),'%s')]", impressionCap));
        return impressionCapLocator.count() > 0;
    }

    public void clickImpressionCapCheckbox(String impressionCap) {
        Locator impressionCapLocator = page.locator(String.format("//label[contains(text(),'%s')]", impressionCap));
        for (int i = 0; i < impressionCapLocator.count(); i++) {
            if (impressionCapLocator.nth(i).isVisible()) {
                impressionCapLocator.nth(i).click();
                break;
            }
        }
    }

    public boolean isImpressionCapCheckboxChecked(String impressionCap) {
        Locator impressionCapLocator =
                page.locator(String.format("//label[contains(text(),'%s')]/parent::sui-checkbox", impressionCap));
        for (int i = 0; i < impressionCapLocator.count(); i++) {
            if (impressionCapLocator.nth(i).isVisible()) {
                return impressionCapLocator.nth(i).getAttribute("class").contains("checked");
            }
        }
        return false;
    }

    public boolean isImpressionCapErrorMessageVisible() {
        return IMPRESSION_ERROR_MESSAGE.count() > 0;
    }

    public void addCustomField(String customFieldName) {
        ADD_CUSTOM_FIELD.click();
        ADD_CUSTOM_FIELD_INPUT.fill(customFieldName);
        SAVE_CUSTOM_FIELD_BUTTON.click();
        waitUtility.waitForLocatorVisible(FIELD_CREATE_SUCCESS);
    }

    public boolean isCustomFieldAvailable(String fieldName) {
        waitUtility.waitUntilPreLoaderHidden();
        waitUtility.waitForLocatorVisible(
                page.locator("//app-life-custom-field-setting//label[contains(@class,'form-label')]")
                        .last());
        return page.locator(String.format(
                        "//span[contains(@class,'cmp-form-label-text') and contains(text(),'%s')]", fieldName))
                .isVisible();
    }

    public void clearCustomFieldFromLineItem(String fieldName) {
        waitUtility.waitUntilSpinnerHidden();
        if (LINE_ITEM_PANEL_NAME.last().isVisible()) {
            for (int i = 0; i < LINE_ITEM_PANEL_NAME.count(); i++) {
                LINE_ITEM_PANEL_NAME.nth(i).click();
                waitUtility.waitForLocatorVisible(LINE_ITEM_OVERVIEW_TAB);
                waitUtility.waitForLocatorVisible(LINE_ITEM_DETAILS_TAB);
                LINE_ITEM_DETAILS_TAB.click();
                Locator customFieldInput = page.locator(String.format("//span[@class='cmp-form-label-text' and contains(text(),'%s')]/parent::label[contains(@class,'cmp-form-label')]//following-sibling::input", fieldName));
                waitUtility.waitForLocatorVisible(customFieldInput.last());
                for (int j = 0; j < customFieldInput.count(); j++) {
                    if (customFieldInput.nth(j).isVisible()) {
                        customFieldInput.nth(j).clear();
                    }
                }
                if (SAVE_LINE_ITEM.isVisible()) {
                    SAVE_LINE_ITEM.click();
                    waitUtility.waitForLocatorHidden(LINE_ITEM_SUCCESS);
                }
                TacticDetails tacticDetails = new TacticDetails(page);
                tacticDetails.clearCustomFieldFromTactic(fieldName);
            }
        }
    }
}
