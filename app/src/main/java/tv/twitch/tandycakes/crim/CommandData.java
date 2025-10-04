/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Bradley Whited
 */

package tv.twitch.tandycakes.crim;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <pre>
 * Wow, I used a record, so cool, Java 14+. Happy now?
 * </pre>
 */
public record CommandData(
    Map<String,String> globalOpts
    ,Map<String,String> opts
    ,Map<String,String> args
    ,Deque<String> multiArgs
) {
  public CommandData() {
    this(
        new LinkedHashMap<>()
        ,new LinkedHashMap<>()
        ,new LinkedHashMap<>()
        ,new ArrayDeque<>()
    );
  }
}
