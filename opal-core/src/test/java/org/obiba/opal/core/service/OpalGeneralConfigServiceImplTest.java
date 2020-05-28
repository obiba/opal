/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import java.util.Locale;

import com.google.common.eventbus.EventBus;
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

import static org.fest.assertions.api.Assertions.assertThat;

@ContextConfiguration(classes = OpalGeneralConfigServiceImplTest.Config.class)
public class OpalGeneralConfigServiceImplTest extends AbstractJUnit4SpringContextTests {

  @Autowired
  private OpalGeneralConfigService opalGeneralConfigService;

  @Autowired
  private OrientDbService orientDbService;

  @Autowired
  private EventBus eventBus;

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
    assertThat(found).isNotNull();
    assertThat(config).isEqualTo(found);
    assertThat(config.getName()).isEqualTo(found.getName());
    assertThat(config.getDefaultCharacterSet()).isEqualTo(found.getDefaultCharacterSet());
    assertThat(config.getLocalesAsString()).isEqualTo(found.getLocalesAsString());
    //Asserts.assertCreatedTimestamps(config, found);
  }

  @Configuration
  public static class Config extends AbstractOrientDbTestConfig {

    @Bean
    public EventBus eventBus() {
      return new EventBus();
    }

    @Bean
    public OpalGeneralConfigService userService() {
      return new OpalGeneralConfigServiceImpl(orientDbService(), eventBus());
    }

  }
}
