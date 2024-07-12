<template>
    <span>Hello there!</span>
</template>

<script lang="ts">
export default defineComponent({
  name: 'TaxonomiesList',
});
</script>

<script setup lang="ts">
import { onMounted } from 'vue';
import { notifyError } from 'src/utils/notify';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';

const taxonomiesStore = useTaxonomiesStore();
const { t } = useI18n();
const loading = ref(false);


</script>
