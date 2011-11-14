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

import java.io.StringReader;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class DataShieldExprTest {

  private static final Map<String, String> tests = ImmutableMap.<String, String> builder() //
  .put("A symbol", "A")//
  .put("A number", "1.0")//
  .put("A negative number", "-0.43151402098822")//
  .put("An embedded symbol", "A$B$C.D")//
  .put("A formula", "A ~ B")//
  .put("A function invocation", "A()")//
  .put("An operator on symbols", "A + B")//
  .put("Operator chaining", "A + B * C")//
  .put("Operator on functions", "A() + B * C")//
  .put("A formula with operators", "A ~ B + (C * D)^4 : E %in% F")//
  .put("Grouping", "(A + B) * (C - D)")//
  .put("Function with a single parameter", "A(B)")//
  .put("Function with a function invocation as parameter", "A(B())")//
  .put("Function with multiple parameters", "A(B, C)")//
  .put("Function with multiple kinds of parameters", "A(B, C(), D, E(F(G/H)), A + B * C())")//
  .put("Function with forumla as argument", "glm(A ~ B + C:D, poisson)")//
  .build();

  @Test
  public void test_testCases() throws ParseException {
    for(String msg : tests.keySet()) {
      String test = tests.get(msg);

      System.out.println(test);
      DataShieldGrammar g = new DataShieldGrammar(new StringReader(test));
      SimpleNode expr = g.root();
      expr.dump("");
    }
  }

  @Test
  public void test_visitor() throws ParseException {
    DataShieldGrammarVisitor visitor = new DataShieldGrammarVisitor() {

      @Override
      public Object visit(ASTsymbol node, Object data) {
        StringBuilder sb = (StringBuilder) data;
        sb.append(node.value);
        return data;
      }

      @Override
      public Object visit(ASTfuncCall node, Object data) {
        StringBuilder sb = (StringBuilder) data;
        sb.append(node.value).append("( ");
        for(int i = 0; i < node.jjtGetNumChildren(); i++) {
          Node child = node.jjtGetChild(i);
          if(i > 0) sb.append(' ');
          child.jjtAccept(this, sb);
        }
        sb.append(" )");
        return sb;
      }

      @Override
      public Object visit(ASTBinaryOp node, Object data) {
        StringBuilder sb = (StringBuilder) data;
        sb.append(node.value).append("( ");
        for(int i = 0; i < node.jjtGetNumChildren(); i++) {
          Node child = node.jjtGetChild(i);
          if(i > 0) sb.append(' ');
          child.jjtAccept(this, sb);
        }
        sb.append(" )");
        return sb;
      }

      @Override
      public Object visit(ASTroot node, Object data) {
        StringBuilder sb = (StringBuilder) data;
        node.childrenAccept(this, data);
        return sb;
      }

      @Override
      public Object visit(SimpleNode node, Object data) {
        return node.childrenAccept(this, data);
      }
    };

    String test = "A %*% (B(C ~ A + (G+D)^2 : H, C * D()) * 1/F)";
    DataShieldGrammar g = new DataShieldGrammar(new StringReader(test));
    StringBuilder b = (StringBuilder) g.root().jjtAccept(visitor, new StringBuilder());
    System.out.println(test + " --> " + b.toString());

  }
}
