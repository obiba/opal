<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('users_and_groups')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <profiles-list></profiles-list>
      <groups-list class="q-mt-lg"></groups-list>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import ProfilesList from 'src/components/admin/ProfilesList.vue';


</script>
