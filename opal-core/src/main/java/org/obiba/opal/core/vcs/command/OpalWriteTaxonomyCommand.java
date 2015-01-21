/*
 * Copyright (c) 2015 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.vcs.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;
import org.obiba.git.GitException;
import org.obiba.git.command.AbstractGitWriteCommand;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.support.yaml.TaxonomyYaml;
import org.obiba.opal.core.vcs.OpalGitUtils;

import com.google.common.base.Strings;

public class OpalWriteTaxonomyCommand extends AbstractGitWriteCommand {

  private Taxonomy taxonomy;

  private OpalWriteTaxonomyCommand(@NotNull File repositoryPath, @NotNull Taxonomy taxonomy,
      @NotNull String commitMessage) {
    super(repositoryPath, OpalGitUtils.getGitTaxonomiesWorkFolder(), commitMessage);
    this.taxonomy = taxonomy;
  }

  @Override
  public Iterable<PushResult> execute(Git git) {
    try {
      File localRepo = git.getRepository().getWorkTree();
      serializeTaxonomy(localRepo);
      git.add().addFilepattern(".").call();

      return commitAndPush(git);
    } catch(IOException | GitAPIException e) {
      throw new GitException(e);
    }
  }

  private void serializeTaxonomy(File localRepo)
      throws IOException {
    // Write serialized taxonomy
    File taxoFile = new File(localRepo, OpalGitUtils.TAXONOMY_FILE_NAME);
    try(FileWriter writer = new FileWriter(taxoFile)) {
      TaxonomyYaml yaml = new TaxonomyYaml();
      writer.write(yaml.dump(taxonomy));
      writer.flush();
    }
  }

  public static class Builder {

    private final OpalWriteTaxonomyCommand command;

    public Builder(@NotNull Taxonomy taxonomy, String comment) {
      command = new OpalWriteTaxonomyCommand(OpalGitUtils.getGitTaxonomyRepoFolder(taxonomy.getName()), taxonomy,
          Strings.isNullOrEmpty(comment) ? "Update " + taxonomy.getName() : comment);
    }

    public OpalWriteTaxonomyCommand build() {
      return command;
    }
  }

}
