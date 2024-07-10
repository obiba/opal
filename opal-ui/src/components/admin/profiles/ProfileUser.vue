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
          <q-btn
            no-caps
            v-if="isOpalUserRealm"
            color="primary"
            :label="$t('user_profile.update_password')"
            @click="onUpdatePassword"
          />
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
      <p>{{ $t('user_profile.tokens_info') }}</p>
      <q-table
        flat
        :rows="tokens"
        :columns="columns"
        row-key="name"
        :pagination="initialPagination"
        :hide-pagination="tokens.length <= initialPagination.rowsPerPage"
        :loading="loading"
      >
        <template v-slot:top-left>
          <q-btn-dropdown no-caps color="primary" :label="$t('user_profile.add_token')" icon="add">
            <q-list>
              <q-item clickable v-close-popup @click.prevent="onAddDataShieldToken">
                <q-item-section>
                  <q-item-label>{{ $t('user_profile.add_datashield_token') }}</q-item-label>
                </q-item-section>
              </q-item>
              <q-item clickable v-close-popup @click.prevent="onAddRToken">
                <q-item-section>
                  <q-item-label>{{ $t('user_profile.add_r_token') }}</q-item-label>
                </q-item-section>
              </q-item>
              <q-item clickable v-close-popup @click.prevent="onAddSqlToken">
                <q-item-section>
                  <q-item-label>{{ $t('user_profile.add_sql_token') }}</q-item-label>
                </q-item-section>
              </q-item>
              <q-item clickable v-close-popup @click.prevent="onAddCustomToken">
                <q-item-section>
                  <q-item-label>{{ $t('user_profile.add_custom_token') }}</q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-btn-dropdown>
        </template>
        <template v-slot:body-cell-projects="props">
          <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
            {{ props.col.format(props.row.projects) }}
          </q-td>
        </template>
        <template v-slot:body-cell-name="props">
          <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
            <span class="text-primary">{{ props.value }}</span>
            <div class="float-right">
              <q-btn
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="$t('delete')"
                :icon="toolsVisible[props.row.name] ? 'delete' : 'none'"
                class="q-ml-xs"
                @click="onDeleteToken(props.row)"
              />
            </div>
          </q-td>
        </template>
        <template v-slot:body-cell-commands="props">
          <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
            <q-chip dense class="q-ml-none" v-for="command in props.col.format(props.row.commands)" :key="command">
              {{ command }}
            </q-chip>
          </q-td>
        </template>
        <template v-slot:body-cell-services="props">
          <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
            <q-chip
              dense
              class="q-ml-none"
              v-for="service in props.col.format(props.col.field(props.row))"
              :key="service"
            >
              {{ service }}
            </q-chip>
          </q-td>
        </template>
        <template v-slot:body-cell-inactive="props">
          <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
            {{ props.col.format(props.row.inactiveAt) }}
          </q-td>
        </template>
      </q-table>
      <!-- !PERSONAL ACCESS TOKENS-->

      <confirm-dialog
        v-if="selectedToken"
        v-model="showDelete"
        :title="$t('delete')"
        :text="$t('delete_token_confirm', { token: selectedToken.name })"
        @confirm="doDeleteToken"
      />

      <update-password-dialog v-model="showUpdatePassword" :name="authStore.profile.principal || ''" />
      <add-token-dialog v-model="showAddToken" :type="tokenType" @update:modelValue="onTokenAdded"></add-token-dialog>
    </div>
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'ProfileUser',
});
</script>

<script setup lang="ts">
import { onMounted } from 'vue';
import { SubjectProfileDto, SubjectTokenDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';
import { getDateLabel } from 'src/utils/dates';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import UpdatePasswordDialog from 'src/components/admin/profiles/user/UpdatePasswordDialog.vue';
import AddTokenDialog from 'src/components/admin/profiles/user/AddTokenDialog.vue';

const loading = ref(false);
const authStore = useAuthStore();
const profilesStore = useProfilesStore();
const tokensStore = useTokensStore();
const profile = computed(() => profilesStore.profile || ({} as SubjectProfileDto));
const tokens = computed(() => tokensStore.tokens || ([] as SubjectTokenDto[]));
const otpQrCode = ref<string | null>(null);
const { t } = useI18n();
const toolsVisible = ref<{ [key: string]: boolean }>({});
const selectedToken = ref<SubjectTokenDto | null>(null);
const showDelete = ref(false);
const showUpdatePassword = ref(false);
const showAddToken = ref(false);
const tokenType = ref(TOKEN_TYPES.DATASHIELD);
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
    headerStyle: 'width: 25%;',
  },
  {
    name: 'projects',
    label: t('projects'),
    align: 'left',
    field: 'projects',
    format: (values: string[]) => (values || []).join(', '),
    headerStyle: 'width: 40%; white-space: normal;',
    style: 'width: 40%; white-space: normal;',
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
    align: 'left  ',
    field: 'commands',
    format: (values: string[]) => (values || []).map((val) => t(`command_types.${val}`)).sort(),
    headerStyle: 'width: 40%; white-space: normal;',
    style: 'width: 40%; white-space: normal;',
  },
  {
    name: 'services',
    label: t('services'),
    align: 'left  ',
    field: (row: SubjectTokenDto) => getServicesField(row),
    format: (values: string[]) => values.map((val) => t(`token_services.${val}`)).sort(),
  },
  {
    name: 'inactive',
    label: t('inactive'),
    align: 'left  ',
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

      await fetchData();
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

function onOverRow(row: SubjectTokenDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: SubjectTokenDto) {
  toolsVisible.value[row.name] = false;
}

function onUpdatePassword() {
  showUpdatePassword.value = true;
}

function onDeleteToken(token: SubjectTokenDto) {
  selectedToken.value = token;
  showDelete.value = true;
}

function onAddDataShieldToken() {
  tokenType.value = TOKEN_TYPES.DATASHIELD;
  showAddToken.value = true;
}

function onAddRToken() {
  tokenType.value = TOKEN_TYPES.R;
  showAddToken.value = true;
}

function onAddSqlToken() {
  tokenType.value = TOKEN_TYPES.SQL;
  showAddToken.value = true;
}

function onAddCustomToken() {
  tokenType.value = TOKEN_TYPES.CUSTOM;
  showAddToken.value = true;
}

function onTokenAdded() {
  tokenType.value = TOKEN_TYPES.DATASHIELD;
  showAddToken.value = false;
  fetchData();
}

async function doDeleteToken() {
  showDelete.value = false;
  if (selectedToken.value === null) {
    return;
  }

  const toDelete: SubjectTokenDto | null = selectedToken.value;
  selectedToken.value = null;

  try {
    await tokensStore.deleteToken(toDelete.name);
    await fetchData();
  } catch (err) {
    notifyError(err);
  }
}

async function fetchData() {
  return Promise.all([profilesStore.initProfile(), tokensStore.initTokens()]).catch(notifyError);
}

onMounted(() => {
  fetchData();
});
</script>
