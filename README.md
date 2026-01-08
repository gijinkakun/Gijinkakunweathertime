# GijinkakunWeatherTime Plugin

GijinkakunWeatherTime is a Bukkit/Spigot plugin that allows players to control the time and weather in the game through voting. Players can use specific chat commands to vote for changing the time of day or the weather, and the changes are applied if a majority of online players participate in the vote.

## Features

- Change the time of day with commands to set it to day or night.
- Change the weather conditions with commands to set it to clear or stormy.
- Voting system that requires a majority of online players to participate within 1 minute.
- Real-time updates on the voting progress via action bar messages.
- Command auto-completion for ease of use.
- Detailed logging for easier debugging and monitoring.
- Configurable messages, cooldown times, and vote durations.
- Multi-language support (configurable).

## Commands and permissions

| Command | What it does | Permission | Default |
| --- | --- | --- | --- |
| `/praise the <day|night|clear|storm>` | Starts or withdraws a vote for the chosen option. | `gijinkakunweathertime.praise` | op |
| `/praise force <day|night|clear|storm>` | Immediately applies the change without a vote (staff). | `gijinkakunweathertime.force` | op |
| `/praise reload` | Reloads `config.yml` and messages. | `gijinkakunweathertime.reload` | op |

Additional permission flags:

- `gijinkakunweathertime.bypasscooldown`: Ignore the praise cooldown (default: op)

## Configuration

The plugin provides configuration options through the `config.yml` file:

```yaml
language: "en" (Language file to load: messages_<language>.yml)
cooldown_time: 10 (Cooldown time in minutes)
vote_duration: 1 (Duration of the vote in minutes)
vote_thresholds:
  percentage: 50 (Percent of eligible players required)
worlds:
  enabled: [] (Only allow these worlds; empty = all)
  disabled:
    - id: "world_nether"
      name: "Nether"
    - id: "world_the_end"
      name: "End"

### Customization

Messages live in `messages_<language>.yml` files stored next to `config.yml`. The `language` setting chooses which file to load (e.g., `messages_en.yml` for English, `messages_es.yml` for Spanish, `messages_fr.yml` for French, `messages_de.yml` for German, `messages_pt_br.yml` for Brazilian Portuguese, `messages_ru.yml` for Russian, `messages_zh_cn.yml` for Simplified Chinese, `messages_ja.yml` for Japanese, `messages_ko.yml` for Korean, `messages_it.yml` for Italian). Each message supports the usual `&` color codes plus inline hex via `&#RRGGBB`, `<#RRGGBB>`, or bare `#RRGGBB`, so you can theme every action-bar string directly in the template text. You can also fine-tune the vote thresholds and restrict praise to specific worlds using the configuration blocks shown above.

### Installation

1. Download the plugin JAR file and place it in your server's `plugins` directory.
2. Start your server to load the plugin.
3. Customize the `config.yml` file if needed and restart the server for changes to take effect.
4. Use the `/praise the <day|night|clear|storm>` command to start controlling the environment!

### Usage

1. Join the server and use the `/praise the <day|night|clear|storm>` command to initiate a vote.
2. Players receive real-time updates on the voting progress via action bar messages.
3. If the required number of votes is reached within 1 minute, the desired time or weather change is applied and announced to all players.
4. If not enough votes are received within 1 minute, the votes are reset and players are notified.

### Building the Plugin

If you want to build the plugin from source using Gradle, follow these steps:

1. Clone the repository.
2. Ensure you have Gradle installed (or generate the wrapper with `gradle wrapper --gradle-version 8.7` and use `./gradlew`).
3. Run `gradle build` (or `./gradlew build`) to build the plugin.
4. The compiled JAR file will be located in `build/libs/` (shadowed jar without classifier).

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
