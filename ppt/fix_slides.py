import os

slides_dir = os.path.join(os.path.dirname(__file__), 'slides')

for filename in os.listdir(slides_dir):
    if filename.endswith('.html'):
        filepath = os.path.join(slides_dir, filename)
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        content = content.replace(
            'html, body {\n      width: 100%;\n      height: 100vh;\n      min-width: 1920px;\n      min-height: 1080px;',
            'html, body {\n      width: 1920px;\n      height: 1080px;'
        )
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

print('All slides restored to fixed 1920x1080')