package com.neuronrobotics.bowlerbuilder.controller;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.view.tab.AceCadEditorTab;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import eu.mihosoft.vrl.v3d.CSG;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.controlsfx.glyphfont.Glyph;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHGist;

public class WebBrowserController {

  private static final Logger LOGGER =
      LoggerUtilities.getLogger(WebBrowserController.class.getSimpleName());

  private final MainWindowController parentController;
  @FXML
  private Button backPageButton;
  @FXML
  private Button nextPageButton;
  @FXML
  private Button reloadPageButton;
  @FXML
  private Button homePageButton;
  @FXML
  private TextField urlField;
  @FXML
  private WebView webView;
  @FXML
  private Button runButton;
  @FXML
  private ImageView runIcon;
  @FXML
  private Button modifyButton;
  @FXML
  private ChoiceBox<String> fileBox;

  private final StringProperty currentGist = new SimpleStringProperty("currentGist");
  private final StringProperty lastURL = new SimpleStringProperty("");

  @Inject
  public WebBrowserController(MainWindowController parentController) {
    this.parentController = parentController;
  }

  @FXML
  protected void initialize() {
    backPageButton.setGraphic(new Glyph("FontAwesome", "ARROW_LEFT"));
    nextPageButton.setGraphic(new Glyph("FontAwesome", "ARROW_RIGHT"));
    reloadPageButton.setGraphic(new Glyph("FontAwesome", "REFRESH"));
    homePageButton.setGraphic(new Glyph("FontAwesome", "HOME"));
    runButton.setGraphic(AssetFactory.loadIcon("Run.png"));
    modifyButton.setGraphic(AssetFactory.loadIcon("Edit-Script.png"));

    //Update the url field when a new page gets loaded
    webView.getEngine().locationProperty().addListener((observable, oldValue, newValue) ->
        urlField.setText(newValue));
    webView.getEngine().getLoadWorker().stateProperty()
        .addListener((observableValue, oldState, newState) -> {
          if (newState.equals(State.SUCCEEDED)) {
            final Thread thread = LoggerUtilities.newLoggingThread(LOGGER, () -> {
              Platform.runLater(() -> {
                runButton.setDisable(true);
                modifyButton.setDisable(true);
                fileBox.setDisable(true);
              });

              final ImmutableList<String> gists = ImmutableList.copyOf(
                  ScriptingEngine.getCurrentGist(lastURL.get(), webView.getEngine()));

              if (gists.isEmpty()) {
                LOGGER.info("No gists found on the current page.");
                return;
              }

              //Transform the current page URL into a git URL
              if (lastURL.get().contains("https://github.com/")) {
                //TODO: Support normal repos
              /*if (address.endsWith("/")) {
                address = address.substring(0, address.length() - 1);
              }
              currentGist = address + ".git";*/
                LOGGER.info("Can't parse from normal repo.");
                return;
              } else {
                currentGist.set("https://gist.github.com/" + gists.get(0) + ".git");
              }

              LOGGER.fine("Current gist is: " + currentGist.get());

              try {
                //Load files and remove "csgDatabase.json"
                final ImmutableList<String> files = ImmutableList.copyOf(
                    ScriptingEngine.filesInGit(currentGist.get()).stream()
                        .filter(item -> !item.contains("csgDatabase.json"))
                        .collect(Collectors.toList()));

                if (files.isEmpty()) {
                  //If there aren't files, just clear the file box
                  Platform.runLater(() -> fileBox.getItems().clear());
                } else {
                  //If there are files, add them to the fileBox, re-enable the buttons, and load
                  //the first file
                  WebBrowserController.this.loadGitLocal(currentGist.get(), files.get(0));
                  Platform.runLater(() -> {
                    runButton.setDisable(false);
                    modifyButton.setDisable(false);
                    fileBox.setDisable(false);
                    fileBox.getItems().setAll(files);
                    fileBox.getSelectionModel().select(0);
                  });
                }
              } catch (Exception e) {
                LOGGER.warning("Could not parse and run script.\n"
                    + Throwables.getStackTraceAsString(e));
              }
            });
            thread.setDaemon(true);
            thread.start();
          }
        });
  }

  @FXML
  private void onBackPage(ActionEvent actionEvent) {
    FxUtil.runFX(() -> webView.getEngine().executeScript("history.back()"));
  }

  @FXML
  private void onNextPage(ActionEvent actionEvent) {
    FxUtil.runFX(() -> webView.getEngine().executeScript("history.forward()"));
  }

  @FXML
  private void onReloadPage(ActionEvent actionEvent) {
    webView.getEngine().reload();
  }

  @FXML
  private void onHomePage(ActionEvent actionEvent) {
    loadPage("http://commonwealthrobotics.com/BowlerStudio/Welcome-To-BowlerStudio/");
  }

  @FXML
  private void onNavigate(ActionEvent actionEvent) {
    String url = urlField.getText();

    if (!url.toLowerCase(Locale.ENGLISH).matches("^\\w+://.*")) {
      url = String.format("http://%s", url);
    }

    loadPage(url);
  }

  @FXML
  private void onRun(ActionEvent actionEvent) {
    final Thread thread = LoggerUtilities.newLoggingThread(LOGGER, () -> {
      try {
        final File currentFile = ScriptingEngine.fileFromGit(currentGist.get(),
            fileBox.getSelectionModel().getSelectedItem());
        final String name = currentFile.getName();
        final Object obj = ScriptingEngine.inlineScriptRun(currentFile, null,
            ScriptingEngine.getShellType(name));
        parseResult(obj);
      } catch (Exception e) {
        LOGGER.warning("Could not parse and run script.\n"
            + Throwables.getStackTraceAsString(e));
      }
    });
    thread.setDaemon(true);
    thread.start();
  }

  /**
   * Parse the result of a script. CSG objects get added to a CAD engine.
   *
   * @param result script result
   */
  private void parseResult(@Nullable final Object result) {
    if (result instanceof Iterable) {
      final Iterable<?> iterable = (Iterable) result;
      final Object firstElement = iterable.iterator().next();

      if (firstElement instanceof CSG) {
        parseCSG((Iterable<CSG>) iterable);
      }
    }
  }

  /**
   * Add CSGs to a new {@link AceCadEditorTab}.
   *
   * @param csgIterable CSGs to add
   */
  private void parseCSG(@Nonnull final Iterable<CSG> csgIterable) {
    Platform.runLater(() -> {
      final String selection = fileBox.getSelectionModel().getSelectedItem();
      try {
        AceCadEditorTab tab = new AceCadEditorTab(selection);
        tab.getController().getCADViewerController().addAllCSGs(csgIterable);
        final GHGist gist = ScriptingEngine.getGithub()
            .getGist(ScriptingEngine.urlToGist(currentGist.get()));
        tab.getController().loadGist(gist, gist.getFile(selection));
        parentController.addTab(tab);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  @FXML
  private void onModify(ActionEvent actionEvent) {
    final String selection = fileBox.getSelectionModel().getSelectedItem();

    LOGGER.fine("selection: " + selection);

    try {
      final File currentFile = ScriptingEngine.fileFromGit(currentGist.get(), selection);
      //TODO: Doesn't properly detect owner
      final Boolean isOwner = ScriptingEngine.checkOwner(currentFile);

      LOGGER.fine("currentFile: " + currentFile);
      LOGGER.fine("isOwner: " + isOwner);

      if (isOwner) {
        LOGGER.fine("Opening file from git in editor: " + currentGist.get() + ", " + selection);
        final GHGist gist = ScriptingEngine.getGithub().getGist(currentGist.get());
        parentController.openGistFileInEditor(gist, gist.getFile(selection));
      } else {
        LOGGER.info("Forking file from git: " + currentGist.get());
        final GHGist gist = ScriptingEngine.fork(ScriptingEngine.urlToGist(currentGist.get()));
        currentGist.set(gist.getGitPushUrl());
        LOGGER.info("Fork Push URL: " + currentGist.get());
        LOGGER.info("Fork done.");

        parentController.openGistFileInEditor(gist, gist.getFile(selection));
      }
    } catch (Exception e) {
      LOGGER.warning("Could not load script.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  public void loadPage(@Nonnull final String url) {
    lastURL.set(url);
    webView.getEngine().load(url);
  }

  private void loadGitLocal(@Nonnull final String currentGist, @Nonnull final String file) {
    LOGGER.fine("currentGist: " + currentGist);
    LOGGER.fine("file: " + file);

    try {
      final File currentFile = ScriptingEngine.fileFromGit(currentGist, file);
      final boolean isOwner = ScriptingEngine.checkOwner(currentFile);

      LOGGER.fine("currentFile: " + currentFile);
      LOGGER.fine("isOwner: " + isOwner);

      Platform.runLater(() -> {
        if (isOwner) {
          modifyButton.setText("Edit...");
          modifyButton.setGraphic(AssetFactory.loadIcon("Edit-Script.png"));
        } else {
          modifyButton.setText("Make a Copy");
          modifyButton.setGraphic(AssetFactory.loadIcon("Make-Copy-Script.png"));
        }

        try {
          runIcon.setImage(AssetFactory.loadAsset("Script-Tab-"
              + ScriptingEngine.getShellType(currentFile.getName() + ".png")));
        } catch (Exception e) {
          LOGGER.warning("Could not load asset.\n" + Throwables.getStackTraceAsString(e));
        }
      });
    } catch (GitAPIException | IOException e) {
      LOGGER.warning("Could not parse file from git: " + currentGist + ", " + file + "\n"
          + Throwables.getStackTraceAsString(e));
    }
  }

  public WebEngine getEngine() {
    return webView.getEngine();
  }

}