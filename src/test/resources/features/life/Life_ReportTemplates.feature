Feature: LIFE Regression - Create a Report Template

  @regression
  Scenario Outline: Create a Report Template
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"
    And User navigates to Report Templates page
    Then Verify the tabs displayed on the Report Templates page
    And Verify Template tab is selected by default on the Report Templates page
    When User clicks on New Template
    Then Verify the tabs displayed on the Create New Template panel
    And Verify if "Regular" is selected by default as Template type on the Create New Template panel
    And User selects the template type as "Regular" on the Create New Template panel
    And Verify the delete button is disabled on the Create New Template panel
    And Verify error message when no dimensions and metrics are selected and user tries to save the template "<TEMPLATE NAME>"
    When User enters the template details as "<TEMPLATE NAME>" "<DIMENSIONS>" "<METRICS>"
    And User makes the template "Private"
    Then Verify the selected dimensions and metrics under the Template Structure section
    When User saves the new template
    Then Verify new template is saved and displayed in the template list
    And Verify availability of "Run Report", "Copy" and "Delete" actions for the created template
    And Verify details of the created template on Template Listing page
    And Verify the details of the created template on Edit Template panel
    And Verify the delete button is enabled on the Edit Template panel
    Examples:
      | TEMPLATE NAME | DIMENSIONS      | METRICS     |
      | AutoTemplate  | Advertiser Name | Impressions |

  @regression
  Scenario Outline: Add multiple dimensions and metrics from multiple categories during template creation
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"
    And User navigates to Report Templates page
    Then Verify the tabs displayed on the Report Templates page
    And Verify Template tab is selected by default on the Report Templates page
    When User clicks on New Template
    Then Verify the tabs displayed on the Create New Template panel
    And Verify if "Regular" is selected by default as Template type on the Create New Template panel
    And User selects the template type as "Regular" on the Create New Template panel
    And Verify the delete button is disabled on the Create New Template panel
    And Verify error message when no dimensions and metrics are selected and user tries to save the template "<TEMPLATE NAME>"
    When User enters the template details as "<TEMPLATE NAME>" "<DIMENSIONS>" "<METRICS>"
    And User makes the template "Private"
    Then Verify the selected dimensions and metrics under the Template Structure section
    When User saves the new template
    Then Verify new template is saved and displayed in the template list
    And Verify availability of "Run Report", "Copy" and "Delete" actions for the created template
    And Verify the details of the created template on Edit Template panel
    And Verify the delete button is enabled on the Edit Template panel
    And User deletes the created template
    Then Verify the template is deleted and not displayed in the template list
    Examples:
      | TEMPLATE NAME         | DIMENSIONS                                                                                                          | METRICS                                                          |
      | MultiCategoryTemplate | Advertiser Name, Campaign Name, NPI First Name, Device Type, Area code, Keywords, Deal Name, Age, Current Step Name | Impressions, Clicks, Platform Fee, Complete Views, Midpoint Rate |

  @regression
  Scenario Outline: Verify that user is able to delete the existing report template
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"
    And User navigates to Report Templates page
    Then Verify the tabs displayed on the Report Templates page
    When User clicks on New Template
    Then Verify the tabs displayed on the Create New Template panel
    When User enters the template details as "<TEMPLATE NAME>" "<DIMENSIONS>" "<METRICS>"
    And User makes the template "Private"
    Then Verify the selected dimensions and metrics under the Template Structure section
    When User saves the new template
    Then Verify new template is saved and displayed in the template list
    And User deletes the existing template from the template list
    Then Verify the template is deleted and not displayed in the template list
    Examples:
      | TEMPLATE NAME | DIMENSIONS      | METRICS     |
      | AutoTemplate  | Advertiser Name | Impressions |

  @regression
  Scenario Outline: Verify that user is able to copy the existing report template
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"
    And User navigates to Report Templates page
    Then Verify the tabs displayed on the Report Templates page
    When User clicks on New Template
    Then Verify the tabs displayed on the Create New Template panel
    When User enters the template details as "<TEMPLATE NAME>" "<DIMENSIONS>" "<METRICS>"
    And User makes the template "Private"
    Then Verify the selected dimensions and metrics under the Template Structure section
    When User saves the new template
    Then Verify new template is saved and displayed in the template list
    And User copies the existing template from the template list
    And User enters details in the copied template as "<EDITED_DIMENSIONS>" "<EDITED_METRICS>"
    And User fetches the details of the copied template on Edit Template panel
    And User saves the copied template with updated details
    Then Verify the copied template is displayed in the template list
    And Verify the data persistence of the copied template on Edit Template panel
    Examples:
      | TEMPLATE NAME | DIMENSIONS      | METRICS     | EDITED_DIMENSIONS | EDITED_METRICS |
      | AutoTemplate  | Advertiser Name | Impressions | Campaign ID       | Clicks         |

  @regression
  Scenario Outline: Verify that user is able to run the report using 'Run report' icon from the Templates List View
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"
    And User navigates to Report Templates page
    Then Verify the tabs displayed on the Report Templates page
    When User clicks on New Template
    Then Verify the tabs displayed on the Create New Template panel
    When User enters the template details as "<TEMPLATE NAME>" "<DIMENSIONS>" "<METRICS>"
    And User makes the template "Private"
    Then Verify the selected dimensions and metrics under the Template Structure section
    When User saves the new template
    Then Verify new template is saved and displayed in the template list
    And User clicks Run Report icon for the existing template from the template list
    And Verify Run Report panel should be opened
    And User should be able to select advertiser as "<ADVERTISER>"
    When Campaign should load for selection when user types campaign initials "<CAMPAIGN_INITIALS>" in "Campaign" field
    Then User should be able to select value from dropdown
    When Line Items of selected campaigns should load when user types line items initials "<LINE_ITEM_INITIALS>" in "Line Item" field
    Then User should be able to select value from dropdown
    When Tactic of selected line items should load when user types tactic names initials "<TACTIC_INITIALS>" in "Tactic" field
    Then User should be able to select value from dropdown
    When Creative of selected tactic should load when user types creative names initials "<CREATIVE_INITIALS>" in "Creative" field
    Then User should be able to select value from dropdown
    And Verify that by default "Custom Dates" option is selected for Report Period Field
    And Verify that user is able to select start date and end date when Custom Dates option is selected
    And Verify that user is able to select start "12:00" and end time "09:00" when Custom Dates option is selected
    And Verify that user is able to select Timezone field value "<TIME_ZONE>"
    And Verify the presence of Report Format field and default value - "CSV"
    And Verify the presence of Text Qualifier checkbox and by default it should be checked
    And User should be able to generate the report
    Examples:
      | TEMPLATE NAME | DIMENSIONS      | METRICS     | ADVERTISER     | CAMPAIGN_INITIALS | LINE_ITEM_INITIALS | TACTIC_INITIALS | CREATIVE_INITIALS | TIME_ZONE                       |
      | AutoTemplate  | Advertiser Name | Impressions | 01- Advertiser | CreativeCampaign  | CreativeLine       | CreativeTactic  | Creative          | (GMT+05:30) India Standard Time |
