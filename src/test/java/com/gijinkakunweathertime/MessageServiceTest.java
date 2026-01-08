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
        String expected = "" + ChatColor.COLOR_CHAR + "x"
            + ChatColor.COLOR_CHAR + "f" + ChatColor.COLOR_CHAR + "f"
            + ChatColor.COLOR_CHAR + "a" + ChatColor.COLOR_CHAR + "a"
            + ChatColor.COLOR_CHAR + "0" + ChatColor.COLOR_CHAR + "0"
            + "5 " + ChatColor.AQUA + "minutes remaining";
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
        String expected = "" + ChatColor.COLOR_CHAR + "x"
            + ChatColor.COLOR_CHAR + "f" + ChatColor.COLOR_CHAR + "f"
            + ChatColor.COLOR_CHAR + "0" + ChatColor.COLOR_CHAR + "0"
            + ChatColor.COLOR_CHAR + "0" + ChatColor.COLOR_CHAR + "0"
            + "Expired";
        assertEquals(expected, message);
    }
}
