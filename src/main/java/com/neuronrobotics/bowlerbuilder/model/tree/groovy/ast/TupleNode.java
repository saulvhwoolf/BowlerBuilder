package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class TupleNode extends ASTNode {

  private String expression;

  public TupleNode(String expression) {
    super();
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}