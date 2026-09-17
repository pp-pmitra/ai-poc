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
      | JW Player     | Deal_   | Deal_Name_ | Display (All), Video (All) | Fixed           |   230 |             34 |            60 | 01- Advertiser | PulsePoint (Direct Integrations) | Flat             |    35 |
      | JW Player     | Deal_   | Deal_Name_ | Display (All), Video (All) | Floor           |   230 |             34 |            60 | 01- Advertiser | PulsePoint (Direct Integrations) | Floor+           |       |
      | JW Player     | Deal_   | Deal_Name_ | Display (All), Video (All) | Fixed           |   230 |             34 |            60 | 01- Advertiser | PulsePoint (Direct Integrations) | Default          |       |

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
      | JW Player     | Deal_   | Deal_Name_ | Display (All), Video (All) | Fixed           |   230 | 01- Advertiser | PulsePoint (Direct Integrations) |

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
      | JW Player     | Deal_   | Deal_Name_ | Display (All), Video (All) | Fixed           |   230 | 01- Advertiser | PulsePoint (Direct Integrations) | Auto_Creative |

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
      | JW Player     | Deal_   | Deal_Name_ | Display (All), Video (All) | Fixed           |   230 | 01- Advertiser | PulsePoint (Direct Integrations) | Auto_Creative |

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

  # Source: ET-25051
  @todo
  Scenario Outline: Ad size distribution shows the top 10 sizes plus an Others bucket at and below the boundary
    When User clicks Tactic Setting tab
    Then User should navigate to respective Tactic Setting tab
    When User add new targeting rule for Rule Type "Deals"
    Then user should navigate to PMP Deals Panel
    When User searches the deal and assign it from the deal list
    # Framework Gap: Requires an ad-size-distribution read hook (top 10 + Others bucket) in LifeSteps.java
    And User opens the Inventory Breakdown for a deal with "<DISTINCT_SIZES>" distinct ad sizes
    Then The Display Inventory ad size section shows "<EXPECTED>"
    Examples:
      | DISTINCT_SIZES | EXPECTED                                    |
      | more than 10    | the top 10 sizes plus an Others bucket      |
      | exactly 10      | all 10 sizes with no Others bucket          |
      | fewer than 10   | only the sizes that exist, no Others bucket |

  # Source: ET-25051
  @todo
  Scenario: Video Inventory shows the VAST versus VPAID tag-type split alongside duration
    When User clicks Tactic Setting tab
    Then User should navigate to respective Tactic Setting tab
    When User add new targeting rule for Rule Type "Deals"
    Then user should navigate to PMP Deals Panel
    When User searches the deal and assign it from the deal list
    # Framework Gap: Requires a VAST/VPAID tag-type split read hook in LifeSteps.java
    And User opens the Inventory Breakdown and views Video Inventory
    Then The minimum and maximum video duration are shown, and the VAST and VPAID percentages are shown and sum to 100

  # Source: ET-25051
  @todo
  Scenario: Top 10 domains and app bundles are shown for every media type, and a display-only deal shows no video or audio section
    When User clicks Tactic Setting tab
    Then User should navigate to respective Tactic Setting tab
    When User add new targeting rule for Rule Type "Deals"
    Then user should navigate to PMP Deals Panel
    When User searches the deal and assign it from the deal list
    # Framework Gap: Requires top-10-domains and top-10-app-bundles read hooks in LifeSteps.java
    And User opens the Inventory Breakdown
    Then The top 10 domains and the top 10 app bundles are shown regardless of media type
    # Framework Gap: Requires a display-only deal fixture and a media-type-section-visibility read hook
    Given The deal opened is display-only, with no video or audio inventory
    Then No Video Inventory or Audio Inventory section is shown, or each shows an explicit empty state rather than the wrong media type's data

  # Source: ET-25051
  @todo
  Scenario: The breakdown figures match the underlying deal delivery statistics
    # Framework Gap: Requires a direct read hook against dealdailystats/dealdomaindailystats (or an equivalent reporting API) to compare against the panel, in LifeSteps.java
    Given A deal has known recorded statistics in dealdailystats and dealdomaindailystats for a given timeframe
    When User opens the Inventory Breakdown for that deal and timeframe
    Then The percentages and top-10 lists shown match the underlying statistics for that deal and timeframe

  # Source: ET-25051
  @todo
  Scenario: Opening and closing the Inventory Breakdown leaves the underlying deal selection unchanged
    When User clicks Tactic Setting tab
    Then User should navigate to respective Tactic Setting tab
    When User add new targeting rule for Rule Type "Deals"
    Then user should navigate to PMP Deals Panel
    When User searches the deal and assign it from the deal list
    # Framework Gap: Requires an Inventory Breakdown open/close hook that doesn't disturb deal-selection state, in LifeSteps.java
    And User opens the Inventory Breakdown for the selected deal and then closes it
    Then The deal remains selected exactly as it was before the panel was opened

  # Source: ET-25051
  @todo
  Scenario: The Inventory Breakdown does not regress the Media Planner view, deal labels, or reporting for deals with nothing applied
    # Framework Gap: Requires a before/after Media Planner behavior comparison hook in LifeSteps.java
    Given The Media Planner deals view is used as it was before this release
    Then Its existing behaviour is unaffected by the added Inventory Breakdown control
    # Framework Gap: Requires a deal-label regression check across the deal screens
    And Deal detail labels and figures elsewhere on the deal screens remain correct after the breakdown is added
    # Framework Gap: Requires a no-deals-applied fixture and a curated-market/PMP-attribution read hook
    Given A deal has no deals applied to it
    Then Its breakdown shows no curated market or PMP inventory attribution
    # Framework Gap: Requires a same-deal multi-timeframe percentage-consistency read hook
    And The breakdown percentages for one deal are internally consistent across all three timeframes

  # Source: ET-25054
  @todo
  Scenario: A no-avails warning appears at tactic level for a directly targeted deal, scoped to the 1-day and 7-day windows
    # Framework Gap: Requires an avails-history fixture (deal with no avails yesterday / past 7 days) and a tactic-level warning indicator page object
    Given A tactic directly targets an active deal
    When That deal had no avails yesterday
    Then A warning appears at tactic level and states that the 1-day window triggered
    When That deal has had no avails for the whole 7-day period including yesterday
    Then The warning states that both windows triggered
    When The deal had avails yesterday and across the past 7 days
    Then No warning appears

  # Source: ET-25054
  @todo
  Scenario Outline: No-avails warnings are suppressed for deals that are not actually eligible to deliver
    # Framework Gap: Requires fixtures for out-of-flight-date, disabled, and not-yet-started deals with no avails
    Given A deal has no avails but is "<CONDITION>"
    Then No warning is triggered
    Examples:
      | CONDITION                       |
      | outside its start and end dates |
      | disabled                        |
      | not yet started                 |

  # Source: ET-25054
  @todo
  Scenario: Deal group no-avails warnings surface every flagged deal next to the group name
    # Framework Gap: Requires a targeted-deal-group fixture with a mix of flagged and healthy deals
    Given A targeted deal group contains a deal with no recent avails
    Then A warning appears next to the deal group name without needing to expand the group
    When More than one deal inside the group has no recent avails
    Then All flagged deals are surfaced, not only the first, and the count in the deal section warning matches the number of flagged deals

  # Source: ET-25054
  @todo
  Scenario: An all-deals-unavailable state shows an orange box alongside the per-deal warnings
    # Framework Gap: Requires an all-unavailable fixture, both for a multi-deal group and for a tactic targeting exactly one deal
    Given Every deal applied to a tactic is unavailable
    Then An orange box warning appears
    And Both the per-deal warning and the all-unavailable orange box appear together where the design requires it

  # Source: ET-25054
  @todo
  Scenario: The no-avails warning appears under both inventory targeting modes
    Given A tactic's inventory targeting is set to Selected Inventory Only
    Then The no-avails warning is surfaced when applicable
    Given A tactic's inventory targeting is Selected Inventory plus Open Exchange
    Then The no-avails warning is surfaced when applicable

  # Source: ET-25054
  @todo
  Scenario: A tactic showing a no-avails warning can still be saved and activated, and the warning clears once avails resume
    Given A tactic shows a no-avails warning
    Then The tactic can still be saved and can still be activated
    Given Every deal on a tactic is unavailable
    Then The tactic can still be saved and activated
    When A flagged deal starts recording avails again
    Then Its warning disappears, while a warning on another deal in the same group remains until that deal also recovers
    When A flagged deal is removed from the tactic
    Then The warning updates accordingly

  # Source: ET-25054
  @todo
  Scenario: No-avails warnings coexist with the existing curated market indicator, and a data-lookup failure does not flag every deal
    # Framework Gap: Requires the existing curated-market-incompatibility indicator located and confirmed in the codebase before a coexistence assertion can be written
    Given A deal group carries both a no-avails warning and the existing curated markets incompatibility indicator
    Then Both render correctly and neither suppresses the other
    # Framework Gap: Requires an avails-data-retrieval-failure fixture
    When The avails data retrieval fails
    Then Every applied deal is not automatically flagged as a false positive
    # Framework Gap: Requires a mixed active/inactive deal-group fixture, and a fixture combining a direct deal with a deal group on the same tactic
    Given A deal group holds both an inactive deal with no avails and an active deal with no avails
    Then The warning reflects only the active, eligible deal
    Given A tactic targets both a direct deal and a deal group
    Then Flagged deals from both are surfaced
    # Framework Gap: Requires a hidden/inaccessible-deal fixture, consistent with the unshared-deal handling in ET-25077
    Given A hidden or inaccessible deal is applied to a tactic
    Then No false no-avails warning appears for it beyond what the unshared-deal handling already covers
    And No false no-avails warning appears on a tactic whose deals all have healthy avails