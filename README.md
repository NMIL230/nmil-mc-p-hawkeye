# Hawkeye
Hawkeye is a Spigot server plugin designed to collect various types of information from players within the game, establishing a WebSocket server to send this information in JSON format to a specified port.

## Installation

### Spigot Server
To install the Spigot server, please follow the detailed instructions available on the Spigot website:
[Spigot Installation Guide](https://www.spigotmc.org/wiki/spigot-installation/)

### Hawkeye Plugin
To install the Hawkeye plugin:
1. Download the `Hawkeye.jar` file, or you can find it in the target directory in the hawkeye project.
2. Move the `Hawkeye.jar` file into the `plugins` directory inside your Spigot server directory.

### Hawkeye-web
Hawkeye-web is a Meteor-based project that listens to the WebSocket port to receive, categorize, and store various messages from Hawkeye. These messages include plugin usage, server performance information, and player logs, which are then stored in a local MongoDB database.

To install Hawkeye-web:
1. Ensure that you have both Node.js and Meteor installed on your system.
2. Clone or download the Hawkeye-web project repository to your local machine.
3. Navigate to the project directory in your terminal.
4. Run the following commands to install dependencies and start the server:

    ```bash
    npm install
    meteor run
    ```

Make sure to replace the `Hawkeye.jar` file location with the actual link or method where the user can obtain your plugin. This README.md provides a basic structure for your project documentation, including installation instructions and basic commands to get started.




