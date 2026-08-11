package pages.life;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import factory.DriverFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import utils.CommonUtils;
import utils.Constants;
import utils.WaitUtility;

public class BulkCreativeUpload {
    private final Page page;
    private final Locator BULK_UPLOAD_BUTTON;
    private final Locator BULK_UPLOAD_CREATIVE_HEADER;
    private final Locator BULK_UPLOAD_HEADER;
    private final Locator CREATIVE_TYPE_BUTTON;
    private final Locator ADVERTISER_DROPDOWN;
    private final Locator ADVERTISER_DROPDOWN_VALUE;
    private final Locator APPROVAL_STATUS_BUTTON;
    private final Locator PREVIEW_BUTTON;
    private final Locator OK_BUTTON;
    private final Locator ERROR_ALERT;
    private final Locator CREATIVE_TEXT_DETAILS_FROM_TABLE;
    private final Locator HEADER_MESSAGE;
    private final Locator DROPDOWN_SEARCH;
    private final Locator DOWNLOAD_BLANK_TEMPLATE;
    private final Locator IMAGE_FILE_BROWSE_BUTTON;
    private final Locator TEMPLATE_WITH_URL_LINK;
    private final Locator CAMPAIGN_DROPDOWN;
    private final Locator CAMPAIGN_DROPDOWN_VALUE;
    private final Locator THIRD_PARTY_TRACKING_PIXEL;
    private final Locator DOUBLE_VERIFY_PIXEL;
    private final Locator INPUT_THIRD_PARTY_TRACKING_PIXEL;
    private final Locator DOUBLE_VERIFY_TEXTAREA;
    private final Locator DELETE_ICON;
    private final Locator REMOVE_FILE_ICON;
    private final Locator TEMPLATE_BROWSE_BUTTON;
    private final Locator SUCCESS_ALERT;
    private final Locator LANDING_PAGE_DOMAIN;
    private final Locator FILE_DROPDOWN;
    private final Locator FILE_DROPDOWN_VALUE;
    private final Locator AD_CHOICES_DROPDOWN;
    private final Locator NOTES_DROPDOWN;
    private final Locator RICH_MEDIA_CHECKBOX;
    private final Locator DIRECTION_DROPDOWN;
    private final Locator DIRECTION_DROPDOWN_VALUE;
    private final Locator HTML_CREATIVE_NAME;
    private final Locator IAB_CATEGORY_DROPDOWN;
    private final Locator INLINE_VALIDATION_MESSAGE;
    private final Locator WIDTH_BOX;
    private final Locator HEIGHT_BOX;
    private final Locator UPLOAD_BUTTON;
    private final Locator VALIDATION_ERROR;
    private final Locator TOOL_TIP_TEXT;
    private final Locator WARNING_IMAGE_ICON;
    private final Locator CREATIVE_DROPDOWN_DETAILS_FROM_TABLE;
    private final Locator DOWNLOAD_BULK_UPLOAD_TEMPLATE;
    private final Locator DURATION;
    private final Locator DSA_REQUIREMENTS_HEADER;
    private final Locator ADVERTISER_DSA;
    private final Locator FINANCER;
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());
    CreateCreatives createCreatives = new CreateCreatives(DriverFactory.getPage());
    Constants constants = new Constants();

    public BulkCreativeUpload(Page page) {
        this.page = page;
        this.BULK_UPLOAD_BUTTON = page.locator("//button[contains(text(),'Bulk Upload')]");
        this.BULK_UPLOAD_CREATIVE_HEADER = page.locator("//div[contains(text(),'Bulk Creative Upload')]");
        this.CREATIVE_TYPE_BUTTON =
                page.locator("//label[contains(text(),'Creative Type')]/following-sibling::div//button");
        this.ADVERTISER_DROPDOWN = page.locator("//sui-select[contains(@placeholder,'Select Advertiser')]");
        this.ADVERTISER_DROPDOWN_VALUE =
                page.locator("//div[@suidropdownmenu]//sui-select-option/span[2]"); //no unique attribute in span tag hence index has been used
        this.APPROVAL_STATUS_BUTTON =
                page.locator("//label[contains(text(),'Approval Status')]/following-sibling::div//button");
        this.PREVIEW_BUTTON = page.locator("//button[contains(text(),'Preview')]");
        this.OK_BUTTON = page.locator("//button[contains(text(),'Ok')]");
        this.ERROR_ALERT = page.locator(
                "//div[@role='alert' and contains(@aria-label,'Atleast one creative should be selected') or contains(@aria-label,'Select Advertiser') or contains(@aria-label,'Landing Page Domain is required') or contains(@aria-label, 'Landing Page Domain is not valid.') or contains(@aria-label,'1 error')]");
        this.SUCCESS_ALERT = page.locator("//div[@aria-label='Success!']/following-sibling::div[@role='alert']");
        this.BULK_UPLOAD_HEADER =
                page.locator("//div[contains(@class,'main-heading') and (contains(text(),'Bulk Upload'))]");
        this.CREATIVE_TEXT_DETAILS_FROM_TABLE = page.locator("//tbody//span/input");
        this.HEADER_MESSAGE = page.locator("//div[contains(@class,'appr-status-label')]/span");
        this.DROPDOWN_SEARCH = page.locator("//div[@id='campaignLookup' and contains(@class,'loading')]");
        this.DOWNLOAD_BLANK_TEMPLATE = page.locator("//span[contains(text(),'Blank Template')]");
        this.IMAGE_FILE_BROWSE_BUTTON = page.locator(
                "//span[contains(text(),'Upload Images to Get Template With URLs')]/ancestor::div//div[contains(@class,'drop-wrapper')]");
        this.TEMPLATE_WITH_URL_LINK = page.locator("//span[contains(text(),'Template With URLs')]");
        this.CAMPAIGN_DROPDOWN = page.locator("//div[contains(@id,'campaignLookup')]/input[@placeholder='Search']");
        this.CAMPAIGN_DROPDOWN_VALUE = page.locator("//div[contains(@class,'menu transition visible')]/div");
        this.THIRD_PARTY_TRACKING_PIXEL = page.locator("//div[contains(@class,'leftAddThirdPartyTrackingPixel')]");
        this.DOUBLE_VERIFY_PIXEL = page.locator("//div[contains(@class,'rightAddDoubleVerifyPixel')]");
        this.INPUT_THIRD_PARTY_TRACKING_PIXEL = page.locator("//div[contains(@class,'fluid input')]/input");
        this.DOUBLE_VERIFY_TEXTAREA = page.locator("//textarea[contains(@class,'textarea')]");
        this.DELETE_ICON = page.locator("//div[contains(@class,'successTP')]/div[contains(@title,'delete')]");
        this.REMOVE_FILE_ICON = page.locator("//span[text()='Remove File']");
        this.TEMPLATE_BROWSE_BUTTON = page.locator(
                "//span[contains(text(),'Spreadsheet')]/ancestor::div//div[contains(@class,'drop-wrapper')]");
        this.LANDING_PAGE_DOMAIN = page.locator("//input[@formcontrolname='landingDomain']");
        this.FILE_DROPDOWN = page.locator("//label[text()='File']/parent::div//div[contains(@class,'text')]");
        this.FILE_DROPDOWN_VALUE = page.locator(
                "//label[text()='File']/following-sibling::div//div[contains(@class,'text')]/following-sibling::div[contains(@class,'menu transition visible')]/div");
        this.AD_CHOICES_DROPDOWN = page.locator(
                "//span[contains(text(),'AdChoices Icon')]/ancestor::app-info-label/following-sibling::div//div[@class='text']");
        this.NOTES_DROPDOWN = page.locator(
                "//span[contains(text(),'Notes Column')]/ancestor::app-info-label/following-sibling::div//div[@class='text']");
        this.RICH_MEDIA_CHECKBOX = page.locator("//sui-checkbox[@formcontrolname='expandable']");
        this.DIRECTION_DROPDOWN = page.locator("//div[contains(text(),'Direction')]");
        this.DIRECTION_DROPDOWN_VALUE = page.locator(
                "//div[contains(text(),'Direction')]/following-sibling::div[contains(@class,'menu transition visible')]/div");
        this.HTML_CREATIVE_NAME = page.locator("//input[@placeholder='Creative Name']");
        this.IAB_CATEGORY_DROPDOWN =
                page.locator("//sui-multi-select[contains(@placeholder,'Search Categories')]//input");
        this.INLINE_VALIDATION_MESSAGE = page.locator("//p[contains(@class,'ng-star-inserted')]");
        this.WIDTH_BOX = page.locator("//input[contains(@placeholder,'width')]");
        this.HEIGHT_BOX = page.locator("//input[contains(@placeholder, 'height')]");
        this.UPLOAD_BUTTON = page.locator("//button[contains(@class,'okButton') and contains(text(),'Upload')]");
        this.VALIDATION_ERROR = page.locator("//div[contains(@class,'validation-erros')]");
        this.TOOL_TIP_TEXT = page.locator("//div[contains(@class,'tooltip-row')]");
        this.WARNING_IMAGE_ICON =
                page.locator("//div[@class='preview-table-container']//img[contains(@src,'warning.svg')]");
        this.CREATIVE_DROPDOWN_DETAILS_FROM_TABLE = page.locator("select.selectBox");
        this.DOWNLOAD_BULK_UPLOAD_TEMPLATE = page.locator("//div[contains(@onclick,'BulkUploadTemplate')]");
        this.DURATION = page.locator("//input[contains(@placeholder,'Duration')]");
        this.DSA_REQUIREMENTS_HEADER = page.locator("//h3[contains(text(),'DSA Requirements')]");
        this.ADVERTISER_DSA = page.locator("//input[contains(@placeholder,'Advertiser per DSA')]");
        this.FINANCER = page.locator("//input[contains(@placeholder,'Financer')]");
    }

    public void clickBulkUploadButton() {
        BULK_UPLOAD_BUTTON.click();
        waitUtility.waitForLocatorVisible(BULK_UPLOAD_CREATIVE_HEADER);
    }

    public boolean checkCreativeTypeButtonsArePresent() {
        return CREATIVE_TYPE_BUTTON.count() > 0;
    }

    public boolean checkAdvertiserDropdownIsShown() {
        return ADVERTISER_DROPDOWN.isVisible();
    }

    public String verifyCreativeTypeOptions(List<String> creativeTypeOptions) {
        List<String> missingOptions = new ArrayList<>();

        for (String option : creativeTypeOptions) {
            boolean isVisible = CREATIVE_TYPE_BUTTON.locator("text=" + option).isVisible();
            if (!isVisible) {
                missingOptions.add(option);
            }
        }
        if (missingOptions.isEmpty()) {
            return "All creative type options are available.";
        } else {
            return "Missing creative type options: " + String.join(", ", missingOptions);
        }
    }

    public boolean checkDefaultCreativeType(String defaultOption) {
        for (int i = 0; i < CREATIVE_TYPE_BUTTON.count(); i++) {
            String text = CREATIVE_TYPE_BUTTON.nth(i).innerText();
            String classAttr = CREATIVE_TYPE_BUTTON.nth(i).getAttribute("class");
            if (text.contains(defaultOption) && classAttr != null && classAttr.contains("active")) {
                return true;
            }
        }
        return false;
    }

    public void selectAndClickCreativeType(String creativeType) {
        CREATIVE_TYPE_BUTTON.locator("text=" + creativeType).click();
    }

    public void selectAdvertiser(String advertiser) {
        ADVERTISER_DROPDOWN.click();
        ADVERTISER_DROPDOWN_VALUE.locator("text=" + advertiser).click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void selectApprovalStatus(String status) {
        APPROVAL_STATUS_BUTTON.first().scrollIntoViewIfNeeded();
        APPROVAL_STATUS_BUTTON.locator("text=" + status).click();
    }

    public void enterAdvertiserDSA(String advertiserDSA) {
        if(DSA_REQUIREMENTS_HEADER.isVisible())
            ADVERTISER_DSA.fill(advertiserDSA);
    }

    public void enterFinancer(String financer) {
        if(FINANCER.isVisible())
            FINANCER.fill(financer);
    }

    public void clickPreviewButton() {
        if (PREVIEW_BUTTON.isVisible()) PREVIEW_BUTTON.click();
    }

    public void clickUploadButton() {
        if (UPLOAD_BUTTON.isVisible()) UPLOAD_BUTTON.click();
    }

    public String fetchErrorAlert() {
        String text = "No error alert is displayed.";
        if (ERROR_ALERT.isVisible()) {
            text = ERROR_ALERT.innerText().trim();
            waitUtility.waitForLocatorHidden(ERROR_ALERT);
        }
        return text;
    }

    public List<String> fetchInlineValidationMessage() {
        if (!(INLINE_VALIDATION_MESSAGE.count() > 0)) {
            return Collections.emptyList();
        }
        return INLINE_VALIDATION_MESSAGE.allInnerTexts().stream()
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public String fetchSuccessAlert() {
        String text = SUCCESS_ALERT.innerText().trim();
        waitUtility.waitForLocatorHidden(SUCCESS_ALERT);
        return text;
    }

    public void updateCreativeName(String updatedCreativeName) {
        waitUtility.waitForLocatorVisible(BULK_UPLOAD_HEADER);
        CREATIVE_TEXT_DETAILS_FROM_TABLE.first().fill(updatedCreativeName);
    }

    public String fetchHeaderMessage() {
        return HEADER_MESSAGE.innerText();
    }

    public void clickOKButton() {
        if (OK_BUTTON.isVisible()) OK_BUTTON.click();
    }

    public boolean verifyDisplayCreativeSections(String section) {
        Locator locator = page.locator("//h3").locator("text=" + section);
        return locator.isVisible();
    }

    public boolean isDownloadTemplateButtonVisible() {
        return DOWNLOAD_BLANK_TEMPLATE.isVisible();
    }

    public Path clickBlankTemplateDownloadButton() throws IOException {
        Download download = page.waitForDownload(DOWNLOAD_BLANK_TEMPLATE::click);
        return CommonUtils.downloadFileAndMoveToSystemFolder(download);
    }

    public boolean isBrowseFileButtonVisible(String field) {
        if (field.contains("Upload Images to Get Template With URLs"))
            return IMAGE_FILE_BROWSE_BUTTON.first().isVisible();
        else if (field.contains("Spreadsheet")) return TEMPLATE_BROWSE_BUTTON.isVisible();
        return false;
    }

    public void uploadImageFile(String imageFileName) {
        String locator = "//span[contains(@class,'reupload-image')]";
        CommonUtils.uploadFile(page, 0, locator, imageFileName);
    }

    public void uploadSecondaryCreativeTemplate(String fileName) {
        String locator = "//div[contains(@title,'%s')]";
        CommonUtils.uploadFile(page, 1, locator, fileName);
    }

    public void uploadPrimaryCreativeTemplate(String fileName) {
        String locator = "//div[contains(@title,'%s')]";
        CommonUtils.uploadFile(page, 0, locator, fileName);
    }

    public Path clickTemplateWithURLsLink() throws IOException {
        Download download = page.waitForDownload(TEMPLATE_WITH_URL_LINK::click);
        return CommonUtils.downloadFileAndMoveToSystemFolder(download);
    }

    public boolean isCampaignToRestrictVisible() {
        return CAMPAIGN_DROPDOWN.isVisible();
    }

    public boolean checkDefaultApprovalStatus(String defaultOption) {
        for (int i = 0; i < APPROVAL_STATUS_BUTTON.count(); i++) {
            String text = APPROVAL_STATUS_BUTTON.nth(i).innerText();
            String classAttr = APPROVAL_STATUS_BUTTON.nth(i).getAttribute("class");
            if (text.contains(defaultOption) && classAttr != null && classAttr.contains("active")) {
                return true;
            }
        }
        return false;
    }

    public boolean isApprovalStatusVisible() {
        return APPROVAL_STATUS_BUTTON.first().isVisible();
    }

    public void clickCampaignName(String name) {
        CAMPAIGN_DROPDOWN.click();
        CAMPAIGN_DROPDOWN.type(name);
        try {
            waitUtility.waitForLocatorVisible(DROPDOWN_SEARCH, 1000);
        } catch (Exception ignored) {
        }
        waitUtility.waitForLocatorHidden(DROPDOWN_SEARCH);
        page.keyboard().press("ArrowDown");
        page.keyboard().press("Enter");
        CAMPAIGN_DROPDOWN_VALUE.first().click();
    }

    public boolean isThirdPartyTrackingPixelAvailable() {
        return THIRD_PARTY_TRACKING_PIXEL.isVisible();
    }

    public boolean isDoubleVerifyPixelAvailable() {
        return DOUBLE_VERIFY_PIXEL.isVisible();
    }

    public void addThirdPartyTrackingPixel(String pixelDetails) {
        THIRD_PARTY_TRACKING_PIXEL.click();
        INPUT_THIRD_PARTY_TRACKING_PIXEL.fill(pixelDetails);
    }

    public boolean addDoubleVerifyPixel() {
        DOUBLE_VERIFY_PIXEL.click();
        return DOUBLE_VERIFY_TEXTAREA.isVisible();
    }

    public boolean deleteThirdPartyTrackingPixel() {
        DELETE_ICON.click();
        return !INPUT_THIRD_PARTY_TRACKING_PIXEL.isVisible();
    }

    public void uploadBlankTemplate(String fileName) {
        if (REMOVE_FILE_ICON.isVisible()) {
            String locator = "//div[contains(@aria-label,'Unable to parse file')]";
            REMOVE_FILE_ICON.click();
            CommonUtils.uploadFile(page, 0, locator, fileName);
        }
    }

    public void enterLandingPageDomain(String landingDomainName) {
        LANDING_PAGE_DOMAIN.fill(landingDomainName);
    }

    public String fetchFileDefaultValue() {
        return FILE_DROPDOWN.innerText();
    }

    public String fetchAdChoiceDefaultValue() {
        return AD_CHOICES_DROPDOWN.innerText();
    }

    public String fetchNotesColumnDefaultValue() {
        return NOTES_DROPDOWN.innerText();
    }

    public boolean isRichMediaCheckboxAvailable() {
        return RICH_MEDIA_CHECKBOX.isVisible();
    }

    public boolean isRichMediaCheckboxClickable() {
        RICH_MEDIA_CHECKBOX.click();
        return RICH_MEDIA_CHECKBOX.getAttribute("class").contains("checked");
    }

    public void selectFileTypeAndUploadFile(String fileType, String fileName) throws IOException {
        String locatorValue = "//div[@title='%s']";
        FILE_DROPDOWN.click();
        FILE_DROPDOWN_VALUE.locator("text=" + fileType).first().click();
        if (fileType.contains("PulsePoint") && fileName.equalsIgnoreCase("Download Template")) {
            waitUtility.waitForLocatorVisible(DOWNLOAD_BULK_UPLOAD_TEMPLATE);
            Download download = page.waitForDownload(DOWNLOAD_BULK_UPLOAD_TEMPLATE::click);
            CommonUtils.downloadFileAndMoveToSystemFolder(download);
            Path latestFile = CommonUtils.getMostRecentFileFromDownloads();
            fileName = latestFile.getFileName().toString();
        }
        CommonUtils.uploadFile(page, 0, locatorValue, fileName);
        waitUtility.waitUntilSpinnerHidden();
    }

    public void selectAndClickDirection(String direction) {
        DIRECTION_DROPDOWN.click();
        DIRECTION_DROPDOWN_VALUE.locator("text=" + direction).first().click();
    }

    public List<String> enterCreativeName(String name) {
        waitUtility.waitUntilSpinnerHidden();
        List<String> nameList = new ArrayList<>();
        for (int i = 0; i < HTML_CREATIVE_NAME.count(); i++) {
            String newName =
                    name + "_" + CommonUtils.timeStampCalculation() + "_" + CommonUtils.randomFourDigitNumber();
            HTML_CREATIVE_NAME.nth(i).fill(newName);
            nameList.add(newName);
        }
        return nameList;
    }

    public void typeIABCategory(String iabCategory) {
        IAB_CATEGORY_DROPDOWN.fill(iabCategory);
        page.keyboard().press("ArrowDown");
        page.keyboard().press("Enter");
    }

    public void enterClickthroughURL(String validURL) {
        createCreatives.CLICK_THROUGH_URL.fill(validURL);
    }

    public void isRemoveFileIconAvailable() {
        if (REMOVE_FILE_ICON.isVisible()) {
            REMOVE_FILE_ICON.click();
        }
    }

    public boolean isWidthHeightVisibleAndBlank() {
        for (int i = 0; i < WIDTH_BOX.count(); i++) {
            if (!WIDTH_BOX.nth(i).isVisible()
                    || !HEIGHT_BOX.nth(i).isVisible()
                    || !WIDTH_BOX.nth(i).inputValue().trim().isEmpty()
                    || !HEIGHT_BOX.nth(i).inputValue().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean isDurationVisibleAndBlank() {
        for (int i = 0; i < DURATION.count(); i++) {
            if (!DURATION.nth(i).isVisible()
                    || !DURATION.nth(i).inputValue().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void enterWidthHeight(String size) {
        String[] parts = size.split("x");
        if (parts.length == 2) {
            String width = parts[0].trim();
            String height = parts[1].trim();
            int count = Math.min(WIDTH_BOX.count(), HEIGHT_BOX.count());
            for (int i = 0; i < count; i++) {
                WIDTH_BOX.nth(i).fill(width);
                HEIGHT_BOX.nth(i).fill(height);
            }
        }
    }

    public void enterDuration(String duration) {
        for (int i = 0; i < DURATION.count(); i++) {
            DURATION.nth(i).fill(duration);
        }
    }

    public void enterCreativeAndDSADetails(String advertiser, String advertiserDSA, String financer) {
        selectAdvertiser(advertiser);
        waitUtility.waitUntilSpinnerHidden();
        enterAdvertiserDSA(advertiserDSA);
        enterFinancer(financer);
    }

    public void fillAttributes(String type, Map<String, String> attributeMap, String updatedCreativeName)
            throws IOException {
        switch (type) {
            case "Display", "Native":
                uploadSecondaryCreativeTemplate(attributeMap.get("FileName"));
                if (LANDING_PAGE_DOMAIN.isVisible()) enterLandingPageDomain(attributeMap.get("LandingDomain"));
                if (IAB_CATEGORY_DROPDOWN.isVisible()) typeIABCategory(attributeMap.get("IAB"));
                selectApprovalStatus(attributeMap.get("Status"));
                clickPreviewButton();
                updateCreativeName(updatedCreativeName);
                checkIfValidationErrorsExist();
                clickOKButton();
                clickUploadButton();
                break;
            case "HTML", "Video":
                //                if (type.contains("Video"))
                //                    uploadPrimaryCreativeTemplate(attributeMap.get("ImageFile"));
                selectFileTypeAndUploadFile(attributeMap.get("FileType"), attributeMap.get("FileName"));
                if (createCreatives.CLICK_THROUGH_URL.isVisible())
                    enterClickthroughURL(attributeMap.get("ClickThroughURL"));
                enterLandingPageDomain(attributeMap.get("LandingDomain"));
                selectApprovalStatus(attributeMap.get("Status"));
                clickPreviewButton();
                HTML_CREATIVE_NAME.fill(updatedCreativeName);
                if (type.contains("Video")) {
                    enterWidthHeight(attributeMap.get("Size"));
                    DURATION.fill(attributeMap.get("Duration"));
                }
                clickUploadButton();
                clickOKButton();
                break;
        }
    }

    public void checkIfValidationErrorsExist() {
        if (VALIDATION_ERROR.isVisible()) {
            WARNING_IMAGE_ICON.click();
            String errorField = TOOL_TIP_TEXT.textContent().trim();
            switch (errorField) {
                case "AD SIZE is not supported":
                    for (int i = 0; i < CREATIVE_DROPDOWN_DETAILS_FROM_TABLE.count(); i++) {
                        String fieldPlaceholder =
                                CREATIVE_DROPDOWN_DETAILS_FROM_TABLE.nth(i).getAttribute("class");
                        if (fieldPlaceholder.contains("red-border")) {
                            List<String> texts =
                                    CREATIVE_DROPDOWN_DETAILS_FROM_TABLE
                                            .nth(i)
                                            .locator("option")
                                            .allInnerTexts()
                                            .stream()
                                            .map(String::trim)
                                            .filter(text -> !text.isEmpty())
                                            .toList();
                            String randomText = texts.get(new Random().nextInt(texts.size()));
                            CREATIVE_DROPDOWN_DETAILS_FROM_TABLE
                                    .first()
                                    .selectOption(new SelectOption().setLabel(randomText));
                            break;
                        }
                    }
                    break;
                case "Display URL is required":
                    for (int i = 0; i < CREATIVE_TEXT_DETAILS_FROM_TABLE.count(); i++) {
                        String fieldPlaceholder =
                                CREATIVE_TEXT_DETAILS_FROM_TABLE.nth(i).getAttribute("class");
                        if (fieldPlaceholder.contains("red-border")) {
                            CREATIVE_TEXT_DETAILS_FROM_TABLE.nth(i).fill(constants.DISPLAY_URL);
                            break;
                        }
                    }
                    break;
            }
        }
    }

    public List<String> fetchBulkUploadCreativeDetails() {
        List<String> creativeDetails = new ArrayList<>();
        creativeDetails.add(
                ADVERTISER_DROPDOWN.locator("xpath=./div/span[2]").textContent().trim());
        creativeDetails.add(createCreatives.ADVERTISER_DSA.inputValue().trim());
        creativeDetails.add(createCreatives.FINANCER.inputValue().trim());
        for (int i = 0; i < APPROVAL_STATUS_BUTTON.count(); i++) {
            if (APPROVAL_STATUS_BUTTON.nth(i).getAttribute("class").contains("active")) {
                creativeDetails.add(APPROVAL_STATUS_BUTTON.nth(i).textContent().trim());
                break;
            }
        }
        if (CREATIVE_TEXT_DETAILS_FROM_TABLE.first().isVisible()) {
            for (int i = 0; i < CREATIVE_TEXT_DETAILS_FROM_TABLE.count(); i++) {
                String bulkUploadFields =
                        CREATIVE_TEXT_DETAILS_FROM_TABLE.nth(i).inputValue().trim();
                if (!bulkUploadFields.isEmpty()) creativeDetails.add(bulkUploadFields);
            }
        }
        if (CREATIVE_DROPDOWN_DETAILS_FROM_TABLE.first().isVisible()) {
            for (int i = 0; i < CREATIVE_DROPDOWN_DETAILS_FROM_TABLE.count(); i++) {
                String bulkUploadFields =
                        CREATIVE_DROPDOWN_DETAILS_FROM_TABLE.nth(i).inputValue().trim();
                if (!bulkUploadFields.isEmpty()) creativeDetails.add(bulkUploadFields);
            }
        }
        if (LANDING_PAGE_DOMAIN.isVisible())
            creativeDetails.add(LANDING_PAGE_DOMAIN.inputValue().trim());
        if (createCreatives.SELECTED_AD_CHOICES_ICON.first().isVisible())
            creativeDetails.add(createCreatives
                    .SELECTED_AD_CHOICES_ICON
                    .first()
                    .textContent()
                    .trim());
        if (HTML_CREATIVE_NAME.first().isVisible()) {
            for (int i = 0; i < HTML_CREATIVE_NAME.count(); i++) {
                creativeDetails.add(HTML_CREATIVE_NAME.nth(i).inputValue().trim());
            }
        }
        Locator htmlAdSize = HTML_CREATIVE_NAME.locator("xpath=./ancestor::td/following-sibling::td[2]");
        if (htmlAdSize.first().isVisible()) {
            for (int i = 0; i < htmlAdSize.count(); i++) {
                creativeDetails.add(htmlAdSize.nth(i).textContent().trim());
            }
        }
        return creativeDetails;
    }

    public boolean checkCreativeWidthTypeIsVisible() {
        return createCreatives.CREATIVE_WIDTH_TYPE.first().isVisible();
    }

    public String fetchDefaultCreativeWidthType() {
        for (int i = 0; i < createCreatives.CREATIVE_WIDTH_TYPE.count(); i++) {
            String classAttr = createCreatives.CREATIVE_WIDTH_TYPE.nth(i).getAttribute("class");
            if (classAttr != null && classAttr.contains("active")) {
                return createCreatives.CREATIVE_WIDTH_TYPE.nth(i).textContent().trim();
            }
        }
        return "";
    }

    public boolean isAdvertiserDropdownAvailable() {
        return ADVERTISER_DROPDOWN.isVisible();
    }

    public List<String> fetchAdvertisers() {
        ADVERTISER_DROPDOWN.click();
        ADVERTISER_DROPDOWN_VALUE.first().waitFor();
        List<String> options = ADVERTISER_DROPDOWN_VALUE.allTextContents();
        options.removeIf(String::isBlank);
        return options;
    }
}
