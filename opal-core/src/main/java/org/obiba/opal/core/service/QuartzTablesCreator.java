/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Transactional
@SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
public class QuartzTablesCreator {

  private static final Logger log = LoggerFactory.getLogger(QuartzTablesCreator.class);

  private static final char DEFAULT_STATEMENT_SEPARATOR = ';';

  private static final String DEFAULT_COMMENT_PREFIX = "--";

  private DataSource dataSource;

  private Resource script;

  @PostConstruct
  public void createTablesIfNeeded() {
    if(quartzTablesExist()) {
      log.info("Don't Quartz tables as they already exist");
    } else {
      log.info("Create Quartz tables");
      executeSqlScript(new JdbcTemplate(dataSource), new EncodedResource(script), false);
    }
  }

  private boolean quartzTablesExist() {
    try {
      DatabaseMetaData meta = dataSource.getConnection().getMetaData();
      ResultSet res = meta.getTables(null, null, null, new String[] { "TABLE" });
      while(res.next()) {
        if("qrtz_locks".equalsIgnoreCase(res.getString("TABLE_NAME"))) {
          return true;
        }
      }
    } catch(SQLException e) {
      log.error("Cannot check if config database contains 'qrtz_locks' table", e);
    }
    return false;
  }

  /**
   * Copied from {@link org.springframework.test.jdbc.JdbcTestUtils.executeSqlScript}
   * to avoid dependency on spring-test at runtime
   */
  public static void executeSqlScript(JdbcOperations jdbcTemplate, EncodedResource resource, boolean continueOnError)
      throws DataAccessException {

    if(log.isInfoEnabled()) {
      log.info("Executing SQL script from {}", resource);
    }
    long startTime = System.currentTimeMillis();
    Collection<String> statements = new LinkedList<>();
    LineNumberReader reader = null;
    try {
      reader = new LineNumberReader(resource.getReader());
      String script = readScript(reader, DEFAULT_COMMENT_PREFIX);
      char delimiter = DEFAULT_STATEMENT_SEPARATOR;
      if(!containsSqlScriptDelimiters(script, delimiter)) {
        delimiter = '\n';
      }
      splitSqlScript(script, delimiter, statements);
      int lineNumber = 0;
      for(String statement : statements) {
        lineNumber++;
        try {
          int rowsAffected = jdbcTemplate.update(statement);
          if(log.isDebugEnabled()) {
            log.debug("{} rows affected by SQL: {}", rowsAffected, statement);
          }
        } catch(DataAccessException ex) {
          if(continueOnError) {
            if(log.isWarnEnabled()) {
              log.warn("Failed to execute SQL script statement at line {} of resource {}: {}", lineNumber, resource,
                  statement, ex);
            }
          } else {
            throw ex;
          }
        }
      }
      long elapsedTime = System.currentTimeMillis() - startTime;
      if(log.isInfoEnabled()) {
        log.info(String.format("Executed SQL script from %s in %s ms.", resource, elapsedTime));
      }
    } catch(IOException ex) {
      throw new DataAccessResourceFailureException("Failed to open SQL script from " + resource, ex);
    } finally {
      try {
        if(reader != null) {
          reader.close();
        }
      } catch(IOException ex) {
        // ignore
      }
    }
  }

  /**
   * Read a script from the provided {@code LineNumberReader}, using the supplied
   * comment prefix, and build a {@code String} containing the lines.
   * <p>Lines <em>beginning</em> with the comment prefix are excluded from the
   * results; however, line comments anywhere else &mdash; for example, within
   * a statement &mdash; will be included in the results.
   *
   * @param lineNumberReader the {@code LineNumberReader} containing the script
   * to be processed
   * @param commentPrefix the prefix that identifies comments in the SQL script &mdash; typically "--"
   * @return a {@code String} containing the script lines
   */
  public static String readScript(LineNumberReader lineNumberReader, String commentPrefix) throws IOException {
    String currentStatement = lineNumberReader.readLine();
    StringBuilder scriptBuilder = new StringBuilder();
    while(currentStatement != null) {
      if(StringUtils.hasText(currentStatement) && commentPrefix != null &&
          !currentStatement.startsWith(commentPrefix)) {
        if(scriptBuilder.length() > 0) {
          scriptBuilder.append('\n');
        }
        scriptBuilder.append(currentStatement);
      }
      currentStatement = lineNumberReader.readLine();
    }
    return scriptBuilder.toString();
  }

  /**
   * Determine if the provided SQL script contains the specified delimiter.
   *
   * @param script the SQL script
   * @param delim character delimiting each statement &mdash; typically a ';' character
   * @return {@code true} if the script contains the delimiter; {@code false} otherwise
   */
  public static boolean containsSqlScriptDelimiters(String script, char delim) {
    boolean inLiteral = false;
    char[] content = script.toCharArray();
    for(int i = 0; i < script.length(); i++) {
      if(content[i] == '\'') {
        inLiteral = !inLiteral;
      }
      if(content[i] == delim && !inLiteral) {
        return true;
      }
    }
    return false;
  }

  /**
   * Split an SQL script into separate statements delimited by the provided
   * delimiter character. Each individual statement will be added to the
   * provided {@code List}.
   * <p>Within a statement, "{@code --}" will be used as the comment prefix;
   * any text beginning with the comment prefix and extending to the end of
   * the line will be omitted from the statement. In addition, multiple adjacent
   * whitespace characters will be collapsed into a single space.
   *
   * @param script the SQL script
   * @param delim character delimiting each statement &mdash; typically a ';' character
   * @param statements the list that will contain the individual statements
   */
  public static void splitSqlScript(String script, char delim, Collection<String> statements) {
    splitSqlScript(script, "" + delim, DEFAULT_COMMENT_PREFIX, statements);
  }

  /**
   * Split an SQL script into separate statements delimited by the provided
   * delimiter string. Each individual statement will be added to the provided
   * {@code List}.
   * <p>Within a statement, the provided {@code commentPrefix} will be honored;
   * any text beginning with the comment prefix and extending to the end of the
   * line will be omitted from the statement. In addition, multiple adjacent
   * whitespace characters will be collapsed into a single space.
   *
   * @param script the SQL script
   * @param delim character delimiting each statement &mdash; typically a ';' character
   * @param commentPrefix the prefix that identifies line comments in the SQL script &mdash; typically "--"
   * @param statements the List that will contain the individual statements
   */
  @SuppressWarnings("AssignmentToForLoopParameter")
  private static void splitSqlScript(String script, String delim, String commentPrefix, Collection<String> statements) {
    StringBuilder sb = new StringBuilder();
    boolean inLiteral = false;
    boolean inEscape = false;
    char[] content = script.toCharArray();
    for(int i = 0; i < script.length(); i++) {
      char c = content[i];
      if(inEscape) {
        inEscape = false;
        sb.append(c);
        continue;
      }
      // MySQL style escapes
      if(c == '\\') {
        inEscape = true;
        sb.append(c);
        continue;
      }
      if(c == '\'') {
        inLiteral = !inLiteral;
      }
      if(!inLiteral) {
        if(script.startsWith(delim, i)) {
          // we've reached the end of the current statement
          if(sb.length() > 0) {
            statements.add(sb.toString());
            sb = new StringBuilder();
          }
          i += delim.length() - 1;
          continue;
        } else if(script.startsWith(commentPrefix, i)) {
          // skip over any content from the start of the comment to the EOL
          int indexOfNextNewline = script.indexOf("\n", i);
          if(indexOfNextNewline > i) {
            i = indexOfNextNewline;
            continue;
          } else {
            // if there's no newline after the comment, we must be at the end
            // of the script, so stop here.
            break;
          }
        } else if(c == ' ' || c == '\n' || c == '\t') {
          // avoid multiple adjacent whitespace characters
          if(sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
            c = ' ';
          } else {
            continue;
          }
        }
      }
      sb.append(c);
    }
    if(StringUtils.hasText(sb)) {
      statements.add(sb.toString());
    }
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setScript(Resource script) {
    this.script = script;
  }
}
