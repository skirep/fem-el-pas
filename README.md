# Fem el Pas — Android

Aplicació Android nativa en català per gestionar un protocol de reparació de sis passos, registrar discussions i reprendre converses pendents.

## Compilació
Java 17, Android SDK 35, Gradle 8.9. Executa `gradle :app:assembleDebug` o utilitza GitHub Actions > Build Android APK. L'APK debug es publica com a artifact de l'execució.

## Privacitat
Les dades es desen localment a SharedPreferences, sense xifratge específic de l'aplicació. No es demana permís d'Internet ni s'activa la còpia de seguretat. Evita dades especialment sensibles fins a incorporar xifratge i bloqueig biomètric.

## Estat
Versió inicial, compilació pendent de verificació a GitHub Actions. No inclou notificacions ni sincronització.
