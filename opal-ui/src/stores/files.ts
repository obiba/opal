import { defineStore } from 'pinia';
import { api, baseUrl } from 'src/boot/api';
import { type FileDto, FileDto_FileType } from 'src/models/Opal';
import type { FileObject } from 'src/components/models';

export const useFilesStore = defineStore('files', () => {
  const current = ref({} as FileDto);
  const copySelection = ref<FileDto[]>([]);
  const cutSelection = ref<FileDto[]>([]);

  function reset() {
    current.value = {} as FileDto;
    copySelection.value = [];
    cutSelection.value = [];
  }

  async function getFile(path: string) {
    return api.get(`/files/_meta${path}`).then((response) => {
      return response.data;
    });
  }

  async function initFiles(path: string) {
    if (current.value.path) return Promise.resolve();
    return loadFiles(path);
  }

  async function refreshFiles(path: string) {
    current.value = {} as FileDto;
    return loadFiles(path);
  }

  async function loadFiles(path: string) {
    return api.get(`/files/_meta${path}`).then((response) => {
      current.value = response.data;
      return response;
    });
  }

  function downloadFile(path: string) {
    downloadFiles(path, [], undefined);
  }

  function downloadFiles(path: string, files: FileDto[], password: string | undefined) {
    if (password) {
      return api
        .get(`/files${path}`, {
          params: {
            file: files.map((f) => f.name),
          },
          paramsSerializer: {
            indexes: null, // no brackets at all
          },
          headers: {
            'X-File-Key': password,
          },
          responseType: 'blob',
        })
        .then((response) => {
          let fileName = response.headers['content-disposition'].split('=')[1];
          fileName = fileName.replace(/"/g, '');
          const url = window.URL.createObjectURL(new Blob([response.data]));
          const link = document.createElement('a');
          link.href = url;
          link.setAttribute('download', fileName || 'file.zip');
          document.body.appendChild(link);
          link.click();
          window.URL.revokeObjectURL(url);
          return response;
        });
    } else {
      let uri = `${baseUrl}/files${path}`;
      if (files && files.length > 0) {
        if (files.length > 1) {
          uri += '?';
          files.forEach((f) => (uri += `&file=${f.name}`));
        } else if (files[0]) {
          uri += `/${files[0].name}`;
        }
      }
      window.open(uri, '_self');
    }
  }

  function addFolder(path: string, folderName: string) {
    return api.post(`/files${path}`, folderName, {
      headers: {
        'Content-Type': 'text/plain',
      },
    });
  }

  function uploadFiles(path: string, files: FileObject[]) {
    const formData = new FormData();
    files.forEach((f) => {
      formData.append('attachment', f);
    });
    return api.post(`/files${path}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  }

  function extractArchive(path: string, destination: FileDto, key: string | undefined) {
    if (destination.type !== FileDto_FileType.FOLDER) return Promise.reject('Invalid destination');
    const params = {
      action: 'unzip',
      destination: destination.path,
      key,
    };

    return api.post(`/files${path}`, {}, { params });
  }

  function deleteFile(path: string) {
    return api.delete(`/files${path}`);
  }

  function deleteFiles(files: FileDto[]) {
    return Promise.all(
      files.map((f) => {
        return api.delete(`/files${f.path}`);
      })
    );
  }

  function setCopySelection(files: FileDto[]) {
    copySelection.value = files;
    cutSelection.value = [];
  }

  function setCutSelection(files: FileDto[]) {
    cutSelection.value = files;
    copySelection.value = [];
  }

  function pasteFiles(path: string) {
    if (!canPasteSelection(path)) return Promise.reject('Invalid paste');
    const params = {
      action: copySelection.value.length > 0 ? 'copy' : 'move',
      file:
        copySelection.value.length > 0 ? copySelection.value.map((f) => f.path) : cutSelection.value.map((f) => f.path),
    };
    return api
      .put(
        `/files${path}`,
        {},
        {
          params,
          paramsSerializer: {
            indexes: null, // no brackets at all
          },
        }
      )
      .then((response) => {
        copySelection.value = [];
        cutSelection.value = [];
        return response;
      });
  }

  function canPasteSelection(path: string) {
    // selections not in own parent folder or in itself when is a folder
    function canPaste(files: FileDto[]) {
      return files.every(
        (f) => path !== getParentFolder(f.path) && (f.type === FileDto_FileType.FILE || !path.startsWith(f.path))
      );
    }
    return (
      (copySelection.value.length > 0 && canPaste(copySelection.value)) ||
      (cutSelection.value.length > 0 && canPaste(cutSelection.value))
    );
  }

  function renameFile(path: string, newName: string) {
    const parts = parsePath(path);
    parts.pop();
    parts.push(newName);
    const newPath = `/${parts.join('/')}`;
    // check if file exists
    return api.head(`/files${newPath}`).then(
      () => {
        return Promise.reject('file_already_exists');
      },
      () => {
        const params = {
          action: 'move',
          file: path,
        };
        return api.put(`/files${newPath}`, {}, { params });
      }
    );
  }

  function getParentFolder(path: string) {
    if (path === undefined) return '/';
    const parts = path.split('/');
    parts.pop();
    return parts.join('/');
  }

  function parsePath(path: string) {
    return path.split('/').filter((p) => p !== '');
  }

  return {
    current,
    getFile,
    initFiles,
    refreshFiles,
    loadFiles,
    downloadFile,
    downloadFiles,
    addFolder,
    extractArchive,
    uploadFiles,
    deleteFile,
    deleteFiles,
    setCopySelection,
    setCutSelection,
    canPasteSelection,
    pasteFiles,
    renameFile,
    getParentFolder,
    parsePath,
    reset,
  };
});
