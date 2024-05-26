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
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'ExportHavenForm',
});
</script>
<script setup lang="ts">
import { TableDto } from 'src/models/Magma';
import { FileDto, FileDto_FileType } from 'src/models/Opal';
import FileSelect from 'src/components/files/FileSelect.vue';

interface ExportHavenFormProps {
  modelValue: string | undefined;
  tables: TableDto[];
  type: string;
}

const props = defineProps<ExportHavenFormProps>();
const emit = defineEmits(['update:modelValue'])

const filesStore = useFilesStore();
const authStore = useAuthStore();

const extensions: {
  [key: string]: string;
} = {
  haven_rds: '.rds',
  haven_sas: '.sas7bdat',
  haven_sast: '.xpt',
  haven_spss: '.sav',
  haven_stata: '.dta',
};

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
  const out = `${destinationFolder.value.path}/${prefix}-${currentTime.value}${extensions[props.type] || ''}`;
  emit('update:modelValue', out);
}
</script>
