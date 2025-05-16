<template>
  <div>
    <file-select
      v-model="destinationFolder"
      :label="t('destination_folder')"
      :folder="filesStore.current"
      selection="single"
      @select="onUpdate"
      type="folder"
    />
  </div>
</template>

<script setup lang="ts">
import type { TableDto } from 'src/models/Magma';
import { type FileDto, FileDto_FileType } from 'src/models/Opal';
import FileSelect from 'src/components/files/FileSelect.vue';
import { makeOutputPath } from 'src/components/datasource/export/exportUtils';

interface ExportCsvFormProps {
  modelValue: string | undefined;
  folder?: string | undefined;
  tables: TableDto[];
}

const props = defineProps<ExportCsvFormProps>();
const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();
const filesStore = useFilesStore();
const authStore = useAuthStore();

const destinationFolder = ref<FileDto>();

const username = computed(() => (authStore.profile.principal ? authStore.profile.principal : ''));

onMounted(onInit);

function onInit() {
  destinationFolder.value = {
    name: 'export',
    path: props.folder || `/home/${username.value}/export`,
    type: FileDto_FileType.FOLDER,
    readable: true,
    writable: true,
    children: [],
  };
  onUpdate();
}

function onUpdate() {
  emit(
    'update:modelValue',
    makeOutputPath(destinationFolder.value, props.tables[0]?.datasourceName, props.tables[0]?.name, undefined),
  );
}
</script>
