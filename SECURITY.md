# 🔒 IonAPI Security

This document outlines the security measures implemented in IonAPI to prevent common exploits and vulnerabilities.

---

## GUI Security (ion-gui)

### Duping Prevention

IonAPI's GUI system includes multiple layers of protection against item duping exploits:

#### 1. Default Deny Policy
```java
private boolean allowTake = false;
private boolean allowPlace = false;
private boolean allowDrag = false;
```

By default, all item manipulation is **disabled**. This prevents:
- Taking items from GUI slots
- Placing items into GUI slots
- Dragging items between slots

#### 2. Bottom Inventory Protection
```java
// Clicking in player's own inventory (bottom inventory)
if (slot < 0 || slot >= inventory.getSize()) {
    // Always cancel to prevent shift-click duping exploits
    event.setCancelled(true);
    return;
}
```

**Prevents:**
- Shift-clicking items from player inventory into GUI
- Moving items between player inventory and GUI
- Hotbar key exploits

#### 3. Shift-Click Protection
```java
boolean isShiftClick = event.isShiftClick();
boolean isNumberKey = event.getHotbarButton() >= 0;

// Always cancel shift-click and number key presses to prevent duping
if (isShiftClick || isNumberKey) {
    event.setCancelled(true);
}
```

**Prevents:**
- Shift-click duping (most common exploit)
- Number key (1-9) item swapping
- Quick-move exploits

#### 4. Drag Event Protection
```java
if (!allowDrag) {
    event.setCancelled(true);
    return;
}
```

**Prevents:**
- Drag-and-drop duping
- Multi-slot item distribution exploits

### Safe Usage Examples

#### Read-Only GUI (Default - Safest)
```java
IonGui gui = IonGui.builder()
    .title("Shop")
    .item(10, diamondItem, click -> {
        // Handle purchase
        click.close();
    })
    .build();
// No duping possible - all interactions cancelled
```

#### Allow Taking Items (Use with caution)
```java
IonGui gui = IonGui.builder()
    .title("Rewards")
    .allowTake(true)  // ⚠️ Only use when giving items to players
    .item(10, rewardItem)
    .build();
// Shift-click and number keys still blocked
```

#### Custom Validation
```java
IonGui gui = IonGui.builder()
    .title("Trading")
    .onClick(click -> {
        // Validate the action
        if (!isValidTrade(click.getPlayer())) {
            click.setCancelled(true);
            return;
        }
        // Process trade...
    })
    .build();
```

---

## Database Security (ion-database)

### SQL Injection Prevention

#### 1. Parameterized Queries
```java
// ✅ SAFE - Uses prepared statements
db.execute("UPDATE players SET coins = ? WHERE uuid = ?", newBalance, playerUuid);

// ❌ UNSAFE - Never do this
db.execute("UPDATE players SET coins = " + newBalance + " WHERE uuid = '" + playerUuid + "'");
```

#### 2. Identifier Sanitization
```java
// QueryBuilderImpl sanitizes all identifiers
private static final Pattern VALID_IDENTIFIER = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

private void validateIdentifier(String identifier) {
    if (!VALID_IDENTIFIER.matcher(identifier).matches()) {
        throw new DatabaseException("Invalid identifier: " + identifier);
    }
}
```

**Prevents:**
- SQL injection through column names
- SQL injection through table names
- SQL injection through operators

#### 3. Operator Whitelisting
```java
private static final Set<String> VALID_OPERATORS = Set.of(
    "=", "!=", "<>", ">", "<", ">=", "<=", 
    "LIKE", "NOT LIKE", "IN", "NOT IN", "IS", "IS NOT"
);
```

**Prevents:**
- Malicious operators
- SQL command injection
- Subquery injection

### Transaction Safety

#### 1. Automatic Rollback
```java
try {
    transaction.accept(this);
    conn.commit();
} catch (Exception e) {
    conn.rollback();  // ✅ Automatic rollback on error
    throw new DatabaseException("Transaction failed", e);
}
```

#### 2. Connection Cleanup
```java
try (Connection conn = getConnection()) {
    // Operations...
} catch (SQLException e) {
    // Connection automatically closed
}
```

---

## Economy Security (ion-economy)

### Balance Manipulation Prevention

#### 1. BigDecimal Precision
```java
@Column(name = "balance", columnDefinition = "DECIMAL(19,4)")
private BigDecimal balance;
```

**Prevents:**
- Floating-point rounding errors
- Balance manipulation through precision exploits

#### 2. Transaction Validation
```java
public CompletableFuture<TransactionResult> withdraw(UUID playerId, BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        return CompletableFuture.completedFuture(
            new TransactionResultImpl(false, "Amount must be positive", BigDecimal.ZERO)
        );
    }
    // Validate balance before withdrawal...
}
```

#### 3. Atomic Operations
```java
// Database transactions ensure atomicity
db.transaction(tx -> {
    EconomyAccount account = tx.find(EconomyAccount.class, playerId);
    if (account.getBalance().compareTo(amount) < 0) {
        throw new InsufficientFundsException();
    }
    account.setBalance(account.getBalance().subtract(amount));
    tx.save(account);
});
```

---

## Redis Security (ion-redis)

### Connection Security

#### 1. Optional Authentication
```java
IonRedis redis = IonRedis.builder()
    .host("localhost")
    .port(6379)
    .password("your-secure-password")  // Optional but recommended
    .ssl(true)  // Enable SSL/TLS
    .build();
```

#### 2. Connection Pooling
```java
// HikariCP-style connection management
// Prevents connection exhaustion attacks
```

---

## General Security Best Practices

### 1. Input Validation
Always validate user input before processing:
```java
if (input == null || input.isEmpty()) {
    throw new IllegalArgumentException("Input cannot be empty");
}
```

### 2. Permission Checks
Always check permissions before sensitive operations:
```java
if (!player.hasPermission("myplugin.admin")) {
    player.sendMessage("No permission!");
    return;
}
```

### 3. Rate Limiting
Use the built-in RateLimiter to prevent spam:
```java
RateLimiter limiter = RateLimiter.create("command", 5, 10, TimeUnit.SECONDS);
if (!limiter.tryAcquire(player.getUniqueId())) {
    player.sendMessage("Slow down!");
    return;
}
```

### 4. Async Operations
Use async operations for expensive tasks:
```java
db.findAsync(PlayerData.class, uuid).thenAccept(data -> {
    // Process on async thread
});
```

---

## Reporting Security Issues

If you discover a security vulnerability in IonAPI:

1. **DO NOT** open a public GitHub issue
2. **DO** email: [Your security email]
3. **DO** provide:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

We take security seriously and will respond within 48 hours.

---

## Security Checklist for Plugin Developers

When using IonAPI in your plugin:

- [ ] Never use string concatenation for SQL queries
- [ ] Always validate user input
- [ ] Use parameterized queries for database operations
- [ ] Check permissions before sensitive operations
- [ ] Use rate limiting for commands and actions
- [ ] Enable SSL/TLS for Redis connections
- [ ] Use BigDecimal for currency calculations
- [ ] Test GUI interactions for duping exploits
- [ ] Implement proper error handling
- [ ] Log security-relevant events

---

## Version History

### v1.2.0
- ✅ Enhanced GUI duping prevention
- ✅ Added shift-click protection
- ✅ Added number key protection
- ✅ Improved bottom inventory handling

### v1.1.0
- ✅ Fixed SQL injection in QueryBuilder
- ✅ Fixed resource leak in Transaction
- ✅ Added input validation

### v1.0.0
- ✅ Initial security implementation
- ✅ Parameterized queries
- ✅ GUI event cancellation

---

## License

IonAPI is provided "as is" without warranty. See LICENSE file for details.
