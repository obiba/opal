<template>
  <div>
    <div class="row q-gutter-md">
      <div class="col" style="max-width: 200px;">
        <div v-for="profile in datashieldStore.profiles" :key="profile.name">
          <q-btn
            flat
            no-caps
            :icon="getProfileIcon(profile)"
            :color="profile.enabled ? 'primary' : 'negative'"
            size="12px"
            :label="getProfileLabel(profile)"
            align="left"
            class="full-width"
            :class="`${ tab === profile.name ? 'bg-grey-2' : '' }`"
            @click="tab = profile.name"
          ></q-btn>
        </div>
        <q-btn
          color="primary"
          text-color="white"
          icon="add"
          :label="t('add_profile')"
          size="sm"
          @click="onAddProfile"
          class="q-mt-md full-width"
        />
      </div>
      <div class="col">
        <q-tab-panels v-model="tab" class="q-pl-md q-pr-md">
          <q-tab-panel
            v-for="profile in datashieldStore.profiles"
            :key="profile.name"
            :name="profile.name"
            style="padding-top: 0"
          >
            <datashield-profile :profile="profile" />
          </q-tab-panel>
        </q-tab-panels>
      </div>
    </div>

    <add-datashield-profile-dialog v-model="showAdd" />
  </div>
</template>

<script setup lang="ts">
import type { DataShieldProfileDto } from 'src/models/DataShield';
import DatashieldProfile from 'src/components/admin/datashield/DatashieldProfile.vue';
import AddDatashieldProfileDialog from 'src/components/admin/datashield/AddDatashieldProfileDialog.vue';

const { t } = useI18n();

const datashieldStore = useDatashieldStore();

const tab = ref<string>(datashieldStore.profiles.length && datashieldStore.profiles[0] ? datashieldStore.profiles[0].name : '');
const showAdd = ref(false);

watch(
  () => datashieldStore.profiles,
  () => {
    if (datashieldStore.profiles.length) {
      if (tab.value === '' || !datashieldStore.profiles.find((profile) => profile.name === tab.value))
        tab.value = datashieldStore.profiles[0]?.name || '';
    }
  }
);

function getProfileIcon(profile: DataShieldProfileDto) {
  return profile.name === profile.cluster ? 'push_pin' : 'chevron_right';
}

function getProfileLabel(profile: DataShieldProfileDto) {
  return profile.name === profile.cluster ? profile.name : `${profile.name} (${profile.cluster})`;
}

function onAddProfile() {
  showAdd.value = true;
}
</script>
