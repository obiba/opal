<template>
  <div>
    <file-select
      v-model="destinationFolder"
      :label="$t('destination_folder')"
      :folder="filesStore.current"
      selection="single"
      @select="onUpdate"
      type="folder"/>
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'ExportFsForm',
});
</script>
<script setup lang="ts">
import { TableDto } from 'src/models/Magma';
import { FileDto, FileDto_FileType } from 'src/models/Opal';
import FileSelect from 'src/components/files/FileSelect.vue';

interface ExportFsFormProps {
  modelValue: string | undefined;
  tables: TableDto[];
}

const props = defineProps<ExportFsFormProps>();
const emit = defineEmits(['update:modelValue'])

const filesStore = useFilesStore();
const authStore = useAuthStore();

const destinationFolder = ref<FileDto>();

const currentTime = computed(() => new Date().toISOString().replace(/[:T\-]/g, '').split('.')[0]);

const username = computed(() =>
  authStore.profile.principal ? authStore.profile.principal : ''
);

onMounted(() => {
  if (props.modelValue) {
    destinationFolder.value = {
      name: 'export',
      path: `/home/${username.value}/export`,
      type: FileDto_FileType.FOLDER,
      readable: true,
      writable: true,
      children: [],
    }
  }
});

function onUpdate() {
  if (!destinationFolder.value) {
    emit('update:modelValue', undefined);
    return;
  }
  const prefix = props.tables.length === 1 ? `${props.tables[0].datasourceName}-${props.tables[0].name}` : props.tables[0].datasourceName;
  const out = `${destinationFolder.value.path}/${prefix}-${currentTime.value}.zip`;
  emit('update:modelValue', out);
}
</script>
