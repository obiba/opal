<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('profile')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        {{ t('user_profile.title') }}
      </div>
      <profile-user />

      <div class="text-h6 q-mt-lg q-mb-md">
        {{ t('r_quota.usage_title', { context: DS_CONTEXT }) }}
      </div>
      <r-quota-usage :principal="authStore.profile.principal" :context="DS_CONTEXT" current />

      <div class="text-h6 q-mt-lg q-mb-md">
        {{ t('r_activity') }}
      </div>
      <div class="text-help q-mb-md">
        {{ t('r_activity_info') }}
      </div>
      <r-activity :principal="authStore.profile.principal" current />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import ProfileUser from 'src/components/profile/ProfileUser.vue';
import RActivity from 'src/components/admin/profiles/RActivity.vue';
import RQuotaUsage from 'src/components/admin/profiles/RQuotaUsage.vue';
import { DS_CONTEXT } from 'src/components/models';

const { t } = useI18n();
const authStore = useAuthStore();
</script>
