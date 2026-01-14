// Use relative path when served from same origin, or absolute for standalone
const API_BASE_URL = window.location.origin + '/api';

// Global state
let allAccounts = [];

// Tab management
function showTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });

    // Show selected tab
    document.getElementById(`${tabName}-tab`).classList.add('active');
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach((item, index) => {
        const tabs = ['dashboard', 'accounts', 'transactions', 'transfer'];
        if (tabs[index] === tabName) {
            item.classList.add('active');
        }
    });

    // Load data when switching tabs
    if (tabName === 'dashboard') {
        loadDashboard();
    } else if (tabName === 'accounts') {
        loadAccounts();
    }
}

// Toast notification
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    const icons = {
        success: 'fa-check-circle',
        error: 'fa-exclamation-circle',
        info: 'fa-info-circle'
    };
    
    toast.innerHTML = `<i class="fas ${icons[type]}"></i> ${message}`;
    toast.className = `toast ${type} show`;
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 5000);
}

// API Helper
async function apiCall(endpoint, method = 'GET', body = null) {
    try {
        const options = {
            method,
            headers: {
                'Content-Type': 'application/json',
            }
        };
        if (body) {
            options.body = JSON.stringify(body);
        }
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        if (!response.ok) {
            const errorText = await response.text();
            let errorMessage = `HTTP error! status: ${response.status}`;
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorText;
            } catch {
                errorMessage = errorText || errorMessage;
            }
            throw new Error(errorMessage);
        }
        return await response.json();
    } catch (error) {
        showToast(`Error: ${error.message}`, 'error');
        throw error;
    }
}

// Format currency
function formatCurrency(amount, currency = 'USD') {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: currency
    }).format(amount);
}

// Format account ID (short version)
function formatAccountId(accountId) {
    return accountId ? `${accountId.substring(0, 4)}-${accountId.substring(4, 8)}-${accountId.substring(8, 12)}-${accountId.substring(12, 16)}` : '';
}

// Populate account dropdowns
function populateAccountDropdowns() {
    const selects = ['depositAccountId', 'withdrawAccountId', 'fromAccountId', 'toAccountId', 'historyAccountId'];
    selects.forEach(selectId => {
        const select = document.getElementById(selectId);
        if (select) {
            const currentValue = select.value;
            select.innerHTML = '<option value="">Select account</option>';
            allAccounts.forEach(account => {
                const option = document.createElement('option');
                option.value = account.accountId;
                option.textContent = `${formatAccountId(account.accountId)} - ${account.accountType} (${formatCurrency(account.balance, account.currency)})`;
                if (account.accountId === currentValue) {
                    option.selected = true;
                }
                select.appendChild(option);
            });
        }
    });
}

// Load Dashboard
async function loadDashboard() {
    try {
        const accounts = await apiCall('/accounts');
        allAccounts = accounts;
        
        // Calculate stats
        const totalAccounts = accounts.length;
        const totalBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0);
        
        // Update stats
        document.getElementById('totalAccounts').textContent = totalAccounts;
        document.getElementById('totalBalance').textContent = formatCurrency(totalBalance);
        document.getElementById('totalTransactions').textContent = 'N/A'; // Could be calculated if needed
        
        // Display recent accounts
        const dashboardAccounts = document.getElementById('dashboardAccounts');
        if (accounts.length === 0) {
            dashboardAccounts.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-wallet"></i>
                    <p>No accounts found. Create your first account to get started.</p>
                </div>
            `;
        } else {
            dashboardAccounts.innerHTML = accounts.slice(0, 6).map(account => `
                <div class="account-card">
                    <div class="account-header">
                        <span class="account-type">${account.accountType}</span>
                        <span class="account-status ${account.active ? 'active' : 'inactive'}">
                            ${account.active ? 'Active' : 'Inactive'}
                        </span>
                    </div>
                    <div class="account-balance">
                        <div class="account-balance-label">Available Balance</div>
                        <div class="account-balance-amount">${formatCurrency(account.balance, account.currency)}</div>
                    </div>
                    <div class="account-id">
                        <strong>Account:</strong> ${formatAccountId(account.accountId)}
                    </div>
                </div>
            `).join('');
        }
    } catch (error) {
        console.error('Error loading dashboard:', error);
    }
}

// Account Management
document.getElementById('createAccountForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        const account = await apiCall('/accounts', 'POST', {
            customerId: document.getElementById('customerId').value,
            accountType: document.getElementById('accountType').value,
            initialBalance: parseFloat(document.getElementById('initialBalance').value),
            currency: document.getElementById('currency').value
        });
        showToast(`Account created successfully! Account: ${formatAccountId(account.accountId)}`, 'success');
        document.getElementById('createAccountForm').reset();
        await loadAccounts();
        await loadDashboard();
    } catch (error) {
        // Error already shown by apiCall
    }
});

async function loadAccounts() {
    try {
        const accounts = await apiCall('/accounts');
        allAccounts = accounts;
        populateAccountDropdowns();
        
        const accountsList = document.getElementById('accountsList');
        if (accounts.length === 0) {
            accountsList.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-wallet"></i>
                    <p>No accounts found. Create your first account to get started.</p>
                </div>
            `;
            return;
        }
        accountsList.innerHTML = accounts.map(account => `
            <div class="account-card">
                <div class="account-header">
                    <span class="account-type">${account.accountType}</span>
                    <span class="account-status ${account.active ? 'active' : 'inactive'}">
                        ${account.active ? 'Active' : 'Inactive'}
                    </span>
                </div>
                <div class="account-balance">
                    <div class="account-balance-label">Available Balance</div>
                    <div class="account-balance-amount">${formatCurrency(account.balance, account.currency)}</div>
                </div>
                <div style="margin-top: 1rem; padding-top: 1rem; border-top: 1px solid var(--border-color);">
                    <div style="font-size: 0.875rem; color: var(--text-secondary);">
                        <div><strong>Customer:</strong> ${account.customerId}</div>
                        <div style="margin-top: 0.5rem;"><strong>Account:</strong> ${formatAccountId(account.accountId)}</div>
                    </div>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading accounts:', error);
    }
}

document.getElementById('customerAccountsForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        const customerId = document.getElementById('customerIdSearch').value;
        const accounts = await apiCall(`/accounts/customer/${customerId}`);
        const customerAccountsList = document.getElementById('customerAccountsList');
        if (accounts.length === 0) {
            customerAccountsList.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-search"></i>
                    <p>No accounts found for customer ${customerId}.</p>
                </div>
            `;
            return;
        }
        customerAccountsList.innerHTML = accounts.map(account => `
            <div class="account-card">
                <div class="account-header">
                    <span class="account-type">${account.accountType}</span>
                    <span class="account-status ${account.active ? 'active' : 'inactive'}">
                        ${account.active ? 'Active' : 'Inactive'}
                    </span>
                </div>
                <div class="account-balance">
                    <div class="account-balance-label">Available Balance</div>
                    <div class="account-balance-amount">${formatCurrency(account.balance, account.currency)}</div>
                </div>
                <div class="account-id">
                    <strong>Account:</strong> ${formatAccountId(account.accountId)}
                </div>
            </div>
        `).join('');
    } catch (error) {
        // Error already shown by apiCall
    }
});

// Transaction Management
document.getElementById('depositForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        const accountId = document.getElementById('depositAccountId').value;
        const account = allAccounts.find(acc => acc.accountId === accountId);
        const currency = account ? account.currency : 'USD';
        
        const transaction = await apiCall('/transactions/deposit', 'POST', {
            accountId: accountId,
            amount: parseFloat(document.getElementById('depositAmount').value),
            currency: currency,
            description: document.getElementById('depositDescription').value
        });
        showToast(`Deposit successful! Amount: ${formatCurrency(transaction.amount, transaction.currency)}`, 'success');
        document.getElementById('depositForm').reset();
        await loadAccounts();
        await loadDashboard();
    } catch (error) {
        // Error already shown by apiCall
    }
});

document.getElementById('withdrawForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        const accountId = document.getElementById('withdrawAccountId').value;
        const account = allAccounts.find(acc => acc.accountId === accountId);
        const currency = account ? account.currency : 'USD';
        
        const transaction = await apiCall('/transactions/withdraw', 'POST', {
            accountId: accountId,
            amount: parseFloat(document.getElementById('withdrawAmount').value),
            currency: currency,
            description: document.getElementById('withdrawDescription').value
        });
        showToast(`Withdrawal successful! Amount: ${formatCurrency(transaction.amount, transaction.currency)}`, 'success');
        document.getElementById('withdrawForm').reset();
        await loadAccounts();
        await loadDashboard();
    } catch (error) {
        // Error already shown by apiCall
    }
});

document.getElementById('transferForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        const fromAccountId = document.getElementById('fromAccountId').value;
        const account = allAccounts.find(acc => acc.accountId === fromAccountId);
        const currency = account ? account.currency : 'USD';
        
        const transaction = await apiCall('/transactions/transfer', 'POST', {
            fromAccountId: fromAccountId,
            toAccountId: document.getElementById('toAccountId').value,
            amount: parseFloat(document.getElementById('transferAmount').value),
            currency: currency,
            description: document.getElementById('transferDescription').value
        });
        showToast(`Transfer successful! Amount: ${formatCurrency(transaction.amount, transaction.currency)}`, 'success');
        document.getElementById('transferForm').reset();
        await loadAccounts();
        await loadDashboard();
    } catch (error) {
        // Error already shown by apiCall
    }
});

document.getElementById('transactionHistoryForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        const accountId = document.getElementById('historyAccountId').value;
        const transactions = await apiCall(`/transactions/account/${accountId}`);
        const transactionHistory = document.getElementById('transactionHistory');
        if (transactions.length === 0) {
            transactionHistory.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-history"></i>
                    <p>No transactions found for this account.</p>
                </div>
            `;
            return;
        }
        transactionHistory.innerHTML = transactions.map(t => {
            const isPositive = t.type === 'DEPOSIT' || t.type === 'TRANSFER';
            const typeClass = t.type.toLowerCase();
            return `
                <div class="transaction-item">
                    <div class="transaction-info">
                        <div class="transaction-type ${typeClass}">
                            <i class="fas ${t.type === 'DEPOSIT' ? 'fa-arrow-down' : t.type === 'WITHDRAWAL' ? 'fa-arrow-up' : 'fa-exchange-alt'}"></i>
                            ${t.type}
                        </div>
                        <div class="transaction-details">${t.description}</div>
                        <div class="transaction-date">${new Date(t.timestamp).toLocaleString()}</div>
                        ${t.relatedAccountId ? `<div class="transaction-details" style="margin-top: 0.25rem;">
                            <i class="fas fa-link"></i> To: ${formatAccountId(t.relatedAccountId)}
                        </div>` : ''}
                    </div>
                    <div class="transaction-amount ${isPositive ? 'positive' : 'negative'}">
                        ${isPositive ? '+' : '-'}${formatCurrency(t.amount, t.currency)}
                    </div>
                </div>
            `;
        }).join('');
    } catch (error) {
        // Error already shown by apiCall
    }
});

// Update account dropdowns when account selection changes
['depositAccountId', 'withdrawAccountId', 'fromAccountId', 'toAccountId'].forEach(id => {
    const select = document.getElementById(id);
    if (select) {
        select.addEventListener('change', function() {
            const accountId = this.value;
            const account = allAccounts.find(acc => acc.accountId === accountId);
            if (account) {
                // Update currency if needed (for forms that have currency field)
                const currencyField = this.closest('form').querySelector('input[type="text"][id*="Currency"]');
                if (currencyField) {
                    currencyField.value = account.currency;
                }
            }
        });
    }
});

// Load data on page load
window.addEventListener('load', async () => {
    await loadAccounts();
    await loadDashboard();
});
