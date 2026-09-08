Feature: Medscape Soft Match Workspace in Studio Application
  1. Verify the workspace creation for Medscape Soft Match in Studio Application
  2. Verify the saved and published workspace, Explore tab for the valid data
  3. Verify workspace displayed on Dashboard management page with valid data

  Background:
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    When User navigates to Administrative section
    And User navigates to Accounts Tab
    And User searches the account "PP engineering test" and checks Studio permissions
    And User clicks PulsePoint icon to navigate back to Life
    And User navigates to Studio application

  @todo
  Scenario: Verify that all the list's available in Source NPI List options are Medscape List
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Medscape Soft Match" workspace
    And User selects the advertiser "<ADVERTISER>"
    And Verify the Workspace creation page is displayed
    And Verify all the list's available in Source NPI List options are Medscape List

  @todo
  Scenario Outline: Create and save Medscape Soft Match workspace to verify error msg for mandatory fields
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Medscape Soft Match" workspace
    And User selects the advertiser "<ADVERTISER>"
    And Verify the Workspace creation page is displayed
    Then User clicks the Save button without selecting values in any fields
    And Verify the in-line error messages are displayed for mandatory fields i.e Source NPI List, Business, Business Vertical, Product
    Then User selects values in each fields as "<SOURCE_NPI_LIST>", "<BUSINESS>", "<BUSINESS_VERTICAL>", "<PRODUCT>", "<PHARMA>" and "<BRAND>"
    And Verify user is able to select multiple brands options
    And Verify user is able to deselect the multiple selected brand options
    And Verify the in-line error messages is displayed for Brand field
    Examples:
      | ADVERTISER | SOURCE_NPI_LIST | BUSINESS     | BUSINESS_VERTICAL | PRODUCT     | PHARMA               | BRAND                                                     |
      | Medscape   | Medscape List_1 | Medscape (7) | Sponsorship (6)   | MSITE_TOPIC | Bristol-Myers Squibb | GlaxoSmithKline_Global,GSK Anoro Sample Email Suppression |

  @todo
  Scenario Outline: Create, save and delete Medscape Soft Match workspace without Deliverable ID
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Medscape Soft Match" workspace
    And User selects the advertiser "<ADVERTISER>"
    And Verify the Workspace creation page is displayed
    Then User selects values in each fields as "<SOURCE_NPI_LIST>", "<BUSINESS>", "<BUSINESS_VERTICAL>", "<PRODUCT>", "<PHARMA>", "<BRAND>" and "<STATE_EXCLUSION>"
    And User edits the "Medscape Soft Match" workspace name as "<WORKSPACE_NAME>"
    And User saves the "Medscape Soft Match" workspace
    And Verify the "Medscape Soft Match" Workspace is saved
    And Verify the Publish button is disbaled
    And Verify system Displays details on "Explore" tab
    Then Verify the workspace is visible in workspace management page
    And Workspace status is updated and Workspace Definition is displayed as per selected fields
    And User selects the "Delete" option by clicking More Actions menu
    And Verify user is able to delete the workspace
    Examples:
      | ADVERTISER | WORKSPACE_NAME      | SOURCE_NPI_LIST | BUSINESS     | BUSINESS_VERTICAL | PRODUCT           | PHARMA                 | BRAND                  | STATE_EXCLUSION |
      | Medscape   | Medscape_Soft_Match | Medscape List_1 | Medscape (7) | Sponsorship (6)   | MSITE_TOPIC       | Bristol-Myers Squibb   | BMS Email Suppression  |                 |
      | Medscape   | Medscape_Soft_Match | Medscape List_2 | MD/alert (3) | Sponsorship (19)  | MD/Alert e-Alerts | GlaxoSmithKline_Global | GlaxoSmithKline_Global | Colorado        |
      | Medscape   | Medscape_Soft_Match | Medscape List_3 | MD/alert (3) | Sponsorship (19)  | MD/Alert e-Alerts |                        |                        |                 |

  @todo
  Scenario Outline: Create, save, rename, duplicate and delete Medscape Soft Match workspace
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Medscape Soft Match" workspace
    And User selects the advertiser "<ADVERTISER>"
    And Verify the Workspace creation page is displayed
    Then User selects values in each fields as "<SOURCE_NPI_LIST>", "<BUSINESS>", "<BUSINESS_VERTICAL>", "<PRODUCT>", "<PHARMA>", "<BRAND>" and "<STATE_EXCLUSION>"
    And User edits the "Medscape Soft Match" workspace name as "<WORKSPACE_NAME>"
    And User saves the "Medscape Soft Match" workspace
    And Verify the "Medscape Soft Match" Workspace is saved
    Then Verify the workspace is visible in workspace management page
    And Workspace status is updated and Workspace Definition is displayed as per selected fields
    And User selects the "Rename" option by clicking More Actions menu
    And Verify user is able to rename the "HCP Explorer" workspace as "<NEW_WORKSPACE_NAME>"
    And User is able to search the workspace after performing operation - "Rename"
    And User searches the workspace created to perform Actions from More menu
    And User selects the "Duplicate" option by clicking More Actions menu
    And Verify user is able to duplicate the "HCP Explorer" workspace
    And User is able to search the workspace after performing operation - "Duplicate"
    And User searches the workspace created to perform Actions from More menu
    And User selects the "Delete" option by clicking More Actions menu
    And Verify user is able to delete the workspace
    Examples:
      | ADVERTISER | WORKSPACE_NAME      | NEW_WORKSPACE_NAME      | SOURCE_NPI_LIST | BUSINESS     | BUSINESS_VERTICAL | PRODUCT     | PHARMA               | BRAND                 | STATE_EXCLUSION |
      | Medscape   | Medscape_Soft_Match | New_Medscape_Soft_Match | Medscape List_1 | Medscape (7) | Sponsorship (6)   | MSITE_TOPIC | Bristol-Myers Squibb | BMS Email Suppression |                 |

  @todo
  Scenario Outline: Create, save and delete Medscape Soft Match workspace and publish the workspace with unique SF Deliverable ID and verify the Widget
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Medscape Soft Match" workspace
    And User selects the advertiser "<ADVERTISER>"
    And Verify the Workspace creation page is displayed
    Then User selects values in each fields as "<SOURCE_NPI_LIST>","<DELIVERABLE_ID>", "<BUSINESS>", "<BUSINESS_VERTICAL>", "<PRODUCT>", "<PHARMA>", "<BRAND>" and "<STATE_EXCLUSION>"
    And Verify for valid Deliverable ID, suggestion is displayed below Pharma and brand
    And User edits the "Medscape Soft Match" workspace name as "<WORKSPACE_NAME>"
    And User saves the "Medscape Soft Match" workspace
    And Verify the "Medscape Soft Match" Workspace is saved
    Then User clicks on Publish or download NPI List button
    And User selects Push to Artemis option
    And User clicks on Push to Artemis button
    And Verify "Medscape Soft Match" workspace is published successfully only with unique SF Deliverable ID
    And Verify workspace is in Read only mode once published successfully
    And Verify Match type Distribution widget is visible on explore tab
    Then Verify the workspace is visible in workspace management page
    And Workspace status is updated and Workspace Definition is displayed as per selected fields
    And User selects the "Delete" option by clicking More Actions menu
    And Verify user is able to delete the workspace
    Examples:
      | ADVERTISER | WORKSPACE_NAME      | SOURCE_NPI_LIST | DELIVERABLE_ID | BUSINESS     | BUSINESS_VERTICAL | PRODUCT           | PHARMA                 | BRAND                  | STATE_EXCLUSION |
      | Medscape   | Medscape_Soft_Match | Medscape List_1 | 338482.141     | Medscape (7) | Sponsorship (6)   | MSITE_TOPIC       | Bristol-Myers Squibb   | BMS Email Suppression  |                 |
      | Medscape   | Medscape_Soft_Match | Medscape List_2 | 338482.141     | MD/alert (3) | Sponsorship (19)  | MD/Alert e-Alerts | GlaxoSmithKline_Global | GlaxoSmithKline_Global | Colorado        |
      | Medscape   | Medscape_Soft_Match | Medscape List_3 | 338482.141     | MD/alert (3) | Sponsorship (19)  | MD/Alert e-Alerts |                        |                        |                 |

  @todo
  Scenario Outline: Create Medscape Soft Match workspace and download the reach analysis report and delete the workspace
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "Medscape Soft Match" workspace
    And User selects the advertiser "<ADVERTISER>"
    And Verify the Workspace creation page is displayed
    Then User selects values in each fields as "<SOURCE_NPI_LIST>","<DELIVERABLE_ID>", "<BUSINESS>", "<BUSINESS_VERTICAL>", "<PRODUCT>", "<PHARMA>", "<BRAND>" and "<STATE_EXCLUSION>"
    And User edits the "Medscape Soft Match" workspace name as "<WORKSPACE_NAME>"
    And User saves the "Medscape Soft Match" workspace
    And Verify the "Medscape Soft Match" Workspace is saved
    And Verify the values displayed on Explore tab
    Then User clicks on Publish or download NPI List button
    And User selects Push to Artemis option
    And User selects Push to Artemis button
    And Verify "Medscape Soft Match" workspace is published
    And Verify analysis report is downloaded on clicking "Download Reach Analysis"
    Then Verify the workspace is visible in workspace management page
    And Workspace status is updated and Workspace Definition is displayed as per selected fields
    And User selects the "Delete" option by clicking More Actions menu
    And Verify user is able to delete the workspace

    Examples:
      | ADVERTISER | WORKSPACE_NAME      | SOURCE_NPI_LIST | DELIVERABLE_ID | BUSINESS     | BUSINESS_VERTICAL | PRODUCT           | PHARMA                 | BRAND                  | STATE_EXCLUSION |
      | Medscape   | Medscape_Soft_Match | Medscape List_1 | 338482.141     | Medscape (7) | Sponsorship (6)   | MSITE_TOPIC       |                        |                        |                 |
      | Medscape   | Medscape_Soft_Match | Medscape List_1 | 338482.141     | Medscape (7) | Sponsorship (6)   | MSITE_TOPIC       |                        |                        | Colorado        |
      | Medscape   | Medscape_Soft_Match | Medscape List_1 | 338482.141     | Medscape (7) | Sponsorship (6)   | MSITE_TOPIC       |                        |                        | Colorado        |
      | Medscape   | Medscape_Soft_Match | Medscape List_1 | 338482.141     | Medscape (7) | Sponsorship (6)   | MSITE_TOPIC       | Bristol-Myers Squibb   | BMS Email Suppression  | Colorado        |
      | Medscape   | Medscape_Soft_Match | Medscape List_3 | 338482.141     | MD/alert (3) | Sponsorship (19)  | MD/Alert e-Alerts |                        |                        |                 |
      | Medscape   | Medscape_Soft_Match | Medscape List_2 | 338482.141     | MD/alert (3) | Sponsorship (19)  | MD/Alert e-Alerts |                        |                        |                 |
      | Medscape   | Medscape_Soft_Match | Medscape List_2 | 338482.141     | MD/alert (3) | Sponsorship (19)  | MD/Alert e-Alerts | GlaxoSmithKline_Global | GlaxoSmithKline_Global |                 |

  @todo
  Scenario: Select Medscape Soft Match workspace and delete the workspace
    When User selects the workspace type "Medscape Soft Match"
    Then User sees only the "Medscape Soft Match" workspace
    And User selects the "Delete" option by clicking More Actions menu
    And Verify user is able to delete the workspace
