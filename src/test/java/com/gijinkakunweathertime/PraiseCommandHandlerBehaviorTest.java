package com.gijinkakunweathertime;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PraiseCommandHandlerBehaviorTest {

    @Test
    public void deniesPraiseWithoutPermission() throws Exception {
        YamlConfiguration pluginConfig = basePluginConfig();
        YamlConfiguration messagesConfig = baseMessagesConfig();
        messagesConfig.set("messages.no_permission", "no permission {vote}");

        Logger logger = Logger.getLogger("PraiseCommandHandlerBehaviorTest.deniesPraiseWithoutPermission");
        MessageService messageService = new MessageService(messagesConfig, pluginConfig, logger);
        RecordingActionBar actionBar = new RecordingActionBar(messageService);

        GijinkakunWeatherTime plugin = mock(GijinkakunWeatherTime.class);
        when(plugin.getLogger()).thenReturn(logger);

        World world = mock(World.class);
        when(world.getName()).thenReturn("world");

        Player player = mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getWorld()).thenReturn(world);
        when(player.hasPermission(anyString())).thenReturn(false);
        doNothing().when(player).sendMessage(anyString());

        Set<Player> onlinePlayers = new HashSet<>();
        onlinePlayers.add(player);

        Runnable[] scheduledTask = new Runnable[1];
        Server original = installMockServer(onlinePlayers, List.of(world), scheduledTask);
        try {
            VoteManager voteManager = new VoteManager(actionBar, plugin, pluginConfig, messageService);
            PraiseCommandHandler handler = new PraiseCommandHandler(voteManager, actionBar, messageService, plugin);

            Command command = mock(Command.class);
            assertTrue(handler.onCommand(player, command, "praise", new String[]{"the", "clear"}));
            assertFalse("Player without permission should not start a vote", voteManager.hasActiveVote());
            verify(player).sendMessage("no permission clear");
        } finally {
            setBukkitServer(original);
        }
    }

    @Test
    public void blocksPraiseInDisabledWorld() throws Exception {
        YamlConfiguration pluginConfig = basePluginConfig();
        List<Map<String, Object>> disabledWorlds = new ArrayList<>();
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", "blocked_world");
        entry.put("name", "Blocked");
        disabledWorlds.add(entry);
        pluginConfig.set("worlds.disabled", disabledWorlds);

        YamlConfiguration messagesConfig = baseMessagesConfig();
        messagesConfig.set("messages.world_disabled", "world disabled {world}");

        Logger logger = Logger.getLogger("PraiseCommandHandlerBehaviorTest.blocksPraiseInDisabledWorld");
        MessageService messageService = new MessageService(messagesConfig, pluginConfig, logger);
        RecordingActionBar actionBar = new RecordingActionBar(messageService);

        GijinkakunWeatherTime plugin = mock(GijinkakunWeatherTime.class);
        when(plugin.getLogger()).thenReturn(logger);

        World world = mock(World.class);
        when(world.getName()).thenReturn("blocked_world");

        Player player = mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getWorld()).thenReturn(world);
        when(player.hasPermission(anyString())).thenAnswer(invocation -> {
            String perm = invocation.getArgument(0);
            return "gijinkakunweathertime.praise".equals(perm);
        });
        doNothing().when(player).sendMessage(anyString());

        Set<Player> onlinePlayers = new HashSet<>();
        onlinePlayers.add(player);

        Runnable[] scheduledTask = new Runnable[1];
        Server original = installMockServer(onlinePlayers, List.of(world), scheduledTask);
        try {
            VoteManager voteManager = new VoteManager(actionBar, plugin, pluginConfig, messageService);
            PraiseCommandHandler handler = new PraiseCommandHandler(voteManager, actionBar, messageService, plugin);

            Command command = mock(Command.class);
            assertTrue(handler.onCommand(player, command, "praise", new String[]{"the", "clear"}));
            assertFalse("Disabled world should prevent vote from starting", voteManager.hasActiveVote());
            verify(player).sendMessage("world disabled Blocked");
        } finally {
            setBukkitServer(original);
        }
    }

    @Test
    public void logoutDoesNotResetCooldown() throws Exception {
        YamlConfiguration pluginConfig = basePluginConfig();
        YamlConfiguration messagesConfig = baseMessagesConfig();
        messagesConfig.set("messages.cooldown", "cooldown {minutes}");

        Logger logger = Logger.getLogger("PraiseCommandHandlerBehaviorTest.logoutDoesNotResetCooldown");
        MessageService messageService = new MessageService(messagesConfig, pluginConfig, logger);
        RecordingActionBar actionBar = new RecordingActionBar(messageService);

        GijinkakunWeatherTime plugin = mock(GijinkakunWeatherTime.class);
        when(plugin.getLogger()).thenReturn(logger);

        World world = mock(World.class);
        when(world.getName()).thenReturn("world");

        Player player = mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getWorld()).thenReturn(world);
        when(player.hasPermission(anyString())).thenReturn(true);
        doNothing().when(player).sendMessage(anyString());

        Set<Player> onlinePlayers = new HashSet<>();
        onlinePlayers.add(player);

        Runnable[] scheduledTask = new Runnable[1];
        Server original = installMockServer(onlinePlayers, List.of(world), scheduledTask);
        try {
            VoteManager voteManager = new VoteManager(actionBar, plugin, pluginConfig, messageService);
            PraiseCommandHandler handler = new PraiseCommandHandler(voteManager, actionBar, messageService, plugin);

            Command command = mock(Command.class);
            assertTrue(handler.onCommand(player, command, "praise", new String[]{"the", "clear"}));
            assertFalse("Single player vote should complete immediately", voteManager.hasActiveVote());

            voteManager.removeParticipation(playerId);
            onlinePlayers.remove(player);
            assertTrue("Cooldown notices should be empty before rejoining", actionBar.cooldownNotices.isEmpty());

            onlinePlayers.add(player);
            assertTrue(handler.onCommand(player, command, "praise", new String[]{"the", "clear"}));
            assertTrue("Cooldown should remain tracked after logout", voteManager.getLastPraiseTime(playerId) > 0);
            assertFalse("Vote should not start while on cooldown", voteManager.hasActiveVote());
        } finally {
            setBukkitServer(original);
        }
    }

    @Test
    public void bypassPermissionIgnoresCooldown() throws Exception {
        YamlConfiguration pluginConfig = basePluginConfig();
        YamlConfiguration messagesConfig = baseMessagesConfig();
        messagesConfig.set("messages.cooldown", "cooldown {minutes}");

        Logger logger = Logger.getLogger("PraiseCommandHandlerBehaviorTest.bypassPermissionIgnoresCooldown");
        MessageService messageService = new MessageService(messagesConfig, pluginConfig, logger);
        RecordingActionBar actionBar = new RecordingActionBar(messageService);

        GijinkakunWeatherTime plugin = mock(GijinkakunWeatherTime.class);
        when(plugin.getLogger()).thenReturn(logger);

        World world = mock(World.class);
        when(world.getName()).thenReturn("world");

        Player player = mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getWorld()).thenReturn(world);
        when(player.getName()).thenReturn("PlayerWithBypass");
        when(player.hasPermission(anyString())).thenAnswer(invocation -> {
            String perm = invocation.getArgument(0);
            return "gijinkakunweathertime.praise".equals(perm) || "gijinkakunweathertime.bypasscooldown".equals(perm);
        });
        doNothing().when(player).sendMessage(anyString());

        Set<Player> onlinePlayers = new HashSet<>();
        onlinePlayers.add(player);

        Runnable[] scheduledTask = new Runnable[1];
        Server original = installMockServer(onlinePlayers, List.of(world), scheduledTask);
        try {
            VoteManager voteManager = new VoteManager(actionBar, plugin, pluginConfig, messageService);
            PraiseCommandHandler handler = new PraiseCommandHandler(voteManager, actionBar, messageService, plugin);

            // Simulate a recent praise to populate cooldown state
            Field cooldownField = VoteManager.class.getDeclaredField("playerCooldowns");
            cooldownField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, Long> cooldowns = (Map<UUID, Long>) cooldownField.get(voteManager);
            cooldowns.put(playerId, System.currentTimeMillis());

            Command command = mock(Command.class);
            assertTrue(handler.onCommand(player, command, "praise", new String[]{"the", "clear"}));
            assertFalse("Bypass permission should allow immediate completion for single player", voteManager.hasActiveVote());
            assertTrue("Bypass should prevent cooldown notices", actionBar.cooldownNotices.isEmpty());
        } finally {
            setBukkitServer(original);
        }
    }

    private static YamlConfiguration basePluginConfig() {
        YamlConfiguration pluginConfig = new YamlConfiguration();
        pluginConfig.set("cooldown_time", 10);
        pluginConfig.set("vote_duration", 1);
        pluginConfig.set("vote_thresholds.percentage", 50);
        pluginConfig.set("worlds.enabled", List.of());
        pluginConfig.set("worlds.disabled", List.of());
        return pluginConfig;
    }

    private static YamlConfiguration baseMessagesConfig() {
        YamlConfiguration messagesConfig = new YamlConfiguration();
        messagesConfig.set("messages.cooldown", "cooldown {minutes}");
        messagesConfig.set("messages.vote_progress", "{current}/{required} {vote}");
        messagesConfig.set("messages.vote_success", "{vote} success");
        messagesConfig.set("messages.vote_expired", "{vote} expired");
        messagesConfig.set("messages.vote_removed", "{vote} removed");
        messagesConfig.set("messages.already_praised", "{vote} already");
        messagesConfig.set("messages.vote_in_progress", "in progress");
        messagesConfig.set("messages.invalid_vote", "invalid");
        messagesConfig.set("messages.usage", "usage");
        messagesConfig.set("messages.no_permission", "no permission {vote}");
        messagesConfig.set("messages.world_disabled", "world disabled {world}");
        messagesConfig.set("messages.force_usage", "force usage");
        messagesConfig.set("messages.no_permission_command", "no permission cmd");
        messagesConfig.set("messages.force_applied", "force {vote}");
        return messagesConfig;
    }

    private static Server installMockServer(Set<Player> onlinePlayers, List<World> worlds, Runnable[] scheduledTask) throws Exception {
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        when(scheduler.runTaskTimer(any(JavaPlugin.class), any(Runnable.class), anyLong(), anyLong()))
            .thenAnswer(invocation -> {
                if (scheduledTask != null) {
                    scheduledTask[0] = invocation.getArgument(1);
                }
                return mock(BukkitTask.class);
            });

        Server server = mock(Server.class);
        when(server.getScheduler()).thenReturn(scheduler);
        when(server.getOnlinePlayers()).thenAnswer(invocation -> onlinePlayers);
        when(server.getWorlds()).thenReturn(worlds);

        return setBukkitServer(server);
    }

    private static Server setBukkitServer(Server server) throws Exception {
        Field serverField = Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);
        Server previous = (Server) serverField.get(null);
        serverField.set(null, server);
        return previous;
    }

    private static class RecordingActionBar extends ActionBarUtils {
        final Map<UUID, Long> cooldownNotices = new HashMap<>();

        RecordingActionBar(MessageService messageService) {
            super(messageService);
        }

        @Override
        public void sendActionBarToAllPlayers(String actionBarMessage) {
            // no-op for tests
        }

        @Override
        public void sendCooldownMessage(Player player, long minutesLeft) {
            cooldownNotices.put(player.getUniqueId(), minutesLeft);
        }
    }
}
