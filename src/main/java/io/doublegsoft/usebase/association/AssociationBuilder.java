package io.doublegsoft.usebase.association;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.type.CollectionType;
import com.doublegsoft.jcommons.metamodel.ParameterizedObjectDefinition;
import io.doublegsoft.usebase.modelbase.ModelbaseHelper;

import java.util.*;

public class AssociationBuilder {

  private final ModelDefinition dataModel;

  public AssociationBuilder(ModelDefinition dataModel) {
    this.dataModel = dataModel;
  }

  public AssociationChain build(ObjectDefinition paramObj, ObjectDefinition retObj) {
    AssociationChain retVal = new AssociationChain();
    Set<ObjectDefinition> allowedObjs = new HashSet<>();
    Map<ObjectDefinition, Set<ObjectDefinition>> graph = new HashMap<>();
    for (ObjectDefinition obj : dataModel.getObjects()) {
      Set<ObjectDefinition> refs = new HashSet<>();
      for (AttributeDefinition attr : obj.getAttributes()) {
        if (attr.getType().isCustom()) {
          ObjectDefinition refObj = dataModel.findObjectByName(attr.getType().getName());
          refs.add(refObj);
        }
      }
      graph.put(obj, refs);
      allowedObjs.add(obj);
    }
    for (ObjectDefinition obj : dataModel.getObjects()) {
      Set<ObjectDefinition> refs = graph.get(obj);
      for (ObjectDefinition innerObj : dataModel.getObjects()) {
        for (AttributeDefinition attr : innerObj.getAttributes()) {
          if (attr.getType().getName().equals(obj.getName())) {
            refs.add(innerObj);
          }
        }
      }
      graph.put(obj, refs);
    }

    Map<String, ObjectDefinition> paramDataObjs = new HashMap<>();
    Map<String, ObjectDefinition> retDataObjs = new HashMap<>();
    ObjectDefinition rootObjInRet = null;
    Set<String> visitedObjNames = new HashSet<>();
    for (AttributeDefinition paramAttr : paramObj.getAttributes()) {
      String originalObjName = paramAttr.getLabelledOption("original", "object");
      ObjectDefinition originalObj = dataModel.findObjectByName(originalObjName);
      paramDataObjs.put(originalObjName, originalObj);
    }
    if (retObj == null) {
      return retVal;
    }
    for (AttributeDefinition retAttr : retObj.getAttributes()) {
      String originalObjName = retAttr.getLabelledOption("original", "object");
      if (retAttr.getType().isCollection()) {
        originalObjName = ((CollectionType)retAttr.getType()).getComponentType().getName();
      }
      if (originalObjName != null && !retDataObjs.containsKey(originalObjName)) {
        ObjectDefinition originalObj = dataModel.findObjectByName(originalObjName);
        retDataObjs.put(originalObjName, originalObj);
        if (rootObjInRet == null) {
          rootObjInRet = originalObj;
        }
      }
    }
    for (ObjectDefinition paramDataObj : paramDataObjs.values()) {
      List<ObjectDefinition> path = findPath(graph, paramDataObj, rootObjInRet, allowedObjs);
      retVal.addAssociatingObjects(path);
    }
    return retVal;
  }

  public static List<ObjectDefinition> findPath(
      Map<ObjectDefinition, Set<ObjectDefinition>> graph,
      ObjectDefinition start,
      ObjectDefinition end,
      Set<ObjectDefinition> allowedTables   // 你“给的一张表集合”
  ) {

    List<ObjectDefinition> path = new ArrayList<>();
    Queue<ObjectDefinition> queue = new LinkedList<>();
    Map<ObjectDefinition, ObjectDefinition> prev = new HashMap<>();
    Set<ObjectDefinition> visited = new HashSet<>();

    if (!allowedTables.contains(start) ||
        !allowedTables.contains(end)) {
      return path;
    }

    queue.offer(start);
    visited.add(start);

    while (!queue.isEmpty()) {
      ObjectDefinition cur = queue.poll();

      if (cur.equals(end)) {
        break;
      }

      for (ObjectDefinition next : graph.getOrDefault(cur, Collections.emptySet())) {
        if (!visited.contains(next) && allowedTables.contains(next)) {
          visited.add(next);
          prev.put(next, cur);
          queue.offer(next);
        }
      }
    }

    if (!visited.contains(end)) {
      return path; // 无路径
    }

    // 回溯路径
    for (ObjectDefinition t = end; t != null; t = prev.get(t)) {
      path.add(t);
    }

    Collections.reverse(path);
    return path;
  }
}
