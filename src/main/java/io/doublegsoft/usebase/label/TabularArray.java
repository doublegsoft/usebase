package io.doublegsoft.usebase.label;

import com.doublegsoft.jcommons.metabean.ObjectDefinition;

import java.util.ArrayList;
import java.util.List;

public class TabularArray {

  private String mainVariable;

  private ObjectDefinition mainObject;

  private final List<CompoundCondition> compoundConditions = new ArrayList<>();

  /**
   * Returns a flat list containing every {@link ObjectDefinition} that participates in the
   * current rule/condition set.
   *
   * <p>The list is built in the following order:
   *
   * <ol>
   *   <li>The {@code primaryObject} – the main object that the rule is anchored to.</li>
   *   <li>All “right‑hand” objects that appear in any {@link JoinCondition}
   *       belonging to the {@link CompoundCondition}s stored in {@code compoundConditions}.</li>
   * </ol>
   *
   * <p>Only non‑null {@code rightObject}s are added; if a {@code JoinCondition} has a
   * {@code null} right object it is simply ignored.  The method never returns {@code null}
   * – an empty {@link ArrayList} is returned when there is no primary object and no join
   * conditions.
   *
   * <p><b>Thread‑safety</b>: The method does not perform any synchronization.  If the
   * underlying collections ({@code compoundConditions}, {@code primaryObject}) can be
   * modified concurrently, callers must ensure external synchronization (e.g. by using
   * {@code synchronized} blocks or concurrent collections).  Otherwise the returned list
   * reflects a snapshot of the data at the moment of invocation.
   *
   * @return a mutable {@link List} of all involved {@link ObjectDefinition}s.
   *         The caller may modify the returned list without affecting the internal
   *         state of this class.
   */
  public List<ObjectDefinition> getObjects() {
    List<ObjectDefinition> retVal = new ArrayList<>();
    retVal.add(mainObject);
    for (CompoundCondition compound : compoundConditions) {
      for (JoinCondition cond : compound.getJoinConditions()) {
        if (cond.getRightObject() != null) {
          retVal.add(cond.getRightObject());
        }
      }
    }
    return retVal;
  }

  /**
   * 收集当前对象（可能是一个查询或过滤器）所涉及的全部变量名。
   *
   * <p>返回的列表顺序如下：
   * <ol>
   *   <li>首先放入 {@code mainVariable} —— 代表“主变量”。</li>
   *   <li>随后遍历所有 {@link CompoundCondition} → {@link JoinCondition}，
   *       将每个非空的右侧变量（{@code cond.getRightVariable()}）加入列表。</li>
   * </ol>
   *
   * @return 包含主变量以及所有右侧变量的 {@link List}&lt;String&gt;，顺序保持遍历顺序。
   */
  public List<String> getVariables() {
    List<String> retVal = new ArrayList<>();
    retVal.add(mainVariable);
    for (CompoundCondition compound : compoundConditions) {
      for (JoinCondition cond : compound.getJoinConditions()) {
        if (cond.getRightVariable() != null) {
          retVal.add(cond.getRightVariable());
        }
      }
    }
    return retVal;
  }

  /**
   * Returns the {@link ObjectDefinition} that corresponds to the supplied
   * {@code objName}.
   *
   * <p>The lookup proceeds in the following order:
   *
   * <ol>
   *   <li>If {@code objName} equals the {@code primaryVariable} of the rule,
   *       {@code primaryObject} is returned.</li>
   *   <li>If {@code objName} equals the name of {@code primaryObject},
   *       {@code primaryObject} is returned as well.</li>
   *   <li>Otherwise the method iterates over every {@link CompoundCondition}
   *       and its {@link JoinCondition}s.  For each join condition it checks
   *       <ul>
   *         <li>that the right‑hand side object is not {@code null}, and</li>
   *         <li>whether {@code objName} matches either the right‑hand object's
   *             name or the right‑hand variable name.</li>
   *       </ul>
   *       The first match encountered is returned.</li>
   *   <li>If no match is found, {@code null} is returned.</li>
   * </ol>
   *
   * <p><b>Note on null handling</b>: The method assumes that {@code objName}
   * is not {@code null}.  If a {@code null} value is passed a
   * {@link NullPointerException} will be thrown by the first call to
   * {@code objName.equals(...)}.  Callers should validate the argument
   * beforehand (or the method could be changed to explicitly check for null).
   *
   * <p><b>Performance</b>: The search is a linear scan over all join
   * conditions, i.e. {@code O(n)} where {@code n} is the total number of
   * {@link JoinCondition}s in the rule.  If look‑ups become a hotspot you may
   * consider caching the mappings in a {@code Map<String, ObjectDefinition>}
   * for constant‑time retrieval.
   *
   * <p><b>Thread‑safety</b>: No synchronization is performed.  If the
   * underlying collections {@code compoundConditions}, the {@code primaryObject}
   * or any {@link JoinCondition} can be modified concurrently, the caller
   * must provide external synchronization (e.g. by synchronising on the enclosing
   * rule object).  The method itself returns a reference to the stored
   * {@link ObjectDefinition} objects; it does not create a defensive copy.
   *
   * @param objName the exact name (or variable name) of the object to retrieve.
   *                Must not be {@code null}.
   * @return the matching {@link ObjectDefinition}, or {@code null} if no such
   *         object exists in the current rule.
   */
  public ObjectDefinition getObject(String objName) {
    if (objName.equals(mainVariable)) {
      return mainObject;
    } else if (objName.equals(mainObject.getName())) {
      return mainObject;
    }
    for (CompoundCondition compound : compoundConditions) {
      for (JoinCondition cond : compound.getJoinConditions()) {
        if (cond.getRightObject() != null &&
            (objName.equals(cond.getRightObject().getName()) || objName.equals(cond.getRightVariable()))) {
          return cond.getRightObject();
        }
      }
    }
    return null;
  }

  public String getMainVariable() {
    return mainVariable;
  }

  public void setMainVariable(String mainVariable) {
    this.mainVariable = mainVariable;
  }

  public ObjectDefinition getMainObject() {
    return mainObject;
  }

  public void setMainObject(ObjectDefinition mainObject) {
    this.mainObject = mainObject;
  }

  public List<CompoundCondition> getCompoundConditions() {
    return compoundConditions;
  }

  public boolean hasMoreArrays() {
    return !compoundConditions.isEmpty();
  }
}
