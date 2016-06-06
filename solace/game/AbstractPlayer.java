package solace.game;

import solace.util.Clock;
import solace.util.Log;

/**
 * Implements common functionality of the player interface shared by both
 * characters and mobiles.
 * @author Ryan Sandor Richards
 */
public abstract class AbstractPlayer implements Player {
  // Instance variables
  PlayState state = PlayState.STANDING;
  Room room = null;
  int level;
  int hp;
  int mp;
  int sp;
  String majorStat = "none";
  String minorStat = "none";
  boolean onGCDCooldown = false;
  String comboAction = null;

  // Abstract Player Methods
  public abstract boolean hasPassive(String name);
  public abstract int getMaximumSkillLevelForPassive(String name);
  public abstract int getCooldownSkillLevel(String name);
  public abstract void die(Player killer);
  public abstract boolean isMobile();
  public abstract void sendMessage(String s);
  public abstract void send(String msg);
  public abstract void sendln(String msg);
  public abstract void wrapln(String msg);
  public abstract String getName();
  public abstract boolean hasName(String namePrefix);
  public abstract String getDescription();
  public abstract int getAttackRoll();
  public abstract int getHitMod();
  public abstract int getDamageMod();
  public abstract int getAverageDamage();
  public abstract int getNumberOfAttacks();

  /**
   * Gets the saving throw with the given name.
   * @param name Name of the saving throw.
   * @return The saving throw.
   * @see solace.game.Stats
   */
  protected int getSavingThrow(String name) {
    try {
      return Stats.getSavingThrow(this, name);
    } catch (InvalidSavingThrowException e) {
      Log.error(String.format(
        "Invalid saving throw name encountered: %s", name));
    }
    return 0;
  }

  /**
   * Determines the ability score of the given name.
   * @param name Name of the ability score.
   * @return The ability score for this player.
   * @see solace.game.Stats.getAbility(Player, String)
   */
  protected int getAbility(String name) {
    return Stats.getAbility(this, name);
  }

  /**
   * Gets the maximum value for the given resource.
   * @param name Name of the resource.
   * @return The maximum value of the resource for this player, or -1 if the
   *   provided resource name is invalid.
   */
  protected int getMaxResource(String name) {
    if (name.equals("hp")) {
      return Stats.getMaxHp(this);
    } else if (name.equals("mp")) {
      return Stats.getMaxMp(this);
    } else if (name.equals("sp")) {
      return Stats.getMaxSp(this);
    }
    return -1;
  }

  /**
   * @see solace.game.Player
   */
  public PlayState getPlayState() {
    return state;
  }

  /**
   * @see solace.game.Player
   */
  public void setPlayState(PlayState s) {
    state = s;
  }

  /**
   * @see solace.game.Player
   */
  public boolean isSleeping() {
    return state == PlayState.SLEEPING;
  }

  /**
   * @see solace.game.Player
   */
  public void setSleeping() {
    state = PlayState.SLEEPING;
  }

  /**
   * @see solace.game.Player
   */
  public boolean isResting() {
    return state == PlayState.RESTING;
  }

  /**
   * @see solace.game.Player
   */
  public void setResting() {
    state = PlayState.RESTING;
  }

  /**
   * @see solace.game.Player
   */
  public boolean isSitting() {
    return state == PlayState.SITTING;
  }

  /**
   * @see solace.game.Player
   */
  public void setSitting() {
    state = PlayState.SITTING;
  }

  /**
   * @see solace.game.Player
   */
  public boolean isRestingOrSitting() {
    return state == PlayState.RESTING || state == PlayState.SITTING;
  }

  /**
   * @see solace.game.Player
   */
  public boolean isStanding() {
    return state == PlayState.STANDING;
  }

  /**
   * @see solace.game.Player
   */
  public void setStanding() {
    state = PlayState.STANDING;
  }

  /**
   * @see solace.game.Player
   */
  public boolean isFighting() {
    return state == PlayState.FIGHTING;
  }

  /**
   * @see solace.game.Player
   */
  public void setFighting() {
    state = PlayState.FIGHTING;
  }

  /**
   * @see solace.game.Player
   */
  public boolean isStandingOrFighting() {
    return state == PlayState.FIGHTING || state == PlayState.STANDING;
  }

  /**
   * @see solace.game.Player
   */
  public Room getRoom() {
    return room;
  }

  /**
   * @see solace.game.Player
   */
  public void setRoom(Room r) {
    room = r;
  }

  /**
   * @see solace.game.Player
   */
  public int getLevel() { return level; }

  /**
   * @see solace.game.Player
   */
  public void setLevel(int l) { level = l; }

  /**
   * @see solace.game.Player
   */
  public void setMajorStat(String name) { majorStat = name; }

  /**
   * @see solace.game.Player
   */
  public void setMinorStat(String name) { minorStat = name; }

  /**
   * @see solace.game.Player
   */
  public String getMajorStat() { return majorStat; }

  /**
   * @see solace.game.Player
   */
  public String getMinorStat() { return minorStat; }

  /**
   * @return The player's current hit points.
   */
  public int getHp() { return hp; }

  /**
   * Sets the player's current hit points.
   * @param v HP to set.
   */
  public void setHp(int v) { hp = v; }

  /**
   * @see solace.game.Player
   */
  public int getMaxHp() { return getMaxResource("hp"); }

  /**
   * @return The player's current mp.
   */
  public int getMp() { return mp; }

  /**
   * Sets the player's current mp.
   * @param v MP to set.
   */
  public void setMp(int v) { mp = v; }

  /**
   * @see solace.game.Player
   */
  public int getMaxMp() { return getMaxResource("mp"); }

  /**
   * @return The character's current sp.
   */
  public int getSp() { return sp; }

  /**
   * Sets the character's current sp.
   * @param v SP to set.
   */
  public void setSp(int v) { sp = v; }

  /**
   * @see solace.game.Player
   */
  public int getMaxSp() { return getMaxResource("sp"); }

  /**
   * @see solace.game.Player
   */
  public int getStrength() { return getAbility("strength"); }

  /**
   * @see solace.game.Player
   */
  public int getVitality() { return getAbility("vitality"); }

  /**
   * @see solace.game.Player
   */
  public int getMagic() { return getAbility("magic"); }

  /**
   * @see solace.game.Player
   */
  public int getSpeed() { return getAbility("speed"); }

  /**
   * @see solace.game.Player
   */
  public int getWillSave() { return getSavingThrow("will"); }

  /**
   * @see solace.game.Player
   */
  public int getReflexSave() { return getSavingThrow("reflex"); }

  /**
   * @see solace.game.Player
   */
  public int getResolveSave() { return getSavingThrow("resolve"); }

  /**
   * @see solace.game.Player
   */
  public int getVigorSave() { return getSavingThrow("vigor"); }

  /**
   * @see solace.game.Player
   */
  public int getPrudenceSave() { return getSavingThrow("prudence"); }

  /**
   * @see solace.game.Player
   */
  public int getGuileSave() { return getSavingThrow("guile"); }

  /**
   * @return The player's armor class.
   */
  public int getAC() { return Stats.getAC(this); }

  /**
   * @see solace.game.Player
   */
  public int applyDamage(int damage) {
    hp -= damage;
    return damage;
  }

  /**
   * @see solace.game.Player
   */
  public boolean isDead() {
    return getHp() <= 0;
  }

  /**
   * @see solace.game.Player
   */
  public boolean isOnGCD() {
    return onGCDCooldown;
  }

  /**
   * @see solace.game.Player
   */
  public void setOnGCD() {
    onGCDCooldown = true;
    // TODO GCD cooldowns should really be independent of the global clock...
    Clock.getInstance().schedule(
      String.format("GCD for %s", getName()),
      2,
      new Runnable() { public void run() { onGCDCooldown = false; } });
  }

  /**
   * @see solace.game.Player
   */
  public void setComboAction(String action) {
    comboAction = action;
  }

  /**
   * @see solace.game.Player
   */
  public String getComboAction() {
    return comboAction == null ? "" : comboAction;
  }
}
