package solace.cmd.cooldown;

import java.util.*;
import solace.game.*;
import solace.net.*;
import java.io.*;
import solace.util.*;

/**
 * "Flurry of blows" is a cooldown action that does 150 potency damage to the
 * attacker's target.
 * @author Ryan Sandor Richards
 */
public class Flurry extends CooldownCommand {
  static final int POTENCY = 150;

  public Flurry(Player p) {
    super("flurry", p);
    setDisplayName("flurry of blows");
    setCooldownDuration(CooldownCommand.GLOBAL_COOLDOWN);
    setInitiatesCombat(true);
    addResourceCost(new SpCost(2));
  }

  public boolean execute(int level, Player target) {
    return executePhysicalAttack(target, POTENCY);
  }
}