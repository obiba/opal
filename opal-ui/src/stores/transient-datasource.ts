import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { DatasourceDto, TableDto, ViewDto, VariableDto } from 'src/models/Magma';
import type { DatasourceFactory } from 'src/components/models';
import type { FileDto } from 'src/models/Opal';

const projectsStore = useProjectsStore();

export const useTransientDatasourceStore = defineStore('transientDatasource', () => {
  const project = ref(''); // current project
  const datasource = ref({} as DatasourceDto); // current datasource
  const table = ref({} as TableDto); // selected datasource table
  const variables = ref([] as VariableDto[]); // selected datasource table variables

  function reset() {
    project.value = '';
    datasource.value = {} as DatasourceDto;
    table.value = {} as TableDto;
    variables.value = [];
  }

  async function createFileDatasource(file: FileDto) {
    if (file.name.endsWith('.xml')) {
      const fileBaseName = file.name.split('.').slice(0, -1).join('.');
      const factory = {
        'Magma.StaticDatasourceFactoryDto.params': {
          views: [
            {
              name: fileBaseName,
              from: [],
              innerFrom: [],
              'Magma.FileViewDto.view': {
                filename: file.path,
                type: 'SERIALIZED_XML',
              },
            } as ViewDto,
          ],
        },
      } as DatasourceFactory;
      return createDatasource(factory, false);
    } else {
      const factory = {
        'Magma.ExcelDatasourceFactoryDto.params': {
          file: file.path,
          readOnly: true,
        },
      } as DatasourceFactory;
      return createDatasource(factory, false);
    }
  }

  async function createDatasource(factory: DatasourceFactory, merge: boolean) {
    project.value = projectsStore.project.name;
    return api
      .post<DatasourceDto>(`/project/${project.value}/transient-datasources`, factory, { params: { merge } })
      .then((response) => {
        datasource.value = response.data;
      });
  }

  async function deleteDatasource() {
    if (!datasource.value.name) return Promise.resolve();
    return api.delete(`/datasource/${datasource.value.name}`).then(() => {
      reset();
    });
  }

  async function loadTable(tableName: string) {
    return api
      .get<TableDto>(`/datasource/${datasource.value.name}/table/${tableName}`, { params: { counts: true } })
      .then((response) => {
        table.value = response.data;
      });
  }

  async function loadVariables() {
    return api
      .get<VariableDto[]>(`/datasource/${datasource.value.name}/table/${table.value.name}/variables`)
      .then((response) => {
        variables.value = response.data;
      });
  }

  function loadValueSets(offset: number, limit: number, select: string[] | undefined) {
    const params: { offset: number, limit: number, select: string | undefined } = { offset, limit, select: undefined };
    if (select && select.length > 0) {
      params.select = `name().matches(/^${select.join('$|^')}$/)`;
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
    createFileDatasource,
    createDatasource,
    deleteDatasource,
    loadTable,
    loadVariables,
    loadValueSets,
  };
});
