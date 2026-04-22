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
import org.junit.Assert;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SpecBase {

  public static final String PROJ_NAME = "demo4j";

  public static final String TEMPLATE_TX_ROOT = "java-tx@spring-1.x/src/main/java/$namespace$/$app$";

  public static final String TEMPLATE_MVC_ROOT = "java-mvc@spring-1.x/src/main/java/$namespace$/$app$";

  public static final String TEMPLATE_SERVICE_HELPER = TEMPLATE_TX_ROOT + "/service/helper/$usecase$Helper.java.ftl";

  public static final String TEMPLATE_SERVICE_IMPL = TEMPLATE_TX_ROOT + "/service/impl/$usecase$ServiceImpl.java.ftl";

  public static final String TEMPLATE_SERVICE = TEMPLATE_TX_ROOT + "/service/$usecase$Service.java.ftl";

  public static final String TEMPLATE_CONTROLLER = TEMPLATE_MVC_ROOT + "/mvc/$usecase$Controller.java.ftl";

  protected ModelDefinition loadModel(String... projs) throws Exception {
    String content = "";
    for (String proj : projs) {
      InputStream input = null;
      input = SpecBase.class.getResourceAsStream("/modelbase/" + proj + ".modelbase");
      if (input == null) {
        input = new FileInputStream(proj);
      }
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

  protected void printModelbaseExtensionByUsecase(UsecaseDefinition usecase, ModelDefinition dataModel, ObjectDefinition... objs) throws IOException {
    StringWriter sw = new StringWriter();
    ModelbaseWriter writer = new ModelbaseWriter(sw, dataModel);
    writer.write(usecase.getParameterizedObject());
    writer.write(usecase.getReturnedObject());
    if (objs != null) {
      for (ObjectDefinition obj : objs) {
        writer.write(obj);
      }
    }
    System.out.println(sw);
  }

  protected void printModelbaseExtensionByUsecase(String filePath, UsecaseDefinition usecase,
                                                  ModelDefinition dataModel,
                                                  ObjectDefinition... objs) throws IOException {
    StringWriter sw = new StringWriter();
    ModelbaseWriter writer = new ModelbaseWriter(sw, dataModel);
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
    printJavaCodeForUsecase(PROJ_NAME, templateName, usecase, dataModel, outputFile);
  }

  protected void printJavaCodeForUsecase(String projName, Map<String,Object> data, String templateName, UsecaseDefinition usecase, ModelDefinition dataModel, String outputFile) throws IOException {
    StringWriter sw = new StringWriter();
    Map<String,Object> app = new HashMap<>();
    app.put("name", projName);
    TemplateOutputWriter writer = new TemplateOutputWriter(sw,
        "../usebase-data",
        "../usebase-data/java");
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

  protected void printJavaCodeForUsecase(String projName, String templateName, UsecaseDefinition usecase, ModelDefinition dataModel, String outputFile) throws IOException {
    Map<String,Object> data = new HashMap<>();
    data.put("namespace", "biz.doublegsoft");
    printJavaCodeForUsecase(projName, data, templateName, usecase, dataModel, outputFile);
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

  protected void printSourcesForUsecase(UsecaseDefinition usecase, ModelDefinition dataModel, String usebaseOutput, String projRoot) throws Exception {
    printModelbaseExtensionByUsecase(usebaseOutput, usecase, dataModel);
    printJavaCodeForUsecase(TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, projRoot + "/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, projRoot + "/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(TEMPLATE_SERVICE,
        usecase, dataModel, projRoot + "/" + toPascalCase(usecase.getName()) + "Service.java");
  }

  protected String loadUsebaseExpression(String usebasePath) throws Exception {
    try {
      byte[] bytes = Files.readAllBytes(new File("src/test/resources/usebase/" + usebasePath).toPath());
      return new String(bytes, StandardCharsets.UTF_8);
    } catch (IOException ignored) {

    }
    byte[] bytes = Files.readAllBytes(new File(usebasePath).toPath());
    return new String(bytes, StandardCharsets.UTF_8);
  }

  protected static void rm(String pathStr) {
    Path path = Paths.get(pathStr);

    if (!Files.exists(path)) {
      System.out.println("目录不存在: " + pathStr);
      return;
    }

    try {
      System.out.println("开始删除目录: " + pathStr);
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          Files.delete(dir);                     // 删除空目录
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException e) {
      System.err.println("删除失败: " + e.getMessage());
      e.printStackTrace();
    }
  }

  protected static void mvn(String workingDir, String... commands) {
    try {
      // 构建命令
      ProcessBuilder processBuilder = new ProcessBuilder();
      processBuilder.directory(new File(workingDir));  // 设置工作目录

      // 命令列表（支持 Windows 和 Mac/Linux）
      if (System.getProperty("os.name").toLowerCase().contains("win")) {
        processBuilder.command("cmd.exe", "/c", "mvn", String.join(" ", commands));
      } else {
        List<String> cmds = new ArrayList<>();
        cmds.add("mvn");
        for (String cmd : commands) {
          cmds.add(cmd);
        }
        processBuilder.command(cmds);
      }

      // 合并错误流和输出流
      processBuilder.redirectErrorStream(true);

      System.out.println("开始执行: mvn " + String.join(" ", commands));

      Process process = processBuilder.start();

      // 实时打印 Maven 输出
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          System.out.println(line);
        }
      }

      // 等待命令执行完成（可设置超时）
      boolean finished = process.waitFor(30, TimeUnit.MINUTES);  // 最长等 30 分钟

      if (finished) {
        int exitCode = process.exitValue();
        if (exitCode == 0) {
          System.out.println("mvn " + String.join(" ", commands) + " 执行成功！");
          // 这里可以继续调用你之前的 java -jar
//          runJarAfterBuild(workingDir);
        } else {
          System.err.println("Maven 执行失败，退出码: " + exitCode);
        }
      } else {
        System.err.println("Maven 执行超时！");
        process.destroyForcibly();
      }

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  protected static void runJarAfterBuild(String projectDir) {
    String jarPath = projectDir + "/target/my-test-app.jar";  // 修改成你的 JAR 名
    File jarFile = new File(jarPath);

    if (jarFile.exists()) {
      System.out.println("找到 JAR 文件，开始执行测试...");
      try {
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarPath);
        pb.directory(new File(projectDir));
        pb.inheritIO();           // 让 JAR 的输出直接在当前控制台显示
        Process p = pb.start();
        p.waitFor();              // 等待测试执行完自动停止
        System.out.println("测试执行完毕，JVM 已自动停止！");
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      System.err.println("未找到 JAR 文件: " + jarPath);
    }
  }

  public static boolean bash(String command) {
    try {
      ProcessBuilder processBuilder = new ProcessBuilder();
      if (System.getProperty("os.name").toLowerCase().contains("win")) {
        processBuilder.command("cmd.exe", "/c", command);
      } else {
        processBuilder.command("sh", "-c", command);
      }
      processBuilder.redirectErrorStream(true);
      Process process = processBuilder.start();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          System.out.println("   " + line);
        }
      }
      boolean finished = process.waitFor(10, TimeUnit.MINUTES);

      if (finished) {
        int exitCode = process.exitValue();
        if (exitCode == 0) {
          System.out.println("Shell 命令执行成功！");
          return true;
        } else {
          System.err.println("Shell 命令执行失败，退出码: " + exitCode);
          return false;
        }
      } else {
        System.err.println("Shell 命令执行超时！");
        process.destroyForcibly();
        return false;
      }

    } catch (IOException | InterruptedException e) {
      System.err.println("执行 Shell 时发生异常: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  protected String postData(String path, String json) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8810/api" + path))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();
    HttpResponse<String> response =
        client.send(request, HttpResponse.BodyHandlers.ofString());
    Assert.assertEquals(200, response.statusCode());
    return response.body();
  }

  protected void generateCode(String projname,
                              String usebsaseOutFile, String codeOutDir,
                              UsecaseDefinition usecase, ModelDefinition dataModel) throws IOException {
    printModelbaseExtensionByUsecase(usebsaseOutFile, usecase, dataModel);
    printJavaCodeForUsecase(projname, TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, codeOutDir + "/service/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(projname, TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, codeOutDir + "/service/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(projname, TEMPLATE_SERVICE,
        usecase, dataModel, codeOutDir + "/service/" + toPascalCase(usecase.getName()) + "Service.java");
    printJavaCodeForUsecase(projname, TEMPLATE_CONTROLLER,
        usecase, dataModel, codeOutDir + "/mvc/" + toPascalCase(usecase.getName()) + "Controller.java");
  }

  protected void generateCode(String projname, Map<String,Object> data,
                              String usebsaseOutFile, String codeOutDir,
                              UsecaseDefinition usecase, ModelDefinition dataModel) throws IOException {
    printModelbaseExtensionByUsecase(usebsaseOutFile, usecase, dataModel);

    printJavaCodeForUsecase(projname, data, TEMPLATE_SERVICE_HELPER,
        usecase, dataModel, codeOutDir + "/service/helper/" + toPascalCase(usecase.getName()) + "Helper.java");
    printJavaCodeForUsecase(projname, data, TEMPLATE_SERVICE_IMPL,
        usecase, dataModel, codeOutDir + "/service/impl/" + toPascalCase(usecase.getName()) + "ServiceImpl.java");
    printJavaCodeForUsecase(projname, data, TEMPLATE_SERVICE,
        usecase, dataModel, codeOutDir + "/service/" + toPascalCase(usecase.getName()) + "Service.java");
    printJavaCodeForUsecase(projname, data, TEMPLATE_CONTROLLER,
        usecase, dataModel, codeOutDir + "/mvc/" + toPascalCase(usecase.getName()) + "Controller.java");
  }

}
