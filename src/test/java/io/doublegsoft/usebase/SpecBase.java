package io.doublegsoft.usebase;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.StatementDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import io.doublegsoft.modelbase.Modelbase;
import io.doublegsoft.usebase.association.AssociationBuilder;
import io.doublegsoft.usebase.modelbase.ModelbaseWriter;
import io.doublegsoft.usebase.output.TemplateOutputWriter;
import io.doublegsoft.usebase.aggregate.AggregateBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpecBase {

  public static final String PROJ_NAME = "demo4j";

  public static final String TEMPLATE_ROOT = "java-tx@spring-1.x/src/main/java/$namespace$/$app$";

  public static final String TEMPLATE_SERVICE_HELPER = TEMPLATE_ROOT + "/service/helper/$usecase$Helper.java.ftl";

  public static final String TEMPLATE_SERVICE_IMPL = TEMPLATE_ROOT + "/service/impl/$usecase$ServiceImpl.java.ftl";

  public static final String TEMPLATE_SERVICE = TEMPLATE_ROOT + "/service/$usecase$Service.java.ftl";

  protected ModelDefinition loadModel(String... projs) throws Exception {
    String content = "";
    for (String proj : projs) {
      InputStream input = SpecBase.class.getResourceAsStream("/model/" + proj + ".modelbase");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buff = new byte[4096];
      int len = 0;
      while ((len = input.read(buff)) > 0) {
        baos.write(buff, 0, len);
      }
      baos.flush();
      input.close();

      content += new String(baos.toByteArray(), "UTF-8");
    }
    return new Modelbase().parse(content);
  }

  protected void printStatements(List<StatementDefinition> stmts) {
    for (StatementDefinition stmt : stmts) {
      System.out.println(stmt.getOperator());
      printStatements(stmt.getStatements());
    }
  }

  protected void printModelbaseExtensionByUsecase(UsecaseDefinition usecase, ObjectDefinition... objs) throws IOException {
    StringWriter sw = new StringWriter();
    ModelbaseWriter writer = new ModelbaseWriter(sw);
    writer.write(usecase.getParameterizedObject());
    // TODO
    writer.write(usecase.getReturnedObject());
    if (objs != null) {
      for (ObjectDefinition obj : objs) {
        writer.write(obj);
      }
    }
    System.out.println(sw);
  }

  protected void printModelbaseExtensionByUsecase(String filePath, UsecaseDefinition usecase, ObjectDefinition... objs) throws IOException {
    StringWriter sw = new StringWriter();
    ModelbaseWriter writer = new ModelbaseWriter(sw);
    if (usecase != null) {
      writer.write(usecase.getParameterizedObject());
      if (usecase.getReturnedObject() != null) {
        writer.write(usecase.getReturnedObject());
      }
    }
    if (objs != null && objs.length > 0) {
      for (ObjectDefinition obj : objs) {
        writer.write(obj);
      }
    }
    try (FileWriter fw = new FileWriter(filePath, true)) {
      fw.write(sw.toString());
    }
  }

  protected void printJavaCodeForUsecase(String templateName, UsecaseDefinition usecase, ModelDefinition dataModel) throws IOException {
    printJavaCodeForUsecase(templateName, usecase, dataModel, null);
  }

  protected void printJavaCodeForUsecase(String templateName, UsecaseDefinition usecase, ModelDefinition dataModel, String outputFile) throws IOException {
    StringWriter sw = new StringWriter();
    Map<String,Object> app = new HashMap<>();
    app.put("name", PROJ_NAME);
    Map<String,Object> data = new HashMap<>();
    TemplateOutputWriter writer = new TemplateOutputWriter(sw,
        "../usebase-data",
        "../usebase-data/java");
    data.put("namespace", "biz.doublegsoft");
    data.put("app", app);
    data.put("model", dataModel);
    data.put("usecase", usecase);
    data.put("aggregateBuilder", new AggregateBuilder(dataModel));
    data.put("associationBuilder", new AssociationBuilder(dataModel));
    writer.write(templateName, usecase, data);

    if (outputFile != null) {
      File f = new File(outputFile);
      f.getParentFile().mkdirs();
      f.createNewFile();
      Files.write(f.toPath(), sw.toString().getBytes(StandardCharsets.UTF_8));
    }
  }

  public static String toPascalCase(String input) {
    if (input == null) return null;

    // 1. 用非字母数字拆分，得到“单词”
    String[] parts = input.split("[^a-zA-Z0-9]+");
    StringBuilder sb = new StringBuilder();

    for (String part : parts) {
      if (part.isEmpty()) continue;           // 防止出现空串
      sb.append(Character.toUpperCase(part.charAt(0))); // 首字母大写
      if (part.length() > 1) {
        sb.append(part.substring(1).toLowerCase());   // 其余小写
      }
    }
    return sb.toString();
  }

  protected void checkOriginalIndexAndObject(ObjectDefinition obj) {
    for (AttributeDefinition attr : obj.getAttributes()) {
      if (attr.getLabelledOption("original", "index") == null) {
        throw new IllegalArgumentException(attr.getName() + " has no original index annotation.");
      }
      if (attr.getLabelledOption("original", "object") == null) {
        throw new IllegalArgumentException(attr.getName() + " has no original object annotation.");
      }
    }
  }
}
