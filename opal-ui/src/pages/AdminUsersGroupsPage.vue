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
      <users-list></users-list>
      <groups-list></groups-list>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import UsersList from 'src/components/admin/UsersList.vue';
import GroupsList from 'src/components/admin/GroupsList.vue';


</script>
