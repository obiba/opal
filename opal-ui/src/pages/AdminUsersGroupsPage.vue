<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="t('users_and_groups')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        {{ t('users') }}
      </div>
      <div class="text-help">{{ t('users_info') }}</div>
      <users-list />
      <div class="text-h5 q-mt-lg q-mb-md">
        {{ t('groups') }}
      </div>
      <div class="text-help q-mb-md">{{ t('groups_info') }}</div>
      <groups-list />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import UsersList from 'src/components/admin/users/UsersList.vue';
import GroupsList from 'src/components/admin/users/GroupsList.vue';

const { t } = useI18n();
</script>
