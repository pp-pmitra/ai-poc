package pages.life;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import factory.DriverFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import utils.CommonUtils;
import utils.WaitUtility;

public class NPISmartList {
    private final Page page;
    private final Locator ADD_VISITED_URL;
    private final Locator ADD_IGNORED_URL;
    private final Locator INSERT_VISITED_URL;
    private final Locator INSERT_IGNORED_URL;
    private final Locator CLICK_LIFE_CHECKBOX;
    private final Locator LIST_NAME;
    private final Locator SEARCH_ADVERTISER;
    private final Locator SELECT_ADVERTISER;
    private final Locator SEARCH_DRUG;
    private final Locator SELECT_DROPDOWN_VALUES;
    private final Locator SELECTED_DRUG;
    private final Locator LIFE_AVAILABLE_IN;
    private final Locator HCP365_AVAILABLE_IN;
    private final Locator PULSEPOINT_ICON;
    private final Locator ADD_DRUG_BUTTON;
    private final Locator SMART_PIXEL;
    private final Locator ADVERTISER_NAME;
    private final Locator SMART_LIST_CONTAINER;
    private final Locator SMART_LIST_POPULATION_OPTIONS;
    private final Locator SMART_PIXEL_DROPDOWN;
    private final Locator SMART_PIXEL_DROPDOWN_VALUE;
    private final Locator ENGAGEMENT_TYPE;
    private final Locator SAVE_BUTTON;
    private final Locator SUCCESS_ALERT;
    private final Locator SEARCH_KEYWORDS_CHECKBOX;
    private final Locator SEARCH_KEYWORDS_TEXTAREA;
    private final Locator HCP_SWITCH;
    private final Locator NPI_GROUP_DROPDOWN;
    private final Locator NPI_LIST_DROPDOWN_VALUE;
    private final Locator SPECIALITY_DROPDOWN;
    private final Locator PROFESSION_DROPDOWN;
    private final Locator PRESCRIBED_DRUG_RECENCY;
    private final Locator PRESCRIBED_DRUG_DECILE;
    private final Locator SEARCH_DIAGNOSIS;
    private final Locator ADD_DIAGNOSIS_BUTTON;
    private final Locator DIAGNOSIS_RECENCY;
    private final Locator DIAGNOSIS_DECILE;
    private final Locator BROWSE_DIAGNOSIS_FILE;
    private final Locator MEDICAL_PROCEDURE_RECENCY;
    private final Locator MEDICAL_PROCEDURE_DECILE;
    private final Locator BROWSE_MEDICAL_PROCEDURE_FILE;
    private final Locator SEARCH_MEDICAL_PROCEDURE;
    private final Locator ADD_MEDICAL_PROCEDURE_BUTTON;
    private final Locator MEDICAL_PROCEDURE_INPUT;
    private final Locator PRESCRIPTION_BEHAVIOUR_BUTTON;
    private final Locator PRESCRIPTION_DRUG;
    private final Locator TOP_DROPPERS_PERCENTAGE;
    private final Locator TOP_DROPPER_SLIDER;
    private final Locator TIME_FRAME_SELECTOR;
    private final Locator TIME_FRAME_SELECTOR_SLIDER;
    private final Locator MESH_DROPDOWN;
    private final Locator MESH_CONDITION;
    private final Locator PRIME_LIST_CHECKBOX;
    private final Locator OK_BUTTON;
    private final Locator RECENCY_SLIDER;
    private final Locator RECENCY_DAYS;
    private final Locator TOOL_TIP;
    private final Locator MEDSCAPE_PRIMARY_CONCEPT;
    private final Locator WEBMD_PRIMARY_TOPIC;
    private final Locator ERROR_ALERT;
    private final Locator FETCH_SELECTED_SMART_PIXEL;
    private final Locator FETCH_SELECTED_ADVERTISER;
    private final Locator FETCH_SELECTED_ENGAGEMENT_SWITCH;
    private final Locator FETCH_SELECTED_RECENCY;
    private final Locator FETCH_SELECTED_KEYWORD_FLAG;
    private final Locator FETCH_SELECTED_AVAILABLE_IN;
    private final Locator FETCH_SELECTED_NPI_LIST;
    private final Locator FETCH_SELECTED_SPECIALTY;
    private final Locator FETCH_SELECTED_DIAGNOSIS_CODE;
    private final Locator DIAGNOSIS_CODE_FILE_DETAILS;
    private final Locator FETCH_SELECTED_MEDICAL_PROCEDURE_CODE;
    private final Locator MEDICAL_PROCEDURE_CODE_FILE_DETAILS;
    private final Locator FETCH_SELECTED_PRESCRIPTION_DRUG;
    private final Locator FETCH_SELECTED_TOP_DROPPER_PERCENT;
    private final Locator FETCH_SELECTED_TIME_FRAME_MONTHS;
    private final Locator ENDEMIC_RESEARCH_SECTION;
    private final Locator FETCH_SELECTED_RECENCY_DAYS;
    private final Locator FETCH_SELECTED_DRUG;
    private final Locator FETCH_SELECTED_PROFESSION;
    private final Locator LIST_NAME_FROM_HEADER;
    private final Locator ADVERTISER_NAME_FROM_HEADER;
    private final Locator EDIT_ICON;
    private final Locator DECILE_SLIDER;
    private final Locator BROWSE_PRESCRIBED_DRUG_FILE;
    WaitUtility waitUtility = new WaitUtility(DriverFactory.getPage());

    public NPISmartList(Page page) {
        this.page = page;
        this.CLICK_LIFE_CHECKBOX =
                page.locator("//span[contains(@class,'mat-checkbox-label') and contains(text(),'Life')]");
        this.LIST_NAME = page.locator("//input[contains(@placeholder,'List Name')]");
        this.SEARCH_ADVERTISER = page.locator("app-npilists-manager").getByRole(AriaRole.COMBOBOX);
        this.SELECT_ADVERTISER = page.locator("//div[contains(@class,'dropdown-items ng-star-inserted')]");
        this.SEARCH_DRUG = page.locator("//ng-select[contains(@class, 'drugDropdown')]//input");
        this.SELECT_DROPDOWN_VALUES =
                page.locator("//div[contains(@class,'dropdown-items') or contains(@class,'ng-option')]");
        this.SELECTED_DRUG =
                page.locator("//ng-select[contains(@class, 'drugDropdown ')]//span[contains(@class,'ng-value-label')]");
        this.LIFE_AVAILABLE_IN = page.locator("//span[contains(text(),'Life')]");
        this.HCP365_AVAILABLE_IN = page.locator("//span[contains(text(),'HCP365')]");
        this.PULSEPOINT_ICON = page.locator("//div[@class='logo-lists']/img[@alt='logo']");
        this.ADD_DRUG_BUTTON = page.locator("//span[contains(text(), 'Add Drug')]");
        this.SMART_PIXEL =
                page.locator("//ng-select[@placeholder='Select Smart Pixel']//div[contains(@class,'ng-value')]//span");
        this.ADVERTISER_NAME = page.locator("//div[@class='chips']//span[contains(@class,'ng-value-label')]");
        this.SMART_LIST_CONTAINER = page.locator("//div[@id='smartlistDetailsContainer']");
        this.SMART_LIST_POPULATION_OPTIONS = page.locator(
                "//mat-checkbox[contains(@class,'populationCheckbox')]//span[@class='mat-checkbox-label']");
        this.SMART_PIXEL_DROPDOWN = page.locator("//ng-select[contains(@placeholder,'Select Smart Pixel')]");
        this.SMART_PIXEL_DROPDOWN_VALUE = page.locator("//span[contains(@class,'pixel-template-options')]");
        this.ENGAGEMENT_TYPE = page.locator("//div[contains(@class,'engagementSwitch')]//div[@class='selection']");
        this.ADD_VISITED_URL =
                page.locator("//div[contains(text(),'Visited URLs')]/following-sibling::div[@class='add-icon']");
        this.INSERT_VISITED_URL = page.locator("//input[contains(@class,'visitedUrlInput')]");
        this.INSERT_IGNORED_URL = page.locator("//input[contains(@class,'ignoredUrlInput')]");
        this.ADD_IGNORED_URL =
                page.locator("//div[contains(text(),'Ignored URLs')]/following-sibling::div[@class='add-icon']");
        this.SAVE_BUTTON = page.locator("//span[contains(text(),'Save')]");
        this.SUCCESS_ALERT = page.locator("//div[contains(text(), 'NPI list created successfully')]");
        this.SEARCH_KEYWORDS_CHECKBOX = page.locator("//span[contains(text(),'Search Keywords')]");
        this.SEARCH_KEYWORDS_TEXTAREA = page.locator("//textarea[contains(@class,'textarea-smartlist-padding')]");
        this.HCP_SWITCH = page.locator("//div[contains(@class,'targetHCPSwitch')]//div");
        this.NPI_GROUP_DROPDOWN = page.locator("//ng-select[@placeholder='Select NPI Groups']//input");
        this.NPI_LIST_DROPDOWN_VALUE =
                page.locator("//div[contains(@class,'npilistFilter')]//div[contains(@class,'dropdown-items')]");
        this.SPECIALITY_DROPDOWN = page.locator("//ng-select[@placeholder='Select Specialities']//input");
        this.PROFESSION_DROPDOWN = page.locator("//ng-select[@placeholder='Select Professions']//input");
        this.PRESCRIBED_DRUG_RECENCY = page.locator(
                "//app-clinical-population[@headername='Prescribed Drug']//input[contains(@class,'recencyInp')]");
        this.PRESCRIBED_DRUG_DECILE = page.locator(
                "//app-clinical-population[@headername='Prescribed Drug']//div[contains(@class, 'text ng-star-inserted')]");
        this.SEARCH_DIAGNOSIS =
                page.locator("//div[contains(text(), 'Select Diagnosis')]/following-sibling::div//input");
        this.ADD_DIAGNOSIS_BUTTON = page.locator("//span[contains(text(), 'Add Diagnoses')]");
        this.DIAGNOSIS_RECENCY = page.locator(
                "//app-clinical-population[@headername='Diagnosis']//input[contains(@class,'recencyInp')]");
        this.DIAGNOSIS_DECILE = page.locator(
                "//app-clinical-population[@headername='Diagnosis']//div[contains(@class, 'text ng-star-inserted')]");
        this.BROWSE_DIAGNOSIS_FILE = page.locator(
                "//app-clinical-population[@headername='Diagnosis']//div[contains(@class, 'editOrFileSwitch')]//i[@class='icon-20-csv']");
        this.MEDICAL_PROCEDURE_RECENCY = page.locator(
                "//app-clinical-population[@headername='Medical Procedure']//input[contains(@class,'recencyInp')]");
        this.MEDICAL_PROCEDURE_DECILE = page.locator(
                "//app-clinical-population[@headername='Medical Procedure']//div[contains(@class, 'text ng-star-inserted')]");
        this.BROWSE_MEDICAL_PROCEDURE_FILE = page.locator(
                "//app-clinical-population[@headername='Medical Procedure']//div[contains(@class, 'editOrFileSwitch')]//i[@class='icon-20-csv']");
        this.SEARCH_MEDICAL_PROCEDURE = page.locator(
                "//div[contains(text(), 'Select Medical Procedure')]/ancestor::div[@class='ng-select-container']");
        this.MEDICAL_PROCEDURE_INPUT = page.locator(
                "//div[contains(text(), 'Select Medical Procedure')]/ancestor::div[@class='ng-select-container']//input");
        this.ADD_MEDICAL_PROCEDURE_BUTTON = page.locator("//span[contains(text(), 'Add Procedures')]");
        this.PRESCRIPTION_BEHAVIOUR_BUTTON = page.locator("//div[contains(@class, 'prescriptionBehaviorSwitch')]//div");
        this.PRESCRIPTION_DRUG = page.locator("//div[contains(text(), 'Select Drugs')]/following-sibling::div/input");
        this.TOP_DROPPERS_PERCENTAGE = page.locator("//div[contains(@class, 'topDroppers-percent')]");
        this.TOP_DROPPER_SLIDER = page.locator("//div[contains(@class, 'topDroppersWrapper')]//span[@role='slider']");
        this.TIME_FRAME_SELECTOR = page.locator("//div[contains(@class, 'timeFrame-months')]");
        this.TIME_FRAME_SELECTOR_SLIDER =
                page.locator("//div[contains(@class, 'timeframeWrapper')]//span[@role='slider']");
        this.MESH_DROPDOWN = page.locator("//div[contains(@class, 'mesh-placeholder')]");
        this.MESH_CONDITION = page.locator("//input[contains(@placeholder,'Select MESH Conditions')]");
        this.OK_BUTTON = page.locator("//button[contains(@class,'okButton')]");
        this.RECENCY_SLIDER = page.locator("//div[contains(@class, 'recencyframeWrapper')]//span[@role='slider']");
        this.RECENCY_DAYS = page.locator("//div[contains(@class,'recency-days')]");
        this.PRIME_LIST_CHECKBOX = page.locator("//mat-checkbox[contains(@class, 'checkboxSectionHeading')]");
        this.TOOL_TIP = page.locator("//div[contains(@class,'ng-tooltip-show')]");
        this.MEDSCAPE_PRIMARY_CONCEPT =
                page.locator("//div[contains(text(),'Select Medscape Primary Concept')]/following-sibling::div/input");
        this.WEBMD_PRIMARY_TOPIC =
                page.locator("//div[contains(text(),'Select WebMD Primary Topic')]/following-sibling::div/input");
        this.ERROR_ALERT = page.locator("//div[@role='alertdialog']");
        this.FETCH_SELECTED_ADVERTISER = page.locator("//span[@class='ng-value-label']");
        this.FETCH_SELECTED_SMART_PIXEL = page.locator("//span[contains(@class,'pixel-template-options')]");
        this.FETCH_SELECTED_ENGAGEMENT_SWITCH =
                page.locator("//div[contains(text(),'Engagement')]//following-sibling::div//button");
        this.FETCH_SELECTED_RECENCY =
                page.locator("//div[contains(text(),'Recency')]//following-sibling::div//mat-radio-button");
        this.FETCH_SELECTED_KEYWORD_FLAG = page.locator(
                "//div[contains(@class,'searchKeywordsSection')]//following-sibling::div//mat-radio-button");
        this.FETCH_SELECTED_AVAILABLE_IN =
                page.locator("//div[contains(text(),'Available In ')]//following-sibling::div//mat-checkbox");
        this.FETCH_SELECTED_NPI_LIST = page.locator("//div[@class='npilistFilter']//span[@class='ng-value-label']");
        this.FETCH_SELECTED_SPECIALTY =
                page.locator("//div[@class='hcpSpecialtyFilter']//div[@class='ng-value-label']");
        this.FETCH_SELECTED_DIAGNOSIS_CODE = page.locator(
                "//div[contains(text(),'Select Diagnosis Code')]/following-sibling::div//span[contains(@class,'ng-value-label')]");
        this.DIAGNOSIS_CODE_FILE_DETAILS = page.locator(
                "//div[contains(@class,'fileDetails')]//span[contains(normalize-space(.), 'names /') and contains(normalize-space(.), 'codes')]");
        this.FETCH_SELECTED_MEDICAL_PROCEDURE_CODE = page.locator(
                "//div[contains(text(),'Select Medical Procedure Code')]/following-sibling::div//span[contains(@class,'ng-value-label')]");
        this.MEDICAL_PROCEDURE_CODE_FILE_DETAILS = page.locator(
                "//div[contains(@class,'fileDetails')]//a[contains(normalize-space(.), 'lines are ignored')]");
        this.FETCH_SELECTED_PRESCRIPTION_DRUG = page.locator(
                "//div[contains(text(), 'Select Drugs')]/following-sibling::div//span[contains(@class,'ng-value-label')]");
        this.FETCH_SELECTED_TOP_DROPPER_PERCENT = page.locator(
                "//div[contains(@class,'topDroppersWrapper')]//div[contains(@class,'topDroppers-percent')]");
        this.FETCH_SELECTED_TIME_FRAME_MONTHS =
                page.locator("//div[contains(@class,'timeframeWrapper')]//div[contains(@class,'timeFrame-months')]");
        this.ENDEMIC_RESEARCH_SECTION = page.locator("//div[contains(@class,'endamicResearchContainer')]");
        this.FETCH_SELECTED_RECENCY_DAYS =
                page.locator("//div[contains(@class,'recencyframeWrapper')]//div[contains(@class,'recency-days')]");
        this.FETCH_SELECTED_DRUG =
                page.locator("//ng-select[contains(@class, 'drugDropdown')]//span[contains(@class,'ng-value-label')]");
        this.FETCH_SELECTED_PROFESSION = page.locator(
                "//div[contains(text(),'Select Professions')]/following-sibling::div//span[@class='ng-value-label']");
        this.LIST_NAME_FROM_HEADER = page.locator("//div[contains(@class,'header-name')]");
        this.ADVERTISER_NAME_FROM_HEADER = page.locator("//span[@class='header-adv']");
        this.EDIT_ICON = page.locator("//img[contains(@class,'header-edit-icon')]");
        this.DECILE_SLIDER = page.locator(
                "//div[contains(@class, 'decileSelectorContainer')]//span[@role='slider' and @aria-label='ngx-slider-max']");
        this.BROWSE_PRESCRIBED_DRUG_FILE = page.locator(
                "//app-clinical-population[@headername='Prescribed Drug']//div[contains(@class, 'editOrFileSwitch')]//i[@class='icon-20-csv']");
    }

    public void clickSmartPixelDropDownValue(String smartPixelDropdownValue) {
        page.getByText(smartPixelDropdownValue).click();
    }

    public void clickLifeCheckbox() {
        CLICK_LIFE_CHECKBOX.click();
    }

    public void enterListName(String listName) {
        waitUtility.waitUntilSpinnerHidden();
        LIST_NAME.fill(listName);
    }

    public void selectAdvertiser(String advertiser) {
        SEARCH_ADVERTISER.click();
        SELECT_ADVERTISER.locator("text=" + advertiser).click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void selectDrug(String drug) {
        if (!SEARCH_DRUG.last().isVisible()) {
            ADD_DRUG_BUTTON.scrollIntoViewIfNeeded();
            ADD_DRUG_BUTTON.click();
        }
        SEARCH_DRUG.last().click();
        SEARCH_DRUG.last().fill(drug);
        SELECT_DROPDOWN_VALUES.nth(1).click();
    }

    public String fetchDrugName() {
        return SELECTED_DRUG.innerText();
    }

    public void selectProduct() {
        LIFE_AVAILABLE_IN.check();
        HCP365_AVAILABLE_IN.check();
    }

    public void clickPulsepointIcon() {
        PULSEPOINT_ICON.click();
    }

    public String verifySelectedSmartPixel() {
        waitUtility.waitUntilSpinnerHidden();
        return SMART_PIXEL.innerText().trim();
    }

    public String verifySelectedAdvertiser() {
        waitUtility.waitUntilSpinnerHidden();
        return ADVERTISER_NAME.innerText().trim();
    }

    public boolean verifyListPopulationOptions(String listPopulationOption) {
        waitUtility.waitForLocatorVisible(SMART_LIST_CONTAINER);
        for (int i = 0; i < SMART_LIST_POPULATION_OPTIONS.count(); i++) {
            SMART_LIST_POPULATION_OPTIONS.nth(i).scrollIntoViewIfNeeded();
            String text = SMART_LIST_POPULATION_OPTIONS.nth(i).innerText().trim();
            if (text.equalsIgnoreCase(listPopulationOption)) return true;
        }
        return false;
    }

    public void selectSmartNPIListType(String smartListType) {
        waitUtility.waitForElementVisible("//div[@class='loader']");
        SMART_LIST_POPULATION_OPTIONS.locator("text = " + smartListType).scrollIntoViewIfNeeded();
        SMART_LIST_POPULATION_OPTIONS.locator("text = " + smartListType).click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void clickSmartPixelDropDown() {
        SMART_PIXEL_DROPDOWN.click();
    }

    public List<String> fetchSmartPixelDropdownValue() {
        return SMART_PIXEL_DROPDOWN_VALUE.allInnerTexts();
    }

    public void selectSmartPixelDropdownValue() {
        SMART_PIXEL_DROPDOWN_VALUE.first().click();
    }

    public void selectEngagementType(String engagementType) {
        ENGAGEMENT_TYPE.locator("text = " + engagementType).click();
    }

    public void addVisitedURL(List<String> urls) {
        for (String url : urls) {
            ADD_VISITED_URL.scrollIntoViewIfNeeded();
            ADD_VISITED_URL.click();
            waitUtility.waitForLocatorVisible(INSERT_VISITED_URL.first());
            INSERT_VISITED_URL.first().fill(url);
        }
    }

    public void addIgnoredURL(List<String> urls) {
        for (String url : urls) {
            ADD_IGNORED_URL.scrollIntoViewIfNeeded();
            ADD_IGNORED_URL.click();
            waitUtility.waitForLocatorVisible(INSERT_IGNORED_URL.first());
            INSERT_IGNORED_URL.first().fill(url);
        }
    }

    public void clickSaveButton() {
        SAVE_BUTTON.click();
    }

    public String fetchSuccessAlert() {
        String text = SUCCESS_ALERT.innerText().trim();
        waitUtility.waitForLocatorHidden(SUCCESS_ALERT);
        waitUtility.waitUntilSpinnerHidden();
        return text;
    }

    public String fetchErrorAlert() {
        String text = ERROR_ALERT.innerText().trim();
        waitUtility.waitForLocatorHidden(ERROR_ALERT);
        return text;
    }

    public void clickSearchKeywordsCheckbox() {
        SEARCH_KEYWORDS_CHECKBOX.click();
    }

    public void enterSearchKeywords(List<String> keywordList) {
        for (String keyword : keywordList) {
            SEARCH_KEYWORDS_TEXTAREA.type(keyword);
            page.keyboard().press("Enter");
        }
    }

    public boolean verifyNPIListDisabled(String smartListType) {
        Locator xpath =
                page.locator(String.format("//span[contains(text(),'%s')]/ancestor::mat-checkbox", smartListType));
        return xpath.getAttribute("class").contains("mat-checkbox-disabled");
    }

    public void selectHCPSwitch(String hcpSwitch) {
        HCP_SWITCH.locator("text = " + hcpSwitch).click();
    }

    public void selectNPIGroup(String npiList) {
        NPI_GROUP_DROPDOWN.click();
        NPI_GROUP_DROPDOWN.fill(npiList);
        clickDropdownValue();
    }

    public void clickDropdownValue() {
        NPI_LIST_DROPDOWN_VALUE.first().click();
        page.keyboard().press("Escape");
    }

    public void selectSpeciality(String speciality) {
        SPECIALITY_DROPDOWN.click();
        Locator xpath = page.locator(String.format(
                "//div[@title='%s']//preceding-sibling::div[contains(@class,'targetIcons')]/div", speciality));
        xpath.click();
        page.keyboard().press("Escape");
    }

    public void selectProfession(String profession) {
        PROFESSION_DROPDOWN.click();
        PROFESSION_DROPDOWN.fill(profession);
        clickDropdownValue();
    }

    public void enterDiagnosisDetails(String diagnosis) {
        waitUtility.waitForLocatorVisible(ADD_DIAGNOSIS_BUTTON.last());
        if (!SEARCH_DIAGNOSIS.last().isVisible()) {
            ADD_DIAGNOSIS_BUTTON.scrollIntoViewIfNeeded();
            ADD_DIAGNOSIS_BUTTON.click();
        }
        SEARCH_DIAGNOSIS.last().click();
        SEARCH_DIAGNOSIS.last().fill(diagnosis);
        SELECT_DROPDOWN_VALUES.nth(1).click();
    }

    public void enterMedicalProcedure(String procedure) {
        if (!SEARCH_MEDICAL_PROCEDURE.last().isVisible()) {
            ADD_MEDICAL_PROCEDURE_BUTTON.scrollIntoViewIfNeeded();
            ADD_MEDICAL_PROCEDURE_BUTTON.click();
        }
        SEARCH_MEDICAL_PROCEDURE.last().click();
        MEDICAL_PROCEDURE_INPUT.last().fill(procedure);
        SELECT_DROPDOWN_VALUES.nth(1).click();
    }

    public String fetchRecency(String type) {
        return switch (type) {
            case "Prescribed Drug" -> PRESCRIBED_DRUG_RECENCY.first().inputValue();
            case "Diagnosis Code" -> DIAGNOSIS_RECENCY.first().inputValue();
            case "Medical Procedure Code" -> MEDICAL_PROCEDURE_RECENCY.first().inputValue();
            default -> "";
        };
    }

    public List<String> fetchDecile(String type) {
        List<String> decileValues = new ArrayList<>();
        switch (type) {
            case "Prescribed Drug":
                for (int i = 0; i < PRESCRIBED_DRUG_DECILE.count(); i++) {
                    decileValues.add(PRESCRIBED_DRUG_DECILE.nth(i).innerText().trim());
                }
                break;
            case "Diagnosis Code":
                for (int i = 0; i < DIAGNOSIS_DECILE.count(); i++) {
                    decileValues.add(DIAGNOSIS_DECILE.nth(i).innerText().trim());
                }
                break;
            case "Medical Procedure Code":
                for (int i = 0; i < MEDICAL_PROCEDURE_DECILE.count(); i++) {
                    decileValues.add(MEDICAL_PROCEDURE_DECILE.nth(i).innerText().trim());
                }
                break;
        }
        return decileValues;
    }

    public void selectValueFromClinicalDropdown(String dropdownValue, String type) {
        switch (type) {
            case "Prescribed Drug":
                selectDrug(dropdownValue);
                break;
            case "Diagnosis Code":
                enterDiagnosisDetails(dropdownValue);
                break;
            case "Medical Procedure Code":
                enterMedicalProcedure(dropdownValue);
                break;
        }
    }

    public void clickAddButton(String type) {
        switch (type) {
            case "Prescribed Drug":
                ADD_DRUG_BUTTON.scrollIntoViewIfNeeded();
                ADD_DRUG_BUTTON.click();
                break;
            case "Diagnosis Code":
                ADD_DIAGNOSIS_BUTTON.scrollIntoViewIfNeeded();
                ADD_DIAGNOSIS_BUTTON.click(new Locator.ClickOptions().setForce(true));
                break;
            case "Medical Procedure Code":
                ADD_MEDICAL_PROCEDURE_BUTTON.scrollIntoViewIfNeeded();
                ADD_MEDICAL_PROCEDURE_BUTTON.click();
                break;
        }
    }

    public void browseBulkUploadTemplate(String type, String fileName) {
        switch (type) {
            case "Diagnosis Code" -> BROWSE_DIAGNOSIS_FILE.click();
            case "Medical Procedure Code" -> BROWSE_MEDICAL_PROCEDURE_FILE.click();
            case "Prescribed Drug" -> BROWSE_PRESCRIBED_DRUG_FILE.click();
        }
        String fileLocator = "//div[contains(text(),'%s')]";
        CommonUtils.uploadFile(page, 0, fileLocator, fileName);
    }

    public boolean fetchPrescriptionBehaviourTab(String tabName) {
        for (int i = 0; i < PRESCRIPTION_BEHAVIOUR_BUTTON.count(); i++) {
            if (PRESCRIPTION_BEHAVIOUR_BUTTON.nth(i).innerText().contains(tabName)) return true;
        }
        return false;
    }

    public boolean fetchDefaultPrescriptionBehaviourTab(String defaultTabName) {
        Locator tabXpath = page.locator(String.format(
                "//div[contains(@class, 'prescriptionBehaviorSwitch')]//div[contains(text(),'%s')]/parent::button",
                defaultTabName));
        return tabXpath.getAttribute("class").contains("active");
    }

    public void clickPrescriptionBehaviourTab(String tabName) {
        PRESCRIPTION_BEHAVIOUR_BUTTON.locator("text=" + tabName).click();
    }

    private boolean verifySliderRange(Locator slider, String expectedMin, String expectedMax) {
        String minValue = slider.getAttribute("aria-valuemin");
        String maxValue = slider.getAttribute("aria-valuemax");
        return expectedMin.equals(minValue) && expectedMax.equals(maxValue);
    }

    public boolean fetchTopDropperMinAndMaxValues(String topDropperMin, String topDropperMax) {
        return verifySliderRange(TOP_DROPPER_SLIDER, topDropperMin, topDropperMax);
    }

    public String fetchTopDropperDefaultValue() {
        return TOP_DROPPERS_PERCENTAGE.innerText().trim();
    }

    public boolean fetchTimeframeSelectorMinAndMaxValues(String timeframeSelectorMin, String timeframeSelectorMax) {
        return verifySliderRange(TIME_FRAME_SELECTOR_SLIDER, timeframeSelectorMin, timeframeSelectorMax);
    }

    public String fetchTimeframeSelectorDefaultValue() {
        return TIME_FRAME_SELECTOR.innerText().trim();
    }

    public void selectPrescriptionDrug(String drug) {
        PRESCRIPTION_DRUG.click();
        PRESCRIPTION_DRUG.fill(drug);
        SELECT_DROPDOWN_VALUES.nth(1).click();
        page.keyboard().press("Escape");
    }

    public void selectEngagementTypeAndContextualCategory(String engagementType, String contextualCategory) {
        ENGAGEMENT_TYPE.locator("text = " + engagementType).click();
        ENGAGEMENT_TYPE.locator("text = " + contextualCategory).click();
        waitUtility.waitUntilSpinnerHidden();
    }

    public void selectMESHCondition(String meshCondition) {
        MESH_DROPDOWN.click();
        MESH_CONDITION.fill(meshCondition);
        page.locator(String.format(
                        "//div[contains(@title,'%s')]/preceding-sibling::div[contains(@class,'targetIcons')]/div",
                        meshCondition))
                .click();
        OK_BUTTON.click();
    }

    public boolean fetchRecencyMinAndMaxValues(String recencyMin, String recencyMax) {
        return verifySliderRange(RECENCY_SLIDER, recencyMin, recencyMax);
    }

    public String fetchRecencyDefaultValue() {
        return RECENCY_DAYS.innerText().trim();
    }

    public void selectPrimeListWithHistoricalDataCheckbox() {
        PRIME_LIST_CHECKBOX.click();
    }

    public String hoverAndFetchTooltip(String contextualCategory) {
        Locator TOOLTIP_ICON = page.locator(String.format("//div[contains(text(),'%s')" +
                "]//span[@class='question-icon']", contextualCategory));
        waitUtility.waitForLocatorVisible(TOOLTIP_ICON);
        TOOLTIP_ICON.dispatchEvent("mouseover");
        TOOLTIP_ICON.dispatchEvent("mouseenter");
        waitUtility.waitForLocatorVisible(TOOL_TIP);
        return TOOL_TIP.innerText().trim();
    }

    public void selectMedscapePrimaryConcept(String contextualCategory, String concept) {
        if (contextualCategory.contains("Medscape")) {
            MEDSCAPE_PRIMARY_CONCEPT.click();
            MEDSCAPE_PRIMARY_CONCEPT.fill(concept);
        } else if (contextualCategory.contains("WebMD")) {
            WEBMD_PRIMARY_TOPIC.click();
            WEBMD_PRIMARY_TOPIC.fill(concept);
        }
        SELECT_DROPDOWN_VALUES.first().click();
        page.keyboard().press("Escape");
    }

    public boolean verifyMedscapeAndWebMDAreDisabled(String contextualCategory) {
        Locator xpath =
                page.locator(String.format("//div[contains(text(),'%s')]/ancestor::button", contextualCategory));
        return xpath.getAttribute("class").contains("disabled");
    }

    public void enterPopulationOptionsDetail(String option, Map<String, String> detailsMap) {
        switch (option) {
            case "Prescribed Drug":
                selectDrug(detailsMap.get("Drug"));
                break;
            case "Medical Procedure Code":
                enterMedicalProcedure(detailsMap.get("Procedure"));
                break;
            case "Profession":
                selectProfession(detailsMap.get("Profession"));
                break;
            case "Smart Pixel":
                clickSmartPixelDropDown();
                selectSmartPixelDropdownValue();
                selectEngagementType(detailsMap.get("EngagementType"));
                clickSearchKeywordsCheckbox();
                enterSearchKeywords(Collections.singletonList(detailsMap.get("Keyword")));
                break;
            case "NPI List":
                selectHCPSwitch(detailsMap.get("HCPSwitch"));
                selectNPIGroup(detailsMap.get("NPIGroup"));
                break;
            case "Prescription Behavior Change":
                selectPrescriptionDrug(detailsMap.get("Drug"));
                break;
        }
    }

    public void selectDropperValueFromSlider(String sliderType, String sliderValue) {
        switch (sliderType) {
            case "Top Dropper Percentage":
                CommonUtils.moveSliderToValue(TOP_DROPPER_SLIDER, Integer.parseInt(sliderValue), page);
                break;
            case "Time Frame Selector":
                CommonUtils.moveSliderToValue(TIME_FRAME_SELECTOR_SLIDER, Integer.parseInt(sliderValue), page);
                break;
            case "Recency":
                CommonUtils.moveSliderToValue(RECENCY_SLIDER, Integer.parseInt(sliderValue), page);
                break;
        }
    }

    public List<String> retrieveEnteredData(String listType) {
        List<String> enteredData = new ArrayList<>();
        if (LIST_NAME.isVisible()) enteredData.add(LIST_NAME.inputValue());
        else if (LIST_NAME_FROM_HEADER.isVisible())
            enteredData.add(LIST_NAME_FROM_HEADER.textContent().trim());
        if (!EDIT_ICON.isVisible() && FETCH_SELECTED_ADVERTISER.first().isVisible())
            enteredData.add(FETCH_SELECTED_ADVERTISER.first().textContent().trim());
        else if (ADVERTISER_NAME_FROM_HEADER.isVisible())
            enteredData.add(ADVERTISER_NAME_FROM_HEADER
                    .textContent()
                    .replace("Advertiser: ", "")
                    .trim());
        getValuesByClassAttribute(
                FETCH_SELECTED_AVAILABLE_IN,
                "mat-checkbox-checked",
                "xpath=//span[@class='mat-checkbox-label']",
                enteredData);
        switch (listType) {
            case "Smart Pixel" -> {
                enteredData.add(FETCH_SELECTED_SMART_PIXEL.textContent().trim());
                getValuesByClassAttribute(FETCH_SELECTED_ENGAGEMENT_SWITCH, "active", "xpath=//div", enteredData);
                getValuesByClassAttribute(
                        FETCH_SELECTED_RECENCY,
                        "mat-radio-checked",
                        "xpath=//div[@class='mat-radio-label-content']",
                        enteredData);
                if (FETCH_SELECTED_KEYWORD_FLAG.first().isVisible()) {
                    getValuesByClassAttribute(
                            FETCH_SELECTED_KEYWORD_FLAG,
                            "mat-radio-checked",
                            "xpath=//div[@class='mat-radio-label-content']",
                            enteredData);
                }
                addLocatorData(INSERT_VISITED_URL, enteredData, true);
                addLocatorData(INSERT_IGNORED_URL, enteredData, true);
                if (SEARCH_KEYWORDS_TEXTAREA.isVisible())
                    enteredData.add(SEARCH_KEYWORDS_TEXTAREA.inputValue().trim());
            }
            case "NPI List" -> {
                Locator npiListLocator = page.locator(String.format(
                        "//span[@class='mat-checkbox-label'  and contains(text(),'%s')]/ancestor::div[contains(@class,'section-name')]/following-sibling::div//button",
                        listType));
                getValuesByClassAttribute(npiListLocator, "active", "xpath=//div", enteredData);
                enteredData.add(FETCH_SELECTED_NPI_LIST.textContent().trim());
            }
            case "Specialty" -> {
                Locator specialtyLocator = page.locator(String.format(
                        "//span[@class='mat-checkbox-label'  and contains(text(),'%s')]/ancestor::div[contains(@class,'section-name')]/following-sibling::div//button",
                        listType));
                getValuesByClassAttribute(specialtyLocator, "active", "xpath=//div", enteredData);
                addLocatorData(FETCH_SELECTED_SPECIALTY, enteredData, false);
            }
            case "Profession" -> addLocatorData(FETCH_SELECTED_PROFESSION, enteredData, false);
            case "Prescribed Drug", "Diagnosis Code", "Medical Procedure Code" -> {
                Locator prescribedDrugLocator = page.locator(String.format(
                        "//span[@class='mat-checkbox-label'  and contains(text(),'%s')]/ancestor::div[contains(@class,'section-name')]/following-sibling::div//div[contains(@class,'clinicalPopulationSwitch')]//button",
                        listType));
                getValuesByClassAttribute(prescribedDrugLocator, "active", "xpath=//div", enteredData);
                enteredData.add(fetchRecency(listType));
                if (listType.contains("Prescribed Drug")) addLocatorData(FETCH_SELECTED_DRUG, enteredData, false);
                if (listType.contains("Diagnosis Code")
                        && FETCH_SELECTED_DIAGNOSIS_CODE.first().isVisible())
                    addLocatorData(FETCH_SELECTED_DIAGNOSIS_CODE, enteredData, false);
                if (listType.contains("Medical Procedure Code")
                        && FETCH_SELECTED_MEDICAL_PROCEDURE_CODE.first().isVisible())
                    addLocatorData(FETCH_SELECTED_MEDICAL_PROCEDURE_CODE, enteredData, false);
                enteredData.addAll(fetchDecile(listType));
            }
            case "Prescription Behavior Change" -> {
                getValuesByClassAttribute(
                        PRESCRIPTION_BEHAVIOUR_BUTTON, "active", "xpath=//parent::button", enteredData);
                addLocatorData(FETCH_SELECTED_PRESCRIPTION_DRUG, enteredData, false);
                if (FETCH_SELECTED_TOP_DROPPER_PERCENT.isVisible())
                    enteredData.add(
                            FETCH_SELECTED_TOP_DROPPER_PERCENT.textContent().trim());
                if (FETCH_SELECTED_TIME_FRAME_MONTHS.isVisible())
                    enteredData.add(
                            FETCH_SELECTED_TIME_FRAME_MONTHS.textContent().trim());
            }
            case "Endemic Research" -> {
                Locator buttonXpath = ENDEMIC_RESEARCH_SECTION.locator("xpath=//button");
                getValuesByClassAttribute(buttonXpath, "active", "", enteredData);
                Locator dropdownXpath = ENDEMIC_RESEARCH_SECTION.locator("xpath=//div[@placement='top']");
                addLocatorData(dropdownXpath, enteredData, false);
                enteredData.add(FETCH_SELECTED_RECENCY_DAYS.textContent().trim());
            }
        }
        return enteredData;
    }

    public void getValuesByClassAttribute(
            Locator locator, String classValue, String extendedLocator, List<String> dataList) {
        for (int i = 0; i < locator.count(); i++) {
            String classAttribute = locator.nth(i).getAttribute("class");
            if (classAttribute != null && classAttribute.contains(classValue)) {
                String value = "";
                if (!extendedLocator.isEmpty()) {
                    value = locator.nth(i).locator(extendedLocator).textContent();
                } else {
                    value = locator.nth(i).textContent();
                }
                dataList.add(value.trim());
            }
        }
    }

    private void addLocatorData(Locator locator, List<String> dataList, boolean useInputValue) {
        if (locator.first().isVisible()) {
            for (int i = 0; i < locator.count(); i++) {
                String value = useInputValue
                        ? locator.nth(i).inputValue()
                        : locator.nth(i).textContent();
                dataList.add(value.trim());
            }
        }
    }

    public String fetchDiagnosisCodesFromUI() {
        return DIAGNOSIS_CODE_FILE_DETAILS.textContent().split(" ")[0];
    }

    public String fetchMedicalProcedureCodesFromUI() {
        return MEDICAL_PROCEDURE_CODE_FILE_DETAILS.textContent().split(" ")[3];
    }

    public void selectDecile(String sliderValue, String listOption) {
        switch (listOption) {
            case "Prescribed Drug":
                PRESCRIBED_DRUG_DECILE.first().click();
                break;
            case "Diagnosis Code":
                DIAGNOSIS_DECILE.first().click();
                break;
            case "Medical Procedure":
                MEDICAL_PROCEDURE_DECILE.first().click();
                break;
        }
        CommonUtils.moveSliderToValue(DECILE_SLIDER, Integer.parseInt(sliderValue), page);
        page.keyboard().press("Escape");
    }
}
