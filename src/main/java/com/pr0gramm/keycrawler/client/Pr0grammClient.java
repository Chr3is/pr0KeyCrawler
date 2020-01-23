package com.pr0gramm.keycrawler.client;

import com.pr0gramm.keycrawler.api.Content;
import com.pr0gramm.keycrawler.api.Messages;
import com.pr0gramm.keycrawler.api.Post;
import com.pr0gramm.keycrawler.api.PostInfo;
import com.pr0gramm.keycrawler.client.exception.UnexpectedStatusCodeException;
import com.pr0gramm.keycrawler.config.properties.Pr0grammApiClientProperties;
import com.pr0gramm.keycrawler.model.Nonce;
import com.pr0gramm.keycrawler.model.Pr0grammComment;
import com.pr0gramm.keycrawler.model.Pr0grammMessage;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class Pr0grammClient {

    private final Pr0grammApiClientProperties properties;

    private final WebClient pr0ApiClient;

    private final Nonce nonce;

    @Setter
    private boolean isAuthenticated = true;

    private static <T> Mono<T> executeRequest(WebClient.RequestHeadersSpec<?> requestHeadersSpec, Class<T> responseObject) {
        return requestHeadersSpec
                .retrieve()
                .onStatus(httpStatus -> {
                    if (!httpStatus.is2xxSuccessful()) {
                        log.error("Error while executing request. Status was {}", httpStatus);
                        return true;
                    }
                    return false;
                }, clientResponse -> Mono.error(new UnexpectedStatusCodeException(HttpStatus.valueOf(clientResponse.rawStatusCode()),
                        String.format("Could not execute request. Status was %d", clientResponse.rawStatusCode()))))
                .bodyToMono(responseObject);
    }

    public Mono<Content> fetchNewContent() {
        return executeRequest(pr0ApiClient
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path(properties.getContentEndpoint());
                    if (isAuthenticated) {
                        uriBuilder.queryParam("flags", 15);
                    }
                    return uriBuilder.build();
                })
                .accept(MediaType.APPLICATION_JSON), Content.class)
                .map(content -> content.setFullPostUrl(properties.getNewPostsUrl()));
    }

    public Mono<PostInfo> getPostInfo(Post post) {
        return !isAuthenticated ? Mono.empty() : executeRequest(pr0ApiClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(properties.getInfoEndpoint())
                        .queryParam("itemId", post.getId())
                        .build())
                .accept(MediaType.APPLICATION_JSON), PostInfo.class);
    }

    public Mono<Messages> getPendingMessagesByUser() {
        return !isAuthenticated ? Mono.empty() : executeRequest(pr0ApiClient
                .get()
                .uri(properties.getPendingMessagesEndpoint())
                .accept(MediaType.APPLICATION_JSON), Messages.class);
    }

    public Mono<Messages> getMessagesWith(String userName) {
        return !isAuthenticated ? Mono.empty() : executeRequest(pr0ApiClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(properties.getReadMessagesEndpoint())
                        .queryParam("with", userName).build())
                .accept(MediaType.APPLICATION_JSON), Messages.class);
    }

    public Mono<Void> sendNewMessage(Pr0grammMessage message) {
        return !isAuthenticated ? Mono.empty() : executeRequest(pr0ApiClient
                .post()
                .uri(properties.getSendMessagesEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromFormData(createFrom(message))), Void.class);
    }

    public Mono<Void> postNewComment(Pr0grammComment comment) {
        return !isAuthenticated ? Mono.empty() : executeRequest(pr0ApiClient
                .post()
                .uri(properties.getPostCommentEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromFormData(createFrom(comment))), Void.class);
    }

    private MultiValueMap<String, String> createFrom(Pr0grammMessage message) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("recipientId", "" + message.getUserId());
        formData.add("comment", message.getMessage());
        formData.add("_nonce", nonce.getValue());
        return formData;
    }

    private MultiValueMap<String, String> createFrom(Pr0grammComment comment) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("itemId", "" + comment.getPostId());
        formData.add("parentId", "0");
        formData.add("comment", comment.getMessage());
        formData.add("_nonce", nonce.getValue());
        return formData;
    }
}
