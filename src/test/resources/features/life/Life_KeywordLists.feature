Feature: LIFE Regression – Generate Keyword Lists in the following ways:
  1. Manually input keywords to create a list
  2. Upload a file to generate a keyword list automatically

  Background:
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"
    Given User navigates to the "Keyword Lists" page
    And Verify that the search option is present on the "Keyword Lists" tab
    When User clicks on Create New List
    Then Verify that the Create New List screen is displayed

  @regression
  Scenario Outline: Manage a Keyword List by manually adding, editing, and deleting keywords
    And Verify that an error message is displayed when no listname "<LIST_NAME>" or "Keywords" names are specified
    And Verify that when "<KEYWORD_NAMES>" names are specified manually, the option to upload a file disappears
    And User retrieves all the entered data before saving the list details "<LIST_NAME>"
    And Verify that the user is able to create a "Keywords" list by specifying names manually
    And Verify that the counter on the left displays the correct value for each list in the navigation panel
    And User retrieves all the entered data after saving the list details "<LIST_NAME>"
    And Verify that the user is able to edit an existing "Keywords" name list "<EDITED_KEYWORDS_NAMES>"
    And User retrieves all the entered data after saving the list details "<LIST_NAME>"
    And Verify that the user is able to delete an existing "Keywords" name list
    And Verify the deleted list is no longer displayed in the left panel
    And Verify that PulsePoint provided domain list "Automation_KeywordList" is denoted with a purple P icon
    Examples:
      | LIST_NAME | KEYWORD_NAMES                              | EDITED_KEYWORDS_NAMES        |
      | Keyword   | Active Shooter, Antisemitism, Church Shoot | Cory Booker, Clinton Package |

  @regression
  Scenario Outline: Manage a Keyword List by uploading a file to create, edit, and delete keywords
    And Verify that an error message is displayed when no list names is specified and user tries to upload a file "<UPLOAD_FILENAME1>"
    And Verify that when enters "<LIST_NAME>" and upload file "<UPLOAD_FILENAME1>" option is selected, the text area to direct enter the names disappears
    And Verify the Uploaded Files section displays the entries count, includes download and delete icons after the file "<UPLOAD_FILENAME1>" is uploaded
    And Verify that the user is able to create a "Keywords" list through file upload
    And Verify that the counter on the left displays the correct value after file upload "<UPLOAD_FILENAME1>"
    And Verify that the user is able to edit an existing list by uploading same file "<UPLOAD_FILENAME1>" again and verify the changes
    And Verify that the user is able to edit and save an existing "Keywords" list by uploading another file "<UPLOAD_FILENAME2>" and verify the changes
    And Verify that the counter on the left displays the updated value after new file upload "<UPLOAD_FILENAME2>"
    And Verify that user is able to download the uploaded file "<UPLOAD_FILENAME1>", "<UPLOAD_FILENAME2>" and fetches the count of the downloaded files
    And Verify that the count of the downloaded files "<UPLOAD_FILENAME1>", "<UPLOAD_FILENAME2>" matches with the count displayed in the Uploaded Files section and left side panel
    And Verify that the user is able to delete the uploaded file "<UPLOAD_FILENAME1>"
    And Verify that the user is able to delete an existing "Keywords" name list
    And Verify the deleted list is no longer displayed in the left panel
    Examples:
      | LIST_NAME          | UPLOAD_FILENAME1  | UPLOAD_FILENAME2  |
      | Keyword_FileUpload | KeywordsFile1.csv | KeywordsFile2.csv |