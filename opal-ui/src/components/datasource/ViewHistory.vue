<template>
  <div>
    <div class="row q-col-gutter-md">
      <div class="col-md-8 col-xs-12">
        <div class="text-h6">{{ t('history') }}</div>
        <!-- the history of the view folder: the view, the entity filter and the variable scripts -->
        <git-history
          :key="historyKey"
          :name="datasourceStore.table.name || ''"
          :load-commits="datasourceStore.getViewCommits"
          :diff="datasourceStore.getViewCommit"
          :diff-with-head="datasourceStore.getViewCommitFromHead"
          :restore="noRestore"
          :can-restore="false"
        />
      </div>
      <div class="col-md-4 col-xs-12">
        <div class="text-h6">{{ t('tags') }}</div>
        <q-btn color="primary" icon="add" :label="t('git.add_tag')" size="sm" class="q-mt-sm" @click="onShowAddTag" />
        <q-list separator class="q-mt-sm">
          <q-item v-for="tag in tags" :key="tag.name" class="q-px-none">
            <q-item-section>
              <q-item-label>
                <q-badge color="primary"><q-icon name="sell" size="xs" class="q-mr-xs" />{{ tag.name }}</q-badge>
              </q-item-label>
              <q-item-label v-if="tag.message">{{ tag.message }}</q-item-label>
              <q-item-label caption>{{ tag.author }} {{ tag.date ? getDateLabel(tag.date) : '' }}</q-item-label>
            </q-item-section>
            <q-item-section side>
              <div class="row no-wrap">
                <q-btn
                  rounded
                  dense
                  flat
                  size="sm"
                  color="secondary"
                  :title="t('download')"
                  icon="download"
                  @click="datasourceStore.downloadViewTag(tag.name)"
                />
                <q-btn
                  rounded
                  dense
                  flat
                  size="sm"
                  color="secondary"
                  :title="t('restore')"
                  icon="replay"
                  @click="onRestore(tag)"
                />
              </div>
            </q-item-section>
          </q-item>
        </q-list>
      </div>
    </div>

    <q-dialog v-model="showAddTag">
      <q-card class="dialog-sm">
        <q-card-section>
          <div class="text-h6">{{ t('git.add_tag') }}</div>
        </q-card-section>
        <q-separator />
        <q-card-section>
          <q-form ref="formRef">
            <q-input
              v-model="tagName"
              dense
              :label="t('name') + ' *'"
              :hint="t('git.tag_name_hint')"
              :rules="[validateTagName]"
              lazy-rules
              autofocus
              class="q-mb-md"
            />
            <q-input v-model="tagMessage" dense :label="t('message')" type="textarea" autogrow />
          </q-form>
        </q-card-section>
        <q-separator />
        <q-card-actions align="right" class="bg-grey-3">
          <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
          <q-btn flat :label="t('add')" color="primary" @click="onAddTag" />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <confirm-dialog
      v-model="showRestore"
      :title="t('restore')"
      :text="t('git.restore_view_confirm', { tag: tagToRestore?.name })"
      @confirm="doRestore"
    />
  </div>
</template>

<script setup lang="ts">
import type { QForm } from 'quasar';
import GitHistory from 'src/components/git/GitHistory.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import type { VcsTagInfoDto } from 'src/models/Opal';
import { getDateLabel } from 'src/utils/dates';
import { notifyError } from 'src/utils/notify';

const { t } = useI18n();
const datasourceStore = useDatasourceStore();

const tags = ref<VcsTagInfoDto[]>([]);
// remount the history, so reload the commits, after a tag or a restore
const historyKey = ref(0);
const showAddTag = ref(false);
const formRef = ref<QForm>();
const tagName = ref('');
const tagMessage = ref('');
const showRestore = ref(false);
const tagToRestore = ref<VcsTagInfoDto>();

// the history rows are not restored, only the tags
const noRestore = () => Promise.resolve();

onMounted(loadTags);

async function loadTags() {
  try {
    // most recent first, the dates are ISO 8601 strings
    tags.value = ((await datasourceStore.getViewTags()).tagInfos || []).sort((a, b) =>
      (b.date || '').localeCompare(a.date || ''),
    );
  } catch (error) {
    notifyError(error);
  }
}

// git rejects these characters in a tag name, the server also checks the other git rules
function validateTagName(val: string) {
  return /^[^\s~^:?*[\\]+$/.test(val || '') || t('git.tag_name_invalid');
}

function onShowAddTag() {
  tagName.value = '';
  tagMessage.value = '';
  showAddTag.value = true;
}

async function onAddTag() {
  if (!(await formRef.value?.validate())) return;
  try {
    await datasourceStore.createViewTag(tagName.value, tagMessage.value || undefined);
    showAddTag.value = false;
    await loadTags();
    historyKey.value++;
  } catch (error) {
    notifyError(error);
  }
}

function onRestore(tag: VcsTagInfoDto) {
  tagToRestore.value = tag;
  showRestore.value = true;
}

async function doRestore() {
  showRestore.value = false;
  const tag = tagToRestore.value;
  if (!tag || !datasourceStore.table.name) return;
  try {
    await datasourceStore.restoreViewCommit(tag.commitId);
    await datasourceStore.loadTable(datasourceStore.table.name);
    await loadTags();
    historyKey.value++;
  } catch (error) {
    notifyError(error);
  }
}
</script>
