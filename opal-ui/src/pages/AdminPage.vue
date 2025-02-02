<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('administration')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        {{ t('administration') }}
      </div>
      <div class="row q-col-gutter-lg q-mb-lg">
        <div class="col-6 col-sm-6 col-xs-12">
          <q-list separator>
            <q-item-label header class="text-uppercase">{{ t('data_access') }}</q-item-label>
            <q-item>
              <q-item-section>
                <q-item-label
                  ><router-link to="/admin/users">{{ t('users_and_groups') }}</router-link></q-item-label
                >
                <q-item-label caption lines="2">{{ t('users_and_groups_caption') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item>
              <q-item-section>
                <q-item-label
                  ><router-link to="/admin/profiles">{{ t('user_profiles') }}</router-link></q-item-label
                >
                <q-item-label caption lines="2">{{ t('user_profiles_caption') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item>
              <q-item-section>
                <q-item-label
                  ><router-link to="/admin/idproviders">{{ t('identity_providers') }}</router-link></q-item-label
                >
                <q-item-label caption lines="2">{{ t('identity_providers_caption') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </div>
        <div class="col-6 col-sm-6 col-xs-12">
          <q-list separator>
            <q-item-label header class="text-uppercase">{{ t('data_analysis') }}</q-item-label>
            <q-item>
              <q-item-section>
                <q-item-label
                  ><router-link to="/admin/rservers">{{ t('rservers') }}</router-link></q-item-label
                >
                <q-item-label caption lines="2">{{ t('rservers_caption') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item>
              <q-item-section>
                <q-item-label><router-link to="/admin/datashield">DataSHIELD</router-link></q-item-label>
                <q-item-label caption lines="2">{{ t('datashield.caption') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item>
              <q-item-section>
                <q-item-label
                  ><router-link to="/admin/search">{{ t('search') }}</router-link></q-item-label
                >
                <q-item-label caption lines="2">{{ t('search_caption') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </div>
      </div>
      <div class="row q-col-gutter-lg">
        <div class="col-6 col-sm-6 col-xs-12">
          <q-list separator>
            <q-item-label header class="text-uppercase">{{ t('data_management') }}</q-item-label>
            <q-item>
              <q-item-section>
                <q-item-label
                  ><router-link to="/files">{{ t('files') }}</router-link></q-item-label
                >
                <q-item-label caption lines="2">{{ t('files_caption') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item>
              <q-item-section>
                <q-item-label
                  ><router-link to="/tasks">{{ t('tasks') }}</router-link></q-item-label
                >
                <q-item-label caption lines="2">{{ t('tasks_caption') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item>
              <q-item-section>
                <q-item-label
                  ><router-link to="/admin/identifiers">{{ t('id_mappings.title') }}</router-link></q-item-label
                >
                <q-item-label caption lines="2">{{ t('id_mappings.manage_caption') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </div>
        <div class="col-6 col-sm-6 col-xs-12">
          <q-list separator>
            <q-item-label header class="text-uppercase">{{ t('system') }}</q-item-label>
            <q-item>
              <q-item-section>
                <q-item-label
                  ><router-link to="/admin/settings">{{ t('general_settings') }}</router-link></q-item-label
                >
                <q-item-label caption lines="2">{{ t('general_settings_caption') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item>
              <q-item-section>
                <q-item-label
                  ><router-link to="/taxonomies">{{ t('taxonomies') }}</router-link></q-item-label
                >
                <q-item-label caption lines="2">{{ t('taxonomies_caption') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item>
              <q-item-section>
                <q-item-label
                  ><router-link to="/admin/databases">{{ t('databases') }}</router-link></q-item-label
                >
                <q-item-label caption lines="2">{{ t('databases_caption') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item>
              <q-item-section>
                <q-item-label
                  ><router-link to="/admin/plugins">{{ t('plugins') }}</router-link></q-item-label
                >
                <q-item-label caption lines="2">{{ t('plugins_caption') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item>
              <q-item-section>
                <q-item-label
                  ><router-link to="/admin/apps">{{ t('apps.title') }}</router-link></q-item-label
                >
                <q-item-label caption lines="2">{{ t('apps.caption') }}</q-item-label>
              </q-item-section>
            </q-item>
            <!-- <q-item>
              <q-item-section>
                <q-item-label><router-link to="/admin/jvm">{{ t('jvm') }}</router-link></q-item-label>
                <q-item-label caption lines="2">{{ t('jvm_caption') }}</q-item-label>
              </q-item-section>
            </q-item> -->
          </q-list>
        </div>
      </div>
    </q-page>
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n();
</script>
