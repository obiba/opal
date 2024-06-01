import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { SubjectCredentialsDto } from 'src/models/Opal';

export const useUsersStore = defineStore('users', () => {
  const users = ref([] as SubjectCredentialsDto[]);

  function reset() {
    users.value = [];
  }

  async function initUsers() {
    reset();
    return loadUsers();
  }

  async function loadUsers() {
    return api.get('/system/subject-credentials').then((response) => {
      users.value = response.data
    })
  }

  async function addUser(user: SubjectCredentialsDto) {
    return api.post('/system/subject-credentials', user).then(() => loadUsers());
  }

  async function updateUser(user: SubjectCredentialsDto) {
    return api.put(`/system/subject-credential/${user.name}`, user).then(() => loadUsers());
  }

  async function deleteUser(user: SubjectCredentialsDto) {
    return api.delete(`/system/subject-credential/${user.name}`).then(() => loadUsers());
  }

  return {
    users,
    reset,
    initUsers,
    addUser,
    updateUser,
    deleteUser,
  };

});
