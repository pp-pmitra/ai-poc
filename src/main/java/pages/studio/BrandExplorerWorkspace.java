package pages.studio;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import utils.WaitUtility;

public class BrandExplorerWorkspace {

    private final Page page;
    private final FrameLocator WORKSPACE_FRAME;
    private final Locator BRAND_EXPLORER_CHART;
    private final Locator BRAND_EXPLORER_TABLE;
    private final Locator SAVE_WORKSPACE;
    private final Locator DATE_RANGE_SELECTOR;
    private final Locator DATE_RANGE_PICKER;
    private final Locator START_DATE_INPUT;
    private final Locator END_DATE_INPUT;
    private final Locator DATE_RANGE_ERROR;
    private final Locator DATE_CELLS;
    private final Locator SPINNER;
    WaitUtility waitUtility;

    public BrandExplorerWorkspace(Page page) {
        this.page = page;
        this.waitUtility = new WaitUtility(page);
        this.WORKSPACE_FRAME = page.frameLocator("iframe").frameLocator("iframe");
        this.BRAND_EXPLORER_CHART = WORKSPACE_FRAME.locator("//div[@class='recharts-responsive-container']");
        this.BRAND_EXPLORER_TABLE = WORKSPACE_FRAME.locator("//div[contains(@class,'Box')]//table");
        this.SAVE_WORKSPACE = WORKSPACE_FRAME.locator(
                "//button[contains(@data-tour-id,'save-workspace-button')]//div[contains(text(),'Save')]");
        this.DATE_RANGE_SELECTOR = WORKSPACE_FRAME.locator(
                "//p[normalize-space()='Time Frame']/following-sibling::div//input[starts-with(@id,'listbox-input-')]");
        this.DATE_RANGE_PICKER = WORKSPACE_FRAME.locator("[data-testid='date-range-picker']");
        this.START_DATE_INPUT = WORKSPACE_FRAME.locator("input[data-testid='date-from-text-input']");
        this.END_DATE_INPUT = WORKSPACE_FRAME.locator("input[data-testid='date-to-text-input']");
        this.DATE_RANGE_ERROR =
                WORKSPACE_FRAME.locator("//p[normalize-space()='Start date cannot be later than end date.']");
        this.DATE_CELLS =
                WORKSPACE_FRAME.locator("//div[contains(@class,'Box')]//table//tbody//tr//td[1][@aria-colindex]");
        this.SPINNER = WORKSPACE_FRAME.locator("//div[@data-testid='loading-spinner']");
    }

    public void waitForDashboardLoad() {
        waitUtility.waitForLocatorVisible(BRAND_EXPLORER_CHART);
        waitUtility.waitForLocatorVisible(BRAND_EXPLORER_TABLE);
    }

    public String getDefaultDimensions(String defaultDimension) {
        Locator locator = WORKSPACE_FRAME.locator(String.format(
                "//table//thead//th[@aria-selected='true']//p[normalize-space()='%s']", defaultDimension));
        waitUtility.waitForLocatorVisible(locator);
        return locator.innerText().trim();
    }

    public String getDefaultMetrics(String defaultMetric) {
        Locator locator = WORKSPACE_FRAME.locator(
                String.format("//table//thead//th[@aria-selected='true']//p[normalize-space()='%s']", defaultMetric));
        waitUtility.waitForLocatorVisible(locator);
        return locator.innerText().trim();
    }

    public String getDefaultTimeFrame() {
        Locator locator = WORKSPACE_FRAME.locator(
                "//p[normalize-space()='Time Frame']/following-sibling::div//input[starts-with(@id,'listbox-input-')]");
        waitUtility.waitForLocatorVisible(locator);
        return locator.inputValue().trim();
    }

    public void saveBrandExplorerWorkspace() {
        waitForDashboardLoad();
        SAVE_WORKSPACE.first().click();
    }

    public void clickTimeFrameSelector() {
        waitUtility.waitForLocatorVisible(DATE_RANGE_SELECTOR);
        DATE_RANGE_SELECTOR.click();
    }

    public List<String> getTimeFrameOptions() {
        Locator options = WORKSPACE_FRAME.locator("//div[@role='dialog']//li[@role='option']");
        waitUtility.waitForLocatorVisible(options.first());
        List<String> optionLabels = new ArrayList<>();
        int count = options.count();
        for (int i = 0; i < count; i++) {
            optionLabels.add(options.nth(i).innerText().trim());
        }
        return optionLabels;
    }

    public void selectTimeFramePreset(String timeFrame) {
        Locator option = WORKSPACE_FRAME.locator(
                String.format("//div[@role='dialog']//li[@role='option']/span[normalize-space()='%s']", timeFrame));
        waitUtility.waitForLocatorVisible(option);
        option.click();
        page.keyboard().press("Escape");
        waitForSpinnerToDisappear();
    }

    public String getSelectedTimeFrameAfterUpdate() {
        waitForDashboardLoad();
        return getDefaultTimeFrame();
    }

    public void waitForSpinnerToDisappear() {
        waitUtility.waitForLocatorHidden(SPINNER);
    }

    public List<String> getTableDates(int days) {
        waitUtility.waitForLocatorVisible(DATE_CELLS.first());
        Set<String> seenDates = new LinkedHashSet<>();
        String previousLastDate = "";
        while (seenDates.size() < days) {
            int visibleRowCount = DATE_CELLS.count();
            // Capture all currently visible dates
            for (int i = 0; i < visibleRowCount; i++) {
                String date = DATE_CELLS.nth(i).innerText().trim();
                if (!date.isEmpty()) {
                    seenDates.add(date);
                }
            }
            if (seenDates.size() >= days) {
                break;
            }
            String currentLastDate = DATE_CELLS.last().innerText().trim();
            // Hover over table before scrolling
            DATE_CELLS.last().hover();
            // Scroll down
            page.mouse().wheel(0, 75);
            // Wait for virtualized rows to refresh
            page.waitForTimeout(1000);
            String newLastDate = DATE_CELLS.last().innerText().trim();
            // No new data loaded
            if (currentLastDate.equals(newLastDate) || currentLastDate.equals(previousLastDate)) {
                break;
            }
            previousLastDate = currentLastDate;
        }
        return seenDates.stream().limit(days).collect(Collectors.toList());
    }

    public boolean isDateRangePickerDisplayed() {
        return DATE_RANGE_PICKER.isVisible() && START_DATE_INPUT.isVisible() && END_DATE_INPUT.isVisible();
    }

    public boolean areDateFieldsConfigurable() {
        return START_DATE_INPUT.isEnabled() && END_DATE_INPUT.isEnabled();
    }

    public void setCustomDateRange(String startDate, String endDate) {
        String startFormatted = toDisplayFormat(startDate);
        String endFormatted = toDisplayFormat(endDate);
        START_DATE_INPUT.click(new Locator.ClickOptions().setClickCount(3));
        START_DATE_INPUT.fill(startFormatted);
        page.keyboard().press("Tab");
        END_DATE_INPUT.click(new Locator.ClickOptions().setClickCount(3));
        END_DATE_INPUT.fill(endFormatted);
        page.keyboard().press("Tab");
    }

    public void waitForStartDateInTable(String startDate) {
        // Wait until the table actually reflects the new start date, not just that containers are visible
        Locator startDateCell = WORKSPACE_FRAME.locator(String.format(
                "//div[contains(@class,'Box')]//table//tbody//tr//td[1]//p[normalize-space()='%s']", startDate));
        waitUtility.waitForLocatorVisible(startDateCell);
    }

    public boolean isDateRangeErrorDisplayed() {
        // in case the error message is not displayed, waitForLocatorVisible will throw a TimeoutError, which we catch
        // and return false
        // instead of timing out the test as that would be a regression failure.
        try {
            waitUtility.waitForLocatorVisible(DATE_RANGE_ERROR);
            return true;
        } catch (TimeoutError e) {
            return false;
        }
    }

    public boolean isStartDateFirstInTable(String startDate) {
        waitUtility.waitForLocatorVisible(DATE_CELLS.first());
        return DATE_CELLS.first().innerText().trim().equals(startDate);
    }

    public boolean isEndDateLastInTable(String endDate) {
        // Scroll to the bottom to ensure all rows are rendered
        String previousLastDate = "";
        for (int i = 0; i < 30; i++) {
            String currentLastDate = DATE_CELLS.last().innerText().trim();
            if (currentLastDate.equals(previousLastDate)) {
                break;
            }
            DATE_CELLS.last().hover();
            page.mouse().wheel(0, 75);
            page.waitForTimeout(500);
            previousLastDate = currentLastDate;
        }
        return DATE_CELLS.last().innerText().trim().equals(endDate);
    }

    // Converts YYYY-MM-DD to MM/DD/YYYY for the date input fields
    private String toDisplayFormat(String isoDate) {
        String[] parts = isoDate.split("-");
        return parts[1] + "/" + parts[2] + "/" + parts[0];
    }
}
