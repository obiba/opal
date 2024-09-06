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
          <q-item-label header class="text-uppercase">{{ $t('data_management') }}</q-item-label>
          <div class="row">
            <q-card flat bordered class="on-left q-mb-md o-card-sm bg-grey-1">
              <q-card-section class="q-pa-sm text-h6 text-center bg-grey-2" >
                <router-link to="/projects">{{ $t('projects') }}</router-link>
              </q-card-section>
              <q-separator />
              <q-card-section class="text-hint">
                {{ $t('projects_caption') }}
              </q-card-section>
            </q-card>
            <q-card flat bordered class="on-left q-mb-md o-card-sm bg-grey-1">
              <q-card-section class="q-pa-sm text-h6 text-center bg-grey-2" >
                <router-link to="/files">{{ $t('files') }}</router-link>
              </q-card-section>
              <q-separator />
              <q-card-section class="text-hint">
                {{ $t('files_caption') }}
              </q-card-section>
            </q-card>
            <q-card flat bordered class="on-left q-mb-md o-card-sm bg-grey-1">
              <q-card-section class="q-pa-sm text-h6 text-center bg-grey-2" >
                <router-link to="/tasks">{{ $t('tasks') }}</router-link>
              </q-card-section>
              <q-separator />
              <q-card-section class="text-hint">
                {{ $t('tasks_caption') }}
              </q-card-section>
            </q-card>
          </div>
          <q-item-label header class="text-uppercase">{{ $t('api') }}</q-item-label>
          <div class="row">
            <q-card flat bordered class="on-left q-mb-md o-card-sm bg-grey-1">
              <q-card-section class="q-pa-sm text-h6 text-center text-white bg-info" >
                  Python
              </q-card-section>
              <q-separator />
              <q-card-section class="text-hint">
                <q-markdown :src="$t('main.python_api')" />
              </q-card-section>
            </q-card>
            <q-card flat bordered class="on-left q-mb-md o-card-sm bg-grey-1">
              <q-card-section class="q-pa-sm text-h6 text-center text-black bg-warning" >
                  R/DataSHIELD
              </q-card-section>
              <q-separator />
              <q-card-section class="text-hint">
                <q-markdown :src="$t('main.r_api')" />
              </q-card-section>
            </q-card>
            <q-card flat bordered class="on-left q-mb-md o-card-sm bg-grey-1">
              <q-card-section class="q-pa-sm text-h6 text-center text-white bg-positive" >
                  Javascript
              </q-card-section>
              <q-separator />
              <q-card-section class="text-hint">
                <q-markdown :src="$t('main.javascript_api')" />
              </q-card-section>
            </q-card>
          </div>
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
