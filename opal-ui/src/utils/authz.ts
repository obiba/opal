import type { AxiosResponse } from 'axios';

export class Perms {
  methods: string[];

  constructor(response: AxiosResponse) {
    this.methods =
      response && response.headers && response.headers.allow
        ? response.headers.allow.split(',').map((m: string) => m.trim())
        : [];
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

  none() {
    return this.methods.length === 1 && this.methods.includes('OPTIONS');
  }
}
