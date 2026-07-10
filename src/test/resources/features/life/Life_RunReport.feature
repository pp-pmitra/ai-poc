Feature: LIFE Regression - Run Report fields verification and report generation
  It ensures the correct loading, population, interaction, and behavior of all critical fields within the Run Report pop-up, including:
  1. Template selection
  2. Dimensions and Metrics configuration
  3. Data Granularity options
  4. Advertiser, Campaign, Line Item, Tactic, and Creative selection
  5. Advanced Settings and filter options
  6. Report generation

  Background:
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"

  @regression
  Scenario Outline: Validate Run Report panel's field verification on Reports Page and allow report generation
    And User navigates to Administrative section and fetches the advertiser for the account "automation@pulsepoint"
    And User fetches the logged in username
    When User navigates to run report from mega menu of the life application
    And Verify Run Report panel should be opened
    And Template drop-down should display templates created under "automation@pulsepoint"
    When User clicks on "Pick Dimensions/Metrics" link
    Then Dimensions and Metrics fields should be displayed
    And User should navigate back to Template drop-down by clicking "Select Existing Template"
    Then Template drop-down should be visible
    Then Data Granularity should have default value "Daily"
    And Verify Data Granularity dropdown should show below list of values
      | Hourly                 |
      | Daily                  |
      | Weekly                 |
      | Monthly                |
      | Total                  |
      | Flight                 |
      | Predefined By Template |
    And Verify Data Granularity field should allow selection any of the values "Weekly" from the dropdown
    Then Advertiser drop-down should list advertisers mapped to "automation@pulsepoint"
    And User should be able to select multiple advertisers from the list
    And Verify on selecting "All Advertisers" option, previously selected individual advertisers should be cleared
    And User should be able to select template "<TEMPLATE>" from the dropdown
    And User should be able to select advertiser as "<ADVERTISER>"
    When Campaign should load for selection when user types campaign initials "<CAMPAIGN_INITIALS>" in "Campaign" field
    Then User should be able to select multiple values from dropdown
    When Line Items of selected campaigns should load when user types line items initials "<LINE_ITEM_INITIALS>" in "Line Item" field
    Then User should be able to select multiple values from dropdown
    When Tactic of selected line items should load when user types tactic names initials "<TACTIC_INITIALS>" in "Tactic" field
    Then User should be able to select multiple values from dropdown
    When Creative of selected tactic should load when user types creative names initials "<CREATIVE_INITIALS>" in "Creative" field
    Then User should be able to select multiple values from dropdown
    When User clicks on Advanced Settings
    Then "Filter Report" section should be visible with label "Only Report on Impressions with Identifiable NPIs" checkbox
    Then "Run Now" and "Schedule" tabs should be present in the Run Report pop-up
    When On Run Now tab, Report Period field should have options below
      | Custom Dates |
      | Lifetime     |
      | Flights      |
    And User selects the option Only Report on Impressions with Identifiable NPIs
    And Verify that "Email" tab is selected as Delivery method by default
    And Verify that "Deliver to Users" field is pre-populated with logged in user email and user should be able to edit the email address "<USER_EMAIL>"
    And Verify that "Notify User for Failures" field is pre-populated with logged in user email and user should be able to edit the email address "<USER_EMAIL>"
    And Verify that user is not able to remove the pre-populated logged in user email from "Notify User for Failures" field
    And Verify File Name field is available on report panel
    And Validate that the template name matches the file name, and ensure the help text displays the file name in the ".csv" format
    And User should be able to generate the report
    And Validate report details such as Created By, Reporting period, Report Name from Report Listing page
    And Confirms that the report panel retains the entered data
    Examples:
      | TEMPLATE       | ADVERTISER     | CAMPAIGN_INITIALS | LINE_ITEM_INITIALS | TACTIC_INITIALS | CREATIVE_INITIALS | USER_EMAIL                 |
      | AutoTemplate20 | 01- Advertiser | CreativeCampaign  | CreativeLine       | CreativeTactic  | Creative          | automationUserInter, Lista |

  @regression
  Scenario Outline: Verify Run Now tab's field options and their selection behavior
    When User navigates to run report from mega menu of the life application
    And Verify Run Report panel should be opened
    And User should be able to select template "<TEMPLATE>" from the dropdown
    And User should be able to select advertiser as "<ADVERTISER>"
    When Campaign should load for selection when user types campaign initials "<CAMPAIGN_INITIALS>" in "Campaign" field
    Then User should be able to select value from dropdown
    When Line Items of selected campaigns should load when user types line items initials "<LINE_ITEM_INITIALS>" in "Line Item" field
    Then User should be able to select value from dropdown
    When Tactic of selected line items should load when user types tactic names initials "<TACTIC_INITIALS>" in "Tactic" field
    Then User should be able to select value from dropdown
    When Creative of selected tactic should load when user types creative names initials "<CREATIVE_INITIALS>" in "Creative" field
    Then User should be able to select value from dropdown
    When On Run Now tab, Report Period field should have options below
      | Custom Dates |
      | Lifetime     |
      | Flights      |
    And User selects "Custom Dates" option from Report Period field and verify the fields displayed on selecting the option
    And User selects "Lifetime" option from Report Period field and verify the fields displayed on selecting the option
    And User selects "Flights" option from Report Period field and verify the fields displayed on selecting the option
    Examples:
      | TEMPLATE       | ADVERTISER     | CAMPAIGN_INITIALS | LINE_ITEM_INITIALS | TACTIC_INITIALS | CREATIVE_INITIALS | TIME_ZONE                       | REPORT_FORMATS                                                             |
      | AutoTemplate20 | 01- Advertiser | CreativeCampaign  | CreativeLine       | CreativeTactic  | Creative          | (GMT+05:30) India Standard Time | CSV, Excel, Pipe Delimited CSV, Pipe Delimited TXT, Tab Delimited TXT, TSV |


  @regression
  Scenario Outline:  Validate One time report section's field verification and generate One time report using Template and Custom Dates option from Run Now
    When User navigates to run report from mega menu of the life application
    And Verify Run Report panel should be opened
    And User should be able to select template "<TEMPLATE>" from the dropdown
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
    And Verify the availability of various options of the Report Format field - "<REPORT_FORMATS>"
    And Verify the presence of Text Qualifier checkbox and by default it should be checked
    And Verify Line Coding field is available with below options and default value is "Unix (LF)"
      | Unix (LF)  |
      | DOS (CRLF) |
    And User should be able to generate the report
    And Confirms that the report panel retains the entered data
    Examples:
      | TEMPLATE       | ADVERTISER     | CAMPAIGN_INITIALS | LINE_ITEM_INITIALS | TACTIC_INITIALS | CREATIVE_INITIALS | TIME_ZONE                       | REPORT_FORMATS                                                             |
      | AutoTemplate20 | 01- Advertiser | CreativeCampaign  | CreativeLine       | CreativeTactic  | Creative          | (GMT+05:30) India Standard Time | CSV, Excel, Pipe Delimited CSV, Pipe Delimited TXT, Tab Delimited TXT, TSV |

  @regression
  Scenario Outline:  Validate One time report section's field verification and generate One time report using a Pick Dimensions/Metrics and Life Time option from Run Now
    When User navigates to run report from mega menu of the life application
    And Verify Run Report panel should be opened
    When User clicks on "Pick Dimensions/Metrics" link
    Then Dimensions and Metrics fields should be displayed
    And User should be able to select "<DIMENSIONS>" and "<METRICS>"
    And User should be able to select advertiser as "<ADVERTISER>"
    When Campaign should load for selection when user types campaign initials "<CAMPAIGN_INITIALS>" in "Campaign" field
    Then User should be able to select value from dropdown
    And Verify that "Lifetime" and "Flights" options are disabled until a Line Item is selected
    When Line Items of selected campaigns should load when user types line items initials "<LINE_ITEM_INITIALS>" in "Line Item" field
    Then User should be able to select value from dropdown
    And Verify that "Lifetime" and "Flights" options are enabled
    When Tactic of selected line items should load when user types tactic names initials "<TACTIC_INITIALS>" in "Tactic" field
    Then User should be able to select value from dropdown
    When Creative of selected tactic should load when user types creative names initials "<CREATIVE_INITIALS>" in "Creative" field
    Then User should be able to select value from dropdown
    And User clicks "Lifetime" report period button
    And Verify that user is able to select Timezone field value "<TIME_ZONE>"
    And User should be able to generate the report
    And Confirms that the report panel retains the entered data
    Examples:
      | ADVERTISER     | CAMPAIGN_INITIALS | LINE_ITEM_INITIALS | TACTIC_INITIALS | CREATIVE_INITIALS | DIMENSIONS                                                 | METRICS             | TIME_ZONE                       |
      | 01- Advertiser | CreativeCampaign  | CreativeLine       | CreativeTactic  | Creative          | Advertiser Name, Campaign Name, LineItem Name, Tactic Name | Impressions, Clicks | (GMT+05:30) India Standard Time |

  @regression
  Scenario Outline:  Validate One time report section's field verification and generate One time report by entering Tactic and Flight option from Run Now
    Then User searches the Campaign "<CAMPAIGN_NAME>", navigates to LineItem and fetches the flight details
    When User navigates to run report from mega menu of the life application
    And Verify Run Report panel should be opened
    When Tactic of selected line items should load when user types tactic names initials "<TACTIC_INITIALS>" in "Tactic" field
    Then User should be able to select value from dropdown
    And User should be able to fetch details - Advertiser, Campaign, Line Item, Tactic
    And User should be able to select template "<TEMPLATE>" from the dropdown
    And User clicks "Flights" report period button
    And Verify that Flight details field is displayed with value
    And User fetches all the Flight details and verifies that selected Line Item flight details appear in the Flight tab of the Run Report panel
    And User should be able to generate the report
    And Confirms that the report panel retains the entered data
    Examples:
      | TEMPLATE            | TACTIC_INITIALS                 | CAMPAIGN_NAME            |
      | Template_Automation | Multiple_Flight_Campaign_Tactic | Multiple_Flight_Campaign |

  @regression
  Scenario: Validate Dimensions and Metrics of New Template creation with Run Report
    And User navigates to Report Templates page
    Then Verify the tabs displayed on the Report Templates page
    When User clicks on New Template
    Then Verify the tabs displayed on the Create New Template panel
    And Expand all the groups and fetch dimensions and metrics
    When User navigates to run report from mega menu of the life application
    And Verify Run Report panel should be opened
    When User clicks on "Pick Dimensions/Metrics" link
    Then Dimensions and Metrics fields should be displayed
    And Verify dropdown dimensions with the template
    And Verify dropdown metrics with the template

  @regression
  Scenario Outline: Verify Report generation for file breakdown "<FILE_BREAKDOWN_TYPE>" button and verify Filter Report checkbox label
    When User navigates to run report from mega menu of the life application
    And Verify Run Report panel should be opened
    And User should be able to select template "<TEMPLATE>" from the dropdown
    And User should be able to select advertiser as "<ADVERTISER>"
    When Campaign should load for selection when user types campaign initials "<CAMPAIGN_INITIALS>" in "Campaign" field
    Then User should be able to select value from dropdown
    When Line Items of selected campaigns should load when user types line items initials "<LINE_ITEM_INITIALS>" in "Line Item" field
    Then User should be able to select value from dropdown
    When Tactic of selected line items should load when user types tactic names initials "<TACTIC_INITIALS>" in "Tactic" field
    Then User should be able to select value from dropdown
    When Creative of selected tactic should load when user types creative names initials "<CREATIVE_INITIALS>" in "Creative" field
    Then User should be able to select value from dropdown
    When User clicks on Advanced Settings
    And User selects "<FILE_BREAKDOWN_TYPE>" button
    Then "Filter Report" section should be visible with label "Only Report on Impressions with Identifiable NPIs", "Only Report on Running Advertisers", "Only Report on Running Campaigns" checkbox
    And User should be able to generate the report
    # And User downloads the Scheduled report and verify the data in downloaded report
    # And And confirm that the report panel retains the entered data
    Examples:
      | TEMPLATE       | ADVERTISER     | CAMPAIGN_INITIALS | LINE_ITEM_INITIALS | TACTIC_INITIALS | CREATIVE_INITIALS | FILE_BREAKDOWN_TYPE |
      | AutoTemplate20 | 01- Advertiser | Auto              | Line               | Tactic          | Creative          | Single File         |
      | AutoTemplate20 | 01- Advertiser | Auto              | Line               | Tactic          | Creative          | Per Advertiser      |
      | AutoTemplate20 | Z_Automation   | Auto              | Line               | Tactic          | Creative          | Per Campaign        |

  @regression
  Scenario Outline: Verify fields under Custom Destination tab in Run Report's Delivery Method section
    When User navigates to run report from mega menu of the life application
    And Verify Run Report panel should be opened
    When User clicks on "Custom Destination" tab as Delivery Method
    Then Verify Destination dropdown field is available
    And Verify "Edit" button is available in Destination dropdown field
    And User clicks Edit button from Destination dropdown field
    And User verifies the custom destination fields - Destination Name, Destination Type, Host, Username, Password, Port textfields, Test Access, Create and Cancel buttons
    And Verify "Add Destination" button is available in Destination dropdown field
    When User clicks "Add Destination" button
    Then Verify Destination Name, Destination Type fields are displayed
    And Verify that Destination Type has values "<DESTINATION_TYPE>"
    And Verify File Path field is available
    And Verify File Name field is available
    And Verify the presence of Report Format field and default value - "CSV"
    And Verify the availability of various options of the Report Format field - "<REPORT_FORMATS>"
    And Verify the presence of Text Qualifier checkbox and by default it should be checked
    And Verify Compression field is available with below options and default value is "None"
      | Zip  |
      | BZip |
      | GZip |
      | None |
    And Verify Control File checkbox is present and by default it should be unchecked
    And Verify the presence of Advanced Export checkbox and by default it should be unchecked
    And Verify Line Coding field is available with below options and default value is "Unix (LF)"
      | Unix (LF)  |
      | DOS (CRLF) |
    Examples:
      | DESTINATION_TYPE   | REPORT_FORMATS                                                             |
      | FTP, SFTP, S3, GCP | CSV, Excel, Pipe Delimited CSV, Pipe Delimited TXT, Tab Delimited TXT, TSV |

  @regression
  Scenario Outline: Verify that user is able to create custom destinations from Run Report panel and generate report
    When User navigates to run report from mega menu of the life application
    And Verify Run Report panel should be opened
    And User should be able to select template "<TEMPLATE>" from the dropdown
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
    When User clicks on "Custom Destination" tab as Delivery Method
    Then Verify Destination dropdown field is available
    And Verify "Add Destination" button is available in Destination dropdown field
    When User clicks "Add Destination" button
    And User enters Destination name "<DESTINATION_NAME>", Destination Type "<DESTINATION_TYPE>" and other required details - "<HOST>", "<PORT>", "<SERVER_PATH>"
    Then User runs the connection test and creates the destination
    And Verify destination created should populate in the Destination dropdown field
    And User should be able to generate the report
    And Confirms that the report panel retains the entered data
    Examples:
      | TEMPLATE       | ADVERTISER     | CAMPAIGN_INITIALS | LINE_ITEM_INITIALS | TACTIC_INITIALS | CREATIVE_INITIALS | TIME_ZONE                       | DESTINATION_NAME | DESTINATION_TYPE | HOST                | PORT | SERVER_PATH                    |
      | AutoTemplate20 | 01- Advertiser | CreativeCampaign  | CreativeLine       | CreativeTactic  | Creative          | (GMT+05:30) India Standard Time | Run_Destination_ | SFTP             | ma2-qa-automation01 | 22   | /home/NPIAutoImport/Automation |