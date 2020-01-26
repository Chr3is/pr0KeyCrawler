package com.pr0gramm.crawler.config.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "pr0gramm.api-client")
@EqualsAndHashCode(callSuper = true)
@Data
public class Pr0grammApiClientProperties extends ClientProperties {

    @NotEmpty
    private String newPostsUrl;

    @NotEmpty
    private String contentEndpoint;

    @NotEmpty
    private String infoEndpoint;

    @NotEmpty
    private String pendingMessagesEndpoint;

    @NotEmpty
    private String readMessagesEndpoint;

    @NotEmpty
    private String sendMessagesEndpoint;

    @NotEmpty
    private String postCommentEndpoint;

    @NotNull
    private Map<String, String> cookies = new HashMap<>();
}
