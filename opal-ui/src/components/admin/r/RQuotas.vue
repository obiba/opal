<template>
  <div>
    <q-table
      flat
      :rows="rQuotaStore.quotas"
      :columns="columns"
      :row-key="(row) => `${row.subjectType}:${row.principal}`"
      :pagination="initialPagination"
      :hide-pagination="rQuotaStore.quotas.length <= initialPagination.rowsPerPage"
      :loading="loading"
    >
      <template v-slot:top-left>
        <q-btn
          v-if="authStore.isAdministrator"
          no-caps
          color="primary"
          icon="add"
          size="sm"
          :label="t('add')"
          @click="onAddQuota"
        />
      </template>
      <template v-slot:body-cell-subject="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <span>{{ getSubjectLabel(props.row) }}</span>
          <div class="float-right" v-if="authStore.isAdministrator">
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="t('edit')"
              :icon="toolsVisible[getKey(props.row)] ? 'edit' : 'none'"
              class="q-ml-xs"
              @click="onEditQuota(props.row)"
            />
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="t('delete')"
              :icon="toolsVisible[getKey(props.row)] ? 'delete' : 'none'"
              class="q-ml-xs"
              @click="onDeleteQuota(props.row)"
            />
          </div>
        </q-td>
      </template>
      <template v-slot:body-cell-enabled="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-icon :name="props.value ? 'check' : 'close'" size="sm" />
        </q-td>
      </template>
      <template v-slot:body-cell-usage="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <span v-if="props.row.subjectType !== 'USER'" class="text-hint">-</span>
          <span v-else-if="usages[props.row.principal] === undefined" class="text-hint">...</span>
          <span v-else :class="usages[props.row.principal]?.exceeded ? 'text-negative' : ''">
            {{ getMillisLabel(usages[props.row.principal]?.usedExecutionTimeMillis || 0) || '0 min' }}
          </span>
        </q-td>
      </template>
    </q-table>

    <confirm-dialog
      v-if="selectedQuota"
      v-model="showDelete"
      :title="t('delete')"
      :text="t('r_quota.delete_confirm', { subject: getSubjectLabel(selectedQuota) })"
      @confirm="doDeleteQuota"
    />

    <add-r-quota-dialog v-model="showEdit" :context="props.context" :quota="selectedQuota" @saved="onSaved" />
  </div>
</template>

<script setup lang="ts">
import type { RQuotaDto, RQuotaUsageDto } from 'src/models/OpalR';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import AddRQuotaDialog from 'src/components/admin/r/AddRQuotaDialog.vue';
import { getMillisLabel } from 'src/utils/dates';
import { DefaultAlignment } from 'src/components/models';
import { notifyError } from 'src/utils/notify';

interface Props {
  context: string;
}

const props = defineProps<Props>();

const { t } = useI18n();
const authStore = useAuthStore();
const rQuotaStore = useRQuotaStore();

const loading = ref(false);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const selectedQuota = ref<RQuotaDto | null>(null);
const showDelete = ref(false);
const showEdit = ref(false);
/**
 * Consumption only means something for a user: a group or system quota is what applies to many of them, and has no
 * single figure of its own.
 */
const usages = ref<{ [principal: string]: RQuotaUsageDto }>({});

const initialPagination = ref({
  sortBy: 'subject',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});

const columns = computed(() => [
  {
    name: 'subject',
    required: true,
    label: t('r_quota.subject'),
    align: DefaultAlignment,
    field: 'principal',
    sortable: true,
    style: 'width: 30%',
  },
  {
    name: 'period',
    label: t('r_quota.period'),
    align: DefaultAlignment,
    field: 'period',
    format: (val: string) => t(`r_quota.period_${val.toLowerCase()}`),
    sortable: true,
  },
  {
    name: 'limit',
    label: t('r_quota.limit'),
    align: DefaultAlignment,
    field: 'executionTimeLimitMillis',
    format: (val: number) => getMillisLabel(val) || '0 min',
    sortable: true,
  },
  {
    name: 'usage',
    label: t('r_quota.used'),
    align: DefaultAlignment,
    field: 'principal',
  },
  {
    name: 'enabled',
    label: t('enabled'),
    align: DefaultAlignment,
    field: 'enabled',
  },
]);

onMounted(init);

watch(() => props.context, init);

async function init() {
  loading.value = true;
  try {
    await rQuotaStore.initQuotas(props.context);
    await loadUsages();
  } catch (err) {
    notifyError(err);
  } finally {
    loading.value = false;
  }
}

async function loadUsages() {
  usages.value = {};
  const users = rQuotaStore.quotas.filter((quota) => quota.subjectType === 'USER');
  await Promise.all(
    users.map((quota) =>
      rQuotaStore
        .getUsage(props.context, quota.principal)
        .then((usage) => (usages.value[quota.principal] = usage))
        .catch(() => undefined),
    ),
  );
}

function getKey(quota: RQuotaDto) {
  return `${quota.subjectType}:${quota.principal}`;
}

function getSubjectLabel(quota: RQuotaDto) {
  return quota.subjectType === 'SYSTEM' ? t('r_quota.system_default') : quota.principal;
}

function onOverRow(quota: RQuotaDto) {
  toolsVisible.value[getKey(quota)] = true;
}

function onLeaveRow(quota: RQuotaDto) {
  toolsVisible.value[getKey(quota)] = false;
}

function onAddQuota() {
  selectedQuota.value = null;
  showEdit.value = true;
}

function onEditQuota(quota: RQuotaDto) {
  selectedQuota.value = quota;
  showEdit.value = true;
}

function onDeleteQuota(quota: RQuotaDto) {
  selectedQuota.value = quota;
  showDelete.value = true;
}

async function onSaved() {
  selectedQuota.value = null;
  await init();
}

async function doDeleteQuota() {
  showDelete.value = false;
  const toDelete = selectedQuota.value;
  selectedQuota.value = null;
  if (toDelete === null) return;
  try {
    await rQuotaStore.deleteQuota(toDelete);
    await init();
  } catch (err) {
    notifyError(err);
  }
}
</script>
