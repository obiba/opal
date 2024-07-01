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

  return {
    profiles: profiles,
    reset,
    initProfiles: initProfiles,
    getProfile,
    deleteProfile,
    deleteProfiles,
    disableOtp
  };

});
