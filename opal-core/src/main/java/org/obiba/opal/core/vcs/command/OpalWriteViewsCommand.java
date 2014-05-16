/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.vcs.command;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;
import org.obiba.git.GitException;
import org.obiba.git.command.AbstractGitWriteCommand;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.views.View;
import org.obiba.magma.views.WhereClause;
import org.obiba.magma.views.support.VariableOperationContext;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.opal.core.vcs.OpalGitUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;

import static org.obiba.magma.views.support.VariableOperationContext.Operation;

public class OpalWriteViewsCommand extends AbstractGitWriteCommand {

  private final Set<View> views;

  private final VariableOperationContext context;

  private OpalWriteViewsCommand(@NotNull File repositoryPath, @NotNull Set<View> views, @NotNull String commitMessage,
      VariableOperationContext context) {
    super(repositoryPath, OpalGitUtils.getGitViewsWorkFolder(), commitMessage);
    this.views = views;
    this.context = context;
  }

  @Override
  public Iterable<PushResult> execute(Git git) {
    try {
      File localRepo = git.getRepository().getWorkTree();
      List<String> varFilesToRemove = Lists.newArrayList();
      StringBuilder message = new StringBuilder();
      serializeAllViewFiles(localRepo, varFilesToRemove, message);
      String currentMessage = getCommitMessage();
      setCommitMessage((Strings.isNullOrEmpty(currentMessage) ? "Update " : currentMessage + " ") + message);

      for(String toRemove : varFilesToRemove) {
        git.rm().addFilepattern(toRemove).call();
      }
      git.add().addFilepattern(".").call();
      return commitAndPush(git);
    } catch(IOException | GitAPIException e) {
      throw new GitException(e);
    }
  }

  private void serializeAllViewFiles(File localRepo, Collection<String> varFilesToRemove, StringBuilder message)
      throws IOException {
    message.append("[");
    for(View view : views) {
      doWriteGitView(localRepo, view, varFilesToRemove);
      if(message.length() > 1) {
        message.append(", ");
      }
      message.append(view.getName());
    }
    message.append("]");
  }

  private void doWriteGitView(File localRepo, View view, Collection<String> varFilesToRemove) throws IOException {
    File viewRepo = new File(localRepo, view.getName());
    viewRepo.mkdirs();

    // Write serialized view
    File viewFile = new File(viewRepo, OpalGitUtils.VIEW_FILE_NAME);
    try(OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(viewFile), Charsets.UTF_8)) {
      getXStream().toXML(view, writer);
    }

    doWriteGitViewWhere(viewRepo, view, varFilesToRemove);
    if (context != null && context.hasOperations(view)) {
      doWriteGitViewVariables(viewRepo, view, context.getOperations(view), varFilesToRemove);
    } else {
      doWriteGitViewVariables(viewRepo, view, varFilesToRemove);
    }
  }

  private void doWriteGitViewWhere(File viewRepo, View view, Collection<String> varFilesToRemove) throws IOException {
    File scriptFile = new File(viewRepo, OpalGitUtils.VIEW_WHERE_FILE_NAME);
    WhereClause where = view.getWhereClause();

    if(where != null && where instanceof JavascriptClause) {
      String script = ((JavascriptClause) where).getScript();
      try(FileWriter fileWriter = new FileWriter(scriptFile)) {
        fileWriter.append(script);
        fileWriter.flush();
      }
    } else if (scriptFile.exists()) {
      varFilesToRemove.add(viewRepo.getName() + "/" + scriptFile.getName());
    }
  }

  private void doWriteGitViewVariables(File viewRepo, ValueTable view, Map<Operation, Collection<Variable>> operations,
      Collection<String> varFilesToRemove)
      throws IOException {
    // Write variable script files
    Collection<Variable> variablesToWrite = operations.get(Operation.ADD);
    if (variablesToWrite != null) {
      for(Variable variable : variablesToWrite) {
        doWriteGitViewVariable(viewRepo, variable);
      }
    }

    // Remove variable script files
    Collection<Variable> variablesToDelete = operations.get(Operation.DELETE);
    if (variablesToDelete != null) {
      for(Variable variable : variablesToDelete) {
        varFilesToRemove.add(OpalGitUtils.getVariableFilePath(view.getName(), variable.getName()));
      }
    }

  }

  private void doWriteGitViewVariables(File viewRepo, ValueTable view, Collection<String> varFilesToRemove)
      throws IOException {
    // Write variable script files
    for(Variable variable : view.getVariables()) {
      doWriteGitViewVariable(viewRepo, variable);
    }
    // Detect removed variables

    for(File f : viewRepo.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(OpalGitUtils.VARIABLE_FILE_EXTENSION);
      }
    })) {
      String varName = f.getName().substring(0, f.getName().length() - 3);
      if(!view.hasVariable(varName)) {
        varFilesToRemove.add(f.getParentFile().getName() + File.separator + f.getName());
      }
    }
  }

  private void doWriteGitViewVariable(File viewRepo, Variable variable) throws IOException {
    String script = variable.hasAttribute("script") ? variable.getAttributeStringValue("script") : "null";
    File variableFile = new File(viewRepo, variable.getName() + OpalGitUtils.VARIABLE_FILE_EXTENSION);

    try(FileWriter fileWriter = new FileWriter(variableFile)) {
      fileWriter.append(script);
      fileWriter.flush();
    }
  }

  private XStream getXStream() {
    return MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();
  }

  public static class Builder {

    private final OpalWriteViewsCommand command;

    public Builder(@NotNull File repositoryPath, @NotNull Set<View> views, @NotNull String comment, VariableOperationContext context) {
      command = new OpalWriteViewsCommand(repositoryPath, views, comment, context);
    }

    public OpalWriteViewsCommand build() {
      return command;
    }
  }

}
