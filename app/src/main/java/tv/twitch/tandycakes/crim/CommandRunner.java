/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Jonathan Bradley Whited
 */

package tv.twitch.tandycakes.crim;

import java.util.Map;

/**
 * @author Jonathan Bradley Whited
 * @since 1.0.0
 */
@FunctionalInterface
public interface CommandRunner {
  void run(Crim crim,Command cmd,Map<String,String> opts,Map<String,String> args);
}
