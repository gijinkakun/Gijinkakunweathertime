package com.gijinkakunweathertime;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Provides tab completion for the /praise command, guiding players toward the
 * required phrase "praise the <element>" and listing valid element names.
 */
public class PraiseCommandCompleter implements TabCompleter {

    private static final String PRAISE_KEYWORD = "the";
    private static final List<String> ROOT_SUBCOMMANDS = List.of(PRAISE_KEYWORD, "force", "reload");
    private static final List<String> PRAISE_OPTIONS = java.util.Arrays.stream(PraiseType.values())
        .map(PraiseType::getCommandArgument)
        .collect(Collectors.toUnmodifiableList());

    /**
     * Suggests the keyword {@code the} as the first argument and then offers each praise option
     * by matching the player's partial input for the second argument.
     *
     * @param sender  command sender requesting completions
     * @param command command being completed
     * @param alias   alias used in the invocation
     * @param args    arguments already provided
     * @return immutable list of tab completion suggestions
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            return Collections.singletonList(PRAISE_KEYWORD);
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return ROOT_SUBCOMMANDS.stream()
                .filter(option -> option.startsWith(prefix))
                .collect(Collectors.toList());
        }

        if (args.length == 2 && PRAISE_KEYWORD.equalsIgnoreCase(args[0])) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            List<String> completions = new ArrayList<>();
            for (String option : PRAISE_OPTIONS) {
                if (option.startsWith(prefix)) {
                    completions.add(option);
                }
            }
            return completions;
        }

        if (args.length == 2 && "force".equalsIgnoreCase(args[0])) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            return PRAISE_OPTIONS.stream()
                .filter(option -> option.startsWith(prefix))
                .collect(Collectors.toList());
        }
        return List.of();
    }
}
