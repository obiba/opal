<template>
  <div>
    <div class="text-h5 q-mb-md">
      {{ t('user_profiles') }}
    </div>
    <div class="text-help q-mb-md">{{ t('profiles_info') }}</div>
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
        <div class="row items-center q-gutter-sm">
          <span :class="{ 'text-secondary': selectedProfiles.length === 0 }">{{ t('delete_profiles_selected') }}</span>
          <q-btn
            outline
            color="red"
            icon="delete"
            size="sm"
            :disable="selectedProfiles.length === 0"
            @click="onDeleteProfiles"
          ></q-btn>
        </div>
      </template>
      <template v-slot:body-cell-principal="props">
        <q-td :props="props">
          <router-link :to="`/admin/profile/${props.value}`" class="text-primary">{{
            props.value
          }}</router-link>
        </q-td>
      </template>
      <template v-slot:body-cell-realm="props">
        <q-td :props="props">
          {{ props.value }}
        </q-td>
      </template>
      <template v-slot:body-cell-groups="props">
        <q-td :props="props">
          <q-chip class="q-ml-none" v-for="group in props.col.format(props.row.groups)" :key="group.name">
            {{ group }}
          </q-chip>
        </q-td>
      </template>
      <template v-slot:body-cell-otpEnabled="props">
        <q-td :props="props">
          <q-checkbox v-model="props.row.otpEnabled" :disable="!props.value" @click="disableOtp(props.row)" />
          <q-tooltip>{{
            t(props.value ? 'profile_otp_disable' : 'profile_otp_disabled', { user: props.row.principal })
          }}</q-tooltip>
        </q-td>
      </template>
    </q-table>

    <confirm-dialog
      v-model="showDeletes"
      :title="t('delete')"
      :text="t('delete_profiles_confirm', { count: selectedProfiles.length, profile: principalsToDelete })"
      @confirm="doDeleteProfiles"
    />
  </div>
</template>

<script setup lang="ts">
import type { SubjectProfileDto } from 'src/models/Opal';
import { getDateLabel } from 'src/utils/dates';
import { notifyError } from 'src/utils/notify';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { DefaultAlignment } from 'src/components/models';

const { t } = useI18n();

const profilesStore = useProfilesStore();
const profiles = computed(() => profilesStore.profiles || []);
const showDeletes = ref(false);
const selectedProfiles = ref<SubjectProfileDto[]>([]);

const columns = computed(() => [
  {
    name: 'principal',
    required: true,
    label: t('name'),
    align: DefaultAlignment,
    field: 'principal',
    format: (val: string) => val,
    sortable: true,
    style: 'width: 25%',
  },
  {
    name: 'realm',
    label: t('realm'),
    align: DefaultAlignment,
    field: 'realm',
    format: (val: string) => val,
  },
  {
    name: 'groups',
    label: t('groups'),
    align: DefaultAlignment,
    field: 'groups',
    format: (val: string) => val,
  },
  {
    name: 'otpEnabled',
    label: t('2fa.name'),
    align: DefaultAlignment,
    field: 'otpEnabled',
    format: (val: string) => val,
  },
  {
    name: 'created',
    required: true,
    label: t('created'),
    align: DefaultAlignment,
    field: 'created',
    format: (val: string) => getDateLabel(val),
  },
  {
    name: 'lastUpdate',
    required: true,
    label: t('last_update'),
    align: DefaultAlignment,
    field: 'lastUpdate',
    format: (val: string) => getDateLabel(val),
  },
]);

const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});

const principalsToDelete = computed(() => selectedProfiles.value.map((p) => p.principal).join(', '));

async function disableOtp(profile: SubjectProfileDto) {
  try {
    await profilesStore.disableOtp(profile);
    await profilesStore.initProfiles();
  } catch (err) {
    notifyError(err);
  }
}

function onDeleteProfiles() {
  showDeletes.value = true;
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
