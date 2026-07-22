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
      | ADVERTISER         |
      | TAMTESTING ACCOUNT |

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

  @regression
  Scenario Outline: Verify saved non-default dimension and metric persist when the workspace is closed and reopened
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User edits the workspace name as "<WORKSPACE_NAME>"
    And User removes the default dimensions and metric
    And User selects "<DIMENSION>" from "<DIM_CATEGORY>" component panel
    And User selects "<METRIC>" from "<METRIC_CATEGORY>" component panel
    And User saves the "Brand Explorer" workspace
    Then Verify the "Brand Explorer" Workspace is saved
    When User navigates back to the workspace list and reopens the saved Brand Explorer workspace
    Then Verify "<DIMENSION>" and "<METRIC>" persist as table columns after reopening
    Examples:
      | ADVERTISER         | WORKSPACE_NAME       | DIMENSION | DIM_CATEGORY | METRIC           | METRIC_CATEGORY |
      | TAMTESTING ACCOUNT | Automation_Persist   | Month     | Time Frame   | HCP Active Users | HCP Events      |

  @regression
  Scenario Outline: Verify a Brand Explorer dimension can be selected and removed
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    Then Verify "dimension" tab with all types under below categories
      | Campaign Details         |
      | Collection Details       |
      | Custom Parameters        |
      | DS Values                |
      | Healthcare Professionals |
      | Technographic            |
      | Time Frame               |
      | UTM Values               |
      | Visitation               |
    And User removes the default dimensions and metric
    Then Verify each "dimension" under below categories can be selected and removed
      | Campaign Details         | Ad Type, Campaign ID, Campaign Name, Click Text, Creative, Creative Type, Form Text, Keyword, Line Item ID, Line Item Name, Search Engine, Tactic ID, Tactic Name                                 |
      | Collection Details       | Account ID, Account Name, Advertiser ID, Advertiser Name, Channel, Channel ID, Collection ID, Collection Name                                                                                     |
      | Custom Parameters        | Param 1, Param 2, Param 3, Param 4, Param 5                                                                                                                                                       |
      | DS Values                | DS Account Type, DS Ad Group, DS Ad Group ID, DS Campaign, DS Campaign ID, DS Keyword ID, DS Search Term                                                                                          |
      | Healthcare Professionals | First Name, HCP Flag, Hospital Affiliation, Last Name, NPI, NPI Flag, Practice Affiliation, Primary Specialty, Profession, Secondary Specialty, Specialties, Specialty (separate rows), User Type |
      | Technographic            | Device Type, Operating System                                                                                                                                                                     |
      | Time Frame               | Day, Day of Week, Hour, Month, Time Range, Timestamp, Week, Weekday or Weekend, Year                                                                                                              |
      | UTM Values               | Third Party CID, UTM Campaign, UTM Content, UTM Medium, UTM Source, UTM Term                                                                                                                      |
      | Visitation               | Attributed Source, File Name, From Domain (domain referrer), From URL (URL Referrer), Page Domain, Page URL, Page URL (Denormalized), Social Provider, Source, Source Type, Video Title           |
    Examples:
      | ADVERTISER         |
      | TAMTESTING ACCOUNT |

  @regression
  Scenario Outline: Verify a Brand Explorer metric can be selected and removed
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    Then Verify "metric" tab with all types under below categories
      | HCP Events   |
      | NPI Events   |
      | Time Spent   |
      | Total Events |
    And User removes the default dimensions and metric
    Then Verify each "metric" under below categories can be selected and removed
      | HCP Events   | Avg. Video Progress, HCP Active Users, HCP Avg. Engagement Time (sec), HCP Avg. Engagement Time per Session (sec), HCP Email Clicks, HCP Email Opens, HCP Engaged Sessions per User, HCP Events, HCP File Downloads, HCP First Visits, HCP Form Starts, HCP Form Submissions, HCP Media Clicks, HCP Media CTR, HCP Media Impressions, HCP Pageviews, HCP Returning Visits, HCP Search Clicks, HCP Social Clicks, HCP Social Impressions, HCP Unique Sessions, HCP Video Completes, HCP Video Starts, HCP Visits                                       |
      | NPI Events   | Avg. Video Progress, Identified NPIs, NPI Active Users, NPI Avg. Engagement Time (sec), NPI Avg. Engagement Time per Session (sec), NPI Email Clicks, NPI Email Opens, NPI Engaged Sessions per User, NPI Events, NPI File Downloads, NPI First Visits, NPI Form Starts, NPI Form Submissions, NPI Media Clicks, NPI Media CTR, NPI Media Frequency, NPI Media Impressions, NPI Pageviews, NPI Returning Visits, NPI Search Clicks, NPI Social Clicks, NPI Social Impressions, NPI Unique Sessions, NPI Video Completes, NPI Video Starts, NPI Visits |
      | Time Spent   | Time Spent (days), Time Spent (hours), Time Spent (minutes), Time Spent (seconds)                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
      | Total Events | Avg. Video Progress, Total Active Users, Total Avg. Engagement Time (sec), Total Avg. Engagement Time per Session (sec), Total Email Clicks, Total Email Opens, Total Engaged Sessions per User, Total Events, Total File Downloads, Total First Visits, Total Form Starts, Total Form Submissions, Total Media Clicks, Total Media CTR, Total Media Impressions, Total Pageviews, Total Returning Visits, Total Search Clicks, Total Social Clicks, Total Social Impressions, Total Video Completes, Total Video Starts, Total Visits                |
    Examples:
      | ADVERTISER         |
      | TAMTESTING ACCOUNT |

  # Parked: flaky/not yet stable - editing workspace name does not show toast message for brand explorer workspace while other workspace types do show the toast message
  # @regression
  # Scenario Outline: Verify a saved filter persists when the workspace is closed and reopened
  #   When User clicks on Create New Workspace
  #   Then User sees the types of workspaces they have permissions for
  #   And User clicks on "Brand Explorer" workspace
  #   And User selects the advertiser "<ADVERTISER>"
  #   And User edits the workspace name as "<WORKSPACE_NAME>"
  #   And User clicks on the Filters tab
  #   And User adds a filter on "<FIELD>" from "<CATEGORY>" category with value "<VALUE>"
  #   Then Verify the filter on "<FIELD>" shows value "<VALUE>"
  #   And User saves the "Brand Explorer" workspace
  #   Then Verify the "Brand Explorer" Workspace is saved
  #   When User navigates back to the workspace list and reopens the saved Brand Explorer workspace
  #   And User clicks on the Filters tab
  #   Then Verify the filter on "<FIELD>" shows value "<VALUE>"
  #   Examples:
  #     | ADVERTISER         | WORKSPACE_NAME     | CATEGORY                 | FIELD      | VALUE     |
  #     | TAMTESTING ACCOUNT | Automation_Persist | Healthcare Professionals | Profession | Physician |

  @regression
  Scenario Outline: Verify an applied filter is reflected immediately without saving and correctly narrows its own dimension/metric column
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User clicks on the Filters tab
    And User adds a filter on "<FIELD>" from "<CATEGORY>" category with value "<VALUE>"
    Then Verify the filter on "<FIELD>" shows value "<VALUE>"
    And User clicks on the Components tab
    And User selects "<FIELD>" from "<CATEGORY>" component panel
    Then Verify "<FIELD>" is visible as a table column
    And Verify the table column "<FIELD>" only shows rows with value "<VALUE>"
    Examples:
      | ADVERTISER         | CATEGORY                 | FIELD      | VALUE     |
      | TAMTESTING ACCOUNT | Healthcare Professionals | Profession | Physician |

