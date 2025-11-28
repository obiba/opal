import { Notify } from 'quasar';
import { i18n } from 'src/boot/i18n';

const { t } = i18n.global;

export function notifySuccess(message: string) {
  Notify.create({
    type: 'positive',
    message: t(message),
  });
}

export function notifyInfo(message: string) {
  Notify.create({
    type: 'info',
    message: t(message),
  });
}

export function notifyWarning(message: string) {
  Notify.create({
    type: 'warning',
    message: t(message),
  });
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function notifyError(error: any) {
  let message = t('unknown_error');
  if (typeof error === 'string') {
    message = t(error);
  } else {
    console.error(error);
    message = error.message;
    if (error.response?.data && error.response.data?.status) {
      message = t(`error.${error.response?.data.status}`);
      if (error.response.data.arguments && error.response.data.arguments.length) {
        const args = error.response.data.arguments.map((arg: string) => {
          const msg = t(`error.argument.${arg}`)
          if (msg.startsWith('error.argument.')) {
            return arg;
          }
          return msg;
        }).join(', ');
        message = `${message} - ${args}`;
      }
    }
  }
  Notify.create({
    type: isReAuthError(error) ? 'warning' : 'negative',
    message,
  });
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function isReAuthError(error: any): boolean {
  if (error.response?.status === 401) {
    if (error.response.data.arguments && error.response.data.arguments.length) {
      const args: string[] = error.response.data.arguments;
      if (args.includes('reauthentication_required')) {
        return true;
      }
    }
  }
  return false;
}