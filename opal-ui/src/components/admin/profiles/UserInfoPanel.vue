<template>
  <div v-if="userInfo">
    <div class="row q-col-gutter-lg">
      <div class="col-12 col-md-6">
        <fields-list :items="items1" :dbobject="userInfo" max-width="200" />
      </div>
      <div class="col-12 col-md-6">
        <fields-list :items="items2" :dbobject="userInfo" max-width="200" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import DOMPurify from 'dompurify';
import FieldsList, { type FieldItem } from 'src/components/FieldsList.vue';
import { type SubjectProfileDto } from 'src/models/Opal';

interface Props {
  profile: SubjectProfileDto;
}

const props = defineProps<Props>();

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function flattenObject(obj: Record<string, any>, parentKey = '', result: Record<string, any> = {}) {
  for (const [key, value] of Object.entries(obj)) {
    const newKey = parentKey ? `${parentKey}.${key}` : key;
    if (
      value !== null &&
      typeof value === 'object' &&
      !Array.isArray(value)
    ) {
      flattenObject(value, newKey, result);
    } else {
      result[newKey] = value;
    }
  }
  return result;
}

const userInfo = computed(() => {
  if (!props.profile.userInfo) {
    return null;
  }
  try {
    const info = JSON.parse(props.profile.userInfo);
    if (!info || typeof info !== 'object') {
      return null;
    }
    return flattenObject(info);
  } catch (e) {
    console.error('Failed to parse userInfo JSON', e);
    return null;
  }
});

const items = computed<FieldItem[]>(() => {
  if (!userInfo.value) {
    return [];
  }
  return Object.keys(userInfo.value).map((key) => {
    const item = {
      field: key,
    } as FieldItem;
    const value = userInfo.value ? userInfo.value[key] : null;
    // html sanitize value
    if (value === null || value === undefined) {
      return item;
    }
    const sanitizedValue = DOMPurify.sanitize(String(value));
    if (typeof value === 'string' && value.startsWith('http')) {
      // if value is an url make it a link or an image
      if (['image', 'avatar', 'photo', 'picture'].includes(key.toLowerCase())) {
        item.html = () => `<img src="${sanitizedValue}" alt="${DOMPurify.sanitize(key)}" style="max-width: 100px; max-height: 100px;" />`;
      } else {
        item.html = () => `<a href="${sanitizedValue}" target="_blank">${sanitizedValue}</a>`;
      }
    } else if (typeof value === 'string' && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
      // if value is an email make it a mailto link
      item.html = () => `<a href="mailto:${sanitizedValue}">${sanitizedValue}</a>`;
    }
    return item;
  });
});

const items1 = computed(() => items.value.slice(0, Math.ceil(items.value.length / 2)));
const items2 = computed(() => items.value.slice(Math.ceil(items.value.length / 2)));

</script>