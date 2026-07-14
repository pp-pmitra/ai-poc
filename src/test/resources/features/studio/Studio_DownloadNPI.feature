Feature: Studio Download NPI List

  @regression
  Scenario Outline: Verify "Download NPI" functionality on studio platform
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    And User searches the "<WORKSPACE>" and selects it
    And User fetches the Identified NPI count from the workspace
    Then Download button is enabled to the user
    And User clicks Download NPI option
    And User selects download format as "<FILE_EXTENSION>" and clicks Download button
    And User verifies the total Identified "NPI" count in the downloaded file - "<FILE_EXTENSION>"
    Examples:
      | WORKSPACE              | FILE_EXTENSION |
      | Automation_HCPExplorer | CSV            |
      | Automation_HCPExplorer | XLSX           |