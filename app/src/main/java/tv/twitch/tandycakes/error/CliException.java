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
public class CliException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 1L;

  public CliException() {
    super();
  }

  public CliException(String message) {
    super(message);
  }

  public CliException(String message,Throwable cause) {
    super(message,cause);
  }

  public CliException(Throwable cause) {
    super(cause);
  }
}
