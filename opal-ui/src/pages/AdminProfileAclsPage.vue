<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('profiles')" to="/admin/profiles" />
        <q-breadcrumbs-el :label="$t('permissions', {principal: $route.params.principal})" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <profile-acls-list></profile-acls-list>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import ProfileAclsList from 'src/components/admin/profiles/ProfileAclsList.vue';
</script>
