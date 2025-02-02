<template>
  <div>
    <q-tabs
        v-model="type"
        dense
        class="text-grey"
        active-color="primary"
        indicator-color="primary"
        align="justify"
      >
        <q-tab name="user" :label="t('user')">
          <q-badge color="info" floating style="right: -22px;">
            {{ aclsCount }}
          </q-badge>
        </q-tab>
        <q-tab name="groups" :label="t('groups')">
          <q-badge color="info" floating style="right: -22px;">
            {{ groupAclsCount }}
          </q-badge>
        </q-tab>
      </q-tabs>
      <q-separator />
      <q-tab-panels v-model="type">
        <q-tab-panel name="user">
          <div class="text-help q-mb-md">{{ t('profile_user_acls_info') }}</div>
          <profile-acls-list :principal="principal" :type="Subject_SubjectType.USER"/>
        </q-tab-panel>
        <q-tab-panel name="groups">
          <div class="text-help q-mb-md">{{ t('profile_groups_acls_info') }}</div>
          <div v-if="profile && profile.groups?.length>0">
            <div class="row q-gutter-md">
              <div class="col" style="max-width: 200px;">
                <div v-for="grp in groups" :key="grp">
                  <q-btn
                    flat
                    no-caps
                    icon="group"
                    color="primary"
                    size="12px"
                    :label="grp"
                    align="left"
                    class="full-width"
                    :class="`${ tab === grp ? 'bg-grey-2' : '' }`"
                    @click="tab = grp"
                  >
                  <q-badge color="info" class="on-right">
                    {{ profileAclsStore.groupAcls[grp] ? 1 : 0 }}
                  </q-badge>
                </q-btn>
                </div>
              </div>
              <div class="col">
                <q-tab-panels v-model="tab">
                  <q-tab-panel v-for="grp in groups" :key="grp" :name="grp"
                    style="padding-top: 0">
                    <div class="text-h6 q-mb-sm">{{ grp }}</div>
                    <q-separator />
                    <profile-acls-list :principal="grp" :type="Subject_SubjectType.GROUP"/>
                  </q-tab-panel>
                </q-tab-panels>
              </div>
            </div>
          </div>
        </q-tab-panel>
      </q-tab-panels>
  </div>
</template>

<script setup lang="ts">
import { type SubjectProfileDto, Subject_SubjectType } from 'src/models/Opal';
import ProfileAclsList from 'src/components/admin/profiles/ProfileAclsList.vue';

const { t } = useI18n();

interface Props {
  principal: string;
}

const props = defineProps<Props>();

const profilesStore = useProfilesStore();
const profileAclsStore = useProfileAclsStore();

const type = ref('user');
const tab = ref();
const profile = ref<SubjectProfileDto>();

const groups = computed(() => profile.value?.groups || []);
const aclsCount = computed(() => profileAclsStore.acls ? profileAclsStore.acls.length : 0);
const groupAclsCount = computed(() => profileAclsStore.groupAcls ? Object.keys(profileAclsStore.groupAcls).map(grp => profileAclsStore.groupAcls[grp] ? 1 : 0).reduce((a: number, b: number) => a + b, 0) : 0);

onMounted(() => {
  if (props.principal) {
    profilesStore.getProfile(props.principal).then((p) => {
      profile.value = p;
      if (p.groups && p.groups.length > 0) {
        tab.value = p.groups[0];
      }
      profileAclsStore.initSubjectAcls(profile.value);
    });
  }
});
</script>