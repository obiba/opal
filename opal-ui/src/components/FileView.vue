<template>
  <div>
    <q-toolbar>
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
          :class="crumb.to !== props.file.path ? 'cursor-pointer' : ''"
          @click="onFolderSelection(crumb.to)"
        />
      </q-breadcrumbs>
    </q-toolbar>
    <div v-if="props.file.type === 'FOLDER'">
      <q-table
        ref="tableRef"
        flat
        :rows="rows"
        :columns="columns"
        row-key="name"
        :pagination="initialPagination"
        :loading="loading"
        @row-click="onRowClick"
        selection="multiple"
        v-model:selected="selected"
      >
        <template v-slot:top>
          <q-btn
            color="primary"
            icon="add"
            :label="$t('add_folder')"
            :disable="!props.file.writable"
            size="sm"
            @click="onShowAddFolder"
          >
          </q-btn>
          <q-btn
            color="secondary"
            icon="file_upload"
            :label="$t('upload')"
            :disable="!props.file.writable"
            size="sm"
            class="on-right"
            @click="onShowUpload"
          >
          </q-btn>
          <q-btn
            color="secondary"
            icon="file_download"
            :label="$t('download')"
            :disable="!isreadableselected"
            size="sm"
            class="on-right"
            @click="onShowDownload"
          >
          </q-btn>
          <q-btn
            color="secondary"
            icon="unarchive"
            :label="$t('extract')"
            :disable="!isArchiveSelected"
            size="sm"
            class="on-right"
          >
          </q-btn>
          <q-btn-group class="on-right">
            <q-btn
              color="secondary"
              size="sm"
              icon="content_copy"
              :disable="readables.length === 0"
              @click="onCopy"
            />
            <q-btn
              color="secondary"
              size="sm"
              icon="content_cut"
              :disable="writables.length === 0"
              @click="onCut"
            />
            <q-btn
              color="secondary"
              size="sm"
              icon="content_paste"
              :disable="!canPaste"
              @click="onPaste"
            />
          </q-btn-group>
          <q-btn
            rounded
            color="red"
            icon="delete_outline"
            size="sm"
            class="on-right"
            @click="onShowDelete"
            :disable="writables.length === 0"
          >
          </q-btn>
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
    <div v-if="props.file.type === 'FILE'">
      <div class="q-mb-md">
        <q-btn
          rounded
          color="red"
          icon="delete_outline"
          size="sm"
          @click="onShowDelete"
          :disable="props.file.writable === false"
        >
        </q-btn>
      </div>
      <q-card flat bordered>
        <q-card-section class="q-pt-sm q-pb-sm">
          <div class="text-subtitle1">{{ props.file.name }}</div>
        </q-card-section>
        <q-separator />
        <q-card-section class="text-center bg-grey-3">
          <q-btn
            color="secondary"
            icon="file_download"
            :label="$t('download')"
            @click="onShowDownload"
            class="q-mb-md"
          />
          <div class="text-caption">
            {{ $t('size') }}: {{ getSizeLabel(props.file.size) }}
          </div>
          <div class="text-caption">
            {{ getDateLabel(props.file.lastModifiedTime) }}
          </div>
        </q-card-section>
      </q-card>
    </div>

    <q-dialog v-model="showAddFolder">
      <q-card>
        <q-card-section>
          <div class="text-h6">{{ $t('add_folder') }}</div>
        </q-card-section>

        <q-separator />

        <q-card-section>
          <q-input
            v-model="newFolderName"
            dense
            type="text"
            :label="$t('name')"
            style="width: 300px"
          >
          </q-input>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="bg-grey-3">
          <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
          <q-btn
            flat
            :label="$t('add')"
            color="primary"
            :disable="newFolderName === ''"
            @click="onAddFolder"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <q-dialog v-model="showDelete">
      <q-card>
        <q-card-section>
          <div class="text-h6">{{ $t('delete') }}</div>
        </q-card-section>

        <q-separator />

        <q-card-section>
          <div v-if="props.file.type === 'FILE' || writables.length === 1">
            {{ $t('delete_file_confirm') }}
          </div>
          <div v-else>{{ $t('delete_files_confirm') }}</div>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="bg-grey-3">
          <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
          <q-btn
            flat
            :label="$t('confirm')"
            color="primary"
            @click="onDelete"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <q-dialog v-model="showUpload">
      <q-card>
        <q-card-section>
          <div class="text-h6">{{ $t('upload') }}</div>
        </q-card-section>

        <q-separator />

        <q-card-section>
          <q-file
            v-model="newFiles"
            dense
            multiple
            append
            :label="$t('select_files_to_upload')"
            style="width: 300px"
          >
          </q-file>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="bg-grey-3">
          <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
          <q-btn
            flat
            :label="$t('upload')"
            color="primary"
            :disable="newFiles.length === 0"
            @click="onUpload"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <q-dialog v-model="showDownload">
      <q-card>
        <q-card-section>
          <div class="text-h6">{{ $t('download') }}</div>
        </q-card-section>

        <q-separator />

        <q-card-section>
          <div>
            <q-checkbox
              v-model="encryptContent"
              :label="$t('encrypt_file_content')"
              @update:model-value="onEncryptContentUpdated"
            />
          </div>
          <div class="q-ml-sm q-mr-sm q-mb-md q-mt-md">
            <div class="row q-gutter-md">
              <div class="col-8">
                <q-input
                  v-model="encryptPassword"
                  dense
                  :disable="encryptContent === false"
                  :type="showPwd ? 'text' : 'password'"
                  :label="$t('encrypt_password')"
                  :hint="$t('encrypt_password_hint')"
                >
                  <template v-slot:append>
                    <q-icon
                      :name="showPwd ? 'visibility_off' : 'visibility'"
                      class="cursor-pointer"
                      @click="showPwd = !showPwd"
                    />
                  </template>
                </q-input>
              </div>
              <div class="col-2">
                <q-btn
                  flat
                  :label="$t('generate')"
                  :disable="encryptContent === false"
                  @click="onGenerateDownloadPwd"
                ></q-btn>
              </div>
            </div>
          </div>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="bg-grey-3">
          <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
          <q-btn
            flat
            :label="$t('download')"
            color="primary"
            @click="onDownload"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'FileView',
});
</script>
<script setup lang="ts">
import { File, FileObject } from 'src/components/models';

const { t } = useI18n();
const filesStore = useFilesStore();

interface FolderViewProps {
  file: File;
}

const props = withDefaults(defineProps<FolderViewProps>(), {
  file: { name: 'root', path: '/', type: 'FOLDER', files: [] },
});

const tableRef = ref();
const loading = ref(false);
const initialPagination = ref({
  sortBy: 'desc',
  descending: false,
  page: 1,
  rowsPerPage: 50,
});

const selected = ref<File[]>([]);
const showDownload = ref(false);
const encryptContent = ref(false);
const encryptPassword = ref('');
const showPwd = ref(false);
const showAddFolder = ref(false);
const newFolderName = ref('');
const showUpload = ref(false);
const newFiles = ref<FileObject[]>([]);
const showDelete = ref(false);

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

const crumbs = computed(() => {
  if (props.file.path === undefined) {
    return [];
  }
  const parts = props.file.path.split('/');
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

const rows = computed(() => {
  const result =
    props.file.path === '/'
      ? []
      : [
          {
            name: '..',
            type: 'FOLDER',
            path: filesStore.getParentFolder(props.file.path),
            readable: true,
          },
        ];
  if (props.file.children === undefined) {
    return result;
  }
  props.file.children
    .filter((file) => file.type === 'FOLDER')
    .sort((a, b) => a.name.localeCompare(b.name))
    .forEach((file) => {
      result.push(file);
    });

  props.file.children
    .filter((file) => file.type === 'FILE')
    .sort((a, b) => a.name.localeCompare(b.name))
    .forEach((file) => {
      result.push(file);
    });

  return result;
});

function getSizeLabel(size: number | undefined) {
  if (size === undefined || isNaN(size)) {
    return '-';
  }
  if (size < 1024) {
    return `${size} B`;
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(2)} KB`;
  }
  return `${(size / 1024 / 1024).toFixed(2)} MB`;
}

function getIconName(file: File) {
  if (file.type === 'FOLDER') {
    return file.readable ? 'folder' : 'folder_off';
  }
  return file.readable ? 'description' : 'insert_drive_file';
}

function getDateLabel(date: number | undefined) {
  if (date === undefined || isNaN(date)) {
    return '-';
  }
  return new Date(date).toLocaleString();
}

const isreadableselected = computed(() => {
  return readables.value.length > 0;
});

const isArchiveSelected = computed(() => {
  return (
    selected.value.length === 1 &&
    selected.value[0].type === 'FILE' &&
    selected.value[0].name.endsWith('.zip')
  );
});

const readables = computed(() => {
  return selected.value.filter((file) => file.name !== '..' && file.readable);
});

const writables = computed(() => {
  return selected.value.filter((file) => file.name !== '..' && file.writable);
});

function onShowAddFolder() {
  showAddFolder.value = true;
}

function onAddFolder() {
  filesStore.addFolder(props.file.path, newFolderName.value).then(() => {
    filesStore.loadFiles(props.file.path);
  });
}

function onShowUpload() {
  newFiles.value = [];
  showUpload.value = true;
}

function onUpload() {
  filesStore
    .uploadFiles(props.file.path, newFiles.value as FileObject[])
    .then(() => filesStore.loadFiles(props.file.path));
}

function onShowDownload() {
  encryptContent.value = false;
  encryptPassword.value = '';
  showPwd.value = false;
  showDownload.value = true;
}

function onDownload() {
  filesStore.downloadFiles(
    props.file.path,
    readables.value,
    encryptContent.value ? encryptPassword.value : undefined
  );
}

function onEncryptContentUpdated() {
  encryptPassword.value = '';
}

function onGenerateDownloadPwd() {
  const length = 12;
  const charset =
    'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789;,:?/()';
  let retVal = '';
  for (var i = 0, n = charset.length; i < length; ++i) {
    retVal += charset.charAt(Math.floor(Math.random() * n));
  }
  encryptPassword.value = retVal;
}

function onShowDelete() {
  showDelete.value = true;
}

function onDelete() {
  if (props.file.type === 'FOLDER') {
    onDeleteSelections();
  } else {
    onDeleteFile();
  }
}

function onDeleteSelections() {
  filesStore.deleteFiles(writables.value).then(() => {
    selected.value = [];
    filesStore.loadFiles(props.file.path);
  });
}

function onDeleteFile() {
  const parentPath = filesStore.getParentFolder(props.file.path);
  filesStore.deleteFile(props.file.path).then(() => {
    selected.value = [];
    filesStore.loadFiles(parentPath);
  });
}

function onCopy() {
  filesStore.setCopySelection(readables.value);
}

function onCut() {
  filesStore.setCutSelection(writables.value);
}

const canPaste = computed(() => {
  return props.file.writable && filesStore.canPasteSelection(props.file.path);
});

function onPaste() {
  filesStore.pasteFiles(props.file.path).then(() => {
    selected.value = [];
    filesStore.loadFiles(props.file.path);
  });
}

function onFolderSelection(path: string) {
  selected.value = [];
  filesStore.loadFiles(path);
}

function onRowClick(evt: unknown, row: File) {
  selected.value = [];
  if (!row.readable) {
    return;
  }
  filesStore.loadFiles(row.path);
  // if (row.type === 'FOLDER') {
  //   filesStore.loadFiles(row.path);
  // } else {
  //   selected.value = [row];
  //   onShowDownload();
  // }
}
</script>
