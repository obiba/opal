<template>
  <div>
    <q-toolbar>
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="dns" @click="onFolderSelection('/')" class="cursor-pointer" />
        <q-breadcrumbs-el
          v-for="crumb in crumbs"
          :key="crumb.to"
          :label="crumb.label"
          :class="crumb.to !== props.file.path ? 'cursor-pointer' : ''"
          @click="onFolderSelection(crumb.to)"
        />
        <q-btn
          v-if="isEditable(props.file)"
          rounded
          dense
          flat
          size="sm"
          color="secondary"
          :title="t('edit')"
          icon="edit"
          class="q-ml-md"
          @click="onShowEditName(props.file)"
        />
        <q-btn
          v-if="isEditable(props.file)"
          rounded
          dense
          flat
          color="negative"
          :title="t('delete')"
          icon="delete"
          size="sm"
          @click="onShowDeleteSingle(props.file)"
        />
      </q-breadcrumbs>
    </q-toolbar>
    <div v-if="props.file.type === 'FOLDER'">
      <q-table
        ref="tableRef"
        v-model:selected="selected"
        flat
        :rows="rows"
        :columns="columns"
        row-key="name"
        :pagination="initialPagination"
        :loading="loading"
        @row-dblclick="onRowDblClick"
        selection="multiple"
        :filter="filter"
      >
        <template v-slot:top-left>
          <div class="row q-gutter-sm">
            <q-btn
              color="primary"
              icon="add"
              :label="t('add_folder')"
              :disable="!props.file.writable"
              size="sm"
              @click="onShowAddFolder"
            >
            </q-btn>
            <q-btn
              color="secondary"
              icon="file_upload"
              :label="t('upload')"
              :disable="!props.file.writable"
              size="sm"
              @click="onShowUpload"
            >
            </q-btn>
            <q-btn
              color="secondary"
              icon="file_download"
              :label="t('download')"
              :disable="!isReadableSelected"
              size="sm"
              @click="onShowDownload"
            >
            </q-btn>
            <q-btn
              color="secondary"
              icon="unarchive"
              :label="t('extract')"
              :disable="!isArchiveSelected"
              size="sm"
              @click="onShowExtract"
            >
            </q-btn>
            <q-btn-group>
              <q-btn color="secondary" size="sm" icon="file_copy" :disable="readables.length === 0" @click="onCopy" />
              <q-btn color="secondary" size="sm" icon="content_cut" :disable="writables.length === 0" @click="onCut" />
              <q-btn color="secondary" size="sm" icon="content_paste" :disable="!canPaste" @click="onPaste" />
            </q-btn-group>
            <q-btn outline color="red" icon="delete" size="sm" @click="onShowDelete" :disable="writables.length === 0">
            </q-btn>
          </div>
        </template>
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
          <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
            <q-icon
              :name="getIconName(props.row)"
              :color="props.row.type === 'FOLDER' ? 'primary' : 'secondary'"
              size="sm"
              class="q-mr-sm"
            />
            <span>{{ props.row.name }}</span>
            <div v-if="isEditable(props.row)" class="float-right">
              <q-btn
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="t('edit')"
                :icon="toolsVisible[props.row.path] ? 'edit' : 'none'"
                class="q-ml-xs"
                @click="onShowEditName(props.row)"
              />
              <q-btn
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="t('delete')"
                :icon="toolsVisible[props.row.path] ? 'delete' : 'none'"
                class="q-ml-xs"
                @click="onShowDeleteSingle(props.row)"
              />
            </div>
          </q-td>
        </template>
        <template v-slot:body-cell-size="props">
          <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
            {{ props.value }}
          </q-td>
        </template>
        <template v-slot:body-cell-lastModifiedTime="props">
          <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
            {{ props.value }}
          </q-td>
        </template>
      </q-table>
    </div>
    <div v-if="props.file.type === FileDto_FileType.FILE">
      <q-card flat bordered class="q-mt-md">
        <q-card-section class="q-pt-sm q-pb-sm">
          <div class="text-subtitle1">{{ props.file.name }}</div>
        </q-card-section>
        <q-separator />
        <q-card-section class="text-center bg-grey-3">
          <q-btn
            color="secondary"
            icon="file_download"
            :label="t('download')"
            @click="onShowDownload"
            class="q-mb-md"
          />
          <div class="text-caption">{{ t('size') }}: {{ getSizeLabel(props.file.size) }}</div>
          <div class="text-caption">
            {{ getDateLabel(props.file.lastModifiedTime) }}
          </div>
        </q-card-section>
      </q-card>
    </div>

    <extract-archive-dialog v-if="selectedSingle" v-model="showExtract" :file="selectedSingle" />

    <add-folder-dialog v-model="showAddFolder" :file="props.file" />

    <edit-file-name-dialog v-if="selectedSingle" v-model="showEditName" :file="selectedSingle" />

    <confirm-dialog
      v-model="showDelete"
      :title="t('delete')"
      :text="t('delete_files_confirm', { count: props.file.type === FileDto_FileType.FILE ? 1 : writables.length })"
      @cancel="onConfirmCancelled"
      @confirm="onDelete"
    />

    <upload-file-dialog v-model="showUpload" :file="props.file" :extensions="[]" />

    <q-dialog v-model="showDownload">
      <q-card>
        <q-card-section>
          <div class="text-h6">{{ t('download') }}</div>
        </q-card-section>

        <q-separator />

        <q-card-section>
          <q-form ref="formRef">
            <div>
              <q-checkbox
                v-model="encryptContent"
                :label="t('encrypt_file_content')"
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
                    :label="t('encrypt_password')"
                    :hint="t('encrypt_password_hint')"
                    lazy-rules
                    :rules="[validatePassword]"
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
                    :label="t('generate')"
                    :disable="encryptContent === false"
                    @click="onGenerateDownloadPwd"
                  ></q-btn>
                </div>
              </div>
            </div>
          </q-form>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="bg-grey-3">
          <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
          <q-btn flat :label="t('download')" color="primary" @click="onDownload" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup lang="ts">
import AddFolderDialog from 'src/components/files/AddFolderDialog.vue';
import EditFileNameDialog from 'src/components/files/EditFileNameDialog.vue';
import UploadFileDialog from 'src/components/files/UploadFileDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import ExtractArchiveDialog from 'src/components/files/ExtractArchiveDialog.vue';
import { type FileDto, FileDto_FileType } from 'src/models/Opal';
import { getSizeLabel, getIconName } from 'src/utils/files';
import { getDateLabel } from 'src/utils/dates';
import { includesToken } from 'src/utils/strings';
import { DefaultAlignment } from 'src/components/models';

const { t } = useI18n();
const filesStore = useFilesStore();

interface Props {
  file: FileDto;
}

const MIN_PASSWORD_LENGTH = 8;
const props = defineProps<Props>();

const tableRef = ref();
const filter = ref('');
const loading = ref(false);
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 50,
});

const formRef = ref();
const selected = ref<FileDto[]>([]);
const selectedSingle = ref<FileDto>();
const showDownload = ref(false);
const encryptContent = ref(false);
const encryptPassword = ref('');
const showPwd = ref(false);
const showAddFolder = ref(false);
const showExtract = ref(false);
const showUpload = ref(false);
const showDelete = ref(false);
const showEditName = ref(false);
const toolsVisible = ref<{ [key: string]: boolean }>({});

// Validators
const validatePassword = (val: string) =>
  (encryptContent && val.trim().length >= MIN_PASSWORD_LENGTH) ||
  t('validation.password_min_length', { min: MIN_PASSWORD_LENGTH });

watch(
  () => props.file,
  () => {
    selected.value = [];
  }
);

const columns = computed(() => [
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
]);

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
  const result:FileDto[] = [];

  if (props.file.children === undefined) {
    return result;
  }
  props.file.children
    .filter((file) => file.type === FileDto_FileType.FOLDER && includesToken(file.name, filter.value))
    .sort((a, b) => a.name.localeCompare(b.name))
    .forEach((file) => {
      result.push(file);
    });

  props.file.children
    .filter((file) => file.type === FileDto_FileType.FILE && includesToken(file.name, filter.value))
    .sort((a, b) => a.name.localeCompare(b.name))
    .forEach((file) => {
      result.push(file);
    });

  return result;
});

const isReadableSelected = computed(() => {
  return readables.value.length > 0;
});

const isArchiveSelected = computed(() => {
  return (
    selected.value.length === 1 &&
    selected.value[0]?.type === FileDto_FileType.FILE &&
    selected.value[0].name.endsWith('.zip')
  );
});

const readables = computed(() => {
  return selected.value.filter((file) => file.name !== '..' && file.readable);
});

const writables = computed(() => {
  return selected.value.filter((file) => file.name !== '..' && isEditable(file));
});

function isEditable(file: FileDto) {
  return file.writable && ['/', '/home', '/projects', '/tmp'].includes(file.path) === false;
}

function onShowAddFolder() {
  showAddFolder.value = true;
}

function onShowEditName(file: FileDto) {
  selectedSingle.value = file;
  showEditName.value = true;
}

function onShowExtract() {
  selectedSingle.value = selected.value[0];
  showExtract.value = true;
}

function onShowUpload() {
  showUpload.value = true;
}

function onShowDownload() {
  encryptContent.value = false;
  encryptPassword.value = '';
  showPwd.value = false;
  showDownload.value = true;
}

async function onDownload() {
  const valid = await formRef.value.validate();
  if (valid) {
    filesStore.downloadFiles(
      props.file.path,
      readables.value,
      encryptContent.value ? encryptPassword.value : undefined
    );
    showDownload.value = false;
  }
}

function onEncryptContentUpdated() {
  encryptPassword.value = '';
  if (!encryptContent.value) formRef.value.resetValidation();
}

function onGenerateDownloadPwd() {
  const length = 12;
  const charset = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789;,:?/()';
  let retVal = '';
  for (let i = 0, n = charset.length; i < length; ++i) {
    retVal += charset.charAt(Math.floor(Math.random() * n));
  }
  encryptPassword.value = retVal;
}

function onShowDeleteSingle(file: FileDto) {
  selected.value = [file];
  onShowDelete();
}

function onShowDelete() {
  showDelete.value = true;
}

function onConfirmCancelled() {
  selected.value = [];
}

function onDelete() {
  if (
    props.file.type !== FileDto_FileType.FOLDER ||
    (selected.value.length === 1 && selected.value[0]?.path === props.file.path)
  ) {
    onDeleteFile();
  } else {
    onDeleteSelections();
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

function onRowDblClick(evt: unknown, row: FileDto) {
  selected.value = [];
  if (!row.readable) {
    return;
  }
  filesStore.loadFiles(row.path);
}

function onOverRow(row: FileDto) {
  toolsVisible.value[row.path] = true;
}

function onLeaveRow(row: FileDto) {
  toolsVisible.value[row.path] = false;
}
</script>
