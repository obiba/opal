<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="t('user_profiles')" to="/admin/profiles" />
        <q-breadcrumbs-el :label="`${route.params.principal}`" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        <q-icon name="person" class="q-mb-xs on-left"/>
        <span>{{ principal }}</span>
      </div>
      <div class="text-h6 q-mb-md">
        {{ t('permissions') }}
      </div>
      <profile-acls :principal="principal"/>
      
      <div class="text-h6 q-mt-md q-mb-md">
        {{ t('r_activity') }}
      </div>
      <div class="text-help q-mb-md">
        {{ t('r_activity_info') }}
      </div>
      <r-activity :principal="principal" />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import ProfileAcls from 'src/components/admin/profiles/ProfileAcls.vue';
import RActivity from 'src/components/admin/profiles/RActivity.vue';

const { t } = useI18n();
const route = useRoute();

const principal = computed(() => route.params.principal as string);

</script>
