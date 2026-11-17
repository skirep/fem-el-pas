-- Executa aquest SQL al SQL Editor de Lletrimon (Lletrix al panell de Supabase).
-- Cada usuari només pot veure i modificar les seves anotacions.
begin;

-- Conserva les files si Fem el Pas ja utilitzava aquest mateix projecte.
do $migration$
begin
 if to_regclass('public.fem_el_pas_entries') is not null then
  if to_regclass('public.fempas_entries') is null then
   alter table public.fem_el_pas_entries rename to fempas_entries;
   alter index if exists public.fem_el_pas_entries_user_created_idx rename to fempas_entries_user_created_idx;
  else
   raise exception 'Existeixen les dues taules de Fem el Pas. Revisa les dades abans de migrar.';
  end if;
 end if;
end;
$migration$;

create table if not exists public.fempas_entries (
 id uuid primary key default gen_random_uuid(),
 user_id uuid not null references auth.users(id) on delete cascade,
 kind text not null default 'conversation' check (kind in ('conversation','reflection')),
 title text not null check (char_length(title) between 1 and 120),
 what text not null default '',
 felt text not null default '',
 own text not null default '',
 next text not null default '',
 done boolean not null default false,
 created_at timestamptz not null default now()
);
alter table public.fempas_entries add column if not exists reminder_at timestamptz;
create index if not exists fempas_entries_user_reminder_idx on public.fempas_entries(user_id,reminder_at) where reminder_at is not null;
create index if not exists fempas_entries_user_created_idx on public.fempas_entries(user_id,created_at desc);
alter table public.fempas_entries enable row level security;
revoke all on public.fempas_entries from anon;
grant select,insert,update,delete on public.fempas_entries to authenticated;
drop policy if exists "read own entries" on public.fempas_entries;
drop policy if exists "insert own entries" on public.fempas_entries;
drop policy if exists "update own entries" on public.fempas_entries;
drop policy if exists "delete own entries" on public.fempas_entries;
create policy "read own entries" on public.fempas_entries for select to authenticated using ((select auth.uid())=user_id);
create policy "insert own entries" on public.fempas_entries for insert to authenticated with check ((select auth.uid())=user_id);
create policy "update own entries" on public.fempas_entries for update to authenticated using ((select auth.uid())=user_id) with check ((select auth.uid())=user_id);
create policy "delete own entries" on public.fempas_entries for delete to authenticated using ((select auth.uid())=user_id);

commit;
