<template>
  <div>
    <slot name="header"></slot>
    <span
      v-if="!$slots.header && !!title"
      class="text-subtitle1"
      :class="{ 'text-primary': dirty, 'text-secondary': !dirty }"
      >{{ title }}</span
    >

    <q-card flat class="bg-grey-2" >
      <q-card-section>
        <div v-for="label in labels" :key="label.locale" class="row q-col-gutter-md q-mb-md">
          <q-input v-model="label.locale" dense type="text" :label="$t('locale')" :debounce="500" style="width: 80px" />
          <q-input v-model="label.text" dense type="text" :label="$t('value')" style="min-width: 290px" />
          <span class="q-mt-md">
            <q-btn
              flat
              size="sm"
              color="negative"
              icon="delete"
              @click="labels = labels.filter((l) => l.locale !== label.locale)"
              class="on-right"
            />
          </span>
        </div>
        <q-btn
          size="sm"
          color="primary"
          icon="add"
          :label="labels.length ? '' : $t('add')"
          @click="labels = labels.concat({ locale: '', text: '' })"
        />
      </q-card-section>
    </q-card>
    <span v-if="!!hint" class="text-caption text-secondary q-pa-none">{{ hint }}</span>
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'LocalizedField',
});
</script>

<script setup lang="ts">
import { LocaleTextDto } from 'src/models/Opal';

interface Props {
  modelValue: LocaleTextDto[];
  title?: string;
  hint?: string;
}

// TODO: Add validation for correct locales

const emit = defineEmits(['update:modelValue']);
const props = defineProps<Props>();
const dirty = ref(false);
const labels = computed({
  get: () => props.modelValue || [],
  set: (value: LocaleTextDto[]) => {
    emit('update:modelValue', value);
  },
});

watch(
  labels,
  () => {
    dirty.value = labels.value.some((l) => l.text || l.locale);
  },
  { deep: true }
);
</script>
