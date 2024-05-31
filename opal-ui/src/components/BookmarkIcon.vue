<template>
  <q-icon
    :name="name"
    :color="color"
    :title="$t(title)"
    size="sm"
    class="on-right"
    @click="authStore.toggleBookmark(props.resource)"
  />
</template>


<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'BookmarkIcon',
});
</script>
<script setup lang="ts">
interface BookmarkIconProps {
  resource: string;
}

const props = defineProps<BookmarkIconProps>();

const authStore = useAuthStore();

const name = computed(() => authStore.isBookmarked(props.resource) ? 'star' : 'star_outline');

const color = computed(() => authStore.isBookmarked(props.resource) ? 'warning' : 'grey-5');

const title = computed(() => authStore.isBookmarked(props.resource) ? 'bookmark_remove' : 'bookmark_add');
</script>
