<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="row q-col-gutter-lg q-mb-lg">
        <div class="col-6 col-sm-6 col-xs-12">
          <q-list separator>
            <q-item-label header class="text-uppercase">{{ $t('data_management') }}</q-item-label>
            <q-item>
              <q-item-section>
                <q-item-label><router-link to="/projects">{{ $t('projects') }}</router-link></q-item-label>
                <q-item-label caption lines="2">{{ $t('projects_caption') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item>
              <q-item-section>
                <q-item-label><router-link to="/files">{{ $t('files') }}</router-link></q-item-label>
                <q-item-label caption lines="2">{{ $t('files_caption') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item>
              <q-item-section>
                <q-item-label><router-link to="/tasks">{{ $t('tasks') }}</router-link></q-item-label>
                <q-item-label caption lines="2">{{ $t('tasks_caption') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </div>
        <div class="col-6 col-sm-6 col-xs-12">
          <bookmarks-list/>
        </div>
      </div>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import BookmarksList from 'src/components/BookmarksList.vue';
</script>
