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

    private static final String REQUIRED_SYMBOLS = "((?=.*(\\d|\\?))(?=.*[A-z?]))+";
    private static final String LEFT_PART = '(' + REQUIRED_SYMBOLS + "([A-z0-9?]{4,8}))";
    private static final String MIDDLE_PART = '(' + REQUIRED_SYMBOLS + "(-[A-z0-9?]{4,8})+)";
    private static final String RIGHT_PART = '(' + REQUIRED_SYMBOLS + "-([A-z0-9?]{4,8}))";
    private static final Pattern KEY_REGEX = Pattern.compile('(' + LEFT_PART + MIDDLE_PART + RIGHT_PART + ')');

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
