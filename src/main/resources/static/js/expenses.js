let budgetId = null;
let budget = null;
let expenses = [];
let incomes = [];
let plans = [];
let expenseCategories = [];
let incomeCategories = [];

// Check authentication and load data
window.addEventListener('DOMContentLoaded', async () => {
    const response = await fetch('/api/auth/check', { credentials: 'include' });
    const data = await response.json();

    if (!data.authenticated) {
        window.location.href = '/login.html';
        return;
    }

    // Get budget ID from URL
    const urlParams = new URLSearchParams(window.location.search);
    budgetId = urlParams.get('budgetId');

    if (!budgetId) {
        window.location.href = '/budgets.html';
        return;
    }

    // YÜKLEME SIRASI ÖNEMLİ: Hesaplamalar için önce verileri çekmeliyiz
    await loadCategories();     // Önce kategoriler
    await loadExpenses();       // Sonra harcamalar
    await loadIncomes();        // Sonra gelirler (Bütçe hesabına katmak için)
    await loadPlans();          // Sonra planlar
    await loadBudget();         // En son bütçe (Gelirleri dahil edip göstereceğiz)
});

// Load budget details
async function loadBudget() {
    try {
        const response = await fetch(`/api/budgets/${budgetId}`, { credentials: 'include' });
        if (!response.ok) {
            window.location.href = '/budgets.html';
            return;
        }
        budget = await response.json();
        displayBudgetInfo();
    } catch (error) {
        console.error('Error loading budget:', error);
        window.location.href = '/budgets.html';
    }
}

// Display budget info
function displayBudgetInfo() {
    const budgetInfo = document.getElementById('budgetInfo');

    // 1. GELİR HESAPLAMASI:
    // Backend'den gelen 'remainingBudget' muhtemelen gelirleri kapsamıyor.
    // Biz burada manuel olarak toplam geliri hesaplayıp limite ekliyoruz.
    const totalIncome = incomes.reduce((sum, income) => sum + (income.amount || 0), 0);

    // Backend'den gelen kalan bütçeye gelirleri ekle
    // Not: Eğer backend zaten ekliyorsa bu satırı: const remainingBudget = budget.remainingBudget; yap.
    const remainingBudget = (budget.remainingBudget || 0) + totalIncome;

    // Toplam bütçe (Başlangıç parası) + Eklenen Gelirler
    const effectiveTotalBudget = (budget.totalBudget || 0) + totalIncome;

    const remainingMonthlyLimit = budget.remainingMonthlyLimit || 0;

    // Renklendirme sınıfları
    const remainingBudgetClass = remainingBudget < 0 ? 'negative' : 'positive';
    const remainingMonthlyClass = remainingMonthlyLimit < 0 ? 'negative' : 'positive';

    // HTML Yapısı
    budgetInfo.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h2 style="margin: 0;">${budget.name}</h2>
            <div class="date-badge" style="background: #f0f2f5; padding: 5px 10px; border-radius: 5px; font-size: 0.9em;">
                📅 ${formatDate(budget.startDate)} - ${formatDate(budget.endDate)}
            </div>
        </div>

        <div class="stats-container" style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">
            <div class="stat-box" style="background: #f8f9fa; padding: 15px; border-radius: 8px; border-left: 4px solid #4a90e2;">
                <h4 style="margin-top: 0; color: #666;">Genel Bütçe Durumu</h4>
                <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                    <span>Başlangıç Bütçesi:</span>
                    <span>${formatCurrency(budget.totalBudget)}</span>
                </div>
                 <div style="display: flex; justify-content: space-between; margin-bottom: 5px; color: #28a745;">
                    <span>+ Eklenen Gelir:</span>
                    <span>${formatCurrency(totalIncome)}</span>
                </div>
                 <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                    <span>Toplam Harcanan:</span>
                    <span style="color: #dc3545;">-${formatCurrency(budget.totalExpenses || 0)}</span>
                </div>
                 <div style="display: flex; justify-content: space-between; border-top: 1px solid #ddd; padding-top: 5px; margin-top: 5px;">
                    <span>Net Kalan:</span>
                    <strong class="${remainingBudgetClass}" style="font-size: 1.1em;">${formatCurrency(remainingBudget)}</strong>
                </div>
            </div>

            <div class="stat-box" style="background: #f8f9fa; padding: 15px; border-radius: 8px; border-left: 4px solid #28a745;">
                <h4 style="margin-top: 0; color: #666;">Bu Ayın Durumu</h4>
                <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                    <span>Aylık Limit:</span>
                    <strong>${formatCurrency(budget.monthlyLimit)}</strong>
                </div>
                <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                    <span>Bu Ay Harcanan:</span>
                    <span style="color: #dc3545;">-${formatCurrency(budget.monthlyExpenses || 0)}</span>
                </div>
                 <div style="display: flex; justify-content: space-between; border-top: 1px solid #ddd; padding-top: 5px; margin-top: 5px;">
                    <span>Limitten Kalan:</span>
                    <strong class="${remainingMonthlyClass}" style="font-size: 1.1em;">${formatCurrency(remainingMonthlyLimit)}</strong>
                </div>
            </div>
        </div>
    `;
}

// Load categories
async function loadCategories() {
    try {
        const response = await fetch('/api/categories', { credentials: 'include' });
        const categories = await response.json();

        expenseCategories = categories.filter(c => c.type === 'EXPENSE');
        incomeCategories = categories.filter(c => c.type === 'INCOME');

        const expenseSelect = document.getElementById('expenseCategory');
        expenseSelect.innerHTML = '<option value="">Kategori Seçin</option>' +
            expenseCategories.map(cat => `<option value="${cat.id}">${cat.name}</option>`).join('');

        const incomeSelect = document.getElementById('incomeCategory');
        incomeSelect.innerHTML = '<option value="">Kategori Seçin</option>' +
            incomeCategories.map(cat => `<option value="${cat.id}">${cat.name}</option>`).join('');

        const planCategorySelect = document.getElementById('planCategory');
        if(planCategorySelect) {
            planCategorySelect.innerHTML = '<option value="">Kategori Seçin</option>' +
                expenseCategories.map(cat => `<option value="${cat.id}">${cat.name}</option>`).join('');
        }

    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

// Load expenses
async function loadExpenses() {
    try {
        const response = await fetch(`/api/expenses/budget/${budgetId}`, { credentials: 'include' });
        expenses = await response.json();
        displayExpenses();
    } catch (error) {
        console.error('Error loading expenses:', error);
    }
}

// Display expenses
function displayExpenses() {
    const expensesList = document.getElementById('expensesList');

    if (expenses.length === 0) {
        expensesList.innerHTML = '<p>Henüz harcama eklenmedi.</p>';
        return;
    }

    expensesList.innerHTML = expenses.map(expense => `
        <div class="item-card expense">
            <div class="item-info">
                <h4>${expense.category.name}</h4>
                <p>${expense.description || 'Açıklama yok'}</p>
                <p><small>${formatDate(expense.date)}</small></p>
            </div>
            <div class="item-amount">-${formatCurrency(expense.amount)}</div>
            <button onclick="deleteExpense(${expense.id})" class="btn btn-danger btn-sm">Sil</button>
        </div>
    `).join('');
}

// Load incomes
async function loadIncomes() {
    try {
        const response = await fetch(`/api/incomes/budget/${budgetId}`, { credentials: 'include' });
        incomes = await response.json();
        displayIncomes();
    } catch (error) {
        console.error('Error loading incomes:', error);
    }
}

// Display incomes
function displayIncomes() {
    const incomesList = document.getElementById('incomesList');

    if (incomes.length === 0) {
        incomesList.innerHTML = '<p>Henüz gelir eklenmedi.</p>';
        return;
    }

    incomesList.innerHTML = incomes.map(income => `
        <div class="item-card income">
            <div class="item-info">
                <h4>${income.category.name}</h4>
                <p>${income.description || 'Açıklama yok'}</p>
                <p><small>${formatDate(income.date)}</small></p>
            </div>
            <div class="item-amount">+${formatCurrency(income.amount)}</div>
            <button onclick="deleteIncome(${income.id})" class="btn btn-danger btn-sm">Sil</button>
        </div>
    `).join('');
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

// Show add expense modal
function showAddExpenseModal() {
    document.getElementById('addExpenseModal').style.display = 'block';
    document.getElementById('addExpenseForm').reset();
    document.getElementById('expenseDate').value = new Date().toISOString().split('T')[0];
    document.getElementById('errorMessage').style.display = 'none';
}

// Close add expense modal
function closeAddExpenseModal() {
    document.getElementById('addExpenseModal').style.display = 'none';
}

// Add expense form handler
document.getElementById('addExpenseForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const errorMessage = document.getElementById('errorMessage');
    errorMessage.style.display = 'none';

    const formData = {
        amount: parseFloat(document.getElementById('expenseAmount').value),
        description: document.getElementById('expenseDescription').value,
        date: document.getElementById('expenseDate').value,
        budgetId: budgetId,
        categoryId: parseInt(document.getElementById('expenseCategory').value)
    };

    try {
        const response = await fetch('/api/expenses', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData),
            credentials: 'include'
        });

        const data = await response.json();

        if (response.ok) {
            closeAddExpenseModal();
            await loadExpenses();
            await loadBudget();
        } else {
            errorMessage.textContent = data.message || 'Harcama eklenemedi!';
            errorMessage.style.display = 'block';
        }
    } catch (error) {
        errorMessage.textContent = 'Bir hata oluştu. Lütfen tekrar deneyin.';
        errorMessage.style.display = 'block';
    }
});

// Show add income modal
function showAddIncomeModal() {
    document.getElementById('addIncomeModal').style.display = 'block';
    document.getElementById('addIncomeForm').reset();
    document.getElementById('incomeDate').value = new Date().toISOString().split('T')[0];
    document.getElementById('errorMessageIncome').style.display = 'none';
}

// Close add income modal
function closeAddIncomeModal() {
    document.getElementById('addIncomeModal').style.display = 'none';
}

// Add income form handler
document.getElementById('addIncomeForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const errorMessage = document.getElementById('errorMessageIncome');
    errorMessage.style.display = 'none';

    const formData = {
        amount: parseFloat(document.getElementById('incomeAmount').value),
        description: document.getElementById('incomeDescription').value,
        date: document.getElementById('incomeDate').value,
        budgetId: budgetId,
        categoryId: parseInt(document.getElementById('incomeCategory').value)
    };

    try {
        const response = await fetch('/api/incomes', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData),
            credentials: 'include'
        });

        const data = await response.json();

        if (response.ok) {
            closeAddIncomeModal();
            await loadIncomes();
            // Gelir eklenince bütçeyi yeniden yükle ki hesaplama güncellensin
            await loadBudget();
        } else {
            errorMessage.textContent = data.message || 'Gelir eklenemedi!';
            errorMessage.style.display = 'block';
        }
    } catch (error) {
        errorMessage.textContent = 'Bir hata oluştu. Lütfen tekrar deneyin.';
        errorMessage.style.display = 'block';
    }
});

// Delete expense
async function deleteExpense(id) {
    if (!confirm('Bu harcamayı silmek istediğinize emin misiniz?')) {
        return;
    }

    try {
        const response = await fetch(`/api/expenses/${id}`, {
            method: 'DELETE',
            credentials: 'include'
        });

        if (response.ok) {
            await loadExpenses();
            await loadBudget();
        } else {
            alert('Harcama silinemedi!');
        }
    } catch (error) {
        console.error('Error deleting expense:', error);
        alert('Bir hata oluştu!');
    }
}

// Delete income
async function deleteIncome(id) {
    if (!confirm('Bu geliri silmek istediğinize emin misiniz?')) {
        return;
    }

    try {
        const response = await fetch(`/api/incomes/${id}`, {
            method: 'DELETE',
            credentials: 'include'
        });

        if (response.ok) {
            await loadIncomes();
            // Gelir silinince de bütçeyi güncelle
            await loadBudget();
        } else {
            alert('Gelir silinemedi!');
        }
    } catch (error) {
        console.error('Error deleting income:', error);
        alert('Bir hata oluştu!');
    }
}

// Go to budgets page
function goToBudgets() {
    window.location.href = '/budgets.html';
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

// Load plans
async function loadPlans() {
    try {
        const response = await fetch(`/api/plans/budget/${budgetId}`, { credentials: 'include' });
        plans = await response.json();
        displayPlans();
    } catch (error) {
        console.error('Error loading plans:', error);
    }
}

// Display plans
function displayPlans() {
    const plansList = document.getElementById('plansList');

    if (plans.length === 0) {
        plansList.innerHTML = '<p>Henüz plan eklenmedi.</p>';
        return;
    }

    plansList.innerHTML = plans.map(plan => {
        const completedClass = plan.completed ? 'completed' : '';
        const statusText = plan.completed ? 'Tamamlandı' : 'Beklemede';
        const statusIcon = plan.completed ? '✓' : '○';
        const buttonText = plan.completed ? 'İptal Et' : 'Tamamla ve Harcama Olarak Ekle';

        return `
        <div class="item-card plan ${completedClass}">
            <div class="item-info">
                <h4>${plan.category.name} - ${plan.description}</h4>
                <p><strong>Plan Tarihi:</strong> ${formatDate(plan.planDate)}</p>
                <p><strong>Durum:</strong> <span class="plan-status">${statusIcon} ${statusText}</span></p>
            </div>
            <div class="item-amount">${formatCurrency(plan.plannedAmount)}</div>
            <div class="plan-actions">
                <button onclick="togglePlanStatus(${plan.id}, ${!plan.completed}, '${plan.description}', ${plan.plannedAmount}, ${plan.category.id})" 
                        class="btn btn-sm ${plan.completed ? 'btn-secondary' : 'btn-primary'}">
                    ${buttonText}
                </button>
                <button onclick="deletePlan(${plan.id})" class="btn btn-danger btn-sm">Sil</button>
            </div>
        </div>
    `;
    }).join('');
}

// Show add plan modal
function showAddPlanModal() {
    document.getElementById('addPlanModal').style.display = 'block';
    document.getElementById('addPlanForm').reset();
    const today = new Date();
    document.getElementById('planDate').value = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0];
    document.getElementById('errorMessagePlan').style.display = 'none';

    const planCategorySelect = document.getElementById('planCategory');
    if(planCategorySelect && expenseCategories.length > 0) {
        planCategorySelect.innerHTML = '<option value="">Kategori Seçin</option>' +
            expenseCategories.map(cat => `<option value="${cat.id}">${cat.name}</option>`).join('');
    }
}

// Close add plan modal
function closeAddPlanModal() {
    document.getElementById('addPlanModal').style.display = 'none';
}

// Add plan form handler
document.getElementById('addPlanForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const errorMessage = document.getElementById('errorMessagePlan');
    errorMessage.style.display = 'none';

    const formData = {
        description: document.getElementById('planDescription').value,
        plannedAmount: parseFloat(document.getElementById('planAmount').value),
        planDate: document.getElementById('planDate').value,
        budgetId: budgetId,
        categoryId: parseInt(document.getElementById('planCategory').value)
    };

    try {
        const response = await fetch('/api/plans', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData),
            credentials: 'include'
        });

        const data = await response.json();

        if (response.ok) {
            closeAddPlanModal();
            await loadPlans();
        } else {
            errorMessage.textContent = data.message || 'Plan eklenemedi!';
            errorMessage.style.display = 'block';
        }
    } catch (error) {
        errorMessage.textContent = 'Bir hata oluştu. Lütfen tekrar deneyin.';
        errorMessage.style.display = 'block';
    }
});

// 2. OTOMATİK HARCAMA EKLEME FONKSİYONU
// Plan tamamlandıysa, harcamalar tablosuna da ekliyoruz.
async function togglePlanStatus(id, completed, description, amount, categoryId) {
    try {
        // Önce planın durumunu güncelle
        const response = await fetch(`/api/plans/${id}/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ completed: completed }),
            credentials: 'include'
        });

        if (response.ok) {
            // Eğer plan "Tamamlandı" olarak işaretlendiyse, HARCAMA EKLE
            if (completed) {
                const expenseData = {
                    amount: amount,
                    description: description + ' (Planlanan)',
                    date: new Date().toISOString().split('T')[0], // Bugünün tarihi
                    budgetId: budgetId,
                    categoryId: categoryId
                };

                // Harcama ekleme isteği
                await fetch('/api/expenses', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(expenseData),
                    credentials: 'include'
                });

                alert('Plan tamamlandı ve harcama olarak eklendi!');
            }

            // Sayfadaki tüm verileri yenile
            await loadPlans();
            await loadExpenses();
            await loadBudget();
        } else {
            const data = await response.json();
            alert(data.message || 'Plan durumu güncellenemedi!');
        }
    } catch (error) {
        console.error('Error updating plan status:', error);
        alert('Bir hata oluştu!');
    }
}

// Delete plan
async function deletePlan(id) {
    if (!confirm('Bu planı silmek istediğinize emin misiniz?')) {
        return;
    }

    try {
        const response = await fetch(`/api/plans/${id}`, {
            method: 'DELETE',
            credentials: 'include'
        });

        if (response.ok) {
            await loadPlans();
        } else {
            alert('Plan silinemedi!');
        }
    } catch (error) {
        console.error('Error deleting plan:', error);
        alert('Bir hata oluştu!');
    }
}

// Close modals when clicking outside
window.onclick = function(event) {
    const expenseModal = document.getElementById('addExpenseModal');
    const incomeModal = document.getElementById('addIncomeModal');
    const planModal = document.getElementById('addPlanModal');

    if (event.target == expenseModal) {
        closeAddExpenseModal();
    }
    if (event.target == incomeModal) {
        closeAddIncomeModal();
    }
    if (event.target == planModal) {
        closeAddPlanModal();
    }
}