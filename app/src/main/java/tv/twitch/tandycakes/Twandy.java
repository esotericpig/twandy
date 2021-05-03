/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Jonathan Bradley Whited
 */

package tv.twitch.tandycakes;

import com.esotericpig.jeso.botbuddy.BotBuddy;
import tv.twitch.tandycakes.error.CliException;

import java.awt.Point;
import java.util.Locale;
import java.util.Scanner;

/**
 * @author Jonathan Bradley Whited
 * @since 1.0.0
 */
public class Twandy {
  public static void main(String[] args) {
    Twandy twandy = new Twandy(args);

    try {
      twandy.run();
    }
    catch(CliException e) {
      twandy.showHelp("{err ERROR }: " + e.getLocalizedMessage());
    }
  }

  public static final String APP_NAME = "twandy";
  public static final String APP_VERSION = "0.1.0";

  private final String[] args;
  private final Scanner stdin = new Scanner(System.in);
  private final Fansi fansi = new Fansi();
  private final CommandTrie commandTrie = new CommandTrie();
  private final CommandTrie gameArgTrie = new CommandTrie();

  public Twandy(String[] args) {
    this.args = args;

    // Add our custom styles.
    this.fansi.storeStyleAlias("title","bold/red");
    this.fansi.storeStyleAlias("usg","bold/blue");
    this.fansi.storeStyleAlias("arg","bold/white");
    this.fansi.storeStyleAlias("opt","bold/yellow");
    this.fansi.storeStyleAlias("err","bold/btRed");

    this.commandTrie.addCommand("x");
    this.commandTrie.addCommand("play");
    this.commandTrie.addCommand("fhat");

    // Options, not commands, but that's okay.
    this.commandTrie.addCommandAndAlias("--help","-h");
    this.commandTrie.addCommandAndAlias("--version","-v");

    this.gameArgTrie.addCommand("solarus");
    this.gameArgTrie.addCommand("lichess");
  }

  public void run() throws CliException {
    String commandArg = parseCommand(0);

    if(commandArg == null || commandArg.isEmpty()) {
      showHelp();
      return;
    }

    String command = commandTrie.findCommand(commandArg,"");

    switch(command) {
      case "x" -> showCoords();
      case "play" -> playGame();
      case "fhat" -> runFhat();

      case "--help" -> showHelp();
      case "--version" -> showVersion();

      default -> throw new CliException(Formatter.format("Invalid command/option: '{}'",commandArg));
    }
  }

  public String parseCommand(int argIndex) {
    if(argIndex < 0) {
      argIndex = args.length + argIndex;
    }
    if(argIndex < 0 || argIndex >= args.length) {
      return null;
    }

    return args[argIndex].trim().toLowerCase(Locale.ROOT);
  }

  public void printCmdOrOpt(String detail) {
    printCmdOrOpt("","",detail);
  }

  public void printCmdOrOpt(String cmdOrOpt,String detail) {
    printCmdOrOpt(cmdOrOpt,"",detail);
  }

  public void printCmdOrOpt(String cmdOrOpt,String arg,String detail) {
    // Have to do it this way because style takes up space internally,
    //   but not actually on the screen.
    // Also, if we were to do it the other way using the styled string,
    //   then we have to use a big number like "%-35s", which would be
    //   bad if fansi is disabled.

    int length = 15 - cmdOrOpt.length();

    if(length < 0) {
      length = 0;
    }

    fansi.srintln("  {opt {} } {arg {} }    {}"
        ,cmdOrOpt,String.format("%-" + length + "s",arg),detail);
  }

  public void showHelp() {
    showHelp(null);
  }

  public void showHelp(String error) {
    fansi.srintln("{title USAGE }");
    fansi.srintln("  {usg {} } {arg <options> <commands> }",APP_NAME);
    fansi.srintln();
    fansi.srintln("{title ABOUT }");
    fansi.srintln("  {bold/white Tandy, have you had your cake today? }");
    fansi.srintln();
    fansi.srintln("{title COMMANDS }");
    printCmdOrOpt("x","show coords of cursor");
    printCmdOrOpt("play","<game>","play a game:");
    printCmdOrOpt("- solarus");
    printCmdOrOpt("- lichess");
    printCmdOrOpt("fhat","show filtered chat");
    fansi.srintln();
    fansi.srintln("{title OPTIONS }");
    printCmdOrOpt("-h --help","show help (this)");
    printCmdOrOpt("-v --version","show version");

    if(error != null) {
      fansi.srintln();
      fansi.srintln(error);
    }
  }

  public void showVersion() {
    fansi.srintln("{usg {} } {arg v{} }",APP_NAME,APP_VERSION);
  }

  public void showCoords() {
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

    fansi.srintln();
  }

  public void playGame() {
    String gameArg = parseCommand(1);

    if(gameArg == null || gameArg.isEmpty()) {
      throw new CliException("No <game> arg.");
    }

    String game = gameArgTrie.findCommand(gameArg,"");

    switch(game) {
      case "solarus" -> playSolarus();
      case "lichess" -> playLichess();

      default -> throw new CliException(Formatter.format("Invalid <game> arg: '{}'",gameArg));
    }
  }

  public void playSolarus() {
  }

  public void playLichess() {
  }

  public void runFhat() {
  }
}
