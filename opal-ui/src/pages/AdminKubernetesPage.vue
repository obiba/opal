<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="t('kubernetes.title')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        {{ t('kubernetes.title') }}
      </div>
      <div class="text-help">{{ t('kubernetes.info') }}</div>
      <q-card flat>
        <q-card-section class="q-px-none">
          <pod-specs />
        </q-card-section>
      </q-card>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import PodSpecs from 'src/components/admin/kubernetes/PodSpecs.vue';

const { t } = useI18n();
</script>
