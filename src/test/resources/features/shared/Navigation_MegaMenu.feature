Feature: Mega Menu Navigation - Verify all accessible menu links navigate to correct destination pages
  It covers below points
  1. Verify primary Mega Menu module links navigate to respective dashboards
  2. Verify Mega Menu Links submenu items navigate to respective pages
  3. Verify Campaign Reporting submenu items navigate to respective pages

  Background:
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"

  @todo
  @regression
  Scenario Outline: Verify Mega Menu primary module link "<MENU_ACTION>" navigates to "<TARGET_PAGE>"
    When User clicks Hamburger icon to open Mega Menu
    And User clicks on "<MENU_ACTION>" link in Mega Menu
    Then Verify user is navigated to "<TARGET_PAGE>" page

    Examples:
      | MENU_ACTION | TARGET_PAGE       |
      | Campaigns   | CampaignDashboard |
      | Studio      | StudioDashboard   |
      | Signal      | HCPLanding        |

  @todo
  @regression
  Scenario Outline: Verify Mega Menu Links submenu item "<SUBMENU_ACTION>" navigates to "<TARGET_PAGE>"
    When User clicks Hamburger icon to open Mega Menu
    And User clicks on Menu Links option in Mega Menu
    And User clicks on "<SUBMENU_ACTION>" link in Menu Links
    Then Verify user is navigated to "<TARGET_PAGE>" page

    Examples:
      | SUBMENU_ACTION      | TARGET_PAGE            |
      | NPI Lists           | NPIListsPage           |
      | Domain & App Lists  | DomainAndAppListsPage  |
      | Keyword Lists       | KeywordListsPage       |
      | IP Address Lists    | IPAddressListsPage     |
      | Email Lists         | EmailListsPage         |
      | Curated Markets     | CuratedMarketsPage     |
      | Pixels              | PixelsPage             |
      | Targeting Templates | TargetingTemplatesPage |
      | Creative Library    | CreativeLibraryPage    |
      | Smart Actions       | SmartActionsPage       |

  @todo
  @regression
  Scenario Outline: Verify Campaign Reporting submenu item "<REPORT_ACTION>" navigates to "<TARGET_PAGE>"
    When User clicks Hamburger icon to open Mega Menu
    And User clicks on Menu Links option in Mega Menu
    And User clicks on Campaign Reporting link in Menu Links
    And User clicks on "<REPORT_ACTION>" link in Campaign Reporting submenu
    Then Verify user is navigated to "<TARGET_PAGE>" page

    Examples:
      | REPORT_ACTION     | TARGET_PAGE          |
      | Run a Report      | RunReportPage        |
      | Report Templates  | ReportTemplatesPage  |
      | Scheduled Reports | ScheduledReportsPage |
      | Generated Reports | GeneratedReportsPage |