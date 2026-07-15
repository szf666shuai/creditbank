import os
from PIL import Image

png_dir = r"D:\creditbank-1\deck-export\png"
out_pdf = r"D:\creditbank-1\deck-export\星秩存册-界面设计答辩.pdf"

# Logical order: node ids 12:2 .. 12:29  ->  P1 .. P28
files = []
for n in range(2, 30):
    p = os.path.join(png_dir, f"12_{n}.png")
    if os.path.exists(p):
        files.append(p)
    else:
        print("MISSING:", p)

print(f"Found {len(files)} slide images")

imgs = []
for p in files:
    im = Image.open(p).convert("RGB")
    # Keep native 2x resolution for crisp text; embed as-is.
    imgs.append(im)

if imgs:
    imgs[0].save(
        out_pdf, "PDF",
        resolution=300.0,
        save_all=True,
        append_images=imgs[1:],
    )
    size_mb = os.path.getsize(out_pdf) / (1024 * 1024)
    print(f"Saved PDF -> {out_pdf}")
    print(f"Pages: {len(imgs)} | Size: {size_mb:.1f} MB")
else:
    print("No images found, PDF not created.")
