package com.pr0gramm.crawler.service.telegram;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.pr0gramm.crawler.config.properties.TelegramProperties;
import lombok.extern.slf4j.Slf4j;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.sender.DefaultSender;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.abilitybots.api.util.Pair;
import org.telegram.abilitybots.api.util.Trio;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.MultimapBuilder.hashKeys;
import static java.lang.String.format;
import static java.time.ZonedDateTime.now;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.telegram.abilitybots.api.objects.Ability.builder;
import static org.telegram.abilitybots.api.objects.Locality.*;
import static org.telegram.abilitybots.api.objects.MessageContext.newContext;
import static org.telegram.abilitybots.api.objects.Privacy.CREATOR;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityMessageCodes.*;
import static org.telegram.abilitybots.api.util.AbilityUtils.*;

/**
 * This is a copied and modified version of
 * https://github.com/rubenlagus/TelegramBots/blob/master/telegrambots-abilities/src/main/java/org/telegram/abilitybots/api/bot/BaseAbilityBot.java
 * Date: 18.08.2019
 */

@Slf4j
public class DefaultTelegramBot extends BaseTelegramBot {

    private static final String COMMANDS = "help";

    private final TelegramProperties telegramProperties;
    protected SilentSender silent;

    protected DefaultTelegramBot(TelegramProperties telegramProperties, DefaultBotOptions botOptions) {
        super(botOptions);
        this.telegramProperties = telegramProperties;
        this.silent = new SilentSender(new DefaultSender(this));
    }

    public void onUpdateReceived(Update update) {
        log.debug("[{}] New update [{}] received at {}", getBotUsername(), update.getUpdateId(), now());

        Stream.of(update)
                .filter(this::filterReply)
                .map(this::getAbility)
                .filter(this::validateAbility)
                .filter(this::checkPrivacy)
                .filter(this::checkLocality)
                .filter(this::checkInput)
                .map(this::getContext)
                .map(this::consumeUpdate)
                .forEach(this::postConsumption);
    }

    public Ability commands() {
        return builder()
                .name(COMMANDS)
                .locality(USER)
                .privacy(PUBLIC)
                .input(0)
                .action(ctx -> {
                    Privacy privacy = getPrivacy(ctx.user().getId());

                    ListMultimap<Privacy, String> abilitiesPerPrivacy = abilities.values().stream()
                            .map(ability -> {
                                String name = ability.name();
                                String info = ability.info();

                                if (!isEmpty(info))
                                    return Pair.of(ability.privacy(), format("/%s - %s", name, info));
                                return Pair.of(ability.privacy(), format("/%s", name));
                            })
                            .sorted(comparing(Pair::b))
                            .collect(() -> hashKeys().arrayListValues().build(), (map, pair) ->
                                    map.put(pair.a(), pair.b()), Multimap::putAll);

                    String commands = abilitiesPerPrivacy.asMap().entrySet().stream()
                            .filter(entry -> privacy.compareTo(entry.getKey()) >= 0)
                            .sorted(comparing(Map.Entry::getKey))
                            .map(entry -> entry.getValue().stream().reduce(entry.getKey()
                                    .toString(), (a, b) -> format("%s\n%s", a, b)))
                            .collect(joining("\n"));

                    if (commands.isEmpty())
                        commands = getLocalizedMessage(ABILITY_COMMANDS_NOT_FOUND, ctx.user().getLanguageCode());

                    silent.send(commands, ctx.chatId());
                })
                .build();
    }

    private void postConsumption(Pair<MessageContext, Ability> pair) {
        ofNullable(pair.b().postAction())
                .ifPresent(consumer -> consumer.accept(pair.a()));
    }

    private Pair<MessageContext, Ability> consumeUpdate(Pair<MessageContext, Ability> pair) {
        pair.b().action().accept(pair.a());
        return pair;
    }

    private Pair<MessageContext, Ability> getContext(Trio<Update, Ability, String[]> trio) {
        Update update = trio.a();
        User user = getUser(update);
        return Pair.of(newContext(update, user, getChatId(update), trio.c()), trio.b());
    }

    private boolean checkInput(Trio<Update, Ability, String[]> trio) {
        String[] tokens = trio.c();
        int abilityTokens = trio.b().tokens();

        boolean isOk = abilityTokens == 0 || (tokens.length > 0 && tokens.length == abilityTokens);

        if (!isOk) {
            silent.send(getLocalizedMessage(CHECK_INPUT_FAIL, getUser(trio.a()).getLanguageCode(),
                    abilityTokens, abilityTokens == 1 ? "input" : "inputs"), getChatId(trio.a()));
        }
        return isOk;
    }

    private boolean checkLocality(Trio<Update, Ability, String[]> trio) {
        Update update = trio.a();
        Locality locality = isUserMessage(update) ? USER : GROUP;
        Locality abilityLocality = trio.b().locality();

        boolean isOk = abilityLocality == ALL || locality == abilityLocality;

        if (!isOk) {
            silent.send(getLocalizedMessage(CHECK_LOCALITY_FAIL, getUser(trio.a()).getLanguageCode(),
                    abilityLocality.toString().toLowerCase()), getChatId(trio.a()));
        }
        return isOk;
    }

    private boolean checkPrivacy(Trio<Update, Ability, String[]> trio) {
        Update update = trio.a();
        User user = getUser(update);
        Privacy privacy;
        int id = user.getId();

        privacy = getPrivacy(id);

        boolean isOk = privacy.compareTo(trio.b().privacy()) >= 0;

        if (!isOk) {
            silent.send(getLocalizedMessage(CHECK_PRIVACY_FAIL, getUser(trio.a()).getLanguageCode()),
                    getChatId(trio.a()));
        }
        return isOk;
    }

    private Privacy getPrivacy(int id) {
        return isCreator(id) ? CREATOR : PUBLIC;
    }

    private boolean isCreator(int id) {
        return id == creatorId();
    }

    private boolean validateAbility(Trio<Update, Ability, String[]> trio) {
        return trio.b() != null;
    }

    private Trio<Update, Ability, String[]> getAbility(Update update) {
        Message msg = update.getMessage();
        if (!update.hasMessage() || !msg.hasText()) {
            return Trio.of(update, null, new String[]{});
        }

        String[] tokens = msg.getText().split(" ");

        if (tokens[0].startsWith("/")) {
            String abilityToken = stripBotUsername(tokens[0].substring(1)).toLowerCase();
            Ability ability = abilities.get(abilityToken);
            tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
            return Trio.of(update, ability, tokens);
        } else {
            return Trio.of(update, null, tokens);
        }
    }

    private String stripBotUsername(String token) {
        return compile(format("@%s", getBotUsername()), CASE_INSENSITIVE)
                .matcher(token)
                .replaceAll("");
    }

    private boolean filterReply(Update update) {
        return replies.stream()
                .filter(reply -> reply.isOkFor(update))
                .map(reply -> {
                    reply.actOn(update);
                    return false;
                })
                .reduce(true, Boolean::logicalAnd);
    }

    public String getBotToken() {
        return telegramProperties.getToken();
    }

    public String getBotUsername() {
        return telegramProperties.getUserName();
    }

    private int creatorId() {
        return telegramProperties.getCreatorId();
    }
}
