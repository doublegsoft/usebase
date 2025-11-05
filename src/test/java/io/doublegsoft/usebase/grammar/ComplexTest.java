/*
** ████████████████████████████████████████████
** █▄─██─▄█─▄▄▄▄█▄─▄▄─█▄─▄─▀██▀▄─██─▄▄▄▄█▄─▄▄─█
** ██─██─██▄▄▄▄─██─▄█▀██─▄─▀██─▀─██▄▄▄▄─██─▄█▀█
** ▀▀▄▄▄▄▀▀▄▄▄▄▄▀▄▄▄▄▄▀▄▄▄▄▀▀▄▄▀▄▄▀▄▄▄▄▄▀▄▄▄▄▄▀
*/
package io.doublegsoft.usebase.grammar;

import io.doublegsoft.usebase.*;
import org.junit.Test;

import java.util.List;

public class ComplexTest {

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
//    Usebase usebase = new Usebase();
//    UsebaseUsecase usecase = usebase.parse(expr).get(0);
//    Assert.assertEquals("reward", usecase.getName());
//    Assert.assertEquals("team_id", usecase.getArguments().get(0).getName());
//    printStatements(usecase.getStatements());
//    Assert.assertEquals(2, usecase.getStatements().size());
//    Assert.assertEquals(3, usecase.getStatements().get(1).getStatements().size());
//    Assert.assertEquals(1, usecase.getStatements().get(1).getStatements().get(0).getStatements().size());
//    Assert.assertEquals(1, usecase.getStatements().get(1).getStatements().get(1).getStatements().size());
//
//    UsebaseAssignment stmt1 = (UsebaseAssignment) usecase.getStatements().get(0);
//    UsebaseAggregate value = (UsebaseAggregate)stmt1.getValue();
////    Assert.assertEquals("player", value.getArrayValue().getName());
//    Assert.assertEquals(1, value.getArrays().get(0).getFilterArguments().size());
//    Assert.assertEquals("team_id", value.getArrays().get(0).getFilterArguments().get(0).getName());
//    UsebaseLoop stmt2 = (UsebaseLoop) usecase.getStatements().get(1);
//    Assert.assertEquals("player", stmt2.getItemVar());
//    Assert.assertEquals("players", stmt2.getArrayVar());
//    UsebaseComparison stmt3 = (UsebaseComparison) stmt2.getStatements().get(0);
//    Assert.assertEquals("player.age", stmt3.getComparand());
//    Assert.assertEquals("<", stmt3.getComparator());
//    Assert.assertEquals(new BigDecimal("20"), stmt3.getValue().getNumber());
//
//    UsebaseAssignment stmt4 = (UsebaseAssignment) stmt3.getStatements().get(0);
//    Assert.assertEquals("player.rating", stmt4.getAssignee());
//    Assert.assertEquals("+=", stmt4.getAssignOp());
//    Assert.assertEquals(new BigDecimal("10"), stmt4.getValue().getNumber());
//
//    UsebaseUpdate stmt7 = (UsebaseUpdate) stmt2.getStatements().get(2);
//    Assert.assertEquals("player", stmt7.getVariable());
//    Assert.assertEquals("player", stmt7.getTypeName());
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
//    Usebase usebase = new Usebase();
//    UsebaseUsecase usecase = usebase.parse(expr).get(0);
//    Assert.assertEquals("reimburse", usecase.getName());
//    Assert.assertEquals("patient_id", usecase.getArguments().get(0).getName());
//    Assert.assertEquals("amount", usecase.getArguments().get(1).getName());
//
//    UsebaseComparison stmt2 = (UsebaseComparison) usecase.getStatements().get(1);
//    Assert.assertEquals("patient.type", stmt2.getComparand());
//    Assert.assertEquals("==", stmt2.getComparator());
//    Assert.assertEquals("普通百姓", stmt2.getValue().getString());
//    Assert.assertEquals("amount", stmt2.getAndComparisons().get(0).getComparand());
//    Assert.assertEquals(">", stmt2.getAndComparisons().get(0).getComparator());
//    Assert.assertEquals(new BigDecimal("5000"), stmt2.getAndComparisons().get(0).getValue().getNumber());
//
//    UsebaseAssignment stmt3 = (UsebaseAssignment) stmt2.getStatements().get(0);
//    Assert.assertEquals("amount", stmt3.getAssignee());
//    Assert.assertEquals("*=", stmt3.getAssignOp());
//    Assert.assertEquals(new BigDecimal("0.6"), stmt3.getValue().getNumber());
//    printStatements(usecase.getStatements());
  }

  /**
   * 定制化产品订单到收款全流程。
   */
  @Test
  public void test_03() throws Exception {
    String expr =
        "@input(prod_spec)\n" +
        "  |+| order = prod_spec \n" +
        "  |+| wbs = prod_spec  \n";
//    Usebase usebase = new Usebase();
//    UsebaseUsecase usecase = usebase.parse(expr).get(0);
  }

  /**
   * 盲僧Q技能
   * <p>
   */
  @Test
  public void test_extra_01() throws Exception {
    String expr =
        "@reimburse(leeSin, others, timestamp, skill):{reimburse}\n" +
            "  |:| q_skill = leeSin.q \n" +
            "  |*| other in others \n" +
            "  |*|?| q_skill.pos ~= other.pos \n" +
            "  |*|?|:| other.status = 'F' \n" +
            "  |*|?|:| other.time = now \n";
//    Usebase usebase = new Usebase();
//    UsebaseUsecase usecase = usebase.parse(expr).get(0);
//    printStatements(usecase.getStatements());
//    String dot = new DotBuilder().build(usecase.getName(), 1, usecase.getStatements()).toString();
//    System.out.println(dot);
  }

}
