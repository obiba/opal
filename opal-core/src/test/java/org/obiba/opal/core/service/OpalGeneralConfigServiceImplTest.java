package org.obiba.opal.core.service;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OSchema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@ContextConfiguration(classes = OpalGeneralConfigServiceImplTest.Config.class)
public class OpalGeneralConfigServiceImplTest extends AbstractJUnit4SpringContextTests {

  @Autowired
  private OpalGeneralConfigService opalGeneralConfigService;

  @Autowired
  private OrientDbService orientDbService;

  @Before
  public void setUp() throws Exception {
    orientDbService.execute(new OrientDbService.WithinDocumentTxCallbackWithoutResult() {
      @Override
      protected void withinDocumentTxWithoutResult(ODatabaseDocumentTx db) {
        String className = OpalGeneralConfig.class.getSimpleName();
        OSchema schema = db.getMetadata().getSchema();
        if(schema.getClass(className) != null) {
          schema.dropClass(className);
        }
      }
    });
  }

  @Test(expected = OpalGeneralConfigMissingException.class)
  public void test_missing_config() {
    opalGeneralConfigService.getConfig();
  }

  @Test
  public void test_save() {
    OpalGeneralConfig config = new OpalGeneralConfig();
    config.setLocales(Lists.newArrayList(Locale.ENGLISH, Locale.FRENCH));
    opalGeneralConfigService.save(config);

    OpalGeneralConfig found = opalGeneralConfigService.getConfig();
    assertConfigEquals(config, found);
  }

  @Test
  public void test_update() {
    OpalGeneralConfig config = new OpalGeneralConfig();
    config.setLocales(Lists.newArrayList(Locale.ENGLISH, Locale.FRENCH));
    opalGeneralConfigService.save(config);

    config.setName("new name");
    config.getLocales().clear();
    opalGeneralConfigService.save(config);

    OpalGeneralConfig found = opalGeneralConfigService.getConfig();
    assertConfigEquals(config, found);
    Asserts.assertUpdatedTimestamps(config, found);

  }

  private void assertConfigEquals(OpalGeneralConfig config, OpalGeneralConfig found) {
    assertNotNull(found);
    assertEquals(config, found);
    assertEquals(config.getName(), found.getName());
    assertEquals(config.getDefaultCharacterSet(), found.getDefaultCharacterSet());
    assertEquals(config.getLocalesAsString(), found.getLocalesAsString());
    Asserts.assertCreatedTimestamps(config, found);
  }

  @Configuration
  public static class Config extends AbstractOrientDbTestConfig {

    @Bean
    public OpalGeneralConfigService userService() {
      return new OpalGeneralConfigServiceImpl();
    }

  }
}
