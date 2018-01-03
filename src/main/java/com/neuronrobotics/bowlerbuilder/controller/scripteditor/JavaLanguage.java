package com.neuronrobotics.bowlerbuilder.controller.scripteditor;

import com.neuronrobotics.bowlerstudio.scripting.GroovyHelper;
import com.neuronrobotics.bowlerstudio.scripting.IScriptingLanguage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import net.openhft.compiler.CachedCompiler;

/**
 * Simple copy of {@link GroovyHelper} that keeps a flag for when it is compiling or running.
 */
public class JavaLanguage implements IScriptingLanguage {

  private final BooleanProperty compilingProperty;
  private final BooleanProperty runningProperty;

  public JavaLanguage() {
    compilingProperty = new SimpleBooleanProperty(false);
    runningProperty = new SimpleBooleanProperty(false);
  }

  @Override
  public String getShellType() {
    return "BowlerJava";
  }

  @Override
  public Object inlineScriptRun(File code, ArrayList<Object> args) throws Exception {
    return this.inline(code);
  }

  @Override
  public Object inlineScriptRun(String code, ArrayList<Object> args) throws Exception {
    return this.inline(code);
  }

  @Override
  public boolean getIsTextFile() {
    return true;
  }

  @Override
  public ArrayList<String> getFileExtenetion() {
    return new ArrayList<>(Collections.singletonList("java"));
  }

  private Object inline(Object code) throws Exception {
    if (code instanceof String) {
      try {
        compilingProperty.setValue(true);
        CachedCompiler cachedCompiler = new CachedCompiler(null, null);
        Callable<Object> runner = (Callable<Object>) cachedCompiler
            .loadFromJava("BowlerMain", (String) code)
            .newInstance();
        compilingProperty.setValue(false);

        runningProperty.setValue(true);
        Object result = runner.call();
        runningProperty.setValue(false);

        cachedCompiler.close();

        return result;
      } finally {
        compilingProperty.setValue(false);
        runningProperty.setValue(false);
      }
    }

    return null;
  }

  public ReadOnlyBooleanProperty compilingProperty() {
    return compilingProperty;
  }

  public ReadOnlyBooleanProperty runningProperty() {
    return runningProperty;
  }

}
