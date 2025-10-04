/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Bradley Whited
 */

package tv.twitch.tandycakes.crim;

@FunctionalInterface
public interface CommandRunner {
  void run(Crim crim,Command cmd,CommandData data);
}
