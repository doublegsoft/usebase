/*
** ████████████████████████████████████████████
** █▄─██─▄█─▄▄▄▄█▄─▄▄─█▄─▄─▀██▀▄─██─▄▄▄▄█▄─▄▄─█
** ██─██─██▄▄▄▄─██─▄█▀██─▄─▀██─▀─██▄▄▄▄─██─▄█▀█
** ▀▀▄▄▄▄▀▀▄▄▄▄▄▀▄▄▄▄▄▀▄▄▄▄▀▀▄▄▀▄▄▀▄▄▄▄▄▀▄▄▄▄▄▀
*/
package io.doublegsoft.usebase.grammar;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.type.CollectionType;
import com.doublegsoft.jcommons.metabean.type.PrimitiveType;
import com.doublegsoft.jcommons.metamodel.*;
import com.doublegsoft.jcommons.metamodel.query.ConditionDefinition;
import io.doublegsoft.usebase.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class ComplexTest extends TestBase {

  /**
   * 奖励某支球队的队员。
   * <p>
   * <ul>
   *   <li>队员年龄小于20，球员评分加10</li>
   *   <li>队员年龄大于等于20，加5</li>
   * </ul>
   */
  @Test
  public void test_01() throws Exception {
    String expr =
        "@reward(team_id)\n" +
        "  |&| players = [{player} <team_player> {team}]#(team_id)\n" +
        "  |*| player in players \n" +
        "  |*|?| player.age < 20 \n" +
        "  |*|?|:| player.rating += 10\n" +
        "  |*|?| player.age >= 20 \n" +
        "  |*|?|:| player.rating += 5\n" +
        "  |*|=| player{player} ";
    Usebase usebase = new Usebase(build_data_model_01());
    UsecaseDefinition usecase = usebase.parse(expr).get(0);
    Assert.assertEquals("reward", usecase.getName());
    ParameterizedObjectDefinition params = usecase.getParameterizedObject();
    Assert.assertEquals("team_id", params.getAttributes()[0].getName());
    Assert.assertEquals(2, usecase.getStatements().size());

    AssignmentDefinition assign = (AssignmentDefinition) usecase.getStatements().get(0);
    VariableDefinition assignee = assign.getAssignee();
    Assert.assertEquals("players", assignee.getName());
    Assert.assertTrue(assignee.getType().isCollection());

    LoopDefinition loop = (LoopDefinition) usecase.getStatements().get(1);
    Assert.assertEquals(3, loop.getStatements().size());
    Assert.assertEquals("player", loop.getItemVar().getName());
    Assert.assertEquals("players", loop.getArrayVar().getName());
    Assert.assertEquals("player", loop.getItemVar().getType().getName());

    ComparisonDefinition firstIfInLoop = (ComparisonDefinition) loop.getStatements().get(0);
    Assert.assertEquals(1, firstIfInLoop.getStatements().size());
    Assert.assertEquals("age", firstIfInLoop.getComparand().getAttribute().getName());

    assign = (AssignmentDefinition) firstIfInLoop.getStatements().get(0);
    Assert.assertEquals("rating", assign.getAssignee().getAttribute().getName());
    Assert.assertEquals("+=", assign.getAssignOp());
    Assert.assertEquals(new BigDecimal("10"), assign.getValue().getNumber());
  }

  /**
   * 医疗报销。
   * <p>
   * <ul>
   *   <li>患者是普通百姓并且费用大于5000</li>
   * </ul>
   */
  @Test
  public void test_02() throws Exception {
    String expr =
        "@reimburse(patient_id, amount):{reimburse}\n" +
        "  |&| patient = {patient}#(patient_id) \n" +
        "  |?| patient.type == '普通百姓' and amount > 5000  \n" +
        "  |?|:| amount *= 0.6 \n";
    Usebase usebase = new Usebase(build_data_model_02());
    UsecaseDefinition usecase = usebase.parse(expr).get(0);
    Assert.assertEquals("reimburse", usecase.getName());
    ParameterizedObjectDefinition params = usecase.getParameterizedObject();
    Assert.assertEquals("patient_id", params.getAttributes()[0].getName());
    Assert.assertEquals("amount", params.getAttributes()[1].getName());
    Assert.assertEquals(2, usecase.getStatements().size());

    ComparisonDefinition cmp = (ComparisonDefinition) usecase.getStatements().get(1);
    Assert.assertEquals("type", cmp.getAndComparisons().get(0).getComparand().getAttribute().getName());
    Assert.assertEquals("==", cmp.getAndComparisons().get(0).getComparator());
    Assert.assertEquals("普通百姓", cmp.getAndComparisons().get(0).getValue().getString());
    Assert.assertEquals("amount", cmp.getAndComparisons().get(1).getComparand().getName());
    Assert.assertEquals(">", cmp.getAndComparisons().get(1).getComparator());
    Assert.assertEquals(new BigDecimal("5000"), cmp.getAndComparisons().get(1).getValue().getNumber());

    AssignmentDefinition assign = (AssignmentDefinition) cmp.getStatements().get(0);
    Assert.assertEquals("amount", assign.getAssignee().getName());
    Assert.assertEquals("*=", assign.getAssignOp());
    Assert.assertEquals(new BigDecimal("0.6"), assign.getValue().getNumber());
  }

  private ModelDefinition build_data_model_01() {
    ModelDefinition retVal = new ModelDefinition();

    ObjectDefinition teamObj = createPersistentObject(retVal, "team");
    createIdentifiableAttribute(teamObj, "id", new PrimitiveType("long"));
    createAttributeWithPrimitiveType(teamObj, "name", new PrimitiveType("string"));

    ObjectDefinition playerObj = createPersistentObject(retVal, "player");
    createIdentifiableAttribute(playerObj, "id", new PrimitiveType("long"));
    createAttributeWithPrimitiveType(playerObj, "name", new PrimitiveType("string"));
    createAttributeWithPrimitiveType(playerObj, "age", new PrimitiveType("int"));
    createAttributeWithPrimitiveType(playerObj, "rating", new PrimitiveType("decimal"));

    ObjectDefinition teamPlayerObj = createPersistentObject(retVal, "team_player");
    createIdentifiableAttribute(teamPlayerObj, "id", new PrimitiveType("long"));
    createAttributeWithCustomType(teamPlayerObj, "team", teamObj);
    createAttributeWithCustomType(teamPlayerObj, "player", playerObj);

    return retVal;
  }

  private ModelDefinition build_data_model_02() {
    ModelDefinition retVal = new ModelDefinition();

    // 1. 患者定义 (patient)
    ObjectDefinition patientObj = createPersistentObject(retVal, "patient");
    createIdentifiableAttribute(patientObj, "id", new PrimitiveType("long"));
    createAttributeWithPrimitiveType(patientObj, "name", new PrimitiveType("string"));
    // 对应: patient.type == '普通百姓'
    createAttributeWithPrimitiveType(patientObj, "type", new PrimitiveType("string"));

    // 2. 报销单/报销结果定义 (reimburse) - 对应返回值 :{reimburse}
    ObjectDefinition reimburseObj = createPersistentObject(retVal, "reimburse");
    createIdentifiableAttribute(reimburseObj, "id", new PrimitiveType("long"));
    createAttributeWithCustomType(reimburseObj, "patient", patientObj);
    createAttributeWithPrimitiveType(reimburseObj, "amount", new PrimitiveType("decimal"));

    return retVal;
  }

}
