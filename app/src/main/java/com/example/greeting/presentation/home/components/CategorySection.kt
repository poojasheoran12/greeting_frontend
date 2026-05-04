package com.example.greeting.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.greeting.domain.model.Template
import com.example.greeting.presentation.core.components.shimmerEffect

@Composable
fun CategorySection(
    title: String,
    templates: LazyPagingItems<Template>,
    onTemplateClick: (Template) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            if (templates.loadState.refresh is LoadState.Loading) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(3) {
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(200.dp)
                                .shimmerEffect()
                        )
                    }
                }
            } else if (templates.loadState.refresh is LoadState.Error) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Failed to load templates",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.Button(onClick = { templates.retry() }) {
                        Text("Retry")
                    }
                }
            } else if (templates.itemCount == 0 && templates.loadState.refresh is LoadState.NotLoading) {
                Text(
                    text = "No templates available",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val listState = remember(title) { androidx.compose.foundation.lazy.LazyListState() }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        count = templates.itemCount,
                        key = templates.itemKey { it.id },
                        contentType = templates.itemContentType { "template" }
                    ) { index ->
                        val template = templates[index]
                        template?.let {
                            TemplateCard(
                                template = it,
                                onClick = { onTemplateClick(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
