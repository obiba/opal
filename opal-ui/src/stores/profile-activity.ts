import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { RActivitySummaryDto } from 'src/models/OpalR';

export const useProfileActivityStore = defineStore('profileActivity', () => {
  const rSummaries = ref<RActivitySummaryDto[]>([]);
  const datashieldSummaries = ref<RActivitySummaryDto[]>([]);
  const sqlSummaries = ref<RActivitySummaryDto[]>([]);

  const summaries = computed(() =>
    [...rSummaries.value, ...datashieldSummaries.value, ...sqlSummaries.value].sort((a, b) =>
      a.profile.localeCompare(b.profile),
    ),
  );

  function reset() {
    rSummaries.value = [];
    datashieldSummaries.value = [];
    sqlSummaries.value = [];
  }

  /**
   * Reading someone else's activity requires administration permissions, whereas the _current resources report on the
   * authenticated user and are open to them: a plain R or DataSHIELD user can only see their own activity that way.
   */
  async function getSummaries(context: string, principal: string, current: boolean) {
    return current
      ? api.get('/service/r/activity/_current/_summary', { params: { context } })
      : api.get('/service/r/activity/_summary', { params: { context, user: principal } });
  }

  async function initSummaries(principal: string, current: boolean = false) {
    return Promise.all([
      getSummaries('R', principal, current).then((resp) => (rSummaries.value = resp.data)),
      getSummaries('DataSHIELD', principal, current).then((resp) => (datashieldSummaries.value = resp.data)),
      getSummaries('SQL', principal, current).then((resp) => (sqlSummaries.value = resp.data)),
    ]);
  }

  async function getRSessionActivities(
    principal: string,
    context: string,
    profile: string,
    current: boolean = false,
  ) {
    return (
      current
        ? api.get('/service/r/activity/_current', { params: { context, profile } })
        : api.get('/service/r/activity', { params: { context, profile, user: principal } })
    ).then((resp) => resp.data);
  }

  return {
    rSummaries,
    datashieldSummaries,
    summaries,
    reset,
    initSummaries,
    getRSessionActivities,
  };
});
