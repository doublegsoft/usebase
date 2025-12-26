package io.doublegsoft.usebase.relation;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;

import java.util.ArrayList;
import java.util.List;

public class Relationships {

  private final AttributeDefinition attribute;

  private final List<Relationship> relations = new ArrayList<>();

  public Relationships(AttributeDefinition attribute) {
    this.attribute = attribute;
  }

  public Relationship getRelation(String targetObjName, String targetAttrName) {
    for (Relationship rel : relations) {
      if (rel.getTargetAttribute().getName().equals(targetAttrName) &&
          rel.getTargetObject().getName().equals(targetObjName)) {
        return rel;
      }
    }
    return null;
  }

  public Relationship getRelation(String targetAttrName) {
    for (Relationship rel : relations) {
      if (rel.getTargetAttribute().getName().equals(targetAttrName)) {
        return rel;
      }
    }
    return null;
  }

  public AttributeDefinition getAttribute() {
    return attribute;
  }

  public List<Relationship> getRelations() {
    return relations;
  }

  public void addRelation(Relationship attrRel) {
    for (Relationship rel : relations) {
      if (rel.equals(attrRel)) {
        return;
      }
    }
    relations.add(attrRel);
  }
}
