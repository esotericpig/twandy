/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Jonathan Bradley Whited
 */

package tv.twitch.tandycakes.crim;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <pre>
 * Wow, I've used a record, so cool, Java 14+. Are you happy now?
 * </pre>
 *
 * @author Jonathan Bradley Whited
 * @since 1.0.0
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
