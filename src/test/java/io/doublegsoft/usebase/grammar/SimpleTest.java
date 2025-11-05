/*
** ████████████████████████████████████████████
** █▄─██─▄█─▄▄▄▄█▄─▄▄─█▄─▄─▀██▀▄─██─▄▄▄▄█▄─▄▄─█
** ██─██─██▄▄▄▄─██─▄█▀██─▄─▀██─▀─██▄▄▄▄─██─▄█▀█
** ▀▀▄▄▄▄▀▀▄▄▄▄▄▀▄▄▄▄▄▀▄▄▄▄▀▀▄▄▀▄▄▀▄▄▄▄▄▀▄▄▄▄▄▀
*/
package io.doublegsoft.usebase.grammar;

import io.doublegsoft.usebase.dot.DotBuilder;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Assert;
import org.junit.Test;

public class SimpleTest {

  private static io.doublegsoft.usebase.UsebaseParser parse(String expr) {
    CharStream input = CharStreams.fromString(expr);
    io.doublegsoft.usebase.UsebaseLexer lexer = new io.doublegsoft.usebase.UsebaseLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    return new io.doublegsoft.usebase.UsebaseParser(tokens);
  }

  @Test
  public void test_usebase_object_to_find_01() throws Exception {
    String expr = "home{team: status, join_time}";
    io.doublegsoft.usebase.UsebaseParser parser = parse(expr);
    io.doublegsoft.usebase.UsebaseParser.Usebase_objectContext ctx = parser.usebase_object();
    Assert.assertEquals("home", ctx.alias.getText());
    Assert.assertEquals("team", ctx.name.getText());
  }

  @Test
  public void test_usebase_object_to_save_01() throws Exception {
    String expr = "home{team: status = 'E', join_time = now}";
    io.doublegsoft.usebase.UsebaseParser parser = parse(expr);
    io.doublegsoft.usebase.UsebaseParser.Usebase_objectContext ctx = parser.usebase_object();
    Assert.assertEquals("home", ctx.alias.getText());
    Assert.assertEquals("team", ctx.name.getText());
  }

  @Test
  public void test_usebase_statement_to_read_01() throws Exception {
    String expr = "|=| home{team: status = 'E', join_time = now}#(id)";
    io.doublegsoft.usebase.UsebaseParser parser = parse(expr);
    io.doublegsoft.usebase.UsebaseParser.Usebase_statementContext ctx = parser.usebase_statement();
    io.doublegsoft.usebase.UsebaseParser.Usebase_expressionContext ctxExpr = ctx.usebase_expression();
    Assert.assertEquals("|=|", ctx.usebase_operator().getText());
    Assert.assertEquals("home", ctxExpr.usebase_object().alias.getText());
    Assert.assertEquals("team", ctxExpr.usebase_object().name.getText());
    Assert.assertEquals(1, ctxExpr.usebase_object().usebase_arguments().usebase_argument().size());
    Assert.assertEquals("id", ctxExpr.usebase_object().usebase_arguments().usebase_argument(0).getText());
  }

  @Test
  public void test_usebase_statement_to_check_01() throws Exception {
    String expr = "|~| birthday < now '出生日期必须小于当前日期'";
    io.doublegsoft.usebase.UsebaseParser parser = parse(expr);
    io.doublegsoft.usebase.UsebaseParser.Usebase_statementContext ctx = parser.usebase_statement();
    io.doublegsoft.usebase.UsebaseParser.Usebase_expressionContext ctxExpr = ctx.usebase_expression();
    Assert.assertEquals("'出生日期必须小于当前日期'", ctxExpr.msg.getText());
  }

  @Test
  public void test_usebase_example_01() throws Exception {
    String expr = "@find:{match: match_time, home_score, away_score} <home_team> {team: name, logo} <away_team> {team: name, logo}";
    io.doublegsoft.usebase.UsebaseParser parser = parse(expr);
    io.doublegsoft.usebase.UsebaseParser.Usebase_usecaseContext ctx = parser.usebase_usecase();
    io.doublegsoft.usebase.UsebaseParser.Usebase_aggregateContext ctxAgg = ctx.usebase_return().usebase_aggregate();
    io.doublegsoft.usebase.UsebaseParser.Usebase_objectContext ctxMatch = ctxAgg.usebase_data(0).usebase_object();
    Assert.assertEquals("find", ctx.name.getText());
    Assert.assertEquals("match", ctxMatch.name.getText());
    Assert.assertEquals("match_time", ctxMatch.usebase_attributes().usebase_attribute(0).name.getText());
    Assert.assertEquals("home_score", ctxMatch.usebase_attributes().usebase_attribute(1).name.getText());
    Assert.assertEquals("away_score", ctxMatch.usebase_attributes().usebase_attribute(2).name.getText());
  }

  @Test
  public void test_usebase_example_02() throws Exception {
    String expr = "@find:{team: name, logo, found_date} <> {club: name, logo} \n" +
        "<team> :average_player_rating%average[{team_player} <player> {player: rating}]% \n" +
        "<team.id=entity_id + entity_type='TEAM'> tags[tag]";
    io.doublegsoft.usebase.UsebaseParser parser = parse(expr);
    io.doublegsoft.usebase.UsebaseParser.Usebase_usecaseContext ctx = parser.usebase_usecase();
    Assert.assertEquals("find", ctx.name.getText());
    io.doublegsoft.usebase.UsebaseParser.Usebase_aggregateContext ctxAgg = ctx.usebase_return().usebase_aggregate();
    io.doublegsoft.usebase.UsebaseParser.Usebase_objectContext ctxObject0 = ctxAgg.usebase_data(0).usebase_object();
    Assert.assertEquals("team", ctxObject0.name.getText());
    Assert.assertEquals("name", ctxObject0.usebase_attributes().usebase_attribute(0).name.getText());
    Assert.assertEquals("logo", ctxObject0.usebase_attributes().usebase_attribute(1).name.getText());
    Assert.assertEquals("found_date", ctxObject0.usebase_attributes().usebase_attribute(2).name.getText());
//    io.doublegsoft.usebase.UsebaseParser.Usebase_derivativeContext ctxDeri2 = ctxRels.usebase_value(2).usebase_derivative();
//    Assert.assertEquals("average_player_rating", ctxDeri2.name.getText());
//    io.doublegsoft.usebase.UsebaseParser.Usebase_arrayContext ctxArray3 = ctxRels.usebase_value(3).usebase_array();
//    Assert.assertEquals("tags", ctxArray3.alias.getText());
//    Assert.assertEquals("tag", ctxArray3.usebase_relations().getText());
  }

  @Test
  public void test_usebase_example_03() throws Exception {
    String expr =
        "@reward(team_id)\n" +
        "  |&| players = [{player} <team_player> {team}]#(team_id)\n" +
        "  |*| {player} in players\n" +
        "  |*|?| player.age < 20 \n" +
        "  |*|?|:| player.rating += 10\n" +
        "  |*|?| player.age >= 20 \n" +
        "  |*|?|:| player.rating += 5\n" +
        "  |*|=| {player} ";
    io.doublegsoft.usebase.UsebaseParser parser = parse(expr);
    io.doublegsoft.usebase.UsebaseParser.Usebase_usecaseContext ctx = parser.usebase_usecase();


  }

  /**
   * 业务场景：
   * <p>
   * 新添加人员的时候同时创建用户，
   * “#(national_id, person_name)”说明通过身份证号和人名控制重复录入，
   * “<person_id=user_id>”说明人员标识和用户标识构成一对一关系；
   * 默认通过标识判断新增或者更新。
   */
  @Test
  public void test_person_user_save() throws Exception {
    String expr =
        "@save({person}#(national_id, person_name) <person_id=user_id> {user})";
    io.doublegsoft.usebase.UsebaseParser parser = parse(expr);
    io.doublegsoft.usebase.UsebaseParser.Usebase_usecaseContext ctx = parser.usebase_usecase();
  }

  /**
   * 业务场景：
   * <p>
   * 参考test_person_user_save，同时增加用户角色的保存。
   */
  @Test
  public void test_person_user_roles_save() throws Exception {
    String expr =
        "@save({person}#(national_id, person_name) <person_id=user_id> {user} <user_role> [role])";
    io.doublegsoft.usebase.UsebaseParser parser = parse(expr);
    io.doublegsoft.usebase.UsebaseParser.Usebase_usecaseContext ctx = parser.usebase_usecase();
  }

  /**
   * 业务场景：
   * <p>
   * 参考test_person_user_roles_save，同时保存日志（业务无关，系统相关）。
   */
  @Test
  public void test_person_user_roles_log_save() throws Exception {
    String expr =
        "@save({person}#(national_id, person_name) <person_id=user_id> {user} <user_role> [role])" +
        "  |+| {audit_log: name = person_name, audit_time = now, modifier_id = 'SYS'}";
    io.doublegsoft.usebase.UsebaseParser parser = parse(expr);
    io.doublegsoft.usebase.UsebaseParser.Usebase_usecaseContext ctx = parser.usebase_usecase();
  }
}
