/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.sql;

import com.google.common.collect.Sets;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class SQLExtractor {

  private static final Logger log = LoggerFactory.getLogger(SQLExtractor.class);

  public static Set<String> extractTables(String sql) throws SQLParserException {
    try {
      Set<String> tables = Sets.newLinkedHashSet();
      SQLLexer lexer = new SQLLexer(CharStreams.fromString(sql));
      CommonTokenStream tokenStream = new CommonTokenStream(lexer);
      SQLParser parser = new SQLParser(tokenStream);
      parser.parse().accept(new SQLParserBaseVisitor<Void>() {
        @Override
        public Void visitCreate_table_stmt(SQLParser.Create_table_stmtContext ctx) {
          throw new SQLParserException("CREATE TABLE statement not supported");
        }

        @Override
        public Void visitCreate_view_stmt(SQLParser.Create_view_stmtContext ctx) {
          throw new SQLParserException("CREATE VIEW statement not supported");
        }

        @Override
        public Void visitCreate_virtual_table_stmt(SQLParser.Create_virtual_table_stmtContext ctx) {
          throw new SQLParserException("CREATE VIRTUAL TABLE statement not supported");
        }

        @Override
        public Void visitDrop_stmt(SQLParser.Drop_stmtContext ctx) {
          throw new SQLParserException("DROP statement not supported");
        }

        @Override
        public Void visitAlter_table_stmt(SQLParser.Alter_table_stmtContext ctx) {
          throw new SQLParserException("ALTER TABLE statement not supported");
        }

        @Override
        public Void visitInsert_stmt(SQLParser.Insert_stmtContext ctx) {
          throw new SQLParserException("INSERT statement not supported");
        }

        @Override
        public Void visitDelete_stmt(SQLParser.Delete_stmtContext ctx) {
          throw new SQLParserException("DELETE statement not supported");
        }

        @Override
        public Void visitUpdate_stmt(SQLParser.Update_stmtContext ctx) {
          throw new SQLParserException("UPDATE statement not supported");
        }

        @Override
        public Void visitTable_name(SQLParser.Table_nameContext ctx) {
          log.info("table {} from context {}", ctx.getText(), ctx.getParent().getClass().getSimpleName());
          if (ctx.getParent() instanceof SQLParser.Table_or_subqueryContext)
            tables.add(ctx.getText());
          return super.visitTable_name(ctx);
        }

      });

      return tables;
    } catch (Exception e) {
      throw new SQLParserException(e);
    }
  }

}
