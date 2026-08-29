/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.easymock.EasyMock;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

/**
 * A configuration database for service tests: schema from the Liquibase changelog, entities validated against it, and
 * the repositories of {@code org.obiba.opal.core.repository} as beans, which is what the services autowire.
 * <p>
 * Each test class gets its own in-memory database. Within a class the tests share it, as they did when this was a
 * folder holding an OrientDB database, so the tests that need a clean slate still clear it themselves.
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "org.obiba.opal.core.repository",
    entityManagerFactoryRef = "configEntityManagerFactory",
    transactionManagerRef = "configTransactionManager")
@SuppressWarnings("ResultOfMethodCallIgnored")
public abstract class AbstractConfigDbTestConfig {

  private static final File TEMP_FILE;

  static {
    try {
      TEMP_FILE = File.createTempFile("opal-test-", "");
      TEMP_FILE.delete();
      TEMP_FILE.mkdirs();
      TEMP_FILE.deleteOnExit();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Bean
  public PropertySourcesPlaceholderConfigurer propertiesResolver() {
    PropertySourcesPlaceholderConfigurer placeholderConfigurer = new PropertySourcesPlaceholderConfigurer();
    Properties properties = new Properties();
    properties.setProperty("OPAL_HOME", TEMP_FILE.getAbsolutePath());
    appendProperties(properties);
    placeholderConfigurer.setProperties(properties);
    return placeholderConfigurer;
  }

  @Bean
  public DataSource configDataSource() throws Exception {
    SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
    //noinspection unchecked
    dataSource.setDriverClass((Class) Class.forName("org.h2.Driver"));
    dataSource.setUrl("jdbc:h2:mem:opal-config-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
    dataSource.setUsername("sa");
    dataSource.setPassword("");
    return dataSource;
  }

  @Bean
  public SpringLiquibase configLiquibase(DataSource configDataSource) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(configDataSource);
    liquibase.setChangeLog("classpath:db/changelog/config/db.changelog-master.xml");
    liquibase.setDatabaseChangeLogTable("opal_changelog");
    liquibase.setDatabaseChangeLogLockTable("opal_changelog_lock");
    return liquibase;
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean configEntityManagerFactory(DataSource configDataSource,
                                                                          SpringLiquibase configLiquibase) {
    LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
    factoryBean.setDataSource(configDataSource);
    factoryBean.setPersistenceUnitName("opal-config-test");
    factoryBean.setPackagesToScan("org.obiba.opal.core.domain", "org.obiba.opal.core.runtime");
    factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

    Properties properties = new Properties();
    properties.setProperty("hibernate.hbm2ddl.auto", "validate");
    properties.setProperty("hibernate.transaction.jta.platform",
        "org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform");
    properties.setProperty("hibernate.transaction.coordinator_class", "jdbc");
    factoryBean.setJpaProperties(properties);
    return factoryBean;
  }

  @Bean
  public PlatformTransactionManager configTransactionManager(EntityManagerFactory configEntityManagerFactory) {
    return new JpaTransactionManager(configEntityManagerFactory);
  }

  @Bean
  public OpalConfigurationService opalConfigurationService() {
    OpalConfiguration configuration = new OpalConfiguration();
    configuration.setDatabasePassword("admin");
    configuration.setSecretKey("testsecretkey1234");
    OpalConfigurationService mock = EasyMock.createMock(OpalConfigurationService.class);
    mock.afterPropertiesSet();
    EasyMock.expect(mock.getOpalConfiguration()).andReturn(configuration).anyTimes();
    EasyMock.replay(mock);
    return mock;
  }

  protected void appendProperties(Properties properties) {

  }

}
