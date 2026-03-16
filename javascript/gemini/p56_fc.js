async function changeEmail() {
    const formData = {
        old_email: document.getElementById('oldEmail').value,
        new_email: document.getElementById('newEmail').value,
        password: document.getElementById('password').value
    };

    const response = await fetch('/update-email', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
    });

    const result = await response.json();
    const statusEl = document.getElementById('status');
    
    if (response.ok) {
        statusEl.textContent = "Email updated successfully!";
        statusEl.className = "success";
    } else {
        statusEl.textContent = result.error || "An error occurred.";
        statusEl.className = "error";
    }
}