// API Base URL - Update this to match your server
const API_BASE_URL = 'http://localhost:8080/api';

// Global state
let openPositionsData = [];

// Initialize app on load
document.addEventListener('DOMContentLoaded', function() {
    initializeDates();
    refreshAll();
    
    // Add input listeners for buy/sell totals
    document.getElementById('buyQuantity').addEventListener('input', calculateBuyTotal);
    document.getElementById('buyPrice').addEventListener('input', calculateBuyTotal);
    document.getElementById('sellQuantity').addEventListener('input', calculateSellTotal);
    document.getElementById('sellPrice').addEventListener('input', calculateSellTotal);
});

// Tab Management
function showTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Remove active class from all buttons
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Show selected tab
    document.getElementById(tabName).classList.add('active');
    
    // Add active class to clicked button
    event.target.classList.add('active');
    
    // Load data for the tab
    loadTabData(tabName);
}

function showPositionSection(section) {
    document.querySelectorAll('.position-section').forEach(sec => {
        sec.classList.remove('active');
    });
    document.querySelectorAll('.section-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    if (section === 'open') {
        document.getElementById('openPositions').classList.add('active');
    } else {
        document.getElementById('watchlistPositions').classList.add('active');
    }
    
    event.target.classList.add('active');
}

// Load data based on active tab
function loadTabData(tabName) {
    switch(tabName) {
        case 'dashboard':
            loadDashboard();
            break;
        case 'positions':
            loadOpenPositions();
            loadWatchlist();
            break;
        case 'market':
            loadMarketData();
            break;
        case 'transactions':
            loadAllTransactions();
            loadTransactionSummary();
            break;
        case 'trade':
            loadPositionsForSell();
            break;
        case 'analytics':
            loadAnalytics();
            break;
    }
}

// Initialize dates
function initializeDates() {
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('buyDate').value = today;
    document.getElementById('sellDate').value = today;
}

// Refresh all data
async function refreshAll() {
    const activeTab = document.querySelector('.tab-content.active');
    if (activeTab) {
        loadTabData(activeTab.id);
    }
    showToast('Data refreshed', 'success');
}

// ==================== DASHBOARD ====================

async function loadDashboard() {
    try {
        // Load portfolio summary
        const summary = await fetchAPI('/portfolio/summary');
        
        // Update summary cards
        document.getElementById('totalValue').textContent = formatCurrency(summary.totalValue || 0);
        document.getElementById('totalGainLoss').textContent = formatCurrency(summary.totalGainLoss || 0);
        document.getElementById('totalGainLoss').className = 'card-value ' + (summary.totalGainLoss >= 0 ? 'positive' : 'negative');
        document.getElementById('totalGainLossPercent').textContent = formatPercent(summary.totalGainLossPercent || 0);
        document.getElementById('totalPositions').textContent = summary.totalPositions || 0;
        
        // Load top performers
        const topPerformers = await fetchAPI('/portfolio/top-performers?limit=5');
        displayTopPerformers(topPerformers);
        
        // Load allocation
        const allocation = await fetchAPI('/portfolio/allocation/type');
        displayAllocation(allocation);
        
        // Load largest positions
        const largest = await fetchAPI('/portfolio/largest-positions?limit=10');
        displayLargestPositions(largest);
        
    } catch (error) {
        console.error('Error loading dashboard:', error);
        showToast('Error loading dashboard data', 'error');
    }
}

function displayTopPerformers(performers) {
    const container = document.getElementById('topPerformers');
    
    if (!performers || performers.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">📊</div><div class="empty-state-text">No positions yet</div></div>';
        return;
    }
    
    container.innerHTML = performers.map(p => `
        <div class="list-item">
            <div>
                <div class="list-item-label">${p.symbol || 'N/A'}</div>
                <div style="font-size: 12px; color: #6c757d;">${p.quantity || 0} shares</div>
            </div>
            <div class="list-item-value ${p.gainLossPercent >= 0 ? 'positive' : 'negative'}">
                ${formatPercent(p.gainLossPercent || 0)}
            </div>
        </div>
    `).join('');
}

function displayAllocation(allocation) {
    const container = document.getElementById('allocationByType');
    
    if (!allocation || allocation.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">📈</div><div class="empty-state-text">No allocation data</div></div>';
        return;
    }
    
    container.innerHTML = allocation.map(a => `
        <div class="list-item">
            <div class="list-item-label">${a.type || 'Unknown'}</div>
            <div>
                <div class="list-item-value">${formatPercent(a.percentage || 0)}</div>
                <div style="font-size: 12px; color: #6c757d; text-align: right;">${formatCurrency(a.value || 0)}</div>
            </div>
        </div>
    `).join('');
}

function displayLargestPositions(positions) {
    const container = document.getElementById('largestPositions');
    
    if (!positions || positions.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">💼</div><div class="empty-state-text">No positions</div></div>';
        return;
    }
    
    container.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>Symbol</th>
                    <th>Quantity</th>
                    <th>Avg Cost</th>
                    <th>Current Price</th>
                    <th>Market Value</th>
                    <th>Gain/Loss</th>
                    <th>%</th>
                </tr>
            </thead>
            <tbody>
                ${positions.map(p => `
                    <tr>
                        <td><strong>${p.symbol || 'N/A'}</strong></td>
                        <td>${p.quantity || 0}</td>
                        <td>${formatCurrency(p.averageCost || 0)}</td>
                        <td>${formatCurrency(p.currentPrice || 0)}</td>
                        <td>${formatCurrency(p.marketValue || 0)}</td>
                        <td class="${(p.gainLoss || 0) >= 0 ? 'positive' : 'negative'}">${formatCurrency(p.gainLoss || 0)}</td>
                        <td class="${(p.gainLossPercent || 0) >= 0 ? 'positive' : 'negative'}">${formatPercent(p.gainLossPercent || 0)}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

// ==================== POSITIONS ====================

async function loadOpenPositions() {
    try {
        openPositionsData = await fetchAPI('/positions/open');
        const container = document.getElementById('openPositionsTable');
        
        if (!openPositionsData || openPositionsData.length === 0) {
            container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">📦</div><div class="empty-state-text">No open positions</div><div class="empty-state-subtext">Buy some stocks to get started!</div></div>';
            return;
        }
        
        container.innerHTML = `
            <table>
                <thead>
                    <tr>
                        <th>Symbol</th>
                        <th>Type</th>
                        <th>Quantity</th>
                        <th>Avg Cost</th>
                        <th>Current Price</th>
                        <th>Market Value</th>
                        <th>Gain/Loss</th>
                        <th>%</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    ${openPositionsData.map(p => `
                        <tr>
                            <td><strong>${p.symbol || 'N/A'}</strong></td>
                            <td>${p.assetType || 'N/A'}</td>
                            <td>${p.quantity || 0}</td>
                            <td>${formatCurrency(p.averageCost || 0)}</td>
                            <td>${formatCurrency(p.currentPrice || 0)}</td>
                            <td>${formatCurrency(p.marketValue || 0)}</td>
                            <td class="${(p.gainLoss || 0) >= 0 ? 'positive' : 'negative'}">${formatCurrency(p.gainLoss || 0)}</td>
                            <td class="${(p.gainLossPercent || 0) >= 0 ? 'positive' : 'negative'}">${formatPercent(p.gainLossPercent || 0)}</td>
                            <td><span class="badge badge-success">${p.status || 'OPEN'}</span></td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (error) {
        console.error('Error loading positions:', error);
        showToast('Error loading positions', 'error');
    }
}

async function loadWatchlist() {
    try {
        const watchlist = await fetchAPI('/positions/watchlist');
        const container = document.getElementById('watchlistTable');
        
        if (!watchlist || watchlist.length === 0) {
            container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">👁️</div><div class="empty-state-text">No watchlist items</div><div class="empty-state-subtext">Add symbols to track</div></div>';
            return;
        }
        
        container.innerHTML = `
            <table>
                <thead>
                    <tr>
                        <th>Symbol</th>
                        <th>Type</th>
                        <th>Current Price</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    ${watchlist.map(w => `
                        <tr>
                            <td><strong>${w.symbol || 'N/A'}</strong></td>
                            <td>${w.assetType || 'N/A'}</td>
                            <td>${formatCurrency(w.currentPrice || 0)}</td>
                            <td>
                                <button class="btn btn-delete" onclick="removeFromWatchlist(${w.id})">Remove</button>
                            </td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (error) {
        console.error('Error loading watchlist:', error);
        showToast('Error loading watchlist', 'error');
    }
}

async function addToWatchlist() {
    const symbol = document.getElementById('watchlistSymbol').value.trim().toUpperCase();
    
    if (!symbol) {
        showToast('Please enter a symbol', 'error');
        return;
    }
    
    try {
        await fetchAPI('/positions/watchlist', {
            method: 'POST',
            body: JSON.stringify({ symbol })
        });
        
        document.getElementById('watchlistSymbol').value = '';
        showToast(`${symbol} added to watchlist`, 'success');
        loadWatchlist();
    } catch (error) {
        console.error('Error adding to watchlist:', error);
        showToast('Error adding to watchlist', 'error');
    }
}

async function removeFromWatchlist(id) {
    if (!confirm('Remove from watchlist?')) return;
    
    try {
        await fetchAPI(`/positions/watchlist/${id}`, { method: 'DELETE' });
        showToast('Removed from watchlist', 'success');
        loadWatchlist();
    } catch (error) {
        console.error('Error removing from watchlist:', error);
        showToast('Error removing from watchlist', 'error');
    }
}

// ==================== MARKET DATA ====================

async function loadMarketData() {
    try {
        const marketData = await fetchAPI('/market-data');
        displayAllMarketData(marketData);
        
        const gainers = await fetchAPI('/market-data/top-gainers?limit=5');
        displayTopGainers(gainers);
        
        const losers = await fetchAPI('/market-data/top-losers?limit=5');
        displayTopLosers(losers);
    } catch (error) {
        console.error('Error loading market data:', error);
        showToast('Error loading market data', 'error');
    }
}

function displayAllMarketData(data) {
    const container = document.getElementById('allMarketData');
    
    if (!data || data.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">📊</div><div class="empty-state-text">No market data available</div></div>';
        return;
    }
    
    container.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>Symbol</th>
                    <th>Price</th>
                    <th>Change</th>
                    <th>% Change</th>
                    <th>Volume</th>
                    <th>Updated</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${data.map(m => `
                    <tr>
                        <td><strong>${m.symbol || 'N/A'}</strong></td>
                        <td>${formatCurrency(m.currentPrice || 0)}</td>
                        <td class="${(m.change || 0) >= 0 ? 'positive' : 'negative'}">${formatCurrency(m.change || 0)}</td>
                        <td class="${(m.changePercent || 0) >= 0 ? 'positive' : 'negative'}">${formatPercent(m.changePercent || 0)}</td>
                        <td>${formatNumber(m.volume || 0)}</td>
                        <td>${formatDate(m.lastUpdated)}</td>
                        <td>
                            <button class="btn-action btn-buy" onclick="openQuickBuy('${m.symbol}', ${m.currentPrice || 0})" title="Buy">
                                🛒 Buy
                            </button>
                            <button class="btn-action btn-watch" onclick="quickAddToWatchlist('${m.symbol}')" title="Add to Watchlist">
                                👁️ Watch
                            </button>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

function displayTopGainers(gainers) {
    const container = document.getElementById('topGainers');
    
    if (!gainers || gainers.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-state-text">No data</div></div>';
        return;
    }
    
    container.innerHTML = gainers.map(g => `
        <div class="list-item">
            <div>
                <div class="list-item-label">${g.symbol || 'N/A'}</div>
                <div style="font-size: 12px; color: #6c757d;">${formatCurrency(g.currentPrice || 0)}</div>
            </div>
            <div style="display: flex; align-items: center; gap: 10px;">
                <div class="list-item-value positive">${formatPercent(g.changePercent || 0)}</div>
                <button class="btn-action btn-buy" onclick="openQuickBuy('${g.symbol}', ${g.currentPrice || 0})" title="Buy">🛒</button>
                <button class="btn-action btn-watch" onclick="quickAddToWatchlist('${g.symbol}')" title="Watch">👁️</button>
            </div>
        </div>
    `).join('');
}

function displayTopLosers(losers) {
    const container = document.getElementById('topLosers');
    
    if (!losers || losers.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-state-text">No data</div></div>';
        return;
    }
    
    container.innerHTML = losers.map(l => `
        <div class="list-item">
            <div>
                <div class="list-item-label">${l.symbol || 'N/A'}</div>
                <div style="font-size: 12px; color: #6c757d;">${formatCurrency(l.currentPrice || 0)}</div>
            </div>
            <div style="display: flex; align-items: center; gap: 10px;">
                <div class="list-item-value negative">${formatPercent(l.changePercent || 0)}</div>
                <button class="btn-action btn-buy" onclick="openQuickBuy('${l.symbol}', ${l.currentPrice || 0})" title="Buy">🛒</button>
                <button class="btn-action btn-watch" onclick="quickAddToWatchlist('${l.symbol}')" title="Watch">👁️</button>
            </div>
        </div>
    `).join('');
}

async function searchMarketData() {
    const symbol = document.getElementById('searchSymbol').value.trim().toUpperCase();
    
    if (!symbol) {
        loadMarketData();
        return;
    }
    
    try {
        const data = await fetchAPI(`/market-data/symbol/${symbol}`);
        displayAllMarketData([data]);
    } catch (error) {
        console.error('Error searching market data:', error);
        showToast('Symbol not found', 'error');
    }
}

async function filterBySector() {
    const sector = document.getElementById('sectorFilter').value;
    
    if (!sector) {
        loadMarketData();
        return;
    }
    
    try {
        const data = await fetchAPI(`/market-data/sector/${sector}`);
        displayAllMarketData(data);
        showToast(`Filtered by ${sector} sector`, 'info');
    } catch (error) {
        console.error('Error filtering by sector:', error);
        showToast('Error filtering market data', 'error');
    }
}

async function filterByAssetType() {
    const assetTypeId = document.getElementById('assetTypeFilter').value;
    
    if (!assetTypeId) {
        loadMarketData();
        return;
    }
    
    try {
        const data = await fetchAPI(`/market-data/asset-type/${assetTypeId}`);
        const assetTypeNames = {
            '1': 'Stocks',
            '2': 'ETFs',
            '3': 'Crypto',
            '4': 'Bonds',
            '6': 'Mutual Funds'
        };
        displayAllMarketData(data);
        showToast(`Showing ${assetTypeNames[assetTypeId] || 'Assets'}`, 'info');
    } catch (error) {
        console.error('Error filtering by asset type:', error);
        showToast('Error filtering market data', 'error');
    }
}

// ==================== TRANSACTIONS ====================

async function loadAllTransactions() {
    try {
        const transactions = await fetchAPI('/transactions');
        displayTransactions(transactions);
    } catch (error) {
        console.error('Error loading transactions:', error);
        showToast('Error loading transactions', 'error');
    }
}

async function loadTransactionSummary() {
    try {
        const invested = await fetchAPI('/transactions/total-invested');
        document.getElementById('totalInvested').textContent = formatCurrency(invested.totalInvested || 0);
        
        const gains = await fetchAPI('/transactions/realized-gains');
        document.getElementById('realizedGains').textContent = formatCurrency(gains.realizedGains || 0);
        document.getElementById('realizedGains').className = 'card-value ' + ((gains.realizedGains || 0) >= 0 ? 'positive' : 'negative');
    } catch (error) {
        console.error('Error loading transaction summary:', error);
    }
}

function displayTransactions(transactions) {
    const container = document.getElementById('transactionsTable');
    
    if (!transactions || transactions.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">📜</div><div class="empty-state-text">No transactions</div></div>';
        return;
    }
    
    container.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>Date</th>
                    <th>Type</th>
                    <th>Symbol</th>
                    <th>Quantity</th>
                    <th>Price</th>
                    <th>Total</th>
                    <th>Fees</th>
                </tr>
            </thead>
            <tbody>
                ${transactions.map(t => `
                    <tr>
                        <td>${formatDate(t.transactionDate)}</td>
                        <td><span class="badge ${t.transactionType === 'BUY' ? 'badge-success' : 'badge-danger'}">${t.transactionType || 'N/A'}</span></td>
                        <td><strong>${t.symbol || 'N/A'}</strong></td>
                        <td>${t.quantity || 0}</td>
                        <td>${formatCurrency(t.price || 0)}</td>
                        <td>${formatCurrency((t.quantity || 0) * (t.price || 0))}</td>
                        <td>${formatCurrency(t.fees || 0)}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

async function filterTransactions() {
    const startDate = document.getElementById('transStartDate').value;
    const endDate = document.getElementById('transEndDate').value;
    
    if (!startDate || !endDate) {
        showToast('Please select both dates', 'error');
        return;
    }
    
    try {
        const transactions = await fetchAPI(`/transactions/date-range?startDate=${startDate}&endDate=${endDate}`);
        displayTransactions(transactions);
        showToast('Transactions filtered by date', 'info');
    } catch (error) {
        console.error('Error filtering transactions:', error);
        showToast('Error filtering transactions', 'error');
    }
}

async function filterByTransType() {
    const type = document.getElementById('transTypeFilter').value;
    
    if (!type) {
        loadAllTransactions();
        return;
    }
    
    try {
        const transactions = await fetchAPI(`/transactions/type/${type}`);
        displayTransactions(transactions);
        showToast(`Showing ${type} transactions only`, 'info');
    } catch (error) {
        console.error('Error filtering by type:', error);
        showToast('Error filtering transactions', 'error');
    }
}

// ==================== TRADE ====================

async function loadPositionsForSell() {
    try {
        const positions = await fetchAPI('/positions/open');
        const select = document.getElementById('sellPositionId');
        
        select.innerHTML = '<option value="">-- Select Position --</option>' +
            positions.map(p => `
                <option value="${p.id}" data-quantity="${p.quantity}" data-symbol="${p.symbol}">
                    ${p.symbol} (${p.quantity} shares available)
                </option>
            `).join('');
    } catch (error) {
        console.error('Error loading positions for sell:', error);
    }
}

function updateSellForm() {
    const select = document.getElementById('sellPositionId');
    const option = select.options[select.selectedIndex];
    
    if (option.value) {
        document.getElementById('sellAvailable').value = `${option.dataset.symbol} - ${option.dataset.quantity} shares`;
    } else {
        document.getElementById('sellAvailable').value = '';
    }
}

function calculateBuyTotal() {
    const quantity = parseFloat(document.getElementById('buyQuantity').value) || 0;
    const price = parseFloat(document.getElementById('buyPrice').value) || 0;
    document.getElementById('buyTotal').textContent = (quantity * price).toFixed(2);
}

function calculateSellTotal() {
    const quantity = parseFloat(document.getElementById('sellQuantity').value) || 0;
    const price = parseFloat(document.getElementById('sellPrice').value) || 0;
    document.getElementById('sellTotal').textContent = (quantity * price).toFixed(2);
}

async function buyStock() {
    const symbol = document.getElementById('buySymbol').value.trim().toUpperCase();
    const quantity = parseInt(document.getElementById('buyQuantity').value);
    const price = parseFloat(document.getElementById('buyPrice').value);
    const date = document.getElementById('buyDate').value;
    
    if (!symbol || !quantity || !price || !date) {
        showToast('Please fill all fields', 'error');
        return;
    }
    
    if (quantity <= 0 || price <= 0) {
        showToast('Quantity and price must be positive', 'error');
        return;
    }
    
    try {
        await fetchAPI('/positions/buy', {
            method: 'POST',
            body: JSON.stringify({ symbol, quantity, price, date })
        });
        
        document.getElementById('buySymbol').value = '';
        document.getElementById('buyQuantity').value = '';
        document.getElementById('buyPrice').value = '';
        document.getElementById('buyTotal').textContent = '0.00';
        
        showToast(`Successfully bought ${quantity} shares of ${symbol}`, 'success');
        loadOpenPositions();
    } catch (error) {
        console.error('Error buying stock:', error);
        showToast('Error executing buy order', 'error');
    }
}

async function sellStock() {
    const positionId = document.getElementById('sellPositionId').value;
    const quantity = parseInt(document.getElementById('sellQuantity').value);
    const price = parseFloat(document.getElementById('sellPrice').value);
    const date = document.getElementById('sellDate').value;
    
    if (!positionId || !quantity || !price || !date) {
        showToast('Please fill all fields', 'error');
        return;
    }
    
    if (quantity <= 0 || price <= 0) {
        showToast('Quantity and price must be positive', 'error');
        return;
    }
    
    const select = document.getElementById('sellPositionId');
    const option = select.options[select.selectedIndex];
    const available = parseInt(option.dataset.quantity);
    
    if (quantity > available) {
        showToast(`Cannot sell ${quantity} shares. Only ${available} available.`, 'error');
        return;
    }
    
    try {
        await fetchAPI('/positions/sell', {
            method: 'POST',
            body: JSON.stringify({ positionId, quantity, price, date })
        });
        
        document.getElementById('sellQuantity').value = '';
        document.getElementById('sellPrice').value = '';
        document.getElementById('sellTotal').textContent = '0.00';
        
        showToast(`Successfully sold ${quantity} shares`, 'success');
        loadPositionsForSell();
        loadOpenPositions();
    } catch (error) {
        console.error('Error selling stock:', error);
        showToast('Error executing sell order', 'error');
    }
}

// ==================== UTILITY FUNCTIONS ====================

async function fetchAPI(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const config = {
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        },
        ...options
    };
    
    const response = await fetch(url, config);
    
    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    // Handle 204 No Content
    if (response.status === 204) {
        return null;
    }
    
    return response.json();
}

function formatCurrency(value) {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
    }).format(value);
}

function formatPercent(value) {
    return (value >= 0 ? '+' : '') + value.toFixed(2) + '%';
}

function formatNumber(value) {
    return new Intl.NumberFormat('en-US').format(value);
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// Update last update time
function updateLastUpdateTime() {
    const now = new Date();
    const timeString = now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    document.getElementById('lastUpdate').textContent = `Last updated: ${timeString}`;
}

// ==================== ANALYTICS TAB ====================

async function loadAnalytics() {
    try {
        // Load all instruments
        const instruments = await fetchAPI('/instruments');
        document.getElementById('totalInstruments').textContent = instruments.length || 0;
        displayInstruments(instruments);
        
        // Load YTD realized gains
        const ytdGains = await fetchAPI('/portfolio/realized-gains/ytd');
        document.getElementById('ytdGains').textContent = formatCurrency(ytdGains.totalGains || 0);
        
        // Load top and worst performers
        const topPerformers = await fetchAPI('/portfolio/top-performers?limit=1');
        const worstPerformers = await fetchAPI('/portfolio/worst-performers?limit=1');
        
        if (topPerformers && topPerformers.length > 0) {
            document.getElementById('bestPerformer').textContent = topPerformers[0].symbol;
        }
        if (worstPerformers && worstPerformers.length > 0) {
            document.getElementById('worstPerformer').textContent = worstPerformers[0].symbol;
        }
        
        // Load sector allocation
        const sectorAllocation = await fetchAPI('/portfolio/allocation/sector');
        displaySectorAllocation(sectorAllocation);
        
        // Load performance summary
        displayPerformanceSummary();
        
    } catch (error) {
        console.error('Error loading analytics:', error);
        showToast('Error loading analytics data', 'error');
    }
}

function displayInstruments(instruments) {
    const container = document.getElementById('instrumentsTable');
    
    if (!instruments || instruments.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">📊</div><div class="empty-state-text">No instruments found</div></div>';
        return;
    }
    
    container.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>Symbol</th>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Sector</th>
                    <th>Exchange</th>
                </tr>
            </thead>
            <tbody>
                ${instruments.map(i => `
                    <tr>
                        <td><strong>${i.symbol || 'N/A'}</strong></td>
                        <td>${i.name || 'N/A'}</td>
                        <td><span class="badge badge-${(i.instrumentType || '').toLowerCase()}">${i.instrumentType || 'N/A'}</span></td>
                        <td>${i.sector || 'N/A'}</td>
                        <td>${i.exchange || 'N/A'}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

function displaySectorAllocation(allocation) {
    const container = document.getElementById('sectorAllocation');
    
    if (!allocation || allocation.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">🎯</div><div class="empty-state-text">No sector data</div></div>';
        return;
    }
    
    container.innerHTML = allocation.map(a => `
        <div class="list-item">
            <div class="list-item-label">${a.sector || 'Unknown'}</div>
            <div>
                <div class="list-item-value">${formatPercent(a.percentage || 0)}</div>
                <div style="font-size: 12px; color: #6c757d; text-align: right;">${formatCurrency(a.value || 0)}</div>
            </div>
        </div>
    `).join('');
}

async function displayPerformanceSummary() {
    const container = document.getElementById('performanceSummary');
    
    try {
        const topPerformers = await fetchAPI('/portfolio/top-performers?limit=3');
        
        if (!topPerformers || topPerformers.length === 0) {
            container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">📊</div><div class="empty-state-text">No performance data</div></div>';
            return;
        }
        
        container.innerHTML = topPerformers.map((p, index) => `
            <div class="list-item">
                <div>
                    <div class="list-item-label">#${index + 1} ${p.symbol || 'N/A'}</div>
                    <div style="font-size: 12px; color: #6c757d;">${p.quantity || 0} shares</div>
                </div>
                <div class="list-item-value ${p.gainLossPercent >= 0 ? 'positive' : 'negative'}">
                    ${formatPercent(p.gainLossPercent || 0)}
                </div>
            </div>
        `).join('');
        
    } catch (error) {
        console.error('Error loading performance summary:', error);
        container.innerHTML = '<div class="empty-state"><div class="empty-state-text">Error loading data</div></div>';
    }
}

async function loadRealizedGains() {
    const startDate = document.getElementById('gainsStartDate').value;
    const endDate = document.getElementById('gainsEndDate').value;
    
    if (!startDate || !endDate) {
        showToast('Please select both start and end dates', 'error');
        return;
    }
    
    try {
        const gains = await fetchAPI(`/portfolio/realized-gains?startDate=${startDate}&endDate=${endDate}`);
        displayRealizedGains(gains);
    } catch (error) {
        console.error('Error loading realized gains:', error);
        showToast('Error loading realized gains', 'error');
    }
}

function displayRealizedGains(gains) {
    const container = document.getElementById('realizedGainsData');
    
    if (!gains || (!gains.totalGains && gains.totalGains !== 0)) {
        container.innerHTML = '<div class="empty-state"><div class="empty-state-icon">📊</div><div class="empty-state-text">No realized gains data</div></div>';
        return;
    }
    
    container.innerHTML = `
        <div class="list-item">
            <div class="list-item-label">Total Realized Gains</div>
            <div class="list-item-value ${gains.totalGains >= 0 ? 'positive' : 'negative'}">
                ${formatCurrency(gains.totalGains || 0)}
            </div>
        </div>
        <div class="list-item">
            <div class="list-item-label">Number of Transactions</div>
            <div class="list-item-value">${gains.transactionCount || 0}</div>
        </div>
        <div class="list-item">
            <div class="list-item-label">Average Gain per Transaction</div>
            <div class="list-item-value">
                ${gains.transactionCount > 0 ? formatCurrency(gains.totalGains / gains.transactionCount) : '$0.00'}
            </div>
        </div>
    `;
}

// Call updateLastUpdateTime when refreshing
const originalRefreshAll = refreshAll;
async function refreshAll() {
    await originalRefreshAll();
    updateLastUpdateTime();
}

// ==================== QUICK BUY MODAL ====================

function openQuickBuy(symbol, currentPrice) {
    const modal = document.getElementById('quickBuyModal');
    const today = new Date().toISOString().split('T')[0];
    
    // Set modal data
    document.getElementById('modalSymbol').textContent = symbol;
    document.getElementById('modalCurrentPrice').value = `$${currentPrice.toFixed(2)}`;
    document.getElementById('modalQuantity').value = '';
    document.getElementById('modalPrice').value = currentPrice.toFixed(2);
    document.getElementById('modalDate').value = today;
    document.getElementById('modalTotal').textContent = '0.00';
    
    // Store for later use
    modal.dataset.symbol = symbol;
    modal.dataset.price = currentPrice;
    
    // Show modal
    modal.style.display = 'flex';
}

function closeQuickBuyModal() {
    document.getElementById('quickBuyModal').style.display = 'none';
}

function calculateModalTotal() {
    const quantity = parseFloat(document.getElementById('modalQuantity').value) || 0;
    const price = parseFloat(document.getElementById('modalPrice').value) || 0;
    const total = quantity * price;
    document.getElementById('modalTotal').textContent = total.toFixed(2);
}

async function executeQuickBuy() {
    const modal = document.getElementById('quickBuyModal');
    const symbol = modal.dataset.symbol;
    const quantity = parseFloat(document.getElementById('modalQuantity').value);
    const price = parseFloat(document.getElementById('modalPrice').value);
    const date = document.getElementById('modalDate').value;
    
    if (!quantity || quantity <= 0) {
        showToast('Please enter a valid quantity', 'error');
        return;
    }
    
    if (!price || price <= 0) {
        showToast('Please enter a valid price', 'error');
        return;
    }
    
    try {
        await fetchAPI('/positions/buy', {
            method: 'POST',
            body: JSON.stringify({ symbol, quantity, price, date })
        });
        
        closeQuickBuyModal();
        showToast(`Successfully purchased ${quantity} shares of ${symbol}!`, 'success');
        
        // Refresh positions if on that tab
        if (document.getElementById('positions').classList.contains('active')) {
            loadOpenPositions();
        }
    } catch (error) {
        console.error('Error buying stock:', error);
        showToast('Error purchasing stock', 'error');
    }
}

async function quickAddToWatchlist(symbol) {
    try {
        await fetchAPI('/positions/watchlist', {
            method: 'POST',
            body: JSON.stringify({ symbol })
        });
        
        showToast(`${symbol} added to watchlist!`, 'success');
    } catch (error) {
        console.error('Error adding to watchlist:', error);
        showToast('Error adding to watchlist', 'error');
    }
}

// Close modal when clicking outside
window.onclick = function(event) {
    const modal = document.getElementById('quickBuyModal');
    if (event.target === modal) {
        closeQuickBuyModal();
    }
}

