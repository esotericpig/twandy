/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Jonathan Bradley Whited
 */

package tv.twitch.tandycakes.crim;

import tv.twitch.tandycakes.Fansi;
import tv.twitch.tandycakes.Formatter;
import tv.twitch.tandycakes.error.CrimException;

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
  // TODO: command/option groups?
  // TODO: implement "--" to stop parsing rest of args to allow "--" in arg's text
  // TODO: implement way to capture all remaining args for a command (for help command)
  // TODO: multi-arg for option "-f file1 -f file2"
  // FIXME: "twandy play --help" should work, not ask for "<arg>"

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
    this.root = new Command(null,appName,summary,this::runRoot);
    this.globalOptions = new Command(this.root,"globalopts",null,null); // Don't use addCommand().

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
    return globalOptions.addOption("--help","-h","Show this help",this::runHelp);
  }

  public Option addVersionOption() {
    return root.addOption("--version","-v","Show version",this::runVersion);
  }

  public Command addHelpCommand() {
    // TODO: implement; doesn't currently work because need all rest of args

    //return root.addCommand("help","Help about a command",this::runHelp);
    return null;
  }

  public void runRoot(Crim crim,Command cmd,Map<String,String> opts,Map<String,String> args) {
    crim.showHelp();
  }

  public void runHelp(Crim crim,Command cmd,Map<String,String> opts,Map<String,String> args) {
    crim.showHelp(cmd);
  }

  public void runVersion(Crim crim,Command cmd,Map<String,String> opts,Map<String,String> args) {
    crim.showVersion();
  }

  public void parse(String... args) {
    Command command = root;
    final Map<String,String> cmdOpts = new LinkedHashMap<>();
    final Map<String,String> cmdArgs = new LinkedHashMap<>();
    Option optionToRun = null;

    for(int i = 0; i < args.length; ++i) {
      String arg = args[i];

      String[] parts = Option.split(arg);
      String optName = parts[0];
      String optArg = (parts.length == 2) ? parts[1] : null;

      Option opt = null;
      Command subcmd = null;

      // Local opts should supersede global opts.
      if((opt = command.optionTrie.find(optName)) != null) {
        if(opt.argName == null) {
          if(optArg != null) {
            throw new CrimException(Formatter.format(
                "{} '{}' does not accept args: '{}'"
                ,buildExceptionCommandOptionText(command)
                ,optName,optArg));
          }
        }
        else {
          if(optArg == null) {
            if((++i) >= args.length) {
              throw new CrimException(Formatter.format(
                  "{} '{}' requires an arg."
                  ,buildExceptionCommandOptionText(command)
                  ,optName));
            }

            optArg = args[i];
          }

          cmdOpts.put(opt.argName,optArg);
        }

        if(opt.runner != null) {
          if(optionToRun != null) {
            throw new CrimException(Formatter.format(
                "{} '{}' conflicts with option '{}' runner."
                ,buildExceptionCommandOptionText(command)
                ,optName
                ,optionToRun.name));
          }

          optionToRun = opt;
        }
      }
      else if((opt = globalOptions.optionTrie.find(optName)) != null) {
        if(opt.argName == null) {
          if(optArg != null) {
            throw new CrimException(Formatter.format(
                "Global option '{}' does not accept args: '{}'"
                ,optName,optArg));
          }
        }
        else {
          if(optArg == null) {
            if((++i) >= args.length) {
              throw new CrimException(Formatter.format(
                  "Global option '{}' requires an arg."
                  ,optName));
            }

            optArg = args[i];
          }

          cmdOpts.put(opt.argName,optArg);
        }

        if(opt.runner != null) {
          if(optionToRun != null) {
            throw new CrimException(Formatter.format(
                "Global option '{}' conflicts with option '{}' runner."
                ,optName
                ,optionToRun.name));
          }

          optionToRun = opt;
        }
      }
      else if((subcmd = command.commandTrie.find(arg)) != null) {
        // If there is an option runner, ignore args.
        // For example, "--help subcmd1 subcmd2".
        if(optionToRun == null && subcmd.argNames != null && subcmd.argNames.length > 0) {
          if((i + subcmd.argNames.length) >= args.length) {
            throw new CrimException(Formatter.format(
                "{} '{}' requires {} arg{}."
                ,buildExceptionSubcommandText(command)
                ,arg,subcmd.argNames.length
                ,(subcmd.argNames.length == 1) ? "" : 's'));
          }

          for(String argName: subcmd.argNames) {
            ++i;
            cmdArgs.put(argName,args[i]);
          }
        }

        command = subcmd;
      }
      else {
        if(command.isRoot()) {
          throw new CrimException(Formatter.format(
              "Invalid command/option: '{}'"
              ,arg));
        }
        else {
          throw new CrimException(Formatter.format(
              "For command '{}', invalid command/option: '{}'"
              ,command.buildFullName(root)
              ,arg));
        }
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
      if(command.runner != null) {
        runner = command.runner;
      }
      else {
        runner = root.runner;
      }
    }

    // The runner might not be set, as the user is just setting the
    // initial skeleton for testing.
    if(runner != null) {
      runner.run(this,command,cmdOpts,cmdArgs);
    }
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
    fansi.srintln("  {cmd {} } {arg <options> <commands> }",command.buildFullName());

    if(command.aliases != null && command.aliases.length > 0) {
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

    for(Command sub: command.commands.values()) {
      StringBuilder buffer = new StringBuilder();
      int len = sub.name.length();

      buffer.append(fansi.style("{cmd {} }",sub.name));

      if(sub.argNames != null && sub.argNames.length > 0) {
        for(String argName: sub.argNames) {
          buffer.append(' ').append(fansi.style("{arg {} }",argName));
          len += 1 + argName.length();
        }
      }
      if(sub.aliases != null && sub.aliases.length > 0) {
        buffer.append(';');
        ++len;

        for(String alias: sub.aliases) {
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
