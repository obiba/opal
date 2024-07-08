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
					<p class="q-py-md q-ml-sm ">{{ $t('user_profile.otp_qr_core_info') }}</p>
          <div class="text-center q-pb-lg"><img  :src="otpQrCode" alt="QR Code" /></div>
        </div>
      </div>

      <!--!2FA -->

      <pre>{{ profile }}</pre>
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
import { SubjectProfileDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';

const profilesStore = useProfilesStore();
const profile = ref<SubjectProfileDto | null>(null);
const otpQrCode = ref<string | null>(null);
const { t } = useI18n();
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

function fetchProfile() {
  return profilesStore
    .getCurrentProfile()
    .then((profileDto) => {
      profile.value = profileDto;
    })
    .catch(notifyError);
}

onMounted(() => {
  fetchProfile();
});
</script>
