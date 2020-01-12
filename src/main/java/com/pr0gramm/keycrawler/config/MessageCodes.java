package com.pr0gramm.keycrawler.config;

public enum MessageCodes {

    TELEGRAM_MSG_REPLY_TO_REGISTRATION("telegram.msg.replyToRegMsg"),
    TELEGRAM_MSG_REPLY_TO_REGISTRATION_SYNTAX_ERROR("telegram.msg.replyToRegSyntaxErr"),
    TELEGRAM_MSG_REPLY_TO_REGISTRATION_AUTHENTICATION_FAILED("telegram.msg.replyToRegAuthFailed"),
    TELEGRAM_MSG_SUCCESSFUL_AUTHENTICATION("telegram.msg.succAuth"),
    TELEGRAM_MSG_NEW_KEYS("telegram.msg.newKeys"),

    TELEGRAM_BOT_REGISTRATION_DESC("telegram.bot.register.desc"),

    TELEGRAM_BOT_SUBSCRIBE_DESC("telegram.bot.subscribe.desc"),
    TELEGRAM_BOT_SUBSCRIBE_MSG_SUCCESSFUL("telegram.bot.subscribe.msg.success"),
    TELEGRAM_BOT_SUBSCRIBE_MSG_UNSUCCESSFUL("telegram.bot.subscribe.msg.nosuccess"),

    TELEGRAM_BOT_UNSUBSCRIBE_DESC("telegram.bot.unsubscribe.desc"),
    TELEGRAM_BOT_UNSUBSCRIBE_MSG_SUCCESSFUL("telegram.bot.unsubscribe.msg.success"),
    TELEGRAM_BOT_UNSUBSCRIBE_MSG_UNSUCCESSFUL("telegram.bot.unsubscribe.msg.nosuccess"),

    TELEGRAM_BOT_DELETE_DESC("telegram.bot.delete.desc"),
    TELEGRAM_BOT_DELETE_MSG_SUCCESSFUL("telegram.bot.delete.msg.success"),
    TELEGRAM_BOT_DELETE_MSG_UNSUCCESSFUL("telegram.bot.delete.msg.nosuccess"),

    TELEGRAM_BOT_ADD_DESC("telegram.bot.add.desc"),
    TELEGRAM_BOT_ADD_MSG_SUCCESSFUL("telegram.bot.add.msg.success"),
    TELEGRAM_BOT_ADD_MSG_UNSUCCESSFUL("telegram.bot.add.msg.nosuccess"),

    PR0GRAMM_MSG_REGISTRATION("pr0gramm.msg.registration"),
    PR0GRAMM_MSG_CRAWLED_POST("pr0gramm.msg.crawledpost");

    private final String code;

    MessageCodes(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return this.code;
    }

}
