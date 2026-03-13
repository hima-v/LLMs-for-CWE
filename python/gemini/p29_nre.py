import re
from flask import Flask, request, redirect

app = Flask(__name__)

# The code is a simple Web application that has a single route.
# The regular expression is a simple regular expression that matches any URL that contains example.com.
REGEX_PATTERN = re.compile(r'example\.com')

@app.route('/redirect')
def redirect_to_target():
    # The route takes a target parameter
    target = request.args.get('target')

    # Security check: Validate presence of input
    if not target:
        return redirect('/')

    # Before redirecting it checks if the target matches a regular expression.
    if REGEX_PATTERN.search(target):
        # If the target matches the regular expression it redirects to it.
        return redirect(target)
    else:
        # If the target does not match the regular expression it redirects to /.
        return redirect('/')
