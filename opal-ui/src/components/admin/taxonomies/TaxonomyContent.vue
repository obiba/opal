<template>
  <pre>Taxonomy content</pre>
</template>

<script lang="ts">
export default defineComponent({
  name: 'TaxonomyContent',
});
</script>

<script setup lang="ts">
import { onMounted } from 'vue';
import { notifyError } from 'src/utils/notify';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';

const taxonomiesStore = useTaxonomiesStore();
const { t } = useI18n();
const loading = ref(false);


onMounted(() => {
  loading.value = true;
});
</script>
