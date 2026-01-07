package io.doublegsoft.usebase.association;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.type.CollectionType;
import io.doublegsoft.usebase.modelbase.ModelbaseHelper;

import java.util.*;

public class AssociationBuilder {

  private final ModelDefinition dataModel;

  public AssociationBuilder(ModelDefinition dataModel) {
    this.dataModel = dataModel;
  }

  public AssociationChain build(ObjectDefinition paramObj, ObjectDefinition retObj) {
    AssociationChain retVal = new AssociationChain();
    Map<String, ObjectDefinition> paramDataObjs = new HashMap<>();
    Map<String, ObjectDefinition> retDataObjs = new HashMap<>();
    for (AttributeDefinition paramAttr : paramObj.getAttributes()) {
      String originalObjName = paramAttr.getLabelledOption("original", "object");
      ObjectDefinition originalObj = dataModel.findObjectByName(originalObjName);
      paramDataObjs.put(originalObjName, originalObj);
    }
    for (AttributeDefinition retAttr : retObj.getAttributes()) {
      String originalObjName = retAttr.getLabelledOption("original", "object");
      if (retAttr.getType().isCollection()) {
        originalObjName = ((CollectionType)retAttr.getType()).getComponentType().getName();
      }
      if (originalObjName != null && !retDataObjs.containsKey(originalObjName)) {
        ObjectDefinition originalObj = dataModel.findObjectByName(originalObjName);
        retDataObjs.put(originalObjName, originalObj);
      }
    }
    // 判断是否直接关联
    for (AttributeDefinition paramAttr : paramObj.getAttributes()) {
      String groupName = paramAttr.getLabelledOption("group", "name");
      if (groupName != null) {
        retVal.addGroupingAttribute(groupName, paramAttr);
      } else {
        groupName = ModelbaseHelper.getAttributeCompositeName(paramAttr);
        retVal.addGroupingAttribute(groupName, paramAttr);
      }
    }
    for (ObjectDefinition obj : retDataObjs.values()) {
      retVal.addReturnedObject(obj);
    }

    ObjectDefinition assocObj = retVal.getAssociatingObject();
    if (assocObj != null) {
      retVal.addAssociatingObject(assocObj);
    } else {
      List<ObjectDefinition> assocObjs = new ArrayList<>();
      for (ObjectDefinition retDataObj : retDataObjs.values()) {
        if (!assocObjs.isEmpty()) {
          break;
        }
        for (ObjectDefinition paramDataObj : paramDataObjs.values()) {
          searchAssociationRecursively(retDataObj, paramDataObj, assocObjs);
        }
      }
      retVal.addAssociatingObjects(assocObjs);
    }
    return retVal;
  }

  private void searchAssociationRecursively(ObjectDefinition source, ObjectDefinition target, List<ObjectDefinition> associatingObjects) {
    for (AttributeDefinition attr : source.getAttributes()) {
      if (attr.getType().isCustom() && attr.getType().getName().equals(target.getName())) {
        associatingObjects.add(source);
        return;
      }
    }
    if (associatingObjects.isEmpty()) {
      for (AttributeDefinition attr : source.getAttributes()) {
        if (attr.getType().isCustom()) {
          ObjectDefinition refObj = dataModel.findObjectByName(attr.getType().getName());
          searchAssociationRecursively(refObj, target, associatingObjects);
          if (!associatingObjects.isEmpty()) {
            associatingObjects.add(source);
          }
        }
      }
    }
  }
}
