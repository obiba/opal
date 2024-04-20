import { as } from 'app/dist/spa/assets/index.72143f35';
import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { Datasource, Table, Variable } from 'src/components/models';
import { Perms, getPerms } from 'src/utils/authz';

interface DatasourcePerms {
  datasource: Perms;
  tables: Perms
  table: Perms;
  variables: Perms;
  variable: Perms;
}

export const useDatasourceStore = defineStore('datasource', () => {
  const datasource = ref({} as Datasource); // current datasource
  const tables = ref([] as Table[]); // current datasource tables
  const table = ref({} as Table); // current table
  const variables = ref([] as Variable[]); // current table variables
  const variable = ref({} as Variable); // current variable
  const perms = ref({} as DatasourcePerms);

  function reset() {
    datasource.value = {} as Datasource;
    tables.value = [];
    table.value = {} as Table;
    variables.value = [];
    variable.value = {} as Variable;
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
    return api.get(`/datasource/${name}`).then((response) => {
      perms.value.datasource = getPerms(response);
      datasource.value = response.data;
      return response;
    });
  }

  async function loadTables() {
    tables.value = [];
    return api
      .get(`/datasource/${datasource.value.name}/tables`)
      .then((response) => {
        perms.value.tables = getPerms(response);
        tables.value = response.data;
        return response;
      });
  }

  async function loadTable(name: string) {
    table.value = {} as Table;
    variables.value = [];
    return api
      .get(`/datasource/${datasource.value.name}/table/${name}`)
      .then((response) => {
        perms.value.table = getPerms(response);
        table.value = response.data;
        return response;
      });
  }

  async function loadTableVariables() {
    variables.value = [];
    return api
      .get(
        `/datasource/${datasource.value.name}/table/${table.value.name}/variables`
      )
      .then((response) => {
        perms.value.variables = getPerms(response);
        variables.value = response.data;
        return response;
      });
  }

  async function loadTableVariable(name: string) {
    variable.value = {} as Variable;
    return api
      .get(
        `/datasource/${datasource.value.name}/table/${table.value.name}/variable/${name}`
      )
      .then((response) => {
        perms.value.variable = getPerms(response);
        variable.value = response.data;
        return response;
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

  return {
    datasource,
    tables,
    table,
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
    reset,
  };
});
