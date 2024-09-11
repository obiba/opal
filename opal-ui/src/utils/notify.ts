import { Notify } from 'quasar';
import { t } from 'src/boot/i18n';

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

export function notifyError(error) {
  let message = t('unknown_error');
  if (typeof error === 'string') {
    message = t(error);
  } else {
    console.error(error);
    message = error.message;
    if (error.response?.data && error.response.data?.status) {
      message = t(`error.${error.response?.data.status}`, error.response?.data.arguments);
    }
  }
  Notify.create({
    type: 'negative',
    message,
  });
}
