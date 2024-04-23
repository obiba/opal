export interface SubjectProfile {
  principal: string;
  realm: string;
  created: string;
  lastUpdate: string;
  groups: string[];
  otpEnabled: boolean;
}

export interface Message {
  msg: string;
  timestamp: number;
}

export interface Progress {
  message: string;
  current: number;
  end: number;
  percent: number;
}

export interface CommandState {
  id: number;
  command: string;
  commandArgs: string;
  owner: string;
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'SUCCEEDED' | 'FAILED' | 'CANCEL_PENDING' | 'CANCELED';
  startTime: string | undefined;
  endTime: string | undefined;
  project: string | undefined;
  name: string;
  progress: Progress | undefined;
  messages: Message[];
}

export interface File {
  name: string;
  path: string;
  type: 'FILE' | 'FOLDER';
  children: File[] | undefined;
  size: number | undefined;
  lastModifiedTime: number | undefined;
  readable: boolean;
  writable: boolean;
}

export interface FileObject extends Blob {
  readonly size: number;
  readonly name: string;
  readonly type: string;
}

export interface Timestamps {
  created: string;
  lastUpdate: string;
}

export interface Project {
  name: string;
  title: string;
  description: string;
  tags: string[];
  directory: string;
  archived: boolean;
  database: string;
  datasourceStatus: string;
  datasource: Datasource;
}

export interface ProjectSummary {
  name: string;
  tableCount: number;
  variableCount: number;
  entityCount: number;
  viewCount: number;
  derivedVariableCount: number;
  resourceCount: number;
  datasourceStatus: string;
  timestamps: Timestamps;
}

export interface Datasource {
  name: string;
  table: string[];
  view: string[];
  type: string;
  timestamps: Timestamps;
}

export interface Table {
  name: string;
  entityType: string;
  status: string;
  viewType: string | undefined;
  datasourceName: string;
  timestamps: Timestamps;
}

export interface View {
  name: string;
  datasourceName: string;
  status: string;
  from: string[];
  'Magma.VariableListViewDto.view': ViewVariables;
}

export interface ViewVariables {
  variables: Variable[];
}


export interface Variable {
  name: string;
  entityType: string;
  valueType: string;
  mimeType: string | undefined;
  isRepeatable: boolean;
  occurrenceGroup: string | undefined;
  unit: string | undefined;
  index: number;
  referencedEntityType: string | undefined;
  attributes: Attribute[] | undefined;
  categories: Category[] | undefined;
}

export interface Category {
  name: string;
  isMissing: boolean;
  attributes: Attribute[];
}

export interface Attribute {
  name: string;
  value: string;
  locale: string | undefined;
  namespace: string | undefined;
}
