/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.shell.commands;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.runtime.NoSuchServiceException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.shell.commands.options.ImportVCFCommandOptions;
import org.obiba.opal.spi.vcf.VCFStore;
import org.obiba.opal.spi.vcf.VCFStoreException;
import org.obiba.opal.spi.vcf.VCFStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.NoSuchElementException;

@CommandUsage(description = "Import VCF file into a project.",
    syntax = "Syntax: import-vcf --project PROJECT --name NAME --file FILE")
public class ImportVCFCommand extends AbstractOpalRuntimeDependentCommand<ImportVCFCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(ImportVCFCommand.class);

  @Autowired
  private ProjectService projectService;

  @Autowired
  private OpalRuntime opalRuntime;

  private VCFStore store;

  //
  // AbstractOpalRuntimeDependentCommand Methods
  //

  @Override
  public int execute() {
    Stopwatch stopwatch = Stopwatch.createStarted();

    getShell().printf("Importing VCF file '%s' located at '%s' in project '%s'...", options.getName(), options.getFile(), options.getProject());
    getShell().progress(String.format("Preparing VCF file store for project '%s'", options.getProject()), 0, 3, 0);
    Project project = projectService.getProject(getOptions().getProject());
    if (!opalRuntime.hasVCFStoreServices()) throw new NoSuchServiceException(VCFStoreService.SERVICE_TYPE);
    if (!project.hasVCFStoreService()) {
      // for now get the first one. Some day, the service type will be a project admin choice
      VCFStoreService service = opalRuntime.getVCFStoreServices().iterator().next();
      project.setVCFStoreService(service.getName());
      projectService.save(project);
    }
    setVCFStore(project.getVCFStoreService(), project.getName());

    try {
      importVCF();
    } catch (Exception e) {
      log.error("Cannot import VCF {} in project {}: {}", options.getName(), options.getProject(), options.getFile(), e);
      getShell().printf("Cannot import VCF file: %s", e.getMessage());
      log.info("Import VCF failed in {}", stopwatch.stop());
      return 1;
    }

    log.info("Import VCF succeeded in {}", stopwatch.stop());
    return 0;
  }

  //
  // Methods
  //

  private void setVCFStore(String serviceName, String name) {
    if (!opalRuntime.hasVCFStoreServices()) throw new NoSuchElementException("No VCF store service is available");
    VCFStoreService service = opalRuntime.getVCFStoreService(serviceName);
    if (!service.hasStore(name)) service.createStore(name);
    store = service.getStore(name);
  }

  private void importVCF() throws IOException, VCFStoreException {
    getShell().progress(String.format("Preparing VCF file '%s'", options.getFile()), 1, 3, 10);
    FileObject fileObject = resolveFileInFileSystem(options.getFile());
    if (fileObject == null || !fileObject.exists() || fileObject.getType() == FileType.FOLDER)
      throw new IllegalArgumentException("Not a valid path to VCF file: " + options.getFile());
    if (!fileObject.isReadable()) throw new IllegalArgumentException("VCF file is not readable: " + options.getFile());
    File vcfFile = opalRuntime.getFileSystem().getLocalFile(fileObject);
    String vcfName = options.getName();
    if (Strings.isNullOrEmpty(vcfName)) vcfName = vcfFile.getName();

    getShell().progress(String.format("Importing VCF file as '%s'", vcfName), 2, 3, 20);
    store.writeVCF(vcfName, new FileInputStream(vcfFile));
    getShell().progress(String.format("VCF file import completed."), 3, 3, 100);
  }

  FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    if (Strings.isNullOrEmpty(path)) return null;
    return opalRuntime.getFileSystem().getRoot().resolveFile(path);
  }

  @Override
  public String toString() {
    return "import-vcf -n '" + getOptions().getName() + "' -p '" + getOptions().getProject() + "' -f '" + options.getFile() + "'";
  }

}
