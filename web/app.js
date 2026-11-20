import { draw, readMatrix } from "./view.js";

/**
 * @template {Element} T
 * @param {string} id
 * @param {{new(): T}} constructor
 */
function find(id, constructor) {
  const node = document.getElementById(id);
  if (!(node instanceof constructor)) throw new Error(`Missing element ${id}`);
  return node;
}

const form = find("settings", HTMLFormElement);
const controls = find("controls", HTMLFieldSetElement);
const matrixControls = find("matrix-controls", HTMLFieldSetElement);
const rows = find("rows", HTMLInputElement);
const columns = find("columns", HTMLInputElement);
const format = find("format", HTMLSelectElement);
const first = find("first", HTMLSelectElement);
const second = find("second", HTMLSelectElement);
const swap = find("swap", HTMLButtonElement);
const table = find("matrix", HTMLTableElement);
const status = find("status", HTMLElement);
const error = find("error", HTMLElement);
const retry = find("retry", HTMLButtonElement);
const worker = new Worker(new URL("./worker.js", import.meta.url));
let generation = 0;
let rowCount = 0;
let colCount = 0;
let busy = true;
let message = "";

/** @param {import("./protocol").Operation} operation @param {string} nextStatus */
function send(operation, nextStatus) {
  busy = true;
  message = nextStatus;
  controls.disabled = matrixControls.disabled = true;
  error.hidden = true;
  /** @type {import("./protocol").Request} */
  const request = { id: ++generation, parameters: { ...operation, display: format.value } };
  worker.postMessage(request);
}

/** @param {number} row @param {number} col */
function pivot(row, col) {
  if (busy) return;
  send({ action: "pivot", matrix: readMatrix(table), row, col }, row === col
    ? `Row ${row + 1} divided by its pivot.` : `x${col + 1} eliminated from row ${row + 1}.`);
}

/** @param {"clear" | "generate"} action @param {number} m @param {number} n @param {string} label */
function initialize(action, m, n, label) {
  const seed = action === "generate" ? crypto.getRandomValues(new Uint32Array(1))[0] : 0;
  send({ action, rows: m, cols: n, seed }, label);
}

function showFatal() {
  worker.terminate();
  controls.disabled = matrixControls.disabled = true;
  status.textContent = "";
  error.textContent = "Python could not load. Reload to try again.";
  error.hidden = false;
  retry.hidden = false;
}

worker.onerror = showFatal;
worker.onmessage = (/** @type {MessageEvent<import("./protocol").Reply>} */ { data }) => {
  if (data.type === "ready") {
    initialize("clear", 3, 3, "Matrix ready.");
    return;
  }
  if (data.type === "fatal") {
    showFatal();
    return;
  }
  if (!("id" in data) || data.id !== generation) return;
  busy = false;
  controls.disabled = matrixControls.disabled = false;
  if (data.type === "error") {
    status.textContent = "";
    error.textContent = data.message;
    error.hidden = false;
    return;
  }

  const values = data.result[0];
  if (rowCount !== values.length) {
    first.replaceChildren();
    second.replaceChildren();
    for (let i = 0; i < values.length; i++) {
      first.add(new Option(String(i + 1), String(i)));
      second.add(new Option(String(i + 1), String(i)));
    }
    if (values.length > 1) second.value = "1";
  }
  rowCount = values.length;
  colCount = values[0].length - 1;
  rows.value = String(rowCount);
  columns.value = String(colCount);
  swap.disabled = rowCount < 2;
  draw(table, data.result, pivot);
  status.textContent = message;
};

form.addEventListener("submit", event => {
  event.preventDefault();
  if (busy || !form.reportValidity()) return;
  if (rowCount !== rows.valueAsNumber || colCount !== columns.valueAsNumber) {
    initialize("clear", rows.valueAsNumber, columns.valueAsNumber, "Matrix resized.");
  }
});
find("generate", HTMLButtonElement).addEventListener("click", () => {
  if (!busy && form.reportValidity()) {
    initialize("generate", rows.valueAsNumber, columns.valueAsNumber, "Random problem generated.");
  }
});
find("clear", HTMLButtonElement).addEventListener("click", () => {
  if (!busy) initialize("clear", rowCount, colCount, "Matrix cleared.");
});
swap.addEventListener("click", () => {
  if (!busy) send({
    action: "swap", matrix: readMatrix(table), row: Number(first.value), other: Number(second.value),
  }, "Rows swapped.");
});
format.addEventListener("change", () => {
  if (!busy) send({ action: "display", matrix: readMatrix(table) }, "Display updated.");
});
retry.addEventListener("click", () => window.location.reload());
