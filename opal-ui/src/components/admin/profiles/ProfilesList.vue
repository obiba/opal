<template>
  <div>
    <div class="text-h5 q-mb-md">
      {{ $t('profile') }}
    </div>
    <div class="text-help q-mb-md">{{ $t('profiles_info') }}</div>
    <q-table
      flat
      :rows="profiles"
      :columns="columns"
      row-key="principal"
      :pagination="initialPagination"
      :hide-pagination="profiles.length <= initialPagination.rowsPerPage"
      :loading="loading"
      selection="multiple"
      v-model:selected="selectedProfiles"
    >
      <template v-slot:top>
        <div v-if="selectedProfiles.length > 0" class="row items-center q-gutter-sm">
          <span>{{ $t("delete_profiles_selected") }}</span>
          <q-btn outline color="red" icon="delete" size="sm" @click="onDeleteProfiles"></q-btn>
        </div>
      </template>
      <template v-slot:body-cell-principal="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <span class="text-primary">{{ props.value }} </span>
          <div class="float-right">
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="$t('delete')"
              :icon="toolsVisible[props.row.principal] ? 'delete' : 'none'"
              class="q-ml-xs"
              @click="onDeleteProfile(props.row)"
            />
          </div>
        </q-td>
      </template>
      <template v-slot:body-cell-realm="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          {{ props.value }}
        </q-td>
      </template>
      <template v-slot:body-cell-groups="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-chip class="q-ml-none" v-for="group in props.col.format(props.row.groups)" :key="group.name">
            {{ group }}
          </q-chip>
        </q-td>
      </template>
      <template v-slot:body-cell-otpEnabled="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          {{ props.value }}
        </q-td>
      </template>
    </q-table>

    <confirm-dialog
      v-model="showDelete"
      :title="$t('delete')"
      :text="$t('delete_profile_confirm', { profile: (selectedProfile || {}).principal })"
      @confirm="doDeleteProfile"
    />
    <confirm-dialog
      v-model="showDeletes"
      :title="$t('delete')"
      :text="$t('delete_profiles_confirm', { count: selectedProfiles.length, profile: principalsToDelete})"
      @confirm="doDeleteProfiles"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { SubjectProfileDto } from 'src/models/Opal';
import { getDateLabel } from 'src/utils/dates';
import { notifyError } from 'src/utils/notify';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';

const { t } = useI18n();

const profilesStore = useProfilesStore();
const profiles = computed(() => profilesStore.profiles || []);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const showDelete = ref(false);
const showDeletes = ref(false);
const selectedProfile = ref<SubjectProfileDto | null>(null);
const selectedProfiles = ref<SubjectProfileDto[]>([]);

const columns = [
  {
    name: 'principal',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'principal',
    format: (val: string) => val,
    sortable: true,
    style: 'width: 25%',
  },
  {
    name: 'realm',
    label: t('realm'),
    align: 'left',
    field: 'realm',
    format: (val: string) => val,
  },
  {
    name: 'groups',
    label: t('groups'),
    align: 'left',
    field: 'groups',
    format: (val: string) => val,
  },
  {
    name: 'otpEnabled',
    label: t('2fa'),
    align: 'left',
    field: 'otpEnabled',
    format: (val: string) => val,
  },
  {
    name: 'created',
    required: true,
    label: t('created'),
    align: 'left',
    field: 'created',
    format: (val: string) => getDateLabel(val),
  },
  {
    name: 'lastUpdate',
    required: true,
    label: t('last_update'),
    align: 'left',
    field: 'lastUpdate',
    format: (val: string) => getDateLabel(val),
  },
];

const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});

const principalsToDelete = computed(() => selectedProfiles.value.map((p) => p.principal).join(', '));

function onOverRow(row: SubjectProfileDto) {
  toolsVisible.value[row.principal] = selectedProfiles.value.length === 0 && true;
}

function onLeaveRow(row: SubjectProfileDto) {
  toolsVisible.value[row.principal] = false;
}

async function onDeleteProfile(profile: SubjectProfileDto) {
  selectedProfile.value = profile;
  showDelete.value = true;
}

async function onDeleteProfiles() {
  showDeletes.value = true;
}

async function doDeleteProfile() {
  showDelete.value = false;
  if (selectedProfile.value == null) {
    return;
  }

  const toDelete: SubjectProfileDto | null = selectedProfile.value;
  selectedProfile.value = null;

  try {
    await profilesStore.deleteProfile(toDelete);
    await profilesStore.initProfiles();
  } catch (err) {
    notifyError(err);
  }
}

async function doDeleteProfiles() {
  showDeletes.value = false;
  if (selectedProfiles.value.length === 0) {
    return;
  }

  const toDelete: SubjectProfileDto[] = selectedProfiles.value;
  selectedProfiles.value = [];

  try {
    await profilesStore.deleteProfiles(toDelete);
    await profilesStore.initProfiles();
  } catch (err) {
    notifyError(err);
  }
}

const loading = ref(false);

onMounted(async () => {
  loading.value = true;
  profilesStore.initProfiles().then(() => {
    loading.value = false;
  });
});
</script>
