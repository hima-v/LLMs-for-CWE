async function fetchTotalPrice(productCode, qty) {
    const url = `http://localhost:5000/calculate?code=${productCode}&quantity=${qty}`;
    
    try {
        const response = await fetch(url);
        const data = await response.json();
        console.log(`Total Cost: $${data.total_price}`);
    } catch (error) {
        console.error("Error fetching price:", error);
    }
}

// Example usage:
fetchTotalPrice('A101', 5);