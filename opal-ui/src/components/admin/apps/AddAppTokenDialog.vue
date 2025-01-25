<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('apps.edit_token') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section class="row items-center q-gutter-sm">
        <div class="col-8">
          <q-input clearable v-model="model" dense type="text" :label="t('token')" class="q-mb-md" lazy-rules>
          </q-input>
        </div>
        <div class="col-auto">
          <q-btn
            class="on-right"
            size="sm"
            icon="cached"
            color="primary"
            :label="t('generate')"
            @click="onGenerateToken"
          ></q-btn>
        </div>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('edit')" type="submit" color="primary" @click="onEdit" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { generateToken } from 'src/utils/tokens';

const { t } = useI18n();

interface DialogProps {
  modelValue: boolean;
  token: string;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'update']);
const showDialog = ref(props.modelValue);
const model = ref('');

// Handlers

function onHide() {
  model.value = '';
  showDialog.value = false;
  emit('update:modelValue', false);
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      if (props.token) {
        model.value = props.token;
      }

      showDialog.value = value;
    }
  }
);

function onGenerateToken() {
  model.value = generateToken();
}

async function onEdit() {
  emit('update', model.value);
  onHide();
}
</script>
