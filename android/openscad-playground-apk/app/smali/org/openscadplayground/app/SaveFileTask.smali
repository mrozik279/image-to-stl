.class public Lorg/openscadplayground/app/SaveFileTask;
.super Ljava/lang/Object;
.source "SaveFileTask.java"
.implements Ljava/lang/Runnable;

# Launches the system "Save As" document picker (Storage Access Framework)
# on the UI thread. The actual bytes to write are read back from
# MainActivity.pendingSaveBytes in onActivityResult once the user picks a
# location (which can be any provider offered by the picker, including
# Google Drive).


# instance fields
.field private final activity:Lorg/openscadplayground/app/MainActivity;

.field private final filename:Ljava/lang/String;

.field private final mimeType:Ljava/lang/String;


# direct methods
.method public constructor <init>(Lorg/openscadplayground/app/MainActivity;Ljava/lang/String;Ljava/lang/String;)V
    .locals 0
    .param p1, "activity"    # Lorg/openscadplayground/app/MainActivity;
    .param p2, "filename"    # Ljava/lang/String;
    .param p3, "mimeType"    # Ljava/lang/String;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Lorg/openscadplayground/app/SaveFileTask;->activity:Lorg/openscadplayground/app/MainActivity;

    iput-object p2, p0, Lorg/openscadplayground/app/SaveFileTask;->filename:Ljava/lang/String;

    iput-object p3, p0, Lorg/openscadplayground/app/SaveFileTask;->mimeType:Ljava/lang/String;

    return-void
.end method


# virtual methods
.method public run()V
    .locals 4

    new-instance v0, Landroid/content/Intent;

    const-string v1, "android.intent.action.CREATE_DOCUMENT"

    invoke-direct {v0, v1}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

    const-string v1, "android.intent.category.OPENABLE"

    invoke-virtual {v0, v1}, Landroid/content/Intent;->addCategory(Ljava/lang/String;)Landroid/content/Intent;

    iget-object v1, p0, Lorg/openscadplayground/app/SaveFileTask;->mimeType:Ljava/lang/String;

    invoke-virtual {v0, v1}, Landroid/content/Intent;->setType(Ljava/lang/String;)Landroid/content/Intent;

    const-string v1, "android.intent.extra.TITLE"

    iget-object v2, p0, Lorg/openscadplayground/app/SaveFileTask;->filename:Ljava/lang/String;

    invoke-virtual {v0, v1, v2}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;

    iget-object v1, p0, Lorg/openscadplayground/app/SaveFileTask;->activity:Lorg/openscadplayground/app/MainActivity;

    const/16 v2, 0x3e9

    invoke-virtual {v1, v0, v2}, Landroid/app/Activity;->startActivityForResult(Landroid/content/Intent;I)V

    return-void
.end method
