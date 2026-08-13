.class public Lorg/openscadplayground/app/MainActivity;
.super Landroid/app/Activity;
.source "MainActivity.java"


# instance fields
.field private webView:Landroid/webkit/WebView;

.field private webChromeClient:Lorg/openscadplayground/app/AssetWebChromeClient;

.field private pendingSaveBytes:[B


# direct methods
.method public constructor <init>()V
    .locals 0

    invoke-direct {p0}, Landroid/app/Activity;-><init>()V

    return-void
.end method


# virtual methods
.method protected onCreate(Landroid/os/Bundle;)V
    .locals 4
    .param p1, "savedInstanceState"    # Landroid/os/Bundle;

    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V

    new-instance v0, Landroid/webkit/WebView;

    invoke-direct {v0, p0}, Landroid/webkit/WebView;-><init>(Landroid/content/Context;)V

    iput-object v0, p0, Lorg/openscadplayground/app/MainActivity;->webView:Landroid/webkit/WebView;

    invoke-virtual {v0}, Landroid/webkit/WebView;->getSettings()Landroid/webkit/WebSettings;

    move-result-object v1

    .local v1, "settings":Landroid/webkit/WebSettings;
    const/4 v2, 0x1

    invoke-virtual {v1, v2}, Landroid/webkit/WebSettings;->setJavaScriptEnabled(Z)V

    invoke-virtual {v1, v2}, Landroid/webkit/WebSettings;->setDomStorageEnabled(Z)V

    invoke-virtual {v1, v2}, Landroid/webkit/WebSettings;->setAllowFileAccess(Z)V

    invoke-virtual {v1, v2}, Landroid/webkit/WebSettings;->setAllowFileAccessFromFileURLs(Z)V

    invoke-virtual {v1, v2}, Landroid/webkit/WebSettings;->setAllowUniversalAccessFromFileURLs(Z)V

    invoke-virtual {v1, v2}, Landroid/webkit/WebSettings;->setAllowContentAccess(Z)V

    invoke-virtual {v1, v2}, Landroid/webkit/WebSettings;->setSupportZoom(Z)V

    invoke-virtual {v1, v2}, Landroid/webkit/WebSettings;->setBuiltInZoomControls(Z)V

    const/4 v3, 0x0

    invoke-virtual {v1, v3}, Landroid/webkit/WebSettings;->setDisplayZoomControls(Z)V

    invoke-virtual {v0, v2}, Landroid/webkit/WebView;->setVerticalScrollBarEnabled(Z)V

    invoke-virtual {v0, v2}, Landroid/webkit/WebView;->setHorizontalScrollBarEnabled(Z)V

    new-instance v3, Lorg/openscadplayground/app/AssetWebViewClient;

    invoke-direct {v3, p0}, Lorg/openscadplayground/app/AssetWebViewClient;-><init>(Landroid/content/Context;)V

    invoke-virtual {v0, v3}, Landroid/webkit/WebView;->setWebViewClient(Landroid/webkit/WebViewClient;)V

    new-instance v3, Lorg/openscadplayground/app/AssetWebChromeClient;

    invoke-direct {v3, p0}, Lorg/openscadplayground/app/AssetWebChromeClient;-><init>(Lorg/openscadplayground/app/MainActivity;)V

    iput-object v3, p0, Lorg/openscadplayground/app/MainActivity;->webChromeClient:Lorg/openscadplayground/app/AssetWebChromeClient;

    invoke-virtual {v0, v3}, Landroid/webkit/WebView;->setWebChromeClient(Landroid/webkit/WebChromeClient;)V

    const-string v3, "AndroidFileBridge"

    invoke-virtual {v0, p0, v3}, Landroid/webkit/WebView;->addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V

    const-string v3, "https://appassets.androidplatform.net/www/index.html"

    invoke-virtual {v0, v3}, Landroid/webkit/WebView;->loadUrl(Ljava/lang/String;)V

    invoke-virtual {p0, v0}, Landroid/app/Activity;->setContentView(Landroid/view/View;)V

    return-void
.end method

.method public onBackPressed()V
    .locals 2

    iget-object v0, p0, Lorg/openscadplayground/app/MainActivity;->webView:Landroid/webkit/WebView;

    if-eqz v0, :cond_0

    iget-object v0, p0, Lorg/openscadplayground/app/MainActivity;->webView:Landroid/webkit/WebView;

    invoke-virtual {v0}, Landroid/webkit/WebView;->canGoBack()Z

    move-result v1

    if-eqz v1, :cond_0

    iget-object v0, p0, Lorg/openscadplayground/app/MainActivity;->webView:Landroid/webkit/WebView;

    invoke-virtual {v0}, Landroid/webkit/WebView;->goBack()V

    return-void

    :cond_0
    invoke-super {p0}, Landroid/app/Activity;->onBackPressed()V

    return-void
.end method

# Called from JavaScript (window.AndroidFileBridge.saveFile(...)) by the
# download-interception script injected into index.html by
# AssetWebViewClient. Runs on a WebView background thread, so the actual
# document picker must be launched on the UI thread.
.method public saveFile(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
    .locals 3
    .param p1, "base64Data"    # Ljava/lang/String;
    .param p2, "filename"    # Ljava/lang/String;
    .param p3, "mimeType"    # Ljava/lang/String;
    .annotation runtime Landroid/webkit/JavascriptInterface;
    .end annotation

    const/4 v0, 0x0

    invoke-static {p1, v0}, Landroid/util/Base64;->decode(Ljava/lang/String;I)[B

    move-result-object v1

    iput-object v1, p0, Lorg/openscadplayground/app/MainActivity;->pendingSaveBytes:[B

    new-instance v2, Lorg/openscadplayground/app/SaveFileTask;

    invoke-direct {v2, p0, p2, p3}, Lorg/openscadplayground/app/SaveFileTask;-><init>(Lorg/openscadplayground/app/MainActivity;Ljava/lang/String;Ljava/lang/String;)V

    invoke-virtual {p0, v2}, Landroid/app/Activity;->runOnUiThread(Ljava/lang/Runnable;)V

    return-void
.end method

.method public onActivityResult(IILandroid/content/Intent;)V
    .locals 5
    .param p1, "requestCode"    # I
    .param p2, "resultCode"    # I
    .param p3, "data"    # Landroid/content/Intent;

    invoke-super {p0, p1, p2, p3}, Landroid/app/Activity;->onActivityResult(IILandroid/content/Intent;)V

    const/16 v0, 0x3e9

    if-ne p1, v0, :check_open

    const/4 v1, -0x1

    if-ne p2, v1, :done

    if-eqz p3, :done

    invoke-virtual {p3}, Landroid/content/Intent;->getData()Landroid/net/Uri;

    move-result-object v2

    if-eqz v2, :done

    :try_start_save
    invoke-virtual {p0}, Landroid/app/Activity;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v3

    invoke-virtual {v3, v2}, Landroid/content/ContentResolver;->openOutputStream(Landroid/net/Uri;)Ljava/io/OutputStream;

    move-result-object v3

    iget-object v4, p0, Lorg/openscadplayground/app/MainActivity;->pendingSaveBytes:[B

    invoke-virtual {v3, v4}, Ljava/io/OutputStream;->write([B)V

    invoke-virtual {v3}, Ljava/io/OutputStream;->close()V
    :try_end_save
    .catch Ljava/io/IOException; {:try_start_save .. :try_end_save} :catch_save

    goto :clear_pending

    :catch_save
    move-exception v3

    :clear_pending
    const/4 v3, 0x0

    iput-object v3, p0, Lorg/openscadplayground/app/MainActivity;->pendingSaveBytes:[B

    goto :done

    :check_open
    const/16 v0, 0x3ea

    if-ne p1, v0, :done

    iget-object v1, p0, Lorg/openscadplayground/app/MainActivity;->webChromeClient:Lorg/openscadplayground/app/AssetWebChromeClient;

    if-eqz v1, :done

    const/4 v2, 0x0

    if-eqz p3, :call_complete

    invoke-virtual {p3}, Landroid/content/Intent;->getData()Landroid/net/Uri;

    move-result-object v2

    :call_complete
    invoke-virtual {v1, v2}, Lorg/openscadplayground/app/AssetWebChromeClient;->completeFileChooser(Landroid/net/Uri;)V

    :done
    return-void
.end method
