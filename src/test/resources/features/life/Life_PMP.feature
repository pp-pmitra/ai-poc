Feature: Life PMP Regression - Verify Private and Life MarketPlace Deals Creation and Assignment
  1. Verify Private Deals Tab
  2. Verify Life Marketplace Deals Tab
  3. Addition of Private Deals and assigned to a tactic when Only Target Applied Deals toggle is ON
  4. Addition of Private Deals and assigned to a tactic when Only Target Applied Deals toggle is OFF

  Background:
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"
    And User clicks on Create Campaign
    When User enters the campaign details as "01- Advertiser" "Auto" "Regular" "20000" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "Line" "500", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "Tactic" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    And User selects the "Display Advanced" as channel
    And User selects "Behavioral Segment" as rule type and configures the targeting rules, and saves the settings
    Then Verify settings details are saved and user is navigated to the creatives tab

  @regression
  Scenario: Verify Private Deals Tab
    When User clicks Tactic Setting tab
    Then User should navigate to respective Tactic Setting tab
    When User add new targeting rule for Rule Type "Deals"
    Then user should navigate to PMP Deals Panel
    When User clicks "Private" Deals Tab
    Then User should see Add New Deal button, filters such as Exchange, Search
    And Verify that "Active" and "Archived" buttons are available and by default "Active" button is selected
    When User enters below details in respective search field, verify that the deal list appears based on the selected filters
      | SearchByName     | Deal                  |
      | SearchByExchange | PulsePoint, JW Player |

  @regression
  Scenario: Verify Life Marketplace Deals Tab
    When User clicks Tactic Setting tab
    Then User should navigate to respective Tactic Setting tab
    When User add new targeting rule for Rule Type "Deals"
    Then user should navigate to PMP Deals Panel
    When User clicks "Life Marketplace Deals" Deals Tab
    And Verify Edit icon availability for the deals listed under "Life Marketplace" Deals tab
    And Verify that "Premium Publisher" should not display in deals listing under Life Marketplace Deals tab
    When User enters below details in respective search field, verify that the deal list appears based on the selected filters
      | SearchByName     | Deal     |
      | SearchByExchange | Pubmatic |

  @regression
  Scenario Outline: Add New Private Deals with deal price type "<DEAL_PRICE_TYPE>", pricing strategy "<PRICING_STRATEGY>" and assign to a tactic
    When User clicks Tactic Setting tab
    Then User should navigate to respective Tactic Setting tab
    When User add new targeting rule for Rule Type "Deals"
    Then user should navigate to PMP Deals Panel
    When User clicks "Private" Deals Tab
    And User clicks on Add New Deal button
    And Verify Deal Type field is available with default value as "PMP"
    And Verify Curator field is available with default value as "Client"
    And Verify Pricing Type field is available with default value as "Floor"
    Then New Deal panel should open and user should be able to add new deal with details "<EXCHANGE_TYPE>", "<DEAL_ID>", "<DEAL_NAME>", "<MEDIA_TYPE>", "<ADVERTISER>", "<DEAL_PRICE_TYPE>", "<PRICE>", "<CURATOR>"
    When User searches the deal and assign it from the deal list
    Then Verify Edit icon availability for the deals listed under "Private" Deals tab
    And Verify Clearing Price field is available and fetch the tool-tip details on hover for the field
    Then Selected Deals should appear in Applied Deals panel
    When User clicks on OK button
    Then Deal details should appear on Tactic Settings tab under Targeting section, Curated Markets and Deals section depending on toggle button status
    And Verify Pricing Strategy is editable and update it with "<PRICING_STRATEGY>" and "<VALUE>" for Deals present in Curated Markets and Deals section
    And Verify user can add new "Private" deals by clicking Add Deal button present in Curated Markets and Deals section using details "<EXCHANGE_TYPE>", "<DEAL_ID>", "<DEAL_NAME>", "<MEDIA_TYPE>", "<ADVERTISER>", "<DEAL_PRICE_TYPE>", "<PRICE>", "<CURATOR>"
    And Verify Base Bid Price "<BASE_BID_PRICE>" and Max Bid Price "<MAX_BID_PRICE>" fields are editable when deals are targeted
    When User clicks Save button from Tactic Setting tab
    Then Deals should get assigned to the Tactic
    Examples:
      | EXCHANGE_TYPE | DEAL_ID | DEAL_NAME  | MEDIA_TYPE                 | DEAL_PRICE_TYPE | PRICE | BASE_BID_PRICE | MAX_BID_PRICE | ADVERTISER     | CURATOR                          | PRICING_STRATEGY | VALUE |
      | JW Player     | Deal_   | Deal_Name_ | Display (All), Video (All) | Fixed           | 230   | 34             | 60            | 01- Advertiser | PulsePoint (Direct Integrations) | Flat             | 35    |
      | JW Player     | Deal_   | Deal_Name_ | Display (All), Video (All) | Floor           | 230   | 34             | 60            | 01- Advertiser | PulsePoint (Direct Integrations) | Floor+           |       |
      | JW Player     | Deal_   | Deal_Name_ | Display (All), Video (All) | Fixed           | 230   | 34             | 60            | 01- Advertiser | PulsePoint (Direct Integrations) | Default          |       |

  @regression
  Scenario Outline: Verify active deal moves to archived while campaign is not running state
    When User clicks Tactic Setting tab
    Then User should navigate to respective Tactic Setting tab
    When User add new targeting rule for Rule Type "Deals"
    Then user should navigate to PMP Deals Panel
    When User clicks "Private" Deals Tab
    And User clicks on Add New Deal button
    And Verify Deal Type field is available with default value as "PMP"
    And Verify Curator field is available with default value as "Client"
    And Verify Pricing Type field is available with default value as "Floor"
    Then New Deal panel should open and user should be able to add new deal with details "<EXCHANGE_TYPE>", "<DEAL_ID>", "<DEAL_NAME>", "<MEDIA_TYPE>", "<ADVERTISER>", "<DEAL_PRICE_TYPE>", "<PRICE>", "<CURATOR>"
    When User searches the deal and assign it from the deal list
    And User clicks 3 dot menu and selects Archive button for the active deal from the deal listing
    And Verify Archive option is available based on the campaign state
    And User clicks "Archived" button from the search section of deal listing page
    Then Verify that the deal is moved to archived deal section
    Examples:
      | EXCHANGE_TYPE | DEAL_ID | DEAL_NAME  | MEDIA_TYPE                 | DEAL_PRICE_TYPE | PRICE | ADVERTISER     | CURATOR                          |
      | JW Player     | Deal_   | Deal_Name_ | Display (All), Video (All) | Fixed           | 230   | 01- Advertiser | PulsePoint (Direct Integrations) |

  @regression
  Scenario Outline: Verify active deal should not be deleted while campaign is running state
    And User assigns the existing creative named "<CREATIVE>", enables the tactic and saves the changes
    When User clicks Tactic Setting tab
    Then User should navigate to respective Tactic Setting tab
    When User add new targeting rule for Rule Type "Deals"
    Then user should navigate to PMP Deals Panel
    When User clicks "Private" Deals Tab
    And User clicks on Add New Deal button
    Then New Deal panel should open and user should be able to add new deal with details "<EXCHANGE_TYPE>", "<DEAL_ID>", "<DEAL_NAME>", "<MEDIA_TYPE>", "<ADVERTISER>", "<DEAL_PRICE_TYPE>", "<PRICE>", "<CURATOR>"
    When User searches the deal and assign it from the deal list
    When User clicks on OK button
    Then Deal details should appear on Tactic Settings tab under Targeting section, Curated Markets and Deals section depending on toggle button status
    And User saves the settings
    When User add new targeting rule for Rule Type "Deals"
    Then user should navigate to PMP Deals Panel
    When User searches the deal and assign it from the deal list
    And User clicks 3 dot menu and selects Archive button for the active deal from the deal listing
    And Verify Archive option is available based on the campaign state
    And Verify the Tactic Link is available in the confirmation pop-up
    And Verify the Tactic Link is clickable and navigates to the respective tactic page
    Examples:
      | EXCHANGE_TYPE | DEAL_ID | DEAL_NAME  | MEDIA_TYPE                 | DEAL_PRICE_TYPE | PRICE | ADVERTISER     | CURATOR                          | CREATIVE      |
      | JW Player     | Deal_   | Deal_Name_ | Display (All), Video (All) | Fixed           | 230   | 01- Advertiser | PulsePoint (Direct Integrations) | Auto_Creative |

  @regression
  Scenario Outline: Verify that after deleting an active deal from targeting, the user is able to delete the deal while the campaign is in a running state
    And User assigns the existing creative named "<CREATIVE>", enables the tactic and saves the changes
    When User clicks Tactic Setting tab
    Then User should navigate to respective Tactic Setting tab
    When User add new targeting rule for Rule Type "Deals"
    Then user should navigate to PMP Deals Panel
    When User clicks "Private" Deals Tab
    And User clicks on Add New Deal button
    Then New Deal panel should open and user should be able to add new deal with details "<EXCHANGE_TYPE>", "<DEAL_ID>", "<DEAL_NAME>", "<MEDIA_TYPE>", "<ADVERTISER>", "<DEAL_PRICE_TYPE>", "<PRICE>", "<CURATOR>"
    When User searches the deal and assign it from the deal list
    When User clicks on OK button
    Then Deal details should appear on Tactic Settings tab under Targeting section, Curated Markets and Deals section depending on toggle button status
    And User saves the settings
    When User add new targeting rule for Rule Type "Deals"
    Then user should navigate to PMP Deals Panel
    When User searches the deal and assign it from the deal list
    And User clicks 3 dot menu and selects Archive button for the active deal from the deal listing
    And Verify Archive option is available based on the campaign state
    And Verify the Tactic Link is available in the confirmation pop-up
    And Verify the Tactic Link is clickable and navigates to the respective tactic page
    When User searches the deal and assign it from the deal list
    And User unassigns active deal from the applied deals section of All Deals tab
    When User clicks on OK button
    And User saves the settings
    When User add new targeting rule for Rule Type "Deals"
    Then user should navigate to PMP Deals Panel
    When User searches the deal and assign it from the deal list
    And User clicks 3 dot menu and selects Archive button for the active deal from the deal listing
    And Verify Archive option is available based on the campaign state
    And User clicks "Archived" button from the search section of deal listing page
    Then Verify that the deal is moved to archived deal section
    Examples:
      | EXCHANGE_TYPE | DEAL_ID | DEAL_NAME  | MEDIA_TYPE                 | DEAL_PRICE_TYPE | PRICE | ADVERTISER     | CURATOR                          | CREATIVE      |
      | JW Player     | Deal_   | Deal_Name_ | Display (All), Video (All) | Fixed           | 230   | 01- Advertiser | PulsePoint (Direct Integrations) | Auto_Creative |

    # Source: QA-1849
  @todo
  Scenario: Inventory Breakdown affordance opens a panel scoped to the deal added via the Tactic Deals targeting rule
    When User clicks Tactic Setting tab
    Then User should navigate to respective Tactic Setting tab
    When User add new targeting rule for Rule Type "Deals"
    Then user should navigate to PMP Deals Panel
    When User clicks "Private" Deals Tab
    And User searches the deal and assign it from the deal list
  # Framework Gap: Requires the Inventory Breakdown affordance hook and panel page object in LifeSteps.java (new feature, not yet implemented)
    When User clicks the Inventory Breakdown icon for that deal
    Then The Inventory Breakdown panel opens scoped to the deal added to the tactic, showing Display Inventory and Video Inventory views

# Source: QA-1849
  @todo
  Scenario Outline: Inventory Breakdown affordance opens a panel scoped to the correct deal from the remaining entry surfaces
  # Framework Gap: Requires navigation hook for the "<SURFACE>" entry point in LifeSteps.java (no existing page object reaches this surface)
    Given User navigates to "<SURFACE>" and reaches "<CONTEXT>" with an active deal in the list
  # Framework Gap: Requires the Inventory Breakdown affordance hook and panel page object in LifeSteps.java (new feature, not yet implemented)
    When User clicks the Inventory Breakdown icon for that deal
    Then The Inventory Breakdown panel opens scoped to "<SCOPE>", showing Display Inventory and Video Inventory views
    Examples:
      | SURFACE                    | CONTEXT                                   | SCOPE                                   |
      | Supply > Deals              | the Supply deals library view             | the selected deal                       |
      | Targeting Template > Deals  | a deal selected in a Deals targeting rule | the deal selected in the template       |
      | Media Planner > Deals       | a deal selected for a new media plan      | the deal selected for the media plan    |
      | Deal Group > Add Deals      | a single expanded deal within the group   | the expanded deal, not the whole group  |

# Source: QA-1849
  @todo
  Scenario: Inventory Breakdown panel exposes both Display and Video Inventory views without losing deal context
  # Framework Gap: Requires navigation hook for Supply > Deals in LifeSteps.java
    Given User navigates to "Supply > Deals" and opens the Inventory Breakdown panel for a deal
  # Framework Gap: Requires Display/Video Inventory view-toggle hook in LifeSteps.java
    Then A Display Inventory view and a Video Inventory view are both visible and selectable
  # Framework Gap: Requires view-toggle + deal-context retention check in LifeSteps.java
    When User switches between Display Inventory and Video Inventory twice
    Then The figures shown remain scoped to the same deal in both views

# Source: QA-1849
  @todo
  Scenario Outline: Timeframe filter exposes exactly three windows and updates the displayed figures
  # Framework Gap: Requires navigation hook for Supply > Deals in LifeSteps.java
    Given User navigates to "Supply > Deals" and opens the Inventory Breakdown panel for a deal with known inventory history
  # Framework Gap: Requires Timeframe control hook in LifeSteps.java
    Then The Timeframe control lists exactly "Yesterday", "Last 7 Days", and "Last 30 Days", no more and no fewer
  # Framework Gap: Requires Timeframe selection + figure-refresh hook in LifeSteps.java
    When User selects "<TIMEFRAME>"
    Then Displayed inventory reflects "<WINDOW>"
    Examples:
      | TIMEFRAME     | WINDOW                              |
      | Yesterday     | the prior calendar day only         |
      | Last 7 Days   | the trailing 7-day window           |
      | Last 30 Days  | the trailing 30-day window          |

# Source: QA-1849, GAP-3, AMB-4
  @todo
  Scenario: Timeframe boundary handling and first-open default require product confirmation before automated pass/fail
  # Framework Gap: Requires navigation hook for Supply > Deals in LifeSteps.java
    Given User navigates to "Supply > Deals" and opens the Inventory Breakdown panel in a fresh session
  # Framework Gap: Requires Timeframe default-state read hook in LifeSteps.java
    Then Document which Timeframe is pre-selected by default, since AMB-4 leaves this unspecified
  # Framework Gap: Requires inventory record seeded exactly on the 7x24h boundary plus a Timeframe boundary read hook
    When An inventory record is timestamped exactly 7x24 hours before now and User selects "Last 7 Days"
    Then Document whether that record is included or excluded, since GAP-3 leaves the exact window undefined

# Source: QA-1849
  @todo
  Scenario Outline: Video Min/Max Duration formatting applies the greater-than-120-second rule at its boundaries
  # Framework Gap: Requires navigation hook for Supply > Deals in LifeSteps.java
    Given User navigates to "Supply > Deals" and opens Video Inventory for a deal with an item of max duration "<MAX_DURATION>"
  # Framework Gap: Requires Min/Max Duration field-read hook in LifeSteps.java
    Then The Max Duration field reads "<MAX_DISPLAY>"
    Examples:
      | MAX_DURATION | MAX_DISPLAY |
      | 150s         | >120s       |
      | 90s          | 90s         |
      | 120s         | 120s        |
      | 121s         | >120s       |

# Source: QA-1849, AMB-1
  @todo
  Scenario: Min Duration display when Max Duration crosses the 120-second threshold requires product confirmation
  # Framework Gap: Requires navigation hook for Supply > Deals in LifeSteps.java
    Given User navigates to "Supply > Deals" and opens Video Inventory for a deal with Min Duration "30s" and Max Duration "150s"
  # Framework Gap: Requires Min Duration field-read hook in LifeSteps.java
    Then Document whether Min Duration reads "30s" independently or ">120s" forced by Max, since AMB-1 leaves this unspecified
  # Framework Gap: Requires Min/Max Duration field-read hook in LifeSteps.java
    When User views a deal with Min Duration "20s" and Max Duration "100s"
    Then Both Min Duration and Max Duration display their exact values and ">120s" appears for neither

# Source: QA-1849, GAP-1, GAP-2, GAP-4, GAP-5
  @todo
  Scenario: Deal Group breakdown scopes to the individually expanded deal and handles empty, error, and permission states
  # Framework Gap: Requires navigation hook for Deal Group > Add Deals in LifeSteps.java
    Given User navigates to "Deal Group > Add Deals" with 3 deals of differing inventory profiles added to the group
  # Framework Gap: Requires per-deal expand + Inventory Breakdown scoping hook in LifeSteps.java
    When User expands each deal individually and opens Inventory Breakdown for it
    Then Each deal's breakdown reflects only that deal's data, with no bleed-through from a previously expanded deal
  # Framework Gap: Requires zero-inventory empty-state hook in LifeSteps.java
    When User opens Inventory Breakdown for a deal with zero available inventory in the selected Timeframe
    Then Document the empty-state shown, since GAP-1 leaves the exact empty-state behavior unspecified
  # Framework Gap: Requires forced API-error simulation + error-state read hook in LifeSteps.java
    When The inventory-aggregation call is forced to error or time out
    Then Document the error state shown, since GAP-4 leaves the exact failure state unspecified
  # Framework Gap: Requires role-restricted test account + permission-check hook in LifeSteps.java
    When A user without deal-management permissions attempts to open Inventory Breakdown
    Then Document the access behavior against the intended role scope, since GAP-5 leaves this unspecified

# Source: QA-1849, AMB-2, AMB-3
  @todo
  Scenario: Inventory Breakdown figures stay consistent across surfaces, views, and rapid Timeframe changes
  # Framework Gap: Requires navigation hooks for Supply > Deals and Media Planner > Deals, plus cross-surface figure comparison, in LifeSteps.java
    Given The same deal is opened via "Supply > Deals" and via "Media Planner > Deals", both set to "Last 7 Days"
    Then Display Inventory and Video Inventory figures match exactly across both surfaces
  # Framework Gap: Requires Timeframe-scope-across-views read hook in LifeSteps.java
    When User sets Timeframe to "Last 30 Days" then switches between Display Inventory and Video Inventory
    Then Document whether Timeframe persists across the switch or resets per view, since AMB-3 leaves this unspecified
  # Framework Gap: Requires mixed-creative-type deal fixture and category-read hook in LifeSteps.java
    When User views a deal with both a video creative and a static creative in inventory
    Then Document how each item is categorized, since AMB-2 leaves the Display/Video boundary unspecified
  # Framework Gap: Requires rapid-Timeframe-switch simulation and stale-response-guard read hook in LifeSteps.java
    When User selects "Last 30 Days" immediately after "Last 7 Days", before the first response returns
    Then The panel reflects only the most recently selected Timeframe once loading completes