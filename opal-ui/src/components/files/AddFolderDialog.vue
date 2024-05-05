<template>
  <q-dialog v-model="showDialog" @hide="onHide">
      <q-card>
        <q-card-section>
          <div class="text-h6">{{ $t('add_folder') }}</div>
        </q-card-section>

        <q-separator />

        <q-card-section>
          <q-input
            v-model="newFolder"
            dense
            type="text"
            :label="$t('name')"
            style="width: 300px"
            class="q-mb-md"
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
            :disable="newFolder === ''"
            @click="onAddFolder"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
</template>


<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'AddFolderDialog',
});
</script>
<script setup lang="ts">
import { FileDto } from 'src/models/Opal';

interface DialogProps {
  modelValue: boolean;
  file: FileDto;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue'])

const filesStore = useFilesStore();

const showDialog = ref(props.modelValue);
const newFolder = ref<string>('');

watch(() => props.modelValue, (value) => {
  if (value) {
    newFolder.value = '';
  }
  showDialog.value = value;
});

function onHide() {
  emit('update:modelValue', false);
}

function onAddFolder() {
  filesStore.addFolder(props.file.path, newFolder.value).then(() => {
    filesStore.loadFiles(props.file.path);
  });
}

</script>
