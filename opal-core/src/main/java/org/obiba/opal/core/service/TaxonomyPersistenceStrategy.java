/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.git.CommitInfo;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;

public interface TaxonomyPersistenceStrategy {

  /**
   * Persist a {@link org.obiba.opal.core.domain.taxonomy.Taxonomy}: create it if not existing, update it
   * otherwise (taxonomy can be renamed).
   *
   * @param name
   * @param taxonomy
   * @param comment
   */
  void writeTaxonomy(@NotNull String name, @NotNull Taxonomy taxonomy, @Nullable String comment);

  /**
   * Remove a {@link org.obiba.opal.core.domain.taxonomy.Taxonomy}.
   *
   * @param name
   * @param comment
   */
  void removeTaxonomy(@NotNull String name, @Nullable String comment);

  /**
   * Read all {@link org.obiba.opal.core.domain.taxonomy.Taxonomy}
   * @return
   */
  @NotNull
  Set<Taxonomy> readTaxonomies();

  /**
   *  Returns commit history
   *
   * @param name
   * @return
   */
  Iterable<CommitInfo> getCommitsInfo(@NotNull String name);

  /**
   * Returns the info of a given commit
   *
   * @param name
   * @param commitId
   * @return
   */
  CommitInfo getCommitInfo(@NotNull String name, @NotNull String commitId);

  /**
   * Returns the blob (content) of a given commit
   *
   * @param name
   * @param commitId
   * @return
   */
  String getBlob(@NotNull String name, @NotNull String commitId);

  /**
   * Returns the diff entries between two commits
   *
   * @param name
   * @param commitId
   * @param prevCommitId
   * @return
   */
  Iterable<String> getDiffEntries(@NotNull String name, @NotNull String commitId, @Nullable String prevCommitId);
}
