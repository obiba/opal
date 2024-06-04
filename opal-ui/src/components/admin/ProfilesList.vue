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
        row-key="name"
        :pagination="initialPagination"
        :hide-pagination="profiles.length <= initialPagination.rowsPerPage"
        :loading="loading"
      >
        <template v-slot:body-cell-name="props">
          <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
            <span class="text-primary">{{ props.value }}</span>
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
              />
            </div>
          </q-td>
        </template>
        <template v-slot:body-cell-realm="props">
          <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
              {{ props.value }}
          </q-td>
        </template>
      </q-table>

    </div>
  </template>

<script setup lang="ts">
import { onMounted } from 'vue';

const { t } = useI18n();
import { SubjectProfileDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';

const profilesStore = useProfilesStore();
const profiles = computed(() => profilesStore.profiles || []);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const showDelete = ref(false);

const columns = [
  {
    name: 'principal',
    required: true,
    label: t('principal'),
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

];

const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});

function onOverRow(row: SubjectProfileDto) {
  toolsVisible.value[row.principal] = true;
}

function onLeaveRow(row: SubjectProfileDto) {
  toolsVisible.value[row.principal] = false;
}

// async function onDeleteGroup(user: SubjectProfileDto) {
//   selectedGroup.value = user;
//   showDelete.value = true;
// }

// async function doDeleteGroup() {
//   showDelete.value = false;
//   if (selectedGroup.value == null) {
//     return;
//   }

//   const toDelete: SubjectProfileDto | null = selectedGroup.value;
//   selectedGroup.value = null;

//   try {
//     await profilesStore.deleteGroup(toDelete);
//     await usersStore.initUsers();
//   } catch (err) {
//     notifyError(err);
//   }
// }

const loading = ref(false);

onMounted(async () => {
  loading.value = true;
  profilesStore.initProfiles().then(() => {
    loading.value = false;
  });
});
</script>