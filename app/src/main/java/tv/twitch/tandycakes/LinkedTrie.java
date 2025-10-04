/*
 * This file is part of Twandy.
 * Copyright (c) 2021 Bradley Whited
 */

package tv.twitch.tandycakes;

import java.util.HashMap;
import java.util.Map;

/**
 * For efficiency, could add an internal HashMap, but no need.
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

  public void addNameAndAlias(V value,String... aliases) {
    add(value);
    addAlias(value,aliases);
  }

  public void addNameAndAlias(String name,V value,String... aliases) {
    add(name,value);
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
    if(partial == null || partial.isEmpty()) {
      return defaultValue;
    }

    final int length = partial.length();
    Node node = rootNode;
    int codePoint;

    for(int i = 0; i < length; i += Character.charCount(codePoint)) {
      codePoint = partial.codePointAt(i);
      Node child = node.getChild(codePoint);

      if(child == null) {
        // If allowLonger and the partial to find is "helpme", but the only name
        //   found is "help", then return the value of "help".
        // This allows a user to be more verbose than necessary.
        if(allowLonger && node.hasValue()) {
          return node.getValueBag().value;
        }
        else {
          return defaultValue;
        }
      }

      node = child;
    }

    // If the partial to find is "ver" and the only name that starts with that
    //   is "version", then return the value of "version".
    while(node.hasNoValue()) {
      // If there are 2 names that start with "ver", then just return the
      //   default value, since it's ambiguous,
      //   for example "version" and "verbose".
      if(node.children.size() != 1) {
        return defaultValue;
      }

      node = node.children.values().iterator().next();
    }

    return node.getValue(defaultValue);
  }

  public void setAllowLonger(boolean allowLonger) {
    this.allowLonger = allowLonger;
  }

  public boolean isAllowLonger() {
    return allowLonger;
  }

  public class Node {
    private final Map<Integer,Node> children = new HashMap<>();
    private final ValueBag valueBag;

    private Node() {
      this.valueBag = null;
    }

    private Node(V value) {
      this.valueBag = new ValueBag(value);
    }

    protected Node storeChild(Integer codePoint) {
      return storeChild(codePoint,new Node());
    }

    protected Node storeChild(Integer codePoint,V value) {
      return storeChild(codePoint,new Node(value));
    }

    private Node storeChild(Integer codePoint,Node child) {
      Node thisChild = children.get(codePoint);

      if(thisChild == null || (thisChild.valueBag == null && child.valueBag != null)) {
        children.put(codePoint,child);
        thisChild = child;
      }

      return thisChild;
    }

    public Node getChild(int codePoint) {
      return children.get(codePoint);
    }

    public V getValue() {
      return getValue(null);
    }

    public V getValue(V defaultValue) {
      return (valueBag != null) ? valueBag.value : defaultValue;
    }

    public boolean hasValue() {
      return valueBag != null;
    }

    public boolean hasNoValue() {
      return valueBag == null;
    }

    public ValueBag getValueBag() {
      return valueBag;
    }
  }

  public class ValueBag {
    public final V value;

    public ValueBag(V value) {
      this.value = value;
    }
  }
}
