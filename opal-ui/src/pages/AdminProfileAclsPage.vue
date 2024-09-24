<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('user_profiles')" to="/admin/profiles" />
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
        <div class="row q-gutter-md">
          <div class="col" style="max-width: 200px;">
            <div v-for="grp in groups" :key="grp">
              <q-btn
                flat
                no-caps
                icon="group"
                color="primary"
                size="12px"
                :label="grp"
                align="left"
                class="full-width"
                :class="`${ tab === grp ? 'bg-grey-2' : '' }`"
                @click="tab = grp"
              ></q-btn>
            </div>
          </div>
          <div class="col">
            <q-tab-panels v-model="tab">
              <q-tab-panel v-for="grp in groups" :key="grp" :name="grp"
                style="padding-top: 0">
                <div class="text-h6 q-mb-sm">{{ grp }}</div>
                <q-separator />
                <profile-acls-list :principal="grp" :type="Subject_SubjectType.GROUP"/>
              </q-tab-panel>
            </q-tab-panels>
          </div>
        </div>
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
