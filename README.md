# Lockers | Backup Armor Storage & Swap System - Plug and Play!

**Lockers** is a powerful, flexible plugin designed for managing backup armor in Minecraft. It provides an easy-to-use way to store, access, and swap armor sets through a customizable GUI, making it ideal for both players and server admins. With simple commands, permission handling, and instant armor row swapping, Lockers ensures smooth server performance and keeps your players' gear secure.

## Features:

- **Backup Armor Storage**: Securely store backup armor sets in a customizable interface.
- **Row Swapping**: Swap the first armor row using Shift + Right-Click in the air or towards a block.
- **Fully Customizable GUI**: Tailor the plugin to fit your server with easily configurable settings.
- **Easy Setup**: Plug-and-play functionality for a quick start.
- **Open-Source & Paid**: Access the source code, modify it, and enjoy premium support for paying users.

## Permissions:

- `lockers.row.x`: Permission to access specific armor rows (x being any number above 0). This permission is not configurable and players need the appropriate permissions to interact with each row.
- `lockers.use`: Permission to use the `/locker` command to interact with the locker.
- `lockers.admin`: Admin-level permission for managing and opening other players' lockers.
- Additional permissions can be configured in `config.yml`.

## Commands:

- **/locker**: Opens the locker to interact with stored armor.
  - Aliases: `/loc`, `/wardrobe`, `/wr`
  - Permission: `lockers.use`
- **/locker open <player>**: Opens another player's locker (staff only).
  - Permission: `lockers.admin`
- **/locker reload**: Reloads the plugin’s configuration.
  - Permission: `lockers.admin`

## Configuration:

The plugin uses `MySQL` or `SQLite` for database storage. Configuration for the locker command, GUI, row swapping, and more is customizable through `config.yml`.

## Important Notes:

- Once downloaded, you are responsible for the use of the plugin. Unauthorized redistribution or leaks of the plugin may result in removal from the buyers list and a report to SpigotMC.
- Redistribution, modification, or misuse outside the terms of the open-source license is prohibited.
- No refunds are provided, as access is granted immediately upon purchase.

## License:

Lockers is a paid resource with open-source access. You may modify and use the code under the terms of the open-source license, but redistribution must comply with the provided license.

## Commitment:

I won’t add unnecessary or bloated features to the plugin. If there are no bugs, issues, or useful feature requests, I won’t add features just for the sake of adding them. I strive to keep this plugin clean and efficient for server owners.

If you enjoy using **Lockers**, consider leaving a donation, rating, or starring the repository. Your support is always appreciated!

For bugs or issues, please use the discussion forum or PM me directly.

[Lockers Plugin on SpigotMC](https://www.spigotmc.org/resources/lockers-effortless-backup-armor-storage-swap-plug-and-play.120722/)
