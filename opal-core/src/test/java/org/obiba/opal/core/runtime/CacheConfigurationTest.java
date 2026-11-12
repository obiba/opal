/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.runtime;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obiba.magma.MagmaCacheExtension;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.lang.VariableEntityList;
import org.obiba.magma.support.VariableEntitiesCache;
import org.obiba.magma.support.VariableEntityBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;

import static org.fest.assertions.api.Assertions.assertThat;

public class CacheConfigurationTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  private String opalHome;

  @Before
  public void setUp() {
    opalHome = System.getProperty("OPAL_HOME");
    System.setProperty("OPAL_HOME", folder.getRoot().getAbsolutePath());
  }

  @After
  public void tearDown() {
    if (opalHome == null) System.clearProperty("OPAL_HOME");
    else System.setProperty("OPAL_HOME", opalHome);
  }

  @Test
  public void test_view_entities_are_kept_across_restarts() throws Exception {
    VariableEntity entity = new VariableEntityBean("Participant", "1");
    VariableEntitiesCache cached = new VariableEntitiesCache(new VariableEntityList(List.of(entity)), 1L);

    javax.cache.CacheManager jcache = new CacheConfiguration().opalJCacheManager();
    try {
      MagmaCacheExtension extension = new MagmaCacheExtension(cacheManager(jcache));
      assertThat(extension.hasVariableEntitiesCache()).isTrue();
      assertThat(extension.hasValueSetsCache()).isFalse();
      extension.getVariableEntitiesCache().put("ds.view", cached);
      // written to disk, then read once into memory, where each count finds it as it is
      Object read = extension.getVariableEntitiesCache().get("ds.view").get();
      assertThat(extension.getVariableEntitiesCache().get("ds.view").get()).isSameAs(read);
    } finally {
      jcache.close();
    }
    assertThat(new File(folder.getRoot(), "work/ehcache-opal")).isDirectory();

    jcache = new CacheConfiguration().opalJCacheManager();
    try {
      Cache.ValueWrapper wrapper = cacheManager(jcache).getCache("magma-table-entities").get("ds.view");
      assertThat(wrapper).isNotNull();
      VariableEntitiesCache restored = (VariableEntitiesCache) wrapper.get();
      assertThat(restored.getEntities()).containsExactly(entity);
      assertThat(restored.getEntities().contains(entity)).isTrue();
    } finally {
      jcache.close();
    }
  }

  private static CacheManager cacheManager(javax.cache.CacheManager jcache) {
    JCacheCacheManager cacheManager = (JCacheCacheManager) new CacheConfiguration().cacheManager(jcache);
    cacheManager.afterPropertiesSet();
    return cacheManager;
  }
}
