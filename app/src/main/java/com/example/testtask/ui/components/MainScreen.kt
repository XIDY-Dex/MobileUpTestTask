package com.example.testtask.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.testtask.R
import com.example.testtask.domain.models.CoinListItemModel
import com.example.testtask.ui.viewmodels.MainScreenViewModel
import com.example.testtask.utils.UIState
import kotlin.math.absoluteValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainScreenViewModel, onCoinClick: (String) -> Unit) {
    val currencies = listOf("USD", "RUB")
    var chosenCurrency by remember { mutableIntStateOf(0) }
    val loadingState = viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false)}

    LaunchedEffect(pullToRefreshState.isRefreshing) {
        isRefreshing = pullToRefreshState.isRefreshing
    }

    when(loadingState.value) {
        is UIState.Error -> {
            ErrorScreen { viewModel.loadCoins(currencies[chosenCurrency]) }
        }
        UIState.Loading -> {
            LoadingScreen()
        }
        is UIState.Success<*> -> {
            val coins = (loadingState.value as UIState.Success<List<CoinListItemModel>>).data
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .shadow(1.dp)) {
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "Список криптовалют",
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            currencies.forEachIndexed { index, currency ->
                                Button(
                                    onClick = {
                                        chosenCurrency = index // Устанавливаем выбранную валюту
                                        viewModel.loadCoins(currencies[index])
                                    },
                                    modifier = Modifier
                                        .padding(end = 10.dp)
                                        .width(90.dp)
                                        .height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = if(index == chosenCurrency) colorResource(
                                        id = R.color.chosenCurrencyButtonColor
                                    ).copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.3f), contentColor = if(index == chosenCurrency) colorResource(
                                        id = R.color.chosenCurrencyTextColor
                                    ) else Color.White)
                                ) {
                                    Text(currency)
                                }
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(coins.size) { coin ->
                            CoinListItem(coinInfo = coins[coin]) {
                                onCoinClick(coins[coin].coinName.toLowerCase())
                            }
                        }
                    }

                    if(pullToRefreshState.isRefreshing) {
                        LaunchedEffect(true) {
                            isRefreshing = true
                            viewModel.loadCoins(currencies[chosenCurrency])
                            pullToRefreshState.endRefresh()
                        }
                    }

                    LaunchedEffect(isRefreshing) {
                        if(isRefreshing) {
                            pullToRefreshState.startRefresh()
                        } else {
                            pullToRefreshState.endRefresh()
                        }
                    }
                    
                    PullToRefreshContainer(state = pullToRefreshState, modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp))
                }
            }
        }
    }
}


@SuppressLint("DefaultLocale")
@Composable
fun CoinListItem(coinInfo: CoinListItemModel, onClick: () -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, top = 12.dp)
        .clickable { onClick() }) {
        AsyncImage(model = coinInfo.coinImageUrl, contentDescription = null, modifier = Modifier.size(40.dp))
        Column {
            Text(coinInfo.coinName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(coinInfo.coinShortName, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(end = 10.dp), contentAlignment = Alignment.CenterEnd) {
            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.End) {
                val sign = if(coinInfo.currentTendency < 0) "-" else "+"
                val formatedTendency = String.format("%.4f", coinInfo.currentTendency.absoluteValue)
                Text("${coinInfo.currencyForPrice} ${coinInfo.coinCurrentPrice}")
                Text("$sign${formatedTendency}%", color = if(coinInfo.currentTendency < 0) Color.Red else Color.Green)
            }
        }
    }
}