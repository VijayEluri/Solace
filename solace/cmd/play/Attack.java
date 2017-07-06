package solace.cmd.play;

import solace.game.*;
import solace.net.*;

/**
 * The attack command is used to attack other players in the game and initiate
 * combat.
 * @author Ryan Sandor Richards
 */
public class Attack extends PlayStateCommand {
  public Attack(solace.game.Character ch) {
    super("attack", ch);
  }

  public void run(Connection c, String []params) {
    Room room = character.getRoom();

    if (character.isFighting()) {
      character.sendln("You are already in battle!");
      return;
    }

    if (character.isRestingOrSitting()) {
      character.sendln("You cannot initiate battle unless standing and alert.");
      return;
    }

    if (character.isSleeping()) {
      character.sendln("You dream of attacking, as you are asleep.");
      return;
    }

    if (params.length < 2) {
      character.sendln("Who would you like to attack?");
      return;
    }

    String name = params[1];
    Player target = room.findPlayerIfVisible(name, character);

    if (target == null) {
      character.sendln(String.format("You do not see %s here.", name));
      return;
    }

    if (!target.isMobile()) {
      character.sendln("You cannot attack other players.");
      return;
    }

    if (target.isDead()) {
      character.sendln("You cannot attack a target that is already dead!");
      return;
    }

    if (((Mobile)target).isProtected()) {
      character.sendln("You cannot attack " + target.getName() + ".");
      return;
    }

    // TODO going to have to modify this when player groups come along
    if (target.isFighting()) {
      character.sendln(
        String.format("%s is already engaged in battle.",
        target.getName()
      ));
      return;
    }

    // Start the battle
    BattleManager.initiate(character, target);
    c.skipNextPrompt();
  }
}
