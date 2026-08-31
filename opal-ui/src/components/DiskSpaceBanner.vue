<template>
  <q-banner v-if="banner" dense :class="banner.classes" class="q-px-md">
    <template v-slot:avatar>
      <q-icon :name="banner.icon" />
    </template>
    <div>{{ banner.message }}</div>
    <div class="text-caption">
      <span v-for="(disk, idx) in lowDisks" :key="disk.path">
        <span v-if="idx > 0"> — </span>
        {{ disk.name }} ({{ disk.path }}): {{ getSizeLabel(disk.usable) }} {{ t('disk_space.free_of') }}
        {{ getSizeLabel(disk.total) }}
      </span>
    </div>
  </q-banner>
</template>

<script setup lang="ts">
import { OpalStatus_DiskLevel } from 'src/models/Opal';
import type { OpalStatus_DiskUsage } from 'src/models/Opal';
import { getSizeLabel } from 'src/utils/files';

// The server samples once a minute, so asking more often than that would only report the same number again.
const REFRESH_INTERVAL = 60000;

const { t } = useI18n();
const systemStore = useSystemStore();
const authStore = useAuthStore();

const level = ref<OpalStatus_DiskLevel>(OpalStatus_DiskLevel.UNKNOWN);
const disks = ref<OpalStatus_DiskUsage[]>([]);
let timer: ReturnType<typeof setInterval> | undefined;

// UNKNOWN is not the least severe level but outside the scale: a volume that could not be measured is not a reason to
// tell an administrator anything.
const lowDisks = computed(() =>
  disks.value.filter((disk) =>
    [OpalStatus_DiskLevel.WARN, OpalStatus_DiskLevel.DEGRADED, OpalStatus_DiskLevel.CRITICAL].includes(disk.level),
  ),
);

const banner = computed(() => {
  switch (level.value) {
    case OpalStatus_DiskLevel.CRITICAL:
      return {
        classes: 'bg-negative text-white',
        icon: 'error',
        message: t('disk_space.critical'),
      };
    case OpalStatus_DiskLevel.DEGRADED:
      return {
        classes: 'bg-negative text-white',
        icon: 'warning',
        message: t('disk_space.degraded'),
      };
    case OpalStatus_DiskLevel.WARN:
      return {
        classes: 'bg-warning text-dark',
        icon: 'warning',
        message: t('disk_space.warn'),
      };
    default:
      return undefined;
  }
});

onMounted(() => {
  refresh();
  timer = setInterval(refresh, REFRESH_INTERVAL);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});

function refresh() {
  if (!authStore.isAdministrator) return;
  systemStore
    .getStatus()
    .then((status) => {
      level.value = status.diskLevel || OpalStatus_DiskLevel.UNKNOWN;
      disks.value = status.disks || [];
    })
    .catch(() => {
      // the banner is a courtesy, and a failed poll is not worth a notification
      level.value = OpalStatus_DiskLevel.UNKNOWN;
    });
}
</script>
