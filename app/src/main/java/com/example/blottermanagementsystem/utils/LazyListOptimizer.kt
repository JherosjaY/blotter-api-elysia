package com.example.blottermanagementsystem.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Optimized LazyList utilities for smooth 60 FPS scrolling
 */
object LazyListOptimizer {
    
    /**
     * Optimal item spacing for smooth scrolling
     */
    val OPTIMAL_ITEM_SPACING: Dp = 8.dp
    
    /**
     * Optimal content padding
     */
    val OPTIMAL_CONTENT_PADDING: Dp = 16.dp
    
    /**
     * Prefetch distance (items to load ahead)
     */
    const val PREFETCH_DISTANCE = 3
    
    /**
     * Remember scroll position to restore on recomposition
     */
    @Composable
    fun rememberOptimizedLazyListState(
        initialFirstVisibleItemIndex: Int = 0,
        initialFirstVisibleItemScrollOffset: Int = 0
    ): LazyListState {
        return rememberSaveable(
            saver = LazyListState.Saver
        ) {
            LazyListState(
                initialFirstVisibleItemIndex,
                initialFirstVisibleItemScrollOffset
            )
        }
    }
    
    /**
     * Detect if list is scrolling (for hiding UI elements)
     */
    @Composable
    fun LazyListState.isScrollingUp(): Boolean {
        var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
        var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
        
        return remember(this) {
            derivedStateOf {
                if (previousIndex != firstVisibleItemIndex) {
                    previousIndex > firstVisibleItemIndex
                } else {
                    previousScrollOffset >= firstVisibleItemScrollOffset
                }.also {
                    previousIndex = firstVisibleItemIndex
                    previousScrollOffset = firstVisibleItemScrollOffset
                }
            }
        }.value
    }
    
    /**
     * Detect if reached end of list (for pagination)
     */
    @Composable
    fun LazyListState.isAtEnd(): Boolean {
        return remember(this) {
            derivedStateOf {
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                lastVisibleItem?.index == layoutInfo.totalItemsCount - 1
            }
        }.value
    }
}

/**
 * Pagination helper for large lists
 */
class PaginationState<T>(
    private val pageSize: Int = 20,
    private val allItems: List<T>
) {
    private var currentPage by mutableStateOf(0)
    
    val visibleItems: List<T>
        get() = allItems.take((currentPage + 1) * pageSize)
    
    val hasMore: Boolean
        get() = visibleItems.size < allItems.size
    
    fun loadMore() {
        if (hasMore) {
            currentPage++
        }
    }
    
    fun reset() {
        currentPage = 0
    }
}

/**
 * Remember pagination state
 */
@Composable
fun <T> rememberPaginationState(
    items: List<T>,
    pageSize: Int = 20
): PaginationState<T> {
    return remember(items) {
        PaginationState(pageSize, items)
    }
}

/**
 * Optimized key for LazyColumn items
 * Prevents unnecessary recompositions
 */
fun <T> optimizedItemKey(item: T, getId: (T) -> Any): Any {
    return getId(item)
}

/**
 * Content type for better recycling
 */
fun <T> optimizedContentType(item: T, getType: (T) -> Any): Any {
    return getType(item)
}
