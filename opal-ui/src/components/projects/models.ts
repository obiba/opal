export interface CopyCommandOptions {
  tables: string[];
  destination: string;
  destinationTableName: string | undefined;
  nonIncremental: boolean;
  copyNullValues: boolean;
  noVariables: boolean;
}
