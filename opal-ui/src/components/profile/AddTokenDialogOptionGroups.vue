<template>
  <div class="row q-mt-lg">
    <div>
      <span class="text-subtitle1" :class="{ 'text-primary': group.length > 0, 'text-secondary': group.length < 1 }">{{
        $t(title)
      }}</span>

      <q-option-group dense :options="groupOptions" type="checkbox" v-model="group" />
      <span class="text-caption text-secondary">{{ $t(hint) }}</span>
    </div>
  </div>
</template>
access

<script lang="ts">
export default defineComponent({
  name: 'AddTokenDialogOptionGroups.vue',
});
</script>

<script setup lang="ts">
interface GroupProps {
  modelValue: string[] | undefined;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  groupOptions: { [props: string]: any; label: string; value: any; disable?: boolean | undefined }[];
  title: string;
  hint: string;
}

const props = defineProps<GroupProps>();
const emit = defineEmits(['update:modelValue']);

const group = computed({
  get: () => props.modelValue || [],
  set: (value) => emit('update:modelValue', value),
});
</script>
