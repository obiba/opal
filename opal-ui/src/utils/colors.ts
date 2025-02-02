import { CommandStateDto_Status } from 'src/models/Commands';
import { TableStatusDto } from 'src/models/Magma';
import { ProjectDatasourceStatusDto, AnalysisStatusDto } from 'src/models/Projects';

export function projectStatusColor(status: string | ProjectDatasourceStatusDto) {
  if (status === 'READY' || status === ProjectDatasourceStatusDto.READY) {
    return 'positive';
  }
  if (status === 'ERRORS' || status === ProjectDatasourceStatusDto.ERRORS) {
    return 'negative';
  }
  if (status === 'LOADING' || status === ProjectDatasourceStatusDto.LOADING) {
    return 'secondary';
  }
  if (status === 'BUSY' || status === ProjectDatasourceStatusDto.BUSY) {
    return 'warning';
  }
  if (status === 'NONE' || status === ProjectDatasourceStatusDto.NONE) {
    return 'black';
  }
  return 'white';
}

export function tableStatusColor(status: string | TableStatusDto | undefined) {
  if (status === 'READY' || status === TableStatusDto.READY) {
    return 'positive';
  }
  if (status === 'ERROR' || status === TableStatusDto.ERROR) {
    return 'negative';
  }
  if (status === 'LOADING' || status === TableStatusDto.LOADING) {
    return 'secondary';
  }
  if (status === 'CLOSED' || status === TableStatusDto.CLOSED) {
    return 'black';
  }
  return 'white';
}

export function commandStatusColor(status: string | CommandStateDto_Status) {
  if (status === 'NOT_STARTED' || status === CommandStateDto_Status.NOT_STARTED) {
    return 'black';
  }
  if (status === 'IN_PROGRESS' || status === CommandStateDto_Status.IN_PROGRESS) {
    return 'primary';
  }
  if (status === 'SUCCEEDED' || status === CommandStateDto_Status.SUCCEEDED) {
    return 'positive';
  }
  if (status === 'FAILED' || status === CommandStateDto_Status.FAILED) {
    return 'negative';
  }
  if (status === 'CANCEL_PENDING' || status === CommandStateDto_Status.CANCEL_PENDING) {
    return 'secondary';
  }
  if (status === 'CANCELED' || status === CommandStateDto_Status.CANCELED) {
    return 'warning';
  }
  return 'white';
}

export function analysisColor(status: string | AnalysisStatusDto) {
  if (status === 'PASSED' || status === AnalysisStatusDto.PASSED) {
    return 'positive';
  }
  return 'negative';
}