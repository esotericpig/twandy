/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Jonathan Bradley Whited
 */

package tv.twitch.tandycakes.crim;

/**
 * @author Jonathan Bradley Whited
 * @since 1.0.0
 */
@FunctionalInterface
public interface CommandRunner {
  void run(Crim crim,Command cmd,CommandData data);
}
