(() => {
'use strict';
const $ = id => document.getElementById(id);
const show = (id,yes) => $(id).classList.toggle('hidden',!yes);
const cfg=window.FEM_EL_PAS_CONFIG||{};
const configured=Boolean(cfg.supabaseUrl && cfg.supabaseAnonKey && window.supabase);
let db=null,user=null,entries=[],filter='all',editing=null;
const keys=['kind','title','what','felt','own','next'];
const message=(id,text,error=false)=>{ $(id).textContent=text; $(id).classList.toggle('error',error); };
const safeError=e=>e?.message||'No s’ha pogut completar l’operació.';
function view(which){['setup-view','auth-view','dashboard'].forEach(id=>show(id,id===which));show('logout',which==='dashboard');}
function init(){
 $('today').textContent=new Intl.DateTimeFormat('ca-ES',{day:'numeric',month:'long',year:'numeric'}).format(new Date());
 if(!configured){view('setup-view');return;}
 db=window.supabase.createClient(cfg.supabaseUrl,cfg.supabaseAnonKey,{auth:{storageKey:'fempas-auth',persistSession:true,autoRefreshToken:true,detectSessionInUrl:true}});
 db.auth.onAuthStateChange((_event,session)=>{const next=session?.user||null;if(next?.id!==user?.id){user=next;entries=[];if(user){view('dashboard');setTimeout(loadEntries,0)}else view('auth-view')}});
 db.auth.getSession().then(({data,error})=>{if(error)message('auth-message',safeError(error),true);user=data?.session?.user||null;if(user){view('dashboard');loadEntries()}else view('auth-view')});
}
async function loadEntries(){
 if(!user)return;
 const {data,error}=await db.from('fempas_entries').select('id,kind,title,what,felt,own,next,done,created_at').order('created_at',{ascending:false}).limit(200);
 if(error){$('entry-list').textContent='Error carregant les anotacions: '+safeError(error);return;}
 entries=data||[];render();
}
function render(){
 $('pending-count').textContent=entries.filter(e=>e.kind==='conversation'&&!e.done).length;
 $('closed-count').textContent=entries.filter(e=>e.kind==='conversation'&&e.done).length;
 $('reflection-count').textContent=entries.filter(e=>e.kind==='reflection').length;
 const list=$('entry-list');list.replaceChildren();
 const selected=entries.filter(e=>filter==='all'||(filter==='pending'&&!e.done)||(filter==='closed'&&e.done));
 if(!selected.length){const empty=document.createElement('div');empty.className='empty';empty.textContent='Encara no hi ha anotacions en aquesta categoria. Pots començar amb una reflexió.';list.append(empty);return;}
 for(const entry of selected){
  const btn=document.createElement('button');btn.type='button';btn.className='entry';
  const left=document.createElement('div'),name=document.createElement('strong'),date=document.createElement('small'),tag=document.createElement('span');
  name.textContent=entry.title;date.textContent=new Intl.DateTimeFormat('ca-ES',{day:'numeric',month:'short',year:'numeric'}).format(new Date(entry.created_at))+' · '+(entry.kind==='reflection'?'Reflexió':'Conversa');
  tag.className='tag'+(entry.done?' closed':'');tag.textContent=entry.done?'Tancada':'Pendent';
  left.append(name,date);btn.append(left,tag);btn.addEventListener('click',()=>openEditor(entry));list.append(btn);
 }
}
function openEditor(entry=null){
 editing=entry?.id||null;$('editor-title').textContent=entry?'Editar anotació':'Nova anotació';
 for(const key of keys)$(key).value=entry?.[key]|| (key==='kind'?'conversation':'');
 $('done').checked=Boolean(entry?.done);show('delete-entry',Boolean(entry));message('form-message','');$('editor').showModal();
}
async function save(e){
 e.preventDefault();if(!user)return;
 const payload={user_id:user.id,done:$('done').checked};
 for(const key of keys)payload[key]=$(key).value.trim();
 if(!payload.title){message('form-message','Escriu un títol.',true);return;}
 const btn=$('entry-form').querySelector('[type=submit]');btn.disabled=true;
 try{
  const q=editing?db.from('fempas_entries').update(payload).eq('id',editing).eq('user_id',user.id):db.from('fempas_entries').insert(payload);
  const {error}=await q;if(error)throw error;
  $('editor').close();await loadEntries();
 }catch(err){message('form-message',safeError(err),true)}
 finally{btn.disabled=false;}
}
async function remove(){
 if(!editing||!user||!confirm('Vols eliminar definitivament aquesta anotació?'))return;
 const {error}=await db.from('fempas_entries').delete().eq('id',editing).eq('user_id',user.id);
 if(error){message('form-message',safeError(error),true);return;}
 $('editor').close();await loadEntries();
}
$('login-form').addEventListener('submit',async e=>{
 e.preventDefault();if(!db)return;const email=$('email').value.trim();const btn=$('login-form').querySelector('button');btn.disabled=true;
 try{const {error}=await db.auth.signInWithOtp({email,options:{emailRedirectTo:location.origin+location.pathname}});if(error)throw error;message('auth-message','Revisa el correu: t’hem enviat un enllaç per entrar.');}
 catch(err){message('auth-message',safeError(err),true)}finally{btn.disabled=false;}
});
$('google-login').addEventListener('click',async()=>{if(!db)return;const {error}=await db.auth.signInWithOAuth({provider:'google',options:{redirectTo:location.origin+location.pathname}});if(error)message('auth-message',safeError(error),true)});
$('logout').addEventListener('click',async()=>{if(db){await db.auth.signOut();user=null;entries=[];view('auth-view')}});
$('new-entry').addEventListener('click',()=>openEditor());
$('close-editor').addEventListener('click',()=>$('editor').close());
$('entry-form').addEventListener('submit',save);
$('delete-entry').addEventListener('click',remove);
document.querySelectorAll('.filter').forEach(b=>b.addEventListener('click',()=>{filter=b.dataset.filter;document.querySelectorAll('.filter').forEach(x=>x.classList.toggle('active',x===b));render()}));
init();
})();