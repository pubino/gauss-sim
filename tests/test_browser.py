import threading
import unittest
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

from playwright.sync_api import (
    Browser,
    BrowserContext,
    Page,
    Playwright,
    expect,
    sync_playwright,
)

BUILD = Path(__file__).resolve().parents[1] / "build"


class BrowserTest(unittest.TestCase):
    server: ThreadingHTTPServer
    thread: threading.Thread
    playwright: Playwright
    browser: Browser
    context: BrowserContext
    page: Page

    @classmethod
    def setUpClass(cls) -> None:
        if not (BUILD / "site/index.html").is_file():
            raise RuntimeError("Build the site before running browser tests.")
        # Serve from a subdirectory to match a GitHub project Pages URL.
        cls.server = ThreadingHTTPServer(
            ("127.0.0.1", 0), partial(SimpleHTTPRequestHandler, directory=BUILD)
        )
        cls.thread = threading.Thread(target=cls.server.serve_forever, daemon=True)
        cls.thread.start()
        cls.addClassCleanup(cls.server.server_close)
        cls.addClassCleanup(cls.thread.join, 5)
        cls.addClassCleanup(cls.server.shutdown)
        cls.playwright = sync_playwright().start()
        cls.addClassCleanup(cls.playwright.stop)
        cls.browser = cls.playwright.chromium.launch()
        cls.addClassCleanup(cls.browser.close)

    def setUp(self) -> None:
        self.context = self.browser.new_context()
        self.addCleanup(self.context.close)
        self.page = self.context.new_page()
        self.base_url = f"http://127.0.0.1:{self.server.server_port}/site/"

    def test_divide_eliminate_swap_and_display(self) -> None:
        self.page.goto(self.base_url)
        divide = self.page.get_by_role("button", name="Divide row 1 by x1", exact=True)
        expect(divide).to_be_enabled(timeout=45_000)
        x11 = self.page.get_by_role("textbox", name="Row 1, x1", exact=True)
        x12 = self.page.get_by_role("textbox", name="Row 1, x2", exact=True)
        x21 = self.page.get_by_role("textbox", name="Row 2, x1", exact=True)
        rhs1 = self.page.get_by_role("textbox", name="Row 1, result", exact=True)
        rhs2 = self.page.get_by_role("textbox", name="Row 2, result", exact=True)
        for field, value in [
            (x11, "2"),
            (x12, "1/3"),
            (x21, "4"),
            (rhs1, "1"),
            (rhs2, "7"),
        ]:
            field.fill(value)

        divide.click()
        expect(rhs1).to_have_value("0.5")
        self.page.get_by_label("Display").select_option("fraction")
        expect(rhs1).to_have_value("1/2")
        expect(x12).to_have_value("1/6")
        self.page.get_by_role("button", name="Eliminate x1 from row 2").click()
        expect(rhs2).to_have_value("5")
        expect(x21).to_have_value("0")
        self.page.get_by_role("button", name="Swap rows").click()
        expect(rhs1).to_have_value("5")
        divide.click()
        expect(self.page.get_by_role("alert")).to_contain_text("pivot is zero")
        expect(rhs1).to_have_value("5")

        self.page.get_by_label("Rows", exact=True).fill("2")
        self.page.get_by_label("Columns", exact=True).fill("4")
        self.page.get_by_role("button", name="Resize").click()
        expect(self.page.get_by_role("textbox", name="Row 2, x4")).to_have_value("0")
        expect(
            self.page.get_by_role("button", name="Eliminate x4 from row 2")
        ).to_have_count(0)
        self.page.get_by_role("button", name="Generate problem").click()
        expect(self.page.get_by_role("status")).to_have_text(
            "Random problem generated."
        )
        self.page.get_by_role("button", name="Clear matrix").click()
        expect(self.page.get_by_role("status")).to_have_text("Matrix cleared.")
        expect(self.page.get_by_role("textbox", name="Row 2, x4")).to_have_value("0")
        self.page.set_viewport_size({"width": 390, "height": 844})
        self.assertTrue(
            self.page.evaluate("document.documentElement.scrollWidth <= innerWidth")
        )

    def test_runtime_failure_shows_retry(self) -> None:
        self.page.route("**/vendor/pyodide.js", lambda route: route.abort())
        self.page.goto(self.base_url)
        expect(self.page.get_by_role("alert")).to_contain_text("Python could not load")
        expect(
            self.page.get_by_role("button", name="Reload", exact=True)
        ).to_be_visible()
