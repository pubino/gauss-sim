export type Cells = (string | number)[][];
export type Result = [values: number[][], display: string[][]];
export type Operation =
  | { action: "generate" | "clear"; rows: number; cols: number; seed: number }
  | { action: "pivot"; matrix: Cells; row: number; col: number }
  | { action: "swap"; matrix: Cells; row: number; other: number }
  | { action: "display"; matrix: Cells };
export type Request = { id: number; parameters: Operation & { display: string } };
export type Reply =
  | { type: "ready" | "fatal" }
  | { type: "result"; id: number; result: Result }
  | { type: "error"; id: number; message: string };
export interface WorkerScope {
  importScripts(url: string): void;
  location: Location;
  loadPyodide(options: { indexURL: string }): Promise<{
    FS: { writeFile(path: string, contents: string): void };
    runPython(code: string): (parameters: string) => {
      toJs(options: { create_pyproxies: false }): Result;
      destroy(): void;
    };
  }>;
  onmessage: (event: MessageEvent<Request>) => void;
  postMessage(message: Reply): void;
}
