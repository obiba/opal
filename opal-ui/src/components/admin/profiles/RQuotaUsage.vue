<template>
  <div>
    <div v-if="loading" class="text-hint">...</div>
    <div v-else-if="usage === null" class="text-hint">{{ t('r_quota.usage_unavailable') }}</div>
    <div v-else-if="!usage.quota">
      <span class="text-hint">{{ t('r_quota.usage_unlimited', { context: props.context }) }}</span>
      <q-btn
        v-if="canSetQuota"
        flat
        dense
        no-caps
        size="sm"
        color="primary"
        :label="t('r_quota.set_personal')"
        class="q-ml-sm"
        @click="onSetQuota"
      />
    </div>
    <div v-else>
      <q-linear-progress
        :value="ratio"
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
              used: getMillisLabel(usage.usedExecutionTimeMillis) || '0 min',
              limit: getMillisLabel(usage.quota.executionTimeLimitMillis) || '0 min',
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
          :label="isPersonal ? t('r_quota.edit_personal') : t('r_quota.set_personal')"
          class="q-ml-sm"
          @click="onSetQuota"
        />
      </div>
      <div class="text-hint">{{ sourceLabel }}</div>
      <div v-if="usage.exceeded" class="text-negative q-mt-xs">
        {{ t('r_quota.usage_blocked', { context: props.context }) }}
        <span v-if="usage.nextCreditDate">
          {{ t('r_quota.usage_credit', { distance: getDateDistanceLabel(usage.nextCreditDate) }) }}
        </span>
      </div>
    </div>

    <add-r-quota-dialog
      v-model="showEdit"
      :context="props.context"
      :quota="personalQuota"
      :default-principal="props.principal"
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
const usage = ref<RQuotaUsageDto | null>(null);
const showEdit = ref(false);

const isPersonal = computed(() => usage.value?.quota?.subjectType === 'USER');

/**
 * The shortcut is an administrator's tool on someone else's page: on their own profile a user is being informed, not
 * given a way to lift their own limit.
 */
const canSetQuota = computed(() => !props.current && authStore.isAdministrator);

/**
 * Editing only makes sense for the subject's own quota; a group or system quota reached from here would be edited for
 * everyone it applies to, which is what the DataSHIELD administration page is for.
 */
const personalQuota = computed<RQuotaDto | null>(() => (isPersonal.value ? (usage.value?.quota ?? null) : null));

const ratio = computed(() => {
  const quota = usage.value?.quota;
  if (!quota) return 0;
  if (quota.executionTimeLimitMillis <= 0) return 1;
  return Math.min(1, (usage.value?.usedExecutionTimeMillis || 0) / quota.executionTimeLimitMillis);
});

const sourceLabel = computed(() => {
  const quota = usage.value?.quota;
  if (!quota) return '';
  if (quota.subjectType === 'USER') return t('r_quota.usage_from_user');
  if (quota.subjectType === 'GROUP') return t('r_quota.usage_from_group', { principal: quota.principal });
  return t('r_quota.usage_from_system');
});

onMounted(init);

watch(() => [props.principal, props.context], init);

async function init() {
  if (!props.principal) return;
  loading.value = true;
  try {
    usage.value = props.current
      ? await rQuotaStore.getCurrentUsage(props.context)
      : await rQuotaStore.getUsage(props.context, props.principal);
  } catch (err) {
    // an auditor may read a profile page without being allowed to read quotas
    usage.value = null;
    if (!props.current) notifyError(err);
  } finally {
    loading.value = false;
  }
}

function onSetQuota() {
  showEdit.value = true;
}
</script>
