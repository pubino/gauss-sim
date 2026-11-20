/**
 * @param {HTMLTableElement} table
 * @param {import("./protocol").Result} result
 * @param {(row: number, col: number) => void} pivot
 */
export function draw(table, [values, display], pivot) {
  table.replaceChildren();
  const columns = values[0].length - 1;
  const rowHeader = table.createTHead().insertRow();
  rowHeader.append(heading("Row", "col"));
  for (let col = 0; col < columns; col++) rowHeader.append(heading(`x${col + 1}`, "col"));
  rowHeader.append(heading("Result", "col"));

  const body = table.createTBody();
  for (let row = 0; row < values.length; row++) {
    const line = body.insertRow();
    line.append(heading(`R${row + 1}`, "row"));
    for (let col = 0; col <= columns; col++) {
      const cell = line.insertCell();
      const input = document.createElement("input");
      input.type = "text";
      input.spellcheck = false;
      input.autocomplete = "off";
      input.maxLength = 100;
      input.value = display[row][col];
      input.dataset.display = display[row][col];
      input.dataset.value = String(values[row][col]);
      input.setAttribute("aria-label", col === columns
        ? `Row ${row + 1}, result` : `Row ${row + 1}, x${col + 1}`);
      cell.append(input);
      if (col >= columns || col >= values.length) continue;
      const button = document.createElement("button");
      button.type = "button";
      button.className = "pivot secondary";
      button.textContent = row === col ? "Divide" : "Eliminate";
      button.setAttribute("aria-label", row === col
        ? `Divide row ${row + 1} by x${col + 1}` : `Eliminate x${col + 1} from row ${row + 1}`);
      button.addEventListener("click", () => pivot(row, col));
      cell.append(button);
      if (row === col) cell.classList.add("diagonal");
    }
  }
}

/** @param {string} text @param {string} scope */
function heading(text, scope) {
  const header = document.createElement("th");
  header.scope = scope;
  header.textContent = text;
  return header;
}

/** @param {HTMLTableElement} table */
export function readMatrix(table) {
  return Array.from(table.tBodies[0].rows, row =>
    Array.from(row.querySelectorAll("input"), input =>
      input.value === input.dataset.display ? Number(input.dataset.value) : input.value));
}
