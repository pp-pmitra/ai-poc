Feature: Create a Destination in Admin and run below report against it.
  1. Run a Report
  2. Schedule a Report

  Background:
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    When User navigates to Administrative section
    And User navigates to Accounts Tab
    And User searches the account "automation@pulsepoint" and selects the account
    And User navigates to Reporting tab
    Then User clicks Add Destination button
    And User enters Destination details - "Auto_Destination_", "SFTP", "ma2-qa-automation01", "22"
    And User clicks Test Connection link to verify if connection happened successfully
    And User saves the custom destination
    And User clicks PulsePoint icon to navigate back to Life

  @e2e
  Scenario Outline: End to End Workflow of create a Destination in Admin and run a Run Report against it
    And User navigates to Report Templates page
    Then Verify the tabs displayed on the Report Templates page
    When User clicks on New Template
    Then Verify the tabs displayed on the Create New Template panel
    When User enters the template details for end to end as "<TEMPLATE NAME>" "<DIMENSIONS>" "<METRICS>"
    When User saves the new template
    When User navigates to run report from mega menu of the life application
    And Verify Run Report panel should be opened
    When Tactic of selected line items should load when user types tactic names initials "<TACTIC_INITIALS>" in "Tactic" field
    Then User should be able to select value from dropdown
    And User should be able to fetch details - Advertiser, Campaign, Line Item, Tactic
    And User should be able to select the created template from the dropdown
    When User clicks on "Custom Destination" tab as Delivery Method
    Then User selects destination name created, and other details - "<FILE_PATH>", "<FILE_NAME>"
    And User should be able to generate the report
    #Then User downloads the report and verify the data in downloaded report
    Examples:
      | FILE_PATH                      | FILE_NAME                  | TACTIC_INITIALS | TEMPLATE NAME | DIMENSIONS                    | METRICS            |
      | /home/NPIAutoImport/Automation | AutoImport_Automation1.csv | Multiple_Flight | Template      | Advertiser Name,Campaign Name | Impressions,Clicks |

  @e2e
  Scenario Outline: End to End Workflow of create a Destination in Admin and run a Schedule Report against it
    And User navigates to Report Templates page
    Then Verify the tabs displayed on the Report Templates page
    When User clicks on New Template
    Then Verify the tabs displayed on the Create New Template panel
    When User enters the template details for end to end as "<TEMPLATE NAME>" "<DIMENSIONS>" "<METRICS>"
    When User saves the new template
    When User navigates to Schedule report from mega menu of the life application
    And User clicks Schedule Report button
    Then Verify Schedule Report panel should be opened
    When Tactic of selected line items should load when user types tactic names initials "<TACTIC_INITIALS>" in "Tactic" field
    Then User should be able to select value from dropdown
    And User should be able to fetch details - Advertiser, Campaign, Line Item, Tactic
    And User should be able to select the created template from the dropdown
    And Verify Report Name field is available and accepts input "<REPORT_NAME>"
    And User clicks "<FREQUENCY_VALUE>" as frequency
    And Verify that user is able to select Schedule start date and Schedule end date
    And Verify that user is able to select Data Timezone field value "<TIME_ZONE>"
    And Verify user is able to select Time "10:00" and Timezone "<TIME_ZONE>" for Send At fields
    And Verify Report Period is selected as "Month to Date"
    When User clicks on "Custom Destination" tab as Delivery Method
    Then User selects destination name created, and other details - "<FILE_PATH>", "<FILE_NAME>"
    And User clicks Schedule button to generate the report
    #Then User downloads the report and verify the data in downloaded report
    Examples:
      | FILE_PATH                      | FILE_NAME                  | TACTIC_INITIALS | REPORT_NAME    | FREQUENCY_VALUE | TIME_ZONE                       | TEMPLATE NAME | DIMENSIONS                    | METRICS            |
      | /home/NPIAutoImport/Automation | AutoImport_Automation1.csv | Multiple_Flight | ScheduleReport | Daily           | (GMT+05:30) India Standard Time | Template      | Advertiser Name,Campaign Name | Impressions,Clicks |
