-keep class masmini.codegen.** { *; }

-keepnames class * extends masmini.Store { *; }

-keep class masmini.Action
-keep @masmini.Action class * { *; }

-keep class masmini.Resource { *; }