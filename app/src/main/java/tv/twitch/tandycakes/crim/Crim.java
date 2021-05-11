/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Jonathan Bradley Whited
 */

package tv.twitch.tandycakes.crim;

import tv.twitch.tandycakes.Fansi;
import tv.twitch.tandycakes.Formatter;
import tv.twitch.tandycakes.error.CrimException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
  // TODO: parse any opt/arg/cmd prefixed with "-" or "--" as an opt?
  // TODO: implement "--" to stop parsing rest of args to allow "--" in arg's text
  // TODO: implement "-aBcD" combined short opts?
  // TODO: add "--no-opt" automatically for boolean "--opt" if request it?
  // TODO: add fuzzy suggestions for invalid commands/opts?
  // TODO: set required/optional opts/args?
  // TODO: change to use functional DSL, like Ruby?
  // TODO: multi-args for options "-f file1 -f file2"

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

  public void addBasics() {
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
    Command cmdToShow = root;

    for(String multiArg: data.multiArgs()) {
      Command subcmd = cmdToShow.commandTrie.find(multiArg);

      if(subcmd != null) {
        cmdToShow = subcmd;
      }
    }

    crim.showHelp(cmdToShow);
  }

  public void parse(String... args) {
    boolean hasOptRunner = parse(true,false,args);

    parse(false,hasOptRunner,args);
  }

  /**
   * <pre>
   * Not polished, just a monster method for initial idea.
   * </pre>
   */
  public boolean parse(boolean isForOptionRunner,boolean hasOptionRunner,String... args) {
    Command cmd = root;

    final Map<String,String> globalOpts = new LinkedHashMap<>();
    final Map<String,String> cmdOpts = new LinkedHashMap<>();
    final Map<String,String> cmdArgs = new LinkedHashMap<>();
    final Deque<String> cmdMultiArgs = new ArrayDeque<>();

    Option optionToRun = null;
    String[] subcmdArgNames = null;
    int subcmdArgNameIndex = 0;
    boolean eatAllSubcmdArgs = false;

    for(int i = 0; i < args.length; ++i) {
      String arg = args[i];
      String[] parts = Option.split(arg);

      String optName = parts[0];
      String optArg = (parts.length == 2) ? parts[1] : null;

      Option opt = null;
      Command subcmd = null;

      // Local opts should supersede global opts.
      if((opt = cmd.optionTrie.find(optName)) != null) {
        if(opt.argName == null) {
          if(optArg != null) {
            throw new CrimException(Formatter.format(
                "{} '{}' does not accept args: '{}'."
                ,buildExceptionCommandOptionText(cmd)
                ,optName,optArg));
          }

          cmdOpts.put(opt.name,"");
        }
        else {
          if(optArg == null) {
            if((++i) >= args.length) {
              throw new CrimException(Formatter.format(
                  "{} '{}' requires an arg."
                  ,buildExceptionCommandOptionText(cmd)
                  ,optName));
            }

            optArg = args[i];
          }

          cmdOpts.put(opt.name,optArg);
        }

        if(opt.runner != null) {
          if(isForOptionRunner) {
            return true;
          }
          if(optionToRun != null) {
            throw new CrimException(Formatter.format(
                "{} '{}' conflicts with option '{}' runner."
                ,buildExceptionCommandOptionText(cmd)
                ,optName,optionToRun.name));
          }

          optionToRun = opt;
        }
      }
      else if((opt = globalOptions.optionTrie.find(optName)) != null) {
        if(opt.argName == null) {
          if(optArg != null) {
            throw new CrimException(Formatter.format(
                "Global option '{}' does not accept args: '{}'."
                ,optName,optArg));
          }

          globalOpts.put(opt.name,"");
        }
        else {
          if(optArg == null) {
            if((++i) >= args.length) {
              throw new CrimException(Formatter.format(
                  "Global option '{}' requires an arg.",optName));
            }

            optArg = args[i];
          }

          globalOpts.put(opt.name,optArg);
        }

        if(opt.runner != null) {
          if(isForOptionRunner) {
            return true;
          }
          if(optionToRun != null) {
            throw new CrimException(Formatter.format(
                "Global option '{}' conflicts with option '{}' runner."
                ,optName,optionToRun.name));
          }

          optionToRun = opt;
        }
      }
      else {
        if(isForOptionRunner || hasOptionRunner || optionToRun != null) {
          // Don't consume subcommand args if there's an option runner
          //   because of "cmd1 cmd2 --help" with "cmd1 <args...>".
          if((subcmd = cmd.commandTrie.find(arg)) != null) {
            cmd = subcmd;
          }
        }
        else {
          if(eatAllSubcmdArgs) {
            cmdMultiArgs.add(arg);
          }
          else if(subcmdArgNames != null) {
            cmdArgs.put(subcmdArgNames[subcmdArgNameIndex],arg);

            if((++subcmdArgNameIndex) >= subcmdArgNames.length) {
              // Reset.
              subcmdArgNames = null;
              subcmdArgNameIndex = 0;
            }
          }
          else if((subcmd = cmd.commandTrie.find(arg)) != null) {
            if(subcmd.isMultiArg) {
              eatAllSubcmdArgs = true;
            }
            else if(subcmd.hasArgs()) {
              if((i + subcmd.argNames.length) >= args.length) {
                throw new CrimException(Formatter.format(
                    "{} '{}' requires {} arg{}."
                    ,buildExceptionSubcommandText(cmd)
                    ,arg,subcmd.argNames.length
                    ,(subcmd.argNames.length == 1) ? "" : 's'));
              }

              subcmdArgNames = subcmd.argNames;
            }

            cmd = subcmd;
          }
          else {
            if(cmd.isRoot()) {
              throw new CrimException(Formatter.format(
                  "Invalid command/option: '{}'.",arg));
            }
            else {
              throw new CrimException(Formatter.format(
                  "For command '{}', invalid command/option: '{}'."
                  ,cmd.buildFullName(root),arg));
            }
          }
        }
      }
    }

    if(isForOptionRunner) {
      return optionToRun != null;
    }

    // Check if have the required number of args, if there was no option runner.
    if(optionToRun == null && subcmdArgNames != null
        && subcmdArgNameIndex < subcmdArgNames.length) {
      if(cmd.isRoot()) {
        throw new CrimException(Formatter.format(
            "Missing {} required arg{}."
            ,subcmdArgNames.length
            ,(subcmdArgNames.length == 1) ? "" : 's'));
      }
      else {
        throw new CrimException(Formatter.format(
            "Command '{}' requires {} arg{}."
            ,cmd.buildFullName(root)
            ,subcmdArgNames.length
            ,(subcmdArgNames.length == 1) ? "" : 's'));
      }
    }

    CommandRunner runner = null;

    // Option runners supersede command runners.
    // For example, "--version" should show the version, not run the command.
    // For example, "--help subcmd1 subcmd2" should show the help of subcmd2,
    //   not run subcmd2.
    if(optionToRun != null) {
      runner = optionToRun.runner;
    }
    else {
      if(cmd.runner != null) {
        runner = cmd.runner;
      }
      else {
        runner = root.runner;
      }
    }

    // The runner might not be set, as the user is just setting the
    // initial skeleton for testing.
    if(runner != null) {
      CommandData data = new CommandData(globalOpts,cmdOpts,cmdArgs,cmdMultiArgs);

      runner.run(this,cmd,data);
    }

    return optionToRun != null;
  }

  public void parseText(String text) {
    parse(text.split("\\s+"));
  }

  public String buildExceptionCommandOptionText(Command command) {
    if(command.isRoot()) {
      return "Option";
    }

    return Formatter.format("Command '{}' option",command.buildFullName(root));
  }

  public String buildExceptionSubcommandText(Command command) {
    if(command.isRoot()) {
      return "Command";
    }

    return Formatter.format("Command '{}' subcommand",command.buildFullName(root));
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
    fansi.srint("  {cmd {} } {arg [<options>] }",command.buildFullName());

    if(command.argNames != null) {
      for(String argName: command.argNames) {
        fansi.srint(" {arg {} }",argName);
      }
    }

    fansi.srintln(" {arg [<commands>] }");

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

      if(sub.summary.isEmpty()) {
        fansi.println();
      }
      else {
        final Iterator<String> it = sub.summary.iterator();

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
