/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Jonathan Bradley Whited
 */

package tv.twitch.tandycakes;

import com.esotericpig.jeso.botbuddy.BotBuddy;
import tv.twitch.tandycakes.crim.Command;
import tv.twitch.tandycakes.crim.CommandData;
import tv.twitch.tandycakes.crim.Crim;
import tv.twitch.tandycakes.crim.Option;
import tv.twitch.tandycakes.error.CrimException;

import java.awt.Point;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

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

  // Java note: Runnable is also a functional interface.
  private final Map<String,Consumer<CommandData>> gameNames = MapMaker.make(
      new LinkedHashMap<>(),(map) -> {
        // These must be lower-cased without spaces.
        map.put("solarus",this::playSolarus);
        map.put("lichess",this::playLichess);
      });
  private final LinkedTrie<String> gameNameTrie = new LinkedTrie<>();

  public Twandy() {
    super("twandy","0.3.0");

    root.addAbout("{bold/white Tandy, have you had your cake today? }");

    root.addCommand(Command.builder()
        .name("x").alias("coords")
        .summary("Show coords of cursor.")
        .runner(this::showCoords));

    Command playCmd = root.addCommand(Command.builder()
        .name("play <game>")
        .runner(this::playGame));

    playCmd.addSummary("Play a game:");

    for(String gameName: gameNames.keySet()) {
      playCmd.addSummary("- " + gameName);
      gameNameTrie.add(gameName);
    }

    playCmd.addOption(Option.builder()
        .name("--fhat").alias("-f")
        .summary("Run filtered chat."));

    addDefaults();
  }

  public void showCoords(Crim crim,Command cmd,CommandData data) {
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

  public void playGame(Crim crim,Command cmd,CommandData data) {
    String gameArg = data.args().get("<game>");

    if(gameArg == null || (gameArg = gameArg.strip()).isEmpty()) {
      throw new CrimException("No <game> arg.");
    }

    gameArg = gameArg.toLowerCase(Locale.ROOT);

    String game = gameNameTrie.find(gameArg,"");
    Consumer<CommandData> runner = gameNames.get(game);

    if(runner == null) {
      throw new CrimException(Formatter.format("Invalid <game> arg: '{}'.",gameArg));
    }

    runner.accept(data);
  }

  public void playSolarus(CommandData data) {
    System.out.println("Playing solarus...");
  }

  public void playLichess(CommandData data) {
    System.out.println("Playing lichess...");
  }
}

// TODO: so I'm thinking GameData, Game(data), Fhat(data)

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

// TODO: separate Fhat and Game?

/*
// TODO: parseChatMessage
// TODO: onChatMessage( functional interface )
// TODO: display of commands in onChatMessage
// TODO: call beginSafeMode() in connect/run/start/whatever method
class Game implements AutoCloseable {
  public final Fansi fansi;
  public final LinkedTrie<String> chatCommands = new LinkedTrie<>();

  public Game(Fansi fansi) throws AWTException {
    this.fansi = fansi;
  }

  public void play() {
    // TODO: test with just while-loop and stdin

    BotBuddy bot = BotBuddy.builder()
        .autoDelay(true)
        .autoWaitForIdle(true)
        .releaseMode(true)
        .build();
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
*/
