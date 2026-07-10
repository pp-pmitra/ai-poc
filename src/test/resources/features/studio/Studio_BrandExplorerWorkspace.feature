Feature: Brand Explorer Workspace creation in Studio

  Background:
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    When User navigates to Administrative section
    And User navigates to Accounts Tab
    And User searches the account "PP engineering test" and checks Studio permissions
    And User clicks PulsePoint icon to navigate back to Life
    And User navigates to Studio application

  @e2e
  Scenario Outline: Create and save Brand Explorer workspace with default selections
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User edits the workspace name as "<WORKSPACE_NAME>"
    Then Verify that advertiser field is disabled and displayed in "rgba(34, 34, 34, 0.55)" after saving the workspace
    Then Verify Dimension "Day" and Metric "Identified NPIs" are selected by default in the workspace
    Then Verify Time Frame is selected as "Last 7 Days" by default in the workspace
    And User saves the "Brand Explorer" workspace
    Then Verify the "Brand Explorer" Workspace is saved
    Examples:
      | ADVERTISER         | WORKSPACE_NAME            |
      | TAMTESTING ACCOUNT | Automation_Brand_Explorer |

  @regression
  Scenario Outline: Verify all 9 preset timeframe options are present and correctly labeled
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    When User clicks the TimeFrame selector
    Then All 9 preset timeframe options are visible in the dropdown with correct labels
      | Yesterday     |
      | Last 7 Days   |
      | Last 14 Days  |
      | Last 30 Days  |
      | Last 60 Days  |
      | Last 90 Days  |
      | Last 180 Days |
      | Last 365 Days |
      | Custom        |
    Examples:
      | ADVERTISER          |
      | TAMTESTING ACCOUNT  |

  @regression
  Scenario Outline: Verify chart and table update immediately when a preset timeframe is selected
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    When User clicks the TimeFrame selector
    When User selects the timeframe preset "<TIMEFRAME>"
    Then Verify the chart and table update immediately to reflect "<TIMEFRAME>" data
    And Verify the Day column shows <DAYS> dates in ascending order
    Examples:
      | ADVERTISER         | TIMEFRAME    | DAYS |
      | TAMTESTING ACCOUNT | Last 14 Days | 14   |
      | TAMTESTING ACCOUNT | Last 30 Days | 30   |
      | TAMTESTING ACCOUNT | Yesterday    | 1    |

  @regression
  Scenario Outline: Verify Custom date range picker and inclusive start and end dates in the returned dataset
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    When User clicks the TimeFrame selector
    And User selects the timeframe preset "Custom"
    Then Verify a date picker with separate start and end date fields is displayed
    And Verify both start and end date fields are configurable
    When User sets a custom date range with start date "<START_DATE>" and end date "<END_DATE>"
    Then Verify "<START_DATE>" is the first date row in the table
    And Verify "<END_DATE>" is the last date row in the table
    Examples:
      | ADVERTISER         | START_DATE | END_DATE   |
      | TAMTESTING ACCOUNT | 2026-05-01 | 2026-05-07 |
      | TAMTESTING ACCOUNT | 2026-03-01 | 2026-05-07 |
      | TAMTESTING ACCOUNT | 2026-05-01 | 2026-05-01 |

  @regression
  Scenario Outline: Verify an error is shown when the custom start date is later than the end date
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    When User clicks the TimeFrame selector
    And User selects the timeframe preset "Custom"
    When User enters a start date "<START_DATE>" that is later than the end date "<END_DATE>"
    Then Verify an error message is displayed indicating the start date cannot be later than the end date
    Examples:
      | ADVERTISER         | START_DATE | END_DATE   |
      | TAMTESTING ACCOUNT | 2026-05-07 | 2026-05-01 |

  @regression
  Scenario Outline: Verify a saved non-default timeframe persists when the workspace is closed and reopened
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User edits the workspace name as "<WORKSPACE_NAME>"
    When User clicks the TimeFrame selector
    And User selects the timeframe preset "<TIMEFRAME>"
    And User saves the "Brand Explorer" workspace
    Then Verify the "Brand Explorer" Workspace is saved
    When User navigates back to the workspace list and reopens the saved Brand Explorer workspace
    Then Verify the Time Frame still shows "<TIMEFRAME>" after reopening the workspace
    And Verify the Day column shows <DAYS> dates in ascending order
    Examples:
      | ADVERTISER         | WORKSPACE_NAME     | TIMEFRAME    | DAYS |
      | TAMTESTING ACCOUNT | Automation_Persist | Last 30 Days | 30   |
