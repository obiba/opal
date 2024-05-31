import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { GroupDto } from 'src/models/Opal';

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
    const response = await  Promise.all([api.get('/system/subject-credentials'), api.get('/system/groups')]);
    groups.value = response[1].data;
  }

  async function deleteGroup(group: GroupDto) {
    await api.delete(`/system/group/${group.name}`);
    await loadGroups();
  }

  return {
    groups,
    reset,
    initGroups,
    deleteGroup,
  };

});
