/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Jonathan Bradley Whited
 */

package tv.twitch.tandycakes.crim;

import tv.twitch.tandycakes.Fansi;

import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 * <pre>
 * This isn't perfect, has some problems, but it's a good experiment for a
 * potential, future library.
 * </pre>
 *
 * @author Jonathan Bradley Whited
 * @since 1.0.0
 */
public class Crim {
  // TODO: command/option groups? for visual use only
  // TODO: add "--no-opt" automatically for boolean "--opt" if request it?
  // TODO: change to use functional DSL, like Ruby?

  public final Scanner stdin = new Scanner(System.in);
  public final Fansi fansi = new Fansi();

  public final String appName;
  public final String appVersion;
  public final Command root;
  public final Command globalOptions;

  public Crim(String appName,String appVersion) {
    this(appName,appVersion,null);
  }

  public Crim(String appName,String appVersion,String summary) {
    this.appName = appName;
    this.appVersion = appVersion;
    this.root = Command.builder()
        .name(appName).summary(summary).runner(this::runRootCommand).build();
    // Do NOT use this/root.addCommand().
    this.globalOptions = Command.builder()
        .parent(this.root).name("globalopts").build();

    // Add our custom styles.
    this.fansi.storeStyleAlias("title","bold/red");
    this.fansi.storeStyleAlias("cmd","bold/green");
    this.fansi.storeStyleAlias("arg","bold/white");
    this.fansi.storeStyleAlias("opt","bold/yellow");
    this.fansi.storeStyleAlias("err","bold/btRed");
  }

  public void addDefaults() {
    addHelpGlobalOption();
    addVersionOption();
    addHelpCommand();
  }

  public Option addHelpGlobalOption() {
    return globalOptions.addOption(Option.builder()
        .name("--help").alias("-h")
        .summary("Show this help.")
        .runner(this::runHelpGlobalOption));
  }

  public Option addVersionOption() {
    return root.addOption(Option.builder()
        .name("--version").alias("-v")
        .summary("Show version.")
        .runner(this::runVersionOption));
  }

  public Command addHelpCommand() {
    return root.addCommand(Command.builder()
        .name("help")
        .multiArg(true)
        .summary("Help with a command.")
        .runner(this::runHelpCommand));
  }

  public void runRootCommand(Crim crim,Command cmd,CommandData data) {
    crim.showHelp();
  }

  public void runHelpGlobalOption(Crim crim,Command cmd,CommandData data) {
    crim.showHelp(cmd);
  }

  public void runVersionOption(Crim crim,Command cmd,CommandData data) {
    crim.showVersion();
  }

  public void runHelpCommand(Crim crim,Command cmd,CommandData data) {
    Command cmdToShow = crim.root;

    for(String multiArg: data.multiArgs()) {
      Command subcmd = cmdToShow.commandTrie.find(multiArg);

      if(subcmd != null) {
        cmdToShow = subcmd;
      }
    }

    crim.showHelp(cmdToShow);
  }

  public void parse(String... mainArgs) {
    CrimParser parser = new CrimParser(root,globalOptions,mainArgs);
    CommandRunner runner = parser.parse();

    // The runner might not be set, as the user is just setting the
    //   initial skeleton for testing.
    if(runner != null) {
      runner.run(this,parser.command,parser.commandData);
    }
  }

  public void showHelp() {
    showHelp(root);
  }

  public void showHelp(String errorMessage) {
    showHelp(root,errorMessage);
  }

  public void showHelp(Command command) {
    showHelp(command,null);
  }

  public void showHelp(Command command,String errorMessage) {
    showCommand(command);

    if(errorMessage != null) {
      fansi.srintln();
      fansi.srintln("{err ERROR }: {}",errorMessage);
    }
  }

  public void showCommand(Command command) {
    showCommandUsage(command);
    showCommandAbout(command,true);
    showCommandSubcommands(command,true);
    showCommandOptions(command,true);
    showGlobalOptions(true);
  }

  public void showCommandUsage(Command command) {
    fansi.srintln("{title USAGE }");
    fansi.srint("  {cmd {} } {arg [options] }",command.buildFullName());

    if(command.argNames != null) {
      for(String argName: command.argNames) {
        fansi.srint(" {arg {} }",argName);
      }
    }

    fansi.srintln(" {arg [commands] }");

    if(command.hasAliases()) {
      fansi.println();
      fansi.srintln("  aliases: {cmd {} }",String.join(" ",command.aliases));
    }
  }

  public boolean showCommandAbout(Command command,boolean printNewline) {
    List<String> about = command.about;

    if(about.isEmpty()) {
      about = command.summary; // Try summary instead.
    }
    if(about.isEmpty()) {
      return printNewline;
    }

    if(printNewline) {
      fansi.println();
    }

    fansi.srintln("{title ABOUT }");

    for(String line: about) {
      // Use srintln() so can optionally add style to about lines.
      fansi.srintln("  " + line);
    }

    return true;
  }

  public boolean showCommandSubcommands(Command command,boolean printNewline) {
    if(command.commands.isEmpty()) {
      return printNewline;
    }

    if(printNewline) {
      fansi.println();
    }
    if(command.isRoot()) {
      fansi.srintln("{title COMMANDS }");
    }
    else {
      fansi.srintln("{title SUBCOMMANDS }");
    }

    final String[] cmds = new String[command.commands.size()];
    final int[] lens = new int[command.commands.size()];
    int maxLen = 0;
    int index = 0;

    for(Command subcmd: command.commands.values()) {
      StringBuilder buffer = new StringBuilder();
      int len = subcmd.name.length();

      buffer.append(fansi.style("{cmd {} }",subcmd.name));

      if(subcmd.hasArgs()) {
        for(String argName: subcmd.argNames) {
          buffer.append(' ').append(fansi.style("{arg {} }",argName));
          len += 1 + argName.length();
        }
      }
      if(subcmd.hasAliases()) {
        buffer.append(';');
        ++len;

        for(String alias: subcmd.aliases) {
          buffer.append(' ').append(fansi.style("{cmd {} }",alias));
          len += 1 + alias.length();
        }
      }

      cmds[index] = buffer.toString();
      lens[index] = len;
      ++index;

      if(len > maxLen) {
        maxLen = len;
      }
    }

    maxLen += 4; // Indent summary; '%s' also can't be 0.
    index = 0;

    for(Command sub: command.commands.values()) {
      fansi.print("  "); // Indent.
      fansi.print(cmds[index]);

      List<String> summary = sub.summary;

      if(summary.isEmpty()) {
        summary = sub.about; // Try about instead.
      }

      if(summary.isEmpty()) {
        fansi.println();
      }
      else {
        final Iterator<String> it = summary.iterator();

        for(int len = (maxLen - lens[index]); len > 0; --len) {
          fansi.print(" ");
        }

        // Use srintln() so can optionally add style to summary lines.
        fansi.srintln(it.next());

        while(it.hasNext()) {
          fansi.print("  "); // Indent.

          for(int i = 0; i < maxLen; ++i) {
            fansi.print(" ");
          }

          // Use srintln() so can optionally add style to summary lines.
          fansi.srintln(it.next());
        }
      }

      ++index;
    }

    return true;
  }

  public boolean showCommandOptions(Command command,boolean printNewline) {
    if(command.options.isEmpty()) {
      return printNewline;
    }

    if(printNewline) {
      fansi.println();
    }
    if(command == globalOptions) {
      fansi.srintln("{title GLOBAL OPTIONS }");
    }
    else {
      fansi.srintln("{title OPTIONS }");
    }

    final String[] opts = new String[command.options.size()];
    final int[] lens = new int[command.options.size()];
    int maxLen = 0;
    int index = 0;

    for(Option opt: command.options.values()) {
      StringBuilder buffer = new StringBuilder();
      int len = opt.name.length();

      if(opt.alias != null) {
        buffer.append(fansi.style("{opt {} }",opt.alias)).append(' ');
        len += opt.alias.length() + 1;
      }

      buffer.append(fansi.style("{opt {} }",opt.name));

      if(opt.argName != null) {
        buffer.append('=').append(fansi.style("{arg {} }",opt.argName));
        len += 1 + opt.argName.length();
      }

      opts[index] = buffer.toString();
      lens[index] = len;
      ++index;

      if(len > maxLen) {
        maxLen = len;
      }
    }

    maxLen += 4; // Indent summary; '%s' also can't be 0.
    index = 0;

    for(Option opt: command.options.values()) {
      fansi.print("  "); // Indent.
      fansi.print(opts[index]);

      if(opt.summary.isEmpty()) {
        fansi.println();
      }
      else {
        final Iterator<String> it = opt.summary.iterator();

        for(int len = (maxLen - lens[index]); len > 0; --len) {
          fansi.print(" ");
        }

        // Use srintln() so can optionally add style to summary lines.
        fansi.srintln(it.next());

        while(it.hasNext()) {
          fansi.print("  "); // Indent.

          for(int i = 0; i < maxLen; ++i) {
            fansi.print(" ");
          }

          // Use srintln() so can optionally add style to summary lines.
          fansi.srintln(it.next());
        }
      }

      ++index;
    }

    return true;
  }

  public boolean showGlobalOptions(boolean printNewline) {
    return showCommandOptions(globalOptions,printNewline);
  }

  public void showVersion() {
    fansi.srintln("{cmd {} } {arg v{} }",appName,appVersion);
  }
}
