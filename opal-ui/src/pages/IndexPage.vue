<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="row">
        <div class="col-md-auto col-sm-auto col-xs-12">
          <q-card class="on-left bg-primary text-white q-mb-md o-card">
            <q-card-section>
              <div class="text-caption">{{  $t('projects_caption') }}</div>
            </q-card-section>
            <q-separator/>
            <q-card-actions>
              <div class="text-subtitle2">
                {{ $t('projects') }}
                <q-btn
                  flat
                  rounded
                  icon="arrow_forward"
                  size="sm"
                  class="q-pa-sm bg-grey-4 text-grey-10 q-ml-sm"
                  to="/projects"
                ></q-btn>
              </div>
            </q-card-actions>
          </q-card>
        </div>
        <div class="col-md-auto col-sm-auto col-xs-12">
          <q-card class="on-left bg-warning text-black q-mb-md o-card">
            <q-card-section>
              <div class="text-caption">{{ $t('files_caption') }}</div>
            </q-card-section>
            <q-separator/>
            <q-card-actions>
              <div class="text-subtitle2">
                {{ $t('files') }}
                <q-btn
                  flat
                  rounded
                  icon="arrow_forward"
                  size="sm"
                  class="q-pa-sm bg-grey-4 text-grey-10 q-ml-sm"
                  :to="`/files/home/${username}`"
                ></q-btn>
              </div>
            </q-card-actions>
          </q-card>
        </div>
      </div>
      <div>
        <q-btn outline no-caps icon="admin_panel_settings" color="secondary" to="/admin">{{ $t('administration') }}</q-btn>
      </div>
    </q-page>
  </div>
</template>

<script setup lang="ts">
const authStore = useAuthStore();

const username = computed(() =>
  authStore.profile?.principal ? authStore.profile.principal : ''
);
</script>
