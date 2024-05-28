import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { SubjectCredentialsDto, GroupDto } from 'src/models/Opal';

export const useUsersGroupsStore = defineStore('usersGroups', () => {
  const users = ref([] as SubjectCredentialsDto[]);
  const groups = ref([] as GroupDto[]);

  function reset() {
    users.value = [];
    groups.value = [];
  }

  async function initUsersAndGroups() {
    reset();
    await loadUsersAndGroups();
  }

  async function loadUsersAndGroups() {
    const response = await  Promise.all([api.get('/system/subject-credentials'), api.get('/system/groups')]);
    users.value = response[0].data;
    groups.value = response[1].data;
  }

  async function deleteUser(username: string) {
    await api.delete(`/system/subject-credential/${username}`);
  }

  return {
    users,
    groups,
    initUsersAndGroups,
    deleteUser,
  };

});
