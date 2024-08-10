import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { SqlCommand, SqlResults } from 'src/components/models';

const datasourceStore = useDatasourceStore();

export const useSqlStore = defineStore('sql', () => {
  const history = ref<SqlCommand[]>([]);

  async function execute(statement: string): Promise<SqlResults> {
    const command = {
      query: statement,
      datasource: datasourceStore.datasource.name,
      delay: 0,
    } as SqlCommand;
    const currentTimeMillis = Date.now();
    return api.post(`/datasource/${command.datasource}/_sql`, command.query, {
        headers: {
          'Content-Type': 'text/plain',
        },
      })
      .then((resp) => resp.data as SqlResults)
      .finally(() => {
        command.delay = Date.now() - currentTimeMillis;
        history.value.push(command);
      });
  }

  return {
    history,
    execute,
  };
});
