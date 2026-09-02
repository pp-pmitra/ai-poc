package pages.life;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import factory.DriverFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import utils.CommonUtils;
import utils.WaitUtility;

public class CreateCreatives {
    public final Locator CLICK_THROUGH_URL;
    public final Locator ADVERTISER_DSA;
    public final Locator FINANCER;
    public final Locator SELECTED_AD_CHOICES_ICON;
    public final Locator CREATIVE_WIDTH_TYPE;
    public final Locator CREATIVE_STATUS_FROM_CREATIVE_TILE;
    public final Locator CREATED_BY_FROM_CREATIVE_TILE;
    public final Locator SOURCE_FROM_CREATIVE_TILE;
    public final Locator LAST_UPDATED_FROM_CREATIVE_TILE;
    public final Locator ASSOCIATIONS_TAB;
    public final Locator COLUMN_SELECTION_ICON;
    public final Locator COLUMN_NAME_FROM_SELECTION_ICON;
    public final Locator ASSOCIATIONS_TAB_COLUMN_NAME;
    public final Locator MENU_BUTTONS_FROM_ASSOCIATION_ICON;
    private final Page page;
    private final Locator CREATIVE_PAGE_TITLE;
    private final Locator UNARCHIVED_BUTTON;
    private final Locator ENABLED_ARCHIVED_BUTTON;
    private final Locator DISABLED_ARCHIVED_BUTTON;
    private final Locator ARCHIVED_BUTTON;
    private final Locator SELECT_ADVERTISER;
    private final Locator DROPDOWN_VALUES;
    private final Locator CLEAR_ALL_BUTTON;
    private final Locator SELECT_CAMPAIGN;
    private final Locator SELECT_AD_SIZE;
    private final Locator SELECT_CREATIVE_TYPE;
    private final Locator CREATIVE_STATUS;
    private final Locator CREATIVE_TYPE_ICON;
    private final Locator CREATED_BY;
    private final Locator SORT_DROPDOWN;
    private final Locator SEARCH_BOX;
    private final Locator CANCEL_BUTTON;
    private final Locator CREATIVE_HEADER;
    private final Locator OK_BUTTON;
    private final Locator SUCCESS_ALERT;
    private final Locator NEW_CREATIVE_BUTTON;
    private final Locator ADVERTISER_DROPDOWN;
    private final Locator ADVERTISER_DROPDOWN_VALUES;
    private final Locator CREATIVE_NAME;
    private final Locator CREATIVE_TYPE;
    private final Locator CREATIVE_HTML_CODE;
    private final Locator MACROS_CHECKBOX;
    private final Locator CREATIVE_AD_SIZE;
    private final Locator CREATIVE_AD_SIZE_VALUE;
    private final Locator DOMAIN_LANDING;
    private final Locator DURATION;
    private final Locator URL;
    private final Locator VAST_XML_TEXTAREA;
    private final Locator IAB_CATEGORY;
    private final Locator IAB_CATEGORY_VALUE;
    private final Locator WIDTH;
    private final Locator HEIGHT;
    private final Locator HEADLINE;
    private final Locator DESCRIPTION;
    private final Locator DISPLAY_URL;
    private final Locator SPONSORED_BY;
    private final Locator PRODUCT_DESCRIPTION;
    private final Locator SELECTED_IAB_CATEGORY;
    private final Locator PAGINATION_NEXT_BUTTON;
    private final Locator PAGINATION_PREVIOUS_BUTTON;
    private final Locator ITEMS_PER_PAGE_DROPDOWN;
    private final Locator ITEMS_PER_PAGE;
    private final Locator SORT_BY_NAME_ASC;
    private final Locator SORT_BY_NAME_DESC;
    private final Locator SORT_BY_ID_ASC;
    private final Locator SORT_BY_ID_DESC;
    private final Locator SORT_BY_LAST_UPDATED_ASC;
    private final Locator SORT_BY_LAST_UPDATED_DESC;
    private final Locator CREATIVE_NAME_LIST;
    private final Locator CREATIVE_ID_LIST;
    private final Locator CREATIVE_LAST_UPDATED_LIST;
    private final Locator CREATIVE_SOURCE_LIST;
    private final Locator CREATIVE_AD_SIZE_LIST;
    private final Locator APPROVAL_STATUS_BUTTON;
    private final Locator CREATIVE_FREQUENCY_OPTIONS;
    private final Locator SELECTED_AD_SIZE;
    private final Locator DELETE_ICON;
    private final Locator BULK_ACTIONS_BUTTON;
    private final Locator BULK_ASSIGN_CREATIVE_HEADER;
    private final Locator BULK_ASSIGN_CHECKBOX;
    private final Locator FETCH_TOOLTIP_TEXT_FOR_ASSIGNED_CAMPAIGNS;
    private final Locator REMOVAL_CONFIRMATION_POP_UP;
    private final Locator REMOVAL_CONFIRMATION_TEXT;
    private final Locator REMOVE_BUTTON;
    private final Locator NO_CREATIVE_FOUND_MESSAGE;
    private final Locator CREATIVE_STATUS_LABEL;
    private final Locator APPROVE_ALL_BUTTON;
    private final Locator APPROVED_BUTTON;
    private final Locator PREVIEW_LINK;
    private final Locator UPLOADING_PROGRESS_BAR;
    private final Locator UPLOAD_DELETE_ICON;
    private final Locator DOMAIN_LANDING_FROM_CREATIVE_TILE;
    private final Locator ADSIZE_FROM_CREATIVE_TILE;
    private final Locator DURATION_FROM_CREATIVE_TILE;
    private final Locator FILTER_ICON;
    private final Locator NO_FILTER_TEXT;
    private final Locator FILTER_BUTTONS;
    private final Locator SELECTED_FILTER_NAME;
    private final Locator FILTER_VALUE;
    private final Locator FILTER_START_DATE;
    private final Locator FILTER_END_DATE;
    private final Locator ASSOCIATIONS_TAB_LINE_ITEM_NAME;
    private final Locator ASSOCIATIONS_TAB_CAMPAIGN_NAME;
    private final Locator FILTERED_RECORD_COUNT;
    private final Locator CALENDAR_TITLE;
    private final Locator CALENDAR_MONTH;
    private final Locator CALENDAR_DATE;
    private final Locator LINE_ITEM_PAGE_TITLE;
    private final Locator NO_CAMPAIGN_FOUND_MESSAGE;
    private final Locator BULK_PANEL_CANCEL_BUTTON;
    Page newTab;
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());
    String imageTextLocator = "//span[contains(text(),'%s')]";

    public CreateCreatives(Page page) {
        this.page = page;
        this.CREATIVE_PAGE_TITLE = page.locator("//div[contains(text(),'Creatives')]");
        this.UNARCHIVED_BUTTON = page.locator("//div[contains(@class,'content-section')]//img[@title='unarchive']");
        this.ENABLED_ARCHIVED_BUTTON =
                page.locator("//div[contains(@class,'content-section')]//img[contains(@title,'archive')]");
        this.DISABLED_ARCHIVED_BUTTON =
                page.locator("//div[contains(@class,'content-section')]//span[@class='d-block']");
        this.ARCHIVED_BUTTON = page.locator("//div[contains(@class,'content-section')]//img[@title]");
        this.SELECT_ADVERTISER =
                page.locator("//app-multi-select[contains(@placeholder,'Select Advertisers')]/div/span/input");
        this.DROPDOWN_VALUES = page.locator("//div[@class='menu transition visible']//div[@class='item']/span");
        this.CLEAR_ALL_BUTTON = page.locator("//div[contains(text(),'Clear All')]");
        this.SELECT_CAMPAIGN =
                page.locator("//app-multi-select[contains(@placeholder,'Select Campaigns')]/div/span/input");
        this.SELECT_AD_SIZE =
                page.locator("//app-multi-select[contains(@placeholder,'Select Ad Sizes')]/div/span/input");
        this.SELECT_CREATIVE_TYPE =
                page.locator("//app-multi-select[contains(@placeholder,'Select Type')]/div/span/input");
        this.CREATIVE_STATUS =
                page.locator("//app-multi-select[contains(@placeholder,'Select Status')]/div/span/input");
        this.CREATED_BY = page.locator("//app-multi-select[contains(@placeholder,'Select Created By')]/div/span/input");
        this.SORT_DROPDOWN = page.locator("//div[contains(@class, 'sort-option-dropdown')]");
        this.SEARCH_BOX = page.locator("//div[contains(@class,'search-field')]/input[@placeholder='Search']");
        this.CANCEL_BUTTON = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cancel"));
        this.CREATIVE_HEADER = page.locator("//div[contains(@class,'rightPanelHeader1')]");
        this.OK_BUTTON = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save"));
        this.SUCCESS_ALERT = page.locator("//div[@aria-label='Success!']/following-sibling::div[@role='alert']");
        this.NEW_CREATIVE_BUTTON = page.locator("//app-ds-button-wrapper[@label='New Creative']");
        this.ADVERTISER_DROPDOWN =
                page.locator("//app-single-select-dropdown[contains(@placeholder,'Select Advertiser')]/div/div/input");
        this.ADVERTISER_DROPDOWN_VALUES = page.locator(
                "//app-single-select-dropdown[contains(@placeholder,'Select Advertiser')]//input/following-sibling::div/div");
        this.CREATIVE_NAME = page.locator("//input[contains(@formcontrolname,'creativeName')]");
        this.ADVERTISER_DSA = page.locator("//input[contains(@placeholder,'Advertiser per DSA')]");
        this.FINANCER = page.locator("//input[contains(@placeholder,'Financer')]");
        this.CREATIVE_TYPE = page.locator("//app-creativetype/div//following-sibling::div");
        this.CREATIVE_HTML_CODE = page.locator("//textarea[contains(@formcontrolname,'htmlCode')]");
        this.MACROS_CHECKBOX = page.locator("//sui-checkbox[contains(@class,'multi_click_macros_check ')]");
        this.CREATIVE_AD_SIZE =
                page.locator("//app-single-select-dropdown//input[contains(@placeholder,'Select Ad Size')]");
        this.CREATIVE_AD_SIZE_VALUE =
                page.locator("//input[contains(@placeholder,'Select Ad Size')]/following-sibling::div/div");
        this.DOMAIN_LANDING = page.locator("//input[contains(@formcontrolname,'landingDomain')]");
        this.DURATION = page.locator(
                "//input[contains(@formcontrolname,'audioDuration') or contains(@formcontrolname,'duration')]");
        this.URL = page.locator("//input[contains(@placeholder,'url')]");
        this.VAST_XML_TEXTAREA = page.locator(
                "//textarea[contains(@formcontrolname,'vastDoc') or contains(@formcontrolname,'vastAudioDoc')]");
        this.IAB_CATEGORY = page.locator("//div[contains(@id,'iabcreativeLookup')]/div/input");
        this.IAB_CATEGORY_VALUE = page.locator("//div[contains(@id,'iabcreativeLookup')]/div/div/div");
        this.WIDTH = page.locator("//input[contains(@formcontrolname,'width')]");
        this.HEIGHT = page.locator("//input[contains(@formcontrolname,'height')]");
        this.HEADLINE =
                page.locator("//input[contains(@formcontrolname,'headline') or contains(@formcontrolname,'tittle')]");
        this.DESCRIPTION = page.locator("//input[contains(@formcontrolname,'description')]");
        this.DISPLAY_URL = page.locator(
                "//input[contains(@formcontrolname,'srchDisplayUrl') or contains(@formcontrolname,'displayUrl')]");
        this.SPONSORED_BY = page.locator("//input[contains(@formcontrolname,'sponsoredText')]");
        this.PRODUCT_DESCRIPTION = page.locator("//textarea[contains(@formcontrolname,'productDescription')]");
        this.SELECTED_IAB_CATEGORY = page.locator("//div[@id='iabcreativeLookup']//span");
        this.CLICK_THROUGH_URL = page.locator("//input[contains(@formcontrolname,'clickThruUrl')]");
        this.PAGINATION_NEXT_BUTTON =
                page.locator("//button[contains(@class,'button basic')]//i[contains(@class,'angle right icon')]");
        this.PAGINATION_PREVIOUS_BUTTON =
                page.locator("//button[contains(@class,'button basic')]//i[contains(@class,'angle left icon')]");
        this.ITEMS_PER_PAGE_DROPDOWN =
                page.locator("//td[contains(@class,'sui-select-single-dropdown')]//div[@class='text']");
        this.ITEMS_PER_PAGE = page.locator("//div[contains(@class,'content-section')]");
        this.SORT_BY_NAME_ASC =
                page.locator("//span[contains(text(),'Name')]/following-sibling::i[contains(@class,'up')]");
        this.SORT_BY_NAME_DESC =
                page.locator("//span[contains(text(),'Name')]/following-sibling::i[contains(@class,'down')]");
        this.SORT_BY_ID_ASC = page.locator("//span[contains(text(),'ID')]/following-sibling::i[contains(@class,'up')]");
        this.SORT_BY_ID_DESC =
                page.locator("//span[contains(text(),'ID')]/following-sibling::i[contains(@class,'down')]");
        this.SORT_BY_LAST_UPDATED_ASC =
                page.locator("//span[contains(text(),'Last Updated')]/following-sibling::i[contains(@class,'up')]");
        this.SORT_BY_LAST_UPDATED_DESC =
                page.locator("//span[contains(text(),'Last Updated')]/following-sibling::i[contains(@class,'down')]");
        this.CREATIVE_NAME_LIST = page.locator("//div[contains(@class,'name-section-truncate')]");
        this.CREATIVE_ID_LIST = page.locator("//div[contains(@class,'id-section')]");
        this.CREATIVE_LAST_UPDATED_LIST =
                page.locator("//span[contains(text(),'Last updated:')]//following-sibling::span");
        this.CREATIVE_SOURCE_LIST = page.locator("//span[contains(text(),'Source:')]//following-sibling::span");
        this.CREATIVE_AD_SIZE_LIST = page.locator("//span[contains(text(),'AdSize:')]//following-sibling::span");
        this.APPROVAL_STATUS_BUTTON = page.locator("div.approval-container button[role='tab']");
        this.CREATIVE_FREQUENCY_OPTIONS = page.locator("//button[@name='frequencyOptionType']");
        this.SELECTED_AD_SIZE = page.locator("//div[contains(@class,'ad-size-group')]//input/following-sibling::span");
        this.DELETE_ICON = page.locator("//app-icon-lable-link[@text='Delete']//div");
        this.BULK_ACTIONS_BUTTON = page.locator("//div[contains(@id,'CreativeBulkActions')]");
        this.BULK_ASSIGN_CREATIVE_HEADER = page.locator(
                "//div[contains(@class,'rightPanelHeader2') and contains(text(),'Bulk Assign Creatives')]");
        this.BULK_ASSIGN_CHECKBOX = page.locator("//div[contains(@class,'shtCategoryContainer')]//sui-checkbox");
        this.FETCH_TOOLTIP_TEXT_FOR_ASSIGNED_CAMPAIGNS = page.locator("//div[contains(@class,'ng-tooltip-show')]");
        this.REMOVAL_CONFIRMATION_POP_UP = page.locator("//div[contains(text(),'Removal Confirmation')]");
        this.REMOVAL_CONFIRMATION_TEXT = page.locator(
                "//div[contains(text(),'Removal Confirmation')]/following-sibling::div[contains(@class,'text-middle')]");
        this.REMOVE_BUTTON = page.locator(
                "//div[contains(text(),'Removal Confirmation')]/following-sibling::div[contains(@class,'popup-actions-footer')]//span[contains(text(),'Remove')]");
        this.NO_CREATIVE_FOUND_MESSAGE =
                page.locator("//img[contains(@alt,'No Creatives Found')]/following-sibling::div");
        this.CREATIVE_STATUS_LABEL =
                page.locator("//div[contains(@class,'content-section')]//div[contains(@class,'status-label')]");
        this.APPROVE_ALL_BUTTON =
                page.locator("//div[contains(@class,'bulkapprove')]//button[contains(text(),'Approve All')]");
        this.APPROVED_BUTTON =
                page.locator("//div[contains(@class,'basic group')]//button[contains(text(),'Approved')]");
        this.CREATIVE_TYPE_ICON = page.locator(
                "//div[contains(@class,'image pointer') or contains(@class,'video pointer') or contains(@class,'generichtml pointer') or contains(@class,'search pointer') or contains(@class,'audio pointer') or contains(@class,'native pointer') or contains(@class,'nativevideo') or contains(@class,'expandablerichmedia pointer')]");
        this.PREVIEW_LINK = page.locator("//app-icon-lable-link[contains(@text,'Open a creative preview')]");
        this.UPLOADING_PROGRESS_BAR = page.locator("//sui-progress[contains(@class,'uploadProgressbar')]");
        this.UPLOAD_DELETE_ICON = page.locator("//img[contains(@alt,'delete')]");
        this.SELECTED_AD_CHOICES_ICON = page.locator(
                "//span[contains(text(),'AdChoices Icon')]/ancestor::app-info-label/following::div//div[@class='text']");
        this.CREATIVE_WIDTH_TYPE = page.locator("//button[contains(@name,'VideoTechType')]");
        this.DOMAIN_LANDING_FROM_CREATIVE_TILE =
                page.locator("//span[contains(text(),'Domain Landing:')]/following-sibling::span");
        this.ADSIZE_FROM_CREATIVE_TILE = page.locator("//span[contains(text(),'AdSize:')]/following-sibling::span");
        this.DURATION_FROM_CREATIVE_TILE = page.locator("//span[contains(text(),'DURATION:')]/following-sibling::span");
        this.CREATIVE_STATUS_FROM_CREATIVE_TILE = page.locator("//div[contains(@class,'status-label')]//span");
        this.CREATED_BY_FROM_CREATIVE_TILE =
                page.locator("//span[contains(text(),'Created by:')]/following-sibling::span");
        this.SOURCE_FROM_CREATIVE_TILE = page.locator("//span[contains(text(),'Source:')]/following-sibling::span");
        this.LAST_UPDATED_FROM_CREATIVE_TILE =
                page.locator("//span[contains(text(),'Last updated:')]/following-sibling::span");
        this.ASSOCIATIONS_TAB =
                page.locator("//div[contains(@class,'creativeContainer')]//a[contains(text(),'Associations')]");
        this.COLUMN_SELECTION_ICON = page.locator("//span[@class='icon-bars']");
        this.COLUMN_NAME_FROM_SELECTION_ICON =
                page.locator("//span[@class='icon-bars']/following-sibling::div//div[contains(@class,'item')]");
        this.ASSOCIATIONS_TAB_COLUMN_NAME = page.locator("//div[@class='filter-container']");
        this.MENU_BUTTONS_FROM_ASSOCIATION_ICON =
                page.locator("//div[@class='menuButtons']/button[contains(@class,'button small')]");
        this.FILTER_ICON = page.locator("//span[text()='Filter']/ancestor::app-icon-lable-link");
        this.NO_FILTER_TEXT =
                page.locator("//span[text()='Filter']/ancestor::app-icon-lable-link/following-sibling::div//p");
        this.FILTER_BUTTONS =
                page.locator("//span[text()='Filter']/ancestor::app-icon-lable-link/following-sibling::div//button");
        this.SELECTED_FILTER_NAME = page.locator("//table[contains(@class,'filterTable')]//div[@class='text']");
        this.FILTER_VALUE = page.locator("//input[@value='Enter Value']");
        this.FILTER_START_DATE = page.locator("//td[not(@hidden)]//input[contains(@placeholder,'Start Date')]");
        this.FILTER_END_DATE = page.locator("//td[not(@hidden)]//input[contains(@placeholder,'End Date')]");
        this.ASSOCIATIONS_TAB_LINE_ITEM_NAME = page.locator("//td[contains(@class,'gaTableRow creativeNames')]");
        this.ASSOCIATIONS_TAB_CAMPAIGN_NAME =
                page.locator("//td[contains(@class,'multiline-wrapper')]//div[contains(@class,'ellipsediv')]");
        this.FILTERED_RECORD_COUNT = page.locator("//td[contains(@class,'total-record-counts')]");
        this.CALENDAR_TITLE = page.locator("//sui-calendar-view-title//span[@class='title link']");
        this.CALENDAR_MONTH = page.locator("//sui-calendar-month-view//tbody//td");
        this.CALENDAR_DATE = page.locator("//sui-calendar-date-view//tbody//td");
        this.LINE_ITEM_PAGE_TITLE = page.locator("//div[contains(@class,'lineitem-name')]");
        this.NO_CAMPAIGN_FOUND_MESSAGE = page.locator("//div[@class='creative_message']//div[@class='no_campaings']");
        this.BULK_PANEL_CANCEL_BUTTON =
                page.locator("//div[@class='targetingFooterActions']//button[contains(text(),'Cancel')]");
    }

    public String verifyCreativeLibraryPageTitle() {
        waitUtility.waitUntilPreLoaderHidden();
        waitUtility.waitForLocatorVisible(CREATIVE_PAGE_TITLE);
        return CREATIVE_PAGE_TITLE.innerText().trim();
    }

    public void clickActivityButton(String buttonType) {
        Locator button = page.locator(String.format("//div[contains(text(), '%s')]/parent::button", buttonType));
        button.click();
        waitUtility.waitUntilPreLoaderHidden();
        waitUtility.waitForLocatorVisible(CREATIVE_NAME_LIST.last());
        page.waitForTimeout(1000);
    }

    public void clickClearAllButton() {
        CLEAR_ALL_BUTTON.scrollIntoViewIfNeeded();
        CLEAR_ALL_BUTTON.click();
        waitUtility.waitUntilPreLoaderHidden();
        waitUtility.waitForLocatorVisible(CREATIVE_NAME_LIST.last());
    }

    public boolean isArchiveUnarchiveButtonsPresent(String buttonType) {
        boolean flag = false;
        if (buttonType.contains("Active") && ENABLED_ARCHIVED_BUTTON.first().isVisible()
                || DISABLED_ARCHIVED_BUTTON.first().isVisible()) {
            waitUtility.waitUntilPreLoaderHidden();
            flag = true;
        } else if (buttonType.contains("Archived") && UNARCHIVED_BUTTON.first().isVisible()) {
            waitUtility.waitUntilPreLoaderHidden();
            flag = true;
        }
        return flag;
    }

    public String clickArchiveButton() {
        String text = "";
        if (CREATIVE_NAME_LIST.first().isVisible()) {
            text = CREATIVE_NAME_LIST.first().textContent().trim();
        }
        Locator archiveXpath = page.locator(String.format(
                "//div[contains(@class,'name-section-truncate') and @title='%s']/parent::div/following-sibling::div//img[@title='archive']",
                text));
        archiveXpath.click();
        waitUtility.waitUntilPreLoaderHidden();
        return text;
    }

    public String clickUnarchiveButton() {
        String text = "";
        if (CREATIVE_NAME_LIST.first().isVisible()) {
            text = CREATIVE_NAME_LIST.first().textContent().trim();
        }
        Locator archiveXpath = page.locator(String.format(
                "//div[contains(@class,'name-section-truncate') and @title='%s']/parent::div/following-sibling::div//img[@title='unarchive']",
                text));
        archiveXpath.click();
        waitUtility.waitUntilPreLoaderHidden();
        return text;
    }

    public boolean verifyFilterOptions(String key, List<String> values) {
        boolean flag = false;
        Locator keyLocator;
        switch (key) {
            case "Advertiser":
                selectDropdownAndFill(SELECT_ADVERTISER, values);
                for (String value : values) {
                    keyLocator = page.locator(String.format(
                            "//span[@class='label']/following-sibling::span[contains(text(),'%s')]", value));
                    flag = isContentCountMatching(keyLocator);
                }
                clickClearAllButton();
                break;
            case "Associated Campaigns":
                selectDropdownAndFill(SELECT_CAMPAIGN, values);
                for (String value : values) {
                    keyLocator = page.locator(String.format(
                            "//span[@class='label']/following-sibling::span[contains(text(),'%s')]", value));
                    flag = isContentCountMatching(keyLocator);
                }
                clickClearAllButton();
                break;
            case "Creative Status":
                selectDropdownAndFill(CREATIVE_STATUS, values);
                for (String value : values) {
                    keyLocator = page.locator(String.format(
                            "//div[contains(@class,'status-label')]//span[contains(text(),'%s')]", value));
                    flag = isContentCountMatching(keyLocator);
                }
                clickClearAllButton();
                break;
            case "Ad Sizes":
                selectDropdownAndFill(SELECT_AD_SIZE, values);
                for (String value : values) {
                    keyLocator = page.locator(String.format(
                            "//span[@class='label']/following-sibling::span[contains(text(),'%s')]", value));
                    flag = isContentCountMatching(keyLocator);
                }
                clickClearAllButton();
                break;
            case "Creative Type":
                selectDropdownAndFill(SELECT_CREATIVE_TYPE, values);
                flag = isContentCountMatching(CREATIVE_TYPE_ICON);
                clickClearAllButton();
                break;
            case "CreatedBy":
                selectDropdownAndFill(CREATED_BY, values);
                for (String value : values) {
                    keyLocator = page.locator(String.format(
                            "//span[@class='label']/following-sibling::span[contains(text(),'%s')]", value));
                    flag = isContentCountMatching(keyLocator);
                }
                clickClearAllButton();
                break;
        }
        return flag;
    }

    private void selectDropdownAndFill(Locator dropdown, List<String> values) {
        dropdown.click();
        for (String value : values) {
            dropdown.fill(value);
            DROPDOWN_VALUES.first().click();
        }
        page.keyboard().press("Escape");
        waitUtility.waitUntilPreLoaderHidden();
    }

    public boolean checkSortingOrder(String sortBy) {
        String[] parts = sortBy.split("-");
        String column = parts[0].trim();
        String order = parts.length > 1 ? parts[1].trim() : "";
        boolean ascending = order.equalsIgnoreCase("Asc");
        SORT_DROPDOWN.click();
        return switch (column) {
            case "Name" -> {
                if (ascending) SORT_BY_NAME_ASC.click();
                else SORT_BY_NAME_DESC.click();
                waitUtility.waitUntilPreLoaderHidden();
                yield isColumnOrderCorrect(CREATIVE_NAME_LIST, column, ascending);
            }
            case "ID" -> {
                if (ascending) SORT_BY_ID_ASC.click();
                else SORT_BY_ID_DESC.click();
                waitUtility.waitUntilPreLoaderHidden();
                yield isColumnOrderCorrect(CREATIVE_ID_LIST, column, ascending);
            }
            case "Last Updated" -> {
                if (ascending) SORT_BY_LAST_UPDATED_ASC.click();
                else SORT_BY_LAST_UPDATED_DESC.click();
                waitUtility.waitUntilPreLoaderHidden();
                yield isColumnOrderCorrect(CREATIVE_LAST_UPDATED_LIST, column, ascending);
            }
            default -> false;
        };
    }

    public boolean isColumnOrderCorrect(Locator creativeListType, String columnType, boolean ascending) {
        List<String> values = creativeListType.allTextContents().stream()
                .map(text -> {
                    text = text.trim();
                    if (columnType.equals("Name") && text.contains("|")) {
                        String[] parts = text.split("\\|");
                        if (parts.length > 5) {
                            return parts[5].trim();
                        }
                    }
                    return text;
                })
                .toList();

        for (int i = 0; i < values.size() - 1; i++) {
            boolean valid = false;
            switch (columnType) {
                case "Name":
                    int nameCompare = values.get(i).compareToIgnoreCase(values.get(i + 1));
                    valid = ascending ? nameCompare <= 0 : nameCompare >= 0;
                    break;
                case "ID":
                    long currentId = Long.parseLong(values.get(i).replaceAll("\\D+", ""));
                    long nextId = Long.parseLong(values.get(i + 1).replaceAll("\\D+", ""));
                    valid = ascending ? currentId <= nextId : currentId >= nextId;
                    break;
                case "Last Updated":
                    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("M/d/yyyy h:mm")
                            .optionalStart()
                            .appendLiteral(' ')
                            .optionalEnd()
                            .appendPattern("a")
                            .toFormatter();
                    LocalDateTime currentDateTime =
                            LocalDateTime.parse(values.get(i).toUpperCase(), formatter);
                    LocalDateTime nextDateTime =
                            LocalDateTime.parse(values.get(i + 1).toUpperCase(), formatter);
                    valid = ascending
                            ? !currentDateTime.isAfter(nextDateTime)
                            : !currentDateTime.isBefore(nextDateTime);
                    break;
            }
            if (!valid) return false;
        }
        return true;
    }

    public boolean checkSearchedValue(String searchValue) {
        Locator[] locators = {CREATIVE_NAME_LIST, CREATIVE_ID_LIST, CREATIVE_AD_SIZE_LIST, CREATIVE_SOURCE_LIST};
        for (Locator locator : locators) {
            if (locator.filter(new Locator.FilterOptions().setHasText(searchValue))
                            .count()
                    > 0) {
                return true;
            }
        }
        return false;
    }

    public void searchCreative(String searchValue) {
        SEARCH_BOX.fill(searchValue);
        page.keyboard().press("Enter");
        waitUtility.waitUntilPreLoaderHidden();
        if (CREATIVE_NAME_LIST.last().isVisible()) waitUtility.waitForLocatorVisible(CREATIVE_NAME_LIST.last());
    }

    public void clearSearchBox() {
        SEARCH_BOX.clear();
        page.keyboard().press("Enter");
        waitUtility.waitUntilPreLoaderHidden();
        waitUtility.waitForLocatorVisible(CREATIVE_NAME_LIST.last());
    }

    public void clickCopyCreative(String creativeName) {
        Locator copyLocator = page.locator(String.format(
                "//div[@title='%s']/parent::div/following-sibling::div//span[@title='copy']", creativeName));
        copyLocator.first().click();
        page.waitForTimeout(2000);
        waitUtility.waitUntilSpinnerHidden();
        waitUtility.waitForLocatorVisible(CREATIVE_NAME);
    }

    public void clickSearchedCreative(String creativeName) {
        for (int i = 0; i < CREATIVE_NAME_LIST.count(); i++) {
            if (CREATIVE_NAME_LIST.nth(i).textContent().equalsIgnoreCase(creativeName)) {
                CREATIVE_NAME_LIST.nth(i).click();
                page.waitForTimeout(2000);
                break;
            }
        }
        waitUtility.waitUntilPreLoaderHidden();
        waitUtility.waitForLocatorVisible(CREATIVE_NAME);
    }

    public void enterCreativeName(String creativeName) {
        CREATIVE_NAME.fill(creativeName);
    }

    public void clickNewCreativeButton() {
        NEW_CREATIVE_BUTTON.click();
        waitUtility.waitForLocatorVisible(CREATIVE_HEADER);
    }

    public void enterCreativeDetails(String advertiser, String creativeName, String advertiserDSA, String financer) {
        ADVERTISER_DROPDOWN.fill(advertiser);
        ADVERTISER_DROPDOWN_VALUES.first().click();
        CREATIVE_NAME.fill(creativeName);
        ADVERTISER_DSA.scrollIntoViewIfNeeded();
        ADVERTISER_DSA.fill(advertiserDSA);
        FINANCER.scrollIntoViewIfNeeded();
        FINANCER.fill(financer);
        waitUtility.waitUntilPreLoaderHidden();
    }

    public void selectCreativeType(String creativeType) {
        for (int i = 0; i < CREATIVE_TYPE.count(); i++) {
            if (CREATIVE_TYPE.nth(i).innerText().trim().equalsIgnoreCase(creativeType)) {
                CREATIVE_TYPE.nth(i).click();
                break;
            }
        }
    }

    public void fillAttributes(String type, Map<String, String> attributeMap) {
        switch (type) {
            case "Html", "Html5", "Image":
                page.locator(String.format("button[role='tab']:text-is('%s')", type)).click();
                if (type.equals("Html5")) {
                    CommonUtils.uploadFile(page, 0, imageTextLocator, attributeMap.get("ArchiveFile"));
                }
                if (type.contains("Image")) {
                    CommonUtils.uploadFile(page, 0, imageTextLocator, attributeMap.get("ImageFile"));
                }
                if (type.equals("Html")) {
                    CREATIVE_HTML_CODE.fill(attributeMap.get("HTMLCode"));
                    MACROS_CHECKBOX.click();
                }
                CREATIVE_AD_SIZE.click();
                CommonUtils.selectAndClickElement(
                        CREATIVE_AD_SIZE_VALUE, Collections.singletonList(attributeMap.get("Size")));
                if (CLICK_THROUGH_URL.isEditable()) {
                    CLICK_THROUGH_URL.fill(attributeMap.get("ClickThroughURL"));
                }
                DOMAIN_LANDING.fill(attributeMap.get("DomainLanding"));
                break;

            case "Upload", "Audio URL", "VAST URL", "VAST XML", "Video URL":
                page.locator(String.format("button[role='tab']:text-is('%s')", type)).click();
                if (type.contains("Upload")) {
                    CommonUtils.uploadFile(page, 0, imageTextLocator, attributeMap.get("FileName"));
                    if (UPLOADING_PROGRESS_BAR.isVisible()) {
                        waitUtility.waitForLocatorHidden(UPLOADING_PROGRESS_BAR);
                        waitUtility.waitUntilSpinnerHidden();
                        waitUtility.waitForLocatorVisible(UPLOAD_DELETE_ICON);
                    } else {
                        waitUtility.waitUntilSpinnerHidden();
                        waitUtility.waitForLocatorVisible(page.locator(String.format("//span[contains(text(),'%s')]", attributeMap.get("FileName"))));
                    }
                }
                else if (type.contains("Audio URL") || type.contains("Video URL")) URL.fill(attributeMap.get("URL"));
                else if (type.contains("VAST URL")) URL.fill(attributeMap.get("VASTURL"));
                else VAST_XML_TEXTAREA.fill(attributeMap.get("VASTXML"));
                if (type.contains("Audio URL")
                        || type.contains("Video URL")
                        || type.contains("VAST URL")
                        || type.contains("VAST XML")) {
                    if (CREATIVE_WIDTH_TYPE.first().isVisible()) {
                        CommonUtils.selectAndClickElement(
                                CREATIVE_WIDTH_TYPE, Collections.singletonList(attributeMap.get("Type")));
                    }
                    DURATION.fill(attributeMap.get("Durations"));
                    if (WIDTH.isVisible() && HEIGHT.isVisible()) {
                        WIDTH.fill(attributeMap.get("Width"));
                        HEIGHT.fill(attributeMap.get("Height"));
                    }
                }
                if (CLICK_THROUGH_URL.isVisible()) CLICK_THROUGH_URL.fill(attributeMap.get("ClickThroughURL"));
                DOMAIN_LANDING.fill(attributeMap.get("AdvertiserDomain"));
                IAB_CATEGORY.fill(attributeMap.get("IAB"));
                IAB_CATEGORY_VALUE.first().click();
                if (HEADLINE.isVisible()) HEADLINE.fill(attributeMap.get("Headline"));
                if (SPONSORED_BY.isVisible()) SPONSORED_BY.fill(attributeMap.get("SponsoredBy"));
                if (PRODUCT_DESCRIPTION.isVisible()) PRODUCT_DESCRIPTION.fill(attributeMap.get("Description"));
                if (DISPLAY_URL.isVisible()) DISPLAY_URL.fill(attributeMap.get("DisplayURL"));
                // Adding below two conditions since Duration and URL are getting blanked automatically
                if(DURATION.isVisible() && DURATION.textContent().isEmpty() && attributeMap.get("Durations")!=null && !attributeMap.get("Durations").isEmpty()) DURATION.fill(attributeMap.get("Durations"));
                if(URL.isVisible() && URL.textContent().isEmpty() && attributeMap.get("URL")!=null && !attributeMap.get("URL").isEmpty()) URL.fill(attributeMap.get("URL"));
                break;

            case "Search":
                CREATIVE_AD_SIZE.click();
                CommonUtils.selectAndClickElement(
                        CREATIVE_AD_SIZE_VALUE, Collections.singletonList(attributeMap.get("Size")));
                HEADLINE.fill(attributeMap.get("Headline"));
                DESCRIPTION.fill(attributeMap.get("Description"));
                DISPLAY_URL.fill(attributeMap.get("DisplayURL"));
                CLICK_THROUGH_URL.fill(attributeMap.get("ClickThroughURL"));
                IAB_CATEGORY.fill(attributeMap.get("IAB"));
                IAB_CATEGORY_VALUE.first().click();
                break;

            case "Native Display":
                CommonUtils.uploadFile(page, 0, imageTextLocator, attributeMap.get("ImageFile"));
                CLICK_THROUGH_URL.fill(attributeMap.get("ClickThroughURL"));
                DOMAIN_LANDING.fill(attributeMap.get("DomainLanding"));
                IAB_CATEGORY.fill(attributeMap.get("IAB"));
                IAB_CATEGORY_VALUE.first().click();
                HEADLINE.fill(attributeMap.get("Headline"));
                SPONSORED_BY.fill(attributeMap.get("SponsoredBy"));
                PRODUCT_DESCRIPTION.fill(attributeMap.get("Description"));
                DISPLAY_URL.fill(attributeMap.get("DisplayURL"));
        }
    }

    public String saveCreative() {
        OK_BUTTON.click();
        String text = SUCCESS_ALERT.innerText().trim();
        waitUtility.waitForLocatorHidden(SUCCESS_ALERT);
        return text;
    }

    public String fetchCreativeName(String message) {
        return message.replaceAll("\\b(Creative|created)\\b\\p{Punct}?", "")
                .trim()
                .replaceAll(" +", " ");
    }

    public boolean verifyCreativesInLibrary(String name) {
        searchCreative(name);
        Locator locator = page.locator(String.format("//div[@title='%s']", name));
        return locator.innerText().equalsIgnoreCase(name);
    }

    public String fetchRecordsNumberAfterSearch() {
        return page.locator("//div[contains(@class,'total-record')]")
                .textContent()
                .trim();
    }

    public boolean showsActiveCreativesWhenActiveClicked() {
        return isContentCountMatching(ARCHIVED_BUTTON);
    }

    public boolean fetchCreativeCount() {
        return isContentCountMatching(CREATIVE_NAME_LIST);
    }

    public boolean isContentCountMatching(Locator locator) {
        int expectedCount = ITEMS_PER_PAGE.count();
        if (locator.count() != expectedCount) {
            return false;
        }
        if (expectedCount
                        >= Integer.parseInt(
                                ITEMS_PER_PAGE_DROPDOWN.textContent().trim())
                && PAGINATION_NEXT_BUTTON.isVisible()) {
            navigateToNextCreativePage();
            expectedCount = ITEMS_PER_PAGE.count();
            return locator.count() == expectedCount;
        }
        return true;
    }

    public void selectPaginationItemsPerPage(String itemsCount) {
        ITEMS_PER_PAGE_DROPDOWN.click();
        Locator itemsLocator = ITEMS_PER_PAGE_DROPDOWN.locator(
                String.format("xpath=following-sibling::div//div[normalize-space(text())='%s']", itemsCount));
        itemsLocator.click();
        waitUtility.waitUntilPreLoaderHidden();
        waitUtility.waitForLocatorVisible(CREATIVE_NAME_LIST.last());
    }

    public void navigateToNextCreativePage() {
        if (PAGINATION_NEXT_BUTTON
                .locator("xpath=./parent::button")
                .getAttribute("class")
                .contains("disabledButton")) {
            return;
        }
        PAGINATION_NEXT_BUTTON.click();
        waitUtility.waitUntilPreLoaderHidden();
        waitUtility.waitForLocatorVisible(page.locator("//td[contains(normalize-space(.), 'of')]"));
    }

    public void navigateToFirstCreativePage() {
        Locator pageLocator = page.locator("//td[contains(normalize-space(.), 'of')]");
        while (!pageLocator.textContent().trim().contains("1 of")) {
            PAGINATION_PREVIOUS_BUTTON.click();
            waitUtility.waitUntilPreLoaderHidden();
        }
    }

    public boolean showsArchivedCreativesWhenArchivedClicked() {
        return isContentCountMatching(UNARCHIVED_BUTTON);
    }

    public List<String> fetchCreativeDetails() {
        List<String> creativeDetails = new ArrayList<>();
        creativeDetails.add(ADVERTISER_DROPDOWN
                .locator("xpath = ./following-sibling::span")
                .textContent()
                .trim());
        if (CREATIVE_NAME.isVisible())
            creativeDetails.add(CREATIVE_NAME.inputValue().trim());
        for (int i = 0; i < APPROVAL_STATUS_BUTTON.count(); i++) {
            Locator tab = APPROVAL_STATUS_BUTTON.nth(i);
            if (tab.getAttribute("aria-selected") != null) {
                creativeDetails.add(tab.textContent().trim());
                break;
            }
        }
        creativeDetails.add(ADVERTISER_DSA.inputValue().trim());
        creativeDetails.add(FINANCER.inputValue().trim());
        for (int i = 0; i < CREATIVE_TYPE.count(); i++) {
            if (CREATIVE_TYPE
                    .locator("xpath = ./parent::div")
                    .nth(i)
                    .getAttribute("class")
                    .contains("active")) {
                creativeDetails.add(CREATIVE_TYPE.nth(i).textContent().trim());
                break;
            }
        }
        for (int i = 0; i < CREATIVE_FREQUENCY_OPTIONS.count(); i++) {
            if (CREATIVE_FREQUENCY_OPTIONS.nth(i).getAttribute("class").contains("active")) {
                creativeDetails.add(
                        CREATIVE_FREQUENCY_OPTIONS.nth(i).textContent().trim());
                break;
            }
        }
        if (SELECTED_AD_SIZE.isVisible())
            creativeDetails.add(SELECTED_AD_SIZE.textContent().trim());
        if (CLICK_THROUGH_URL.isVisible() && CLICK_THROUGH_URL.isEnabled())
            creativeDetails.add(CLICK_THROUGH_URL.inputValue().trim());
        if (DOMAIN_LANDING.isVisible())
            creativeDetails.add(DOMAIN_LANDING.inputValue().trim());
        if (DURATION.isVisible()) {
            String duration = DURATION.inputValue();
            creativeDetails.add((duration == null || duration.trim().isEmpty()) ? "0" : duration.trim());
        }
        if (SELECTED_IAB_CATEGORY.first().isVisible())
            creativeDetails.add(SELECTED_IAB_CATEGORY.first().textContent().trim());
        if (HEIGHT.isVisible() && WIDTH.isVisible()) {
            creativeDetails.add(HEIGHT.inputValue().trim());
            creativeDetails.add(WIDTH.inputValue().trim());
        }
        if (HEADLINE.isVisible()) creativeDetails.add(HEADLINE.inputValue().trim());
        if (SPONSORED_BY.isVisible())
            creativeDetails.add(SPONSORED_BY.inputValue().trim());
        if (DESCRIPTION.isVisible())
            creativeDetails.add(DESCRIPTION.inputValue().trim());
        if (PRODUCT_DESCRIPTION.isVisible())
            creativeDetails.add(PRODUCT_DESCRIPTION.inputValue().trim());
        if (SELECTED_AD_CHOICES_ICON.isVisible())
            creativeDetails.add(SELECTED_AD_CHOICES_ICON.textContent().trim());
        if (DISPLAY_URL.isVisible())
            creativeDetails.add(DISPLAY_URL.inputValue().trim());
        return creativeDetails;
    }

    public void clickCancelButton() {
        CANCEL_BUTTON.click();
        waitUtility.waitForLocatorVisible(CREATIVE_PAGE_TITLE);
    }

    public List<String> fetchFilterValues(String key) {
        return switch (key) {
            case "Creative Status" -> {
                CREATIVE_STATUS.click();
                List<String> values = DROPDOWN_VALUES.allTextContents().stream()
                        .map(String::trim)
                        .toList();
                page.keyboard().press("Escape");
                yield values;
            }
            case "Ad Sizes" -> {
                SELECT_AD_SIZE.click();
                List<String> values = DROPDOWN_VALUES.allTextContents().stream()
                        .map(String::trim)
                        .toList();
                page.keyboard().press("Escape");
                yield values;
            }
            case "Creative Type" -> {
                SELECT_CREATIVE_TYPE.click();
                List<String> values = DROPDOWN_VALUES.allTextContents().stream()
                        .map(String::trim)
                        .toList();
                page.keyboard().press("Escape");
                yield values;
            }
            default -> List.of();
        };
    }

    public String selectCheckboxWithArchiveButton() {
        return clickCheckboxAndFetchCreativeName(ENABLED_ARCHIVED_BUTTON);
    }

    public String selectCheckboxWithCreativeStatusLabel() {
        return clickCheckboxAndFetchCreativeName(CREATIVE_STATUS_LABEL);
    }

    public String clickCreativeTypeIconAndFetchCreativeName() {
        String text = "";
        checkIfCreativeIsPresent(CREATIVE_TYPE_ICON);
        for (int i = 0; i < CREATIVE_TYPE_ICON.count(); i++) {
            if (CREATIVE_TYPE_ICON.nth(i).isVisible()) {
                final int index = i;
                text = CREATIVE_TYPE_ICON
                        .nth(i)
                        .locator("xpath=.//following-sibling::div//div[contains(@class,'name-section-truncate')]")
                        .textContent()
                        .trim();
                newTab = DriverFactory.getContext().waitForPage(() -> {
                    CREATIVE_TYPE_ICON.nth(index).click();
                });
                newTab.waitForLoadState();
                newTab.bringToFront();
                DriverFactory.threadLocalDriver.set(newTab);
                return text;
            }
        }
        return text;
    }

    public void clickPreviewLinkFromCreativeDetailsPage() {
        newTab = DriverFactory.getContext().waitForPage(PREVIEW_LINK::click);
        newTab.waitForLoadState();
        newTab.bringToFront();
        DriverFactory.threadLocalDriver.set(newTab);
    }

    public void checkIfCreativeIsPresent(Locator locator) {
        while (locator.count() == 0) {
            PAGINATION_NEXT_BUTTON.scrollIntoViewIfNeeded();
            PAGINATION_NEXT_BUTTON.click();
            waitUtility.waitUntilPreLoaderHidden();
            waitUtility.waitForLocatorVisible(CREATIVE_NAME_LIST.last());
        }
    }

    public String clickCheckboxAndFetchCreativeName(Locator locator) {
        checkIfCreativeIsPresent(locator);
        for (int i = 0; i < locator.count(); i++) {
            if (locator.nth(i).isVisible()) {
                Locator checkBoxLocator = locator.nth(i)
                        .locator(
                                "xpath=.//ancestor::div[contains(@class,'content-section')]/preceding-sibling::div//sui-checkbox");
                String checkboxClass = checkBoxLocator.getAttribute("class");
                if (checkboxClass != null && checkboxClass.contains("checked")) {
                    continue;
                }
                checkBoxLocator.click();
                return checkBoxLocator
                        .locator(
                                "xpath=parent::div/following-sibling::div//div[contains(@class,'name-section-truncate')]")
                        .textContent()
                        .trim();
            }
        }
        return "";
    }

    public void clickBulkActionsButton() {
        BULK_ACTIONS_BUTTON.click();
    }

    public void selectBulkActionsOption(String bulkAssign) {
        if (bulkAssign.contains("Bulk Assign") || bulkAssign.contains("Bulk Edit"))
            BULK_ACTIONS_BUTTON
                    .locator("xpath=//div//a//div[normalize-space(.)='" + bulkAssign + "']")
                    .click();
        else
            BULK_ACTIONS_BUTTON
                    .locator("xpath=//div//a[normalize-space(.)='" + bulkAssign + "']")
                    .click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public String assignCampaignToCreative() {
        waitUtility.waitForLocatorVisible(BULK_ASSIGN_CREATIVE_HEADER);
        int count = BULK_ASSIGN_CHECKBOX.count();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices);
        int selectCount = Math.min(5, count);
        for (int i = count - 1; i >= count - selectCount; i--) {
            if (BULK_ASSIGN_CHECKBOX.nth(i).isVisible()) {
                BULK_ASSIGN_CHECKBOX.nth(i).click();
            }
        }
        OK_BUTTON.click();
        waitUtility.waitUntilSpinnerHidden();
        String text = SUCCESS_ALERT.innerText().trim();
        waitUtility.waitForLocatorHidden(SUCCESS_ALERT);
        return text;
    }

    public String fetchTooltipTextForAssignedCampaigns() {
        DISABLED_ARCHIVED_BUTTON.first().hover();
        return FETCH_TOOLTIP_TEXT_FOR_ASSIGNED_CAMPAIGNS.textContent().trim();
    }

    public String deleteCreative() {
        String text = "";
        if (DELETE_ICON.getAttribute("class").contains("disabled"))
            text = "Delete icon is disabled, cannot delete the creative.";
        else {
            DELETE_ICON.click();
            waitUtility.waitForLocatorVisible(REMOVAL_CONFIRMATION_POP_UP);
            text = REMOVAL_CONFIRMATION_TEXT.textContent().trim();
            REMOVE_BUTTON.click();
            waitUtility.waitUntilSpinnerHidden();
        }
        return text;
    }

    public String fetchNoCreativeFoundMessage() {
        return NO_CREATIVE_FOUND_MESSAGE.textContent().trim();
    }

    public void selectCreativeStatus(List<String> statusList) {
        selectDropdownAndFill(CREATIVE_STATUS, statusList);
        waitUtility.waitUntilPreLoaderHidden();
        waitUtility.waitForLocatorVisible(CREATIVE_NAME_LIST.last());
    }

    public boolean performBulkApproval() {
        boolean flag = false;
        waitUtility.waitForLocatorVisible(APPROVE_ALL_BUTTON);
        APPROVE_ALL_BUTTON.click();
        for (int i = 0; i < APPROVED_BUTTON.count(); i++) {
            if (APPROVED_BUTTON.nth(i).getAttribute("class").contains("active")) flag = true;
        }
        OK_BUTTON.click();
        page.waitForTimeout(2000);
        waitUtility.waitUntilPreLoaderHidden();
        waitUtility.waitForLocatorVisible(CREATIVE_NAME_LIST.last());
        return flag;
    }

    public String fetchCreativeStatusLabel() {
        return CREATIVE_STATUS_LABEL.first().textContent().trim();
    }

    public String isCreativePreviewTabDisplayed() {
        waitUtility.waitForLocatorVisible(newTab.locator("xpath=//div[@class='pagetitle']"));
        return newTab.locator("xpath=//div[@class='pagetitle']").textContent().trim();
    }

    public String fetchCreativeNameFromPreviewTab() {
        return newTab.locator("xpath=//span[text()='Creative Name:']/parent::div")
                .textContent()
                .trim();
    }

    public void closeCreativePreviewTab() {
        newTab.close();
    }

    public List<String> fetchCreativeDetailsFromCreativeTile() {
        List<String> creativeDetails = new ArrayList<>();
        creativeDetails.add(CREATIVE_NAME_LIST.textContent().trim());
        creativeDetails.add(DOMAIN_LANDING_FROM_CREATIVE_TILE.textContent().trim());
        if (ADSIZE_FROM_CREATIVE_TILE.isVisible()) {
            String size =
                    ADSIZE_FROM_CREATIVE_TILE.textContent().replace("px", "").trim();
            creativeDetails.add(size);
        }
        if (DURATION_FROM_CREATIVE_TILE.isVisible()) {
            String duration = DURATION_FROM_CREATIVE_TILE.inputValue();
            creativeDetails.add((duration == null || duration.trim().isEmpty()) ? "0" : duration.trim());
        }
        creativeDetails.add(CREATIVE_STATUS_FROM_CREATIVE_TILE.textContent().trim());
        return creativeDetails;
    }

    public String fetchCreatedByFromCreativeTile() {
        return CREATED_BY_FROM_CREATIVE_TILE.textContent().trim();
    }

    public String fetchSourceFromCreativeTile() {
        return SOURCE_FROM_CREATIVE_TILE.textContent().trim();
    }

    public String fetchLastUpdatedTimeFromCreativeTile() {
        return LAST_UPDATED_FROM_CREATIVE_TILE.textContent().trim();
    }

    public void clickAssociationTab() {
        ASSOCIATIONS_TAB.click();
    }

    public void clickColumnSelectionIcon() {
        if (!COLUMN_NAME_FROM_SELECTION_ICON.first().isVisible()) COLUMN_SELECTION_ICON.click();
    }

    public List<String> fetchColumnNamesFromSelectionIconList() {
        List<String> columnNames = new ArrayList<>();
        for (int i = 0; i < COLUMN_NAME_FROM_SELECTION_ICON.count(); i++) {
            columnNames.add(COLUMN_NAME_FROM_SELECTION_ICON.nth(i).textContent().trim());
        }
        return columnNames;
    }

    public void deselectColumnNamesFromSelectionIconList(List<String> unselectedColumnNames) {
        for (String columnName : unselectedColumnNames) {
            for (int i = 0; i < COLUMN_NAME_FROM_SELECTION_ICON.count(); i++) {
                if (COLUMN_NAME_FROM_SELECTION_ICON.nth(i).textContent().trim().equalsIgnoreCase(columnName)) {
                    COLUMN_NAME_FROM_SELECTION_ICON.nth(i).click();
                    break;
                }
            }
        }
        page.keyboard().press("Escape");
    }

    public List<String> fetchColumnNamesFromAssociationsTab() {
        List<String> columnNames = new ArrayList<>();
        for (int i = 0; i < ASSOCIATIONS_TAB_COLUMN_NAME.count(); i++) {
            Locator columnHeader = ASSOCIATIONS_TAB_COLUMN_NAME.nth(i).locator("xpath=./parent::th");
            if (columnHeader.getAttribute("hidden") == null)
                columnNames.add(
                        ASSOCIATIONS_TAB_COLUMN_NAME.nth(i).textContent().trim());
        }
        return columnNames;
    }

    public void clickMenuButtonFromColumnSelection(String buttonName) {
        for (int i = 0; i < MENU_BUTTONS_FROM_ASSOCIATION_ICON.count(); i++) {
            if (MENU_BUTTONS_FROM_ASSOCIATION_ICON.nth(i).textContent().trim().equalsIgnoreCase(buttonName)) {
                MENU_BUTTONS_FROM_ASSOCIATION_ICON.nth(i).click();
                break;
            }
        }
    }

    public void clickFilterIcon() {
        FILTER_ICON.click();
    }

    public List<String> fetchFilterFields() {
        List<String> filterFields = new ArrayList<>();
        filterFields.add(NO_FILTER_TEXT.textContent().trim());
        for (int i = 0; i < FILTER_BUTTONS.count(); i++) {
            filterFields.add(FILTER_BUTTONS.nth(i).textContent().trim());
        }
        return filterFields;
    }

    public void clickFilterButton(String buttonName) {
        FILTER_BUTTONS.getByText(buttonName).scrollIntoViewIfNeeded();
        FILTER_BUTTONS.getByText(buttonName).click();
    }

    public void selectFilterName(String name) {
        for (int i = 0; i < SELECTED_FILTER_NAME.count(); i++) {
            String text = SELECTED_FILTER_NAME.nth(i).textContent().trim();
            if (text.equalsIgnoreCase(name)) {
                return;
            }
        }
    }

    public String fetchLineItemFromAssociation() {
        return ASSOCIATIONS_TAB_LINE_ITEM_NAME.first().textContent().trim();
    }

    public String fetchCampaignFromAssociation() {
        return ASSOCIATIONS_TAB_CAMPAIGN_NAME.first().textContent().trim();
    }

    public void enterFilterValue(String name) {
        for (int i = 0; i < FILTER_VALUE.count(); i++) {
            if (FILTER_VALUE.nth(i).inputValue().isEmpty()) FILTER_VALUE.nth(i).fill(name);
        }
    }

    public void enterDates() {
        FILTER_START_DATE.click();
        CALENDAR_TITLE.click();
        CALENDAR_MONTH.getByText("Jan").click();
        CALENDAR_DATE
                .getByText("1", new Locator.GetByTextOptions().setExact(true))
                .first()
                .click();

        FILTER_END_DATE.click();
        CALENDAR_TITLE.scrollIntoViewIfNeeded();
        CALENDAR_TITLE.click();
        CALENDAR_MONTH.getByText("Dec").scrollIntoViewIfNeeded();
        CALENDAR_MONTH.getByText("Dec").click();
        CALENDAR_DATE
                .getByText("31", new Locator.GetByTextOptions().setExact(true))
                .first()
                .scrollIntoViewIfNeeded();
        CALENDAR_DATE
                .getByText("31", new Locator.GetByTextOptions().setExact(true))
                .first()
                .click();
    }

    public String fetchFilteredRecordsCount() {
        waitUtility.waitUntilSpinnerHidden();
        return FILTERED_RECORD_COUNT.textContent().trim();
    }

    public String clickLineItemName(String lineItemName) {
        page.locator(String.format("//div[contains(@title,'%s')]", lineItemName))
                .click();
        waitUtility.waitForLocatorVisible(LINE_ITEM_PAGE_TITLE);
        return LINE_ITEM_PAGE_TITLE.textContent().trim();
    }

    public boolean isNoCampaignFoundMessageDisplayed() {
        return NO_CAMPAIGN_FOUND_MESSAGE.isVisible();
    }

    public void selectAdvertiser(List<String> advertiser) {
        SELECT_ADVERTISER.click();
        DROPDOWN_VALUES.locator("text=" + advertiser.getFirst()).click();
        page.keyboard().press("Escape");
        page.waitForTimeout(1000);
        waitUtility.waitUntilPreLoaderHidden();
        waitUtility.waitForLocatorVisible(CREATIVE_NAME_LIST.last());
    }

    public void clickBulkPanelCancelButton() {
        BULK_PANEL_CANCEL_BUTTON.click();
        waitUtility.waitForLocatorVisible(CREATIVE_NAME_LIST.last());
    }
}
