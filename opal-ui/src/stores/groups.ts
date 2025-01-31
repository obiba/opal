import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { GroupDto } from 'src/models/Opal';

export const useGroupsStore = defineStore('groups', () => {
  const groups = ref([] as GroupDto[]);

  function reset() {
    groups.value = [];
  }

  async function initGroups() {
    reset();
    return loadGroups();
  }

  async function loadGroups() {
    return api.get('/system/groups').then((response) => {
      groups.value = response.data;
    });
  }

  async function deleteGroup(group: GroupDto) {
    return api.delete(`/system/group/${group.name}`).then(() => loadGroups());
  }

  return {
    groups,
    reset,
    initGroups,
    deleteGroup,
  };
});
