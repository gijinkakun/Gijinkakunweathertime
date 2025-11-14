package com.gijinkakunweathertime;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

public class MessageServiceTest {

    private MessageService messageService;
    private YamlConfiguration primaryMessages;
    private YamlConfiguration fallbackMessages;

    @Before
    public void setUp() {
        primaryMessages = new YamlConfiguration();
        fallbackMessages = new YamlConfiguration();

        primaryMessages.set("messages.cooldown", "&#FFAA00{minutes} &bminutes remaining");
        primaryMessages.set("messages.vote_expired", "#FF0000Expired");
        fallbackMessages.set("messages.vote_success", "&aVote passed");

        messageService = new MessageService(primaryMessages, fallbackMessages, Logger.getLogger("MessageServiceTest"));
    }

    @Test
    public void formatsPlaceholdersAndAppliesColors() {
        String formatted = messageService.format("cooldown", Map.of("minutes", "5"));
        String expected = net.md_5.bungee.api.ChatColor.of("#FFAA00") + "5 " + ChatColor.AQUA + "minutes remaining";
        assertEquals(expected, formatted);
    }

    @Test
    public void fallsBackWhenMessageMissing() {
        String message = messageService.getMessage("vote_success");
        assertEquals(ChatColor.GREEN + "Vote passed", message);
    }

    @Test
    public void parsesPlainHashHexCodes() {
        String message = messageService.getMessage("vote_expired");
        assertEquals(net.md_5.bungee.api.ChatColor.of("#FF0000") + "Expired", message);
    }
}
