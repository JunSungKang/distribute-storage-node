package com.jskang.storagenode.common;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RequestApi {

    private List<String> commonHeaders = Arrays.asList(
        "Accept", "application/json",
        "Content-type", "application/json;charset=UTF-8"
    );

    public Object get(String url) throws Exception {
        return this.get(url, null);
    }

    public Object get(String url, String[] headers) throws Exception {

        // Common headers setting.
        if (headers != null) {
            for (String header : headers) {
                commonHeaders.add(header);
            }
        }

        HttpClient client = HttpClient.newBuilder().version(Version.HTTP_1_1).build();

        String result = client.sendAsync(
            HttpRequest
                .newBuilder(new URI(url))
                .GET()
                .headers(commonHeaders.toArray(String[]::new))
                .build(),
            HttpResponse
                .BodyHandlers
                .ofString()
        ).thenApply(HttpResponse::body).get();

        return Converter.jsonToMap(result);
    }

    public Object post(String url, String[] headers, Map<?, ?> data) throws Exception {
        // Common headers setting.
        if (headers != null) {
            for (String header : headers) {
                commonHeaders.add(header);
            }
        }

        String requestBody = Converter.mapToJson(data);
        if (requestBody.equals("null")) {
            requestBody = "";
        }
        BodyPublisher body = BodyPublishers.ofString(requestBody);

        HttpClient client = HttpClient.newBuilder().version(Version.HTTP_1_1).build();

        String result = client.sendAsync(
            HttpRequest
                .newBuilder(new URI(url))
                .POST(body)
                .headers(commonHeaders.toArray(String[]::new))
                .build(),
            HttpResponse
                .BodyHandlers
                .ofString()
        ).thenApply(HttpResponse::body).get();

        return Converter.stringArrayToList(result);
    }
}
