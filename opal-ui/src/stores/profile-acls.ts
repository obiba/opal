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

  async function deleteAcls(acls:Acl[], type: Subject_SubjectType = Subject_SubjectType.USER) {
    const requests = acls.map((acl) => api.delete(`/authz/${acl.resource.replace(/^\//, '')}`, { params: { subject: acl.subject?.principal, perm: acl.actions.pop() || '', type }}));
    return Promise.all(requests);
  }

  return {
    acls,
    reset,
    initAcls,
    deleteAcls
  }
});
