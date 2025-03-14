package com.gijinkakunweathertime;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides tab completion for the /praise command.
 */
public class PraiseCommandCompleter implements TabCompleter {

    private static final List<String> COMMANDS = Arrays.asList("the light", "the dark", "the sun", "the rain");

    /**
     * Handles tab completion for the /praise command.
     *
     * @param sender The command sender.
     * @param command The command.
     * @param alias The command alias.
     * @param args The command arguments.
     * @return A list of tab completion suggestions.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String prefix = args[0].toLowerCase();
            for (String cmd : COMMANDS) {
                if (cmd.startsWith(prefix)) {
                    completions.add(cmd);
                }
            }
            return completions;
        }
        return null;
    }
}