Feature: Enable Studio permissions for an account,advertiser and external users
  1. This Feature verifies the ability to enable Studio permissions for an account and external users.
  2. It includes scenarios for both internal users and external users, ensuring that the Studio permissions can be granted and verified correctly.
  3. It covers logging in, navigating through accounts, advertisers, and users, enabling permissions, and confirming that the external user can see the assigned Studio permissions in the workspace after logging in.
  4. Multiple permission types and advertisers are validated using example data.

  @regression
  Scenario Outline: Enable Studio for an Account for internal users
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And User enables the studio for "<ACCOUNT_NAME>" account
    And User navigates to workspace permissions
    When User selects the workspace types and saves the settings
    Then Studio should be enabled for that account
    Then User verifies if Studio appears in submenu for "<ACCOUNT_NAME>" account
    When User disables the studio permission for "<ACCOUNT_NAME>" account
    Then User should not be able to see the studio permission for that account

    Examples:
      | ACCOUNT_NAME |
      |      100Plus |

  @np
  Scenario Outline: Enable Studio permissions for an External User
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    When User navigates to Administrative section
    And User navigates to Accounts Tab
    And User searches and selects the account "<ACCOUNT_NAME>"
    And User navigates to advertisers page under the selected account
    And User enables the "<ADVERTISER_PERMISSIONS>" permission for the "<ADVERTISER_NAME>" advertiser
    And User navigates to users page under the selected account
    And User selects the "<USER_NAME>" external user
    And User enables the "<STUDIO_PERMISSIONS>" permission for the "<ACCOUNT_NAME>" for an external user
    Given This scenario will be executed in the "Pre-release" environment as a "External User"
    And "Studio" application is logged in successfully with Account "<ACCOUNT_NAME>"
    And External user selects the workspace
    Then External user should be able to see the "<STUDIO_PERMISSIONS>" permission in the workspace

    Examples:
      | USER_NAME | STUDIO_PERMISSIONS | ACCOUNT_NAME | ADVERTISER_PERMISSIONS | ADVERTISER_NAME |
      | hmtdemo   | MOMENTS            | HMT Demo     | MOMENTS                | Demo W2O        |
      | hmtdemo   | IB HEALTH          | HMT Demo     | IB HEALTH              | Demo W2O        |
      | hmtdemo   | CLAIMS DATA        | HMT Demo     | CLAIMS DATA            | Demo W2O        |
