import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { DatasourceDto, TableDto, VariableDto } from 'src/models/Magma';
import { DatasourceFactory } from 'src/components/models';

const projectsStore = useProjectsStore();

export const useTransientDatasourceStore = defineStore('transientDatasource', () => {

  const project = ref(''); // current project
  const datasource = ref({} as DatasourceDto); // current datasource
  const table = ref({} as TableDto); // selected datasource table
  const variables = ref([] as VariableDto[]); // selected datasource table variables

  function reset() {
    datasource.value = {} as DatasourceDto;
    table.value = {} as TableDto;
    variables.value = [];
  }

  async function createDatasource(factory: DatasourceFactory, merge: boolean) {
    project.value = projectsStore.project.name;
    return api.post<DatasourceDto>(`/project/${project.value}/transient-datasources`, factory, { params: { merge } })
      .then((response) => {
        datasource.value = response.data;
      });
  }

  async function deleteDatasource() {
    if (!datasource.value.name)
      return Promise.resolve();
    return api.delete(`/datasource/${datasource.value.name}`).then(() => {
      reset();
    });
  }

  async function loadTable(tableName: string) {
    return api.get<TableDto>(`/datasource/${datasource.value.name}/table/${tableName}`, { params: { counts: true } })
      .then((response) => {
        table.value = response.data;
      });
  }

  async function loadVariables() {
    return api.get<VariableDto[]>(`/datasource/${datasource.value.name}/table/${table.value.name}/variables`)
      .then((response) => {
        variables.value = response.data;
      });
  }

  function loadValueSets(offset: number, limit: number, select: string[] | undefined) {
    const params = { offset, limit };
    if (select && select.length > 0) {
      params.select = `name().matches(/^${select.join('$|^')}$/)`
    }
    return api
      .get(`/datasource/${datasource.value.name}/table/${table.value.name}/valueSets`, { params })
      .then((response) => {
        return response.data;
      });
  }


  return {
    project,
    datasource,
    table,
    variables,
    reset,
    createDatasource,
    deleteDatasource,
    loadTable,
    loadVariables,
    loadValueSets,
  };
});
