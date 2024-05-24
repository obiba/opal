import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { SubjectProfileDto } from 'src/models/Opal';

export const useAuthStore = defineStore('auth', () => {
  const sid = ref('');
  const version = ref('');
  const profile = ref<SubjectProfileDto>({} as SubjectProfileDto);

  function reset() {
    sid.value = '';
    version.value = '';
    profile.value = {} as SubjectProfileDto;
  }

  const isAuthenticated = computed(() => {
    return profile.value.principal !== undefined;
  });

  async function signin(username: string, password: string) {
    const params = new URLSearchParams();
    params.append('username', username);
    params.append('password', password);
    sid.value = '';
    version.value = '';
    return api.post('/auth/sessions', params).then((response) => {
      if (response.status === 201) {
        const sessionUrl = response.headers['location'];
        sid.value = sessionUrl.split('/').pop();
        version.value = response.headers['x-opal-version'];
      }
      return response;
    });
  }

  async function signout() {
    return api.delete('/auth/session/_current').then((response) => {
      sid.value = '';
      version.value = '';
      profile.value = {} as SubjectProfileDto;
      return response;
    });
  }

  async function userProfile() {
    return api.get('/system/subject-profile/_current').then((response) => {
      if (response.status === 200) {
        version.value = response.headers['x-opal-version'];
        profile.value = response.data;
      }
      return response;
    });
  }

  return {
    sid,
    version,
    profile,
    isAuthenticated,
    signin,
    signout,
    userProfile,
    reset,
  };
});