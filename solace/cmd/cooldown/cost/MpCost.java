package solace.cmd.cooldown;

import solace.game.Player;

/**
 * Resource cost for magic points.
 * @author Ryan Sandor Richards
 */
public class MpCost extends AbstractResourceCost {
  /**
   * Creates a new mp cost for the given percentage amount.
   * @param a Percentage of max mp for the cost.
   */
  public MpCost(int a) { super(a); }

  /**
   * Creates a new mp cost of the given type and amount.
   * @param t Type for the cost.
   * @param a Amount for the cost.
   * @see solace.cmd.cooldown.AbstractResourceCost.CostType
   */
  public MpCost(AbstractResourceCost.CostType t, int a) { super(t, a); }

  /**
   * @see solace.cmd.cooldown.AbstractResourceCost
   */
  protected int getPlayerResource(Player p) { return p.getMp(); }

  /**
   * @see solace.cmd.cooldown.AbstractResourceCost
   */
  protected int getPlayerResourceMax(Player p) { return p.getMaxMp(); }

  /**
   * @see solace.cmd.cooldown.ResourceCost
   */
  public void withdraw(Player p) {
    if (!canWithdraw(p)) return;
    int cost = getCost(p);
    if (p.hasPassive("metamagical")) {
      cost = (int)(0.9 * cost);
    }
    p.setMp(getPlayerResource(p) - cost);
  }

  /**
   * @see solace.cmd.cooldown.ResourceCost
   */
  public String getInsufficentResourceMessage() {
    return "Not enough {mmp{x.";
  }
}