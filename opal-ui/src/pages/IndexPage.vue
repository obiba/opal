<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="row">
        <div class="col-2">
          <q-card class="on-left bg-primary text-white">
            <q-card-section>
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
            </q-card-section>
          </q-card>
        </div>
        <div class="col-2">
          <q-card class="on-left bg-warning text-black">
            <q-card-section>
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
            </q-card-section>
          </q-card>
        </div>
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
