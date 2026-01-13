<template>
  <div v-if="profile?.userInfo">
    <div class="text-h6 q-mb-md">
      {{ t('user_profile.user_info') }}
    </div>
    <div class="row">
      <div class="col-12 col-md-6">
        <user-info-panel :profile="profile" class="q-mb-md" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import UserInfoPanel from 'src/components/admin/profiles/UserInfoPanel.vue';
import { type SubjectProfileDto } from 'src/models/Opal';

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
    });
  }
});

</script>