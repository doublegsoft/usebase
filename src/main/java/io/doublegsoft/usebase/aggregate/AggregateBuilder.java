package io.doublegsoft.usebase.aggregate;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.type.CollectionType;

import java.util.HashMap;
import java.util.Map;

public class AggregateBuilder {

  private final ModelDefinition dataModel;

  private final Map<String, Map<String, ObjectDefinition>> attrRefObjs = new HashMap<>();

  public AggregateBuilder(ModelDefinition dataModel) {
    this.dataModel = dataModel;
  }

  public AggregateRelationshipChain build(ObjectDefinition aggregateObj) {
    AggregateRelationshipChain retVal = new AggregateRelationshipChain(aggregateObj);
    Map<String, ObjectDefinition> objsInRet = new HashMap<>();
    // 返回对象的定义中全部的对象函数
    for (AttributeDefinition attr : aggregateObj.getAttributes()) {
      String origObjName = attr.getLabelledOption("original", "object");
      String conjObjName = attr.getLabelledOption("conjunction", "object");
      // TODO
      String conjSourceObjName = attr.getLabelledOption("conjunction", "source_object");
      String conjSourceAttrName = attr.getLabelledOption("conjunction", "source_attribute");
      String conjTargetObjName = attr.getLabelledOption("conjunction", "target_object");
      String conjTargetAttrName = attr.getLabelledOption("conjunction", "target_attribute");
      if (origObjName != null && !objsInRet.containsKey(origObjName)) {
        ObjectDefinition obj = dataModel.findObjectByName(origObjName);
        if (!objsInRet.containsKey(origObjName)) {
          retVal.addObject(obj);
        }
        objsInRet.put(origObjName, obj);
      } else if (conjObjName != null && !objsInRet.containsKey(conjObjName)) {
        ObjectDefinition obj = dataModel.findObjectByName(conjObjName);
        if (!objsInRet.containsKey(origObjName)) {
          retVal.addObject(obj, true);
        }
        objsInRet.put(conjObjName, obj);
      }
      if (attr.getType().isCollection()) {
        CollectionType collType = (CollectionType) attr.getType();
        ObjectDefinition obj = dataModel.findObjectByName(collType.getComponentType().getName());
        if (!objsInRet.containsKey(origObjName)) {
          retVal.addObject(obj, true);
        }
        objsInRet.put(obj.getName(), obj);
      }
    }
    // 构建相互关联的数据
    for (ObjectDefinition obj : retVal.getObjects()) {
      for (AttributeDefinition attr : obj.getAttributes()) {
        // 这里是直接的
        if (objsInRet.containsKey(attr.getType().getName())) {
          ObjectDefinition refObj = objsInRet.get(attr.getType().getName());
          Relationship rel = new Relationship();
          rel.setSourceAttribute(attr);
          rel.setSourceObject(attr.getParent());
          rel.setTargetObject(refObj);
          rel.setTargetAttribute(refObj.getIdentifiableAttribute());
          retVal.addRelationship(rel);
        }
        // 这里是间接的
        for (ObjectDefinition inObj : objsInRet.values()) {
          if (obj.getName().equals(inObj.getName())) {
            // 自己不跟自己关联
            continue;
          }
          for (AttributeDefinition inObjAttr : inObj.getAttributes()) {
            if (!inObjAttr.isIdentifiable() && !attr.isIdentifiable()) {
              // 关联关系的确认，必须确保至少一方是主键定义
              continue;
            }
            if (inObjAttr.getType().isCustom() && attr.getType().isCustom() &&
                inObjAttr.getType().getName().equals(attr.getType().getName())) {
              Relationship rel = new Relationship();
              rel.setSourceAttribute(attr);
              rel.setSourceObject(attr.getParent());
              rel.setTargetObject(inObj);
              rel.setTargetAttribute(inObjAttr);
              retVal.addRelationship(rel);
            }
          }
        }
      }
    }
    return retVal;
  }
}
