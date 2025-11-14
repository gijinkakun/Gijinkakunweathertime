# GijinkakunWeatherTime Plugin

GijinkakunWeatherTime is a Bukkit/Spigot plugin that allows players to control the time and weather in the game through voting. Players can use specific chat commands to vote for changing the time of day or the weather, and the changes are applied if a majority of online players participate in the vote.

## Features

- Change the time of day with commands to set it to day or night.
- Change the weather conditions with commands to set it to sunny or rainy.
- Voting system that requires a majority of online players to participate within 1 minute.
- Real-time updates on the voting progress via action bar messages.
- Command auto-completion for ease of use.
- Detailed logging for easier debugging and monitoring.
- Configurable messages, cooldown times, and vote durations.
- Multi-language support (configurable).

## Commands

### `/praise the <light|dark|sun|rain>`

Initiates a vote to change the time or weather based on the specified option.

- **Usage:** `/praise the <light|dark|sun|rain>`
- **Permission:** `gijinkakunweathertime.praise`

### `/praise force <light|dark|sun|rain>`

Applies the requested weather or time change immediately without waiting for a vote (staff only).

- **Permission:** `gijinkakunweathertime.force`

### `/praise reload`

Reloads `config.yml` and message files without restarting the server.

- **Permission:** `gijinkakunweathertime.reload`

## Permissions

- `gijinkakunweathertime.praise`: Allows the player to use all `/praise` commands and initiate new praise votes (default: op)
- `gijinkakunweathertime.bypasscooldown`: Allows skipping the praise cooldown timer (default: op)
- `gijinkakunweathertime.force`: Allows `/praise force` (default: op)
- `gijinkakunweathertime.reload`: Allows `/praise reload` (default: op)

## Configuration

The plugin provides extensive configuration options through the `config.yml` file:

```yaml
messages:
  cooldown: "&#FF5555You have already praised. cooldown for &#FFFFFF{minutes} minutes&#FF5555."
  vote_progress: "&#FFFFFF{current}/{required} &#619B8A have praised for &#FFFFFF{vote}&#619B8A."
  vote_success: "&#FFFFFF{vote} &#619B8A has been praised!"
  vote_expired: "&#FF5555Time has expired. Not enough votes for &#FFFFFF{vote}&#FF5555."
  usage: "&#FF5555Please use one of the following commands: /praise the light, /praise the dark, /praise the sun, /praise the rain."
  vote_in_progress: "&#FF5555 different vote is in progress."
  already_praised: "&#FF5555The &#FFFFFF{vote} &#FF5555has already been praised recently."
  world_disabled: "&#FF5555The &#FFFFFF{world} &#FF5555does not allow praise votes."
  reload_success: "&#619B8AGijinkakunWeatherTime configuration reloaded."
  reload_failed: "&#FF5555Failed to reload the configuration. Check console for errors."
  force_usage: "&#FF5555Usage: /praise force <light|dark|sun|rain>."
  force_applied: "&#619B8AForced praise applied for &#FFFFFF{vote}&#619B8A."
language: "en"
cooldown_time: 10 (Cooldown time in minutes)
vote_duration: 1 (Duration of the vote in minutes)
vote_thresholds:
  percentage: 50 (Percent of eligible players required)
  minimum_players: 2 (Minimum raw votes regardless of percentage)
worlds:
  enabled: [] (Only allow these worlds; empty = all)
  disabled:
    - id: "world_nether"
      name: "Nether"
    - id: "world_the_end"
      name: "End"

### Customization

Messages and settings can be customized to suit your server's needs. The `language` setting allows for multi-language support by providing different message files (e.g., `messages_en.yml` for English, `messages_es.yml` for Spanish, `messages_fr.yml` for French, `messages_de.yml` for German, `messages_pt_br.yml` for Brazilian Portuguese, `messages_ru.yml` for Russian, `messages_zh_cn.yml` for Simplified Chinese, `messages_ja.yml` for Japanese, `messages_ko.yml` for Korean, `messages_it.yml` for Italian). Each message supports the usual `&` color codes plus inline hex via `&#RRGGBB`, `<#RRGGBB>`, or bare `#RRGGBB`, so you can theme every action-bar string directly in the template text. You can also fine-tune the vote thresholds and restrict praise to specific worlds using the new configuration blocks.

### Installation

1. Download the plugin JAR file and place it in your server's `plugins` directory.
2. Start your server to load the plugin.
3. Customize the `config.yml` file if needed and restart the server for changes to take effect.
4. Use the `/praise the <light|dark|sun|rain>` command to start controlling the environment!

### Usage

1. Join the server and use the `/praise the <light|dark|sun|rain>` command to initiate a vote.
2. Players receive real-time updates on the voting progress via action bar messages.
3. If the required number of votes is reached within 1 minute, the desired time or weather change is applied and announced to all players.
4. If not enough votes are received within 1 minute, the votes are reset and players are notified.

### Building the Plugin

If you want to build the plugin from source, follow these steps:

1. Clone the repository.
2. Ensure you have Maven installed.
3. Run `mvn clean install` to build the plugin.
4. The compiled JAR file will be located in the `target` directory.

### Event Listeners

- **Vote Expiration Task:** Periodically checks if the voting period has expired and resets votes if necessary.

### Dependencies

No external dependencies are required for this plugin.

### Troubleshooting

- Ensure that the plugin is correctly placed in the `plugins` directory and the server is restarted.
- Verify that the required commands are being typed correctly by players.
- Check the server console for any error messages related to the plugin.

### License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.

### Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your changes.

### Contact

For any questions or support, feel free to open an issue on the GitHub repository.
