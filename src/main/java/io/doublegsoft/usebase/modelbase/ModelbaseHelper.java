/*
** ██╗░░░██╗░██████╗███████╗██████╗░░█████╗░░██████╗███████╗
** ██║░░░██║██╔════╝██╔════╝██╔══██╗██╔══██╗██╔════╝██╔════╝
** ██║░░░██║╚█████╗░█████╗░░██████╦╝███████║╚█████╗░█████╗░░
** ██║░░░██║░╚═══██╗██╔══╝░░██╔══██╗██╔══██║░╚═══██╗██╔══╝░░
** ╚██████╔╝██████╔╝███████╗██████╦╝██║░░██║██████╔╝███████╗
** ░╚═════╝░╚═════╝░╚══════╝╚═════╝░╚═╝░░╚═╝╚═════╝░╚══════╝
*/
package io.doublegsoft.usebase.modelbase;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.type.CollectionType;
import com.doublegsoft.jcommons.metabean.type.CustomType;
import com.doublegsoft.jcommons.metabean.type.DomainType;
import com.doublegsoft.jcommons.metabean.type.PrimitiveType;
import com.doublegsoft.jcommons.metamodel.JoinConditionDefinition;
import com.doublegsoft.jcommons.metamodel.ParameterizedObjectDefinition;
import com.doublegsoft.jcommons.metamodel.ReturnedObjectDefinition;
import com.doublegsoft.jcommons.metamodel.ValuedAttributeDefinition;
import com.doublegsoft.jcommons.utils.Strings;

import java.util.*;

public final class ModelbaseHelper {

  public static ObjectDefinition findObject(String name, ModelDefinition dataModel) {
    ObjectDefinition objDef = dataModel.findObjectByName(name);
    return objDef;
  }

  public static AttributeDefinition findAttribute(String objName, String attrName, ModelDefinition dataModel) {
    AttributeDefinition retVal = dataModel.findAttributeByNames(objName, attrName);
    return retVal;
  }

  public static AttributeDefinition findAttribute(String attrName, ObjectDefinition obj) {
    for (AttributeDefinition attrDef : obj.getAttributes()) {
      if (attrDef.getName().equals(attrName)) {
        return attrDef;
      }
    }
    for (AttributeDefinition attrDef : obj.getAttributes()) {
      if (attrDef.getType().isCustom()) {
        CustomType type = (CustomType) attrDef.getType();
        if (attrName.equals(type.getObjectDefinition().getName() + "_id")) {
          return attrDef;
        }
      }
    }
    throw new IllegalArgumentException("not found attribute '" + attrName + "' in object '" + obj.getName() + "'");
  }

  /**
   * @see #cloneObject(String, ObjectDefinition, ModelDefinition)
   */
  public static ObjectDefinition cloneObject(ObjectDefinition original, ModelDefinition owner) {
    return cloneObject(original.getName(), original, owner);
  }

  /**
   * Clones original attribute defined in data model to apply in api model.
   *
   * @param newObjName
   *      the new object name in api model
   *
   * @param original
   *      the original attribute of data model
   *
   * @param apiModel
   *      the api model
   *
   * @return the cloned attribute
   */
  public static ObjectDefinition cloneObject(String newObjName, ObjectDefinition original, ModelDefinition apiModel) {
    ObjectDefinition retVal = apiModel.findObjectByName(newObjName);
    if (retVal != null) {
      return retVal;
    }
    retVal = new ObjectDefinition(original.getName(), apiModel);
    retVal.setAlias(original.getAlias());
    retVal.setPlural(original.getPlural());
    retVal.setModuleName(original.getModuleName());
    retVal.setSingular(original.getSingular());
    retVal.setPersistenceName(original.getPersistenceName());
    retVal.setText(original.getText());
    retVal.setRole(original.getRole());
    // labelled options
    for (Map.Entry<String, Map<String,String>> entry : original.getLabelledOptions().entrySet()) {
      Map<String, String> options = new HashMap<>();
      options.putAll(entry.getValue());
      retVal.setLabelledOptions(entry.getKey(), options);
    }
    retVal.setLabelledOptions("original", addOptions(retVal, "original", "name", original.getName()));
    return retVal;
  }

  /**
   * Clones original attribute defined in data model to apply in api model.
   *
   * @param original
   *      the original attribute of data model
   *
   * @param owner
   *      the owner object
   *
   * @return the cloned attribute
   */
  public static AttributeDefinition cloneAttribute(AttributeDefinition original, ObjectDefinition owner) {
    String attrname = original.getName();
    if (attrname.equals("id") || attrname.equals("name")) {
      attrname = original.getParent().getName() + "_" + attrname;
    }
    return cloneAttribute(attrname, original, owner);
  }

  public static AttributeDefinition cloneAttribute(String compoundName, ObjectDefinition owner, ModelDefinition dataModel) {
    String[] strs = compoundName.split("\\.");
    if (strs.length == 1) {
      throw new IllegalArgumentException("there is no object name defined in name: " + compoundName);
    }
    if (strs.length > 2) {
      throw new IllegalArgumentException("there is more hierarchical names defined in name: " + compoundName);
    }
    AttributeDefinition attrDef = dataModel.findAttributeByNames(strs[0], strs[1]);
    String attrname = attrDef.getName();
    if (attrname.equals("id") || attrname.equals("name")) {
      attrname = attrDef.getParent().getName() + "_" + attrname;
    }
    AttributeDefinition retVal = cloneAttribute(attrname, attrDef, owner);
    return retVal;
  }

  public static AttributeDefinition cloneAttribute(String alias, AttributeDefinition original, ObjectDefinition owner) {
//    AttributeDefinition retVal = owner.getModel().findAttributeByNames(owner.getName(), original.getName());
//    if (retVal != null) {
//      return retVal;
//    }
    AttributeDefinition retVal = new ValuedAttributeDefinition(alias, owner);
    retVal.setUnit(original.getUnit());
    retVal.setAlias(original.getAlias());
    retVal.setType(original.getType());
    // FIXME: ACCURATE DOMAIN TYPE EXPRESSION
    retVal.getConstraint().setDomainType(new DomainType(original.getName()));
    retVal.setSingular(original.getSingular());
    retVal.setPlural(original.getPlural());
    retVal.setPersistenceName(original.getPersistenceName());
    retVal.setText(original.getText());
    // constraint
    retVal.getConstraint().setNullable(original.getConstraint().isNullable());
    retVal.getConstraint().setIdentifiable(original.getConstraint().isIdentifiable());
    retVal.getConstraint().setDomainType(original.getConstraint().getDomainType());
    retVal.getConstraint().setDefaultValue(original.getConstraint().getDefaultValue());
    // labelled options
    for (Map.Entry<String, Map<String,String>> entry : original.getLabelledOptions().entrySet()) {
      Map<String, String> options = new HashMap<>();
      options.putAll(entry.getValue());
      retVal.setLabelledOptions(entry.getKey(), options);
    }
    addOptions(retVal, "original", "object", original.getParent().getName());
    addOptions(retVal, "original", "attribute", original.getName());
    return retVal;
  }

  public static ObjectDefinition cloneObject(String name, ModelDefinition owner, ModelDefinition dataModel) {
    ObjectDefinition objDef = dataModel.findObjectByName(name);
    ObjectDefinition retVal = cloneObject(objDef, owner);
    for (AttributeDefinition attrDef : objDef.getAttributes()) {
      if (isSystemAttribute(attrDef) ||
          isCollectionAttribute(attrDef)) {
        continue;
      }
      cloneAttribute(attrDef, retVal);
    }
    return retVal;
  }

  public static List<AttributeDefinition> cloneAttributes(List<AttributeDefinition> originalAttrs, ObjectDefinition owner) {
    List<AttributeDefinition> retVal = new ArrayList<>();
    for (AttributeDefinition originalAttr :  originalAttrs) {
      retVal.add(cloneAttribute(originalAttr, owner));
    }
    return retVal;
  }

  public static Map<String, String> addOptions(ObjectDefinition obj, String label, String key, String value) {
    Map<String, String> retVal = obj.getLabelledOptions(label);
    if (retVal == null) {
      retVal = new HashMap<>();
      obj.setLabelledOptions(label, retVal);
    }
    try {
      retVal.put(key, value);
    } catch (Throwable cause) {
      Map<String, String> opts = retVal;
      retVal = new HashMap<>();
      retVal.putAll(opts);
      retVal.put(key, value);
      obj.setLabelledOptions(label, retVal);
    }
    return retVal;
  }

  public static Map<String, String> addOptions(AttributeDefinition attr, String label, String key, String value) {
    Map<String, String> retVal = attr.getLabelledOptions(label);
    if (retVal == null) {
      retVal = new HashMap<>();
      attr.setLabelledOptions(label, retVal);
    }
    try {
      retVal.put(key, value);
    } catch (Throwable cause) {
      Map<String, String> opts = retVal;
      retVal = new HashMap<>();
      retVal.putAll(opts);
      retVal.put(key, value);
      attr.setLabelledOptions(label, retVal);
    }
    return retVal;
  }

  public static boolean isSystemAttribute(AttributeDefinition attrDef) {
    if ("created_time".equals(attrDef.getName()) ||
        "state".equals(attrDef.getName()) ||
        "last_modified_time".equals(attrDef.getName()) ||
        "modifier_id".equals(attrDef.getName()) ||
        "modifier_type".equals(attrDef.getName())) {
      return true;
    }
    return false;
  }

  public static boolean isCollectionAttribute(AttributeDefinition attrDef) {
    return attrDef.getType() instanceof CollectionType;
  }

  public static boolean isSystemOrExistingInObject(String attrname, ObjectDefinition owner) {
    String objname = owner.getName();
    if (owner.isLabelled("original")) {
      Map<String,String> original = owner.getLabelledOptions("original");
      if (original.containsKey("object")) {
        objname = original.get("object");
      }
    }
    for (AttributeDefinition attr : owner.getAttributes()) {
      if (attr.getName().equals(attrname) || (objname + "_" + attr.getName()).equals(attrname)) {
        return true;
      }
    }
    if ("created_time".equals(attrname) ||
        "modified_time".equals(attrname) ||
        "state".equals(attrname) ||
        "modifier_id".equals(attrname) ||
        "last_modified_time".equals(attrname)) {
      return true;
    }
    return false;
  }

  public static PrimitiveType getPrimitiveType(AttributeDefinition attr) {
    if (attr.getType().isCustom()) {
      ObjectDefinition obj = attr.getParent();
      return getPrimitiveType(obj.getIdentifiableAttribute());
    } else if (attr.getType().isPrimitive()) {
      return (PrimitiveType) attr.getType();
    }
    throw new IllegalArgumentException("unsupported attribute type: " + attr.getType().toString());
  }

  public static String getAttributeCompositeName(AttributeDefinition attr) {
    ObjectDefinition parentObj = attr.getParent();
    String objName = parentObj.getName();
    String attrName = attr.getName();
    if (parentObj instanceof ParameterizedObjectDefinition) {
      objName = attr.getLabelledOption("original", "object");
    } else if (parentObj instanceof ReturnedObjectDefinition) {
      objName = attr.getLabelledOption("original", "object");
    }
    if (attrName.equals("id") || attrName.equals("name") || attrName.equals("type")) {
      return objName + "_" + attrName;
    }
    return attrName;
  }

  public static List<JoinConditionDefinition> createJoinConditions(AttributeDefinition anyone,
                                                                   ModelDefinition dataModel) {
    List<JoinConditionDefinition> retVal = new ArrayList<>();
    if (!anyone.isLabelled("conjunction")) {
      return retVal;
    }
    JoinConditionDefinition joinCond = createJoinCondition(anyone.getLabelledOptions("conjunction"), dataModel);
    retVal.add(joinCond);

    for (int i = 1; i < 10; i++) {
      if (!anyone.isLabelled("conjunction_" + i)) {
        return retVal;
      }
      joinCond = createJoinCondition(anyone.getLabelledOptions("conjunction_" + i), dataModel);
      retVal.add(joinCond);
    }
    return retVal;
  }

  public static JoinConditionDefinition createJoinCondition(Map<String, String> anyoneConj,
                                                            ModelDefinition dataModel) {
    String anyoneSourceObjName = anyoneConj.get("source_object");
    String anyoneSourceAttrName = anyoneConj.get("source_attribute");
    String anyoneSourceAlias = anyoneConj.get("source_alias");
    String anyoneTargetObjName = anyoneConj.get("target_object");
    String anyoneTargetAttrName = anyoneConj.get("target_attribute");
    String anyoneTargetAlias = anyoneConj.get("target_alias");
    String value = anyoneConj.get("value");

    ObjectDefinition anyoneObj = dataModel.findObjectByName(anyoneSourceObjName);
    if (anyoneObj == null) {
      throw new IllegalArgumentException("没有在数据模型中找到'" + anyoneSourceObjName + "'，请检查模型文件。");
    }
    AttributeDefinition anyoneAttr = anyoneObj.getAttribute(anyoneSourceAttrName);
    if (anyoneAttr == null) {
      throw new IllegalArgumentException("没有在'" + anyoneSourceObjName + "'对象中找到'" + anyoneSourceAttrName + "'，请检查模型文件。");
    }
    JoinConditionDefinition retVal = new JoinConditionDefinition();
    retVal.setLeftObjectAlias(anyoneSourceAlias);
    retVal.setLeftObject(anyoneObj);
    retVal.setLeftAttribute(anyoneAttr);
    if (anyoneTargetObjName != null) {
      ObjectDefinition anotherObj = dataModel.findObjectByName(anyoneTargetObjName);
      if (anotherObj == null) {
        throw new IllegalArgumentException("没有在数据模型中找到'" + anyoneTargetObjName + "'，请检查模型文件。");
      }
      AttributeDefinition anotherAttr = anotherObj.getAttribute(anyoneTargetAttrName);
      if (anotherAttr == null) {
        throw new IllegalArgumentException("没有在'" + anyoneTargetObjName + "'对象中找到'" + anyoneTargetAttrName + "'，请检查模型文件。");
      }
      retVal.setRightObjectAlias(anyoneTargetAlias);
      retVal.setRightObject(anotherObj);
      retVal.setRightAttribute(anotherAttr);
    } else if (value != null) {
      retVal.setValue(value);
    } else {
      throw new IllegalArgumentException("在连接的定义中既没有目前对象和属性，也没有常量值的定义。");
    }
    return retVal;
  }

  private ModelbaseHelper() {

  }

}
