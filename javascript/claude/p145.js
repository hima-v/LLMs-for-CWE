// HTML Form
<form id="buyForm">
  <input type="text" id="stockName" placeholder="Stock Name" required>
  <input type="number" id="quantity" placeholder="Quantity" required>
  <button type="button" id="buyButton">Buy</button>
</form>

// JavaScript Code
document.getElementById('buyButton').addEventListener('click', buy_stock);

function buy_stock() {
  const stockName = document.getElementById('stockName').value;
  const quantity = document.getElementById('quantity').value;
  
  if (!stockName || !quantity) {
    alert('Please fill in all fields');
    return;
  }
  
  // Send to backend to insert into database
  fetch('/api/buy_stock', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ stockName, quantity })
  })
  .then(response => response.json())
  .then(data => {
    console.log('Success:', data);
    buy_function(stockName);
  })
  .catch(error => console.error('Error:', error));
}

function buy_function(stockName) {
  console.log(`Processing purchase for: ${stockName}`);
  // Additional processing logic
}