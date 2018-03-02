package com.neuronrobotics.bowlerbuilder.controller;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.annotation.Nonnull;
import org.controlsfx.glyphfont.Glyph;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHGist;

public class WebBrowserController {

  private static final Logger logger =
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

  private String currentGist;
  private String lastURL = "";

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
        .addListener((observableValue, state, t1) -> {
          if (t1.equals(Worker.State.SUCCEEDED)) {
            Thread thread = LoggerUtilities.newLoggingThread(logger, () -> {
              Platform.runLater(() -> {
                runButton.setDisable(true);
                modifyButton.setDisable(true);
                fileBox.setDisable(true);
              });

              ImmutableList<String> gists = ImmutableList.copyOf(
                  ScriptingEngine.getCurrentGist(lastURL, webView.getEngine()));

              if (gists.isEmpty()) {
                logger.info("No gists found on the current page.");
                return;
              }

              //Transform the current page URL into a git URL
              if (lastURL.contains("https://github.com/")) {
                //TODO: Support normal repos
                /*if (address.endsWith("/")) {
                  address = address.substring(0, address.length() - 1);
                }
                currentGist = address + ".git";*/
                logger.info("Can't parse from normal repo.");
                return;
              } else {
                currentGist = "https://gist.github.com/" + gists.get(0) + ".git";
              }

              logger.fine("Current gist is: " + currentGist);

              try {
                //Load files and remove "csgDatabase.json"
                final ImmutableList<String> files = ImmutableList.copyOf(
                    ScriptingEngine.filesInGit(currentGist).stream()
                        .filter(item -> !item.contains("csgDatabase.json"))
                        .collect(Collectors.toList()));

                if (files.isEmpty()) {
                  //If there aren't files, just clear the file box
                  Platform.runLater(() -> fileBox.getItems().clear());
                } else {
                  //If there are files, add them to the fileBox, re-enable the buttons, and load
                  //the first file
                  loadGitLocal(currentGist, files.get(0));
                  Platform.runLater(() -> {
                    runButton.setDisable(false);
                    modifyButton.setDisable(false);
                    fileBox.setDisable(false);
                    fileBox.getItems().setAll(files);
                    fileBox.getSelectionModel().select(0);
                  });
                }
              } catch (Exception e) {
                logger.warning("Could not parse and run script.\n"
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
    Thread thread = LoggerUtilities.newLoggingThread(logger, () -> {
      try {
        final File currentFile = ScriptingEngine.fileFromGit(currentGist,
            fileBox.getSelectionModel().getSelectedItem());
        final String name = currentFile.getName();
        final Object obj = ScriptingEngine.inlineScriptRun(currentFile, null,
            ScriptingEngine.getShellType(name));
      } catch (Exception e) {
        logger.warning("Could not parse and run script.\n"
            + Throwables.getStackTraceAsString(e));
      }
    });
    thread.setDaemon(true);
    thread.start();
  }

  @FXML
  private void onModify(ActionEvent actionEvent) {
    final String selection = fileBox.getSelectionModel().getSelectedItem();

    try {
      final File currentFile = ScriptingEngine.fileFromGit(currentGist, selection);
      //TODO: Doesn't properly detect owner
      final Boolean isOwner = ScriptingEngine.checkOwner(currentFile);

      if (isOwner) {
        logger.fine("Opening file from git in editor: " + currentGist + ", " + selection);
        final GHGist gist = ScriptingEngine.getGithub().getGist(currentGist);
        parentController.openGistFileInEditor(gist, gist.getFile(selection));
      } else {
        logger.info("Forking file from git: " + currentGist);
        final GHGist gist = ScriptingEngine.fork(ScriptingEngine.urlToGist(currentGist));
        logger.info("Fork done.");

        parentController.openGistFileInEditor(gist, gist.getFile(selection));
      }
    } catch (Exception e) {
      logger.warning("Could not load script.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  public void loadPage(@Nonnull final String url) {
    lastURL = url;
    webView.getEngine().load(url);
  }

  private void loadGitLocal(@Nonnull final String currentGist, @Nonnull final String file) {
    try {
      final File currentFile = ScriptingEngine.fileFromGit(currentGist, file);
      final boolean isOwner = ScriptingEngine.checkOwner(currentFile);

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
          logger.warning("Could not load asset.\n" + Throwables.getStackTraceAsString(e));
        }
      });
    } catch (GitAPIException | IOException e) {
      logger.warning("Could not parse file from git: " + currentGist + ", " + file + "\n"
          + Throwables.getStackTraceAsString(e));
    }
  }

  public WebEngine getEngine() {
    return webView.getEngine();
  }

}