package solace.cmd.cooldown;

import java.util.*;
import solace.game.*;
import solace.net.*;
import java.io.*;
import solace.util.*;

/**
 * "Riposte" is a cooldown action that does 150 potency damage to the attacker's
 * target. If executed immediately after a "slash" cooldown then this has a
 * combo potency of 350.
 * @author Ryan Sandor Richards
 */
public class Riposte extends CooldownCommand {
  public Riposte(Player p) {
    super("riposte", p);
    setCooldownDuration(CooldownCommand.GLOBAL_COOLDOWN);
    setInitiatesCombat(true);
    setBasePotency(150);
    setComboPotency(350);
    setCombosWith("slash");
    addResourceCost(new SpCost(6));
  }

  public boolean execute(int level, Player target) {
    return executeAttack(target);
  }
}
