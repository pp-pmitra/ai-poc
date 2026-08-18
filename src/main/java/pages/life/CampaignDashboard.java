package pages.life;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import utils.CommonUtils;
import utils.WaitUtility;

public class CampaignDashboard {
    private final Page page;
    private final Locator CAMPAIGN_PAGE_TEXT;
    private final Locator CAMPAIGN_COMMENT_BOX;
    private final Locator LINE_ITEM_COMMENT_BOX;
    private final Locator TACTIC_NAME_COMMENT_BOX;
    private final Locator COMMENT_BOX;
    private final Locator COMMENT_BOX_OK_BUTTON;
    private final Locator COMMENT_SUCCESS_ALERT;
    private final Locator COMMENT_ICON;
    private final Locator TOOLTIP_TEXT;
    private final Locator LINE_ITEM_TOGGLE_BUTTON;
    private final Locator TACTIC_TOGGLE_BUTTON;
    private final Locator LINE_ITEM_NAME;
    private final Locator TACTIC_NAME;
    private final Locator CAMPAIGN_PAGE_TITLE;
    private final Locator LINE_ITEM_PAGE_TITLE;
    private final Locator TACTIC_PAGE_TITLE;
    private final Locator DASHBOARD_MENU_ICON;
    private final Locator SELECT_COLUMNS_FROM_ICON;
    private final Locator SHOW_ALL;
    private final Locator HIDE_ALL;
    private final Locator DASHBOARD_MENU_COLUMNS;
    private final Locator FILTER_OK_BUTTON;
    private final Locator SELECTED_FILTER_LABEL;
    private final Locator FILTER_ICON;
    private final Locator FAVORITE_ONLY_CHECKBOX;
    private final Locator FAVORITE_CAMPAIGN_LIST;
    private final Locator HIDE_FINISHED_CHECKBOX;
    private final Locator CALENDER_APPLY_BUTTON;
    private final Locator CUSTOM_DATE_TEXTBOX;
    private final Locator SETTING_ICON;
    private final Locator GROUP_BY_CAMPAIGN_RADIO_BUTTON;
    private final Locator GROUP_BY_ADVERTISER_RADIO_BUTTON;
    private final Locator NO_GROUP_RADIO_BUTTON;
    private final Locator ADVERTISER_COLUMN_NAME;
    private final Locator CAMPAIGN_COLUMN_NAME;
    private final Locator CREATIVE_TOOLTIP;
    private final Locator LIFE_TIME_FILTER;
    private final Locator CLICK_SETTINGS;
    private final Locator SEARCH_CAMPAIGN;
    private final Locator CLICK_CAMPAIGN_SEARCH;
    private final Locator EXPAND_CREATED_LINE_ITEM;
    private final Locator LINE_ITEM_EXPAND_ICON;
    private final Locator VERIFY_CREATED_TACTIC;
    private final Locator CREATED_TACTIC;
    private final Locator CAMPAIGN_ENTRIES;
    private final Locator SUB_TITLE_AFTER_CAMPAIGN_SEARCH;
    private final Locator FILTER_APPLIED_ICON;
    private final Locator RESET_FILTER_ICON;
    private final Locator SELECTED_FILTER_VALUES;
    private final Locator FILTER_LIST;
    private final Locator STATUS_COLUMN_DATA;
    private final Locator TYPE_COLUMN_DATA;
    private final Locator ENABLED_COLUMN_DATA;
    private final Locator CAMPAIGN_COUNT_FROM_PAGINATION;
    private final Locator FLIGHT_START_END_DATE;
    private final Locator INACTIVE_FLIGHT_LIST;
    private final Locator NO_CAMPAIGN_AVAILABLE_TEXT;
    private final Locator CAMPAIGN_FROM_DASHBOARD;
    private final Locator GLOBAL_TACTIC_ICON;
    private final Locator CAMPAIGN_TILE;
    private final Locator CAMPAIGN_PAGINATION;
    WaitUtility waitUtility;
    String lineItemClassBeforeClick, lineItemClassAfterClick, tacticClassBeforeClick, tacticClassAfterClick;

    public CampaignDashboard(Page page) {
        this.page = page;
        this.waitUtility = new WaitUtility(page);
        this.CAMPAIGN_PAGE_TEXT = page.locator("//span[contains(text(),'Campaigns')]");
        this.CAMPAIGN_COMMENT_BOX =
                page.locator("//div[@class='camp-data']//span[@class='notesIconEmpty' or @class='notesIconProvided']");
        this.LINE_ITEM_COMMENT_BOX = page.locator(
                "//div[contains(@class,'lineitem-data pointer')]//span[@class='notesIconEmpty' or @class='notesIconProvided']");
        this.TACTIC_NAME_COMMENT_BOX = page.locator(
                "//div[contains(@class,'tactic-data pointer')]//span[@class='notesIconEmpty' or @class='notesIconProvided']");
        this.COMMENT_BOX = page.locator("//textarea[@id='notesId']");
        this.COMMENT_BOX_OK_BUTTON = page.locator("//button[@class='okButton']");
        this.COMMENT_SUCCESS_ALERT = page.locator(
                "//div[@aria-label='Success!']/following-sibling::div[contains(text(),'Notes saved successfully.')]");
        this.COMMENT_ICON = page.locator("span.notesIconProvided");
        this.TOOLTIP_TEXT = page.locator("//span[@class='tooltip-text']");
        this.LINE_ITEM_TOGGLE_BUTTON = page.locator(
                "//div[contains(@class,'lineitem-data pointer')]//sui-checkbox[contains(@class,'ng-valid')]");
        this.TACTIC_TOGGLE_BUTTON = page.locator(
                "//div[contains(@class,'tactic-data pointer')]//sui-checkbox[contains(@class,'ng-valid')]");
        this.LINE_ITEM_NAME = page.locator("//span[contains(@class,'color-black lineitem-name-section')]");
        this.LINE_ITEM_PAGE_TITLE = page.locator("//div[contains(@class,'lineitem-name')]");
        this.CAMPAIGN_PAGE_TITLE = page.locator("//div[contains(@class,'campaign-name')]");
        this.TACTIC_NAME = page.locator("//span[contains(@class,'color-black tactic-name-section')]");
        this.TACTIC_PAGE_TITLE = page.locator("//div[contains(@class,'tactic-name')]");
        this.DASHBOARD_MENU_ICON = page.locator("//span[@class='icon-bars']");
        this.SELECT_COLUMNS_FROM_ICON = page.locator("//sui-checkbox[contains(@class,'form-control')]/label");
        this.SHOW_ALL = page.locator("//div[contains(@class,'menuButtons')]//div[contains(@class,'show-all')]");
        this.HIDE_ALL = page.locator("//div[contains(@class,'menuButtons')]//div[contains(@class,'hide-all')]");
        this.DASHBOARD_MENU_COLUMNS = page.locator(
                "//div[contains(@class,'data-table-header')]/div[contains(@id,'liHeader') or contains(@class,'align-left')]");
        this.FILTER_OK_BUTTON = page.locator("//button[contains(@class,'ui primary button')]");
        this.SELECTED_FILTER_LABEL = page.locator("//div[contains(@class,'selected-filters')]//label");
        this.FILTER_ICON = page.locator("//div[contains(@class,'filterApplied')]");
        this.FAVORITE_ONLY_CHECKBOX = page.locator("//sui-checkbox[label[normalize-space()='Favorite Only']]");
        this.FAVORITE_CAMPAIGN_LIST = page.locator("//i[@class = 'star display-inline']");
        this.HIDE_FINISHED_CHECKBOX = page.locator("//label[contains(text(),'Hide Finished')]/ancestor::sui-checkbox");
        this.CALENDER_APPLY_BUTTON =
                page.locator("//button[contains(@class, 'applyBtn') and (contains(text(), 'Apply'))]");
        this.CUSTOM_DATE_TEXTBOX = page.locator("//input[@name='customDateRange']");
        this.SETTING_ICON = page.locator("//div[contains(@class,'gaGroupingType')]/i");
        this.GROUP_BY_CAMPAIGN_RADIO_BUTTON = page.locator("//i[contains(@class,'gaGroupCampaigns')]");
        this.GROUP_BY_ADVERTISER_RADIO_BUTTON = page.locator("//i[contains(@class,'gaGroupAdvertisers')]");
        this.NO_GROUP_RADIO_BUTTON = page.locator("//i[contains(@class,'gaNoGrouping')]");
        this.ADVERTISER_COLUMN_NAME = page.locator("//div[contains(@id,'liHeaderAdvertiser')]");
        this.CAMPAIGN_COLUMN_NAME =
                page.locator("//div[contains(@class,'display-inline')]/span[contains(text(),'Campaign')]");
        this.CREATIVE_TOOLTIP = page.locator(
                "//div[contains(@class, 'tactic-data')]//span[contains(@class,'approval-icon creative-red') or contains(@class, 'approval-icon creative')]");
        this.LIFE_TIME_FILTER = page.locator("//button[@name='FilterOptionType' and normalize-space()='Lifetime']");
        this.CLICK_SETTINGS = page.locator("//i[@class='icon gearIcon']");
        this.SEARCH_CAMPAIGN = page.locator("//input[@placeholder='Search' and contains(@class, 'gaTableSearch')]");
        this.CLICK_CAMPAIGN_SEARCH = page.locator("//div[contains(@class,'gaTableSearchBtn')]");
        this.LINE_ITEM_EXPAND_ICON = page.locator("//i[contains(@class,'fa-angle-right')]");
        this.VERIFY_CREATED_TACTIC = page.locator("//span[contains(@class,'tactic-name')]");
        this.EXPAND_CREATED_LINE_ITEM =
                page.locator("//div[contains(@class,'cl-expand-li')]/div[contains(@class,'collapsed-thin')]");
        this.CREATED_TACTIC = page.locator("//div[contains(@class,'cl-entity--tactic')]//span[contains(@class,'cl-entity__name')]");
        this.CAMPAIGN_ENTRIES = page.locator("//tr[contains(@class,'cl-li-row')]");
        this.SUB_TITLE_AFTER_CAMPAIGN_SEARCH =
                page.locator("//div[contains(@class,'sub-title') and contains(text(),'Line items, 1 Campaigns')]");
        this.FILTER_APPLIED_ICON = page.locator("//div[contains(@class,'filterApplied')]");
        this.RESET_FILTER_ICON = page.locator("//span[contains(text(),'Reset All Filters')]");
        this.SELECTED_FILTER_VALUES = page.locator("//div[contains(@class,'filter-value')]");
        this.FILTER_LIST = page.locator("//app-overlay[@class='line-item-list-filter']//label");
        this.STATUS_COLUMN_DATA = page.locator(
                "//div[contains(@class,'status-col') and not(contains(@id,'liHeaderStatus'))]//span[contains(@class,'display-inline')]//span");
        this.TYPE_COLUMN_DATA =
                page.locator("//div[contains(@class,'type-col') and not(contains(@id,'liHeaderType'))]");
        this.ENABLED_COLUMN_DATA = page.locator(
                "//div[contains(@class,'enable-col') and not(contains(@id,'liHeaderEnabled'))]//sui-checkbox");
        this.CAMPAIGN_COUNT_FROM_PAGINATION = page.locator("//div[contains(@class,'paging-desc')]");
        this.FLIGHT_START_END_DATE = page.locator("//div[contains(@class,'date-col trc lac')]//span");
        this.INACTIVE_FLIGHT_LIST =
                page.locator("//div[contains(@class, 'active-flight-col') and (contains(text(), 'inactive flight'))]");
        this.NO_CAMPAIGN_AVAILABLE_TEXT =
                page.locator("//p[contains(text(), 'No campaigns matching filtering criteria found')]");
        this.CAMPAIGN_FROM_DASHBOARD = page.locator("//span[contains(@class,'adv-camp-name')]//span");
        this.GLOBAL_TACTIC_ICON = page.locator("//img[contains(@src,'T.svg')]");
        this.CAMPAIGN_TILE = page.locator("//div[contains(@class,'campaign-tile')]");
        this.CAMPAIGN_PAGINATION = page.locator("//div[@class='paging-desc']");
    }

    public String isCampaignDashboardVisibleWithTitle(String text) {
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitUntilPreLoaderHidden();
        page.waitForCondition(() -> CAMPAIGN_PAGE_TEXT
                        .filter(new Locator.FilterOptions().setHasText(text))
                        .count()
                == 1);
        return CAMPAIGN_PAGE_TEXT.innerText();
    }

    public String verifyCampaignDetails(String campaignID) {
        return page.locator(String.format("//span[contains(text(),'%s')]", campaignID))
                .innerText();
    }

    public String addCommentsToCampaign(String commentType, List<String> commentValues) {
        page.waitForLoadState(LoadState.LOAD);
        String successAlertText = "";
        switch (commentType) {
            case "Campaign Name":
                for (String val : commentValues) {
                    CAMPAIGN_COMMENT_BOX.isVisible();
                    CAMPAIGN_COMMENT_BOX.click();
                    successAlertText = addComments(val);
                }
                break;
            case "Line Item Name":
                for (String val : commentValues) {
                    LINE_ITEM_COMMENT_BOX.isVisible();
                    LINE_ITEM_COMMENT_BOX.click();
                    successAlertText = addComments(val);
                }
                break;
            case "Tactic Name":
                for (String val : commentValues) {
                    TACTIC_NAME_COMMENT_BOX.isVisible();
                    TACTIC_NAME_COMMENT_BOX.click();
                    successAlertText = addComments(val);
                }
                break;
        }
        return successAlertText;
    }

    public String addComments(String comment) {
        COMMENT_BOX.fill(comment);
        COMMENT_BOX_OK_BUTTON.click();
        waitUtility.waitForLocatorVisible(COMMENT_SUCCESS_ALERT);
        String successAlertText = COMMENT_SUCCESS_ALERT.innerText();
        waitUtility.waitForLocatorHidden(COMMENT_SUCCESS_ALERT);
        return successAlertText;
    }

    public List<String> verifyCommentIconColor() {
        List<String> backgroundImages = new ArrayList<>();
        for (int i = 0; i < COMMENT_ICON.count(); i++) {
            String bgImage = COMMENT_ICON
                    .nth(i)
                    .evaluate("element => getComputedStyle(element).backgroundImage")
                    .toString();
            backgroundImages.add(bgImage.substring(bgImage.lastIndexOf("/") + 1, bgImage.length() - 2));
        }
        return backgroundImages;
    }

    public List<String> verifyCommentIconText() {
        List<String> returnValues = new ArrayList<>();
        for (int i = 0; i < COMMENT_ICON.count(); i++) {
            COMMENT_ICON.nth(i).hover();
            waitUtility.waitForLocatorVisible(TOOLTIP_TEXT);
            returnValues.add(TOOLTIP_TEXT.innerText());
        }
        return returnValues;
    }

    public void clickLineAndTacticToggleButton() {
        DASHBOARD_MENU_ICON.click();
        SHOW_ALL.click();
        page.keyboard().press("Escape");
        lineItemClassBeforeClick = LINE_ITEM_TOGGLE_BUTTON.getAttribute("class");
        LINE_ITEM_TOGGLE_BUTTON.click();
        waitUtility.waitUntilPreLoaderHidden();
        lineItemClassAfterClick = LINE_ITEM_TOGGLE_BUTTON.getAttribute("class");

        tacticClassBeforeClick = TACTIC_TOGGLE_BUTTON.getAttribute("class");
        TACTIC_TOGGLE_BUTTON.click();
        waitUtility.waitUntilPreLoaderHidden();
        tacticClassAfterClick = TACTIC_TOGGLE_BUTTON.getAttribute("class");
    }

    public boolean verifyLineTacticToggleStatus() {
        return !Objects.equals(lineItemClassBeforeClick, lineItemClassAfterClick)
                && !Objects.equals(tacticClassBeforeClick, tacticClassAfterClick);
    }

    public List<String> fetchLineAndTacticToggleStatus() {
        if (lineItemClassAfterClick.contains("checked")) lineItemClassAfterClick = "Enabled";
        else lineItemClassAfterClick = "Disabled";

        if (tacticClassAfterClick.contains("checked")) tacticClassAfterClick = "Enabled";
        else tacticClassAfterClick = "Disabled";

        return List.of(lineItemClassAfterClick, tacticClassAfterClick);
    }

    public boolean isCampaignPageDisplayed() {
        return (CAMPAIGN_PAGE_TITLE.getAttribute("class").contains("campaign"));
    }

    public boolean isLineItemPageDisplayed() {
        return LINE_ITEM_PAGE_TITLE.getAttribute("class").contains("lineitem");
    }

    public boolean isTacticPageDisplayed() {
        return TACTIC_PAGE_TITLE.getAttribute("class").contains("tactic-name");
    }

    public void clickMenuTransitionIcon(List<String> columnName) {
        DASHBOARD_MENU_ICON.click();
        HIDE_ALL.click();
        CommonUtils.selectAndClickElement(SELECT_COLUMNS_FROM_ICON, columnName);
        page.keyboard().press("Escape");
    }

    public List<String> fetchDashboardColumns() {
        List<String> columnNames = new ArrayList<>();
        for (int i = 0; i < DASHBOARD_MENU_COLUMNS.count(); i++) {
            columnNames.add(DASHBOARD_MENU_COLUMNS.nth(i).innerText());
        }
        return columnNames;
    }

    public boolean clickHideAllOption() {
        DASHBOARD_MENU_ICON.click();
        HIDE_ALL.click();
        page.keyboard().press("Escape");
        return DASHBOARD_MENU_COLUMNS.count() == 0;
    }

    public boolean clickShowAllOption() {
        DASHBOARD_MENU_ICON.click();
        SHOW_ALL.click();
        page.keyboard().press("Escape");
        return DASHBOARD_MENU_COLUMNS.count() > 0;
    }

    public void applyFilterOnSelectedColumns(String filterName, List<String> filterValues) {
        String filterXpath = String.format(
                "//span[contains(text(), '%s')]/following-sibling::div//span[contains(@class,'filter')]", filterName);
        page.locator(filterXpath).click();
        for (int i = 0; i < FILTER_LIST.count(); i++) {
            Locator option = FILTER_LIST.nth(i);
            String text = option.innerText().trim();
            boolean shouldBeSelected = filterValues.stream().anyMatch(text::equals);
            boolean isChecked =
                    option.locator("xpath=//preceding-sibling::input").isChecked();
            if (shouldBeSelected && !isChecked) {
                option.click();
            }
            if (!shouldBeSelected && isChecked) {
                option.click();
            }
        }
        FILTER_OK_BUTTON.click();
        waitUtility.waitUntilPreLoaderHidden();
    }

    public List<String> fetchSelectedFilterLabels() {
        List<String> selectedFilter = new ArrayList<>();
        for (int i = 0; i < SELECTED_FILTER_LABEL.count(); i++) {
            selectedFilter.add(SELECTED_FILTER_LABEL.nth(i).innerText());
        }
        return selectedFilter;
    }

    public String verifyFilterIcon() {
        String bgImage = FILTER_ICON
                .evaluate("element => getComputedStyle(element).backgroundImage")
                .toString();
        return bgImage.substring(bgImage.lastIndexOf("/") + 1, bgImage.length() - 2);
    }

    public void clickFavoriteOnlyCheckbox() {
        unselectFavoriteCheckboxIfSelected();
        FAVORITE_ONLY_CHECKBOX.click();
        waitUtility.waitUntilPreLoaderHidden();
    }

    public boolean isFavoriteCampaignShown() {
        boolean flag = false;
        for (int i = 0; i < FAVORITE_CAMPAIGN_LIST.count(); i++) {
            if (!FAVORITE_CAMPAIGN_LIST.nth(i).getAttribute("class").contains("empty")) flag = true;
        }
        return flag;
    }

    public boolean isFavoriteNonFavoriteCampaignAvailable() {
        boolean flag = false;
        for (int i = 0; i < FAVORITE_CAMPAIGN_LIST.count(); i++) {
            if (!FAVORITE_CAMPAIGN_LIST.nth(i).getAttribute("class").contains("empty")
                    || FAVORITE_CAMPAIGN_LIST.nth(i).getAttribute("class").contains("empty")) flag = true;
        }
        return flag;
    }

    public void clickHideFinishedCheckbox() {
        unselectHideFinishedCheckboxIfSelected();
        HIDE_FINISHED_CHECKBOX.click();
        waitUtility.waitUntilPreLoaderHidden(120000);
    }

    public boolean isFinishedCampaignListHidden() {
        boolean flag = false;
        for (int i = 0; i < STATUS_COLUMN_DATA.count(); i++) {
            String text = STATUS_COLUMN_DATA.nth(i).innerText().trim();
            if (!text.equalsIgnoreCase("FINISHED")) {
                flag = true;
            }
        }
        return flag;
    }

    public boolean isFinishedCampaignListShownWithOtherStatus() {
        boolean flag = false;
        for (int i = 0; i < STATUS_COLUMN_DATA.count(); i++) {
            String text = STATUS_COLUMN_DATA.nth(i).innerText().trim();
            if (text.equalsIgnoreCase("FINISHED")
                    || text.equalsIgnoreCase("INCOMPLETE")
                    || text.equalsIgnoreCase("PENDING APPROVAL")
                    || text.equalsIgnoreCase("READY")
                    || text.equalsIgnoreCase("RUNNING")
                    || text.equalsIgnoreCase("DENIED")) {
                flag = true;
            }
        }
        return flag;
    }

    public boolean ifInactiveFlightPresent() {
        return !INACTIVE_FLIGHT_LIST.first().isVisible();
    }

    public List<LocalDate> fetchFlightStartAndEndDate() {
        List<LocalDate> dates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < FLIGHT_START_END_DATE.count(); i++) {
            String dateText = FLIGHT_START_END_DATE.nth(i).innerText().trim();
            LocalDate date = LocalDate.parse(dateText, formatter);
            dates.add(date);
        }
        return dates;
    }

    public void clickFilterTypeButton(String filterType) {
        Locator filterTypeButton = page.locator(String.format("//button[@data-title='%s']", filterType));
        filterTypeButton.click();
        waitUtility.waitUntilPreLoaderHidden();
        waitUtility.waitForLocatorVisible(CAMPAIGN_ENTRIES.last());
    }

    public void enterCustomDateRange(String startDate, String endDate) {
        CUSTOM_DATE_TEXTBOX.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        CUSTOM_DATE_TEXTBOX.clear();
        CUSTOM_DATE_TEXTBOX.type(startDate + " - " + endDate);
        CALENDER_APPLY_BUTTON.click();
        waitUtility.waitUntilPreLoaderHidden();
    }

    public void clickSettingIcon() {
        SETTING_ICON.click();
    }

    public boolean clickGroupByOptionsAndCheckDashboardData(String groupByOption) {
        boolean flag = false;
        switch (groupByOption) {
            case "Group By Campaign":
                GROUP_BY_CAMPAIGN_RADIO_BUTTON.click();
                waitUtility.waitUntilPreLoaderHidden();
                if (ADVERTISER_COLUMN_NAME.isVisible()) flag = true;
                break;
            case "Group By Advertiser":
                GROUP_BY_ADVERTISER_RADIO_BUTTON.click();
                waitUtility.waitUntilPreLoaderHidden();
                if (CAMPAIGN_COLUMN_NAME.isVisible()) flag = true;
                break;
            case "No Grouping":
                NO_GROUP_RADIO_BUTTON.click();
                waitUtility.waitUntilPreLoaderHidden();
                if (ADVERTISER_COLUMN_NAME.isVisible() && CAMPAIGN_COLUMN_NAME.isVisible()) flag = true;
        }
        return flag;
    }

    public String fetchCreativeToolTipText() {
        String text = "";
        String tooltipText = CREATIVE_TOOLTIP.getAttribute("tooltip-text");
        if (tooltipText != null) {
            if (tooltipText.contains("No creative assigned")
                    || tooltipText.contains("are pending approval")
                    || tooltipText.contains("are denied")) {
                text = tooltipText;
            }
        } else {
            text = "Creative assigned and approved";
        }
        return text;
    }

    public void navigateToLineItemDetails() {
        LINE_ITEM_NAME.click();
        waitUtility.waitForLocatorVisible(LINE_ITEM_PAGE_TITLE);
    }

    public void navigateToTacticDetails() {
        TACTIC_NAME.click();
        waitUtility.waitForLocatorVisible(TACTIC_PAGE_TITLE);
    }

    public void clickLifetimeFilter() {
        LIFE_TIME_FILTER.click();
        waitUtility.waitUntilPreLoaderHidden();
    }

    public void ensureCampaignRadioBtnSelected() {
        CLICK_SETTINGS.click();
        if (!GROUP_BY_CAMPAIGN_RADIO_BUTTON.getAttribute("class").contains("groupingMenuRadioSelected")) {
            GROUP_BY_CAMPAIGN_RADIO_BUTTON.click();
            waitUtility.waitUntilPreLoaderHidden();
            waitUtility.waitForLocatorVisible(CAMPAIGN_ENTRIES.last());
        } else {
            page.keyboard().press("Escape");
        }
    }

    public void unselectFavoriteCheckboxIfSelected() {
        if (FAVORITE_ONLY_CHECKBOX.getAttribute("class").contains("checked")) {
            FAVORITE_ONLY_CHECKBOX.click();
            waitUtility.waitForLocatorVisible(CAMPAIGN_ENTRIES.last());
        }
    }

    public void unselectHideFinishedCheckboxIfSelected() {
        if (HIDE_FINISHED_CHECKBOX.getAttribute("class").contains("checked")) {
            HIDE_FINISHED_CHECKBOX.click();
            waitUtility.waitForLocatorVisible(CAMPAIGN_ENTRIES.last());
        }
    }

    public void resetFiltersIfApplied() {
        if (FILTER_APPLIED_ICON.isVisible()) {
            FILTER_APPLIED_ICON.click();
            RESET_FILTER_ICON.click();
            waitUtility.waitForLocatorVisible(CAMPAIGN_ENTRIES.last());
        }
    }

    public void waitUntilCampaignPaginationAppears(){
        waitUtility.waitForLocatorVisible(CAMPAIGN_PAGINATION.last());
    }

    public void searchCreatedCampaign(String createdCampaign) {
        waitUntilCampaignPaginationAppears();
        ensureCampaignRadioBtnSelected();
        unselectFavoriteCheckboxIfSelected();
        unselectHideFinishedCheckboxIfSelected();
        resetFiltersIfApplied();
        while (true) {
            SEARCH_CAMPAIGN.fill(createdCampaign);
            CLICK_CAMPAIGN_SEARCH.click();
            waitUtility.waitUntilPreLoaderHidden();
            if (SUB_TITLE_AFTER_CAMPAIGN_SEARCH.isVisible()) {
                break;
            }
            if (NO_CAMPAIGN_AVAILABLE_TEXT.isVisible()) {
                break;
            }
        }
    }

    public void searchCreatedTactic(String tacticName) {
        waitUtility.waitForLocatorVisible(CAMPAIGN_ENTRIES.last());
        while (true) {
            SEARCH_CAMPAIGN.fill(tacticName);
            CLICK_CAMPAIGN_SEARCH.click();
            waitUtility.waitUntilPreLoaderHidden();
            if (CAMPAIGN_FROM_DASHBOARD.first().isVisible()) {
                break;
            }
            if (NO_CAMPAIGN_AVAILABLE_TEXT.isVisible()) {
                break;
            }
        }
    }

    public String verifyCreatedCampaign(String createdCampaign) {
        String campaignNameXpath = String.format("//a[contains(text(),'%s')]", createdCampaign);
        waitUtility.waitForLocatorVisible(page.locator(campaignNameXpath).first());
        return page.locator(campaignNameXpath).first().innerText();
    }

    public String verifyCreatedLineItem(String lineItemNameRandom) {
        String lineItemNameXpath = String.format("//span[contains(text(),'%s')]", lineItemNameRandom);
        waitUtility.waitForElementVisible(lineItemNameXpath);
        return page.locator(lineItemNameXpath).innerText();
    }

    public void expandCreatedLineItem() {
        if (EXPAND_CREATED_LINE_ITEM.first().isVisible()) {
            EXPAND_CREATED_LINE_ITEM.first().click();
        }
    }

    public void clickLineItemExpandIcon() {
            LINE_ITEM_EXPAND_ICON.first().click();
    }

    public String verifyCreatedTactic() {
        page.waitForLoadState();
        return CREATED_TACTIC.innerText();
    }

    public void navigateToCampaign(String campaignID) {
        page.locator(String.format("//span[contains(text(),'%s')]", campaignID)).click();
        waitUtility.waitForLocatorVisible(CAMPAIGN_PAGE_TITLE);
    }

    /**
     * Temporary workaround:
     * In the demo environment, campaigns are currently not clickable, so navigation
     * is performed through the associated line item instead.
     *
     * We are investigating whether this is a temporary issue in the demo environment
     * or an intended permanent change.
     */
    public void navigateToCreatedCampaign(String lineItemName) {
        Locator expandIcon = page.locator("//div[@class='cl-expand-li']");

        if ("false".equals(expandIcon.getAttribute("aria-expanded"))) {
            expandIcon.click();
            waitUtility.waitForLocatorVisible(page.locator("//div[@class='cl-expand-li' and @aria-expanded='true']"));
        }

        page.locator(String.format("//span[contains(text(),'%s')]", lineItemName)).click();
        waitUtility.waitForLocatorVisible(LINE_ITEM_PAGE_TITLE);
        CAMPAIGN_TILE.click();
        waitUtility.waitForLocatorVisible(CAMPAIGN_PAGE_TITLE);
    }

    public List<String> fetchSelectedFilterValues() {
        List<String> selectedFilter = new ArrayList<>();
        for (int i = 0; i < SELECTED_FILTER_VALUES.count(); i++) {
            List<String> splitValues = Arrays.stream(
                            SELECTED_FILTER_VALUES.nth(i).innerText().split(","))
                    .map(String::trim)
                    .toList();
            selectedFilter.addAll(splitValues);
        }
        return selectedFilter;
    }

    public boolean isCampaignDataFilteredAccordingToSelectedFilters(String keyType, List<Object> keyValues) {
        return switch (keyType) {
            case "Status" -> checkValueInList(keyType, STATUS_COLUMN_DATA, keyValues);
            case "Type" -> checkValueInList(keyType, TYPE_COLUMN_DATA, keyValues);
            case "Enabled" -> checkValueInList(keyType, ENABLED_COLUMN_DATA, keyValues);
            default -> false;
        };
    }

    private boolean checkValueInList(String keyType, Locator columnName, List<Object> allowedValues) {
        String cellValue;
        for (int i = 0; i < columnName.count(); i++) {
            if (keyType.contains("Enabled")) {
                if (columnName.nth(i).getAttribute("class").contains("checked")) cellValue = "Enabled";
                else cellValue = "Disabled";
            } else cellValue = columnName.nth(i).innerText().trim();
            if (!allowedValues.contains(cellValue)) {
                return false;
            }
        }
        return true;
    }

    public void clickResetAllFilters() {
        RESET_FILTER_ICON.click();
        waitUtility.waitUntilPreLoaderHidden();
        waitUtility.waitForLocatorVisible(CAMPAIGN_ENTRIES.last());
    }

    public String fetchCampaignDataCountFromPagination() {
        waitUtility.waitForLocatorVisible(CAMPAIGN_ENTRIES.last());
        return CAMPAIGN_COUNT_FROM_PAGINATION.innerText().trim();
    }

    public boolean isCampaignDataAvailableInCustomDateRange() {
        return NO_CAMPAIGN_AVAILABLE_TEXT.isVisible();
    }

   public void clickGlobalTacticResult() {
        GLOBAL_TACTIC_ICON.click();
    }

    public void clickCampaignFromDashboard() {
        waitUtility.waitForLocatorVisible(CAMPAIGN_ENTRIES.last());
        CAMPAIGN_FROM_DASHBOARD.first().click();
        waitUtility.waitForLocatorVisible(CAMPAIGN_PAGE_TITLE);
    }

    public String fetchNoCampaignFoundMessage(String campaignName) {
        waitUtility.waitForLocatorVisible(CAMPAIGN_PAGE_TEXT);
        searchCreatedCampaign(campaignName);
        return NO_CAMPAIGN_AVAILABLE_TEXT.innerText();
    }
}
