.class public Lorg/openscadplayground/app/OpenFileTask;
.super Ljava/lang/Object;
.source "OpenFileTask.java"
.implements Ljava/lang/Runnable;

# Launches the system "Open" document picker (Storage Access Framework)
# on the UI thread, for the "Upload file(s)" editor menu command. The
# picked file's bytes are read back in MainActivity.onActivityResult and
# handed to the web app via window.__openScadNativeOpenFile(...).


# instance fields
.field private final activity:Lorg/openscadplayground/app/MainActivity;


# direct methods
.method public constructor <init>(Lorg/openscadplayground/app/MainActivity;)V
    .locals 0
    .param p1, "activity"    # Lorg/openscadplayground/app/MainActivity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Lorg/openscadplayground/app/OpenFileTask;->activity:Lorg/openscadplayground/app/MainActivity;

    return-void
.end method


# virtual methods
.method public run()V
    .locals 3

    new-instance v0, Landroid/content/Intent;

    const-string v1, "android.intent.action.OPEN_DOCUMENT"

    invoke-direct {v0, v1}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

    const-string v1, "android.intent.category.OPENABLE"

    invoke-virtual {v0, v1}, Landroid/content/Intent;->addCategory(Ljava/lang/String;)Landroid/content/Intent;

    const-string v1, "*/*"

    invoke-virtual {v0, v1}, Landroid/content/Intent;->setType(Ljava/lang/String;)Landroid/content/Intent;

    iget-object v1, p0, Lorg/openscadplayground/app/OpenFileTask;->activity:Lorg/openscadplayground/app/MainActivity;

    const/16 v2, 0x3eb

    invoke-virtual {v1, v0, v2}, Landroid/app/Activity;->startActivityForResult(Landroid/content/Intent;I)V

    return-void
.end method
