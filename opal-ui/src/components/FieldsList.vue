<template>
  <q-list v-if="dbobject" separator dense class="fields-list">
    <!-- Makes a re-render when locale changes. For example, getDateLabel does not account for locale change hence this trick -->
    <template v-if="locale"></template>

    <q-item v-for="item in visibleItems" :key="item.field">
      <q-item-section :style="`max-width: ${maxWidth}px`">
        <q-item-label>
          <div class="text-overline text-grey-6">{{ t(item.label ? item.label : item.field) }}</div>
          <div v-if="item.hint" class="text-hint">{{ t(item.hint) }}</div>
        </q-item-label>
      </q-item-section>
      <q-item-section>
        <q-item-label>
          <span v-if="item.html" v-html="item.html(dbobject)"></span>
          <span v-else-if="item.format">{{ item.format(dbobject) }}</span>
          <span v-else-if="item.links">
            <div v-for="link in item.links(dbobject)" :key="link.to" class="text-caption">
              <router-link :to="link.to">
                <q-icon v-if="link.icon" :name="link.icon" class="q-mr-xs" />
                <span>{{ link.label }}</span>
                <q-icon v-if="link.iconRight" :name="link.iconRight" class="q-ml-xs" />
              </router-link>
            </div>
          </span>
          <span v-else-if="item.icon">
            <q-icon :name="item.icon(dbobject)" />
          </span>
          <span v-else>
            {{
              (dbobject as any)[item.field] !== undefined
                ? typeof (dbobject as any)[item.field] === 'number'
                  ? toMaxDecimals((dbobject as any)[item.field], 3)
                  : (dbobject as any)[item.field]
                : '-'
            }}
          </span>
          {{ item.unit }}
        </q-item-label>
      </q-item-section>
    </q-item>
    <q-separator />
  </q-list>
</template>

<script setup lang="ts">
import { toMaxDecimals } from 'src/utils/numbers';
import type { TableDto, VariableDto } from 'src/models/Magma';
import type { DescriptiveStatsDto } from 'src/models/Math';
import type { DataShieldProfileDto } from 'src/models/DataShield';
import type { ResourceFactoryDto } from 'src/models/Resources';
import type { StringMap } from 'src/components/models';

export interface FieldLink {
  label: string;
  to: string;
  icon?: string;
  iconRight?: string;
}

export interface FieldItem<T> {
  field: string;
  label?: string;
  hint?: string;
  unit?: string;
  format?: (val: T) => string | undefined;
  html?: (val: T) => string | undefined;
  visible?: (val: T) => boolean;
  links?: (val: T) => FieldLink[];
  icon?: (val: T) => string;
}

export interface FieldsListProps {
  dbobject?: TableDto | VariableDto | DescriptiveStatsDto | DataShieldProfileDto | StringMap | ResourceFactoryDto;
  items?: FieldItem<TableDto | VariableDto | DescriptiveStatsDto | DataShieldProfileDto | StringMap | ResourceFactoryDto>[];
  maxWidth: string;
}

const { t, locale } = useI18n({ useScope: 'global' });
const props = withDefaults(defineProps<FieldsListProps>(), {
  maxWidth: '200px',
});

const visibleItems = computed(() => {
  return props.items?.filter((item) => {
    if (item.visible && props.dbobject) {
      return item.visible(props.dbobject);
    }
    return true;
  });
});
</script>
