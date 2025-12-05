import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { PasswordDto, SubjectCredentialsDto } from 'src/models/Opal';

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
      users.value = response.data;
    });
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

  async function updateCurrentPassword(password: PasswordDto) {
    return api.put('/system/subject-credential/_current/password', password);
  }

  function generatePassword(length: number = 12): string {
    if (length < 8) {
      throw new Error('Password length should be at least 8 characters for strength.');
    }

    const upperCase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const lowerCase = 'abcdefghijklmnopqrstuvwxyz';
    const numbers = '0123456789';
    const specialChars = '@#$%^&+=!';

    const allChars = upperCase + lowerCase + numbers + specialChars;

    const getRandomChar = (chars: string) => {
      const randomValues = new Uint32Array(1);
      crypto.getRandomValues(randomValues);
      return chars[(randomValues[0] ?? 0) % chars.length];
    };

    // Ensure the password contains at least one of each character type
    const password = [
      getRandomChar(upperCase),
      getRandomChar(lowerCase),
      getRandomChar(numbers),
      getRandomChar(specialChars),
    ];

    // Fill the rest of the password length with random characters from all types
    for (let i = password.length; i < length; i++) {
      password.push(getRandomChar(allChars));
    }

    // Shuffle the password to make it more random using Fisher-Yates and crypto.getRandomValues()
    for (let i = password.length - 1; i > 0; i--) {
      const randomValues = new Uint32Array(1);
      crypto.getRandomValues(randomValues);
      const j = (randomValues[0] ?? 0) % (i + 1);
      [password[i], password[j]] = [password[j], password[i]];
    }
    return password.join('');
  }

  return {
    users,
    reset,
    initUsers,
    addUser,
    updateUser,
    deleteUser,
    updateCurrentPassword,
    generatePassword,
  };
});
