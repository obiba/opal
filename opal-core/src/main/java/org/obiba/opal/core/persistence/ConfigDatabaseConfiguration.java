/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.persistence;

import com.google.common.base.Strings;
import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.runtime.jdbc.DataSourceFactoryBean;
import org.obiba.opal.core.service.security.CryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * The Opal configuration database: projects, permissions, users, registered databases and the rest of the
 * configuration Opal keeps for itself. It is an embedded H2 database by default, and can be pointed at an external
 * server (PostgreSQL) through {@code org.obiba.opal.config.datasource.*} in {@code opal-config.properties}.
 * <p>
 * Three things about this wiring are deliberate.
 * <p>
 * <b>Its credentials are the ones Opal already generates.</b> The embedded database is opened as {@code opal} with the
 * {@code <databasePassword>} of {@code ${OPAL_HOME}/data/opal-config.xml}, generated on first boot and stored there
 * encrypted under the {@code <secretKey>} of that same file. Nothing new for an administrator to configure, and no
 * password in a properties file. An external database is different: there Opal is a guest, the password is the
 * server's, and it has to be given as {@code org.obiba.opal.config.datasource.password}.
 * <p>
 * <b>It is built after {@link OpalConfigurationService} has started.</b> The password is not known until the
 * configuration file has been read and, on a fresh installation, written back with a freshly generated one. Injecting
 * the service rather than reading a placeholder is what orders the two.
 * <p>
 * <b>It has its own transaction manager.</b> The application's {@code transactionManager} is the Atomikos JTA one, and
 * it belongs to the Magma datasources that import, export and copy data. Configuration writes have never taken part in
 * those transactions and must not start now, so they get {@code configTransactionManager} instead.
 */
@Configuration
@EnableJpaRepositories(
    // Repositories live in a `repository` package of the module that owns their entities. Naming the packages rather
    // than scanning `org.obiba.opal` keeps startup from walking every class in the application.
    basePackages = {
        "org.obiba.opal.core.repository",
        "org.obiba.opal.datashield.repository",
        "org.obiba.opal.r.repository"
    },
    entityManagerFactoryRef = "configEntityManagerFactory",
    transactionManagerRef = "configTransactionManager")
public class ConfigDatabaseConfiguration {

  private static final Logger log = LoggerFactory.getLogger(ConfigDatabaseConfiguration.class);

  /**
   * Entity packages, scanned recursively. The configuration model is spread over three modules: most of it is in
   * opal-core-api, DataShield profiles are in opal-datashield and R session activity is in opal-r. opal-core cannot
   * name the last two as classes, only as packages.
   */
  private static final String[] ENTITY_PACKAGES = {
      "org.obiba.opal.core.domain",
      "org.obiba.opal.core.runtime",
      "org.obiba.opal.datashield.cfg",
      "org.obiba.opal.r.service"
  };

  private static final String DEFAULT_DRIVER_CLASS = "org.h2.Driver";

  private static final String DEFAULT_USERNAME = "opal";

  private static final String DATABASE_NAME = "opal-config";

  /**
   * SQL state for an invalid authorization specification, which every JDBC driver reports the same way. H2 gives no
   * more than "Wrong user name or password", so the cause has to be explained here.
   */
  private static final String SQLSTATE_INVALID_AUTHORIZATION = "28000";

  /**
   * The configuration database lives in its own folder, not in {@code data/h2} where the databases registered by users
   * are: those are named by the user, and one of them could otherwise be called {@code opal-config}.
   */
  @Value("${OPAL_HOME}/data/config")
  private File configFolder;

  @Value("${org.obiba.opal.config.datasource.url:}")
  private String url;

  @Value("${org.obiba.opal.config.datasource.driverClass:}")
  private String driverClass;

  @Value("${org.obiba.opal.config.datasource.username:}")
  private String username;

  @Value("${org.obiba.opal.config.datasource.password:}")
  private String password;

  @Value("${org.obiba.opal.config.hibernate.dialect:}")
  private String dialect;

  @Bean
  public DataSource configDataSource(OpalConfigurationService opalConfigurationService, CryptoService cryptoService) {
    String jdbcUrl = resolveUrl();
    boolean embedded = isEmbedded();

    DataSourceFactoryBean factoryBean = new DataSourceFactoryBean();
    factoryBean.setDriverClass(Strings.isNullOrEmpty(driverClass) ? DEFAULT_DRIVER_CLASS : driverClass);
    factoryBean.setUrl(jdbcUrl);
    factoryBean.setUsername(Strings.isNullOrEmpty(username) ? DEFAULT_USERNAME : username);
    factoryBean.setPassword(resolvePassword(opalConfigurationService, cryptoService, embedded));

    log.info("Opal configuration database: {}", jdbcUrl);
    DataSource dataSource = factoryBean.getObject();
    verifyConnection(dataSource, jdbcUrl, embedded);
    return dataSource;
  }

  /**
   * Bring the schema up to date before anything reads it. Hibernate only validates (see
   * {@link #configEntityManagerFactory}), so this is the single place the schema is created or changed.
   */
  @Bean
  public SpringLiquibase configLiquibase(DataSource configDataSource) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(configDataSource);
    liquibase.setChangeLog("classpath:db/changelog/config/db.changelog-master.xml");
    // Prefixed, because on an external server the configuration schema may not be alone.
    liquibase.setDatabaseChangeLogTable("opal_changelog");
    liquibase.setDatabaseChangeLogLockTable("opal_changelog_lock");
    return liquibase;
  }

  @Bean
  @DependsOn("configLiquibase")
  public LocalContainerEntityManagerFactoryBean configEntityManagerFactory(DataSource configDataSource) {
    LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
    factoryBean.setDataSource(configDataSource);
    factoryBean.setPersistenceUnitName("opal-config");
    factoryBean.setPackagesToScan(ENTITY_PACKAGES);
    factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

    Properties properties = new Properties();
    // Liquibase owns the schema. Validating rather than updating means an entity that has drifted from the changelog
    // fails at startup, instead of Hibernate quietly reshaping a database that holds the only copy of a
    // configuration.
    properties.setProperty(AvailableSettings.HBM2DDL_AUTO, "validate");
    // Atomikos is on the classpath for the Magma datasources, and Hibernate will bind itself to it on sight -
    // "Using JTA platform [AtomikosJtaPlatform]". That is the opposite of what this persistence unit is for: it is
    // driven by its own JpaTransactionManager over plain JDBC, and must stay out of the transactions that import,
    // export and copy data.
    properties.setProperty(AvailableSettings.JTA_PLATFORM, NoJtaPlatform.class.getName());
    properties.setProperty(AvailableSettings.TRANSACTION_COORDINATOR_STRATEGY, "jdbc");
    // A backstop, not the mechanism: the enumerations in the configuration model go through an EnumNameConverter,
    // because this setting alone does not keep them out of a native type - for @Enumerated(STRING) Hibernate consults
    // the dialect's inline ENUM descriptor before ever reading it, which is how H2 ends up with
    // `enum ('EXPORT','IMPORT','STORAGE')`. What it does prevent is a future @Enumerated field silently acquiring a
    // `create type ... as enum` of its own on PostgreSQL.
    properties.setProperty(AvailableSettings.PREFER_NATIVE_ENUM_TYPES, "false");
    // Only the entities with an application-assigned key can actually be batched - an identity column has to be read
    // back per row - but that is the R session activity, which is the one table large enough for it to matter.
    properties.setProperty(AvailableSettings.STATEMENT_BATCH_SIZE, "50");
    if(!Strings.isNullOrEmpty(dialect)) {
      properties.setProperty(AvailableSettings.DIALECT, dialect);
    }
    factoryBean.setJpaProperties(properties);
    return factoryBean;
  }

  @Bean
  public PlatformTransactionManager configTransactionManager(EntityManagerFactory configEntityManagerFactory) {
    return new JpaTransactionManager(configEntityManagerFactory);
  }

  /**
   * The embedded database is addressed by folder, the way the OrientDB one was; an external one is addressed by the
   * URL an administrator configured.
   */
  private String resolveUrl() {
    if(!Strings.isNullOrEmpty(url)) return url;
    if(!configFolder.exists() && !configFolder.mkdirs() && !configFolder.exists()) {
      throw new ConfigDatabaseException(
          "Cannot create the Opal configuration database folder: " + configFolder.getAbsolutePath());
    }
    return "jdbc:h2:file:" + new File(configFolder, DATABASE_NAME).getAbsolutePath();
  }

  private boolean isEmbedded() {
    return Strings.isNullOrEmpty(url);
  }

  private String resolvePassword(OpalConfigurationService opalConfigurationService, CryptoService cryptoService,
                                 boolean embedded) {
    if(!Strings.isNullOrEmpty(password)) return password;
    if(!embedded) {
      throw new ConfigDatabaseException(
          "No password for the Opal configuration database at " + url + ". An external configuration database needs " +
              "org.obiba.opal.config.datasource.password in opal-config.properties; only the embedded database uses " +
              "the password Opal generates for itself.");
    }
    String encrypted = opalConfigurationService.getOpalConfiguration().getDatabasePassword();
    if(Strings.isNullOrEmpty(encrypted)) {
      throw new ConfigDatabaseException(
          "No <databasePassword> in the Opal configuration file. It is generated on first startup, so an empty one " +
              "means the file was edited or truncated.");
    }
    return cryptoService.decrypt(encrypted);
  }

  /**
   * Open a connection now rather than letting the first Liquibase statement fail: at this point it is still possible
   * to say which of the two files is the problem.
   */
  private void verifyConnection(DataSource dataSource, String jdbcUrl, boolean embedded) {
    try(Connection ignored = dataSource.getConnection()) {
      // opening it is the check
    } catch(SQLException e) {
      throw new ConfigDatabaseException(explain(e, jdbcUrl, embedded), e);
    }
  }

  private String explain(SQLException e, String jdbcUrl, boolean embedded) {
    if(embedded && isInvalidAuthorization(e)) {
      // Worth spelling out: the database and the password that opens it are in two different directories, which get
      // backed up and restored separately more often than one would like.
      return "Cannot open the Opal configuration database at " + jdbcUrl + ": the password does not match.\n" +
          "The password is the <databasePassword> of " + configFolder.getParentFile().getAbsolutePath() +
          File.separator + "opal-config.xml, encrypted with the <secretKey> of that same file.\n" +
          "It no longer opens the database if that file and the database folder come from different installations, " +
          "or if the secret key was replaced.";
    }
    return "Cannot open the Opal configuration database at " + jdbcUrl + ": " + e.getMessage();
  }

  /**
   * The connection pool reports its own failure to build a connection and keeps the driver's exception as a cause, so
   * the SQL state that says what actually went wrong is somewhere down the chain rather than on top.
   */
  private boolean isInvalidAuthorization(SQLException e) {
    for(Throwable t = e; t != null; t = t.getCause()) {
      if(t instanceof SQLException && SQLSTATE_INVALID_AUTHORIZATION.equals(((SQLException) t).getSQLState())) {
        return true;
      }
    }
    return false;
  }
}
