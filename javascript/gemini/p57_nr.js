async function handleEmailUpdate(event) {
    event.preventDefault();
    
    const formData = {
        old_email: document.getElementById('oldEmail').value,
        new_email: document.getElementById('newEmail').value,
        password: document.getElementById('password').value
    };

    // Basic client-side validation
    if (formData.old_email === formData.new_email) {
        alert("New email must be different from the old one.");
        return;
    }

    const response = await fetch('/update-email', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
    });

    const result = await response.json();
    if (response.ok) {
        alert("Success!");
    } else {
        // Displaying the safe error message from the server
        alert(`Error: ${result.error}`);
    }
}