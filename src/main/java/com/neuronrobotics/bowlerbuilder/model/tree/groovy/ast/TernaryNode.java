package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class TernaryNode extends ASTNode {

  private String expression;

  public TernaryNode(String expression) {
    super(ASTNodeType.TernaryNode);
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }

}
