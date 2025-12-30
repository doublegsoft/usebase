package io.doublegsoft.usebase.relation;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;

import java.util.Objects;

public class Relationship {

  private AttributeDefinition sourceAttribute;

  private ObjectDefinition sourceObject;

  private AttributeDefinition targetAttribute;

  private ObjectDefinition targetObject;

  public AttributeDefinition getSourceAttribute() {
    return sourceAttribute;
  }

  public void setSourceAttribute(AttributeDefinition sourceAttribute) {
    this.sourceAttribute = sourceAttribute;
  }

  public ObjectDefinition getSourceObject() {
    return sourceObject;
  }

  public void setSourceObject(ObjectDefinition sourceObject) {
    this.sourceObject = sourceObject;
  }

  public AttributeDefinition getTargetAttribute() {
    return targetAttribute;
  }

  public void setTargetAttribute(AttributeDefinition targetAttribute) {
    this.targetAttribute = targetAttribute;
  }

  public ObjectDefinition getTargetObject() {
    return targetObject;
  }

  public void setTargetObject(ObjectDefinition targetObject) {
    this.targetObject = targetObject;
  }

  public AttributeDefinition getAnotherAttribute(String objName) {
    if (targetObject.getName().equals(objName)) {
      return sourceAttribute;
    }
    return targetAttribute;
  }

  public ObjectDefinition getAnotherObject(String objName) {
    if (targetObject.getName().equals(objName)) {
      return sourceObject;
    }
    return targetObject;
  }

  public AttributeDefinition getSelfAttribute(String objName) {
    if (targetObject.getName().equals(objName)) {
      return targetAttribute;
    }
    return sourceAttribute;
  }

  public ObjectDefinition getSelfObject(String objName) {
    if (targetObject.getName().equals(objName)) {
      return targetObject;
    }
    return sourceObject;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Relationship that = (Relationship) o;
    return Objects.equals(sourceAttribute, that.sourceAttribute) && Objects.equals(targetAttribute, that.targetAttribute);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceAttribute, targetAttribute);
  }

  @Override
  public String toString() {
    return sourceObject.getName() + "." + sourceAttribute.getName() + " <<>> " +
        targetObject.getName() + "." + targetAttribute.getName();
  }
}
