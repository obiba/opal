import { defineStore } from 'pinia';
import { api, baseUrl } from 'src/boot/api';
import type {
  ProjectDto,
  ProjectSummaryDto,
  ProjectDto_IdentifiersMappingDto,
} from 'src/models/Projects';
import { ProjectDatasourceStatusDto } from 'src/models/Projects';
import type { Acl } from 'src/models/Opal';
import type { Subject, KeyForm } from 'src/models/Opal';
import type {
  CommandStateDto,
  ImportCommandOptionsDto,
  ExportCommandOptionsDto,
  CopyCommandOptionsDto,
  BackupCommandOptionsDto,
  RestoreCommandOptionsDto,
  ImportVCFCommandOptionsDto,
  ExportVCFCommandOptionsDto,
  AnalyseCommandOptionsDto,
} from 'src/models/Commands';
import { CommandStateDto_Status } from 'src/models/Commands';
import { Perms } from 'src/utils/authz';
import type { VCFSamplesMappingDto } from 'src/models/Plugins';

interface ProjectPerms {
  summary: Perms | undefined;
  export: Perms | undefined;
  copy: Perms | undefined;
  import: Perms | undefined;
  projects: Perms | undefined;
  project: Perms | undefined;
  keystore: Perms | undefined;
  reload: Perms | undefined;

  vcfstore: Perms | undefined;
  samples: Perms | undefined;
  vcfs: Perms | undefined;
  permissions_vcfstore: Perms | undefined;
  import_vcf: Perms | undefined;
  export_vcf: Perms | undefined;

  analyses: Perms | undefined;
  anayses_export: Perms | undefined;
}

export const useProjectsStore = defineStore('projects', () => {
  const projects = ref([] as ProjectDto[]);
  const project = ref({} as ProjectDto);
  const summary = ref({} as ProjectSummaryDto);
  const commandStates = ref([] as CommandStateDto[]);
  const perms = ref({} as ProjectPerms);
  const subjects = ref([] as Subject[]);
  const acls = ref([] as Acl[]);

  const isReady = computed(() => summary.value?.datasourceStatus === ProjectDatasourceStatusDto.READY);

  function reset() {
    projects.value = [];
    project.value = {} as ProjectDto;
    summary.value = {} as ProjectSummaryDto;
    commandStates.value = [];
    perms.value = {} as ProjectPerms;
    subjects.value = [] as Subject[];
  }

  async function initProjects() {
    projects.value = [];
    return loadProjects();
  }

  async function initProject(name: string) {
    if (project.value.name !== name) {
      return loadProject(name);
    }
    return Promise.resolve();
  }

  async function refreshProject(name: string) {
    project.value = {} as ProjectDto;
    return loadProject(name);
  }

  async function loadProjects() {
    return api.get('/projects', { params: { digest: true } }).then((response) => {
      projects.value = response.data.sort((a: ProjectDto, b: ProjectDto) => a.name.localeCompare(b.name));
      return api.options('/projects').then((response) => {
        perms.value.projects = new Perms(response);
        return response;
      });
    });
  }

  async function loadProject(name: string) {
    project.value = {} as ProjectDto;
    perms.value = {} as ProjectPerms;
    return api.get(`/project/${name}`).then((response) => {
      project.value = response.data;
      return Promise.all([
        api.options(`/project/${project.value.name}`).then((response) => {
          perms.value.project = new Perms(response);
          return response;
        }),
        api.options(`/project/${project.value.name}/summary`).then((response) => {
          console.log('summary', response);
          perms.value.summary = new Perms(response);
          return response;
        }),
        api.options(`/project/${project.value.name}/commands/_export`).then((response) => {
          perms.value.export = new Perms(response);
          return response;
        }),
        api.options(`/project/${project.value.name}/commands/_import`).then((response) => {
          perms.value.import = new Perms(response);
          return response;
        }),
        api.options(`/project/${project.value.name}/commands/_copy`).then((response) => {
          perms.value.copy = new Perms(response);
          return response;
        }),
        api.options(`/project/${project.value.name}/commands/_reload`).then((response) => {
          perms.value.reload = new Perms(response);
          return response;
        }),
        api.options(`/project/${project.value.name}/keystore`).then((response) => {
          perms.value.keystore = new Perms(response);
          return response;
        }),
        api.options(`/project/${project.value.name}/vcf-store`).then((response) => {
          perms.value.vcfstore = new Perms(response);
          return response;
        }),

        api.options(`/project/${project.value.name}`).then((response) => {
          perms.value.project = new Perms(response);
          return response;
        }),
      ]);
    });
  }

  async function loadAnalysesPermissions(name: string, tableName: string) {
    return Promise.all([
      api.options(`/project/${name}/table/${tableName}/analyses`),
      api.options(`/project/${name}/table/${tableName}/analyses_export`),
    ]).then(([analyses, analyses_export]) => {
      perms.value.analyses = new Perms(analyses);
      perms.value.vcfs = new Perms(analyses_export);
    });
  }

  async function loadVcfPermissions(name: string) {
    return Promise.all([
      api.options(`/project/${name}/vcf-store/samples`),
      api.options(`/project/${name}/vcf-store/vcfs`),
      api.options(`/project/${name}/permissions/vcf-store`),
      api.options(`/project/${name}/commands/_import_vcf`),
      api.options(`/project/${name}/commands/_export_vcf`),
    ]).then(([samples, vcfs, permissions_vcfstore, import_vcf, export_vcf]) => {
      perms.value.samples = new Perms(samples);
      perms.value.vcfs = new Perms(vcfs);
      perms.value.permissions_vcfstore = new Perms(permissions_vcfstore);
      perms.value.import_vcf = new Perms(import_vcf);
      perms.value.export_vcf = new Perms(export_vcf);
    });
  }

  async function addProject(project: ProjectDto) {
    return api.post('/projects', project).then((response) => response.data);
  }

  async function updateProject(toUpdate: ProjectDto) {
    return api.put(`/project/${toUpdate.name}`, toUpdate).then((response) => {
      if (toUpdate.name === project.value.name) project.value = toUpdate;
      return response;
    });
  }

  async function deleteProject(toDelete: ProjectDto) {
    return api.delete(`/project/${toDelete.name}`).then((response) => {
      if (toDelete.name === project.value.name) project.value = {} as ProjectDto;
      return response;
    });
  }

  /**
   * Must load the project first: @see {@link loadProject}.
   *
   * @returns A summary of the project counts (tables, views, variables, etc.)
   */
  async function loadSummary() {
    summary.value = {} as ProjectSummaryDto;
    return api.get(`/project/${project.value.name}/summary`).then((response) => {
      summary.value = response.data;
      return response;
    });
  }

  async function copyCommand(name: string, options: CopyCommandOptionsDto) {
    return api.post(`/project/${name}/commands/_copy`, options);
  }

  async function exportCommand(name: string, options: ExportCommandOptionsDto) {
    return api.post(`/project/${name}/commands/_export`, options);
  }

  async function importCommand(name: string, options: ImportCommandOptionsDto) {
    return api.post(`/project/${name}/commands/_import`, options);
  }

  async function loadCommandStates() {
    commandStates.value = [];
    return api.get(`/project/${project.value.name}/commands`).then((response) => {
      commandStates.value = response.data;
      return response;
    });
  }

  async function clearCommandStates(commands: CommandStateDto[]) {
    if (commands.length === 0) {
      return api.delete(`/project/${project.value.name}/commands/completed`).then((response) => {
        commandStates.value = [];
        return response;
      });
    } else {
      return Promise.all(commands.map((cmd) => api.delete(`/project/${project.value.name}/command/${cmd.id}`)));
    }
  }

  function cancelCommandState(command: CommandStateDto) {
    return api.put(`/project/${project.value.name}/command/${command.id}/status`, {
      status: CommandStateDto_Status.CANCELED,
    });
  }

  async function reloadDbCommand(name: string) {
    return api.post(`/project/${name}/commands/_reload`);
  }

  async function getState(name: string): Promise<ProjectDatasourceStatusDto> {
    return api.get(`/project/${name}/state`).then((response) => response.data);
  }

  async function loadAcls(project: ProjectDto) {
    acls.value = [];
    return api.get(`/project/${project.name}/permissions/project`).then((response) => {
      acls.value = response.data;
      return response;
    });
  }

  async function loadSubjects() {
    subjects.value = [] as Subject[];
    return api.get(`/project/${project.value.name}/permissions/subjects`).then((response) => {
      subjects.value = response.data;
      return response;
    });
  }

  async function deleteSubject(subject: Subject) {
    return api.delete(`/project/${project.value.name}/permissions/subject/${subject.principal}`, {
      params: { type: subject.type },
    });
  }

  async function getSubjectPermissions(subject: Subject) {
    return api
      .get(`/project/${project.value.name}/permissions/subject/${subject.principal}`, {
        params: { type: subject.type },
      })
      .then((response) => {
        return response.data;
      });
  }

  async function deleteSubjectPermission(subject: Subject, acl: Acl) {
    const resource = acl.resource
      .replace(/^\//, '')
      .replace(/^project.*/, 'project')
      .replace(/^datasource\/[^/]+$/, 'datasource')
      .replace(/.*(table|view)/, 'table');

    const params = { principal: subject.principal, type: subject.type };
    return api.delete(`/project/${project.value.name}/permissions/${resource}`, {
      params,
    });
  }

  async function backup(project: ProjectDto, options: BackupCommandOptionsDto) {
    return api.post(`/project/${project.name}/commands/_backup`, options);
  }

  async function restore(project: ProjectDto, options: RestoreCommandOptionsDto) {
    return api.post(`/project/${project.name}/commands/_restore`, options);
  }

  async function archive(project: ProjectDto) {
    return api.delete(`/project/${project.name}`, { params: { archive: true } });
  }

  async function getIdMappings(name: string) {
    return api.get(`/project/${name}/identifiers-mappings`).then((response) => response.data);
  }

  async function getVcfStore(name: string) {
    return api.get(`/project/${name}/vcf-store`).then((response) => response.data);
  }

  async function getVcfSamplesMapping(name: string) {
    return api.get(`/project/${name}/vcf-store/samples`).then((response) => response.data);
  }

  async function addVcfSamplesMapping(name: string, mapping: VCFSamplesMappingDto) {
    return api.put(`/project/${name}/vcf-store/samples`, mapping);
  }

  async function deleteVcfSamplesMapping(name: string) {
    return api.delete(`/project/${name}/vcf-store/samples`);
  }

  async function getVcfs(name: string) {
    return api.get(`/project/${name}/vcf-store/vcfs`).then((response) => response.data);
  }

  async function deleteVcf(name: string, files: string[]) {
    return api
      .delete(`/project/${name}/vcf-store/vcfs`, {
        params: { file: files },
        paramsSerializer: {
          indexes: null,
        },
      })
      .then((response) => response.data);
  }

  async function importVcfFiles(name: string, importOptions: ImportVCFCommandOptionsDto) {
    return api.post(`/project/${name}/commands/_import_vcf`, importOptions).then((response) => response.data.id);
  }

  async function exportVcfFiles(name: string, exportOptions: ExportVCFCommandOptionsDto) {
    return api.post(`/project/${name}/commands/_export_vcf`, exportOptions).then((response) => response.data.id);
  }

  async function addIdMappings(project: ProjectDto, mapping: ProjectDto_IdentifiersMappingDto) {
    if (!project.idMappings) project.idMappings = [];
    const index: number = project.idMappings.findIndex(
      (m) => m.name === mapping.name && m.entityType === mapping.entityType && m.mapping === mapping.mapping
    );

    if (index === -1) {
      project.idMappings = project.idMappings.concat(mapping);
      return updateProject(project);
    }

    return Promise.resolve();
  }

  async function deleteIdMappings(project: ProjectDto, mapping: ProjectDto_IdentifiersMappingDto) {
    if (!project.idMappings) return Promise.resolve();
    project.idMappings = project.idMappings.filter(
      (m) => m.entityType !== mapping.entityType || m.name !== mapping.name
    );
    return updateProject(project);
  }

  async function getKeyPairs(name: string) {
    return api.get(`/project/${name}/keystore`).then((response) => response.data);
  }

  async function addKeyPair(name: string, keyPair: KeyForm) {
    return api.post(`/project/${name}/keystore`, keyPair);
  }

  async function deleteKeyPair(name: string, alias: string) {
    return api.delete(`/project/${name}/keystore/${alias}`);
  }

  async function getAnalysis(name: string, table: string, analysisName: string) {
    return api.get(`/project/${name}/table/${name}/analysis/${analysisName}`).then((response) => response.data);
  }

  async function getAnalyses(name: string, table: string) {
    return api.get(`/project/${name}/table/${table}/analyses`).then((response) => response.data);
  }

  async function runAnalysis(name: string, analysis: AnalyseCommandOptionsDto) {
    return api.post(`/project/${name}/commands/_analyse`, analysis).then((response) => response.data.id);
  }

  async function removeAnalysis(name: string, table: string, analysisName: string) {
    return api.delete(`/project/${name}/table/${table}/analysis/${analysisName}`);
  }

  function getAnalysisReportUrl(name: string, table: string, analysisName: string, resultId: string) {
    return `${baseUrl}/project/${name}/table/${table}/analysis/${analysisName}/result/${resultId}/_report`;
  }

  async function removeAnalysisResult(name: string, table: string, analysisName: string, resultId: string) {
    return api.delete(`/project/${name}/table/${table}/analysis/${analysisName}/result/${resultId}`);
  }

  return {
    projects,
    project,
    summary,
    commandStates,
    perms,
    subjects,
    acls,
    isReady,
    initProjects,
    initProject,
    refreshProject,
    addProject,
    updateProject,
    deleteProject,
    loadSummary,
    loadCommandStates,
    loadAcls,
    loadSubjects,
    loadVcfPermissions,
    loadAnalysesPermissions,
    deleteSubject,
    getSubjectPermissions,
    deleteSubjectPermission,
    clearCommandStates,
    cancelCommandState,
    copyCommand,
    exportCommand,
    importCommand,
    reloadDbCommand,
    getState,
    reset,
    backup,
    restore,
    archive,
    getIdMappings,
    getVcfStore,
    getVcfSamplesMapping,
    addVcfSamplesMapping,
    deleteVcfSamplesMapping,
    getVcfs,
    deleteVcf,
    importVcfFiles,
    exportVcfFiles,
    addIdMappings,
    deleteIdMappings,
    getKeyPairs,
    addKeyPair,
    deleteKeyPair,
    getAnalysis,
    getAnalyses,
    runAnalysis,
    removeAnalysis,
    getAnalysisReportUrl,
    removeAnalysisResult,
  };
});
