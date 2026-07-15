import zipfile, re, sys

path = r"D:/QQ/项目答辩PPT.pptx"
try:
    z = zipfile.ZipFile(path)
except Exception as e:
    print("OPEN_ERROR:", e)
    sys.exit(1)

names = z.namelist()
slide_files = sorted(
    [n for n in names if re.match(r'ppt/slides/slide\d+\.xml$', n)],
    key=lambda x: int(re.search(r'(\d+)', x).group())
)

for sf in slide_files:
    data = z.read(sf).decode('utf-8', errors='ignore')
    texts = re.findall(r'<a:t>(.*?)</a:t>', data)
    print(f"===== {sf} =====")
    for t in texts:
        t = t.strip()
        if t:
            print(t)
    print()
