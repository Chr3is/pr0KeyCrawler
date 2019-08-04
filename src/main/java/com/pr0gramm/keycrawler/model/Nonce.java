package com.pr0gramm.keycrawler.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pr0gramm.keycrawler.config.properties.Pr0grammApiClientProperties;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

@Component
@Getter
@Slf4j
public class Nonce {

    private static final String ME_COOKIE = "ME";

    private final String value;

    public Nonce(Pr0grammApiClientProperties properties, ObjectMapper objectMapper) {
        String meCookie = properties.getCookies()
                .entrySet()
                .stream()
                .filter(cookie -> cookie.getKey().equalsIgnoreCase(ME_COOKIE))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

        this.value = getNonce(meCookie, objectMapper);
    }

    private String getNonce(String meCookie, ObjectMapper objectMapper) {
        String nonce = null;

        if (meCookie == null) {
            log.info("No me cookie given");
            return nonce;
        }

        try {
            String jsonString = toJsonString(meCookie);
            String id = getId(jsonString, objectMapper);
            nonce = getNoncePart(id);
        } catch (IOException e) {
            log.error("Could not get nonce from me cookie", e);
        }
        return nonce;
    }

    private String toJsonString(String meCookie) throws UnsupportedEncodingException {
        return URLDecoder.decode(meCookie, "UTF-8");
    }

    private String getId(String jsonString, ObjectMapper objectMapper) throws IOException {
        return objectMapper.readValue(jsonString, NonceValues.class).id;
    }

    private String getNoncePart(String id) {
        return id.substring(0, 16);
    }

    @Data
    private static final class NonceValues {
        private String id;
    }

}
