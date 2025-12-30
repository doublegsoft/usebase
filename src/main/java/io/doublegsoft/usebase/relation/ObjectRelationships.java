package io.doublegsoft.usebase.relation;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;

import java.util.ArrayList;
import java.util.List;

public class ObjectRelationships {

  private final ObjectDefinition object;

  private boolean array = false;

  private final List<Relationship> relationships = new ArrayList<>();

  public ObjectRelationships(ObjectDefinition object) {
    this.object = object;
  }

  public Relationship getRelationship(String targetObjName, String targetAttrName) {
    for (Relationship rel : relationships) {
      if (rel.getTargetAttribute().getName().equals(targetAttrName) &&
          rel.getTargetObject().getName().equals(targetObjName)) {
        return rel;
      }
    }
    return null;
  }

  public Relationship getRelationship(String targetAttrName) {
    for (Relationship rel : relationships) {
      if (rel.getTargetAttribute().getName().equals(targetAttrName)) {
        return rel;
      }
    }
    return null;
  }

  public ObjectDefinition getObject() {
    return object;
  }

  public List<Relationship> getRelationships() {
    return relationships;
  }

  public void addRelationship(Relationship attrRel) {
    for (Relationship rel : relationships) {
      if (rel.equals(attrRel)) {
        return;
      }
    }
    relationships.add(attrRel);
  }

  public boolean isArray() {
    return array;
  }

  public void setArray(boolean array) {
    this.array = array;
  }
}
