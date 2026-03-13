import re
from flask import Flask, request, redirect

app = Flask(__name__)

# 2. Validate using regex (compiled once)
# 3. Only URLs containing example.com
PATTERN = re.compile(r'example\.com')

@app.route('/redirect')
def simple_redirect():
    # 1. Accept target parameter
    target = request.args.get('target')

    # 6. Ensure user-controlled input cannot bypass validation (check existence)
    if not target:
        return redirect('/')

    # 2. Validate using regex
    # 5. Regex used only for validation
    if PATTERN.search(target):
        return redirect(target)
    else:
        # 4. If does not match, redirect to /
        return redirect('/')
