<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ editMode ? t('edit_view') : t('add_view') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-select
          v-show="!editMode"
          v-model="projectDestination"
          :options="projectNames"
          :label="t('project_destination')"
          dense
          class="q-mb-md"
        />
        <q-input
          v-model="name"
          dense
          type="text"
          :label="t('view_name')"
          :hint="t('resource_ref.view_destination_hint')"
          class="q-mb-md"
        />
        <q-input
          v-show="editMode"
          v-model="resourceFullName"
          dense
          type="text"
          :label="t('resource')"
          :hint="t('resource_ref.from_hint')"
          class="q-mb-md"
        />
        <q-input
          v-model="id"
          dense
          type="text"
          :label="t('resource_ref.id_column')"
          :hint="t('resource_ref.id_column_hint')"
          class="q-mb-md"
        />
        <q-input
          v-model="entityType"
          dense
          type="text"
          :label="t('entity_type')"
          :hint="t('resource_ref.entity_type_hint')"
          class="q-mb-md"
        />
        <q-checkbox dense v-model="allColumns" :label="t('resource_ref.all_columns')" />
        <div class="text-hint q-mt-sm q-mb-md">{{ t('resource_ref.all_columns_hint') }}</div>

        <q-input
          v-model="profile"
          dense
          type="text"
          :label="t('resource_ref.r_server_profile')"
          :hint="t('resource_ref.r_server_profile_hint')"
          class="q-mb-md"
        />
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-spinner-dots v-if="processing" class="on-left" />
        <q-btn flat :label="t('cancel')" color="secondary" :disable="processing" v-close-popup />
        <q-btn
          flat
          :label="t('save')"
          color="primary"
          @click="onSaveView"
          :disable="!projectDestination || !name || processing"
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { ResourceReferenceDto } from 'src/models/Projects';
import type { ViewDto, ResourceViewDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  resource?: ResourceReferenceDto;
  view?: ViewDto;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();
const router = useRouter();
const projectsStore = useProjectsStore();
const datasourceStore = useDatasourceStore();

const projectNames = computed(() => projectsStore.projects.map((p) => p.name));
const editMode = computed(() => props.view);
const processing = ref(false);

const showDialog = ref(props.modelValue);
const projectDestination = ref('');
const name = ref('');
const resourceFullName = ref('');
const id = ref('');
const entityType = ref('Participant');
const allColumns = ref(true);
const profile = ref('');

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      projectDestination.value = projectsStore.project.name || datasourceStore.datasource.name;
      id.value = '';
      entityType.value = 'Participant';
      allColumns.value = true;
      profile.value = '';
      if (props.resource) {
        name.value = props.resource.name;
        resourceFullName.value = `${props.resource.project}.${props.resource.name}`;
      }
      if (props.view) {
        name.value = props.view.name || '';
        resourceFullName.value = props.view.from[0] || '';
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const resView = (props.view as any)['Magma.ResourceViewDto.view'] as ResourceViewDto;
        id.value = resView.idColumn || '';
        entityType.value = resView.entityType || 'Participant';
        allColumns.value = resView.allColumns || true;
        profile.value = resView.profile || '';
      }
    }
    showDialog.value = value;
  }
);

onMounted(() => {
  projectsStore.initProjects();
});

function onHide() {
  processing.value = false;
  emit('update:modelValue', false);
}

function onSaveView() {
  if (!projectDestination.value || !name.value) {
    return;
  }

  const resView = {
    entityType: entityType.value || 'Participant',
    idColumn: id.value,
    profile: profile.value || 'default',
    allColumns: allColumns.value,
  } as ResourceViewDto;

  const newViewPage = `/project/${projectDestination.value}/table/${name.value}`;

  processing.value = true;

  if (editMode.value) {
    // update existing
    const currentView = { ...props.view } as ViewDto;
    currentView.name = name.value;
    currentView.from = [resourceFullName.value];
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (currentView as any)['Magma.ResourceViewDto.view'] = resView;
    datasourceStore
      .updateView(projectDestination.value, props.view?.name || name.value, currentView, 'Updated from resource view')
      .then(() => {
        router.push(newViewPage);
      })
      .catch((error) => {
        notifyError(error);
      })
      .finally(onHide);
  } else {
    // update existing or add
    datasourceStore
      .getView(projectDestination.value, name.value)
      .then((view: ViewDto) => {
        view.from = [resourceFullName.value];
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (view as any)['Magma.ResourceViewDto.view'] = resView;
        datasourceStore
          .updateView(projectDestination.value, name.value, view, 'Updated from resource')
          .then(() => {
            router.push(newViewPage);
          })
          .catch((error) => {
            notifyError(error);
          })
          .finally(onHide);
      })
      .catch(() => {
        datasourceStore
          .addResourceView(projectDestination.value, name.value, resourceFullName.value, resView)
          .then(() => router.push(newViewPage))
          .finally(onHide);
      });
  }
}
</script>
