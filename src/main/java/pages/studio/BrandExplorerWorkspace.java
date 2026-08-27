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
    private final Locator TIMEFRAME;
    private final Locator DATE_RANGE_PICKER;
    private final Locator START_DATE_INPUT;
    private final Locator END_DATE_INPUT;
    private final Locator DATE_RANGE_ERROR;
    private final Locator DATE_CELLS;
    private final Locator SPINNER;
    private final Locator FILTERS_TAB;
    private final Locator ADD_FILTER_BUTTON;
    private final Locator COMPONENTS_TAB;
    private final Locator CHART_EMPTY_STATE;
    private final Locator CLEAR_ALL_BUTTON;
    private final Locator NO_COMPONENTS_SELECTED_TAB;
    private final Locator WORKSPACE_LEFT_RAIL;
    WaitUtility waitUtility;

    public BrandExplorerWorkspace(Page page) {
        this.page = page;
        this.waitUtility = new WaitUtility(page);
        this.WORKSPACE_FRAME = page.frameLocator("iframe").frameLocator("iframe");
        this.BRAND_EXPLORER_CHART = WORKSPACE_FRAME.locator("//div[@class='recharts-responsive-container']");
        this.BRAND_EXPLORER_TABLE = WORKSPACE_FRAME.locator("//div[contains(@class,'Box')]//table");
        this.SAVE_WORKSPACE = WORKSPACE_FRAME.locator(
                "//button[contains(@data-tour-id,'save-workspace-button')]//div[contains(text(),'Save')]");
        this.TIMEFRAME = WORKSPACE_FRAME.locator(
                "//ds-typography[normalize-space()='Time Frame']/following-sibling::div//input[starts-with(@id,'listbox-input-')]");
        this.DATE_RANGE_PICKER = WORKSPACE_FRAME.locator("[data-testid='date-range-picker']");
        this.START_DATE_INPUT = WORKSPACE_FRAME.locator("input[data-testid='date-from-text-input']");
        this.END_DATE_INPUT = WORKSPACE_FRAME.locator("input[data-testid='date-to-text-input']");
        this.DATE_RANGE_ERROR =
                WORKSPACE_FRAME.locator("//p[normalize-space()='Start date cannot be later than end date.']");
        this.DATE_CELLS =
                WORKSPACE_FRAME.locator("//div[contains(@class,'Box')]//table//tbody//tr//td[1][@aria-colindex]");
        this.SPINNER = WORKSPACE_FRAME.locator("//div[@data-testid='loading-spinner']");
        this.FILTERS_TAB = WORKSPACE_FRAME.getByRole(
                                AriaRole.TAB,
                                new FrameLocator.GetByRoleOptions().setName("Filters").setExact(true)
                            );
        this.ADD_FILTER_BUTTON = WORKSPACE_FRAME.getByRole(
                                        AriaRole.BUTTON,
                                        new FrameLocator.GetByRoleOptions().setName("Add Filters").setExact(true)
                                );
        this.COMPONENTS_TAB = WORKSPACE_FRAME.getByRole(
                                AriaRole.TAB,
                                new FrameLocator.GetByRoleOptions().setName("Components").setExact(true)
                            );
        this.CHART_EMPTY_STATE = WORKSPACE_FRAME.locator(
                "//ds-typography[normalize-space()='Chart requires at least 1 dimension and 1 metric' or normalize-space()='Choose 1 or more fields to see analytics']");
        this.CLEAR_ALL_BUTTON = WORKSPACE_FRAME.getByRole(
                AriaRole.BUTTON,
                new FrameLocator.GetByRoleOptions().setName("Clear All").setExact(true)
        );
        this.NO_COMPONENTS_SELECTED_TAB = WORKSPACE_FRAME.getByRole(
                AriaRole.TAB,
                new FrameLocator.GetByRoleOptions().setName("Selected (0)").setExact(true)
        );
        this.WORKSPACE_LEFT_RAIL = WORKSPACE_FRAME.locator("//div[@data-tour-id='workspace-left-rail']");
    }

    public void waitForDashboardLoad() {
        waitUtility.waitForLocatorVisible(BRAND_EXPLORER_CHART);
        waitUtility.waitForLocatorVisible(BRAND_EXPLORER_TABLE);
    }

    public String getDefaultDimensions(String defaultDimension) {
        return getSelectedTableHeader(defaultDimension);
    }

    public String getDefaultMetrics(String defaultMetric) {
        return getSelectedTableHeader(defaultMetric);
    }

    private String getSelectedTableHeader(String headerName) {
        Locator locator = selectedTableColumnHeader(headerName);
        waitUtility.waitForLocatorVisible(locator);
        return locator.innerText().trim();
    }

    private Locator selectedTableColumnHeader(String headerName) {
        return WORKSPACE_FRAME.locator(
                String.format("//table//thead//th[@aria-selected='true' and normalize-space()='%s']", headerName));
    }

    public String getDefaultTimeFrame() {
        waitUtility.waitForLocatorVisible(TIMEFRAME);
        return TIMEFRAME.inputValue().trim();
    }

    public void saveBrandExplorerWorkspace() {
        waitForDashboardLoad();
        SAVE_WORKSPACE.first().click();
    }

    public void clickTimeFrameSelector() {
        waitUtility.waitForLocatorVisible(TIMEFRAME);
        TIMEFRAME.click();
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
        page.waitForCondition(() -> {
            for (int i = 0; i < SPINNER.count(); i++) {
                if (SPINNER.nth(i).isVisible()) {
                    return false;
                }
            }
            return true;
        });
    }

    public void waitForSpinnerToAppear() {
        waitUtility.waitForLocatorVisible(SPINNER);
    }

    private boolean isVisible(Locator locator) {
        try {
            waitUtility.waitForLocatorVisible(locator);
            return true;
        } catch (TimeoutError e) {
            return false;
        }
    }

    private boolean isHidden(Locator locator) {
        try {
            waitUtility.waitForLocatorHidden(locator);
            return true;
        } catch (TimeoutError e) {
            return false;
        }
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
                "//div[contains(@class,'Box')]//table//tbody//tr//td[1]//ds-typography[normalize-space()='%s']", startDate));
        waitUtility.waitForLocatorVisible(startDateCell);
    }

    public boolean isDateRangeErrorDisplayed() {
        return isVisible(DATE_RANGE_ERROR);
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

    private Locator componentCheckbox(String component) {
        return WORKSPACE_FRAME.getByRole(
                AriaRole.CHECKBOX, new FrameLocator.GetByRoleOptions().setName(component).setExact(true));
    }

    private Locator tableColumnHeader(String columnName) {
        return WORKSPACE_FRAME.locator(String.format("//th//ds-typography[text()='%s']", columnName));
    }

    private Locator chartToggleButton(String label) {
        return WORKSPACE_FRAME.getByRole(
                AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName(label).setExact(true));
    }

    public List<String> getMissingComponentCategories(List<String> categories) {
        List<String> missing = new ArrayList<>();
        for (String category : categories) {
            if (!isVisible(categoryTab(category))) {
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
        if (!checkbox.isChecked()) {
            checkbox.check();
            waitForSpinnerToAppear();
            waitForSpinnerToDisappear();
        }
        waitUtility.waitForLocatorVisible(tableColumnHeader(component));
        clickColumnHeader(component);
    }

    public void clickColumnHeader(String columnName) {
        Locator header = tableColumnHeader(columnName);
        waitUtility.waitForLocatorVisible(header);
        header.click();
        waitForSpinnerToDisappear();
    }

    public boolean isChartVisible() {
        return isVisible(BRAND_EXPLORER_CHART);
    }

    public boolean isChartHidden() {
        return isHidden(BRAND_EXPLORER_CHART);
    }

    public boolean isTableVisible() {
        return isVisible(BRAND_EXPLORER_TABLE);
    }

    public void clickClearAllComponents() {
        waitUtility.waitForLocatorVisible(CLEAR_ALL_BUTTON);
        CLEAR_ALL_BUTTON.click();
        waitUtility.waitForLocatorVisible(NO_COMPONENTS_SELECTED_TAB);
    }

    public boolean areNoComponentsSelected() {
        return isVisible(NO_COMPONENTS_SELECTED_TAB);
    }

    public void clickChartToggle(String label) {
        Locator toggle = chartToggleButton(label);
        waitUtility.waitForLocatorVisible(toggle);
        toggle.click();
    }

    public boolean isChartEmptyStateDisplayed() {
        try {
            waitUtility.waitForLocatorVisible(CHART_EMPTY_STATE);
            return true;
        } catch (TimeoutError e) {
            return false;
        }
    }

    public boolean isComponentVisibleAsTableColumn(String component) {
        return isVisible(tableColumnHeader(component));
    }

    public void removeTableColumnFromHeader(String columnName) {
        Locator header = tableColumnHeader(columnName).locator("xpath=ancestor::th[1]");
        waitUtility.waitForLocatorVisible(header);
        header.hover();
        header.locator("button").last().click(new Locator.ClickOptions().setForce(true));
        waitUtility.waitForLocatorHidden(tableColumnHeader(columnName));
        waitForSpinnerToAppear();
        waitForSpinnerToDisappear();
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
        addMissingSelectedColumns(components, failures);
        components.forEach(component -> categoryCheckbox(category, component).uncheck());
        addRemainingDeselectedColumns(components, failures);
        return failures;
    }

    public List<String> verifyStandaloneComponentsSelectAndRemove(List<String> components) {
        List<String> failures = new ArrayList<>();
        components.forEach(component -> componentCheckbox(component).check());
        waitForSpinnerToAppear();
        waitForSpinnerToDisappear();
        addMissingSelectedColumns(components, failures);
        components.forEach(component -> componentCheckbox(component).uncheck());
        addRemainingDeselectedColumns(components, failures);
        return failures;
    }

    private void addMissingSelectedColumns(List<String> components, List<String> failures) {
        for (String component : components) {
            if (!isVisible(tableColumnHeader(component))) {
                failures.add(component + ": did not appear as a table column after being selected");
            }
        }
    }

    private void addRemainingDeselectedColumns(List<String> components, List<String> failures) {
        for (String component : components) {
            if (!isHidden(tableColumnHeader(component))) {
                failures.add(component + ": was not removed from the table after being deselected");
            }
        }
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
                .locator(String.format("//ds-typography[normalize-space()='%s']/ancestor::div[2]", field))
                .first();
    }

    private Locator filterOperatorTrigger(String field) {
        return filterFieldCard(field).locator("xpath=.//button[@aria-expanded='false']");
    }

    public void selectFilterOperator(String field, String operator) {
        Locator trigger = filterOperatorTrigger(field);
        waitUtility.waitForLocatorVisible(trigger);
        trigger.click();
        Locator option = WORKSPACE_FRAME.getByRole(
                AriaRole.MENUITEM, new FrameLocator.GetByRoleOptions().setName(operator).setExact(true));
        waitUtility.waitForLocatorVisible(option);
        option.click();
        waitForSpinnerToDisappear();
    }

    public void enterFilterValue(String field, String value) {
        enterFilterValue(field, value, true);
    }

    public void enterFilterTextValue(String field, String value) {
        enterFilterValue(field, value, false);
    }

    private void enterFilterValue(String field, String value, boolean selectDropdownOption) {
        Locator input = filterFieldCard(field).locator("input[placeholder]");
        waitUtility.waitForLocatorVisible(input);
        input.click();
        input.fill(value);
        if(selectDropdownOption) {
            Locator option = WORKSPACE_FRAME.getByRole(
                    AriaRole.OPTION, new FrameLocator.GetByRoleOptions().setName(value).setExact(true));
            waitUtility.waitForLocatorVisible(option);
            option.click();
        }
        WORKSPACE_LEFT_RAIL.click(); // Click outside the input to trigger the filter update
        waitForSpinnerToAppear();
        waitForSpinnerToDisappear();
    }

    public void enterRangeFilterValues(String field, String from, String to) {
        Locator inputs = filterFieldCard(field).locator("input:not([readonly])");
        Locator startInput = inputs.nth(0);
        Locator endInput = inputs.nth(1);
        waitUtility.waitForLocatorVisible(startInput);
        startInput.fill(from);
        endInput.fill(to);
        WORKSPACE_LEFT_RAIL.click(); // Click outside the input to trigger the filter update
        waitForSpinnerToAppear();
        waitForSpinnerToDisappear();
    }

    public String getFilterCardSummary(String field) {
        return getFilterCardSummary(field, null);
    }

    public String getExpectedTypedFilterDisplayValue(String operator, String value) {
        return switch (operator.toLowerCase()) {
            case "contains" -> "%" + value + "%";
            case "doesn't contain" -> "-%" + value + "%";
            case "starts with" -> value + "%";
            case "doesn't start with" -> "-" + value + "%";
            case "ends with" -> "%" + value;
            case "doesn't end with" -> "-%" + value;
            default -> value;
        };
    }

    public String getAppliedTypedFilterSummary(String field, String operator, String value) {
        String expectedDisplayValue = getExpectedTypedFilterDisplayValue(operator, value);
        Locator chipText = WORKSPACE_FRAME.locator("ds-typography.truncate")
                .filter(new Locator.FilterOptions().setHasText(field))
                .filter(new Locator.FilterOptions().setHasText(expectedDisplayValue));
        waitUtility.waitForLocatorVisible(chipText);
        return chipText.innerText().replaceAll("\\s+", " ").trim();
    }

    // The applied value renders asynchronously, so wait for its text node rather than a generic loading signal.
    public String getAppliedFilterSummary(String field, String expectedValue) {
        return getFilterCardSummary(field, expectedValue);
    }

    private String getFilterCardSummary(String field, String expectedValue) {
        Locator card = filterFieldCard(field);
        waitUtility.waitForLocatorVisible(card);
        if (expectedValue != null) {
            waitUtility.waitForLocatorVisible(
                    card.getByText(expectedValue, new Locator.GetByTextOptions().setExact(true)).first());
        }
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

    public boolean deselectComponent(String component) {
        Locator tableColumn = tableColumnHeader(component).locator("xpath=.//ancestor::th");
        waitUtility.waitForLocatorVisible(tableColumn);
        if (tableColumn.getAttribute("aria-selected").equals("true")) {
            tableColumn.click();
        }
        return tableColumn.getAttribute("aria-selected").equals("false");
    }
}
