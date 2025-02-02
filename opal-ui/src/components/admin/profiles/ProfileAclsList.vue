<template>
  <div>
    <access-control-table
      v-model="selectedAcls"
      :loading="loading"
      :acls="acls"
      :principal="principal"
      :type="props.type"
      @selected="onSelectedAcl"
      :on-delete-acls="onDeleteAcls"
    />

    <confirm-dialog
      v-model="showDeletes"
      :title="t('delete')"
      :text="t('delete_profile_acl_confirm', { count: selectedAcls.length })"
      @confirm="doRemoveAcls"
    />
  </div>
</template>

<script setup lang="ts">
import { notifyError } from 'src/utils/notify';
import { type Acl, Subject_SubjectType } from 'src/models/Opal';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import AccessControlTable from 'src/components/permissions/AccessControlTable.vue';

const { t } = useI18n();

interface Props {
  principal: string;
  type: Subject_SubjectType;
}

const props = defineProps<Props>();

const profileAclsStore = useProfileAclsStore();

const selectedAcls = ref<Acl[]>([]);
const showDeletes = ref(false);
const loading = ref(false);

const acls = computed(() => {
  if (props.type === Subject_SubjectType.USER) {
    return profileAclsStore.acls || [];
  } else if (profileAclsStore.groupAcls[props.principal]) {
    return profileAclsStore.groupAcls[props.principal] || [];
  } else {
    return [];
  }
});

function onSelectedAcl(acls: Acl[]) {
  selectedAcls.value = acls;
}

function onDeleteAcls() {
  showDeletes.value = true;
}

async function doRemoveAcls() {
  showDeletes.value = false;
  const toDelete: Acl[] = selectedAcls.value;
  selectedAcls.value = [];

  loading.value = true;
  try {
    await profileAclsStore.deleteAcls(toDelete);
    await profileAclsStore.initAcls(props.principal, props.type);
  } catch (err) {
    notifyError(err);
  }
  loading.value = false;
}
</script>
