<template>
  <div v-if="kind">
    <div v-if="tooLarge" class="text-hint q-pa-md">{{ t('file_too_large_preview') }}</div>
    <q-spinner-dots v-else-if="loading" size="lg" class="q-ma-md" />
    <q-card flat bordered>
      <q-card-section class="q-pa-none">
        <q-markdown v-if="kind === 'md'" class="q-pa-md" :src="text" no-heading-anchor-links />
        <q-table
          v-else-if="kind === 'csv'"
          flat
          :rows="table.rows"
          :columns="table.columns"
          :pagination="{ rowsPerPage: 20 }"
        />
        <div v-else-if="kind === 'txt'" class="relative-position">
          <pre class="q-pa-md q-ma-none text-content">{{ text }}</pre>
          <q-btn
            flat
            dense
            round
            size="sm"
            icon="content_copy"
            :title="t('copy')"
            class="absolute-top-right q-ma-sm"
            @click="onCopyText"
          />
        </div>
        <iframe v-else-if="kind === 'pdf' && blobUrl" :src="blobUrl" :title="file.name" class="pdf-frame" />
        <div v-else-if="kind === 'img' && blobUrl" class="text-center q-pa-md">
          <img :src="blobUrl" :alt="file.name" class="image-content" />
        </div>
      </q-card-section>
    </q-card>
  </div>
</template>

<script setup lang="ts">
import Papa from 'papaparse';
import type { FileDto } from 'src/models/Opal';
import { DefaultAlignment } from 'src/components/models';
import { notifyError, notifyInfo } from 'src/utils/notify';
import { copyToClipboard } from 'quasar';

const props = defineProps<{ file: FileDto }>();

const { t } = useI18n();
const filesStore = useFilesStore();

// max file size (bytes) per previewable kind
const MAX_SIZES = { md: 1024 * 1024, csv: 2 * 1024 * 1024, pdf: 20 * 1024 * 1024, txt: 1024 * 1024, img: 10 * 1024 * 1024 };
// raw text files, displayed as is
const TEXT_EXTENSIONS = [
  'txt', 'log', 'json', 'geojson', 'xml', 'yaml', 'yml', 'toml', 'ini', 'conf', 'properties',
  'r', 'rmd', 'qmd', 'py', 'jl', 'sh', 'sql', 'js', 'ts', 'html', 'css', 'sas', 'sps', 'do',
];
const EXTENSIONS: Record<string, keyof typeof MAX_SIZES> = {
  md: 'md',
  csv: 'csv',
  tsv: 'csv',
  pdf: 'pdf',
  ...Object.fromEntries(['jpg', 'jpeg', 'gif', 'png', 'webp'].map((ext) => [ext, 'img' as const])),
  ...Object.fromEntries(TEXT_EXTENSIONS.map((ext) => [ext, 'txt' as const])),
};

const loading = ref(false);
const text = ref('');
const blobUrl = ref<string>();

const kind = computed(() => EXTENSIONS[props.file.name.split('.').pop()?.toLowerCase() ?? '']);
const tooLarge = computed(() => !!kind.value && (props.file.size ?? 0) > MAX_SIZES[kind.value]);

const table = computed(() => {
  // delimiter is auto-detected by papaparse
  const data = Papa.parse<string[]>(text.value, { skipEmptyLines: true }).data;
  const header = data[0] ?? [];
  return {
    columns: header.map((label, i) => ({ name: `${i}`, label, field: (row: string[]) => row[i], align: DefaultAlignment })),
    rows: data.slice(1),
  };
});

function onCopyText() {
  copyToClipboard(text.value)
    .then(() => notifyInfo(t('text_copied')))
    .catch(notifyError);
}

function revokeBlob() {
  if (blobUrl.value) URL.revokeObjectURL(blobUrl.value);
  blobUrl.value = undefined;
}

watch(
  () => props.file.path,
  async (path) => {
    revokeBlob();
    text.value = '';
    if (!kind.value || tooLarge.value) return;
    loading.value = true;
    try {
      const blob = await filesStore.getFileContent(path);
      if (path !== props.file.path) return; // file changed while loading
      if (kind.value === 'pdf') blobUrl.value = URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
      else if (kind.value === 'img') blobUrl.value = URL.createObjectURL(blob);
      else text.value = await blob.text();
    } catch (err) {
      notifyError(err);
    } finally {
      loading.value = false;
    }
  },
  { immediate: true },
);

onUnmounted(revokeBlob);
</script>

<style scoped>
.text-content {
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 80vh;
  overflow: auto;
}
.image-content {
  max-width: 100%;
  max-height: 80vh;
}
.pdf-frame {
  width: 100%;
  height: 80vh;
  border: none;
}
</style>
