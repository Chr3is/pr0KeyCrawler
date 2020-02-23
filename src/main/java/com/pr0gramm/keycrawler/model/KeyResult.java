package com.pr0gramm.keycrawler.model;

import com.pr0gramm.keycrawler.api.Post;
import lombok.Getter;
import lombok.ToString;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ToString
public class KeyResult {

    private static final Pattern KEY_REGEX = Pattern.compile("\\b(?=[a-zA-Z0-9-]*[a-zA-Z])(?=[a-zA-Z0-9-]*[0-9])[?a-zA-Z0-9]{4,8}(?:-[?a-zA-Z0-9]{4,8})+\\b");

    @Getter
    private final Post post;

    private final String imageText;

    private List<String> keys;

    public KeyResult(Tuple2<Post, String> test) {
        this.post = test.getT1();
        this.imageText = test.getT2();
    }

    public boolean isKey() {
        return KEY_REGEX.matcher(imageText).find();
    }

    public String getKeysFormatted() {
        StringBuilder builder = new StringBuilder();
        getKeys().forEach(key -> builder.append(key).append("\n"));
        return builder.toString();
    }

    public List<String> getKeys() {
        if (keys == null) {
            keys = new ArrayList<>();
            Matcher matcher = KEY_REGEX.matcher(imageText);
            while (matcher.find()) {
                keys.add(matcher.group());
            }
        }
        return keys;
    }
}
