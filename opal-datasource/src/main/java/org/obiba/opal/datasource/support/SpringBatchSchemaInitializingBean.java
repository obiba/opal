package org.obiba.opal.datasource.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.sql.DataSource;

import org.obiba.core.util.StreamUtil;
import org.springframework.batch.support.DatabaseType;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.test.jdbc.SimpleJdbcTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class SpringBatchSchemaInitializingBean implements ResourceLoaderAware {

  private ResourceLoader resourceLoader;

  private DataSource dataSource;

  private PlatformTransactionManager transactionManager;

  private String tablePrefix;

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setTablePrefix(String tablePrefix) {
    this.tablePrefix = tablePrefix;
  }

  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public void initializeSchema() throws MetaDataAccessException, DataAccessException, IOException {
    DatabaseType type = DatabaseType.fromMetaData(dataSource);
    Resource schemaResource = resourceLoader.getResource(getSchemaScriptName(type));
    if(schemaResource.exists() == false) {
      throw new IllegalStateException("No schema initializing script ");
    }

    InputStream is = null;
    try {
      String script = mergeLinesAndRemoveComments(StreamUtil.readLines(is = schemaResource.getInputStream()));
      final ByteArrayResource scriptResource = new ByteArrayResource(script.getBytes("ISO-8859-1"));

      new TransactionTemplate(transactionManager).execute(new TransactionCallback() {

        public Object doInTransaction(TransactionStatus status) {
          SimpleJdbcTestUtils.executeSqlScript(new SimpleJdbcTemplate(dataSource), scriptResource, false);
          return null;
        }
      });
    } catch(UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    } finally {
      StreamUtil.silentSafeClose(is);
    }

  }

  protected String mergeLinesAndRemoveComments(List<String> lines) {
    StringBuilder sb = new StringBuilder();
    for(String line : lines) {
      if(line.startsWith("//") || line.startsWith("--")) {
        continue;
      }
      sb.append(applyTablePrefix(line)).append("\n");
    }
    return sb.toString();
  }

  protected String applyTablePrefix(String line) {
    if(tablePrefix != null && tablePrefix.equals("BATCH_") == false) {
      return line.replaceAll("CREATE TABLE BATCH_", "CREATE TABLE " + tablePrefix);
    } else {
      return line;
    }
  }

  protected String getSchemaScriptName(DatabaseType type) {
    return new StringBuilder("classpath:/schema-").append(type.toString().toLowerCase()).append(".sql").toString();
  }
}
