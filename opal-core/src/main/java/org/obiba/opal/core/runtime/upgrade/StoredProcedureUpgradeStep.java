/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.upgrade;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.obiba.runtime.upgrade.support.jdbc.SqlScriptUpgradeStep;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

/**
 * Allows creating a store procedure, execute it and then drop it. This is useful for executing upgrade steps that are
 * too complex to write as simple SQL update statements.
 */
public class StoredProcedureUpgradeStep extends SqlScriptUpgradeStep {

  private String procedureName;

  public void setProcedureName(String procedureName) {
    this.procedureName = procedureName;
  }

  @Override
  protected void executeScript(DataSource dataSource, Resource script) {
    SimpleJdbcTemplate template = new SimpleJdbcTemplate(dataSource);
    String sql = readFully(script);
    template.update(sql);
    template.getJdbcOperations().execute("CALL " + procedureName + "()", new CallableStatementCallback<Boolean>() {

      @Override
      public Boolean doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
        return cs.execute();
      }
    });

    template.update("DROP PROCEDURE " + procedureName);
  }

  private String readFully(Resource script) {
    Reader reader = null;
    try {
      reader = new InputStreamReader(script.getInputStream());
      StringBuilder sb = new StringBuilder();
      CharStreams.copy(reader, sb);
      return sb.toString();
    } catch(IOException e) {
      throw new RuntimeException(e);
    } finally {
      Closeables.closeQuietly(reader);
    }
  }
}
