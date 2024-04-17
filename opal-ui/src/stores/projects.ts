import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { Project, ProjectSummary } from 'src/components/models';

export const useProjectsStore = defineStore('projects', () => {
  const projects = ref([] as Project[]);
  const project = ref({} as Project);
  const summary = ref({} as ProjectSummary);

  function reset() {
    projects.value = [];
    project.value = {} as Project;
    summary.value = {} as ProjectSummary;
  }

  async function initProjects() {
    return api
      .get('/projects', { params: { digest: true } })
      .then((response) => {
        projects.value = response.data.sort((a: Project, b: Project) =>
          a.name.localeCompare(b.name)
        );
        return response;
      });
  }

  async function initProject(name: string) {
    if (project.value.name !== name) {
      return loadProject(name);
    }
    return Promise.resolve();
  }

  async function loadProject(name: string) {
    return api.get(`/project/${name}`).then((response) => {
      project.value = response.data;
      return response;
    });
  }

  async function loadSummary() {
    return api
      .get(`/project/${project.value.name}/summary`)
      .then((response) => {
        summary.value = response.data;
        return response;
      });
  }

  return {
    projects,
    project,
    summary,
    initProjects,
    initProject,
    loadSummary,
    reset,
  };
});
