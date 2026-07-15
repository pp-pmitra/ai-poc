Feature: LIFE regression - Create NPI List of following types:
  1. Medscape List by uploading file

  Background:
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"

  @regression
  Scenario Outline: Create and delete Medscape List by uploading file "<FILE_NAME>"
    And User navigates to NPI Lists page
    When User clicks on Create New List
    Then Verify creation of NPI List screen is displayed
    And User selects Medscape List
    And Verify Advertiser is auto selected as "Medscape"
    And User enters the Medscape NPI list name as "<LIST_NAME>" and saves the list
    And Verify Upload File section is displayed on the Medscape List details page
    And User uploads the file "<FILE_NAME>"
    And User maps row headers from the uploaded spreadsheet to predefined labels as below
      | LABEL      | COLUMN_VALUE           |
      | RECORD_ID  | CUSTOMER_ID (Required) |
      | FIRST_NAME | FIRST_NAME             |
      | LAST_NAME  | LAST_NAME              |
      | ZIPCODE    | ZIP                    |
      | NPI_ID     | NPI_NUMBER             |
    And User saves the Medscape List and verify that the list is uploaded successfully with message "<MESSAGE>" and "<WAIT_TEXT>"
    When User edits the saved list
    And Verify file details are displayed correctly in the list details page
    And User saves the list after making updates
    Then Verify the updates are applied successfully
    When User deletes the "Medscape" list
    Then Verify the list is deleted successfully
    Examples:
      | LIST_NAME     | FILE_NAME            | MESSAGE                      | WAIT_TEXT                           |
      | Medscape_List | NPI_MedscapeList.csv | Soft Matching in Progress... | This process may take a few minutes |