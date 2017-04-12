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
import com.google.common.collect.Lists;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.runtime.NoSuchServiceException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.VCFSamplesMappingService;
import org.obiba.opal.shell.commands.options.ExportVCFCommandOptions;
import org.obiba.opal.spi.ServicePlugin;
import org.obiba.opal.spi.vcf.VCFStore;
import org.obiba.opal.spi.vcf.VCFStoreException;
import org.obiba.opal.spi.vcf.VCFStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

@CommandUsage(description = "Export one or more VCF files from a project into a destination folder.",
    syntax = "Syntax: export-vcf --project PROJECT --destination DESTINATION NAMES")
public class ExportVCFCommand extends AbstractOpalRuntimeDependentCommand<ExportVCFCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(ExportVCFCommand.class);

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

  @Autowired
  private ProjectService projectService;

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private VCFSamplesMappingService vcfSamplesMappingService;

  @Autowired
  private IdentifiersTableService identifiersTableService;

  private VCFStore store;

  private Iterable<ValueSet> participantsMappingTable;

  private Map<String, ValueSet> mapParticipantInternalIdWithParticipantMappingValueSet;

  //
  // AbstractOpalRuntimeDependentCommand Methods
  //

  @Override
  public int execute() {
    Stopwatch stopwatch = Stopwatch.createStarted();

    String names = String.join(", ", options.getNames());

    if (options.hasTable())
      getShell().printf("Exporting VCF/BCF files '%s' from project '%s' into '%s', filtered by '%s'...", names, options.getProject(), options.getDestination(), options.getTable());
    else
      getShell().printf("Exporting VCF/BCF files '%s' from project '%s' into '%s'...", names, options.getProject(), options.getDestination());
    Project project = projectService.getProject(getOptions().getProject());
    if (!opalRuntime.hasServicePlugins(VCFStoreService.class)) throw new NoSuchServiceException(VCFStoreService.SERVICE_TYPE);
    if (!project.hasVCFStoreService()) {
      getShell().printf("The project '%s' has no VCF store", options.getProject());
      return 1;
    }
    setVCFStore(project.getVCFStoreService(), project.getName());

    try {
      exportVCF();
    } catch (Exception e) {
      log.error("Cannot export VCF/BCF files from project {} into {}: {}", options.getProject(), options.getDestination(), names, e);
      getShell().printf("Cannot export VCF files: %s", e.getMessage());
      log.info("Export VCF/BCF failed in {}", stopwatch.stop());
      return 1;
    }

    log.info("Export VCF/BCF succeeded in {}", stopwatch.stop());
    return 0;
  }

  //
  // Methods
  //

  private void setVCFStore(String serviceName, String name) {
    if (!opalRuntime.hasServicePlugins(VCFStoreService.class)) throw new NoSuchServiceException(VCFStoreService.SERVICE_TYPE);
    ServicePlugin servicePlugin = opalRuntime.getServicePlugin(serviceName);
    if (!(servicePlugin instanceof VCFStoreService)) throw new NoSuchServiceException(serviceName);
    VCFStoreService service = (VCFStoreService) servicePlugin;
    store = service.getStore(name);
  }

  private void exportVCF() throws IOException, VCFStoreException {
    FileObject fileObject = resolveFileInFileSystem(options.getDestination());
    if (fileObject.exists() && fileObject.getType() == FileType.FILE)
      throw new IllegalArgumentException("Not a valid path to VCF file: " + options.getDestination());
    if (!fileObject.exists()) fileObject.createFolder();
    if (!fileObject.isWriteable()) throw new IllegalArgumentException("Export destination is not writable: " + options.getDestination());
    String timestamp = DATE_FORMAT.format(new Date());
    String destinationFolderName = store.getName() + "-vcf-" + timestamp;
    File destinationFolder = new File(opalRuntime.getFileSystem().getLocalFile(fileObject), destinationFolderName);
    destinationFolder.mkdirs();
    getShell().printf(String.format("Exporting VCF/BCF files in: %s", options.getDestination() + File.separator + destinationFolderName));

    int total = options.getNames().size() + 1;
    getShell().progress(String.format("Exporting VCF/BCF file(s): %s", String.join(", ", options.getNames())), 0, total, 0);

    List<String> filterSampleIds = options.hasTable() ?
            vcfSamplesMappingService.getFilteredSampleIds(options.getProject(), options.getTable(), options.isCaseControl()) :
            Lists.newArrayList();

    int count = 1;
    for (String vcfName : options.getNames()) {
      String baseVcfName = vcfName;
      if (vcfName.endsWith(".vcf.gz")) baseVcfName = vcfName.replaceAll("\\.vcf\\.gz$", "");
      else if (vcfName.endsWith(".bcf.gz")) baseVcfName = vcfName.replaceAll("\\.bcf\\.gz$", "");
      getShell().progress(String.format("Exporting VCF/BCF file (%s)", baseVcfName), count, total, (count * 100) / total);
      VCFStore.VCFSummary summary = store.getVCFSummary(baseVcfName);
      String vcfFileName = baseVcfName + "." + summary.getFormat().name().toLowerCase() + ".gz";
      String csvFileName = baseVcfName + "-samples.csv";
      getShell().printf(String.format("Exporting VCF/BCF file (%s)", vcfFileName));
      File vcfFile = new File(destinationFolder, vcfFileName);
      File csvFile = new File(destinationFolder, csvFileName);
      if (!options.hasTable()) {
        if (!options.isCaseControl()) {
          Collection<String> sampleIds = summary.getSampleIds();
          sampleIds.removeAll(vcfSamplesMappingService.getControls(options.getProject()));

          store.readVCF(baseVcfName, new FileOutputStream(vcfFile), sampleIds);
          exportCsvFileMappingSampleIdAndParticipantId(csvFile, sampleIds);
        } else {
          store.readVCF(baseVcfName, new FileOutputStream(vcfFile));
          exportCsvFileMappingSampleIdAndParticipantId(csvFile, summary.getSampleIds());
        }
      } else {
        Collection<String> sampleIds = summary.getSampleIds();
        sampleIds.retainAll(filterSampleIds);
        if (sampleIds.size() > 0) {
          store.readVCF(baseVcfName, new FileOutputStream(vcfFile), filterSampleIds);
          exportCsvFileMappingSampleIdAndParticipantId(csvFile, sampleIds);
        }
      }
      count++;
    }
    getShell().progress(String.format("VCF/BCF file(s) export completed."), total, total, 100);
  }

  private void exportCsvFileMappingSampleIdAndParticipantId(File csvFile, Collection<String> sampleIds) throws IOException {

    if (StringUtils.isEmpty(options.getParticipantIdentifiersMapping()))
      return;

    getShell().printf(String.format("Exporting csv mapping file (%s)", csvFile));

    Map<String, VCFSamplesMappingService.ParticipantRolePair> mapSampleIdParticipantId = vcfSamplesMappingService.findParticipantIdBySampleId(options.getProject(), sampleIds);

    try (FileWriter fileWriter = new FileWriter(csvFile)) {
      for (Map.Entry<String, VCFSamplesMappingService.ParticipantRolePair> mapSampleIdParticipantIdEntry : mapSampleIdParticipantId.entrySet()) {

        String sampleId = mapSampleIdParticipantIdEntry.getKey();
        String role = mapSampleIdParticipantIdEntry.getValue().getRole();
        String internalParticipantId = mapSampleIdParticipantIdEntry.getValue().getParticipantId();
        String externalParticipantId = findExternalParticipantIdFromInternalParticipantId(internalParticipantId);
        fileWriter.write(String.format("\"%s\",\"%s\",\"%s\"\n", escapeCsvField(sampleId), escapeCsvField(externalParticipantId), escapeCsvField(role)));
      }
    }
  }

  private String escapeCsvField(String csvField) {
    return csvField != null ? csvField.replaceAll("\"", "\\\"") : "";
  }

  private String findExternalParticipantIdFromInternalParticipantId(String internalParticipantId) {

    Optional<ValueSet> participantValueSet = Optional.ofNullable(mapParticipantInternalIdWithParticipantMappingValueSet().get(internalParticipantId));
    if (!participantValueSet.isPresent())
      return "";

    Optional<Variable> participantExternalIdVariable = StreamSupport.stream(participantValueSet.get().getValueTable().getVariables().spliterator(), true)
            .filter((s) -> s.getName().equals(options.getParticipantIdentifiersMapping()))
            .findFirst();
    if (!participantExternalIdVariable.isPresent())
      return "";

    Value value = getParticipantsMappingTable().iterator().next().getValueTable().getValue(
            participantExternalIdVariable.get(),
            participantValueSet.get());
    return value.isNull() || value.getValue() == null ? "" : value.getValue().toString();
  }

  private synchronized Map<String, ValueSet> mapParticipantInternalIdWithParticipantMappingValueSet() {

    if (mapParticipantInternalIdWithParticipantMappingValueSet == null) {
      mapParticipantInternalIdWithParticipantMappingValueSet = new HashMap<>();

      getParticipantsMappingTable().forEach(valueSet ->
              mapParticipantInternalIdWithParticipantMappingValueSet.put(valueSet.getVariableEntity().getIdentifier(), valueSet));
    }

    return mapParticipantInternalIdWithParticipantMappingValueSet;
  }

  private synchronized Iterable<ValueSet> getParticipantsMappingTable() {
    if (participantsMappingTable == null)
      participantsMappingTable = identifiersTableService.getIdentifiersTable("Participant").getValueSets();
    return participantsMappingTable;
  }

  FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    if (Strings.isNullOrEmpty(path)) return null;
    return opalRuntime.getFileSystem().getRoot().resolveFile(path);
  }

  @Override
  public String toString() {
    return "export-vcf -p '" + getOptions().getProject() + "' -d '" + options.getDestination() + "' " + String.join(", ", options.getNames());
  }
}
