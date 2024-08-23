<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('search')" to="/search" />
        <q-breadcrumbs-el :label="$t('variables')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">

    </q-page>
  </div>
</template>

<script setup lang="ts">
</script>
