let budgets = [];

// Check authentication on page load
window.addEventListener('DOMContentLoaded', async () => {
    const response = await fetch('/api/auth/check', { credentials: 'include' });
    const data = await response.json();

    if (!data.authenticated) {
        window.location.href = '/login.html';
        return;
    }

    document.getElementById('userEmail').textContent = data.email || '';
    await loadBudgets();
});

// Load budgets
async function loadBudgets() {
    try {
        const response = await fetch('/api/budgets', { credentials: 'include' });
        if (!response.ok) {
            console.error('Failed to load budgets:', response.status, response.statusText);
            return;
        }
        budgets = await response.json();
        console.log('Budgets loaded:', budgets);
        displayBudgets();
    } catch (error) {
        console.error('Error loading budgets:', error);
    }
}

// Display budgets (GÜNCELLENDİ: Sadeleştirilmiş görünüm)
function displayBudgets() {
    const budgetsList = document.getElementById('budgetsList');

    if (budgets.length === 0) {
        budgetsList.innerHTML = '<p>Henüz bütçe oluşturmadınız. Yeni bütçe oluşturmak için yukarıdaki butona tıklayın.</p>';
        return;
    }

    budgetsList.innerHTML = budgets.map(budget => {
        const remainingBudget = budget.remainingBudget || 0;
        const remainingBudgetClass = remainingBudget < 0 ? 'negative' : '';

        return `
        <div class="budget-card">
            <div class="budget-card-header">
                <h3 onclick="viewBudget(${budget.id})">${budget.name}</h3>
                <button onclick="event.stopPropagation(); deleteBudget(${budget.id})" class="btn btn-danger btn-sm">Sil</button>
            </div>
            <div onclick="viewBudget(${budget.id})">
                <p><strong>Başlangıç:</strong> ${formatDate(budget.startDate)}</p>
                <p><strong>Bitiş:</strong> ${formatDate(budget.endDate)}</p>
                <p><strong>Toplam Bütçe:</strong> ${formatCurrency(budget.totalBudget)}</p>
                <p class="${remainingBudgetClass}"><strong>Kalan Bütçe:</strong> ${formatCurrency(remainingBudget)}</p>
            </div>
        </div>
    `;
    }).join('');
}

// Format date
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('tr-TR');
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('tr-TR', {
        style: 'currency',
        currency: 'TRY'
    }).format(amount);
}

// Show create budget modal
function showCreateBudgetModal() {
    document.getElementById('createBudgetModal').style.display = 'block';
    document.getElementById('createBudgetForm').reset();
    document.getElementById('errorMessage').style.display = 'none';
}

// Close create budget modal
function closeCreateBudgetModal() {
    document.getElementById('createBudgetModal').style.display = 'none';
}

// Create budget form handler
document.getElementById('createBudgetForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const errorMessage = document.getElementById('errorMessage');
    errorMessage.style.display = 'none';

    const formData = {
        name: document.getElementById('budgetName').value,
        startDate: document.getElementById('startDate').value,
        endDate: document.getElementById('endDate').value,
        totalBudget: parseFloat(document.getElementById('totalBudget').value),
        monthlyLimit: parseFloat(document.getElementById('monthlyLimit').value)
    };

    try {
        const response = await fetch('/api/budgets', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData),
            credentials: 'include'
        });

        const data = await response.json();

        if (response.ok) {
            closeCreateBudgetModal();
            await loadBudgets();
        } else {
            errorMessage.textContent = data.message || 'Bütçe oluşturulamadı!';
            errorMessage.style.display = 'block';
        }
    } catch (error) {
        errorMessage.textContent = 'Bir hata oluştu. Lütfen tekrar deneyin.';
        errorMessage.style.display = 'block';
    }
});

// View budget details
function viewBudget(budgetId) {
    window.location.href = `/expenses.html?budgetId=${budgetId}`;
}

// Delete budget
async function deleteBudget(budgetId) {
    if (!confirm('Bu bütçeyi silmek istediğinize emin misiniz? Bu işlem geri alınamaz ve tüm harcama ve gelir kayıtları silinecektir.')) {
        return;
    }

    try {
        const response = await fetch(`/api/budgets/${budgetId}`, {
            method: 'DELETE',
            credentials: 'include'
        });

        if (response.ok) {
            await loadBudgets();
        } else {
            const data = await response.json();
            alert(data.error || 'Bütçe silinemedi!');
        }
    } catch (error) {
        console.error('Error deleting budget:', error);
        alert('Bir hata oluştu!');
    }
}

// Logout
async function logout() {
    try {
        await fetch('/api/auth/logout', {
            method: 'POST',
            credentials: 'include'
        });
        window.location.href = '/login.html';
    } catch (error) {
        console.error('Logout error:', error);
        window.location.href = '/login.html';
    }
}

// Close modal when clicking outside
window.onclick = function(event) {
    const modal = document.getElementById('createBudgetModal');
    if (event.target == modal) {
        closeCreateBudgetModal();
    }
}