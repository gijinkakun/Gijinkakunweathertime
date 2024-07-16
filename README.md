# GijinkakunWeatherTime Plugin

GijinkakunWeatherTime is a Bukkit/Spigot plugin that allows players to control the time and weather in the game through voting. Players can use specific chat commands to vote for changing the time of day or the weather, and the changes are applied if a majority of online players participate in the vote.

## Features

- Change the time of day with commands to set it to day or night.
- Change the weather conditions with commands to set it to sunny or rainy.
- Voting system that requires a majority of online players to participate within 1 minute.
- Real-time updates on the voting progress via action bar messages.
- Command auto-completion for ease of use.

## Commands

### `/praise the <light|dark|sun|rain>`

Initiates a vote to change the time or weather based on the specified option.

- **Usage:** `/praise the <light|dark|sun|rain>`
- **Permission:** This command can only be run by players.

## Permissions

No specific permissions are required for this plugin. Any player can initiate a vote using the specified commands.

## Configuration

The plugin does not require any specific configuration files. All functionality is controlled through commands and in-game interactions.

## Installation

1. Download the plugin JAR file and place it in your server's `plugins` directory.
2. Start your server to load the plugin.
3. Use the `/praise the <light|dark|sun|rain>` command to start controlling the environment!

## Building the Plugin

If you want to build the plugin from source, follow these steps:

1. Clone the repository.
2. Ensure you have Maven installed.
3. Run `mvn clean install` to build the plugin.
4. The compiled JAR file will be located in the `target` directory.

## Usage

1. Join the server and use the `/praise the <light|dark|sun|rain>` command to initiate a vote.
2. Players receive real-time updates on the voting progress via action bar messages.
3. If the required number of votes is reached within 1 minute, the desired time or weather change is applied and announced to all players.
4. If not enough votes are received within 1 minute, the votes are reset and players are notified.

## Event Listeners

- **PlayerChatEvent:** Listens for specific phrases in chat to register votes.
- **Vote Expiration Task:** Periodically checks if the voting period has expired and resets votes if necessary.

## Dependencies

- No external dependencies are required for this plugin.

## Troubleshooting

- Ensure that the plugin is correctly placed in the `plugins` directory and the server is restarted.
- Verify that the required commands are being typed correctly by players.
- Check the server console for any error messages related to the plugin.

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your changes.

## Contact

For any questions or support, feel free to open an issue on the GitHub repository.
