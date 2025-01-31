<template>
  <div v-show="!loading">
    <div>
      <!-- Account -->
      <div class="text-h6">{{ t('account') }}</div>
      <div>
        <span>{{ t('user_profile.groups', { count: (profile?.groups || []).length }) }}</span>
        <q-chip v-for="group in profile?.groups" :key="group">
          {{ group }}
        </q-chip>
      </div>
      <div class="q-mb-md">
        <q-btn
          no-caps
          v-if="isOpalUserRealm"
          color="primary"
          size="sm"
          :label="t('user_profile.update_password')"
          @click="onUpdatePassword"
        />
        <div v-else-if="profile?.accountUrl">
          <q-btn
            no-caps
            color="primary"
            size="sm"
            icon-right="open_in_new"
            :label="t('user_profile.external_account')"
            @click="onAccountLink"
          />
        </div>
        <div v-else square class="box-info q-mt-md" text-color="white" icon="warning">
          <q-icon name="info" size="1.2rem" />
          <span class="on-right">{{ t('user_profile.password_update_not_allowed', { realm: profile?.realm }) }}</span>
        </div>
      </div>
      <!-- !Account -->

      <!-- 2FA -->
      <div v-if="isAnOpalRealm">
        <div class="text-h6 q-mt-lg">{{ t('2fa.title') }}</div>
        <div
          class="text-help q-mb-md"
          v-html="
            t('user_profile.2fa_info', {
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
          size="sm"
          :label="profile?.otpEnabled ? t('user_profile.disable_2fa') : t('user_profile.enable_2fa')"
          @click="onToggleOtp"
        />

        <q-card v-if="profile?.otpEnabled && otpQrCode" bordered flat class="bg-grey-3">
          <q-card-section>
            <div class="text-help">{{ t('user_profile.otp_qr_core_info') }}</div>
          </q-card-section>
          <q-card-section>
            <div class="text-center"><img :src="otpQrCode" alt="QR Code" /></div>
          </q-card-section>
        </q-card>
      </div>
      <!--!2FA -->

      <!-- PERSONAL ACCESS TOKENS-->
      <div class="text-h6 q-mt-lg">{{ t('user_profile.personal_access_tokens') }}</div>
      <div class="text-help q-mb-md">{{ t('user_profile.tokens_info') }}</div>
      <q-btn-dropdown no-caps color="primary" :title="t('user_profile.add_token')" icon="add" size="sm">
        <q-list>
          <q-item clickable v-close-popup @click.prevent="onAddDataShieldToken">
            <q-item-section>
              <q-item-label>{{ t('user_profile.add_datashield_token') }}</q-item-label>
            </q-item-section>
          </q-item>
          <q-item clickable v-close-popup @click.prevent="onAddRToken">
            <q-item-section>
              <q-item-label>{{ t('user_profile.add_r_token') }}</q-item-label>
            </q-item-section>
          </q-item>
          <q-item clickable v-close-popup @click.prevent="onAddSqlToken">
            <q-item-section>
              <q-item-label>{{ t('user_profile.add_sql_token') }}</q-item-label>
            </q-item-section>
          </q-item>
          <q-item clickable v-close-popup @click.prevent="onAddCustomToken">
            <q-item-section>
              <q-item-label>{{ t('user_profile.add_custom_token') }}</q-item-label>
            </q-item-section>
          </q-item>
        </q-list>
      </q-btn-dropdown>

      <div v-if="tokens.length" class="q-mt-md q-mb-md">
        <q-table
          flat
          :rows="tokens"
          :columns="columns"
          row-key="name"
          :pagination="initialPagination"
          :hide-pagination="tokens.length <= initialPagination.rowsPerPage"
          :loading="loading"
        >
          <template v-slot:body-cell-projects="props">
            <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
              <q-chip
                dense
                square
                class="q-ml-none"
                v-for="project in props.col.format(props.row.projects)"
                :key="project"
              >
                {{ project }}
              </q-chip>
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
                  :title="t('renew')"
                  :icon="toolsVisible[props.row.name] ? 'restore' : 'none'"
                  class="q-ml-xs"
                  @click="onRenewToken(props.row)"
                />
                <q-btn
                  rounded
                  dense
                  flat
                  size="sm"
                  color="secondary"
                  :title="t('delete')"
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
          <template v-slot:body-cell-administration="props">
            <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
              <q-chip
                dense
                class="q-ml-none"
                v-for="admin in props.col.format(props.col.field(props.row))"
                :key="admin"
              >
                {{ admin }}
              </q-chip>
            </q-td>
          </template>
          <template v-slot:body-cell-inactive="props">
            <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
              <span :title="getDateLabel(props.row.inactiveAt)" :class="props.row.inactive ? 'text-negative' : ''">{{
                getDateDistanceLabel(props.row.inactiveAt)
              }}</span>
            </q-td>
          </template>
          <template v-slot:body-cell-expires="props">
            <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
              <span v-if="props.row.expiresAt" :title="getDateLabel(props.row.expiresAt)">{{
                getDateDistanceLabel(props.row.expiresAt)
              }}</span>
              <span v-else class="text-help">-</span>
            </q-td>
          </template>
        </q-table>
      </div>
      <div v-else class="text-hint q-mt-md q-mb-md">{{ t('user_profile.no_tokens') }}</div>
      <!-- !PERSONAL ACCESS TOKENS-->

      <!-- Dialogs -->
      <confirm-dialog
        v-if="selectedToken"
        v-model="showDelete"
        :title="t('delete')"
        :text="t('delete_token_confirm', { token: selectedToken.name })"
        @confirm="doDeleteToken"
      />

      <update-password-dialog v-model="showUpdatePassword" :name="authStore.profile.principal || ''" />

      <add-token-dialog
        v-model="showAddToken"
        :type="tokenType"
        :names="tokenNames"
        @added="onTokenAdded"
      ></add-token-dialog>

      <q-dialog v-if="tokenAdded" v-model="showTokenAdded" position="bottom" @hide="onTokenAddedHide">
        <q-card>
          <q-card-section class="bg-positive">
            <div class="text-grey-4 q-mb-sm">{{ t('added_token') }}</div>
            <div class="row">
              <div class="text-white text-bold">{{ tokenAdded?.token }}</div>
              <q-btn
              flat
              dense
              size="sm"
              icon="content_copy"
              color="white"
              :title="t('clipboard.copy')"
              @click="onCopyToClipboard"
              aria-label="Copy to clipboard"
              class="q-ml-sm"
            />
            </div>
            
          </q-card-section>
        </q-card>
      </q-dialog>
      <!-- !Dialogs -->
    </div>
  </div>
</template>

<script setup lang="ts">
import { copyToClipboard } from 'quasar';
import type { SubjectProfileDto, SubjectTokenDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import UpdatePasswordDialog from 'src/components/profile/UpdatePasswordDialog.vue';
import AddTokenDialog from 'src/components/profile/AddTokenDialog.vue';
import { getDateLabel, getDateDistanceLabel } from 'src/utils/dates';
import { DefaultAlignment } from 'src/components/models';

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
const showTokenAdded = ref(false);
const tokenType = ref(TOKEN_TYPES.DATASHIELD);
const tokenAdded = ref<SubjectTokenDto | null>(null);
const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});
const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: DefaultAlignment,
    field: 'name',
    sortable: true,
    headerStyle: 'width: 25%;',
  },
  {
    name: 'projects',
    label: t('projects'),
    align: DefaultAlignment,
    field: 'projects',
    format: (values: string[]) => ((values || []).length > 0 ? values : [t('user_profile.all_projects')]),
    headerStyle: 'width: 40%; white-space: normal;',
    style: 'width: 40%; white-space: normal;',
  },
  {
    name: 'access',
    label: t('data_access'),
    align: DefaultAlignment,
    field: 'access',
    format: (val: string) => t(`access.${val}`),
  },
  {
    name: 'commands',
    label: t('tasks'),
    align: DefaultAlignment,
    field: 'commands',
    format: (values: string[]) => (values || []).map((val) => t(`command_types.${val}`)).sort(),
    headerStyle: 'width: 40%; white-space: normal;',
    style: 'width: 40%; white-space: normal;',
  },
  {
    name: 'administration',
    label: t('administration'),
    align: DefaultAlignment,
    field: (row: SubjectTokenDto) => getAdministrationField(row),
    format: (values: string[]) => values.map((val) => t(`token_administration.${val}`)).sort(),
  },
  {
    name: 'services',
    label: t('services'),
    align: DefaultAlignment,
    field: (row: SubjectTokenDto) => getServicesField(row),
    format: (values: string[]) => values.map((val) => t(`token_services.${val}`)).sort(),
  },
  {
    name: 'inactive',
    label: t('inactive'),
    align: DefaultAlignment,
    field: 'inactiveAt',
    format: (val: string) => getDateLabel(val),
  },
  {
    name: 'expires',
    label: t('expires'),
    align: DefaultAlignment,
    field: 'expiresAt',
    format: (val: string) => getDateLabel(val),
  },
]);

const tokenNames = computed(() => tokens.value.map((t) => t.name));
const otpIcon = computed(() => (profile.value?.otpEnabled ? 'lock_open' : 'lock'));
const realms = computed(() => profile.value?.realm?.split(',') || []);
const isOpalUserRealm = computed(() => realms.value?.includes('opal-user-realm'));
const isAnOpalRealm = computed(
  () => profile.value && (realms.value?.includes('opal-user-realm') || realms.value?.includes('opal-ini-realm'))
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

function getServicesField(row: SubjectTokenDto): string[] {
  const services: string[] = [];
  if (row.useR) services.push('useR');
  if (row.useDatashield) services.push('useDatashield');
  if (row.useSQL) services.push('useSQL');
  if (row.sysAdmin) services.push('sysAdmin');
  return services;
}

function getAdministrationField(row: SubjectTokenDto): string[] {
  const admin: string[] = [];
  if (row.createProject) admin.push('createProject');
  if (row.updateProject) admin.push('updateProject');
  if (row.deleteProject) admin.push('deleteProject');
  return admin;
}

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

async function onRenewToken(token: SubjectTokenDto) {
  try {
    await tokensStore.renewToken(token.name);
    await fetchData();
  } catch (err) {
    notifyError(err);
  }
}

async function fetchData() {
  return Promise.all([profilesStore.initProfile(), tokensStore.initTokens()])
    .then(() => {
      authStore.profile = profile.value;
    })
    .catch(notifyError);
}

// Hook and event handlers
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

function onTokenAdded(token: SubjectTokenDto) {
  if (token.token) {
    tokenAdded.value = token;
    showTokenAdded.value = true;
  }
  fetchData();
}

onMounted(() => {
  loading.value = true;
  fetchData().then(() => {
    loading.value = false;
  });
});

function onAccountLink() {
  window.open(profile.value?.accountUrl, '_blank');
}

function onCopyToClipboard() {
  if (tokenAdded.value && tokenAdded.value.token) {
    copyToClipboard(tokenAdded.value.token);
    onTokenAddedHide();
  }
}

function onTokenAddedHide() {
  showTokenAdded.value = false;
  tokenAdded.value = null;
}

</script>
