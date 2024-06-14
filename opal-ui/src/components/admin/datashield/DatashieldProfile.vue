<template>
  <div>
    <div class="text-h5 q-mb-md">
      {{ profile.name }}
      <q-icon
        name="circle"
        size="sm"
        :color="profile.enabled ? 'green' : 'red'"
      />
      <q-btn
        v-if="!builtinProfile"
        outline
        color="red"
        icon="delete"
        size="sm"
        class="on-right"
        @click="onDeleteProfile" />
    </div>
    <div v-if="missingCluster" class="bg-warning q-pa-md q-mb-md">
      <q-icon name="warning" />
      {{ $t('datashield_profile_missing_cluster') }}
    </div>
    <div class="row q-col-gutter-md q-mb-md">
      <div class="col-md-6 col-xs-12">
        <div class="text-h6 q-mb-md">{{ $t('status') }}</div>
        <div class="q-mb-md">
          <q-toggle v-model="enabled"
            :label="$t('datashield_profile_status_toggle')"
            left-label
            @update:model-value="datashieldStore.updateProfileStatus(enabled)"
          />
        </div>
        <fields-list
          :items="items"
          :dbobject="profile"
        />
      </div>
      <div class="col-md-6 col-xs-12">
        <div class="text-h6 q-mb-md">{{ $t('permissions') }}</div>
        <div v-if="profile?.restrictedAccess">
          <div class="text-help">
            {{ $t('datashield_access_restricted') }}
          </div>
        </div>
        <div v-else>
          <div class="text-help">
            {{ $t('datashield_access_not_restricted') }}
          </div>
        </div>
      </div>
    </div>
    <div class="text-h6 q-mb-md">{{ $t('settings') }}</div>
    <div v-if="!missingCluster" class="q-mb-md">
      <q-btn
        color="primary"
        text-color="white"
        icon="restart_alt"
        :label="$t('initialize')"
        :title="$t('datashield_settings_init')"
        size="sm"
        @click="onShowInitSettings" />
    </div>
    <q-tabs
      v-model="tab"
      dense
      class="text-grey"
      active-color="primary"
      indicator-color="primary"
      align="justify"
      narrow-indicator
    >
      <q-tab name="aggregate" :label="$t('aggregate')" />
      <q-tab name="assign" :label="$t('assign')" />
      <q-tab name="options" :label="$t('options')" />
    </q-tabs>
    <q-separator />
    <q-tab-panels v-model="tab">
      <q-tab-panel v-for="env in ['aggregate', 'assign']" :key="env" :name="env">
        <datashield-methods :env="env"/>
      </q-tab-panel>
      <q-tab-panel name="options">
        <datashield-options />
      </q-tab-panel>
    </q-tab-panels>
    <datashield-profile-init-dialog v-model="showInitSettings" />
  </div>
</template>

<script setup lang="ts">
import { DataShieldProfileDto } from 'src/models/DataShield';
import FieldsList, { FieldItem } from 'src/components/FieldsList.vue';
import DatashieldMethods from 'src/components/admin/datashield/DatashieldMethods.vue';
import DatashieldOptions from 'src/components/admin/datashield/DatashieldOptions.vue';
import DatashieldProfileInitDialog from 'src/components/admin/datashield/DatashieldProfileInitDialog.vue';

interface Props {
  profile: DataShieldProfileDto;
}

const props = defineProps<Props>();

onMounted(() => {
  enabled.value = props.profile.enabled;
  datashieldStore.initProfileSettings(props.profile);
});

const rStore = useRStore();
const datashieldStore = useDatashieldStore();

const tab = ref<string>('aggregate');
const enabled = ref(props.profile.enabled);
const showDelete = ref(false);
const showInitSettings = ref(false);

const missingCluster = computed(() => props.profile === undefined || getCluster(props.profile) === undefined)
const builtinProfile = computed(() => props.profile?.name === props.profile?.cluster);

const items: FieldItem<DataShieldProfileDto>[] = [
  {
    field: 'name',
  },
  {
    field: 'cluster',
    label: 'r_cluster',
    html: (val: DataShieldProfileDto) =>
      val ? `<code>${val.cluster}</code>` : '',
  },
]

function getCluster(profile: DataShieldProfileDto) {
  return rStore.clusters.find((c) => c.name === profile.cluster)
}

function onDeleteProfile() {
  showDelete.value = true;
}

function onShowInitSettings() {
  showInitSettings.value = true;
}

</script>
