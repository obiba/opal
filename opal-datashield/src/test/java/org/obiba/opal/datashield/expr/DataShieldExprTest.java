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

  private static final Map<String, String> tests = ImmutableMap.<String, String>builder() //
      .put("A symbol", "A")//
      .put("A number", "1.0")//
      .put("An integer", "5L")//
      .put("A negative number", "-0.43151402098822")//
      .put("An embedded symbol", "A$B$C.D")//
      .put("A subset symbol", "A[2,1]")//
      .put("An open row subset symbol", "A[,1]")//
      .put("An open column subset symbol", "A[1,]")//
      .put("A subset symbol with range", "A[,1:2]")//
      .put("A subset symbol with function call", "D[,func(D[,1])]")//
      .put("A subset symbol with spaces", "A[ , 1 ]")//
      .put("An empty subset symbol", "A[]")//
      .put("An almost empty subset symbol", "A[,]")//
      .put("A subset value symbol", "A[[1]]")//
      .put("A formula", "A ~ B")//
      .put("A function invocation", "A()")//
      .put("An operator on symbols", "A + B")//
      .put("Operator chaining", "A + B * C")//
      .put("Operator on functions", "A() + B * C")//
      .put("A formula with operators", "A ~ B + (C * D)^4 : E %in% F")//
      .put("Grouping", "(A + B) * (C - D)")//
      .put("Function with a single parameter", "A(B)")//
      .put("Function with a subset parameter", "A(B[,2])")//
      .put("Function with a subset parameter and a simple parameter", "A(B[,2], C)")//
      .put("Function with a function invocation as parameter", "A(B())")//
      .put("Function with multiple parameters", "A(B, C)")//
      .put("Function with multiple kinds of parameters", "A(B, C(), D, E(F(G/H)), A + B * C())")//
      .put("Function with formula as argument", "glm(A ~ B + C:D, poisson)")//
      .put("Function with named argument", "A(arg=x,another=y)")//
      .put("Function with string argument", "A('this')")//
      .put("Function with double quoted string argument", "A(\"this\")")//
      .put("Function with numerical string argument", "A('123')")//
      .put("Function with alphanumerical string argument and some special characters", "A('this1_that.this-that1')")//
      .put("Function with a list of colon separated numerical string argument", "A('1.2:1.3:-8.5')")//
      .put("Function with a list of comma separated numerical string argument", "A('1.2,1.3,-8.5')")//
      .put("Function with a list of semi colon separated numerical string argument", "A('1.2;1.3;-8.5')")//
      .put("Function with data frame column name string argument", "A('D$abc')")//
      .put("Function with data frame index string argument", "A('D[123]')")//
      .build();

  @Test
  public void test_testCases() throws ParseException {
    for(String msg : tests.keySet()) {
      doTest(msg, tests.get(msg));
    }
  }

  @Test(expected = TokenMgrError.class)
  public void test_funcCallInString() throws ParseException {
    doTest("Function with function call in string argument", "A('this(that)')");
  }

  @Test(expected = TokenMgrError.class)
  public void test_spaceInString() throws ParseException {
    doTest("Function with space in string argument", "A(\"this that\")");
  }

  @Test(expected = TokenMgrError.class)
  public void test_slashInString() throws ParseException {
    doTest("Function with slash in string argument", "A('this/that')");
  }

  @Test(expected = TokenMgrError.class)
  public void test_backslashInString() throws ParseException {
    doTest("Function with backslash in string argument", "A('this\\that')");
  }

  @Test(expected = TokenMgrError.class)
  public void test_operatorInString() throws ParseException {
    doTest("Function with operator in string argument", "A('this+that')");
  }

  @Test(expected = TokenMgrError.class)
  public void test_equalInString() throws ParseException {
    doTest("Function with equal in string argument", "A('this=that')");
  }

  @Test(expected = TokenMgrError.class)
  public void test_assignInString() throws ParseException {
    doTest("Function with operator in string argument", "A('this<-that')");
  }

  @Test(expected = TokenMgrError.class)
  public void test_mixedQuoteInString1() throws ParseException {
    doTest("Function with mixed quotes in string argument", "A('this\")");
  }

  @Test(expected = TokenMgrError.class)
  public void test_mixedQuoteInString2() throws ParseException {
    doTest("Function with mixed quotes in string argument", "A(\"that')");
  }

  private void doTest(String msg, String test) throws ParseException {
    System.out.println(msg + ": " + test);
    DataShieldGrammar g = new DataShieldGrammar(new StringReader(test));
    SimpleNode expr = g.root();
    expr.dump("");
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
      public Object visit(ASTstring node, Object data) {
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
      public Object visit(ASTsubsetCall node, Object data) {
        StringBuilder sb = (StringBuilder) data;
        sb.append(node.value).append("[ ");
        for(int i = 0; i < node.jjtGetNumChildren(); i++) {
          Node child = node.jjtGetChild(i);
          if(i > 0) sb.append(' ');
          child.jjtAccept(this, sb);
        }
        sb.append(" ]");
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
    System.out.println(test + " --> " + b);

  }
}
