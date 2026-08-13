.class public Lorg/openscadplayground/app/AssetWebChromeClient;
.super Landroid/webkit/WebChromeClient;
.source "AssetWebChromeClient.java"

# Wires up <input type=file> (if the page ever shows one) to Android's
# system document picker (Storage Access Framework), via
# MainActivity.onActivityResult.


# instance fields
.field private final activity:Lorg/openscadplayground/app/MainActivity;

.field private pendingFileCallback:Landroid/webkit/ValueCallback;


# direct methods
.method public constructor <init>(Lorg/openscadplayground/app/MainActivity;)V
    .locals 0
    .param p1, "activity"    # Lorg/openscadplayground/app/MainActivity;

    invoke-direct {p0}, Landroid/webkit/WebChromeClient;-><init>()V

    iput-object p1, p0, Lorg/openscadplayground/app/AssetWebChromeClient;->activity:Lorg/openscadplayground/app/MainActivity;

    return-void
.end method


# virtual methods
.method public onShowFileChooser(Landroid/webkit/WebView;Landroid/webkit/ValueCallback;Landroid/webkit/WebChromeClient$FileChooserParams;)Z
    .locals 3
    .param p1, "webView"    # Landroid/webkit/WebView;
    .param p2, "filePathCallback"    # Landroid/webkit/ValueCallback;
    .param p3, "fileChooserParams"    # Landroid/webkit/WebChromeClient$FileChooserParams;

    iput-object p2, p0, Lorg/openscadplayground/app/AssetWebChromeClient;->pendingFileCallback:Landroid/webkit/ValueCallback;

    new-instance v0, Landroid/content/Intent;

    const-string v1, "android.intent.action.OPEN_DOCUMENT"

    invoke-direct {v0, v1}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

    const-string v1, "android.intent.category.OPENABLE"

    invoke-virtual {v0, v1}, Landroid/content/Intent;->addCategory(Ljava/lang/String;)Landroid/content/Intent;

    const-string v1, "*/*"

    invoke-virtual {v0, v1}, Landroid/content/Intent;->setType(Ljava/lang/String;)Landroid/content/Intent;

    iget-object v1, p0, Lorg/openscadplayground/app/AssetWebChromeClient;->activity:Lorg/openscadplayground/app/MainActivity;

    const/16 v2, 0x3ea

    invoke-virtual {v1, v0, v2}, Landroid/app/Activity;->startActivityForResult(Landroid/content/Intent;I)V

    const/4 v0, 0x1

    return v0
.end method

.method public completeFileChooser(Landroid/net/Uri;)V
    .locals 3
    .param p1, "result"    # Landroid/net/Uri;

    iget-object v0, p0, Lorg/openscadplayground/app/AssetWebChromeClient;->pendingFileCallback:Landroid/webkit/ValueCallback;

    if-eqz v0, :cond_done

    if-eqz p1, :build_empty

    const/4 v1, 0x1

    new-array v2, v1, [Landroid/net/Uri;

    const/4 v1, 0x0

    aput-object p1, v2, v1

    goto :have_array

    :build_empty
    const/4 v1, 0x0

    new-array v2, v1, [Landroid/net/Uri;

    :have_array
    invoke-interface {v0, v2}, Landroid/webkit/ValueCallback;->onReceiveValue(Ljava/lang/Object;)V

    :cond_done
    const/4 v0, 0x0

    iput-object v0, p0, Lorg/openscadplayground/app/AssetWebChromeClient;->pendingFileCallback:Landroid/webkit/ValueCallback;

    return-void
.end method
