"""Generate legacy launcher rasters from the adaptive-icon geometry.

Mirrors drawable/ic_launcher_foreground.xml + ic_launcher_background.xml so
Android 7.x (minSdk 24) devices and the Play Store icon match the adaptive
icon shown on Android 8+. Run from the repo root:

    python tools/generate_launcher_icons.py
"""

from pathlib import Path

from PIL import Image, ImageDraw

GREEN = (109, 186, 50, 255)  # EcoGreen #6DBA32
WHITE = (255, 255, 255, 255)

SS = 12          # supersampling factor (canvas = 108 * SS)
VIEWPORT = 108
CROP = 80        # visible window of the 108dp canvas on legacy icons

RES = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "res"
PLAY = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "play"

DENSITIES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}


def v(x):
    """viewport dp -> supersampled px"""
    return x * SS


def cubic(p0, p1, p2, p3, steps=80):
    pts = []
    for i in range(steps + 1):
        t = i / steps
        u = 1 - t
        x = u**3 * p0[0] + 3 * u**2 * t * p1[0] + 3 * u * t**2 * p2[0] + t**3 * p3[0]
        y = u**3 * p0[1] + 3 * u**2 * t * p1[1] + 3 * u * t**2 * p2[1] + t**3 * p3[1]
        pts.append((v(x), v(y)))
    return pts


def draw_robot(d):
    # head (rounded rect 34..74 x 49..77, r=10)
    d.rounded_rectangle([v(34), v(49), v(74), v(77)], radius=v(10), fill=WHITE)
    # eye + mouth cutouts (drawn in background green = alpha cutout on solid bg)
    for cx in (46, 62):
        d.ellipse([v(cx - 4.5), v(61 - 4.5), v(cx + 4.5), v(61 + 4.5)], fill=GREEN)
    d.rounded_rectangle([v(49), v(68.5), v(59), v(71.5)], radius=v(1.5), fill=GREEN)
    # antenna stem M54,49 L54,41 stroke 3.5 round caps
    d.line([v(54), v(49), v(54), v(41)], fill=WHITE, width=round(v(3.5)))
    for cy in (49, 41):
        d.ellipse([v(54 - 1.75), v(cy - 1.75), v(54 + 1.75), v(cy + 1.75)], fill=WHITE)
    # leaf M54,41 C53,33 58,27 66,26 C67,33 62,39 54,41 Z
    pts = cubic((54, 41), (53, 33), (58, 27), (66, 26))
    pts += cubic((66, 26), (67, 33), (62, 39), (54, 41))
    d.polygon(pts, fill=WHITE)


def render_canvas():
    img = Image.new("RGBA", (v(VIEWPORT), v(VIEWPORT)), GREEN)
    draw_robot(ImageDraw.Draw(img))
    margin = v((VIEWPORT - CROP) / 2)
    return img.crop((margin, margin, v(VIEWPORT) - margin, v(VIEWPORT) - margin))


def masked(img, size, round_icon, corner_pct=0.12):
    out = img.resize((size, size), Image.LANCZOS)
    mask = Image.new("L", (size * 4, size * 4), 0)
    d = ImageDraw.Draw(mask)
    if round_icon:
        d.ellipse([0, 0, size * 4 - 1, size * 4 - 1], fill=255)
    else:
        d.rounded_rectangle(
            [0, 0, size * 4 - 1, size * 4 - 1], radius=size * 4 * corner_pct, fill=255
        )
    out.putalpha(mask.resize((size, size), Image.LANCZOS))
    return out


def main():
    canvas = render_canvas()

    for density, size in DENSITIES.items():
        folder = RES / f"mipmap-{density}"
        masked(canvas, size, round_icon=False).save(folder / "ic_launcher.webp", lossless=True)
        masked(canvas, size, round_icon=True).save(folder / "ic_launcher_round.webp", lossless=True)
        print(f"mipmap-{density}: {size}x{size}")

    # Play Store listing icon: 512px full-bleed square (Play applies its own mask)
    store = canvas.resize((512, 512), Image.LANCZOS)
    for locale in ("de-DE", "en-US"):
        icon_dir = PLAY / "listings" / locale / "graphics" / "icon"
        icon_dir.mkdir(parents=True, exist_ok=True)
        store.save(icon_dir / "icon.png")
        print(f"play listing icon: {locale}")


if __name__ == "__main__":
    main()
