# ViewModel's empty constructor is considered to be unused by proguard, so keep it
-keepclassmembers,allowobfuscation class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keepclassmembers,allowobfuscation class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}