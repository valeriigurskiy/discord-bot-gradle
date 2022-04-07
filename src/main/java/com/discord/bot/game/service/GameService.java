package com.discord.bot.game.service;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GameService {

    public void startGame(MessageReceivedEvent event) {

        event.getMessage()
                .reply("\nИгра начинается!" + "\n" + "Привет " + event.getMessage().getAuthor().getName()).queue();
    }

}
