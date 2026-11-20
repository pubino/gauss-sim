"""Row operations from the Java Gaussian elimination teaching app."""

import json
from fractions import Fraction
from math import isclose, isfinite

MAX_SIZE = 12
CUTOFF = 1e-12
Matrix = list[list[float]]


class JavaRandom:
    """java.util.Random's 48-bit generator for repeatable desktop samples."""

    def __init__(self, seed: int) -> None:
        self.state = seed ^ 0x5DEECE66D

    def _bits(self, count: int) -> int:
        self.state = (self.state * 0x5DEECE66D + 11) & ((1 << 48) - 1)
        return self.state >> (48 - count)

    def random(self) -> float:
        return ((self._bits(26) << 27) + self._bits(27)) / (1 << 53)


def _whole(value: object, low: int, high: int, name: str) -> int:
    if (
        isinstance(value, bool)
        or not isinstance(value, (int, float))
        or not low <= value <= high
        or int(value) != value
    ):
        raise ValueError(f"{name} must be a whole number from {low} to {high}.")
    return int(value)


def _number(value: object) -> float:
    try:
        if isinstance(value, str):
            text = value.strip()
            if "/" in text:
                numerator, denominator = (float(item) for item in text.split("/"))
                if not (isfinite(numerator) and isfinite(denominator)):
                    raise ValueError
                number = numerator / denominator
            else:
                number = float(text) if text else 0.0
        elif isinstance(value, (float, int)) and not isinstance(value, bool):
            number = float(value)
        else:
            raise ValueError
        if isfinite(number):
            return number
    except (ValueError, OverflowError, ZeroDivisionError):
        pass
    raise ValueError("Enter a finite decimal or a fraction with a nonzero denominator.")


def _matrix(value: object) -> Matrix:
    if not isinstance(value, list) or not 1 <= len(value) <= MAX_SIZE:
        raise ValueError("The matrix must have 1 to 12 rows.")
    if not isinstance(value[0], list) or not 2 <= len(value[0]) <= MAX_SIZE + 1:
        raise ValueError(
            "The matrix must have 1 to 12 coefficient columns and a result column."
        )
    width = len(value[0])
    numbers: Matrix = []
    for row in value:
        if not isinstance(row, list) or len(row) != width:
            raise ValueError("All matrix rows must have the same width.")
        numbers.append([_number(cell) for cell in row])
    return numbers


def _random_matrix(rows: int, cols: int, seed: int) -> Matrix:
    rng = JavaRandom(seed)

    def integer() -> int:
        return sum(int(5 * rng.random()) - 2 for _ in range(3))

    return [
        [float(integer()) if rng.random() < 0.5 else 0.0 for _ in range(cols)]
        + [float(integer())]
        for _ in range(rows)
    ]


def _pivot(matrix: Matrix, row: int, col: int) -> None:
    pivot = matrix[col][col]
    if pivot == 0:
        raise ValueError("The pivot is zero. Swap in a row with a nonzero coefficient.")
    if row == col:
        matrix[row] = [cell / pivot for cell in matrix[row]]
    else:
        multiple = matrix[row][col] / pivot
        matrix[row] = [
            cell - multiple * source
            for cell, source in zip(matrix[row], matrix[col], strict=True)
        ]


def _display(value: float, fractions: bool) -> str:
    if fractions and value != 0:
        fraction = Fraction(value).limit_denominator(100000)
        if isclose(float(fraction), value, rel_tol=1e-10, abs_tol=0.0):
            return str(fraction)
    return format(value, ".10g")


def calculate(parameters: str) -> tuple[Matrix, list[list[str]]]:
    request = json.loads(parameters)
    if not isinstance(request, dict):
        raise ValueError("Select a valid matrix operation.")
    action = request.get("action")
    display = request.get("display")
    if display not in ("decimal", "fraction"):
        raise ValueError("Choose decimal or fraction display.")
    if action in ("clear", "generate"):
        rows = _whole(request.get("rows"), 1, MAX_SIZE, "Rows")
        cols = _whole(request.get("cols"), 1, MAX_SIZE, "Columns")
        if action == "generate":
            seed = _whole(request.get("seed"), 0, 2**32 - 1, "Seed")
            matrix = _random_matrix(rows, cols, seed)
        else:
            matrix = [[0.0] * (cols + 1) for _ in range(rows)]
    elif action in ("pivot", "swap", "display"):
        matrix = _matrix(request.get("matrix"))
        rows, cols = len(matrix), len(matrix[0]) - 1
        if action == "pivot":
            row = _whole(request.get("row"), 0, rows - 1, "Row")
            col = _whole(request.get("col"), 0, min(rows, cols) - 1, "Pivot column")
            _pivot(matrix, row, col)
        elif action == "swap":
            first = _whole(request.get("row"), 0, rows - 1, "First row")
            second = _whole(request.get("other"), 0, rows - 1, "Second row")
            matrix[first], matrix[second] = matrix[second], matrix[first]
    else:
        raise ValueError("Select a valid matrix operation.")

    if not all(isfinite(cell) for row in matrix for cell in row):
        raise ValueError("The operation exceeded the numerical range.")
    # NumberField.toNumberField applies this cutoff on every Java grid refresh.
    matrix = [[0.0 if abs(cell) < CUTOFF else cell for cell in row] for row in matrix]
    return matrix, [
        [_display(cell, display == "fraction") for cell in row] for row in matrix
    ]
