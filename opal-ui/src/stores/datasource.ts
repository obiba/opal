import { defineStore } from 'pinia';
import { api, baseUrl } from 'src/boot/api';
import type { DatasourceDto, TableDto, ViewDto, VariableDto, AttributeDto, ResourceViewDto } from 'src/models/Magma';
import { Perms } from 'src/utils/authz';
import type { AttributesBundle } from 'src/components/models';
import type { QueryResultDto } from 'src/models/Search';
import type { ScheduleDto, TableIndexStatusDto } from 'src/models/Opal';

interface DatasourcePerms {
  datasource: Perms | undefined;
  datasourcePermissions: Perms | undefined;
  tables: Perms | undefined;
  table: Perms | undefined;
  tablePermissions: Perms | undefined;
  tableValueSets: Perms | undefined;
  variables: Perms | undefined;
  variable: Perms | undefined;
  variablePermissions: Perms | undefined;
}

export const useDatasourceStore = defineStore('datasource', () => {
  const datasource = ref({} as DatasourceDto); // current datasource
  const tables = ref([] as TableDto[]); // current datasource tables
  const table = ref({} as TableDto); // current table
  const view = ref({} as ViewDto); // current view
  const tableIndex = ref({} as TableIndexStatusDto); // current table index
  const variables = ref([] as VariableDto[]); // current table variables
  const variable = ref({} as VariableDto); // current variable
  const perms = ref({} as DatasourcePerms);

  const variableAttributesBundles = computed(() => {
    if (!variable.value?.attributes) return [];
    // bundle attributes by namespace and name into a AttributesBundle array
    const bundles = variable.value.attributes.reduce((acc: AttributesBundle[], attr) => {
      const id = attr.namespace ? `${attr.namespace}::${attr.name}` : attr.name;
      const index = acc.findIndex((bundle) => bundle.id === id);
      if (index === -1) {
        acc.push({
          id,
          attributes: [attr],
        } as AttributesBundle);
      } else {
        acc[index]?.attributes.push(attr);
      }
      return acc;
    }, []);
    return bundles;
  });

  function reset() {
    datasource.value = {} as DatasourceDto;
    tables.value = [];
    table.value = {} as TableDto;
    view.value = {} as ViewDto;
    tableIndex.value = {} as TableIndexStatusDto;
    variables.value = [];
    variable.value = {} as VariableDto;
    perms.value = {} as DatasourcePerms;
  }

  // Initializers
  async function initDatasourceTables(dsName: string) {
    if (datasource.value?.name !== dsName) {
      return loadDatasource(dsName).then(() => loadTables());
    } else {
      return loadTables();
    }
  }

  async function initDatasourceTable(dsName: string, tName: string) {
    if (datasource.value?.name !== dsName || !tables.value) {
      return initDatasourceTables(dsName).then(() => {
        if (table.value?.name !== tName) {
          return loadTable(tName).then(() => Promise.resolve());
        } else {
          return Promise.resolve();
        }
      });
    } else if (table.value?.name !== tName) {
      return loadTable(tName);
    } else {
      return Promise.resolve();
    }
  }

  async function initDatasourceTableVariables(dsName: string, tName: string) {
    if (datasource.value?.name !== dsName) {
      return initDatasourceTable(dsName, tName);
    } else if (table.value?.name !== tName) {
      return loadTable(tName);
    } else if (variables.value.length === 0) {
      return loadTableVariables();
    } else {
      return Promise.resolve();
    }
  }

  async function initDatasourceTableVariable(dsName: string, tName: string, vName: string) {
    if (datasource.value?.name !== dsName || table.value?.name !== tName) {
      return initDatasourceTable(dsName, tName).then(() => loadTableVariable(vName));
    } else {
      return loadTableVariable(vName);
    }
  }

  // Loaders
  async function loadDatasource(name: string) {
    datasource.value = {} as DatasourceDto;
    perms.value.datasource = undefined;
    return api.get(`/datasource/${name}`).then((response) => {
      perms.value.datasource = new Perms(response);
      datasource.value = response.data;
      return api.options(`/project/${name}/permissions/datasource`).then((response) => {
        perms.value.datasourcePermissions = new Perms(response);
        return response;
      });
    });
  }

  async function loadTables() {
    tables.value = [];
    // table.value = {} as TableDto;
    // view.value = {} as ViewDto;
    // tableIndex.value = {} as TableIndexStatusDto;
    // variables.value = [];
    // variable.value = {} as VariableDto;
    perms.value.tables = undefined;
    return api.get(`/datasource/${datasource.value.name}/tables`, { params: { counts: true } }).then((response) => {
      perms.value.tables = new Perms(response);
      tables.value = response.data;
      return response;
    });
  }

  async function loadTable(name: string) {
    table.value = {} as TableDto;
    view.value = {} as ViewDto;
    tableIndex.value = {} as TableIndexStatusDto;
    perms.value.table = undefined;
    perms.value.tableValueSets = undefined;
    variables.value = [];
    perms.value.variables = undefined;
    return api
      .get(`/datasource/${datasource.value.name}/table/${name}`, { params: { counts: true } })
      .then((response) => {
        perms.value.table = new Perms(response);
        table.value = response.data;
        return Promise.all([
          table.value.viewType !== undefined ? loadView(name) : Promise.resolve(),
          loadTableVariables(),
          api.options(`/datasource/${datasource.value.name}/table/${name}/valueSets`).then((response) => {
            perms.value.tableValueSets = new Perms(response);
            return response;
          }),
          api.options(`/project/${datasource.value.name}/permissions/table/${name}`).then((response) => {
            perms.value.tablePermissions = new Perms(response);
            return response;
          }),
        ]);
      });
  }

  async function loadView(name: string) {
    view.value = {} as ViewDto;
    return api.get(`/datasource/${datasource.value.name}/view/${name}`).then((response) => {
      view.value = response.data;
      return response;
    });
  }

  async function loadTableVariables() {
    variables.value = [];
    perms.value.variables = undefined;
    return api.get(`/datasource/${datasource.value.name}/table/${table.value.name}/variables`).then((response) => {
      perms.value.variables = new Perms(response);
      variables.value = response.data;
      return response;
    });
  }

  async function getTableVariables(dsName: string, tName: string) {
    return api.get(`/datasource/${dsName}/table/${tName}/variables`).then((response) => {
      return response.data;
    });
  }

  async function loadTableVariable(name: string) {
    variable.value = {} as VariableDto;
    perms.value.variable = undefined;
    return api
      .get(`/datasource/${datasource.value.name}/table/${table.value.name}/variable/${name}`)
      .then((response) => {
        perms.value.variable = new Perms(response);
        variable.value = response.data;
        return api
          .options(`/project/${datasource.value.name}/permissions/table/${table.value.name}/variable/${name}`)
          .then((response) => {
            perms.value.variablePermissions = new Perms(response);
            return response;
          });
      });
  }

  function isNewTableNameValid(name: string) {
    return name && name.trim() !== '' && !tables.value.map((t) => t.name).includes(name.trim());
  }

  async function addTable(name: string, entityType: string) {
    return api.post(`/datasource/${datasource.value.name}/tables`, {
      name: name.trim(),
      entityType: entityType ? entityType.trim() : 'Participant',
    });
  }

  async function addVariablesView(project: string, name: string, from: string[], variables: VariableDto[]) {
    return api.post(`/datasource/${project}/views`, {
      name: name.trim(),
      from: from,
      'Magma.VariableListViewDto.view': { variables },
    });
  }

  async function addResourceView(project: string, name: string, from: string, view: ResourceViewDto) {
    return api.post(`/datasource/${project}/views`, {
      name: name.trim(),
      from: [from],
      'Magma.ResourceViewDto.view': view,
    });
  }

  async function updateTable(original: TableDto, updated: TableDto) {
    return api.put(`/datasource/${datasource.value.name}/table/${original.name}`, updated);
  }

  async function updateView(project: string, name: string, updated: ViewDto, comment: string) {
    return api.put(`/datasource/${project}/view/${name}`, updated, { params: { comment } });
  }

  async function deleteTable(name: string) {
    return api.delete(`/datasource/${datasource.value.name}/table/${name}`);
  }

  async function deleteTables(tables: string[] | undefined) {
    return api.delete(`/datasource/${datasource.value.name}/tables`, {
      params: { table: tables },
      paramsSerializer: {
        indexes: null, // no brackets at all
      },
    });
  }

  async function deleteVariables(variables: string[] | undefined) {
    const type = table.value.viewType ? 'view' : 'table';
    return api
      .delete(`/datasource/${datasource.value.name}/${type}/${table.value.name}/variables`, {
        params: { variable: variables },
        paramsSerializer: {
          indexes: null, // no brackets at all
        },
      })
      .then(() => loadTableVariables());
  }

  function downloadTablesDictionary(tables: string[] | undefined) {
    let uri = `${baseUrl}/datasource/${datasource.value.name}/tables/excel`;
    if (tables && tables.length > 0) {
      uri += '?';
      tables.forEach((t) => (uri += `&table=${t}`));
    }
    window.open(uri, '_self');
  }

  function downloadTableDictionary() {
    const uri = `${baseUrl}/datasource/${datasource.value.name}/table/${table.value.name}/variables/excel`;
    window.open(uri, '_self');
  }

  function downloadViews(tables: string[] | undefined) {
    let uri = `${baseUrl}/datasource/${datasource.value.name}/views`;
    if (tables && tables.length > 0) {
      uri += '?';
      tables.forEach((t) => (uri += `&views=${t}`));
    }
    window.open(uri, '_self');
  }

  function downloadView() {
    const uri = `${baseUrl}/datasource/${datasource.value.name}/view/${table.value.name}/xml`;
    window.open(uri, '_self');
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

  function loadVariableSummary(localVar: VariableDto | undefined, fullIfCached: boolean, limit: number | undefined) {
    if (!localVar?.parentLink) return Promise.reject('No variable');
    const link = localVar
      ? `${localVar.parentLink?.link}/variable/${localVar.name}`
      : `/datasource/${datasource.value.name}/table/${table.value.name}/variable`;
    const params: { fullIfCached: boolean, limit: number | undefined, resetCache: boolean | undefined } = { fullIfCached, limit, resetCache: undefined };
    if (limit) {
      params.limit = limit;
    } else {
      params.resetCache = true;
    }
    return api.get(`${link}/summary`, { params }).then((response) => {
      return response.data;
    });
  }

  async function getAllTables(entityType: string | undefined): Promise<TableDto[]> {
    return api.get('/datasources/tables', { params: { entityType } }).then((response) => response.data as TableDto[]);
  }

  function addOrUpdateVariables(tName: string, localVars: VariableDto[]) {
    const link = `/datasource/${datasource.value.name}/table/${tName}`;
    return api.post(`${link}/variables`, localVars);
  }

  function addVariable(localVar: VariableDto) {
    const tableType = table.value.viewType ? 'view' : 'table';
    const link = `/datasource/${datasource.value.name}/${tableType}/${table.value.name}`;
    const isNew = variables.value === undefined || variables.value.find((v) => v.name === localVar.name) === undefined;
    localVar.entityType = table.value.entityType;
    if (isNew) {
      return api.post(`${link}/variables`, [localVar]).then(() => {
        return loadTableVariables();
      });
    }
    return api.put(`${link}/variable/${localVar.name}`, localVar).then(() => {
      return loadTableVariables();
    });
  }

  function updateVariable(localVar: VariableDto) {
    const tableType = table.value.viewType ? 'view' : 'table';
    localVar.entityType = table.value.entityType;
    return api
      .put(
        `/datasource/${datasource.value.name}/${tableType}/${table.value.name}/variable/${variable.value.name}`,
        localVar
      )
      .then(() => {
        return loadTableVariables();
      });
  }

  function saveDerivedVariable(localVar: VariableDto, comment: string | undefined) {
    const link = `/datasource/${datasource.value.name}/view/${table.value.name}`;
    return api.put(`${link}/variable/${variable.value.name}`, localVar, { params: { comment } }).then(() => {
      return Promise.all([loadTableVariable(localVar.name), loadTableVariables()]);
    });
  }

  async function createView(destination: string, view: ViewDto) {
    delete view.datasourceName;
    delete view.status;
    return api.post(`/datasource/${destination}/views`, view);
  }

  async function getView(project: string, name: string) {
    return api.get(`/datasource/${project}/view/${name}`).then((response) => {
      return response.data;
    });
  }

  async function annotate(
    variables: VariableDto[],
    taxonomy: string,
    vocabulary: string,
    termOrTexts: string | { [key: string]: string | undefined }
  ) {
    const grouped = groupVariablesByTableLink(variables);

    return Promise.all(
      Object.keys(grouped).map((tableLink) => {
        grouped[tableLink]?.forEach((v) => {
          if (v.attributes === undefined) {
            v.attributes = [];
          }
          // filter out existing annotations
          v.attributes = v.attributes.filter((a) => a.namespace !== taxonomy || a.name !== vocabulary);
          // add new annotations
          if (typeof termOrTexts === 'string') {
            v.attributes.push({ namespace: taxonomy, name: vocabulary, value: termOrTexts });
          } else {
            for (const [key, value] of Object.entries(termOrTexts)) {
              if (value !== undefined && value.trim() !== '') {
                const locale = key === 'default' ? undefined : key;
                v.attributes.push({ namespace: taxonomy, name: vocabulary, locale, value });
              }
            }
          }
        });
        return api.post(`${tableLink}/variables`, variables);
      })
    );
  }

  async function deleteAnnotation(variables: VariableDto[], taxonomy: string, vocabulary: string) {
    const grouped = groupVariablesByTableLink(variables);

    return Promise.all(
      Object.keys(grouped).map((tableLink) => {
        grouped[tableLink]?.forEach((v) => {
          if (v.attributes !== undefined) {
            v.attributes = v.attributes.filter((a) => a.namespace !== taxonomy || a.name !== vocabulary);
          }
        });
        return api.post(`${tableLink}/variables`, variables);
      })
    );
  }

  function groupVariablesByTableLink(variables: VariableDto[]) {
    const grouped: { [key: string]: VariableDto[] } = {};
    variables.forEach((v) => {
      const key = v.parentLink?.link;
      if (key) {
        if (!grouped[key]) {
          grouped[key] = [];
        }
        grouped[key].push(v);
      }
    });
    return grouped;
  }

  async function applyAttributes(variable: VariableDto, attributes: AttributeDto[]) {
    const parentLink = variable.parentLink?.link;
    if (!parentLink) return Promise.reject('No parent link found');

    if (variable.attributes === undefined) {
      variable.attributes = [];
    }
    variable.attributes.push(...attributes);

    return api.post(`${parentLink}/variables`, [variable]);
  }

  async function deleteAttributes(variable: VariableDto, namespace: string | undefined, name: string) {
    const parentLink = variable.parentLink?.link;
    if (!parentLink) return Promise.reject('No parent link found');

    if (variable.attributes !== undefined) {
      variable.attributes = variable.attributes.filter((a) => a.namespace !== namespace || a.name !== name);
    }

    return api.post(`${parentLink}/variables`, [variable]);
  }

  async function reconnectView(pName: string, name: string) {
    return api.put(`/datasource/${pName}/view/${name}/_init`);
  }

  //
  // Table summary: indexing and contingency
  //

  async function getContingencyTable(var0: string, var1: string): Promise<QueryResultDto> {
    return api
      .get(`/datasource/${datasource.value.name}/table/${table.value.name}/_contingency`, {
        params: {
          v0: var0,
          v1: var1,
        },
      })
      .then((response) => response.data);
  }

  async function loadTableIndex() {
    return api
      .get(`/datasource/${datasource.value.name}/table/${table.value.name}/index`)
      .then((response) => (tableIndex.value = response.data));
  }

  async function updateTableIndex() {
    return api.put(`/datasource/${datasource.value.name}/table/${table.value.name}/index`).finally(loadTableIndex);
  }

  async function deleteTableIndex() {
    return api.delete(`/datasource/${datasource.value.name}/table/${table.value.name}/index`).finally(loadTableIndex);
  }

  async function scheduleTableIndex(schedule: ScheduleDto) {
    return api
      .put(`/datasource/${datasource.value.name}/table/${table.value.name}/index/schedule`, schedule)
      .finally(loadTableIndex);
  }

  //
  // Entities
  //

  async function getEntityValueSet(datasource: string, table: string, entity: string) {
    return api.get(`/datasource/${datasource}/table/${table}/valueSet/${entity}`).then((response) => response.data);
  }

  return {
    datasource,
    tables,
    table,
    view,
    tableIndex,
    variables,
    variable,
    variableAttributesBundles,
    perms,
    initDatasourceTables,
    initDatasourceTable,
    initDatasourceTableVariables,
    initDatasourceTableVariable,
    loadTable,
    loadTableIndex,
    updateTableIndex,
    deleteTableIndex,
    scheduleTableIndex,
    loadTableVariables,
    getTableVariables,
    isNewTableNameValid,
    addTable,
    addOrUpdateVariables,
    addVariablesView,
    addResourceView,
    updateTable,
    updateView,
    deleteTable,
    deleteTables,
    deleteVariables,
    downloadTablesDictionary,
    downloadTableDictionary,
    downloadViews,
    downloadView,
    loadValueSets,
    loadTableVariable,
    loadVariableSummary,
    addVariable,
    updateVariable,
    saveDerivedVariable,
    getAllTables,
    createView,
    getView,
    annotate,
    deleteAnnotation,
    applyAttributes,
    deleteAttributes,
    reset,
    reconnectView,
    getContingencyTable,
    getEntityValueSet,
  };
});
