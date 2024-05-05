import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { DatasourceDto } from 'src/models/Magma';
import { DatasourceFactory } from 'src/components/models';

const projectsStore = useProjectsStore();

export const useTransientDatasourceStore = defineStore('transientDatasource', () => {

  const project = ref(''); // current project
  const datasource = ref({} as DatasourceDto); // current datasource

  function reset() {
    datasource.value = {} as DatasourceDto;
  }

  async function createDatasource(factory: DatasourceFactory) {
    project.value = projectsStore.project.name;
    return api.post<DatasourceDto>(`/project/${project.value}/transient-datasources`, factory, { params: { merge: false } })
      .then((response) => {
        datasource.value = response.data;
      });
  }

  async function deleteDatasource() {
    if (!datasource.value.name)
      return Promise.resolve();
    return api.delete(`/datasource/${datasource.value.name}`);
  }

  return {
    project,
    datasource,
    reset,
    createDatasource,
    deleteDatasource,
  };
});
