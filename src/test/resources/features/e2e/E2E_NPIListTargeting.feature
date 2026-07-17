Feature: End to End workflow for NPI Lists - Attributed and Auto-Imported creation and target it at Tactic level
  It covers below points
  1. Creation of NPI List - Attribute List and Auto-Imported List
  2. Targeting the created List at Tactic level

  Background:
    #1
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    #2
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed

  @e2e
  Scenario Outline: Create a Attribute NPI List and target in 'NPI' targeting at Tactic level
    #3
    And User selects the Attributes List and uploads the file "<FILE_NAME>"
    Then Verify file "<FILE_NAME>" is uploaded successfully
    And User selects the "<COLUMN_NAME>" column and clicks on Next
    And User enters the Attributes list details as "<LIST_NAME>" "<ADVERTISER>"
    When User makes list available in LIFE and HCP365 and clicks on next
    Then Verify the Attributes list is saved successfully
    #4
    And Navigate to Campaign Dashboard and clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    #5
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    #6
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    And User selects the "<CHANNEL>" as channel
    And User add and configure "NPI" targeting rule and verify list is displayed in the targeting rule
    And Verify that the total NPI count and the matched NPI count from the list are correctly displayed in the targeting rule
    And User saves the rule configured in the tactic
    #7
    Then Verify that the "NPI" rule is added to the tactic and retrieve the count of selected lists
    And Verify that the selected list is displayed in the targeting rule and retrieve the total count of targeted items
    And User saves the targeting
    Examples:
      | LIST_NAME | ADVERTISER     | FILE_NAME                | COLUMN_NAME | CP_NAME               | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME | CHANNEL          |
      | ATTRIBUTE | 01- Advertiser | NPI_AttributeList01.xlsx | NPI         | AttributeNPI_Campaign | Regular | 20000     | Line      | 500         | Tactic      | Display Advanced |

  @e2e
  Scenario Outline: Create Auto-Imported NPI List using Reload Now button and target in 'NPI' targeting at Tactic level
    #3
    And User selects the Auto-Imported List
    And Verify if user navigates to the Auto-Imported List page
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
    #4
    And Navigate to Campaign Dashboard and clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    #5
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    #6
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    And User selects the "<CHANNEL>" as channel
    And User add and configure "NPI" targeting rule and verify list is displayed in the targeting rule
    And Verify that the total NPI count and the matched NPI count from the list are correctly displayed in the targeting rule
    And User saves the rule configured in the tactic
    #7
    Then Verify that the "NPI" rule is added to the tactic and retrieve the count of selected lists
    And Verify that the selected list is displayed in the targeting rule and retrieve the total count of targeted items
    And User saves the targeting
    Examples:
      | LIST_NAME     | ADVERTISER     | FILE_LOCATION | FILE_PATH                      | FILE_NAME                  | LIST_TYPE            | NPI_COLUMN_NAME | IMPORT_TYPE    | CP_NAME                  | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME | CHANNEL          |
      | Auto_Imported | 01- Advertiser | 1OurVM        | /home/NPIAutoImport/Automation | AutoImport_Automation1.csv | List with Attributes | NPI             | Import Columns | AutoImportedNPI_Campaign | Regular | 20000     | Line      | 500         | Tactic      | Display Advanced |