<template>
  <div>
    <div class="row">
      <span v-if="selectedPaths" class="text-caption on-left q-pt-xs">{{ selectedPaths }}</span>
      <q-btn outline no-caps icon="more_horiz" :label="$t('select')" color="primary" size="12px" @click="onShowDialog" />
    </div>
    <q-dialog v-model="showDialog" @hide="onHide">
      <q-card style="width: 700px; max-width: 80vw;">
        <q-card-section>
          <q-breadcrumbs>
              <q-breadcrumbs-el
                icon="dns"
                @click="onFolderSelection('/')"
                class="cursor-pointer"
              />
              <q-breadcrumbs-el
                v-for="crumb in crumbs"
                :key="crumb.to"
                :label="crumb.label"
                :class="crumb.to !== props.folder.path ? 'cursor-pointer' : ''"
                @click="onFolderSelection(crumb.to)"
              />
              <q-btn-dropdown flat rounded icon="add" size="sm">
                <q-list>
                  <q-item clickable v-close-popup @click="onShowAddFolder">
                    <q-item-section>
                      <q-item-label>{{  $t('add_folder') }}</q-item-label>
                    </q-item-section>
                  </q-item>
                  <q-item clickable v-close-popup @click="onShowUpload">
                    <q-item-section>
                      <q-item-label>{{  $t('upload') }}</q-item-label>
                    </q-item-section>
                  </q-item>
                </q-list>
              </q-btn-dropdown>
              <q-badge class="bg-warning text-black">{{ extensions ? extensions.join(', ') : '' }}</q-badge>
            </q-breadcrumbs>

        </q-card-section>

        <q-separator />

        <q-card-section>
          <table class="full-width">
            <tr>
              <td style="width: 150px;vertical-align: top;">
                <div class="text-grey-8">
                  <div>
                    <q-btn flat no-caps icon="person" color="primary" size="12px" :label="$t('user')" align="left" class="full-width" @click="onFolderSelection(`/home/${username}`)"></q-btn>
                  </div>
                  <div v-if="projectName">
                    <q-btn flat no-caps icon="table_chart" color="primary" size="12px" :label="$t('project')" align="left" class="full-width" @click="onFolderSelection(`/projects/${projectName}`)"></q-btn>
                  </div>
                  <div>
                    <q-item-label header class="text-weight-bolder">{{
                      $t('content')
                    }}</q-item-label>
                  </div>
                  <div>
                    <q-btn flat no-caps icon="group" color="primary" size="12px" :label="$t('users')" align="left" class="full-width" @click="onFolderSelection('/home')"></q-btn>
                  </div>
                  <div>
                    <q-btn flat no-caps icon="table_chart" color="primary" size="12px" :label="$t('projects')" align="left" class="full-width" @click="onFolderSelection('/projects')"></q-btn>
                  </div>
                  <div>
                    <q-btn flat no-caps icon="dns" color="primary" size="12px" :label="$t('file_system')" align="left" class="full-width" @click="onFolderSelection('/')"></q-btn>
                  </div>
                </div>
              </td>
              <td style="vertical-align: top;">
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
                    @row-click="onRowClick"
                    :selection="props.selection ? props.selection : 'single'"
                    v-model:selected="selected"
                    @update:selected="onFileSelection"
                  >
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
          </table>

        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="bg-grey-3">
          <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
          <q-btn
            flat
            :label="$t('select')"
            color="primary"
            :disable="selected.length === 0"
            @click="onSubmitSelection"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <add-folder-dialog v-model="showAddFolder" :file="$props.folder" />
    <upload-file-dialog v-model="showUpload" :file="$props.folder" :extensions="extensions" />
  </div>
</template>


<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'FileSelect',
});
</script>
<script setup lang="ts">
import AddFolderDialog from 'src/components/files/AddFolderDialog.vue';
import UploadFileDialog from 'src/components/files/UploadFileDialog.vue';
import { File } from 'src/components/models';
import { getSizeLabel, getIconName } from 'src/utils/files';
import { getDateLabel } from 'src/utils/dates';

interface DialogProps {
  folder: File;
  selection: 'single' | 'multiple';
  extensions: string[] | undefined;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'select'])

const { t } = useI18n();
const authStore = useAuthStore();
const filesStore = useFilesStore();
const projectsStore = useProjectsStore();

const tableRef = ref();
const selected = ref<File[]>([]);
const initialPagination = { descending: false, page: 1, rowsPerPage: 10 };
const loading = ref(false);
const showAddFolder = ref(false);
const showUpload = ref(false);

const username = computed(() =>
  authStore.profile.principal ? authStore.profile.principal : ''
);

const projectName = computed(() =>
  projectsStore.project?.name ? projectsStore.project.name : ''
);

const selectedPaths = computed(() => selected.value.map((file) => file.path).join(', '));

const showDialog = ref(false);

const columns = [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'name',
    format: (val: string) => val,
    sortable: true,
  },
  {
    name: 'size',
    required: true,
    label: t('size'),
    align: 'left',
    field: 'size',
    format: (val: number) => getSizeLabel(val),
    sortable: true,
  },
  {
    name: 'lastModifiedTime',
    required: true,
    label: t('last_update'),
    align: 'left',
    field: 'lastModifiedTime',
    format: (val: number) => getDateLabel(val),
    sortable: true,
  },
];

const rows = computed(() => {
  const result =
    props.folder.path === '/'
      ? []
      : [
          {
            name: '..',
            type: 'FOLDER',
            path: filesStore.getParentFolder(props.folder.path),
            readable: true,
          },
        ];
  if (props.folder.children === undefined) {
    return result;
  }
  props.folder.children
    .filter((file) => file.type === 'FOLDER')
    .sort((a, b) => a.name.localeCompare(b.name))
    .forEach((file) => {
      result.push(file);
    });

  props.folder.children
    .filter((file) => file.type === 'FILE')
    .filter((file) => props.extensions === undefined || props.extensions.some((ext) => file.name.endsWith(ext)))
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

function onHide() {
  emit('update:modelValue', false);
}

function onFolderSelection(path: string) {
  filesStore.loadFiles(path);
}

function onFileSelection() {
  selected.value = selected.value.filter((file) => file.type === 'FILE' && file.readable);
}

function onRowClick(evt: unknown, row: File) {
  selected.value = [];
  if (!row.readable) {
    return;
  }
  if (row.type === 'FOLDER') {
     filesStore.loadFiles(row.path);
  } else {
    selected.value = [row];
  }
}

function onSubmitSelection() {
  emit('select', unref(props.selection === 'multiple' ? selected.value : selected.value[0]));
}

function onShowDialog() {
  selected.value = [];
  showDialog.value = true;
}

function onShowAddFolder() {
  showAddFolder.value = true;
}

function onShowUpload() {
  showUpload.value = true;
}
</script>
