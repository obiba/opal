<template>
  <div>
    <div class="text-h5 q-mb-md">
      {{ $t('profile_acls') }}
    </div>
    <div class="text-help q-mb-md">{{ $t('profile_acls_info') }}</div>

    <access-control-table
      v-model="selectedAcls"
      :loading="loading"
      :acls="profileAclsStore.acls"
      :principal="principal"
      @selected="onSelectedAcl"
      :on-delete-acls="onDeleteAcls"
    />

    <confirm-dialog
      v-model="showDeletes"
      :title="$t('delete')"
      :text="$t('delete_profile_acl_confirm', { count: selectedAcls.length })"
      @confirm="doRemoveAcls"
    />
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'ProfileAclsList',
});
</script>

<script setup lang="ts">
import { onMounted } from 'vue';
import { notifyError } from 'src/utils/notify';
import { Acl } from 'src/models/Opal';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import AccessControlTable from 'src/components/permissions/AccessControlTable.vue';

const profileAclsStore = useProfileAclsStore();
const route = useRoute();
const selectedAcls = ref<Acl[]>([]);
const showDeletes = ref(false);
const loading = ref(false);

const principal = computed(() => route.params.principal.toString());

onMounted(async () => {
  loading.value = true;
  profileAclsStore.initAcls(`${route.params.principal}`).then(() => {
    loading.value = false;
  });
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

  try {
    await profileAclsStore.deleteAcls(toDelete);
    await profileAclsStore.initAcls(`${route.params.principal}`);
  } catch (err) {
    notifyError(err);
  }
}
</script>
