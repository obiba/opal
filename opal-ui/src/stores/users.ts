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
    await loadUsers();
  }

  async function loadUsers() {
    const response = await api.get('/system/subject-credentials');
    users.value = response.data;
  }

  async function addUser(user: SubjectCredentialsDto) {
    await api.post(`/system/subject-credentials`, user);
    await loadUsers();
  }

  async function updateUser(user: SubjectCredentialsDto) {
    await api.put(`/system/subject-credential/${user.name}`, user);
    await loadUsers();
  }

  async function deleteUser(user: SubjectCredentialsDto) {
    await api.delete(`/system/subject-credential/${user.name}`);
    await loadUsers();
  }

  return {
    users,
    initUsers,
    addUser,
    updateUser,
    deleteUser,
  };

});
