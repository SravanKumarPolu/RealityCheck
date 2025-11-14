package com.realitycheck.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.realitycheck.app.R

@Composable
fun RealityCheckLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    Image(
        painter = painterResource(id = R.drawable.ic_logo),
        contentDescription = "RealityCheck Logo",
        modifier = modifier.size(size)
    )
}

@Composable
fun RealityCheckLogoSmall(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    Image(
        painter = painterResource(id = R.drawable.ic_logo_simple),
        contentDescription = "RealityCheck Logo",
        modifier = modifier.size(size)
    )
}

