<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('copy_view') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-select
          v-model="projectDestination"
          :options="projectNames"
          :label="t('project_destination')"
          dense
          style="min-width: 300px"
          class="q-mb-md"
        />
        <q-input
          v-model="newTableName"
          dense
          type="text"
          :label="t('new_name')"
          style="min-width: 300px"
          class="q-mb-md"
        >
        </q-input>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('copy')" color="primary" @click="onCopyView" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { TableDto, ViewDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  table: TableDto;
  view: ViewDto;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();
const router = useRouter();
const projectsStore = useProjectsStore();
const datasourceStore = useDatasourceStore();

const projectNames = computed(() => projectsStore.projects.map((p) => p.name));

const showDialog = ref(props.modelValue);
const projectDestination = ref('');
const newTableName = ref('');

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      projectDestination.value = props.table.datasourceName as string;
      newTableName.value = '';
      const tableNames = datasourceStore.tables.map((t) => t.name);
      let idx = 1;
      while (newTableName.value === '') {
        const name = `${props.table.name}_${idx}`;
        if (!tableNames.includes(name)) {
          newTableName.value = name;
        }
        idx += 1;
      }
    }
    showDialog.value = value;
  }
);

onMounted(() => {
  projectsStore.initProjects();
});

function onHide() {
  emit('update:modelValue', false);
}

function onCopyView() {
  const newView = { ...props.view };
  newView.name = newTableName.value;
  datasourceStore
    .createView(projectDestination.value, newView)
    .then(() => router.push(`/project/${projectDestination.value}/table/${newTableName.value}`))
    .catch((err) => {
      notifyError(err);
    });
}
</script>
