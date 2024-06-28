import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { Acl, Subject_SubjectType } from 'src/models/Opal';

export const useProfileAclsStore = defineStore('profileAcls', () => {

  const acls = ref([] as Acl[]);

  function reset() {
    acls.value = [];
  }

  async function initAcls(principal: string, type: Subject_SubjectType = Subject_SubjectType.USER) {
    reset();
    return loadAcls(principal, type);
  }

  async function loadAcls(principal: string, type: Subject_SubjectType) {
    return api.get(`/authz-subject/${principal}`, { params: {type}}).then((response) => {
      acls.value = response.data;
    });
  }

  async function deleteAcl(principal: string, permission: string, resource: string, type: Subject_SubjectType = Subject_SubjectType.USER) {
    return api.delete(`/authz/${resource.replace(/^\//, '')}`, { params: { subject: principal, perm: permission, type }}).then(() => initAcls(principal, type));
  }

  return {
    acls,
    reset,
    initAcls,
    deleteAcl
  }
});
