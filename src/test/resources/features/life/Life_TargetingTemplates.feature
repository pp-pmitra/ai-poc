Feature: LIFE Regression - Create a Targeting Template for below Line Item Type and import in respective Tactic
  1. Display
  2. Video
  3. Native Display
  4. Audio
  5. Search Extension
  6. DOOH
  7. Native Video

  Background:
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"

  @e2e
  Scenario Outline: Create a Targeting Template and import the template in Tactic
    #1
    When User navigates to Targeting template page by clicking the icon from Activation section
    Then Verify New Template button is present above the Search option
    And Verify Targeting template section opens by clicking New Template button
    #2
    When User creates Targeting template "<TEMPLATE_NAME>" for the line items "<LINE_ITEMS>" with channel "<CHANNEL>" and Targeting Rules
      | Behavioral Segment | AutoSegment |
      | Age                | 25-29       |
      | IP                 | AutoIP      |
      | Postal Codes       | 112233      |
    Then User searches and verifies the already created targeting template using the search option
    #3
    And Create a tactic with "<LINE_ITEMS>" line items and other details "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" "<LINE_NAME>" "<LINE_BUDGET>" "<TACTIC_NAME>" and import the template in Tactic
    Then Verify the template created can be imported in the Tactic
    Examples:
      | TEMPLATE_NAME | CHANNEL                                         | LINE_ITEMS                                                                  | ADVERTISER     | CP_NAME           | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME |
      | Template      | Display Advanced, Video Advanced, DOOH Advanced | Display, Video, Native Display, Audio, Search Extension, DOOH, Native Video | 01- Advertiser | TargetingTemplate | Regular | 20000     | Line      | 500         | Tactic      |

  @regression
  Scenario Outline: Create a Targeting Template, verifies the existing template and import the template in Tactic
    #1
    When User navigates to Targeting template page by clicking the icon from Activation section
    Then Verify New Template button is present above the Search option
    And Verify Targeting template section opens by clicking New Template button
    #2
    When User creates Targeting template "<TEMPLATE_NAME>" for the line items "<LINE_ITEMS>" with channel "<CHANNEL>" and Targeting Rules
      | Behavioral Segment | AutoSegment |
      | Age                | 25-29       |
      | IP                 | AutoIP      |
      | Postal Codes       | 112233      |
    Then User searches and verifies the already created targeting template using the search option
    #3
    And User tries to save the targeting template with targeting rule "Behavioral Segment" and without specifying a template name
    And User tries to save the targeting template with template name "<TEMPLATE_NAME>" without specifying any targeting
    #4
    And User clicks on Show Expression and verifies the query is displayed for the "Template"
    And User edits an existing targeting template and verifies the changes are saved for the "Template"
    And User deletes an existing targeting template and verifies it is removed from the list for the "Template"
    #5
    And Create a tactic with "<LINE_ITEMS>" line items and other details "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" "<LINE_NAME>" "<LINE_BUDGET>" "<TACTIC_NAME>" and import the template in Tactic
    Then Verify the template created can be imported in the Tactic
    Examples:
      | TEMPLATE_NAME | CHANNEL                                         | LINE_ITEMS                                                                  | ADVERTISER     | CP_NAME           | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME |
      | Template      | Display Advanced, Video Advanced, DOOH Advanced | Display, Video, Native Display, Audio, Search Extension, DOOH, Native Video | 01- Advertiser | TargetingTemplate | Regular | 20000     | Line      | 500         | Tactic      |

  @e2e @regression
  Scenario Outline: Create a Targeting Template from Tactic and its availability under Targeting templates page
    #1
    And Create a tactic with below targeting rules and "<LINE_ITEMS>" line items and other details "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" "<LINE_NAME>" "<LINE_BUDGET>" "<TACTIC_NAME>"
      | Behavioral Segment | AutoSegment |
      | Age                | 25-29       |
      | IP                 | AutoIP      |
      | Postal Codes       | 112233      |
    Then Verify the template created are saved
    #2
    When User navigates to Targeting template page by clicking the icon from Activation section
    Then Verify New Template button is present above the Search option
    And Verify Targeting template section opens by clicking New Template button
    #3
    Then User searches and verifies the already created targeting template using the search option
    Examples:
      | LINE_ITEMS                                                                  | ADVERTISER     | CP_NAME           | CP_TYPE | CP_BUDGET | LINE_NAME     | LINE_BUDGET | TACTIC_NAME     |
      | Display, Video, Native Display, Audio, Search Extension, DOOH, Native Video | 01- Advertiser | TargetingTemplate | Regular | 20000     | TargetingLine | 500         | TargetingTactic |

  @regression
  Scenario: Verify only life marketplace tab should be displayed under Targeting templates from Activation section
    When User navigates to Targeting template page by clicking the icon from Activation section
    Then Verify New Template button is present above the Search option
    And Verify Targeting template section opens by clicking New Template button
    And Verify only life marketplace tab is displayed under Targeting templates section for "Deals" rule type