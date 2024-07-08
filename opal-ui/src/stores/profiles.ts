import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { SubjectProfileDto } from 'src/models/Opal';

export const useProfilesStore = defineStore('profiles', () => {
  const profiles = ref([] as SubjectProfileDto[]);

  function reset() {
    profiles.value = [];
  }

  async function initProfiles() {
    reset();
    return loadProfiles();
  }

  async function loadProfiles() {
    return api.get('/system/subject-profiles').then((response) => {
      profiles.value = response.data
    })
  }

  async function getCurrentProfile() : Promise<SubjectProfileDto> {
    return api.get('/system/subject-profile/_current').then((response) => response.data);
  }

  async function getProfile(profile: SubjectProfileDto) {
    return api.delete(`/system/subject-profile/${profile.principal}`);
  }

  async function deleteProfile(profile: SubjectProfileDto) {
    return api.delete(`/system/subject-profile/${profile.principal}`);
  }

  async function deleteProfiles(profiles: SubjectProfileDto[]) {
    const principals = profiles.map((profile) => profile.principal);
    return api.delete('/system/subject-profiles',{
      params: {
          p: principals
      },
      paramsSerializer: {
        indexes: null,
      }
    });
  }

  async function disableOtp(profile: SubjectProfileDto) {
    return api.delete(`/system/subject-profile/${profile.principal}/otp`);
  }

  async function enableCurrentOtp(): Promise<string> {
    return api.put('/system/subject-profile/_current/otp').then(response => response.data);
  }

  async function disableCurrentOtp() {
    return api.delete('/system/subject-profile/_current/otp');
  }

  return {
    profiles: profiles,
    reset,
    initProfiles: initProfiles,
    getProfile,
    deleteProfile,
    getCurrentProfile,
    deleteProfiles,
    disableOtp,
    enableCurrentOtp,
    disableCurrentOtp
  };

});
