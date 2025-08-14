@file:OptIn(ExperimentalMaterial3Api::class)

package ca.ilianokokoro.umihi.music.ui.screens.auth

import android.annotation.SuppressLint
import android.app.Application
import android.view.ContextThemeWrapper
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AuthScreen(
    onBack: () -> Unit,
    application: Application,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(application))
) {
    val uiState = authViewModel.uiState.collectAsStateWithLifecycle().value

    if (uiState.isLoggedIn) {
        LaunchedEffect(Unit) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back_description)
                        )
                    }
                },
                title = { Text(stringResource(R.string.log_in)) }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val isDarkMode = isSystemInDarkTheme()

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = { ctx ->

                val themedContext = ContextThemeWrapper(
                    ctx,
                    if (isDarkMode) R.style.Theme_WebView_Dark
                    else R.style.Theme_WebView_Light
                )

                WebView(themedContext).apply {
                    settings.javaScriptEnabled = true

                    if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                        WebSettingsCompat.setAlgorithmicDarkeningAllowed(
                            settings,
                            true
                        )
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            authViewModel.onPageFinished(url)
                        }
                    }
                    loadUrl(Constants.Auth.START_URL)
                }
            }
        )
    }
}


