<template>
  <div class="row">
    <div class="col-12 col-md-6">
      <fields-list
        :items="items"
        :dbobject="systemStore.generalConf"
      />
    </div>
  </div>
</template>


<script lang="ts">
export default defineComponent({
  name: 'GeneralConfiguration',
});
</script>
<script setup lang="ts">
import FieldsList, { FieldItem } from 'src/components/FieldsList.vue';
import { StringMap } from 'src/components/models';

const items: FieldItem<StringMap>[] = [
  { field: 'name' },
  { field: 'publicURL', label: 'public_url', html: (val) => val.publicURL ? `<a href="${val.publicURL}" target="_blank">${val.publicURL}</a>` : '-' },
  { field: 'logoutURL', label: 'logout_url', html: (val) => val.logoutURL ? `<a href="${val.logoutURL}" target="_blank">${val.logoutURL}</a>` : '-' },
  { field: 'languages', label: 'laguages', format: (val) => val.languages ? (val.languages as string[]).join(', ') : '-' },
  { field: 'defaultCharSet', label: 'default_charset' },
  { field: 'enforced2FA', label: 'enforced_2fa' },

];

const systemStore = useSystemStore();

</script>
