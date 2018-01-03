package com.neuronrobotics.bowlerbuilder.controller.scripteditor.scriptrunner;

import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.groovy.GroovyLanguage;
import com.neuronrobotics.bowlerstudio.scripting.IScriptingLanguage;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import java.util.ArrayList;
import javafx.beans.property.ReadOnlyBooleanProperty;

/**
 * {@link ScriptRunner} passthrough to {@link ScriptingEngine}.
 */
public class GroovyScriptRunner implements ScriptRunner {

  private final GroovyLanguage language;

  @Inject
  public GroovyScriptRunner(GroovyLanguage language) {
    this.language = language;

    ScriptingEngine.addScriptingLanguage(language);
  }

  @Override
  public Object runScript(String script, ArrayList<Object> arguments, String languageName)
      throws Exception {
    return ScriptingEngine.inlineScriptStringRun(script, arguments, languageName);
  }

  @Override
  public IScriptingLanguage getLanguage() {
    return language;
  }

  @Override
  public boolean isScriptCompiling() {
    return language.compilingProperty().getValue();
  }

  @Override
  public ReadOnlyBooleanProperty scriptCompilingProperty() {
    return language.compilingProperty();
  }

  @Override
  public boolean isScriptRunning() {
    return language.runningProperty().getValue();
  }

  @Override
  public ReadOnlyBooleanProperty scriptRunningProperty() {
    return language.runningProperty();
  }

}
