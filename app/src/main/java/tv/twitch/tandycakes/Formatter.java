/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Jonathan Bradley Whited
 */

package tv.twitch.tandycakes;

/**
 * @author Jonathan Bradley Whited
 * @since 1.0.0
 */
public class Formatter {
  private Formatter() {
    throw new UnsupportedOperationException("Not allowed to construct.");
  }

  public static String format(String format) {
    return (format != null) ? format : "";
  }

  /**
   * <pre>
   * Safe method to use for logging, exceptions, etc.
   *
   * "{}" is preserved in some cases in case {@code format} needs to be reused
   * in another formatter.
   *
   * Examples:
   *   (null)                              //=> ""
   *   ("This is {} text.", "some")        //=> "This is some text."
   *   ("This is {} text.")                //=> "This is {} text."
   *   ("This is {} text.", null)          //=> "This is {} text."
   *   ("This is {} text.", (String)null)  //=> "This is null text."
   *   ("This is {} text.", null, null)    //=> "This is null text."
   *   ("This is {{} text.", "escaped")    //=> "This is {} text."
   *
   * </pre>
   */
  public static String format(String format,Object... args) {
    if(format == null || format.isEmpty()) {
      return "";
    }
    if(args == null || args.length == 0) {
      return format;
    }

    final int length = format.length();
    int argIndex = 0;
    StringBuilder buffer = new StringBuilder(length * 2);

    for(int i = 0; i < length; ) {
      final int codePoint = format.codePointAt(i);

      if(codePoint == '{') {
        int j = i + 1;

        if(j < length) {
          final int codePoint2 = format.codePointAt(j);

          // Escaped?
          if(codePoint2 == '{') {
            buffer.append('{');
            i += 2; // Processed "{{".
            continue;
          }
          // Arg?
          else if(codePoint2 == '}') {
            if(argIndex < args.length) {
              buffer.append(args[argIndex]);
              ++argIndex;
            }
            else {
              // Append "{}" in case this needs to be used again in another formatter.
              buffer.append("{}");
            }

            i += 2; // Processed "{}".
            continue;
          }
        }
      }

      buffer.appendCodePoint(codePoint);
      i += Character.charCount(codePoint);
    }

    return buffer.toString();
  }
}
