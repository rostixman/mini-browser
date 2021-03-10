package com.rostixman.webview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URI
import java.net.URL
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.text.method.ScrollingMovementMethod
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.net.http.SslError
import android.webkit.SslErrorHandler




class MainActivity : AppCompatActivity() {

    private fun isHttpOrHttpsUrl(url: String): Boolean {
        val patter = "^(http|https)://.*$"
        return url.matches(patter.toRegex())
    }

    private fun loadUrl() {
        var urlString = urlInput.text.toString()
        if (!isHttpOrHttpsUrl(urlString)) {
            urlString = "http://$urlString"
        }

        var url = URL(urlString)
        val uri = URI(url.protocol, url.userInfo, url.host, url.port, url.path, url.query, url.ref)
        url = uri.toURL()

        urlInput.setText(url.toString())
        webView.loadUrl(url.toString())
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView.settings.javaScriptEnabled = true
        webView.settings.userAgentString = "MAPS.ME_mobile"
        webView.webViewClient = WebViewClient()
        consoleLog.movementMethod = ScrollingMovementMethod()

        urlInput.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        urlInput.setText("10.0.2.2:8000/ru/v2/mobilefront/")

        urlInput.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                this.loadUrl()
                return@OnKeyListener true
            }
            false
        })


        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                urlInput.setText(url)
                super.onPageFinished(view, url)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                consoleLog.text = ""
                super.onPageStarted(view, url, favicon)
            }

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                handler.proceed() // Ignore SSL certificate errors
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                val message = consoleMessage.message() + " -- From line "+ consoleMessage.lineNumber() + " of " + consoleMessage.sourceId();
                val sep = "\n ------------------------------------ \n"

                Log.d("consoleLog", message)


                consoleLog.text = "$message $sep ${consoleLog.text}"

                return super.onConsoleMessage(consoleMessage)
            }

            override fun onProgressChanged(view: WebView, progress: Int) {
                title = "Loading... $progress"
                toolbarProgressBar.visibility = View.VISIBLE
                toolbarProgressBar.progress = progress

                if (progress == 100) {
                    toolbarProgressBar.visibility = View.GONE
                    toolbarProgressBar.progress = 0
                    setTitle(R.string.app_name)
                }
            }


        }

        goButton.setOnClickListener {
            this.loadUrl()
        }

        backButton.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }

        refreshButton.setOnClickListener {
            webView.reload()
        }
    }
}
