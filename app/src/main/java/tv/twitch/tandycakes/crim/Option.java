/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Bradley Whited
 */

package tv.twitch.tandycakes.crim;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class Option {
  public static final Pattern ARG_PATTERN = Pattern.compile("\\s+|\\s*=\\s*");

  public CommandRunner runner = null;

  public final String name;
  public final String alias;
  public final String argName;
  public boolean isMultiArg = false;

  public final List<String> summary = new LinkedList<>();

  private Command parent = Command.EMPTY;

  public static String[] split(String optAndArg) {
    return ARG_PATTERN.split(optAndArg,2);
  }

  public Option(String name) {
    this(name,null);
  }

  public Option(String name,String alias) {
    if(name == null) {
      throw new IllegalArgumentException("Null name.");
    }

    var parts = split(name.strip());
    name = parts[0];

    if(name.isEmpty()) {
      throw new IllegalArgumentException("Empty name.");
    }

    this.name = name;
    this.alias = alias;
    this.argName = (parts.length == 2) ? parts[1] : null;
  }

  public Option parent(Command parent) {
    if(parent == null) {
      throw new IllegalArgumentException("Null parent.");
    }

    this.parent = parent;
    return this;
  }

  public Option runner(CommandRunner runner) {
    this.runner = runner;
    return this;
  }

  public Option multiArg() {
    isMultiArg = true;
    return this;
  }

  public Option singleArg() {
    isMultiArg = false;
    return this;
  }

  public Option summary(String line) {
    summary.add(line);
    return this;
  }

  public Option summary(String... lines) {
    Collections.addAll(summary,lines);
    return this;
  }

  public Option tap(Consumer<Option> tapper) {
    tapper.accept(this);
    return this;
  }

  public Command end_option() {
    return parent;
  }
}
