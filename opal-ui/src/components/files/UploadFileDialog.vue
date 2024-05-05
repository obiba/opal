<template>
  <q-dialog v-model="showDialog" @hide="onHide">
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
            :accept="accept"
          >
          </q-file>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="bg-grey-3">
          <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
          <q-btn
            flat
            :label="$t('add')"
            color="primary"
            :disable="newFiles.length === 0"
            @click="onUpload"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
</template>


<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'UploadFileDialog',
});
</script>
<script setup lang="ts">
import { FileObject } from 'src/components/models';
import { FileDto } from 'src/models/Opal';

interface DialogProps {
  modelValue: boolean;
  file: FileDto;
  extensions: string[] | undefined;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue'])

const filesStore = useFilesStore();

const showDialog = ref(props.modelValue);
const newFiles = ref<FileObject[]>([]);

const accept = props.extensions ? props.extensions.join(',') : undefined;

watch(() => props.modelValue, (value) => {
  if (value) {
    newFiles.value = [];
  }
  showDialog.value = value;
});

function onHide() {
  emit('update:modelValue', false);
}

function onUpload() {
  filesStore
    .uploadFiles(props.file.path, newFiles.value as FileObject[])
    .then(() => filesStore.loadFiles(props.file.path));
}

</script>
