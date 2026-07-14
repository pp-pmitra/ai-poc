Feature: Curated Markets - Verify Deal Import, Validation, and Tactic Assignment
  1. Verify the media type configured in Admin Setup matches the media type in the Curated Markets section for the same market
  2. Verify importing deals via template upload and validate the deals in the Deals section
  3. Verify imported deals are correctly associated and visible in the Curated Markets page for the selected market
  4. Verify floor price display logic in Curated Markets (single value for same prices, range for different prices)
  5. Verify a curated market can be selected and successfully added to a tactic, and is reflected in the targeting section
  6. Verify the error messages for mandatory fields - market name, accounts and market KPI and benchmark

  Background:
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    When User navigates to Administrative section
    And User navigates to Setup Tab

  @regression
  Scenario Outline: Verify the error messages for mandatory fields - market name, accounts and market KPI and benchmark
    When User clicks Create Curated Market link
    And User creates a curated market with only "<DESCRIPTION>"
    Examples:
      | DESCRIPTION                |
      | Curated Market Description |

  @regression
  Scenario Outline: Create a Curated Market from Admin settings by importing a deal using a template and verify its details in the Admin Deals tab and Curated Markets section of the Supply module
    When User clicks Create Curated Market link
    And User creates a curated market with details "<MARKET_NAME>", "<ACCOUNTS>", "<DESCRIPTION>", "<MARKET_KPI_BENCHMARK>"
    And User clicks "DEALS" tab
    And User clicks Import Deals button
    Then Verify Import Deal panel is opened
    And User downloads the curated market template
    And User fills the template with deal details and uploads the template
      | DEAL_NAME    | EXCHANGE  | MEDIA_TYPE | CURATOR | DEAL_PRICE | PRICING_TYPE | MPC_DEAL_TYPE                     |
      | Curated_Deal | JW Player | Display    | Client  | 234        | Fixed        | Deals that can run on any adgroup |
    Then Verify the imported deal is displayed in the Deals Tab on Admin's Curated Market page with details matching the uploaded template
    And User fetches floor price for the imported deal
    And User enables the Curated Market created
    And User clicks PulsePoint icon to navigate back to Life
    And User navigates to Curated Markets section
    And Verify Curated Market tab is displayed
    And User searches for the created Curated Market
    Then Verify the market id, media type, and floor price displayed in Curated Markets section matches the media type in Admin Setup for the same market
    Examples:
      | MARKET_NAME    | ACCOUNTS              | DESCRIPTION                | MARKET_KPI_BENCHMARK |
      | Curated_Market | automation@pulsepoint | Curated Market Description | 0.5                  |


  @regression
  Scenario Outline: Create a Curated Market from Admin settings by importing multiple deals using a template and verify its details in the Admin Deals tab and Curated Markets section of the Supply module
    When User clicks Create Curated Market link
    And User creates a curated market with details "<MARKET_NAME>", "<ACCOUNTS>", "<DESCRIPTION>", "<MARKET_KPI_BENCHMARK>"
    And User clicks "DEALS" tab
    And User clicks Import Deals button
    Then Verify Import Deal panel is opened
    And User downloads the curated market template
    And User fills the template with deal details and uploads the template
      | DEAL_NAME    | EXCHANGE  | MEDIA_TYPE | CURATOR | DEAL_PRICE | PRICING_TYPE | MPC_DEAL_TYPE                     |
      | Curated_Deal | JW Player | Display    | Client  | 234        | Fixed        | Deals that can run on any adgroup |
      | Curated_Deal | JW Player | Video      | Client  | 123        | Floor        | Deals that can run on any adgroup |
      | Curated_Deal | ADX       | Display    | Client  | 321        | Fixed        | Deals that can run on any adgroup |
      | Curated_Deal | ADX       | Video      | Client  | 111        | Floor        | Deals that can run on any adgroup |
    Then Verify the imported deal is displayed in the Deals Tab on Admin's Curated Market page with details matching the uploaded template
    And User fetches floor price for the imported deal
    And User enables the Curated Market created
    And User clicks PulsePoint icon to navigate back to Life
    And User navigates to Curated Markets section
    And Verify Curated Market tab is displayed
    And User searches for the created Curated Market
    Then Verify the market id, media type, and floor price displayed in Curated Markets section matches the media type in Admin Setup for the same market
    Examples:
      | MARKET_NAME    | ACCOUNTS              | DESCRIPTION                | MARKET_KPI_BENCHMARK |
      | Curated_Market | automation@pulsepoint | Curated Market Description | 0.5                  |