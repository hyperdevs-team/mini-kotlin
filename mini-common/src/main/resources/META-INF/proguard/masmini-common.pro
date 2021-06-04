-keep class mini.codegen.** { *; }

-keepnames class * extends mini.Store { *; }

-keep class mini.Action
-keep @mini.Action class * { *; }

-keep class mini.Resource { *; }