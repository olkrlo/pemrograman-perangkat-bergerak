package com.example.money_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.money_tracker.ui.theme.Money_TrackerTheme
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Money_TrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MoneyTrackerScreen()
                }
            }
        }
    }
}

data class Transaction(
    val name: String,
    val type: String,
    val amount: String,
    val date: String,
    val category: String
)

@Composable
fun MoneyTrackerScreen() {
    val transactions = remember { mutableStateListOf<Transaction>() }

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTransactionType by remember { mutableStateOf("Pengeluaran") }
    var filterType by remember { mutableStateOf("All") }

    var selectedTransactionIndex by remember { mutableStateOf(-1) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    val displayedTransactions = remember(transactions, filterType) {
        when (filterType) {
            "Pemasukan" -> transactions.filter { it.type == "Pemasukan" }
            "Pengeluaran" -> transactions.filter { it.type == "Pengeluaran" }
            else -> transactions
        }
    }

    val totalIncome = transactions.filter { it.type == "Pemasukan" }
        .sumOf { it.amount.replace(".", "").toInt() }
    val totalExpense = transactions.filter { it.type == "Pengeluaran" }
        .sumOf { it.amount.replace(".", "").toIntOrNull() ?: 0 }
    val balance = totalIncome - totalExpense

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Money Tracker",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Saldo", color = Color.White)
                    Text(
                        text = "Rp${balance.format()}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Pemasukan")
                        Text("Rp${totalIncome.format()}", fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Pengeluaran")
                        Text("Rp${totalExpense.format()}", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("All", "Pemasukan", "Pengeluaran").forEach { type ->
                    FilterChip(
                        selected = filterType == type,
                        onClick = { filterType = type },
                        label = { Text(type) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (displayedTransactions.isEmpty()) {
                EmptyStateMessage("Belum ada transaksi")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(displayedTransactions.size) { index ->
                        val globalIndex = transactions.indexOf(displayedTransactions[index])
                        TransactionItem(
                            transaction = displayedTransactions[index],
                            onClick = {
                                selectedTransactionIndex = globalIndex
                                editingTransaction = transactions[globalIndex]
                                showEditDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            transactionType = selectedTransactionType,
            onTypeChange = { selectedTransactionType = it },
            onAddTransaction = { name, amount, category, date ->
                val formattedAmount = amount.replace(".", "").toInt().format()
                transactions.add(
                    Transaction(name, selectedTransactionType, formattedAmount, date, category)
                )
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (showEditDialog && editingTransaction != null) {
        EditTransactionDialog(
            transaction = editingTransaction!!,
            onUpdateTransaction = { name, type, amount, category, date ->
                transactions[selectedTransactionIndex] =
                    Transaction(name, type, amount.replace(".", "").toInt().format(), date, category)
                showEditDialog = false
            },
            onDeleteTransaction = {
                transactions.removeAt(selectedTransactionIndex)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

fun Int.format(): String {
    return NumberFormat.getNumberInstance(Locale("in", "ID")).format(this)
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = Color.Gray)
    }
}

@Composable
fun TransactionItem(transaction: Transaction, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = transaction.name, fontWeight = FontWeight.Bold)
                Text(text = "${transaction.category} • ${transaction.date}", fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                text = "Rp${transaction.amount}",
                color = if (transaction.type == "Pemasukan") Color(0xFF4CAF50) else Color(0xFFF44336),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AddTransactionDialog(
    transactionType: String,
    onTypeChange: (String) -> Unit,
    onAddTransaction: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Transaksi") },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FilterChip(selected = transactionType == "Pemasukan", onClick = { onTypeChange("Pemasukan") }, label = { Text("Pemasukan") })
                    FilterChip(selected = transactionType == "Pengeluaran", onClick = { onTypeChange("Pengeluaran") }, label = { Text("Pengeluaran") })
                }
                TextField(value = name, onValueChange = { name = it }, label = { Text("Nama") })
                TextField(value = amount, onValueChange = { amount = it }, label = { Text("Jumlah") })
                TextField(value = category, onValueChange = { category = it }, label = { Text("Kategori") })
                TextField(value = date, onValueChange = { date = it }, label = { Text("Tanggal") })
            }
        },
        confirmButton = {
            Button(onClick = { onAddTransaction(name, amount, category, date) }) {
                Text("Tambah")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    onUpdateTransaction: (String, String, String, String, String) -> Unit,
    onDeleteTransaction: () -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(transaction.name) }
    var type by remember { mutableStateOf(transaction.type) }
    var amount by remember { mutableStateOf(transaction.amount) }
    var category by remember { mutableStateOf(transaction.category) }
    var date by remember { mutableStateOf(transaction.date) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Transaksi") },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FilterChip(selected = type == "Pemasukan", onClick = { type = "Pemasukan" }, label = { Text("Pemasukan") })
                    FilterChip(selected = type == "Pengeluaran", onClick = { type = "Pengeluaran" }, label = { Text("Pengeluaran") })
                }
                TextField(value = name, onValueChange = { name = it }, label = { Text("Nama") })
                TextField(value = amount, onValueChange = { amount = it }, label = { Text("Jumlah") })
                TextField(value = category, onValueChange = { category = it }, label = { Text("Kategori") })
                TextField(value = date, onValueChange = { date = it }, label = { Text("Tanggal") })
            }
        },
        confirmButton = {
            Column {
                Button(onClick = { onUpdateTransaction(name, type, amount, category, date) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Update")
                }
                TextButton(onClick = onDeleteTransaction, modifier = Modifier.fillMaxWidth()) {
                    Text("Hapus", color = Color.Red)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
