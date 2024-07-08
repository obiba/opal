<template>
  <div>
    <div class="q-mt-none q-mb-none q-pa-md text-bold text-grey-6">
      <span v-if="authStore.isAuthenticated">{{  username }}</span>
      <span v-else>&nbsp;</span>
    </div>
    <q-list>
      <q-item to="/admin/profile" v-if="authStore.isAuthenticated">
        <q-item-section avatar>
          <q-icon name="person" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $t('my_profile') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item to="/projects">
        <q-item-section avatar>
          <q-icon name="table_chart" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $t('projects') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item to="/files">
        <q-item-section avatar>
          <q-icon name="folder" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $t('files') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item to="/admin">
        <q-item-section avatar>
          <q-icon name="admin_panel_settings" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $t('administration') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item-label header>{{ $t('other_links') }}</q-item-label>
      <EssentialLink
        v-for="link in essentialLinks"
        :key="link.title"
        v-bind="link"
      />
      <q-item class="fixed-bottom text-caption">
        <div>
          {{ $t('main.powered_by') }}
          <a
            class="text-weight-bold"
            href="https://www.obiba.org/pages/products/opal"
            target="_blank"
            >OBiBa Opal</a
          >
          <span class="q-ml-xs" style="font-size: smaller">{{
            authStore.version
          }}</span>
        </div>
      </q-item>
    </q-list>
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'MainDrawer',
});
</script>
<script setup lang="ts">
import EssentialLink, {
  EssentialLinkProps,
} from 'components/EssentialLink.vue';
import { t } from 'src/boot/i18n';

const authStore = useAuthStore();

const username = computed(() =>
  authStore.profile.principal ? authStore.profile.principal : '?'
);

const essentialLinks: EssentialLinkProps[] = [
  {
    title: t('docs'),
    caption: t('documentation_cookbook'),
    icon: 'school',
    link: 'https://opaldoc.obiba.org',
  },
  {
    title: t('source_code'),
    caption: 'github.com/obiba/opal',
    icon: 'code',
    link: 'https://github.com/obiba/opal',
  },
];
</script>
