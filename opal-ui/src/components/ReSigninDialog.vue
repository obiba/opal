<template>
  <q-dialog v-model="showDialog" backdrop-filter="blur(5px)" @hide="onHide">
    <signin-panel @signed-in="onConfirm" />
  </q-dialog>
</template>

<script setup lang="ts">
import SigninPanel from 'src/components/SigninPanel.vue';

interface DialogProps {
  modelValue: boolean;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'confirm', 'cancel']);

const showDialog = ref(props.modelValue);

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
  },
);

function onHide() {
  emit('update:modelValue', false);
}

function onConfirm() {
  emit('confirm', true);
  onHide();
}
</script>
