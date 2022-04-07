package com.discord.bot.music.service;

import com.discord.bot.music.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MusicService {

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    private static final String CAMERA_URL = "https://emoji.gg/assets/emoji/5471-caught-in-4k.png";
    private static final String TROLL_URL = "https://emoji.gg/assets/emoji/3303-letrollface.png";

    public MusicService() {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public void load(SlashCommandInteractionEvent event, String track) {

        GuildMusicManager musicManager = getGuildAudioPlayer(event.getTextChannel().getGuild());
        playerManager.loadItemOrdered(musicManager, track, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                long duration = audioTrack.getInfo().length;

                final EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(new Color(120, 30, 180));
                embed.setTitle("Hello");
                embed.addField("Название", "[" + audioTrack.getInfo().title + "](" + audioTrack.getInfo().uri + ")", false);
                embed.addField("Автор", audioTrack.getInfo().author, false);
                embed.addField("Длительность", String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(duration),
                        TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                ), false);
                embed.addField("Hello4", "qwe", false);
                embed.addField("Hello5", "qwe", false);

                event.replyEmbeds(embed.build()).queue();
                return;
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {

            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {

            }
        });

    }

    public void loadAndPlay(final SlashCommandInteractionEvent event, final String track) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event.getTextChannel().getGuild());

        if (track.equals("troll")) {

            playerManager.loadItemOrdered(musicManager, "https://www.youtube.com/watch?v=dQw4w9WgXcQ", new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack audioTrack) {
                    play(event, musicManager, audioTrack);
                }

                @Override
                public void playlistLoaded(AudioPlaylist audioPlaylist) {

                }

                @Override
                public void noMatches() {

                }

                @Override
                public void loadFailed(FriendlyException e) {

                }
            });

        }

        if (!track.startsWith("https://") && !track.equals("troll")) {

            playerManager.loadItemOrdered(musicManager, "ytsearch: " + track, new FunctionalResultHandler(
                    null, audioPlaylist -> play(event, musicManager, audioPlaylist.getTracks().get(0)),
                    null, null));

        } else if (track.startsWith("https://")) {
            playerManager.loadItemOrdered(musicManager, track, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    play(event, musicManager, track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    AudioTrack firstTrack = playlist.getSelectedTrack();

                    if (firstTrack == null) {
                        firstTrack = playlist.getTracks().get(0);
                    }

                    play(event, musicManager, firstTrack);
                }

                @Override
                public void noMatches() {
                    event.reply("Nothing found by " + track).queue();
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    event.reply("Could not play: " + exception.getMessage()).queue();
                }

            });
        }
    }

    public void play(SlashCommandInteractionEvent event, GuildMusicManager musicManager, AudioTrack track) {

        if (!event.getMember().getVoiceState().inAudioChannel()) {
            event.reply("Зайдите в один из голосовых каналов").queue();
            return;
        }

        String channelId = event.getMember().getVoiceState().getChannel().getId();

        connectToVoiceChannel(event.getTextChannel().getGuild().getAudioManager(), channelId);
        musicManager.scheduler.queue(track);

        String message;

        if (track.getInfo().title.equals("Rick Astley - Never Gonna Give You Up (Official Music Video)")) {
            event.reply(CAMERA_URL)
                    .delay(1, TimeUnit.SECONDS)
                    .queue(m1 -> m1.editOriginalFormat(":one:")
                            .delay(1, TimeUnit.SECONDS)
                            .queue(m2 -> m2.editMessage(":one: :two:")
                                    .delay(1, TimeUnit.SECONDS)
                                    .queue(m3 -> m3.editMessage(":one: :two: :three:")
                                            .delay(1, TimeUnit.SECONDS)
                                            .queue(m4 -> m4.editMessage(TROLL_URL)
                                                    .queue()))));
            return;
        } else {
            message = "Трек " + "[" + track.getInfo().title + "](" + track.getInfo().uri + ")" + " добавлен в очередь";
        }

        event.reply(message).queue();
    }

    public void skipTrack(SlashCommandInteractionEvent event) {
        TextChannel channel = event.getTextChannel();

        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        if (musicManager.scheduler.queue.size() == 0) {
            event.reply("Список треков пуст");
            musicManager.scheduler.stopTrack();

        } else {
            event.reply(event.getMember().getEffectiveName() + " скипнул трек").queue();
            musicManager.scheduler.nextTrack();
        }
    }



    private static void connectToVoiceChannel(AudioManager audioManager, String channelId) {
        VoiceChannel voiceChannel = audioManager.getGuild()
                .getVoiceChannelById(channelId);
        audioManager.openAudioConnection(voiceChannel);
    }

    // BUTTON INTERACTIONS

    public void skipTrack(ButtonInteractionEvent event) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event.getTextChannel().getGuild());
        musicManager.scheduler.nextTrack();
    }

    public Map<String, String> queue(ButtonInteractionEvent event) {
        Map<String, String> queue = new HashMap<>();
        GuildMusicManager musicManager = getGuildAudioPlayer(event.getTextChannel().getGuild());

        musicManager.scheduler.queue.forEach(t -> queue.put(t.getInfo().uri, t.getInfo().title));

        return queue;
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }
}