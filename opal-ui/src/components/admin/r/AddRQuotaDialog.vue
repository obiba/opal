<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ editMode ? t('r_quota.edit') : t('r_quota.add') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="formRef" class="q-gutter-md">
          <q-select
            v-model="subjectType"
            dense
            emit-value
            map-options
            :options="subjectTypeOptions"
            :label="t('r_quota.subject_type') + ' *'"
            :hint="t('r_quota.subject_type_hint')"
            :disable="props.defaultPrincipal !== undefined"
            class="q-mb-md"
          />
          <q-input
            v-if="subjectType !== 'SYSTEM'"
            v-model="principal"
            type="text"
            dense
            :label="t('r_quota.subject') + ' *'"
            :hint="subjectType === 'GROUP' ? t('r_quota.group_hint') : t('r_quota.user_hint')"
            class="q-mb-md"
            debounce="300"
            @update:model-value="onSearchSubject"
            lazy-rules
            :rules="[validateRequired('r_quota.subject_required')]"
            :disable="props.defaultPrincipal !== undefined"
          >
            <q-menu v-model="showSuggestions" no-parent-event no-focus auto-close>
              <q-list style="min-width: 100px">
                <q-item clickable v-close-popup v-for="sugg in suggestions" :key="sugg" @click="principal = sugg">
                  <q-item-section>{{ sugg }}</q-item-section>
                </q-item>
              </q-list>
            </q-menu>
          </q-input>
          <q-select
            v-model="period"
            dense
            emit-value
            map-options
            :options="periodOptions"
            :label="t('r_quota.period') + ' *'"
            :hint="t('r_quota.period_hint')"
            class="q-mb-md"
          />
          <q-input
            v-model.number="limitMinutes"
            dense
            type="number"
            min="0"
            :label="t('r_quota.limit_minutes') + ' *'"
            :hint="t('r_quota.limit_minutes_hint')"
            class="q-mb-md"
            lazy-rules
            :rules="[validatePositiveNumber('r_quota.limit_required')]"
          />
          <q-toggle v-model="enabled" :label="t('enabled')" />
          <div class="text-hint">{{ t('r_quota.enabled_hint') }}</div>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="editMode ? t('update') : t('add')" color="primary" @click="onSave" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { RQuotaDto } from 'src/models/OpalR';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  context: string;
  quota: RQuotaDto | null;
  /**
   * The subject a new quota is for, when the dialog is opened from that subject's page instead of from the list.
   */
  defaultPrincipal?: string;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'saved']);

const { t } = useI18n();
const rQuotaStore = useRQuotaStore();
const authzStore = useAuthzStore();

const formRef = ref();
const showDialog = ref(props.modelValue);
const subjectType = ref<string>('SYSTEM');
const principal = ref<string | null>(null);
const period = ref<string>('WEEKLY');
const limitMinutes = ref<number>(60);
const enabled = ref<boolean>(true);
const suggestions = ref<string[]>([]);
const showSuggestions = ref(false);

const editMode = computed(() => props.quota !== null && props.quota !== undefined);

const subjectTypeOptions = computed(() => [
  { label: t('r_quota.system_default'), value: 'SYSTEM' },
  { label: t('group'), value: 'GROUP' },
  { label: t('user'), value: 'USER' },
]);

const periodOptions = computed(() => [
  { label: t('r_quota.period_daily'), value: 'DAILY' },
  { label: t('r_quota.period_weekly'), value: 'WEEKLY' },
]);

const validateRequired = (id: string) => (val: string) => (val && val.trim().length > 0) || t(`validation.${id}`);
const validatePositiveNumber = (id: string) => (val: number) =>
  (val !== null && val !== undefined && !isNaN(val) && val >= 0) || t(`validation.${id}`);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      reset();
      showDialog.value = value;
    }
  },
);

watch(subjectType, () => {
  principal.value = null;
  clearSuggestions();
});

function reset() {
  if (props.quota) {
    subjectType.value = props.quota.subjectType;
    principal.value = props.quota.subjectType === 'SYSTEM' ? null : props.quota.principal;
    period.value = props.quota.period;
    limitMinutes.value = Math.round(props.quota.executionTimeLimitMillis / 60000);
    enabled.value = props.quota.enabled;
  } else {
    subjectType.value = props.defaultPrincipal ? 'USER' : 'SYSTEM';
    principal.value = props.defaultPrincipal ?? null;
    period.value = 'WEEKLY';
    limitMinutes.value = 60;
    enabled.value = true;
  }
  clearSuggestions();
}

function clearSuggestions() {
  suggestions.value = [];
  showSuggestions.value = false;
}

/**
 * The suggestions come from the subject profiles, which is the only place where users and groups of an external realm
 * are known. That knowledge is incomplete by nature -- a subject who has never logged in has no profile yet -- so the
 * field stays free text and the suggestions are only a convenience.
 */
function onSearchSubject(value: string | number | null) {
  if (!value || typeof value !== 'string' || value.trim().length < 3) {
    clearSuggestions();
    return;
  }
  const needle = value.trim();
  authzStore
    .searchSubjects(subjectType.value, needle)
    .then((response) => {
      const found = response.suggestions || [];
      if (found.length === 0 || (found.length === 1 && found[0] === needle)) {
        clearSuggestions();
      } else {
        suggestions.value = found;
        showSuggestions.value = true;
      }
    })
    .catch(clearSuggestions);
}

async function onSave() {
  const valid = await formRef.value.validate();
  if (!valid) return;
  const quota: RQuotaDto = {
    context: props.context,
    subjectType: subjectType.value,
    principal: subjectType.value === 'SYSTEM' ? '' : (principal.value ?? ''),
    period: period.value,
    executionTimeLimitMillis: Math.max(0, Math.round(limitMinutes.value)) * 60000,
    enabled: enabled.value,
  };
  try {
    if (editMode.value && props.quota?.id !== undefined) {
      quota.id = props.quota.id;
      await rQuotaStore.updateQuota(quota);
    } else {
      await rQuotaStore.addQuota(quota);
    }
    showDialog.value = false;
    emit('saved');
  } catch (err) {
    notifyError(err);
  }
}

function onHide() {
  emit('update:modelValue', false);
}
</script>
