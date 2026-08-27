package pages.life;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import factory.DriverFactory;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import utils.CommonUtils;
import utils.WaitUtility;

public class ScheduleReport {

    private final Page page;
    private final Locator SCHEDULE_REPORT_BUTTON;
    private final Locator SCHEDULE_REPORT_PANEL_HEADER;
    private final Locator REPORT_NAME;
    private final Locator FREQUENCY_BUTTON;
    private final Locator SCHEDULE_START_DATE;
    private final Locator SCHEDULE_END_DATE;
    private final Locator CALENDAR_VIEW;
    private final Locator TIME_ZONE;
    private final Locator DEFAULT_TIME_ZONE;
    private final Locator WEEK_DAYS_BUTTON;
    private final Locator SEND_AT_TIME;
    private final Locator DELIVERY_METHODS;
    private final Locator DELIVERY_TO_USER;
    private final Locator ADD_EMAILS_LINK;
    private final Locator DELIVER_TO_EXTERNAL_EMAILS;
    private final Locator DESTINATION_DROPDOWN;
    private final Locator DESTINATION_NAME;
    private final Locator DESTINATION_TYPE;
    private final Locator DESTINATION_TYPE_VALUES;
    private final Locator FILE_PATH;
    private final Locator FILE_NAME;
    private final Locator COMPRESSION_BUTTON;
    private final Locator CONTROL_FILE;
    private final Locator REPORT_PERIOD;
    private final Locator START_DATE;
    private final Locator END_DATE;
    private final Locator SCHEDULE_START_TIME;
    private final Locator SCHEDULE_END_TIME;
    private final Locator SCHEDULE_BUTTON;
    private final Locator SUCCESS_ALERT;
    private final Locator SEARCH_TEXTBOX;
    private final Locator SEARCH_ICON;
    private final Locator FETCHED_TEMPLATE_NAME;
    private final Locator SEND_ON_DROPDOWN;
    private final Locator REPORT_TIMING_CHECKBOX;
    private final Locator GENERAL_AND_TIME_VARIABLE;
    private final Locator THREE_DOT_MENU_CLOSE_BUTTON;
    private final Locator THREE_DOT_MENU;
    private final Locator DATE_TIME_FORMAT_OPTIONS;
    private final Locator REPORT_SECOND_ROW;
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());
    RunReportPanel runReportPanel = new RunReportPanel(DriverFactory.getPage());
    private String[] cachedDates;

    public ScheduleReport(Page page) {
        this.page = page;
        this.SCHEDULE_REPORT_BUTTON = page.locator("//button[text()='Schedule Report']");
        this.SCHEDULE_REPORT_PANEL_HEADER = page.locator("//div[contains(text(),'Schedule Report')]");
        this.REPORT_NAME = page.locator("//input[@formcontrolname='scheduleReportName']");
        this.FREQUENCY_BUTTON = page.locator("//button[@name='frequencyOptionType']");
        this.SCHEDULE_START_DATE = page.locator("//input[@id='scheduleStartDate']");
        this.SCHEDULE_END_DATE = page.locator("//input[@id='scheduleEndDate']");
        this.CALENDAR_VIEW = page.locator("sui-calendar-date-view");
        this.TIME_ZONE = page.locator("//div[contains(@class, 'gaTimezone')]//div[contains(@class,'dropdown')]");
        this.DEFAULT_TIME_ZONE = page.locator(
                "//div[contains(@class, 'gaTimezone')]/div[contains(@class,'dropdown')]/div[@class='text']");
        this.WEEK_DAYS_BUTTON = page.locator("//button[@name='reportScheduleWeekDayTYpe']");
        this.SEND_AT_TIME =
                page.locator("//label[contains(text(),'Send At')]/following-sibling::div/input[@type='time']");
        this.DELIVERY_METHODS = page.locator("//div[contains(@class,'delivery-method-navbar')]/a");
        this.DELIVERY_TO_USER =
                page.locator("//div[contains(text(),'Deliver to Users')]/following-sibling::div/app-multi-select");
        this.ADD_EMAILS_LINK = page.locator("//label[contains(text(),'Add Emails')]");
        this.DELIVER_TO_EXTERNAL_EMAILS =
                page.locator("//div[contains(text(),'Deliver to External Emails')]/following-sibling::textarea");
        this.DESTINATION_DROPDOWN = page.locator("//div[contains(text(),'Destination')]/following-sibling::sui-select");
        this.DESTINATION_NAME = page.locator("//label[contains(text(),'Destination Name')]/following-sibling::input");
        this.DESTINATION_TYPE =
                page.locator("//label[contains(text(),'Destination Type')]/following-sibling::sui-select");
        this.DESTINATION_TYPE_VALUES = page.locator(
                "//label[contains(text(),'Destination Type')]/following-sibling::sui-select//sui-select-option/span[normalize-space()]");
        this.FILE_PATH = page.locator("//div[contains(text(),'File Path')]/following-sibling::textarea");
        this.FILE_NAME = page.locator("//div[contains(text(),'File Name')]/following-sibling::textarea");
        this.COMPRESSION_BUTTON = page.locator("//div[contains(text(),'Compression')]/following-sibling::div/button");
        this.CONTROL_FILE = page.locator("//label[contains(text(),'Control File')]/following-sibling::sui-checkbox");
        this.REPORT_PERIOD = page.locator(
                "//div[text()='Report Period']/following-sibling::div//div[contains(@class,'reportPeriodDropdown')]");
        this.START_DATE = page.locator("//input[@placeholder='Start Date']");
        this.END_DATE = page.locator("//input[@placeholder='End Date']");
        this.SCHEDULE_START_TIME = page.locator("//input[@formcontrolname='scheduleDataStartTime']");
        this.SCHEDULE_END_TIME = page.locator("//input[@formcontrolname='scheduleDataEndTime']");
        this.SCHEDULE_BUTTON = page.locator("//button[contains(@class, 'okButton') and contains(text(),'Schedule')]");
        this.SUCCESS_ALERT = page.locator("//div[@aria-label='Success!']");
        this.SEARCH_TEXTBOX = page.locator("//input[contains(@class,'gaTableSearch')]");
        this.SEARCH_ICON = page.locator("//div[contains(@class,'gaTableSearchBtn')]");
        this.FETCHED_TEMPLATE_NAME = page.locator(
                "//label[text()='Template']//following-sibling::app-single-select-dropdown//input/following-sibling::span");
        this.SEND_ON_DROPDOWN = page.locator("//label[contains(text(),'Send On')]/following-sibling::div");
        this.REPORT_TIMING_CHECKBOX = page.locator("//div[contains(@class,'report-timing-container')]//sui-checkbox");
        this.GENERAL_AND_TIME_VARIABLE = page.locator("//div[contains(@class,'variablesContainer')]//a");
        this.THREE_DOT_MENU_CLOSE_BUTTON = page.locator("//div[contains(@class,'icons_20-close')]");
        this.THREE_DOT_MENU = page.locator("//div[@class='variableTooltip']");
        this.DATE_TIME_FORMAT_OPTIONS = page.locator("//span[@class='variable-text']");
        this.REPORT_SECOND_ROW = page.locator("//tr[contains(@class,'fixedrow ng-star-inserted')][2]");
    }

    public void clickScheduleReportButton() {
        SCHEDULE_REPORT_BUTTON.click();
        waitUtility.waitForLocatorVisible(SCHEDULE_REPORT_PANEL_HEADER);
    }

    public boolean isScheduleReportPanelOpened() {
        waitUtility.waitForLocatorVisible(SCHEDULE_REPORT_PANEL_HEADER);
        return SCHEDULE_REPORT_PANEL_HEADER.isVisible();
    }

    public boolean isReportNameAvailable() {
        return REPORT_NAME.isVisible();
    }

    public void enterReportName(String reportName) {
        REPORT_NAME.fill(reportName);
    }

    public List<String> fetchFrequencyOptions() {
        return FREQUENCY_BUTTON.allInnerTexts();
    }

    public boolean checkDefaultFrequencyOption(String defaultValue) {
        for (int i = 0; i < FREQUENCY_BUTTON.count(); i++) {
            if (FREQUENCY_BUTTON.nth(i).innerText().contains(defaultValue)
                    && FREQUENCY_BUTTON.nth(i).getAttribute("class").contains("active")) {
                return true;
            }
        }
        return false;
    }

    public boolean selectScheduleStartDate() {
        cachedDates = CommonUtils.generateStartAndEndDates();
        return runReportPanel.selectDate(SCHEDULE_START_DATE, cachedDates[0]);
    }

    public boolean selectScheduleEndDate() {
        if (cachedDates == null) {
            cachedDates = CommonUtils.generateStartAndEndDates();
        }
        return runReportPanel.selectDate(SCHEDULE_END_DATE, cachedDates[1]);
    }

    private boolean selectDate(Locator input, int day) {
        String dayToSelect = String.valueOf(day);
        try {
            input.click();
            CALENDAR_VIEW.waitFor(new Locator.WaitForOptions().setTimeout(3000));
            Locator dayCells = CALENDAR_VIEW.locator("//td[normalize-space()='" + dayToSelect + "']");
            Locator correctDay = dayCells.nth(0);

            int maxDay = YearMonth.now().lengthOfMonth();
            if (day >= 1 && day <= maxDay) {
                correctDay = dayCells.last();
            }
            correctDay.click();
            waitUtility.waitForLocatorDetached(CALENDAR_VIEW);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean selectDataTimeZone(String timeZone) {
        selectTimeZone(TIME_ZONE, timeZone);
        return DEFAULT_TIME_ZONE.innerText().contains(timeZone);
    }

    public void selectTimeZone(Locator locator, String timeZone) {
        locator.click();
        locator.getByText(timeZone).first().click();
        page.waitForTimeout(1000);
    }

    public String fetchDefaultTimeZone() {
        return DEFAULT_TIME_ZONE.innerText().trim();
    }

    public List<String> fetchWeekDays() {
        return WEEK_DAYS_BUTTON.allInnerTexts();
    }

    public boolean checkDefaultWeekDay(String defaultValue) {
        for (int i = 0; i < WEEK_DAYS_BUTTON.count(); i++) {
            if (WEEK_DAYS_BUTTON.nth(i).innerText().contains(defaultValue)
                    && WEEK_DAYS_BUTTON.nth(i).getAttribute("class").contains("active")) {
                return true;
            }
        }
        return false;
    }

    public boolean isSendAtFieldAvailable() {
        return SEND_AT_TIME.isVisible() && TIME_ZONE.isVisible();
    }

    public String fetchSendAtTimeValue() {
        return SEND_AT_TIME.inputValue();
    }

    public String fetchSendAtTimezoneValue() {
        return TIME_ZONE.innerText().trim();
    }

    public void enterSendAtTime(String time) {
        SEND_AT_TIME.fill(time);
    }

    public List<String> verifyDeliveryMethods() {
        return DELIVERY_METHODS.allInnerTexts();
    }

    public void clickDeliveryTab(String tabName) {
        for (int i = 0; i < DELIVERY_METHODS.count(); i++) {
            if (DELIVERY_METHODS.nth(i).innerText().contains(tabName))
                DELIVERY_METHODS.nth(i).click();
        }
    }

    public boolean isDeliveryToUserAvailable() {
        return DELIVERY_TO_USER.isVisible();
    }

    public void selectUsersForDelivery(List<String> userLists) {
        DELIVERY_TO_USER.click();
        for (String user : userLists) {
            DELIVERY_TO_USER.getByRole(AriaRole.TEXTBOX).fill(user.trim());
            page.getByText(user.trim()).first().click();
        }
        page.keyboard().press("Escape");
    }

    public boolean isAddEmailLinkAvailable() {
        return ADD_EMAILS_LINK.isVisible();
    }

    public void clickAddEmailsLink() {
        ADD_EMAILS_LINK.click();
    }

    public boolean isDeliverToExternalEmailsAvailable() {
        return DELIVER_TO_EXTERNAL_EMAILS.isVisible();
    }

    public void enterExternalEmails(List<String> emailList) {
        for (String email : emailList) {
            DELIVER_TO_EXTERNAL_EMAILS.type(email);
            page.keyboard().press("Shift+Enter");
        }
    }

    public boolean isDestinationAvailable() {
        return DESTINATION_DROPDOWN.isVisible();
    }

    public boolean isAddDestinationAvailable(String buttonName) {
        DESTINATION_DROPDOWN.click();
        return page.getByText(buttonName).isVisible();
    }

    public void clickAddDestination(String buttonName) {
        page.getByText(buttonName).click();
    }

    public boolean isDestinationNameAndTypeDisplayed() {
        return DESTINATION_NAME.isVisible() && DESTINATION_TYPE.isVisible();
    }

    public List<String> fetchDestinationTypes() {
        return DESTINATION_TYPE_VALUES.allInnerTexts();
    }

    public boolean isFilePathAvailable() {
        return FILE_PATH.isVisible();
    }

    public boolean checkDefaultCompression(String defaultCompressionType) {
        for (int i = 0; i < COMPRESSION_BUTTON.count(); i++) {
            if (COMPRESSION_BUTTON.nth(i).innerText().contains(defaultCompressionType)
                    && COMPRESSION_BUTTON.nth(i).getAttribute("class").contains("active")) {
                return true;
            }
        }
        return false;
    }

    public List<String> fetchCompressionTypes() {
        return COMPRESSION_BUTTON.allInnerTexts();
    }

    public boolean isControlFileCheckboxAvailable() {
        return CONTROL_FILE.isVisible();
    }

    public boolean isControlFileCheckboxSelected() {
        return !CONTROL_FILE.getAttribute("class").contains("checked");
    }

    public void clickFrequencyOption(String frequencyType) {
        CommonUtils.selectAndClickElement(FREQUENCY_BUTTON, Collections.singletonList(frequencyType));
    }

    public boolean isFileNameAvailable() {
        return FILE_NAME.isVisible();
    }

    public void selectReportPeriod(String periodType) {
        String text = REPORT_PERIOD.locator("xpath=//div[@class='text']").innerText();
        if (!text.contains(periodType)) {
            REPORT_PERIOD.click();
            REPORT_PERIOD.getByText(periodType).click();
        }
    }

    public boolean selectStartAndEndTime(String startTime, String endTime) {
        try {
            SCHEDULE_START_TIME.fill(startTime);
            SCHEDULE_END_TIME.fill(endTime);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean selectStartDate() {
        String[] dates = CommonUtils.generateStartAndEndDates();
        return runReportPanel.selectDate(START_DATE, dates[0]);
    }

    public boolean selectEndDate() {
        String[] dates = CommonUtils.generateStartAndEndDates();
        return runReportPanel.selectDate(END_DATE, dates[1]);
    }

    public void clickScheduleButton() {
        SCHEDULE_BUTTON.click();
    }

    public String fetchSuccessAlert() {
        String text = SUCCESS_ALERT.innerText().trim();
        waitUtility.waitForLocatorHidden(SUCCESS_ALERT);
        return text;
    }

    public void searchReport(String reportName) {
        Locator locator = page.locator(String.format("//div[contains(@title,'%s')]", reportName));
        waitUtility.waitForLocatorVisible(REPORT_SECOND_ROW);
        SEARCH_TEXTBOX.fill(reportName);
        SEARCH_ICON.click();
        waitUtility.waitForLocatorVisible(locator);
    }

    public void clickReportName(String reportName) {
        Locator locator = page.locator(String.format("//div[contains(@title,'%s')]", reportName));
        locator.click();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(FETCHED_TEMPLATE_NAME);
    }

    public String fetchStartTime() {
        if (SCHEDULE_START_TIME.isVisible()) return SCHEDULE_START_TIME.inputValue();
        return " ";
    }

    public String fetchEndTime() {
        if (SCHEDULE_END_TIME.isVisible()) return SCHEDULE_END_TIME.inputValue();
        return " ";
    }

    public String fetchTimeZone() {
        if (TIME_ZONE.isVisible())
            return TIME_ZONE.locator("xpath=/div[@class='text']").innerText().trim();
        return " ";
    }

    public String fetchSendAtTime() {
        if (SEND_AT_TIME.isVisible()) return SEND_AT_TIME.inputValue();
        return " ";
    }

    public String fetchSendAtTimezone() {
        if (TIME_ZONE.isVisible()) return DEFAULT_TIME_ZONE.innerText().trim();
        return " ";
    }

    public String fetchReportName() {
        return REPORT_NAME.inputValue();
    }

    public String fetchReportPeriod() {
        return REPORT_PERIOD.innerText();
    }

    public boolean isSendOnAvailable() {
        return SEND_ON_DROPDOWN.isVisible();
    }

    public boolean selectDayOfTheMonth(String dayOfTheMonth) {
        SEND_ON_DROPDOWN.click();
        SEND_ON_DROPDOWN
                .getByText(dayOfTheMonth, new Locator.GetByTextOptions().setExact(true))
                .click();
        return SEND_ON_DROPDOWN
                .locator("xpath=//div[@class='text']")
                .innerText()
                .contains(dayOfTheMonth);
    }

    public boolean selectWeekDay(String weekDay) {
        for (int i = 0; i < WEEK_DAYS_BUTTON.count(); i++) {
            if (WEEK_DAYS_BUTTON.nth(i).innerText().contains(weekDay)) {
                WEEK_DAYS_BUTTON.nth(i).click();
                return true;
            }
        }
        return false;
    }

    public void enterCustomDestinationDetailsOnReportPanel(String destinationName, String filePath, String fileName) {
        waitUtility.waitForLocatorVisible(DESTINATION_DROPDOWN);
        DESTINATION_DROPDOWN.click();
        page.getByText(destinationName).click();
        FILE_PATH.click();
        FILE_PATH.fill(filePath);
        FILE_NAME.click();
        FILE_NAME.fill(fileName);
    }

    public boolean isReportPeriodFieldAvailable() {
        return REPORT_PERIOD.isVisible();
    }

    public String fetchDefaultReportPeriodValue() {
        return REPORT_PERIOD.locator("xpath=//div[@class='text']").textContent().trim();
    }

    public List<String> fetchReportPeriodOptions() {
        Locator options =
                REPORT_PERIOD.locator("xpath=//div[contains(@class,'item') and not(contains(@class,'hidden'))]");
        REPORT_PERIOD.click();
        List<String> optionTexts = options.allInnerTexts();
        page.keyboard().press("Escape");
        return optionTexts;
    }

    public boolean isReportTimingCheckboxAvailable() {
        return REPORT_TIMING_CHECKBOX.isVisible();
    }

    public boolean isReportTimingCheckboxChecked() {
        return !REPORT_TIMING_CHECKBOX.getAttribute("class").contains("checked");
    }

    public List<String> fetchListingPageDetails(String reportName) {
        List<String> fetchedData = new ArrayList<>();
        Locator tableHeader = page.locator(String.format(
                "//div[@title='%s']/ancestor::div[contains(@class,'firsttablewrapper')]/following-sibling::div//tr//th//div[@class='filter-container']",
                reportName));
        int index = 0;
        for (int i = 0; i < tableHeader.count(); i++) {
            String headerText = tableHeader.nth(i).innerText().trim();
            if (headerText.equals("Template Name")
                    || headerText.equals("Frequency")
                    || headerText.equals("Reporting Period")
                    || headerText.equals("Generated By")) {
                index = i;
                Locator tableRow = page.locator(String.format(
                        "//div[@title='%s']/ancestor::div[contains(@class,'firsttablewrapper')]/following-sibling::div//td[%d]//div",
                        reportName, index + 1));
                fetchedData.add(tableRow.textContent().trim());
            }
        }
        return fetchedData;
    }

    public void clickThreeDotMenuForFileName() {
        THREE_DOT_MENU.click();
    }

    public void selectGeneralVariableFromThreeDotMenu(String generalVariable) {
        GENERAL_AND_TIME_VARIABLE.getByText(generalVariable).click();
    }

    public void selectTimeVariableFromThreeDotMenu(String timeVariable, String dateTimeFormat) {
        List<String> options = List.of(dateTimeFormat.split("-"));
        GENERAL_AND_TIME_VARIABLE.getByText(timeVariable).click();
        for (String option : options) {
            DATE_TIME_FORMAT_OPTIONS
                    .getByText(option, new Locator.GetByTextOptions().setExact(true))
                    .click();
        }
    }

    public void closeThreeDotMenu() {
        THREE_DOT_MENU_CLOSE_BUTTON.click();
        THREE_DOT_MENU.scrollIntoViewIfNeeded();
    }

    public String fetchDestinationOptions() {
        return DESTINATION_DROPDOWN.locator("xpath=//span[2]").textContent();
    }
}
