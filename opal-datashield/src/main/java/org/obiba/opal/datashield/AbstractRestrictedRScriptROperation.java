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
import org.obiba.datashield.core.DSEnvironment;
import org.obiba.datashield.core.impl.DefaultDSMethod;
import org.obiba.datashield.r.expr.ParseException;
import org.obiba.datashield.r.expr.RScriptGenerator;
import org.obiba.datashield.r.expr.RScriptGeneratorFactory;
import org.obiba.opal.spi.r.AbstractROperationWithResult;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.ROperations;
import org.slf4j.MDC;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractRestrictedRScriptROperation extends AbstractROperationWithResult {

  private final String script;


  private final DSEnvironment environment;

  private final RScriptGenerator rScriptGenerator;

  @SuppressWarnings("ConstantConditions")
  public AbstractRestrictedRScriptROperation(String script, DSEnvironment environment,
                                             String rParserVersion) throws ParseException {
    Preconditions.checkArgument(script != null, "script cannot be null");
    Preconditions.checkArgument(environment != null, "environment cannot be null");
    Preconditions.checkArgument(rParserVersion != null, "R parser version cannot be null");

    this.script = script;
    this.environment = environment;
    MDC.put("ds_script_in", script);
    MDC.put("ds_script_out", null);
    MDC.put("ds_func", null);
    try {
      this.rScriptGenerator = RScriptGeneratorFactory.make(rParserVersion, environment, script);
      String toScript = rScriptGenerator.toScript();
      String mapped = Joiner.on(";").join(rScriptGenerator.getMappedFunctions().entrySet().stream()
          .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
          .collect(Collectors.toList()));
      MDC.put("ds_script_out", toScript);
      MDC.put("ds_func", mapped);
      DataShieldLog.userLog(null, DataShieldLog.Action.PARSED, "parsed '{}'", toScript);
    } catch (Throwable e) {
      DataShieldLog.userLog(null, DataShieldLog.Action.PARSE_ERROR,"Script failed validation: {}", e.getMessage());
      if (e instanceof ParseException)
        throw e;
      throw new ParseException(e.getMessage(), e);
    }
  }

  @Override
  protected void doWithConnection() {
    prepareOps(environment).forEach(op -> op.doWithConnection(getConnection()));
  }

  protected String restricted() {
    return rScriptGenerator.toScript();
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
