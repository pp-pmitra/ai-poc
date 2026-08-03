package pages.studio;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
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
    private final Locator FILTERS_TAB;
    private final Locator ADD_FILTER_BUTTON;
    private final Locator COMPONENTS_TAB;
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
                "//ds-typography[normalize-space()='Time Frame']/following::input[starts-with(@id,'listbox-input-')][1]");
        this.DATE_RANGE_PICKER = WORKSPACE_FRAME.locator("[data-testid='date-range-picker']");
        this.START_DATE_INPUT = WORKSPACE_FRAME.locator("input[data-testid='date-from-text-input']");
        this.END_DATE_INPUT = WORKSPACE_FRAME.locator("input[data-testid='date-to-text-input']");
        this.DATE_RANGE_ERROR =
                WORKSPACE_FRAME.locator("//p[normalize-space()='Start date cannot be later than end date.']");
        this.DATE_CELLS =
                WORKSPACE_FRAME.locator("//div[contains(@class,'Box')]//table//tbody//tr//td[1][@aria-colindex]");
        this.SPINNER = WORKSPACE_FRAME.locator("//div[@data-testid='loading-spinner']");
        this.FILTERS_TAB = WORKSPACE_FRAME.locator("//div[normalize-space(text())='Filters']");
        this.ADD_FILTER_BUTTON =
                WORKSPACE_FRAME.locator("//div[normalize-space(text())='Add Filters']");
        this.COMPONENTS_TAB = WORKSPACE_FRAME.locator("//div[normalize-space(text())='Components']");
    }

    public void waitForDashboardLoad() {
        waitUtility.waitForLocatorVisible(BRAND_EXPLORER_CHART);
        waitUtility.waitForLocatorVisible(BRAND_EXPLORER_TABLE);
    }

    public String getDefaultDimensions(String defaultDimension) {
        Locator locator = WORKSPACE_FRAME.locator(String.format(
                "//table//thead//th[@aria-selected='true' and normalize-space()='%s']", defaultDimension));
        waitUtility.waitForLocatorVisible(locator);
        return locator.innerText().trim();
    }

    public String getDefaultMetrics(String defaultMetric) {
        Locator locator = WORKSPACE_FRAME.locator(
                String.format("//table//thead//th[@aria-selected='true' and normalize-space()='%s']", defaultMetric));
        waitUtility.waitForLocatorVisible(locator);
        return locator.innerText().trim();
    }

    public String getDefaultTimeFrame() {
        waitUtility.waitForLocatorVisible(DATE_RANGE_SELECTOR);
        return DATE_RANGE_SELECTOR.inputValue().trim();
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

    public void waitForSpinnerToAppear() {
        waitUtility.waitForLocatorVisible(SPINNER);
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

    private Locator categoryTab(String category) {
        return WORKSPACE_FRAME.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName(category));
    }

    private Locator categoryCheckbox(String category, String field) {
        return WORKSPACE_FRAME
                .getByRole(AriaRole.REGION, new FrameLocator.GetByRoleOptions().setName(category))
                .getByRole(AriaRole.CHECKBOX, new Locator.GetByRoleOptions().setName(field).setExact(true));
    }

    private Locator tableColumnHeader(String columnName) {
        return WORKSPACE_FRAME.locator(String.format("//th//p[text()='%s']", columnName));
    }

    public List<String> getMissingComponentCategories(List<String> categories) {
        List<String> missing = new ArrayList<>();
        for (String category : categories) {
            try {
                waitUtility.waitForLocatorVisible(categoryTab(category));
            } catch (TimeoutError e) {
                missing.add(category);
            }
        }
        return missing;
    }

    // Component checkboxes only render once their category accordion is expanded.
    private void expandCategory(String category) {
        Locator categoryTab = categoryTab(category);
        waitUtility.waitForLocatorVisible(categoryTab);
        if (!"true".equals(categoryTab.getAttribute("aria-expanded"))) {
            categoryTab.click();
        }
    }

    public void selectComponent(String category, String component) {
        expandCategory(category);
        Locator checkbox = categoryCheckbox(category, component);
        waitUtility.waitForLocatorVisible(checkbox);
        checkbox.check();
        waitForSpinnerToAppear();
        waitForSpinnerToDisappear();
        clickColumnHeader(component);
    }

    public void clickColumnHeader(String columnName) {
        Locator header = tableColumnHeader(columnName);
        waitUtility.waitForLocatorVisible(header);
        header.click();
        waitForSpinnerToDisappear();
    }

    public boolean isChartVisible() {
        try {
            waitUtility.waitForLocatorVisible(BRAND_EXPLORER_CHART);
            return true;
        } catch (TimeoutError e) {
            return false;
        }
    }

    public boolean isComponentVisibleAsTableColumn(String component) {
        try {
            waitUtility.waitForLocatorVisible(tableColumnHeader(component));
            return true;
        } catch (TimeoutError e) {
            return false;
        }
    }

    public void deselectComponent(String category, String component) {
        expandCategory(category);
        Locator checkbox = categoryCheckbox(category, component);
        waitUtility.waitForLocatorVisible(checkbox);
        checkbox.uncheck();
    }

    // Removes the default dimension (Day) and metric (Identified NPIs) to start from an empty table.
    public void removeDefaultDimensionAndMetric() {
        deselectComponent("Time Frame", "Day");
        deselectComponent("NPI Events", "Identified NPIs");
    }

    // Selects then deselects every component in the category, verifying each appears/disappears as a table column.
    public List<String> verifyComponentsSelectAndRemove(String category, List<String> components) {
        expandCategory(category);
        List<String> failures = new ArrayList<>();
        components.forEach(component -> categoryCheckbox(category, component).check());
        waitForSpinnerToAppear();
        waitForSpinnerToDisappear();
        for (String component : components) {
            try {
                waitUtility.waitForLocatorVisible(tableColumnHeader(component));
            } catch (TimeoutError e) {
                failures.add(component + ": did not appear as a table column after being selected");
            }
        }
        components.forEach(component -> categoryCheckbox(category, component).uncheck());
        for (String component : components) {
            try {
                waitUtility.waitForLocatorHidden(tableColumnHeader(component));
            } catch (TimeoutError e) {
                failures.add(component + ": was not removed from the table after being deselected");
            }
        }
        return failures;
    }

    public void clickFiltersTab() {
        waitUtility.waitForLocatorVisible(FILTERS_TAB.first());
        FILTERS_TAB.first().click();
    }

    public void clickComponentsTab() {
        waitUtility.waitForLocatorVisible(COMPONENTS_TAB.first());
        COMPONENTS_TAB.first().click();
    }

    public void clickAddFilter() {
        waitUtility.waitForLocatorVisible(ADD_FILTER_BUTTON.first());
        ADD_FILTER_BUTTON.first().click();
    }

    // Filter fields live in the same accordion categories as dimensions/metrics, inside the "Select Filter" modal.
    public void selectFilterField(String category, String field) {
        expandCategory(category);
        Locator checkbox = categoryCheckbox(category, field);
        waitUtility.waitForLocatorVisible(checkbox);
        checkbox.check();
    }

    public void closeFilterDialog() {
        page.keyboard().press("Escape");
    }

    private Locator filterFieldCard(String field) {
        return WORKSPACE_FRAME
                .locator(String.format("//p[normalize-space()='%s']/ancestor::div[2]", field))
                .first();
    }

    // Options have no accessible checkbox name, so match by OPTION role/text instead; must click the
    // exact option since Enter would select every option still matching the typed search text.
    public void enterFilterValue(String field, String value) {
        Locator input = filterFieldCard(field).locator("input:not([readonly])").first();
        waitUtility.waitForLocatorVisible(input);
        input.click();
        input.fill(value);
        Locator option = WORKSPACE_FRAME.getByRole(
                AriaRole.OPTION, new FrameLocator.GetByRoleOptions().setName(value).setExact(true));
        waitUtility.waitForLocatorVisible(option.first());
        option.first().click();
        page.keyboard().press("Escape");
        waitForSpinnerToDisappear();
    }

    // The applied value renders asynchronously, so wait for its text node rather than a generic loading signal.
    public String getAppliedFilterSummary(String field, String expectedValue) {
        Locator card = filterFieldCard(field);
        waitUtility.waitForLocatorVisible(card);
        waitUtility.waitForLocatorVisible(
                card.getByText(expectedValue, new Locator.GetByTextOptions().setExact(true)).first());
        return card.innerText().replaceAll("\\s+", " ").trim();
    }

    private Locator tableColumnCells(String columnName) {
        Locator header = tableColumnHeader(columnName).locator("xpath=ancestor::th[1]");
        waitUtility.waitForLocatorVisible(header);
        String colIndex = header.getAttribute("aria-colindex");
        return WORKSPACE_FRAME.locator(
                String.format("//div[contains(@class,'Box')]//table//tbody//tr//td[@aria-colindex='%s']", colIndex));
    }

    public List<String> getTableColumnValues(String columnName) {
        Locator cells = tableColumnCells(columnName);
        waitUtility.waitForLocatorVisible(cells.first());
        return cells.allInnerTexts().stream().map(String::trim).collect(Collectors.toList());
    }
}
