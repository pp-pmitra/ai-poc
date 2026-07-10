Feature: HCP Audience Workspace in Studio Application
  1. Verify the workspace creation for HCP Audience Expansion type in Studio application.
  2. Verify the source audience selection and expansion options.
  3. Verify the filters application in HCP Audience Expansion workspace.
  4. Verify the publishing of the expanded audience list to LIFE application.
  5. Verify NPI list's download and scheduling
  6. Verify Report download and scheduling

  Background:
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    When User navigates to Administrative section and go to Accounts Tab
    And User searches the account "PP engineering test" and checks Studio permissions
    And User clicks PulsePoint icon to navigate back to Life
    And User navigates to Studio application

  @todo
  Scenario Outline: Create and save an HCP Audience Expansion workspace with Expansion Audience
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Audience Expansion" workspace
    And User selects the advertiser "<ADVERTISER>"
    Then User selects Source Audience details as "<SOURCE_AUDIENCE>","<OPTIONS>"
    And User selects "<EXPANDED_AUDIENCE>"
    Then User verifies the expanded audience count
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And User saves the "HCP Audience Expansion" workspace
    Then Verify the workspace is visible in workspace management page
    Examples:
      | ADVERTISER | WORKSPACE_NAME | SOURCE_AUDIENCE  | OPTIONS      | EXPANDED_AUDIENCE             |
      | Abbvie     | HCP_Expansion  | Studio Workspace | PB_Test      | Expand with Care Team         |
      | Abbvie     | HCP_Expansion  | Studio Workspace | PB_Test      | Expand with Affiliation Graph |
      | Abbvie     | HCP_Expansion  | NPI List         | PB_Test_List | Expand with Care Team         |
      | Abbvie     | HCP_Expansion  | NPI List         | PB_Test_List | Expand with Affiliation Graph |

  @todo
  Scenario Outline: Create and save an HCP Audience Expansion workspace and publish the workspace
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Audience Expansion" workspace
    And User selects the advertiser "<ADVERTISER>"
    Then User selects Source Audience details as "<SOURCE_AUDIENCE>","<OPTIONS>"
    And User selects "<EXPANDED_AUDIENCE>"
    And User applies the following filters one by one and checks that NPI details are refined after each filter:
      | FilterName         | Option                                                                                                                  |
      | NPI Age            | Below 25, 25 to 35, 35 to 45, 45 to 55, 55 to 65, 65 or Above                                                           |
      | NPI Gender         | Female, Male, Unknown                                                                                                   |
      | Graduation Year    | 1900-2025                                                                                                               |
      | Net Worth          | Less than $50٫000, $100٫000 to $249٫999, $250٫000 to $499٫999, $500٫000 or above                                        |
      | Number of Patients | Below 5, 6 to 20, 21 to 50, 51 to 100, 101 to 200, 201 to 300, 301 to 400, 401 to 500, 501 to 1000, 1001 or above       |
      | Patient Age        | Below 25, 25 to 35, 35 to 45, 45 to 55, 55 to 65, 65 or Above                                                           |
      | Patient Gender     | Female, Male, Unknown                                                                                                   |
      | Years Practiced    | Below 5, 5 to 10, 10 to 15, 15 to 20, 20 to 25, 25 to 30, 30 to 35, 35 to 40, 40 to 45, 45 to 50, 50 or Above           |
      | Facility Name      | Visiting Nurse Service, Ahs Hospital Corp, Centrastate, Robert Wood Johnson University Hospital, Montclair Hospital Llc |
      | State              | New                                                                                                                     |
      | Profession         | Physician                                                                                                               |
      | Specialty          | Foot & Ankle Surgery, Internal Medicine                                                                                 |
      | NPI List Name      | Large file test                                                                                                         |
      | Medical School     | New York College                                                                                                        |
    And User clicks on Ok and closes the filter popup
    Then Verify that the applied filters are displayed correctly
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And User saves the "HCP Audience Expansion" workspace
    Then Verify the "HCP Audience Expansion" Workspace is saved
    And Download button is enabled to the user
    And User clicks on Publish NPI List
    And User selects publish "<LIST_TYPE>"
    And User select the "<PLATFORM>" to publish the list
    Then Verify list is published
    And User navigates to NPI Lists page
    And User searches the workspace in "<PLATFORM>" and selects it
    And User clicks on the published workspace
    Then User Verify the list is displayed in the LIFE
    Examples:
      | ADVERTISER | SOURCE_AUDIENCE  | OPTIONS      | EXPANDED_AUDIENCE                                           | LIST_TYPE | PLATFORM                      |
      | Abbvie     | Studio Workspace | PB_Test      | Basic, Exact Diagnosis, Extended, Professions, Specialities | Static    | Life, HCP365,Audience Manager |
      | Abbvie     | Studio Workspace | PB_Test      | Basic, Exact Diagnosis, Extended, Professions, Specialities | Live      | Life, HCP365,Audience Manager |
      | Abbvie     | Studio Workspace | PB_Test      | Expand with Affiliation Graph                               | Static    | Life, HCP365,Audience Manager |
      | Abbvie     | NPI List         | PB_Test_List | Basic, Exact Diagnosis, Extended, Professions, Specialities | Live      | Life, HCP365,Audience Manager |
      | Abbvie     | NPI List         | PB_Test_List | Expand with Affiliation Graph                               | Static    | Life, HCP365,Audience Manager |
      | Abbvie     | NPI List         | PB_Test_List | Expand with Affiliation Graph                               | Live      | Life, HCP365,Audience Manager |

  @todo
  Scenario Outline: Create and save Private/Public HCP Audience Expansion workspace and check workspace is visible in respective user accordingly
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Audience Expansion" workspace
    And User selects the advertiser "<ADVERTISER>"
    Then User selects Source Audience details as "<SOURCE_AUDIENCE>","<OPTIONS>"
    And User selects "<EXPANDED_AUDIENCE>"
    Then User selects Draft option as "<DRAFT>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And User saves the "HCP Audience Expansion" workspace
    Then Verify the "HCP Audience Expansion" Workspace is saved
    And Internal User is able to view "<WORKSPACE_NAME>" in workspace management page
    And "Internal User" logs out from the "Studio" application
    Given This scenario will be executed in the "Pre-release" environment as a "External User"
    And "Studio" application is logged in successfully with Account "<ACCOUNT_NAME>"
    When External user Searches the workspace name in studio application with "<DRAFT_OPTION>" draft option
    Then External user Verifies whether the workspace with "<DRAFT_OPTION>" is visible in workspace management page
    Examples:
      | ADVERTISER | SOURCE_AUDIENCE  | OPTIONS | EXPANDED_AUDIENCE             | DRAFT   | WORKSPACE_NAME |
      | Abbvie     | Studio Workspace | PB_Test | Expand with Care Team         | PRIVATE | HCP_Expansion  |
      | Abbvie     | Studio Workspace | PB_Test | Expand with Affiliation Graph | PUBLIC  | HCP_Expansion  |

  @todo
  Scenario Outline: Create Private HCP Audience Expansion workspace then save and publish the workspace then check the change in status
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Audience Expansion" workspace
    And User selects the advertiser "<ADVERTISER>"
    Then User selects Source Audience details as "<SOURCE_AUDIENCE>","<OPTIONS>"
    And User selects "<EXPANDED_AUDIENCE>"
    Then User verifies the expanded audience count
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And User saves the "HCP Audience Expansion" workspace
    Then Verify the "HCP Audience Expansion" Workspace is saved
    And Download button is enabled to the user
    And User clicks on Publish NPI List
    And User selects publish "<LIST_TYPE>"
    And User select the "<PLATFORM>" to publish the list
    Then Verify the workspace is visible in workspace management page
    And Status is updated to Public
    Examples:
      | ADVERTISER | SOURCE_AUDIENCE  | OPTIONS      | EXPANDED_AUDIENCE                                           | LIST_TYPE | PLATFORM                      |
      | Abbvie     | Studio Workspace | PB_Test      | Basic, Exact Diagnosis, Extended, Professions, Specialities | Static    | Life, HCP365,Audience Manager |
      | Abbvie     | Studio Workspace | PB_Test      | Basic, Exact Diagnosis, Extended, Professions, Specialities | Live      | Life, HCP365,Audience Manager |
      | Abbvie     | Studio Workspace | PB_Test      | Expand with Affiliation Graph                               | Static    | Life, HCP365,Audience Manager |
      | Abbvie     | NPI List         | PB_Test_List | Basic, Exact Diagnosis, Extended, Professions, Specialities | Live      | Life, HCP365,Audience Manager |
      | Abbvie     | NPI List         | PB_Test_List | Expand with Affiliation Graph                               | Static    | Life, HCP365,Audience Manager |
      | Abbvie     | NPI List         | PB_Test_List | Expand with Affiliation Graph                               | Live      | Life, HCP365,Audience Manager |

  @todo
  Scenario Outline: Create and save an HCP Audience Expansion workspace and Download NPI's
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Audience Expansion" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User selects Source Audience details as "<SOURCE_AUDIENCE>","<OPTIONS>"
    And User selects "<EXPANDED_AUDIENCE>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And User saves the "HCP Audience Expansion" workspace
    Then Verify the "HCP Audience Expansion" Workspace is saved
    And Download button is enabled to the user
    And User clicks Download NPI option
    And User selects download format as "<FORMAT>" and clicks Download button
    Examples:
      | ADVERTISER | SOURCE_AUDIENCE  | OPTIONS | EXPANDED_AUDIENCE             | FORMAT | WORKSPACE_NAME |
      | Abbvie     | Studio Workspace | PB_Test | Expand with Care Team         | CSV    | HCP_Expansion  |
      | Abbvie     | Studio Workspace | PB_Test | Expand with Affiliation Graph | EXCEL  | HCP_Expansion  |

  @todo
  Scenario Outline: Create and save an HCP Audience Expansion workspace and Schedule NPI's
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Audience Expansion" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User selects Source Audience details as "<SOURCE_AUDIENCE>","<OPTIONS>"
    And User selects "<EXPANDED_AUDIENCE>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And User saves the "HCP Audience Expansion" workspace
    Then Verify the "HCP Audience Expansion" Workspace is saved
    And Download button is enabled to the user
    And User clicks Schedule NPI button
    And User enters data and clicks Save button
    Examples:
      | ADVERTISER | SOURCE_AUDIENCE  | OPTIONS | EXPANDED_AUDIENCE             | WORKSPACE_NAME |
      | Abbvie     | Studio Workspace | PB_Test | Expand with Care Team         | HCP_Expansion  |
      | Abbvie     | Studio Workspace | PB_Test | Expand with Affiliation Graph | HCP_Expansion  |

  @todo
  Scenario Outline: Create and save an HCP Audience Expansion workspace and Download Report
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Audience Expansion" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User selects Source Audience details as "<SOURCE_AUDIENCE>","<OPTIONS>"
    And User selects "<EXPANDED_AUDIENCE>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And User saves the "HCP Audience Expansion" workspace
    Then Verify the "HCP Audience Expansion" Workspace is saved
    And Report button is enabled to the user
    And User clicks on Download Report
    And User enters the Report Name
    And User selects download format as "<FORMAT>" and clicks Download button
    Examples:
      | ADVERTISER | SOURCE_AUDIENCE  | OPTIONS | EXPANDED_AUDIENCE             | FORMAT | WORKSPACE_NAME |
      | Abbvie     | Studio Workspace | PB_Test | Expand with Care Team         | CSV    | HCP_Expansion  |
      | Abbvie     | Studio Workspace | PB_Test | Expand with Affiliation Graph | EXCEL  | HCP_Expansion  |

  @todo
  Scenario Outline: Create and save an HCP Audience Expansion workspace and Schedule Report
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Audience Expansion" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User selects Source Audience details as "<SOURCE_AUDIENCE>","<OPTIONS>"
    And User selects "<EXPANDED_AUDIENCE>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And User saves the "HCP Audience Expansion" workspace
    Then Verify the "HCP Audience Expansion" Workspace is saved
    And Report button is enabled to the user
    And User clicks Schedule Report button
    And User enters data and clicks Save button
    Examples:
      | ADVERTISER | SOURCE_AUDIENCE  | OPTIONS | EXPANDED_AUDIENCE             | WORKSPACE_NAME |
      | Abbvie     | Studio Workspace | PB_Test | Expand with Care Team         | HCP_Expansion  |
      | Abbvie     | Studio Workspace | PB_Test | Expand with Affiliation Graph | HCP_Expansion  |
