package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class ForNode extends ASTNode {

  private ASTNode collectionNode;
  private ASTNode loopBlockNode;

  public ForNode(ASTNode collectionNode, ASTNode loopBlockNode) {
    this.collectionNode = collectionNode;
    this.loopBlockNode = loopBlockNode;
  }

  public ASTNode getCollectionNode() {
    return collectionNode;
  }

  public ASTNode getLoopBlockNode() {
    return loopBlockNode;
  }

}