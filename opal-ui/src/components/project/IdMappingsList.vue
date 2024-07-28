<template>
  <slot name="title"></slot>
  <q-table
    flat
    :filter="filter"
    :filter-method="onFilter"
    :rows="users"
    :columns="columns"
    row-key="name"
    :pagination="initialPagination"
    :hide-pagination="users.length <= initialPagination.rowsPerPage"
    :loading="loading"
  >
    <template v-slot:top-left>
      <q-btn-dropdown color="primary" :label="$t('add')" icon="add" size="sm">
        <q-list>
          <q-item clickable v-close-popup @click.prevent="onAddWithPassword">
            <q-item-section>
              <q-item-label>{{ $t('user_add_with_pwd') }}</q-item-label>
            </q-item-section>
          </q-item>

          <q-item clickable v-close-popup @click.prevent="onAddWithCertificate">
            <q-item-section>
              <q-item-label>{{ $t('user_add_with_crt') }}</q-item-label>
            </q-item-section>
          </q-item>
        </q-list>
      </q-btn-dropdown>
    </template>
    <template v-slot:top-right>
      <q-input dense clearable debounce="400" color="primary" v-model="filter">
        <template v-slot:append>
          <q-icon name="search" />
        </template>
      </q-input>
    </template>
    <template v-slot:body-cell-name="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <span class="text-primary">{{ props.value }}</span>
        <div class="float-right">
          <q-btn
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="$t('edit')"
            :icon="toolsVisible[props.row.name] ? 'edit' : 'none'"
            class="q-ml-xs"
            @click="onEditUser(props.row)"
          />
          <q-btn
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="$t('delete')"
            :icon="toolsVisible[props.row.name] ? 'delete' : 'none'"
            class="q-ml-xs"
            @click="onDeleteUser(props.row)"
          />
          <q-btn
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="props.row.enabled ? $t('disable') : $t('enable')"
            :icon="toolsVisible[props.row.name] ? (props.row.enabled ? 'block' : 'check_circle') : 'none'"
            class="q-ml-xs"
            @click="onEnableUser(props.row)"
          />
        </div>
      </q-td>
    </template>
    <template v-slot:body-cell-groups="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <q-chip class="q-ml-none" v-for="group in props.col.format(props.row.groups)" :key="group.name">
          {{ group }}
        </q-chip>
      </q-td>
    </template>
    <template v-slot:body-cell-authentication="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <span class="text-caption">{{ props.value }}</span>
      </q-td>
    </template>
    <template v-slot:body-cell-enabled="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <q-icon :name="props.value ? 'check' : 'close'" size="sm" />
      </q-td>
    </template>
  </q-table>
</template>

<script lang="ts">
export default defineComponent({
  name: 'IdMappingsList',
});
</script>

<script setup lang="ts">
import { t } from 'src/boot/i18n';

const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'name',
    format: (val: string) => val,
    sortable: true,
    style: 'width: 25%',
  },
  {
    name: 'mappings',
    label: t('id_mappings'),
    align: 'left',
    field: 'groups',
    format: (val: string[]) => (val || []).filter((val) => !!val && val.length > 0),
  },
  {
    name: 'authentication',
    label: t('authentication'),
    align: 'left',
    field: 'authenticationType',
    format: (val: string) => t(`auth_types.${val}`),
  },
]);
</script>
