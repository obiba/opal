<template>
  <div>
    <div v-if="loading" class="text-hint">...</div>
    <div v-else-if="usages === null" class="text-hint">{{ t('r_quota.usage_unavailable') }}</div>
    <div v-else>
      <!-- every metric gets a block, limited or not: a metric with no quota is where one is added from -->
      <div v-for="usage in usages" :key="usage.metric" class="q-mb-md">
        <template v-if="usage.quota">
          <q-linear-progress
            :value="getRatio(usage)"
            :color="usage.exceeded ? 'negative' : 'primary'"
            track-color="grey-4"
            size="16px"
            rounded
            class="q-mb-sm"
            style="max-width: 400px"
          />
          <div>
            <span :class="usage.exceeded ? 'text-negative' : ''">
              {{
                t('r_quota.usage_used', {
                  used: getMillisLabel(usage.usedMillis) || '0 min',
                  limit: getMillisLabel(usage.quota.limitMillis) || '0 min',
                  metric: getMetricLabel(usage),
                  window: t(`r_quota.period_${usage.quota.period.toLowerCase()}`).toLowerCase(),
                })
              }}
            </span>
            <q-btn
              v-if="canSetQuota"
              flat
              dense
              no-caps
              size="sm"
              color="primary"
              :label="isPersonal(usage) ? t('r_quota.edit_personal') : t('r_quota.set_personal')"
              class="q-ml-sm"
              @click="onSetQuota(personalQuotaOf(usage), usage.metric)"
            />
          </div>
          <div class="text-hint">
            {{ getSourceLabel(usage) }}
            <span v-if="isSpentByOpenSessions(usage)">
              {{ t('r_quota.usage_open_sessions', { count: usage.openSessionsCount }) }}
            </span>
          </div>
          <div v-if="usage.exceeded" class="text-negative q-mt-xs">
            {{ t('r_quota.usage_blocked', { context: props.context }) }}
            <span v-if="isSpentByOpenSessions(usage)">
              {{ t('r_quota.usage_close_sessions', { count: usage.openSessionsCount }) }}
            </span>
            <span v-else-if="usage.nextCreditDate">
              {{ t('r_quota.usage_credit', { distance: getDateDistanceLabel(usage.nextCreditDate) }) }}
            </span>
          </div>
        </template>
        <template v-else>
          <span class="text-hint">
            {{ t('r_quota.usage_unlimited', { context: props.context, metric: getMetricLabel(usage) }) }}
          </span>
          <q-btn
            v-if="canSetQuota"
            flat
            dense
            no-caps
            size="sm"
            color="primary"
            :label="t('r_quota.set_personal')"
            class="q-ml-sm"
            @click="onSetQuota(null, usage.metric)"
          />
        </template>
      </div>
    </div>

    <add-r-quota-dialog
      v-model="showEdit"
      :context="props.context"
      :quota="editedQuota"
      :default-principal="props.principal"
      :default-metric="editedMetric"
      @saved="init"
    />
  </div>
</template>

<script setup lang="ts">
import type { RQuotaUsageDto, RQuotaDto } from 'src/models/OpalR';
import AddRQuotaDialog from 'src/components/admin/r/AddRQuotaDialog.vue';
import { getMillisLabel, getDateDistanceLabel } from 'src/utils/dates';
import { notifyError } from 'src/utils/notify';

interface Props {
  principal: string;
  context: string;
  /**
   * Report on the authenticated user, through the endpoint they are allowed to read without any administration
   * permission. Reading someone else's usage is an administrator's business.
   */
  current?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  current: false,
});

const { t } = useI18n();
const authStore = useAuthStore();
const rQuotaStore = useRQuotaStore();

const loading = ref(false);
const usages = ref<RQuotaUsageDto[] | null>(null);
const showEdit = ref(false);
const editedQuota = ref<RQuotaDto | null>(null);
const editedMetric = ref<string>('EXECUTION_TIME');

/**
 * The shortcut is an administrator's tool on someone else's page: on their own profile a user is being informed, not
 * given a way to lift their own limit.
 */
const canSetQuota = computed(() => !props.current && authStore.isAdministrator);

onMounted(init);

watch(() => [props.principal, props.context], init);

async function init() {
  if (!props.principal) return;
  loading.value = true;
  try {
    usages.value = props.current
      ? await rQuotaStore.getCurrentUsage(props.context)
      : await rQuotaStore.getUsage(props.context, props.principal);
  } catch (err) {
    // an auditor may read a profile page without being allowed to read quotas
    usages.value = null;
    if (!props.current) notifyError(err);
  } finally {
    loading.value = false;
  }
}

function getMetricLabel(usage: RQuotaUsageDto) {
  return t(`r_quota.metric_${usage.metric.toLowerCase()}`).toLowerCase();
}

function isPersonal(usage: RQuotaUsageDto) {
  return usage.quota?.subjectType === 'USER';
}

/**
 * Only a session time allowance keeps being spent by a session that is merely open: an execution time allowance stops
 * moving the moment the user stops running commands, and there waiting for the window to roll is what helps.
 */
function isSpentByOpenSessions(usage: RQuotaUsageDto) {
  return usage.metric === 'SESSION_TIME' && !!usage.openSessionsCount;
}

/**
 * Editing only makes sense for the subject's own quota; a group or system quota reached from here would be edited for
 * everyone it applies to, which is what the DataSHIELD administration page is for.
 */
function personalQuotaOf(usage: RQuotaUsageDto): RQuotaDto | null {
  return isPersonal(usage) ? (usage.quota ?? null) : null;
}

function getRatio(usage: RQuotaUsageDto) {
  const limit = usage.quota?.limitMillis ?? 0;
  if (limit <= 0) return 1;
  return Math.min(1, usage.usedMillis / limit);
}

function getSourceLabel(usage: RQuotaUsageDto) {
  const quota = usage.quota;
  if (!quota) return '';
  if (quota.subjectType === 'USER') return t('r_quota.usage_from_user');
  if (quota.subjectType === 'GROUP') return t('r_quota.usage_from_group', { principal: quota.principal });
  return t('r_quota.usage_from_system');
}

function onSetQuota(quota: RQuotaDto | null, metric: string) {
  editedQuota.value = quota;
  editedMetric.value = metric;
  showEdit.value = true;
}
</script>
