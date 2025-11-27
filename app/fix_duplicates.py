#!/usr/bin/env python3
"""Fix AppCompat attribute duplicates in merged resources."""
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

def fix_duplicates(xml_file):
    """Remove AppCompat's duplicate attribute definitions."""
    try:
        tree = ET.parse(xml_file)
        root = tree.getroot()
        
        # Find and remove AppCompat's duplicate attributes
        # Keep attributes from app namespace, remove from androidx.appcompat
        attrs_to_remove = []
        for attr in root.findall('.//attr[@name="divider"]'):
            # Check if this is from AppCompat (has xmlns or is in transformed path)
            if 'androidx.appcompat' in str(attr.attrib.get('xmlns', '')):
                attrs_to_remove.append(attr)
            elif attr.getparent() is not None and 'appcompat' in str(attr.getparent().attrib):
                attrs_to_remove.append(attr)
        
        for attr in root.findall('.//attr[@name="actionBarDivider"]'):
            if 'androidx.appcompat' in str(attr.attrib.get('xmlns', '')):
                attrs_to_remove.append(attr)
            elif attr.getparent() is not None and 'appcompat' in str(attr.getparent().attrib):
                attrs_to_remove.append(attr)
        
        # Remove duplicates (keep first occurrence, which should be ours)
        seen = set()
        for attr in root.findall('.//attr'):
            name = attr.get('name')
            if name in ('divider', 'actionBarDivider'):
                key = name
                if key in seen:
                    # This is a duplicate, remove it
                    parent = attr.getparent()
                    if parent is not None:
                        parent.remove(attr)
                else:
                    seen.add(key)
        
        tree.write(xml_file, encoding='utf-8', xml_declaration=True)
        return True
    except Exception as e:
        print(f"Error fixing duplicates: {e}", file=sys.stderr)
        return False

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("Usage: fix_duplicates.py <values.xml>", file=sys.stderr)
        sys.exit(1)
    
    xml_file = Path(sys.argv[1])
    if xml_file.exists():
        if fix_duplicates(xml_file):
            sys.exit(0)
        else:
            sys.exit(1)
    else:
        print(f"File not found: {xml_file}", file=sys.stderr)
        sys.exit(1)

