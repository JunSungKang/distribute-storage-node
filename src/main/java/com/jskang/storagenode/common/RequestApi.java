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

    public Map get(String address, int port, String[] headers) throws Exception {
        // Exception port range.
        if (port < 0 || port > 65535) {
            System.out.println("The port input value range has been exceeded.");
            return null;
        }

        // Common headers setting.
        for (String header : headers) {
            commonHeaders.add(header);
        }

        HttpClient client = HttpClient.newBuilder().version(Version.HTTP_1_1).build();

        String result = client.sendAsync(
            HttpRequest
                .newBuilder(new URI(address.concat(":").concat(String.valueOf(port))))
                .GET()
                .headers(headers)
                .build(),
            HttpResponse
                .BodyHandlers
                .ofString()
        ).thenApply(HttpResponse::body).get();

        return Converter.jsonToMap(result);
    }

    public Map post(String address, int port, String[] headers, Map<?, ?> data) throws Exception {
        // Exception port range.
        if (port < 0 || port > 65535) {
            System.out.println("The port input value range has been exceeded.");
            return null;
        }

        // Common headers setting.
        for (String header : headers) {
            commonHeaders.add(header);
        }

        String requestBody = Converter.mapToJson(data);
        BodyPublisher body = BodyPublishers.ofString(requestBody);

        HttpClient client = HttpClient.newBuilder().version(Version.HTTP_1_1).build();

        String result = client.sendAsync(
            HttpRequest
                .newBuilder(new URI(address.concat(":").concat(String.valueOf(port))))
                .POST(body)
                .headers(headers)
                .build(),
            HttpResponse
                .BodyHandlers
                .ofString()
        ).thenApply(HttpResponse::body).get();

        return Converter.jsonToMap(result);
    }
}
