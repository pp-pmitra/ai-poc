Feature: Fetch, Create, Add, Delete and Replace NPI List using below APIs
  1. GET NPI List
  2. Create NPI
  3. Add NPI to the Existing List
  4. Delete NPI
  5. Replace NPI

  @api @regression
  Scenario Outline: Fetch NPI Lists for different list types - "<LIST_TYPE>"
    Given I call the Token API for user "<USERNAME>" and password "<PASSWORD>" for authentication
    Then Verify the Token API response status and presence of a valid bearer token
    When User uses the token to call the GET NPI List API with list ID "<LIST_ID>"
    Then Verify the GET NPI List API response contains the expected NPI block and a successful status code
    Examples:
      | USERNAME | PASSWORD | LIST_TYPE     | LIST_ID |
      | Lista44  | Lista@44 | STATIC        |   43323 |
      | Lista44  | Lista@44 | ATTRIBUTE     |   43220 |
      | Lista44  | Lista@44 | AUTO_IMPORTED |   43264 |
      | Lista44  | Lista@44 | SMART         |   43224 |

  @api @regression
  Scenario Outline: Fetch NPI Lists using Account ID
    Given I call the Token API for user "<USERNAME>" and password "<PASSWORD>" for authentication
    Then Verify the Token API response status and presence of a valid bearer token
    When User uses the token to call the GET NPI List API with account ID "<ACCOUNT_ID>"
    Then Verify the GET NPI List API response contains the NPI details and a successful status code
    Examples:
      | USERNAME | PASSWORD | ACCOUNT_ID |
      | Lista44  | Lista@44 |     566607 |

  @api @regression
  Scenario Outline: Verify Create NPI API using Account ID
    Given I call the Token API for user "<USERNAME>" and password "<PASSWORD>" for authentication
    Then Verify the Token API response status and presence of a valid bearer token
    When User calls the Create NPI API with account ID "<ACCOUNT_ID>", list name "<LIST_NAME>" and NPIs "<NPIs>"
    Then The API response should have status "<STATUS_CODE>", errors "<EXPECTED_ERRORS>", and contain the submitted NPI list "<NPIs>" if applicable
    Examples:
      | USERNAME | PASSWORD | ACCOUNT_ID | LIST_NAME     | NPIs                                                              | STATUS_CODE | EXPECTED_ERRORS                                    |
      | Lista44  | Lista@44 |     566607 |               |                                             2342342342,2342342343 |         400 | NPI List name must be between 1 and 100 characters |
      | Lista44  | Lista@44 |     566607 | Test_LIST_101 |                                             1639137706,1639138019 |         422 | NPI Group name must be unique within the account.  |
      | Lista44  | Lista@44 |     566607 | NPI_LIST_     |                       2342342342,2342342342,3453453456,3453453457 |         200 |                                                    |
      | Lista44  | Lista@44 |     559145 | NPI_LIST_     | 2342342342,3453453456,3453453457,1639137706,1639138019,1639138035 |         200 |                                                    |

  @api @regression
  Scenario Outline: Verify Create NPI API with Attributes using Account ID
    Given I call the Token API for user "<USERNAME>" and password "<PASSWORD>" for authentication
    Then Verify the Token API response status and presence of a valid bearer token
    When User calls the Create NPI API with account ID "<ACCOUNT_ID>", list name "<LIST_NAME>"
    Then Verify the Create NPI API with Attributes API response contains the same list name and a successful status code
    Examples:
      | USERNAME | PASSWORD | ACCOUNT_ID | LIST_NAME           |
      | Lista44  | Lista@44 |     566607 | NPI_LIST_ATTRIBUTE_ |

  @api @regression
  Scenario Outline: Verify Addition of NPIs to existing NPI List with "<LIST_TYPE>"
    Given I call the Token API for user "<USERNAME>" and password "<PASSWORD>" for authentication
    Then Verify the Token API response status and presence of a valid bearer token
    And Add NPIs to the existing NPI list "<LIST_ID>" using patch API
    And Verify the API response contains a successful status code
    When User uses the token to call the GET NPI List API with list ID "<LIST_ID>"
    Then Verify the NPI block contains the newly added NPIs
    Examples:
      | USERNAME | PASSWORD | LIST_TYPE | LIST_ID |
      | Lista44  | Lista@44 | STATIC    |   43323 |
      | Lista44  | Lista@44 | ATTRIBUTE |   43346 |
      | Lista44  | Lista@44 | SMART     |   43224 |
