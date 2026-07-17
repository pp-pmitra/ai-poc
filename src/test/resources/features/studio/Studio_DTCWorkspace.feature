Feature: DTC Workspace creation in Studio
  1. Creation of DTC Workspace in Studio
  2. Applying advertiser and filters to the workspace
  3. Verify the workspace is saved successfully
  4. Verify the submission based on Unique Consumers count

  Background:
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    When User navigates to Administrative section
    And User navigates to Accounts Tab
    And User searches the account "PP engineering test" and checks Studio permissions
    And User clicks PulsePoint icon to navigate back to Life
    And User navigates to Studio application

  @regression
  Scenario Outline: Create DTC workspace based on Unique Consumers
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "DTC Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And User applies the filter and selects option
      | FilterName | Option |
      | Gender     | Female |
    And User clicks on Ok and closes the filter popup
    Then Verify that the applied filters are displayed correctly
    And User saves the "DTC Explorer" workspace
    Then Verify the "DTC Explorer" Workspace is saved
    Then User captures the "Unique Consumers" count
    Then Verify whether the "Unique Consumers" count is greater than or equals to 100000
    And User clicks on Submit button
    And User verifies if workspace is saved successfully and the submission is successful
    Then User verifies the dialog message as "Your Audience is being processed"
    Examples:
      | ADVERTISER         | WORKSPACE_NAME          |
      | TAMTESTING ACCOUNT | Automation_DTC_Explorer |
