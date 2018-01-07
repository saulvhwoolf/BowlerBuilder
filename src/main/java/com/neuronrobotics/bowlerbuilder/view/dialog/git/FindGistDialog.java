package com.neuronrobotics.bowlerbuilder.view.dialog.git;

import com.google.common.collect.TreeMultiset;
import com.neuronrobotics.bowlerbuilder.model.GitItem;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHObject;

public class FindGistDialog extends Dialog<GHGist> {

  private ObservableList<RankedGitItem<GHGist>> items;
  private List<RankedGitItem<GHGist>> gitData;
  private TreeMultiset<RankedGitItem<GHGist>> itemSet;

  public FindGistDialog(Iterable<GHGist> gists) {
    super();

    items = FXCollections.observableArrayList();
    gitData = new ArrayList<>();
    itemSet = TreeMultiset.create();

    gists.forEach(gist -> {
      String name = gist.getDescription();
      if (!"".equals(name)) {
        gitData.add(new RankedGitItem<>(new GitItem<>(name, gist)));
      }
    });

    items.addAll(gitData);

    TextField nameField = new TextField();
    nameField.setOnAction(event -> {
      gitData.forEach(item -> {
        item.setRank(FuzzySearch.ratio(nameField.getText(), item.gitItem.getDisplayName()));
        itemSet.add(item);
      });

      items.setAll(gitData);
    });

    ListView<RankedGitItem<GHGist>> listView = new ListView<>();
    listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    listView.setItems(items);

    VBox vBox = new VBox(5, nameField, listView);

    getDialogPane().setContent(vBox);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    setResultConverter(buttonType -> {
      if (buttonType.equals(ButtonType.OK)) {
        return listView.getSelectionModel().getSelectedItem().gitItem.getData();
      }

      return null;
    });
  }

  private class RankedGitItem<T extends GHObject> implements Comparable<RankedGitItem> {

    GitItem<T> gitItem;
    Integer rank;

    public RankedGitItem(GitItem<T> data) {
      this.gitItem = data;
    }

    public RankedGitItem(GitItem<T> gitItem, Integer rank) {
      this.gitItem = gitItem;
      this.rank = rank;
    }

    @Override
    public int compareTo(RankedGitItem rankedGitItem) {
      return gitItem.compareTo(rankedGitItem.gitItem);
    }

    @Override
    public String toString() {
      return gitItem.toString();
    }

    public void setRank(Integer rank) {
      this.rank = rank;
    }

  }

}
