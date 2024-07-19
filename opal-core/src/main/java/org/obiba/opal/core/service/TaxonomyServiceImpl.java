/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import jakarta.annotation.Nullable;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.git.CommitInfo;
import org.obiba.opal.core.cfg.DuplicateTaxonomyException;
import org.obiba.opal.core.cfg.GitService;
import org.obiba.opal.core.cfg.NoSuchTaxonomyException;
import org.obiba.opal.core.cfg.NoSuchVocabularyException;
import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Term;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.core.support.yaml.TaxonomyYaml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class TaxonomyServiceImpl implements TaxonomyService, GitService {

  @Autowired
  private OpalFileSystemService opalFileSystemService;

  @Autowired
  private TaxonomyPersistenceStrategy taxonomyPersistence;

  private static final String MLSTRM_USER = "maelstrom-research";

  private static final String TAXONOMY_YAML = "taxonomy.yml";

  private static final String GITHUB_URL = "https://raw.githubusercontent.com";

  private static final String GITHUB_API_REF_URL = "https://api.github.com/repos/%s/%s/tags";

  private static final String GITHUB_API_ZIPBALL_URL = "https://api.github.com/repos/%s/%s/zipball/%s";

  private List<Taxonomy> taxonomies = Lists.newArrayList();

  @Override
  public void start() {
    taxonomies = Collections.synchronizedList(Lists.newArrayList(taxonomyPersistence.readTaxonomies()));
    sort();
  }

  @Override
  public void stop() {
  }

  @Override
  public List<Taxonomy> importGitHubTaxonomies(@NotNull String username, @NotNull String repo,
                                               @NotNull String ref, boolean override) {
    String user = username;
    if (Strings.isNullOrEmpty(username)) user = MLSTRM_USER;
    if (Strings.isNullOrEmpty(repo)) throw new IllegalArgumentException("GitHub repository is required.");
    if (Strings.isNullOrEmpty(ref)) throw new IllegalArgumentException("GitHub ref name is required.");

    return importZipball(String.format(GITHUB_API_ZIPBALL_URL, user, repo, ref), override);
  }

  @Override
  public List<String> getGitHubTaxonomyTags(@NotNull String username, @NotNull String repo) {
    String user = username;
    if (Strings.isNullOrEmpty(username)) user = MLSTRM_USER;
    if (Strings.isNullOrEmpty(repo)) throw new IllegalArgumentException("GitHub repository is required.");
    List<String> tags = Lists.newArrayList();

    try {
      ObjectMapper mapper = new ObjectMapper();
      List<Map<String, Object>> tagsInfo = mapper.readValue(new URL(String.format(GITHUB_API_REF_URL, user, repo)),
        new TypeReference<List<Map<String, Object>>>() {
        });
      for (Map<String, Object> tag : tagsInfo) {
        tags.add(tag.get("name").toString());
      }

      return tags;
    } catch (IOException e) {
      throw new TaxonomyImportException(e);
    }
  }

  @Override
  public Taxonomy importGitHubTaxonomy(@NotNull String username, @NotNull String repo, @Nullable String ref,
                                       @NotNull String taxonomyFile, boolean override) {

    String user = username;
    if (Strings.isNullOrEmpty(username)) user = MLSTRM_USER;
    if (Strings.isNullOrEmpty(repo)) throw new IllegalArgumentException("GitHub repository is required.");
    String reference = ref;
    if (Strings.isNullOrEmpty(ref)) reference = "master";
    String fileName = taxonomyFile;
    if (Strings.isNullOrEmpty(taxonomyFile)) fileName = TAXONOMY_YAML;
    if (!fileName.endsWith(".yml")) fileName = taxonomyFile + "/" + TAXONOMY_YAML;

    String uri = GITHUB_URL + "/" + user + "/" + repo + "/" + reference + "/" + fileName;

    return importUriTaxonomy(uri, override);
  }

  @Override
  public Taxonomy importFileTaxonomy(@NotNull String file, boolean override) throws FileSystemException {
    FileObject fileObj = resolveFileInFileSystem(file);

    try {
      return importInputStreamTaxonomy(fileObj.getContent().getInputStream(), null, override);
    } catch (IOException e) {
      throw new TaxonomyImportException(e);
    }
  }

  @Override
  public Taxonomy importInputStreamTaxonomy(@NotNull InputStream input, @Nullable String name, boolean override) {
    TaxonomyYaml yaml = new TaxonomyYaml();
    Taxonomy taxonomy = yaml.load(input);
    taxonomySanitizeName(taxonomy);
    String taxonomyName = Strings.isNullOrEmpty(name) ? taxonomy.getName() : name;
    if (override || !hasTaxonomy(taxonomyName)) {
      saveTaxonomy(taxonomy);
      return taxonomy;
    } else {
      return null;
    }
  }

  @Override
  public Iterable<Taxonomy> getTaxonomies() {
    return taxonomies;
  }

  @Override
  public void ensureUniqueTaxonomy(String name) throws DuplicateTaxonomyException {
    taxonomies.stream().filter(t -> t.getName().equals(name)).findFirst()
      .ifPresent(t -> {
        throw new DuplicateTaxonomyException(t);
      });
  }

  @Override
  public boolean hasTaxonomy(@NotNull String name) {
    for (Taxonomy taxonomy : taxonomies) {
      if (taxonomy.getName().equals(name)) return true;
    }
    return false;
  }

  @Nullable
  @Override
  public Taxonomy getTaxonomy(@NotNull String name) {
    for (Taxonomy taxonomy : taxonomies) {
      if (taxonomy.getName().equals(name)) return taxonomy;
    }
    return null;
  }

  @Override
  public void saveTaxonomy(@NotNull Taxonomy taxonomy) {
    synchronized (this) {
      Taxonomy stored = getTaxonomy(taxonomy.getName());
      if (stored == null) {
        taxonomies.add(taxonomy);
        sort();
      } else {
        int idx = taxonomies.indexOf(stored);
        taxonomies.set(idx, taxonomy);
      }
      taxonomyPersistence.writeTaxonomy(taxonomy.getName(), taxonomy, null);
    }
  }

  @Override
  public void saveTaxonomy(@NotNull String taxonomy, @NotNull Taxonomy taxonomyObj)
    throws NoSuchTaxonomyException {
    synchronized (this) {
      if (!hasTaxonomy(taxonomy)) throw new NoSuchTaxonomyException(taxonomy);
      taxonomies.remove(getTaxonomy(taxonomy));
      taxonomies.add(taxonomyObj);
      sort();
      taxonomyPersistence.writeTaxonomy(taxonomy, taxonomyObj, null);
    }
  }

  @Override
  public void deleteTaxonomy(@NotNull String name) {
    synchronized (this) {
      Taxonomy taxonomy = getTaxonomy(name);
      if (taxonomy != null) {
        taxonomyPersistence.removeTaxonomy(name, null);
        taxonomies.remove(taxonomy);
      }
    }
  }

  @Override
  public Iterable<CommitInfo> getCommitsInfo(@NotNull String name) {
    return taxonomyPersistence.getCommitsInfo(name);
  }

  @Override
  public CommitInfo getCommitInfo(@NotNull String name, @NotNull String commitId) {
    return taxonomyPersistence.getCommitInfo(name, commitId);
  }

  @Override
  public String getBlob(@NotNull String name, @NotNull String commitId) {
    return taxonomyPersistence.getBlob(name, commitId);
  }

  @Override
  public Iterable<String> getDiffEntries(@NotNull String name, @NotNull String commitId,
                                         @Nullable String prevCommitId) {
    return taxonomyPersistence.getDiffEntries(name, commitId, prevCommitId);
  }

  @Override
  public Iterable<Vocabulary> getVocabularies(@NotNull String name) {
    Taxonomy taxonomy = getTaxonomy(name);
    if (taxonomy == null) throw new NoSuchTaxonomyException(name);
    return taxonomy.getVocabularies();
  }

  @Override
  public boolean hasVocabulary(@NotNull String taxonomyName, @NotNull String vocabularyName)
    throws NoSuchTaxonomyException {
    Taxonomy taxonomy = getTaxonomy(taxonomyName);
    if (taxonomy == null) throw new NoSuchTaxonomyException(taxonomyName);
    return taxonomy.hasVocabulary(vocabularyName);
  }

  @Override
  public Vocabulary getVocabulary(@NotNull String taxonomyName, @NotNull String vocabularyName) {
    Taxonomy taxonomy = getTaxonomy(taxonomyName);
    if (taxonomy == null) throw new NoSuchTaxonomyException(taxonomyName);
    return taxonomy.getVocabulary(vocabularyName);
  }

  @Override
  public void saveVocabulary(@Nullable String taxonomyName, @NotNull Vocabulary vocabulary) {
    Taxonomy taxonomy = getTaxonomy(taxonomyName);
    if (taxonomy == null) throw new NoSuchTaxonomyException(taxonomyName);
    taxonomy.addVocabulary(vocabulary);
    saveTaxonomy(taxonomy);
  }

  @Override
  public void saveVocabulary(@NotNull String taxonomyName, @NotNull String vocabularyName,
                             @NotNull Vocabulary vocabulary) throws NoSuchTaxonomyException, NoSuchVocabularyException {
    Taxonomy taxonomy = getTaxonomy(taxonomyName);
    if (taxonomy == null) throw new NoSuchTaxonomyException(taxonomyName);
    taxonomy.updateVocabulary(vocabularyName, vocabulary);
    saveTaxonomy(taxonomy);
  }

  @Override
  public void deleteVocabulary(@Nullable String taxonomyName, @NotNull String vocabularyName) {
    Taxonomy taxonomy = getTaxonomy(taxonomyName);
    if (taxonomy == null) throw new NoSuchTaxonomyException(taxonomyName);
    taxonomy.removeVocabulary(vocabularyName);
    saveTaxonomy(taxonomy);
  }

  //
  // Private methods
  //

  private void sort() {
    Collections.sort(taxonomies, (t1, t2) -> t1.getName().compareTo(t2.getName()));
  }

  private List<Taxonomy> importZipball(@NotNull String uri, boolean override) {
    List<Taxonomy> result = Lists.newArrayList();
    try (InputStream input = new URL(uri).openStream();
         ZipInputStream zipIn = new ZipInputStream(input)) {
      ZipEntry entry;
      while ((entry = zipIn.getNextEntry()) != null) {
        Matcher matcher = Pattern.compile("/(.*?\\.yml)$").matcher(entry.getName());
        if (matcher.find()) {
          TaxonomyYaml yaml = new TaxonomyYaml();
          Taxonomy taxonomy = yaml.load(zipIn);
          taxonomySanitizeName(taxonomy);
          if (override || !hasTaxonomy(taxonomy.getName())) {
            saveTaxonomy(taxonomy);
            result.add(taxonomy);
          }
        }
      }

      return result;
    } catch (Exception e) {
      throw new TaxonomyImportException(e);
    }
  }

  private Taxonomy importUriTaxonomy(@NotNull String uri, boolean override) {
    try (InputStream input = new URL(uri).openStream()) {
      TaxonomyYaml yaml = new TaxonomyYaml();
      Taxonomy taxonomy = yaml.load(input);
      taxonomySanitizeName(taxonomy);
      if (override || !hasTaxonomy(taxonomy.getName())) {
        saveTaxonomy(taxonomy);
        return taxonomy;
      }
    } catch (IOException e) {
      throw new TaxonomyImportException(e);
    }
    return null;
  }

  private void taxonomySanitizeName(Taxonomy taxonomy) {
    taxonomy.setName(sanitizeName(taxonomy.getName()));
    if (taxonomy.hasVocabularies()) {
      taxonomy.getVocabularies().forEach(vocabulary -> vocabularySanitizeName(vocabulary));
    }
  }

  private void vocabularySanitizeName(Vocabulary vocabulary) {
    vocabulary.setName(sanitizeName(vocabulary.getName()));
    if (vocabulary.hasTerms()) {
      vocabulary.getTerms().forEach(term -> termSanitizeName(term));
    }
  }

  private void termSanitizeName(Term term) {
    term.setName(sanitizeName(term.getName()));
  }

  private String sanitizeName(String name) {
    return name.replaceAll("[^\\w_-]", "_");
  }

  private FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    return opalFileSystemService.getFileSystem().getRoot().resolveFile(path);
  }

  /**
   * For testing.
   */
  void clear() {
    taxonomies.clear();
  }

}
