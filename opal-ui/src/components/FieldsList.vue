<template>
  <q-list v-if="dbobject" separator dense>
    <q-item v-for="item in visibleItems" :key="item.field">
      <q-item-section>
        <q-item-label overline>
          {{ $t(item.label ? item.label : item.field) }}
        </q-item-label>
      </q-item-section>
      <q-item-section>
        <q-item-label>
          <span v-if="item.html" v-html="item.html(dbobject)"></span>
          <span v-else-if="item.format">{{ item.format(dbobject) }}</span>
          <span v-else>
            {{
              dbobject[item.field] !== undefined
                ? typeof dbobject[item.field] === 'number'
                  ? toMaxDecimals(dbobject[item.field], 3)
                  : dbobject[item.field]
                : '-'
            }}
          </span>
          {{ item.unit }}
        </q-item-label>
      </q-item-section>
    </q-item>
  </q-list>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { toMaxDecimals } from 'src/utils/numbers';
export default defineComponent({
  name: 'FieldsList',
});
</script>
<script setup lang="ts">
import { withDefaults } from 'vue';
import { Table, Variable } from 'src/components/models';

export interface FieldItem<T> {
  field: string;
  label?: string;
  unit?: string;
  format?: (val: T) => string;
  html?: (val: T) => string;
  visible?: (val: T) => boolean;
}

export interface FieldsListProps {
  dbobject: Table | Variable;
  items: FieldItem<Table | Variable>[];
}

const props = withDefaults(defineProps<FieldsListProps>(), {
  dbobject: undefined,
  items: undefined,
});

const visibleItems = computed(() => {
  return props.items.filter((item) => {
    if (item.visible) {
      return item.visible(props.dbobject);
    }
    return true;
  });
});
</script>
./models
