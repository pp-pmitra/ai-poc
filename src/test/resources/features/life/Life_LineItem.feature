Feature: LIFE Regression - Line Item Management
  Verify all functionalities related to creating, editing, managing, and validating line items within a campaign

  Background:
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"

  @regression
  Scenario Outline: Validate that a line item cannot be saved without valid flight details and appropriate budget and date constraints
    And User clicks on Create Campaign
    When User enters the campaign details as "01- Advertiser" "Auto" "Regular" "20000" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    And Verify Line Item page has below tabs
      | Overview       |
      | Details        |
      | Flights        |
      | Conversion     |
      | Tactics        |
      | Media Insights |
    And Verify status of line item is Incomplete when there are no tactics under the line item
    When User fills in required details "<LINE_ITEM>" except for flight information and save
    Then User should see an error message to add flight details
    And User clicks Add Flight button
    And User tries to save the line item without entering any flight details
    Then User should see error message "Invalid budget" when tries to save line item page
    And Verify if user enters flight budget that exceeds Campaign budget
    Then User should see error message when tries to save line item page
    And User adds the flight details - Flight Start Date, Flight End Date, "<BUDGET>"
    And User adds new flight and enter overlapping flight details - Flight Start Date, Flight End Date, "<BUDGET>"
    And User should see error message when tries to save line item page and dates fields should get highlighted with inline error message
    And Verify "Apply Impression Cap for This Flight" and "Apply Daily Impression Cap" checkboxes are available for each flight entry
    And User should be able to check the "Apply Impression Cap for This Flight" and "Apply Daily Impression Cap" checkboxes
    And Verify error message if user fails to add impression cap value when the checkboxes are selected and tries to save the line item page
    Examples:
      | LINE_ITEM  | BUDGET |
      | Line_Item_ | 200    |

  @regression
  Scenario Outline: Add multiple flights to a line item and verify Flights tab after deleting some entries
    And User clicks on Create Campaign
    When User enters the campaign details as "01- Advertiser" "Auto" "Regular" "20000" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters line item name "<LINE_ITEM>" on details page
    And User adds "<NUMBER_OF_FLIGHTS>" flights, fills in the details with "<BUDGET>" for each flight section, and saves the line item
    And User fetches all the flight details added
    Then User navigates to the Flights tab and verifies the flight details
    When User deletes some flight entries
    Then User should see the remaining flights listed under the Flights section
    Examples:
      | LINE_ITEM  | BUDGET | NUMBER_OF_FLIGHTS |
      | Line_Item_ | 200    | 3                 |

  @regression
  Scenario Outline: Add sequential flights to a line item and verify Flights tab after deleting some entries
    And User clicks on Create Campaign
    When User enters the campaign details as "01- Advertiser" "Auto" "Regular" "20000" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters line item name "<LINE_ITEM>" on details page
    And User generates sequential flights for the line item using "<BUDGET>" and "<NUMBER_OF_MONTHS>"
    And Verify that Sequential flights should be added based on the start month and verify start date of the month for each flight entry
    And Verify end date of the month for each flight entry
    And User fetches all the flight details added
    Then User navigates to the Flights tab and verifies the flight details
    When User deletes some flight entries
    Then User should see the remaining flights listed under the Flights section
    Examples:
      | LINE_ITEM  | BUDGET | NUMBER_OF_MONTHS |
      | Line_Item_ | 200    | 4                |

  @regression
  Scenario Outline: Perform create, modify, duplicate, delete, and toggle operations on line items using Bulk Edit Mode and verify data integrity after duplication
    And User clicks on Create Campaign
    When User enters the campaign details as "01- Advertiser" "Auto" "Regular" "20000" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User creates line items with below line types and other details, enables the line item and saves the changes
      | LINE_TYPE        | LINE_ITEM_DETAILS                                                                                                                          |
      | Display          | LineName:LineItem, LineBudget:500, CostModel:Fixed CPM, CPMAmount:50, BudgetDistribution:Priority, PacingMode:Even, PacingPercentage:60    |
      | Audio            | LineName:LineItem, LineBudget:500, CostModel:CPM, CPMAmount:50, BudgetDistribution:Dollars, PacingMode:ASAP, PacingPercentage:60           |
      | Video            | LineName:LineItem, LineBudget:500, CostModel:Fixed CPM, CPMAmount:50, BudgetDistribution:Percentage, PacingMode:Ahead, PacingPercentage:60 |
      | Native Display   | LineName:LineItem, LineBudget:500, CostModel:CPM, CPMAmount:50, BudgetDistribution:Priority, PacingMode:Even, PacingPercentage:60          |
      | Native Video     | LineName:LineItem, LineBudget:500, CostModel:Fixed CPM, CPMAmount:50, BudgetDistribution:Priority, PacingMode:Ahead, PacingPercentage:60   |
      | DOOH             | LineName:LineItem, LineBudget:500, CostModel:CPM, CPMAmount:50, BudgetDistribution:Dollars, PacingMode:ASAP, PacingPercentage:60           |
      | Search Extension | LineName:LineItem, LineBudget:500, CostModel:Fixed CPM, CPMAmount:50, BudgetDistribution:Percentage, PacingMode:Ahead, PacingPercentage:60 |
    And Verify Bulk Edit Mode successfully "disables" multiple selected line items
    And Verify that each selected line item is "Disabled"
    And Verify Bulk Edit Mode successfully "enables" multiple selected line items
    And Verify that each selected line item is "Enabled"
    Then User adds Comments or Notes "<LINE_ITEM_NOTE>" to each line item
    And Verify the notes added to each line item
    And Verify user is able to create a copy of the line items using "Duplicate" option
    And Verify "Delete" is available for each item, and deleted items are removed from the Left menu
    And Verify "Generate Report" option opens the Run report screen for user and run the report for "<TEMPLATE>"
    And Verify that the reports generated on the Line Item page are available on the Generate Report page
    Examples:
      | LINE_ITEM_NOTE | TEMPLATE            |
      | Notes          | Template_Automation |

  @regression
  Scenario: Verify data integrity after creating line items with different line types and details
    And User clicks on Create Campaign
    When User enters the campaign details as "01- Advertiser" "Auto" "Regular" "20000" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User creates line items with below line types and other details and verifies the details after saving the line item
      | LINE_TYPE        | LINE_ITEM_DETAILS                                                                                                                          |
      | Display          | LineName:LineItem, LineBudget:500, CostModel:Fixed CPM, CPMAmount:50, BudgetDistribution:Priority, PacingMode:Ahead, PacingPercentage:60   |
      | Audio            | LineName:LineItem, LineBudget:500, CostModel:Fixed CPM, CPMAmount:50, BudgetDistribution:Dollars, PacingMode:ASAP, PacingPercentage:60     |
      | Video            | LineName:LineItem, LineBudget:500, CostModel:Fixed CPM, CPMAmount:50, BudgetDistribution:Percentage, PacingMode:Ahead, PacingPercentage:60 |
      | Native Display   | LineName:LineItem, LineBudget:500, CostModel:Fixed CPM, CPMAmount:50, BudgetDistribution:Priority, PacingMode:Even, PacingPercentage:60    |
      | Native Video     | LineName:LineItem, LineBudget:500, CostModel:Fixed CPM, CPMAmount:50, BudgetDistribution:Priority, PacingMode:Ahead, PacingPercentage:60   |
      | DOOH             | LineName:LineItem, LineBudget:500, CostModel:Fixed CPM, CPMAmount:50, BudgetDistribution:Dollars, PacingMode:ASAP, PacingPercentage:60     |
      | Search Extension | LineName:LineItem, LineBudget:500, CostModel:Fixed CPM, CPMAmount:50, BudgetDistribution:Percentage, PacingMode:Ahead, PacingPercentage:60 |

  @regression
  Scenario Outline: Verify addition of Line items to the existing campaign and validate the data persistence after updating the line item details
    When User clicks on the existing campaign to open the campaign details page
    And User clicks on Add Line Item button
    And User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify that the new line item is added to the existing campaign and displayed in the left menu under the campaign
    And User updates line item details such as "<UPDATED_LINE_BUDGET>" and flight dates and saves the line item
    Then Verify that the line item details are updated successfully and reflected on the Line Item page
    Examples:
      | LINE_NAME | LINE_BUDGET | UPDATED_LINE_BUDGET |
      | Line      | 500         | 505.00              |

  @regression
  Scenario Outline: Create new custom field in Line Item details page and verify its availability in all the new and existing line items under all Campaigns of the selected account
    And User clicks on Create Campaign
    When User enters the campaign details as "01- Advertiser" "Auto" "Regular" "20000" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters line item name "<LINE_ITEM>" on details page
    Then User creates new custom field "<CUSTOM_NAME>" and verifies the same in the line item details page
    And User enters details in "<LINE_BUDGET>" enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    And User navigates to Campaign Dashboard
    And User clicks on the existing campaign to open the campaign details page
    Then Verify the custom field created in line item details page is available for all line items under the campaign
    Then User deletes the custom field and verify its removed from new tactic
    Examples:
      | LINE_ITEM  | CUSTOM_NAME     | LINE_BUDGET |
      | Line_Item_ | Custom_Field_ID | 50          |