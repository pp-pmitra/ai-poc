Feature: Brand Explorer Workspace creation in Studio

  Background:
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
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
    And User edits the "Brand Explorer" workspace name as "<WORKSPACE_NAME>"
    Then Verify that advertiser field is disabled and displayed in "rgba(34, 34, 34, 0.55)" after saving the workspace
    Then Verify Dimension "Day" and Metric "Identified NPIs" are selected by default in the workspace
    Then Verify Time Frame is selected as "Last 7 Days" by default in the workspace
    And User saves the "Brand Explorer" workspace
    Then Verify the "Brand Explorer" Workspace is saved
    And Navigate to workspace dashboard
    And User selects the workspace type "Brand Explorer"
    And User searches the workspace created to perform Actions from More menu
    And User selects the "Delete" option by clicking More Actions menu
    And Verify user is able to delete the workspace

    Examples:
      | ADVERTISER         | WORKSPACE_NAME |
      | TAMTESTING ACCOUNT | Brand_Explorer |

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
      | TAMTESTING ACCOUNT | Last 14 Days |   14 |
      | TAMTESTING ACCOUNT | Last 30 Days |   30 |
      | TAMTESTING ACCOUNT | Yesterday    |    1 |

  @regression
  Scenario Outline: Verify Custom date range picker and inclusive start "<START_DATE>" and end "<END_DATE>" dates in the returned dataset
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
    And User edits the "Brand Explorer" workspace name as "<WORKSPACE_NAME>"
    When User clicks the TimeFrame selector
    And User selects the timeframe preset "<TIMEFRAME>"
    And User saves the "Brand Explorer" workspace
    Then Verify the "Brand Explorer" Workspace is saved
    When User navigates back to the workspace list and reopens the saved Brand Explorer workspace
    Then Verify the Time Frame still shows "<TIMEFRAME>" after reopening the workspace
    And Verify the Day column shows <DAYS> dates in ascending order
    And Navigate to workspace dashboard
    And User searches the workspace created to perform Actions from More menu
    And User selects the "Delete" option by clicking More Actions menu
    And Verify user is able to delete the workspace

    Examples:
      | ADVERTISER         | WORKSPACE_NAME | TIMEFRAME    | DAYS |
      | TAMTESTING ACCOUNT | Brand_Explorer | Last 30 Days |   30 |

  @regression
  Scenario Outline: Verify saved non-default dimension and metric persist when the workspace is closed and reopened
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User edits the "Brand Explorer" workspace name as "<WORKSPACE_NAME>"
    And User removes the default dimensions and metric
    And User selects "<DIMENSION>" from "<DIM_CATEGORY>" component panel
    And User selects "<METRIC>" from "<METRIC_CATEGORY>" component panel
    And User saves the "Brand Explorer" workspace
    Then Verify the "Brand Explorer" Workspace is saved
    When User navigates back to the workspace list and reopens the saved Brand Explorer workspace
    Then Verify "<DIMENSION>" and "<METRIC>" persist as table columns after reopening
    And Navigate to workspace dashboard
    And User searches the workspace created to perform Actions from More menu
    And User selects the "Delete" option by clicking More Actions menu
    And Verify user is able to delete the workspace

    Examples:
      | ADVERTISER         | WORKSPACE_NAME | DIMENSION | DIM_CATEGORY | METRIC           | METRIC_CATEGORY |
      | TAMTESTING ACCOUNT | Brand_Explorer | Month     | Time Frame   | HCP Active Users | HCP Events      |

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

  @regression
  Scenario Outline: Verify Brand Explorer segments can be selected and removed
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User removes the default dimensions and metric
    Then Verify each "segment" under below categories can be selected and removed
      | NPI List Name                 |
      | NPI List Name - separate rows |

    Examples:
      | ADVERTISER         |
      | TAMTESTING ACCOUNT |

  @regression
  Scenario Outline: Verify Brand Explorer chart can be hidden and shown
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    Then Verify the Brand Explorer chart is visible
    When User clicks the "Hide Chart" chart toggle
    Then Verify the Brand Explorer chart is hidden
    And Verify the Brand Explorer table is visible
    When User clicks the "Show Chart" chart toggle
    Then Verify the Brand Explorer chart is visible

    Examples:
      | ADVERTISER         |
      | TAMTESTING ACCOUNT |

  @regression
  Scenario Outline: Verify a Brand Explorer table column can be removed from the table header
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User selects "<FIELD>" from "<CATEGORY>" component panel
    Then Verify "<FIELD>" is visible as a table column
    When User removes "<FIELD>" from the table header
    Then Verify "<FIELD>" is not visible as a table column

    Examples:
      | ADVERTISER         | CATEGORY                 | FIELD      |
      | TAMTESTING ACCOUNT | Healthcare Professionals | Profession |

  @regression
  Scenario Outline: Verify a saved filter persists when the workspace is closed and reopened
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User edits the "Brand Explorer" workspace name as "<WORKSPACE_NAME>"
    And User clicks on the Filters tab
    And User adds a filter on "<FIELD>" from "<CATEGORY>" category with value "<VALUE>"
    Then Verify the filter on "<FIELD>" shows value "<VALUE>"
    And User saves the "Brand Explorer" workspace
    Then Verify the "Brand Explorer" Workspace is saved
    When User navigates back to the workspace list and reopens the saved Brand Explorer workspace
    And User clicks on the Filters tab
    Then Verify the filter on "<FIELD>" shows value "<VALUE>"
    And Navigate to workspace dashboard
    And User searches the workspace created to perform Actions from More menu
    And User selects the "Delete" option by clicking More Actions menu
    And Verify user is able to delete the workspace

    Examples:
      | ADVERTISER         | WORKSPACE_NAME | CATEGORY                 | FIELD      | VALUE     |
      | TAMTESTING ACCOUNT | Brand_Explorer | Healthcare Professionals | Profession | Physician |

  @regression
  Scenario Outline: Verify an applied filter is reflected immediately and correctly narrows the dataset in the table
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

  @regression
  Scenario Outline: Verify user can rename, duplicate, and delete a Brand Explorer workspace
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User edits the "Brand Explorer" workspace name as "<WORKSPACE_NAME>"
    And User clicks the TimeFrame selector
    And User selects the timeframe preset "<TIMEFRAME>"
    And User saves the "Brand Explorer" workspace
    Then Verify the "Brand Explorer" Workspace is saved
    And Navigate to workspace dashboard
    And User selects the workspace type "Brand Explorer"
    And User clicks on the More Actions menu for the saved workspace
    And User selects the "Rename" option by clicking More Actions menu
    And Verify user is able to rename the "Brand Explorer" workspace as "<NEW_WORKSPACE_NAME>"
    And User is able to search the workspace after performing operation - "Rename"
    And User searches the workspace created to perform Actions from More menu
    And User selects the "Duplicate" option by clicking More Actions menu
    And Verify user is able to duplicate the "Brand Explorer" workspace
    And User is able to search the workspace after performing operation - "Duplicate"
    And User searches the workspace created to perform Actions from More menu
    And User selects the "Delete" option by clicking More Actions menu
    And Verify user is able to delete the workspace

    Examples:
      | ADVERTISER         | WORKSPACE_NAME     | NEW_WORKSPACE_NAME  | TIMEFRAME    |
      | TAMTESTING ACCOUNT | Automation_Persist | New_Brand_Explorer_ | Last 30 Days |

  @regression
  Scenario Outline: Verify the chart shows an empty state when only a dimension or only a metric remains
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User removes the default "<COMPONENT>" only
    Then Verify the chart shows the empty state message
    And Verify "<REMAINING_COLUMN>" is visible as a table column

    Examples:
      | ADVERTISER         | COMPONENT | REMAINING_COLUMN |
      | TAMTESTING ACCOUNT | dimension | Identified NPIs  |
      | TAMTESTING ACCOUNT | metric    | Day              |

  @regression
  Scenario Outline: Verify the chart shows an empty state when both the dimension and metric are removed
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User removes the default dimensions and metric
    Then Verify the chart shows the empty state message

    Examples:
      | ADVERTISER         |
      | TAMTESTING ACCOUNT |

  @regression
  Scenario Outline: Verify Clear All resets Brand Explorer components and chart recovers after defaults are reselected
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    When User clicks Clear All in the Brand Explorer component panel
    Then Verify no Brand Explorer components are selected
    And Verify the chart shows the empty state message
    When User selects "Day" from "Time Frame" component panel
    And User selects "Identified NPIs" from "NPI Events" component panel
    Then Verify Dimension "Day" and Metric "Identified NPIs" are selected by default in the workspace
    And Verify the Brand Explorer chart is visible
    And Verify the Brand Explorer table is visible

    Examples:
      | ADVERTISER         |
      | TAMTESTING ACCOUNT |

  @regression
  Scenario Outline: Verify Brand Explorer chart refreshes after replacing the default dimension and metric
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User deselects "Day" from the table
    And User deselects "Identified NPIs" from the table
    When User selects "<DIMENSION>" from "<DIMENSION_CATEGORY>" component panel
    And User selects "<METRIC>" from "<METRIC_CATEGORY>" component panel
    Then Verify "<DIMENSION>" is visible as a table column
    And Verify "<METRIC>" is visible as a table column
    And Verify the Brand Explorer chart is visible

    Examples:
      | ADVERTISER         | DIMENSION | DIMENSION_CATEGORY | METRIC           | METRIC_CATEGORY |
      | TAMTESTING ACCOUNT | Month     | Time Frame         | HCP Active Users | HCP Events      |

  @regression
  Scenario Outline: Verify categorical filter operators that use a value list can be applied
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User clicks on the Filters tab
    And User adds a filter on "<FIELD>" from "<CATEGORY>" category with operator "<OPERATOR>" and value "<VALUE>"
    Then Verify the filter on "<FIELD>" shows value "<VALUE>"
    And User clicks on the Components tab
    And User selects "<FIELD>" from "<CATEGORY>" component panel
    Then Verify "<FIELD>" is visible as a table column
    And Verify the table column "<FIELD>" is filtered by operator "<OPERATOR>" and value "<VALUE>"

    Examples:
      | ADVERTISER         | CATEGORY                 | FIELD      | OPERATOR | VALUE     |
      | TAMTESTING ACCOUNT | Healthcare Professionals | Profession | is       | Physician |

  @regression
  Scenario Outline: Verify filter operators that use a free-text value can be applied
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User clicks on the Filters tab
    And User adds a typed filter on "<FIELD>" from "<CATEGORY>" category with operator "<OPERATOR>" and value "<VALUE>"
    Then Verify the typed filter on "<FIELD>" with operator "<OPERATOR>" shows value "<VALUE>"
    And User clicks on the Components tab
    And User selects "<FIELD>" from "<CATEGORY>" component panel
    Then Verify "<FIELD>" is visible as a table column
    And Verify the table column "<FIELD>" is filtered by operator "<OPERATOR>" and value "<VALUE>"

    Examples:
      | ADVERTISER         | CATEGORY                 | FIELD      | OPERATOR           | VALUE |
      | TAMTESTING ACCOUNT | Healthcare Professionals | Profession | contains           | Phys  |
      | TAMTESTING ACCOUNT | Healthcare Professionals | Profession | starts with        | Phys  |
      | TAMTESTING ACCOUNT | Healthcare Professionals | Profession | ends with          | ian   |
      | TAMTESTING ACCOUNT | Healthcare Professionals | Profession | doesn't contain    | Nurse |
      | TAMTESTING ACCOUNT | Healthcare Professionals | Profession | doesn't start with | Nurse |
      | TAMTESTING ACCOUNT | Healthcare Professionals | Profession | doesn't end with   | Nurse |

  @regression
  Scenario Outline: Verify the is between numeric operator applies an inclusive range filter
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User clicks on the Filters tab
    And User adds a range filter on "<FIELD>" from "<CATEGORY>" category with operator "is between" and values "<FROM>" and "<TO>"
    Then Verify the filter on "<FIELD>" shows operator "is between"
    And Verify "<FIELD>" is visible as a table column
    And Verify the table column "<FIELD>" is filtered between values "<FROM>" and "<TO>"

    Examples:
      | ADVERTISER         | CATEGORY   | FIELD           | FROM | TO |
      | TAMTESTING ACCOUNT | NPI Events | Identified NPIs |    0 |  3 |

  @regression
  Scenario Outline: Verify numeric filter operators that use a typed value can be applied
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Brand Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User clicks on the Filters tab
    And User adds a typed filter on "<FIELD>" from "<CATEGORY>" category with operator "<OPERATOR>" and value "<VALUE>"
    Then Verify the typed filter on "<FIELD>" with operator "<OPERATOR>" shows value "<VALUE>"
    And Verify "<FIELD>" is visible as a table column
    And Verify the table column "<FIELD>" is filtered by operator "<OPERATOR>" and value "<VALUE>"

    Examples:
      | ADVERTISER         | CATEGORY   | FIELD           | OPERATOR | VALUE |
      | TAMTESTING ACCOUNT | NPI Events | Identified NPIs | =        |     1 |
      | TAMTESTING ACCOUNT | NPI Events | Identified NPIs | >        |     0 |
      | TAMTESTING ACCOUNT | NPI Events | Identified NPIs | >=       |     1 |
      | TAMTESTING ACCOUNT | NPI Events | Identified NPIs | <        |     2 |
      | TAMTESTING ACCOUNT | NPI Events | Identified NPIs | <=       |     1 |
      | TAMTESTING ACCOUNT | NPI Events | Identified NPIs | !=       |     1 |
