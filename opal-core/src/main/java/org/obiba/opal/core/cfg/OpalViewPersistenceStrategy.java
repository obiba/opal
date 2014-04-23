/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.obiba.opal.core.cfg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.obiba.core.util.FileUtil;
import org.obiba.core.util.StreamUtil;
import org.obiba.git.command.DeleteFilesCommand;
import org.obiba.git.command.GitCommandHandler;
import org.obiba.git.command.ReadFilesCommand;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewPersistenceStrategy;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.opal.core.vcs.OpalGitUtils;
import org.obiba.opal.core.vcs.command.OpalWriteViewsCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.thoughtworks.xstream.XStream;

import edu.umd.cs.findbugs.annotations.Nullable;

@Component
public class OpalViewPersistenceStrategy implements ViewPersistenceStrategy {

  private static final Logger log = LoggerFactory.getLogger(OpalViewPersistenceStrategy.class);

  @Autowired
  private GitCommandHandler handler;

  @Override
  public void writeViews(@NotNull String datasourceName, @NotNull Set<View> views, @Nullable String comment) {
    log.info("WriteViews ds: {} views: {}", datasourceName, views.size());
    OpalWriteViewsCommand.Builder builder = new OpalWriteViewsCommand.Builder(
        OpalGitUtils.getDatasourceGitFolder(datasourceName), views,
        Strings.isNullOrEmpty(comment) ? getDefaultComment(views) : comment);

    handler.execute(builder.build());
  }

  @Override
  public void writeView(@NotNull String datasourceName, @NotNull View view, @Nullable String comment) {
    writeViews(datasourceName, ImmutableSet.of(view), comment);
  }

  @Override
  public void removeView(@NotNull String datasourceName, @NotNull String viewName) {
    log.info("RemoveView ds: {}, view: {}", datasourceName, viewName);
    handler.execute(new DeleteFilesCommand.Builder(OpalGitUtils.getDatasourceGitFolder(datasourceName), viewName,
        "Remove " + viewName).build());
  }

  @Override
  public void removeViews(String datasourceName) {
    log.info("RemoveViews ds: {}", datasourceName);
    try {
      FileUtil.delete(OpalGitUtils.getDatasourceGitFolder(datasourceName));
    } catch(IOException e) {
      throw new RuntimeException("Failed deleting views in git for datasource: " + datasourceName, e);
    }

  }

  @Override
  public Set<View> readViews(@NotNull String datasourceName) {
    log.info("ReadViews ds: {}", datasourceName);
    File datasourceRepo = OpalGitUtils.getDatasourceGitFolder(datasourceName);
    return datasourceRepo.exists() ? readGitViews(datasourceRepo) : new LegacyViews().readViews(datasourceName);
  }

  @Override
  public void dispose() {

  }

  @Override
  public void initialise() {

  }

  private Set<View> readGitViews(File datasourceRepo) {
    ImmutableSet.Builder<View> builder = ImmutableSet.builder();
    Set<InputStream> files = handler
        .execute(new ReadFilesCommand.Builder(datasourceRepo).recursive(true).filter("View\\.xml$").build());

    for(InputStream file : files) {
      InputStreamReader reader = new InputStreamReader(file, Charsets.UTF_8);
      builder.add((View) getXStream().fromXML(reader));
    }

    return builder.build();
  }

  private XStream getXStream() {
    return MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();
  }

  private String getDefaultComment(Set<View> views) {
    StringBuilder builder = new StringBuilder();
    for(View view : views) {
      if(builder.length() > 0) builder.append(", ");
      builder.append(view.getName());
    }

    return builder.insert(0, "Update ").toString();
  }

  /**
   * Used to read views from previous Opal versions (<= 1.15)
   */
  private final class LegacyViews {
    private static final String CONF_DIRECTORY_NAME = "conf";
    private static final String VIEWS_DIRECTORY_NAME = "views";

    private final File viewsDirectory;

    public LegacyViews() {
      String viewsDirectoryName = System.getProperty("OPAL_HOME") + File.separator //
          + CONF_DIRECTORY_NAME + File.separator + VIEWS_DIRECTORY_NAME;
      viewsDirectory = new File(viewsDirectoryName);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public Set<View> readViews(@NotNull String datasourceName) {
      Set<View> result = ImmutableSet.of();
      if(!viewsDirectory.isDirectory()) {
        log.info("The legacy views directory '{}' does not exist.", viewsDirectory.getAbsolutePath());
        return result;
      }

      InputStreamReader reader = null;
      try {
        reader = new InputStreamReader(new FileInputStream(getDatasourceViewsFile(datasourceName)), Charsets.UTF_8);
        result = (Set<View>) getXStream().fromXML(reader);
      } catch(FileNotFoundException e) {
        return ImmutableSet.of();
      } finally {
        StreamUtil.silentSafeClose(reader);
      }
      return result;
    }

    private String normalizeDatasourceName(@SuppressWarnings("TypeMayBeWeakened") String datasourceName) {
      return Pattern.compile("([^a-zA-Z0-9-_. ])").matcher(datasourceName).replaceAll("");
    }

    private File getDatasourceViewsFile(String datasourceName) {
      return new File(viewsDirectory, normalizeDatasourceName(datasourceName) + ".xml");
    }
  }

}
