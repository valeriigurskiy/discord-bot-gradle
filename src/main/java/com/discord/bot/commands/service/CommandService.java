package com.discord.bot.commands.service;

import com.discord.bot.minigames.dto.CoinSide;
import com.discord.bot.music.service.MusicService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class CommandService extends ListenerAdapter {

    private final MusicService musicService = new MusicService();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null)
            return;

        switch (event.getName()) {
            case "saysomething":
                say(event);
                break;
            case "roll":
                roll(event);
                break;
            case "coin":
                coin(event);
                break;
            case "game":
                if (event.getChannel().getName().equals("yatbg")) {
                    game(event);
                } else {
                    wrongChannel(event, "942699475930083339");
                }
                break;
            case "play":
                if (event.getChannel().getName().equals("боты")) {
                    playMusic(event);
                } else {
                    wrongChannel(event, "942699475930083339");
                }
                break;
            case "skip":
                skipMusic(event);
                break;
            case "song":
                findSong(event);
                break;
            case "cube":
                cube(event);
                break;
            default:
                event.reply("Такой команды не существует :(").queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        final EmbedBuilder embed = new EmbedBuilder();
//            https://i.ytimg.com/vi/ image url identifier /0.jpg

        String queue = queue(event);

        embed.setColor(new Color(120, 30, 180));
        embed.setTitle("Hello");
        embed.addField("Очередь", queue, true);
        embed.addField("Hello2", "qwe", false);
        embed.addBlankField(false);
        embed.addField("Hello3", "qwe", false);
        embed.setDescription("Text");
        embed.addField("Hello4", "qwe", false);
        embed.addField("Hello5", "qwe", false);

        event.getMessage().delete().queue();

//            event.getChannel().sendMessageEmbeds(embed.build()).queue(t -> {
//
//                MessageEmbed embed1 = embed.setColor(Color.PINK).build();
//
//                t.editMessageEmbeds(embed1).queue();
//            });

        event.getChannel().sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(Button.primary("pause", "Пауза"),
                Button.primary("play", "Играть"),
                Button.primary("skip", "Пропуск"),
                Button.success("join", "Позвать"),
                Button.danger("stop", "Стоп"))).queue();

        switch (event.getComponentId()) {
            case "skip":
                skipMusic(event);
                event.editButton(event.getButton()).queue();
                break;
        }
    }

    private void roll(SlashCommandInteractionEvent event) {
        int startNum = (int) event.getOption("n1").getAsLong();
        int endNum = (int) event.getOption("n2").getAsLong();
        event.reply(String.valueOf(new Random().nextInt(endNum - startNum) + startNum)).queue();
    }

    private void cube(SlashCommandInteractionEvent event) {
        String cubeName = "<:cube_" + (new Random().nextInt(6 - 1) + 1) + ":>";
        event.reply(cubeName).queue();
    }

    private void say(SlashCommandInteractionEvent event) {
        event.reply(event.getOption("content").getAsString()).queue();
    }

    private void coin(SlashCommandInteractionEvent event) {
        event.reply(String.valueOf(CoinSide.values()[new Random().nextInt(CoinSide.values().length)])).queue();
    }

    private void game(SlashCommandInteractionEvent event) {
        event.deferReply(true);
        event.reply("Привет " + event.getMember().getEffectiveName() + "!")
                .addActionRow(
                        Button.success("start", "Играть"),
                        Button.primary("stats", "Статистика"),
                        Button.danger("stop", "Стоп").withDisabled(true)).setEphemeral(true)
                .queue();
    }

    public void playMusic(SlashCommandInteractionEvent event) {
        musicService.loadAndPlay(event, event.getOption("track").getAsString());
    }

    public void skipMusic(SlashCommandInteractionEvent event) {
        musicService.skipTrack(event);
    }

    public void skipMusic(ButtonInteractionEvent event) {
        musicService.skipTrack(event);
    }

    public void findSong(SlashCommandInteractionEvent event) {
        musicService.load(event, event.getOption("url").getAsString());
    }

    public String queue(ButtonInteractionEvent event) {
        StringBuilder queueResponse = new StringBuilder("Нет треков в очереди");

        final int[] i = {0};

        Map<String, String> queue = musicService.queue(event);

        if (queue.size() != 0) {
            queueResponse.setLength(0);
            queue.forEach((u, t) -> {
                if (t.length() > 61) {
                    t = t.substring(0, 60).concat("...");
                }
                queueResponse.append(i[0] + 1).append(". ").append("[").append(t).append("](").append(u).append(")").append("\n");
                i[0] += 1;
            });
        }
        return queueResponse.toString();
    }

    private void wrongChannel(SlashCommandInteractionEvent event, String channelId) {
        event.reply("Команда недоступа в этом чате. Перейдите в <#".concat(channelId).concat(">"))
                .queue();
    }

    public void commandInitialization(CommandListUpdateAction commands) {
        commands.addCommands(
                Commands.slash("saysomething", "Сказать что-то")
                        .addOption(STRING, "content", "Сообщение", true));

        commands.addCommands(
                Commands.slash("roll", "Выводит случайное число в промежутке от <n1> до <n2>")
                        .addOptions(new OptionData(INTEGER, "n1", "Начало диапазона", true)
                                .setRequiredRange(0, Integer.MAX_VALUE))
                        .addOptions(new OptionData(INTEGER, "n2", "Конец диапазона", true)
                                .setRequiredRange(0, Integer.MAX_VALUE)));

//        commands.addCommands(
//                Commands.slash("game", "Начать игру"));

        commands.addCommands(
                Commands.slash("play", "Включить музыку")
                        .addOptions(new OptionData(STRING, "track", "Ссылка на видео или название", true)));

        commands.addCommands(
                Commands.slash("skip", "Пропустить трек"));

        commands.addCommands(
                Commands.slash("cube", "Кубик"));

//        commands.addCommands(
//                Commands.slash("song", "Получить информацию о треке")
//                        .addOptions(new OptionData(STRING, "url", "Ссылка на трек", true)));

        commands.addCommands(
                Commands.slash("coin", "Монетка"));
    }
}