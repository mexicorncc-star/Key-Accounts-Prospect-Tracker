package com.example.keyaccounttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// --- Categories & Custom Colors ---

enum class AccountCategory(val label: String, val color: Color) {
    RETAIL_CHAIN("Retail Chains", Color(0xFF1565C0)),      // Deep Blue
    FRANCHISE("Franchise Groups", Color(0xFF2E7D32)),      // Emerald Green
    DISTRIBUTOR("Distributors", Color(0xFF00838F)),        // Teal / Cyan
    INDEPENDENT("Independent Customer", Color(0xFFF57F17)),// Amber / Gold
    PROSPECT("Prospects", Color(0xFF6A1B9A))               // Purple
}

data class Account(
    val id: String,
    val name: String,
    val category: AccountCategory,
    val isKeyAccount: Boolean = true
)

data class AccountActivity(
    val id: String,
    val accountId: String,
    val date: LocalDate,
    val activityType: String,
    val notes: String,
    val isCompleted: Boolean = true
)

// Default Pre-loaded Accounts
val initialAccounts = listOf(
    // Retail Chains
    Account("1", "Shoprite checkers WC", AccountCategory.RETAIL_CHAIN),
    Account("2", "Shoprite checkers GT", AccountCategory.RETAIL_CHAIN),
    Account("3", "Shoprite checkers KZN", AccountCategory.RETAIL_CHAIN),
    Account("4", "Foodlovers WC", AccountCategory.RETAIL_CHAIN),
    Account("5", "Foodlovers GT", AccountCategory.RETAIL_CHAIN),
    Account("6", "Econofoods", AccountCategory.RETAIL_CHAIN),

    // Franchise Groups
    Account("7", "Spur", AccountCategory.FRANCHISE),
    Account("8", "Kauai", AccountCategory.FRANCHISE),
    Account("9", "Ocean Basket", AccountCategory.FRANCHISE),

    // Distributors
    Account("10", "Bidvest GT", AccountCategory.DISTRIBUTOR),
    Account("11", "Bidvest KZN", AccountCategory.DISTRIBUTOR),
    Account("12", "Bidvest PE", AccountCategory.DISTRIBUTOR),
    Account("13", "Bidvest WC", AccountCategory.DISTRIBUTOR),
    Account("14", "Key for Health", AccountCategory.DISTRIBUTOR),
    Account("15", "Niche Distributors", AccountCategory.DISTRIBUTOR),
    Account("16", "Prima Sales", AccountCategory.DISTRIBUTOR),
    Account("17", "Olive Grove", AccountCategory.DISTRIBUTOR),
    Account("18", "Meze foods", AccountCategory.DISTRIBUTOR),
    Account("19", "CNS Durban", AccountCategory.DISTRIBUTOR)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainAppContainer()
            }
        }
    }
}

@Composable
fun MainAppContainer() {
    var accounts by remember { mutableStateOf(initialAccounts) }
    var activities by remember { mutableStateOf<List<AccountActivity>>(emptyList()) }
    var showAccountManager by remember { mutableStateOf(false) }

    if (showAccountManager) {
        AccountManagementScreen(
            accounts = accounts,
            onAddAccount = { newAccount -> accounts = accounts + newAccount },
            onRemoveAccount = { accountId -> accounts = accounts.filter { it.id != accountId } },
            onClose = { showAccountManager = false }
        )
    } else {
        MainMatrixDashboard(
            accounts = accounts,
            activities = activities,
            onOpenSettings = { showAccountManager = true },
            onAddActivity = { newLog -> activities = activities + newLog }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMatrixDashboard(
    accounts: List<Account>,
    activities: List<AccountActivity>,
    onOpenSettings: () -> Unit,
    onAddActivity: (AccountActivity) -> Unit
) {
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var currentWindowStart by remember { mutableStateOf(LocalDate.now().minusWeeks(6)) }
    val scrollState = rememberScrollState()

    val weekSlots = remember(currentWindowStart) {
        (0..11).map { currentWindowStart.plusWeeks(it.toLong()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Key Account Matrix", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                actions = {
                    IconButton(onClick = { currentWindowStart = currentWindowStart.minusMonths(1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev")
                    }
                    Text(
                        text = currentWindowStart.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { currentWindowStart = currentWindowStart.plusMonths(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Manage Accounts")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            // Timeline Headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier.width(160.dp).height(44.dp).padding(start = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("Account Name", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Row(modifier = Modifier.horizontalScroll(scrollState)) {
                    weekSlots.forEach { weekDate ->
                        val isCurrent = ChronoUnit.WEEKS.between(weekDate, LocalDate.now()) == 0L
                        Box(
                            modifier = Modifier
                                .width(54.dp)
                                .height(44.dp)
                                .background(if (isCurrent) MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(weekDate.format(DateTimeFormatter.ofPattern("MMM")), fontSize = 9.sp, color = Color.Gray)
                                Text("W${(weekDate.dayOfMonth / 7) + 1}", fontSize = 11.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }

            Divider()

            // Account Rows with Alternating Shading and Category Left-Borders
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(accounts.indices.toList()) { index ->
                    val account = accounts[index]
                    val isEvenRow = index % 2 == 0
                    val rowBg = if (isEvenRow) Color.White else Color(0xFFF8F9FA)

                    val accountLogs = activities.filter { it.accountId == account.id && it.isCompleted }
                    val lastContact = accountLogs.maxByOrNull { it.date }
                    val daysSince = lastContact?.let { ChronoUnit.DAYS.between(it.date, LocalDate.now()) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable { selectedAccount = account }
                            .background(rowBg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category Tag Indicator (Left Border)
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .fillMaxHeight()
                                .background(account.category.color)
                        )

                        // Name & Recency
                        Box(
                            modifier = Modifier.width(154.dp).padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Text(account.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                Text(
                                    text = if (daysSince != null) "$daysSince d ago" else "No log",
                                    fontSize = 10.sp,
                                    color = if (daysSince != null && daysSince <= 14) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                        }

                        // 12-Week Interactive Matrix Bar
                        Row(
                            modifier = Modifier.horizontalScroll(scrollState),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            weekSlots.forEach { weekDate ->
                                val hasLog = accountLogs.any { ChronoUnit.WEEKS.between(it.date, weekDate) == 0L }
                                Box(
                                    modifier = Modifier.width(54.dp).height(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.LightGray.copy(alpha = 0.5f))
                                    )
                                    if (hasLog) {
                                        Box(
                                            modifier = Modifier.size(10.dp).clip(CircleShape).background(account.category.color)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Divider(color = Color.LightGray.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
fun AccountManagementScreen(
    accounts: List<Account>,
    onAddAccount: (Account) -> Unit,
    onRemoveAccount: (String) -> Unit,
    onClose: () -> Unit
) {
    var newName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AccountCategory.RETAIL_CHAIN) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Manage Accounts", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text("Account Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                if (newName.isNotBlank()) {
                    onAddAccount(Account(System.currentTimeMillis().toString(), newName, selectedCategory))
                    newName = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Account")
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(accounts) { acc ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(acc.name, fontSize = 14.sp)
                    TextButton(onClick = { onRemoveAccount(acc.id) }) {
                        Text("Remove", color = Color.Red)
                    }
                }
            }
        }

        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("Done")
        }
    }
}
