package io.doublegsoft.usebase.projection;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.type.PrimitiveType;
import io.doublegsoft.usebase.modelbase.ModelbaseHelper;
import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ProjectionBuilder {

  public static final ModelDefinition DUMMY = new ModelDefinition();

  private final ModelDefinition dataModel;

  public ProjectionBuilder(ModelDefinition dataModel) {
    this.dataModel = dataModel;
  }

  public ObjectDefinition build(ObjectDefinition obj) {
    return build(obj, Collections.emptySet());
  }

  public ObjectDefinition build(ObjectDefinition obj, Set<String> exclusions) {
    ObjectDefinition retVal = new ObjectDefinition(obj.getName() + "_info", DUMMY);
    build(null, obj, exclusions, 0, retVal);
    return retVal;
  }

  private List<AttributeDefinition> build(AttributeDefinition attrRef, ObjectDefinition obj,
                                          Set<String> exclusions, int level,
                                          ObjectDefinition owner) {
    List<AttributeDefinition> retVal = new ArrayList<>();
    if (exclusions.contains(obj.getName())) {
      return retVal;
    }
    // 避免cycle引用，检查
    int objLevel = 0;
    String objName = null;
    for (AttributeDefinition attr : owner.getAttributes()) {
      String origObjName = attr.getLabelledOption("original", "object");
      if (objName == null) {
        objName = origObjName;
      }
      if (!objName.equals(origObjName)) {
        objLevel++;
        objName = origObjName;
      }
      if (obj.getName().equals(objName) && objLevel < level) {
        return retVal;
      }
    }
    // 第一，基础属性
    for (AttributeDefinition attr : obj.getAttributes()) {
      if (attr.getType().isCollection() || ModelbaseHelper.isSystemAttribute(attr) ||
          attr.getType().isCustom() || (attr.isIdentifiable() && attrRef != null)) {
        continue;
      }
      String attrname = attr.getName();
      if (attr.isIdentifiable()) {
        attrname = attr.getParent().getName() + "_id";
      }
      if ("name".equals(attr.getName())) {
        attrname = attr.getParent().getName() + "_name";
      }
      if ("type".equals(attr.getName())) {
        attrname = attr.getParent().getName() + "_type";
      }
      if (owner.getAttribute(attrname) == null) {
        ModelbaseHelper.cloneAttribute(attrname, attr, owner);
      }
      retVal.add(attr);
    }
    // 第二，引用属性
    for (AttributeDefinition attr : obj.getAttributes()) {
      if (!attr.getType().isCustom()) {
        continue;
      }
      // 直接添加引用属性
      String attrname = attr.getName();
      ObjectDefinition refObj = dataModel.findObjectByName(attr.getType().getName());
      if (attrname.equals(refObj.getName())) {
        attrname = refObj.getName() + "_id";
      } else if (attrname.endsWith("_" + refObj.getName())) {
        attrname = attrname + "_id";
      } else if (attrname.startsWith(refObj.getName() + "_")) {
        attrname = attrname + "_id";
      }

      if (owner.getAttribute(attrname) == null) {
        AttributeDefinition cloningAttr = ModelbaseHelper.cloneAttribute(attrname, attr, owner);
        PrimitiveType primType = ModelbaseHelper.getPrimitiveType(attr);
        cloningAttr.setType(primType);
      }
      //
      List<AttributeDefinition> attrs = build(attr, refObj, exclusions, level + 1, owner);
      retVal.addAll(attrs);
    }
    return retVal;
  }
}
