-- Executa aquest SQL a Supabase > SQL Editor del projecte escollit.
-- Cada usuari només pot veure i modificar les seves anotacions.
create table if not exists public.fem_el_pas_entries (
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
create index if not exists fem_el_pas_entries_user_created_idx on public.fem_el_pas_entries(user_id,created_at desc);
alter table public.fem_el_pas_entries enable row level security;
revoke all on public.fem_el_pas_entries from anon;
grant select,insert,update,delete on public.fem_el_pas_entries to authenticated;
drop policy if exists "read own entries" on public.fem_el_pas_entries;
drop policy if exists "insert own entries" on public.fem_el_pas_entries;
drop policy if exists "update own entries" on public.fem_el_pas_entries;
drop policy if exists "delete own entries" on public.fem_el_pas_entries;
create policy "read own entries" on public.fem_el_pas_entries for select to authenticated using ((select auth.uid())=user_id);
create policy "insert own entries" on public.fem_el_pas_entries for insert to authenticated with check ((select auth.uid())=user_id);
create policy "update own entries" on public.fem_el_pas_entries for update to authenticated using ((select auth.uid())=user_id) with check ((select auth.uid())=user_id);
create policy "delete own entries" on public.fem_el_pas_entries for delete to authenticated using ((select auth.uid())=user_id);
