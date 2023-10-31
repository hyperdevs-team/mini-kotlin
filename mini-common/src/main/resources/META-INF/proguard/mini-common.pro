-keep class mini.codegen.** { *; }

-keepnames class * extends mini.Store { *; }
-keepnames class * extends mini.State { *; }
-keepnames @mini.Action class * { *; }

-keep class mini.Action
-keep @mini.Action class * { *; }

-keep class mini.Resource { *; }

-keep class mini.State { *; }
-keep class * implements mini.State { *; }

-keep class mini.StateContainer { *; }
-keep class * implements mini.StateContainer { *; }