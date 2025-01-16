import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { VariableDto } from 'src/models/Magma';

export const useCartStore = defineStore(
  'cart',
  () => {
    const variables = ref([] as VariableDto[]);

    function reset() {
      variables.value = [];
    }

    async function refresh() {
      if (variables.value.length === 0) return;
      // verify that variables in the cart still exist, are accessible and up to date
      await Promise.all(
        variables.value.map((v, i) =>
          api
            .get(`${v.parentLink?.link}/variable/${v.name}`)
            .then((response) => {
              // replace the variable in the cart with the updated version
              variables.value[i] = response.data;
            })
            .catch(() => {
              // remove the variable from the cart if it no longer exists
              variables.value[i] = {} as VariableDto;
            })
        )
      );
      variables.value = variables.value.filter((v) => v.parentLink !== undefined);
    }

    function addVariables(vars: VariableDto[]) {
      const merged = [...variables.value, ...vars];
      const unique = merged.filter((v, i, a) => a.findIndex((t) => t.link === v.link) === i);
      variables.value = unique;
    }

    function removeVariables(vars: VariableDto[]) {
      variables.value = variables.value.filter((v) => vars.findIndex((t) => t.link === v.link) === -1);
    }

    function isInCart(variable: VariableDto) {
      return variables.value.findIndex((v) => v.link === variable.link) !== -1;
    }

    return {
      variables,
      reset,
      refresh,
      addVariables,
      removeVariables,
      isInCart,
    };
  },
  { persist: true }
);
