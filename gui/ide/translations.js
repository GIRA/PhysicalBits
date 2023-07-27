
var TRANSLATIONS = [
  // spec
  ["label", "en", "es", "et", "se"],

  ["New...", null, "Nuevo...", "Uus...", "Ny..."],
  ["Open...", null, "Abrir...", "Ava...", "Öppna..."],
  ["Save", null, "Guardar", "Salvesta", "Spara"],
  ["Save as...", null, "Guardar como...", "Salvesta kui...", "Spara som..."],
  ["Download...", null, "Descargar...", "Laadi...", "Ladda..."],
  ["Automatic", null, "Automático", "Automaatne", "Automatisk"],
  ["Simulator", null, "Simulador", null, null],
  ["Other...", null, "Otro...", "Muu...", "Annan..."],
  ["Connect", null, "Conectar", "Ühenda", "Anslut"],
  ["Disconnect", null, "Desconectar", "Ühenda lahti", "Koppla ifrån"],
  ["Verify", null, "Verificar", "Kontrolli", "Kontrollera"],
  ["Run", null, "Ejecutar", "Jooksuta", "Kör"],
  ["Install", null, "Instalar", "Paigalda", "Installera"],
  ["Debug", null, "Depurar", "Silu", null],
  ["Interactive mode", null, "Modo interactivo", "Interaktiivne", "Interaktiv"],
  ["Options...", null, "Opciones...", "Seaded...", "Inställningar..."],
  ["Pins", null, "Pines", "Viigud", "Ben"],
  ["* no values reporting *", null, "* no hay datos *", "* väärtusi ei edastata *", "* ingen värden rapporteras *"],
  ["Globals", null, "Variables globales", "Globaalsed muutujad", "Globala variabler"],
  ["ERROR: Broken layout", null, "ERROR: Diseño arruinado", "VIGA: Katkine asetus", "FEL: bruten layout"],
  ["It seems you've broken the page layout.", null, "Parece que se rompió el diseño de la aplicación.", "Paistab, et sa oled asetuse lõhkunud.", "Det verkar som något gått fel med layouten"],
  ["But don't worry! Click the button below to restore it to its former glory.", null, "¡Pero no te preocupes! Hacé clic en el botón siguiente para restaurarlo.", "Aga ära pabista! Vajuta nupule, et taastada õige seis.", "Men oroa dig inte! Klicka på knappen nedan för att återställa den till sin tidigare härlighet."],
  ["Restore layout", null, "Restaurar diseño", "Taasta asetus", "Återställ layout"],
  ["ERROR: Server not found", null, "ERROR: Servidor no encontrado", "VIGA: Serverit ei leitud", "FEL: Servern kunde inte nås"],
  ["Please make sure the Physical BITS server is up and running.", null, "Por favor, asegurate de que el servidor de Physical BITS está ejecutándose.", "Palun tee kindlaks, et Physical BITS server on püsti.", "Se till att Physical BITS-servern är igång."],
  ["Attempting to reconnect...", null, "Intentando volver a conectarse...", "Püüan uuesti ühenduda...", "Försöker återansluta..."],
  ["Options", null, "Opciones", "Seaded", "Alternativ"],
  ["User interface", null, "Interfaz de usuario", "Kasutajaliides", "Användargränssnitt"],
  ["Internationalization", null, "Internacionalización", "Keeled", "Språk"],
  ["Panels:", null, "Paneles:", "Paanid:", "Paneler:"],
  ["Inspector", null, "Inspector", "Inspektor", "Inspektör"],
  ["Output", null, "Salida", "Väljund", "Utdata"],
  ["Blocks", null, "Bloques", "Klotsid", "Klossar"],
  ["Serial Monitor", null, "Monitor Serial", "Jadapordi monitor", "Serieport monitor"],
  ["Code", null, "Código", "Kood", "Kod"],
  ["Debugger", null, "Depurador", "Siluja", null],
  ["Current language:", null, "Idioma actual:", "Keel:", "Valt språk:"],
  ["Choose pins", null, "Elegir pines", "Vali viigud", "Välj ben"],
  ["Choose globals", null, "Elegir variables globales", "Vali globaalsed muutujad", "Välj variabler"],
  ["Configure motors", null, "Configurar motores", "Seadista mootorid", "Konfigurera motorer"],
  ["Configure lcds", null, "Configurar lcds", null, null],
  ["Configure variables", null, "Configurar variables", "Seadista muutujaid", "Konfigurera variabler"],
  ["Motor name", null, "Nombre del motor", "Mootori nimi", "Motorns namn"],
  ["Enable pin", null, "Pin Enable", "Sisselülituse viik", "Påslagningsben"],
  ["Forward pin", null, "Pin Forward", "Viik ettepoole pööramiseks", "Ben för att rotera framåt"],
  ["Backward pin", null, "Pin Backward", "Viik tahapoole pööramiseks", "Ben för att rotera bakåt"],
  ["Lcd name", null, "Nombre del lcd", null, null],
  ["Address", null, null, null, null],
  ["Columns", null, null, null, null],
  ["Rows", null, null, null, null],
  ["Configure sonars", null, "Configurar sonares", "Seadista kajalood", "Konfigurera ekolod"],
  ["Sonar name", null, "Nombre del sonar", "Kajaloo nimi", "Ekolodets namn"],
  ["Trig pin", null, "Pin Trig", "Kõlari viik", "Högtalarens ben"],
  ["Echo pin", null, "Pin Echo", "Kaja (mikrofoni) viik", "Mikrofonens ben"],
  ["Max distance (cm)", null, "Distancia máxima (cm)", "Pikim kaugus (cm)", "Längsta avstånd (cm)"],
  ["This variable is being used by the program!", null, "¡Esta variable está siendo usada en el programa!", "Hetkel kasutatakse seda muutujat mujal programmis!", "Denna variabel används just nu i programmet!"],
  ["This motor is being used by the program!", null, "¡Este motor está siendo usado en el programa!", "Hetkel kasutatakse seda mootorit mujal programmis!", "Denna motor används just nu i programmet!"],
  ["This lcd is being used by the program!", null, "¡Este lcd está siendo usado en el programa!", null, null],
  ["This sonar is being used by the program!", null, "¡Este sonar está siendo usado en el programa!", "Hetkel kasutatakse seda kajaloodi mujal programmis!", "Detta ekolod används just nu i programmet!"],
  ["Display text in ALL-CAPS", null, "Mostrar todo el texto en MAYÚSCULAS", "Kuva kõik tekst TRÜKITÄHTEDES", "Visa all text som STORA BOKSTÄVER"],
  ["No available ports found", null, "No se encontraron puertos disponibles", "Ei leitud ühtki vaba porti", "Inga tillgängliga portar hittades"],
  ["Beware!", null, "¡Cuidado!", "Ettevaatust!", "Varning!"],
  ["You will lose all your unsaved changes. Are you sure?", null, "Vas a perder todos los cambios que no hayas guardado. ¿Estás seguro?", "Kõik salvestamata muudatused lähevad kaotsi. Oled sa ikka kindel?", "Du kommer förlora alla dina osparade ändringar. Är du säker?"],
  ["Save project", null, "Guardar proyecto", "Salvesta projekt", "Spara projekt"],
  ["File name:", null, "Nombre de archivo:", "Faili nimi:", "Filnamn:"],
  ["Choose port", null, "Elegir puerto", "Vali port", "Välj port"],
  ["Port name:", null, "Nombre del puerto:", "Pordi nimi:", "Portnamn:"],
  ["Accept", null, "Aceptar", "Nõustu", "Acceptera"],
  ["Cancel", null, "Cancelar", "Loobu", "Avbryt"],
  ["Controls", null, "Controles", "Kontrollid", "Kontroller"],
  ["General:", null, "General:", null, null],
  ["Program:", null, "Programa:", "Programmeerimisrežiim:", "Programmeringsläge:"],
  ["Blocks:", null, "Bloques:", null, null],
  ["Uzi syntax", "Code syntax instead of block text", "Código en lugar del texto de los bloques", "Uzi süntaks", null],
  ["Advanced user (enables all features)", null, "Usuario avanzado (habilita todas las características)", null, null],
  ["Text mode:", "Text:", "Texto:", "Teksti kuvamine:", "Textläge:"],
  ["Saving...", null, "Guardando...", "Salvestamine...", "Sparar..."],
  ["Saved!", null, "¡Guardado!", "Salvestatud!", "Sparad!"],
  ["Available memory", null, "Memoria disponible", null, null],
  ["Arduino", null, "Arduino", null, null],
  ["Program", null, "Programa", null, null],
  ["Stack", null, "Pila", null, null],
  ["Autosave", null, "Guardado automático", null, null],
  ["Plotter", null, "Graficador", null, null],
  ["Lists", null, "Listas", null, null],
  ["This list is being used by the program!", null, "¡Esta lista está siendo usada en el programa!", null, null],
  ["Configure joysticks", null, "Configurar joysticks", null, null],
  ["Configure lists", null, "Configurar listas", null, null],
  
  ["Pause", null, "Pausar", null, null],
  ["Continue", null, "Continuar", null, null],
  ["Over", null, null, null, null], // TODO(Richo): Spanish translation?
  ["Into", null, null, null, null], // TODO(Richo): Spanish translation?
  ["Out", null, null, null, null], // TODO(Richo): Spanish translation?
  ["Next", null, null, null, null], // TODO(Richo): Spanish translation?
  ["Call stack", null, "Pila de llamadas", null, null],
  ["Locals", null, "Variables locales", null, null],

  ["Welcome to the online DEMO of the Physical Bits environment!", null, "¡Bienvenido a la DEMO online de Physical Bits!", null, null],
  ["This DEMO is intended to showcase the editor and its capabilities. Therefore, its functionality is limited and it doesn't currently support connecting to a device in order to interactively program it.", null, "El objetivo de esta DEMO es exhibir el editor y sus capacidades. Por lo tanto, su funcionalidad es limitada y actualmente no soporta la conexión con un dispositivo para poder programarlo de forma interactiva.", null, null],
  ["If you want to experience the full benefits of this programming environment, please download an appropriate version for your OS here:", null, "Si te interesa experimentar todos los beneficios de este entorno de programación, te recomendamos descargar una versión apropiada para tu sistema operativo:", null, null],
  ["DOWNLOADS", null, "DESCARGAS", null, null],

  // Server
  ["Program size (bytes): %1", null, "Tamaño del programa (en bytes): %1", "Programmi suurus baitides: %1", "Programmstorlek i bytes: %1"],
  ["Compilation successful!", null, "¡Compilación exitosa!", "Kompileerimine õnnestus!", "Kompileringen lyckades!"],
  ["Compilation failed!", null, "¡Error de compilación!", null, null],
  ["Connecting on serial...", null, "Conectando por serie...", "Ühendatakse jadapordiga...", "Ansluter till serieporten..."],
  ["Connecting on socket...", null, "Conectando por socket...", "Ühendatakse kiibipesaga...", "Ansluter till serieuttag..."],
  ["Installed program successfully!", null, "¡Programa instalado correctamente!", "Programmi installimine õnnestus!", "Programmet installerad!"],
  ["Opening port: %1", null, "Abriendo puerto: %1", "Jadapordi avamine: %1", "Öppnar serieport: %1"],
  ["Opening port failed!", null, "¡La apertura del puerto falló!", "Jadapordi avamine nurjus!", "Serieporten kunde inte öppnas!"],
  ["Connection lost!", null, "¡Conexión perdida!", "Ühendus katkes!", "Anslutningen bröts!"],
  ["%1 detected. The program has been stopped.", null, "Se detectó %1. El programa ha sido detenido.", "%1 avastatud. Programm on peatatud.", "%1 hittades. Programmet har stoppats."],
  ["Uzi - Invalid response code: %1", null, "Uzi - Código de respuesta inválido: %1", "Uzi – Vigane kostekood: %1", "Uzi - ogiltig svarskod: %1"],
  ["Connection timeout!", null, "¡Expiró el tiempo de espera de la conexión!", "Ühendus aegus!", "Anslutningen avbröts!"],
  ['%1 detected on script "%2". The script has been stopped.', '%1 detected on script "%2". The script has been stopped.', 'Se detectó %1 en el script "%2". El script ha sido detenido.', '%1 avastatud skriptis "%2". Skript on peatatud.', '%1 hittades i skriptet %2. Skriptet har stoppats.'],
  ["Requesting connection...", null, "Solicitando conexión...", "Ühenduse taotlemine...", "Begär anslutning..."],
  ["Connection accepted!", null, "¡Conexión aceptada!", "Ühendus aktsepteeriti!", "Anslutningen accepterad!"],
  ["Connection rejected", null, "Conexión rechazada", "Ühendus lükati tagasi!", "Anslutningen avvisad!"],
  ["Connection timeout", null, "Expiró el tiempo de espera de la conexión", "Ühendus aegus", "Anslutnings-timeout"],

  // Blockly
  ["task %1 () { \n %2 }", "task named %1 \n %2", "tarea llamada %1 \n %2", "ülesanne nimega %1 \n %2", "jobb med namn %1 \n %2"],
  ["task %1 () %4 %2 / %3 { \n %5 }", "timer named %1 \n running %2 times per %3 \n initial state %4 \n %5", "temporizador llamado %1 \n ejecutándose %2 veces por %3 \n estado inicial %4 \n %5", "töö nimega %1 \n mis jookseb %2 korda iga %3 \n algseisuga %4 \n %5", "timer med namn %1 \n som tickar %2 gånger per %3 \n med initialt tillstånd %4 \n %5"],
  ["start %name ;", "start %name", "iniciar %name", "alusta %name", "starta %name"],
  ["pause %name ;", "pause %name", "pausar %name", "pausi %name", "paus %name"],
  ["stop %name ;", "stop %name", "detener %name", "peata %name", "stoppa %name"],
  ["resume %name ;", "resume %name", "continuar %name", "jätka %name", "återta %name"],
  ["%taskName () ;", "run %taskName", "ejecutar %taskName", "käivita %taskName", "kör %taskName"],
  ["yield ;", "wait 1 tick", "esperar 1 ciclo", null, null],
  ["toggle( %1 ) ;", "toggle pin %1", "alternar pin %1", "lülita ümber viigu %1 väärtus", "växla värdet på ben %1"],
  ["turn %1 ( %2 ) ;", "%1 pin %2", "%1 pin %2", "%1 viik %2", "%1 ben %2"],
  ["%1 ( %2 )", "is %1 pin %2 ?", "¿ está %1 el pin %2 ?", "on viik %1 %2", "är ben %1 %2"],
  ["read( %1 )", "read pin %1", "leer pin %1", "loe viiku %1", "läs värdet på ben %1"],
  ["write( %1 , %2 );", "write pin %1 value %2 \n", "escribir en el pin %1 el valor %2 \n", "määra viigu %1 väärtuseks %2 \n", "sätt värde %2 på ben %1 \n"],
  ["setPinMode( %1 , %2 );", "set pin %1 mode to %2", "configurar pin %1 como %2", "vali viigu %1 režiimiks %2", "sätt ben %1 i läge %2"],
  ["%pin", null, "%pin", "viik %pin", null],
  ["pin ( %1 )", "pin %1", "pin %1", "pin %1", "pin %1"],
  ["setServoDegrees( %1 , %2 ) ;", "move servo on pin %1 degrees %2", "mover servo en pin %1 grados %2", "pööra servot viigus %1 %2 kraadini", "vrid servot i ben %1 till %2 grader"],
  ["getServoDegrees( %1 ) ;", "get degrees of servo on pin %1", "obtener grados de servo en el pin %1", "servo asend viigus %1", "läs servots lutning i ben %1"],
  ["%name . %direction (speed: %speed ) ;", "move %name %direction at speed %speed", "mover %name hacia %direction a velocidad %speed", "liiguta mootorit %name suunas %direction kiirusega %speed", "rotera motorn %name i riktning %direction med hastigheten %speed"],
  ["%name . brake() ;", "stop %name", "detener %name", "peata mootor %name", "stanna %name"],
  ["%name . setSpeed (speed: %speed ) ;", "set %name speed to %speed", "mover %name a velocidad %speed", "määra mootori %name kiiruseks %speed", "sätt motor %name hastighet till %speed"],
  ["%name .getSpeed( )", "get %name speed", "obtener la velocidad de %name", "mootori %name kiirus", "läs %name hastighet"],
  ["%name . printNumber ( %number ) ;", "print number: %number in %name", null, null, null],
  ["%name . printString ( %string ) ;", "print string: %string in %name", null, null, null],
  ["%name . %unit ()", "read distance from %name in %unit", "leer distancia de %name en %unit", "mõõda kaugus kajaloost %name ühikutes %unit", "läs avståndet från ekolodet %name i %unit"],
  ["buttons. %state ( %pin )", "is button %state on pin %pin ?", "¿ está %state el botón en el pin %pin ?", "kas lüliti on %state viigus %pin", "är knapp %state på ben %pin"],
  ["buttons. %action ( %pin ) ;", "wait for button %action on pin %pin", "esperar hasta que %action el botón en el pin %pin", "oota lüliti %action viigus %pin", "vänta på knapp %action på ben %pin"],
  ["buttons . %action ( %pin, %time %timeUnit );", "wait button %action on pin %pin for %time %timeUnit", "esperar que %action durante %time %timeUnit \n el botón en %pin", "oota lüliti %action viigus %pin %time %timeUnit", "vänta på knapp %action ansluten till ben %pin för %time %timeUnit"],
  ["buttons . millisecondsHolding ( %pin )", "elapsed milliseconds while pressing %pin", "milisegundos transcurridos presionando el pin %pin", "möödunud aeg millisekundites %pin vajutuse ajal", "förfluten tid i millisekunder medan %pin nedtryckt"],

  ["Configure joysticks...", null, "Configurar joysticks...", null, null],
  ["%name .x", "read joystick x position from %name", "leer posición en x de %name", "juhtkangi %name x-telje asend", "läs %name x-position"],
  ["%name .y", "read joystick y position from %name", "leer posición en y de %name", "juhtkangi %name y-telje asend", "läs %name y-position"],
  ["%name .getAngle()", "read angle from %name", "leer ángulo de %name", "juhtkangi %name x-telje nurk kraadides", "läs %name x-position i grader"],
  ["%name .getMagnitude()", "read magnitude from %name", "leer magnitud de %name", "juhtkangi %name y-telje nurk kraadides", "läs %name y-position i grader"],
  ["Joystick name", null, "Nombre del joystick", null, null],
  ["X pin", null, "Pin X", null, null],
  ["Y pin", null, "Pin Y", null, null],
  ["This joystick is being used by the program!", null, "¡Este joystick está siendo usado en el programa!", null, null],

  ["startTone( %tone , %pinNumber ) ;", "play tone %tone on pin %pinNumber", "reproducir tono %tone en el pin %pinNumber", "mängi tooni %tone viigus %pinNumber \n", "spela ton %tone på ben %pinNumber \n"],
  ["playTone( %tone , %pinNumber , %time %unit ) ;", "play tone %tone on pin %pinNumber for %time %unit", "reproducir tono %tone en el pin %pinNumber durante %time %unit", "mängi tooni %tone viigus %pinNumber %time %unit", "spela ton %tone på ben %pinNumber under %time %unit"],
  ["startTone( %note , %pinNumber ) ;", "play note %note on pin %pinNumber", "reproducir nota %note en el pin %pinNumber", "mängi nooti %note viigus %pinNumber \n", "spela not %note på ben %pinNumber"],
  ["playTone( %note , %pinNumber , %time %unit ) ;", "play note %note on pin %pinNumber for %time %unit", "reproducir nota %note en el pin %pinNumber durante %time %unit", "mängi nooti %note viigus %pinNumber %time %unit", "spela not %note på ben %pinNumber under %time %unit"],
  ["stopTone( %pinNumber ) ;", "stop tone on pin %pinNumber", "detener tono en el pin %pinNumber", "peata toon viigus %pinNumber", "sluta tonen på ben %pinNumber"],
  ["stopToneAndWait( %pinNumber , %time %unit ) ;", "stop tone on pin %pinNumber and wait %time %unit", "detener tono en el pin %pinNumber y esperar %time %unit", "peata toon viigus %pinNumber ja oota %time %unit", "sluta tonen på ben %pinNumber och vänta %time %unit"],

  ["%boolean", null, "%boolean", null, null],
  ["bool ( %1 )", "bool %1", "bool %1", "bool %1", "bool %1"],
  ["if %1 { \n %2 }", "if %1 then %2", "si %1 entonces %2", "juhul kui kehtib %1 siis %2", "om %1 isåfall %2"],
  ["if %1 { \n %2 } else { \n %3 }", "if %1 then %2 else %3", "si %1 entonces %2 si no %3", "juhul kui kehtib %1 siis %2 muidu %3", "om %1 isåfall %2 annars %3"],
  ["forever { \n %1 }", "repeat forever \n %1", "repetir por siempre \n %1", "korda igavesti \n %1", "upprepa för evigt \n %1"],
  ["%negate %condition { \n %statements }", "repeat %negate %condition \n %statements", "repetir %negate %condition \n %statements", "korda %negate %condition \n %statements", "upprepa %negate %condition \n %statements"],
  ["repeat %times { \n %statements }", "repeat %times times \n %statements", "repetir %times veces \n %statements", "korda %times korda \n %statements", "upprepa %times gånger \n %statements"],
  ["for %1 = %2 to %3 by %4 { \n %5 }", "count with %1 from %2 to %3 by %4 \n %5", "contar con %1 desde %2 hasta %3 por %4 \n %5", "loenda muutujat %1 alates %2 kuni %3 sammuga %4 %5", "räkna med variabeln %1 från %2 till %3 med steg av %4 \n %5"],
  ["%delay ( %time ) ;", "wait %time %delay", "esperar %time %delay", "oota %time %delay", "vänta %time %delay"],
  ["%negate %condition ;", "wait %negate %condition", "esperar %negate %condition", "oota %negate %condition", "vänta %negate %condition"],
  ["%timeUnit", "elapsed %timeUnit", "%timeUnit transcurridos", "möödunud %timeUnit algusest", "förfluten tid sen start i %timeUnit"],
  ["( %left %logical_compare_op %right )", "%left %logical_compare_op %right", "%left %logical_compare_op %right", "%left %logical_compare_op %right", "%left %logical_compare_op %right"],
  ["( %left %logical_operation_op %right )", "%left %logical_operation_op %right", "%left %logical_operation_op %right", "%left %logical_operation_op %right", "%left %logical_operation_op %right"],
  ["! %1", "not %1", "no %1", "mitte %1", "inte %1"],

  ["%number", null, "%number", "arv %number", null],
  ["number ( %1 )", "number %1", "número %1", "number %1", "number %1"],
  ["%numProp ( %value )", "%value %numProp ?", "¿ %value %numProp ?", "%value %numProp", "%value %numProp"],
  ["isDivisibleBy( %1 , %2 )", "%1 is divisible by %2 ?", "¿ %1 es divisible por %2 ?", "kas %1 jaguneb arvuga %2 \n", "är %1 delbart med %2 \n"],
  ["%operation %number \n", null, null, null, null],
  ["%trigOperation %number \n", null, null, null, null],
  ["%constant", null, null, null, null],
  ["( %left %arithmeticOperator %right )", "%left %arithmeticOperator %right", "%left %arithmeticOperator %right", "%left %arithmeticOperator %right", "%left %arithmeticOperator %right"],
  ["%roundingOperation %number \n", null, null, null, null],
  ["%1 % %2 \n", "remainder of %1 ÷ %2 \n", "resto de %1 ÷ %2 \n", "%1 ÷ %2 jääk", "resten av %1 ÷ %2 \n"],
  ["constrain ( %1 , %2 , %3 )", "constrain %1 low %2 high %3", "mantener %1 entre %2 y %3", "piira %1 olema %2 ja %3 vahel", "begränsa %1 att vara mellan %2 och %3"],
  ["isBetween ( value: %1 , min: %2 , max: %3 )", "is %1 between %2 and %3 ?", "¿ está %1 entre %2 y %3 ?", "kas %1 on %2 ja %3 vahel", "är värdet %1 mellan %2 och %3"],

  ["randomInt( %1, %2 )", "random integer between %1 and %2", "número al azar entre %1 y %2", "suvaline täisarv %1 ja %2 vahel", "slumpmässigt heltal mellan %1 och %2"],
  ["random()", "random fraction", "fracción al azar", "suvaline murdarv 0.0 ja 1.0 vahel", "slumpmässig bråkdel mellan 0.0 och 1.0"],

  ["%name", null, null, "muutuja %name", null],
  ["var %name = %value ;", "declare local variable %name with value %value", "declarar variable local %name con valor %value", "loo muutuja %name väärtusega %value", "skapa variabeln %name med värdet %value"],
  ["%name = %value ;", "set %name to %value", "establecer %name a %value", "määra %name väärtuseks %value \n", "sätt värdet på %name till %value"],
  ["%name += %value ;", "increment %name by %value", "incrementar %name por %value", "kasvata %name suurusega %value", "lägg till %value på %name"],

  ["proc %name () { \n %stmts }", "procedure named %name %stmts", "procedimiento llamado %name %stmts", "protseduur nimega %name %stmts", "procedur med namn %name %stmts"],
  ["proc %name ( %arg0 ) { \n %stmts }", "procedure named %name with argument %arg0 %stmts", "procedimiento llamado %name con argumento %arg0 %stmts", "protseduur nimega %name argumendiga %arg0 %stmts", "procedur med namn %name och argumentet %arg0 %stmts"],
  ["proc %name ( %arg0 , %arg1 ) { \n %stmts }", "procedure named %name with arguments %arg0 %arg1 %stmts", "procedimiento llamado %name con argumentos %arg0 %arg1 %stmts", "protseduur nimega %name argumentidega %arg0 %arg1 %stmts", "procedur med namn %name och argumenten %arg0 %arg1 %stmts"],
  ["proc %name ( %arg0 , %arg1 , %arg2 ) { \n %stmts }", "procedure named %name with arguments %arg0 %arg1 %arg2 %stmts", "procedimiento llamado %name con argumentos %arg0 %arg1 %arg2 %stmts", "protseduur nimega %name argumentidega %arg0 %arg1 %arg2 %stmts", "procedur med namn %name och argumenten %arg0 %arg1 %arg2 %stmts"],
  ["proc %name ( %arg0 , %arg1 , %arg2 , %arg3 ) { \n %stmts }", "procedure named %name with arguments %arg0 %arg1 %arg2 %arg3 %stmts", "procedimiento llamado %name con argumentos %arg0 %arg1 %arg2 %arg3 %stmts", "protseduur nimega %name argumentidega %arg0 %arg1 %arg2 %arg3 %stmts", "procedur med namn %name och argumenten %arg0 %arg1 %arg2 %arg3 %stmts"],
  ["proc %name ( %arg0 , %arg1 , %arg2 , %arg3 , %arg4 ) { \n %stmts }", "procedure named %name with arguments %arg0 %arg1 %arg2 %arg3 %arg4 %stmts", "procedimiento llamado %name con argumentos %arg0 %arg1 %arg2 %arg3 %arg4 %stmts", "protseduur nimega %name argumentidega %arg0 %arg1 %arg2 %arg3 %arg4 %stmts", "procedur med namn %name och argumenten %arg0 %arg1 %arg2 %arg3 %arg4 %stmts"],
  ["exit ;", "exit", "salir", "lõpeta", "sluta"],
  ["%procName () ;", "execute %procName", "ejecutar %procName", "jooksuta %procName", "kör %procName"],
  ["%procName ( %arg0 ) ;", "execute %procName %arg0", "ejecutar %procName %arg0", "jooksuta toimingut %procName argumendiga %arg0", "kör %procName %arg0"],
  ["%procName ( %arg0 , %arg1 ) ;", "execute %procName %arg0 %arg1", "ejecutar %procName %arg0 %arg1", "jooksuta toimingut %procName argumentidega %arg0 %arg1", "kör %procName %arg0 %arg1"],
  ["%procName ( %arg0 , %arg1 , %arg2 ) ;", "execute %procName %arg0 %arg1 %arg2", "ejecutar %procName %arg0 %arg1 %arg2", "jooksuta toimingut %procName argumentidega %arg0 %arg1 %arg2", "kör %procName %arg0 %arg1 %arg2"],
  ["%procName ( %arg0 , %arg1 , %arg2 , %arg3 ) ;", "execute %procName %arg0 %arg1 %arg2 %arg3", "ejecutar %procName %arg0 %arg1 %arg2 %arg3", "jooksuta toimingut %procName argumentidega %arg0 %arg1 %arg2 %arg3", "kör %procName %arg0 %arg1 %arg2 %arg3"],
  ["%procName ( %arg0 , %arg1 , %arg2 , %arg3 , %arg4 ) ;", "execute %procName %arg0 %arg1 %arg2 %arg3 %arg4", "ejecutar %procName %arg0 %arg1 %arg2 %arg3 %arg4", "jooksuta toimingut %procName argumentidega %arg0 %arg1 %arg2 %arg3 %arg4", "kör %procName %arg0 %arg1 %arg2 %arg3 %arg4"],

  ["func %name () { \n %stmts }", "function named %name %stmts", "función llamada %name %stmts", "funktsioon nimega %name %stmts", "funktion med namn %name %stmts"],
  ["func %name ( %arg0 ) { \n %stmts }", "function named %name with argument %arg0 %stmts", "función llamada %name con argumento %arg0 %stmts", "funktsioon nimega %name argumentidega %arg0 %stmts", "funktion med namn %name och argumentet %arg0 %stmts"],
  ["func %name ( %arg0 , %arg1 ) { \n %stmts }", "function named %name with arguments %arg0 %arg1 %stmts", "función llamada %name con argumentos %arg0 %arg1 %stmts", "funktsioon nimega %name argumentidega %arg0 %arg1 %stmts", "funktion med namn %name och argument %arg0 %arg1 %stmts"],
  ["func %name ( %arg0 , %arg1 , %arg2 ) { \n %stmts }", "function named %name with arguments %arg0 %arg1 %arg2 %stmts", "función llamada %name con argumentos %arg0 %arg1 %arg2 %stmts", "funktsioon nimega %name argumentidega %arg0 %arg1 %arg2 %stmts", "funktion med namn %name och argument %arg0 %arg1 %arg2 %stmts"],
  ["func %name ( %arg0 , %arg1 , %arg2 , %arg3 ) { \n %stmts }", "function named %name with arguments %arg0 %arg1 %arg2 %arg3 %stmts", "función llamada %name con argumentos %arg0 %arg1 %arg2 %arg3 %stmts", "funktsioon nimega %name argumentidega %arg0 %arg1 %arg2 %arg3 %stmts", "funktion med namn %name och argument %arg0 %arg1 %arg2 %arg3 %stmts"],
  ["func %name ( %arg0 , %arg1 , %arg2 , %arg3 , %arg4 ) { \n %stmts }", "function named %name with arguments %arg0 %arg1 %arg2 %arg3 %arg4 %stmts", "función llamada %name con argumentos %arg0 %arg1 %arg2 %arg3 %arg4 %stmts", "funktsioon nimega %name argumentidega %arg0 %arg1 %arg2 %arg3 %arg4 %stmts", "funktion med namn %name och argument %arg0 %arg1 %arg2 %arg3 %arg4 %stmts"],
  ["return %value ;", "return %value", "devolver %value", "tagasta %value", "svara %value"],
  ["%funcName ()", "evaluate function %funcName", "evaluar %funcName", "arvuta %funcName", "beräkna %funcName"],
  ["%funcName ( %arg0 )", "evaluate function %funcName %arg0", "evaluar %funcName %arg0", "arvuta %funcName argumendiga %arg0", "beräkna %funcName med argumentet %arg0"],
  ["%funcName ( %arg0 , %arg1 )", "evaluate function %funcName %arg0 %arg1", "evaluar %funcName %arg0 %arg1", "arvuta %funcName argumentidega %arg0 %arg1", "beräkna %funcName med argumenten %arg0 %arg1"],
  ["%funcName ( %arg0 , %arg1 , %arg2 )", "evaluate function %funcName %arg0 %arg1 %arg2", "evaluar %funcName %arg0 %arg1 %arg2", "arvuta %funcName argumentidega %arg0 %arg1 %arg2", "beräkna %funcName med argumenten %arg0 %arg1 %arg2"],
  ["%funcName ( %arg0 , %arg1 , %arg2 , %arg3 )", "evaluate function %funcName %arg0 %arg1 %arg2 %arg3", "evaluar %funcName %arg0 %arg1 %arg2 %arg3", "arvuta %funcName argumentidega %arg0 %arg1 %arg2 %arg3", "beräkna %funcName med argumenten %arg0 %arg1 %arg2 %arg3"],
  ["%funcName ( %arg0 , %arg1 , %arg2 , %arg3 , %arg4 )", "evaluate function %funcName %arg0 %arg1 %arg2 %arg3 %arg4", "evaluar %funcName %arg0 %arg1 %arg2 %arg3 %arg4", "arvuta %funcName argumentidega %arg0 %arg1 %arg2 %arg3 %arg4", "beräkna %funcName med argumenten %arg0 %arg1 %arg2 %arg3 %arg4"],

  ["isEven", "is even", "es par", "on paarisarv", "är ett jämnt tal"],
  ["isOdd", "is odd", "es impar", "on paaritu arv", "är ett ojämnt tal"],
  ["isPrime", "is prime", "es primo", "on algarv", "är ett primtal"],
  ["isWhole", "is whole", "es entero", "on täisarv", "är ett heltal"],
  ["isPositive", "is positive", "es positivo", "on positiivne", "är positivt"],
  ["isNegative", "is negative", "es negativo", "on negatiivne", "är negativt"],
  ["/", "÷", "÷", "÷", "÷"],
  ["*", "×", "×", "×", "×"],
  ["-", null, null, null, null],
  ["+", null, null, null, null],
  ["**", "^", "^", "^", "^"],
  ["Tasks", null, "Tareas", "Tööd", "Jobb"],
  ["Pins", null, "Pines", "Viigud", "Ben"],
  ["Motors", null, "Motores", "Mootorid", "Motorer"],
  ["Servo", null, "Servo", null, "Servon"],
  ["DC motor", null, "Motor CC", null, null],
  ["Sensors", null, "Sensores", "Andurid", "Sensorer"],
  ["Sonar", null, "Sonar", "Kajalood", "Ekolod"],
  ["Joystick", null, "Joystick", "Juhtkang", null],
  ["Control", null, "Control", "Kontroll", "Kontroll"],
  ["Math", null, "Matemática", "Aritmeetika", "Matematik"],
  ["Logic", null, "Lógica", null, null],
  ["Variables", null, "Variables", "Muutujad", "Variabler"],
  ["Functions", null, "Funciones", "Arvutused", "Funktioner"],
  ["Comments", null, "Comentarios", "Kommentaarid", "Kommentarer"],
  ["Procedures", null, "Procedimientos", "Toimingud", "Procedurer"],
  ["Configure DC motors...", null, "Configurar motores CC...", "Seadista mootorid...", "Konfigurera motorer..."],
  ["Configure sonars...", null, "Configurar sonares...", "Seadista kajalood...", "Konfigurera ekolod..."],
  ["square root", null, "raíz cuadrada", "ruutjuur", "roten ur"],
  ["absolute", null, "valor absoluto", "absoluutväärtus", "absolutvärde"],
  ["==", "=", "=", "=", "="],
  ["!=", "≠", "≠", "≠", "≠"],
  ["<", null, null, null, null],
  ["<=", "≤", "≤", "≤", "≤"],
  [">", null, null, null, null],
  [">=", "≥", "≥", "≥", "≥"],
  ["3.141592653589793", "π", "π", "π (pii)", "π"],
  ["2.718281828459045", "ℯ", "ℯ", "ℯ", "ℯ"],
  ["1.61803398875", "φ", "φ", "φ (kuldlõige)", "φ"],
  ["1.4142135623730951", "√2", "√2", "√2 (ruutjuur kahest)", "√2"],
  ["0.7071067811865476", "√½", "√½", "√½ (ruutjuur poolest)", "√½"],
  ["Infinity", "∞", "∞", "∞ (lõpmatus)", "∞"],
  ["ln", null, null, null, null],
  ["log10", null, null, null, null],
  ["e^", null, null, null, null],
  ["10^", null, null, null, null],
  ["sin", null, "seno", null, null],
  ["cos", null, "coseno", null, null],
  ["tan", null, "tangente", null, null],
  ["asin", null, "arcoseno", null, null],
  ["acos", null, "arcocoseno", null, null],
  ["atan", null, "arcotangente", null, null],
  ["round", null, "redondear", "ümardamine", "avrunda"],
  ["ceil", "round up", "redondear hacia arriba", "ümarda üles", "avrunda uppåt"],
  ["floor", "round down", "redondear hacia abajo", "ümarda alla", "avrunda nedåt"],
  ["Configure variables...", null, "Configurar variables...", "Seadista muutujaid...", "Konfigurera variabler..."],
  ["Variable name", null, "Nombre de variable", "Muutuja nimi", "Variabelns namn"],
  ["Initial value (if global)", null, "Valor inicial (en caso de global)", "Algväärtus (kui on globaalne)", "Initialvärde (om globalt)"],
  ["Sound", null, "Sonido", "Heli", "Ljud"],
  ["Buttons", null, "Botones", "Lülitid", "Knappar"],
  ["isPressed", "pressed", "presionado", "vajutatud", "nedtryckt"],
  ["isReleased", "released", "suelto", "vabastatud", "släppt"],
  ["waitForPress", "press", "presione", "vajutamist", "tryck"],
  ["waitForRelease", "press and release", "presione y suelte", "vajutamist ja vabastamist", "tryck och släpp"],
  ["INPUT", null, "ENTRADA", "SISEND", "INGÅNG"],
  ["OUTPUT", null, "SALIDA", "VÄLJUND", "UTGÅNG"],
  ["INPUT PULLUP", null, "ENTRADA PULLUP", "PULLUP-SISEND", "PULLUP-INGÅNG"],

  ["On", "turn on", "encender", "lülita sisse", "slå på"],
  ["Off", "turn off", "apagar", "lülita välja", "slå av"],
  ["isOn", "on", "encendido", "sees", "på"],
  ["isOff", "off", "apagado", "väljas", "av"],
  ["milliseconds", null, "milisegundos", "millisekundit", "millisekunder"],
  ["delayMs", "milliseconds", "milisegundos", "millisekundit", "millisekunder"],
  ["seconds", null, "segundos", "sekundit", "sekunder"],
  ["delayS", "seconds", "segundos", "sekundit", "sekunder"],
  ["minutes", null, "minutos", "minutit", "minuter"],
  ["delayM", "minutes", "minutos", "minutit", "minuter"],
  ["s", "second", "segundo", "sekund", "sekund"],
  ["m", "minute", "minute", "minut", "minut"],
  ["h", "hour", "hour", "tund", "timme"],
  ["running", "started", "iniciado", "käivitatud", "startad"],
  ["stopped", null, "detenido", "peatatud", "stoppad"],
  ["forward", null, "adelante", "edasi", "framåt"],
  ["backward", null, "atrás", "tagasi", "bakåt"],
  ["distance_mm", "mm", "mm", "millimeeter", "mm"],
  ["distance_cm", "cm", "cm", "sentimeeter", "cm"],
  ["distance_m", "m", "m", "meeter", "m"],
  ["while", null, "mientras que", "nii kaua kui kehtib", "medan"],
  ["until", null, "hasta que", "nii kaua kuni kehtib", "tills"],
  ["true", null, "verdadero", "tõene", "sann"],
  ["false", null, "falso", "väär", "falsk"],
  ["&&", "and", "y", "ja", "och"],
  ["||", "or", "o", "või", "eller"],

  ["Configure lists...", null, "Configurar listas...", null, null],
  ["List name", null, "Nombre de la lista", null, null],
  ["Capacity", null, "Capacidad", null, null],
  ["%name . get ( %index )", "get value of %name at index %index", "valor de %name en posición %index", null, null],
  ["%name . set ( %index , %value ) ;", "set value of %name at %index to value %value", "establecer %name en posición %index al valor %value", null, null],
  ["%name . push ( %value ) ;", "append to %name %value", "agregar a %name %value", null, null],
  ["%name . pop ( ) ;", "remove last from %name", "eliminar último de %name", null, null],
  ["%name . count ( )", "get count of %name", "cantidad de elementos en %name", null, null],
  ["%name . size ( )", "get capacity of %name", "capacidad máxima de %name", null, null],
  ["%name . clear ( ) ;", "clear %name", "limpiar %name", null, null],
  ["%name . get_random ( )", "get random value from %name", "valor al azar de %name", null, null],
  ["%name . max ( )", "get max value from %name", "valor máximo de %name", null, null],
  ["%name . min ( )", "get min value from %name", "valor mínimo de %name", null, null],
  ["%name . sum ( )", "get sum from %name", "sumatoria de %name", null, null],
  ["%name . avg ( )", "get average from %name", "promedio de %name", null, null],
  
  ["'%text'", "%text", "%text", "%text", "%text"],

  ["Toggle Breakpoint", null, "Agregar/Quitar punto de interrupción", null, null],
  ["BREAKPOINT ON LINE: ", null, "PUNTO DE INTERRUPCIÓN EN LÍNEA: ", null, null],
];
