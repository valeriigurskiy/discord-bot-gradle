package com.discord.bot;

import com.discord.bot.commands.service.CommandService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.EnumSet;

public class Main extends ListenerAdapter {

    public static final String APIKEY = "OTQyNTAyNDc4MzE2OTcwMDg0.Yglb1Q.LxA2Yw0EzWOrqe5nHBXak2sTm3o";

    private static CommandService commandService = new CommandService();

    public Main(CommandService commandService) {
        Main.commandService = commandService;
    }

    public static void main(String[] args) throws LoginException {
        EnumSet<GatewayIntent> intents = EnumSet.of(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGES);
        JDA jda = JDABuilder.createDefault(APIKEY, intents)
                .addEventListeners(new CommandService())
//                .addEventListeners(new Main(commandService))
                .enableCache(CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.EMOTE)
                .build();

        CommandListUpdateAction commands = jda.updateCommands();
        commandService.commandInitialization(commands);
        commands.queue();
    }

//    @Override
//    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
//        switch (event.getComponentId()) {
//            case "start": {
//                event.editMessage("Игра в разработке!").setActionRows(
//                        ActionRow.of(Button.success("start", "Играть").withDisabled(true),
//                                Button.primary("stats", "Статистика"),
//                                Button.danger("stop", "Стоп").withDisabled(false))).queue();
//            }
//            case "stats":
//                event.editMessage("Статистика временно недоступна").queue();
//            case "stop":
//                event.editMessage("Невозможно остановить игру")
//                        .setActionRows(
//                                ActionRow.of(Button.success("start", "Играть").withDisabled(false),
//                                        Button.primary("stats", "Статистика"),
//                                        Button.danger("stop", "Стоп").withDisabled(true))).queue();
//        }
//    }
}