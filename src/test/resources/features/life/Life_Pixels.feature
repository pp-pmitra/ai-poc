Feature: LIFE Regression - Create Pixel of following types:
  1. Retargeting Pixel using Javascript pixel type
  2. Retargeting Pixel using Image pixel type
  3. Smart Pixel with associated Smart List
  4. Smart Pixel without associated Smart List
  5. Conversion Pixel

  Background:
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"
    And User navigates to Pixels page
    Then Verify the tabs displayed on the Pixels page
    Then Verify the Advertiser dropdown and search box are displayed on the Pixels page
    When User clicks on Add Pixel button
    Then Verify the Create New Pixel panel and types of Pixel

  @regression
  Scenario Outline: Manage a Retargeting Pixel using Javascript pixel type (Create, Edit and Remove)
    And User selects the "<PIXEL_TYPE>" type
    When User tries to save the Retargeting pixel without entering any details, an error message should be displayed
    And User enters the pixel details as "<PIXEL_NAME>" "<ADVERTISER>"
    And User selects the "JavaScript" pixel
    And User saves the pixel
    Then Verify the pixel is saved successfully, search for it by name, and confirm it is displayed in the pixel list
    When User edits the name of the created "<PIXEL_TYPE>"
    Then Verify the "<PIXEL_TYPE>" gets updated successfully
    When User removes the created pixel
    Then Verify the pixel gets removed successfully
    Then Verify the removed pixel should not be displayed in the pixel list
    Examples:
      | PIXEL_TYPE        | PIXEL_NAME  | ADVERTISER   |
      | Retargeting Pixel | Retargeting | Z_Automation |

  @regression
  Scenario Outline: Manage a Retargeting Pixel using Image pixel type (Create, Edit and Remove)
    And User selects the "<PIXEL_TYPE>" type
    When User tries to save the Retargeting pixel without entering any details, an error message should be displayed
    And User enters the pixel details as "<PIXEL_NAME>" "<ADVERTISER>"
    And User selects the "Image" pixel
    And User saves the pixel
    Then Verify the pixel is saved successfully, search for it by name, and confirm it is displayed in the pixel list
    When User edits the name of the created "<PIXEL_TYPE>"
    Then Verify the "<PIXEL_TYPE>" gets updated successfully
    When User removes the created pixel
    Then Verify the pixel gets removed successfully
    Then Verify the removed pixel should not be displayed in the pixel list
    Examples:
      | PIXEL_TYPE        | PIXEL_NAME  | ADVERTISER             |
      | Retargeting Pixel | Retargeting | CacheTestAdvertise232n |

  @regression
  Scenario Outline: Manage a Smart Pixel with associated Smart List (Create and Edit)
    And User selects the "<PIXEL_TYPE>" type
    When User selects "<ADVERTISER>" as advertiser
    Then Verify the Smart Pixel name is auto populated with "<ADVERTISER>" and Smart Pixel text
    And User selects the associated campaign
    And User saves the pixel
    Then Verify the smart pixel is saved successfully, search for it by name, and confirm it is displayed in the pixel list
    And User adds the associated Smart List and enters list details as "<LIST_NAME>"
    Then Verify the selected "<ADVERTISER>" and Smart Pixel
    Then Save and Verify the list gets saved successfully
    And User navigates to Pixels page
    And User selects the created Smart Pixel
    Then Verify the selected Smart List should be reflected in the Associated Smartlists tab
    And User navigates to the Pixel Codes tab
    When User edits the name of the created "<PIXEL_TYPE>"
    Then Verify the "<PIXEL_TYPE>" gets updated successfully
    Then Verify user should not be able to deactivate the Smart Pixel if any Smart list is associated with it
    Examples:
      | PIXEL_TYPE  | ADVERTISER     | LIST_NAME   |
      | Smart Pixel | 01- Advertiser | Smart_Pixel |

  @regression
  Scenario Outline: Manage a Smart Pixel without associated Smart List (Create, Edit and Deactivate)
    And User selects the "<PIXEL_TYPE>" type
    When User selects "<ADVERTISER>" as advertiser
    Then Verify the Smart Pixel name is auto populated with "<ADVERTISER>" and Smart Pixel text
    And User selects the associated campaign
    And User saves the pixel
    Then Verify the smart pixel is saved successfully, search for it by name, and confirm it is displayed in the pixel list
    When User edits the name of the created "<PIXEL_TYPE>"
    Then Verify the "<PIXEL_TYPE>" gets updated successfully
    When User deactivates the created pixel
    Then Verify the pixel gets deactivated successfully
    Then Verify the deactivated pixel should not be displayed in the pixel list
    Examples:
      | PIXEL_TYPE  | ADVERTISER       |
      | Smart Pixel | 1Demo Advertiser |

  @regression
  Scenario Outline: Manage a Conversion Pixel (Create, Edit and Remove)
    And User selects the "<PIXEL_TYPE>" type
    When User tries to save the Conversion pixel without entering any details, an error message should be displayed
    And User enters the pixel details as "<PIXEL_NAME>" "<ADVERTISER>" "<SCOPE>" "<TYPE>"
    And User saves the pixel
    Then Verify the pixel is saved successfully, search for it by name, and confirm it is displayed in the pixel list
    When User edits the name of the created "<PIXEL_TYPE>"
    Then Verify the "<PIXEL_TYPE>" gets updated successfully
    When User removes the created pixel
    Then Verify the pixel gets removed successfully
    Then Verify the removed pixel should not be displayed in the pixel list
    Examples:
      | PIXEL_TYPE       | PIXEL_NAME | ADVERTISER       | SCOPE  | TYPE               |
      | Conversion Pixel | Conversion | 1Demo Advertiser | Person | Submit Application |