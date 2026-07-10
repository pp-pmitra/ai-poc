Feature: External user permission to enable or disable studio platform

  @regression
  Scenario Outline: Verify external user permission for studio application
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    When "Life" application is logged in successfully with Account "automation@pulsepoint"
    And User navigates to administration tab
    And User clicks on accounts tab
    And Locate an account "<EXTERNAL_ACCOUNT>" with external user permission and select it
    And Go to users tab and search "<EXTERNAL_USER>" and select studio tab
    Then User turns on studio toggle for external users and verifies that it is enabled
    Given This scenario will be executed in the "Pre-release" environment as a "External User"
    And "Studio" application is logged in successfully with Account "<ACCOUNT_NAME>"
    Then User should be able to see Studio for that account

    Examples:
      | EXTERNAL_ACCOUNT | EXTERNAL_USER |
      | HMT Demo         | hmtdemo       |