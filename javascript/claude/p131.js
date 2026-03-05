// Function to render response from environment
function renderResponseFromEnv(env) {
    // Get the name parameter from the user request
    const name = new URLSearchParams(window.location.search).get('name') || 'World';
    
    // Get the template from the environment
    const template = env.getTemplate('template.html');
    
    // Render the template with the name parameter from the request
    const renderedContent = template.render({ name: name });
    
    // Return a response with the rendered template
    return renderedContent;
}

// Home function that calls renderResponseFromEnv
function home(env) {
    // Call the renderResponseFromEnv function with the env variable
    const response = renderResponseFromEnv(env);
    
    // Return the response
    return response;
}

// Example usage
// const environment = new TemplateEnvironment();
// const result = home(environment);
