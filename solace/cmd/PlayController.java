package solace.cmd;

import java.util.*;
import solace.game.*;
import solace.net.*;
import java.io.*;
import solace.util.*;
import solace.cmd.play.*;
import solace.cmd.admin.*;
import solace.script.Commands;
import solace.script.ScriptedCommand;
import com.google.common.base.Joiner;

/**
 * Main game play controller (the actual game).
 * @author Ryan Sandor Richards
 */
public class PlayController extends AbstractStateController {
  solace.game.Character character;

  static final String[] moveAliases = {
    "move", "go", "north", "south",
    "east", "west", "up", "down",
    "exit", "enter"
  };

  static final String[] attackAliases = {
    "attack", "kill", "fight"
  };

  static final String[] buffsAliases = {
    "buffs", "affects"
  };

  /**
   * Generates the dynamic custom prompt for the player.
   * NOTE This may not belong here, factor out?
   * TODO %t    Target health percentage
   * TODO %T    Target health percentage, with colors
   * @return The generated prompt for the player.
   */
  public String getPrompt() {
    String out = new String(character.getPrompt());
    out = out
      .replace("%h", character.getHp() + "")
      .replace("%H", character.getMaxHp() + "")
      .replace("%m", character.getMp() + "")
      .replace("%M", character.getMaxMp() + "")
      .replace("%s", character.getSp() + "")
      .replace("%S", character.getMaxSp() + "")
      .replace("%g", character.getGold() + "")
      .replace("%a", character.getRoom().getArea().getTitle());

    // TODO Refactor the fuck out of me
    int targetHealthRemaning = -1;
    if (character.isFighting()) {
      Battle battle = BattleManager.getBattleFor(character);
      if (battle == null) {
        Log.error(String.format(
          "Player '%s' marked as fighting but not in battle.",
          character.getName()
        ));
      } else {
        Player target = battle.getTargetFor(character);
        if (target != null) {
          targetHealthRemaning = (int)(
            100.0 * (double)target.getHp() / (double)target.getMaxHp()
          );
        }
      }
    }

    if (targetHealthRemaning < 0) {
      out = out.replace("%t", "").replace("%T", "");
    } else {
      String healthRemaining = targetHealthRemaning + "%%";
      out = out.replace("%t", " " + healthRemaining);

      if (targetHealthRemaning > 85) {
        out = out.replace("%T", "{G}" + healthRemaining + "{x}");
      } else if (targetHealthRemaning > 70) {
        out = out.replace("%T", "{g}" + healthRemaining + "{x}");
      } else if (targetHealthRemaning > 55) {
        out = out.replace("%T", "{y}" + healthRemaining + "{x}");
      } else if (targetHealthRemaning > 40) {
        out = out.replace("%T", "{Y}" + healthRemaining + "{x}");
      } else if (targetHealthRemaning > 25) {
        out = out.replace("%T", "{r}" + healthRemaining + "{x}");
      } else {
        out = out.replace("%T", "{R}" + healthRemaining + "{x}");
      }
    }
    // TODO end "refactor the fuck out of me"

    return out.replace("%%", "%");
  }

  /**
   * Creates a new game play controller.
   * @param c The connection.
   * @param ch The character.
   * @throws GameException if anything goes wrong when logging the user in.
   */
  public PlayController(Connection c, solace.game.Character ch)
    throws GameException
  {
    // Initialize the menu
    super(c, "Sorry, that is not an option. Type '{y}help{x}' to see a list.");
    character = ch;

    // Character location initialization
    if (ch.getRoom() == null) {
      Room room = World.getDefaultRoom();
      room.getCharacters().add(ch);
      ch.setRoom(room);
    }

    // Inform other players in the room that they player has entered the game
    ch.getRoom()
      .sendMessage(ch.getName() + " has entered the game.", character);

    // Add commands
    addCommands();

    // Place the player in the world
    World.getActiveCharacters().add(ch);
    c.sendln("\n\rNow playing as {y}" + ch.getName() + "{x}, welcome!\n\r");

    // Describe the room to the player
    if (ch.isSleeping()) {
      ch.sendln("You are fast asleep.");
    } else {
      c.sendln(ch.getRoom().describeTo(ch));
    }
  }

  /**
   * Determines if a input command from a user represents a hotbar command.
   * @param  input [description]
   * @return       [description]
   */
  public boolean isHotbarCommand(String input) {
    return input.length() == 1 && (
      (input.charAt(0) >= '0' && input.charAt(0) <= '9') ||
      input.charAt(0) == '-' ||
      input.charAt(0) == '='
    );
  }

  /**
   * Parses input commands while accounting for hotbar commands, macros, etc.
   * @param input Input to parse.
   */
  public void parse(String input) {
    if (character.hasBuff("stun")) {
      character.sendln("You are stunned and cannot act!");
      return;
    }

    if (character.isCasting()) {
      character.sendln("You are focusing on casting and cannot act further!");
      return;
    }

    if (input == null || connection == null || input.length() < 1) return;
    String[] params = input.split("\\s");
    if (params.length < 1) return;

    if (isHotbarCommand(params[0])) {
      String command = character.getHotbarCommand(params[0]);
      if (command != null && command.length() > 0) {
        params[0] = command;
      }
      super.parse(Joiner.on(" ").join(params));
      return;
    }

    super.parse(input);
  }

  /**
   * Adds basic gameplay commands to the controller.
   */
  protected void addCommands() {
    addCommand(new Quit(character));
    addCommand(new Help());
    addCommand(moveAliases, new Move(character));
    addCommand(new Look(character));
    addCommand(new Say(character));
    addCommand(new Scan(character));
    addCommand(new Tick());

    addCommand(new Score(character));
    addCommand(new Worth(character));
    addCommand(new ListSkills(character));
    addCommand(buffsAliases, new solace.cmd.play.Buffs(character));
    addCommand(new Cooldown(character));
    addCommand(new Passive(character));

    addCommand(new Wear(character));
    addCommand(new Equipment(character));
    addCommand(new Remove(character));

    addCommand(new ShopList(character));
    addCommand(new ShopBuy(character));
    addCommand(new ShopAppraise(character));
    addCommand(new ShopSell(character));

    addCommand(attackAliases, new Attack(character));
    addCommand(new Flee(character));

    addCommand(new Sit(character));
    addCommand(new Stand(character));
    addCommand(new Rest(character));
    addCommand(new Sleep(character));
    addCommand(new Wake(character));

    addCommand(new Prompt(character));
    addCommand(new Hotbar(character));

    // Add all scripted play commands
    for (ScriptedCommand command : Commands.getCommands()) {
      addCommand(command.getInstance(character));
    }

    // Emotes
    Emote emote = new Emote(character);
    addCommand(emote);
    addCommand(Emotes.getInstance().getEmoteAliases(), new Emote(character));

    // Admin Commands
    if (character.getAccount().isAdmin()) {
      addCommand(new Inspect(character));
      addCommand(new solace.cmd.admin.Set(character));
    }
  }
}
