import { defineStore } from 'pinia';
import { api, baseUrl } from 'src/boot/api';
import { Datasource, Table, View, Variable } from 'src/components/models';
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
  const datasource = ref({} as Datasource); // current datasource
  const tables = ref([] as Table[]); // current datasource tables
  const table = ref({} as Table); // current table
  const view = ref({} as View); // current view
  const variables = ref([] as Variable[]); // current table variables
  const variable = ref({} as Variable); // current variable
  const perms = ref({} as DatasourcePerms);

  function reset() {
    datasource.value = {} as Datasource;
    tables.value = [];
    table.value = {} as Table;
    view.value = {} as View;
    variables.value = [];
    variable.value = {} as Variable;
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
    } else {
      return loadTableVariables();
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
    datasource.value = {} as Datasource;
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
    table.value = {} as Table;
    view.value = {} as View;
    delete perms.value.table;
    delete perms.value.tableValueSets;
    variables.value = [];
    delete perms.value.variables;
    return api
      .get(`/datasource/${datasource.value.name}/table/${name}`)
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
    view.value = {} as View;
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
    variable.value = {} as Variable;
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
    deleteTable,
    deleteTables,
    downloadTablesDictionary,
    downloadTableDictionary,
    downloadViews,
    downloadView,
    reset,
  };
});
