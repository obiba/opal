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
import DOMPurify from 'isomorphic-dompurify';
import FieldsList, { type FieldItem } from 'src/components/FieldsList.vue';
import { type SubjectProfileDto } from 'src/models/Opal';
import { escapeAttribute, escapeHtml } from 'src/utils/strings';

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

/**
 * The claims come from the identity provider as the user set them there: they are untrusted. A claim is rendered as
 * HTML only when it is a well-formed http(s) URL or an email address, with the text and the attributes escaped
 * separately and the assembled snippet sanitized against an allow-list. Anything else is rendered as text by the
 * fields list.
 */
const items = computed<FieldItem[]>(() => {
  if (!userInfo.value) {
    return [];
  }
  return Object.keys(userInfo.value).map((key) => {
    const item = {
      field: key,
    } as FieldItem;
    const value = userInfo.value ? userInfo.value[key] : null;
    if (typeof value !== 'string') {
      return item;
    }
    const url = asHttpUrl(value);
    if (url) {
      // if value is an url make it a link or an image
      if (['image', 'avatar', 'photo', 'picture'].includes(key.toLowerCase())) {
        item.html = () =>
          DOMPurify.sanitize(`<img src="${escapeAttribute(url)}" alt="${escapeAttribute(key)}" style="max-width: 100px; max-height: 100px;" />`, {
            ALLOWED_TAGS: ['img'],
            ALLOWED_ATTR: ['src', 'alt', 'style'],
          });
      } else {
        item.html = () =>
          DOMPurify.sanitize(`<a href="${escapeAttribute(url)}" target="_blank" rel="noopener noreferrer">${escapeHtml(value)}</a>`, {
            ALLOWED_TAGS: ['a'],
            ALLOWED_ATTR: ['href', 'target', 'rel'],
          });
      }
    } else if (/^[^\s@"'<>]+@[^\s@"'<>]+\.[^\s@"'<>]+$/.test(value)) {
      // if value is an email make it a mailto link
      item.html = () =>
        DOMPurify.sanitize(`<a href="mailto:${escapeAttribute(value)}">${escapeHtml(value)}</a>`, {
          ALLOWED_TAGS: ['a'],
          ALLOWED_ATTR: ['href'],
        });
    }
    return item;
  });
});

/**
 * The normalized form of the value when it is an absolute http(s) URL, null otherwise. The parser rejects a
 * malformed value and percent-encodes the characters that could break out of an attribute.
 */
function asHttpUrl(value: string): string | null {
  try {
    const url = new URL(value);
    return url.protocol === 'http:' || url.protocol === 'https:' ? url.href : null;
  } catch {
    return null;
  }
}

const items1 = computed(() => items.value.slice(0, Math.ceil(items.value.length / 2)));
const items2 = computed(() => items.value.slice(Math.ceil(items.value.length / 2)));

</script>