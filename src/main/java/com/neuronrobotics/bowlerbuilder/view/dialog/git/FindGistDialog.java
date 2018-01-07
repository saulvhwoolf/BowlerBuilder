package com.neuronrobotics.bowlerbuilder.view.dialog.git;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultiset;
import com.neuronrobotics.bowlerbuilder.model.GitItem;
import com.neuronrobotics.bowlerbuilder.model.RankedGitItem;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.kohsuke.github.GHGist;

public class FindGistDialog extends Dialog<GHGist> {

  private ListView<RankedGitItem<GHGist>> listView;

  private List<RankedGitItem<GHGist>> gitData;
  private TreeMultiset<RankedGitItem<GHGist>> itemSet;

  public FindGistDialog(Iterable<GHGist> gists) {
    super();

    listView = new ListView<>();
    gitData = new ArrayList<>();
    itemSet = TreeMultiset.create(Ordering.natural().reverse());

    gists.forEach(gist -> {
      String name = gist.getDescription();
      if (!"".equals(name)) {
        gitData.add(new RankedGitItem<>(new GitItem<>(name, gist)));
      }
    });

    listView.setItems(FXCollections.observableArrayList(gitData));

    TextField nameField = new TextField();
    nameField.setOnAction(event -> {
      itemSet.clear();

      gitData.forEach(item -> {
        item.setRank(FuzzySearch.ratio(nameField.getText(), item.getDisplayName()));
        itemSet.add(item);
      });

      itemSet.forEach(item -> System.out.println(item.getRank()));
      listView.setItems(FXCollections.observableArrayList(itemSet));
    });

    listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

    VBox vBox = new VBox(5, nameField, listView);

    getDialogPane().setContent(vBox);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    setResultConverter(buttonType -> {
      if (buttonType.equals(ButtonType.OK)) {
        return listView.getSelectionModel().getSelectedItem().getData();
      }

      return null;
    });
  }

}
