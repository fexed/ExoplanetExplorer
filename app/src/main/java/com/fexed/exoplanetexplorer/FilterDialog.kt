package com.fexed.exoplanetexplorer

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

fun getCategoryLocalizedName(context: Context, category: Int): String {
    val str = (
            when(category) {
                0 -> context.getString(R.string.label_category_rocky_mercurian)
                1 -> context.getString(R.string.label_category_rocky_subterran)
                2 -> context.getString(R.string.label_category_rocky_terran)
                3 -> context.getString(R.string.label_category_rocky_superterran)
                4 -> context.getString(R.string.label_category_gasgiant_neptunian)
                5 -> context.getString(R.string.label_category_gasgiant_jovian)
                else -> context.getString(R.string.label_category_unknown)
            }
    )
    return str
}

@Composable
fun FilterDialog(
    selectedOrder: Int,
    invertedOrder: Boolean,
    orderOptions: List<String>,
    searchQuery: String,
    facilityOptions: List<String>,
    telescopeOptions: List<String>,
    facilityCounts: Map<String, Int>,
    telescopeCounts: Map<String, Int>,
    selectedFacility: String?,
    selectedTelescope: String?,
    onOrderSelected: (Int) -> Unit,
    onInvertedOrderChanged: (Boolean) -> Unit,
    onFiltersApplied: (String) -> Unit,
    onFacilitySelected: (String?) -> Unit,
    onTelescopeSelected: (String?) -> Unit,
    onClose: () -> Unit
) {
    var query by remember { mutableStateOf(searchQuery) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onClose, DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = MaterialTheme.shapes.large, elevation = 10.dp, modifier = Modifier
            .padding(all = 16.dp)
            .wrapContentHeight()) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text(text = stringResource(R.string.title_filter), style = MaterialTheme.typography.h5)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(text = stringResource(R.string.title_search)) },
                    trailingIcon = if (query.isNotEmpty()) {
                        {
                            IconButton(onClick = { query = "" }) {
                                Image(
                                    painter = painterResource(R.drawable.clear),
                                    contentDescription = stringResource(R.string.label_clear_search)
                                )
                            }
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                FilterSelectionField(
                    title = stringResource(R.string.label_discovery_facility),
                    selected = selectedFacility,
                    options = facilityOptions,
                    counts = facilityCounts,
                    allLabel = stringResource(R.string.label_all_facilities),
                    onSelected = onFacilitySelected
                )
                Spacer(modifier = Modifier.height(8.dp))
                FilterSelectionField(
                    title = stringResource(R.string.label_discovery_telescope),
                    selected = selectedTelescope,
                    options = telescopeOptions,
                    counts = telescopeCounts,
                    allLabel = stringResource(R.string.label_all_telescopes),
                    onSelected = onTelescopeSelected
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Text(text = stringResource(R.string.label_orderby), style = MaterialTheme.typography.h5)
                    Row(
                        modifier = Modifier
                            .clickable(onClick = { expanded = true })
                            .height(48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.clickable{
                            onInvertedOrderChanged(!invertedOrder)
                        }) {
                            Icon(
                                modifier = Modifier.rotate(if (invertedOrder) 90f else -90f).size(48.dp),
                                painter = painterResource(R.drawable.switch_order),
                                contentDescription = null
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = orderOptions[selectedOrder],
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Image(
                            modifier = Modifier.size(48.dp),
                            painter = painterResource(R.drawable.dropdownarrow),
                            contentDescription = null
                        )
                    }
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    Text(modifier = Modifier.padding(8.dp), text = if (invertedOrder) stringResource(R.string.title_order_inverted) else stringResource(R.string.title_order), fontWeight = FontWeight.Bold)
                    orderOptions.forEachIndexed { index, value ->
                        if (index > 0) {
                            Divider()
                        }
                        DropdownMenuItem(onClick = {
                            expanded = false
                            onOrderSelected(index)
                        }) {
                            Text(text = value)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = {
                        onFiltersApplied(query.trim())
                    }) {
                        Text(text = stringResource(R.string.title_filter), color = MaterialTheme.colors.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSelectionField(
    title: String,
    selected: String?,
    options: List<String>,
    counts: Map<String, Int>,
    allLabel: String,
    onSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(text = title, style = MaterialTheme.typography.subtitle2)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selected ?: allLabel,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Image(
                painter = painterResource(R.drawable.dropdownarrow),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(onClick = {
                onSelected(null)
                expanded = false
            }) {
                Text(text = allLabel)
            }
            if (options.isNotEmpty()) {
                Divider()
            }
            options.forEachIndexed { index, option ->
                DropdownMenuItem(onClick = {
                    onSelected(option)
                    expanded = false
                }) {
                    Text(
                        text = stringResource(
                            R.string.label_filter_option_count,
                            option,
                            counts[option] ?: 0
                        )
                    )
                }
                if (index < options.lastIndex) {
                    Divider()
                }
            }
        }
    }
}

@Composable
fun getOrderOptions(invertedOrder: Boolean): List<String> = listOf(
        stringResource(R.string.label_order_none),
        (if (invertedOrder) stringResource(R.string.label_order_name_desc) else stringResource(R.string.label_order_name_asc)),
        (if (invertedOrder) stringResource(R.string.label_order_year_desc) else stringResource(R.string.label_order_year_asc)),
        (if (invertedOrder) stringResource(R.string.label_order_radius_desc) else stringResource(R.string.label_order_radius_asc)),
        (if (invertedOrder) stringResource(R.string.label_order_mass_desc) else stringResource(R.string.label_order_mass_asc)),
        (if (invertedOrder) stringResource(R.string.label_order_star_desc) else stringResource(R.string.label_order_star_asc)),
        (if (invertedOrder) stringResource(R.string.label_order_distanceearth_desc) else stringResource(R.string.label_order_distanceearth_asc)),
        (if (invertedOrder) stringResource(R.string.label_order_period_desc) else stringResource(R.string.label_order_period_asc)),
        (if (invertedOrder) stringResource(R.string.label_order_distancestar_desc) else stringResource(R.string.label_order_distancestar_asc)),
    )

