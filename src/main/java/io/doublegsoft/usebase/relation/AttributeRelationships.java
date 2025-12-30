package io.doublegsoft.usebase.relation;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;

import java.util.ArrayList;
import java.util.List;

public class AttributeRelationships {

  private final AttributeDefinition attribute;

  private final List<Relationship> relations = new ArrayList<>();

  public AttributeRelationships(AttributeDefinition attribute) {
    this.attribute = attribute;
  }

  public Relationship getRelationship(String targetObjName, String targetAttrName) {
    for (Relationship rel : relations) {
      if (rel.getTargetAttribute().getName().equals(targetAttrName) &&
          rel.getTargetObject().getName().equals(targetObjName)) {
        return rel;
      }
    }
    return null;
  }

  public Relationship getRelationship(String targetAttrName) {
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

  public List<Relationship> getRelationships() {
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
