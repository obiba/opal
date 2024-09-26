import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { RActivitySummaryDto } from 'src/models/OpalR';

export const useProfileActivityStore = defineStore('profileActivity', () => {
  
  const rSummaries = ref<RActivitySummaryDto[]>([]);
  const datashieldSummaries = ref<RActivitySummaryDto[]>([]);
  
  const summaries = computed(() => [...rSummaries.value, ...datashieldSummaries.value].sort((a, b) => a.profile.localeCompare(b.profile)))

  function reset() {
    rSummaries.value = [];
    datashieldSummaries.value = [];
  }

  async function initSummaries(principal: string) {
    return Promise.all([
      api.get('/service/r/activity/_summary', { params: { context: 'R', user: principal } }).then((resp) => rSummaries.value = resp.data),
      api.get('/service/r/activity/_summary', { params: { context: 'DataSHIELD', user: principal } }).then((resp) => datashieldSummaries.value = resp.data),
    ]);
  }

  return {
    rSummaries,
    datashieldSummaries,
    summaries,
    reset,
    initSummaries,
  }
});