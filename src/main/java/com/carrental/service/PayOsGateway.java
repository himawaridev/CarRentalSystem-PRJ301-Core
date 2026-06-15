package com.carrental.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONArray;
import org.json.JSONObject;

public class PayOsGateway {
    public static final String PROVIDER = "PAYOS";

    private static final String DEFAULT_BASE_URL = "https://api-merchant.payos.vn";
    private static final String DEFAULT_APP_BASE_URL = "http://localhost:9999/CarRentalSystem";
    private static final String LOCAL_CONFIG_PATH = "config/payment-local.properties";

    private final String baseUrl;
    private final String appBaseUrl;
    private final String clientId;
    private final String apiKey;
    private final String checksumKey;
    private final HttpClient httpClient;

    public PayOsGateway() {
        this.baseUrl = config("PAYOS_BASE_URL", DEFAULT_BASE_URL);
        this.appBaseUrl = trimTrailingSlash(config("APP_BASE_URL", DEFAULT_APP_BASE_URL));
        this.clientId = config("PAYOS_CLIENT_ID", "");
        this.apiKey = config("PAYOS_API_KEY", "");
        this.checksumKey = config("PAYOS_CHECKSUM_KEY", "");
        this.httpClient = HttpClient.newBuilder().build();
    }

    public boolean isConfigured() {
        return !clientId.isBlank() && !apiKey.isBlank() && !checksumKey.isBlank();
    }

    public PaymentLinkResponse createPaymentLink(PaymentLinkRequest request) throws IOException, InterruptedException {
        requireConfigured();

        long amount = request.getAmount().setScale(0, java.math.RoundingMode.HALF_UP).longValueExact();
        String description = buildDescription(request.getOrderCode());
        String returnUrl = appBaseUrl + "/my-contracts";
        String cancelUrl = appBaseUrl + "/my-contracts";
        long expiredAt = request.getExpiredAt()
                .atZone(ZoneId.systemDefault())
                .toEpochSecond();

        String dataToSign = "amount=" + amount
                + "&cancelUrl=" + cancelUrl
                + "&description=" + description
                + "&orderCode=" + request.getOrderCode()
                + "&returnUrl=" + returnUrl;

        JSONObject payload = new JSONObject();
        payload.put("orderCode", request.getOrderCode());
        payload.put("amount", amount);
        payload.put("description", description);
        payload.put("cancelUrl", cancelUrl);
        payload.put("returnUrl", returnUrl);
        payload.put("expiredAt", expiredAt);
        payload.put("signature", hmacSha256(dataToSign, checksumKey));

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(trimTrailingSlash(baseUrl) + "/v2/payment-requests"))
                .header("Content-Type", "application/json")
                .header("x-client-id", clientId)
                .header("x-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        JSONObject responseBody = new JSONObject(response.body());
        if (response.statusCode() < 200 || response.statusCode() >= 300
                || !"00".equals(responseBody.optString("code"))) {
            throw new IOException("payOS create payment link failed: " + response.body());
        }

        JSONObject data = responseBody.getJSONObject("data");
        PaymentLinkResponse link = new PaymentLinkResponse();
        link.setProvider(PROVIDER);
        link.setPaymentLinkId(data.optString("paymentLinkId", null));
        link.setCheckoutUrl(data.optString("checkoutUrl", null));
        link.setQrCode(data.optString("qrCode", null));
        link.setRawResponse(response.body());
        return link;
    }

    public boolean verifyWebhook(JSONObject data, String currentSignature) {
        if (currentSignature == null || currentSignature.isBlank() || checksumKey.isBlank()) {
            return false;
        }
        String signedData = toSortedQueryString(data, false);
        String expectedSignature = hmacSha256(signedData, checksumKey);
        return constantTimeEquals(expectedSignature, currentSignature);
    }

    public PaymentLinkStatusResponse getPaymentLinkInformation(long orderCode)
            throws IOException, InterruptedException {
        requireConfigured();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(trimTrailingSlash(baseUrl) + "/v2/payment-requests/" + orderCode))
                .header("Content-Type", "application/json")
                .header("x-client-id", clientId)
                .header("x-api-key", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        JSONObject responseBody = new JSONObject(response.body());
        if (response.statusCode() < 200 || response.statusCode() >= 300
                || !"00".equals(responseBody.optString("code"))) {
            throw new IOException("payOS get payment link failed: " + response.body());
        }

        JSONObject data = responseBody.getJSONObject("data");
        PaymentLinkStatusResponse status = new PaymentLinkStatusResponse();
        status.setOrderCode(data.optLong("orderCode", orderCode));
        status.setAmount(BigDecimal.valueOf(data.optLong("amount", 0L)));
        status.setAmountPaid(BigDecimal.valueOf(data.optLong("amountPaid", 0L)));
        status.setAmountRemaining(BigDecimal.valueOf(data.optLong("amountRemaining", 0L)));
        status.setStatus(data.optString("status", ""));
        status.setProviderPaymentRef(firstTransactionReference(data));
        status.setRawResponse(response.body());
        return status;
    }

    private void requireConfigured() throws IOException {
        if (!isConfigured()) {
            throw new IOException("Missing PAYOS_CLIENT_ID, PAYOS_API_KEY, or PAYOS_CHECKSUM_KEY.");
        }
    }

    private String firstTransactionReference(JSONObject data) {
        Object transactions = data.opt("transactions");
        if (transactions instanceof JSONArray array && array.length() > 0) {
            JSONObject first = array.optJSONObject(0);
            return first == null ? "" : first.optString("reference", "");
        }
        if (transactions instanceof JSONObject object) {
            return object.optString("reference", "");
        }
        return "";
    }

    private String buildDescription(long orderCode) {
        String tail = Long.toString(Math.abs(orderCode % 10_000_000L));
        return ("CR" + tail).substring(0, Math.min(9, 2 + tail.length()));
    }

    private String toSortedQueryString(JSONObject object, boolean encodeValues) {
        List<String> keys = new ArrayList<>(object.keySet());
        Collections.sort(keys);

        List<String> pairs = new ArrayList<>();
        for (String key : keys) {
            Object value = object.opt(key);
            pairs.add(key + "=" + stringifyValue(value, encodeValues));
        }
        return String.join("&", pairs);
    }

    private String stringifyValue(Object value, boolean encodeValues) {
        if (value == null || JSONObject.NULL.equals(value)
                || "undefined".equals(value) || "null".equals(value)) {
            return "";
        }
        if (value instanceof JSONArray array) {
            JSONArray sortedArray = new JSONArray();
            for (int i = 0; i < array.length(); i++) {
                Object child = array.opt(i);
                sortedArray.put(child instanceof JSONObject jsonObject ? sortObject(jsonObject) : child);
            }
            value = sortedArray.toString();
        }
        String text = String.valueOf(value);
        return encodeValues ? java.net.URLEncoder.encode(text, StandardCharsets.UTF_8) : text;
    }

    private JSONObject sortObject(JSONObject object) {
        List<String> keys = new ArrayList<>(object.keySet());
        Collections.sort(keys);
        JSONObject sorted = new JSONObject();
        for (String key : keys) {
            sorted.put(key, object.opt(key));
        }
        return sorted;
    }

    private static String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot sign payOS payload.", e);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        byte[] expectedBytes = expected == null ? new byte[0] : expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = actual == null ? new byte[0] : actual.getBytes(StandardCharsets.UTF_8);
        return java.security.MessageDigest.isEqual(expectedBytes, actualBytes);
    }

    private static String config(String envName, String defaultValue) {
        String fileValue = localConfig(envName);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue.trim();
        }

        String systemProperty = System.getProperty(envName.toLowerCase().replace('_', '.'));
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty.trim();
        }

        String environmentValue = System.getenv(envName);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }

        return defaultValue;
    }

    private static String localConfig(String key) {
        String fileValue = fileConfig(key);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue;
        }

        Properties properties = new Properties();
        try (java.io.InputStream input = PayOsGateway.class.getClassLoader()
                .getResourceAsStream("payment-local.properties")) {
            if (input == null) {
                return null;
            }
            properties.load(input);
            return properties.getProperty(key);
        } catch (IOException e) {
            return null;
        }
    }

    private static String fileConfig(String key) {
        for (Path configPath : candidateConfigPaths()) {
            if (!Files.isRegularFile(configPath)) {
                continue;
            }

            Properties properties = new Properties();
            try (java.io.Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                properties.load(reader);
                String value = properties.getProperty(key);
                if (value != null && !value.isBlank()) {
                    return value;
                }
            } catch (IOException e) {
                // Try the next location. Local config is optional.
            }
        }
        return null;
    }

    private static List<Path> candidateConfigPaths() {
        List<Path> paths = new ArrayList<>();
        paths.add(Path.of(LOCAL_CONFIG_PATH));

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            paths.add(Path.of(catalinaBase, LOCAL_CONFIG_PATH));
            paths.add(Path.of(catalinaBase, "conf", "payment-local.properties"));
        }

        try {
            Path classLocation = Path.of(PayOsGateway.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            Path current = Files.isDirectory(classLocation) ? classLocation : classLocation.getParent();
            for (int i = 0; current != null && i < 8; i++) {
                paths.add(current.resolve(LOCAL_CONFIG_PATH));
                current = current.getParent();
            }
        } catch (URISyntaxException | IllegalArgumentException e) {
            // Ignore invalid code source location.
        }

        return paths;
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
