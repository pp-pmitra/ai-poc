Feature: HCP365 Regression - Dashboard data Validation and Permission

  Scenario Outline: Verify Overview Tabs and its Sub Tabs availability on Dashboard on HCP 365
    Given Life application is logged in as "<USER>"
    And User clicks on HCP 365 module from main menu
    When User land on Overview tab on HCP Dashboard
    Then Verify Tabs and Subtabs of Overview tab on HCP Dashboard
    Examples:
      | USER    | ADVERTISER | TYPE    | CLIENT | ExpectedOutput | User Type |
      | admin11 |            | Regular |        | XYZ            | Internal  |

  Scenario Outline: Verify Site Tabs and its Sub Tabs availability on Dashboard on HCP 365
    Given Life application is logged in as "<USER>"
    And User clicks on HCP 365 module from main menu
    When User clicks on Site tab on HCP Dashboard
    Then Verify Tabs and Subtabs of Site tab on HCP Dashboard
    Examples:
      | USER    | ADVERTISER | TYPE    | CLIENT | ExpectedOutput | User Type |
      | admin11 |            | Regular |        | XYZ            | Internal  |

  Scenario Outline: Verify Search Tabs and its Sub Tabs availability on Dashboard on HCP 365
    Given Life application is logged in as "<USER>"
    And User clicks on HCP 365 module from main menu
    When User clicks on Search tab on HCP Dashboard
    Then Verify Tabs and Subtabs of Search tab on HCP Dashboard
    Examples:
      | USER    | ADVERTISER | TYPE    | CLIENT | ExpectedOutput | User Type |
      | admin11 |            | Regular |        | XYZ            | Internal  |

  Scenario Outline: Verify Media Tabs and its Sub Tabs availability on Dashboard on HCP 365
    Given Life application is logged in as "<USER>"
    And User clicks on HCP 365 module from main menu
    When User clicks on Media tab on HCP Dashboard
    Then Verify Tabs and Subtabs of Media tab on HCP Dashboard
    Examples:
      | USER    | ADVERTISER | TYPE    | CLIENT | ExpectedOutput | User Type |
      | admin11 |            | Regular |        | XYZ            | Internal  |

  Scenario Outline: Verify Email Tabs and its Sub Tabs availability on Dashboard on HCP 365
    Given Life application is logged in as "<USER>"
    And User clicks on HCP 365 module from main menu
    When User clicks on Email tab on HCP Dashboard
    Then Verify Tabs and Subtabs of Email tab on HCP Dashboard
    Examples:
      | USER    | ADVERTISER | TYPE    | CLIENT | ExpectedOutput | User Type |
      | admin11 |            | Regular |        | XYZ            | Internal  |

  Scenario Outline: Verify Social Tabs and its Sub Tabs availability on Dashboard on HCP 365
    Given Life application is logged in as "<USER>"
    And User clicks on HCP 365 module from main menu
    When User clicks on Social tab on HCP Dashboard
    Then Verify Tabs and Subtabs of Social tab on HCP Dashboard
    Examples:
      | USER    | ADVERTISER | TYPE    | CLIENT | ExpectedOutput | User Type |
      | admin11 |            | Regular |        | XYZ            | Internal  |

  Scenario Outline: Verify user access modification of Dashboard on HCP 365 in all tab.
    Given Life application is logged in as "<USER>"
    And User clicks on HCP 365 module from main menu
    When User change settings of HCP Dashboard Display modules
    Then Verify Tabs and Subtabs of shall change on HCP Dashboard
    Examples:
      | USER    | ADVERTISER | TYPE    | CLIENT | ExpectedOutput | User Type |
      | admin11 |            | Regular |        | XYZ            | Internal  |

  Scenario Outline: Verify data after modification of filters in Dashboard on HCP 365 in all tab.
    Given Life application is logged in as "<USER>"
    And User clicks on HCP 365 module from main menu
    When User change filters of HCP Dashboard Overview tabs
    Then Verify data after change in filter of HCP Dashboard
    Examples:
      | USER    | ADVERTISER | TYPE    | CLIENT | ExpectedOutput | User Type |
      | admin11 |            | Regular |        | XYZ            | Internal  |
