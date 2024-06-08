import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { ProjectDto, ProjectSummaryDto } from 'src/models/Projects';
import { CommandStateDto, CommandStateDto_Status, ImportCommandOptionsDto, ExportCommandOptionsDto, CopyCommandOptionsDto } from 'src/models/Commands';
import { Perms } from 'src/utils/authz';

interface ProjectPerms {
  export: Perms | undefined;
  copy: Perms | undefined;
  import: Perms | undefined;
}

export const useProjectsStore = defineStore('projects', () => {
  const projects = ref([] as ProjectDto[]);
  const project = ref({} as ProjectDto);
  const summary = ref({} as ProjectSummaryDto);
  const commandStates = ref([] as CommandStateDto[]);
  const perms = ref({} as ProjectPerms);

  function reset() {
    projects.value = [];
    project.value = {} as ProjectDto;
    summary.value = {} as ProjectSummaryDto;
    commandStates.value = [];
    perms.value = {} as ProjectPerms;
  }

  async function initProjects() {
    projects.value = [];
    loadProjects();
  }

  async function initProject(name: string) {
    if (project.value.name !== name) {
      return loadProject(name);
    }
    return Promise.resolve();
  }

  async function loadProjects() {
    return api
      .get('/projects', { params: { digest: true } })
      .then((response) => {
        projects.value = response.data.sort((a: ProjectDto, b: ProjectDto) =>
          a.name.localeCompare(b.name)
        );
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
      ]);
    });
  }

  async function loadSummary() {
    summary.value = {} as ProjectSummaryDto;
    return api
      .get(`/project/${project.value.name}/summary`)
      .then((response) => {
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
    return api.put(`/project/${project.value.name}/command/${command.id}/status`, { status: CommandStateDto_Status.CANCELED });
  }

  return {
    projects,
    project,
    summary,
    commandStates,
    perms,
    initProjects,
    initProject,
    loadSummary,
    loadCommandStates,
    clearCommandStates,
    cancelCommandState,
    copyCommand,
    exportCommand,
    importCommand,
    reset,
  };
});
