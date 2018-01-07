package com.neuronrobotics.bowlerbuilder.model;

import org.kohsuke.github.GHObject;

public class RankedGitItem<T extends GHObject> implements Comparable<RankedGitItem> {

  private final GitItem<T> gitItem;
  private Integer rank;

  public RankedGitItem(GitItem<T> data) {
    this.gitItem = data;
  }

  public RankedGitItem(GitItem<T> gitItem, Integer rank) {
    this.gitItem = gitItem;
    this.rank = rank;
  }

  @Override
  public int compareTo(RankedGitItem rankedGitItem) {
    return rank.compareTo(rankedGitItem.rank);
  }

  @Override
  public String toString() {
    return gitItem.toString();
  }

  public void setRank(Integer rank) {
    this.rank = rank;
  }

  public String getDisplayName() {
    return gitItem.getDisplayName();
  }

  public T getData() {
    return gitItem.getData();
  }

  public Integer getRank() {
    return rank;
  }

}
