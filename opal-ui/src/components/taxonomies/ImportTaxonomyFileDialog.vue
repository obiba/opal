<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('taxonomy.import.title') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section style="max-height: 75vh" class="scroll">
        <q-form class="q-gutter-md" persistent>
          <file-select
            v-model="destinationFolder"
            :label="t('destination_folder')"
            :folder="filesStore.current"
            selection="single"
            @select="onUpdate"
            type="file"
            :extensions="['.yml']"
          />

          <q-checkbox class="q-ml-sm" v-model="override" :label="t('taxonomy.import_gh.override')" />
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup @click="onHide" />
        <q-btn
          flat
          :label="t('import')"
          type="submit"
          color="primary"
          :disable="!canImport"
          @click="onImportTaxonomy"
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { notifyError } from 'src/utils/notify';
import FileSelect from 'src/components/files/FileSelect.vue';
import type { FileDto } from 'src/models/Opal';
import { FileDto_FileType } from 'src/models/Opal';

interface DialogProps {
  modelValue: boolean;
}

const { t } = useI18n();
const taxonomiesStore = useTaxonomiesStore();
const filesStore = useFilesStore();
const authStore = useAuthStore();
const override = ref(false);
const props = defineProps<DialogProps>();
const canImport = ref(false);
const emit = defineEmits(['update:modelValue', 'updated']);

const emptyFileDto = {
  name: '/',
  path: '/tmp',
  type: FileDto_FileType.FOLDER,
  readable: true,
  writable: true,
  children: [],
} as FileDto;

const showDialog = ref(props.modelValue);
const destinationFolder = ref({ ...emptyFileDto } as FileDto);
const username = computed(() => (authStore.profile.principal ? authStore.profile.principal : ''));

// Handlers

async function onUpdate() {
  canImport.value =
    destinationFolder.value.type === FileDto_FileType.FILE && destinationFolder.value.name.endsWith('.yml');
}

function onHide() {
  showDialog.value = false;
  destinationFolder.value = emptyFileDto;
  emit('update:modelValue', false);
}

async function onImportTaxonomy() {
  try {
    await taxonomiesStore.importFileTaxonomy(destinationFolder.value.path, override.value);
    onHide();
    emit('updated');
  } catch (error) {
    notifyError(error);
  }
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      showDialog.value = value;
      destinationFolder.value.path = `/home/${username.value}`;
    }
  }
);

onMounted(() => filesStore.initFiles('/home/administrator'));
</script>
