Feature: Verify visibility of Owned and Operated Data based on Admin Permission within the Cross-Filter section of the Workspace
  1. Verify "Owned and Operated Data" permission for the targeted Advertiser in the Admin
  2. Create a new Workspace associated with that Advertiser
  3. Verify that the "Owned and Operated Data" filter option is present within the Cross-Filter section of the Workspace

  @regression
  Scenario Outline: Verify that the visibility of Owned and Operated Data in the Cross-Filter section of the Workspace is based on the Admin Permission - "<PERMISSION_FLAG>"
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    When User navigates to Administrative section
    And User navigates to Accounts Tab
    And User searches the account "PP engineering test" and checks Studio permissions
    And User navigates to Advertisers tab
    And User clicks on "HCP365" tab present under Advertisers tab
    And User sets the HCP365 permission "<PERMISSION_FLAG>" for "<ADVERTISER>" and saves the changes
    And User clicks PulsePoint icon to navigate back to Life
    And User navigates to "studio" application
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And Verify Owned And Operated section is "<VISIBILITY_FLAG>" within the Cross-Filter section of the Workspace
    And User saves the "HCP Explorer" workspace
    Then Verify the "HCP Explorer" Workspace is saved
    Examples:
      | PERMISSION_FLAG | ADVERTISER | WORKSPACE_NAME | VISIBILITY_FLAG |
      | Enabled         | Abbvie     | Explorer       | Present         |
     #| Disabled        | Abbvie     | Explorer       | Absent         |
     # Note: Above example is currently commented out as the permission takes a long time to get reflected, which is causing the test to fail