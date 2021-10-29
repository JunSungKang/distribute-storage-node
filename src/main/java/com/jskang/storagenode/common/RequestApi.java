package com.jskang.storagenode.common;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestApi {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    private List<String> commonHeaders = Arrays.asList(
        "Accept", "application/json",
        "Content-type", "application/json;charset=UTF-8"
    );

    public Object get(String url) {
        return this.get(url, null);
    }

    public Object get(String url, String[] headers) {

        // Common headers setting.
        if (headers != null) {
            for (String header : headers) {
                commonHeaders.add(header);
            }
        }

        HttpClient client = HttpClient.newBuilder().version(Version.HTTP_1_1).build();

        String result = "";
        try {
            result = client.sendAsync(
                HttpRequest
                    .newBuilder(new URI("http://" +url))
                    .GET()
                    .headers(commonHeaders.toArray(String[]::new))
                    .build(),
                HttpResponse
                    .BodyHandlers
                    .ofString()
            ).thenApply(HttpResponse::body).get();
        } catch (URISyntaxException e) {
            LOG.error(e.getMessage());
        } catch (ExecutionException e) {
            LOG.error(e.getMessage());
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }

        if (result.equals("")){
            return "connect fail";
        } else {
            return Converter.jsonToMap(result);
        }
    }

    public Object post(String url, String[] headers, Map<?, ?> data) throws Exception {
        // Common headers setting.
        if (headers != null) {
            for (String header : headers) {
                commonHeaders.add(header);
            }
        }

        String requestBody = Converter.objToJson(data);
        if (requestBody.equals("null")) {
            requestBody = "";
        }
        BodyPublisher body = BodyPublishers.ofString(requestBody);

        HttpClient client = HttpClient.newBuilder().version(Version.HTTP_1_1).build();

        String result = "";
        try {
            result = client.sendAsync(
                HttpRequest
                    .newBuilder(new URI("http://" +url))
                    .POST(body)
                    .headers(commonHeaders.toArray(String[]::new))
                    .build(),
                HttpResponse
                    .BodyHandlers
                    .ofString()
            ).thenApply(HttpResponse::body).get();
        } catch (URISyntaxException e) {
            LOG.error(e.getMessage());
        } catch (ExecutionException e) {
            LOG.error(e.getMessage());
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }

        return Converter.jsonToMap(result);
    }
}
