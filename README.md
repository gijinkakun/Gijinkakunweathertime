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
- **Permission:** `gijinkakunweathertime.praise` or specific permissions for each type (see below).

## Permissions

- `gijinkakunweathertime.praise`: Allows the player to use all `/praise` commands (default: op)
  - `gijinkakunweathertime.praise.light`: Allows the player to praise the light (default: op)
  - `gijinkakunweathertime.praise.dark`: Allows the player to praise the dark (default: op)
  - `gijinkakunweathertime.praise.sun`: Allows the player to praise the sun (default: op)
  - `gijinkakunweathertime.praise.rain`: Allows the player to praise the rain (default: op)
- `gijinkakunweathertime.initiatevote`: Allows the player to initiate a vote (default: op)

## Configuration

The plugin provides extensive configuration options through the `config.yml` file:

```yaml
messages:
  cooldown: "You have already praised. On cooldown for {minutes} minutes."
  vote_progress: "{current}/{required} have praised for {vote}."
  vote_success: "{vote} has been praised!"
  vote_expired: "Time has expired. Not enough votes for {vote}."
  usage: "Please use one of the following commands: /praise the light, /praise the dark, /praise the sun, /praise the rain."
  vote_in_progress: "A different vote is in progress."
  already_praised: "The {vote} has already been praised recently."
language: "en"
cooldown_time: 10 (Cooldown time in minutes)
vote_duration: 1 (Duration of the vote in minutes)

### Customization

Messages and settings can be customized to suit your server's needs. The `language` setting allows for multi-language support by providing different message files (e.g., `messages_en.yml` for English).

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

- **PlayerChatEvent:** Listens for specific phrases in chat to register votes.
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