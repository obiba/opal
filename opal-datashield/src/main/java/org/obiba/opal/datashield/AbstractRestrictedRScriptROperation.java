/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.datashield;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.opentelemetry.context.Scope;
import org.obiba.datashield.core.DSEnvironment;
import org.obiba.datashield.core.impl.DefaultDSMethod;
import org.obiba.datashield.r.expr.ParseException;
import org.obiba.datashield.r.expr.RScriptGenerator;
import org.obiba.datashield.r.expr.RScriptGeneratorFactory;
import org.obiba.opal.datashield.cfg.RestrictedROperation;
import org.obiba.opal.spi.r.AbstractROperationWithResult;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.ROperations;
import org.slf4j.MDC;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractRestrictedRScriptROperation extends AbstractROperationWithResult implements RestrictedROperation {

  private final String script;

  private final RScriptGenerator rScriptGenerator;

  private final DataShieldContext context;

  private final DataShieldLog.Action action;

  /**
   * The operation's trace: begun here, on the request thread, and ended by {@link #evaluate} on the
   * session's consumer thread - or here, when the parser refuses the script.
   */
  private final DataShieldTracer.Operation trace;

  @SuppressWarnings("ConstantConditions")
  public AbstractRestrictedRScriptROperation(String script, DataShieldContext context, DataShieldLog.Action action,
      String symbol) throws ParseException {
    Preconditions.checkArgument(script != null, "script cannot be null");
    Preconditions.checkArgument(context.getEnvironment() != null, "environment cannot be null");
    Preconditions.checkArgument(context.getRParserVersion() != null, "R parser version cannot be null");

    this.script = script;
    this.context = context;
    this.action = action;
    this.trace = DataShieldTracer.begin(context, action, symbol, script);
    MDC.put("ds_script_in", script);
    // the operation is current while the parse is logged, so that the record is anchored on it
    try (Scope ignored = trace.makeCurrent()) {
      // parsed here, on the request thread: the restriction is applied before anything reaches R,
      // and a refusal is the one thing an auditor wants to find in the session's trace
      this.rScriptGenerator = trace.parse(script,
          () -> RScriptGeneratorFactory.make(context.getRParserVersion(), context.getEnvironment(), script),
          RScriptGenerator::toScript);
      String toScript = rScriptGenerator.toScript();
      String mapped = Joiner.on(";").join(rScriptGenerator.getMappedFunctions().entrySet().stream()
          .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
          .collect(Collectors.toList()));
      MDC.put("ds_script_out", toScript);
      MDC.put("ds_map", mapped);
      DataShieldLog.userLog(context, DataShieldLog.Action.PARSE, "parsed '{}'", toScript);
    } catch (Throwable e) {
      DataShieldLog.userErrorLog(context, DataShieldLog.Action.PARSE, "Script failed validation: {}", e.getMessage());
      // a no-op when the parse itself failed: it has already ended the operation
      trace.refuse(e);
      if (e instanceof ParseException)
        throw e;
      throw new ParseException(e.getMessage(), e);
    }
  }

  @Override
  protected void doWithConnection() {
    prepareOps(context.getEnvironment()).forEach(op -> op.doWithConnection(getConnection()));
  }

  /**
   * Runs the evaluation of the restricted script as the last step of the operation's trace, with the
   * audit records around it anchored on the operation.
   */
  protected void evaluate(Runnable evaluation) {
    String restricted = restrictedScript();
    try (Scope ignored = trace.makeCurrent()) {
      beforeLog(restricted);
      DataShieldLog.userDebugLog(context, action, "evaluating '{}'", restricted);
      try {
        trace.evaluate(restricted, evaluation);
        beforeLog(restricted);
        DataShieldLog.userLog(context, action, "evaluated '{}'", restricted);
      } catch (Throwable e) {
        beforeLog(restricted);
        DataShieldLog.userErrorLog(context, action, "evaluation failure '{}'", restricted);
        throw e;
      }
    }
  }

  /**
   * The MDC keys the evaluation records carry, put back before each record because writing one
   * clears them.
   */
  protected void beforeLog(String restricted) {
    MDC.put("ds_eval", restricted);
    MDC.put("ds_profile", context.getProfile());
    context.getContextMap().forEach(MDC::put);
  }

  @Override
  public String restrictedScript() {
    return rScriptGenerator.toScript();
  }

  public DataShieldContext getContext() {
    return context;
  }

  /**
   * Returns a sequence of {@code ROperation} instances to run in order to prepare an R environment for executing the
   * methods defined by this {@code DataShieldEnvironment}. Once the operations are executed, an environment is setup
   * and the method {@code DataShieldMethod#invoke(Environment)} will allow obtaining the signature to invoke the
   * method.
   *
   * @return a sequence of {@code ROperation} that will create a protected R environment for executing methods defined.
   */
  public Iterable<ROperation> prepareOps(DSEnvironment environment) {
    String envSymbol = environment.getMethodType().symbol();
    List<ROperation> rops = environment.getMethods().stream()
        .filter(m -> !m.hasPackage())
        .map(m -> ROperations.assign(m.getName(), ((DefaultDSMethod) m).getFunction(), envSymbol, true))
        .collect(Collectors.toList());
    if (rops.isEmpty())
      return rops;

    return ImmutableList.<ROperation>builder()//
        .add(ROperations.eval(String.format("base::rm(%s)", envSymbol), null))
        .add(ROperations.assign(envSymbol, "base::new.env()"))
        .addAll(rops)
        // Protect the contents of the environment
        .add(ROperations.eval(String.format("base::lockEnvironment(%s, bindings=TRUE)", envSymbol), null))//
        // Protect the contents of the environment
        .add(ROperations.eval(String.format("base::lockBinding('%s', base::environment())", envSymbol), null))
        .build();
  }

  @Override
  public String toString() {
    return script;
  }
}
