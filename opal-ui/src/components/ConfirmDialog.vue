<template>
  <q-dialog v-model="showDialog" persistent @hide="onHide">
    <q-card>
      <q-card-section>
        <div class="text-h6">{{ props.title }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <span>{{ props.text }}</span>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="$t('cancel')" color="secondary" @click="onCancel" v-close-popup />
        <q-btn flat :label="$t('confirm')" color="primary" @click="onConfirm" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'ConfirmDialog',
});
</script>
<script setup lang="ts">
interface DialogProps {
  modelValue: boolean;
  title: string;
  text: string;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'confirm', 'cancel']);

const showDialog = ref(props.modelValue);

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
  }
);

function onHide() {
  emit('update:modelValue', false);
}

function onCancel() {
  emit('cancel', true);
}

function onConfirm() {
  emit('confirm', true);
}
</script>
