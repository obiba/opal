<template>
  <div>
    <q-table
      v-if="rows?.length > 0"
      :rows="rows"
      flat
      :row-key="getRowKey"
      :columns="columns"
      :pagination="initialPagination"
      :filter="filter"
      :filter-method="onFilter"
    >
      <template v-slot:top-left>
        <q-btn-dropdown color="primary" :title="t('add')" icon="add" size="sm">
          <q-list>
            <q-item clickable v-close-popup @click.prevent="onShowAddUser">
              <q-item-section>
                <q-item-label>{{ t('add_user_permission') }}</q-item-label>
              </q-item-section>
            </q-item>

            <q-item clickable v-close-popup @click.prevent="onShowAddGroup">
              <q-item-section>
                <q-item-label>{{ t('add_group_permission') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>
      </template>
      <template v-slot:top-right>
        <q-input dense debounce="500" v-model="filter">
          <template v-slot:append>
            <q-icon name="search" />
          </template>
        </q-input>
      </template>
      <template v-slot:body="props">
        <q-tr :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-td key="name" :props="props">
            <span class="text-primary">{{ props.row.subject.principal }}</span>
            <div class="float-right">
              <q-btn
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :icon="toolsVisible[getRowKey(props.row)] ? 'edit' : 'none'"
                class="q-ml-xs"
                @click="onShowEdit(props.row)"
              />
              <q-btn
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="t('delete')"
                :icon="toolsVisible[getRowKey(props.row)] ? 'delete' : 'none'"
                class="q-ml-xs"
                @click="onShowDelete(props.row)"
              />
            </div>
          </q-td>
          <q-td key="type" :props="props" class="text-caption">
            {{ t(props.row.subject.type.toLowerCase()) }}
          </q-td>
          <q-td key="permissions" :props="props" class="text-help">
            <div v-for="action in props.row.actions" :key="action">
              <span :title="t(`acls.${action}.description`)">{{ t(`acls.${action}.label`) }}</span>
            </div>
          </q-td>
        </q-tr>
      </template>
    </q-table>
    <div v-else class="q-mt-sm">
      <q-btn-dropdown color="primary" :label="t('add')" icon="add" size="sm">
        <q-list>
          <q-item clickable v-close-popup @click.prevent="onShowAddUser">
            <q-item-section>
              <q-item-label>{{ t('add_user_permission') }}</q-item-label>
            </q-item-section>
          </q-item>

          <q-item clickable v-close-popup @click.prevent="onShowAddGroup">
            <q-item-section>
              <q-item-label>{{ t('add_group_permission') }}</q-item-label>
            </q-item-section>
          </q-item>
        </q-list>
      </q-btn-dropdown>
      <div class="text-hint q-mt-md">
        {{ t('no_permissions') }}
      </div>
    </div>

    <confirm-dialog
      v-if="selected && selected.subject"
      v-model="showDelete"
      :title="t('delete')"
      :text="t('delete_permission_confirm', { principal: selected.subject.principal })"
      @confirm="deletePermission"
    />

    <q-dialog v-model="showEdit">
      <q-card class="dialog-sm">
        <q-card-section>
          <div class="text-h6">{{ t(editMode ? 'edit_permission' : 'add_permission') }}</div>
        </q-card-section>
        <q-separator />
        <q-card-section>
          <q-input
            v-model="selected.subject.principal"
            type="text"
            dense
            :label="t(selected.subject.type.toLowerCase())"
            :disable="editMode"
            class="q-mb-md"
            debounce="300"
            @update:model-value="onSearchSubject"
          >
            <q-menu v-model="showSuggestions" no-parent-event no-focus auto-close>
              <q-list style="min-width: 100px">
                <q-item
                  clickable
                  v-close-popup
                  v-for="sugg in suggestions"
                  :key="sugg"
                  @click="selected.subject.principal = sugg"
                >
                  <q-item-section>{{ sugg }}</q-item-section>
                </q-item>
              </q-list>
            </q-menu>
          </q-input>
          <div>
            {{ t('permission') }}
          </div>
          <div v-for="option in props.options" :key="option">
            <q-radio v-model="action" :label="t(`acls.${option}.label`)" :val="option" />
            <div class="text-hint q-ml-sm">{{ t(`acls.${option}.description`) }}</div>
          </div>
        </q-card-section>
        <q-separator />
        <q-card-actions align="right" class="bg-grey-3"
          ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
          <q-btn
            flat
            :label="t('submit')"
            color="primary"
            :disable="selected.subject.principal"
            @click="onSubmitPermission"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup lang="ts">
import type { Acl } from 'src/models/Opal';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { DefaultAlignment } from 'src/components/models';

interface Props {
  resource: string;
  options: string[];
}

const props = defineProps<Props>();

const authzStore = useAuthzStore();
const { t } = useI18n();

const filter = ref<string>('');
const toolsVisible = ref<{ [key: string]: boolean }>({});
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const showEdit = ref(false);
const showDelete = ref(false);
const selected = ref();
const editMode = ref(false);
const action = ref('');
const suggestions = ref<string[]>([]);

const showSuggestions = ref(false);

const rows = computed(() => authzStore.acls[props.resource] || []);

const columns = computed(() => [
  { name: 'name', label: t('name'), align: DefaultAlignment, field: 'subject', style: 'width: 30%' },
  { name: 'type', label: t('type'), align: DefaultAlignment, field: 'subject' },
  { name: 'permissions', label: t('permissions'), align: DefaultAlignment, field: 'actions' },
]);

onMounted(async () => {
  authzStore.initAcls(props.resource);
});

onUnmounted(() => {
  authzStore.resetAcls(props.resource);
});

watch(
  () => props.resource,
  async (resource) => {
    authzStore.initAcls(resource);
  }
);

function getRowKey(row: Acl) {
  return `${row.subject?.principal}:${row.subject?.type}`;
}

function onOverRow(row: Acl) {
  toolsVisible.value[getRowKey(row)] = true;
}

function onLeaveRow(row: Acl) {
  toolsVisible.value[getRowKey(row)] = false;
}

function onShowEdit(row: Acl) {
  selected.value = row;
  editMode.value = true;
  action.value = row.actions[0] || '';
  showEdit.value = true;
}

function onShowDelete(row: Acl) {
  selected.value = row;
  showDelete.value = true;
}

function onFilter() {
  if (!filter.value) {
    return authzStore.acls[props.resource] || [];
  }
  return authzStore.acls[props.resource]?.filter((row) => {
    return row.subject?.principal.toLowerCase().includes(filter.value.toLowerCase());
  }) || [];
}

function onShowAddUser() {
  selected.value = { subject: { principal: '', type: 'USER' }, actions: [], resource: props.resource, domain: 'opal' };
  editMode.value = false;
  action.value = props.options[0] || '';
  suggestions.value = [];
  showEdit.value = true;
}

function onShowAddGroup() {
  selected.value = { subject: { principal: '', type: 'GROUP' }, actions: [], resource: props.resource, domain: 'opal' };
  editMode.value = false;
  action.value = props.options[0] || '';
  suggestions.value = [];
  showEdit.value = true;
}

function onSubmitPermission() {
  selected.value.actions = [action.value];
  authzStore.setAcl(props.resource, selected.value);
}

function deletePermission() {
  authzStore.deleteAcl(props.resource, selected.value);
}

function onSearchSubject(value: string | number | null) {
  if (!value || typeof value !== 'string' || value.length < 3) {
    suggestions.value = [];
    showSuggestions.value = false;
    return;
  }
  authzStore.searchSubjects(selected.value.subject.type, value).then((response) => {
    if (response.suggestions && response.suggestions.length > 0) {
      if (response.suggestions.length === 1 && response.suggestions[0] === value) {
        suggestions.value = [];
        showSuggestions.value = false;
      } else {
        suggestions.value = response.suggestions;
        showSuggestions.value = true;
      }
    } else {
      suggestions.value = [];
      showSuggestions.value = false;
    }
  });
}
</script>
