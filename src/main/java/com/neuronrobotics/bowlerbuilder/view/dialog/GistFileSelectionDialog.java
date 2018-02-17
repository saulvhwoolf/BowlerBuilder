package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.view.dialog.util.ValidatedTextField;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class GistFileSelectionDialog extends Dialog<String[]> {

  private static final Logger logger =
      LoggerUtilities.getLogger(GistFileSelectionDialog.class.getSimpleName());
  private final ValidatedTextField gistField;
  private final ComboBox<String> fileChooser;

  public GistFileSelectionDialog(String title) {
    super();

    gistField = new ValidatedTextField("Invalid Gist URL", url ->
        validateURL(url).isPresent());
    gistField.setId("gistField");

    fileChooser = new ComboBox<>();
    fileChooser.setId("gistFileChooser");

    fileChooser.disableProperty().bind(gistField.invalidProperty());
    gistField.invalidProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) {
        try {
          List<String> files = ScriptingEngine.filesInGit(gistField.getText());
          fileChooser.setItems(FXCollections.observableArrayList(files));
        } catch (Exception e) {
          logger.warning("Could not fetch files in the gist: " + gistField.getText() + "\n"
              + Throwables.getStackTraceAsString(e));
        }
      }
    });

    setTitle(title);

    GridPane pane = new GridPane();
    pane.setId("root");
    pane.setAlignment(Pos.CENTER);
    pane.setHgap(5);
    pane.setVgap(5);

    pane.add(new Label("Gist URL"), 0, 0);
    pane.add(gistField, 1, 0);
    pane.add(new Label("File name"), 0, 1);
    pane.add(fileChooser, 1, 1);

    getDialogPane().setContent(pane);
    getDialogPane().setMinWidth(300);
    setResizable(true);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    FxUtil.runFX(gistField::requestFocus);

    Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
    okButton.disableProperty().bind(gistField.invalidProperty());
    okButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            fileChooser.getSelectionModel().getSelectedItem() == null
                || gistField.getText().isEmpty(),
        gistField.textProperty(),
        fileChooser.getSelectionModel().selectedItemProperty()));
    okButton.setDefaultButton(true);

    setResultConverter(buttonType -> {
      if (buttonType.equals(ButtonType.OK)) {
        return new String[]{gistField.getText(),
            fileChooser.getSelectionModel().getSelectedItem()};
      }

      return null;
    });
  }

  /**
   * Will accept http:// or https:// with .git or .git/.
   *
   * @param url gist URL
   * @return optional containing a valid gist URL, empty otherwise
   */
  private Optional<String> validateURL(String url) {
    //Any git URL is ((git|ssh|http(s)?)|(git@[\w\.]+))(:(//)?)([\w\.@\:/\-~]+)(\.git)(/)?
    if (url.matches("(http(s)?)(:(//)?)([\\w.@:/\\-~]+)(\\.git)(/)?")) {
      return Optional.of(url);
    }

    return Optional.empty();
  }

}
