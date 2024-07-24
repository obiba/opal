import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { ProjectDto, ProjectSummaryDto } from 'src/models/Projects';
import { Acl } from 'src/models/Opal';
import { Subject } from 'src/models/Opal';
import {
  CommandStateDto,
  CommandStateDto_Status,
  ImportCommandOptionsDto,
  ExportCommandOptionsDto,
  CopyCommandOptionsDto,
} from 'src/models/Commands';
import { Perms } from 'src/utils/authz';

interface ProjectPerms {
  export: Perms | undefined;
  copy: Perms | undefined;
  import: Perms | undefined;
  subjects: Perms | undefined;
}

export const useProjectsStore = defineStore('projects', () => {
  const projects = ref([] as ProjectDto[]);
  const project = ref({} as ProjectDto);
  const summary = ref({} as ProjectSummaryDto);
  const commandStates = ref([] as CommandStateDto[]);
  const perms = ref({} as ProjectPerms);
  const subjects = ref([] as Subject[]);

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

  async function loadProjects() {
    return api.get('/projects', { params: { digest: true } }).then((response) => {
      projects.value = response.data.sort((a: ProjectDto, b: ProjectDto) => a.name.localeCompare(b.name));
      return response;
    });
  }

  async function loadProject(name: string) {
    project.value = {} as ProjectDto;
    perms.value = {} as ProjectPerms;
    return api.get(`/project/${name}`).then((response) => {
      project.value = response.data;
      return Promise.all([
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
        api.options(`/project/${project.value.name}/permissions/subjects`).then((response) => {
          perms.value.subjects = new Perms(response);
          return response;
        }),
      ]);
    });
  }

  async function addProject(project: ProjectDto) {
    return api.post('/projects', project).then((response) => response.data);
  }

  async function updateProject(updated: ProjectDto) {
    return api.put(`/project/${updated.name}`, updated).then((response) => {
      if (updated.name === project.value.name) project.value = updated;
      return response;
    });
  }

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
      .replace(/^datasource.*/, 'datasource')
      .replace(/.*(table|view)/, 'table')
      .replace(/.*report-template/, 'report-template');

    const params = { principal: subject.principal, type: subject.type };
    return api.delete(`/project/${project.value.name}/permissions/${resource}`, {
      params,
    });
  }

  return {
    projects,
    project,
    summary,
    commandStates,
    perms,
    subjects,
    initProjects,
    initProject,
    addProject,
    updateProject,
    loadSummary,
    loadCommandStates,
    loadSubjects,
    deleteSubject,
    getSubjectPermissions,
    deleteSubjectPermissions: deleteSubjectPermission,
    clearCommandStates,
    cancelCommandState,
    copyCommand,
    exportCommand,
    importCommand,
    reset,
  };
});
