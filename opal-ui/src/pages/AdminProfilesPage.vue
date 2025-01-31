<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="t('user_profiles')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <profiles-list></profiles-list>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import ProfilesList from 'src/components/admin/profiles/ProfilesList.vue';

const { t } = useI18n();
</script>
