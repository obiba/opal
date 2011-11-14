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

public class FirstNodeInvokesFunctionValidator extends DataShieldScriptValidator {

  @Override
  public Object visit(ASTroot node, Object data) {
    boolean valid = node.jjtGetNumChildren() == 1 && node.jjtGetChild(0) instanceof ASTfuncCall;
    if(valid == false) throw new InvalidScriptException("must invoke a function.");
    return null;
  }

}
