<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('profile')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <profile-user></profile-user>   
    </q-page>
  </div>
</template>

<script setup lang="ts">
import ProfileUser from 'src/components/admin/profiles/ProfileUser.vue';
</script>
