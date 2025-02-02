<template>
  <div>
    <div v-if="label" class="text-grey-6 q-mb-sm">
      {{ label }}
    </div>
    <div class="row q-gutter-sm">
      <span v-if="selectedPaths" class="text-caption q-pt-xs">{{ selectedPaths }}</span>
      <q-btn outline no-caps icon="more_horiz" :label="t('select')" color="primary" size="12px" @click="onShowDialog" />
    </div>
    <slot name="error"></slot>
    <div v-if="hint" class="text-hint q-mb-sm q-mt-xs">
      <q-markdown :src="hint" no-heading-anchor-links />
    </div>
    <q-dialog v-model="showDialog">
      <q-card class="dialog-lg">
        <q-card-section>
          <q-breadcrumbs>
            <q-breadcrumbs-el icon="dns" @click="onFolderSelection('/')" class="cursor-pointer" />
            <q-breadcrumbs-el
              v-for="crumb in crumbs"
              :key="crumb.to"
              :label="crumb.label"
              :class="crumb.to !== props.folder.path ? 'cursor-pointer' : ''"
              @click="onFolderSelection(crumb.to)"
            />
          </q-breadcrumbs>
        </q-card-section>

        <q-separator />

        <q-card-section>
          <table class="full-width">
            <tbody>
              <tr>
                <td style="width: 150px; vertical-align: top">
                  <div class="text-grey-8">
                    <div>
                      <q-btn
                        flat
                        no-caps
                        icon="person"
                        color="primary"
                        size="12px"
                        :label="t('user')"
                        align="left"
                        class="full-width"
                        @click="onFolderSelection(`/home/${username}`)"
                      ></q-btn>
                    </div>
                    <div v-if="projectName">
                      <q-btn
                        flat
                        no-caps
                        icon="table_chart"
                        color="primary"
                        size="12px"
                        :label="t('project')"
                        align="left"
                        class="full-width"
                        @click="onFolderSelection(`/projects/${projectName}`)"
                      ></q-btn>
                    </div>
                    <q-separator class="q-mt-md q-mb-md" />
                    <div>
                      <q-btn
                        flat
                        no-caps
                        icon="group"
                        color="primary"
                        size="12px"
                        :label="t('users')"
                        align="left"
                        class="full-width"
                        @click="onFolderSelection('/home')"
                      ></q-btn>
                    </div>
                    <div>
                      <q-btn
                        flat
                        no-caps
                        icon="table_chart"
                        color="primary"
                        size="12px"
                        :label="t('projects')"
                        align="left"
                        class="full-width"
                        @click="onFolderSelection('/projects')"
                      ></q-btn>
                    </div>
                    <div>
                      <q-btn
                        flat
                        no-caps
                        icon="dns"
                        color="primary"
                        size="12px"
                        :label="t('file_system')"
                        align="left"
                        class="full-width"
                        @click="onFolderSelection('/')"
                      ></q-btn>
                    </div>
                  </div>
                </td>
                <td style="vertical-align: top">
                  <div>
                    <q-table
                      ref="tableRef"
                      flat
                      dense
                      :rows="rows"
                      :columns="columns"
                      row-key="name"
                      :pagination="initialPagination"
                      :loading="loading"
                      @row-dblclick="onRowDblClick"
                      @row-click="onRowClick"
                      :selection="props.selection ? props.selection : 'single'"
                      v-model:selected="selected"
                      @update:selected="onFileSelection"
                      :filter="filter"
                    >
                      <template v-slot:top-right>
                        <q-input
                          dense
                          clearable
                          debounce="400"
                          color="primary"
                          v-model="filter"
                          :placeholder="t('file_folder_search')"
                        >
                          <template v-slot:append>
                            <q-icon name="search" />
                          </template>
                        </q-input>
                      </template>
                      <template v-slot:body-cell-name="props">
                        <q-td :props="props">
                          <q-icon
                            :name="getIconName(props.row)"
                            :color="props.row.type === 'FOLDER' ? 'primary' : 'secondary'"
                            size="sm"
                            class="q-mr-sm"
                          />
                          <span>{{ props.value }}</span>
                        </q-td>
                      </template>
                    </q-table>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </q-card-section>

        <q-separator />

        <q-card-actions class="bg-grey-3">
          <div>
            <q-btn-dropdown outline icon="add" :label="t('add')" size="sm">
              <q-list>
                <q-item clickable v-close-popup @click="onShowAddFolder">
                  <q-item-section>
                    <q-item-label>{{ t('add_folder') }}</q-item-label>
                  </q-item-section>
                </q-item>
                <q-item v-if="type !== 'folder' || hasExtensions" clickable v-close-popup @click="onShowUpload">
                  <q-item-section>
                    <q-item-label>{{ t('upload') }}</q-item-label>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-btn-dropdown>
            <span v-if="hasExtensions" class="q-ml-xs">
              <q-badge v-for="ext in extensions" :key="ext" class="bg-warning text-black q-ml-xs">{{ ext }}</q-badge>
            </span>
          </div>
          <q-space />
          <div>
            <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
            <q-btn
              flat
              :label="t('select')"
              color="primary"
              :disable="selected.length === 0"
              @click="onSubmitSelection"
              v-close-popup
            />
          </div>
        </q-card-actions>
      </q-card>
    </q-dialog>

    <add-folder-dialog v-model="showAddFolder" :file="$props.folder" />
    <upload-file-dialog v-model="showUpload" :file="$props.folder" :extensions="extensions" />
  </div>
</template>

<script setup lang="ts">
import AddFolderDialog from 'src/components/files/AddFolderDialog.vue';
import UploadFileDialog from 'src/components/files/UploadFileDialog.vue';
import { type FileDto, FileDto_FileType } from 'src/models/Opal';
import { getSizeLabel, getIconName } from 'src/utils/files';
import { getDateLabel } from 'src/utils/dates';
import { includesToken } from 'src/utils/strings';
import { DefaultAlignment } from 'src/components/models';

interface DialogProps {
  modelValue: FileDto | FileDto[] | undefined;
  label?: string | undefined;
  hint?: string | undefined;
  folder: FileDto;
  selection: 'single' | 'multiple';
  extensions?: string[] | undefined;
  type?: 'file' | 'folder';
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'select']);

const { t } = useI18n();
const authStore = useAuthStore();
const filesStore = useFilesStore();
const projectsStore = useProjectsStore();
const filter = ref('');
const tableRef = ref();
const selected = ref<FileDto[]>(
  props.modelValue ? (Array.isArray(props.modelValue) ? props.modelValue : [props.modelValue]) : [],
);
const initialPagination = { descending: false, page: 1, rowsPerPage: 10 };
const loading = ref(false);
const showAddFolder = ref(false);
const showUpload = ref(false);

const hasExtensions = computed(() => props.extensions && props.extensions.length > 0);

const username = computed(() => (authStore.profile.principal ? authStore.profile.principal : ''));

const projectName = computed(() => (projectsStore.project?.name ? projectsStore.project.name : ''));

const selectedPaths = computed(() => {
  return selected.value.map((file) => file.path).join(', ');
});

const showDialog = ref(false);

const columns = computed(() => {
  return [
    {
      name: 'name',
      required: true,
      label: t('name'),
      align: DefaultAlignment,
      field: 'name',
      format: (val: string) => val,
      sortable: true,
    },
    {
      name: 'size',
      required: true,
      label: t('size'),
      align: DefaultAlignment,
      field: 'size',
      format: (val: number) => getSizeLabel(val),
      sortable: true,
    },
    {
      name: 'lastModifiedTime',
      required: true,
      label: t('last_update'),
      align: DefaultAlignment,
      field: 'lastModifiedTime',
      format: (val: number) => getDateLabel(val),
      sortable: true,
    },
  ];
});

watch(
  () => props.modelValue,
  (value) => {
    selected.value = value ? (Array.isArray(value) ? value : [value]) : [];
  },
);

const rows = computed(() => {
  const result: FileDto[] = [];

  if (props.folder.children === undefined) {
    return result;
  }
  props.folder.children
    .filter((file) => file.type === FileDto_FileType.FOLDER && includesToken(file.name, filter.value))
    .sort((a, b) => a.name.localeCompare(b.name))
    .forEach((file) => {
      result.push(file);
    });

  props.folder.children
    .filter(
      (file) =>
        (props.type !== 'folder' || hasExtensions.value) &&
        file.type === FileDto_FileType.FILE &&
        includesToken(file.name, filter.value),
    )
    .filter(
      (file) =>
        props.extensions === undefined ||
        props.extensions.length === 0 ||
        props.extensions.some((ext) => file.name.endsWith(ext)),
    )
    .sort((a, b) => a.name.localeCompare(b.name))
    .forEach((file) => {
      result.push(file);
    });

  return result;
});

const crumbs = computed(() => {
  if (props.folder.path === undefined) {
    return [];
  }
  const parts = props.folder.path.split('/');
  const result = [];
  let path = '';
  for (const part of parts) {
    if (part === '') {
      continue;
    }
    path += `/${part}`;
    result.push({ label: part, to: path });
  }
  return result;
});

function onFolderSelection(path: string) {
  filesStore.loadFiles(path);
}

function onFileSelection() {
  selected.value = selected.value.filter((file) => file.type === FileDto_FileType.FILE && file.readable);
}

function onRowDblClick(evt: unknown, row: FileDto) {
  selected.value = [];
  if (!row.readable) {
    return;
  }
  if (row.type === FileDto_FileType.FOLDER) {
    filesStore.loadFiles(row.path);
    if (props.type === 'folder' && row.name !== '..') {
      selected.value = [row];
    }
  } else {
    selected.value = [row];
  }
}

function onRowClick(evt: unknown, row: FileDto) {
  selected.value = [];
  if (props.type === 'folder' && row.type === FileDto_FileType.FOLDER) {
    if (row.name !== '..') {
      selected.value = [row];
    }
  } else if (row.type === FileDto_FileType.FILE || hasExtensions.value) {
    selected.value = [row];
  }
}

function onSubmitSelection() {
  const selection = unref(props.selection === 'multiple' ? selected.value : selected.value[0]);
  emit('update:modelValue', selection);
  emit('select', selection);
}

function onShowDialog() {
  showDialog.value = true;
}

function onShowAddFolder() {
  showAddFolder.value = true;
}

function onShowUpload() {
  showUpload.value = true;
}
</script>
