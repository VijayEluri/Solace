package solace.cmd.play;

import solace.net.*;
import solace.util.*;
import solace.game.*;

/**
 * Wear command, used to equip items.
 * @author Ryan Sandor Richards
 */
public class Wear extends PlayCommand {
  public Wear(solace.game.Character ch) {
    super("wear", ch);
  }

  public boolean run(Connection c, String []params) {
    if (params.length < 2) {
      character.sendln("What would you like to wear?");
      return false;
    }

    String itemName = params[1];
    Item item = character.getItem(itemName);

    // Do they have the item?
    if (item == null) {
      character.sendln("You do not possess " + itemName);
      return false;
    }

    String itemDesc = item.get("description.inventory");
    String itemLevel = item.get("level");

    // Can it be equipped?
    if (!item.isEquipment()) {
      character.sendln("You cannot wear " + itemDesc);
      return false;
    }

    // Are they high enough level to wear it?
    // TODO Move the item level offset out into the world configuration
    if (itemLevel != null) {
      if (Integer.parseInt(itemLevel) > 10 + character.getLevel()) {
        character.sendln("You are not powerful enough to wear " + itemDesc);
        return false;
      }
    }

    // Attempt to equip the item
    try {
      Item old = character.equip(item);
      if (old != null) {
        character.sendln("You remove " + old.get("description.inventory"));
      }
      character.sendln("You wear " + itemDesc);
    }
    catch (NotEquipmentException e) {
      // This should not happen with checks above, log it and inform the user
      Log.error("Check for valid equipment failed in wear command");
      e.printStackTrace();
      character.sendln("You cannot wear " + itemDesc);
      return false;
    }

    return true;
  }
}
