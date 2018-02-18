package com.neuronrobotics.bowlerbuilder.controller;

import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.controlsfx.glyphfont.Glyph;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class WebBrowserController {

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

  private String currentGit;


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
//    startStopAction();
    FxUtil.runFX(() -> {
      String fileName = fileBox.getSelectionModel().getSelectedItem();

      try {
        File currentFile = ScriptingEngine.fileFromGit(currentGit, fileName);
        String name = currentFile.getName();
        Object obj = ScriptingEngine.inlineScriptRun(currentFile, null, ScriptingEngine.getShellType(name));


      } catch (InvalidRemoteException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (TransportException e) {
        e.printStackTrace();
      } catch (GitAPIException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }

//
//        List<String> args;
//
//        ScriptingEngine.inlineScriptRun(currentFile, args, "");

    });


  }

  @FXML
  private void onModify(ActionEvent actionEvent) {

  }


  public void loadPage(final String url) {
    webView.getEngine().load(url);

    FxUtil.runFX(() -> {

      runButton.setDisable(true);
      modifyButton.setDisable(true);
      fileBox.getItems().clear();

      List<String> gists = ScriptingEngine.getCurrentGist(url, webView.getEngine());

      String currentGist;
      String address = url;

      if (!gists.isEmpty()) {
        currentGist = gists.get(0);
        currentGit = "https://gist.github.com/" + currentGist + ".git";

      } else if (address.contains("https://github.com/")) {

        if (address.endsWith("/")) {
          address = address.substring(0, address.length() - 1);
        }
        currentGit = address + ".git";

      } else {
        return;
      }
      try {
        List<String> files = ScriptingEngine.filesInGit(currentGit).stream()
            .filter(item -> !item.contains("csgDatabase.json"))
            .collect(Collectors.toList());

        if (!files.isEmpty()) {
          loadGitLocal(currentGit, files.get(0));
          runButton.setDisable(false);
          modifyButton.setDisable(false);

        }

        files.forEach(file -> fileBox.getItems().add(file));

      } catch (Exception e) {
        e.printStackTrace();
      }

    });
  }

  private void loadGitLocal(String currentGit, String file) {
    try {
      String[] code = ScriptingEngine.codeFromGit(currentGit, file);

      if (code != null) {
        File currentFile = ScriptingEngine.fileFromGit(currentGit, file);

        boolean isOwnedByUser = ScriptingEngine.checkOwner(currentFile);
        FxUtil.runFX(() -> {
          if (isOwnedByUser) {
            modifyButton.setText("Edit...");
            modifyButton.setGraphic(AssetFactory.loadIcon("Edit-Script.png"));
          } else {
            modifyButton.setText("Make a Copy");
            modifyButton.setGraphic(AssetFactory.loadIcon("Make-Copy-Script.png"));
          }
          try {
            runIcon.setImage(AssetFactory.loadAsset("Script-Tab-" +
                ScriptingEngine.getShellType(currentFile.getName() + ".png")));
          } catch (Exception e) {
            e.printStackTrace();
          }

        });


      }


    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public WebEngine getEngine() {
    return webView.getEngine();
  }

}
