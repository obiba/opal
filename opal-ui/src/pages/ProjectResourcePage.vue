<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="pName" :to="`/project/${pName}`" />
        <q-breadcrumbs-el :label="$t('resources')" :to="`/project/${pName}/resources`"/>
        <q-breadcrumbs-el :label="rName" />
      </q-breadcrumbs>
      <q-space />
      <q-btn
        outline
        icon="navigate_before"
        size="sm"
        :label="previousReference?.name"
        :to="`/project/${pName}/resource/${previousReference?.name}`"
        v-if="previousReference"
        class="on-right"
      />
      <q-btn
        outline
        icon-right="navigate_next"
        size="sm"
        :label="nextReference?.name"
        :to="`/project/${pName}/resource/${nextReference?.name}`"
        v-if="nextReference"
        class="on-right"
      />
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        <q-icon name="link" /><span>{{ rName }}</span>
        <q-btn
          color="secondary"
          size="sm"
          outline
          icon="edit"
          @click="onShowEditResource"
          class="on-right"
        />
        <q-btn
          color="positive"
          size="sm"
          outline
          icon="terminal"
          :label="$t('test')"
          @click="onTest"
          class="on-right"
        />
      </div>
      <q-tabs
        v-model="tab"
        dense
        class="text-grey q-mt-md"
        active-color="primary"
        indicator-color="primary"
        align="justify"
        narrow-indicator
      >
        <q-tab name="reference" :label="$t('reference')" />
        <q-tab name="permissions" :label="$t('permissions')"/>
      </q-tabs>

      <q-separator />

      <q-tab-panels v-model="tab">
        <q-tab-panel name="reference">
          <resource-reference />
        </q-tab-panel>

        <q-tab-panel name="permissions">
          <div class="text-h6">{{ $t('permissions') }}</div>
          <access-control-list
            :resource="`/project/${pName}/permissions/resource/${rName}`"
            :options="['RESOURCE_VIEW', 'RESOURCE_ALL']"
          />
        </q-tab-panel>
      </q-tab-panels>

    </q-page>
  </div>
</template>

<script setup lang="ts">
import ResourceReference from 'src/components/resources/ResourceReference.vue';
import AccessControlList from 'src/components/permissions/AccessControlList.vue';
import { notifyError, notifySuccess } from 'src/utils/notify';

const route = useRoute();
const projectsStore = useProjectsStore();
const resourcesStore = useResourcesStore();
const { t } = useI18n();

const pName = computed(() => route.params.id as string);
const rName = computed(() => route.params.rid as string);
const tab = ref('reference');

const previousReference = computed(() => {
  const idx = resourcesStore.resourceReferences.findIndex((rsrc) => rsrc.name === rName.value);
  return idx > 0 ? resourcesStore.resourceReferences[idx - 1] : null;
});

const nextReference = computed(() => {
  const idx = resourcesStore.resourceReferences.findIndex((rsrc) => rsrc.name === rName.value);
  return idx === resourcesStore.resourceReferences.length - 1 ? null : resourcesStore.resourceReferences[idx + 1];
});

onMounted(() => {
  projectsStore.initProject(pName.value);
});

function onTest() {
  resourcesStore.testResource(pName.value, rName.value)
    .then(() => {
      notifySuccess(t('resource_ref.test_success'));
    })
    .catch((error) => {
      notifyError(t('resource_ref.test_error', { error: error.response.data.message }));
    });
}
</script>
