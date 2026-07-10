Feature: Create and Publish HCP Explorer Workspace in Studio and Verify in LIFE
  1. Creation of Workspace in Studio
  2. Publish the workspace as Studio List (Static List or Live List)
  3. Verify the published Studio list in LIFE

  Background:
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    When User navigates to Administrative section
    And User navigates to Accounts Tab
    And User searches the account "PP engineering test" and checks Studio permissions
    And User clicks PulsePoint icon to navigate back to Life
    And User navigates to Studio application

  @e2e
  Scenario Outline: Create and save HCP Explorer workspace with specific primary filters
    #1
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And Verify that advertiser field is disabled and displayed in "rgba(34, 34, 34, 0.55)" after saving the workspace
    #2
    And User applies the filter and selects option
      | FilterName      | Option                                                        |
      | NPI Age         | Below 25, 25 to 35, 35 to 45, 45 to 55, 55 to 65, 65 or Above |
      | NPI Gender      | Female, Male, Unknown                                         |
      | Graduation Year | 1900-2025                                                     |
    And User clicks on Ok and closes the filter popup
    #3
    Then Verify that the applied filters are displayed correctly
    And User saves the "HCP Explorer" workspace
    Then Verify the "HCP Explorer" Workspace is saved
    And Download button is enabled to the user
    #4
    And User clicks on Publish NPI List
    And User selects publish "<LIST_TYPE>"
    And User select the platform to publish the list
    Then Verify list is published
    #5
    And User navigates to NPI Lists page
    And User searches the workspace in LIFE and selects it
    And User clicks on the published workspace
    Then User Verify the list is displayed in the Life
    And User navigates to "Studio" application
    And User searches the workspace created to perform Actions from More menu
    When User selects the "Delete" option by clicking More Actions menu
    And Verify user is able to delete the workspace
    Examples:
      | ADVERTISER | WORKSPACE_NAME | LIST_TYPE |
      | Abbvie     | Explorer       | Static    |

  @e2e
  Scenario Outline: Create and save HCP Explorer workspace with Engagement filters and verify the Retrofit feature when the Live List is selected
    #1
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And Verify that advertiser field is disabled and displayed in "rgba(34, 34, 34, 0.55)" after saving the workspace
    #2
    When User applies the filter and selects option
      | FilterName | Option             |
      | Site       | Associated Device  |
      | Search     | Associated Network |
    And User clicks on Ok and closes the filter popup
    #3
    Then Download button is enabled to the user
    And User clicks on Publish NPI List
     #4
    And User selects publish "<LIST_TYPE>"
    And Verify the Retrofit checkbox is selected
    And User selects "<NPI_RETENTION_OPTION>" as Keep NPIs on the list option
    And User select the platform to publish the list
    Then Verify list is published
    And User clicks on Published button, verifies the "<LIST_TYPE>" and "NPIs engaging" text are displayed
    And Navigate to workspace dashboard
    And User searches the workspace created to perform Actions from More menu
    And User selects the "Delete" option by clicking More Actions menu
    And Verify user is able to delete the workspace
    Examples:
      | WORKSPACE_NAME | ADVERTISER | LIST_TYPE | NPI_RETENTION_OPTION |
      | Explorer       | Abbvie     | Live      | indefinitely         |
      | Explorer       | Abbvie     | Live      | remove after         |

  @e2e
  Scenario Outline: Create and save HCP Explorer workspace by building audience using AI Configurator, applying primary filters and using NPI Cross Filters
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And Verify that advertiser field is disabled and displayed in "rgba(34, 34, 34, 0.55)" after saving the workspace
    And User applies the filter and selects option
      | FilterName | Option                                                        |
      | NPI Age    | Below 25, 25 to 35, 35 to 45, 45 to 55, 55 to 65, 65 or Above |
      | NPI Gender | Female, Male, Unknown                                         |
    And User clicks on Ok and closes the filter popup
    Then Verify that the applied filters are displayed correctly
    And Fetch and verify that NPI details are refined
    And User clicks on AI Configurator and build audience using the AIPrompt "<AI_PROMPT>"
    Then Verify the filter is applied correctly "<PRIMARY_FILTERS>"
    And Verify after adding ai prompt filter selected manually should be overwritten
    And Fetch and verify that NPI details are refined
    And User applies the filter and selects option
      | FilterName      | Option                                                        |
      | Patient Age     | Below 25, 25 to 35, 35 to 45, 45 to 55, 55 to 65, 65 or Above |
      | Patient Gender  | Female, Male, Unknown                                         |
      | Graduation Year | 1900-2025                                                     |
    And User clicks on Ok and closes the filter popup
    And Fetch and verify that NPI details are refined
    And User hovers over the dashboard filters, selects the region with maximum NPIs and clicks on it
      | NPI Geographic Location  |
      | NPI Facilities Geography |
    And Fetch and verify that NPI details are refined
    Then Delete the filter
    And Fetch and verify that NPI details are refined
    And User saves the "HCP Explorer" workspace
    Then Verify the "HCP Explorer" Workspace is saved
    And Navigate to workspace dashboard
    And User searches the workspace created to perform Actions from More menu
    And User selects the "Delete" option by clicking More Actions menu
    And Verify user is able to delete the workspace
    Examples:
      | ADVERTISER | WORKSPACE_NAME | AI_PROMPT                                                                                | PRIMARY_FILTERS |
      | Abbvie     | Explorer       | Find doctors within certain age ranges, with specific professions, and narrow by wealth. | Profession      |