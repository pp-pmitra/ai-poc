package pages.life;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import factory.DriverFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import utils.CommonUtils;
import utils.WaitUtility;

public class RunReportPanel {
    private final Page page;
    private final Locator RUN_REPORT_PANEL_HEADER;
    private final Locator TEMPLATE_DROPDOWN;
    private final Locator DIMENSION_DROPDOWN;
    private final Locator METRICS_DROPDOWN;
    private final Locator DATA_GRANULARITY_SELECTED_VALUE;
    private final Locator DATA_GRANULARITY_DROPDOWN;
    private final Locator DATA_GRANULARITY_DROPDOWN_VALUES;
    private final Locator ADVERTISER_DROPDOWN;
    private final Locator ADVERTISER_DROPDOWN_VALUES;
    private final Locator ADVERTISER_SELECTED;
    private final Locator DROPDOWN_LIST;
    private final Locator SELECTED_VALUES_FROM_DROPDOWN;
    private final Locator DROPDOWN_LOADER;
    private final Locator FILTER_REPORT_CHECKBOX_LABEL;
    private final Locator ADVANCED_SETTING_LINK;
    private final Locator REPORT_PERIOD_BUTTONS;
    private final Locator RUN_BUTTON;
    private final Locator SUCCESS_ALERT;
    private final Locator REPORT_MODIFY_OPTION;
    private final Locator FETCHED_TEMPLATE_NAME;
    private final Locator FETCHED_ADVERTISER_NAME;
    private final Locator FETCHED_CAMPAIGN_NAME;
    private final Locator FETCHED_LINE_ITEM_NAME;
    private final Locator FETCHED_TACTIC_NAME;
    private final Locator FETCHED_CREATIVE_NAME;
    private final Locator SEARCH_REPORT;
    private final Locator SEARCH_BUTTON;
    private final Locator START_DATE;
    private final Locator END_DATE;
    private final Locator START_TIME;
    private final Locator END_TIME;
    private final Locator TIME_ZONE;
    private final Locator REPORT_FORMAT_SELECTED_VALUE;
    private final Locator CALENDAR_VIEW;
    private final Locator TEXT_QUALIFIER_CHECKBOX;
    private final Locator FETCHED_DIMENSIONS_AND_METRICS;
    private final Locator DIMENSION_LABEL;
    private final Locator FILE_NAME_ERROR;
    private final Locator DEFAULT_FLIGHT_DETAILS;
    private final Locator FLIGHT_DETAILS_DROPDOWN;
    private final Locator FLIGHT_DETAILS_DROPDOWN_VALUE;
    private final Locator FILE_BREAKDOWN_TYPE;
    private final Locator ALERT_MESSAGE;
    private final Locator CALENDAR_TITLE;
    private final Locator CALENDAR_DATA;
    private final Locator DELIVERY_METHOD;
    private final Locator LOGGED_IN_USERNAME;
    private final Locator SCHEDULE_REPORT_USERNAME_LIST;
    private final Locator FILE_NAME;
    private final Locator REPORT_TIMING_CHECKBOX;
    private final Locator CREATED_BY;
    private final Locator REPORTING_PERIOD;
    private final Locator REPORT_NAME;
    private final Locator TOOL_TIP;
    private final Locator ADVANCED_EXPORT_CHECKBOX;
    private final Locator ADVANCED_DELIVERY_SETTING_LINK;
    private final Locator LINE_CODING;
    private final Locator LINE_CODING_OPTIONS;
    private final Locator DESTINATION_NAME;
    private final Locator DESTINATION_TYPE;
    private final Locator HOST;
    private final Locator USERNAME;
    private final Locator PASSWORD;
    private final Locator PORT;
    private final Locator SERVER_PATH;
    private final Locator EDIT_DESTINATION_BUTTON;
    private final Locator TEST_ACCESS_BUTTON;
    private final Locator CUSTOM_DESTINATION_RUN_BUTTON;
    private final Locator CUSTOM_DESTINATION_CREATE_BUTTON;
    private final Locator CUSTOM_DESTINATION_CANCEL_BUTTON;
    private final Locator DESTINATION_DROPDOWN;
    private final Locator FILE_NAME_HELP_TEXT;
    private final Locator RE_RUN_ACCESS_BUTTON;
    private final Locator GENERATED_REPORT_OPTIONS;
    private final Locator DELETE_REPORT_BUTTON;
    private final Locator DELETE_REPORT_CONFIRMATION_POPUP;
    private final Locator DELETE_REPORT_REMOVE_BUTTON;
    private final Locator DELETE_REPORT_SUCCESS_ALERT;
    private final Locator NO_REPORT_AVAILABLE_TEXT;
    private final Locator CLEAR_SEARCH_ICON;
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());
    private String[] cachedDates;

    public RunReportPanel(Page page) {
        this.page = page;
        this.RUN_REPORT_PANEL_HEADER = page.locator("//div[contains(text(),'Run Report')]");
        this.TEMPLATE_DROPDOWN = page.locator("//input[@placeholder='Select Template']");
        this.DIMENSION_DROPDOWN = page.locator("//input[@placeholder='All Dimensions']");
        this.METRICS_DROPDOWN = page.locator("//input[@placeholder='All Metrics']");
        this.DATA_GRANULARITY_DROPDOWN = page.locator("//sui-select[@placeholder='Data Granularity']");
        this.DATA_GRANULARITY_SELECTED_VALUE = page.locator(
                "//sui-select[@placeholder='Data Granularity']//div[@class='text']/span[normalize-space()]");
        this.DATA_GRANULARITY_DROPDOWN_VALUES = page.locator(
                "//sui-select[@placeholder='Data Granularity']//div[@class='menu transition visible']//span[normalize-space()]");
        this.ADVERTISER_DROPDOWN = page.locator("//sui-multi-select[@placeholder='Select Advertiser']");
        this.ADVERTISER_DROPDOWN_VALUES = page.locator(
                "//sui-multi-select[@placeholder='Select Advertiser']//div[@class='menu transition visible']//span[normalize-space()]");
        this.ADVERTISER_SELECTED =
                page.locator("//sui-multi-select[@placeholder='Select Advertiser']//sui-multi-select-label");
        this.DROPDOWN_LIST = page.locator("//div[contains(@class,'menu transition visible')]");
        this.SELECTED_VALUES_FROM_DROPDOWN = page.locator(
                "//div[@id='tacticLookup']/a");
        this.DROPDOWN_LOADER = page.locator("//div[contains(@class,'loading')]");
        this.FILTER_REPORT_CHECKBOX_LABEL =
                page.locator("//sui-checkbox[contains(@class,'advancedSettingsCheck')]//label");
        this.ADVANCED_SETTING_LINK =
                page.locator("//label[@class='advanceSettings' and contains(text(),'Show Advanced Settings')]");
        this.REPORT_PERIOD_BUTTONS =
                page.locator("//label[contains(text(),'Report Period')]/following-sibling::div//button");
        this.RUN_BUTTON = page.locator("//button[contains(@class, 'okButton') and contains(text(),'Run')]");
        this.SUCCESS_ALERT = page.locator("//div[@aria-label='Success!']");
        this.REPORT_MODIFY_OPTION = page.locator(
                "//div[@class='icon report-progress']/ancestor::div[@class='left icon-section']/following-sibling::div//img/following-sibling::div//span[contains(text(),'Modify and Re-run')]");
        this.FETCHED_TEMPLATE_NAME = page.locator(
                "//label[text()='Template']//following-sibling::app-single-select-dropdown//input/following-sibling::span");
        this.FETCHED_ADVERTISER_NAME =
                page.locator("//sui-multi-select[@placeholder='Select Advertiser']//sui-multi-select-label//span[2]");
        this.FETCHED_CAMPAIGN_NAME = page.locator("//input[@name='campaignLookupInp']/following-sibling::a");
        this.FETCHED_LINE_ITEM_NAME = page.locator("//input[@name='lineItemLookupInp']/following-sibling::a");
        this.FETCHED_TACTIC_NAME = page.locator("//input[@name='tacticLookupInp']/following-sibling::a");
        this.FETCHED_CREATIVE_NAME = page.locator("//input[@name='creativesLookupInp']/following-sibling::a");
        this.SEARCH_REPORT = page.locator("//div[contains(@class,'search-field')]/input[@placeholder='Search']");
        this.SEARCH_BUTTON = page.locator("div.iconSprite.search1");
        this.START_DATE = page.locator("//input[@formcontrolname='startDate']");
        this.END_DATE = page.locator("//input[@formcontrolname='endDate']");
        this.START_TIME = page.locator("input[formcontrolname='startTime']");
        this.END_TIME = page.locator("input[formcontrolname='endTime']");
        this.TIME_ZONE = page.locator("//select[@formcontrolname='stimeZone']");
        this.REPORT_FORMAT_SELECTED_VALUE =
                page.locator("//div[text()='Report Format']/following-sibling::div//div[@class='text']");
        this.CALENDAR_VIEW = page.locator("sui-calendar-date-view");
        this.TEXT_QUALIFIER_CHECKBOX =
                page.locator("//div[text()='Text Qualifier']/following-sibling::div/sui-checkbox");
        this.FETCHED_DIMENSIONS_AND_METRICS = page.locator("//div[@class='field customTemplate']//span");
        this.DIMENSION_LABEL = page.locator("//label[contains(text(),'Dimensions')]");
        this.FILE_NAME_ERROR =
                page.locator("//div[contains(@class,'run-report-error') and contains(text(),'Invalid file Name')]");
        this.DEFAULT_FLIGHT_DETAILS = page.locator("//div[@id='date-option-dropdown']/div[contains(@class, 'text')]");
        this.FLIGHT_DETAILS_DROPDOWN = page.locator("//div[@id='date-option-dropdown']");
        this.FLIGHT_DETAILS_DROPDOWN_VALUE =
                page.locator("//div[@id='date-option-dropdown']/div[@class='menu transition visible']/div");
        this.FILE_BREAKDOWN_TYPE =
                page.locator("//label[contains(text(),'File Breakdown')]/following-sibling::div/button");
        this.ALERT_MESSAGE = page.locator("//div[@role='alert']");
        this.CALENDAR_TITLE = page.locator("//span[contains(@class,'title link')]");
        this.CALENDAR_DATA = page.locator("//td[contains(@class,'link')]");
        this.DELIVERY_METHOD = page.locator("//div[contains(@class,'delivery-method-navbar')]/a");
        this.LOGGED_IN_USERNAME = page.locator("//div[@class='username']");
        this.SCHEDULE_REPORT_USERNAME_LIST = page.locator("//div[@class='menu transition visible']//span");
        this.FILE_NAME = page.locator("//textarea[@placeholder='File Name']");
        this.REPORT_TIMING_CHECKBOX = page.locator(
                "//div[contains(text(),'Report Timing')]/following-sibling::div/sui-checkbox[contains(@class,'evtLevelReport')]");
        this.CREATED_BY = page.locator("//label[contains(text(),'Created by:')]/following-sibling::div");
        this.REPORTING_PERIOD = page.locator("//label[contains(text(),'Reporting Period:')]/following-sibling::div");
        this.REPORT_NAME = page.locator("//div[contains(@class,'name-section')]");
        this.TOOL_TIP = page.locator("//div[contains(@class,'ng-tooltip-show')]");
        this.ADVANCED_EXPORT_CHECKBOX =
                page.locator("//label[contains(text(),'Advanced Export')]/following-sibling::div//sui-checkbox");
        this.ADVANCED_DELIVERY_SETTING_LINK = page.locator(
                "//label[@class='advanceSettings' and contains(text(),'Show Advanced Delivery Settings')]");
        this.LINE_CODING = page.locator("//div[@class='line-endings']");
        this.LINE_CODING_OPTIONS =
                page.locator("//div[@class='line-endings']//sui-radio-button[@name='reportLineEndings']");
        this.DESTINATION_NAME = page.locator("//label[contains(text(),'Destination Name')]/following-sibling::input");
        this.DESTINATION_TYPE =
                page.locator("//label[contains(text(),'Destination Type')]/following-sibling::sui-select");
        this.HOST = page.locator("//label[contains(text(),'Host')]/following-sibling::input");
        this.USERNAME = page.locator("//label[contains(text(),'Username')]/following-sibling::input");
        this.PASSWORD = page.locator("//input[@type='password']");
        this.PORT = page.locator("//label[contains(text(),'Port')]/following-sibling::input");
        this.SERVER_PATH = page.locator("//label[contains(text(),'Path on the server')]/following-sibling::input");
        this.EDIT_DESTINATION_BUTTON = page.locator(
                "//div[@class='destination-name']/following-sibling::div//img[contains(@src,'edit-simple.svg')]");
        this.TEST_ACCESS_BUTTON = page.locator("//span[contains(text(),'Test Access')]");
        this.CUSTOM_DESTINATION_RUN_BUTTON = page.locator(
                "//div[contains(text(),'Choose file size to test access')]/parent::div/following-sibling::div//button[contains(text(),'Run')]");
        this.CUSTOM_DESTINATION_CREATE_BUTTON = page.locator(
                "//div[contains(@class,'test-connection')]/following-sibling::div//button[contains(text(),'Create')]");
        this.CUSTOM_DESTINATION_CANCEL_BUTTON = page.locator(
                "//div[contains(@class,'test-connection')]/following-sibling::div//button[contains(text(),'Cancel')]");
        this.DESTINATION_DROPDOWN = page.locator("//div[contains(text(),'Destination')]/following-sibling::sui-select");
        this.FILE_NAME_HELP_TEXT = page.locator("//span[@class='custom-destination-example-texr']//span");
        this.RE_RUN_ACCESS_BUTTON = page.locator("//span[contains(text(),'Re-run Access')]");
        this.GENERATED_REPORT_OPTIONS = page.locator("//img[@title='options' and contains(@class,'actions')]");
        this.DELETE_REPORT_BUTTON = page.locator("//a[contains(@class,'item')]//span[@class='text' and text()='Delete']");
        this.DELETE_REPORT_CONFIRMATION_POPUP = page.locator("//div[contains(@class,'confirm-modal header') and contains(text(),'Removal Confirmation')]");
        this.DELETE_REPORT_REMOVE_BUTTON = page.locator("//div[contains(@class,'approveButtonText')]/span[contains(text(),'Remove')]");
        this.DELETE_REPORT_SUCCESS_ALERT = page.locator("//div[@role='alert' and contains(text(),'Report deleted successfully')]");
        this.NO_REPORT_AVAILABLE_TEXT = page.locator("//div[contains(text(), 'No Generated Reports')]");
        this.CLEAR_SEARCH_ICON = page.locator("//div[contains(@class,'clear-search-close')]");
    }

    public boolean isRunReportPanelOpened() {
        waitUtility.waitForLocatorVisible(RUN_REPORT_PANEL_HEADER);
        return RUN_REPORT_PANEL_HEADER.isVisible();
    }

    public void clickLink(String linkName) {
        Locator locator = page.locator(String.format("//span[text()='%s']", linkName));
        locator.click();
    }

    public boolean isTemplateDropdownAvailable() {
        return TEMPLATE_DROPDOWN.isVisible();
    }

    public void selectTemplateFromDropdown(String templateName) {
        TEMPLATE_DROPDOWN.fill(templateName);
        Locator optionLocator = page.locator(String.format("[title*='%s']:visible", templateName));
        int count = optionLocator.count();
        while (count == 0) {
            TEMPLATE_DROPDOWN.fill(templateName);
            page.waitForTimeout(2000);
            count = optionLocator.count();
        }
        int randomIndex = (int) (Math.random() * count);
        optionLocator.nth(randomIndex).click();
    }

    public boolean isDimensionsAndMetricsDisplayed() {
        return DIMENSION_DROPDOWN.isVisible() && METRICS_DROPDOWN.isVisible();
    }

    public String getDefaultDataGranularity() {
        return DATA_GRANULARITY_SELECTED_VALUE.innerText().trim();
    }

    public void showDataGranularityOptions() {
        DATA_GRANULARITY_DROPDOWN.click();
    }

    public List<String> fetchDataGranularityOptions() {
        assertThat(DATA_GRANULARITY_DROPDOWN).hasAttribute("class", Pattern.compile(".*active visible.*"));
        return DATA_GRANULARITY_DROPDOWN_VALUES.allInnerTexts();
    }

    public boolean setDataGranularity(String dropdownValue) {
        if (!DATA_GRANULARITY_DROPDOWN.getAttribute("class").contains("active visible")) showDataGranularityOptions();
        CommonUtils.selectAndClickElement(DATA_GRANULARITY_DROPDOWN_VALUES, Collections.singletonList(dropdownValue));
        return DATA_GRANULARITY_SELECTED_VALUE.innerText().equalsIgnoreCase(dropdownValue);
    }

    public boolean isAdvertiserDropdownAvailable() {
        return ADVERTISER_DROPDOWN.isVisible();
    }

    public void clickAdvertiserDropdown() {
        ADVERTISER_DROPDOWN.click();
        waitUtility.waitForLocatorVisible(ADVERTISER_DROPDOWN_VALUES.first());
    }

    public List<String> fetchAdvertisers() {
        return ADVERTISER_DROPDOWN_VALUES.allInnerTexts();
    }

    public boolean selectMultipleAdvertisersFromDropdown() {
        assertThat(ADVERTISER_DROPDOWN).hasAttribute("class", Pattern.compile(".*active visible.*"));
        for (int i = 1; i <= ADVERTISER_DROPDOWN_VALUES.count(); i++) {
            ADVERTISER_DROPDOWN_VALUES.nth(i).click();
        }
        return ADVERTISER_SELECTED.count() > 0;
    }

    public String selectAdvertiser(String advertiser) {
        assertThat(ADVERTISER_DROPDOWN).hasAttribute("class", Pattern.compile(".*active visible.*"));
        CommonUtils.selectAndClickElement(ADVERTISER_DROPDOWN_VALUES, Collections.singletonList(advertiser));
        page.keyboard().press("Escape");
        return ADVERTISER_SELECTED.locator("xpath=.//span[normalize-space()]").innerText();
    }

    public boolean isDropdownValueLoadedForInitials(String initials, String fieldName) {
        Locator locator = page.locator(String.format(
                "//label[text()='%s']/following-sibling::div//input[contains(@placeholder,'%s')]",
                fieldName, fieldName));
        locator.click();
        locator.type(initials);
        try {
            waitUtility.waitForLocatorVisible(DROPDOWN_LOADER, 1000);
        } catch (Exception ignored) {
        }
        waitUtility.waitForLocatorHidden(DROPDOWN_LOADER);
        assertThat(locator.locator("xpath=./following-sibling::div"))
                .hasAttribute("class", Pattern.compile(".*visible.*"));
        return locator.locator("xpath=./following-sibling::div/div").count() > 0;
    }

    public List<String> selectMultipleValueFromDropdown() {
        int count = DROPDOWN_LIST.count();
        int randomIndex = (int) (Math.random() * count);
        for (int i = 0; i < 2; i++) {
            DROPDOWN_LIST.nth(randomIndex).click();
            waitUtility.waitUntilSpinnerHidden();
        }
        page.waitForTimeout(500);
        page.keyboard().press("Escape");
        return SELECTED_VALUES_FROM_DROPDOWN.allInnerTexts();
    }

    public void clickAdvanceSettings() {
        ADVANCED_SETTING_LINK.click();
    }

    public boolean isFilterReportSectionAvailable(String checkboxName) {
        Locator checkboxLocator =
                page.locator(String.format("//label[contains(text(),'%s')]/following-sibling::div", checkboxName));
        return checkboxLocator.isVisible();
    }

    public String fetchAndClickFilterReportCheckboxLabel(String checkboxLabel) {
        Locator locator = FILTER_REPORT_CHECKBOX_LABEL.locator("text=" + checkboxLabel);
        locator.click();
        return locator.innerText().trim();
    }

    public boolean isFilterReportCheckboxAvailable(String checkboxLabel) {
        Locator locator = FILTER_REPORT_CHECKBOX_LABEL.locator("text=" + checkboxLabel);
        return locator.isVisible();
    }

    public boolean isRunNowAndScheduleTabsAvailable(String runNowTab, String scheduleTab) {
        Locator runNowTabLocator = page.locator(String.format("//a[contains(text(),'%s')]", runNowTab));
        Locator scheduleTabLocator = page.locator(String.format("//a[contains(text(),'%s')]", scheduleTab));
        return runNowTabLocator.isVisible() && scheduleTabLocator.isVisible();
    }

    public List<String> fetchReportPeriodOptions() {
        return REPORT_PERIOD_BUTTONS.allInnerTexts();
    }

    public void clickFilterReportCheckbox() {
        FILTER_REPORT_CHECKBOX_LABEL.click();
    }

    public void clickRunButton(String fileName) {
        RUN_BUTTON.click();
        if (FILE_NAME_ERROR.isVisible()) {
            FILE_NAME.fill(fileName);
            RUN_BUTTON.click();
        }
    }

    public String fetchSuccessAlert() {
        String text = ALERT_MESSAGE.innerText().trim();
        waitUtility.waitForLocatorHidden(ALERT_MESSAGE);
        return text;
    }

    public void searchReportName(String templateName) {
        waitUtility.waitUntilPreLoaderHidden();
        waitUtility.waitForElementVisible("div.ui.dropdown.selection.sort-option-dropdown");
        SEARCH_REPORT.fill(templateName);
        SEARCH_BUTTON.click();
    }

    public void clickModifyOption(String templateName) {
        // 1. Safely hide the parent survey element so it doesn't block the screen
        if (page.locator(".app-survey-cow").first().isVisible()) {
            page.locator(".app-survey-cow").evaluate("el => el.style.display = 'none'");
        }
        Locator templateElements = page.locator(String.format(
                "//div[contains(@class, 'content-section')][.//div[contains(@class, 'report-progress')] and .//div[contains(@class, 'name-section') and contains(text(), '%s')]]//img[contains(@class, 'icon-image')]",
                templateName));
        if (!templateElements.first().isVisible()) {
            waitUtility.waitUntilPreLoaderHidden();
            waitUtility.waitForElementVisible("div.ui.dropdown.selection.sort-option-dropdown");
            if (!templateName.contains("Custom Template")) {
                searchReportName(templateName);
            }
        }
        waitUtility.waitForLocatorVisible(templateElements.first());
        templateElements.first().click();
        waitUtility.waitForLocatorVisible(REPORT_MODIFY_OPTION.first());
        REPORT_MODIFY_OPTION.first().click();
        waitUtility.waitUntilSpinnerHidden();
        page.waitForCondition(() -> FETCHED_TEMPLATE_NAME.isVisible() || DIMENSION_LABEL.isVisible());
    }

    public List<String> extractInnerTextFromLocator(Locator locator) {
        List<String> texts = new ArrayList<>();
        int count = locator.count();
        for (int i = 0; i < count; i++) {
            texts.add(locator.nth(i).innerText());
        }
        return texts;
    }

    public List<String> fetchTemplateValue() {
        if (!FETCHED_TEMPLATE_NAME.isVisible()) return Collections.singletonList(" ");
        return extractInnerTextFromLocator(FETCHED_TEMPLATE_NAME);
    }

    public List<String> fetchDimensionAndMetricValues() {
        if (!FETCHED_DIMENSIONS_AND_METRICS.first().isVisible()) return Collections.singletonList(" ");
        return extractInnerTextFromLocator(FETCHED_DIMENSIONS_AND_METRICS);
    }

    public List<String> fetchAdvertiserName() {
        return extractInnerTextFromLocator(FETCHED_ADVERTISER_NAME);
    }

    public List<String> fetchCampaignName() {
        return extractInnerTextFromLocator(FETCHED_CAMPAIGN_NAME);
    }

    public List<String> fetchLineItemName() {
        return extractInnerTextFromLocator(FETCHED_LINE_ITEM_NAME);
    }

    public List<String> fetchTacticName() {
        return extractInnerTextFromLocator(FETCHED_TACTIC_NAME);
    }

    public List<String> fetchCreativeName() {
        return extractInnerTextFromLocator(FETCHED_CREATIVE_NAME);
    }

    public boolean isReportPeriodSelected(String buttonName) {
        for (int i = 0; i < REPORT_PERIOD_BUTTONS.count(); i++) {
            if (REPORT_PERIOD_BUTTONS.nth(i).innerText().contains(buttonName)
                    && REPORT_PERIOD_BUTTONS.nth(i).getAttribute("class").contains("active")) {
                return true;
            }
        }
        return false;
    }

    public boolean selectStartAndEndDate() {
        cachedDates = CommonUtils.generateStartAndEndDates();
        boolean startSelected = selectDate(START_DATE, cachedDates[0]);
        boolean endSelected = selectDate(END_DATE, cachedDates[1]);
        return startSelected && endSelected;
    }

    public boolean selectDate(Locator input, String date) {
        String[] parts = date.split("-");
        String dd = parts[0];
        String mm = parts[1];
        String yyyy = parts[2];
        try {
            input.click();
            while (!CALENDAR_TITLE.textContent().trim().equalsIgnoreCase(yyyy)) {
                CALENDAR_TITLE.click();
            }
            CommonUtils.selectCalendarData(CALENDAR_DATA, yyyy);
            CommonUtils.selectCalendarData(CALENDAR_DATA, mm);
            CALENDAR_VIEW.waitFor(new Locator.WaitForOptions().setTimeout(3000));
            Locator dayCells = CALENDAR_VIEW.locator("//td[normalize-space()='" + dd + "']");
            int count = dayCells.count();
            int day = Integer.parseInt(dd);
            Locator correctDay;
            if (day < 15) {
                correctDay = dayCells.first();
            } else if (day > 20) {
                correctDay = dayCells.last();
            } else {
                if (count == 3) {
                    correctDay = dayCells.nth(1);
                } else {
                    correctDay = dayCells.first();
                }
            }
            correctDay.click();
            waitUtility.waitForLocatorDetached(CALENDAR_VIEW);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean enterStartAndEndTime(String startTime, String endTime) {
        try {
            START_TIME.fill(startTime);
            END_TIME.fill(endTime);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String fetchStartTime() {
        if (START_TIME.isVisible()) return START_TIME.inputValue();
        return " ";
    }

    public String fetchEndTime() {
        if (END_TIME.isVisible()) return END_TIME.inputValue();
        return " ";
    }

    public boolean selectTimeZone(String timeZone) {
        try {
            TIME_ZONE.scrollIntoViewIfNeeded();
            TIME_ZONE.selectOption(new SelectOption().setLabel(timeZone));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String fetchTimeZone() {
        if (TIME_ZONE.isVisible())
            return TIME_ZONE.locator("option:checked").textContent().trim();
        return " ";
    }

    public String fetchDefaultReportFormat() {
        return REPORT_FORMAT_SELECTED_VALUE.innerText().trim();
    }

    public List<String> fetchReportFormatList() {
        List<String> reportFormatValues = new ArrayList<>();
        REPORT_FORMAT_SELECTED_VALUE.click();
        Locator visibleMenu = page.locator("div.menu.transition:not(.hidden)");
        waitUtility.waitForLocatorVisible(visibleMenu, 2000);
        Locator visibleItems = visibleMenu.locator("div.item");
        List<String> allTexts = visibleItems.allInnerTexts();
        for (String text : allTexts) {
            reportFormatValues.add(text.trim());
        }
        page.keyboard().press("Escape");
        return reportFormatValues;
    }

    public boolean isTextQualifierCheckboxAvailable() {
        return TEXT_QUALIFIER_CHECKBOX.isVisible();
    }

    public boolean isTextQualifierCheckboxChecked() {
        return TEXT_QUALIFIER_CHECKBOX.getAttribute("class").contains("checked");
    }

    public List<String> verifyButtonsDisabledBeforeLineItemSelection() {
        List<String> disabledButtons = new ArrayList<>();
        for (int i = 0; i < REPORT_PERIOD_BUTTONS.count(); i++) {
            if (REPORT_PERIOD_BUTTONS.nth(i).getAttribute("class").contains("inactive"))
                disabledButtons.add(REPORT_PERIOD_BUTTONS.nth(i).textContent());
        }
        return disabledButtons;
    }

    public void selectDimension(List<String> dimensionList) {
        DIMENSION_DROPDOWN.click();
        for (String dimension : dimensionList) {
            page.getByText(dimension).click();
        }
        page.keyboard().press("Escape");
    }

    public void selectMetrics(List<String> metricsList) {
        METRICS_DROPDOWN.click();
        for (String metric : metricsList) {
            page.getByText(metric).click();
        }
        page.keyboard().press("Escape");
    }

    public List<String> verifyButtonsEnabledAfterLineItemSelection() {
        List<String> enabledButtons = new ArrayList<>();
        for (int i = 0; i < REPORT_PERIOD_BUTTONS.count(); i++) {
            String disabledAttribute = REPORT_PERIOD_BUTTONS.nth(i).getAttribute("disabled");
            boolean isEnabled = (disabledAttribute == null || disabledAttribute.isEmpty());
            if (isEnabled) enabledButtons.add(REPORT_PERIOD_BUTTONS.nth(i).innerText());
        }
        return enabledButtons;
    }

    public void selectReportPeriodButton(String buttonName) {
        CommonUtils.selectAndClickElement(REPORT_PERIOD_BUTTONS, Collections.singletonList(buttonName));
    }

    public String selectValueFromDropdown() {
        int count = DROPDOWN_LIST.count();
        int randomIndex = (int) (Math.random() * count);
        DROPDOWN_LIST.nth(randomIndex).click();
        waitUtility.waitUntilSpinnerHidden();
        page.waitForTimeout(500);
        page.keyboard().press("Escape");
        return SELECTED_VALUES_FROM_DROPDOWN.innerText();
    }

    public String isFlightDetailsDisplayed() {
        try {
            waitUtility.waitForLocatorVisible(DEFAULT_FLIGHT_DETAILS);
            return DEFAULT_FLIGHT_DETAILS.innerText();
        } catch (Exception e) {
            return " ";
        }
    }

    public List<String> clickDimensionDropdownAndFetchValues() {
        DIMENSION_DROPDOWN.click();
        List<String> values = DIMENSION_DROPDOWN
                .locator("xpath=/following-sibling::div/div[@class='item']")
                .allInnerTexts();
        page.keyboard().press("Escape");
        return values;
    }

    public List<String> clickMetricDropdownAndFetchValues() {
        METRICS_DROPDOWN.click();
        List<String> values = METRICS_DROPDOWN
                .locator("xpath=/following-sibling::div/div[@class='item']")
                .allInnerTexts();
        page.keyboard().press("Escape");
        return values;
    }

    public void clickFileBreakdownType(String buttonType) {
        for (int i = 0; i < FILE_BREAKDOWN_TYPE.count(); i++) {
            if (FILE_BREAKDOWN_TYPE.nth(i).innerText().trim().contains(buttonType)) {
                FILE_BREAKDOWN_TYPE.nth(i).click();
                break;
            }
        }
    }

    public List<String> fetchAndSelectFlightDetails() {
        FLIGHT_DETAILS_DROPDOWN.click();
        waitUtility.waitForLocatorVisible(FLIGHT_DETAILS_DROPDOWN_VALUE.last());
        List<String> flightDetails = FLIGHT_DETAILS_DROPDOWN_VALUE.allInnerTexts();
        if (!FLIGHT_DETAILS_DROPDOWN_VALUE.all().isEmpty()) {
            FLIGHT_DETAILS_DROPDOWN_VALUE.first().click();
        }
        return flightDetails;
    }

    public void downloadScheduledReport() {
        waitUtility.waitForElementVisible("div.ui.dropdown.selection.sort-option-dropdown", 60000);
    }

    public String fetchFileNameFromUI() {
        return FILE_NAME.inputValue().trim();
    }

    public void clickSearchButton() {
        SEARCH_BUTTON.click();
        waitUtility.waitUntilPreLoaderHidden();
    }

    public boolean fetchDefaultDeliveryTab(String deliveryTab) {
        for (int i = 0; i < DELIVERY_METHOD.count(); i++) {
            if (DELIVERY_METHOD.nth(i).textContent().trim().equalsIgnoreCase(deliveryTab)
                    && DELIVERY_METHOD.nth(i).getAttribute("class").contains("active")) {
                return true;
            }
        }
        return false;
    }

    public List<String> fetchScheduleReportInputValue(String fieldName) {
        Locator locator;
        List<String> capturedValues = new ArrayList<>();
        if (fieldName.contains("Deliver to Users"))
            locator = page.locator(String.format(
                    "//div[contains(text(),'%s')]/following-sibling::div//div[contains(@class,'transition ui label')]//span",
                    fieldName));
        else
            locator = page.locator(String.format(
                    "//div[contains(text(),'%s')]/parent::div/following-sibling::div//div[contains(@class,'transition ui label')]//span",
                    fieldName));
        for (int i = 0; i < locator.count(); i++) {
            if (locator.nth(i).isVisible()) {
                locator.nth(i).scrollIntoViewIfNeeded();
                capturedValues.add(locator.nth(i).textContent().trim());
            }
        }
        return capturedValues;
    }

    public String fetchLoggedInUsername() {
        return LOGGED_IN_USERNAME.innerText().trim();
    }

    public void enterDataInScheduleReport(String fieldName, String newEmail) {
        Locator locator;
        if (fieldName.contains("Deliver to Users"))
            locator = page.locator(String.format("//div[contains(text(),'%s')]/following-sibling::div//i", fieldName));
        else
            locator = page.locator(
                    String.format("//div[contains(text(),'%s')]/parent::div/following-sibling::div//i", fieldName));
        locator.click();
        SCHEDULE_REPORT_USERNAME_LIST.locator("text=" + newEmail).click();
        page.keyboard().press("Escape");
    }

    public boolean isFileNameFieldAvailable() {
        return FILE_NAME.isVisible();
    }

    public boolean verifyReportPeriodRelatedFields(String option) {
        return switch (option) {
            case "Custom Dates" -> START_DATE.isVisible()
                    && END_DATE.isVisible()
                    && START_TIME.isVisible()
                    && END_TIME.isVisible()
                    && TIME_ZONE.isVisible()
                    && REPORT_TIMING_CHECKBOX.isVisible();
            case "Lifetime" -> TIME_ZONE.isVisible() && REPORT_TIMING_CHECKBOX.isVisible();
            case "Flights" -> FLIGHT_DETAILS_DROPDOWN.isVisible() && REPORT_TIMING_CHECKBOX.isVisible();
            default -> false;
        };
    }

    public List<String> fetchReportDetailsFromListingPage() {
        List<String> fetchReportDetails = new ArrayList<>();
        fetchReportDetails.add(CREATED_BY.first().textContent().trim());
        fetchReportDetails.add(
                REPORTING_PERIOD.first().textContent().split("—")[0].trim());
        fetchReportDetails.add(
                REPORT_NAME.first().textContent().split("from")[0].trim());
        return fetchReportDetails;
    }

    public String verifyEmailNotRemovable(String fieldName, String loggedInUser) {
        String usernameText = loggedInUser.split("\\(")[0];
        Locator locator = page.locator(String.format(
                "//div[contains(text(),'%s')]/parent::div/following-sibling::div//div[contains(@class,'transition ui label')]//span[contains(text(),'%s')]",
                fieldName, usernameText));
        locator.click();
        String tooltipText = TOOL_TIP.innerText().trim();
        page.keyboard().press("Escape");
        return tooltipText;
    }

    public boolean isReportFormatFieldAvailable() {
        return REPORT_FORMAT_SELECTED_VALUE.isVisible();
    }

    public boolean isAdvancedExportCheckboxAvailable() {
        return ADVANCED_EXPORT_CHECKBOX.isVisible();
    }

    public boolean isAdvancedExportCheckboxChecked() {
        return !TEXT_QUALIFIER_CHECKBOX.getAttribute("class").contains("checked");
    }

    public void clickAdvancedDeliverySettingLink() {
        ADVANCED_DELIVERY_SETTING_LINK.click();
    }

    public boolean isLineCodingFieldAvailable() {
        return LINE_CODING.isVisible();
    }

    public boolean checkDefaultLineCodingType(String defaultLineCodingType) {
        Locator locator = LINE_CODING_OPTIONS.locator("xpath=/label");
        for (int i = 0; i < LINE_CODING_OPTIONS.count(); i++) {
            if (locator.nth(i).innerText().contains(defaultLineCodingType)
                    && LINE_CODING_OPTIONS.nth(i).getAttribute("class").contains("checked")) {
                return true;
            }
        }
        return false;
    }

    public List<String> fetchLineCodingTypes() {
        return LINE_CODING_OPTIONS.locator("xpath=/label").allInnerTexts();
    }

    public void enterDestinationDetails(
            String destinationName,
            String destinationType,
            String host,
            String username,
            String password,
            String port,
            String serverPath) {
        DESTINATION_NAME.fill(destinationName);
        DESTINATION_TYPE.click();
        String destinationXpath = String.format("//sui-select-option//span[text()='%s']", destinationType);
        DESTINATION_TYPE.locator("xpath=" + destinationXpath).click();
        HOST.fill(host);
        USERNAME.fill(username);
        PASSWORD.fill(password);
        PORT.fill(port);
        SERVER_PATH.fill(serverPath);
    }

    public boolean isEditDestinationAvailable() {
        DESTINATION_DROPDOWN.click();
        return EDIT_DESTINATION_BUTTON.first().isVisible();
    }

    public void clickEditDestination() {
        EDIT_DESTINATION_BUTTON.first().click();
        waitUtility.waitForLocatorVisible(DESTINATION_NAME);
    }

    public void clickTestAccessButton() {
        TEST_ACCESS_BUTTON.click();
        waitUtility.waitForLocatorVisible(CUSTOM_DESTINATION_RUN_BUTTON);
        CUSTOM_DESTINATION_RUN_BUTTON.click();
    }

    public void clickCreateDestinationButton() {
        CUSTOM_DESTINATION_CREATE_BUTTON.click();
    }

    public boolean isDestinationNameAvailable() {
        return DESTINATION_NAME.isVisible() && !DESTINATION_NAME.inputValue().isEmpty();
    }

    public boolean isDestinationTypeAvailable() {
        return DESTINATION_TYPE.isVisible()
                && !DESTINATION_TYPE
                .locator("xpath=//div[@class='text']//span[2]")
                .textContent()
                .isEmpty();
    }

    public boolean isHostFieldAvailable() {
        return HOST.isVisible() && !HOST.inputValue().isEmpty();
    }

    public boolean isUsernameFieldAvailable() {
        return USERNAME.isVisible() && !USERNAME.inputValue().isEmpty();
    }

    public boolean isPasswordFieldAvailable() {
        return PASSWORD.isVisible() && !PASSWORD.inputValue().isEmpty();
    }

    public boolean isPortFieldAvailable() {
        return PORT.isVisible() && !PORT.inputValue().isEmpty();
    }

    public boolean isTestAccessButtonAvailable() {
        return TEST_ACCESS_BUTTON.isVisible();
    }

    public boolean isReRunAccessButtonAvailable() {
        return RE_RUN_ACCESS_BUTTON.isVisible();
    }

    public boolean isCreateButtonAvailable() {
        return CUSTOM_DESTINATION_CREATE_BUTTON.isVisible();
    }

    public boolean isCancelButtonAvailable() {
        return CUSTOM_DESTINATION_CANCEL_BUTTON.isVisible();
    }

    public String fetchFileNameHelpText() {
        FILE_NAME_HELP_TEXT.first().scrollIntoViewIfNeeded();
        return FILE_NAME_HELP_TEXT.first().textContent().trim();
    }

    public void clickReportOptions() {
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(GENERATED_REPORT_OPTIONS);
        GENERATED_REPORT_OPTIONS.click();
    }

    public void deleteReport() {
        waitUtility.waitForLocatorVisible(DELETE_REPORT_BUTTON);
        DELETE_REPORT_BUTTON.click();
        waitUtility.waitForLocatorVisible(DELETE_REPORT_CONFIRMATION_POPUP);
        DELETE_REPORT_REMOVE_BUTTON.click();
        waitUtility.waitForLocatorVisible(DELETE_REPORT_SUCCESS_ALERT);
        waitUtility.waitUntilSpinnerHidden();
    }

    public String fetchReportDeleteSuccessAlert() {
        String text = DELETE_REPORT_SUCCESS_ALERT.innerText().trim();
        waitUtility.waitForLocatorHidden(DELETE_REPORT_SUCCESS_ALERT);
        return text;
    }

    public String fetchNoReportFoundMessage(String reportName) {
        clearSearchField();
        searchReportName(reportName);
        return NO_REPORT_AVAILABLE_TEXT.innerText();
    }

    public void clearSearchField() {
        if (CLEAR_SEARCH_ICON.isVisible()) {
            CLEAR_SEARCH_ICON.click();
            waitUtility.waitUntilPreLoaderHidden();
        }
    }
}
