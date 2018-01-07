package com.neuronrobotics.bowlerbuilder.model;

import org.kohsuke.github.GHObject;

public class GitItem<T extends GHObject> implements Comparable<GitItem> {

  private String displayName;
  private T data;

  public GitItem(String displayName, T data) {
    this.displayName = displayName;
    this.data = data;
  }

  public String getDisplayName() {
    return displayName;
  }

  public T getData() {
    return data;
  }

  @Override
  public String toString() {
    return displayName;
  }

  @Override
  public int compareTo(GitItem otherItem) {
    return displayName.compareTo(otherItem.displayName);
  }

}
