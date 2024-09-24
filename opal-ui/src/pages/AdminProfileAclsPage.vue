<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('profiles')" to="/admin/profiles" />
        <q-breadcrumbs-el :label="`${$route.params.principal}`" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        {{ $t('profile_user_acls') }}
      </div>
      <div class="text-help q-mb-md">{{ $t('profile_user_acls_info') }}</div>
      <profile-acls-list :principal="principal" :type="Subject_SubjectType.USER"/>

      <div class="text-h5 q-mt-lg q-mb-md">
        {{ $t('profile_groups_acls') }}
      </div>
      <div class="text-help q-mb-md">{{ $t('profile_groups_acls_info') }}</div>
      <div v-if="profile && profile.groups?.length>0">
        <q-tabs v-model="tab" dense class="text-grey" active-color="primary" indicator-color="primary" align="justify">
          <template v-for="grp in groups" :key="grp">
            <q-tab :name="grp" :label="grp" />
          </template>
        </q-tabs>
        <q-separator />
        <q-tab-panels v-model="tab">
          <q-tab-panel v-for="grp in groups" :key="grp" :name="grp"
            style="padding-top: 0">
            <profile-acls-list :principal="grp" :type="Subject_SubjectType.GROUP"/>
          </q-tab-panel>
        </q-tab-panels>
      </div>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import { SubjectProfileDto, Subject_SubjectType } from 'src/models/Opal';
import ProfileAclsList from 'src/components/admin/profiles/ProfileAclsList.vue';

const route = useRoute();
const profilesStore = useProfilesStore();
const profileAclsStore = useProfileAclsStore();

const tab = ref();
const profile = ref<SubjectProfileDto>();

const principal = computed(() => route.params.principal);
const groups = computed(() => profile.value?.groups || []);

onMounted(() => {
  if (principal.value) {
    profilesStore.getProfile(principal.value).then((p) => {
      profile.value = p;
      if (p.groups && p.groups.length > 0) {
        tab.value = p.groups[0];
      }
      profileAclsStore.initSubjectAcls(profile.value);
    });
  }
});

</script>
