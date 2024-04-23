import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { Project, ProjectSummary } from 'src/components/models';
import { CopyCommandOptions } from 'src/components/projects/models';
import { Perms } from 'src/utils/authz';

interface ProjectPerms {
  export: Perms | undefined;
  copy: Perms | undefined;
  import: Perms | undefined;
}

export const useProjectsStore = defineStore('projects', () => {
  const projects = ref([] as Project[]);
  const project = ref({} as Project);
  const summary = ref({} as ProjectSummary);
  const perms = ref({} as ProjectPerms);

  function reset() {
    projects.value = [];
    project.value = {} as Project;
    summary.value = {} as ProjectSummary;
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
        projects.value = response.data.sort((a: Project, b: Project) =>
          a.name.localeCompare(b.name)
        );
        return response;
      });
  }

  async function loadProject(name: string) {
    project.value = {} as Project;
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
    summary.value = {} as ProjectSummary;
    return api
      .get(`/project/${project.value.name}/summary`)
      .then((response) => {
        summary.value = response.data;
        return response;
      });
  }

  async function copyCommand(name: string, options: CopyCommandOptions) {
    return api.post(`/project/${name}/commands/_copy`, options).then((response) => {
      return response;
    });
  }

  return {
    projects,
    project,
    summary,
    perms,
    initProjects,
    initProject,
    loadSummary,
    copyCommand,
    reset,
  };
});
