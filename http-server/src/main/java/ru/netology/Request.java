package ru.netology;

import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> queryParams;

    public Request(String method, String rawPath) {
        this.method = method;
        RequestParser.ParsedRequest parsedRequest = RequestParser.parse(rawPath);
        this.path = parsedRequest.getPath();
        this.queryParams = parsedRequest.getQueryParams();
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
}
