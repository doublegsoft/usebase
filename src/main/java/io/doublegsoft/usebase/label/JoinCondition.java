package io.doublegsoft.usebase.label;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;

public class JoinCondition {

  private ObjectDefinition leftObject;

  private AttributeDefinition leftAttribute;

  private ObjectDefinition rightObject;

  private AttributeDefinition rightAttribute;

  private String rightVariable;

  private String constantType;

  private String constantValue;

  public ObjectDefinition getLeftObject() {
    return leftObject;
  }

  public void setLeftObject(ObjectDefinition leftObject) {
    this.leftObject = leftObject;
  }

  public AttributeDefinition getLeftAttribute() {
    return leftAttribute;
  }

  public void setLeftAttribute(AttributeDefinition leftAttribute) {
    this.leftAttribute = leftAttribute;
  }

  public ObjectDefinition getRightObject() {
    return rightObject;
  }

  public void setRightObject(ObjectDefinition rightObject) {
    this.rightObject = rightObject;
  }

  public AttributeDefinition getRightAttribute() {
    return rightAttribute;
  }

  public void setRightAttribute(AttributeDefinition rightAttribute) {
    this.rightAttribute = rightAttribute;
  }

  public String getConstantType() {
    return constantType;
  }

  public void setConstantType(String constantType) {
    this.constantType = constantType;
  }

  public String getConstantValue() {
    return constantValue;
  }

  public void setConstantValue(String constantValue) {
    this.constantValue = constantValue;
  }

  public String getRightVariable() {
    return rightVariable;
  }

  public void setRightVariable(String rightVariable) {
    this.rightVariable = rightVariable;
  }
}
