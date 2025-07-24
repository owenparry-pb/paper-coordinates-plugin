# CoordinatesPlugin

A simple Minecraft plugin for Spigot/Bukkit servers that displays player coordinates in real-time.  
Players can choose to display coordinates in the action bar or the sidebar scoreboard, and toggle display on/off.

## Features

- Shows X, Y, Z coordinates live to players.
- Players can toggle coordinates display (`/coords on` / `/coords off`).
- Players can choose between action bar or scoreboard sidebar (`/coords location actionbar` or `/coords location scoreboard`).
- Automatically checks for plugin updates on server start and logs the result locally.

## Installation

1. **Add Dependencies**

   Ensure your build system includes the following dependencies:

   **For Maven (`pom.xml`):**
   ```xml
   <dependency>
       <groupId>javax.json</groupId>
       <artifactId>javax.json-api</artifactId>
       <version>1.1.4</version>
   </dependency>
   <dependency>
       <groupId>net.kyori</groupId>
       <artifactId>adventure-api</artifactId>
       <version>4.14.0</version>
   </dependency>
   ```
   _(Replace `adventure-api` version as needed for your environment.)_

2. **Build the Plugin**

   - Clone this repository.
   - Build using Maven or your preferred build tool.
   - Place the generated JAR file into your server's `plugins` folder.

3. **Start the Server**

   - On server startup, the plugin will check for new releases on GitHub and log the result to `plugins/CoordinatesPlugin/update-check.log`.

## Usage

- `/coords on`  
  Enables live coordinates display in your chosen location.

- `/coords off`  
  Disables coordinates display.

- `/coords location actionbar`  
  Shows coordinates in the action bar (default).

- `/coords location scoreboard`  
  Shows coordinates in the sidebar scoreboard (smaller text).

When a player joins, they are notified about how to toggle coordinates.

## Update Check Logging

Each time the plugin starts, it checks for the latest release from the repository and logs the result to `plugins/CoordinatesPlugin/update-check.log`.  
No update is performed automatically; this is for informational purposes.

## Example plugin.yml

```yaml
name: CoordinatesPlugin
main: com.parryproject.CoordinatesPlugin
version: 1.0.0
api-version: 1.16
commands:
  coords:
    description: Toggle coordinates display or change location.
    usage: /coords <on|off|location>
```

## Configuration

No additional configuration is required.

## Contributing

Feel free to fork and submit pull requests for new features or improvements!

## License

MIT
