package com.neuronrobotics.bowlerbuilder.view.dialog.git;

import com.neuronrobotics.bowlerbuilder.model.GitItem;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
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

public class FindGistDialog extends Dialog<GHGist> {

  private ObservableList<GitItem<GHGist>> items;
  private Map<GitItem<GHGist>, Integer> itemMap;

  public FindGistDialog(Iterable<GHGist> gists) {
    super();

    items = FXCollections.observableArrayList();
    itemMap = new TreeMap<>();

    gists.forEach(gist -> {
      String name = gist.getDescription();
      if (!"".equals(name)) {
        items.add(new GitItem<>(name, gist));
      }
    });

    TextField nameField = new TextField();
    nameField.setOnAction(event -> {
      itemMap.clear();

      gists.forEach(gist -> {
        String name = gist.getDescription();
        if (!"".equals(name)) {
          itemMap.put(new GitItem<>(name, gist), FuzzySearch.ratio(nameField.getText(), name));
        }
      });

      items.setAll(entriesSortedByValues(itemMap).stream()
          .map(Map.Entry::getKey)
          .collect(Collectors.toList()));
    });

    ListView<GitItem<GHGist>> listView = new ListView<>();
    listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    listView.setItems(items);

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

  static <K, V extends Comparable<? super V>>
  SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
    SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<>(
        Collections.reverseOrder(Comparator.comparing(Map.Entry::getValue)));
    sortedEntries.addAll(map.entrySet());
    return sortedEntries;
  }

}
