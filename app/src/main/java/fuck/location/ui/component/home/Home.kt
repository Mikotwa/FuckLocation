package fuck.location.ui.component.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fuck.location.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                scrollBehavior = scrollBehavior
            )
        }, content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)
                .padding(top = 16.dp)) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null
                            )
                        }
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Activated",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Fuck Location is working properly. [02]",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Fuck Location had altered 0 requests.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        })
}