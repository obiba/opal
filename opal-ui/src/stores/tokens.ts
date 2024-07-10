import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { SubjectTokenDto } from 'src/models/Opal';

export enum TOKEN_TYPES {
  DATASHIELD = 'datashield',
  R = 'r',
  SQL = 'sql',
  CUSTOM = 'custom',
}

export const useTokensStore = defineStore('tokens', () => {
  const tokens = ref([] as SubjectTokenDto[]);

  function reset() {
    tokens.value = [];
  }

  async function initTokens() {
    reset();
    return loadTokens();
  }

  async function loadTokens() {
    return api.get('/system/subject-token/_current/tokens').then((response) => {
      tokens.value = response.data;
    });
  }

  function addToken(token: SubjectTokenDto): Promise<void> {
    return api.post('/system/subject-token/_current/tokens', token);
  }

  function deleteToken(name: string): Promise<void> {
    return api.delete(`/system/subject-token/_current/token/${name}`);
  }

  return {
    tokens: tokens,
    reset,
    initTokens,
    deleteToken,
    addToken,
  };
});
