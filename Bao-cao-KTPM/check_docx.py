import zipfile
import xml.etree.ElementTree as ET

def extract_text(docx_path):
    try:
        with zipfile.ZipFile(docx_path) as docx:
            xml_content = docx.read('word/document.xml')
            tree = ET.fromstring(xml_content)
            namespace = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}
            paragraphs = []
            for m in tree.findall('.//w:t', namespace):
                if m.text:
                    paragraphs.append(m.text)
            return ''.join(paragraphs)
    except Exception as e:
        return str(e)

files_to_check = ['KẾ HOẠCH KIỂM THỬ PHẦN MỀM-GK.docx', 'BÁO CÁO TÓM TẮT KIỂM THỬ-GK.docx', 'CÁC TRƯỜNG HỢP KIỂM THỬ-GK.docx']

for f in files_to_check:
    print(f'--- {f} ---')
    text = extract_text(f)
    print(text[:1000])
