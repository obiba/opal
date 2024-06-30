<template>
  <div>
    <q-list separator>
      <q-item-label header class="text-uppercase">{{ $t('bookmarks') }}</q-item-label>
      <q-item
        v-for="item in items"
        :key="item.link"
      >
        <q-item-section>
          <q-item-label><router-link :to="item.link">{{ item.title }} </router-link></q-item-label>
          <q-item-label caption lines="2">{{ $t(item.caption) }}</q-item-label>
        </q-item-section>
        <q-item-section avatar>
          <bookmark-icon :resource="item.resource" />
        </q-item-section>
      </q-item>
      <q-item v-if="items.length === 0">
        <q-item-section>
          <q-item-label class="text-hint">{{ $t('no_bookmarks') }}</q-item-label>
        </q-item-section>
      </q-item>
    </q-list>
    <div  class="text-help">

    </div>
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'BookmarksList',
});
</script>
<script setup lang="ts">
import { BookmarkDto, BookmarkDto_ResourceType } from 'src/models/Opal';
import BookmarkIcon from 'src/components/BookmarkIcon.vue';
const authStore = useAuthStore();

const items = computed(() => authStore.bookmarks.map((bookmark) => ({
  title: getTitle(bookmark),
  caption: bookmark.type.toLowerCase(),
  link: getLink(bookmark),
  resource: bookmark.resource,
})));

function getTitle(bookmark: BookmarkDto) {
  const title = bookmark.links.find((link) => link.rel === bookmark.resource).link;
  if (bookmark.type === BookmarkDto_ResourceType.TABLE) {
    const dsName = bookmark.links.find((link) => link.rel !== bookmark.resource).link;
    return `${dsName}.${title}`;
  }
  return title;
}

function getLink(bookmark: BookmarkDto) {
  const link = bookmark.resource;
  if (bookmark.type === BookmarkDto_ResourceType.TABLE)
    return link.replace(/\/datasource\//g, '/project/')
  return link;
}
</script>
