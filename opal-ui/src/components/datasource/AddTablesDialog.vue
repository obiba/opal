<template>
  <q-dialog v-model="showDialog" @hide="onHide">
      <q-card>
        <q-card-section>
          <div class="text-h6">{{ $t('add_tables') }}</div>
        </q-card-section>

        <q-separator />

        <q-card-section>
          <file-select
            v-model="excelFile"
            :folder="filesStore.current"
            selection="single"
            :extensions="['.xlsx','.xls','.xml']"
            class="q-mb-md"/>
          <div class="text-help q-mb-md">
            {{  $t('select_dictionary_file') }}
          </div>
          <div class="text-help q-mb-md">
            <span class="on-left">{{ $t('select_dictionary_file_template') }}</span>
            <a :href="`${baseUrl}/templates/OpalVariableTemplate.xlsx`" class="text-primary">OpalVariableTemplate.xlsx</a>
          </div>

          <q-list class="q-mt-md">
            <q-expansion-item
              switch-toggle-side
              dense
              header-class="text-primary text-caption"
              :label="$t('advanced_options')"
            >
              <div class="q-mt-md">
                <q-checkbox v-model="merge" :label="$t('merge_variables')"/>
                <div class="text-help q-pl-sm q-pr-sm">{{ $t('merge_variables_hint') }}</div>
              </div>
              <div class="q-mt-md q-pl-sm q-pr-sm">
                <q-input
                  v-model="locale"
                  :label="$t('locale')"
                  dense
                  class="q-mb-md"/>
                <q-input
                  v-model="charSet"
                  :label="$t('locale')"
                  dense
                  class="q-mb-md"/>
                <q-input
                  v-model="entityType"
                  :label="$t('entity_type')"
                  dense
                  class="q-mb-md"/>
              </div>
            </q-expansion-item>
          </q-list>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="bg-grey-3">
          <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
          <q-btn
            flat
            :label="$t('add')"
            color="primary"
            @click="onAddTables"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
</template>


<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'AddTablesDialog',
});
</script>
<script setup lang="ts">
import { FileDto } from 'src/models/Opal';
import FileSelect from 'src/components/files/FileSelect.vue';
import { baseUrl } from 'src/boot/api';
// import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue'])

const filesStore = useFilesStore();
const authStore = useAuthStore();

const username = computed(() =>
  authStore.profile.principal ? authStore.profile.principal : ''
);

const showDialog = ref(props.modelValue);
const excelFile = ref<FileDto>();
const merge = ref(false);
const locale = ref('en');
const charSet = ref('ISO-8859-1');
const entityType = ref('Participant');

watch(() => props.modelValue, (value) => {
  if (value) {
    excelFile.value = undefined;
    merge.value = false;
    locale.value = 'en';
    charSet.value = 'ISO-8859-1';
    entityType.value = 'Participant';
  }
  showDialog.value = value;
});

onMounted(() => {
  filesStore.initFiles(`/home/${username.value}`);
});

function onHide() {
  emit('update:modelValue', false);
}

function onAddTables() {
  console.log('TODO: Add Tables from Excel File', excelFile.value);
}

</script>
