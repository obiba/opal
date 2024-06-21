import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { Acl, SuggestionsDto } from 'src/models/Opal';

export const useAuthzStore = defineStore('authz', () => {

  const resource = ref<string>(''); // the path of the current resource
  const acls = ref<Acl[]>([]); // the list of ACLs for the current resource

  async function initAcls(path: string) {
    resource.value = path;
    acls.value = [];
    return api.get(resource.value).then((response) => {
      if (response.status === 200) {
        acls.value = response.data;
      }
      return response;
    });
  }

  async function setAcl(acl: Acl) {
    return api.post(resource.value, {}, { params: {
      type: acl.subject?.type,
      principal: acl.subject?.principal,
      permission: acl.actions.join(','),
    }}).then(() => {
      return initAcls(resource.value);
    });
  }

  async function deleteAcl(acl: Acl) {
    return api.delete(resource.value, { params: {
      type: acl.subject?.type,
      principal: acl.subject?.principal,
    }}).then(() => {
      return initAcls(resource.value);
    });
  }

  async function searchSubjects(type: string, query: string): Promise<SuggestionsDto> {
    return api.get('/system/subject-profiles/_search', { params: {
      type,
      query,
    }}).then((response) => {
      return response.data;
    });
  }

  return {
    acls,
    initAcls,
    setAcl,
    deleteAcl,
    searchSubjects,
  }
});
