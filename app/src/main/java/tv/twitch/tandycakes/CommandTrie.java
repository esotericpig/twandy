/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Jonathan Bradley Whited
 */

package tv.twitch.tandycakes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jonathan Bradley Whited
 * @since 1.0.0
 */
public class CommandTrie {
  private final Node rootNode = new Node();
  private boolean allowLonger = true;

  public CommandTrie() {
  }

  public CommandTrie(boolean allowLonger) {
    this.allowLonger = allowLonger;
  }

  public void addCommand(String command) {
    addCommand(command,command);
  }

  public void addCommand(String nameOrAlias,String command) {
    if(nameOrAlias.isEmpty()) {
      throw new IllegalArgumentException("Empty name/alias.");
    }
    if(command.isEmpty()) {
      throw new IllegalArgumentException("Empty command.");
    }

    final int length = nameOrAlias.length();
    Node parent = rootNode;

    for(int i = 0; ; ) {
      final int codePoint = nameOrAlias.codePointAt(i);
      i += Character.charCount(codePoint);

      if(i >= length) {
        // Store the actual command.
        parent = parent.storeChild(codePoint,command);
        break;
      }

      // Store part of the name/alias.
      parent = parent.storeChild(codePoint);
    }
  }

  public void addCommandAndAlias(String command,String... aliases) {
    addCommand(command,command);
    addAlias(command,aliases);
  }

  public void addAlias(String command,String... aliases) {
    for(String alias: aliases) {
      addCommand(alias,command);
    }
  }

  public String findCommand(String partial) {
    return findCommand(partial,null);
  }

  public String findCommand(String partial,String defaultValue) {
    if(partial == null) {
      return defaultValue;
    }

    final int length = partial.length();
    Node node = rootNode;
    int codePoint;

    for(int i = 0; i < length; i += Character.charCount(codePoint)) {
      codePoint = partial.codePointAt(i);
      Node child = node.getChild(codePoint);

      if(child == null) {
        if(allowLonger && node.getCommand() != null) {
          return node.getCommand();
        }
        else {
          return defaultValue;
        }
      }

      node = child;
    }

    while(node.getCommand() == null) {
      if(node.children.size() != 1) {
        return defaultValue;
      }

      node = node.children.values().iterator().next();
    }

    return (node.getCommand() != null) ? node.getCommand() : defaultValue;
  }

  public void setAllowLonger(boolean allowLonger) {
    this.allowLonger = allowLonger;
  }

  public boolean isAllowLonger() {
    return allowLonger;
  }

  /**
   * @author Jonathan Bradley Whited
   * @since 1.0.0
   */
  public static class Node {
    private final Map<Integer,Node> children = new HashMap<>();
    private final String command;

    private Node() {
      this(null);
    }

    private Node(String command) {
      this.command = command;
    }

    protected Node storeChild(Integer codePoint) {
      return storeChild(codePoint,new Node());
    }

    protected Node storeChild(Integer codePoint,String command) {
      return storeChild(codePoint,new Node(command));
    }

    private Node storeChild(Integer codePoint,Node child) {
      Node thisChild = children.get(codePoint);

      if(thisChild == null || (thisChild.command == null && child.command != null)) {
        children.put(codePoint,child);
        thisChild = child;
      }

      return thisChild;
    }

    public Node getChild(int codePoint) {
      return children.get(codePoint);
    }

    public String getCommand() {
      return command;
    }
  }
}
