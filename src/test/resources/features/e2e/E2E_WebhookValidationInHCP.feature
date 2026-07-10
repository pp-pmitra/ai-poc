Feature: Webhook Creation and Validation in HCP Explorer Workspace
  1. Create a HCP Explorer workspace
  2. Verify the webhook is enabled after engagement filters are applied
  3. Create a webhook with GET and verify inline error messages when wrong data is entered and when webhook setup fails
  4. Add valid URL, append Macros to the URL in GET request and save the webhook setup
  5. Create a webhook with POST and verify inline error messages when wrong data is entered and when webhook setup fails
  6. Add valid URL, append Macros to the URL in POST request
  7. Add valid BODY, append Macros to the BODY in POST request
  8. Save the webhook setup and verify the webhook icon is highlighted in green color
  9. Verify user receives a warning when attempting to delete a workspace with an active webhook

  Background:
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    When User navigates to Administrative section
    And User navigates to Accounts Tab
    And User searches the account "PP engineering test" and checks Studio permissions
    And User clicks PulsePoint icon to navigate back to Life
    And User navigates to Studio application

  @e2e
  Scenario Outline: Create a webhook and verify macros are appended to the GET request URL and body
    #1
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And Verify that advertiser field is disabled and displayed in "rgba(34, 34, 34, 0.55)" after saving the workspace
    #2
    And Verify Webhook panel is disabled before applying filters
    When User applies the filter and selects option
      | FilterName | Option             |
      | Site       | Associated Device  |
      | Search     | Associated Network |
    And User clicks on Ok and closes the filter popup
    Then Verify Webhook panel is enabled after applying engagement filters
    #3
    When User clicks "GET" request method
    Then Verify inline error message for the invalid webhook entries "<INVALID_WEBHOOK_DATA>"
    And Verify error message when webhook setup is failed using "<ERROR_DATA>"
    #4
    When User adds valid URL and append Macros with "<PARAM>" to the "URL" as follow
      | NPI        |
      | URL        |
      | Channel    |
      | Param 1..5 |
    Then Verify if Macros Appended to the URL
    When User saves the webhook setup
    Then Check that the success message appears once the webhook is successfully created
    #5
    And User saves the "HCP Explorer" workspace
    Then Verify the "HCP Explorer" Workspace is saved
    Then Check the webhook icon is highlighted in green color
    #6
    When User tries to delete the workspace associated with active webhook from the workspace list
    Then Verify user receives a warning when attempting to delete a workspace with an active webhook
    Examples:
      | ADVERTISER | WORKSPACE_NAME | PARAM   | INVALID_WEBHOOK_DATA | ERROR_DATA                             |
      | Abbvie     | Explorer       | Param 4 | Test                 | https:www.google.com, WebhookData.json |

  @e2e
  Scenario Outline: Create a webhook and verify macros are appended to the POST request URL and body
    #1
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And Verify that advertiser field is disabled and displayed in "rgba(34, 34, 34, 0.55)" after saving the workspace
    #2
    And Verify Webhook panel is disabled before applying filters
    When User applies the filter and selects option
      | FilterName | Option             |
      | Site       | Associated Device  |
      | Search     | Associated Network |
    And User clicks on Ok and closes the filter popup
    Then Verify Webhook panel is enabled after applying engagement filters
    #3
    When User clicks "POST" request method
    And User selects content type "<CONTENT_TYPE>"
    Then Verify inline error message for the invalid webhook entries "<INVALID_WEBHOOK_DATA>"
    And Verify error message when webhook setup is failed using "<ERROR_DATA>"
    #4
    When User adds valid URL and append Macros with "<PARAM>" to the "URL" as follow
      | NPI        |
      | URL        |
      | Channel    |
      | Param 1..5 |
    Then Verify if Macros Appended to the URL
    #5
    When User adds valid body "<BODY>" and append Macros with "<PARAM>" to the "Body" as follow
      | NPI        |
      | URL        |
      | Channel    |
      | Param 1..5 |
    Then Verify if Macros Appended to the Body "<BODY>"
    When User saves the webhook setup
    Then Check that the success message appears once the webhook is successfully created
    #6
    And User saves the "HCP Explorer" workspace
    Then Verify the "HCP Explorer" Workspace is saved
    Then Check the webhook icon is highlighted in green color
    #7
    When User tries to delete the workspace associated with active webhook from the workspace list
    Then Verify user receives a warning when attempting to delete a workspace with an active webhook
    Examples:
      | ADVERTISER | WORKSPACE_NAME | PARAM   | BODY             | CONTENT_TYPE | INVALID_WEBHOOK_DATA | ERROR_DATA                             |
      | Abbvie     | Explorer       | Param 4 | WebhookData.json | JSON         | Test                 | https:www.google.com, WebhookData.json |
