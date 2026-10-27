from pathlib import Path
import re,sqlite3,xml.etree.ElementTree as E,zipfile
p=Path(__file__).resolve().parents[1]; main=p/'app/src/main'; errors=[]
resources={x.attrib['name'] for x in E.parse(main/'res/values/strings.xml').getroot()}
for f in (main/'java').rglob('*.kt'):
 for r in re.findall(r'R.string.(\w+)',f.read_text()):
  assert r in resources,(f,r)
for f in (main/'res').rglob('*.xml'):E.parse(f)
E.parse(main/'AndroidManifest.xml')
with zipfile.ZipFile(p/'gradle/wrapper/gradle-wrapper.jar') as z:assert 'org/gradle/wrapper/GradleWrapperMain.class' in z.namelist()
# Exercise the literal DAO update/delete statements on an in-memory SQLite schema.
db=sqlite3.connect(':memory:');db.execute('PRAGMA foreign_keys=ON')
db.executescript('''CREATE TABLE projects(id INTEGER PRIMARY KEY, name TEXT, description TEXT, emoji TEXT, coverIndex INTEGER);
CREATE TABLE tasks(id INTEGER PRIMARY KEY,projectId INTEGER REFERENCES projects(id) ON DELETE CASCADE,text TEXT,completed INTEGER);
CREATE TABLE notes(id INTEGER PRIMARY KEY,projectId INTEGER REFERENCES projects(id) ON DELETE CASCADE,type TEXT,content TEXT,position INTEGER);
CREATE TABLE cards(id INTEGER PRIMARY KEY,projectId INTEGER REFERENCES projects(id) ON DELETE CASCADE,title TEXT,status TEXT,position INTEGER);
CREATE TABLE settings(name TEXT PRIMARY KEY,value TEXT);
INSERT INTO projects VALUES(1,'Prueba','','',0);
INSERT INTO tasks VALUES(1,1,'Antes',0);
INSERT INTO notes VALUES(1,1,'text','A',1),(2,1,'text','B',2);
INSERT INTO cards VALUES(1,1,'Tarjeta','todo',1);''')
source=(main/'java/com/udb/flowpad/data/FlowpadDao.kt').read_text()
queries=re.findall(r'@Query\("([^"]+)"\)',source)
q=next(q for q in queries if q.startswith('UPDATE tasks SET text'));db.execute(q,{'id':1,'text':'Después'});assert db.execute('SELECT text FROM tasks').fetchone()[0]=='Después'
q=next(q for q in queries if q.startswith('UPDATE cards SET status'));db.execute(q,{'id':1,'status':'done','position':2});assert db.execute('SELECT status FROM cards').fetchone()[0]=='done'
q=next(q for q in queries if q.startswith('UPDATE notes SET position'))
with db:db.execute(q,{'id':1,'position':2});db.execute(q,{'id':2,'position':1})
assert db.execute('SELECT id FROM notes ORDER BY position').fetchall()==[(2,),(1,)]
db.execute(next(q for q in queries if q.startswith('DELETE FROM projects')),{'id':1})
for table in ['tasks','notes','cards']:assert db.execute('SELECT COUNT(*) FROM '+table).fetchone()[0]==0
print('PASS: XML, referencias string, Wrapper; SQL: editar tarea, mover tarjeta, ordenar notas, borrado en cascada.')
print('LIMITACIÓN: pruebas SQL con esquema reconstruido; no sustituyen Room instrumentado, compilación ni Firebase real.')
