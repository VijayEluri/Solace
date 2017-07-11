# The Game Directory

The `game/` directory holds all of the data for a game using the Solace engine. This document describes
how to modify and add additional files so an author can customize a game running on the solace engine
without the need to directly program against the engine itself.

## Game Files and Common Extensions
Many game objects the engine processes can be describe by using files of various formats within the
game directory. Here is a quick list of common file extension recognized by the engine and what each
represents:

- `.area.xml`: Area files
- `.config.xml`: Engine configuration
- `.emote.json`: Emote command definition
- `.help.md`: Help pages
- `.message.txt`: Game messages
- `.race.json`: Player race definitions
- `.js`: Game script
- `.skill.json`: Player skill definition

### Area Files

**Extension:** `.area.xml`

Areas define the rooms, items, and mobiles that make up a game world for the solace engine. On game load the engine
will search the `game/` directory for any files matching the `.area.xml` extension and load the area as described in
each file it finds. Any errors the engine encounters while processing area files will be output as in the server's
log.

A full treatment of area creation is outside the scope of this document, please see [Solace Area Creation](./areas.md)
for more information.


### Engine Configurations

**Extension:** `.config.xml`

Engine configuration are used to override specific aspects of the game engine, such as how combat calculations are
performed, the id of the starting room, etc. Configurations are structured as a hierarchical key-value map and can
be accessed via the `solace.io.Config` utility from both within the engine and by scripts.

Here's an example from the `prompt.js` command script that uses the configuration to fetch the default game prompt:
```js
var defaultPrompt = Config.get('default.prompt');
```

On game load the engine will search the `game/` directory for any files matching the `.config.xml` extension and load
the configurations into the `Config` utility. This means a game author can add as many additional custom configurations
as desired (as it may make it easier to tweak scripts, etc.).

### Emote Commands

**Extension:** `.emote.json`

Emotes are role playing commands that have no impact other than to add flavor to interactions between players.
On game load the engine will search the `game/` directory for any JSON files matching the `.emote.json` extension
and load them into the game. If the game encounters an unexpected format for the JSON file a warning will be
given in the server logs and the emote will be skipped.

Here is an example emote that conforms to the expected format:
```json
{
  "name": "scowl",
  "toPlayer": "You scowl menacingly.",
  "toRoom": "%s scowls menacingly.",
  "withTarget": {
    "toPlayer": "You narrow your eyes, cock your head, and scowl at %s.",
    "toTarget": "%s gets a mean look upon their face and scowls at you.",
    "toRoom": "Something's amiss... %s just gave %s a pretty mean look."
  }
}
```
### Help Pages

**Extension:** `.help.md`

Help pages provide information about the game for reference by players. On load the engine
finds all files in the `game/` directory with the extension `.help.md` and loads them into the
help system. The system itself is features both direct "by name" lookup and full text searching
capabilities.

The following markdown constructs are used when rendering help pages:

1. `# Title Text` - Top level header, denotes the "page title"
2. `## Sub header` - Subsection headers, highlighted in cyan.
3. \``text`\` - Highlights the given text in yellow, used for referencing other help pages or commands.
4. `[text]` - Highlights the given text in cyan, used for parameters.
5. `*` - Used for list bullets, highlighted in red.

In addition, help pages can be annotated with the following:

* `@author(Author Name)` - Adds an author name to the page (currently unused).
* `@name(lookup-name)` - Sets the lookup name for directly accessing a page.
* `@admin` - Denotes the help page as "admin only"

The annotations are stripped from the resulting page output that is sent to players
after they have been processed and collected.

**IMPORTANT:** If the system encounters a page that does not provide a title and name,
or duplicates an existing title or name then the page will be skipped and a warning will
be displayed in the server's log output.

Here is an example of a help file:

```md
@name(set)
@admin
@author(Ryan Sandor Richards)

# Admin Command: Set
Usage: `set` [player-name] [parameter] [value]

The `set` command allows an administrator to set various values for a player.
The following [parameter] items are currently supported:

* level - Sets the level of the player
* race - Sets the race of the player
* hp - Sets the hit points of the player
* mp - Sets the magic points of the player
* state - Sets the state (e.g. standing, sitting, resting, etc.) of the player
* immortal - Flags the player as immortal negating all damage (for testing)
```

### Game Messages

**Extension:** `.message.txt`

Game messages are relatively large plain text messages that are displayed in the game. Currently the engine supports
the use of messages in only a few ways, but they're ideal for a variety of situations (banners, ascii art, etc.). On
load the engine loads all files in the `game/` directory with the extension `.message.txt` as messages. The resulting
key name for the message is the filename before the extension. For instance, a message file named `cool.message.txt`
can be accessed via the messages utility using `Messages.get("cool")`.

### Player Races

**Extension:** `.race.json`

Races are one of the primary character customization methods of the solace engine. Each race grants a single passive
enhancement and cooldown action to player characters. On game load the engine will search the `game/` directory for any
JSON files with the `.race.json` extension and register them as player races. Any errors that occur while loading a
race will be displayed in the server's log output.

here's an example of the format expected for `.race.json` files:
```json
{
  "name": "dwarf",
  "passives": ["stout-hearted"],
  "cooldowns": ["skullknock"]
}
```

### Game Scripts

**Extension:** `.js`

Game scripts are JavaScript files that the game loads on startup that add programmatic functionality to the game.
A full treatment on scripting is out of the scope of this document, see [scripting.md](./scripting.md) for more
details.

### Player Skills

**Extension:** `.skill.json`

Skills are one of the primary character customization methods of the solace engine. They define a set of passive
enhancements and cooldown actions that players can use to interact with the game world. On game load the engine will
search the `game/` directory for any JSON files with the `.skill.json` extension and register them as game skills.
Any errors encountered while processing skill definitions will be displayed in the server's log output.

Here's an example of the format for `.skill.json` files:
```json
{
  "id": "one-handed",
  "name": "One-handed",
  "passives": [
    { "level": 15, "name": "parry" },
    { "level": 40, "name": "second attack" },
    { "level": 80, "name": "third attack" },
    { "level": 100, "name": "battle trance" },
  ],
  "cooldowns": [
    { "level": 5, "name": "flurry" },
    { "level": 35, "name": "slash" },
    { "level": 65, "name": "riposte" },
    { "level": 95, "name": "coup" }
  ]
}
```