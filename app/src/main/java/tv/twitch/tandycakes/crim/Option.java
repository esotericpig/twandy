/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Jonathan Bradley Whited
 */

package tv.twitch.tandycakes.crim;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Jonathan Bradley Whited
 * @since 1.0.0
 */
public class Option {
  public static final Pattern ARG_PATTERN = Pattern.compile("\\s+|\\s*=\\s*");

  public final String name;
  public final String argName;
  public final String alias;
  public final List<String> summary = new LinkedList<>();
  public CommandRunner runner;

  public static String[] split(String optAndArg) {
    return ARG_PATTERN.split(optAndArg,2);
  }

  public Option(String name,String alias,String summary,CommandRunner runner) {
    String[] parts = split(name.strip());
    name = parts[0];

    if(name.isEmpty()) {
      throw new IllegalArgumentException("Empty name.");
    }

    this.name = name;
    this.argName = (parts.length == 2) ? parts[1] : null;
    this.alias = alias;
    this.runner = runner;

    if(summary != null) {
      addSummary(summary);
    }
  }

  public Option addSummary(String line) {
    summary.add(line);
    return this;
  }
}
