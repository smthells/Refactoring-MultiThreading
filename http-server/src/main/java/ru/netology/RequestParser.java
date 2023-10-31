package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestParser {

    public static ParsedRequest parse(String path) {
        int queryStart = path.indexOf('?');
        String actualPath;
        Map<String, String> queryParams = new HashMap<>();

        if (queryStart != -1) {
            actualPath = path.substring(0, queryStart);
            List<NameValuePair> pairs = URLEncodedUtils.parse(path.substring(queryStart + 1),
                    StandardCharsets.UTF_8);
            for (NameValuePair pair : pairs) {
                queryParams.put(pair.getName(), pair.getValue());
            }
        } else {
            actualPath = path;
        }

        return new ParsedRequest(actualPath, queryParams);
    }

    public static class ParsedRequest {
        private final String path;
        private final Map<String, String> queryParams;

        public ParsedRequest(String path, Map<String, String> queryParams) {
            this.path = path;
            this.queryParams = queryParams;
        }

        public String getPath() {
            return path;
        }

        public Map<String, String> getQueryParams() {
            return queryParams;
        }
    }
}
