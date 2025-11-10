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
    AttributeDefinition retVal = owner.getModel().findAttributeByNames(owner.getName(), original.getName());
    if (retVal != null) {
      return retVal;
    }
    retVal = new AttributeDefinition(original.getName(), owner);
    retVal.setUnit(original.getUnit());
    retVal.setAlias(original.getAlias());
    retVal.setType(original.getType());
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

  public static AttributeDefinition cloneAttribute(String name, ObjectDefinition owner, ModelDefinition dataModel) {
    String[] strs = name.split("\\.");
    if (strs.length == 1) {
      throw new IllegalArgumentException("there is no object name defined in name: " + name);
    }
    if (strs.length > 2) {
      throw new IllegalArgumentException("there is more hierarchical names defined in name: " + name);
    }
    AttributeDefinition attrDef = dataModel.findAttributeByNames(strs[0], strs[1]);
    AttributeDefinition retVal = cloneAttribute(attrDef, owner);
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

  private ModelbaseHelper() {

  }

}
