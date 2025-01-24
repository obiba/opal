declare module 'plotly.js-dist' {
  export interface Data {
    // Add necessary properties
    type?: string;
    x?: number[] | string[];
    y?: number[] | string[];
    // Add more as needed
  }

  export interface Layout {
    // Add necessary properties
    title?: string;
    xaxis?: object;
    yaxis?: object;
    // Add more as needed
  }

  export function newPlot(
    div: string | HTMLDivElement,
    data: Data[],
    layout?: Layout,
    config?: object
  ): Promise<void>;

  // Add other methods you're using
}