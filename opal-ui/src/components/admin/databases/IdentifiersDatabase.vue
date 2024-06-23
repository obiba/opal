<template>
  <div>
    <pre>{{ database }}</pre>
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'DatabasesList',
});
</script>
<script setup lang="ts">
import { DatabaseDto } from 'src/models/Database';
const systemStore = useSystemStore();

const database = ref<DatabaseDto>();

onMounted(() => {
  refresh();
});

function refresh() {
  systemStore.getIdentifiersDatabase().then((data) => {
    database.value = data;
  });
}

</script>
