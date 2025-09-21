@file:OptIn(ExperimentalMaterial3Api::class)

package ca.ilianokokoro.umihi.music.ui.screens.auth

import android.annotation.SuppressLint
import android.app.Application
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AuthScreen(
    onBack: () -> Unit,
    application: Application,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(application))
) {
    val context = LocalContext.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
// Clear Any previous cookies (Not needed)
//        WebStorage.getInstance().deleteAllData()
//        CookieManager.getInstance().removeAllCookies(null)
//        CookieManager.getInstance().flush()
        authViewModel.eventChannel.collectLatest {
            when (it) {
                is AuthViewModel.ScreenEvent.Out.LoginCompleted -> {
                    coroutineScope.launch {
                        Toast
                            .makeText(
                                context,
                                R.string.login_success,
                                Toast.LENGTH_SHORT,
                            ).show()
                        onBack()
                    }
                }

            }

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
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true

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
            },
            onRelease = { view ->
                view.stopLoading()
                view.destroy()
            }
        )
    }
}


