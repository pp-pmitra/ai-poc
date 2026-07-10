Feature: E2E workflow for creating a campaign with an external user and approving it with an admin user
  1. Creation of a campaign by an external user
  2. Approval of the created campaign by an admin user
  3. Verification of the approved campaign by the external user

  @e2e
  Scenario Outline: Create a Campaign with a Line Item and a Tactic for an External user, then approve it as an Admin user
    Given This scenario will be executed in the "Demo" environment as a "External User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"
    And User clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>"
    Then Verify that the campaign budget status is "Pending Appr" and is greyed out
    And User saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    When User clicks on Add Targeting Rule
    And User selects "<RULE_TYPE>" as rule type and configures the targeting rules, and saves the settings
    Then Verify settings details are saved and user is navigated to the creatives tab
    And User assigns the existing creative named "<CREATIVE>", enables the tactic and saves the changes
    Then Verify creative details are saved
    Then Verify that the campaign is in "Pending Appr" state
    Then Verify that the approval status of the campaign is "Pending Appr"
    Then Verify the newly created campaign details in the campaign list: Campaign name, Line item name and Tactic name
    And User logs out from the application
    And This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"
    And User navigates to the created campaign
    And Admin user approves the campaign
    Then Verify that the campaign is in "Running" state
    And User logs out from the application
    Given This scenario will be executed in the "Demo" environment as a "External User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"
    And User navigates to the created campaign
    Then Verify that the campaign is in "Running" state
    Then Verify that the approval status of the campaign is "Approved"
    Examples:
      | ADVERTISER       | CP_NAME       | CP_TYPE | CP_BUDGET | LINE_NAME     | LINE_BUDGET | TACTIC_NAME     | RULE_TYPE          | CREATIVE          |
      | 1Demo Advertiser | External_Auto | Regular | 10000     | External_Line | 500         | External_Tactic | Behavioral Segment | External_Creative |
