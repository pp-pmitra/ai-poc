Feature: LIFE Regression - Create a Creative Library and verify filters, sort, search options for all creative types including Bulk Upload
  It covers:
  1. Creating creatives via UI for the following types:
  a.  Display
  b.  Video
  c.  Audio
  d.  Native Display
  e.  Native Video
  f.  Search Extension
  2. Bulk uploading creatives for supported types
  3. Verifying available filters, sort, and search functionality on the Creative Library page

  Background:
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And Verify Campaign Dashboard is displayed with title "Campaigns"
    And User clicks Creative Library options present under Activation tab
    Then Verify Creative Library page is displayed

  @regression
  Scenario: Verify filters under Active and Archived activity, clear-all button, sort, and search options on the Creative Library page
    And Check Activity buttons "Active" and verify following filters are available and working
      | Advertiser      | 01- Advertiser |
      | Creative Status | Approved       |
      | Ad Sizes        | 1025x800       |
      | CreatedBy       | Anand          |
      | Creative Type   | Image, Video   |
    And Check Activity buttons "Archived" and verify following filters are available and working
      | Advertiser      | Amgen       |
      | Creative Status | Approved    |
      | Ad Sizes        | 320x50      |
      | CreatedBy       | Anand       |
      | Creative Type   | Html, Video |
    And Verify the following sort options are available and working
      | Name-Asc          |
      | Name-Desc         |
      | ID-Asc            |
      | ID-Desc           |
      | Last Updated-Asc  |
      | Last Updated-Desc |
    And Verify Search Box is available and working
      | VideoURLCreative |
      | Manual           |
      | BulkUpload       |
      | 120x600          |
    And User checks Copy option is working for creative and verify details before and after saving the creative
    And User checks Archive option is working for creative and verify the creative is moved to "Archived" tab
    And User checks Unarchive option is working for creative and verify the creative is moved to "Active" tab

  @regression
  Scenario Outline: Verify filter's (Creative Status, Ad Sizes, Creative Type) list of value available under Active and Archived tabs
    When User clicks on "<ACTIVITY_TAB_NAME>" tab and verify following filters value
      | Creative Status | PendingApproval, Approved, Denied                                                                                                                                                                                                                                                                                                                                                                                                                                           |
      | Ad Sizes        | 1024x576, 1025x800, 1040x875, 1050x800, 1050x880, 1200x200, 1200x750, 120x20, 120x240, 120x600, 123x123, 125x125, 1280x720, 1500x900, 160x600, 168x28, 170x130, 180x150, 1920x1080, 200x200, 200x600, 216x36, 234x60, 250x250, 300x100, 300x250, 300x50, 300x600, 301x601, 320x100, 320x480, 320x50, 333x333, 336x280, 400x400, 468x60, 480x320, 640x100, 640x360, 640x480, 700x500, 728x200, 728x90, 768x576, 800x200, 800x250, 800x600, 854x480, 970x250, 970x30, 970x550 |
      | Creative Type   | Image, Html, Video, Search Extension, Html5, Audio, Native Display, Native Video                                                                                                                                                                                                                                                                                                                                                                                            |
    Examples:
      | ACTIVITY_TAB_NAME |
      | Active            |
      | Archived          |

  @regression
  Scenario Outline: Verify page alignment for different pagination values - "<PAGINATION_VALUE>"
    When User selects pagination values "<PAGINATION_VALUE>" from the dropdown
    And Verify pagination is working properly on the Creative Library page
    Examples:
      | PAGINATION_VALUE |
      | 10               |
      | 20               |
      | 50               |
      | 100              |

  @regression
  Scenario: Verify deletion on creatives associated with a Campaign and on creatives that are not associated with any Campaign
    When User assigns a campaign to the creative using "Bulk Assign" option
      | 01- Advertiser |
    Then Verify user is not able to delete a creative associated with a Campaign and appropriate error message is displayed
    And User deletes a creative not associated with any Campaign
    And Verify the creative is removed from the Creative Library page

  @regression
  Scenario: Verify Bulk actions on creatives not associated with any Campaign
    When User performs "Bulk Archive" action using "Archive Creatives" option on multiple creatives - "2" and verifies the selected creatives are moved to "Archived" tab
    And User performs Bulk approve action using "Bulk Approve" option on multiple creatives - "3" with status other than Approved and verifies the selected creatives are marked as "Approved"
      | PendingApproval, Denied |
    And User performs "Bulk Unarchive" action using "Unarchive Creatives" option on multiple creatives - "3" and verifies the selected creatives are moved to "Active" tab

  @regression
  Scenario: Verify Preview option on a creative
    When User clicks on Preview icon for a creative from Creative Library page
    Then Verify Creative Preview tab is displayed with correct creative name
    And Verify user is able to close the Creative Preview tab
    And User searches the creative and clicks the creative details from Creative Library page
    And User clicks on Preview link from Creative Details page
    And Verify Creative Preview tab is displayed with correct creative name
    And Verify user is able to close the Creative Preview tab

  @regression
  Scenario Outline: Create new Creative Library entries for multiple Creative Types
    # Display
    When Verify data persistence when user creates and saves "Display" creative using details "<ADVERTISER>" as Advertiser, "<CREATIVE_NAME>" as Creative Name, "<ADVERTISER_DSA>", "<FINANCER>" and below Creative attributes
      | CreativeType | CreativeAttributes                                                                                                      |
      | Html         | HTMLCode:<html>Auto_Creative</html>, Size:1024x576, DomainLanding:pulsepoint.com                                        |
      | Image        | ImageFile:Display_Image.jpg, Size:1024x576, ClickThroughURL:https://www.pulsepoint.com, DomainLanding:pulsepoint.com    |
      | Html5        | ArchiveFile:HTML5_Template.zip, Size:1024x576, ClickThroughURL:https://www.pulsepoint.com, DomainLanding:pulsepoint.com |
    # Audio
    And Verify data persistence when user creates and saves "Audio" creative using details "<ADVERTISER>" as Advertiser, "<CREATIVE_NAME>" as Creative Name, "<ADVERTISER_DSA>", "<FINANCER>" and below Creative attributes
      | CreativeType | CreativeAttributes                                                                             |
      | Upload       | FileName:Creative_Audio.mp3, AdvertiserDomain:pulsepoint.com, IAB:Profane                      |
      | Audio URL    | URL:https://www.pulsepoint.com, Durations:60, AdvertiserDomain:pulsepoint.com, IAB:Profane     |
      | VAST URL     | VASTURL:https://www.pulsepoint.com, Durations:60, AdvertiserDomain:pulsepoint.com, IAB:Profane |
      | VAST XML     | VASTXML:https://www.pulsepoint.com, Durations:60, AdvertiserDomain:pulsepoint.com, IAB:Profane |
    # Video
    And Verify data persistence when user creates and saves "Video" creative using details "<ADVERTISER>" as Advertiser, "<CREATIVE_NAME>" as Creative Name, "<ADVERTISER_DSA>", "<FINANCER>" and below Creative attributes
      | CreativeType | CreativeAttributes                                                                                                                                                        |
      | Upload       | FileName:Creative_Video.mp4, ClickThroughURL:pulsepoint.com, AdvertiserDomain:pulsepoint.com, IAB:Profane                                                                 |
      | Video URL    | Type:MP4, URL:https://www.pulsepoint.com, Durations:60, Width:100, Height:100, ClickThroughURL:pulsepoint.com, AdvertiserDomain:pulsepoint.com, IAB:Profane               |
      | VAST URL     | Type:STANDARD VAST, VASTURL:https://www.pulsepoint.com, Durations:60, Width:100, Height:100, ClickThroughURL:pulsepoint.com, AdvertiserDomain:pulsepoint.com, IAB:Profane |
      | VAST XML     | Type:VPAID, VASTXML:https://www.pulsepoint.com, Durations:60, Width:100, Height:100, ClickThroughURL:pulsepoint.com, AdvertiserDomain:pulsepoint.com, IAB:Profane         |
    # Search Extension
    And Verify data persistence when user creates and saves "Search Extension" creative using details "<ADVERTISER>" as Advertiser, "<CREATIVE_NAME>" as Creative Name, "<ADVERTISER_DSA>", "<FINANCER>" and below Creative attributes
      | CreativeType | CreativeAttributes                                                                                                                                                           |
      | Search       | Size:300x600, Headline:Test Automation Headline, Description:Test Automation Description, DisplayURL:https://www.pulsepoint.com, ClickThroughURL:pulsepoint.com, IAB:Profane |
    # Native Display
    And Verify data persistence when user creates and saves "Native Display" creative using details "<ADVERTISER>" as Advertiser, "<CREATIVE_NAME>" as Creative Name, "<ADVERTISER_DSA>", "<FINANCER>" and below Creative attributes
      | CreativeType   | CreativeAttributes                                                                                                                                                                                                                                      |
      | Native Display | ClickThroughURL:pulsepoint.com, DomainLanding:pulsepoint.com, IAB:Profane, Headline:Test Automation Headline, SponsoredBy:PulsePoint, Description:Test Automation Description, DisplayURL:https://www.pulsepoint.com, ImageFile:NativeDisplay_Image.jpg |
    # Native Video
    And Verify data persistence when user creates and saves "Native Video" creative using details "<ADVERTISER>" as Advertiser, "<CREATIVE_NAME>" as Creative Name, "<ADVERTISER_DSA>", "<FINANCER>" and below Creative attributes
      | CreativeType | CreativeAttributes                                                                                                                                                                                                                                                                                                   |
      | Upload       | FileName:Creative_Video.mp4, ClickThroughURL:pulsepoint.com, AdvertiserDomain:pulsepoint.com, IAB:Profane, Headline:Test Automation Headline, SponsoredBy:PulsePoint, Description:Test Automation Description, DisplayURL:https://www.pulsepoint.com                                                                 |
      | Video URL    | Type:FLV, URL:https://www.pulsepoint.com, Durations:60, Width:100, Height:100, ClickThroughURL:pulsepoint.com, AdvertiserDomain:pulsepoint.com, IAB:Profane, Headline:Test Automation Headline, SponsoredBy:PulsePoint, Description:Test Automation Description, DisplayURL:https://www.pulsepoint.com               |
      | VAST URL     | Type:STANDARD VAST, VASTURL:https://www.pulsepoint.com, Durations:60, Width:100, Height:100, ClickThroughURL:pulsepoint.com, AdvertiserDomain:pulsepoint.com, IAB:Profane, Headline:Test Automation Headline, SponsoredBy:PulsePoint, Description:Test Automation Description, DisplayURL:https://www.pulsepoint.com |
      | VAST XML     | Type:VPAID, VASTXML:https://www.pulsepoint.com, Durations:60, Width:100, Height:100, ClickThroughURL:pulsepoint.com, AdvertiserDomain:pulsepoint.com, IAB:Profane, Headline:Test Automation Headline, SponsoredBy:PulsePoint, Description:Test Automation Description, DisplayURL:https://www.pulsepoint.com         |
    Then Verify the newly created creative is displayed in the Creative Library page
    Examples:
      | ADVERTISER     | CREATIVE_NAME | ADVERTISER_DSA | FINANCER      |
      | 01- Advertiser | Creative      | Auto_DSA       | Auto_Financer |

  @regression
  Scenario: Verify Bulk Creative Upload panel
    Given User clicks Bulk Upload button on Creative Library page
    Then Verify Bulk Upload panel is displayed with following options - Creative Type and Advertiser Dropdown
    And Verify the availability of below Creative Type options and the Default option is "Display"
      | Display |
      | HTML    |
      | Native  |
      | Video   |
    And Verify the Advertiser dropdown is displaying all Advertisers mapped to the logged in account

  @regression
  Scenario Outline: Verify Bulk Creative Upload with Approval status as "<STATUS>"
    Given User clicks Bulk Upload button on Creative Library page
    When User selects the "<CREATIVE_TYPE>" creative type
    When The advertiser "<ADVERTISER>" is selected for "Display" creative the following sections are visible
      | Spreadsheet Template and Images |
      | Upload Creatives                |
      | Third Party Tracking Pixel/Tag  |
    And User enters "<ADVERTISER_DSA>", "<FINANCER>" mandatory fields data for Display creative
    And User uploads a valid file "<FILE_NAME>" for "Display" creative
    And User selects the Approval status "<STATUS>"
    And Verify an appropriate error message when user attempts to click the Preview or OK button without selecting a creative file
    Then Verify the header message for "<STATUS>" status
    And User uploads a valid file "<FILE_NAME>" for "<CREATIVE_TYPE>" creative and previews the creative details
    And User saves the creative
    And Verify the newly created creative is displayed in the Creative Library page
    Examples:
      | CREATIVE_TYPE | ADVERTISER     | ADVERTISER_DSA | FINANCER      | STATUS   | FILE_NAME                       |
      | Display       | 01- Advertiser | Auto_DSA       | Auto_Financer | Pending  | DisplayBulkUploadTemplate1.xlsx |
      | Display       | 01- Advertiser | Auto_DSA       | Auto_Financer | Approved | DisplayBulkUploadTemplate1.xlsx |
      | Display       | 01- Advertiser | Auto_DSA       | Auto_Financer | Denied   | DisplayBulkUploadTemplate1.xlsx |

  @regression
  Scenario Outline: Validate Bulk Upload Functionality and Field Requirements for Display Creatives
    Given User clicks Bulk Upload button on Creative Library page
    When User selects the "Display" creative type
    And The advertiser "<ADVERTISER>" is selected for "Display" creative the following sections are visible
      | Spreadsheet Template and Images |
      | Upload Creatives                |
      | Third Party Tracking Pixel/Tag  |
    And Verify under the "Spreadsheet Template and Images" section the options "Download Blank Template" and "Upload Images to Get Template With URLs" are available
    And User is able to download a blank template using the Download Blank Template option
    And Verify user is able to upload images "<IMAGE_FILENAME>" to get a template with URLs
    And Verify under the "Upload Creatives" section the fields "Campaign Restrict", "Spreadsheet", "Approval Status" are available
    And User is able to select a "<CAMPAIGN_NAME>" from the Campaign Restrict dropdown
    And User is able to browse and select a template "<FILE_NAME>" from the system
    And Verify default value of the Approval Status field is "Pending"
    And User selects the Approval status "<STATUS>"
    And Verify under the "Third Party Tracking Pixel/Tag" section the fields Add Third Party Tracking Pixel and Add DoubleVerify Pixel are available
    And User is able to click a third-party tracking pixel and add details "Test"
    And User is able to add a DoubleVerify pixel
    And User is able to delete third-party tracking pixel entries
    And An error message is displayed when a blank template "<BLANK_TEMPLATE>" is uploaded
    And User enters "<ADVERTISER_DSA>", "<FINANCER>" mandatory fields data for Display creative
    And User uploads a valid file "Downloaded" for "Display" creative and previews the creative details
    And User saves the creative
    And Verify the newly created creative is displayed in the Creative Library page and contains all the details entered during creation
    Examples:
      | ADVERTISER     | IMAGE_FILENAME    | CAMPAIGN_NAME | FILE_NAME                       | BLANK_TEMPLATE                      | ADVERTISER_DSA | FINANCER      | STATUS   |
      | 01- Advertiser | Display_Image.jpg | Creative      | DisplayBulkUploadTemplate1.xlsx | BlankDisplayBulkUploadTemplate.xlsx | Auto_DSA       | Auto_Financer | Approved |

  @regression
  Scenario Outline: Validate Bulk Upload Functionality and Field Requirements for HTML Creatives using file type "<FILE_TYPE>"
    Given User clicks Bulk Upload button on Creative Library page
    When User selects the "HTML" creative type
    And Verify Advertiser field should be mandatory
    And Verify that the Landing Domain field is mandatory when all other required fields, including "<ADVERTISER>" are filled
    And Verify that an appropriate error message is displayed when invalid data "<INVALID_LANDING_DOMAIN>" is entered for the Landing Domain
    And Verify only valid Landing Domain "<LANDING_DOMAIN>" values should be permitted
    And Verify default value of the File field should be "DCM File"
    And Verify default value of the Approval Status field is "Pending"
    And Verify default value of the AdChoices Icon should be "TopRight"
    And Verify default value of the Notes Column field should be "None"
    And Verify Rich Media checkbox should be present and selectable "Up"
    And Verify that the user is able to browse the computer, upload the following file types, and create creatives using details - "<ADVERTISER>", "<ADVERTISER_DSA>", "<FINANCER>", "<LANDING_DOMAIN>", "<STATUS>", "<CREATIVE_NAME>", "<SIZE>", "<DURATION>", "<FILE_TYPE>", "<FILE_NAME>"
    And Verify the newly created creative is displayed in the Creative Library page
    Examples:
      | ADVERTISER     | INVALID_LANDING_DOMAIN | LANDING_DOMAIN | ADVERTISER_DSA | FINANCER      | STATUS   | CREATIVE_NAME | SIZE    | DURATION | FILE_TYPE    | FILE_NAME              |
      | 01- Advertiser | test                   | www.google.com | Auto_DSA       | Auto_Financer | Approved | HTML_Creative | 800x250 | 60       | DCM File     | HTML_DCM.xls           |
      | 01- Advertiser | test                   | www.google.com | Auto_DSA       | Auto_Financer | Approved | HTML_Creative | 800x250 | 60       | PulsePoint   | HTML_Pulsepoint.xlsx   |
      | 01- Advertiser | test                   | www.google.com | Auto_DSA       | Auto_Financer | Approved | HTML_Creative | 800x250 | 60       | Adform       | HTML_Adform.xls        |
      | 01- Advertiser | test                   | www.google.com | Auto_DSA       | Auto_Financer | Approved | HTML_Creative | 800x250 | 60       | DoubleVerify | HTML_DoubleVerify.xlsx |
      | 01- Advertiser | test                   | www.google.com | Auto_DSA       | Auto_Financer | Approved | HTML_Creative | 800x250 | 60       | Flashtalking | HTML_Flashtalking.csv  |
      | 01- Advertiser | test                   | www.google.com | Auto_DSA       | Auto_Financer | Approved | HTML_Creative | 800x250 | 60       | Sizmek       | HTML_Sizmek.xlsx       |

  @regression
  Scenario Outline: Validate Bulk Upload Functionality for HTML Creatives by downloading the template and uploading the file with type "<FILE_TYPE>"
    Given User clicks Bulk Upload button on Creative Library page
    When User selects the "HTML" creative type
    And Verify that the user is able to browse the computer, upload the following file types, and create creatives using details - "<ADVERTISER>", "<ADVERTISER_DSA>", "<FINANCER>", "<LANDING_DOMAIN>", "<STATUS>", "<CREATIVE_NAME>", "<SIZE>", "<DURATION>", "<FILE_TYPE>", "<FILE_NAME>"
    And Verify the newly created creative is displayed in the Creative Library page
    Examples:
      | ADVERTISER     | LANDING_DOMAIN | ADVERTISER_DSA | FINANCER      | STATUS   | CREATIVE_NAME | SIZE    | DURATION | FILE_TYPE  | FILE_NAME         |
      | 01- Advertiser | www.google.com | Auto_DSA       | Auto_Financer | Approved | HTML_Creative | 800x250 | 60       | PulsePoint | Download Template |

  @regression
  Scenario Outline: Validate Bulk Upload Functionality and Field Requirements for Native Creatives
    Given User clicks Bulk Upload button on Creative Library page
    When User selects the "Native" creative type
    And The advertiser "<ADVERTISER>" is selected for "Native" creative the following sections are visible
      | Spreadsheet Template and Images |
      | Upload Creatives                |
    And Verify under the "Spreadsheet Template and Images" section the options "Download Blank Template" and "Upload Images to Get Template With URLs" are available
    And User is able to download a blank template using the Download Blank Template option
    And Verify user is able to upload images "<IMAGE_FILENAME>" to get a template with URLs
    And Verify under the "Upload Creatives" section the fields "Campaign Restrict", "Spreadsheet", "Approval Status" are available
    And Verify only valid Landing Domain "<LANDING_DOMAIN>" values should be permitted
    And Verify user is able to type in "<IAB>" categories
    And Verify default value of the AdChoices Icon should be "TopRight"
    And User is able to select a "<CAMPAIGN_NAME>" from the Campaign Restrict dropdown
    And Verify default value of the Approval Status field is "Pending"
    And User selects the Approval status "<STATUS>"
    And An error message is displayed when a blank template "<BLANK_TEMPLATE>" is uploaded
    And User enters "<ADVERTISER_DSA>", "<FINANCER>" mandatory fields data for Display creative
    And User uploads a valid file "Downloaded" for "Native" creative and previews the creative details
    And User saves the creative
    And Verify the newly created creative is displayed in the Creative Library page and contains all the details entered during creation
    Examples:
      | ADVERTISER     | IMAGE_FILENAME          | CAMPAIGN_NAME | BLANK_TEMPLATE                     | ADVERTISER_DSA | FINANCER      | LANDING_DOMAIN | IAB     | STATUS   |
      | 01- Advertiser | NativeDisplay_Image.jpg | Creative      | BlankNativeBulkUploadTemplate.xlsx | Auto_DSA       | Auto_Financer | www.google.com | Profane | Approved |

  @regression
  Scenario Outline: Validate Bulk Upload Functionality and Field Requirements for Video Creatives
    Given User clicks Bulk Upload button on Creative Library page
    When User selects the "Video" creative type
    And Verify Creative Type field is present and default value is "Standard VAST"
    And Verify Advertiser field should be mandatory
    And The advertiser "<ADVERTISER>" is selected for "Video" creative the following sections are visible
      | Upload Creatives |
    And Verify that the Clickthrough URL and Landing Domain fields are validated as mandatory when all other required fields are filled
    And Verify only valid Landing Domain "<LANDING_DOMAIN>" values should be permitted
    And Verify only valid Clickthrough URL "<CLICKTHROUGH_URL>" values should be permitted
    And Verify default value of the File field should be "DCM File"
    And Verify default value of the Approval Status field is "Pending"
    And Verify default value of the AdChoices Icon should be "TopRight"
    #And Verify user is able to upload images "<IMAGE_FILENAME>" to get a template with URLs
    And Verify default value of the Notes Column field should be "None"
    And Verify that the user is able to browse the computer, upload the following file types, and create creatives using details - "<ADVERTISER>", "<ADVERTISER_DSA>", "<FINANCER>", "<LANDING_DOMAIN>", "<STATUS>", "<CREATIVE_NAME>", "<SIZE>", "<DURATION>", "<FILE_TYPE>", "<FILE_NAME>"
    And Verify the newly created creative is displayed in the Creative Library page
    Examples:
      | ADVERTISER     | CLICKTHROUGH_URL   | LANDING_DOMAIN | ADVERTISER_DSA | FINANCER      | STATUS   | CREATIVE_NAME  | IMAGE_FILENAME    | SIZE    | DURATION | FILE_TYPE    | FILE_NAME               |
      | 01- Advertiser | AutomationTest.com | www.google.com | Auto_DSA       | Auto_Financer | Approved | Video_Creative | Display_Image.jpg | 800x250 | 60       | DCM File     | Video_DCM.xls           |
      | 01- Advertiser | AutomationTest.com | www.google.com | Auto_DSA       | Auto_Financer | Approved | Video_Creative | Display_Image.jpg | 800x250 | 60       | DoubleVerify | Video_DoubleVerify.xls  |
      | 01- Advertiser | AutomationTest.com | www.google.com | Auto_DSA       | Auto_Financer | Approved | Video_Creative | Display_Image.jpg | 800x250 | 60       | Flashtalking | Video_FlashTalking.xlsx |
      | 01- Advertiser | AutomationTest.com | www.google.com | Auto_DSA       | Auto_Financer | Approved | Video_Creative | Display_Image.jpg | 800x250 | 60       | Innovid      | Video_INNOVID.xlsx      |


  @regression
  Scenario: Verify Column Filter present on Association Tab of existing creative
    When User assigns a campaign to the creative using "Bulk Assign" option
      | 01- Advertiser |
    Then User navigates to creative details and clicks Association tab
    And Verify column selection icon is available and upon clicking it below columns should display
      | Line Item Name |
      | ID             |
      | Status         |
      | Campaign Name  |
      | Start Date     |
      | End Date       |
    And Verify unselected columns are not displayed in the Association tab
      | Start Date |
      | End Date   |
    And Verify if "Hide All" hides all the columns in the Association tab
    And Verify if "Show All" displays all the columns in the Association tab
      | Line Item Name |
      | ID             |
      | Status         |
      | Campaign Name  |
      | Start Date     |
      | End Date       |

  @regression
  Scenario: Verify Column Filter present on Association Tab of existing creative and navigation to the respective Line Item
    When User assigns a campaign to the creative using "Bulk Assign" option
      | 01- Advertiser |
    Then User navigates to creative details and clicks Association tab
    And Verify filter icon is available and upon clicking it "Add Filter", "Done" and "No Filters applied." text should display
    And User clicks "Add Filter", selects below filters and apply using "Done" button
      | Line Item Name  |
      | Campaign Name   |
      | Line Item Dates |
    And User navigates to Line item from Association Tab
