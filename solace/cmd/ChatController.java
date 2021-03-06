package solace.cmd;

import solace.game.*;
import solace.io.Messages;
import solace.net.Connection;

/**
 * Out of game chat controller. The out of game chat allows people to chat on
 * the server without having to actually be logged into the game and playing
 * (useful for discussing strategy, talking with friends, asking questions, and
 * general banter when not actually playing).
 * @author Ryan Sandor Richards (Gaius)
 */
public class ChatController implements Controller {
  private Connection connection;

  ChatController(Connection c) {
    connection = c;
    Game.addChatConnection(connection);
    connection.sendln(Messages.get("ChatIntro"));
  }

  /**
   * @see solace.cmd.Controller
   */
  public String getPrompt() {
    return "{c}chat>{x} ";
  }

  /**
   * Parses chat commands and broadcasts messages.
   * @param message Messages or command to parse.
   */
  public void parse(String message) {
    if (message == null || message.length() == 0) {
      return;
    }
    if (message.toLowerCase().startsWith("/quit")) {
      connection.sendln("Later!");
      Game.removeChatconnection(connection);
      connection.setStateController(new MainMenuController(connection));
    } else if (message.toLowerCase().startsWith("/help")) {
      String help = Messages.get("ChatHelp");
      connection.sendln(help);
    } else {
      String name = connection.getAccount().getName().toLowerCase();
      String format = "{y}" + name + ": {x}" + message;
      for (Object chatter : Game.getChatConnections()) {
        Connection c = (Connection) chatter;
        c.sendln(format);
      }
    }
  }
}
