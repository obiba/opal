<template>
  <div>
    <div class="text-h6 q-mb-sm">
      {{ profile.name }}
      <q-btn
        v-if="!builtinProfile"
        outline
        color="red"
        icon="delete"
        :title="t('delete')"
        size="sm"
        @click="onShowDeleteProfile"
        class="on-right"
      />
    </div>
    <q-separator class="q-mb-sm"/>
    <div v-if="missingCluster" class="box-warning q-mb-md">
      <q-icon name="warning" />
      {{ t('datashield.profile_missing_cluster') }}
    </div>
    <div class="row q-col-gutter-md q-mb-md">
      <div class="col-md-6 col-xs-12">
        <div class="text-bold q-mb-md" style="font-size: larger;">{{ t('status') }}</div>
        <div class="q-mb-md">
          <q-toggle
            v-model="enabled"
            :label="t('datashield.profile_status_toggle')"
            left-label
            @update:model-value="datashieldStore.updateProfileStatus(enabled)"
          />
        </div>
        <fields-list :items="items" :dbobject="profile" />
      </div>
      <div class="col-md-6 col-xs-12">
        <div class="text-bold q-mb-md" style="font-size: larger;">{{ t('permissions') }}</div>
        <q-toggle
          v-model="restricted"
          :label="t('datashield.profile_access_toggle')"
          left-label
          @update:model-value="datashieldStore.applyProfileAccess(!profile.restrictedAccess)"
        />
        <div v-if="profile?.restrictedAccess">
          <div class="text-help">
            {{ t('datashield.access_restricted') }}
          </div>
          <access-control-list
            :resource="`/datashield/profile/${profile.name}/permissions`"
            :options="['DATASHIELD_PROFILE_USE']"
          />
        </div>
        <div v-else>
          <div class="text-help">
            {{ t('datashield.access_not_restricted') }}
          </div>
        </div>
      </div>
    </div>
    <div class="text-bold q-mt-lg" style="font-size: larger;">{{ t('settings') }}</div>
    <div class="text-help q-mb-md">
      {{ t('datashield.profile_settings_help') }}
    </div>
    <div v-if="!missingCluster" class="q-mb-md">
      <q-btn
        color="primary"
        text-color="white"
        icon="restart_alt"
        :label="t('initialize')"
        :title="t('datashield.settings_init')"
        size="sm"
        @click="onShowInitSettings"
      />
    </div>
    <div v-if="loading">
      <q-spinner-dots size="lg" />
    </div>
    <div v-else>
      <q-tabs v-model="tab" dense class="text-grey" active-color="primary" indicator-color="primary" align="justify">
        <q-tab name="aggregate" :label="t('aggregate')" />
        <q-tab name="assign" :label="t('assign')" />
        <q-tab name="options" :label="t('options')" />
      </q-tabs>
      <q-separator />
      <q-tab-panels v-model="tab">
        <q-tab-panel v-for="env in ['aggregate', 'assign']" :key="env" :name="env">
          <datashield-methods :env="env" />
        </q-tab-panel>
        <q-tab-panel name="options">
          <datashield-options />
        </q-tab-panel>
      </q-tab-panels>
    </div>

    <datashield-profile-init-dialog v-model="showInitSettings" @before-init="onBeforeInit" @after-init="onAfterInit" />
    <confirm-dialog
      v-model="showDelete"
      :title="t('delete')"
      :text="t('datashield.profile_delete_confirm')"
      @confirm="onDeleteProfile"
    />
  </div>
</template>

<script setup lang="ts">
import type { DataShieldProfileDto } from 'src/models/DataShield';
import FieldsList, { type FieldItem } from 'src/components/FieldsList.vue';
import DatashieldMethods from 'src/components/admin/datashield/DatashieldMethods.vue';
import DatashieldOptions from 'src/components/admin/datashield/DatashieldOptions.vue';
import AccessControlList from 'src/components/permissions/AccessControlList.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import DatashieldProfileInitDialog from 'src/components/admin/datashield/DatashieldProfileInitDialog.vue';

interface Props {
  profile: DataShieldProfileDto;
}

const props = defineProps<Props>();

onMounted(() => {
  enabled.value = props.profile.enabled;
  restricted.value = props.profile.restrictedAccess;
  datashieldStore.initProfileSettings(props.profile);
});

const { t } = useI18n();
const rStore = useRStore();
const datashieldStore = useDatashieldStore();

const tab = ref<string>('aggregate');
const enabled = ref(props.profile.enabled);
const restricted = ref(props.profile.restrictedAccess);
const showDelete = ref(false);
const showInitSettings = ref(false);
const loading = ref(false);

const missingCluster = computed(
  () => props.profile === undefined || (rStore.clusters.length > 0 && getCluster(props.profile) === undefined)
);
const builtinProfile = computed(() => props.profile?.name === props.profile?.cluster);

const items: FieldItem[] = [
  {
    field: 'name',
  },
  {
    field: 'cluster',
    label: 'r.cluster',
    html: (val: DataShieldProfileDto) => (val ? `<code>${val.cluster}</code>` : ''),
  },
];

function getCluster(profile: DataShieldProfileDto) {
  return rStore.clusters.find((c) => c.name === profile.cluster);
}

function onShowDeleteProfile() {
  showDelete.value = true;
}

function onDeleteProfile() {
  datashieldStore.deleteProfile(props.profile.name);
}

function onShowInitSettings() {
  showInitSettings.value = true;
}

function onBeforeInit() {
  loading.value = true;
}

function onAfterInit() {
  loading.value = false;
}
</script>
