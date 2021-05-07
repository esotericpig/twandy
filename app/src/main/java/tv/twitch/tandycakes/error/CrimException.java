/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Jonathan Bradley Whited
 */

package tv.twitch.tandycakes.error;

import java.io.Serial;

/**
 * @author Jonathan Bradley Whited
 * @since 1.0.0
 */
public class CrimException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 1L;

  public CrimException() {
    super();
  }

  public CrimException(String message) {
    super(message);
  }

  public CrimException(String message,Throwable cause) {
    super(message,cause);
  }

  public CrimException(Throwable cause) {
    super(cause);
  }
}
