<template>
  <div class="row q-mt-lg">
    <div>
      <div class="text-subtitle1" :class="{ 'text-primary': group.length > 0, 'text-secondary': group.length < 1 }">{{
        t(title)
      }}</div>
      <div class="text-hint">{{ t(hint) }}</div>
      <q-option-group size="sm" :options="groupOptions" type="checkbox" v-model="group" />
    </div>
  </div>
</template>

<script setup lang="ts">
interface GroupProps {
  modelValue: string[] | undefined;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  groupOptions: { [props: string]: any; label: string; value: any; disable?: boolean }[];
  title: string;
  hint: string;
}

const { t } = useI18n();

const props = defineProps<GroupProps>();
const emit = defineEmits(['update:modelValue']);

const group = computed({
  get: () => props.modelValue || [],
  set: (value) => emit('update:modelValue', value),
});
</script>
