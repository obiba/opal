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
    <q-page class="q-pa-md">
      <taxonomy-content></taxonomy-content>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import TaxonomyContent from 'src/components/admin/taxonomies/TaxonomyContent.vue';
import { notifyError } from 'src/utils/notify';

const route = useRoute();
const router = useRouter();
const taxonomiesStore = useTaxonomiesStore();
const name = computed(() => route.params.name as string);

onMounted(() => {
  console.log('TaxonomiesPage mounted', taxonomiesStore.summaries);
  taxonomiesStore
    .initSummaries()
    .then(() => {
      if (!!name.value) {
        // fetch taxonomy content
      } else if (taxonomiesStore.summaries.length > 0) {
        router.replace(`${route.path.replace(/\/$/, '')}/${taxonomiesStore.summaries[1].name}`);
      }
    })
    .catch(notifyError);

  //
});
</script>
