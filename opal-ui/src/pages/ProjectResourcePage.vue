<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="pName" :to="`/project/${pName}`" />
        <q-breadcrumbs-el :label="t('resources')" :to="`/project/${pName}/resources`" />
        <q-breadcrumbs-el :label="rName" />
      </q-breadcrumbs>
      <q-space />
      <q-btn
        outline
        no-caps
        icon="navigate_before"
        size="sm"
        :label="previousReference?.name"
        :to="`/project/${pName}/resource/${previousReference?.name}`"
        v-if="previousReference"
        class="on-right"
      />
      <q-btn
        outline
        no-caps
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
        <q-icon name="link" class="on-left" /><span>{{ rName }}</span>
        <q-btn
          v-if="resourcesStore.perms.resource?.canUpdate()"
          color="secondary"
          size="sm"
          outline
          icon="edit"
          @click="onShowEdit"
          :disable="!resourceProvider"
          class="on-right"
        />
        <q-btn
          color="positive"
          size="sm"
          outline
          icon="terminal"
          :label="t('test')"
          @click="onTest"
          class="on-right"
        />
        <q-btn
          color="secondary"
          size="sm"
          icon="content_copy"
          :title="t('duplicate')"
          @click="onShowDuplicate"
          :disable="!resourceProvider"
          class="on-right"
        />
        <q-btn
          v-if="resourcesStore.perms.resource?.canDelete()"
          outline
          color="red"
          icon="delete"
          size="sm"
          @click="onShowDelete"
          class="on-right"
        />
        <q-btn
          :label="t('add_view')"
          icon="add_circle"
          no-caps
          dense
          flat
          size="sm"
          @click="onAddView"
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
      >
        <q-tab name="reference" :label="t('reference')" />
        <q-tab
          name="permissions"
          :label="t('permissions')"
          v-if="resourcesStore.perms.resourcePermissions?.canRead()"
        />
      </q-tabs>

      <q-separator />

      <q-tab-panels v-model="tab">
        <q-tab-panel name="reference">
          <resource-reference />
        </q-tab-panel>

        <q-tab-panel name="permissions" v-if="resourcesStore.perms.resourcePermissions?.canRead()">
          <access-control-list
            :resource="`/project/${pName}/permissions/resource/${rName}`"
            :options="['RESOURCE_VIEW', 'RESOURCE_ALL']"
          />
        </q-tab-panel>
      </q-tab-panels>

      <resource-view-dialog v-if="resourceReference" v-model="showAddView" :resource="resourceReference" />
      <resource-reference-dialog
        v-model="showEdit"
        :provider="resourceProvider"
        :resource="selected"
        @saved="onSaved"
      />
      <confirm-dialog
        v-model="showDelete"
        :title="t('delete')"
        :text="t('delete_resources_confirm', { count: 1 })"
        @confirm="onDeleteResource"
      />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import type { ResourceReferenceDto } from 'src/models/Projects';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import ResourceReference from 'src/components/resources/ResourceReference.vue';
import AccessControlList from 'src/components/permissions/AccessControlList.vue';
import ResourceReferenceDialog from 'src/components/resources/ResourceReferenceDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import ResourceViewDialog from 'src/components/resources/ResourceViewDialog.vue';
import { notifyError, notifySuccess } from 'src/utils/notify';

const route = useRoute();
const router = useRouter();
const projectsStore = useProjectsStore();
const resourcesStore = useResourcesStore();
const { t } = useI18n();

const pName = computed(() => route.params.id as string);
const rName = computed(() => route.params.rid as string);
const tab = ref('reference');
const showEdit = ref(false);
const showDelete = ref(false);
const showAddView = ref(false);
const selected = ref({} as ResourceReferenceDto);

const resourceReference = computed(() => resourcesStore.getResourceReference(rName.value));
const resourceProvider = computed(() =>
  resourceReference.value ? resourcesStore.getResourceProvider(resourceReference.value) : undefined
);

const previousReference = computed(() => {
  const idx = resourcesStore.resourceReferences.findIndex((rsrc) => rsrc.name === rName.value);
  return idx > 0 ? resourcesStore.resourceReferences[idx - 1] : null;
});

const nextReference = computed(() => {
  const idx = resourcesStore.resourceReferences.findIndex((rsrc) => rsrc.name === rName.value);
  return idx === resourcesStore.resourceReferences.length - 1 ? null : resourcesStore.resourceReferences[idx + 1];
});

onMounted(() => {
  projectsStore.initProject(pName.value).finally(loadPerms);
});

watch([() => pName.value, () => rName.value], loadPerms);

function loadPerms() {
  resourcesStore.loadResourcePerms(pName.value, rName.value);
}

function onTest() {
  resourcesStore
    .testResource(pName.value, rName.value)
    .then(() => {
      notifySuccess(t('resource_ref.test_success'));
    })
    .catch((error) => {
      notifyError(t('resource_ref.test_error', { error: error.response.data.message }));
    });
}

function onShowDelete() {
  showDelete.value = true;
}

function onDeleteResource() {
  resourcesStore
    .deleteResource(pName.value, rName.value)
    .then(() => {
      return resourcesStore.loadResourceReferences(pName.value);
    })
    .then(() => {
      router.push(`/project/${pName.value}/resources`);
    });
}

function onShowEdit() {
  selected.value = resourceReference.value ? { ...resourceReference.value } : ({} as ResourceReferenceDto);
  showEdit.value = true;
}

function onShowDuplicate() {
  selected.value = resourceReference.value ? { ...resourceReference.value } : ({} as ResourceReferenceDto);
  selected.value.name = '';
  showEdit.value = true;
}

function onSaved(resource: ResourceReferenceDto) {
  resourcesStore
    .loadResourceReferences(pName.value)
    .then(() => router.push(`/project/${pName.value}/resource/${resource.name}`));
}

function onAddView() {
  showAddView.value = true;
}
</script>
