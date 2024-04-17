export function projectStatusColor(status: string) {
  switch (status) {
    case 'READY':
      return 'green';
    case 'ERRORS':
      return 'red';
    case 'LOADING':
      return 'grey';
    case 'BUSY':
      return 'orange';
    case 'NONE':
      return 'black';
    default:
      return 'white';
  }
}

export function tableStatusColor(status: string) {
  switch (status) {
    case 'READY':
      return 'green';
    case 'ERROR':
      return 'red';
    case 'LOADING':
      return 'grey';
    case 'CLOSED':
      return 'black';
    default:
      return 'white';
  }
}
