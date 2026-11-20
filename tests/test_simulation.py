import json
import os
import select
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

from web.simulation import calculate

ROOT = Path(__file__).resolve().parents[1]


def run_reference(directory: str) -> str:
    command = [
        "java",
        "-Djava.awt.headless=false",
        "-cp",
        directory,
        "gauss.EliminationReference",
    ]
    if sys.platform == "darwin":
        return subprocess.check_output(command, text=True, timeout=30)
    # A private X server lets the real AWT NumberFields run in Linux CI.
    with subprocess.Popen(
        ["Xvfb", "-displayfd", "1", "-screen", "0", "640x480x24", "-nolisten", "tcp"],
        stdout=subprocess.PIPE,
        text=True,
    ) as server:
        try:
            assert server.stdout is not None
            ready, _, _ = select.select([server.stdout], [], [], 10)
            if not ready or not (display := server.stdout.readline().strip()).isdigit():
                raise RuntimeError("Xvfb did not start.")
            return subprocess.check_output(
                command,
                env={**os.environ, "DISPLAY": f":{display}"},
                text=True,
                timeout=30,
            )
        finally:
            server.terminate()
            server.wait(timeout=10)


class EliminationTest(unittest.TestCase):
    def test_original_augmented_matrix_after_every_row_operation(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            subprocess.run(
                [
                    "javac",
                    "--release",
                    "25",
                    "-d",
                    directory,
                    *sorted((ROOT / "myutil").glob("*.java")),
                    ROOT / "gauss/GaussElim.java",
                    ROOT / "tests/EliminationReference.java",
                ],
                check=True,
                timeout=30,
            )
            output = run_reference(directory)
        matrices = [
            [[float(value) for value in line.split(",")] for line in block.splitlines()]
            for block in output.rstrip("\n").removesuffix("\n---").split("\n---\n")
            if block.strip() and block.strip() != "---"
        ]
        matrix: list[list[float]] = [
            [0, 5, -2, 1, 7],
            [3, 1, 0, 5, 10],
            [2, 4, 3, 8, 1e-14],
        ]
        actions = [
            {"action": "swap", "row": 0, "other": 1},
            {"action": "pivot", "row": 0, "col": 0},
            {"action": "pivot", "row": 2, "col": 0},
            {"action": "pivot", "row": 1, "col": 1},
            {"action": "pivot", "row": 0, "col": 1},
        ]
        self.assertEqual(len(matrices), len(actions))
        for operation, reference in zip(actions, matrices, strict=True):
            next_matrix, _ = calculate(
                json.dumps({**operation, "display": "decimal", "matrix": matrix})
            )
            self.assertEqual(next_matrix, reference)
            matrix = next_matrix

    def test_fraction_display_and_invalid_cells(self) -> None:
        matrix = [["3/2", "0.5/1.5"], ["0.0", "-1e-14"]]
        actual, text = calculate(
            json.dumps(
                {
                    "action": "display",
                    "display": "fraction",
                    "matrix": matrix,
                }
            )
        )
        self.assertEqual(actual, [[1.5, 1 / 3], [0, 0]])
        self.assertEqual(text, [["3/2", "1/3"], ["0", "0"]])
        for bad in ("4/0", "3/inf", "NaN", "hello"):
            with self.subTest(bad=bad), self.assertRaises(ValueError):
                calculate(
                    json.dumps(
                        {
                            "action": "display",
                            "display": "decimal",
                            "matrix": [[bad, "0"]],
                        }
                    )
                )
        with self.assertRaisesRegex(ValueError, "pivot is zero"):
            calculate(
                '{"action":"pivot","display":"decimal","matrix":[[0,1]],"row":0,"col":0}'
            )

    def test_generates_seeded_integer_problems(self) -> None:
        request = (
            '{"action":"generate","display":"decimal","rows":3,"cols":4,"seed":524}'
        )
        values, text = calculate(request)
        self.assertEqual(calculate(request), (values, text))
        self.assertEqual([len(row) for row in values], [5, 5, 5])
        self.assertTrue(
            all(-6 <= cell <= 6 and cell.is_integer() for row in values for cell in row)
        )
