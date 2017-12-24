package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class PrefixNode extends ASTNode {

  private String expression;

  public PrefixNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}