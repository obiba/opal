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

import jakarta.persistence.metamodel.EntityType;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Holds the changelog and the entities to each other. Liquibase owns the schema and Hibernate only validates it, so a
 * column added to an entity and forgotten in the changelog - or renamed in one and not the other - has to fail
 * somewhere. It fails in {@link AbstractConfigPersistenceTest#openConfigDatabase()}, which is what the server does at
 * startup.
 */
public class ConfigSchemaTest extends AbstractConfigPersistenceTest {

  /**
   * The configuration model, listed rather than counted, so that an entity dropped from the scan fails with a name in
   * the message instead of a number that no longer matches.
   */
  private static final Set<String> EXPECTED_ENTITIES = Set.of(
      "App", "AppsConfig", "Database", "DataShieldProfile", "Group", "KeyStoreState", "OpalAnalysis",
      "OpalAnalysisResult", "OpalGeneralConfig", "PodSpec", "Project", "ResourceReference", "RQuota",
      "RSessionActivity", "SubjectAcl", "SubjectCredentials", "SubjectProfile", "SubjectToken", "VCFSamplesMapping");

  @Test
  public void test_the_whole_configuration_model_is_mapped() {
    Set<String> mapped = getEntityManagerFactory().getMetamodel().getEntities().stream()
        .map(EntityType::getName)
        .collect(Collectors.toSet());
    assertThat(mapped).isEqualTo(EXPECTED_ENTITIES);
  }
}
