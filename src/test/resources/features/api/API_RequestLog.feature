Feature: Request ID logging for Life API calls
  Every Life API call returns a unique request_id in its response, for both successful and error responses.
  Each call is persisted to the ApiRequestLog table with its client key, user, account, endpoint, verb, response code, payload and response body while api.db-logging.enabled is true.

  # Source: ET-22658 (TC_ET-22658_01, TC_ET-22658_05, TC_ET-22658_07, TC_ET-22658_09, TC_ET-22658_11, TC_ET-22658_13, TC_ET-22658_19, TC_ET-22658_20, TC_ET-22658_21, TC_ET-22658_28), GAP-1
  @todo
  Scenario: Successful Life API GET returns a request_id and logs one ApiRequestLog row with the caller context
    # Framework Gap: Requires a step definition to set the api.db-logging.enabled property in ApiSteps.java
    Given The "api.db-logging.enabled" property is set to "true"
    # Framework Gap: Requires a client-key Token API step definition in ApiSteps.java
    And I call the Token API with client key "CK-1001" for authentication
    Then Verify the Token API response status and presence of a valid bearer token
    # Framework Gap: Requires Life v1 endpoints in ApiEndpoints.java and a generic Life API call step in ApiSteps.java
    When User calls the "GET" Life API endpoint "/life/v1/campaigns" as user "69292" under account "55021"
    # Framework Gap: Requires a response status step definition for Life API calls in ApiSteps.java
    Then Verify the Life API response status is "200"
    # Framework Gap: Requires request_id response assertions in ApiSteps.java (field location unspecified per GAP-1)
    And Verify the Life API response contains a non-null request_id
    And Verify the returned request_id is a valid GUID
    # Framework Gap: Requires ApiRequestLog query step definitions using DatabaseActions in ApiSteps.java
    And Verify exactly "1" ApiRequestLog row exists for the returned request_id
    And Verify the ApiRequestLog row for the returned request_id contains:
      | Column       | Value              |
      | ResponseCode | 200                |
      | ClientKey    | CK-1001            |
      | AccountId    | 55021              |
      | UserId       | 69292              |
      | Endpoint     | /life/v1/campaigns |
      | Verb         | GET                |
    And Verify the ApiRequestLog "Payload" is empty for the returned request_id
    And Verify the ApiRequestLog RequestTimestamp falls between the request send time and the response receive time
    And Verify the ApiRequestLog RequestTimestamp is not "1970-01-01"

  # Source: ET-22658 (TC_ET-22658_01, TC_ET-22658_02, TC_ET-22658_29)
  @todo
  Scenario Outline: Repeated Life API calls from the same client each receive a distinct request_id - "<MODE>"
    # Framework Gap: Requires a step definition to set the api.db-logging.enabled property in ApiSteps.java
    Given The "api.db-logging.enabled" property is set to "true"
    # Framework Gap: Requires a client-key Token API step definition in ApiSteps.java
    And I call the Token API with client key "CK-1001" for authentication
    Then Verify the Token API response status and presence of a valid bearer token
    # Framework Gap: Requires sequential and concurrent Life API call support in ApiActions.java and ApiSteps.java
    When User sends "<CALL_COUNT>" "<MODE>" requests to the "GET" Life API endpoint "/life/v1/campaigns"
    # Framework Gap: Requires request_id uniqueness assertions in ApiSteps.java
    Then Verify all "<CALL_COUNT>" responses contain distinct request_id values
    # Framework Gap: Requires ApiRequestLog query step definitions using DatabaseActions in ApiSteps.java
    And Verify "<CALL_COUNT>" distinct ApiRequestLog rows exist for the returned request_ids
    Examples:
      | MODE       | CALL_COUNT |
      | sequential | 2          |
      | concurrent | 10         |

  # Source: ET-22658 (TC_ET-22658_03, TC_ET-22658_06, TC_ET-22658_08, TC_ET-22658_23, TC_ET-22658_24), QA-912, AMB-1
  @todo
  Scenario Outline: Life API error responses return a request_id and log the actual response code - "<STATUS>"
    # Framework Gap: Requires a step definition to set the api.db-logging.enabled property in ApiSteps.java
    Given The "api.db-logging.enabled" property is set to "true"
    # Framework Gap: Requires a client-key Token API step definition in ApiSteps.java
    And I call the Token API with client key "CK-1001" for authentication
    Then Verify the Token API response status and presence of a valid bearer token
    # Framework Gap: Requires a Life API call step supporting token state and payload files in ApiSteps.java
    When User calls the "<VERB>" Life API endpoint "<ENDPOINT>" with a "<TOKEN_STATE>" token and payload "<PAYLOAD>"
    # Framework Gap: Requires a response status step definition for Life API calls in ApiSteps.java
    Then Verify the Life API response status is "<STATUS>"
    # Framework Gap: Requires request_id response assertions in ApiSteps.java
    And Verify the Life API response contains a non-null request_id
    # Framework Gap: Requires ApiRequestLog query step definitions using DatabaseActions in ApiSteps.java
    And Verify exactly "1" ApiRequestLog row exists for the returned request_id
    And Verify the ApiRequestLog "ResponseCode" is "<STATUS>" for the returned request_id
    Examples:
      | VERB | ENDPOINT                     | TOKEN_STATE | PAYLOAD               | STATUS |
      | POST | /life/v1/tactics             | valid       | tactic_malformed.json | 400    |
      | GET  | /life/v1/campaigns/999999999 | valid       | none                  | 404    |
      | GET  | /life/v1/campaigns           | expired     | none                  | 401    |

  # Source: ET-22658 (TC_ET-22658_04)
  @todo
  Scenario: Life API 500 response still returns a non-null request_id
    # Framework Gap: Requires a client-key Token API step definition in ApiSteps.java
    Given I call the Token API with client key "CK-1001" for authentication
    Then Verify the Token API response status and presence of a valid bearer token
    # Framework Gap: Requires a step definition to make a Life API downstream dependency unavailable in the test environment
    And The Life API downstream dependency is unavailable
    # Framework Gap: Requires Life v1 endpoints in ApiEndpoints.java and a generic Life API call step in ApiSteps.java
    When User calls the "GET" Life API endpoint "/life/v1/campaigns"
    # Framework Gap: Requires a response status step definition for Life API calls in ApiSteps.java
    Then Verify the Life API response status is "500"
    # Framework Gap: Requires request_id response assertions in ApiSteps.java
    And Verify the Life API response contains a non-null request_id

  # Source: ET-22658 (TC_ET-22658_10)
  @todo
  Scenario: Life API call without a client key logs an empty ClientKey
    # Framework Gap: Requires a step definition to set the api.db-logging.enabled property in ApiSteps.java
    Given The "api.db-logging.enabled" property is set to "true"
    # Framework Gap: Requires a Life API call step without a client key in ApiSteps.java
    When User calls the "GET" Life API endpoint "/life/v1/campaigns" without a client key
    # Framework Gap: Requires request_id response assertions in ApiSteps.java
    Then Verify the Life API response contains a non-null request_id
    # Framework Gap: Requires ApiRequestLog query step definitions using DatabaseActions in ApiSteps.java
    And Verify the ApiRequestLog "ClientKey" is empty for the returned request_id

  # Source: ET-22658 (TC_ET-22658_15, TC_ET-22658_16, TC_ET-22658_17, TC_ET-22658_18, TC_ET-22658_27)
  @todo
  Scenario: Life API tactic lifecycle logs the correct endpoint, verb, payload and response body for every call
    # Framework Gap: Requires a step definition to set the api.db-logging.enabled property in ApiSteps.java
    Given The "api.db-logging.enabled" property is set to "true"
    # Framework Gap: Requires a client-key Token API step definition in ApiSteps.java
    And I call the Token API with client key "CK-1001" for authentication
    Then Verify the Token API response status and presence of a valid bearer token
    # Framework Gap: Requires Life v1 endpoints in ApiEndpoints.java and a generic Life API call step in ApiSteps.java
    When User calls the "GET" Life API endpoint "/life/v1/campaigns"
    # Framework Gap: Requires ApiRequestLog query step definitions using DatabaseActions in ApiSteps.java
    Then Verify the ApiRequestLog "Endpoint" is "/life/v1/campaigns" for the returned request_id
    # Framework Gap: Requires a Life API call step with a payload file in ApiSteps.java
    When User calls the "POST" Life API endpoint "/life/v1/tactics" with payload "tactic_create.json"
    Then Verify the ApiRequestLog "Endpoint" is "/life/v1/tactics" for the returned request_id
    And Verify the ApiRequestLog "Verb" is "POST" for the returned request_id
    And Verify the ApiRequestLog Payload matches the sent request body
    And Verify the ApiRequestLog ResponseBody matches the returned response body
    And Verify the ApiRequestLog Endpoint for each captured request_id matches only its own called endpoint
    # Framework Gap: Requires a Life API call step targeting the created tactic ID in ApiSteps.java
    When User calls the "PATCH" Life API endpoint "/life/v1/tactics/{id}" for the created tactic with payload "tactic_patch.json"
    Then Verify the ApiRequestLog "Endpoint" matches the called path "/life/v1/tactics/{id}" for the returned request_id
    And Verify the ApiRequestLog "Verb" is "PATCH" for the returned request_id
    When User calls the "PUT" Life API endpoint "/life/v1/tactics/{id}" for the created tactic with payload "tactic_update.json"
    Then Verify the ApiRequestLog "Verb" is "PUT" for the returned request_id
    When User calls the "DELETE" Life API endpoint "/life/v1/tactics/{id}" for the created tactic
    Then Verify the ApiRequestLog "Verb" is "DELETE" for the returned request_id

  # Source: ET-22658 (TC_ET-22658_22), GAP-3
  @todo
  Scenario: Life API call is not persisted to ApiRequestLog when db logging is disabled
    # Framework Gap: Requires a step definition to set the api.db-logging.enabled property in ApiSteps.java
    Given The "api.db-logging.enabled" property is set to "false"
    # Framework Gap: Requires a client-key Token API step definition in ApiSteps.java
    And I call the Token API with client key "CK-1001" for authentication
    Then Verify the Token API response status and presence of a valid bearer token
    # Framework Gap: Requires ApiRequestLog row count step definitions using DatabaseActions in ApiSteps.java
    And User records the current ApiRequestLog row count
    # Framework Gap: Requires Life v1 endpoints in ApiEndpoints.java and a generic Life API call step in ApiSteps.java
    When User calls the "GET" Life API endpoint "/life/v1/campaigns"
    Then Verify the ApiRequestLog row count is unchanged

  # Source: ET-22658 (TC_ET-22658_25)
  @todo
  Scenario: ApiRequestLogArchive accepts a logged row with the same columns as ApiRequestLog
    # Framework Gap: Requires a step definition to set the api.db-logging.enabled property in ApiSteps.java
    Given The "api.db-logging.enabled" property is set to "true"
    # Framework Gap: Requires a client-key Token API step definition in ApiSteps.java
    And I call the Token API with client key "CK-1001" for authentication
    Then Verify the Token API response status and presence of a valid bearer token
    # Framework Gap: Requires Life v1 endpoints in ApiEndpoints.java and a generic Life API call step in ApiSteps.java
    When User calls the "GET" Life API endpoint "/life/v1/campaigns"
    # Framework Gap: Requires ApiRequestLogArchive insert and query step definitions using DatabaseActions in ApiSteps.java
    And User copies the ApiRequestLog row for the returned request_id into ApiRequestLogArchive
    Then Verify the ApiRequestLogArchive insert succeeds
    And Verify the ApiRequestLogArchive row matches the ApiRequestLog row for columns:
      | RequestId        |
      | ClientKey        |
      | UserId           |
      | AccountId        |
      | RequestTimestamp |
      | Endpoint         |
      | Verb             |
      | ResponseCode     |
      | Payload          |
      | ResponseBody     |
