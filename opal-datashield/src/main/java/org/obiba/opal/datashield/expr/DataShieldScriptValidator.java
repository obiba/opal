/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datashield.expr;

import com.google.common.collect.ImmutableList;

public class DataShieldScriptValidator implements DataShieldGrammarVisitor {

  public DataShieldScriptValidator() {
  }

  public static DataShieldScriptValidator of(DataShieldScriptValidator... validators) {
    return of(ImmutableList.copyOf(validators));
  }

  public static DataShieldScriptValidator of(final Iterable<DataShieldScriptValidator> validators) {
    return new DataShieldScriptValidator() {
      @Override
      public void validate(SimpleNode node) throws InvalidScriptException {
        for(DataShieldScriptValidator validator : validators) {
          validator.validate(node);
        }
      }
    };
  }

  public void validate(SimpleNode node) throws InvalidScriptException {
    node.jjtAccept(this, null);
  }

  @Override
  public Object visit(SimpleNode node, Object data) {
    return null;
  }

  @Override
  public Object visit(ASTroot node, Object data) {
    return null;
  }

  @Override
  public Object visit(ASTBinaryOp node, Object data) {
    return null;
  }

  @Override
  public Object visit(ASTfuncCall node, Object data) {
    return null;
  }

  @Override
  public Object visit(ASTsymbol node, Object data) {
    return null;
  }
}
