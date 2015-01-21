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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.core.util.FileUtil;
import org.obiba.git.command.GitCommandHandler;
import org.obiba.git.command.ReadFilesCommand;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.support.yaml.TaxonomyYaml;
import org.obiba.opal.core.vcs.OpalGitUtils;
import org.obiba.opal.core.vcs.command.OpalWriteTaxonomyCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

@Component
public class TaxonomyPersistenceStrategyImpl implements TaxonomyPersistenceStrategy {

  private static final Logger log = LoggerFactory.getLogger(TaxonomyPersistenceStrategyImpl.class);

  @Autowired
  private GitCommandHandler handler;

  @Override
  public void writeTaxonomy(@NotNull String name, @NotNull Taxonomy taxonomy, @Nullable String comment) {
    if (!name.equals(taxonomy.getName())) {
      File originalRepo = OpalGitUtils.getGitTaxonomyRepoFolder(name);
      try {
        FileUtil.copyDirectory(originalRepo, OpalGitUtils.getGitTaxonomyRepoFolder(taxonomy.getName()));
        FileUtil.delete(originalRepo);
        FileUtil.delete(new File(OpalGitUtils.getGitTaxonomiesWorkFolder(), name));
      } catch(IOException e) {
        throw new RuntimeException("Failed renaming files in git for taxonomy: " + name, e);
      }
    }

    log.debug("WriteTaxonomy: {} taxonomy: {}", name, taxonomy.getName());
    OpalWriteTaxonomyCommand.Builder builder = new OpalWriteTaxonomyCommand.Builder(taxonomy, comment);

    handler.execute(builder.build());
  }

  @Override
  public void removeTaxonomy(@NotNull String name, @Nullable String comment) {
    log.debug("RemoveTaxonomy: {}", name);
    try {
      FileUtil.delete(OpalGitUtils.getGitTaxonomyRepoFolder(name));
      FileUtil.delete(new File(OpalGitUtils.getGitTaxonomiesWorkFolder(), name));
    } catch(IOException e) {
      throw new RuntimeException("Failed deleting files in git for taxonomy: " + name, e);
    }
  }

  @Override
  @NotNull
  public Set<Taxonomy> readTaxonomies() {
    Set<Taxonomy> taxonomies = Sets.newHashSet();
    File repos = OpalGitUtils.getGitTaxonomiesRepoFolder();

    if (repos.exists() && repos.isDirectory()) {
      for (File repo : repos.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith(".git");
        }
      })) {
        taxonomies.addAll(readGitTaxonomy(repo));
      }
    }

    return taxonomies;
  }

  private Collection<Taxonomy> readGitTaxonomy(File taxoRepo) {
    ImmutableSet.Builder<Taxonomy> builder = ImmutableSet.builder();
    Set<InputStream> files = handler
        .execute(new ReadFilesCommand.Builder(taxoRepo, OpalGitUtils.getGitTaxonomiesWorkFolder()).recursive(true).filter("taxonomy\\.yml$").build());

    TaxonomyYaml yaml = new TaxonomyYaml();
    for(InputStream file : files) {
      try {
        InputStreamReader reader = new InputStreamReader(file, Charsets.UTF_8);
        builder.add(yaml.load(reader));
        reader.close();
      } catch(IOException e) {
        log.error("Unable to read taxonomy", e);
      }
    }

    return builder.build();
  }
}
