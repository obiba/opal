<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="name" :to="`/project/${name}`" />
        <q-breadcrumbs-el :label="t('permissions')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        {{ t('permissions') }}
      </div>
      <div class="text-help q-mb-md">
        {{ t('project_permissions_info') }}
      </div>
      <div v-if="hasSubjects" class="row q-gutter-md">
        <div class="col">
          <div v-for="type in [Subject_SubjectType.USER, Subject_SubjectType.GROUP]" :key="type">
            <div v-if="subjectTypes[type]">
              <q-list dense padding>
                <q-item-label header class="text-weight-bolder">
                  <q-icon :name="ICONS[type]" size="sm" class="on-left" />
                  <span>{{ CAPTIONS[type] ? t(CAPTIONS[type]) : type }}</span>
                </q-item-label>
                <q-item
                  clickable
                  active-class="bg-grey-2"
                  :active="selectedSubject.principal === subject.principal"
                  v-for="subject in subjectTypes[type]"
                  :key="subject.principal"
                  @click="onSubject(subject)"
                >
                  <q-item-section class="q-pa-none q-mr-sm text-caption"> {{ subject.principal }} </q-item-section>
                </q-item>
              </q-list>
            </div>
          </div>
        </div>
        <div class="col-10">
          <access-control-table
            v-model="selectedAcls"
            :acls="acls"
            :on-delete-acls="onDeleteAcls"
            :hide-delete="false"
            :loading="loading"
          />
        </div>
      </div>
      <div v-else class="text-help">{{ t('project_acls_empty') }}</div>

      <confirm-dialog
        v-model="showDeletes"
        :title="t('delete')"
        :text="t('delete_profile_acl_confirm', { count: selectedAcls.length })"
        @confirm="doDeleteAcls"
      />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import { type Subject, type Acl, Subject_SubjectType } from 'src/models/Opal';
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

const { t } = useI18n();
const route = useRoute();
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
      await Promise.all(toDelete.map((acl) => projectsStore.deleteSubjectPermission(selectedSubject.value, acl)));
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
    acc[subject.type]?.push(subject);
    acc[subject.type]?.sort((a, b) => a.principal.localeCompare(b.principal));
    return acc;
  }, {} as Record<string, Subject[]>);
}

function findCandidateSubject() {
  const candidates =
    subjectTypes.value[Subject_SubjectType.USER] || subjectTypes.value[Subject_SubjectType.GROUP] || [];
  return candidates[0] || null;
}

async function loadSubjects() {
  projectsStore.loadSubjects().then(() => {
    initializeSubjectTypes();
    const candidate = findCandidateSubject();
    if (candidate) {
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
