export function projectStatusColor(status: string) {
  switch (status) {
    case 'READY':
      return 'positive';
    case 'ERRORS':
      return 'negative';
    case 'LOADING':
      return 'secondary';
    case 'BUSY':
      return 'warning';
    case 'NONE':
      return 'black';
    default:
      return 'white';
  }
}

export function tableStatusColor(status: string) {
  switch (status) {
    case 'READY':
      return 'positive';
    case 'ERROR':
      return 'negative';
    case 'LOADING':
      return 'secondary';
    case 'CLOSED':
      return 'black';
    default:
      return 'white';
  }
}

export function commandStatusColor(status: string) {
  switch (status) {
    case 'NOT_STARTED':
      return 'black';
    case 'IN_PROGRESS':
      return 'primary';
    case 'SUCCEEDED':
      return 'positive';
    case 'FAILED':
      return 'negative';
    case 'CANCEL_PENDING':
      return 'secondary';
    case 'CANCELED':
      return 'warning';
    default:
      return 'white';
  }
}
