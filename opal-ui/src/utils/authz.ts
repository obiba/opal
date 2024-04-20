import { AxiosResponse } from 'axios';

export class Perms {

  methods: string[];

  constructor(methods: string[]) {
    this.methods = methods;
  }

  canCreate() {
    return this.methods.includes('POST');
  }

  canRead() {
    return this.methods.includes('GET');
  }

  canUpdate() {
    return this.methods.includes('PUT');
  }

  canDelete() {
    return this.methods.includes('DELETE');
  }

}

export function getPerms(response: AxiosResponse): Perms {
  return new Perms(response ? response.headers.allow.split(',').map((m: string) => m.trim()) : []);
}
