<template>
  <div>
    <div class="q-mb-md">
      <q-btn
        color="primary"
        text-color="white"
        icon="add"
        :label="$t('add_profile')"
        size="sm"
        @click="onAddProfile" />
    </div>
    <div class="row q-col-gutter-md">
      <div :class="$q.screen.lt.lg ? 'col-3' : 'col-2'">
        <q-tabs
          v-model="tab"
          no-caps
          vertical
          class="text-grey"
          active-color="primary"
          indicator-color="primary"
        >
          <q-tab v-for="profile in datashieldStore.profiles" :key="profile.name"
            :name="profile.name"
            :label="getProfileLabel(profile)"
            :alert="profile.enabled ? 'green' : 'red'"
            :icon="getProfileIcon(profile)"
          />
        </q-tabs>

      </div>
      <div :class="$q.screen.lt.lg ? 'col-9' : 'col-10'">
        <q-tab-panels v-model="tab">
          <q-tab-panel v-for="profile in datashieldStore.profiles" :key="profile.name" :name="profile.name" style="padding-top: 0;">
            <datashield-profile :profile="profile" />
          </q-tab-panel>
        </q-tab-panels>
      </div>
    </div>
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
