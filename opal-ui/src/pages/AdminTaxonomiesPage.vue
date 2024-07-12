<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('taxonomies')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <taxonomies-list></taxonomies-list>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import TaxonomiesList from 'src/components/admin/taxonomies/TaxonomiesList.vue';
</script>
