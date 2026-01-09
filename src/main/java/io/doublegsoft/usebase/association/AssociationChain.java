package io.doublegsoft.usebase.association;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;

import java.util.*;

public class AssociationChain {

  // 分组化的属性
  private final Map<String, List<AttributeDefinition>> groupingAttributes = new HashMap<>();

  // TODO: 分组化的对象，不同的属性组合可以对应不同的关联对象
  private final Map<String, List<ObjectDefinition>> groupingObjects = new HashMap<>();

  private final Map<String, ObjectDefinition> returnedObjects = new HashMap<>();

  private final List<ObjectDefinition> associatingObjects = new ArrayList<>();

  public void addGroupingAttribute(String groupName, AttributeDefinition attr) {
    List<AttributeDefinition> attrs = groupingAttributes.computeIfAbsent(groupName, k -> new ArrayList<>());
    for (AttributeDefinition existing : attrs) {
      if (existing.getName().equals(attr.getName())) {
        return;
      }
    }
    attrs.add(attr);
  }

  public void addReturnedObject(ObjectDefinition obj) {
    returnedObjects.put(obj.getName(), obj);
  }

  public ObjectDefinition getAssociatingObject() {
    for (List<AttributeDefinition> attrs : groupingAttributes.values()) {
      for (AttributeDefinition attr : attrs) {
        String originalObjName = attr.getLabelledOption("original", "object");
        ObjectDefinition obj = returnedObjects.get(originalObjName);
        if (obj != null) {
          return obj;
        }
      }
    }
    return null;
  }

  public ObjectDefinition getAssociatingObject(String attrName) {
    for (List<AttributeDefinition> attrs : groupingAttributes.values()) {
      for (AttributeDefinition attr : attrs) {
        if (attrName.equals(attr.getName())) {
          String originalObjName = attr.getLabelledOption("original", "object");
          ObjectDefinition obj = returnedObjects.get(originalObjName);
          if (obj != null) {
            return obj;
          }
        }
      }
    }
    return null;
  }

  public List<ObjectDefinition> getAssociatingObjects() {
    return Collections.unmodifiableList(associatingObjects);
  }

  public void addAssociatingObject(ObjectDefinition associatingObj) {
    for (ObjectDefinition obj : associatingObjects) {
      if (obj.getName().equals(associatingObj.getName())) {
        return;
      }
    }
    associatingObjects.add(associatingObj);
  }

  public void addAssociatingObjects(List<ObjectDefinition> associatingObjs) {
    for (ObjectDefinition obj : associatingObjs) {
      addAssociatingObject(obj);
    }
  }

}
