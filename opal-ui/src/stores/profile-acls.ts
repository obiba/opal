import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { type Acl, Subject_SubjectType, type SubjectProfileDto } from 'src/models/Opal';

export const useProfileAclsStore = defineStore('profileAcls', () => {
  const acls = ref([] as Acl[]);
  const groupAcls = ref<{ [key: string]: Acl[] }>({});

  function reset() {
    acls.value = [];
    groupAcls.value = {};
  }

  async function initSubjectAcls(profile: SubjectProfileDto) {
    reset();
    initAcls(profile.principal, Subject_SubjectType.USER);
    if (profile.groups) {
      profile.groups.forEach((group) => initAcls(group, Subject_SubjectType.GROUP));
    }
  }

  async function initAcls(principal: string, type: Subject_SubjectType = Subject_SubjectType.USER) {
    return loadAcls(principal, type);
  }

  async function loadAcls(principal: string, type: Subject_SubjectType) {
    return api.get(`/authz-subject/${principal}`, { params: { type } }).then((response) => {
      if (type === Subject_SubjectType.USER) {
        acls.value = response.data;
      } else {
        groupAcls.value[principal] = response.data;
      }
    });
  }

  async function deleteAcls(acls: Acl[]) {
    const requests = acls
      .map((acl) =>
        api.delete(`/authz/${acl.resource.replace(/^\//, '')}`, {
          params: { subject: acl.subject?.principal, perm: acl.actions.pop() || '', type: acl.subject?.type },
        })
      );
    return Promise.all(requests);
  }

  return {
    acls,
    groupAcls,
    reset,
    initSubjectAcls,
    initAcls,
    deleteAcls,
  };
});
