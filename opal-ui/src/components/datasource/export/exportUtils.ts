import { type FileDto } from 'src/models/Opal';

export function makeOutputPath(destinationFolder: FileDto | undefined, datasourceName: string | undefined, tableName: string | undefined, extension: string | undefined): string | undefined {
  if (!destinationFolder?.path) {
    return undefined;
  }
  const currentTime = new Date()
    .toISOString()
    .replace(/[:T-]/g, '')
    .split('.')[0]

  const prefix =
    datasourceName && datasourceName.length > 0
      ? (tableName && tableName.length > 0
        ? `${datasourceName}-${tableName}`
        : datasourceName)
      : 'datasource';
  return `${destinationFolder.path}/${prefix}-${currentTime}${extension ? extension : ''}`;
}