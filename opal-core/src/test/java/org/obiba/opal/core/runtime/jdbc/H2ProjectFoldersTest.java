/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
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

import java.io.File;
import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;

public class H2ProjectFoldersTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void test_delete_takes_everything_h2_wrote_beside_the_store() throws IOException {
    File folder = temporaryFolder.newFolder("h2", "CLSA");
    assertThat(new File(folder, "data.mv.db").createNewFile()).isTrue();
    assertThat(new File(folder, "data.trace.db").createNewFile()).isTrue();

    H2ProjectFolders.delete(folder);

    assertThat(folder.exists()).isFalse();
    assertThat(folder.getParentFile().list()).isEmpty();
  }

  @Test
  public void test_delete_of_a_folder_that_was_never_opened() {
    // the folder is created at the first connection, so a project deleted right after it was created has none
    File h2Root = temporaryFolder.getRoot();
    H2ProjectFolders.delete(new File(h2Root, "Untouched"));
    assertThat(h2Root.list()).isEmpty();
  }

  @Test
  public void test_a_folder_being_deleted_is_told_apart_from_a_project_folder() throws IOException {
    File h2Root = temporaryFolder.newFolder("h2");
    assertThat(H2ProjectFolders.isPendingDeletion(new File(h2Root, "CLSA"))).isFalse();
    // a project name holds no dot, so nothing legitimate carries the marker
    assertThat(H2ProjectFolders.isPendingDeletion(new File(h2Root, "My Study"))).isFalse();
    assertThat(H2ProjectFolders.isPendingDeletion(new File(h2Root, "CLSA.deleted-1757376000000"))).isTrue();
  }

  @Test
  public void test_delete_pending_finishes_what_a_previous_run_began() throws IOException {
    File pending = temporaryFolder.newFolder("h2", "CLSA.deleted-1757376000000");
    assertThat(new File(pending, "data.mv.db").createNewFile()).isTrue();

    H2ProjectFolders.deletePending(pending);

    assertThat(pending.exists()).isFalse();
  }
}
