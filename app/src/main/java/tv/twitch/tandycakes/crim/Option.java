/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Jonathan Bradley Whited
 */

package tv.twitch.tandycakes.crim;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Jonathan Bradley Whited
 * @since 1.0.0
 */
public class Option {
  public static final Pattern ARG_PATTERN = Pattern.compile("\\s+|\\s*=\\s*");

  public CommandRunner runner;

  public final String name;
  public final String alias;
  public final String argName;
  public final boolean isMultiArg;

  public final List<String> summary = new LinkedList<>();

  public static Builder builder() {
    return new Builder();
  }

  public static String[] split(String optAndArg) {
    return ARG_PATTERN.split(optAndArg,2);
  }

  private Option(Builder builder) {
    if(builder.name == null) {
      throw new IllegalArgumentException("Null name.");
    }

    String[] parts = split(builder.name.strip());
    builder.name = parts[0];

    if(builder.name.isEmpty()) {
      throw new IllegalArgumentException("Empty name.");
    }

    this.runner = builder.runner;
    this.name = builder.name;
    this.alias = builder.alias;
    this.argName = (parts.length == 2) ? parts[1] : null;
    this.isMultiArg = builder.isMultiArg;

    if(builder.summary != null) {
      this.summary.addAll(builder.summary);
    }
  }

  public Option addSummary(String line) {
    summary.add(line);
    return this;
  }

  public Option addSummary(String... lines) {
    Collections.addAll(summary,lines);
    return this;
  }

  public static class Builder {
    public CommandRunner runner = null;

    public String name = null;
    public String alias = null;
    public boolean isMultiArg = false;

    public List<String> summary = new LinkedList<>();

    private Builder() {
    }

    public Option build() {
      return new Option(this);
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
      this.alias = alias;
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
  }
}
