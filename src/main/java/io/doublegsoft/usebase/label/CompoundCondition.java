package io.doublegsoft.usebase.label;

import java.util.ArrayList;
import java.util.List;

public class CompoundCondition {

  private final List<JoinCondition> joinConditions = new ArrayList<>();

  public List<JoinCondition> getJoinConditions() {
    return joinConditions;
  }
}
