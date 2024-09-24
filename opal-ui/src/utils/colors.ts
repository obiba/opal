import { CommandStateDto_Status } from 'src/models/Commands';
import { TableStatusDto } from 'src/models/Magma';
import { ProjectDatasourceStatusDto, AnalysisStatusDto } from 'src/models/Projects';

export function projectStatusColor(status: string | ProjectDatasourceStatusDto) {
  const statusEnum = typeof status === 'string' ? ProjectDatasourceStatusDto[status] : status;
  switch (statusEnum) {
    case ProjectDatasourceStatusDto.READY:
      return 'positive';
    case ProjectDatasourceStatusDto.ERRORS:
      return 'negative';
    case ProjectDatasourceStatusDto.LOADING:
      return 'secondary';
    case ProjectDatasourceStatusDto.BUSY:
      return 'warning';
    case ProjectDatasourceStatusDto.NONE:
      return 'black';
    default:
      return 'white';
  }
}

export function tableStatusColor(status: string | TableStatusDto) {
  const statusEnum = typeof status === 'string' ? TableStatusDto[status] : status;
  switch (statusEnum) {
    case TableStatusDto.READY:
      return 'positive';
    case TableStatusDto.ERROR:
      return 'negative';
    case TableStatusDto.LOADING:
      return 'secondary';
    case TableStatusDto.CLOSED:
      return 'black';
    default:
      return 'white';
  }
}

export function commandStatusColor(status: string | CommandStateDto_Status) {
  const statusEnum = typeof status === 'string' ? CommandStateDto_Status[status] : status;
  switch (statusEnum) {
    case CommandStateDto_Status.NOT_STARTED:
      return 'black';
    case CommandStateDto_Status.IN_PROGRESS:
      return 'primary';
    case CommandStateDto_Status.SUCCEEDED:
      return 'positive';
    case CommandStateDto_Status.FAILED:
      return 'negative';
    case CommandStateDto_Status.CANCEL_PENDING:
      return 'secondary';
    case CommandStateDto_Status.CANCELED:
      return 'warning';
    default:
      return 'white';
  }
}

export function analysisColor(status: string | AnalysisStatusDto) {
    const statusEnum = typeof status === 'string' ? AnalysisStatusDto[status] : status;
    switch (statusEnum) {
      case AnalysisStatusDto.PASSED:
        return 'positive';
      default:
        return 'negative';

    }
}