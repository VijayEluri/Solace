package solace.game;

import solace.cmd.play.Move;
import solace.util.*;
import solace.game.*;
import java.util.*;

/**
 * Represents a mobile in the game world.
 * @author Ryan Sandor Richards
 */
public class Mobile extends Template {
  public enum State {
    STATIONARY,
    WANDERING
  }

  private solace.game.Character character;
  private boolean isPlaced = false;
  private State state = State.STATIONARY;

  // Allows the mobile to be, well, mobile...
  private Move move;
  private Clock.Event wanderEvent;

  /**
   * Creates a new mobile.
   */
  public Mobile() {
    super();
    character = new solace.game.Character("");
    move = new Move(character);
    wanderEvent = null;
  }

  /**
   * Sets the state of the mobile.
   * @param s State to set.
   */
  public void setState(State s) {
    state = s;

    if (state == State.STATIONARY) {
      if (wanderEvent != null) {
        wanderEvent.cancel();
        wanderEvent = null;
      }
    }
    else if (state == State.WANDERING) {
      if (wanderEvent == null) {
        wanderEvent = Clock.getInstance().interval(
          "mobile.wander",
          15,
          new Runnable() {
            public void run() {
              // Get a random exit and move there
              Random rand = new Random();

              // 50-50 shot of just staying put
              if (rand.nextInt(2) == 0) {
                return;
              }

              // Move to a random exit
              Room origin = character.getRoom();
              List<Exit> exits = origin.getExits();
              Exit exit = exits.get(rand.nextInt(exits.size()));
              String direction = exit.getNames().get(0);
              Room destination = area.getRoom(exit.getToId());
              if ( move.run(null, new String[] { direction }) ) {
                swapRooms(origin, destination);
              }
            }
          }
        );
      }
    }
  }

  /**
   * Moves a mobile from one room to another.
   * @param origin Room to remove the mobile from.
   * @param destination Room to put the mobile in.
   */
  protected void swapRooms(Room origin, Room destination) {
    origin.getMobiles().remove(this);
    destination.getMobiles().add(this);
  }

  /**
   * @return The state of the mobile.
   */
  public State getState() {
    return state;
  }

  /**
   * Sets the state of the mobile based on its string representation.
   * @param name Name of the state to set for the mobile.
   */
  public void setState(String name) {
    if (name.startsWith("stationary")) {
      setState(State.STATIONARY);
    }
    else if (name.startsWith("wandering")) {
      setState(State.WANDERING);
    }
  }

  /**
   * Places the mobile into the game world.
   */
  public void place(Room room) {
    if (isPlaced) { return; }
    isPlaced = true;

    character.setName(get("description.name"));
    character.setDescription(get("description"));

    String state = get("state");
    setState(get("state"));

    String spawn = get("description.spawn");
    if (spawn == null) {
      spawn = get("description.name") + " enters.";
    }
    room.sendMessage(spawn);
    character.setRoom(room);

    // Set the level of the mob
    character.setLevel(1);
    String levelString = get("level");
    if (levelString != null) {
      try {
        character.setLevel(Integer.parseInt(levelString));
      }
      catch (NumberFormatException nfe) {
        Log.error(String.format(
          "Invalid level for mobile %s: %s", id, levelString
        ));
      }
    }

    // Generate gold to be carried by the mobile
    String gold = get("gold");
    if (gold != null) {
      try {
        character.addGold(Dice.roll(gold));
      }
      catch (DiceParseException dpe) {
        Log.error(String.format(
          "Gold string on mobile %s is invalid: %s", id, gold
        ));
      }
    }

    // Set major and minor stats
    String statMajor = get("stat.major");
    if (statMajor != null) {
      character.setMajorStat(statMajor);
    }

    String statMinor = get("stat.minor");
    if (statMinor != null) {
      character.setMinorStat(statMinor);
    }

    // Generate stats based on level and power
    String powerLevel = get("power");
    int power = 25;
    try {
      if (powerLevel != null) {
        power = Integer.parseInt(powerLevel);
      }
    }
    catch (NumberFormatException nfe) {
      Log.error(String.format(
        "Invalid power for mobile %s: %s", id, levelString
      ));
    }

    // TODO Need to take power into consideration here. Not sure if we are
    // going to keep player characters and mobiles so tightly bounded by the
    // solace.game.Character class.
    //character.generateStats(power);

    room.getMobiles().add(this);
    room.getCharacters().add(character);
  }

  /**
   * Cancels the wander event on this mobile if applicable.
   */
  public void cancelWanderEvent() {
    if (wanderEvent != null) {
      wanderEvent.cancel();
    }
  }

  /**
   * Removes the mobile from the game world.
   */
  public void pluck() {
    if (!isPlaced) { return; }
    if (wanderEvent != null) {
      wanderEvent.cancel();
    }
    Room room = character.getRoom();
    room.getCharacters().remove(character);
    room.getMobiles().remove(this);
    character.setRoom(null);
    isPlaced = false;
  }
}
