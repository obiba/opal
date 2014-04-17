/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.cfg;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.git.command.AddFilesCommand;
import org.obiba.git.command.GitCommandHandler;
import org.obiba.git.command.ReadFileCommand;
import org.obiba.git.command.ReadFilesCommand;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewPersistenceStrategy;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.opal.core.vcs.OpalGitUtils;
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
public class OpalViewPersistenceStrategyNew implements ViewPersistenceStrategy {

  private static final Logger log = LoggerFactory.getLogger(OpalViewPersistenceStrategyNew.class);

  @Autowired
  private GitCommandHandler handler;

  @Override
  public void writeViews(@NotNull String datasourceName, @NotNull Set<View> views, @Nullable String comment) {
    AddFilesCommand.Builder commandBuilder = new AddFilesCommand.Builder(
        OpalGitUtils.getDatasourceGitFolder(datasourceName),
        Strings.isNullOrEmpty(comment) ? getDefaultComment(views) : comment);
    for(View view : views) {
      commandBuilder.addFile(OpalGitUtils.getViewFilePath(view.getName()), getViewAsInputStream(view));
    }
    handler.execute(commandBuilder.build());
  }

  @Override
  public void writeView(@NotNull String datasourceName, @NotNull View view, @Nullable String comment) {
    writeViews(datasourceName, ImmutableSet.of(view), comment);
  }

  @Override
  public void removeView(@NotNull String datasourceName, @NotNull String viewName) {

  }

  @Override
  public void removeViews(String datasourceName) {

  }

  @Override
  public Set<View> readViews(@NotNull String datasourceName) {
    ImmutableSet.Builder<View> builder = ImmutableSet.builder();
    File datasourceRepo = OpalGitUtils.getDatasourceGitFolder(datasourceName);
    if(!datasourceRepo.exists()) {
      log.info("The views directory '{}' does not exist.", datasourceRepo.getAbsolutePath());
      return builder.build();
    }

    Set<InputStream> files = handler
        .execute(new ReadFilesCommand.Builder(datasourceRepo).recursive(true).filter("View\\.xml$").build());

    for(InputStream file : files) {
      InputStreamReader reader = new InputStreamReader(file, Charsets.UTF_8);
      builder.add((View) getXStream().fromXML(reader));
    }

    return builder.build();
  }

  @Override
  public void dispose() {

  }

  @Override
  public void initialise() {

  }

  private InputStream getViewAsInputStream(View view) {
    return new ByteArrayInputStream(getXStream().toXML(view).getBytes(Charsets.UTF_8));
  }

  private XStream getXStream() {
    return MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();
  }

  private String getDefaultComment(Set<View> views) {
    StringBuilder builder = new StringBuilder("Update ");
    for(View view : views) {
      if(builder.length() > 0) builder.append(", ");
      builder.append(view.getName());
    }

    return builder.toString();
  }

}
