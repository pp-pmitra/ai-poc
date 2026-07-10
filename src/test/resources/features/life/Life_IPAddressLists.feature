Feature: LIFE Regression – Generate IP Address Lists in the following ways:
  1. Manually input ip address to create a list
  2. Upload a file to generate a id address list automatically

  Background:
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"
    And User navigates to the "IP Address Lists" page
    And Verify that the search option is present on the "IP Lists" tab
    When User clicks on Create New List
    Then Verify that the Create New List screen is displayed

  @regression
  Scenario Outline: Manage an IP Address List by manually adding, updating, and removing IP addresses
    And Verify that an error message is displayed when no listname "<LIST_NAME>" or "IP Address" names are specified
    And Verify that if multiple "<IP_ADDRESS>" are specified on a single line, a validation error is shown
    And Verify that if multiple "<IP_ADDRESS_ACROSS_LINES>" are specified across multiple lines, an error is shown
    And Verify that when "<IP_ADDRESS>" names are specified manually, the option to upload a file disappears
    And User retrieves all the entered data before saving the list details "<LIST_NAME>"
    And Verify that the user is able to create a "IP Address" list by specifying names manually
    And Verify that the counter on the left displays the correct value for each list in the navigation panel
    And User retrieves all the entered data after saving the list details "<LIST_NAME>"
    And Verify that the user is able to edit an existing "IP Address" name list "<EDITED_IP_ADDRESS>"
    And User retrieves all the entered data after saving the list details "<LIST_NAME>"
    And Verify that the user is able to delete an existing "IP Address" name list
    And Verify the deleted list is no longer displayed in the left panel
    Examples:
      | LIST_NAME  | IP_ADDRESS             | EDITED_IP_ADDRESS                            | IP_ADDRESS_ACROSS_LINES                                                |
      | IP_Address | 123.46.7.5, 123.46.7.7 | 123.46.7.0, 684D:1111:222:3333:4444:5555:6:9 | 123.46.7.5, 123.46.7.7 :: 123.46.7.0, 684D:1111:222:3333:4444:5555:6:9 |

  @regression
  Scenario Outline: Manage an IP Address List by uploading a file to create, update, and delete IP addresses
    And Verify that an error message is displayed when no list names is specified and user tries to upload a file "<UPLOAD_FILENAME1>"
    And Verify that when enters "<LIST_NAME>" and upload file "<UPLOAD_FILENAME1>" option is selected, the text area to direct enter the names disappears
    And Verify the Uploaded Files section displays the entries count, includes download and delete icons after the file "<UPLOAD_FILENAME1>" is uploaded
    And Verify that the user is able to create a "IP Address" list through file upload
    And Verify that the counter on the left displays the correct value after file upload "<UPLOAD_FILENAME1>"
    And Verify that the user is able to edit an existing list by uploading same file "<UPLOAD_FILENAME1>" again and verify the changes
    And Verify that the user is able to edit and save an existing "IP Address" list by uploading another file "<UPLOAD_FILENAME2>" and verify the changes
    And Verify that the counter on the left displays the updated value after new file upload "<UPLOAD_FILENAME2>"
    And Verify that user is able to download the uploaded file "<UPLOAD_FILENAME1>", "<UPLOAD_FILENAME2>" and fetches the count of the downloaded files
    And Verify that the count of the downloaded files "<UPLOAD_FILENAME1>", "<UPLOAD_FILENAME2>" matches with the count displayed in the Uploaded Files section and left side panel
    And Verify that the user is able to delete the uploaded file "<UPLOAD_FILENAME1>"
    And Verify that the user is able to delete an existing "IP Address" name list
    And Verify the deleted list is no longer displayed in the left panel
    Examples:
      | LIST_NAME            | UPLOAD_FILENAME1   | UPLOAD_FILENAME2   |
      | IPAddress_FileUpload | IPAddressFile1.csv | IPAddressFile2.csv |