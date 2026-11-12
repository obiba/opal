<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ title || t('copy_view') }}</div>
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
        <q-btn
          flat
          :label="t('copy')"
          color="primary"
          :disable="!projectDestination"
          @click="onCopyView"
        />
      </q-card-actions>
    </q-card>
  </q-dialog>

  <confirm-dialog
    v-model="showOverride"
    :title="t('override')"
    :text="t('copy_view_override_confirm', { name: newTableName, project: projectDestination })"
    @confirm="doOverride"
  />
</template>

<script setup lang="ts">
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import type { TableDto, ViewDto } from 'src/models/Magma';
import { u } from 'src/boot/api';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  table: TableDto;
  view: ViewDto;
  // default name of the new view, a unique name derived from the table name otherwise
  name?: string;
  title?: string;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();
const router = useRouter();
const datasourceStore = useDatasourceStore();

const projectNames = ref<string[]>([]);

const showDialog = ref(props.modelValue);
const projectDestination = ref('');
const newTableName = ref('');
const showOverride = ref(false);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      projectNames.value = [];
      projectDestination.value = '';
      datasourceStore
        .getViewDestinations()
        .then((names) => {
          const current = props.table.datasourceName as string;
          projectNames.value = names;
          projectDestination.value = names.includes(current) ? current : names[0] || '';
        })
        .catch(notifyError);
      newTableName.value = props.name || '';
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
  },
  // also when mounted already open, e.g. under a v-if
  { immediate: true },
);

function onHide() {
  emit('update:modelValue', false);
}

function newView(): ViewDto {
  return { ...props.view, name: newTableName.value };
}

function goToView() {
  showDialog.value = false;
  router.push(u`/project/${projectDestination.value}/table/${newTableName.value}`);
}

async function onCopyView() {
  const exists = await datasourceStore
    .getView(projectDestination.value, newTableName.value)
    .then(() => true)
    .catch(() => false);
  if (exists) {
    showOverride.value = true;
    return;
  }
  datasourceStore.createView(projectDestination.value, newView()).then(goToView).catch(notifyError);
}

function doOverride() {
  showOverride.value = false;
  const view = newView();
  delete view.datasourceName;
  delete view.status;
  const comment = `Override with view ${props.table.datasourceName}.${props.table.name}`;
  datasourceStore
    .updateView(projectDestination.value, newTableName.value, view, comment)
    .then(goToView)
    .catch(notifyError);
}
</script>
