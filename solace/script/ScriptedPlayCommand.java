package solace.script;
import solace.game.Player;
import java.util.function.BiPredicate;
import solace.cmd.play.AbstractPlayCommand;
import solace.cmd.play.PlayCommand;
import solace.cmd.StateCommand;

/**
 * Data model for scripted gameplay commands (`PlayStateCommand`). Gameplay commands
 * handle basic actions such as getting items, movement, etc.
 * @author Ryan Sandor Richards
 */
public class ScriptedPlayCommand extends AbstractScriptedCommand {
  /**
   * Creates a new play command with the given names and run lamdba.
   * @param name Name of the command.
   * @param displayName The display name for the command.
   * @param runLambda Run lambda for the command.
   */
  public ScriptedPlayCommand(
    String name,
    String displayName,
    BiPredicate<Player, String[]> runLambda
  ) {
    super(name, displayName, runLambda);
  }

  /**
   * Creates an instance of the play command for use by the game engine.
   * @param ch Character for the play command.
   * @return The play command instance.
   */
  public StateCommand getInstance(Player p) {
    return null;
    /*
    PlayCommand command = new AbstractPlayCommand(getName(), getDisplayName()) {
      public void run(Player p, String[] params) {
        getRunLambda().test(p, params);
      }
    };
    return command;
    */
  }
}
