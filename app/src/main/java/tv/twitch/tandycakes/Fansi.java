/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Bradley Whited
 */

package tv.twitch.tandycakes;

import tv.twitch.tandycakes.error.ParseException;

import java.io.PrintStream;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Locale;

public class Fansi {
  public static final String RESET = "\u001b[0m";

  private PrintStream out;
  private boolean isEnabled = true;
  private final LinkedTrie<String> styles = new LinkedTrie<>();

  public Fansi() {
    this(System.out);
  }

  public Fansi(PrintStream out) {
    this.out = out;

    storeDefaultStyles();
  }

  public void storeDefaultStyles() {
    // Styles.
    storeStyleKey("bold","\u001b[1m");

    // Foreground colors.
    storeStyleKey("black","\u001b[30m");
    storeStyleKey("red","\u001b[31m");
    storeStyleKey("green","\u001b[32m");
    storeStyleKey("yellow","\u001b[33m");
    storeStyleKey("blue","\u001b[34m");
    storeStyleKey("magenta","\u001b[35m");
    storeStyleKey("cyan","\u001b[36m");
    storeStyleKey("white","\u001b[37m");
    storeStyleKey("btblack","\u001b[90m");
    storeStyleKey("btred","\u001b[91m");
    storeStyleKey("btgreen","\u001b[92m");
    storeStyleKey("btyellow","\u001b[93m");
    storeStyleKey("btblue","\u001b[94m");
    storeStyleKey("btmagenta","\u001b[95m");
    storeStyleKey("btcyan","\u001b[96m");
    storeStyleKey("btwhite","\u001b[97m");

    // Background colors.
    storeStyleKey("bgblack","\u001b[40m");
    storeStyleKey("bgred","\u001b[41m");
    storeStyleKey("bggreen","\u001b[42m");
    storeStyleKey("bgyellow","\u001b[43m");
    storeStyleKey("bgblue","\u001b[44m");
    storeStyleKey("bgmagenta","\u001b[45m");
    storeStyleKey("bgcyan","\u001b[46m");
    storeStyleKey("bgwhite","\u001b[47m");
    storeStyleKey("bgbtblack","\u001b[100m");
    storeStyleKey("bgbtred","\u001b[101m");
    storeStyleKey("bgbtgreen","\u001b[102m");
    storeStyleKey("bgbtyellow","\u001b[103m");
    storeStyleKey("bgbtblue","\u001b[104m");
    storeStyleKey("bgbtmagenta","\u001b[105m");
    storeStyleKey("bgbtcyan","\u001b[106m");
    storeStyleKey("bgbtwhite","\u001b[107m");
  }

  public String buildKey(String name) {
    return name.replaceAll("\\s+","").toLowerCase(Locale.ROOT);
  }

  protected void storeStyleKey(String key,String style) {
    styles.add(key,style);
  }

  public void storeStyle(String name,String style) {
    if(style.isEmpty()) {
      throw new IllegalArgumentException("Empty style.");
    }

    String key = buildKey(name);

    if(key.isEmpty()) {
      throw new IllegalArgumentException(Formatter.format("Invalid style name '{}' as key '{}'.",name,key));
    }

    storeStyleKey(key,style);
  }

  public void storeStyleAlias(String alias,String styleNames) {
    StringBuilder stylesBuffer = new StringBuilder(styleNames.length());

    for(String styleName: styleNames.split("/")) {
      String styleKey = buildKey(styleName);
      String style = styles.find(styleKey);

      if(style == null) {
        throw new ParseException(Formatter.format("Invalid style name/alias '{}' with key '{}'."
            ,styleName,styleKey));
      }

      stylesBuffer.append(style);
    }

    storeStyle(alias,stylesBuffer.toString());
  }

  /**
   * TODO: in future, use a different escape method for "{bold {red breaks}}"?
   *
   * <pre>
   * Examples:
   *   ("This is {bold some} text!");
   *   ("This is {bold/green some } text!");
   *   ("This is {bold/blue {}} text!", "cool");
   *   ("This text {{}} is escaped and not {{red styled}}!");
   *   ("This is {bold/red some }} {{ {}} stylish escaped text!");
   *   ("This {bold {red works} } because of spaces.");
   *   ("This {bold {red breaks}} because gets escaped.");
   *   ("This {green is {red really } great }, right?");
   * </pre>
   */
  public String style(String format,Object... args) {
    if(format.isEmpty()) {
      return format;
    }
    if(!isEnabled) {
      return Formatter.format(format,args);
    }

    final int length = format.length();
    int argIndex = 0;
    StringBuilder buffer = new StringBuilder((int)(length * 1.5));
    Deque<String> styleBlocks = new LinkedList<>();

    for(int i = 0; i < length; ) {
      final int codePoint = format.codePointAt(i);
      final int charCount = Character.charCount(codePoint);

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
            if(args != null && argIndex < args.length) {
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

          // Read all the styles.
          StringBuilder styleBuffer = new StringBuilder();
          StringBuilder stylesBuffer = new StringBuilder();

          for(i = j; i < length; ) {
            final int codePoint3 = format.codePointAt(i);
            final int charCount3 = Character.charCount(codePoint3);

            boolean isSpace = Character.isWhitespace(codePoint3);
            boolean isToken = codePoint3 == '}' || codePoint3 == '{';
            boolean isEndOfStyles = isSpace || isToken;

            // End of format string? For: "{yellow".
            if(!isEndOfStyles && i >= (length - 1)) {
              styleBuffer.appendCodePoint(codePoint3);
              i += charCount3; // Eat the style name's last char.

              isEndOfStyles = true;
            }

            if(codePoint3 == '/' || isEndOfStyles) {
              String styleName = styleBuffer.toString();
              String styleKey = buildKey(styleName);

              // Allow empty names for future placeholders: "This is { some } text."
              if(!styleKey.isEmpty()) {
                String style = styles.find(styleKey);

                if(style == null) {
                  throw new ParseException(Formatter.format(
                      "Invalid style name/alias '{}' with key '{}' at index {}."
                      ,styleName,styleKey,i));
                }

                stylesBuffer.append(style);
              }

              styleBuffer.setLength(0); // Reset for the next style.

              if(isEndOfStyles) {
                if(isSpace) {
                  i += charCount3; // Eat the space.
                }

                break;
              }
              else {
                i += charCount3; // Eat the separator.
                continue;
              }
            }

            styleBuffer.appendCodePoint(codePoint3);
            i += charCount3;
          }

          String stylesStr = stylesBuffer.toString();

          buffer.append(stylesStr);
          // Add, even if empty, since have to remove later when hit "}".
          styleBlocks.addLast(stylesStr);

          continue;
        }
      }
      // For eating the space in " }".
      else if(Character.isWhitespace(codePoint)) {
        int j = i + charCount;

        if(j < length && format.codePointAt(j) == '}') {
          int k = j + 1;

          // Escaped?
          if(k < length && format.codePointAt(k) == '}') {
            buffer.appendCodePoint(codePoint); // Space.
            buffer.append('}');
            i = k + 1; // Processed " }}".
            continue;
          }

          i = j; // Eat the space only; process "}" on next iteration.
          continue;
        }
      }
      else if(codePoint == '}') {
        int j = i + 1;

        // Escaped?
        if(j < length && format.codePointAt(j) == '}') {
          buffer.append('}');
          i += 2; // Processed "}}".
          continue;
        }

        // Remove the last style block and reset the styles in the string.
        styleBlocks.pollLast();
        buffer.append(RESET);

        // Set back to the styles outside of this block "{...}".
        for(String styleBlock: styleBlocks) {
          buffer.append(styleBlock);
        }

        ++i; // Processed "}".
        continue;
      }

      buffer.appendCodePoint(codePoint);
      i += charCount;
    }

    return buffer.toString();
  }

  public void srint(String format,Object... args) {
    out.print(style(format,args));
  }

  public void srintln(String format,Object... args) {
    out.println(style(format,args));
  }

  public void srintln() {
    out.println();
  }

  public void srintf(String format,Object... args) {
    out.printf(style(format),args);
  }

  public void srintfn(String format,Object... args) {
    srintf(format,args);
    out.println();
  }

  public void print(String format) {
    out.print(format);
  }

  public void print(String format,Object... args) {
    out.print(Formatter.format(format,args));
  }

  public void println(String format) {
    out.println(format);
  }

  public void println(String format,Object... args) {
    out.println(Formatter.format(format,args));
  }

  public void println() {
    out.println();
  }

  public void printf(String format,Object... args) {
    out.printf(format,args);
  }

  public void printfn(String format,Object... args) {
    printf(format,args);
    out.println();
  }

  public void setOut(PrintStream out) {
    this.out = out;
  }

  public void enable() {
    this.isEnabled = true;
  }

  public void disable() {
    this.isEnabled = false;
  }

  public void setEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  public PrintStream getOut() {
    return out;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public boolean isDisabled() {
    return !isEnabled;
  }
}
