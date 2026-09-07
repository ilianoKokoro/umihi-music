### Rules for NewPipeExtractor
-keep class org.mozilla.javascript.** { *; }
-keep class org.mozilla.classfile.ClassFileWriter
-dontwarn org.mozilla.javascript.tools.**
-dontwarn java.beans.*
-dontwarn javax.script.*
-dontwarn jdk.dynalink.**
-dontwarn com.google.re2j.Matcher
-dontwarn com.google.re2j.Pattern
