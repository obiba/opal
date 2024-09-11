<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('taxonomies')" to="/admin/taxonomies" />
        <template v-if="taxonomyName">
          <q-breadcrumbs-el :label="taxonomyName" />
        </template>
      </q-breadcrumbs>
    </q-toolbar>
  </div>
</template>

<script setup lang="ts">
import { notifyError } from 'src/utils/notify';

const route = useRoute();
const router = useRouter();
const taxonomiesStore = useTaxonomiesStore();
const taxonomyName = computed(() => route.params.name as string);
const summaries = computed(() => taxonomiesStore.summaries || []);

watch(taxonomyName, (name) => {
  if (name) {
    const path = `${route.path.replace(/\/$/, '')}/${summaries.value[0].name}`;
    router.replace(path);
  }
});

onMounted(() => {
  taxonomiesStore
    .initSummaries()
    .then(() => {
      if (!taxonomyName.value && summaries.value.length > 0) {
        const path = `${route.path.replace(/\/$/, '')}/${summaries.value[0].name}`;
        router.replace(path);
      }
    })
    .catch(notifyError);
});
</script>
