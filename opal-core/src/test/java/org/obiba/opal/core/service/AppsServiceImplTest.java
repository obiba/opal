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

import com.google.common.eventbus.EventBus;
import org.obiba.opal.core.cfg.AppsService;
import org.junit.Test;
import org.obiba.opal.core.repository.AppRepository;
import org.obiba.opal.core.repository.AppsConfigRepository;
import org.obiba.opal.core.runtime.App;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.util.NoSuchElementException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * An application that never completed registration has no identifier, and the questions asked about it have to be
 * answerable anyway. The document store answered them by itself, a lookup on a null key matching nothing; a
 * repository rejects the null, which is how a scheduled Rock discovery check ended up failing with "The given id must
 * not be null" instead of concluding the application was gone.
 */
@ContextConfiguration(classes = AppsServiceImplTest.Config.class)
public class AppsServiceImplTest extends AbstractConfigDbTest {

  @Autowired
  private AppsService appsService;

  @Autowired
  private AppRepository appRepository;

  @Test
  public void test_has_app_says_no_for_a_missing_identifier() {
    assertThat(appsService.hasApp(null)).isFalse();
    assertThat(appsService.hasApp("")).isFalse();
  }

  @Test
  public void test_has_app_answers_for_a_real_identifier() {
    App app = new App("rock-1");
    app.setName("rock");
    app.setType("rock");
    appRepository.upsert(app);

    assertThat(appsService.hasApp("rock-1")).isTrue();
    assertThat(appsService.hasApp("rock-2")).isFalse();
  }

  @Test(expected = NoSuchElementException.class)
  public void test_get_app_with_no_identifier_is_a_missing_app() {
    appsService.getApp(null);
  }

  @Test(expected = NoSuchElementException.class)
  public void test_get_app_with_an_unknown_identifier_is_a_missing_app() {
    appsService.getApp("rock-nope");
  }

  @Configuration
  public static class Config extends AbstractConfigDbTestConfig {

    @Override
    protected void appendProperties(java.util.Properties properties) {
      properties.setProperty("apps.registration.token", "");
      properties.setProperty("apps.registration.include", "");
      properties.setProperty("apps.registration.exclude", "");
      properties.setProperty("apps.discovery.rock.hosts", "");
    }

    @Bean
    public EventBus eventBus() {
      return new EventBus();
    }

    @Bean
    public AppsService appsService(AppRepository appRepository, AppsConfigRepository appsConfigRepository) {
      return new AppsServiceImpl(appRepository, appsConfigRepository, eventBus());
    }
  }
}
