import { defineStore } from 'pinia';
import { api, baseUrl } from 'src/boot/api';
import { DatasourceDto, TableDto, ViewDto, VariableDto } from 'src/models/Magma';
import { Perms } from 'src/utils/authz';

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
  const variables = ref([] as VariableDto[]); // current table variables
  const variable = ref({} as VariableDto); // current variable
  const perms = ref({} as DatasourcePerms);

  function reset() {
    datasource.value = {} as DatasourceDto;
    tables.value = [];
    table.value = {} as TableDto;
    view.value = {} as ViewDto;
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
    if (datasource.value?.name !== dsName) {
      return loadDatasource(dsName).then(() => {
        if (table.value?.name !== tName) {
          return loadTable(tName);
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
      return initDatasourceTable(dsName, tName).then(() =>
        loadTableVariables()
      );
    } else if (table.value?.name !== tName) {
      return loadTable(tName).then(() => loadTableVariables());
    } else if (variables.value.length === 0) {
      return loadTableVariables();
    } else {
      return Promise.resolve();
    }
  }

  async function initDatasourceTableVariable(
    dsName: string,
    tName: string,
    vName: string
  ) {
    if (datasource.value?.name !== dsName) {
      return initDatasourceTable(dsName, tName).then(() =>
        loadTableVariable(vName)
      );
    } else {
      return loadTableVariable(vName);
    }
  }

  // Loaders
  async function loadDatasource(name: string) {
    datasource.value = {} as DatasourceDto;
    delete perms.value.datasource;
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
    delete perms.value.tables;
    return api
      .get(`/datasource/${datasource.value.name}/tables`, { params: { counts: true } } )
      .then((response) => {
        perms.value.tables = new Perms(response);
        tables.value = response.data;
        return response;
      });
  }

  async function loadTable(name: string) {
    table.value = {} as TableDto;
    view.value = {} as ViewDto;
    delete perms.value.table;
    delete perms.value.tableValueSets;
    variables.value = [];
    delete perms.value.variables;
    return api
      .get(`/datasource/${datasource.value.name}/table/${name}`, { params: { counts: true } })
      .then((response) => {
        perms.value.table = new Perms(response);
        table.value = response.data;
        return Promise.all([
          table.value.viewType === 'View' ? loadView(name) : Promise.resolve(),
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
    return api
      .get(`/datasource/${datasource.value.name}/view/${name}`)
      .then((response) => {
        view.value = response.data;
        return response;
      });
  }

  async function loadTableVariables() {
    variables.value = [];
    delete perms.value.variables;
    return api
      .get(
        `/datasource/${datasource.value.name}/table/${table.value.name}/variables`
      )
      .then((response) => {
        perms.value.variables = new Perms(response);
        variables.value = response.data;
        return response;
      });
  }

  async function loadTableVariable(name: string) {
    variable.value = {} as VariableDto;
    delete perms.value.variable;
    return api
      .get(
        `/datasource/${datasource.value.name}/table/${table.value.name}/variable/${name}`
      )
      .then((response) => {
        perms.value.variable = new Perms(response);
        variable.value = response.data;
        return api.options(`/project/${datasource.value.name}/permissions/table/${table.value.name}/variable/${name}`).then((response) => {
          perms.value.variablePermissions = new Perms(response);
          return response;
        });
      });
  }

  function isNewTableNameValid(name: string) {
    return name && name.trim() !== '' && !tables.value.map((t) => t.name).includes(name.trim());
  }

  async function addTable(name: string, entityType: string) {
    return api.post(
      `/datasource/${datasource.value.name}/tables`,
      {
        name: name.trim(),
        entityType: entityType ? entityType.trim() : 'Participant'
      }
    )
  }

  async function addVariablesView(project: string, name: string, from: string[], variables: VariableDto[]) {
    return api.post(
      `/datasource/${project}/views`,
      {
        name: name.trim(),
        from: from,
        'Magma.VariableListViewDto.view':{variables}
      }
    )
  }

  async function updateTable(original: TableDto, updated: TableDto) {
    return api.put(
      `/datasource/${datasource.value.name}/table/${original.name}`,
      updated)
  }

  async function updateView(project: string, name: string, updated: ViewDto, comment: string) {
    return api.put(
      `/datasource/${project}/view/${name}`,
      updated, { params: { comment } })
  }

  async function deleteTable(name: string) {
    return api.delete(
      `/datasource/${datasource.value.name}/table/${name}`)
  }

  async function deleteTables(tables: string[] | undefined) {
    return api.delete(
      `/datasource/${datasource.value.name}/tables`, {
        params: { table: tables },
        paramsSerializer: {
          indexes: null, // no brackets at all
        },
      })
  }

  async function deleteVariables(variables: string[] | undefined) {
    const type = table.value.viewType ? 'view' : 'table';
    return api.delete(
      `/datasource/${datasource.value.name}/${type}/${table.value.name}/variables`, {
        params: { variable: variables },
        paramsSerializer: {
          indexes: null, // no brackets at all
        },
      }).then(() => loadTableVariables());
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

  function loadVariableSummary(localVar: VariableDto | undefined, fullIfCached: boolean, limit: number | undefined) {
    const link = localVar ? `${localVar.parentLink.link}/variable/${localVar.name}` : `/datasource/${datasource.value.name}/table/${table.value.name}/variable`;
    const params = { fullIfCached };
    if (limit) {
      params.limit = limit;
    } else {
      params.resetCache = true;
    }
    return api
      .get(`${link}/summary`, { params })
      .then((response) => {
        return response.data;
      });
  }

  async function getAllTables(entityType: string | undefined): Promise<TableDto[]> {
    return api.get('/datasources/tables', { params: { entityType } }).then((response) => response.data as TableDto[]);
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
    return api.put(`/datasource/${datasource.value.name}/${tableType}/${table.value.name}/variable/${variable.value.name}`, localVar).then(() => {
      return loadTableVariables();
    });
  }

  function saveDerivedVariable(localVar: VariableDto, comment: string | undefined) {
    const link = `/datasource/${datasource.value.name}/view/${table.value.name}`;
    return api.put(`${link}/variable/${variable.value.name}`, localVar, { params: { comment } }).then(() => {
      return Promise.all([
        loadTableVariable(localVar.name),
        loadTableVariables(),
      ]);
    });
  }

  async function createView(destination: string, view: ViewDto) {
    delete view.datasourceName;
    delete view.status;
    return api.post(`/datasource/${destination}/views`, view);
  }

  async function getView(project: string, name: string) {
    return api
      .get(`/datasource/${project}/view/${name}`)
      .then((response) => {
        return response.data;
      });
  }

  async function annotate(variables: VariableDto[], taxonomy: string, vocabulary: string, termOrTexts: string | { [key: string]: string | undefined}) {
    const parentLink = variables[0].parentLink?.link;
    if (!parentLink) return Promise.reject('No parent link found');

    variables.forEach((v) => {
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
            v.attributes.push({ namespace: taxonomy, name: vocabulary, locale,  value });
          }
        }
      }
    });

    return api.post(`${parentLink}/variables`, variables);
  }

  async function deleteAnnotation(variables: VariableDto[], taxonomy: string, vocabulary: string) {
    const parentLink = variables[0].parentLink?.link;
    if (!parentLink) return Promise.reject('No parent link found');

    variables.forEach((v) => {
      if (v.attributes !== undefined) {
        v.attributes = v.attributes.filter((a) => a.namespace !== taxonomy || a.name !== vocabulary);
      }
    });

    return api.post(`${parentLink}/variables`, variables);
  }

  return {
    datasource,
    tables,
    table,
    view,
    variables,
    variable,
    perms,
    initDatasourceTables,
    initDatasourceTable,
    initDatasourceTableVariables,
    initDatasourceTableVariable,
    loadTable,
    isNewTableNameValid,
    addTable,
    addVariablesView,
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
    reset,
  };
});
