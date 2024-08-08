import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import {
  ProjectDto,
  ProjectSummaryDto,
  ProjectDatasourceStatusDto,
  ProjectDto_IdentifiersMappingDto,
} from 'src/models/Projects';
import { Acl } from 'src/models/Opal';
import { Subject, KeyForm } from 'src/models/Opal';
import {
  CommandStateDto,
  CommandStateDto_Status,
  ImportCommandOptionsDto,
  ExportCommandOptionsDto,
  CopyCommandOptionsDto,
  BackupCommandOptionsDto,
  RestoreCommandOptionsDto,
} from 'src/models/Commands';
import { Perms } from 'src/utils/authz';

interface ProjectPerms {
  export: Perms | undefined;
  copy: Perms | undefined;
  import: Perms | undefined;
  projects: Perms | undefined;
  project: Perms | undefined;
  keystore: Perms | undefined;
  vcfstore: Perms | undefined;
  reload: Perms | undefined;
}

export const useProjectsStore = defineStore('projects', () => {
  const projects = ref([] as ProjectDto[]);
  const project = ref({} as ProjectDto);
  const summary = ref({} as ProjectSummaryDto);
  const commandStates = ref([] as CommandStateDto[]);
  const perms = ref({} as ProjectPerms);
  const subjects = ref([] as Subject[]);
  const acls = ref([] as Acl[]);

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
        // FIXME: the server does not return proper permissions for the vcf-store
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
      .replace(/^datasource\/[^\/]+$/, 'datasource')
      .replace(/.*(table|view)/, 'table')
      .replace(/.*report-template/, 'report-template');

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

  return {
    projects,
    project,
    summary,
    commandStates,
    perms,
    subjects,
    acls,
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
    addIdMappings,
    deleteIdMappings,
    getKeyPairs,
    addKeyPair,
    deleteKeyPair,
  };
});
