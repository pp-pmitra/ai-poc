Feature: End to End Workflow of Creative Creation and its assignment To Tactic
  It covers below points -
  1. Creating creatives via UI for the following types:
  a.  Display
  b.  Audio
  c.  Video
  d.  Native Display
  e.  Native Video
  f.  Search Extension
  2. Bulk uploading creatives for supported types - Display, HTML, Video, Native
  3. Verify Creatives on the Creative Library page
  4. Assign the created Creative to a Tactic

  @e2e
  Scenario Outline: End to End Workflow of Creative Creation with all the creative types and Assignment To Tactic
    # 1
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And User clicks Creative Library options present under Activation tab
    Then Verify Creative Library page is displayed
    And Verify data persistence when user creates and saves "Display" creative using details "<ADVERTISER>" as Advertiser, "<CREATIVE_NAME>" as Creative Name, "<ADVERTISER_DSA>", "<FINANCER>" and below Creative attributes
      | CreativeType | CreativeAttributes                                                               |
      | Html         | HTMLCode:<html>Auto_Creative</html>, Size:1024x576, DomainLanding:pulsepoint.com |
    And Verify data persistence when user creates and saves "Audio" creative using details "<ADVERTISER>" as Advertiser, "<CREATIVE_NAME>" as Creative Name, "<ADVERTISER_DSA>", "<FINANCER>" and below Creative attributes
      | CreativeType | CreativeAttributes                                                                         |
      | Audio URL    | URL:https://www.pulsepoint.com, Durations:60, AdvertiserDomain:pulsepoint.com, IAB:Profane |
    And Verify data persistence when user creates and saves "Video" creative using details "<ADVERTISER>" as Advertiser, "<CREATIVE_NAME>" as Creative Name, "<ADVERTISER_DSA>", "<FINANCER>" and below Creative attributes
      | CreativeType | CreativeAttributes                                                                                                                                          |
      | Video URL    | Type:MP4, URL:https://www.pulsepoint.com, Durations:60, Width:100, Height:100, ClickThroughURL:pulsepoint.com, AdvertiserDomain:pulsepoint.com, IAB:Profane |
    And Verify data persistence when user creates and saves "Search Extension" creative using details "<ADVERTISER>" as Advertiser, "<CREATIVE_NAME>" as Creative Name, "<ADVERTISER_DSA>", "<FINANCER>" and below Creative attributes
      | CreativeType | CreativeAttributes                                                                                                                                                           |
      | Search       | Size:300x600, Headline:Test Automation Headline, Description:Test Automation Description, DisplayURL:https://www.pulsepoint.com, ClickThroughURL:pulsepoint.com, IAB:Profane |
    And Verify data persistence when user creates and saves "Native Display" creative using details "<ADVERTISER>" as Advertiser, "<CREATIVE_NAME>" as Creative Name, "<ADVERTISER_DSA>", "<FINANCER>" and below Creative attributes
      | CreativeType   | CreativeAttributes                                                                                                                                                                                                                                      |
      | Native Display | ClickThroughURL:pulsepoint.com, DomainLanding:pulsepoint.com, IAB:Profane, Headline:Test Automation Headline, SponsoredBy:PulsePoint, Description:Test Automation Description, DisplayURL:https://www.pulsepoint.com, ImageFile:NativeDisplay_Image.jpg |
    And Verify data persistence when user creates and saves "Native Video" creative using details "<ADVERTISER>" as Advertiser, "<CREATIVE_NAME>" as Creative Name, "<ADVERTISER_DSA>", "<FINANCER>" and below Creative attributes
      | CreativeType | CreativeAttributes                                                                                                                                                                                                                                                                                                   |
      | VAST URL     | Type:STANDARD VAST, VASTURL:https://www.pulsepoint.com, Durations:60, Width:100, Height:100, ClickThroughURL:pulsepoint.com, AdvertiserDomain:pulsepoint.com, IAB:Profane, Headline:Test Automation Headline, SponsoredBy:PulsePoint, Description:Test Automation Description, DisplayURL:https://www.pulsepoint.com |
    #2
    Then Verify the newly created creative is displayed in the Creative Library page
    #3
    And Create and verify a tactic with "<LINE_ITEMS>" line items and other details "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" "<LINE_NAME>" "<LINE_BUDGET>" "<TACTIC_NAME>" and assign the created creatives to it
    Examples:
      | ADVERTISER     | CREATIVE_NAME | ADVERTISER_DSA | FINANCER      | CP_NAME          | CP_TYPE | CP_BUDGET | LINE_NAME    | LINE_BUDGET | TACTIC_NAME    | LINE_ITEMS                                                            |
      | 01- Advertiser | Creative      | Auto_DSA       | Auto_Financer | CreativeCampaign | Regular | 20000     | CreativeLine | 500         | CreativeTactic | Display, Audio, Video, Native Display, Native Video, Search Extension |

  @e2e
  Scenario Outline: End to End Workflow of Creative Creation using Bulk Upload with all the creative types and Assignment To Tactic
    # 1
    Given This scenario will be executed in the "Demo" environment as a "User"
    And "Life" application is logged in successfully with Account "automation@pulsepoint"
    And User clicks Creative Library options present under Activation tab
    Then Verify Creative Library page is displayed
    And Verify data persistence when user creates and saves "Display" Bulk upload creative using details "<ADVERTISER>" as Advertiser, "<ADVERTISER_DSA>", "<FINANCER>" and below Creative attributes
      | CreativeType | CreativeAttributes                                        |
      | Display      | FileName:DisplayBulkUploadTemplate1.xlsx, Status:Approved |
    And Verify data persistence when user creates and saves "HTML" Bulk upload creative using details "<ADVERTISER>" as Advertiser, "<ADVERTISER_DSA>", "<FINANCER>" and below Creative attributes
      | CreativeType | CreativeAttributes                                                                                                  |
      | HTML         | FileType: DCM File, FileName:DCM_HTML_E2E_BulkUpload.xlsx, Status:Pending, LandingDomain:https://www.pulsepoint.com |
    And Verify data persistence when user creates and saves "Video" Bulk upload creative using details "<ADVERTISER>" as Advertiser, "<ADVERTISER_DSA>", "<FINANCER>" and below Creative attributes
      | CreativeType | CreativeAttributes                                                                                                                                                                                                |
      | Video        | FileType: DCM File, FileName:DCM_VIDEO_E2E_BulkUpload.xlsx, Status:Denied, ClickThroughURL:pulsepoint.com, LandingDomain:https://www.pulsepoint.com, Size:800x250, Duration:60, ImageFile:NativeDisplay_Image.jpg |
    And Verify data persistence when user creates and saves "Native" Bulk upload creative using details "<ADVERTISER>" as Advertiser, "<ADVERTISER_DSA>", "<FINANCER>" and below Creative attributes
      | CreativeType | CreativeAttributes                                                                                                                                            |
      | Native       | FileName:NativeBulkUploadTemplate1.xlsx, Status:Approved, ClickThroughURL:pulsepoint.com, LandingDomain:https://www.pulsepoint.com, IAB:Profane, Size:800x250 |
    #2
    Then Verify the newly created creative is displayed in the Creative Library page
    #3
    And Create and verify a tactic with "<LINE_ITEMS>" line items and other details "<ADVERTISER>" "<CP_NAME>" "<CP_TYPE>" "<CP_BUDGET>" "<LINE_NAME>" "<LINE_BUDGET>" "<TACTIC_NAME>" and assign the created creatives to it
    Examples:
      | ADVERTISER     | ADVERTISER_DSA | FINANCER      | CP_NAME          | CP_TYPE | CP_BUDGET | LINE_NAME    | LINE_BUDGET | TACTIC_NAME    | LINE_ITEMS                     |
      | 01- Advertiser | Auto_DSA       | Auto_Financer | CreativeCampaign | Regular | 20000     | CreativeLine | 500         | CreativeTactic | Display, Video, Native Display |
