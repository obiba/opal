<template>
  <div>
    <div class="text-h5 q-mb-md">
      {{ $t('user_profile.title') }}
    </div>
    <div>
      <!-- Account -->
      <div class="text-h6">{{ $t('account') }}</div>
      <div>
        <div>
          <span>{{ $t('user_profile.groups', { count: (profile?.groups || []).length }) }}</span>
          <q-chip v-for="group in profile?.groups" :key="group">
            {{ group }}
          </q-chip>
        </div>
        <div>
          <q-btn v-if="isOpalUserRealm" color="primary" :label="$t('update_password')" />
          <q-chip v-else square class="q-py-lg q-ml-none" color="info" text-color="white" icon="warning">{{
            $t('user_profile.password_update_not_allowed', { realm: profile?.realm })
          }}</q-chip>
        </div>
      </div>
      <!-- !Account -->

      <!-- 2FA -->
      <div v-if="isAnOpalRealm" class="q-gutter-sm">
        <div class="text-h6 q-mt-lg">{{ $t('2fa.title') }}</div>
        <div
          v-html="
            $t('user_profile.2fa_info', {
              androidOtp: androidOtpUrl,
              androidOnlyOtp: androidOnlyOtpUrl,
              iosOtp: iosOtpUrl,
            })
          "
        ></div>
        <q-btn
          no-caps
          :icon="otpIcon"
          color="primary"
          :label="profile?.otpEnabled ? $t('user_profile.disable_2fa') : $t('user_profile.enable_2fa')"
          @click="onToggleOtp"
        />

        <div v-if="profile?.otpEnabled && otpQrCode" class="q-p-xl bg-blue-grey-1">
          <p class="q-py-md q-ml-sm">{{ $t('user_profile.otp_qr_core_info') }}</p>
          <div class="text-center q-pb-lg"><img :src="otpQrCode" alt="QR Code" /></div>
        </div>
      </div>
      <!--!2FA -->

      <!-- PERSONAL ACCESS TOKENS-->
      <div class="q-gutter-sm">
        <div class="text-h6 q-mt-lg">{{ $t('user_profile.personal_access_tokens') }}</div>
      </div>
      <q-table
        flat
        :rows="tokens"
        :columns="columns"
        row-key="name"
        :pagination="initialPagination"
        :hide-pagination="tokens.length <= initialPagination.rowsPerPage"
        :loading="loading"
      >
      </q-table>
      <!-- !PERSONAL ACCESS TOKENS-->
      <pre>{{ tokens }}</pre>
    </div>
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'ProfileUser',
});
</script>

<script setup lang="ts">
import { onMounted, nextTick } from 'vue';
import { SubjectProfileDto, SubjectTokenDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';
import { getDateLabel } from 'src/utils/dates';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';

const loading = ref(false);
const profilesStore = useProfilesStore();
const tokensStore = useTokensStore();
const profile = ref<SubjectProfileDto | null>(null);
const tokens = ref<SubjectTokenDto[]>([]);
const otpQrCode = ref<string | null>(null);
const { t } = useI18n();
const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});
const columns = [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'name',
    sortable: true,
    style: 'width: 25%',
  },
  {
    name: 'projects',
    label: t('projects'),
    align: 'left',
    field: 'groups',
  },
  {
    name: 'access',
    label: t('data_access'),
    align: 'left',
    field: 'access',
    format: (val: string) => t(`access.${val}`),
  },
  {
    name: 'commands',
    label: t('tasks'),
    align: 'center  ',
    field: 'commands',
    format: (values: string[]) => (values || []).map((val) => t(`command_types.${val}`)),
  },
  {
    name: 'services',
    label: t('services'),
    align: 'center  ',
    field: (row: SubjectTokenDto) => getServicesField(row),
    format: (values: string[]) => values.map((val) => t(`token_services.${val}`)),
  },
  {
    name: 'inactive',
    label: t('inactive'),
    align: 'center  ',
    field: 'inactiveAt',
    format: (val: string) => getDateLabel(val),
  },
];

const otpIcon = computed(() => (profile.value?.otpEnabled ? 'lock_open' : 'lock'));
const isOpalUserRealm = computed(() => profile.value && 'opal-user-realm' === profile.value.realm);
const isAnOpalRealm = computed(
  () => profile.value && ['opal-ini-realm', 'opal-user-realm'].includes(profile.value.realm)
);
const androidOtpUrl = computed(() => {
  return `<a href="https://play.google.com/store/apps/details?id=com.azure.authenticator" target="_blank">${t(
    'user_profile.android_otp'
  )}</a>`;
});
const androidOnlyOtpUrl = computed(() => {
  return `<a href="https://play.google.com/store/apps/details?id=org.liberty.android.freeotpplus" target="_blank">${t(
    'user_profile.android_only_otp'
  )}</a>`;
});
const iosOtpUrl = computed(() => {
  return `<a href="https://apps.apple.com/us/app/microsoft-authenticator/id983156458" target="_blank">${t(
    'user_profile.ios_otp'
  )}</a>`;
});

async function onToggleOtp() {
  if (profile.value) {
    try {
      if (profile.value.otpEnabled) {
        otpQrCode.value = null;
        await profilesStore.disableCurrentOtp();
      } else {
        otpQrCode.value = await profilesStore.enableCurrentOtp();
      }

      await fetchProfile();
    } catch (e) {
      notifyError(e);
    }
  }
}

function getServicesField(row: SubjectTokenDto): string[] {
  const services: string[] = [];
  if (row.useR) services.push('useR');
  if (row.useDatashield) services.push('useDatashield');
  if (row.useSQL) services.push('useSQL');
  if (row.sysAdmin) services.push('sysAdmin');
  return services;
}

function fetchProfile() {
  return Promise.all([profilesStore.getCurrentProfile(), tokensStore.getCurrentTokens()])
    .then(([profileDto, tokenDtos]) => {
      profile.value = profileDto;
      tokens.value = tokenDtos;
    })
    .catch(notifyError);
}

onMounted(() => {
  fetchProfile();
});
</script>
