package com.pr0gramm.crawler.service.telegram;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.util.WebhookUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

/**
 * This is a copied and modified version of
 * https://github.com/rubenlagus/TelegramBots/blob/master/telegrambots-abilities/src/main/java/org/telegram/abilitybots/api/bot/BaseAbilityBot.java
 * Date: 18.08.2019
 */

@Slf4j
public abstract class BaseTelegramBot extends DefaultAbsSender implements AbilityExtension, LongPollingBot {

    Map<String, Ability> abilities;

    List<Reply> replies;

    BaseTelegramBot(DefaultBotOptions options) {
        super(options);
        registerAbilities();
    }

    private void registerAbilities() {
        try {
            // Collect all classes that implement AbilityExtension declared in the bot
            List<AbilityExtension> extensions = stream(getClass().getMethods())
                    .filter(checkReturnType(AbilityExtension.class))
                    .map(returnExtension(this))
                    .collect(Collectors.toList());

            // Add the bot itself as it is an AbilityExtension
            extensions.add(this);

            // Extract all abilities from every single extension instance
            abilities = extensions.stream()
                    .flatMap(ext -> stream(ext.getClass().getMethods())
                            .filter(checkReturnType(Ability.class))
                            .map(returnAbility(ext)))
                    // Abilities are immutable, build it respectively
                    .collect(ImmutableMap::<String, Ability>builder,
                            (b, a) -> b.put(a.name(), a),
                            (b1, b2) -> b1.putAll(b2.build()))
                    .build();

            // Extract all replies from every single extension instance
            Stream<Reply> extensionReplies = extensions.stream()
                    .flatMap(ext -> stream(ext.getClass().getMethods())
                            .filter(checkReturnType(Reply.class))
                            .map(returnReply(ext)));

            // Replies can be standalone or attached to abilities, fetch those too
            Stream<Reply> abilityReplies = abilities.values().stream()
                    .flatMap(ability -> ability.replies().stream());

            // Now create the replies registry (list)
            replies = Stream.concat(abilityReplies, extensionReplies).collect(
                    ImmutableList::<Reply>builder,
                    ImmutableList.Builder::add,
                    (b1, b2) -> b1.addAll(b2.build()))
                    .build();
        } catch (IllegalStateException e) {
            log.error("Duplicate names found while registering abilities. Make sure that the abilities declared don't clash with the reserved ones.", e);
            throw new RuntimeException(e);
        }
    }

    private Predicate<Method> checkReturnType(Class<?> clazz) {
        return method -> clazz.isAssignableFrom(method.getReturnType());
    }

    private Function<? super Method, AbilityExtension> returnExtension(Object obj) {
        return method -> {
            try {
                return (AbilityExtension) method.invoke(obj);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("Could not add ability extension", e);
                throw new RuntimeException(e);
            }
        };
    }

    private Function<? super Method, Ability> returnAbility(Object obj) {
        return method -> {
            try {
                return (Ability) method.invoke(obj);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("Could not add ability", e);
                throw new RuntimeException(e);
            }
        };
    }

    private Function<? super Method, Reply> returnReply(Object obj) {
        return method -> {
            try {
                return (Reply) method.invoke(obj);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("Could not add reply", e);
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public void clearWebhook() throws TelegramApiRequestException {
        WebhookUtils.clearWebhook(this);
    }
}
