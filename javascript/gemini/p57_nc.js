async function handleEmailChange(event) {
    event.preventDefault();
    
    const formData = {
        old_email: document.getElementById('oldEmail').value.trim(),
        new_email: document.getElementById('newEmail').value.trim(),
        password: document.getElementById('password').value
    };

    // Basic client-side validation
    if (formData.old_email === formData.new_email) {
        alert("New email must be different from the old one.");
        return;
    }

    try {
        const response = await fetch('/api/update-email', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });

        const result = await response.json();
        
        if (response.ok) {
            alert("Success!");
        } else {
            // Safe error messaging
            console.error("Update failed:", result.error);
            alert("Check your details and try again.");
        }
    } catch (err) {
        alert("A connection error occurred.");
    }
}