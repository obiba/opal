/*
 * Copyright (c) 2024 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.runtime;

import java.net.URISyntaxException;

import javax.cache.Caching;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfiguration {

  /**
   * Closed with the application, so that the caches kept on disk are found again at the next start.
   */
  @Bean(destroyMethod = "close")
  public javax.cache.CacheManager opalJCacheManager() throws URISyntaxException {
    return Caching.getCachingProvider()
        .getCacheManager(getClass().getResource("/ehcache-opal.xml").toURI(), getClass().getClassLoader());
  }

  @Bean
  public CacheManager cacheManager(javax.cache.CacheManager opalJCacheManager) {
    return new JCacheCacheManager(opalJCacheManager);
  }

}
