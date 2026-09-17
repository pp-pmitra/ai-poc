Feature: LIFE Regression - Targetings
  It ensures creation of a campaign with different Targeting Rules:
  1. Verify all Targeting Rules under categories and create a campaign by adding all Targeting Rules
  2. Verify list of Targeting Rules available under various LI types
  3. Verify the created targeting template is available on Targeting Templates page
  4. Verify the count of rules added for the selected targeting rule type on the tactic settings page

  @regression
  Scenario Outline: Verify all Targeting Rules under categories and create a campaign by adding all Targeting Rules
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And User clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    When User selects the "<CHANNEL>" as channel
    Then Verify targeting panel with all targeting under below categories
      | AUDIENCE ATTRIBUTE |
      | HEALTH JOURNEY     |
      | DEMOGRAPHICS       |
      | CONTEXTUAL         |
      | GEOGRAPHY          |
      | MEDIA SUPPLY       |
      | LEGAL TARGETINGS   |
    And Verify target type with respect to category
      | AUDIENCE ATTRIBUTE | Behavioral Segment,NPI,NPI Facility Affiliation,Retargeting Pixels,HCP by Specialty,Health Populations,OTC Populations,IP Address,Clickers,Converters,Keyword Populations,Practice Staff,Email,Sensitive Areas,Lookalike Audience |
      | HEALTH JOURNEY     | Health Populations+,In Condition                                                                                                                                                                                                  |
      | DEMOGRAPHICS       | Age,Ethnicity,Gender                                                                                                                                                                                                              |
      | CONTEXTUAL         | Health Pages,IAB Categories,Keywords,Language,Endemics                                                                                                                                                                            |
      | GEOGRAPHY          | Geo Targets,Geo Radius,Postal Codes,Area Codes,Weather Signals                                                                                                                                                                    |
      | MEDIA SUPPLY       | Brand Safety Profile,Brand Suitability,Browser,Curated Markets,Custom Targeting Bundle,Deal Groups,Device,Domains/Apps,IAS Context Control,Invalid Traffic,Inventory Source,Inventory Type,Operating System,Deals,Viewability     |
      | LEGAL TARGETINGS   | Legal Pages,Legal Populations                                                                                                                                                                                                     |
    And User configures targeting rules as below
      | Behavioral Segment       | AutoSegment18577650                                                   |
      | NPI                      | AutoSmartList954103283                                                |
      | HCP by Specialty         | Radiology, Aerospace Medicine                                         |
      | Health Populations       | Anesthesia and Analgesia                                              |
      | Keyword Populations      | CustomTextForKeywordPopulations, KeywordPopulationsTest               |
      | Practice Staff           | SMART_Pixel_NPI_20250701_155147                                       |
      | Health Pages             | Animal Diseases                                                       |
      | Keywords                 | Custom_Keyword, TestingKeyword, Qwerty123                             |
      | Endemics                 | Endemic                                                               |
      | Geo Targets              | New York, California                                                  |
      | Postal Codes             |                                                 123456, 10001, 987654 |
      | Weather Signals          | Below 15F degrees, Outdoor Activity                                   |
      | Brand Safety Profile     |                                                              51246802 |
      | Brand Suitability        | Unknown Brand Safety, Highly Illicit Do Not Monetize                  |
      | Browser                  | Chrome, EDGE, Opera, Safari                                           |
      | Device                   | Mobile, Tablet, Connected Device                                      |
      | Domains/Apps             | APP Regular, updaedList106043912                                      |
      | Inventory Source         | Reporttest                                                            |
      | Operating System         | Windows, macOS, Blackberry                                            |
      | Viewability              |                                                                    50 |
      | Legal Pages              | Emancipation                                                          |
      | Legal Populations        | Adoption                                                              |
      | NPI Facility Affiliation | NEW AGE DERMATOLOGY CENTER PA (NC)                                    |
      | Retargeting Pixels       | Retargeting_20250814_011101                                           |
      | OTC Populations          | Dental/Oral Care                                                      |
      | IP                       | AutoIP101602041                                                       |
      | Clickers                 | Auto_20260506_153916                                                  |
      | Email                    | AutoEmail120220716113986417                                           |
      | Sensitive Areas          | Anxiety Disorders                                                     |
      | IAB Categories           | Agriculture                                                           |
      | IAB Categories New       | Communication                                                         |
      | Language                 | English, Spanish                                                      |
      | Custom Targeting Bundle  |                                                        203397, 203396 |
      | Invalid Traffic          | Sites/Apps with Insufficient Fraud & IVT Stats, Fraudulent Sites/Apps |
      | Inventory Type           | App, Site                                                             |
      | Health Populations+      | Dental Polishing, Dental Cavity Preparation                           |
      | In Condition             | Liver Diseases                                                        |
      | Bespoke                  | AutoSegment384105361                                                  |
      | Ethnicity                | Asian, Arab                                                           |
      | Gender                   | Male                                                                  |
      | Age                      |                                                          25-29, 35-39 |
      | Geo Radius               |                                         35.5::122.42::400::California |
#      | IAS Context Control      | Pollution                                                             |
    Then Verify the configured targeting rules
    And Verify the count of rules added for the selected targeting rule type on the Tactic Settings page
    When User saves the settings
    Then Verify settings details are saved and user is navigated to the creatives tab
    And User assigns the existing creative named "<CREATIVE>", enables the tactic and saves the changes
    And User saves tactic details as a target template "Display" and verifies the template is saved successfully
    Then Verify the newly created campaign is in running state
    Then Verify the newly created campaign details in the campaign list: Campaign name, Line item name and Tactic name
    When User navigates to Targeting template page by clicking the icon from Activation section
    Then User searches and verifies the created targeting template is available on Targeting Templates page
    Examples:
      | ADVERTISER     | CP_NAME | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME | CHANNEL          | CREATIVE      |
      | 01- Advertiser | Test    | Regular |     10000 | Line      |         120 | Tactic      | Display Advanced | Auto_Creative |

  @regression
  Scenario Outline: Verify list of Targeting Rules available under Video Targeting Category and create a campaign by adding selected Targeting Rules
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And User clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>" "<LINE_ITEMS>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    When User selects the "<CHANNEL>" as channel
    Then Verify targeting panel with all targeting under below categories
      | AUDIENCE ATTRIBUTE |
      | HEALTH JOURNEY     |
      | DEMOGRAPHICS       |
      | CONTEXTUAL         |
      | GEOGRAPHY          |
      | MEDIA SUPPLY       |
      | Video              |
      | LEGAL TARGETINGS   |
    And Verify target type with respect to category
      | AUDIENCE ATTRIBUTE | Behavioral Segment,NPI,NPI Facility Affiliation,Retargeting Pixels,HCP by Specialty,Health Populations,OTC Populations,IP Address,Clickers,Converters,Keyword Populations,Practice Staff,Sensitive Areas,Lookalike Audience   |
      | HEALTH JOURNEY     | Health Populations+,In Condition                                                                                                                                                                                              |
      | DEMOGRAPHICS       | Age,Ethnicity,Gender                                                                                                                                                                                                          |
      | CONTEXTUAL         | Health Pages,IAB Categories,IAB Categories New, Keywords,Language,Endemics                                                                                                                                                    |
      | GEOGRAPHY          | Geo Targets,Geo Radius,Postal Codes,Area Codes,Weather Signals                                                                                                                                                                |
      | MEDIA SUPPLY       | Brand Safety Profile,Brand Suitability,Browser,Curated Markets,Custom Targeting Bundle,Deal Groups,Device,Domains/Apps,IAS Context Control,Invalid Traffic,Inventory Source,Inventory Type,Operating System,Deals,Viewability |
      | Video              | Video Size,Video Placement,Video Skipping                                                                                                                                                                                     |
      | LEGAL TARGETINGS   | Legal Pages,Legal Populations                                                                                                                                                                                                 |
    And User configures targeting rules as below
      | Video Size      | Small, Large                          |
      | Video Placement | Interstitial, Accompanying Content    |
      | Video Skipping  | Skippable and Non-Skippable Inventory |
    Then Verify the configured targeting rules
    And Verify the count of rules added for the selected targeting rule type on the Tactic Settings page
    When User saves the settings
    Then Verify settings details are saved and user is navigated to the creatives tab
    And User assigns the existing creative named "<CREATIVE>", enables the tactic and saves the changes
    And User saves tactic details as a target template "<LINE_ITEMS>" and verifies the template is saved successfully
    Then Verify the newly created campaign is in running state
    When User navigates to Targeting template page by clicking the icon from Activation section
    Then User searches and verifies the created targeting template is available on Targeting Templates page
    Examples:
      | ADVERTISER     | CP_NAME       | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | LINE_ITEMS | TACTIC_NAME | CHANNEL        | CREATIVE      |
      | 01- Advertiser | External_Auto | Regular |     10000 | Line      |         500 | Video      | Tactic      | Video Advanced | Auto_Creative |

  @regression
  Scenario Outline: Verify list of Targeting Rules available under Native Video Targeting Category and create a campaign by adding selected Targeting Rules
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And User clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>" "<LINE_ITEMS>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    And User clicks on Add Targeting Rule
    Then Verify targeting panel with all targeting under below categories
      | AUDIENCE ATTRIBUTE |
      | HEALTH JOURNEY     |
      | DEMOGRAPHICS       |
      | CONTEXTUAL         |
      | GEOGRAPHY          |
      | MEDIA SUPPLY       |
      | Video              |
      | LEGAL TARGETINGS   |
    And Verify target type with respect to category
      | AUDIENCE ATTRIBUTE | Behavioral Segment,NPI,NPI Facility Affiliation,Retargeting Pixels,HCP by Specialty,Health Populations,OTC Populations,IP Address,Clickers,Converters,Keyword Populations,Practice Staff,Sensitive Areas,Lookalike Audience   |
      | HEALTH JOURNEY     | Health Populations+,In Condition                                                                                                                                                                                              |
      | DEMOGRAPHICS       | Age,Ethnicity,Gender                                                                                                                                                                                                          |
      | CONTEXTUAL         | Health Pages,IAB Categories,IAB Categories New, Keywords,Language,Endemics                                                                                                                                                    |
      | GEOGRAPHY          | Geo Targets,Geo Radius,Postal Codes,Area Codes,Weather Signals                                                                                                                                                                |
      | MEDIA SUPPLY       | Brand Safety Profile,Brand Suitability,Browser,Curated Markets,Custom Targeting Bundle,Deal Groups,Device,Domains/Apps,IAS Context Control,Invalid Traffic,Inventory Source,Inventory Type,Operating System,Deals,Viewability |
      | Video              | Video Size,Video Placement                                                                                                                                                                                                    |
      | LEGAL TARGETINGS   | Legal Pages,Legal Populations                                                                                                                                                                                                 |
    And User configures targeting rules as below
      | Video Size      | Small, Large                               |
      | Video Placement | NoContent/Standalone, Accompanying Content |
    Then Verify the configured targeting rules
    And Verify the count of rules added for the selected targeting rule type on the Tactic Settings page
    When User saves the settings
    Then Verify settings details are saved and user is navigated to the creatives tab
    And User assigns the existing creative named "<CREATIVE>", enables the tactic and saves the changes
    And User saves tactic details as a target template "<LINE_ITEMS>" and verifies the template is saved successfully
    Then Verify the newly created campaign is in running state
    When User navigates to Targeting template page by clicking the icon from Activation section
    Then User searches and verifies the created targeting template is available on Targeting Templates page
    Examples:
      | ADVERTISER     | CP_NAME       | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | LINE_ITEMS   | TACTIC_NAME | CREATIVE      |
      | 01- Advertiser | External_Auto | Regular |     10000 | Line      |         500 | Native Video | Tactic      | Auto_Creative |

  @regression
  Scenario Outline: Verify list of Targeting Rules available under Search Extension Targeting Category and create a campaign by adding selected Targeting Rules
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And User clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>" "<LINE_ITEMS>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    And User clicks on Add Targeting Rule
    Then Verify targeting panel with all targeting under below categories
      | SEARCH SPECIFIC    |
      | AUDIENCE ATTRIBUTE |
      | HEALTH JOURNEY     |
      | DEMOGRAPHICS       |
      | CONTEXTUAL         |
      | GEOGRAPHY          |
      | MEDIA SUPPLY       |
      | Video              |
      | LEGAL TARGETINGS   |
    And Verify target type with respect to category
      | SEARCH SPECIFIC    | Search Keywords                                                                                                                                                                                                               |
      | AUDIENCE ATTRIBUTE | Behavioral Segment,NPI,NPI Facility Affiliation,Retargeting Pixels,HCP by Specialty,Health Populations,OTC Populations,IP Address,Clickers,Converters,Keyword Populations,Practice Staff,Sensitive Areas,Lookalike Audience   |
      | HEALTH JOURNEY     | Health Populations+,In Condition                                                                                                                                                                                              |
      | DEMOGRAPHICS       | Age,Ethnicity,Gender                                                                                                                                                                                                          |
      | CONTEXTUAL         | Health Pages,IAB Categories,IAB Categories New, Keywords,Language,Endemics                                                                                                                                                    |
      | GEOGRAPHY          | Geo Targets,Geo Radius,Postal Codes,Area Codes,Weather Signals                                                                                                                                                                |
      | MEDIA SUPPLY       | Brand Safety Profile,Brand Suitability,Browser,Curated Markets,Custom Targeting Bundle,Deal Groups,Device,Domains/Apps,IAS Context Control,Invalid Traffic,Inventory Source,Inventory Type,Operating System,Deals,Viewability |
      | LEGAL TARGETINGS   | Legal Pages,Legal Populations                                                                                                                                                                                                 |
    And User configures targeting rules as below
      | Search Keywords | Pandemic, Intestine |
    Then Verify the configured targeting rules
    When User saves the settings
    Then Verify settings details are saved and user is navigated to the creatives tab
    And User assigns the existing creative named "<CREATIVE>", enables the tactic and saves the changes
    And User saves tactic details as a target template "<LINE_ITEMS>" and verifies the template is saved successfully
    Then Verify the newly created campaign is in running state
    When User navigates to Targeting template page by clicking the icon from Activation section
    Then User searches and verifies the created targeting template is available on Targeting Templates page
    Examples:
      | ADVERTISER     | CP_NAME       | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | LINE_ITEMS       | TACTIC_NAME | CREATIVE      |
      | 01- Advertiser | External_Auto | Regular |     10000 | Line      |         500 | Search Extension | Tactic      | Auto_Creative |

  @regression
  Scenario Outline: Verify list of Targeting Rules available under DOOH Targeting Category and create a campaign by adding selected Targeting Rules
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And User clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>" "<LINE_ITEMS>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    And User clicks on Add Targeting Rule
    Then Verify targeting panel with all targeting under below categories
      | AUDIENCE ATTRIBUTE |
      | GEOGRAPHY          |
      | MEDIA SUPPLY       |
    And Verify target type with respect to category
      | AUDIENCE ATTRIBUTE | IP Address                                                                                            |
      | GEOGRAPHY          | Geo Targets,Geo Radius,Postal Codes,Area Codes,Venue Type,Weather Signals                             |
      | MEDIA SUPPLY       | Audience Multiplier,Curated Markets,Custom Targeting Bundle,Deal Groups,Device,Inventory Source,Deals |
    And User configures targeting rules as below
      | Venue Type              | Transit, Retail, Office Buildings |
      | Custom Targeting Bundle |                    203397, 203396 |
      | Audience Multiplier     |                             6-500 |
    Then Verify the configured targeting rules
    And Verify the count of rules added for the selected targeting rule type on the Tactic Settings page
    When User saves the settings
    Then Verify settings details are saved and user is navigated to the creatives tab
    And User assigns the existing creative named "<CREATIVE>", enables the tactic and saves the changes
    And User saves tactic details as a target template "<LINE_ITEMS>" and verifies the template is saved successfully
    Then Verify the newly created campaign is in running state
    When User navigates to Targeting template page by clicking the icon from Activation section
    Then User searches and verifies the created targeting template is available on Targeting Templates page
    Examples:
      | ADVERTISER     | CP_NAME       | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | LINE_ITEMS | TACTIC_NAME | CREATIVE      |
      | 01- Advertiser | Campaign_DOOH | Regular |     10000 | Line      |         500 | DOOH       | Tactic      | Auto_Creative |

  @regression
  Scenario Outline: Verify list of Targeting Rules available under Audio Targeting Category and create a campaign by adding selected Targeting Rules
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And User clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>" "<LINE_ITEMS>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    And User clicks on Add Targeting Rule
    Then Verify targeting panel with all targeting under below categories
      | AUDIENCE ATTRIBUTE |
      | HEALTH JOURNEY     |
      | DEMOGRAPHICS       |
      | GEOGRAPHY          |
      | MEDIA SUPPLY       |
      | LEGAL TARGETINGS   |
    And Verify target type with respect to category
      | AUDIENCE ATTRIBUTE | Behavioral Segment,NPI,NPI Facility Affiliation,Retargeting Pixels,HCP by Specialty,Health Populations,OTC Populations,IP Address,Clickers,Converters,Keyword Populations,Practice Staff,Sensitive Areas,Lookalike Audience |
      | HEALTH JOURNEY     | Health Populations+,Bespoke,In Condition                                                                                                                                                                                    |
      | DEMOGRAPHICS       | Age,Ethnicity,Gender                                                                                                                                                                                                        |
      | GEOGRAPHY          | Geo Targets,Geo Radius,Postal Codes,Area Codes,Weather Signals                                                                                                                                                              |
      | MEDIA SUPPLY       | Brand Safety Profile,Brand Suitability,Browser,Curated Markets,Custom Targeting Bundle,Deal Groups,Device,Domains/Apps,IAS Context Control,Invalid Traffic,Inventory Source,Inventory Type,Operating System,Deals           |
      | LEGAL TARGETINGS   | Legal Populations                                                                                                                                                                                                           |
    And User configures targeting rules as below
      | Clickers | DomainList_Campaign_20260503_001635, TargetingTemplate_20260503_003625 |
      | Age      |                                                    18-24, 50-54, 60-64 |
    Then Verify the configured targeting rules
    And Verify the count of rules added for the selected targeting rule type on the Tactic Settings page
    When User saves the settings
    Then Verify settings details are saved and user is navigated to the creatives tab
    And User assigns the existing creative named "<CREATIVE>", enables the tactic and saves the changes
    And User saves tactic details as a target template "<LINE_ITEMS>" and verifies the template is saved successfully
    Then Verify the newly created campaign is in running state
    When User navigates to Targeting template page by clicking the icon from Activation section
    Then User searches and verifies the created targeting template is available on Targeting Templates page
    Examples:
      | ADVERTISER     | CP_NAME        | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | LINE_ITEMS | TACTIC_NAME | CREATIVE      |
      | 01- Advertiser | Campaign_Audio | Regular |     10000 | Line      |         500 | Audio      | Tactic      | Auto_Creative |
