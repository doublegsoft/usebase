package io.doublegsoft.usebase.parser;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metamodel.InvocationDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import com.doublegsoft.jcommons.metamodel.ValueDefinition;
import com.doublegsoft.jcommons.metamodel.VariableDefinition;
import io.doublegsoft.usebase.Usebase;
import org.antlr.v4.runtime.TokenStream;

public class InvocationParser extends UsebaseParser {

  public InvocationParser(ModelDefinition dataModel) {
    super(dataModel);
  }

  public void assemble(io.doublegsoft.usebase.UsebaseParser.Usebase_invokeContext ctx,
                       InvocationDefinition invocation, UsecaseDefinition usecase) {
    String method = ctx.anybase_identifier().getText();
    invocation.setMethod(method);
    // 方法调用的参数，简单封装
    if (ctx.usebase_arguments() != null) {
      for (io.doublegsoft.usebase.UsebaseParser.Usebase_argumentContext ctxArg : ctx.usebase_arguments().usebase_argument()) {
        if (ctxArg.anybase_identifier() != null) {
          if (ctxArg.anybase_value() == null) {
            String varname = ctxArg.anybase_identifier().getText();
            VariableDefinition var = usecase.getVariable(varname);
            if (var == null) {
              throw new IllegalArgumentException("not found variable named \"" + varname + "\" in this usecase.");
            }
            invocation.getArguments().add(var);
          } else {
            VariableDefinition var = new VariableDefinition();
            ValueDefinition argVal = new ValueDefinition();
            getValueParser().assemble(ctxArg.anybase_value(), argVal);
            var.setName(ctxArg.anybase_identifier().getText());
            var.setType(Usebase.guessVariableType(argVal));
            invocation.getArguments().add(var);
          }
        }
      }
    }
    if (ctx.msg != null) {
      String msg = ctx.msg.getText();
      msg = msg.substring(1, msg.length() - 1);
      invocation.setError(msg);
    }
  }
}
