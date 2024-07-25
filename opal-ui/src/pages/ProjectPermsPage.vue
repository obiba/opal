<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="name" :to="`/project/${name}`" />
        <q-breadcrumbs-el :label="$t('permissions')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div v-if="hasSubjects" class="row q-gutter-md">
        <div class="col">
          <div v-for="(subjects, type, index) in subjectTypes" :key="type">
            <div :class="{ 'q-mt-lg': index !== 0 }">
              <div class="col-2 q-px-md bg-grey-1">
                <div class="text-h5">
                  <q-icon :name="ICONS[type]" size="sm" class="q-mb-xs"></q-icon
                  ><span class="on-right">{{ $t(CAPTIONS[type]) }}</span>
                </div>

                <q-list dense padding>
                  <q-item
                    clickable
                    active-class="bg-light-blue-1"
                    :active="selectedSubject.principal === subject.principal"
                    v-for="subject in subjects"
                    :key="subject.principal"
                    @click="onSubject(subject)"
                  >
                    <q-item-section class="q-pa-none q-mr-sm"> {{ subject.principal }} </q-item-section>
                  </q-item>
                </q-list>
              </div>
            </div>
          </div>
        </div>
        <div class="col-9">
          <div class="text-h5">
            <q-icon name="lock" size="sm" class="q-mb-xs"></q-icon><span class="on-right">{{ $t('permissions') }}</span>
          </div>
          <access-control-table
            class=""
            v-model="selectedAcls"
            :acls="acls"
            :on-delete-acls="onDeleteAcls"
            :hide-delete="false"
            :loading="loading"
          />
        </div>
      </div>
      <div v-else class="text-help">{{ $t('project_acls_empty') }}</div>

      <confirm-dialog
        v-model="showDeletes"
        :title="$t('delete')"
        :text="$t('delete_profile_acl_confirm', { count: selectedAcls.length })"
        @confirm="doDeleteAcls"
      />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import { Subject, Acl } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';
import AccessControlTable from 'src/components/permissions/AccessControlTable.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';

const ICONS: Record<string, string> = {
  USER: 'person',
  GROUP: 'group',
};
const CAPTIONS: Record<string, string> = {
  USER: 'users',
  GROUP: 'groups',
};

const route = useRoute();
const { t } = useI18n();
const projectsStore = useProjectsStore();
const acls = ref([] as Acl[]);
const selectedSubject = ref({} as Subject);
const selectedAcls = ref<Acl[]>([]);
const subjectTypes = ref({} as Record<string, Subject[]>);
const showDeletes = ref(false);
const loading = ref(false);
const name = computed(() => route.params.id as string);
const hasSubjects = computed(() => projectsStore.subjects.length > 0);

async function doDeleteAcls() {
  showDeletes.value = false;
  const toDelete: Acl[] = selectedAcls.value;
  selectedAcls.value = [];

  try {
    if (toDelete.length === acls.value.length) {
      await projectsStore.deleteSubject(selectedSubject.value);
      await loadSubjects();
    } else {
      await Promise.all(toDelete.map((acl) => projectsStore.deleteSubjectPermissions(selectedSubject.value, acl)));
      onSubject(selectedSubject.value);
    }
    await projectsStore.loadSubjects();
  } catch (err) {
    notifyError(err);
  }
}

function initializeSubjectTypes() {
  subjectTypes.value = projectsStore.subjects.reduce((acc: Record<string, Subject[]>, subject: Subject) => {
    acc[subject.type] = acc[subject.type] || [];
    acc[subject.type].push(subject);
    acc[subject.type].sort((a, b) => a.principal.localeCompare(b.principal));
    return acc;
  }, {} as Record<string, Subject[]>);
}

function findCandidateSubject() {
  const candidates = Object.values(subjectTypes.value).flat();
  return candidates[0] || null;
}

async function loadSubjects() {
  projectsStore.loadSubjects().then(() => {
    initializeSubjectTypes();
    const candidate = findCandidateSubject();
    if (!!candidate) {
      onSubject(candidate);
    }
  });
}

// Handlers

function onDeleteAcls() {
  showDeletes.value = true;
}

async function onSubject(subject: Subject) {
  try {
    selectedSubject.value = subject;

    acls.value = await projectsStore.getSubjectPermissions(subject);
  } catch (error) {
    notifyError(error);
  }
}

onMounted(() => {
  projectsStore.initProject(name.value).then(() => loadSubjects());
});
</script>
