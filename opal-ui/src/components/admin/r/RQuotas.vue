<template>
  <div>
    <q-table
      flat
      :rows="rQuotaStore.quotas"
      :columns="columns"
      :row-key="(row) => getKey(row)"
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
          <router-link v-if="props.row.subjectType === 'USER'" :to="`/admin/profile/${props.row.principal}`" class="text-primary">{{ getSubjectLabel(props.row) }}</router-link>
          <span v-else>{{ getSubjectLabel(props.row) }}</span>
          <q-badge v-if="props.row.subjectType === 'GROUP'" color="primary" class="q-ml-sm">
            {{ t('r_quota.group') }}
          </q-badge>
          <q-badge v-else-if="props.row.subjectType === 'USER'" color="accent" class="q-ml-sm">
            {{ t('r_quota.user') }}
          </q-badge>
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
          <span v-else-if="getUsageOf(props.row) === undefined" class="text-hint">...</span>
          <span v-else :class="getUsageOf(props.row)?.exceeded ? 'text-negative' : ''">
            {{ getMillisLabel(getUsageOf(props.row)?.usedMillis || 0) || '0 min' }}
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
 * single figure of its own. A user has one entry per metric, so the figure a row shows is the one of its own metric.
 */
const usages = ref<{ [principal: string]: RQuotaUsageDto[] }>({});

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
    name: 'metric',
    label: t('r_quota.metric'),
    align: DefaultAlignment,
    field: 'metric',
    format: (val: string) => t(`r_quota.metric_${val.toLowerCase()}`),
    sortable: true,
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
    field: 'limitMillis',
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
  // one call per user, whatever the number of metrics they have a quota for: the endpoint reports all of them
  const principals = [
    ...new Set(rQuotaStore.quotas.filter((quota) => quota.subjectType === 'USER').map((quota) => quota.principal)),
  ];
  await Promise.all(
    principals.map((principal) =>
      rQuotaStore
        .getUsage(props.context, principal)
        .then((usage) => (usages.value[principal] = usage))
        .catch(() => undefined),
    ),
  );
}

function getKey(quota: RQuotaDto) {
  return `${quota.subjectType}:${quota.principal}:${quota.metric}`;
}

/**
 * The usage entry of the row's own metric: a user over their session time says nothing about their execution time.
 */
function getUsageOf(quota: RQuotaDto) {
  return usages.value[quota.principal]?.find((usage) => usage.metric === quota.metric);
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
