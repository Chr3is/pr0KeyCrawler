package com.pr0gramm.crawler.client;

import com.pr0gramm.crawler.client.api.Content;
import com.pr0gramm.crawler.client.api.Messages;
import com.pr0gramm.crawler.client.api.PostInfo;
import com.pr0gramm.crawler.client.exception.UnexpectedStatusCodeException;
import com.pr0gramm.crawler.config.properties.Pr0grammApiClientProperties;
import com.pr0gramm.crawler.model.Nonce;
import com.pr0gramm.crawler.model.client.Pr0Content;
import com.pr0gramm.crawler.model.client.Pr0Messages;
import com.pr0gramm.crawler.model.client.Pr0Post;
import com.pr0gramm.crawler.model.client.Pr0PostInfo;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class Pr0grammClient implements ApiClient {

    private final Pr0grammApiClientProperties properties;

    private final WebClient pr0ApiClient;

    private final Nonce nonce;

    private final MapperFacade mapper;

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

    @Override
    public Mono<Pr0Content> fetchNewContent() {
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
                .map(content -> mapper.map(content, Pr0Content.class))
                .map(content -> content.setFullPostUrl(properties.getNewPostsUrl()));
    }

    @Override
    public Mono<Pr0Messages> getPendingMessagesByUser() {
        return !isAuthenticated ? Mono.empty() : executeRequest(pr0ApiClient
                .get()
                .uri(properties.getPendingMessagesEndpoint())
                .accept(MediaType.APPLICATION_JSON), Messages.class)
                .map(messages -> mapper.map(messages, Pr0Messages.class));
    }

    public Mono<Pr0PostInfo> getPostInfo(Pr0Post post) {
        return !isAuthenticated ? Mono.empty() : executeRequest(pr0ApiClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(properties.getInfoEndpoint())
                        .queryParam("itemId", post.getId())
                        .build())
                .accept(MediaType.APPLICATION_JSON), PostInfo.class)
                .map(postInfo -> mapper.map(postInfo, Pr0PostInfo.class));
    }

    @Override
    public Mono<Pr0Messages> getMessagesWith(String userName) {
        return !isAuthenticated ? Mono.empty() : executeRequest(pr0ApiClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(properties.getReadMessagesEndpoint())
                        .queryParam("with", userName).build())
                .accept(MediaType.APPLICATION_JSON), Messages.class)
                .map(messages -> mapper.map(messages, Pr0Messages.class));
    }

    @Override
    public Mono<Void> sendNewMessage(com.pr0gramm.crawler.api.model.Pr0Message message) {
        return !isAuthenticated ? Mono.empty() : executeRequest(pr0ApiClient
                .post()
                .uri(properties.getSendMessagesEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromFormData(createFrom(message))), Void.class);
    }

    @Override
    public Mono<Void> postNewComment(com.pr0gramm.crawler.api.model.Pr0Comment comment) {
        return !isAuthenticated ? Mono.empty() : executeRequest(pr0ApiClient
                .post()
                .uri(properties.getPostCommentEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromFormData(createFrom(comment))), Void.class);
    }

    private MultiValueMap<String, String> createFrom(com.pr0gramm.crawler.api.model.Pr0Message message) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("recipientId", "" + message.getRecipient().getId());
        formData.add("comment", message.getMessage());
        formData.add("_nonce", nonce.getValue());
        return formData;
    }

    private MultiValueMap<String, String> createFrom(com.pr0gramm.crawler.api.model.Pr0Comment comment) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("itemId", "" + comment.getPost().getId());
        formData.add("parentId", "0");
        formData.add("comment", comment.getMessage());
        formData.add("_nonce", nonce.getValue());
        return formData;
    }
}
