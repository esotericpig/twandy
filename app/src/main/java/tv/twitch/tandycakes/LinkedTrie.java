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
public class LinkedTrie<V> {
  private final Node rootNode = new Node();
  private boolean allowLonger = true;

  public LinkedTrie() {
  }

  public LinkedTrie(boolean allowLonger) {
    this.allowLonger = allowLonger;
  }

  public void add(V value) {
    if(value == null) {
      throw new IllegalArgumentException("Null value.");
    }

    add(value.toString(),value);
  }

  public void add(String nameOrAlias,V value) {
    if(nameOrAlias.isEmpty()) {
      throw new IllegalArgumentException("Empty name/alias.");
    }
    if(value == null) {
      throw new IllegalArgumentException("Null value.");
    }

    final int length = nameOrAlias.length();
    Node parent = rootNode;

    for(int i = 0; ; ) {
      final int codePoint = nameOrAlias.codePointAt(i);
      i += Character.charCount(codePoint);

      if(i >= length) {
        // Store the actual value.
        parent = parent.storeChild(codePoint,value);
        break;
      }

      // Store part of the name/alias.
      parent = parent.storeChild(codePoint);
    }
  }

  public void addValueNameAndAlias(V value,String... aliases) {
    add(value);
    addAlias(value,aliases);
  }

  public void addAlias(V value,String... aliases) {
    for(String alias: aliases) {
      add(alias,value);
    }
  }

  public V find(String partial) {
    return find(partial,allowLonger);
  }

  public V find(String partial,boolean allowLonger) {
    return find(partial,null,allowLonger);
  }

  public V find(String partial,V defaultValue) {
    return find(partial,defaultValue,allowLonger);
  }

  public V find(String partial,V defaultValue,boolean allowLonger) {
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
        if(allowLonger && node.getValue() != null) {
          return node.getValue();
        }
        else {
          return defaultValue;
        }
      }

      node = child;
    }

    while(node.getValue() == null) {
      if(node.children.size() != 1) {
        return defaultValue;
      }

      node = node.children.values().iterator().next();
    }

    return (node.getValue() != null) ? node.getValue() : defaultValue;
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
  public class Node {
    private final Map<Integer,Node> children = new HashMap<>();
    private final V value;

    private Node() {
      this(null);
    }

    private Node(V value) {
      this.value = value;
    }

    protected Node storeChild(Integer codePoint) {
      return storeChild(codePoint,new Node());
    }

    protected Node storeChild(Integer codePoint,V value) {
      return storeChild(codePoint,new Node(value));
    }

    private Node storeChild(Integer codePoint,Node child) {
      Node thisChild = children.get(codePoint);

      if(thisChild == null || (thisChild.value == null && child.value != null)) {
        children.put(codePoint,child);
        thisChild = child;
      }

      return thisChild;
    }

    public Node getChild(int codePoint) {
      return children.get(codePoint);
    }

    public V getValue() {
      return value;
    }
  }
}
