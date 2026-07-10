package stepdefinitions;

import api.ApiActions;
import api.ApiEndpoints;
import com.microsoft.playwright.APIResponse;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.node.ArrayNode;
import io.cucumber.core.internal.com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.junit.Assert;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.CommonUtils;
import utils.ConfigReader;

public class ApiSteps {

    APIResponse response;
    JsonNode jsonNode;
    String bearerToken;
    String modifiedName;
    String queryId;
    ArrayNode data;
    static String p2McpAgentApiKey;
    private Scenario scenario;
    ApiActions apiActions = new ApiActions();
    ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(ApiSteps.class);
    Path path = Paths.get("src/main/resources/apiRequest/request.json");

    static {
        try {
            p2McpAgentApiKey = ConfigReader.getP2McpAgentApiKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    @Given("I call the Token API for user {string} and password {string} for authentication")
    public void iCallTheTokenAPIForUserAndPassword(String username, String password) {
        // Headers
        String basicAuth = "Basic "
                + Base64.getEncoder()
                        .encodeToString(
                                (ConfigReader.getProperty("clientId") + ":" + ConfigReader.getProperty("clientSecret"))
                                        .getBytes());
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", basicAuth);
        // Form Data
        HashMap<String, String> formData = new HashMap<>();
        formData.put("username", username);
        formData.put("password", password);
        formData.put("grant_type", "password");
        response = apiActions.postFormURLEncodedRequest(
                ConfigReader.getProperty("baseURL"), ApiEndpoints.OAUTH_TOKEN, headers, formData);
    }

    @Then("Verify the Token API response status and presence of a valid bearer token")
    public void theTokenAPIResponseShouldReturnASuccessfulStatusCodeAndContainAValidBearerToken() throws Exception {
        jsonNode = mapper.readTree(response.text());
        Assert.assertEquals(200, response.status());
        Assert.assertTrue(
                "access_token is missing in response",
                jsonNode.has("access_token") || !jsonNode.get("access_token").isEmpty());
        bearerToken = jsonNode.path("access_token").asText();
    }

    @When("User uses the token to call the GET NPI List API with list ID {string}")
    public void iCallTheGETNPIListAPIToFetchListWithListID(String listId) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + bearerToken);
        response = apiActions.getRequestWithoutBody(
                ConfigReader.getProperty("baseURL"), ApiEndpoints.NPI_LIST_ID + listId, headers);
    }

    @Then("Verify the GET NPI List API response contains the expected NPI block and a successful status code")
    public void theResponseShouldContainTheNPIBlockAndReturnASuccessfulStatusCode() throws Exception {
        jsonNode = mapper.readTree(response.text());
        Assert.assertEquals(200, response.status());
        Assert.assertTrue("NPI key missing in response", jsonNode.get("data").has("npis"));
        Assert.assertFalse("'npis' is empty", jsonNode.get("data").get("npis").isEmpty());
    }

    @When("User uses the token to call the GET NPI List API with account ID {string}")
    public void userUsesTheTokenToCallTheGETNPIListAPIWithAccountID(String accountID) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + bearerToken);
        response = apiActions.getRequestWithoutBody(
                ConfigReader.getProperty("baseURL"), ApiEndpoints.NPI_ACCOUNT_ID + accountID, headers);
    }

    @Then("Verify the GET NPI List API response contains the NPI details and a successful status code")
    public void verifyTheGETNPIListAPIResponseContainsTheNPIDetailsAndASuccessfulStatusCode() throws Exception {
        jsonNode = mapper.readTree(response.text());
        Assert.assertEquals(200, response.status());
        Assert.assertFalse("Response is not empty", jsonNode.isEmpty());
    }

    @When("User calls the Create NPI API with account ID {string}, list name {string} and NPIs {string}")
    public void userCallsTheCreateNPIAPIWithAccountIDListNameAndFollowingNPIs(
            String accountID, String listName, String npis) throws Exception {
        if (!listName.equals("Test_LIST_101") && !listName.isEmpty()) {
            listName = listName + CommonUtils.timeStampCalculation();
        }
        JsonNode fullPayload = mapper.readTree(Files.newBufferedReader(path));
        JsonNode templateNode = fullPayload.path("createNPI");
        ((ObjectNode) templateNode).put("name", listName);
        ArrayNode npiArray = mapper.valueToTree(CommonUtils.parseCommaSeparatedString(npis));
        ((ObjectNode) templateNode).set("npis", npiArray);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + bearerToken);
        String requestBody = templateNode.toString();
        response = apiActions.postRequestWithBody(
                ConfigReader.getProperty("baseURL"), ApiEndpoints.NPI_ACCOUNT_ID + accountID, headers, requestBody);
    }

    @Then(
            "The API response should have status {string}, errors {string}, and contain the submitted NPI list {string} if applicable")
    public void theAPIResponseShouldHaveStatusErrorsAndContainTheSubmittedNPIListIfApplicable(
            String statusCode, String errorMessage, String npis) throws Exception {
        jsonNode = mapper.readTree(response.text());
        Assert.assertEquals(Integer.parseInt(statusCode), response.status());
        if (response.status() != 200) {
            List<String> expectedErrorMessages = CommonUtils.parseCommaSeparatedString(errorMessage);
            List<String> actualErrorMessages = new ArrayList<>();
            JsonNode validationErrors = jsonNode.get("error").get("details");
            if (validationErrors != null && validationErrors.isArray()) {
                for (JsonNode error : validationErrors) {
                    actualErrorMessages.add(error.get("message").asText());
                }
            } else if (jsonNode.has("message")) {
                actualErrorMessages.add(
                        jsonNode.get("errorMessage").get("errorDescription").asText());
            }
            for (String expectedError : expectedErrorMessages) {
                Assert.assertTrue(
                        "Expected error not found: " + expectedError, actualErrorMessages.contains(expectedError));
            }
        }
        if (response.status() == 200) {
            JsonNode returnedNpis = jsonNode.get("data").get("npis");
            Assert.assertTrue("Expected 'npis' array in response", returnedNpis != null && returnedNpis.isArray());
            Set<String> submittedNpisSet = new HashSet<>(CommonUtils.parseCommaSeparatedString(npis));
            Set<String> returnedNpisSet = new HashSet<>();
            for (JsonNode npi : returnedNpis) {
                returnedNpisSet.add(npi.asText());
            }
            Assert.assertEquals(
                    "Returned NPIs do not match submitted NPIs (after deduplication)",
                    submittedNpisSet,
                    returnedNpisSet);
        }
    }

    @When("User calls the Create NPI API with account ID {string}, list name {string}")
    public void userCallsTheCreateNPIAPIWithAccountIDListName(String accountID, String listName) throws Exception {
        modifiedName = listName + CommonUtils.timeStampCalculation();
        JsonNode fullPayload = mapper.readTree(Files.newBufferedReader(path));
        JsonNode templateNode = fullPayload.path("createNPIWithAttribute");
        ((ObjectNode) templateNode).put("name", modifiedName);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + bearerToken);
        String requestBody = templateNode.toString();
        response = apiActions.postRequestWithBody(
                ConfigReader.getProperty("baseURL"),
                ApiEndpoints.NPI_ACCOUNT_ID + accountID + "/attributes",
                headers,
                requestBody);
    }

    @Then(
            "Verify the Create NPI API with Attributes API response contains the same list name and a successful status code")
    public void verifyTheCreateNPIAPIWithAttributesAPIResponseContainsTheSameListNameAndASuccessfulStatusCode()
            throws Exception {
        jsonNode = mapper.readTree(response.text());
        Assert.assertEquals(200, response.status());
        Assert.assertEquals(modifiedName, jsonNode.get("data").get("name").asText());
    }

    @And("Add NPIs to the existing NPI list {string} using patch API")
    public void addNPIsToTheExistingNPIListUsingPatchAPI(String listID) throws Exception {
        JsonNode fullPayload = mapper.readTree(Files.newBufferedReader(path));
        JsonNode templateNode = fullPayload.path("addNPIToList");
        ArrayNode npiArray = mapper.createArrayNode();
        for (int i = 0; i < 2; i++) {
            npiArray.add(CommonUtils.generateRandomNumber());
        }
        data = mapper.createArrayNode();
        data.addAll(npiArray);

        ((ObjectNode) templateNode).set("npis", npiArray);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + bearerToken);
        String requestBody = templateNode.toString();
        response = apiActions.patchRequestWithBody(
                ConfigReader.getProperty("baseURL"), ApiEndpoints.NPI_LIST_ID + listID, headers, requestBody);
    }

    @And("Verify the API response contains a successful status code")
    public void verifyTheAPIResponseContainsASuccessfulStatusCode() {
        Assert.assertEquals(200, response.status());
    }

    @Then("Verify the NPI block contains the newly added NPIs")
    public void verifyTheNPIBlockContainsTheNewlyAddedNPIs() throws Exception {
        jsonNode = mapper.readTree(response.text());
        JsonNode npisInResponse = jsonNode.path("data").path("npis");
        Set<String> responseNpisSet = new HashSet<>();
        for (JsonNode node : npisInResponse) {
            responseNpisSet.add(node.asText());
        }
        for (JsonNode addedNpi : data) {
            String npiValue = addedNpi.asText();
            Assert.assertTrue("Expected NPI not found in response: " + npiValue, responseNpisSet.contains(npiValue));
        }
    }

    @Given("I call the Token API using the API key for authentication with configuration:")
    public void iCallTheTokenAPIUsingTheAPIKeyForAuthentication(Map<String, String> config) {
        // Headers
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", config.get("Content-Type"));
        headers.put("x-api-key", p2McpAgentApiKey);
        // Form Data
        HashMap<String, String> formData = new HashMap<>();
        formData.put("grant_type", config.get("grant_type"));
        formData.put("client_id", config.get("client_id"));
        formData.put("client_secret", p2McpAgentApiKey);
        formData.put("audience", config.get("audience"));
        long startTime = System.currentTimeMillis();
        response = apiActions.postFormURLEncodedRequest(
                ConfigReader.getProperty("p2BaseURL"), ApiEndpoints.P2_OAUTH_TOKEN, headers, formData);
        long responseTime = System.currentTimeMillis() - startTime;
        scenario.log("Bearer Token API Response Time: " + responseTime + " ms");
        scenario.attach(("Bearer Token API Response Time: " + responseTime + " ms").getBytes(),
                "text/plain",
                "API Response Time");
    }

    @Then("Verify the Token API response status and presence of a valid access token")
    public void verifyTokenAPIResponseStatusAndPresenceOfValidAccessToken() throws Exception {
        jsonNode = mapper.readTree(response.text());
        Assert.assertEquals(200, response.status());
        Assert.assertTrue(
                "access_token is missing in response",
                jsonNode.has("access_token") || !jsonNode.get("access_token").isEmpty());
        bearerToken = jsonNode.path("access_token").asText();
    }

    @When("User initializes the MCP server using the access token with headers:")
    public void userInitializesMCPServerWithAccessToken(Map<String, String> headersConfig) throws Exception {
        JsonNode fullPayload = mapper.readTree(Files.newBufferedReader(path));
        JsonNode templateNode = fullPayload.path("mcpInitialize");
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", headersConfig.get("Content-Type"));
        headers.put("Accept", headersConfig.get("Accept"));
        headers.put("Authorization", "Bearer " + bearerToken);
        String requestBody = templateNode.toString();
        long startTime = System.currentTimeMillis();
        response = apiActions.postRequestWithBody(
                ConfigReader.getProperty("p2BaseURL"), ApiEndpoints.P2_MCP_ENDPOINT, headers, requestBody);
        long responseTime = System.currentTimeMillis() - startTime;
        scenario.log("MCP Server Initialization API Response Time: " + responseTime + " ms");
        scenario.attach(("MCP Server Initialization API Response Time: " + responseTime + " ms").getBytes(),
                "text/plain",
                "API Response Time");
    }

    @Then("Verify the MCP server initialization response is successful")
    public void verifyTheMCPServerInitializationResponseIsSuccessful() throws Exception {
        jsonNode = mapper.readTree(apiActions.getCleanJson(response));
        Assert.assertEquals(200, response.status());
        String instructions = jsonNode.path("result").path("instructions").asText();
        Assert.assertFalse("Instruction is empty", instructions.isEmpty());
    }

    @And("User requests the list of available MCP prompts with headers:")
    public void userRequestsTheListOfAvailableMCPPromptsWithHeaders(Map<String, String> headersConfig) throws IOException {
        JsonNode fullPayload = mapper.readTree(Files.newBufferedReader(path));
        JsonNode templateNode = fullPayload.path("fetchPromptsList");
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", headersConfig.get("Content-Type"));
        headers.put("Accept", headersConfig.get("Accept"));
        headers.put("Authorization", "Bearer " + bearerToken);
        String requestBody = templateNode.toString();
        long startTime = System.currentTimeMillis();
        response = apiActions.postRequestWithBody(
                ConfigReader.getProperty("p2BaseURL"), ApiEndpoints.P2_MCP_ENDPOINT, headers, requestBody);
        long responseTime = System.currentTimeMillis() - startTime;
        scenario.log("MCP Prompt List API Response Time: " + responseTime + " ms");
        scenario.attach(("MCP Prompt List API Response Time: " + responseTime + " ms").getBytes(),
                "text/plain",
                "API Response Time");
    }

    @Then("Verify the MCP prompts list is fetched successfully")
    public void verifyTheMCPPromptsListIsFetchedSuccessfully() throws Exception {
        jsonNode = mapper.readTree(apiActions.getCleanJson(response));
        Assert.assertEquals(200, response.status());
        JsonNode prompts = jsonNode.path("result").path("prompts");
        Assert.assertTrue(prompts.isArray() && !prompts.isEmpty());
    }

    @And("User retrieves specific MCP prompt details {string} with headers:")
    public void userRetrievesSpecificMcpPromptDetailsWithHeaders(String promptName, Map<String, String> headersConfig) throws IOException {
        JsonNode fullPayload = mapper.readTree(Files.newBufferedReader(path));
        JsonNode templateNode = fullPayload.path("getPrompt");
        ObjectNode paramsNode = (ObjectNode) templateNode.path("params");
        paramsNode.put("name", promptName);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", headersConfig.get("Content-Type"));
        headers.put("Accept", headersConfig.get("Accept"));
        headers.put("Authorization", "Bearer " + bearerToken);
        String requestBody = templateNode.toString();
        long startTime = System.currentTimeMillis();
        response = apiActions.postRequestWithBody(
                ConfigReader.getProperty("p2BaseURL"), ApiEndpoints.P2_MCP_ENDPOINT, headers, requestBody);
        long responseTime = System.currentTimeMillis() - startTime;
        scenario.log("MCP Prompt Details API Response Time: " + responseTime + " ms");
        scenario.attach(("MCP Prompt Details API Response Time: " + responseTime + " ms").getBytes(),
                "text/plain",
                "API Response Time");
    }

    @Then("Verify the MCP prompt details are retrieved successfully")
    public void verifyTheMcpPromptDetailsAreRetrievedSuccessfully() throws Exception {
        jsonNode = mapper.readTree(apiActions.getCleanJson(response));
        Assert.assertEquals(200, response.status());
        JsonNode messagesArray = jsonNode.path("result").path("messages");
        Assert.assertTrue(messagesArray.isArray() && !messagesArray.isEmpty());
    }

    @And("User calls the MCP tool to get Looker explore metadata with headers:")
    public void userCallsTheMcpToolToGetLookerExploreMetadataWithHeaders(Map<String, String> headersConfig) throws IOException {
        JsonNode fullPayload = mapper.readTree(Files.newBufferedReader(path));
        JsonNode templateNode = fullPayload.path("getExploreMetadata");
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", headersConfig.get("Content-Type"));
        headers.put("Accept", headersConfig.get("Accept"));
        headers.put("X-Account-Id", headersConfig.get("X-Account-Id"));
        headers.put("X-Advertiser-Id", headersConfig.get("X-Advertiser-Id"));
        headers.put("X-User-Id", headersConfig.get("X-User-Id"));
        headers.put("Authorization", "Bearer " + bearerToken);
        String requestBody = templateNode.toString();
        long startTime = System.currentTimeMillis();
        response = apiActions.postRequestWithBody(
                ConfigReader.getProperty("p2BaseURL"), ApiEndpoints.P2_MCP_ENDPOINT, headers, requestBody);
        long responseTime = System.currentTimeMillis() - startTime;
        scenario.log("Looker Explore Metadata API Response Time: " + responseTime + " ms");
        scenario.attach(("Looker Explore Metadata API Response Time: " + responseTime + " ms").getBytes(),
                "text/plain",
                "API Response Time");
    }

    @Then("Verify the Looker explore metadata response is successful")
    public void verifyTheLookerExploreMetadataResponseIsSuccessful() throws Exception {
        jsonNode = mapper.readTree(apiActions.getCleanJson(response));
        Assert.assertEquals(200, response.status());
    }

    @And("User calls the MCP tool to create a query for the user prompt using dimensions and metrics with headers:")
    public void userCallsTheMcpToolToCreateAQueryUsingDimensionsAndMetricsWithHeaders(Map<String, String> headersConfig) throws IOException {
        JsonNode fullPayload = mapper.readTree(Files.newBufferedReader(path));
        JsonNode templateNode = fullPayload.path("createQuery");
        ObjectNode arguments =
                (ObjectNode) templateNode.path("params").path("arguments");
        // Fields
        String fieldsValue = headersConfig.get("Dimension");
        ArrayNode fields = mapper.createArrayNode();
        Arrays.stream(fieldsValue.split(",")).map(field -> field.replace("\"", "").trim()).forEach(fields::add);
        arguments.set("fields", fields);
        // Filters
        ObjectNode filters = mapper.createObjectNode();
        String filterLabelsStr = headersConfig.get("FilterLabel");
        String filterValuesStr = headersConfig.get("FilterValue");
        if (filterLabelsStr != null && filterValuesStr != null && !filterLabelsStr.isEmpty()) {
            String[] labels = filterLabelsStr.split(",");
            String[] values = filterValuesStr.split(",");
            for (int i = 0; i < labels.length; i++) {
                String val = (i < values.length) ? values[i].trim() : "";
                filters.put(labels[i].trim(), val);
            }
        }
        arguments.set("filters", filters);
        // Sorts
        ArrayNode sorts = mapper.createArrayNode();
        sorts.add(headersConfig.get("Sort"));
        arguments.set("sorts", sorts);
        // Limit
        arguments.put("limit", headersConfig.get("Limit"));
        //Headers
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", headersConfig.get("Content-Type"));
        headers.put("Accept", headersConfig.get("Accept"));
        headers.put("X-Account-Id", headersConfig.get("X-Account-Id"));
        headers.put("X-Advertiser-Id", headersConfig.get("X-Advertiser-Id"));
        headers.put("X-User-Id", headersConfig.get("X-User-Id"));
        headers.put("Authorization", "Bearer " + bearerToken);

        String requestBody = templateNode.toString();
        long startTime = System.currentTimeMillis();
        response = apiActions.postRequestWithBody(
                ConfigReader.getProperty("p2BaseURL"), ApiEndpoints.P2_MCP_ENDPOINT, headers, requestBody);
        long responseTime = System.currentTimeMillis() - startTime;
        scenario.log("Create Query API Response Time: " + responseTime + " ms");
        scenario.attach(("Create Query API Response Time: " + responseTime + " ms").getBytes(),
                "text/plain",
                "API Response Time");
    }

    @Then("Verify the query is created successfully and returns a query ID")
    public void verifyTheQueryIsCreatedSuccessfullyAndReturnsAQueryId() throws Exception {
        jsonNode = mapper.readTree(apiActions.getCleanJson(response));
        Assert.assertEquals(200, response.status());
        queryId = jsonNode.path("result").path("structuredContent").path("query_id").asText();
        Assert.assertFalse( "Slug should not be empty", queryId.isEmpty());
    }

    @And("User calls the MCP tool to execute the created query with headers:")
    public void userCallsTheMcpToolToExecuteTheCreatedQueryWithHeaders(Map<String, String> headersConfig) throws IOException {
        JsonNode fullPayload = mapper.readTree(Files.newBufferedReader(path));
        JsonNode templateNode = fullPayload.path("executeQuery");
        ObjectNode arguments =
                (ObjectNode) templateNode.path("params").path("arguments");
        arguments.put("query_slug", queryId);
        //Headers
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", headersConfig.get("Content-Type"));
        headers.put("Accept", headersConfig.get("Accept"));
        headers.put("X-Account-Id", headersConfig.get("X-Account-Id"));
        headers.put("X-Advertiser-Id", headersConfig.get("X-Advertiser-Id"));
        headers.put("X-User-Id", headersConfig.get("X-User-Id"));
        headers.put("Authorization", "Bearer " + bearerToken);
        String requestBody = templateNode.toString();
        long startTime = System.currentTimeMillis();
        response = apiActions.postRequestWithBody(
                ConfigReader.getProperty("p2BaseURL"), ApiEndpoints.P2_MCP_ENDPOINT, headers, requestBody);
        long responseTime = System.currentTimeMillis() - startTime;
        scenario.log("Execute Query API Response Time: " + responseTime + " ms");
        scenario.attach(("Execute Query API Response Time: " + responseTime + " ms").getBytes(),
                "text/plain",
                "API Response Time");
    }

    @Then("Verify the query execution response contains the retrieved data {string}")
    public void verifyTheQueryExecutionResponseContainsTheRetrievedData(String promptDimensions) throws Exception {
        Assert.assertEquals(200, response.status());
        jsonNode = mapper.readTree(apiActions.getCleanJson(response));
        JsonNode structuredContent = jsonNode.path("result").path("structuredContent");
        String queryResultStr = structuredContent.path("query_result").asText();
        if (queryResultStr.trim().isEmpty() && structuredContent.path("error").asBoolean(true)) {
            String errorMessage = structuredContent.path("errorDesc").asText("Unknown error");
            Assert.fail("Query execution failed with error: " + errorMessage);
        } else {
            Assert.assertFalse("query_result string is empty without an API error", queryResultStr.trim().isEmpty());
            ArrayNode queryArray = (ArrayNode) mapper.readTree(queryResultStr);
            String[] expectedFields = promptDimensions.split(",");
            for (int i = 0; i < queryArray.size(); i++) {
                JsonNode row = queryArray.get(i);
                logger.info("Row {}:", i + 1);
                for (String field : expectedFields) {
                    String fieldName = field.trim();
                    JsonNode valueNode = row.path(fieldName);
                    Assert.assertFalse("Missing expected field: " + fieldName, valueNode.isMissingNode());
                    String fetchedValue = valueNode.asText();
                    scenario.log("  -> " + fieldName + " : " + fetchedValue);
                }
            }
        }
    }
}
