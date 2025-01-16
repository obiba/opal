<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="t('identity_providers')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <identity-providers-list />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import IdentityProvidersList from 'src/components/admin/providers/IdentityProvidersList.vue';

const { t } = useI18n();
</script>
