package pages.life;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import factory.DriverFactory;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import utils.ExcelActions;
import utils.WaitUtility;

public class ReportTemplates {
    private final Page page;
    private final Locator REPORT_TEMPLATE_LINK;
    private final Locator TEMPLATES_TAB;
    private final Locator GENERATED_REPORTS_TAB;
    private final Locator SCHEDULING_TAB;
    private final Locator NEW_TEMPLATE;
    private final Locator REPORT_DIMENSIONS;
    private final Locator REPORT_METRICS;
    private final Locator TEMPLATE_NAME;
    private final Locator SEARCH_DIMENSION;
    private final Locator SELECT_DIMENSION;
    private final Locator SEARCH_METRIC;
    private final Locator SELECT_METRIC;
    private final Locator VERIFY_DIMENSION;
    private final Locator VERIFY_METRIC;
    private final Locator SAVE_TEMPLATE;
    private final Locator TEMPLATE_SUCCESS;
    private final Locator SEARCH_TEMPLATE;
    private final Locator CLICK_TEMPLATE_SEARCH;
    private final Locator SELECT_TEMPLATE;
    private final Locator TACTIC_DROPDOWN;
    private final Locator SELECT_LIFETIME;
    private final Locator RUN_REPORT_BUTTON;
    private final Locator REPORT_DOWNLOAD_OPTION;
    private final Locator TEMPLATE_COLUMNS;
    private final Locator SEARCH_ICON;
    private final Locator REPORT_PANEL;
    private final Locator SEARCH_REPORT;
    private final Locator SEARCH_BUTTON;
    private final Locator DOWNLOAD_REPORT;
    private final Locator TEMPLATE_PAGINATION;
    private final Locator TREE_COLLAPSED_ICON;
    private final Locator DIMENSION_AND_METRICS_LABELS;
    private final Locator CANCEL_BUTTON;
    private final Locator EDIT_TEMPLATE;
    private final Locator DELETE_ICON;
    private final Locator ALERT_MESSAGE;
    private final Locator FETCH_TEMPLATE_NAME_FROM_CONFIRMATION_POPUP;
    private final Locator DELETE_BUTTON_FROM_CONFIRMATION_POPUP;
    private final Locator DELETE_ICON_FROM_TEMPLATE_LIST;
    private final Locator SHARED_CHECKBOX;
    private final Locator TEMPLATE_NAME_FROM_LISTING_PAGE;
    private final Locator DIMENSIONS_FROM_LISTING_PAGE;
    private final Locator TEMPLATE_TYPE_FROM_LISTING_PAGE;
    private final Locator SHARED_TYPE_FROM_LISTING_PAGE;
    private final Locator CREATED_BY_FROM_LISTING_PAGE;
    private final Locator TEMPLATE_TYPE;
    private final Locator COPY_ICON;
    private final Locator RUN_REPORT_ICON;
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());

    public ReportTemplates(Page page) {
        this.page = page;
        this.REPORT_TEMPLATE_LINK = page.locator("//div[normalize-space(text())='Report Templates']");
        this.TEMPLATES_TAB = page.locator(
                "//a[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'templates')]");
        this.GENERATED_REPORTS_TAB = page.locator(
                "//a[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'generated reports')]");
        this.SCHEDULING_TAB = page.locator(
                "//a[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'scheduling')]");
        this.NEW_TEMPLATE = page.locator("//button[normalize-space(text())='New Template']");
        this.REPORT_DIMENSIONS = page.locator("//div[normalize-space(text())='Dimensions']");
        this.REPORT_METRICS = page.locator("//div[normalize-space(text())='Metrics']");
        this.TEMPLATE_NAME = page.locator("//input[contains(@class,'template-name')]");
        this.SEARCH_DIMENSION = page.locator("//input[contains(@class,'search group_list') and @placeholder='Search']");
        this.SELECT_DIMENSION = page.locator("//label[text()='Advertiser Name']");
        this.SEARCH_METRIC = page.locator("//input[contains(@class,'search group_list') and @placeholder='Search']");
        this.SELECT_METRIC = page.locator("//label[text()='Impressions']");
        this.VERIFY_DIMENSION = page.locator("//sortable-item[contains(@class,'diemension')]//label");
        this.VERIFY_METRIC = page.locator("//sortable-item[contains(@class,'metric')]//label");
        this.SAVE_TEMPLATE = page.locator("//button[normalize-space(text())='Save']");
        this.TEMPLATE_SUCCESS =
                page.locator("//div[@role='alert' and contains(text(),'Template created successfully')]");
        this.SEARCH_TEMPLATE = page.locator("//input[contains(@class,'gaTableSearch') and @placeholder='Search']");
        this.CLICK_TEMPLATE_SEARCH = page.locator("//div[contains(@class,'gaTableSearchBtn')]");
        this.SELECT_TEMPLATE = page.locator("//input[@placeholder='Select Template']");
        this.TACTIC_DROPDOWN = page.locator("//input[@placeholder='Enter or Paste Tactics']");
        this.SELECT_LIFETIME = page.locator("//button[normalize-space()='Lifetime']");
        this.TEMPLATE_COLUMNS =
                page.locator("//tr[contains(@class, 'highlighted') and contains(@class, 'loadedall')]//td[1]/div");
        this.SEARCH_ICON = page.locator(".search-field > .ui");
        this.RUN_REPORT_BUTTON = page.getByRole(
                AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Run").setExact(true));
        this.REPORT_DOWNLOAD_OPTION = page.locator("//img[@title='options']");
        this.REPORT_PANEL = page.locator(".reports-body > div").first();
        this.SEARCH_REPORT = page.locator("input.form-control.ng-untouched.ng-pristine.ng-valid");
        this.SEARCH_BUTTON = page.locator("div.iconSprite.search1");
        this.DOWNLOAD_REPORT = page.locator("//span[text()='Download']");
        this.TEMPLATE_PAGINATION = page.locator("div.pagination-wrapper");
        this.TREE_COLLAPSED_ICON = page.locator("//i[@class='icon_custom tree-collapsed']");
        this.DIMENSION_AND_METRICS_LABELS =
                page.locator("//div[contains(@class,'checkbox-group-item')]//sui-checkbox//label");
        this.CANCEL_BUTTON = page.locator("//div[@class='targetingFooter']//button[contains(text(),'Cancel')]");
        this.EDIT_TEMPLATE = page.locator("//div[text()='Edit Template']");
        this.DELETE_ICON = page.locator("//span[text()='Delete']/parent::div");
        this.ALERT_MESSAGE = page.locator("//div[@role='alert']");
        this.FETCH_TEMPLATE_NAME_FROM_CONFIRMATION_POPUP = page.locator(
                "//div[contains(text(),'Delete Report Template')]/following-sibling::div//div[contains(@style,'word-break:')]");
        this.DELETE_BUTTON_FROM_CONFIRMATION_POPUP = page.locator(
                "//div[contains(text(),'Delete Report Template')]/following-sibling::div//span[contains(text(),'Delete')]");
        this.DELETE_ICON_FROM_TEMPLATE_LIST = page.locator("//span[@title='Delete']");
        this.SHARED_CHECKBOX = page.locator("//sui-checkbox//label[contains(text(),'Shared')]");
        this.TEMPLATE_NAME_FROM_LISTING_PAGE = page.locator("//div[contains(text(),'Template Name')]/ancestor::div[contains(@class,'headerWrapper')]/following-sibling::table//div");
        this.DIMENSIONS_FROM_LISTING_PAGE = page.locator("//div[contains(text(),'Rows/Metrics')]/ancestor::div[contains(@class,'headerWrapper')]/following-sibling::table//td[1]//div");
        this.TEMPLATE_TYPE_FROM_LISTING_PAGE = page.locator("//div[contains(text(),'Type')]/ancestor::div[contains(@class,'headerWrapper')]/following-sibling::table//td[2]//div");
        this.SHARED_TYPE_FROM_LISTING_PAGE = page.locator("//div[contains(text(),'Shared')]/ancestor::div[contains(@class,'headerWrapper')]/following-sibling::table//td[3]//div");
        this.CREATED_BY_FROM_LISTING_PAGE = page.locator("//div[contains(text(),'Created By')]/ancestor::div[contains(@class,'headerWrapper')]/following-sibling::table//td[4]//div");
        this.TEMPLATE_TYPE = page.locator("//sui-radio-button[@name='templateType']");
        this.COPY_ICON = page.locator("//span[@title='Copy']");
        this.RUN_REPORT_ICON = page.locator("//span[@title='Run Report']");
    }

    public void clickReportTemplatesLink() {
        REPORT_TEMPLATE_LINK.click();
    }

    public String verifyTemplatesTab() {
        return TEMPLATES_TAB.innerText();
    }

    public String verifyGeneratedReportsTab() {
        return GENERATED_REPORTS_TAB.innerText();
    }

    public String verifySchedulingTab() {
        return SCHEDULING_TAB.innerText();
    }

    public void createNewTemplate() {
        NEW_TEMPLATE.click();
    }

    public String verifyDimensionsTab() {
        return REPORT_DIMENSIONS.innerText();
    }

    public String verifyMetricsTab() {
        return REPORT_METRICS.innerText();
    }

    public void clickMetricsTab() {
        REPORT_METRICS.click();
    }

    public void enterTemplateName(String templateName) {
        TEMPLATE_NAME.fill(templateName);
    }

    public void selectDimensionAndMetric(String category) {
        SEARCH_DIMENSION.fill(category);
        page.locator(String.format("//label[text()='%s']", category)).click();
        SEARCH_DIMENSION.clear();
    }

    public List<String> fetchSelectedDimensions() {
        return VERIFY_DIMENSION.allInnerTexts().stream().map(String::trim).toList();
    }

    public List<String> fetchSelectedMetrics() {
        return VERIFY_METRIC.allInnerTexts().stream().map(String::trim).toList();
    }

    public void saveReportTemplate() {
        SAVE_TEMPLATE.click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public String reportTemplateSuccess() {
        String message = TEMPLATE_SUCCESS.innerText();
        TEMPLATE_SUCCESS.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.DETACHED));
        waitUtility.waitUntilPreLoaderHidden();
        return message;
    }

    public void searchCreatedReportTemplate(String createdReportTemplate) {
        SEARCH_TEMPLATE.fill(createdReportTemplate);
        waitUtility.waitForLocatorVisible(CLICK_TEMPLATE_SEARCH, 5000);
        CLICK_TEMPLATE_SEARCH.click(new Locator.ClickOptions().setForce(true));
    }

    public String verifyCreatedReportTemplate(String createdReportTemplate) {
        String templateNameXpath = String.format("//div[contains(text(),'%s')]", createdReportTemplate);
        waitUtility.waitForElementVisible(templateNameXpath);
        return page.locator(templateNameXpath).innerText();
    }

    public int searchResultRowCount() {
        return page.locator("//tbody[@popuptrigger='manual']//tr[contains(@class,'fixedrow')]")
                .count();
    }

    public void enterDetailsToRunReport(String reportTemplateName, String tactic) {
        SELECT_TEMPLATE.fill(reportTemplateName);
        Locator optionLocator = page.getByTitle(reportTemplateName).first();
        while (!optionLocator.isVisible()) {
            SELECT_TEMPLATE.fill(reportTemplateName);
            page.waitForTimeout(2000);
        }
        optionLocator.click();
        TACTIC_DROPDOWN.fill(tactic);
        page.locator(String.format("//div[@id='tacticLookup']//div[@data-text='%s']", tactic))
                .click();
        REPORT_PANEL.click();
        SELECT_LIFETIME.click();
    }

    public String verifyAutopopulatedCampaign(String createdCampaign) {
        return page.locator(String.format("//a[contains(normalize-space(), '%s')]", createdCampaign))
                .innerText();
    }

    public String verifyAutopopulatedLineitem(String createdLineitem) {
        return page.locator(String.format("//a[contains(normalize-space(), '%s')]", createdLineitem))
                .innerText();
    }

    public void runReport() {
        RUN_REPORT_BUTTON.click();
    }

    public String downloadGeneratedReport(String templateNameRandom) throws IOException {
        waitUtility.waitForElementVisible("div.ui.dropdown.selection.sort-option-dropdown", 60000);
        SEARCH_REPORT.fill(templateNameRandom);
        SEARCH_BUTTON.click();
        waitUtility.waitForElementVisible(String.format("//div[contains(text(), '%s')]", templateNameRandom));
        Locator reportProgressIcon = page.locator(String.format(
                "//div[contains(text(), '%s')]/ancestor::div[contains(@class,'left data-container')]/preceding-sibling::div//div[contains(@class,'icon report-progress')]",
                templateNameRandom));
        while (reportProgressIcon.isVisible()) {
            SEARCH_BUTTON.click();
            page.waitForTimeout(5000); // wait for 1 second
        }
        waitUtility.waitForLocatorDetached(reportProgressIcon);
        REPORT_DOWNLOAD_OPTION.click();
        Download download = page.waitForDownload(DOWNLOAD_REPORT::click);
        String REPORT_NAME = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String downloadPath =
                Paths.get(System.getProperty("user.home"), "Downloads").toString();
        String filePath =
                Paths.get(downloadPath, "report_" + REPORT_NAME + ".csv").toString();
        download.saveAs(Paths.get(filePath));
        return filePath;
    }

    public boolean verifyColumnsOfReport(String templateNameRandom, String filePath) throws Exception {
        waitUtility.waitForLocatorVisible(TEMPLATE_PAGINATION);
        waitUtility.waitUntilPreLoaderHidden();
        SEARCH_TEMPLATE.fill(templateNameRandom);
        SEARCH_ICON.click(new Locator.ClickOptions().setForce(true));
        waitUtility.waitForElementVisible(String.format("//div[contains(text(), '%s')]", templateNameRandom), 5000);
        List<String> expectedHeaders = Arrays.stream(
                        TEMPLATE_COLUMNS.innerText().split("\\s*,\\s*"))
                .map(h -> h.toLowerCase().replaceAll("\\s+", "")) // Normalize expected
                .toList();

        List<String> rawActualHeaders = ExcelActions.readCsvExcludingFirstColumn(filePath);
        List<String> actualHeaders = rawActualHeaders.stream()
                .map(h -> h.toLowerCase().replaceAll("\\s+", "")) // Normalize actual
                .toList();

        return expectedHeaders.stream().allMatch(expected -> actualHeaders.stream()
                .anyMatch(actual -> actual.contains(expected) || expected.contains(actual)));
    }

    public List<String> expandGroupsAndFetchDimensionsAndMetrics() {
        while (TREE_COLLAPSED_ICON.count() > 0) {
            TREE_COLLAPSED_ICON.first().scrollIntoViewIfNeeded();
            TREE_COLLAPSED_ICON.first().click();
        }
        return DIMENSION_AND_METRICS_LABELS.allInnerTexts();
    }

    public void clickCancelButton() {
        CANCEL_BUTTON.click();
    }

    public boolean verifyReportGeneratedFromLineItemPage(String reportName) {
        Locator xpath =
                page.locator(String.format("//div[contains(@class,'scopelist') and contains(., '%s')]", reportName));
        return xpath.isVisible();
    }

    public void clickTemplate(String templateName) {
        Locator templateLocator = page.locator(String.format("//div[@title='%s']", templateName));
        templateLocator.click();
        waitUtility.waitForLocatorVisible(EDIT_TEMPLATE);
    }

    public boolean isDeleteIconDisabledOnCreateNewTemplatePanel() {
        return DELETE_ICON.getAttribute("class").contains("disabled");
    }

    public boolean isDeleteIconEnabledOnEditTemplatePanel() {
        return !DELETE_ICON.getAttribute("class").contains("disabled");
    }

    public boolean isTabSelectedByDefault() {
        return TEMPLATES_TAB.getAttribute("class").contains("active");
    }

    public String fetchAlertMessage() {
        return ALERT_MESSAGE.textContent().trim();
    }

    public boolean checkActionIconsForTemplate(String templateName, String actionIcon) {
        Locator actionIconLocator = page.locator(String.format(
                "//div[contains(normalize-space(.),'%s')]/parent::span/following-sibling::span//span[@title='%s']",
                templateName, actionIcon));
        return actionIconLocator.first().isVisible();
    }

    public void clickDeleteIconFromTemplatePanel() {
        DELETE_ICON.click();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(DELETE_ICON_FROM_TEMPLATE_LIST);
    }

    public String getTemplateNameFromConfirmationPopup() {
        String text = FETCH_TEMPLATE_NAME_FROM_CONFIRMATION_POPUP.innerText();
        return text.substring(text.indexOf('"') + 1, text.lastIndexOf('"'));
    }

    public boolean isDeleteReportTemplateConfirmationPopupDisplayed() {
        return FETCH_TEMPLATE_NAME_FROM_CONFIRMATION_POPUP.isVisible();
    }

    public void clickDeleteButtonFromConfirmationPopup() {
        DELETE_BUTTON_FROM_CONFIRMATION_POPUP.click();
    }

    public void clickDeleteIconFromTemplateList() {
        DELETE_ICON_FROM_TEMPLATE_LIST.click();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(DELETE_BUTTON_FROM_CONFIRMATION_POPUP);
    }

    public boolean isDefaultTemplateTypeSelected(String defaultTemplateType) {
        return page.locator(String.format("//label[text()='%s']/parent::sui-radio-button", defaultTemplateType))
                .getAttribute("class")
                .contains("checked");
    }

    public void clickSharedCheckbox() {
        SHARED_CHECKBOX.click();
    }

    public String getTemplateNameInTemplateListPage() {
        return TEMPLATE_NAME_FROM_LISTING_PAGE.textContent().trim();
    }

    public List<String> getDimensionsInTemplateListPage() {
        return Arrays.stream(DIMENSIONS_FROM_LISTING_PAGE.textContent().split(",")).map(String::trim).toList();
    }

    public String getCreatedByInTemplateListPage() {
        return CREATED_BY_FROM_LISTING_PAGE.textContent().trim();
    }

    public String getTemplateTypeInTemplateListPage() {
        return TEMPLATE_TYPE_FROM_LISTING_PAGE.textContent().trim();
    }

    public String getSharedInTemplateListPage() {
        return SHARED_TYPE_FROM_LISTING_PAGE.textContent().trim();
    }

    public void selectTemplateType(String templateType) {
        page.locator(String.format("//sui-radio-button[@name='templateType' and @value='%s']", templateType)).click();
    }

    public String getTemplateTypeInEditTemplatePanel() {
        for(int i=0; i<TEMPLATE_TYPE.count(); i++) {
            Locator radioButton = TEMPLATE_TYPE.nth(i);
            if(radioButton.getAttribute("class").contains("checked")) {
                return radioButton.getAttribute("value").trim();
            }
        }
        return null;
    }

    public String getSharedInEditTemplatePanel() {
        Locator sharedCheckbox = SHARED_CHECKBOX.locator("//preceding-sibling::input");
        if(sharedCheckbox.getAttribute("class").contains("checked")) {
            return "Shared";
        }
        return "Private";
    }

    public void copyExistingTemplate() {
        COPY_ICON.click();
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(TEMPLATE_NAME);
    }

    public String fetchTemplateNameFromEditTemplatePanel() {
        return TEMPLATE_NAME.inputValue().trim();
    }

    public List<String> fetchTemplateDetailsFromEditTemplatePanel() {
        String templateName = TEMPLATE_NAME.inputValue().trim();
        String templateType = getTemplateTypeInEditTemplatePanel();
        String sharedStatus = getSharedInEditTemplatePanel();
        List<String> selectedDimensions = fetchSelectedDimensions();
        List<String> selectedMetrics = fetchSelectedMetrics();
        return Arrays.asList(templateName, templateType, sharedStatus,
                String.join(", ", selectedDimensions), String.join(", ", selectedMetrics));
    }

    public void clickRunReportIconForTemplate() {
        RUN_REPORT_ICON.click();
    }
}
