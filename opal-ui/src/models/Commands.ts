// Code generated by protoc-gen-ts_proto. DO NOT EDIT.
// versions:
//   protoc-gen-ts_proto  v1.181.2
//   protoc               v3.21.12
// source: Commands.proto

import { type IdentifiersMappingConfigDto } from './Identifiers';

export const protobufPackage = 'Opal';

export interface Message {
  msg: string;
  timestamp: number;
}

export interface CommandStateDto {
  id: number;
  command: string;
  commandArgs: string;
  owner: string;
  status: string;
  startTime?: string | undefined;
  endTime?: string | undefined;
  messages: Message[];
  project?: string | undefined;
  name: string;
  progress?: CommandStateDto_ProgressDto | undefined;
}

export enum CommandStateDto_Status {
  NOT_STARTED = 'NOT_STARTED',
  IN_PROGRESS = 'IN_PROGRESS',
  SUCCEEDED = 'SUCCEEDED',
  FAILED = 'FAILED',
  CANCEL_PENDING = 'CANCEL_PENDING',
  CANCELED = 'CANCELED',
  UNRECOGNIZED = 'UNRECOGNIZED',
}

export interface CommandStateDto_ProgressDto {
  message: string;
  current: number;
  end: number;
  percent: number;
}

export interface ImportCommandOptionsDto {
  destination?: string | undefined;
  archive?: string | undefined;
  files: string[];
  source?: string | undefined;
  tables: string[];
  incremental?: boolean | undefined;
  idConfig?: IdentifiersMappingConfigDto | undefined;
}

export interface CopyCommandOptionsDto {
  source?: string | undefined;
  destination: string;
  nonIncremental?: boolean | undefined;
  noValues?: boolean | undefined;
  noVariables?: boolean | undefined;
  tables: string[];
  copyNullValues?: boolean | undefined;
  destinationTableName?: string | undefined;
  query?: string | undefined;
}

export interface ExportCommandOptionsDto {
  source?: string | undefined;
  destination?: string | undefined;
  out?: string | undefined;
  nonIncremental?: boolean | undefined;
  noValues?: boolean | undefined;
  noVariables?: boolean | undefined;
  tables: string[];
  format?: string | undefined;
  copyNullValues?: boolean | undefined;
  destinationTableName?: string | undefined;
  idConfig?: IdentifiersMappingConfigDto | undefined;
  query?: string | undefined;
  multilines?: boolean | undefined;
  entityIdNames?: string | undefined;
}

export interface ImportVCFCommandOptionsDto {
  /** store location */
  project: string;
  /** VCF files location */
  files: string[];
}

export interface ExportVCFCommandOptionsDto {
  /** VCF file names */
  names: string[];
  /** store location */
  project: string;
  /** Destination folder */
  destination: string;
  /** table reference to be used for sample subseting */
  table?: string | undefined;
  caseControl?: boolean | undefined;
  participantIdentifiersMapping?: string | undefined;
}

export interface AnalyseCommandOptionsDto {
  project: string;
  analyses: AnalyseCommandOptionsDto_AnalyseDto[];
}

export interface AnalyseCommandOptionsDto_AnalyseDto {
  table: string;
  name: string;
  plugin: string;
  template: string;
  params: string;
  variables?: string | undefined;
}

export interface ReloadDatasourceCommandOptionsDto {
  project: string;
}

export interface BackupCommandOptionsDto {
  archive: string;
  override?: boolean | undefined;
  /** backup as a view (logical) or a table (data dump) */
  viewsAsTables?: boolean | undefined;
}

export interface RestoreCommandOptionsDto {
  archive: string;
  password?: string | undefined;
  override?: boolean | undefined;
}

export interface RPackagesCommandOptionsDto {
  cluster: string;
}

export interface RPackageCommandOptionsDto {
  cluster: string;
  name: string;
  manager: string;
  ref?: string | undefined;
}
