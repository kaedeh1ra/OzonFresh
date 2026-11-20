# Ktor и OkHttp ссылаются на JVM-классы, которых нет на Android; в рантайме они не используются.
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
