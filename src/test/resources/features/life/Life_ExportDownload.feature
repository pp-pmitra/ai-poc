Feature: LIFE Regression - This feature verifies the export/download functionality for various list types created through file uploads:
  1. Keyword List
  2. Domain List
  3. App Bundle List
  4. IP Address List
  5. Static NPI List

  Background:
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"

  @regression
  Scenario Outline: Verify user is able to export Keyword list created by uploading a file
    Given User navigates to the "Keyword Lists" page
    And Verify that the search option is present on the "Keyword Lists" tab
    When User clicks on Create New List
    Then Verify that the Create New List screen is displayed
    And Verify that when enters "<LIST_NAME>" and upload file "<UPLOAD_FILENAME1>" option is selected, the text area to direct enter the names disappears
    And Verify the Uploaded Files section displays the entries count, includes download and delete icons after the file "<UPLOAD_FILENAME1>" is uploaded
    And Verify that the user is able to create a "Keywords" list through file upload
    And Verify that the counter on the left displays the correct value after file upload "<UPLOAD_FILENAME1>"
    And Verify that user is able to download the uploaded "Keyword" list
    And Verify that the count of items in the downloaded "Keyword" list is the same as the item count displayed in the UI
    Examples:
      | LIST_NAME          | UPLOAD_FILENAME1  |
      | Keyword_FileUpload | KeywordsFile1.csv |

  @regression
  Scenario Outline: Verify user is able to export Domain list created by uploading a file
    Given User navigates to the "Domain & App Lists" page
    When User clicks on Create New List
    And User selects the "Domains" radio button from create new list page
    Then Verify that the Create New List screen is displayed
    And Verify that when enters "<LIST_NAME>" and upload file "<UPLOAD_FILENAME1>" option is selected, the text area to direct enter the names disappears
    And Verify the Uploaded Files section displays the entries count, includes download and delete icons after the file "<UPLOAD_FILENAME1>" is uploaded
    And Verify that the user is able to create a "Domains" list through file upload
    And Verify that the counter on the left displays the correct value after file upload "<UPLOAD_FILENAME1>"
    And Verify that user is able to download the uploaded "Domain" list
    And Verify that the count of items in the downloaded "Domain" list is the same as the item count displayed in the UI
    Examples:
      | LIST_NAME         | UPLOAD_FILENAME1    |
      | Domain_FileUpload | DomainNameFile1.csv |

  @regression
  Scenario Outline: Verify user is able to export App Bundle list created by uploading a file
    Given User navigates to the "Domain & App Lists" page
    When User clicks on Create New List
    And User selects the "App Bundles" radio button from create new list page
    Then Verify that the Create New List screen is displayed
    And Verify that an error message is displayed when no list names is specified and user tries to upload a file "<UPLOAD_FILENAME1>"
    And Verify that when enters "<LIST_NAME>" and upload file "<UPLOAD_FILENAME1>" option is selected, the text area to direct enter the names disappears
    And Verify the Uploaded Files section displays the entries count, includes download and delete icons after the file "<UPLOAD_FILENAME1>" is uploaded
    And Verify that the user is able to create a "AppBundle" list through file upload
    And Verify that the counter on the left displays the correct value after file upload "<UPLOAD_FILENAME1>"
    And Verify that user is able to download the uploaded "App Bundle" list
    And Verify that the count of items in the downloaded "App Bundle" list is the same as the item count displayed in the UI
    Examples:
      | LIST_NAME            | UPLOAD_FILENAME1   |
      | AppBundle_FileUpload | AppBundleFile1.csv |

  @regression
  Scenario Outline: Verify user is able to export IP list created by uploading a file
    And User navigates to the "IP Address Lists" page
    When User clicks on Create New List
    Then Verify that the Create New List screen is displayed
    And Verify that when enters "<LIST_NAME>" and upload file "<UPLOAD_FILENAME1>" option is selected, the text area to direct enter the names disappears
    And Verify the Uploaded Files section displays the entries count, includes download and delete icons after the file "<UPLOAD_FILENAME1>" is uploaded
    And Verify that the user is able to create a "IP Address" list through file upload
    And Verify that the counter on the left displays the correct value after file upload "<UPLOAD_FILENAME1>"
    And Verify that user is able to download the uploaded "IP" list
    And Verify that the count of items in the downloaded "IP" list is the same as the item count displayed in the UI
    Examples:
      | LIST_NAME            | UPLOAD_FILENAME1   |
      | IPAddress_FileUpload | IPAddressFile1.csv |

  @regression
  Scenario Outline: Verify user is able to export the Regular NPI list created by uploading a file
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Static List
    And User enters the NPI Static list details as "<LIST_NAME>" "<ADVERTISER>"
    And User uploads the file "<FILE_NAME>"
    When User makes list available in "LIFE" and saves the list
    Then Verify list gets saved successfully
    And Verify that user is able to download the uploaded "NPI" list
    And Verify that the count of items in the downloaded "NPI" list is the same as the item count displayed in the UI
    Examples:
      | LIST_NAME  | ADVERTISER     | FILE_NAME          |
      | STATIC_NPI | 01- Advertiser | NPIStaticList.xlsx |

  @regression
  Scenario Outline: Verify user is able to export Smart NPI list
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>"
    And User selects Smart NPI list as "<TYPE>"
    And User selects "<MEDICAL_PROCEDURE>" from "<TYPE>" dropdown
    And Verify that Recency is set to "365" by default for "<TYPE>"
    And verify that Decile is set to "1-10" by default for "<TYPE>"
    And User retrieves all the entered data before saving the list "<TYPE>"
    And User saves the Smart List and verifies the successful creation of the list
    And Verify that user is able to download the "NPI" list
    And Verify that the count of items in the downloaded "NPI" list is the same as the item count displayed in the UI
    Examples:
      | ADVERTISER     | LIST_NAME | TYPE                   | MEDICAL_PROCEDURE                                 |
      | 01- Advertiser | SMART_NPI | Medical Procedure Code | Cardiac shunt imaging, Florbetaben f18 diagnostic |

  @regression
  Scenario Outline: Verify user is able to export PulsePoint Provided NPI list
    And User navigates to NPI Lists page
    And User searches and selects the NPI List "<LIST_NAME>"
    And Verify that user is able to download the "NPI" list
    And Verify that the count of items in the downloaded "NPI" list is the same as the item count displayed in the UI
    Examples:
      | LIST_NAME             |
      | AutoNPIAdmin257977008 |

  @regression
  Scenario Outline: Verify export option is not available for Email list created by uploading a file
    And User navigates to the "Email Lists" page
    When User clicks on Create New List
    Then Verify that the Create New List screen is displayed
    And User enters the list name as "<LIST_NAME>" and uploads the file "<UPLOAD_FILENAME1>"
    And User saves the Email list and verify that the list is created successfully
    And Verify that the counter on the left displays the correct value after file upload for "Email list"
    And Verify that download option should not be available for uploaded Email list
    Examples:
      | LIST_NAME        | UPLOAD_FILENAME1 |
      | Email_FileUpload | EmailFile1.csv   |

  @regression
  Scenario Outline: Verify user is able to export the audit log of a campaign, line item and tactic
    And User clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    When User selects the "<CHANNEL>" as channel
    And User selects "<RULE_TYPE>" as rule type and configures the targeting rules, and saves the settings
    Then Verify settings details are saved and user is navigated to the creatives tab
    And User assigns the existing creative named "<CREATIVE>", enables the tactic and saves the changes
    Then Verify the newly created campaign is in running state
    Then Verify that user is able to export the audit log for "campaign"
    When User navigates to "line item" page
    Then Verify that user is able to export the audit log for "line item"
    When User navigates to "tactic" page
    Then Verify that user is able to export the audit log for "tactic"
    Examples:
      | ADVERTISER     | CP_NAME | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME | CHANNEL          | RULE_TYPE          | CREATIVE      |
      | 01- Advertiser | Auto    | Regular | 20000     | Line      | 500         | Tactic      | Display Advanced | Behavioral Segment | Auto_Creative |

  @regression
  Scenario Outline: Verify user is able to export settings of the campaign having "Single" line items
    And User clicks on Create Campaign
    When User enters the campaign details as "01- Advertiser" "<CAMPAIGN_NAME>" "Regular" "20000" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User creates line items with below line types and other details, enables the line item and saves the changes
      | LINE_TYPE | LINE_ITEM_DETAILS                                                                                                                       |
      | Display   | LineName:LineItem, LineBudget:500, CostModel:Fixed CPM, CPMAmount:50, BudgetDistribution:Priority, PacingMode:Even, PacingPercentage:60 |
    And User clicks New Tactic button, create tactic with details - "<RULE_TYPE>", "<CREATIVE>"
    And User navigates to campaign
    Then Verify that user is able to export the campaign settings
    Examples:
      | CAMPAIGN_NAME      | RULE_TYPE          | CREATIVE           |
      | Single_LI_Campaign | Behavioral Segment | Please_Dont_Delete |

  @regression
  Scenario Outline: Verify user is able to export settings of the campaign having "Multiple" line items
    And User clicks on Create Campaign
    When User enters the campaign details as "01- Advertiser" "<CAMPAIGN_NAME>" "Regular" "20000" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User creates line items with below line types and other details, enables the line item and saves the changes
      | LINE_TYPE | LINE_ITEM_DETAILS                                                                                                                       |
      | Display   | LineName:LineItem, LineBudget:500, CostModel:Fixed CPM, CPMAmount:50, BudgetDistribution:Priority, PacingMode:Even, PacingPercentage:60 |
      | Display   | LineName:LineItem, LineBudget:500, CostModel:CPM, CPMAmount:50, BudgetDistribution:Percentage, PacingMode:Ahead, PacingPercentage:60    |
    And User clicks New Tactic button, create tactic with details - "<RULE_TYPE>", "<CREATIVE>"
    And User navigates to campaign
    Then Verify that user is able to export the campaign settings
    Examples:
      | CAMPAIGN_NAME        | RULE_TYPE          | CREATIVE           |
      | Multiple_LI_Campaign | Behavioral Segment | Please_Dont_Delete |

  @regression
  Scenario Outline: Verify user is able to export settings of the campaign having "Different" line items
    And User clicks on Create Campaign
    When User enters the campaign details as "01- Advertiser" "<CAMPAIGN_NAME>" "Regular" "20000" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User creates line items with below line types and other details, enables the line item and saves the changes
      | LINE_TYPE      | LINE_ITEM_DETAILS                                                                                                                          |
      | Audio          | LineName:LineItem, LineBudget:500, CostModel:CPM, CPMAmount:50, BudgetDistribution:Dollars, PacingMode:ASAP, PacingPercentage:60           |
      | Video          | LineName:LineItem, LineBudget:500, CostModel:Fixed CPM, CPMAmount:50, BudgetDistribution:Percentage, PacingMode:Ahead, PacingPercentage:60 |
      | Native Display | LineName:LineItem, LineBudget:500, CostModel:CPM, CPMAmount:50, BudgetDistribution:Priority, PacingMode:Even, PacingPercentage:60          |
    And User clicks New Tactic button, create tactic with details - "<RULE_TYPE>", "<CREATIVE>"
    And User navigates to campaign
    Then Verify that user is able to export the campaign settings
    Examples:
      | CAMPAIGN_NAME         | RULE_TYPE          | CREATIVE           |
      | Different_LI_Campaign | Behavioral Segment | Please_Dont_Delete |