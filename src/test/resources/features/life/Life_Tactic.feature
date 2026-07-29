Feature: LIFE Regression - Verify below scenarios in Tactic creation flow
  1. Create multiple tactics
  2. Verify the availability of three tabs
  3. Verify header section of tactic displays correct status
  4. Verify user is able to add custom field

  Background:
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"

  @regression
  Scenario Outline: Create multiple tactics and verify its tabs and status
    When User clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    Then User creates below tactics under same line item and verifies it
      | Tactic Name           | Channel  | RuleType           |
      | Targeting Segment     | Email    | Health Population  |
      | Health Populations    | EHR      | NPI                |
      | Audience Group tactic | Standard | Behavioral Segment |
    Then Verify that below tabs gets enabled only after saving tactics
      | Settings  |
      | Creatives |
      | Debugger  |
      | Details   |
    And Verify the status of first tactic under line item is "Incomplete"
    Examples:
      | ADVERTISER     | CP_NAME | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET |
      | 01- Advertiser | Auto    | Regular | 20000     | Line      | 500         |

  @regression
  Scenario Outline: Create new custom field in tactic and delete it
    When User clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    Then User creates below tactics under same line item and verifies it
      | Tactic Name       | Channel | RuleType          |
      | Targeting Segment | Email   | Health Population |
    Then User clicks on first tactic and goes to details tab
    Then User creates new custom field "<CUSTOM_NAME>" and verifies the same
    And User verifies if new custom field is visible and empty in new tactic "<TACTIC_SEARCH>"
    Then User clears the custom field text
    Then User deletes the custom field and verify its removed from new tactic
    Examples:
      | ADVERTISER     | CP_NAME | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | CUSTOM_NAME | TACTIC_SEARCH |
      | 01- Advertiser | Auto    | Regular | 20000     | Line      | 500         | Custom ID   | Tactic        |

  @regression
  Scenario Outline: Verify Base bid price and Max bid price populates correctly for a tactic
    When User clicks on Campaign Settings
    Then Verify user is on default bid settings page
    And  User gets Max Bid Base Bid values and Highest Possible Max Bid value from Campaign Settings
    And Navigate to Campaign Dashboard and clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    And  Verify Max Bid and Base Bid values on the tactic settings match with Campaign Settings values
    Examples:
      | ADVERTISER     | CP_NAME | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME |
      | 01- Advertiser | Auto    | Regular | 20000     | Line      | 500         | Tactic      |

  @regression
  Scenario Outline: Verify user is not able to set Base bid price and Max Bid higher than the allowed limit for a tactic
    When User clicks on Campaign Settings
    Then Verify user is on default bid settings page
    And  User gets Max Bid Base Bid values and Highest Possible Max Bid value from Campaign Settings
    And Navigate to Campaign Dashboard and clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    Then Verify user is able to update and save the "base" bid price
    Then Verify user is able to update and save the "max" bid price
    Then Verify user is not able to update "base" bid price more than allowed limit
    Then Verify user is not able to update "max" bid price more than allowed limit
    Examples:
      | ADVERTISER     | CP_NAME | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME |
      | 01- Advertiser | Auto    | Regular | 20000     | Line      | 500         | Tactic      |

  @regression
  Scenario Outline: Verify deletion of Tactic from a Line Item
    When User clicks on create new Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    Then User creates a new tactic with details "<TACTIC_NAME>" "<CHANNEL>" "<COUNT>"
    Then User deletes the tactic and verifies it
    Examples:
      | ADVERTISER     | CP_NAME | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | CHANNEL | TACTIC_NAME | COUNT |
      | 01- Advertiser | Auto    | Regular | 20000     | Line      | 500         | Email   | Tactic      | 3     |

  @regression
  Scenario Outline: Create tactic and enable those tactics through bulk action
    When User clicks on create new Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    Then User creates a new tactic with details "<TACTIC_NAME>" "<CHANNEL>" "<COUNT>"
    And User enables tactic through bulk action and verifies the status
    Examples:
      | ADVERTISER     | CP_NAME | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | CHANNEL | TACTIC_NAME | COUNT |
      | 01- Advertiser | Auto    | Regular | 20000     | Line      | 500         | Email   | Tactic      | 3     |

  @regression
  Scenario Outline: To verify user is able to add frequency cap in campaign, line item and tactic levels
    When User clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    Then User creates below tactics under same line item and verifies it
      | Tactic Name           | Channel  | RuleType           |
      | Audience Group tactic | Standard | Behavioral Segment |
    Then User navigates to campaign
    Then User clicks on details tab
    Then User verifies if Frequency Cap is in disabled state by default
    Then User adds frequency cap with details "<ON_CAMPAIGN_LEVEL>" "<FREQUENCY_VALUE>" "<TIMES_PER>" "<SCOPE>"
    Then User navigates to LineItem
    Then User clicks on details tab
    Then User verifies if frequency cap is saved with details "<FREQUENCY_VALUE>" "<TIMES_PER>" "<SCOPE>" "<ON_CAMPAIGN_LEVEL>"
    Then User verifies if Frequency Cap is in disabled state by default
    Then User adds frequency cap with details "<ON_LI_LEVEL>" "<FREQUENCY_VALUE>" "<TIMES_PER>" "<SCOPE>"
    Then User navigates to Tactic and clicks on settings tab
    Then User verifies if frequency cap is saved with details "<FREQUENCY_VALUE>" "<TIMES_PER>" "<SCOPE>" "<ON_CAMPAIGN_LEVEL>"
    Then User verifies if frequency cap is saved with details "<FREQUENCY_VALUE>" "<TIMES_PER>" "<SCOPE>" "<ON_LI_LEVEL>"
    Then User verifies if Frequency Cap is in disabled state by default
    Then User adds frequency cap with details "<ON_TACTIC_LEVEL>" "<FREQUENCY_VALUE>" "<TIMES_PER>" "<SCOPE>"
    Then User navigates to LineItem
    Then User navigates to Tactic and clicks on settings tab
    Then Verify that frequency cap is saved in tactic
    Examples:
      | ADVERTISER     | CP_NAME | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | FREQUENCY_VALUE | TIMES_PER | SCOPE         | ON_CAMPAIGN_LEVEL | ON_LI_LEVEL        | ON_TACTIC_LEVEL |
      | 01- Advertiser | Auto    | Regular | 20000     | Line      | 500         | 10              | hour(s)   | Per Person    | on Campaign Level | on Line Item Level | on tactic level |
      | 01- Advertiser | Auto    | Regular | 20000     | Line      | 500         | 80              | week      | Per Household | on Campaign Level | on Line Item Level | on tactic level |

  @regression
  Scenario Outline: Add and Verify Comment/notes on New Tactic from Header and Navigation
    When User clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User creates below tactics under same line item and verifies it
      | Tactic Name       | Channel | RuleType          |
      | Targeting Segment | Email   | Health Population |
    When User navigates to Tactic and clicks on settings tab
    And User clicks the comments icon in the tactic "header" section and add "<HEADER_COMMENT>"
    Then Verify that "<HEADER_COMMENT>" is visible in "navigation" section
    Then User validates the comment added in "header" is "<HEADER_COMMENT>" then clear it
    And User clicks the comments icon in the tactic "navigation" section and add "<NAV_COMMENT>"
    Then Verify that "<NAV_COMMENT>" is visible in "header" section
    Then User validates the comment added in "navigation" is "<NAV_COMMENT>" then clear it
    Examples:
      | ADVERTISER     | CP_NAME | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | HEADER_COMMENT        | NAV_COMMENT              |
      | 01- Advertiser | Auto    | Regular | 20000     | Line      | 500         | Test Note from Header | Test Note from Nav Panel |

  @regression
  Scenario Outline: Create tactic and disable those tactics through bulk action
    When User clicks on create new Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    Then User creates a new tactic with details "<TACTIC_NAME>" "<CHANNEL>" "<COUNT>"
    And User enables tactic through bulk action and verifies the status
    And User disables tactic through bulk action and verifies the status
    Examples:
      | ADVERTISER     | CP_NAME | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | CHANNEL | TACTIC_NAME | COUNT |
      | 01- Advertiser | Auto    | Regular | 20000     | Line      | 500         | Email   | Tactic      | 3     |

  @regression
  Scenario Outline: Verify user is able to create duplicate of a Tactic
    When User clicks on Create Campaign
    And User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    When User selects the "<CHANNEL>" as channel
    And User selects "<RULE_TYPE>" as rule type and configures the targeting rules, and saves the settings
    Then Verify settings details are saved and user is navigated to the creatives tab
    And User assigns the existing creative named "<CREATIVE>", enables the tactic and saves the changes
    When User duplicates tactic, verify data on the duplicated tactic using "Duplicate" option
    Examples:
      | ADVERTISER     | CP_NAME | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME | CHANNEL          | RULE_TYPE          | CREATIVE      |
      | 01- Advertiser | Auto    | Regular | 20000     | Line      | 500         | Tactic      | Display Advanced | Behavioral Segment | Auto_Creative |

  @regression
  Scenario Outline: Verify all Bid Multipliers Rules under categories and Create a tactic by adding all Bid multipliers Rules
    And User clicks on create new Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    Then User creates a new tactic with details "<TACTIC_NAME>" "<CHANNEL>" "<COUNT>"
    Then User navigates to tactic setting tab
    Then User verify Behaviour segment and NPI are not allowed in bid multiplier rules when same are not selected in targeting rules
    Then User close the bid panel to add targeting rules
    And User configures targeting rules as below
      | Behavioral Segment | 111 > 222 > Patients of HCPs prescribing Ivig and SCIg competitors |
      | NPI                | AutoSmartList954103283                                             |
    Then Verify Bid multiplier panel with all options under below categories
      | AUDIENCE ATTRIBUTE |
      | DEMOGRAPHICS       |
      | GEOGRAPHY          |
      | MEDIA SUPPLY       |
    And Verify Bid type with respect to category
      | AUDIENCE ATTRIBUTE | Behavioral Segment,Day of The Week,Speciality,Practitioner Type,NPI              |
      | DEMOGRAPHICS       | Age,Gender                                                                       |
      | GEOGRAPHY          | Geo Targets                                                                      |
      | MEDIA SUPPLY       | Browser,Device,Operating Systems,Inventory Source,Domains and Apps,Creative Size |
    And User configures Bid multiplier rules as below with "<BID_VALUE>"
      | Behavioral Segment | 111 > 222 > Patients of HCPs prescribing Ivig and SCIg competitors |
      | NPI                | AutoSmartList954103283                                             |
    Then Verify the configured Bid multiplier rules
    When User saves the Bid multiplier settings
    Then Verify settings details are saved and user is navigated to the creatives tab
    And User assigns the existing creative named "<CREATIVE>", enables the tactic and saves the changes
    Then Verify the newly created campaign is in running state
    Then Verify the newly created campaign details in the campaign list: Campaign name, Line item name and Tactic name
    Examples:
      | ADVERTISER     | CP_NAME | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME | CHANNEL | CREATIVE      | COUNT | BID_VALUE |
      | 01- Advertiser | Test    | Regular | 10000     | Line      | 120         | Tactic      | Email   | Auto_Creative | 1     | 2         |

  @regression
  Scenario Outline: Verify campaign management fee is reflected in line item and line item override is reflected in tactic
    When User clicks on Create Campaign
    And User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>"
    And User sets campaign management fee as "<CAMPAIGN_FEE_OPTION>" "<CAMPAIGN_PERCENT>" "<CAMPAIGN_AMOUNT>"
    And User saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    And User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    Then User navigates to line item and clicks on details tab
    And Verify management fee is set as "<CAMPAIGN_DISPLAY_VALUE>"
    Then User clicks on create new tactic
    Then User creates a new tactic with details "<TACTIC_NAME>" "<CHANNEL>" "<COUNT>"
    Then User navigates to tactic setting tab
    And Verify management fee is set as "<CAMPAIGN_DISPLAY_VALUE>"
    When User overrides line item management fee and verifies tactic reflection for the following fee types
      | Fee Option | Percent | Amount | Expected Display |
      | Percentage | 7.15    |        | + 7.15 %         |
      | CPM        |         | 10.50  | + $10.5          |
      | % + CPM    | 7       | 10     | + 7 % + $10      |
      | Fixed CPM  |         | 11.1   | $11.1            |
    Examples:
      | ADVERTISER     | CP_NAME | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | CAMPAIGN_FEE_OPTION | CAMPAIGN_PERCENT | CAMPAIGN_AMOUNT | CAMPAIGN_DISPLAY_VALUE | CHANNEL | TACTIC_NAME | COUNT |
      | 01- Advertiser | Auto    | Regular | 20000     | Line      | 500         | Percentage          | 5                | 5               | + 5 %                  | Email   | Tactic      | 1     |

  @regression
  Scenario Outline: Verify forecast refreshes after adding Age targeting to a new tactic
    And User clicks on create new Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then User navigates to tactic setting tab
    Then User verifies that forecast data is unavailable when no targeting rules are applied
    When User clicks on Add Targeting Rule
    And User configures targeting rules as below
      | Age | 35-39, 55-59 |
    And User saves the settings
    And User navigates to tactic setting tab
    Then User verifies the forecast data refreshes and displays values after adding targeting rule
    Examples:
      | ADVERTISER     | CP_NAME | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME |
      | 01- Advertiser | Test    | Regular | 10000     | Line      | 120         | Tactic      |

  @regression
  Scenario Outline: Verify show expression query is correct for the chosen targeting rules
    And User clicks on create new Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then User navigates to tactic setting tab
    Then User verifies that forecast data is unavailable when no targeting rules are applied
    When User clicks on Add Targeting Rule
    And User configures targeting rules as below
      | Behavioral Segment | AutoSegment18577650, 10136 Testing |
      | Health Populations | Anesthesia and Analgesia           |
      | Age                | 25-29, 35-39                       |
    And User saves the settings
    And User navigates to tactic setting tab
    Then The user clicks on show expression tab and fetch the values displayed
    Then Verify that all the rule types added in targeting rules are displayed in show expression with correct values along with "<DEFAULT_EXPRESSION>"
    And Verify show expression connector AND OR logic is correct
    Examples:
      | ADVERTISER     | CP_NAME | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME | DEFAULT_EXPRESSION |
      | 01- Advertiser | Test    | Regular | 10000     | Line      | 120         | Dynamic_Tac | COUNTRY            |
