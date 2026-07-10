package api;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.RequestOptions;
import factory.DriverFactory;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiActions {

    private static final Logger logger = LoggerFactory.getLogger(ApiActions.class);
    private final Playwright playwright = DriverFactory.createPlaywright();
    APIResponse response;
    private APIRequestContext request;

    public APIResponse getRequestWithoutBody(String baseURL, String endpointPath, HashMap<String, String> headers) {
        request = playwright
                .request()
                .newContext(
                        new APIRequest.NewContextOptions().setBaseURL(baseURL).setExtraHTTPHeaders(headers));
        response = request.get(endpointPath);
        return response;
    }

    public APIResponse postRequestWithoutBody(String baseURL, String endpointPath, HashMap<String, String> headers) {
        request = playwright
                .request()
                .newContext(new APIRequest.NewContextOptions()
                        .setBaseURL(baseURL)
                        .setExtraHTTPHeaders(headers)
                        .setTimeout(120000));
        try {
            response = request.post(endpointPath);
        } catch (PlaywrightException e) {
            logger.warn("POST request failed for endpoint: {}. Error: {}", endpointPath, e.getMessage());
            return null;
        }
        return response;
    }

    public APIResponse postFormURLEncodedRequest(
            String baseURL, String endpointPath, HashMap<String, String> headers, HashMap<String, String> formData) {
        request = playwright
                .request()
                .newContext(
                        new APIRequest.NewContextOptions().setBaseURL(baseURL).setExtraHTTPHeaders(headers));
        StringBuilder formBody = new StringBuilder();
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            if (!formBody.isEmpty()) formBody.append("&");
            String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
            formBody.append(encodedKey).append("=").append(encodedValue);
        }
        return request.post(
                endpointPath,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/x-www-form-urlencoded")
                        .setData(formBody.toString()));
    }

    public APIResponse postRequestWithBody(
            String baseURL, String endpointPath, HashMap<String, String> headers, String requestBody) {
        request = playwright
                .request()
                .newContext(
                        new APIRequest.NewContextOptions().setBaseURL(baseURL).setExtraHTTPHeaders(headers));
        return request.post(
                endpointPath,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(requestBody));
    }

    public APIResponse patchRequestWithBody(
            String baseURL, String endpointPath, HashMap<String, String> headers, String requestBody) {
        request = playwright
                .request()
                .newContext(
                        new APIRequest.NewContextOptions().setBaseURL(baseURL).setExtraHTTPHeaders(headers));
        return request.patch(
                endpointPath,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(requestBody));
    }

    public String getCleanJson(APIResponse response){
        String rawResponse = response.text();
        String cleanJson = "";
        String[] lines = rawResponse.split("\\r?\\n");
        for (String line : lines) {
            if (line.trim().startsWith("data:")) {
                int jsonStartIndex = line.indexOf("data:") + 5;
                cleanJson = line.substring(jsonStartIndex).trim();
                break;
            }
        }
        return cleanJson;
    }
}
