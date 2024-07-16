<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('taxonomies')" to="/admin/taxonomies"/>
        <template v-if="name">
          <q-breadcrumbs-el :label="name" />
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
const taxonomy = computed(() => taxonomiesStore.taxonomy || null);
const name = computed(() => route.params.name as string);

onMounted(() => {
  taxonomiesStore
    .initSummaries()
    .then(() => {
      if (!name.value) {
        const path = `${route.path.replace(/\/$/, '')}/${taxonomiesStore.summaries[0].name}`;
        router.replace(path);
      }
    })
    .catch(notifyError);
});
</script>
