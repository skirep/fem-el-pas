# Fem el Pas — web app

Aplicació web responsive en català amb autenticació Supabase (Google o enllaç per correu), anotacions personals i sincronització protegida amb Row Level Security.

## URL
https://skirep.github.io/fem-el-pas/ (després d'activar Pages i desplegar).

## Configuració obligatòria
1. A Supabase, executa `supabase/schema.sql` des del SQL Editor.
2. A `config.js`, introdueix el **Project URL** i la clau pública **publishable/anon** del projecte. Mai una clau `service_role` ni secreta.
3. A Supabase Authentication → URL Configuration, configura Site URL i Redirect URLs: `https://skirep.github.io/fem-el-pas/`.
4. Habilita el proveïdor Google i configura OAuth al Google Cloud Console amb el callback indicat per Supabase; o utilitza login per enllaç al correu (configura el proveïdor Email).
5. A GitHub → Settings → Pages, selecciona **GitHub Actions** com a font de desplegament. El workflow `pages.yml` publica el contingut de `main`.

## Seguretat i limitacions
La taula té RLS amb polítiques per usuari; no emmagatzemis dades sensibles fins a validar-ho al teu projecte. El repositori és públic i les claus del client són visibles: només hi ha d'haver claus públiques. Les dades es desen a Supabase, no a GitHub. La sessió persisteix al navegador; tanca-la als dispositius compartits. No hi ha xifratge d'extrem a extrem ni migració automàtica de les dades Android. No s'ha validat encara el login real ni la compilació de Pages.
