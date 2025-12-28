package io.doublegsoft.usebase.relation;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregateRelations {

  private final ObjectDefinition object;

  private final Map<String, Relationships> indexedRelations = new HashMap<>();

  public AggregateRelations(ObjectDefinition object) {
    this.object = object;
  }

  public void addRelationship(Relationship rel) {
    Relationships attrRels = indexedRelations.get(rel.toString());
    if (attrRels == null) {
      attrRels = new Relationships(rel.getSourceAttribute());
      indexedRelations.put(rel.toString(), attrRels);
    }
    attrRels.addRelation(rel);
  }

  public Relationship getRelation(String sourceObjName, String targetObjName) {
    for (Relationships rels : indexedRelations.values()) {
      for (Relationship rel : rels.getRelations()) {
        AttributeDefinition sourceAttr = rel.getSourceAttribute();
        AttributeDefinition targetAttr = rel.getTargetAttribute();
        if (sourceAttr.getParent().getName().equals(sourceObjName) &&
            targetAttr.getParent().getName().equals(targetObjName)) {
          return rel;
        }
        if (sourceAttr.getParent().getName().equals(targetObjName) &&
            targetAttr.getParent().getName().equals(sourceObjName)) {
          return rel;
        }
      }
    }
    return null;
  }

  public List<Relationship> getRelationships() {
    List<Relationship> retVal = new ArrayList<>();
    for (Relationships rels : indexedRelations.values()) {
      retVal.addAll(rels.getRelations());
    }
    return retVal;
  }
}
