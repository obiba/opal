import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { Acl } from 'src/models/Opal';

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

  return {
    acls,
    initAcls,
  }
});
