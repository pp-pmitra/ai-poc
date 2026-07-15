Feature: E2E Workflow for Targeting a Studio HCP Explorer List in LIFE at the Tactic Level
  1. Creation of Workspace in Studio
  2. Publish the workspace as Studio List (Static List or Live List)
  3. Verify the published Studio list in LIFE
  4. Target the published Studio list at Tactic level in LIFE
  5. Verify that the list, once targeted, cannot be deleted from the Studio dashboard

  @e2e
  Scenario Outline: Create HCP Explorer Workspace in Studio and Publish in LIFE and Target at Tactic level
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    When User navigates to Administrative section
    And User navigates to Accounts Tab
    And User searches the account "PP engineering test" and checks Studio permissions
    And User clicks PulsePoint icon to navigate back to Life
    And User navigates to Studio application
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And User applies the filter and selects option
      | FilterName | Option   |
      | NPI Age    | 35 to 45 |
      | NPI Gender | Female   |
    And User clicks on Ok and closes the filter popup
    Then Verify that the applied filters are displayed correctly
    And User saves the "HCP Explorer" workspace
    Then Verify the "HCP Explorer" Workspace is saved
    And Download button is enabled to the user
    And User clicks on Publish NPI List
    And User selects publish "<LIST_TYPE>"
    And User select the platform to publish the list
    Then Verify list is published
    And User navigates to NPI Lists page
    And User searches the workspace in LIFE and selects it
    And User clicks on the published workspace
    Then User Verify the list is displayed in the Life
    And Navigate to Campaign Dashboard and clicks on Create Campaign
    And User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    And User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    And User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    And User selects the "<CHANNEL>" as channel
    And User selects "<RULE_TYPE>" as rule type and selects the published Studio list
    Then Verify the selected targeting rule "<RULE_TYPE>" and rule option
    Then Verify the count of rule options for the selected targeting rule "<RULE_TYPE>" on the Tactic Settings page
    And User saves the settings
    And User navigates to "Studio" application
    And User searches the created workspace
    When User selects the "Delete" option by clicking More Actions menu
    Then Verify that the workspace cannot be deleted and appropriate message is displayed to the user
    Examples:
      | ADVERTISER | WORKSPACE_NAME | LIST_TYPE | CP_NAME     | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME | CHANNEL          | RULE_TYPE |
      | Abbvie     | Studio_HCP     | Static    | Studio_List | Regular | 10000     | New_Line  | 50          | New_Tactic  | Display Advanced | NPI       |
