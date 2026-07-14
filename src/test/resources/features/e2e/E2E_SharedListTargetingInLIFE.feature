Feature: End to End workflow for Shared Lists - Domain, App Bundles, Keywords and Email Lists creation and target it at Tactic level
  It covers below points
  1. Creation of Shared List - Domain, App Bundles, Keywords and Email List
  2. Targeting the created List at Tactic level

  Background:
    #1
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"

  @e2e
  Scenario Outline: Create Domain List by manually entering domain names and target in 'Domains/Apps' targeting at Tactic level
    #2
    Given User navigates to the "Domain & App Lists" page
    And Verify that the search option is present on the "Domain/App Lists" tab
    When User clicks on Create New List
    And User selects the "Domains" radio button from create new list page
    #3
    And User enters "<LIST_NAME>" in the List Name field
    And Verify that when "<DOMAIN_NAMES>" names are specified manually, the option to upload a file disappears
    And Verify that the user is able to create a "Domains" list by specifying names manually
    And Verify that the counter on the left displays the correct value for each list in the navigation panel
    #4
    And Navigate to Campaign Dashboard and clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    #5
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    #6
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    And User selects the "<CHANNEL>" as channel
    And User add and configure "<RULE_TYPE>" targeting rule and verify list is displayed in the targeting rule
    And User saves the rule configured in the tactic
    #7
    Then Verify that the "<RULE_TYPE>" rule is added to the tactic and retrieve the count of selected lists
    And Verify that the selected list is displayed in the targeting rule and retrieve the total count of targeted items
    And User saves the targeting
    Examples:
      | LIST_NAME | DOMAIN_NAMES          | ADVERTISER     | CP_NAME             | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME | CHANNEL          | RULE_TYPE    |
      | Domain    | amazon.com, apple.com | 01- Advertiser | DomainList_Campaign | Regular | 20000     | Line      | 500         | Tactic      | Display Advanced | Domains/Apps |

  @e2e
  Scenario Outline: Create App Bundle List by uploading AppBundles names from a file and target in 'Domains/Apps' targeting at Tactic level
    #2
    Given User navigates to the "Domain & App Lists" page
    And Verify that the search option is present on the "Domain/App Lists" tab
    When User clicks on Create New List
    And User selects the "App Bundles" radio button from create new list page
    #3
    And Verify that when enters "<LIST_NAME>" and upload file "<UPLOAD_FILENAME1>" option is selected, the text area to direct enter the names disappears
    And Verify that the user is able to create a "AppBundle" list through file upload
    And Verify that the counter on the left displays the correct value after file upload "<UPLOAD_FILENAME1>"
    #4
    And Navigate to Campaign Dashboard and clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    #5
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    #6
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    And User selects the "<CHANNEL>" as channel
    And User add and configure "<RULE_TYPE>" targeting rule and verify list is displayed in the targeting rule
    And User saves the rule configured in the tactic
    #7
    Then Verify that the "<RULE_TYPE>" rule is added to the tactic and retrieve the count of selected lists
    And User saves the targeting
    And User clicks Tactic Setting tab
    And Verify that the selected list is displayed in the targeting rule and retrieve the total count of targeted items
    Examples:
      | LIST_NAME | UPLOAD_FILENAME1   | ADVERTISER     | CP_NAME                | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME | CHANNEL          | RULE_TYPE    |
      | AppBundle | AppBundleFile1.csv | 01- Advertiser | AppBundleList_Campaign | Regular | 20000     | Line      | 500         | Tactic      | Display Advanced | Domains/Apps |

  @e2e
  Scenario Outline: Create Keyword List by by manually entering keywords and target in 'Keywords' targeting at Tactic level
    #2
    Given User navigates to the "Keyword Lists" page
    And Verify that the search option is present on the "Keyword Lists" tab
    When User clicks on Create New List
    Then Verify that the Create New List screen is displayed
    #3
    And User enters "<LIST_NAME>" in the List Name field
    And Verify that when "<KEYWORD_NAMES>" names are specified manually, the option to upload a file disappears
    And Verify that the user is able to create a "Keywords" list by specifying names manually
    And Verify that the counter on the left displays the correct value for each list in the navigation panel
    #4
    And Navigate to Campaign Dashboard and clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    #5
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    #6
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    And User selects the "<CHANNEL>" as channel
    And User add and configure "<RULE_TYPE>" targeting rule and verify list is displayed in the targeting rule
    And User saves the rule configured in the tactic
    #7
    Then Verify that the "<RULE_TYPE>" rule is added to the tactic and retrieve the count of selected lists
    And User saves the targeting
    And User clicks Tactic Setting tab
    And Verify that the selected list is displayed in the targeting rule and retrieve the total count of targeted items
    Examples:
      | LIST_NAME | KEYWORD_NAMES                              | ADVERTISER     | CP_NAME          | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME | CHANNEL          | RULE_TYPE |
      | Keyword   | Active Shooter, Antisemitism, Church Shoot | 01- Advertiser | Keyword_Campaign | Regular | 20000     | Line      | 500         | Tactic      | Display Advanced | Keywords  |

  @e2e
  Scenario Outline: Create IP Address List by by manually entering IP Address and target in 'IP Address' targeting at Tactic level
    #2
    And User navigates to the "IP Address Lists" page
    And Verify that the search option is present on the "IP Lists" tab
    When User clicks on Create New List
    Then Verify that the Create New List screen is displayed
    #3
    And User enters "<LIST_NAME>" in the List Name field
    And Verify that when "<IP_ADDRESS>" names are specified manually, the option to upload a file disappears
    And Verify that the user is able to create a "IP Address" list by specifying names manually
    And Verify that the counter on the left displays the correct value for each list in the navigation panel
    #4
    And Navigate to Campaign Dashboard and clicks on Create Campaign
    When User enters the campaign details as "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" and saves the campaign
    Then Verify campaign details are saved and user is navigated to the line item page
    #5
    When User enters the line item details as "<LINE_NAME>" "<LINE_BUDGET>", enables the line item and saves the changes
    Then Verify line item details are saved and user is navigated to the tactic page
    #6
    When User enters the tactic details as "<TACTIC_NAME>" and saves the tactic
    Then Verify tactic details are saved and user is navigated to the settings tab
    And User selects the "<CHANNEL>" as channel
    And User add and configure "<RULE_TYPE>" targeting rule and verify list is displayed in the targeting rule
    And User saves the rule configured in the tactic
    #7
    Then Verify that the "IP" rule is added to the tactic and retrieve the count of selected lists
    And Verify that the selected list is displayed in the targeting rule and retrieve the total count of targeted items
    And User saves the targeting
    Examples:
      | LIST_NAME  | IP_ADDRESS             | ADVERTISER     | CP_NAME          | CP_TYPE | CP_BUDGET | LINE_NAME | LINE_BUDGET | TACTIC_NAME | CHANNEL          | RULE_TYPE  |
      | IP_Address | 123.46.7.5, 123.46.7.7 | 01- Advertiser | Keyword_Campaign | Regular | 20000     | Line      | 500         | Tactic      | Display Advanced | IP Address |