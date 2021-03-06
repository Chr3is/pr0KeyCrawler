package com.pr0gramm.keycrawler.service;

import com.pr0gramm.keycrawler.model.MessageCodes;
import lombok.experimental.UtilityClass;

import java.util.ResourceBundle;

@UtilityClass
public final class MessageBundleResolver {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messagebundle");

    public static String getMessage(MessageCodes messageCode) {
        return RESOURCE_BUNDLE.getString(messageCode.toString());
    }

}
