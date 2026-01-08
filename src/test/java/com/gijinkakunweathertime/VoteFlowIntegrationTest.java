package com.gijinkakunweathertime;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VoteFlowIntegrationTest {

    @Test
    public void voteCompletesWhenEligiblePlayersLeave() throws Exception {
        YamlConfiguration pluginConfig = new YamlConfiguration();
        pluginConfig.set("cooldown_time", 10);
        pluginConfig.set("vote_duration", 1);
        pluginConfig.set("vote_thresholds.percentage", 50);
        pluginConfig.set("worlds.enabled", List.of());
        pluginConfig.set("worlds.disabled", List.of());

        YamlConfiguration messagesConfig = new YamlConfiguration();
        messagesConfig.set("messages.vote_progress", "{current}/{required} {vote}");
        messagesConfig.set("messages.vote_success", "{vote} success");
        messagesConfig.set("messages.vote_expired", "{vote} expired");

        Logger logger = Logger.getLogger("VoteFlowIntegrationTest.voteCompletesWhenEligiblePlayersLeave");
        MessageService messageService = new MessageService(messagesConfig, pluginConfig, logger);
        RecordingActionBar actionBar = new RecordingActionBar(messageService);

        GijinkakunWeatherTime plugin = mock(GijinkakunWeatherTime.class);
        when(plugin.getLogger()).thenReturn(logger);

        World world = mock(World.class);
        when(world.getName()).thenReturn("world");

        Player playerOne = mock(Player.class);
        UUID playerOneId = UUID.randomUUID();
        when(playerOne.getName()).thenReturn("PlayerOne");
        when(playerOne.getUniqueId()).thenReturn(playerOneId);
        when(playerOne.getWorld()).thenReturn(world);
        when(playerOne.hasPermission(anyString())).thenAnswer(invocation -> {
            String perm = invocation.getArgument(0);
            return "gijinkakunweathertime.praise".equals(perm);
        });
        doNothing().when(playerOne).sendMessage(anyString());

        Player playerTwo = mock(Player.class);
        UUID playerTwoId = UUID.randomUUID();
        when(playerTwo.getName()).thenReturn("PlayerTwo");
        when(playerTwo.getUniqueId()).thenReturn(playerTwoId);
        when(playerTwo.getWorld()).thenReturn(world);
        when(playerTwo.hasPermission(anyString())).thenReturn(true);
        doNothing().when(playerTwo).sendMessage(anyString());

        Player playerThree = mock(Player.class);
        UUID playerThreeId = UUID.randomUUID();
        when(playerThree.getName()).thenReturn("PlayerThree");
        when(playerThree.getUniqueId()).thenReturn(playerThreeId);
        when(playerThree.getWorld()).thenReturn(world);
        when(playerThree.hasPermission(anyString())).thenReturn(true);
        doNothing().when(playerThree).sendMessage(anyString());

        Set<Player> onlinePlayers = new HashSet<>();
        onlinePlayers.add(playerOne);
        onlinePlayers.add(playerTwo);
        onlinePlayers.add(playerThree);

        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        when(scheduler.runTaskTimer(any(JavaPlugin.class), any(Runnable.class), anyLong(), anyLong()))
            .thenReturn(mock(BukkitTask.class));

        Server server = mock(Server.class);
        when(server.getScheduler()).thenReturn(scheduler);
        when(server.getOnlinePlayers()).thenAnswer(invocation -> onlinePlayers);
        when(server.getWorlds()).thenReturn(List.of(world));

        Server originalServer = setBukkitServer(server);
        try {
            VoteManager voteManager = new VoteManager(actionBar, plugin, pluginConfig, messageService);
            PraiseCommandHandler commandHandler = new PraiseCommandHandler(voteManager, actionBar, messageService, plugin);

            Command command = mock(Command.class);
            String[] clearArgs = new String[]{"the", "clear"};

            assertTrue(commandHandler.onCommand(playerOne, command, "praise", clearArgs));
            assertTrue(voteManager.hasActiveVote());
            assertEquals("clear:1/2", actionBar.voteProgress.get(actionBar.voteProgress.size() - 1));

            onlinePlayers.remove(playerTwo);
            voteManager.removeParticipation(playerTwoId);

            assertTrue("Vote should remain active with 1/2 after a player leaves", voteManager.hasActiveVote());

            assertTrue(commandHandler.onCommand(playerThree, command, "praise", clearArgs));
            assertEquals("clear", actionBar.lastPraise);
            assertFalse(voteManager.hasActiveVote());
        } finally {
            setBukkitServer(originalServer);
        }
    }

    @Test
    public void voteLifecycle_allowsWithdrawCooldownProgressAndExpiry() throws Exception {
        YamlConfiguration pluginConfig = new YamlConfiguration();
        pluginConfig.set("cooldown_time", 10);
        pluginConfig.set("vote_duration", 1);
        pluginConfig.set("vote_thresholds.percentage", 50);
        pluginConfig.set("worlds.enabled", List.of());
        pluginConfig.set("worlds.disabled", List.of());

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
        messagesConfig.set("messages.no_permission", "no permission");
        messagesConfig.set("messages.world_disabled", "world disabled");
        messagesConfig.set("messages.force_usage", "force usage");
        messagesConfig.set("messages.no_permission_command", "no permission cmd");
        messagesConfig.set("messages.force_applied", "force {vote}");

        Logger logger = Logger.getLogger("VoteFlowIntegrationTest");
        MessageService messageService = new MessageService(messagesConfig, pluginConfig, logger);
        RecordingActionBar actionBar = new RecordingActionBar(messageService);

        GijinkakunWeatherTime plugin = mock(GijinkakunWeatherTime.class);
        when(plugin.getLogger()).thenReturn(logger);

        World world = mock(World.class);
        when(world.getName()).thenReturn("world");

        Player playerOne = mock(Player.class);
        UUID playerOneId = UUID.randomUUID();
        when(playerOne.getName()).thenReturn("PlayerOne");
        when(playerOne.getUniqueId()).thenReturn(playerOneId);
        when(playerOne.getWorld()).thenReturn(world);
        when(playerOne.hasPermission(anyString())).thenAnswer(invocation -> {
            String perm = invocation.getArgument(0);
            return !"gijinkakunweathertime.bypasscooldown".equals(perm);
        });
        doNothing().when(playerOne).sendMessage(anyString());

        Player playerTwo = mock(Player.class);
        UUID playerTwoId = UUID.randomUUID();
        when(playerTwo.getName()).thenReturn("PlayerTwo");
        when(playerTwo.getUniqueId()).thenReturn(playerTwoId);
        when(playerTwo.getWorld()).thenReturn(world);
        when(playerTwo.hasPermission(anyString())).thenAnswer(invocation -> {
            String perm = invocation.getArgument(0);
            return !"gijinkakunweathertime.bypasscooldown".equals(perm);
        });
        doNothing().when(playerTwo).sendMessage(anyString());

        Player playerThree = mock(Player.class);
        UUID playerThreeId = UUID.randomUUID();
        when(playerThree.getName()).thenReturn("PlayerThree");
        when(playerThree.getUniqueId()).thenReturn(playerThreeId);
        when(playerThree.getWorld()).thenReturn(world);
        when(playerThree.hasPermission(anyString())).thenAnswer(invocation -> {
            String perm = invocation.getArgument(0);
            return !"gijinkakunweathertime.bypasscooldown".equals(perm);
        });
        doNothing().when(playerThree).sendMessage(anyString());

        Set<Player> onlinePlayers = new HashSet<>();
        onlinePlayers.add(playerOne);
        onlinePlayers.add(playerTwo);
        onlinePlayers.add(playerThree);

        Runnable[] scheduledTask = new Runnable[1];
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        when(scheduler.runTaskTimer(any(JavaPlugin.class), any(Runnable.class), anyLong(), anyLong()))
            .thenAnswer(invocation -> {
                scheduledTask[0] = invocation.getArgument(1);
                return mock(BukkitTask.class);
            });

        Server server = mock(Server.class);
        when(server.getScheduler()).thenReturn(scheduler);
        when(server.getOnlinePlayers()).thenAnswer(invocation -> onlinePlayers);
        when(server.getWorlds()).thenReturn(List.of(world));

        Server originalServer = setBukkitServer(server);
        try {
            VoteManager voteManager = new VoteManager(actionBar, plugin, pluginConfig, messageService);
            PraiseCommandHandler commandHandler = new PraiseCommandHandler(voteManager, actionBar, messageService, plugin);
            Field playerCooldownsField = VoteManager.class.getDeclaredField("playerCooldowns");
            playerCooldownsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, Long> playerCooldowns = (Map<UUID, Long>) playerCooldownsField.get(voteManager);

            Command command = mock(Command.class);
            String[] clearArgs = new String[]{"the", "clear"};
            String[] stormArgs = new String[]{"the", "storm"};
            String[] nightArgs = new String[]{"the", "night"};

            // Start vote
            assertTrue(commandHandler.onCommand(playerOne, command, "praise", clearArgs));
            assertTrue(voteManager.hasActiveVote());
            assertEquals("clear:1/2", actionBar.voteProgress.get(actionBar.voteProgress.size() - 1));

            // Withdraw vote (toggling the same option)
            assertTrue(commandHandler.onCommand(playerOne, command, "praise", clearArgs));
            assertFalse(voteManager.hasActiveVote());

            // Cooldown message blocks immediate re-vote after withdraw
            assertTrue(commandHandler.onCommand(playerOne, command, "praise", clearArgs));
            assertTrue(actionBar.cooldownNotices.containsKey(playerOneId));

            // Clear cooldown for next phase
            playerCooldowns.put(playerOneId, System.currentTimeMillis() - voteManager.getCooldownTime() - 1000);

            // Successful vote reaches threshold
            assertTrue(commandHandler.onCommand(playerOne, command, "praise", clearArgs));
            assertTrue(voteManager.hasActiveVote());
            assertTrue(commandHandler.onCommand(playerTwo, command, "praise", clearArgs));
            assertEquals("clear", actionBar.lastPraise);
            assertFalse(voteManager.hasActiveVote());

            // Prepare for expiry test
            playerCooldowns.put(playerOneId, System.currentTimeMillis() - voteManager.getCooldownTime() - 1000);
            playerCooldowns.put(playerTwoId, System.currentTimeMillis() - voteManager.getCooldownTime() - 1000);
            assertTrue(commandHandler.onCommand(playerOne, command, "praise", stormArgs));
            assertNotNull("Expiration task should be scheduled", scheduledTask[0]);

            // Force the vote to appear expired and trigger the scheduled task
            Field lastVoteTimeField = VoteManager.class.getDeclaredField("lastVoteTime");
            Field voteDurationField = VoteManager.class.getDeclaredField("voteDurationMillis");
            lastVoteTimeField.setAccessible(true);
            voteDurationField.setAccessible(true);
            long voteDuration = voteDurationField.getLong(voteManager);
            lastVoteTimeField.setLong(voteManager, System.currentTimeMillis() - voteDuration - 1000);

            scheduledTask[0].run();
            assertEquals("storm", actionBar.lastExpired);
            assertFalse(voteManager.hasActiveVote());

            // Single player can now satisfy the percentage threshold without a minimum vote requirement
            onlinePlayers.clear();
            onlinePlayers.add(playerOne);
            assertTrue(commandHandler.onCommand(playerOne, command, "praise", nightArgs));
            assertEquals("night:1/1", actionBar.voteProgress.get(actionBar.voteProgress.size() - 1));
            assertEquals("night", actionBar.lastPraise);
            assertFalse(voteManager.hasActiveVote());
        } finally {
            setBukkitServer(originalServer);
        }
    }

    @Test
    public void actionBarsSkipDisabledWorlds() throws Exception {
        YamlConfiguration pluginConfig = new YamlConfiguration();
        pluginConfig.set("cooldown_time", 10);
        pluginConfig.set("vote_duration", 1);
        pluginConfig.set("vote_thresholds.percentage", 50);
        pluginConfig.set("worlds.enabled", List.of());
        pluginConfig.set("worlds.disabled", List.of("world_nether"));

        YamlConfiguration messagesConfig = new YamlConfiguration();
        messagesConfig.set("messages.vote_progress", "{current}/{required} {vote}");
        messagesConfig.set("messages.vote_success", "{vote} success");

        Logger logger = Logger.getLogger("VoteFlowIntegrationTest.actionBarsSkipDisabledWorlds");
        MessageService messageService = new MessageService(messagesConfig, pluginConfig, logger);
        ActionBarUtils actionBar = new ActionBarUtils(messageService);

        GijinkakunWeatherTime plugin = mock(GijinkakunWeatherTime.class);
        when(plugin.getLogger()).thenReturn(logger);

        World overworld = mock(World.class);
        when(overworld.getName()).thenReturn("world");
        World nether = mock(World.class);
        when(nether.getName()).thenReturn("world_nether");

        Player allowed = mock(Player.class);
        UUID allowedId = UUID.randomUUID();
        when(allowed.getUniqueId()).thenReturn(allowedId);
        when(allowed.getWorld()).thenReturn(overworld);
        when(allowed.getName()).thenReturn("Allowed");
        when(allowed.hasPermission(anyString())).thenReturn(true);
        Player.Spigot allowedSpigot = mock(Player.Spigot.class);
        when(allowed.spigot()).thenReturn(allowedSpigot);
        doNothing().when(allowedSpigot).sendMessage(any(ChatMessageType.class), any(BaseComponent[].class));
        doNothing().when(allowed).sendMessage(anyString());

        Player blocked = mock(Player.class);
        UUID blockedId = UUID.randomUUID();
        when(blocked.getUniqueId()).thenReturn(blockedId);
        when(blocked.getWorld()).thenReturn(nether);
        when(blocked.getName()).thenReturn("Blocked");
        when(blocked.hasPermission(anyString())).thenReturn(true);
        Player.Spigot blockedSpigot = mock(Player.Spigot.class);
        when(blocked.spigot()).thenReturn(blockedSpigot);
        doNothing().when(blockedSpigot).sendMessage(any(ChatMessageType.class), any(BaseComponent[].class));
        doNothing().when(blocked).sendMessage(anyString());

        Set<Player> onlinePlayers = new HashSet<>();
        onlinePlayers.add(allowed);
        onlinePlayers.add(blocked);

        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        when(scheduler.runTaskTimer(any(JavaPlugin.class), any(Runnable.class), anyLong(), anyLong()))
            .thenReturn(mock(BukkitTask.class));

        Server server = mock(Server.class);
        when(server.getScheduler()).thenReturn(scheduler);
        when(server.getOnlinePlayers()).thenAnswer(invocation -> onlinePlayers);
        when(server.getWorlds()).thenReturn(List.of(overworld, nether));

        Server originalServer = setBukkitServer(server);
        try {
            VoteManager voteManager = new VoteManager(actionBar, plugin, pluginConfig, messageService);
            PraiseCommandHandler handler = new PraiseCommandHandler(voteManager, actionBar, messageService, plugin);

            Command command = mock(Command.class);
            assertTrue(handler.onCommand(allowed, command, "praise", new String[]{"the", "clear"}));
            assertFalse(voteManager.hasActiveVote());

            verify(allowedSpigot, atLeastOnce()).sendMessage(eq(ChatMessageType.ACTION_BAR), any(BaseComponent[].class));
            verify(blockedSpigot, never()).sendMessage(eq(ChatMessageType.ACTION_BAR), any(BaseComponent[].class));
        } finally {
            setBukkitServer(originalServer);
        }
    }

    private static Server setBukkitServer(Server server) throws Exception {
        Field serverField = Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);
        Server previous = (Server) serverField.get(null);
        serverField.set(null, server);
        return previous;
    }

    private static class RecordingActionBar extends ActionBarUtils {
        final List<String> voteProgress = new ArrayList<>();
        final Map<UUID, Long> cooldownNotices = new java.util.HashMap<>();
        String lastPraise;
        String lastExpired;

        RecordingActionBar(MessageService messageService) {
            super(messageService);
        }

        @Override
        public void sendVoteMessage(PraiseType vote, int currentVotes, int requiredVotes) {
            voteProgress.add(vote.getCommandArgument() + ":" + currentVotes + "/" + requiredVotes);
        }

        @Override
        public void sendPraiseMessage(PraiseType vote) {
            lastPraise = vote.getCommandArgument();
        }

        @Override
        public void sendVoteExpiredMessage(PraiseType vote) {
            lastExpired = vote.getCommandArgument();
        }

        @Override
        public void sendCooldownMessage(Player player, long minutesLeft) {
            cooldownNotices.put(player.getUniqueId(), minutesLeft);
        }
    }
}
