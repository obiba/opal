<template>
  <q-list separator v-if="bookmarks.length">
    <q-item-label header class="text-uppercase">{{ $t('bookmarks') }}</q-item-label>
    <q-item
      v-for="bookmark in bookmarks"
      :key="bookmark.link"
    >
      <q-item-section>
        <q-item-label><router-link :to="bookmark.link">{{ bookmark.title }}</router-link></q-item-label>
        <q-item-label caption lines="2">{{ bookmark.caption }}</q-item-label>
      </q-item-section>
    </q-item>
  </q-list>
</template>

<script lang="ts">
import { BookmarkDto, BookmarkDto_ResourceType } from 'src/models/Opal';
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'BookmarksList',
});
</script>
<script setup lang="ts">
const authStore = useAuthStore();

const bookmarks = computed(() => authStore.bookmarks.map((bookmark) => ({
  title: getTitle(bookmark),
  caption: bookmark.type,
  link: getLink(bookmark)
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
