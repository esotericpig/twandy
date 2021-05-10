/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Jonathan Bradley Whited
 */

package tv.twitch.tandycakes.crim;

import tv.twitch.tandycakes.Formatter;
import tv.twitch.tandycakes.LinkedTrie;

import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jonathan Bradley Whited
 * @since 1.0.0
 */
public class Command {
  public static final Pattern ARG_PATTERN = Pattern.compile("\\s+");

  public final Command parent;
  public final String name;
  public final String[] argNames;
  public final String[] aliases;
  public final List<String> summary = new LinkedList<>();
  public final List<String> about = new LinkedList<>();
  public CommandRunner runner;

  public final Map<String,Command> commands = new LinkedHashMap<>();
  public final LinkedTrie<Command> commandTrie = new LinkedTrie<>();

  public final Map<String,Option> options = new LinkedHashMap<>();
  public final LinkedTrie<Option> optionTrie = new LinkedTrie<>(false);

  public static String[] split(String cmdAndArgs) {
    return ARG_PATTERN.split(cmdAndArgs);
  }

  public Command(Command parent,String name,String summary,CommandRunner runner,String... aliases) {
    name = name.strip();

    if(name.isEmpty()) {
      throw new IllegalArgumentException("Empty name.");
    }

    this.parent = parent;
    this.aliases = aliases;
    this.runner = runner;

    Matcher matcher = ARG_PATTERN.matcher(name);
    int argsIndex = matcher.find() ? matcher.end() : -1;

    if(argsIndex < 0) {
      this.name = name;
      this.argNames = null;
    }
    else {
      // Store in this.name instead of name, for this.argNames.
      this.name = name.substring(0,matcher.start());

      if(this.name.isEmpty()) {
        throw new IllegalArgumentException("Empty name.");
      }

      this.argNames = split(name.substring(argsIndex));
    }

    if(summary != null) {
      addSummary(summary);
    }
  }

  public Command addSummary(String line) {
    summary.add(line);
    return this;
  }

  public Command addAbout(String line) {
    about.add(line);
    return this;
  }

  public Option addOption(String name) {
    return addOption(name,(String)null);
  }

  public Option addOption(String name,CommandRunner runner) {
    return addOption(name,null,runner);
  }

  public Option addOption(String name,String alias) {
    return addOption(name,alias,(String)null);
  }

  public Option addOption(String name,String alias,String summary) {
    return addOption(name,alias,summary,null);
  }

  public Option addOption(String name,String alias,CommandRunner runner) {
    return addOption(name,alias,null,runner);
  }

  public Option addOption(String name,String alias,String summary,CommandRunner runner) {
    Option option = new Option(name,alias,summary,runner);

    if(options.containsKey(option.name)) {
      throw new IllegalArgumentException(Formatter.format("Duplicate option: '{}'",option.name));
    }

    options.put(option.name,option);
    optionTrie.add(option.name,option);

    if(option.alias != null) {
      optionTrie.addAlias(option,option.alias);
    }

    return option;
  }

  public Command addCommand(String name,String... aliases) {
    return addCommand(name,(String)null,aliases);
  }

  public Command addCommand(String name,CommandRunner runner,String... aliases) {
    return addCommand(name,null,runner,aliases);
  }

  public Command addCommand(String name,String summary,String... aliases) {
    return addCommand(name,summary,null,aliases);
  }

  public Command addCommand(String name,String summary,CommandRunner runner,String... aliases) {
    Command command = new Command(this,name,summary,runner,aliases);

    if(commands.containsKey(command.name)) {
      throw new IllegalArgumentException(Formatter.format("Duplicate command: '{}'",command.name));
    }

    commands.put(command.name,command);
    commandTrie.add(command.name,command);

    if(command.aliases != null && command.aliases.length > 0) {
      commandTrie.addAlias(command,command.aliases);
    }

    return command;
  }

  public String buildFullName() {
    return buildFullName(null);
  }

  public String buildFullName(Command commandToStopAt) {
    Deque<String> names = new LinkedList<>();

    for(Command command = this; command != commandToStopAt; command = command.parent) {
      names.addFirst(command.name);
    }

    return String.join(" ",names);
  }
}
