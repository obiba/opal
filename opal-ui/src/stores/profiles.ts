import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { SubjectProfileDto } from 'src/models/Opal';

export const useProfilesStore = defineStore('profiles', () => {
  const profiles = ref([] as SubjectProfileDto[]);
  const profile = ref({} as SubjectProfileDto);

  function reset() {
    profiles.value = [];
    profile.value = {} as SubjectProfileDto;
  }

  async function initProfiles() {
    profiles.value = [];
    return loadProfiles();
  }

  async function initProfile(principal = '_current') {
    profile.value = {} as SubjectProfileDto;
    return loadProfile(principal);
  }

  async function loadProfiles() {
    return api.get('/system/subject-profiles').then((response) => {
      profiles.value = response.data;
    });
  }

  async function loadProfile(principal: string) {
    return api.get(`/system/subject-profile/${principal}`).then((response) => {
      profile.value = response.data;
    });
  }

  async function deleteProfile(profile: SubjectProfileDto) {
    return api.delete(`/system/subject-profile/${profile.principal}`);
  }

  async function deleteProfiles(profiles: SubjectProfileDto[]) {
    const principals = profiles.map((profile) => profile.principal);
    return api.delete('/system/subject-profiles', {
      params: {
        p: principals,
      },
      paramsSerializer: {
        indexes: null,
      },
    });
  }

  async function disableOtp(profile: SubjectProfileDto) {
    return api.delete(`/system/subject-profile/${profile.principal}/otp`);
  }

  async function enableCurrentOtp(): Promise<string> {
    return api.put('/system/subject-profile/_current/otp').then((response) => response.data);
  }

  async function disableCurrentOtp() {
    return api.delete('/system/subject-profile/_current/otp');
  }

  async function getProfile(principal: string): Promise<SubjectProfileDto> {
    return api.get(`/system/subject-profile/${principal}`).then((response) => {
      return response.data;
    });
  }

  return {
    profiles,
    profile,
    reset,
    initProfiles,
    initProfile,
    deleteProfile,
    deleteProfiles,
    disableOtp,
    enableCurrentOtp,
    disableCurrentOtp,
    getProfile,
  };
});
