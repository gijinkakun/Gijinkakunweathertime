package com.gijinkakunweathertime;

import org.bukkit.World;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents the supported praise types and the world mutations applied when the votes succeed.
 */
public enum PraiseType {
    DAY("day", world -> world.setTime(1000)),
    NIGHT("night", world -> world.setTime(13000)),
    CLEAR("clear", world -> {
        world.setStorm(false);
        world.setThundering(false);
    }),
    STORM("storm", world -> {
        world.setStorm(true);
        world.setThundering(true);
    });

    private final String commandArgument;
    private final Consumer<World> worldAction;

    PraiseType(String commandArgument, Consumer<World> worldAction) {
        this.commandArgument = commandArgument;
        this.worldAction = worldAction;
    }

    /**
     * Gets the argument required by the /praise command.
     *
     * @return The argument string.
     */
    public String getCommandArgument() {
        return commandArgument;
    }

    /**
     * Provides a display-friendly representation.
     *
     * @return The display label.
     */
    public String getDisplayName() {
        return commandArgument;
    }

    /**
     * Applies this praise type to the provided worlds.
     *
     * @param worlds The worlds to update.
     */
    public void applyToWorlds(List<World> worlds) {
        for (World world : worlds) {
            worldAction.accept(world);
        }
    }

    /**
     * Resolves a praise type from a {@code /praise} command argument, ignoring case.
     *
     * @param argument command argument supplied by the player
     * @return An optional praise type.
     */
    public static Optional<PraiseType> fromCommandArgument(String argument) {
        if (argument == null) {
            return Optional.empty();
        }
        String normalized = argument.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
            .filter(value -> value.commandArgument.equals(normalized) || isAlias(normalized, value))
            .findFirst();
    }

    private static boolean isAlias(String normalized, PraiseType value) {
        switch (normalized) {
            case "sun":
            case "clear":
                return value == CLEAR;
            case "rain":
            case "storm":
                return value == STORM;
            case "light":
            case "day":
                return value == DAY;
            case "dark":
            case "night":
                return value == NIGHT;
            default:
                return false;
        }
    }
}
