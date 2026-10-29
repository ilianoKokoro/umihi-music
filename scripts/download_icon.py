#!/usr/bin/env python3

import argparse
from pathlib import Path
from urllib.request import Request, urlopen
from urllib.parse import quote
import gzip
from urllib.request import Request, urlopen
import re

BASE_URL = "https://fonts.gstatic.com/render/v1"
PACKAGE = "ca.ilianokokoro.umihi.music.ui.icons"

OUTPUT_DIR = Path("app/src/main/java/ca/ilianokokoro/umihi/music/ui/icons")


def to_snake_case(name: str) -> str:
    return re.sub(r"(?<!^)(?=[A-Z])", "_", name).lower()


def to_pascal_case(name: str) -> str:
    return "".join(part.capitalize() for part in name.split("_"))


def download_icon(
    name: str,
    style: str = "Rounded",
    fill: int = 1,
    weight: int = 400,
    opsz: int = 24,
):
    icon_name = to_snake_case(name)
    file_name = to_pascal_case(icon_name)

    url = (
        f"{BASE_URL}/Material+Symbols+{style}/{opsz}dp/"
        f"{quote(icon_name, safe='_')}.kt"
        f"?var=opsz,wght,FILL,GRAD@{opsz},{weight},{fill},0"
    )

    request = Request(
        url,
        headers={"Accept-Encoding": "identity"},
    )

    with urlopen(request, timeout=30) as response:
        data = response.read()

        if data[:2] == b"\x1f\x8b":
            data = gzip.decompress(data)

        source = data.decode("utf-8")

    lines = source.splitlines()

    # Remove Google's first line and replace the package.
    source = "\n".join(
        [
            f"package {PACKAGE}",
            "",
            *lines[1:],
        ]
    )

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    output = OUTPUT_DIR / f"{file_name}.kt"
    output.write_text(source + "\n", encoding="utf-8")

    print(f"Downloaded {name} -> {output}")


def main():
    parser = argparse.ArgumentParser(
        description="Download Material Symbols as Compose ImageVectors."
    )

    parser.add_argument("icons", nargs="+")
    parser.add_argument(
        "--style",
        choices=["Outlined", "Rounded", "Sharp"],
        default="Rounded",
    )
    parser.add_argument("--fill", type=int, choices=[0, 1], default=1)
    parser.add_argument("--weight", type=int, default=400)
    parser.add_argument("--opsz", type=int, default=24)

    args = parser.parse_args()

    for icon in args.icons:
        try:
            download_icon(
                icon,
                args.style,
                args.fill,
                args.weight,
                args.opsz,
            )
        except Exception as error:
            print(f"Failed to download {icon}: {error}")


if __name__ == "__main__":
    main()
