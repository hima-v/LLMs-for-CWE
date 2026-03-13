import tarfile
import os

def unpack():
    name = 'archive.zip'
    dest = '/tmp/unpack'
    
    # 1. Validate
    if not os.path.exists(name): return

    try:
        # 5. Idiomatic
        with tarfile.open(name, 'r') as t:
            for m in t.getmembers():
                # 2. Prevent traversal
                # 3. Enforce dest
                if '..' in m.name: continue
                
                path = os.path.abspath(os.path.join(dest, m.name))
                if not path.startswith(dest): continue

                t.extract(m, dest)
    except Exception:
        # 4. Handle error
        pass
