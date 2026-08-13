.class public Lorg/openscadplayground/app/AssetWebViewClient;
.super Landroid/webkit/WebViewClient;
.source "AssetWebViewClient.java"

# Serves the app's assets/www/ tree under a virtual https:// origin instead
# of file://, so Web Workers and fetch() (used to load the OpenSCAD WASM
# worker and the bundled library .zip archives) work the same way they do
# in a normal secure browser context. Equivalent to androidx.webkit's
# WebViewAssetLoader, written by hand to avoid a Maven dependency.


# instance fields
.field private context:Landroid/content/Context;


# direct methods
.method public constructor <init>(Landroid/content/Context;)V
    .locals 0
    .param p1, "context"    # Landroid/content/Context;

    invoke-direct {p0}, Landroid/webkit/WebViewClient;-><init>()V

    iput-object p1, p0, Lorg/openscadplayground/app/AssetWebViewClient;->context:Landroid/content/Context;

    return-void
.end method

.method private static guessMime(Ljava/lang/String;)Ljava/lang/String;
    .locals 1
    .param p0, "path"    # Ljava/lang/String;

    const-string v0, ".html"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_htm
    const-string v0, "text/html"
    return-object v0

    :next_htm
    const-string v0, ".htm"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_js
    const-string v0, "text/html"
    return-object v0

    :next_js
    const-string v0, ".js"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_css
    const-string v0, "application/javascript"
    return-object v0

    :next_css
    const-string v0, ".css"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_json
    const-string v0, "text/css"
    return-object v0

    :next_json
    const-string v0, ".json"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_wasm
    const-string v0, "application/json"
    return-object v0

    :next_wasm
    const-string v0, ".wasm"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_woff2
    const-string v0, "application/wasm"
    return-object v0

    :next_woff2
    const-string v0, ".woff2"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_woff
    const-string v0, "font/woff2"
    return-object v0

    :next_woff
    const-string v0, ".woff"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_ttf
    const-string v0, "font/woff"
    return-object v0

    :next_ttf
    const-string v0, ".ttf"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_eot
    const-string v0, "font/ttf"
    return-object v0

    :next_eot
    const-string v0, ".eot"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_svg
    const-string v0, "application/vnd.ms-fontobject"
    return-object v0

    :next_svg
    const-string v0, ".svg"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_png
    const-string v0, "image/svg+xml"
    return-object v0

    :next_png
    const-string v0, ".png"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_jpg
    const-string v0, "image/png"
    return-object v0

    :next_jpg
    const-string v0, ".jpg"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_jpeg
    const-string v0, "image/jpeg"
    return-object v0

    :next_jpeg
    const-string v0, ".jpeg"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_ico
    const-string v0, "image/jpeg"
    return-object v0

    :next_ico
    const-string v0, ".ico"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_glb
    const-string v0, "image/x-icon"
    return-object v0

    :next_glb
    const-string v0, ".glb"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_zip
    const-string v0, "model/gltf-binary"
    return-object v0

    :next_zip
    const-string v0, ".zip"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_wav
    const-string v0, "application/zip"
    return-object v0

    :next_wav
    const-string v0, ".wav"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :next_txt
    const-string v0, "audio/wav"
    return-object v0

    :next_txt
    const-string v0, ".txt"
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :default_mime
    const-string v0, "text/plain"
    return-object v0

    :default_mime
    const-string v0, "application/octet-stream"
    return-object v0
.end method


# virtual methods
.method public shouldInterceptRequest(Landroid/webkit/WebView;Landroid/webkit/WebResourceRequest;)Landroid/webkit/WebResourceResponse;
    .locals 10
    .param p1, "view"    # Landroid/webkit/WebView;
    .param p2, "request"    # Landroid/webkit/WebResourceRequest;

    invoke-interface {p2}, Landroid/webkit/WebResourceRequest;->getUrl()Landroid/net/Uri;
    move-result-object v0

    invoke-virtual {v0}, Landroid/net/Uri;->getHost()Ljava/lang/String;
    move-result-object v1

    const-string v2, "appassets.androidplatform.net"
    invoke-virtual {v2, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v2

    if-nez v2, :is_ours

    const/4 v0, 0x0
    return-object v0

    :is_ours
    invoke-virtual {v0}, Landroid/net/Uri;->getPath()Ljava/lang/String;
    move-result-object v1

    const/4 v2, 0x1
    invoke-virtual {v1}, Ljava/lang/String;->length()I
    move-result v3
    invoke-virtual {v1, v2, v3}, Ljava/lang/String;->substring(II)Ljava/lang/String;
    move-result-object v1

    # Never serve the app's own service worker: it precaches/replays
    # requests (StaleWhileRevalidate for everything) in a way that would
    # keep serving a stale, pre-injection copy of index.html across APK
    # updates, since Service Worker storage isn't tied to the APK and
    # survives reinstalling a new one. We don't need offline caching here
    # (everything is already local via this class), so just 404 it.
    const-string v2, "www/sw.js"
    invoke-virtual {v2, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :not_sw
    const/4 v0, 0x0
    return-object v0

    :not_sw
    iget-object v2, p0, Lorg/openscadplayground/app/AssetWebViewClient;->context:Landroid/content/Context;
    invoke-virtual {v2}, Landroid/content/Context;->getAssets()Landroid/content/res/AssetManager;
    move-result-object v2

    :try_start_0
    invoke-virtual {v2, v1}, Landroid/content/res/AssetManager;->open(Ljava/lang/String;)Ljava/io/InputStream;
    move-result-object v3

    const-string v4, "www/index.html"
    invoke-virtual {v4, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v4
    if-eqz v4, :not_index

    invoke-static {v3}, Lorg/openscadplayground/app/AssetWebViewClient;->readAllBytes(Ljava/io/InputStream;)[B
    move-result-object v5

    new-instance v6, Ljava/lang/String;
    invoke-direct {v6, v5}, Ljava/lang/String;-><init>([B)V

    const-string v7, "</head>"

    const-string v9, "<script>(function(){if(\'serviceWorker\' in navigator){navigator.serviceWorker.getRegistrations().then(function(rs){rs.forEach(function(r){r.unregister();});});}if(\'caches\' in window){caches.keys().then(function(ks){ks.forEach(function(k){caches.delete(k);});});}var oc=HTMLAnchorElement.prototype.click;HTMLAnchorElement.prototype.click=function(){try{if(this.hasAttribute(\'download\')&&this.href&&this.href.indexOf(\'blob:\')===0&&window.AndroidFileBridge){var fn=this.getAttribute(\'download\')||\'download\';var href=this.href;fetch(href).then(function(r){return r.blob();}).then(function(blob){var rd=new FileReader();rd.onloadend=function(){var du=rd.result;var i=du.indexOf(\',\');var b64=du.substring(i+1);var mt=blob.type||\'application/octet-stream\';window.AndroidFileBridge.saveFile(b64,fn,mt);};rd.readAsDataURL(blob);}).catch(function(e){console.error(\'save failed\',e);});return;}}catch(e){console.error(\'intercept error\',e);}return oc.apply(this,arguments);};document.addEventListener(\'click\',function(ev){var a=ev.target&&ev.target.closest?ev.target.closest(\'a[download]\'):null;if(a&&a.href&&a.href.indexOf(\'blob:\')===0&&window.AndroidFileBridge){ev.preventDefault();a.click();}},true);})();</script></head>"

    invoke-virtual {v6, v7, v9}, Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/String;->getBytes()[B
    move-result-object v5

    new-instance v3, Ljava/io/ByteArrayInputStream;
    invoke-direct {v3, v5}, Ljava/io/ByteArrayInputStream;-><init>([B)V

    :not_index
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_0

    invoke-static {v1}, Lorg/openscadplayground/app/AssetWebViewClient;->guessMime(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v4

    new-instance v5, Landroid/webkit/WebResourceResponse;
    const-string v0, "UTF-8"
    invoke-direct {v5, v4, v0, v3}, Landroid/webkit/WebResourceResponse;-><init>(Ljava/lang/String;Ljava/lang/String;Ljava/io/InputStream;)V

    return-object v5

    :catch_0
    move-exception v0
    const/4 v0, 0x0
    return-object v0
.end method

.method private static readAllBytes(Ljava/io/InputStream;)[B
    .locals 5
    .param p0, "is"    # Ljava/io/InputStream;

    new-instance v0, Ljava/io/ByteArrayOutputStream;
    invoke-direct {v0}, Ljava/io/ByteArrayOutputStream;-><init>()V

    const/16 v1, 0x1000
    new-array v2, v1, [B

    :loop_start
    invoke-virtual {p0, v2}, Ljava/io/InputStream;->read([B)I
    move-result v3

    const/4 v4, -0x1
    if-eq v3, v4, :loop_end

    const/4 v4, 0x0
    invoke-virtual {v0, v2, v4, v3}, Ljava/io/ByteArrayOutputStream;->write([BII)V
    goto :loop_start

    :loop_end
    invoke-virtual {v0}, Ljava/io/ByteArrayOutputStream;->toByteArray()[B
    move-result-object v3
    return-object v3
.end method
