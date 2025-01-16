import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { BookmarkDto, SubjectProfileDto, AuthProviderDto } from 'src/models/Opal';

export const useAuthStore = defineStore('auth', () => {
  const sid = ref('');
  const version = ref('');
  const profile = ref<SubjectProfileDto>({} as SubjectProfileDto);
  const bookmarks = ref<BookmarkDto[]>([]);
  const isAdministrator = ref(false);

  function reset() {
    sid.value = '';
    version.value = '';
    profile.value = {} as SubjectProfileDto;
  }

  const isAuthenticated = computed(() => {
    return profile.value.principal !== undefined;
  });

  async function checkIsAdministrator() {
    return api.options('/system/subject-credentials')
      .then((response) => isAdministrator.value = response.headers['allow'].includes('POST'))
      .catch(() => isAdministrator.value = false);
  }

  async function signin(username: string, password: string, authMethod: string, token: string) {
    const params = new URLSearchParams();
    params.append('username', username);
    params.append('password', password);
    sid.value = '';
    version.value = '';
    const headers: Record<string, string> = {};
    if (authMethod && token) {
      headers[authMethod] = token;
    }
    return api.post('/auth/sessions', params, { headers }).then((response) => {
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

  function isBookmarked(resource: string) {
    return bookmarks.value.find((b) => b.resource === resource) !== undefined;
  }

  async function toggleBookmark(resource: string) {
    if (isBookmarked(resource)) {
      return api.delete(`/system/subject-profile/_current/bookmark${resource}`).then(() => {
        return loadBookmarks();
      });
    } else {
      return api.post('/system/subject-profile/_current/bookmarks', {}, { params: { resource } }).then(() => {
        return loadBookmarks();
      });
    }
  }

  async function loadBookmarks() {
    return api.get('/system/subject-profile/_current/bookmarks').then((response) => {
      bookmarks.value = response.data;
      return response.data;
    });
  }

  async function getProviders(): Promise<AuthProviderDto[]> {
    return api.get('/auth/providers').then((response) => {
      return response.data;
    });
  }

  return {
    sid,
    version,
    profile,
    isAuthenticated,
    bookmarks,
    isAdministrator,
    signin,
    signout,
    userProfile,
    isBookmarked,
    loadBookmarks,
    toggleBookmark,
    getProviders,
    reset,
    checkIsAdministrator,
  };
});
