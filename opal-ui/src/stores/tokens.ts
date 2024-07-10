import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { SubjectTokenDto } from 'src/models/Opal';

export const useTokensStore = defineStore('tokens', () => {
  async function getCurrentTokens(): Promise<SubjectTokenDto[]> {
    return api.get('/system/subject-token/_current/tokens').then((response) => {
      return response.data;
    });
  }

  async function deleteCurrentToken(name: string): Promise<void> {
    return api.delete(`/system/subject-token/_current/token/${name}`);
  }

  return {
    getCurrentTokens,
    deleteCurrentToken
  };
});
