/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Bradley Whited
 */

package tv.twitch.tandycakes.crim;

import tv.twitch.tandycakes.Formatter;
import tv.twitch.tandycakes.LinkedTrie;

import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class Command {
  public static final Pattern ARG_PATTERN = Pattern.compile("\\s+");

  // NOTE: Must be after all other static vars.
  public static final Command EMPTY = new Command("__EMPTY__");

  public CommandRunner runner = null;

  public final String name;
  public final String[] aliases;
  public final String[] argNames;
  public boolean isMultiArg = false;

  public final List<String> summary = new LinkedList<>();
  public final List<String> about = new LinkedList<>();

  public final Map<String,Option> options = new LinkedHashMap<>();
  public final LinkedTrie<Option> optionTrie = new LinkedTrie<>(false);

  public final Map<String,Command> subcommands = new LinkedHashMap<>();
  public final LinkedTrie<Command> subcommandTrie = new LinkedTrie<>();

  private Command parent = null;

  public static String[] split(String cmdAndArgs) {
    return ARG_PATTERN.split(cmdAndArgs);
  }

  public Command(String name,String... aliases) {
    if(name == null) {
      throw new IllegalArgumentException("Null name.");
    }

    name = name.strip();

    if(name.isEmpty()) {
      throw new IllegalArgumentException("Empty name.");
    }

    this.aliases = Arrays.copyOf(aliases,aliases.length);

    var matcher = ARG_PATTERN.matcher(name);
    int argsIndex = matcher.find() ? matcher.end() : -1;

    if(argsIndex < 0) {
      this.name = name;
      this.argNames = null;
    }
    else {
      this.name = name.substring(0,matcher.start());

      if(this.name.isEmpty()) {
        throw new IllegalArgumentException("Empty name with args.");
      }

      this.argNames = split(name.substring(argsIndex));
    }
  }

  public Command parent(Command parent) {
    if(parent == null) {
      throw new IllegalArgumentException("Null parent.");
    }

    this.parent = parent;
    return this;
  }

  public Command run(CommandRunner runner) {
    this.runner = runner;
    return this;
  }

  public Command multiArg() {
    isMultiArg = true;
    return this;
  }

  public Command singleArg() {
    isMultiArg = false;
    return this;
  }

  public Command summary(String line) {
    summary.add(line);
    return this;
  }

  public Command summary(String... lines) {
    Collections.addAll(summary,lines);
    return this;
  }

  public Command about(String line) {
    about.add(line);
    return this;
  }

  public Command about(String... lines) {
    Collections.addAll(about,lines);
    return this;
  }

  public Option option(String name) {
    return option(name,null);
  }

  public Option option(String name,String alias) {
    var option = new Option(name,alias).parent(this);

    if(options.containsKey(option.name)) {
      throw new IllegalArgumentException(Formatter.format("Duplicate option: '{}'.",option.name));
    }

    options.put(option.name,option);
    optionTrie.add(option.name,option);

    if(option.alias != null) {
      optionTrie.addAlias(option,option.alias);
    }

    return option;
  }

  public Command command(String name,String... aliases) {
    var sub = new Command(name,aliases).parent(this);

    if(subcommands.containsKey(sub.name)) {
      throw new IllegalArgumentException(Formatter.format("Duplicate command: '{}'.",sub.name));
    }

    subcommands.put(sub.name,sub);
    subcommandTrie.add(sub.name,sub);

    if(sub.hasAliases()) {
      subcommandTrie.addAlias(sub,sub.aliases);
    }

    return sub;
  }

  public Command tap(Consumer<Command> tapper) {
    tapper.accept(this);
    return this;
  }

  public Command end_command() {
    return (parent != null) ? parent : this;
  }

  public String buildFullName() {
    return buildFullName(null);
  }

  public String buildFullName(Command commandToStopBefore) {
    Deque<String> names = new LinkedList<>();

    for(var top = this; top != null && top != commandToStopBefore; top = top.parent) {
      names.addFirst(top.name);
    }

    return String.join(" ",names);
  }

  public boolean isRoot() {
    return parent == null;
  }

  public boolean hasAliases() {
    return aliases != null && aliases.length > 0;
  }

  public boolean hasArgs() {
    return argNames != null && argNames.length > 0;
  }
}
