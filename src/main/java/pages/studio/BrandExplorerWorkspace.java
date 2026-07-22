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
                "//p[normalize-space()='Time Frame']/following-sibling::div//input[starts-with(@id,'listbox-input-')]");
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

    // Some actions (e.g. applying a filter) trigger more than one loading spinner at once - one per
    // panel that reloads (Filters tabpanel, chart/table container, etc.) - so each matched spinner is
    // waited on individually via nth() rather than through the bare (multi-match) SPINNER locator,
    // which throws a Playwright strict-mode violation as soon as more than one is present.
    public void waitForSpinnerToDisappear() {
        int count = SPINNER.count();
        for (int i = 0; i < count; i++) {
            waitUtility.waitForLocatorHidden(SPINNER.nth(i));
        }
    }

    public void waitForSpinnerToAppear() {
        waitUtility.waitForLocatorVisible(SPINNER.first());
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

    // Both Dimensions and Metrics are organized as accordion categories containing checkboxes,
    // so these locators and the methods below serve either component type.
    private Locator componentCategoryTab(String category) {
        return WORKSPACE_FRAME.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName(category));
    }

    // Scoped to the category's own accordion region: some component labels (e.g. "Avg. Video Progress")
    // repeat across multiple metric categories, and accordions don't auto-collapse siblings, so an
    // unscoped lookup can match more than one checkbox once several categories are expanded.
    private Locator componentCheckbox(String category, String component) {
        return WORKSPACE_FRAME
                .getByRole(AriaRole.REGION, new FrameLocator.GetByRoleOptions().setName(category))
                .getByRole(AriaRole.CHECKBOX, new Locator.GetByRoleOptions().setName(component).setExact(true));
    }

    private Locator tableColumnHeader(String columnName) {
        return WORKSPACE_FRAME.locator(String.format("//th//p[text()='%s']", columnName));
    }

    public List<String> getMissingComponentCategories(List<String> categories) {
        List<String> missing = new ArrayList<>();
        for (String category : categories) {
            try {
                waitUtility.waitForLocatorVisible(componentCategoryTab(category));
            } catch (TimeoutError e) {
                missing.add(category);
            }
        }
        return missing;
    }

    // Component checkboxes only render once their category accordion is expanded.
    private void expandComponentCategory(String category) {
        Locator categoryTab = componentCategoryTab(category);
        waitUtility.waitForLocatorVisible(categoryTab);
        if (!"true".equals(categoryTab.getAttribute("aria-expanded"))) {
            categoryTab.click();
        }
    }

    public void selectComponent(String category, String component) {
        expandComponentCategory(category);
        Locator checkbox = componentCheckbox(category, component);
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
        expandComponentCategory(category);
        Locator checkbox = componentCheckbox(category, component);
        waitUtility.waitForLocatorVisible(checkbox);
        checkbox.uncheck();
    }

    // Removes the workspace's default dimension (Day, under Time Frame) and default metric
    // (Identified NPIs, under NPI Events) so later selections can be verified against an empty table.
    public void removeDefaultDimensionAndMetric() {
        deselectComponent("Time Frame", "Day");
        deselectComponent("NPI Events", "Identified NPIs");
    }

    // Selects every item in the category (dimension or metric), verifies each renders as a table
    // column, then deselects all of them and verifies the columns disappear - proves both the
    // component list and the select/remove behavior for the whole category in one pass. Returns a
    // human-readable failure per component that didn't behave as expected, empty if all passed.
    public List<String> verifyComponentsSelectAndRemove(String category, List<String> components) {
        expandComponentCategory(category);
        List<String> failures = new ArrayList<>();
        components.forEach(component -> componentCheckbox(category, component).check());
        waitForSpinnerToAppear();
        waitForSpinnerToDisappear();
        for (String component : components) {
            try {
                waitUtility.waitForLocatorVisible(tableColumnHeader(component));
            } catch (TimeoutError e) {
                failures.add(component + ": did not appear as a table column after being selected");
            }
        }
        components.forEach(component -> componentCheckbox(category, component).uncheck());
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

    // Filter fields are organized into the same accordion categories as dimensions/metrics,
    // inside the "Select Filter" modal opened by clickAddFilter().
    private Locator filterCategoryButton(String category) {
        return WORKSPACE_FRAME.getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName(category));
    }

    private Locator filterFieldCheckbox(String category, String field) {
        return WORKSPACE_FRAME
                .getByRole(AriaRole.REGION, new FrameLocator.GetByRoleOptions().setName(category))
                .getByRole(AriaRole.CHECKBOX, new Locator.GetByRoleOptions().setName(field).setExact(true));
    }

    public void selectFilterField(String category, String field) {
        Locator categoryButton = filterCategoryButton(category);
        waitUtility.waitForLocatorVisible(categoryButton);
        if (!"true".equals(categoryButton.getAttribute("aria-expanded"))) {
            categoryButton.click();
        }
        Locator checkbox = filterFieldCheckbox(category, field);
        waitUtility.waitForLocatorVisible(checkbox);
        checkbox.check();
    }

    public void closeFilterDialog() {
        page.keyboard().press("Escape");
    }

    // Scoped to the field's own card in the "Data Filters" panel, since the field name can be
    // echoed elsewhere (e.g. an accessibility live region) once a value is applied.
    private Locator filterFieldCard(String field) {
        return WORKSPACE_FRAME
                .locator(String.format("//p[normalize-space()='%s']/ancestor::div[2]", field))
                .first();
    }

    // The value control is a searchable multi-select list: each option is a div[role='option']
    // whose checkbox has no accessible name (its <label> is empty; the visible text lives in a
    // sibling <p>), so the option's own accessible name - built from that sibling text - is what
    // getByRole(OPTION) must match, not getByRole(CHECKBOX). Typing narrows the options, and the
    // exact-match option must be clicked directly - pressing Enter instead selects every option
    // still matching the search text, not just the intended one.
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

    // The applied value renders asynchronously after the field's checkbox list loads, so the card's
    // text reads as just "<field> is" for a brief window - most noticeable right after reopening a
    // saved workspace, where the value has to round-trip from the server before it appears. Wait for
    // the expected value's own text node rather than a generic "loading done" signal, since no
    // reliable loading-skeleton element exists in this card's DOM.
    public String getAppliedFilterSummary(String field, String expectedValue) {
        Locator card = filterFieldCard(field);
        waitUtility.waitForLocatorVisible(card);
        waitUtility.waitForLocatorVisible(
                card.getByText(expectedValue, new Locator.GetByTextOptions().setExact(true)).first());
        return card.innerText().replaceAll("\\s+", " ").trim();
    }

    // Every cell in the table body carries the same aria-colindex as its header, so the header's
    // index is what scopes the read to this one column instead of every cell in the row.
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
        int count = cells.count();
        List<String> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            values.add(cells.nth(i).innerText().trim());
        }
        return values;
    }
}
