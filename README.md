# Fem el Pas — web app

Aplicació web responsive en català amb autenticació Supabase (Google o enllaç per correu), anotacions personals i sincronització protegida amb Row Level Security.

## URL
https://skirep.github.io/fem-el-pas/ (després d'activar Pages i desplegar).

## Configuració obligatòria
1. Al projecte Supabase de Lletrimon (Lletrix al panell), executa `supabase/schema.sql` des del SQL Editor. El fitxer crea `public.fempas_entries` o reanomena l’antiga taula si ja existeix al mateix projecte.
2. A `config.js`, hi ha l’URL i la clau publicable de Lletrimon. Mai una clau `service_role` ni secreta.
3. A Supabase Authentication → URL Configuration, afegeix `https://skirep.github.io/fem-el-pas/` a Redirect URLs i conserva la Site URL de Lletrimon.
4. Habilita el proveïdor Google i configura OAuth al Google Cloud Console amb el callback indicat per Supabase; o utilitza login per enllaç al correu (configura el proveïdor Email).
5. A GitHub → Settings → Pages, selecciona **GitHub Actions** com a font de desplegament. El workflow `pages.yml` publica el contingut de `main`.

## Seguretat i limitacions
La taula té RLS amb polítiques per usuari; no emmagatzemis dades sensibles fins a validar-ho al teu projecte. El repositori és públic i les claus del client són visibles: només hi ha d'haver claus públiques. Les dades es desen a Supabase, no a GitHub. La sessió persisteix al navegador; tanca-la als dispositius compartits. No hi ha xifratge d'extrem a extrem ni migració automàtica de les dades Android. No s'ha validat encara el login real ni la compilació de Pages.

## Recordatoris i calendari
A cada anotació pots establir una data i hora. Desa-la i torna a obrir l'anotació per seleccionar **Afegir al calendari (.ics)**. El fitxer permet importar l'esdeveniment al calendari del mòbil o ordinador, amb una alarma de 15 minuts abans (segons el suport del calendari). Cal confirmar-ne la importació; la web no té permisos per escriure automàticament als calendaris. Per privacitat, només s'exporten el títol i una descripció genèrica, no els detalls íntims. Les modificacions posteriors a Supabase no actualitzen automàticament un esdeveniment ja importat.

**Migració necessària:** executa la versió actualitzada de `supabase/schema.sql` al SQL Editor de Supabase per afegir `reminder_at`. Si no ho fas, les consultes fallaran perquè la columna encara no existeix.
