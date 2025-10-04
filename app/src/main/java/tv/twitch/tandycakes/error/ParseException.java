/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Bradley Whited
 */

package tv.twitch.tandycakes.error;

import java.io.Serial;

public class ParseException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 1L;

  public ParseException() {
    super();
  }

  public ParseException(String message) {
    super(message);
  }

  public ParseException(String message,Throwable cause) {
    super(message,cause);
  }

  public ParseException(Throwable cause) {
    super(cause);
  }
}
