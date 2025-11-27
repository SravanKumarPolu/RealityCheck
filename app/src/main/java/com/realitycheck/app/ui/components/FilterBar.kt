package com.realitycheck.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import com.realitycheck.app.data.Decision
import com.realitycheck.app.data.DecisionGroup
import com.realitycheck.app.ui.theme.*

@Composable
fun FilterBar(
    selectedCategory: String?,
    selectedTags: List<String>,
    availableTags: List<String>,
    categories: List<String>,
    groups: List<DecisionGroup> = emptyList(),
    selectedGroupId: Long? = null,
    onCategorySelected: (String?) -> Unit,
    onTagSelected: (String) -> Unit,
    onTagRemoved: (String) -> Unit,
    onGroupSelected: ((Long?) -> Unit)? = null,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasActiveFilters = selectedCategory != null || selectedTags.isNotEmpty() || selectedGroupId != null
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.md),
        colors = CardDefaults.cardColors(
            containerColor = if (hasActiveFilters) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null, // Decorative - text provides context
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Filters",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (hasActiveFilters) {
                    TextButton(onClick = onClearFilters) {
                        Text("Clear All")
                    }
                }
            }
            
            // Category Filter
            Column {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = Spacing.xs)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { onCategorySelected(null) },
                            label = { Text("All") }
                        )
                    }
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { 
                                onCategorySelected(if (selectedCategory == category) null else category)
                            },
                            label = { Text(category) }
                        )
                    }
                }
            }
            
            // Groups Filter
            if (groups.isNotEmpty() && onGroupSelected != null) {
                Column {
                    Text(
                        text = "Groups",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = Spacing.xs)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedGroupId == null,
                                onClick = { onGroupSelected(null) },
                                label = { Text("All") }
                            )
                        }
                        items(groups) { group ->
                            FilterChip(
                                selected = selectedGroupId == group.id,
                                onClick = { 
                                    onGroupSelected(if (selectedGroupId == group.id) null else group.id)
                                },
                                label = { 
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    color = Color(android.graphics.Color.parseColor(group.color)),
                                                    shape = CircleShape
                                                )
                                        )
                                        Text(group.name)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            // Tags Filter
            if (availableTags.isNotEmpty()) {
                Column {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = Spacing.xs)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        items(availableTags) { tag ->
                            FilterChip(
                                selected = selectedTags.contains(tag),
                                onClick = {
                                    if (selectedTags.contains(tag)) {
                                        onTagRemoved(tag)
                                    } else {
                                        onTagSelected(tag)
                                    }
                                },
                                label = { Text(tag) }
                            )
                        }
                    }
                }
            }
            
            // Active Filters Display
            if (hasActiveFilters) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedCategory != null) {
                        AssistChip(
                            onClick = { onCategorySelected(null) },
                            label = { Text(selectedCategory) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                    if (selectedGroupId != null && onGroupSelected != null) {
                        val selectedGroup = groups.find { it.id == selectedGroupId }
                        selectedGroup?.let { group ->
                            AssistChip(
                                onClick = { onGroupSelected(null) },
                                label = { 
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(
                                                    color = Color(android.graphics.Color.parseColor(group.color)),
                                                    shape = CircleShape
                                                )
                                        )
                                        Text(group.name)
                                    }
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                    selectedTags.forEach { tag ->
                        AssistChip(
                            onClick = { onTagRemoved(tag) },
                            label = { Text(tag) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

