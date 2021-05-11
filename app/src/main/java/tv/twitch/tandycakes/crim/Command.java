/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Jonathan Bradley Whited
 */

package tv.twitch.tandycakes.crim;

import tv.twitch.tandycakes.Formatter;
import tv.twitch.tandycakes.LinkedTrie;

import java.util.ArrayList;
import java.util.Collections;
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
  public CommandRunner runner;

  public final String name;
  public final String[] aliases;
  public final String[] argNames;
  public final boolean isMultiArg;

  public final List<String> summary = new LinkedList<>();
  public final List<String> about = new LinkedList<>();

  public final Map<String,Option> options = new LinkedHashMap<>();
  public final LinkedTrie<Option> optionTrie = new LinkedTrie<>(false);

  public final Map<String,Command> commands = new LinkedHashMap<>();
  public final LinkedTrie<Command> commandTrie = new LinkedTrie<>();

  public static Builder builder() {
    return new Builder();
  }

  public static String[] split(String cmdAndArgs) {
    return ARG_PATTERN.split(cmdAndArgs);
  }

  private Command(Builder builder) {
    if(builder.name == null) {
      throw new IllegalArgumentException("Null name.");
    }
    if(builder.aliases == null) {
      throw new IllegalArgumentException("Null aliases.");
    }

    builder.name = builder.name.strip();

    if(builder.name.isEmpty()) {
      throw new IllegalArgumentException("Empty name.");
    }

    this.parent = builder.parent;
    this.runner = builder.runner;
    this.aliases = builder.aliases.toArray(new String[0]);
    this.isMultiArg = builder.isMultiArg;

    Matcher matcher = ARG_PATTERN.matcher(builder.name);
    int argsIndex = matcher.find() ? matcher.end() : -1;

    if(argsIndex < 0) {
      this.name = builder.name;
      this.argNames = null;
    }
    else {
      // Store in this.name instead of builder.name, for this.argNames.
      this.name = builder.name.substring(0,matcher.start());

      if(this.name.isEmpty()) {
        throw new IllegalArgumentException("Empty name with args.");
      }

      this.argNames = split(builder.name.substring(argsIndex));
    }

    if(builder.summary != null) {
      this.summary.addAll(builder.summary);
    }
    if(builder.about != null) {
      this.about.addAll(builder.about);
    }
  }

  public Command addSummary(String line) {
    summary.add(line);
    return this;
  }

  public Command addSummary(String... lines) {
    Collections.addAll(summary,lines);
    return this;
  }

  public Command addAbout(String line) {
    about.add(line);
    return this;
  }

  public Command addAbout(String... lines) {
    Collections.addAll(about,lines);
    return this;
  }

  public Option addOption(Option.Builder builder) {
    Option option = builder.build();

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

  public Command addCommand(Command.Builder builder) {
    Command command = builder.parent(this).build();

    if(commands.containsKey(command.name)) {
      throw new IllegalArgumentException(Formatter.format("Duplicate command: '{}'.",command.name));
    }

    commands.put(command.name,command);
    commandTrie.add(command.name,command);

    if(command.hasAliases()) {
      commandTrie.addAlias(command,command.aliases);
    }

    return command;
  }

  public String buildFullName() {
    return buildFullName(null);
  }

  public String buildFullName(Command commandToStopBefore) {
    Deque<String> names = new LinkedList<>();

    for(Command command = this; command != commandToStopBefore; command = command.parent) {
      names.addFirst(command.name);
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

  public static class Builder {
    public Command parent = null;
    public CommandRunner runner = null;

    public String name = null;
    public List<String> aliases = new ArrayList<>();
    public boolean isMultiArg = false;

    public List<String> summary = new LinkedList<>();
    public List<String> about = new LinkedList<>();

    private Builder() {
    }

    public Command build() {
      return new Command(this);
    }

    public Builder parent(Command parent) {
      this.parent = parent;
      return this;
    }

    public Builder runner(CommandRunner runner) {
      this.runner = runner;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder alias(String alias) {
      this.aliases.add(alias);
      return this;
    }

    public Builder alias(String... aliases) {
      Collections.addAll(this.aliases,aliases);
      return this;
    }

    public Builder multiArg(boolean multiArg) {
      this.isMultiArg = multiArg;
      return this;
    }

    public Builder summary(String line) {
      this.summary.add(line);
      return this;
    }

    public Builder summary(String... lines) {
      Collections.addAll(this.summary,lines);
      return this;
    }

    public Builder about(String line) {
      this.about.add(line);
      return this;
    }

    public Builder about(String... lines) {
      Collections.addAll(this.about,lines);
      return this;
    }
  }
}
