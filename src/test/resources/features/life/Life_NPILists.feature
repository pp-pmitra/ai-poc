Feature: LIFE regression - Create NPI List of following types:
  1. Static NPI List by specifying NPI Numbers
  2. Static NPI List by uploading file with NPI Numbers
  3. Smart NPI List by specifying Type
  4. Attribute NPI List
  5. Auto Import List creation

  Background:
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"

  @regression
  Scenario Outline: Create Static NPI List by specifying NPI Numbers.
    When User navigates to Administrative section
    And User navigates to Accounts Tab
    And User searches the account "automation@pulsepoint" and selects the account
    And User opens Life Settings
    And User fetches PulsePoint Data Fees, NPI Targeting Gross CPM and calculates the data cost
    And User clicks PulsePoint icon to navigate back to Life
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Static List
    And User enters the NPI list details as "<LIST_NAME>" "<ADVERTISER>" "<NPI_NUMBER>"
    When User makes list available in "LIFE" and saves the list
    And User retrieves all the entered data before saving the Static List
    Then Verify list gets saved successfully
    And User verifies the calculated data cost is similar to the displayed data cost
    And User retrieves all the entered data after saving the Static List
    Examples:
      | ADVERTISER                      | NPI_NUMBER | LIST_NAME  |
      | 01- Advertiser,1Demo Advertiser | 1478523698 | STATIC_NPI |

  @regression
  Scenario Outline: Create, update, and delete Static NPI List by uploading file "<FILE_NAME>" with NPI Numbers
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Static List
    When User tries to save the list without entering any details, an error message should be displayed
    And User enters the NPI Static list details as "<LIST_NAME>" "<ADVERTISER>"
    And User uploads the file "<FILE_NAME>"
    When User makes list available in "LIFE" and saves the list
    Then Verify list gets saved successfully
    And Verify the NPI Numbers from the uploaded file "<FILE_NAME>" are displayed correctly in the list details page
    When User edits the created list
    Then Verify list gets updated successfully
    When User deletes the created list
    Then Verify list gets deleted successfully
    Examples:
      | LIST_NAME  | ADVERTISER     | FILE_NAME          |
      | STATIC_NPI | 01- Advertiser | NPIStaticList.xlsx |
      | STATIC_NPI | 01- Advertiser | NPIStaticList.csv  |
      | STATIC_NPI | 01- Advertiser | NPIStaticList.txt  |

  @regression
  Scenario Outline: Create, update, and delete an Attribute NPI List by uploading a "<FILE_NAME>" file with NPI Attributes
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects the Attributes List and uploads the file "<FILE_NAME>"
    Then Verify file "<FILE_NAME>" is uploaded successfully
    And User selects the "<COLUMN_NAME>" column and clicks on Next
    When User tries to save the Attribute list without entering any details, an error message should be displayed
    And User enters the Attributes list details as "<LIST_NAME>" "<ADVERTISER>"
    When User makes list available in LIFE and HCP365 and clicks on next
    Then Verify the Attributes list is saved successfully
    And Verify the NPI Numbers from the uploaded file "<FILE_NAME>" are displayed correctly in the list details page
    When User edits the saved list
    And User saves the list after making updates
    Then Verify the updates are applied successfully
    When User deletes the "Attribute" list
    Then Verify the list is deleted successfully
    Examples:
      | LIST_NAME | ADVERTISER     | FILE_NAME                 | COLUMN_NAME |
      | ATTRIBUTE | 01- Advertiser | NPIAttributeList.xlsx     | NPI         |
      | ATTRIBUTE | 01- Advertiser | NPI_AttributeListCSV.csv  | NPI         |
      | ATTRIBUTE | 01- Advertiser | NPI_AttributeListText.txt | NPI         |

  @regression
  Scenario Outline: Create Auto-Imported NPI List with "<LIST_TYPE>" by uploading file using API
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects the Auto-Imported List
    And Verify if user navigates to the Auto-Imported List page
    Then User tries to save the Auto-Imported list without entering any details, an error message should be displayed
    When User enters the Auto-Imported list details as "<LIST_NAME>" "<ADVERTISER>"
    And User makes list available in LIFE and HCP365 module
    And User clicks Setup Import button to import File details
    And User enters file details "<FILE_LOCATION>" "<FILE_PATH>" "<FILE_NAME>"
    And User selects the "<LIST_TYPE>" radio button
    And User enters NPI column "Name" "<NPI_COLUMN_NAME>"
    And User selects the "<IMPORT_TYPE>"
    Then User clicks Check File button to verify the file details are correct
    Then User saves the import settings and verifies the data is imported successfully
    And Verify that Token is fetched successfully from URL "BuyerProxy.ashx"
    And Pass token in the API Header and run it to upload the data into the list
    And Verify list data is uploaded successfully
    And Refresh the Browser to view the data uploaded
    And Verify the Total NPI count displayed in Matched NPI section is similar to NPI records present in "<FILE_NAME>"
    Examples:
      | LIST_NAME     | ADVERTISER     | FILE_LOCATION | FILE_PATH                      | FILE_NAME                  | LIST_TYPE            | NPI_COLUMN_NAME | IMPORT_TYPE    |
      | Auto_Imported | 01- Advertiser | 1OurVM        | /home/NPIAutoImport/Automation | AutoImport_Automation1.csv | Plain List           | NPI             | Add new NPIs   |
      | Auto_Imported | 01- Advertiser | 1OurVM        | /home/NPIAutoImport/Automation | AutoImport_Automation1.csv | List with Attributes | NPI             | Import Columns |

  @regression
  Scenario Outline: Create Auto-Imported NPI List with "<LIST_TYPE>" by uploading file using Reload Now button
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects the Auto-Imported List
    And Verify if user navigates to the Auto-Imported List page
    Then User tries to save the Auto-Imported list without entering any details, an error message should be displayed
    When User enters the Auto-Imported list details as "<LIST_NAME>" "<ADVERTISER>"
    And User makes list available in LIFE and HCP365 module
    And User clicks Setup Import button to import File details
    And User enters file details "<FILE_LOCATION>" "<FILE_PATH>" "<FILE_NAME>"
    And User selects the "<LIST_TYPE>" radio button
    And User enters NPI column "Name" "<NPI_COLUMN_NAME>"
    And User selects the "<IMPORT_TYPE>"
    Then User clicks Check File button to verify the file details are correct
    Then User saves the import settings and verifies the data is imported successfully
    And Verify Reload Now button is available and enabled
    When User clicks on Reload Now button
    Then Verify the file is reloaded successfully
    And Verify the Total NPI count displayed in Matched NPI section is similar to NPI records present in "<FILE_NAME>"
    Examples:
      | LIST_NAME     | ADVERTISER     | FILE_LOCATION | FILE_PATH                      | FILE_NAME                  | LIST_TYPE            | NPI_COLUMN_NAME | IMPORT_TYPE    |
      | Auto_Imported | 01- Advertiser | 1OurVM        | /home/NPIAutoImport/Automation | AutoImport_Automation1.csv | List with Attributes | NPI             | Import Columns |

  @regression
  Scenario Outline: Validate List Population Options in Smart List Creation Panel
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    Then Verify Smart List Creation Panel should display the following List Population Options
      | Smart Pixel                                       |
      | NPI List                                          |
      | Specialty                                         |
      | Profession                                        |
      | Prescribed Drug                                   |
      | Prescription Behavior Change                      |
      | Diagnosis Code                                    |
      | Medical Procedure Code                            |
      | Endemic Research                                  |
      | Expand based on Practice and Hospital Affiliation |
    Examples:
      | ADVERTISER     | LIST_NAME       |
      | 01- Advertiser | SMART_Pixel_NPI |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List as a Smart Pixel by selecting the engagement type "<ENGAGEMENT_TYPE>"
    And User navigates to Pixels page
    Then Verify the tabs displayed on the Pixels page
    And User selects the "<ADVERTISER>" and fetches Smart pixel list
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And User verifies the Smart Pixel dropdown displays all Smart Pixels for the selected advertiser and select the pixel
    And User selects Engagement Type "<ENGAGEMENT_TYPE>" and enter related details "<VISITED_URLS>", "<IGNORED_URLS>", "<KEYWORDS>"
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE        | ENGAGEMENT_TYPE    | VISITED_URLS                        | IGNORED_URLS                         | KEYWORDS                     |
      | 01- Advertiser | SMART_Pixel_NPI | Smart Pixel | Engaged on Site    | www.brooklyn.com, www.manhattan.com | www.cambridge.org, www.wikipedia.org |                              |
      | 01- Advertiser | SMART_Pixel_NPI | Smart Pixel | Engaged via Search |                                     |                                      | Active Shooter, Antisemitism |
      | 01- Advertiser | SMART_Pixel_NPI | Smart Pixel | Engaged Anywhere   | www.brooklyn.com, www.manhattan.com | www.cambridge.org, www.wikipedia.org | Active Shooter, Antisemitism |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List as a NPI List by selecting the HCP Switch type "<HCP_SWITCH>"
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And Verify "<TYPE>" population option is disabled when Advertiser value is not selected
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And User selects the HCP switch "<HCP_SWITCH>"
    And User selects the NPI Group "<NPI_GROUP_NAME>"
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE     | HCP_SWITCH | NPI_GROUP_NAME |
      | 01- Advertiser | SMART_Pixel_NPI | NPI List | HCP From   | NPI_           |

  @regression
  Scenario Outline: Validate the error message while creating Smart List as a NPI List by selecting the HCP Switch type "<HCP_SWITCH>"
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And User selects the HCP switch "<HCP_SWITCH>"
    And User selects the NPI data "<NPI_DATA>" for "<TYPE>"
    And User saves the Smart List and verifies the error message is displayed
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE      | HCP_SWITCH | NPI_DATA             |
      | 01- Advertiser | SMART_Pixel_NPI | NPI List  | Not From   | NPI_                 |
      | 01- Advertiser | SMART_Pixel_NPI | Specialty | Exclude    | Allergy & Immunology |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List as a Speciality by selecting the HCP Switch type "<HCP_SWITCH>"
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And User selects the HCP switch "<HCP_SWITCH>"
    And User selects the Speciality "<SPECIALITY>"
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE      | HCP_SWITCH   | SPECIALITY                           |
      | 01- Advertiser | SMART_Pixel_NPI | Specialty | Include Only | Allergy & Immunology, Anesthesiology |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List as a Profession
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And User selects the Profession "<PROFESSION>"
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE       | PROFESSION                     |
      | 01- Advertiser | SMART_Pixel_NPI | Profession | Nurse Practitioner, Pharmacist |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List as a Prescribed Drug without File upload
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And User selects "<DRUG>" from "<TYPE>" dropdown
    And Verify that Recency is set to "365" by default for "<TYPE>"
    And verify that Decile is set to "1-10" by default for "<TYPE>"
    And User selects the Decile value as "<DECILE>" from the slider for "Prescribed Drug"
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE            | DRUG                              | DECILE |
      | 01- Advertiser | SMART_Pixel_NPI | Prescribed Drug | Glynase, L-Oral PARACETAMOL Syrup | 4      |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List as a Prescribed Drug by uploading a File
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And User clicks Browse button to upload "<TYPE>" file "<FILE_NAME>"
    And Verify Bulk Upload template "<FILE_NAME>" records count matches UI count post upload
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE            | FILE_NAME                      |
      | 01- Advertiser | SMART_Pixel_NPI | Prescribed Drug | PrescribedDrugs_BulkUpload.txt |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List as a Diagnosis Code without File upload
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And User selects "<DIAGNOSIS>" from "<TYPE>" dropdown
    And Verify that Recency is set to "365" by default for "<TYPE>"
    And verify that Decile is set to "1-10" by default for "<TYPE>"
    And User selects the Decile value as "<DECILE>" from the slider for "Diagnosis Code"
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE           | DIAGNOSIS                                     | DECILE |
      | 01- Advertiser | SMART_Pixel_NPI | Diagnosis Code | Alcoholic fatty liver, Other specified sepsis | 5      |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List as a Diagnosis Code by uploading a File
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And User clicks Browse button to upload "<TYPE>" file "<FILE_NAME>"
    And Verify Bulk Upload template "<FILE_NAME>" records count matches UI count post upload
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE           | FILE_NAME                |
      | 01- Advertiser | SMART_Pixel_NPI | Diagnosis Code | Diagnosis_BulkUpload.txt |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List as a Medical Procedure without File upload
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And User selects "<MEDICAL_PROCEDURE>" from "<TYPE>" dropdown
    And Verify that Recency is set to "365" by default for "<TYPE>"
    And verify that Decile is set to "1-10" by default for "<TYPE>"
    And User selects the Decile value as "<DECILE>" from the slider for "Medical Procedure"
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE                   | MEDICAL_PROCEDURE                                 | DECILE |
      | 01- Advertiser | SMART_Pixel_NPI | Medical Procedure Code | Cardiac shunt imaging, Florbetaben f18 diagnostic | 6      |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List as a Medical Procedure by uploading a File
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And User clicks Browse button to upload "<TYPE>" file "<FILE_NAME>"
    And Verify Bulk Upload template "<FILE_NAME>" records count matches UI count post upload
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE                   | FILE_NAME                       |
      | 01- Advertiser | SMART_Pixel_NPI | Medical Procedure Code | MedicalProcedure_BulkUpload.txt |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List as a Prescription Behavior Change with Top Dropper option
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And Verify that Prescribed Behavior Change should display below tabs
      | Droppers        |
      | New Prescribers |
    And Verify that "Droppers" tab should be selected by default
    And User selects the prescription drug name "<DRUG_NAME>"
    And Verify that Top Droppers percentage slider should range from "1" to "100" and should be set to "100%" by default
    And User selects the "Top Dropper Percentage" value as "<TOP_DROPPER_PERCENT>" from the slider
    And Verify that Time Frame Selector slider should range from "6" to "12" months and should be set to "6 months" by default
    And User selects the "Time Frame Selector" value as "<TIME_FRAME_SELECTOR>" from the slider
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE                         | DRUG_NAME                   | TOP_DROPPER_PERCENT | TIME_FRAME_SELECTOR |
      | 01- Advertiser | SMART_Pixel_NPI | Prescription Behavior Change | Acne Reparatif, Parathyroid | 60                  | 10                  |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List as a Prescription Behavior Change with New Prescribers option
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And User selects the "New Prescribers" tab under Prescription Behavior Change
    And User selects the prescription drug name "<DRUG_NAME>"
    And Verify that Time Frame Selector slider should range from "6" to "12" months and should be set to "6 months" by default
    And User selects the "Time Frame Selector" value as "<TIME_FRAME_SELECTOR>" from the slider
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE                         | DRUG_NAME                   | TIME_FRAME_SELECTOR |
      | 01- Advertiser | SMART_Pixel_NPI | Prescription Behavior Change | Acne Reparatif, Parathyroid | 8                   |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List as a Endemic Research under IB Health with MESH option
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And User selects Engagement Type "<ENGAGEMENT_TYPE>" and contextual category "<CONTEXTUAL_CATEGORY>"
    And User clicks MESH dropdown, enters "<MESH_CONDITION>" and selects it
    And Verify that Recency slider should range from "1" to "60" days and should be set to "1 day" by default
    And User selects the "Recency" value as "<RECENCY>" from the slider
    And User checks Prime list with historical data check box
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE             | ENGAGEMENT_TYPE | CONTEXTUAL_CATEGORY | MESH_CONDITION  | RECENCY |
      | 01- Advertiser | SMART_Pixel_NPI | Endemic Research | IB Health       | MESH                | Animal Diseases | 55      |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List as a Endemic Research under IB Health with Medscape option
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And User selects Engagement Type "<ENGAGEMENT_TYPE>" and contextual category "<CONTEXTUAL_CATEGORY>"
    And User hovers over the "<CONTEXTUAL_CATEGORY>" question icon and fetches tool-tip "Primary Concept"
    And User clicks "<CONTEXTUAL_CATEGORY>" primary concept dropdown, enters "<MEDSCAPE_PRIMARY_CONCEPT>" and selects it
    And Verify that Recency slider should range from "1" to "60" days and should be set to "1 day" by default
    And User selects the "Recency" value as "<RECENCY>" from the slider
    And User checks Prime list with historical data check box
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE             | ENGAGEMENT_TYPE | CONTEXTUAL_CATEGORY | MEDSCAPE_PRIMARY_CONCEPT | RECENCY |
      | 01- Advertiser | SMART_Pixel_NPI | Endemic Research | IB Health       | Medscape            | Pacemaker, Arthritis     | 10      |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List as a Endemic Research under IB Health with WebMD option
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And User selects Engagement Type "<ENGAGEMENT_TYPE>" and contextual category "<CONTEXTUAL_CATEGORY>"
    And User hovers over the "<CONTEXTUAL_CATEGORY>" question icon and fetches tool-tip "Primary Topic"
    And User clicks "<CONTEXTUAL_CATEGORY>" primary concept dropdown, enters "<WEBMD_PRIMARY_TOPIC>" and selects it
    And Verify that Recency slider should range from "1" to "60" days and should be set to "1 day" by default
    And User selects the "Recency" value as "<RECENCY>" from the slider
    And User checks Prime list with historical data check box
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE             | ENGAGEMENT_TYPE | CONTEXTUAL_CATEGORY | WEBMD_PRIMARY_TOPIC | RECENCY |
      | 01- Advertiser | SMART_Pixel_NPI | Endemic Research | IB Health       | WebMD               | Knee Pain, Asthma   | 15      |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List as a Endemic Research under Endemic Network
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And User selects Engagement Type "<ENGAGEMENT_TYPE>" and contextual category "<CONTEXTUAL_CATEGORY>"
    And Verify that "Medscape" is disabled under Endemic Network
    And Verify that "WebMD" is disabled under Endemic Network
    And User clicks MESH dropdown, enters "<MESH_CONDITION>" and selects it
    And Verify that Recency slider should range from "1" to "60" days and should be set to "1 day" by default
    And User selects the "Recency" value as "<RECENCY>" from the slider
    And User checks Prime list with historical data check box
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE             | ENGAGEMENT_TYPE | CONTEXTUAL_CATEGORY | MESH_CONDITION  | RECENCY |
      | 01- Advertiser | SMART_Pixel_NPI | Endemic Research | Endemic Network | MESH                | Animal Diseases | 20      |

  @regression
  Scenario Outline: Validate error message when saving Smart List as Expand based on Practice and Hospital Affiliation without selecting other Population options
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And The user saves the Smart List without selecting any other Population options and verifies error message
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE                                              |
      | 01- Advertiser | SMART_Pixel_NPI | Expand based on Practice and Hospital Affiliation |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List with multiple population options such as a Profession, Prescribed Drug and Medical Procedure Code
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as below with mandatory details
      | PopulationOption       | OptionDetails                   |
      | Profession             | Profession:Nurse Practitioner   |
      | Prescribed Drug        | Drug:Glynase                    |
      | Medical Procedure Code | Procedure:Cardiac shunt imaging |
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE                                                |
      | 01- Advertiser | SMART_Pixel_NPI | Profession, Prescribed Drug, Medical Procedure Code |

  @regression
  Scenario Outline: Validate the successful creation of a Smart List with multiple population options such as a Smart Pixel, NPI List, Prescription Behavior Change
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as below with mandatory details
      | PopulationOption                                  | OptionDetails                                             |
      | Smart Pixel                                       | EngagementType:Engaged via Search, Keyword:Active Shooter |
      | NPI List                                          | HCPSwitch:HCP From, NPIGroup:NPI_                         |
      | Prescription Behavior Change                      | Drug:Parathyroid                                          |
      | Expand based on Practice and Hospital Affiliation |                                                           |
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that the retrieved data for the "<TYPE>" list was saved correctly
    Examples:
      | ADVERTISER     | LIST_NAME       | TYPE                                                                                                   |
      | 01- Advertiser | SMART_Pixel_NPI | Smart Pixel, NPI List, Prescription Behavior Change, Expand based on Practice and Hospital Affiliation |