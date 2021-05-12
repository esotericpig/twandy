/*
 * This file is part of twandy.
 * Copyright (c) 2021 Jonathan Bradley Whited
 */

package tv.twitch.tandycakes;

import java.util.Map;

/**
 * <pre>
 * Completely unnecessary, as an instance initialization block is better,
 * but made it for fun.
 * </pre>
 *
 * @author Jonathan Bradley Whited
 * @since 1.0.0
 */
@FunctionalInterface
public interface MapMaker<M> {
  static <M extends Map<K,V>,K,V> M make(M map,MapMaker<M> maker) {
    maker.build(map);
    return map;
  }

  void build(M map);
}
