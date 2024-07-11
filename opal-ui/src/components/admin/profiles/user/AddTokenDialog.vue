<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ $t(`user_profile.token_dialog.add_${type}_token`) }}</div>
      </q-card-section>

      <q-separator />
      <q-card-section style="max-height: 75vh" class="scroll">
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-input
            type="text"
            :label="$t('name') + ' *'"
            :hint="$t('user_profile.token_dialog.name_hint')"
            v-model="token.name"
            color="grey-10"
            lazy-rules
            :rules="[validateRequiredName]"
          >
          </q-input>
          <q-input
            type="text"
            :label="$t('token') + ' *'"
            :hint="$t('user_profile.token_dialog.name_hint')"
            v-model="token.token"
            color="grey-10"
            lazy-rules
          >
            <template v-slot:append>
              <q-btn flat icon="content_copy" @click="onCopyToClipboard" aria-label="Copy to clipboard" />
            </template>
          </q-input>

          <q-select
            v-model="token.projects"
            use-input
            use-chips
            multiple
            input-debounce="0"
            :options="projectFilters"
            @new-value="addProject"
            @filter="filterProjects"
          >
          </q-select>
        </q-form>
      </q-card-section>
      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="$t('update')" type="submit" color="primary" @click="onAddToken" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'AddTokenDialog',
});
</script>

<script setup lang="ts">
import { copyToClipboard, Notify } from 'quasar';
import { SubjectTokenDto /*, ProjectDto*/ } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';
import { generateToken } from 'src/utils/tokens';
import { generateName } from 'src/utils/strings';

interface DialogProps {
  modelValue: boolean;
  type: string;
  names: string[];
}

const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const formRef = ref();
const emit = defineEmits(['update:modelValue']);
const tokensStore = useTokensStore();
const projectsStore = useProjectsStore();

let projectsFilterOptions = Array<string>();
const projectFilters = ref(Array<string>());

const emptyToken: SubjectTokenDto = {
  name: '',
  projects: [],
  commands: [],
} as SubjectTokenDto;
const token = ref({ ...emptyToken } as SubjectTokenDto);

const { t } = useI18n();

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      console.log('Watch token Dlg', value);
      projectsStore.initProjects().then(() => {
        projectsFilterOptions = projectsStore.projects.map((p) => p.name);
        projectFilters.value = [...projectsFilterOptions];
      });

      token.value.token = generateToken();
      token.value.name = generateName(props.type, props.names);
      updateTokenType();
      showDialog.value = value;
    }
  }
);

const validateRequiredName = (val: string) => (val && val.trim().length > 0) || t('validation.token.name_required');

function updateTokenType() {
  switch (props.type) {
    case TOKEN_TYPES.DATASHIELD:
      token.value.useDatashield = true;
      break;
    case TOKEN_TYPES.R:
      token.value.useR = true;
      break;
    case TOKEN_TYPES.SQL:
      token.value.useSQL = true;
      break;
  }
}

function filterProjects(val: string, update: any) {
  update(() => {
    if (val.trim().length === 0) {
      projectFilters.value = [...projectsFilterOptions];
    } else {
      const needle = val.toLowerCase();
      projectFilters.value = projectFilters.value.filter((v) => v.toLowerCase().indexOf(needle) > -1);
    }
  });
}

function onCopyToClipboard() {
  copyToClipboard(token.value.token || '')
    .then(() => {
      Notify.create('Text copied to clipboard!');
    })
    .catch(() => {
      Notify.create('Failed to copy text');
    });
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function addProject(val: string, done: any) {
  if (val.trim().length > 0) {
    const modelValue = (token.value.projects || []).slice();
    if (modelValue.includes(val) === false) {
      modelValue.push(val);
    }

    done(null);
    token.value.projects = modelValue;
  }
}

function onHide() {
  token.value = { ...emptyToken };
  showDialog.value = false;
  projectsFilterOptions = [];
  projectFilters.value = [];
  emit('update:modelValue', false);
}

async function onAddToken() {
  const valid = await formRef.value.validate();
  if (valid) {
    try {
      await tokensStore.addToken(token.value);
      showDialog.value = false;
      emit('update:modelValue', false);
    } catch (error) {
      notifyError(error);
    }
  }
}
</script>
