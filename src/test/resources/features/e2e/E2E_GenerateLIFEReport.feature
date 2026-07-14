Feature: End to End Workflow of Report Generation.
  It covers below points
  1. Create a Campaign with Line item and Tactic.
  2. Create a report template by passing dimensions and metrics.
  3. Run the report with newly created Campaign, Line item, Tactic and Report Template
  4. Download the report from generated report tab.
  5. Verify the Column headers of Template and Report file.

  @e2e @regression
  Scenario Outline: End to End Workflow of Report Generation with Campaign and Report Template creation.
    # 1
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And User clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    And User selects the "<CHANNEL>" as channel
    And User selects "<RULE_TYPE>" as rule type and configures the targeting rules, and saves the settings
    Then Verify settings details are saved and user is navigated to the creatives tab
    And User assigns the existing creative named "<CREATIVE>", enables the tactic and saves the changes
    Then Verify the newly created campaign is in running state
    Then Verify the newly created campaign details in the campaign list: Campaign name, Line item name and Tactic name
    # 2
    And User navigates to Report Templates page
    Then Verify the tabs displayed on the Report Templates page
    When User clicks on New Template
    Then Verify the tabs displayed on the Create New Template panel
    When User enters the template details for end to end as "<TEMPLATE NAME>" "<DIMENSIONS>" "<METRICS>"
    When User saves the new template
    Then Verify new template is saved and displayed in the template list
    # 3
    And User navigates to run report from mega menu of the life application
    Then User selects the report template created tactic and other fields for running the report
    Then User verifies the selected campaign,line item, tactic and runs report by clicking on Run button
    Then User navigates to generate report field and verifies the report name by campaign name
    # 4 & 5
    Then User downloads the report and verify the data in downloaded report
    Examples:
      | USER  | ADVERTISER     | CP_NAME | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME | CHANNEL          | CREATIVE            | REPORT_TEMPLATE | TEMPLATE NAME | DIMENSIONS                    | METRICS            | RULE_TYPE          |
      | Admin | 01- Advertiser | Auto    | Regular | 20000     | Line      | 500         | Tactic      | Display Advanced | Automation_Creative | E2E Report      | Template      | Advertiser Name,Campaign Name | Impressions,Clicks | Behavioral Segment |