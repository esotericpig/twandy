/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Jonathan Bradley Whited
 */

package tv.twitch.tandycakes;

import com.esotericpig.jeso.botbuddy.BotBuddy;
import tv.twitch.tandycakes.crim.Command;
import tv.twitch.tandycakes.crim.Crim;
import tv.twitch.tandycakes.error.CrimException;

import java.awt.AWTException;
import java.awt.Point;
import java.util.Map;

/**
 * @author Jonathan Bradley Whited
 * @since 1.0.0
 */
public class Twandy extends Crim {
  // TODO: twitch channel where can decide what youtube video to play
  // TODO: play "hello <username>" audio text-to-speech when someone enters (after 5min)

  public static void main(String[] args) {
    Twandy twandy = new Twandy();

    try {
      twandy.parse(args);
    }
    catch(CrimException e) {
      // Concat to empty string, in case null.
      twandy.showHelp("" + e.getLocalizedMessage());
    }
  }

  private final LinkedTrie<String> gameArgValues = new LinkedTrie<>();

  public Twandy() {
    super("twandy","0.3.0");

    this.gameArgValues.add("solarus");
    this.gameArgValues.add("lichess");

    root.addAbout("{bold/white Tandy, have you had your cake today? }");

    root.addCommand("x","Show coords of cursor",this::showCoords,"coords");

    Command playCmd = root.addCommand("play <game>",this::playGame);
    playCmd.addSummary("Play a game:");
    playCmd.addSummary("- solarus");
    playCmd.addSummary("- lichess");

    root.addCommand("fhat","Run filtered chat",this::runFhat);

    addBasics();
  }

  public void showCoords(Crim crim,Command cmd,Map<String,String> opts,Map<String,String> args) {
    fansi.srintln("{bold/yellow");
    fansi.println("> Press enter to continue.");
    fansi.println("> Enter in any char to exit.");
    fansi.srintln("}");

    for(int i = 1; ; ++i) {
      Point coords = BotBuddy.getCoords();

      fansi.srintf("#{bold/btWhite %d }: ( x: {bold/btBlue %4d }, y: {bold/btBlue %4d } )"
          ,i,coords.x,coords.y);
      String input = stdin.nextLine();

      if(!input.isEmpty()) {
        break;
      }
    }

    fansi.println();
  }

  public void playGame(Crim crim,Command cmd,Map<String,String> opts,Map<String,String> args) {
    String gameArg = args.get("<game>");

    if(gameArg == null || gameArg.isEmpty()) {
      throw new CrimException("No <game> arg.");
    }

    String game = gameArgValues.find(gameArg,"");

    switch(game) {
      case "solarus" -> playSolarus();
      case "lichess" -> playLichess();

      default -> throw new CrimException(Formatter.format(
          "Invalid <game> arg: '{}'",gameArg));
    }
  }

  public void playSolarus() {
  }

  public void playLichess() {
  }

  public void runFhat(Crim crim,Command cmd,Map<String,String> opts,Map<String,String> args) {
  }
}

/*

// ctor grabs user/pass from env vars or sys props using user/pass as keys?
Game game = new Game(channel, user, pass);

game.setAutoDelay(200); // 200ms
game.setMessageFloodingDelay(3000); // 3s
game.setPrefixRegex("[,.!]"); // "^\s*(#{prefix})(.+)" => grab \2 => split("\s+")

game.chatCommands.add("up");
// ...

game.onChatMessage((game,commands) {
  for(String cmd: commands) {
    switch(cmd) {
      case "up":
        game.botbuddy.type(...);
    }
  }
});

 */

// TODO: Fhat class

// TODO: parseChatMessage
// TODO: onChatMessage( functional interface )
// TODO: display of commands in onChatMessage
// TODO: call beginSafeMode() in connect/run/start/whatever method
class Game implements AutoCloseable {
  public final Fansi fansi;
  public final BotBuddy bot;
  public final LinkedTrie<String> chatCommands = new LinkedTrie<>();

  public Game(Fansi fansi) throws AWTException {
    this.fansi = fansi;
    this.bot = BotBuddy.builder()
        .autoDelay(true)
        .autoWaitForIdle(true)
        .releaseMode(true)
        .build();
  }

  public void play() {
    // TODO: test with just while-loop and stdin
  }

  @Override
  public void close() {
    bot.endSafeMode();
    bot.close();
  }

  public void setAutoDelay(int autoDelay) {
    bot.setAutoDelay(autoDelay);
  }
}
