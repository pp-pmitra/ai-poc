Feature: HCP Explorer Workspace creation in Studio using filters, AI Configurator, and visualization
  1. Creation of Workspace in Studio
  2. Applying filters to the workspace by clicking Add Filter icon, building audience using AI prompts, and visualizing the audience
  3. Verify the creation of Draft workspace in Studio application.
  4. Verify the presence of Draft workspaces in Workspace Management page for external user.
  5. Verify the editing and publishing of Draft workspaces in Studio application.

  Background:
    Given This scenario will be executed in the "Pre-release" environment as a "User"
    And "Studio" application is logged in successfully with Account "automation@pulsepoint"
    When User navigates to Administrative section
    And User navigates to Accounts Tab
    And User searches the account "PP engineering test" and checks Studio permissions
    And User clicks PulsePoint icon to navigate back to Life
    And User navigates to Studio application

  @regression
  Scenario Outline: Create and save HCP Explorer workspace with specific filters
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And Verify that advertiser field is disabled and displayed in "rgba(34, 34, 34, 0.55)" after saving the workspace
    And User applies the filter and selects option
      | FilterName           | Option                                                                                                                  |
      | NPI Age              | Below 25, 25 to 35, 35 to 45, 45 to 55, 55 to 65, 65 or Above                                                           |
      | NPI Gender           | Female, Male, Unknown                                                                                                   |
      | Graduation Year      | 1900-2025                                                                                                               |
      | Net Worth            | Less than $50٫000, $100٫000 to $249٫999, $250٫000 to $499٫999, $500٫000 or above                                        |
      | Number of Patients   | Below 5, 6 to 20, 21 to 50, 51 to 100, 101 to 200, 201 to 300, 301 to 400, 401 to 500, 501 to 1000, 1001 or above       |
      #| Reachable Audience   | Yes                                                                                                                     |
      | Patient Age          | Below 25, 25 to 35, 35 to 45, 45 to 55, 55 to 65, 65 or Above                                                           |
      | Patient Gender       | Female, Male, Unknown                                                                                                   |
      | Years Practiced      | Below 5, 5 to 10, 10 to 15, 15 to 20, 20 to 25, 25 to 30, 30 to 35, 35 to 40, 40 to 45, 45 to 50, 50 or Above           |
      | Facility Name        | Visiting Nurse Service, Ahs Hospital Corp, Centrastate, Robert Wood Johnson University Hospital, Montclair Hospital Llc |
      | State                | New                                                                                                                     |
      | Profession           | Physician                                                                                                               |
      | Specialty            | Foot & Ankle Surgery, Internal Medicine                                                                                 |
      #| NPI List Name      | Large file test                                                                                                         |
      | Medical School       | New York College                                                                                                        |
      | Patient Facility     | .Arizona Autism United٫ Inc.                                                                                            |
      | Prescriptions        | .Insulin Aspart Protamine And Insulin Aspart                                                                            |
      | Prescribing behavior | .Insulin Aspart Protamine And Insulin Aspart                                                                            |
      | Diagnoses            | ABO incompatibility w hemolytic transfs react٫ unsp٫ subs                                                               |
      | Procedures           | Abatacept injection                                                                                                     |
      | IAB                  | Travel                                                                                                                  |
      | MeSH                 | Anatomy                                                                                                                 |
    And User clicks on Ok and closes the filter popup
    Then Verify that the applied filters are displayed correctly
    And User saves the "HCP Explorer" workspace
    Then Verify the "HCP Explorer" Workspace is saved
    Examples:
      | ADVERTISER | WORKSPACE_NAME |
      | Abbvie     | Explorer       |

  @regression
  Scenario Outline: Create and save HCP Explorer workspace by building audience using AI Configurator - <AI_PROMPT>
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And Verify that advertiser field is disabled and displayed in "rgba(34, 34, 34, 0.55)" after saving the workspace
    And User clicks on AI Configurator and build audience using the AIPrompt "<AI_PROMPT>"
    Then Verify the filter is applied correctly "<PRIMARY_FILTERS>"
#    And User saves the workspace
#    Then Verify the HCP Explorer Workspace is saved
    Examples:
      | ADVERTISER | WORKSPACE_NAME | AI_PROMPT                                                                                                                                    | PRIMARY_FILTERS                                  |
      | Abbvie     | Explorer       | Select Cardiovascular Professionals who are reachable in California state and also exclude net worth Less than $50٫000                       | Net Worth, Specialty Filter, State               |
      | Abbvie     | Explorer       | Filter doctors by their gender, how long they've been practicing, and then narrow down patients by their age groups.                         | Clinical Recency, NPI Gender, Years Practiced    |
      | Abbvie     | Explorer       | Look for doctors who graduated within a specific time frame, are located in certain states, and then focus on patient gender.                | Graduation Year, Profession, State               |
      | Abbvie     | Explorer       | Find doctors within certain age ranges, with specific professions, and narrow by wealth.                                                     | NPI Age, Net Worth, Profession                   |
      | Abbvie     | Explorer       | Search for doctors from specific medical schools with third-level specialties and certain patient counts.                                    | Clinical Recency, Number of Patients, Profession |
      | Abbvie     | Explorer       | Focus on patients of specific age groups and genders.                                                                                        | Patient Age, Patient Gender                      |
      | Abbvie     | Explorer       | Filter doctors by their gender and age, then look for those who graduated from particular medical schools.                                   | NPI Age, NPI Gender, Profession                  |
      | Abbvie     | Explorer       | Doctors with specific experience, states, wealth level, and patient count.                                                                   | Clinical Recency, Profession, Years Practiced    |
      | Abbvie     | Explorer       | Select cardiovascular specialist who are graduated before 2004 in New York or California                                                     | Graduation Year, Specialty Filter, State         |
      | Abbvie     | Explorer       | Decide if you want to reach a broader audience, then focus on patients of specific age groups and genders.                                   | Clinical Recency, Patient Age, Patient Gender    |
      | Abbvie     | Explorer       | Create a mix of NPIs by choosing profession and specialties in heart and brain surgery.                                                      | Profession, Specialty Filter                     |
      | Abbvie     | Explorer       | Find doctors based on their years of experience, the states they work in, their wealth level, and how many patients they typically see.      | Clinical Recency, Profession, Years Practiced    |
      | Abbvie     | Explorer       | Pick the types of NPI lists you want, look for doctors who graduated during a specific period, and then target certain hospitals or clinics. | Graduation Year, Profession                      |

  @regression
  Scenario Outline: Create and save HCP Explorer workspace by applying filters one by one and validating NPI details are refined
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And Verify that advertiser field is disabled and displayed in "rgba(34, 34, 34, 0.55)" after saving the workspace
    And User applies the following filters one by one and checks that NPI details are refined after each filter:
      | FilterName         | Option                                                                                                                  |
      | NPI Age            | Below 25, 25 to 35, 35 to 45, 45 to 55, 55 to 65, 65 or Above                                                           |
      | NPI Gender         | Female, Male, Unknown                                                                                                   |
      | Graduation Year    | 1900-2025                                                                                                               |
      | Net Worth          | Less than $50٫000, $100٫000 to $249٫999, $250٫000 to $499٫999, $500٫000 or above                                        |
      | Number of Patients | Below 5, 6 to 20, 21 to 50, 51 to 100, 101 to 200, 201 to 300, 301 to 400, 401 to 500, 501 to 1000, 1001 or above       |
      #| Reachable Audience | Yes                                                                                                                     |
      | Patient Age        | Below 25, 25 to 35, 35 to 45, 45 to 55, 55 to 65, 65 or Above                                                           |
      | Patient Gender     | Female, Male, Unknown                                                                                                   |
      | Years Practiced    | Below 5, 5 to 10, 10 to 15, 15 to 20, 20 to 25, 25 to 30, 30 to 35, 35 to 40, 40 to 45, 45 to 50, 50 or Above           |
      | Facility Name      | Visiting Nurse Service, Ahs Hospital Corp, Centrastate, Robert Wood Johnson University Hospital, Montclair Hospital Llc |
      | State              | New                                                                                                                     |
      | Profession         | Physician                                                                                                               |
      | Specialty          | Foot & Ankle Surgery, Internal Medicine                                                                                 |
      | NPI List Name      | Large file test                                                                                                         |
      | Medical School     | New York College                                                                                                        |
    Examples:
      | ADVERTISER | WORKSPACE_NAME |
      | Abbvie     | Explorer       |

  @regression
  Scenario Outline: Create and save HCP Explorer workspace using NPI Cross Filters
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And Verify that advertiser field is disabled and displayed in "rgba(34, 34, 34, 0.55)" after saving the workspace
    And User applies the filter and selects option
      | FilterName | Option                                                        |
      | NPI Age    | Below 25, 25 to 35, 35 to 45, 45 to 55, 55 to 65, 65 or Above |
      | NPI Gender | Female, Male, Unknown                                         |
    And User clicks on Ok and closes the filter popup
    Then Verify that the applied filters are displayed correctly
    And User hovers over the dashboard filters, selects the region with maximum NPIs and clicks on it
      | NPI Geographic Location    |
      | NPI Facilities Geography   |
      | Patient Age Range          |
      | Patient Gender             |
      | Patient Distribution       |
      | Net Worth                  |
      | NPI Gender                 |
      | NPI Age Range              |
      | Years Practiced            |
      | Top 20 Market Areas        |
      | Top 20 Professions         |
      | Top 20 Specialties         |
      | Top 20 Insurance Providers |
      | Top 20 Prescriptions       |
      | Top 20 Diagnoses           |
      | Top 20 Procedures          |
      | Top 20 MeSH Categories     |
      | Top 20 IAB Categories      |
    And Verify that dashboard filters are displayed correctly in Filter section
    And User saves the "HCP Explorer" workspace
    Then Verify the "HCP Explorer" Workspace is saved
    And Verify dashboard filters are merged with Primary filters
    And Fetch and verify that NPI details are refined
    Examples:
      | ADVERTISER | WORKSPACE_NAME |
      | Abbvie     | Explorer       |

  @regression
  Scenario Outline: Manage operations on Workspace - Rename, Duplication, and Delete on HCP Explorer workspace
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And Verify that advertiser field is disabled and displayed in "rgba(34, 34, 34, 0.55)" after saving the workspace
    And User applies the filter and selects option
      | FilterName | Option                |
      | NPI Age    | Below 25, 25 to 35,   |
      | NPI Gender | Female, Male, Unknown |
    And User clicks on Ok and closes the filter popup
    Then Verify that the applied filters are displayed correctly
    And User saves the "HCP Explorer" workspace
    Then Verify the "HCP Explorer" Workspace is saved
    And User clicks Edit button and updates workspace name to "<WORKSPACE_NAME_EDIT>"
    Then Verify the Workspace is updated with edited name
    And User saves the "HCP Explorer" workspace
    Then Verify the "HCP Explorer" Workspace is saved
    And Navigate to workspace dashboard
    And User searches the workspace created to perform Actions from More menu
    And User selects the "Rename" option by clicking More Actions menu
    And Verify user is able to rename the workspace as "<NEW_WORKSPACE_NAME>"
    And User is able to search the workspace after performing operation - "Rename"
    And User searches the workspace created to perform Actions from More menu
    And User selects the "Duplicate" option by clicking More Actions menu
    And Verify user is able to duplicate the workspace
    And User is able to search the workspace after performing operation - "Duplicate"
    And User searches the workspace created to perform Actions from More menu
    And User selects the "Delete" option by clicking More Actions menu
    And Verify user is able to delete the workspace
    Examples:
      | ADVERTISER | WORKSPACE_NAME | NEW_WORKSPACE_NAME | WORKSPACE_NAME_EDIT |
      | Abbvie     | Explorer       | New_Explorer_      | Edit_Explorer       |

  @regression
  Scenario Outline: Validate Clinical and Contextual Recency filters in HCP Explorer workspace
    When User clicks on Create New Workspace
    Then User sees the types of workspaces they have permissions for
    And User clicks on "HCP Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    And Verify that advertiser field is disabled and displayed in "rgba(34, 34, 34, 0.55)" after saving the workspace
    And User applies "Clinical" filter, selects filter options as below and verifies the clinical recency filter is updated correctly
      | FilterName           | Option                                                  | Recency  |
      | Prescriptions        | 100％ Mineral Sunscreen                                  | 1 Month  |
      | Prescribing behavior | 100％ Mineral Broad Spectrum Sunscreen Spf 30            | 3 Months |
      | Diagnoses            | Maternal care for face٫ brow and chin presentation٫ oth | 6 Months |
      | Procedures           | Removal of face wrinkles                                | 1 Year   |
    And User applies "Contextual" filter, selects filter options as below and verifies the clinical recency filter is updated correctly
      | FilterName | Option               | Recency |
      | IAB        | Arts & Entertainment | 1 Day   |
      | MeSH       | Anatomy              | 1 Week  |
    And User saves the "HCP Explorer" workspace
    Then Verify the "HCP Explorer" Workspace is saved
    Examples:
      | ADVERTISER | WORKSPACE_NAME |
      | Abbvie     | Explorer       |

  @regression
  Scenario Outline: Validate the persistence of applied filters (Workspace Type, Advertiser, Created By, Workspace Name) on the Studio Workspace Details page
    When User selects the workspace type "<WORKSPACE_TYPE>"
    And User selects "<ADVERTISER>" from the Studio Workspace Advertiser dropdown
    And User selects "<CREATED_BY>" from the Studio Workspace Created By dropdown
    And User searches for a workspace by name using the search box on the Workspace Details page
    And User navigates to another page within Studio and then returns to the workspace list page
    Then User verifies that the selected filters, dropdown values, and search input remain persistent unless they are manually deselected or cleared - "<WORKSPACE_TYPE>", "<ADVERTISER>", "<CREATED_BY>"
    And Verify on refresh of the page, the filters are reset and search input is cleared
    Examples:
      | WORKSPACE_TYPE | ADVERTISER | CREATED_BY                     |
      | HCP Explorer   | Abbvie     | ppqa_automation@pulsepoint.com |

  @regression
  Scenario Outline: Create and save a Draft workspace with specific filters and verify visibility with External User
    When User clicks on Create New Workspace
    And User clicks on "HCP Explorer" workspace
    And User selects the advertiser "<ADVERTISER>"
    And User updates the workspace name as "<WORKSPACE_NAME>"
    Then User applies the filter and selects option
      | FilterName | Option                                                        |
      | NPI Age    | Below 25, 25 to 35, 35 to 45, 45 to 55, 55 to 65, 65 or Above |
    And User clicks on Ok and closes the filter popup
    Then Verify that the applied filters are displayed correctly
    And User selects the Draft option as "<DRAFT_OPTION>"
    And User saves the "HCP Explorer" workspace
    And Internal user logs out from the application
    Given This scenario will be executed in the "Pre-release" environment as a "External User"
    And "Studio" application is logged in successfully with Account "<ACCOUNT_NAME>"
    #And External User switches the "<ACCOUNT_NAME>"account in Studio application -- commiting this step for future changes, if pp engineering test account does not appears in external user account list in studio application
    When External user searches the workspace name in studio application with "<DRAFT_OPTION>" draft option
    Then External user verifies whether the workspace with "<DRAFT_OPTION>" is visible in workspace management page
    Examples:
      | ADVERTISER |   DRAFT_OPTION | WORKSPACE_NAME | ACCOUNT_NAME        |
      | Abbvie     |   Public       | Explorer       | PP engineering test |
      | Abbvie     |   Private      | Explorer       | PP engineering test |
