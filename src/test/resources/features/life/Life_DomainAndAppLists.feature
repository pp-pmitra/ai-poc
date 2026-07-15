Feature: LIFE Regression - Validate the ability to create and delete Domain and App Bundle Lists using two input methods:
  1. Manual entry of domain or app bundle names
  2. File upload containing domain or app bundle names

  Background:
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"
    Given User navigates to the "Domain & App Lists" page
    And Verify that the search option is present on the "Domain/App Lists" tab

  @regression
  Scenario Outline: Manage a Domain List by manually entering domain names (Create, Edit, and Delete)
    And Verify that the sub-tabs "<SUB_TABS>" on the left navigation panel are available and "Both" is selected by default
    And Verify that when the "Domains" tab is selected, only "domain" lists are visible in the panel
    When User clicks on Create New List
    And User selects the "Domains" radio button from create new list page
    Then Verify that the Create New List screen is displayed
    And Verify that an error message is displayed when no listname "<LIST_NAME>" or "Domains" names are specified
    And Verify that if multiple "<DOMAIN_NAMES>" are specified on a single line, a validation error is shown
    And Verify that if multiple "<DOMAIN_NAMES_ACROSS_LINES>" are specified across multiple lines, an error is shown
    And Verify that when "<DOMAIN_NAMES>" names are specified manually, the option to upload a file disappears
    And User retrieves all the entered data before saving the list details "<LIST_NAME>"
    And Verify that the user is able to create a "Domains" list by specifying names manually
    And Verify that the counter on the left displays the correct value for each list in the navigation panel
    And User retrieves all the entered data after saving the list details "<LIST_NAME>"
    And Verify that the user is able to edit an existing "Domains" name list "<EDITED_DOMAIN_NAMES>"
    And User retrieves all the entered data after saving the list details "<LIST_NAME>"
    And Verify that the user is able to delete an existing "Domains" name list
    And Verify the deleted list is no longer displayed in the left panel
    And Verify that PulsePoint provided domain list "Automation_DomainList" is denoted with a purple P icon
    Examples:
      | SUB_TABS                   | LIST_NAME | DOMAIN_NAMES                      | EDITED_DOMAIN_NAMES           | DOMAIN_NAMES_ACROSS_LINES                                          |
      | Both, Domains, App Bundles | Domain    | brooklyn.com, docs.pulsepoint.com | manhattan.com, pulsepoint.com | brooklyn.com, docs.pulsepoint.com :: manhattan.com, pulsepoint.com |

  @regression
  Scenario Outline: Manage a Domain List by uploading domain names from a file (Create, Edit, and Delete)
    And Verify that the sub-tabs "<SUB_TABS>" on the left navigation panel are available and "Both" is selected by default
    And Verify that when the "Domains" tab is selected, only "domain" lists are visible in the panel
    When User clicks on Create New List
    And User selects the "Domains" radio button from create new list page
    Then Verify that the Create New List screen is displayed
    And Verify that an error message is displayed when no list names is specified and user tries to upload a file "<UPLOAD_FILENAME1>"
    And Verify that when enters "<LIST_NAME>" and upload file "<UPLOAD_FILENAME1>" option is selected, the text area to direct enter the names disappears
    And Verify the Uploaded Files section displays the entries count, includes download and delete icons after the file "<UPLOAD_FILENAME1>" is uploaded
    And Verify that the user is able to create a "Domains" list through file upload
    And Verify that the counter on the left displays the correct value after file upload "<UPLOAD_FILENAME1>"
    And Verify that the user is able to edit an existing list by uploading same file "<UPLOAD_FILENAME1>" again and verify the changes
    And Verify that the user is able to edit and save an existing "Domains" list by uploading another file "<UPLOAD_FILENAME2>" and verify the changes
    And Verify that the counter on the left displays the updated value after new file upload "<UPLOAD_FILENAME2>"
    And Verify that user is able to download the uploaded file "<UPLOAD_FILENAME1>", "<UPLOAD_FILENAME2>" and fetches the count of the downloaded files
    And Verify that the count of the downloaded files "<UPLOAD_FILENAME1>", "<UPLOAD_FILENAME2>" matches with the count displayed in the Uploaded Files section and left side panel
    And Verify that the user is able to delete the uploaded file "<UPLOAD_FILENAME1>"
    And Verify that the user is able to delete an existing "Domains" name list
    And Verify the deleted list is no longer displayed in the left panel
    Examples:
      | SUB_TABS                   | LIST_NAME         | UPLOAD_FILENAME1    | UPLOAD_FILENAME2    |
      | Both, Domains, App Bundles | Domain_FileUpload | DomainNameFile1.csv | DomainNameFile2.csv |

  @regression
  Scenario Outline: Manage a App Bundle List by entering AppBundle names manually (Create, Edit, and Delete)
    And Verify that the sub-tabs "<SUB_TABS>" on the left navigation panel are available and "Both" is selected by default
    And Verify that when the "App Bundles" tab is selected, only "applist" lists are visible in the panel
    When User clicks on Create New List
    And User selects the "App Bundles" radio button from create new list page
    Then Verify that the Create New List screen is displayed
    And Verify that an error message is displayed when no listname "<LIST_NAME>" or "AppBundle" names are specified
    And Verify that when "<APP_BUNDLES>" names are specified manually, the option to upload a file disappears
    And User retrieves all the entered data before saving the list details "<LIST_NAME>"
    And Verify that the user is able to create a "AppBundle" list by specifying names manually
    And Verify that the counter on the left displays the correct value for each list in the navigation panel
    And User retrieves all the entered data after saving the list details "<LIST_NAME>"
    And Verify that the user is able to edit an existing "AppBundle" name list "<EDITED_APP_BUNDLES>"
    And User retrieves all the entered data after saving the list details "<LIST_NAME>"
    And Verify that the user is able to delete an existing "AppBundle" name list
    And Verify the deleted list is no longer displayed in the left panel
    And Verify that PulsePoint provided domain list "Automation_AppBundleList" is denoted with a purple P icon
    Examples:
      | SUB_TABS                   | LIST_NAME  | APP_BUNDLES                 | EDITED_APP_BUNDLES       |
      | Both, Domains, App Bundles | App_Bundle | cambridge.org, redcross.org | wikipedia.org, mitre.org |

  @regression
  Scenario Outline: Manage a App Bundle List by uploading AppBundles names from a file (Create, Edit, and Delete)
    And Verify that the sub-tabs "<SUB_TABS>" on the left navigation panel are available and "Both" is selected by default
    And Verify that when the "App Bundles" tab is selected, only "applist" lists are visible in the panel
    When User clicks on Create New List
    And User selects the "App Bundles" radio button from create new list page
    Then Verify that the Create New List screen is displayed
    And Verify that an error message is displayed when no list names is specified and user tries to upload a file "<UPLOAD_FILENAME1>"
    And Verify that when enters "<LIST_NAME>" and upload file "<UPLOAD_FILENAME1>" option is selected, the text area to direct enter the names disappears
    And Verify the Uploaded Files section displays the entries count, includes download and delete icons after the file "<UPLOAD_FILENAME1>" is uploaded
    And Verify that the user is able to create a "AppBundle" list through file upload
    And Verify that the counter on the left displays the correct value after file upload "<UPLOAD_FILENAME1>"
    And Verify that the user is able to edit an existing list by uploading same file "<UPLOAD_FILENAME1>" again and verify the changes
    And Verify that the user is able to edit and save an existing "AppBundle" list by uploading another file "<UPLOAD_FILENAME2>" and verify the changes
    And Verify that the counter on the left displays the updated value after new file upload "<UPLOAD_FILENAME2>"
    And Verify that user is able to download the uploaded file "<UPLOAD_FILENAME1>", "<UPLOAD_FILENAME2>" and fetches the count of the downloaded files
    And Verify that the count of the downloaded files "<UPLOAD_FILENAME1>", "<UPLOAD_FILENAME2>" matches with the count displayed in the Uploaded Files section and left side panel
    And Verify that the user is able to delete the uploaded file "<UPLOAD_FILENAME1>"
    And Verify that the user is able to delete an existing "AppBundle" name list
    And Verify the deleted list is no longer displayed in the left panel
    Examples:
      | SUB_TABS                   | LIST_NAME            | UPLOAD_FILENAME1   | UPLOAD_FILENAME2   |
      | Both, Domains, App Bundles | AppBundle_FileUpload | AppBundleFile1.csv | AppBundleFile2.csv |