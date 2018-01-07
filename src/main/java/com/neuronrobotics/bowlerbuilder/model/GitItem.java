package com.neuronrobotics.bowlerbuilder.model;

import java.util.Objects;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GitItem<?> gitItem = (GitItem<?>) o;
    return Objects.equals(displayName, gitItem.displayName)
        && Objects.equals(data, gitItem.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(displayName, data);
  }

}
