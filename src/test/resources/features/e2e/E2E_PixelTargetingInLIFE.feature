Feature: End to End workflow for all types of Pixel creation and targeting at Tactic level
  It covers below points
  1. Creation of Retargeting Pixel.
  2. Creation of Smart Pixel.
  3. Creation of Conversion Pixel.
  4. Targeting the created Pixel at Tactic level.

  @e2e
  Scenario Outline: Create a Retargeting Pixel and target in 'Retargeting Pixel' targeting at Tactic level
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And User navigates to Pixels page
    When User clicks on Add Pixel button
    Then Verify the Create New Pixel panel and types of Pixel
    And User selects the "<PIXEL_TYPE>" type
    And User enters the pixel details as "<PIXEL_NAME>" "<ADVERTISER>"
    And User saves the pixel
    Then Verify the pixel is saved successfully, search for it by name, and confirm it is displayed in the pixel list
    And User navigates to Campaign Dashboard
    And User clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    When User selects the "<CHANNEL>" as channel
    And User selects "<RULE_TYPE>" as rule type and selects the created pixel
    Then Verify the selected targeting rule "<RULE_TYPE>"
    Then Verify the count of rule options for the selected targeting rule "<RULE_TYPE>" on the Tactic Settings page
    When User saves the settings
    Then Verify settings details are saved and user is navigated to the creatives tab
    Examples:
      | PIXEL_TYPE        | PIXEL_NAME  | ADVERTISER     | CP_NAME              | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME | CHANNEL          | RULE_TYPE          | CREATIVE         |
      | Retargeting Pixel | Retargeting | 01- Advertiser | Retargeting_Campaign | Regular | 10000     | New_Line  | 50          | New_Tactic  | Display Advanced | Retargeting Pixels | Retargeting_Auto |

  @e2e
  Scenario Outline: Create a Smart Pixel and then create a Smart list with that Smart Pixel and target the Smart list in Tactic
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And User navigates to Pixels page
    When User clicks on Add Pixel button
    Then Verify the Create New Pixel panel and types of Pixel
    And User selects the "<PIXEL_TYPE>" type
    And User selects the "<ADVERTISER>" and the associated campaign
    And User saves the pixel
    Then Verify the smart pixel is saved successfully, search for it by name, and confirm it is displayed in the pixel list
    And User navigates to NPI Lists page
    When User clicks on Create New List
    And User selects Smart List
    And User enters the Smart NPI list details as "<LIST_NAME>" "<ADVERTISER>" and selects the created "Smart Pixel"
    Then Verify the selected Smart Pixel
    Then Save and Verify the list gets saved successfully
    And User navigates to Campaign Dashboard
    And User clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    When User selects the "<CHANNEL>" as channel
    And User selects "<RULE_TYPE>" as rule type and selects the created Smart list
    Then Verify the selected targeting rule "<RULE_TYPE>" for Smart list
    Then Verify the count of rule options for the selected targeting rule "<RULE_TYPE>" on the Tactic Settings page
    When User saves the settings
    Then Verify settings details are saved and user is navigated to the creatives tab
    Examples:
      | PIXEL_TYPE  | ADVERTISER       | LIST_NAME   | CP_NAME        | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME | CHANNEL          | RULE_TYPE |
      | Smart Pixel | 1Demo Advertiser | Smart_Pixel | Smart_Campaign | Regular | 12500     | Auto_Line | 70          | Auto_Tactic | Display Advanced | NPI       |

  @e2e
  Scenario Outline: Create a Conversion Pixel and target in 'Converters' targeting at Tactic level
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And User navigates to Pixels page
    When User clicks on Add Pixel button
    Then Verify the Create New Pixel panel and types of Pixel
    And User selects the "<PIXEL_TYPE>" type
    And User enters the pixel details as "<PIXEL_NAME>" "<ADVERTISER>" "<SCOPE>" "<TYPE>"
    And User saves the pixel
    Then Verify the pixel is saved successfully, search for it by name, and confirm it is displayed in the pixel list
    And User navigates to Campaign Dashboard
    And User clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    When User selects the "<CHANNEL>" as channel
    And User selects "<RULE_TYPE>" as rule type and selects the created pixel
    Then Verify the selected targeting rule "<RULE_TYPE>"
    Then Verify the count of rule options for the selected targeting rule "<RULE_TYPE>" on the Tactic Settings page
    When User saves the settings
    Then Verify settings details are saved and user is navigated to the creatives tab
    Examples:
      | PIXEL_TYPE       | PIXEL_NAME | ADVERTISER       | SCOPE  | TYPE     | CP_NAME             | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME | CHANNEL          | RULE_TYPE  |
      | Conversion Pixel | Conversion | 1Demo Advertiser | Device | Download | Conversion_Campaign | Regular | 1000      | New_Line  | 20          | New_Tactic  | Display Advanced | Converters |