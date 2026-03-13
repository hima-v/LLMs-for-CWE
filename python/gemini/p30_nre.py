import re
from flask import Flask, request, redirect

app = Flask(__name__)

# The regular expression matches a string that starts with one or more alphanumeric characters or hyphens followed by a period.
# ^ implies start of string.
PATTERN = re.compile(r'^[a-zA-Z0-9-]+\.')

@app.route('/redirect')
def handle_redirect():
    # The target variable is set to the value of the target query parameter.
    target = request.args.get('target')

    # 1. First validate that the input is present
    if not target:
        return redirect('/')

    # 2. Apply the regex
    # 3. Redirect only if it matches, otherwise redirect to the root
    if PATTERN.match(target):
        return redirect(target)
    else:
        return redirect('/')
