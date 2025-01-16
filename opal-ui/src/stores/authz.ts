import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { Acl, SuggestionsDto } from 'src/models/Opal';

export const useAuthzStore = defineStore('authz', () => {
  const acls = ref<{ [key: string]: Acl[] }>({}); // the list of ACLs for the current resource

  function reset() {
    acls.value = {};
  }

  async function initAcls(path: string) {
    acls.value[path] = [];
    return api.get(path).then((response) => {
      if (response.status === 200) {
        acls.value[path] = response.data;
      }
      return response;
    });
  }

  function resetAcls(path: string) {
    delete acls.value[path];
  }

  async function setAcl(path: string, acl: Acl) {
    return api
      .post(
        path,
        {},
        {
          params: {
            type: acl.subject?.type,
            principal: acl.subject?.principal,
            permission: acl.actions.join(','),
          },
        }
      )
      .then(() => {
        return initAcls(path);
      });
  }

  async function deleteAcl(path: string, acl: Acl) {
    return api
      .delete(path, {
        params: {
          type: acl.subject?.type,
          principal: acl.subject?.principal,
        },
      })
      .then(() => {
        return initAcls(path);
      });
  }

  async function searchSubjects(type: string, query: string): Promise<SuggestionsDto> {
    return api
      .get('/system/subject-profiles/_search', {
        params: {
          type,
          query,
        },
      })
      .then((response) => {
        return response.data;
      });
  }

  return {
    acls,
    reset,
    initAcls,
    resetAcls,
    setAcl,
    deleteAcl,
    searchSubjects,
  };
});
