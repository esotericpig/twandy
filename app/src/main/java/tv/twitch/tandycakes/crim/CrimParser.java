/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Bradley Whited
 */

package tv.twitch.tandycakes.crim;

import tv.twitch.tandycakes.Formatter;
import tv.twitch.tandycakes.error.CrimException;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

/**
 * <pre>
 * For a library, maybe decouple/abstract it even further by just parsing each
 * arg as classes ArgOrCmd and Opt without needing command, globalOptions, etc.,
 * but then it requires O(3n).
 *
 * It's still kind of bad now as O(2n), but the Runner logic kind of forces
 * that at the moment.
 * </pre>
 */
public class CrimParser {
  // TODO: parse any opt/arg/cmd prefixed with "-" or "--" as an opt?
  // TODO: implement "--" to stop parsing rest of args to allow "--" in arg's text
  // TODO: implement "-aBcD" combined short opts?
  // TODO: add fuzzy suggestions for invalid commands/opts?
  // TODO: multi-args for options "-f file1 -f file2"
  // TODO: set required/optional opts/args?

  public final String[] mainArgs;
  public int mainArgsIndex = 0;

  public final Command root;
  public final Command globalOptions;

  public Command command;
  public final CommandData commandData = new CommandData();

  public Option optionToRun = null;
  public String[] commandArgNames;
  public int commandArgNamesIndex = 0;
  public boolean eatAllCommandArgs;

  // Like a stack, because the most recent parent's options should supersede grandparents'.
  public final Deque<Command> parentCommands = new LinkedList<>();

  public CrimParser(Command root,Command globalOptions,String... mainArgs) {
    if(root == null) {
      throw new IllegalArgumentException("Null root command.");
    }
    if(mainArgs == null) {
      throw new IllegalArgumentException("Null main args.");
    }

    this.mainArgs = mainArgs;
    this.root = root;
    this.globalOptions = globalOptions;
  }

  /**
   * <pre>
   * Uses an instance method, instead of a static method, so that this can be
   * converted to interface-style code one day so can set/use different parsers.
   * </pre>
   */
  public CommandRunner parse() {
    CommandRunner runner = parse(ParseFlag.FOR_OPT_RUNNER);

    return parse((runner == null) ? ParseFlag.NONE : ParseFlag.HAS_OPT_RUNNER);
  }

  public CommandRunner parse(ParseFlag flag) {
    // Reset non-final fields.
    command = root;

    optionToRun = null;
    commandArgNames = command.hasArgs() ? command.argNames : null;
    commandArgNamesIndex = 0;
    eatAllCommandArgs = command.isMultiArg;

    for(mainArgsIndex = 0; mainArgsIndex < mainArgs.length; ++mainArgsIndex) {
      String mainArg = mainArgs[mainArgsIndex];
      String[] parts = Option.split(mainArg);

      String optionName = parts[0];
      String optionArg = (parts.length == 2) ? parts[1] : null;

      Option option;

      // First, current command's local options.
      if((option = command.optionTrie.find(optionName)) != null) {
        if(option.runner != null && flag == ParseFlag.FOR_OPT_RUNNER) {
          return option.runner;
        }

        parseOption(commandData.opts(),optionName,optionArg,option);

        continue;
      }

      // Second, global options.
      if((option = globalOptions.optionTrie.find(optionName)) != null) {
        if(option.runner != null && flag == ParseFlag.FOR_OPT_RUNNER) {
          return option.runner;
        }

        parseOption(commandData.globalOpts(),optionName,optionArg,option);

        continue;
      }

      // Third, parent/previous commands' local options.
      for(Command parentCommand: parentCommands) {
        if((option = parentCommand.optionTrie.find(optionName)) != null) {
          if(option.runner != null && flag == ParseFlag.FOR_OPT_RUNNER) {
            return option.runner;
          }

          parseOption(commandData.opts(),optionName,optionArg,option);

          break;
        }
      }

      // Found a parent/previous command's local option from the loop?
      if(option != null) {
        continue;
      }

      // Lastly, args/subcommands.
      Command sub;

      if(flag == ParseFlag.FOR_OPT_RUNNER || flag == ParseFlag.HAS_OPT_RUNNER
          || optionToRun != null) {
        // Don't consume subcommand args if there's an option runner
        //   because of "cmd1 cmd2 --help" with "cmd1 <mainArgs...>".
        if((sub = command.subcommandTrie.find(mainArg)) != null) {
          parentCommands.addFirst(command);
          command = sub;
        }
      }
      else {
        if(eatAllCommandArgs) {
          commandData.multiArgs().add(mainArg);
        }
        else if(commandArgNames != null) {
          commandData.args().put(commandArgNames[commandArgNamesIndex],mainArg);

          if((++commandArgNamesIndex) >= commandArgNames.length) {
            // Reset.
            commandArgNames = null;
            commandArgNamesIndex = 0;
          }
        }
        else if((sub = command.subcommandTrie.find(mainArg)) != null) {
          if(sub.isMultiArg) {
            eatAllCommandArgs = true;
          }
          else if(sub.hasArgs()) {
            commandArgNames = sub.argNames;
          }

          parentCommands.addFirst(command);
          command = sub;
        }
        else {
          if(command.isRoot()) {
            throw new CrimException(Formatter.format(
                "Invalid option/command: '{}'.",mainArg));
          }
          else {
            throw new CrimException(Formatter.format(
                "For command '{}', invalid option/command: '{}'."
                ,command.buildFullName(root),mainArg));
          }
        }
      }
    }

    if(flag == ParseFlag.FOR_OPT_RUNNER) {
      return (optionToRun != null) ? optionToRun.runner : null;
    }

    // Check if have the required number of args for the last [sub]command,
    //   if there was no option runner.
    if(optionToRun == null && commandArgNames != null
        && commandArgNamesIndex < commandArgNames.length) {
      if(command.isRoot()) {
        throw new CrimException(Formatter.format(
            "Missing {} required arg{}."
            ,commandArgNames.length
            ,(commandArgNames.length == 1) ? "" : 's'));
      }
      else {
        throw new CrimException(Formatter.format(
            "Command '{}' requires {} arg{}."
            ,command.buildFullName(root)
            ,commandArgNames.length
            ,(commandArgNames.length == 1) ? "" : 's'));
      }
    }

    CommandRunner runner;

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

    return runner;
  }

  public void parseOption(Map<String,String> dataOptions,String optionName,String optionArg,Option option) {
    if(option.argName == null) {
      if(optionArg != null) {
        if(command.isRoot()) {
          throw new CrimException(Formatter.format(
              "Option '{}' does not accept args: '{}'."
              ,optionName,optionArg));
        }
        else {
          throw new CrimException(Formatter.format(
              "Command '{}' option '{}' does not accept args: '{}'."
              ,command.buildFullName(root),optionName,optionArg));
        }
      }

      dataOptions.put(option.name,"");
    }
    else {
      if(optionArg == null) {
        if((++mainArgsIndex) >= mainArgs.length) {
          if(command.isRoot()) {
            throw new CrimException(Formatter.format(
                "Option '{}' requires an arg."
                ,optionName));
          }
          else {
            throw new CrimException(Formatter.format(
                "Command '{}' option '{}' requires an arg."
                ,command.buildFullName(root),optionName));
          }
        }

        optionArg = mainArgs[mainArgsIndex];
      }

      dataOptions.put(option.name,optionArg);
    }

    if(option.runner != null) {
      if(optionToRun != null) {
        if(command.isRoot()) {
          throw new CrimException(Formatter.format(
              "Option '{}' conflicts with option '{}' runner."
              ,optionName,optionToRun.name));
        }
        else {
          throw new CrimException(Formatter.format(
              "Command '{}' option '{}' conflicts with option '{}' runner."
              ,command.buildFullName(root),optionName,optionToRun.name));
        }
      }

      optionToRun = option;
    }
  }

  public enum ParseFlag {
    NONE,FOR_OPT_RUNNER,HAS_OPT_RUNNER
  }
}
