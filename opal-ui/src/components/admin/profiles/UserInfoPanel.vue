<template>
  <div v-if="userInfo">
    <div class="row">
      <div class="col-12 col-md-6">
        <fields-list :items="items" :dbobject="userInfo" max-width="200" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
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
  const info = JSON.parse(props.profile.userInfo);
  return flattenObject(info);
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
    // if value is an url make it a link
    if (typeof value === 'string' && value.startsWith('http')) {
      if (['image', 'avatar', 'photo', 'picture'].includes(key.toLowerCase())) {
        item.html = (val) => `<img src="${val[key]}" alt="${key}" style="max-width: 100px; max-height: 100px;" />`;
      } else {
        item.html = (val) => `<a href="${val[key]}" target="_blank">${val[key]}</a>`;
      }
    }
    // if value is an email make it a mailto link
    else if (typeof value === 'string' && value.includes('@')) {
      item.html = (val) => `<a href="mailto:${val[key]}">${val[key]}</a>`;
    }
    return item;
  });
});

</script>