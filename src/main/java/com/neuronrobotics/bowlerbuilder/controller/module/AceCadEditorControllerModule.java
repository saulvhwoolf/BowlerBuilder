package com.neuronrobotics.bowlerbuilder.controller.module;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.JavaLanguage;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditorView;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.groovy.GroovyLanguage;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.scriptrunner.GroovyScriptRunner;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.scriptrunner.JavaScriptRunner;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.scriptrunner.ScriptRunner;
import java.util.HashMap;
import java.util.Map;

public class AceCadEditorControllerModule extends AbstractModule {

  private final ScriptEditorView scriptEditorView;

  public AceCadEditorControllerModule(ScriptEditorView scriptEditorView) {
    this.scriptEditorView = scriptEditorView;
  }

  @Override
  protected void configure() {
    bind(ScriptEditorView.class).toInstance(scriptEditorView);

    bind(new TypeLiteral<Map<String, ScriptRunner>>() {
    }).toProvider(() -> {
      Map<String, ScriptRunner> languageMappings = new HashMap<>();
      languageMappings.put("Groovy", new GroovyScriptRunner(new GroovyLanguage()));
      languageMappings.put("Java", new JavaScriptRunner(new JavaLanguage()));
      return languageMappings;
    });
  }

}
