<template>
  <div>
    <q-btn
      color="primary"
      text-color="white"
      icon="add"
      :label="$t('add_profile')"
      size="sm"
      @click="onAddProfile"
      class="q-mb-md" />

    <q-card v-if="datashieldStore.profiles?.length" bordered flat>
      <q-card-section class="q-pa-none">
        <q-tabs
          v-model="tab"
          no-caps
          dense
          inline-label
          class="text-grey"
          active-color="primary"
          indicator-color="primary"
          align="justify"
        >
          <q-tab v-for="profile in datashieldStore.profiles" :key="profile.name"
            :name="profile.name"
            :label="getProfileLabel(profile)"
            :alert="profile.enabled ? 'green' : 'red'"
            :icon="getProfileIcon(profile)"
          />
        </q-tabs>
        <q-separator />
        <q-tab-panels v-model="tab" class="q-pl-md q-pr-md">
          <q-tab-panel v-for="profile in datashieldStore.profiles" :key="profile.name" :name="profile.name" style="padding-top: 0;">
            <datashield-profile :profile="profile" class="q-mt-md"/>
          </q-tab-panel>
        </q-tab-panels>
      </q-card-section>
    </q-card>
    <add-datashield-profile-dialog v-model="showAdd" />
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'DatashieldProfiles',
});
</script>
<script setup lang="ts">
import { DataShieldProfileDto } from 'src/models/DataShield';
import DatashieldProfile from 'src/components/admin/datashield/DatashieldProfile.vue';
import AddDatashieldProfileDialog from 'src/components/admin/datashield/AddDatashieldProfileDialog.vue';

const datashieldStore = useDatashieldStore();

const tab = ref<string>(datashieldStore.profiles.length ? datashieldStore.profiles[0].name : '');
const showAdd = ref(false);

watch(() => datashieldStore.profiles, () => {
  if (datashieldStore.profiles.length) {
    if (tab.value === '' || !datashieldStore.profiles.find((profile) => profile.name === tab.value))
      tab.value = datashieldStore.profiles[0].name;
  }
});

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
