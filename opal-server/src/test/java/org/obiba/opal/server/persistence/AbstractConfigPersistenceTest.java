/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.server.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.After;
import org.junit.Before;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.UUID;

/**
 * A configuration database built the way the server builds it: schema from the Liquibase changelog, entities
 * validated against it rather than generating it.
 * <p>
 * It lives in opal-server because that is the only module with the whole configuration model on its classpath - most
 * of it is in opal-core-api, keystore states in opal-core, DataShield profiles in opal-datashield and R session
 * activity in opal-r.
 */
public abstract class AbstractConfigPersistenceTest {

  /**
   * The packages the running server scans, so that a package left out here would show up as a missing entity rather
   * than as a passing test.
   */
  protected static final String[] ENTITY_PACKAGES = {
      "org.obiba.opal.core.domain",
      "org.obiba.opal.core.runtime",
      "org.obiba.opal.datashield.cfg",
      "org.obiba.opal.r.service"
  };

  private LocalContainerEntityManagerFactoryBean factoryBean;

  private DataSource dataSource;

  @Before
  public void openConfigDatabase() throws Exception {
    dataSource = memoryDataSource();

    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog("classpath:db/changelog/config/db.changelog-master.xml");
    liquibase.setDatabaseChangeLogTable("opal_changelog");
    liquibase.setDatabaseChangeLogLockTable("opal_changelog_lock");
    liquibase.afterPropertiesSet();

    factoryBean = new LocalContainerEntityManagerFactoryBean();
    factoryBean.setDataSource(dataSource);
    factoryBean.setPersistenceUnitName("opal-config-test");
    factoryBean.setPackagesToScan(ENTITY_PACKAGES);
    factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

    Properties properties = new Properties();
    // Building the factory is itself the schema assertion: a column an entity expects and the changelog does not
    // declare fails here.
    properties.setProperty("hibernate.hbm2ddl.auto", "validate");
    factoryBean.setJpaProperties(properties);
    factoryBean.afterPropertiesSet();
  }

  @After
  public void closeConfigDatabase() {
    if(factoryBean != null) factoryBean.destroy();
  }

  protected EntityManagerFactory getEntityManagerFactory() {
    return factoryBean.getObject();
  }

  /**
   * Write an entity, then read it back through a second entity manager so the value comes from the database rather
   * than from the persistence context that just held it.
   */
  @SuppressWarnings("unchecked")
  protected <T> T roundTrip(T entity) {
    Object id;
    EntityManager writer = getEntityManagerFactory().createEntityManager();
    try {
      writer.getTransaction().begin();
      writer.persist(entity);
      writer.getTransaction().commit();
      id = getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
    } finally {
      writer.close();
    }

    EntityManager reader = getEntityManagerFactory().createEntityManager();
    try {
      return (T) reader.find(entity.getClass(), id);
    } finally {
      reader.close();
    }
  }

  private DataSource memoryDataSource() throws Exception {
    SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
    //noinspection unchecked
    dataSource.setDriverClass((Class) Class.forName("org.h2.Driver"));
    dataSource.setUrl("jdbc:h2:mem:opal-config-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
    dataSource.setUsername("sa");
    dataSource.setPassword("");
    return dataSource;
  }
}
