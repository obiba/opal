import { File } from 'src/components/models';

export function getSizeLabel(size: number | undefined) {
  if (size === undefined || isNaN(size)) {
    return '-';
  }
  if (size < 1024) {
    return `${size} B`;
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(2)} KB`;
  }
  return `${(size / 1024 / 1024).toFixed(2)} MB`;
}

export function getIconName(file: File) {
  if (file.type === 'FOLDER') {
    return file.readable ? 'folder' : 'folder_off';
  }
  return file.readable ? 'description' : 'insert_drive_file';
}

export function getDateLabel(date: number | undefined) {
  if (date === undefined || isNaN(date)) {
    return '-';
  }
  return new Date(date).toLocaleString();
}
