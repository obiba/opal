<template>
  <div v-if="profile?.userInfo">
    <div class="text-h6 q-mb-md">
      {{ t('user_profile.user_info') }}
    </div>
    <user-info-panel :profile="profile" class="q-mb-md" />
  </div>
</template>

<script setup lang="ts">
import UserInfoPanel from 'src/components/admin/profiles/UserInfoPanel.vue';
import { type SubjectProfileDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';

const { t } = useI18n();

interface Props {
  principal: string;
}

const props = defineProps<Props>();

const profilesStore = useProfilesStore();

const profile = ref<SubjectProfileDto>();

onMounted(() => {
  if (props.principal) {
    profilesStore.getProfile(props.principal).then((p) => {
      profile.value = p;
    }).catch(() => {
      profile.value = undefined;
      notifyError(t('user_profile.load_error', { user: props.principal }));
    });
  }
});

</script>