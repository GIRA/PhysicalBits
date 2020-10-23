
var TRANSLATIONS = [
  // spec
  ["label", "en", "es", "et", "se"],

  ["New...", "New...", "Nuevo...", "Uus...", "Ny..."],
  ["Open...", "Open...", "Abrir...", "Ava...", "Öppna..."],
  ["Save", "Save", "Guardar", "Salvesta", "Spara"],
  ["Save as...", "Save as...", "Guardar como...", "Salvesta kui...", "Spara som..."],
  ["Download...", "Download...", "Descargar...", "Laadi...", "Ladda..."],
  ["Automatic", "Automatic", "Automático", "Automaatne", "Automatisk"],
  ["Other...", "Other...", "Otro...", "Muu...", "Annan..."],
  ["Connect", "Connect", "Conectar", "Ühenda", "Anslut"],
  ["Disconnect", "Disconnect", "Desconectar", "Ühenda lahti", "Koppla ifrån"],
  ["Verify", "Verify", "Verificar", "Kontrolli", "Kontrollera"],
  ["Run", "Run", "Ejecutar", "Jooksuta", "Kör"],
  ["Install", "Install", "Instalar", "Paigalda", "Installera"],
  ["Debug", "Debug", "Depurar", "Silu", "Debug"],
  ["Interactive mode", "Interactive mode", "Modo interactivo", "Interaktiivne", "Interaktiv"],
  ["Options...", "Options...", "Opciones...", "Seaded...", "Inställningar..."],
  ["Pins", "Pins", "Pines", "Viigud", "Ben"],
  ["* no values reporting *", "* no values reporting *", "* no hay datos *", "* väärtusi ei edastata *", "* ingen värden rapporteras *"],
  ["Globals", "Globals", "Variables globales", "Globaalsed muutujad", "Globala variabler"],
  ["ERROR: Broken layout", "ERROR: Broken layout", "ERROR: Diseño arruinado", "VIGA: Katkine asetus", "FEL: bruten layout"],
  ["It seems you've broken the page layout.", "It seems you've broken the page layout.", "Parece que se rompió el diseño de la aplicación.", "Paistab, et sa oled asetuse lõhkunud.", "Det verkar som något gått fel med layouten"],
  ["But don't worry! Click the button below to restore it to its former glory.", "But don't worry! Click the button below to restore it to its former glory.", "¡Pero no te preocupes! Hacé clic en el botón siguiente para restaurarlo.", "Aga ära pabista! Vajuta nupule, et taastada õige seis.", "Men oroa dig inte! Klicka på knappen nedan för att återställa den till sin tidigare härlighet."],
  ["Restore default layout", "Restore default layout", "Restaurar diseño", "Taasta vaikimisi asetus", "Återställ layout"],
  ["ERROR: Server not found", "ERROR: Server not found", "ERROR: Servidor no encontrado", "VIGA: Serverit ei leitud", "FEL: Servern kunde inte nås"],
  ["Please make sure the Physical BITS server is up and running.", "Please make sure the Physical BITS server is up and running.", "Por favor, asegurate de que el servidor de Physical BITS está ejecutándose.", "Palun tee kindlaks, et Physical BITS server on püsti.", "Se till att Physical BITS-servern är igång."],
  ["Attempting to reconnect...", "Attempting to reconnect...", "Intentando volver a conectarse...", "Püüan uuesti ühenduda...", "Försöker återansluta..."],
  ["Options", "Options", "Opciones", "Seaded", "Alternativ"],
  ["User interface", "User interface", "Interfaz de usuario", "Kasutajaliides", "Användargränssnitt"],
  ["Internationalization", "Internationalization", "Internacionalización", "Keeled", "Språk"],
  ["Panels:", "Panels:", "Paneles:", "Paanid:", "Paneler:"],
  ["Inspector", "Inspector", "Inspector", "Inspektor", "Inspektör"],
  ["Output", "Output", "Salida", "Väljund", "Utdata"],
  ["Blocks", "Blocks", "Bloques", "Klotsid", "Klossar"],
  ["Serial Monitor", "Serial Monitor", "Monitor Serial", "Jadapordi monitor", "Serieport monitor"],
  ["Code", "Code", "Código", "Kood", "Kod"],
  ["Debugger", "Debugger", "Depurador", "Siluja", "Debugger"],
  ["Current language:", "Current language:", "Idioma actual:", "Keel:", "Valt språk:"],
  ["Choose pins", "Choose pins", "Elegir pines", "Vali viigud", "Välj ben"],
  ["Choose globals", "Choose globals", "Elegir variables globales", "Vali globaalsed muutujad", "Välj variabler"],
  ["Configure motors", "Configure motors", "Configurar motores", "Seadista mootorid", "Konfigurera motorer"],
  ["Motor name", "Motor name", "Nombre del motor", "Mootori nimi", "Motorns namn"],
  ["Enable pin", "Enable pin", "Pin Enable", "Sisselülituse viik", "Påslagningsben"],
  ["Forward pin", "Forward pin", "Pin Forward", "Viik ettepoole pööramiseks", "Ben för att rotera framåt"],
  ["Backward pin", "Backward pin", "Pin Backward", "Viik tahapoole pööramiseks", "Ben för att rotera bakåt"],
  ["Configure sonars", "Configure sonars", "Configurar sonares", "Seadista kajalood", "Konfigurera ekolod"],
  ["Sonar name", "Sonar name", "Nombre del sonar", "Kajaloo nimi", "Ekolodets namn"],
  ["Trig pin", "Trig pin", "Pin Trig", "Kõlari viik", "Högtalarens ben"],
  ["Echo pin", "Echo pin", "Pin Echo", "Kaja (mikrofoni) viik", "Mikrofonens ben"],
  ["Max distance (cm)", "Max distance (cm)", "Distancia máxima (cm)", "Pikim kaugus (cm)", "Längsta avstånd (cm)"],
  ["This variable is being used by the program!", "This variable is being used by the program!", "¡Esta variable está siendo usada en el programa!", "Hetkel kasutatakse seda muutujat mujal programmis!", "Denna variabel används just nu i programmet!"],
  ["This motor is being used by the program!", "This motor is being used by the program!", "¡Este motor está siendo usado en el programa!", "Hetkel kasutatakse seda mootorit mujal programmis!", "Denna motor används just nu i programmet!"],
  ["This sonar is being used by the program!", "This sonar is being used by the program!", "¡Este sonar está siendo usado en el programa!", "Hetkel kasutatakse seda kajaloodi mujal programmis!", "Detta ekolod används just nu i programmet!"],
  ["Display text in ALL-CAPS", "Display text in ALL-CAPS", "Mostrar todo el texto en MAYÚSCULAS", "Kuva kõik tekst TRÜKITÄHTEDES", "Visa all text som STORA BOKSTÄVER"],
  ["No available ports found", "No available ports found", "No se encontraron puertos disponibles", "Ei leitud ühtki vaba porti", "Inga tillgängliga portar hittades"],
  ["Beware!", "Beware!", "¡Cuidado!", "Ettevaatust!", "Varning!"],
  ["You will lose all your unsaved changes. Are you sure?", "You will lose all your unsaved changes. Are you sure?", "Vas a perder todos los cambios que no hayas guardado. ¿Estás seguro?", "Kõik salvestamata muudatused lähevad kaotsi. Oled sa ikka kindel?", "Du kommer förlora alla dina osparade ändringar. Är du säker?"],
  ["Save project", "Save project", "Guardar proyecto", "Salvesta projekt", "Spara projekt"],
  ["File name:", "File name:", "Nombre de archivo:", "Faili nimi:", "Filnamn:"],
  ["Choose port", "Choose port", "Elegir puerto", "Vali port", "Välj port"],
  ["Port name:", "Port name:", "Nombre del puerto:", "Pordi nimi:", "Portnamn:"],
  ["Accept", "Accept", "Aceptar", "Nõustu", "Acceptera"],
  ["Cancel", "Cancel", "Cancelar", "Loobu", "Avbryt"],
  ["Controls", "Controls", "Controles", "Kontrollid", "Kontroller"],
  ["General", "General", "General", "General", "General"],
  ["Program:", "Program:", "Programa:", "Programmeerimisrežiim:", "Programmeringsläge:"],
  ["Blocks:", "Blocks:", "Bloques:", "Blocks:", "Blocks:"],
  ["Uzi syntax", "Code syntax instead of block text", "Código en lugar del texto de los bloques", "Uzi süntaks", "Uzi syntax"],
  ["Text mode:", "Text:", "Texto:", "Teksti kuvamine:", "Textläge:"],
  ["Saving...", "Saving...", "Guardando...", "Salvestamine...", "Sparar..."],
  ["Saved!", "Saved!", "¡Guardado!", "Salvestatud!", "Sparad!"],

  // Server
  ["Program size (bytes): %1", "Program size (bytes): %1", "Tamaño del programa (en bytes): %1", "Programmi suurus baitides: %1", "Programmstorlek i bytes: %1"],
  ["Compilation successful!", "Compilation successful!", "¡Compilación exitosa!", "Kompileerimine õnnestus!", "Kompileringen lyckades!"],
  ["Connecting on serial...", "Connecting on serial...", "Conectando por serie...", "Ühendatakse jadapordiga...", "Ansluter till serieporten..."],
  ["Connecting on socket...", "Connecting on socket...", "Conectando por socket...", "Ühendatakse kiibipesaga...", "Ansluter till serieuttag..."],
  ["Installed program successfully!", "Installed program successfully!", "¡Programa instalado correctamente!", "Programmi installimine õnnestus!", "Programmet installerad!"],
  ["Opening port: %1", "Opening port: %1", "Abriendo puerto: %1", "Jadapordi avamine: %1", "Öppnar serieport: %1"],
  ["Opening port failed!", "Opening port failed!", "¡La apertura del puerto falló!", "Jadapordi avamine nurjus!", "Serieporten kunde inte öppnas!"],
  ["Connection lost!", "Connection lost!", "¡Conexión perdida!", "Ühendus katkes!", "Anslutningen bröts!"],
  ["%1 detected. The program has been stopped.", "%1 detected. The program has been stopped.", "Se detectó %1. El programa ha sido detenido.", "%1 avastatud. Programm on peatatud.", "%1 hittades. Programmet har stoppats."],
  ["Free Arduino RAM: %1 bytes", "Free Arduino RAM: %1 bytes", "RAM de Arduino disponible: %1 bytes", "Vaba muutmälu Arduinos: %1 baiti", "Ledigt Arduino RAM-minne: %1"],
  ["Free Uzi RAM: %1 bytes", "Free Uzi RAM: %1 bytes", "RAM de Uzi disponible: %1 bytes", "Vaba töömälu Uzi jaoks: %1 baiti", "Ledigt Uzi RAM-minne: %1 bytes"],
  ["Uzi - Invalid response code: %1", "Uzi - Invalid response code: %1", "Uzi - Código de respuesta inválido: %1", "Uzi – Vigane kostekood: %1", "Uzi - ogiltig svarskod: %1"],
  ["Connection timeout!", "Connection timeout!", "¡Expiró el tiempo de espera de la conexión!", "Ühendus aegus!", "Anslutningen avbröts!"],
  ['%1 detected on script "%2". The script has been stopped.', '%1 detected on script "%2". The script has been stopped.', 'Se detectó %1 en el script "%2". El script ha sido detenido.', '%1 avastatud skriptis "%2". Skript on peatatud.', '%1 hittades i skriptet %2. Skriptet har stoppats.'],
  ["Requesting connection...", "Requesting connection...", "Solicitando conexión...", "Ühenduse taotlemine...", "Begär anslutning..."],
  ["Connection accepted!", "Connection accepted!", "¡Conexión aceptada!", "Ühendus aktsepteeriti!", "Anslutningen accepterad!"],
  ["Connection rejected", "Connection rejected", "Conexión rechazada", "Ühendus lükati tagasi!", "Anslutningen avvisad!"],
  ["Connection timeout", "Connection timeout", "Expiró el tiempo de espera de la conexión", "Ühendus aegus", "Anslutnings-timeout"],

  // Blockly
  /* "task named %1 statements %2" */ ["task %1 () { \n %2 }", "task named %1 \n %2", "tarea llamada %1 \n %2", "ülesanne nimega %1 \n %2", "jobb med namn %1 \n %2"],
  /* "timer named %1 running %2 times per %3 with initial state %4 statements %5" */ ["task %1 () %4 %2 / %3 { \n %5 }", "timer named %1 \n running %2 times per %3 \n initial state %4 \n %5", "temporizador llamado %1 \n ejecutándose %2 veces por %3 \n estado inicial %4 \n %5", "töö nimega %1 \n mis jookseb %2 korda iga %3 \n algseisuga %4 \n %5", "timer med namn %1 \n som tickar %2 gånger per %3 \n med initialt tillstånd %4 \n %5"],
  /* "start task %name" */ ["start %name ;", "start %name", "iniciar %name", "alusta %name", "starta %name"],
  /* "pause task %name" */ ["pause %name ;", "pause %name", "pausar %name", "pausi %name", "paus %name"],
  /* "stop task %name" */ ["stop %name ;", "stop %name", "detener %name", "peata %name", "stoppa %name"],
  /* "resume task %name" */ ["resume %name ;", "resume %name", "continuar %name", "jätka %name", "återta %name"],
  /* "run task %name" */ ["%taskName () ;", "run %taskName", "ejecutar %taskName", "käivita %taskName", "kör %taskName"],
  /* "toggle pin %1" */ ["toggle( %1 ) ;", "toggle pin %1", "alternar pin %1", "lülita ümber viigu %1 väärtus", "växla värdet på ben %1"],
  /* "set state %1 on pin %2" */ ["turn %1 ( %2 ) ;", "%1 pin %2", "%1 pin %2", "%1 viik %2", "%1 ben %2"],
  /* "is %1 pin %2" */ ["%1 ( %2 )", "is %1 pin %2", "está %1 el pin %2", "on viik %1 %2", "är ben %1 %2"],
  /* "read pin %1" */ ["read( %1 )", "read pin %1", "leer pin %1", "loe viiku %1", "läs värdet på ben %1"],
  /* "set pin %1 to value %2" */ ["write( %1 , %2 );", "write pin %1 value %2 \n", "escribir en el pin %1 el valor %2 \n", "määra viigu %1 väärtuseks %2 \n", "sätt värde %2 på ben %1 \n"],
  /* "set pin %1 mode to %2" */ ["setPinMode( %1 , %2 );", "set pin %1 mode to %2", "configurar pin %1 como %2", "vali viigu %1 režiimiks %2", "sätt ben %1 i läge %2"],
  /* "pin %pin" */ ["%pin", "%pin", "%pin", "viik %pin", "%pin"],
  /* "pin cast %1" */ ["pin ( %1 )", "pin %1", "pin %1", "pin %1", "pin %1"],
  /* "set degrees of servo on pin %1 to %2" */ ["setServoDegrees( %1 , %2 ) ;", "move servo on pin %1 degrees %2", "mover servo en pin %1 grados %2", "pööra servot viigus %1 %2 kraadini", "vrid servot i ben %1 till %2 grader"],
  /* "get degrees of servo on pin %1" */ ["getServoDegrees( %1 ) ;", "get degrees of servo on pin %1", "obtener grados de servo en el pin %1", "servo asend viigus %1", "läs servots lutning i ben %1"],
  /* "move dcmotor %name in %direction at speed %speed" */ ["%name . %direction (speed: %speed ) ;", "move %name %direction at speed %speed", "mover %name hacia %direction a velocidad %speed", "liiguta mootorit %name suunas %direction kiirusega %speed", "rotera motorn %name i riktning %direction med hastigheten %speed"],
  /* "stop dcmotor %name" */ ["%name . brake() ;", "stop %name", "detener %name", "peata mootor %name", "stanna %name"],
  /* "set dcmotor %name speed to %speed" */ ["%name . setSpeed (speed: %speed ) ;", "set %name speed to %speed", "fijar velocidad de %name a %speed", "määra mootori %name kiiruseks %speed", "sätt motor %name hastighet till %speed"],
  /* "get dcmotor %name speed" */ ["%name .getSpeed( )", "get %name speed", "obtener la velocidad del motor %name", "mootori %name kiirus", "läs %name hastighet"],
  /* "read distance from sonar %name in units %unit" */ ["%name . %unit ()", "read distance from %name in %unit", "leer distancia de %name en %unit", "mõõda kaugus kajaloost %name ühikutes %unit", "läs avståndet från ekolodet %name i %unit"],
  /* "is button %state on pin %pin" */ ["buttons. %state ( %pin )", "is button %state on pin %pin", "está %state el botón en el pin %pin", "kas lüliti on %state viigus %pin", "är knapp %state på ben %pin"],
  /* "wait for button %action on pin %pin" */ ["buttons. %action ( %pin ) ;", "wait for button %action on pin %pin", "esperar hasta que %action el botón en el pin %pin", "oota lüliti %action viigus %pin", "vänta på knapp %action på ben %pin"],
  /* "wait button %action on pin %pin for %time %timeUnit" */ ["buttons . %action ( %pin, %time %timeUnit );", "wait button %action on pin %pin for %time %timeUnit", "esperar que %action durante %time %timeUnit \n el botón en %pin", "oota lüliti %action viigus %pin %time %timeUnit", "vänta på knapp %action ansluten till ben %pin för %time %timeUnit"],
  /* "elapsed milliseconds while pressing %pin" */ ["buttons . millisecondsHolding ( %pin )", "elapsed milliseconds while pressing %pin", "milisegundos transcurridos presionando el pin %pin", "möödunud aeg millisekundites %pin vajutuse ajal", "förfluten tid i millisekunder medan %pin nedtryckt"],
  /* "read joystick x position from %name" */ ["%name .x", "read joystick x position from %name", "read x position from %name", "juhtkangi %name x-telje asend", "läs %name x-position"],
  /* "read joystick y position from %name" */ ["%name .y", "read joystick y position from %name", "read y position from %name", "juhtkangi %name y-telje asend", "läs %name y-position"],
  /* "read joystick angle from %name" */ ["%name .getAngle()", "read angle from %name", "read angle from %name", "juhtkangi %name x-telje nurk kraadides", "läs %name x-position i grader"],
  /* "read joystick magnitude from %name" */ ["%name .getMagnitude()", "read magnitude from %name", "read magnitude from %name", "juhtkangi %name y-telje nurk kraadides", "läs %name y-position i grader"],

  /* "play tone %1 on pin %2" */ ["startTone( %tone , %pinNumber ) ;", "play tone %tone on pin %pinNumber", "reproducir tono %tone en el pin %pinNumber", "mängi tooni %tone viigus %pinNumber \n", "spela ton %tone på ben %pinNumber \n"],
  /* "play tone %1 on pin %2 for %3 %4" */ ["playTone( %tone , %pinNumber , %time %unit ) ;", "play tone %tone on pin %pinNumber for %time %unit", "reproducir tono %tone en el pin %pinNumber durante %time %unit", "mängi tooni %tone viigus %pinNumber %time %unit", "spela ton %tone på ben %pinNumber under %time %unit"],
  /* "play note %1 on pin %2" */ ["startTone( %note , %pinNumber ) ;", "play note %note on pin %pinNumber", "reproducir nota %note en el pin %pinNumber", "mängi nooti %note viigus %pinNumber \n", "spela not %note på ben %pinNumber"],
  /* "play note %1 on pin %2 for %3 %4" */ ["playTone( %note , %pinNumber , %time %unit ) ;", "play note %note on pin %pinNumber for %time %unit", "reproducir nota %note en el pin %pinNumber durante %time %unit", "mängi nooti %note viigus %pinNumber %time %unit", "spela not %note på ben %pinNumber under %time %unit"],
  /* "silence pin %1" */ ["stopTone( %pinNumber ) ;", "stop tone on pin %pinNumber", "detener tono en el pin %pinNumber", "peata toon viigus %pinNumber", "sluta tonen på ben %pinNumber"],
  /* "silence pin %1 and wait %2 %3" */ ["stopToneAndWait( %pinNumber , %time %unit ) ;", "stop tone on pin %pinNumber and wait %time %unit", "detener tono en el pin %pinNumber y esperar %time %unit", "peata toon viigus %pinNumber ja oota %time %unit", "sluta tonen på ben %pinNumber och vänta %time %unit"],

  /* "boolean %value" */ ["%boolean", "%boolean", "%boolean", "%boolean", "%boolean"],
  /* "boolean cast %1" */ ["bool ( %1 )", "bool %1", "bool %1", "bool %1", "bool %1"],
  /* "if %1 then %2" */ ["if %1 { \n %2 }", "if %1 then %2", "si %1 entonces %2", "juhul kui kehtib %1 siis %2", "om %1 isåfall %2"],
  /* "if %1 then %2 else %3" */ ["if %1 { \n %2 } else { \n %3 }", "if %1 then %2 else %3", "si %1 entonces %2 si no %3", "juhul kui kehtib %1 siis %2 muidu %3", "om %1 isåfall %2 annars %3"],
  /* "repeat forever \n %1" */ ["forever { \n %1 }", "repeat forever \n %1", "repetir por siempre \n %1", "korda igavesti \n %1", "upprepa för evigt \n %1"],
  /* "repeat %1 mode %2 condition %3 statements" */ ["%negate %condition { \n %statements }", "repeat %negate %condition \n %statements", "repetir %negate %condition \n %statements", "korda %negate %condition \n %statements", "upprepa %negate %condition \n %statements"],
  /* "repeat %1 times \n %2" */ ["repeat %times { \n %statements }", "repeat %times times \n %statements", "repetir %times veces \n %statements", "korda %times korda \n %statements", "upprepa %times gånger \n %statements"],
  /* "count with %1 from %2 to %3 by %4 %5" */ ["for %1 = %2 to %3 by %4 { \n %5 }", "count with %1 from %2 to %3 by %4 \n %5", "contar con %1 desde %2 hasta %3 por %4 \n %5", "loenda muutujat %1 alates %2 kuni %3 sammuga %4 %5", "räkna med variabeln %1 från %2 till %3 med steg av %4 \n %5"],
  /* "delay %1 %2" */ ["%delay ( %time ) ;", "wait %time %delay", "esperar %time %delay", "oota %time %delay", "vänta %time %delay"],
  /* "wait %1 %2" */ ["%negate %condition ;", "wait %negate %condition", "esperar %negate %condition", "oota %negate %condition", "vänta %negate %condition"],
  /* "elapsed time since bootup in %timeUnit" */ ["%timeUnit", "elapsed %timeUnit", "%timeUnit transcurridos", "möödunud %timeUnit algusest", "förfluten tid sen start i %timeUnit"],
  /* "logical comparison %1 left %2 operator %3 right" */ ["( %left %logical_compare_op %right )", "%left %logical_compare_op %right", "%left %logical_compare_op %right", "%left %logical_compare_op %right", "%left %logical_compare_op %right"],
  /* "logical operation %1 left %2 operator %3 right" */ ["( %left %logical_operation_op %right )", "%left %logical_operation_op %right", "%left %logical_operation_op %right", "%left %logical_operation_op %right", "%left %logical_operation_op %right"],
  /* "logical not %1" */ ["! %1", "not %1", "no %1", "mitte %1", "inte %1"],

  /* "number %1" */ ["%number", "%number", "%number", "arv %number", "%number"],
  /* "number cast %1" */ ["number ( %1 )", "number %1", "número %1", "number %1", "number %1"],
  /* "number property %1 value %2 property" */ ["%numProp ( %value )", "%value %numProp", "%value %numProp", "%value %numProp", "%value %numProp"],
  /* "number %1 is divisible by number %2" */ ["isDivisibleBy( %1 , %2 )", "%1 is divisible by %2 \n", "%1 es divisible por %2 \n", "kas %1 jaguneb arvuga %2 \n", "är %1 delbart med %2 \n"],
  /* "perform %operation on %number" */ ["%operation %number \n", "%operation %number \n", "%operation %number \n", "%operation %number \n", "%operation %number \n"],
  /* "perform trigonometric %operation on %number" */ ["%trigOperation %number \n", "%trigOperation %number \n", "%trigOperation %number \n", "%trigOperation %number \n", "%trigOperation %number \n"],
  /* "math %constant" */ ["%constant", "%constant", "%constant", "%constant", "%constant"],
  /* "arithmetic function %left %operator %right" */ ["( %left %arithmeticOperator %right )", "%left %arithmeticOperator %right", "%left %arithmeticOperator %right", "%left %arithmeticOperator %right", "%left %arithmeticOperator %right"],
  /* "perform rounding %operation on %number" */ ["%roundingOperation %number \n", "%roundingOperation %number \n", "%roundingOperation %number \n", "%roundingOperation %number \n", "%roundingOperation %number \n"],
  /* "remainder of %1 ÷ %2" */ ["%1 % %2 \n", "remainder of %1 ÷ %2 \n", "resto de %1 ÷ %2 \n", "%1 ÷ %2 jääk", "resten av %1 ÷ %2 \n"],
  /* "constrain %1 low %2 high %3" */ ["constrain ( %1 , %2 , %3 )", "constrain %1 low %2 high %3", "mantener %1 entre %2 y %3", "piira %1 olema %2 ja %3 vahel", "begränsa %1 att vara mellan %2 och %3"],
  /* "is %1 between %2 and %3" */ ["isBetween ( value: %1 , min: %2 , max: %3 )", "is %1 between %2 and %3", "está %1 entre %2 y %3", "kas %1 on %2 ja %3 vahel", "är värdet %1 mellan %2 och %3"],

  /* "random integer from %1 to %2" */ ["randomInt( %1, %2 )", "random integer between %1 and %2", "número entero al azar entre %1 y %2", "suvaline täisarv %1 ja %2 vahel", "slumpmässigt heltal mellan %1 och %2"],
  /* "random fraction" */ ["random()", "random fraction", "fracción al azar", "suvaline murdarv 0.0 ja 1.0 vahel", "slumpmässig bråkdel mellan 0.0 och 1.0"],

  /* "variable %name" */ ["%name", "%name", "%name", "muutuja %name", "%name"],
  /* "declare local variable %name with %value" */ ["var %name = %value ;", "declare local variable %name with value %value", "declarar variable local %name con valor %value", "loo muutuja %name väärtusega %value", "skapa variabeln %name med värdet %value"],
  /* "set variable %name to value %value" */ ["%name = %value ;", "set %name to %value", "establecer %name a %value", "määra %name väärtuseks %value \n", "sätt värdet på %name till %value"],
  /* "increment variable %name value by %value" */ ["%name += %value ;", "increment %name by %value", "incrementar %name por %value", "kasvata %name suurusega %value", "lägg till %value på %name"],

  /* "procedure named %name %stmts" */ ["proc %name () { \n %stmts }", "procedure named %name %stmts", "procedimiento llamado %name %stmts", "protseduur nimega %name %stmts", "procedur med namn %name %stmts"],
  /* "procedure named %name with argument %arg0 %stmts" */ ["proc %name ( %arg0 ) { \n %stmts }", "procedure named %name with argument %arg0 %stmts", "procedimiento llamado %name con argumento %arg0 %stmts", "protseduur nimega %name argumendiga %arg0 %stmts", "procedur med namn %name och argumentet %arg0 %stmts"],
  /* "procedure named %name with arguments %arg0 %arg1 %stmts" */ ["proc %name ( %arg0 , %arg1 ) { \n %stmts }", "procedure named %name with arguments %arg0 %arg1 %stmts", "procedimiento llamado %name con argumentos %arg0 %arg1 %stmts", "protseduur nimega %name argumentidega %arg0 %arg1 %stmts", "procedur med namn %name och argumenten %arg0 %arg1 %stmts"],
  /* "procedure named %name with arguments %arg0 %arg1 %arg2 %stmts" */ ["proc %name ( %arg0 , %arg1 , %arg2 ) { \n %stmts }", "procedure named %name with arguments %arg0 %arg1 %arg2 %stmts", "procedimiento llamado %name con argumentos %arg0 %arg1 %arg2 %stmts", "protseduur nimega %name argumentidega %arg0 %arg1 %arg2 %stmts", "procedur med namn %name och argumenten %arg0 %arg1 %arg2 %stmts"],
  /* "procedure exit e.g. return with no value" */ ["exit ;", "exit", "salir", "lõpeta", "sluta"],
  /* "execute procedure %name" */ ["%procName () ;", "execute %procName", "ejecutar %procName", "jooksuta %procName", "kör %procName"],
  /* "execute procedure %name with %arg0" */ ["%procName ( %arg0 ) ;", "execute %procName %arg0", "ejecutar %procName %arg0", "jooksuta toimingut %procName argumendiga %arg0", "kör %procName %arg0"],
  /* "execute procedure %name with %arg0 and %arg1" */ ["%procName ( %arg0 , %arg1 ) ;", "execute %procName %arg0 %arg1", "ejecutar %procName %arg0 %arg1", "jooksuta toimingut %procName argumentidega %arg0 %arg1", "kör %procName %arg0 %arg1"],
  /* "execute procedure %name with %arg0, %arg1 and %arg2" */ ["%procName ( %arg0 , %arg1 , %arg2 ) ;", "execute %procName %arg0 %arg1 %arg2", "ejecutar %procName %arg0 %arg1 %arg2", "jooksuta toimingut %procName argumentidega %arg0 %arg1 %arg2", "kör %procName %arg0 %arg1 %arg2"],

  /* "function named %name %stmts" */ ["func %name () { \n %stmts }", "function named %name %stmts", "función llamada %name %stmts", "funktsioon nimega %name %stmts", "funktion med namn %name %stmts"],
  /* "function named %name with argument %arg0 %stmts" */ ["func %name ( %arg0 ) { \n %stmts }", "function named %name with argument %arg0 %stmts", "función llamada %name con argumento %arg0 %stmts", "funktsioon nimega %name argumentidega %arg0 %stmts", "funktion med namn %name och argumentet %arg0 %stmts"],
  /* "function named %name with arguments %arg0 %arg1 %stmts" */ ["func %name ( %arg0 , %arg1 ) { \n %stmts }", "function named %name with arguments %arg0 %arg1 %stmts", "función llamada %name con argumentos %arg0 %arg1 %stmts", "funktsioon nimega %name argumentidega %arg0 %arg1 %stmts", "funktion med namn %name och argument %arg0 %arg1 %stmts"],
  /* "function named %name with arguments %arg0 %arg1 %arg2 %stmts" */ ["func %name ( %arg0 , %arg1 , %arg2 ) { \n %stmts }", "function named %name with arguments %arg0 %arg1 %arg2 %stmts", "función llamada %name con argumentos %arg0 %arg1 %arg2 %stmts", "funktsioon nimega %name argumentidega %arg0 %arg1 %arg2 %stmts", "funktion med namn %name och argument %arg0 %arg1 %arg2 %stmts"],
  /* "function return with value %value" */ ["return %value ;", "return %value", "devolver %value", "tagasta %value", "svara %value"],
  /* "evaluate function %name" */ ["%funcName ()", "evaluate function %funcName", "evaluar %funcName", "arvuta %funcName", "beräkna %funcName"],
  /* "evaluate function %name with argument %arg0" */ ["%funcName ( %arg0 )", "evaluate function %funcName %arg0", "evaluar %funcName %arg0", "arvuta %funcName argumendiga %arg0", "beräkna %funcName med argumentet %arg0"],
  /* "evaluate function %name with arguments %arg0 %arg1" */ ["%funcName ( %arg0 , %arg1 )", "evaluate function %funcName %arg0 %arg1", "evaluar %funcName %arg0 %arg1", "arvuta %funcName argumentidega %arg0 %arg1", "beräkna %funcName med argumenten %arg0 %arg1"],
  /* "evaluate function %name with arguments %arg0 %arg1 %arg2" */ ["%funcName ( %arg0 , %arg1 , %arg2 )", "evaluate function %funcName %arg0 %arg1 %arg2", "evaluar %funcName %arg0 %arg1 %arg2", "arvuta %funcName argumentidega %arg0 %arg1 %arg2", "beräkna %funcName med argumenten %arg0 %arg1 %arg2"],

  /* "is even" */ ["isEven", "is even", "es par", "on paarisarv", "är ett jämnt tal"],
  /* "is odd" */ ["isOdd", "is odd", "es impar", "on paaritu arv", "är ett ojämnt tal"],
  /* "is prime" */ ["isPrime", "is prime", "es primo", "on algarv", "är ett primtal"],
  /* "is whole" */ ["isWhole", "is whole", "es entero", "on täisarv", "är ett heltal"],
  /* "is positive" */ ["isPositive", "is positive", "es positivo", "on positiivne", "är positivt"],
  /* "is negative" */ ["isNegative", "is negative", "es negativo", "on negatiivne", "är negativt"],
  /* "arithmetic operator /" */ ["/", "÷", "÷", "÷", "÷"],
  /* "arithmetic operator *" */ ["*", "×", "×", "×", "×"],
  /* "arithmetic operator -" */ ["-", "-", "-", "-", "-"],
  /* "arithmetic operator +" */ ["+", "+", "+", "+", "+"],
  /* "arithmetic operator ^" */ ["**", "^", "^", "^", "^"],
  /* "Tasks" */ ["Tasks", "Tasks", "Tareas", "Tööd", "Jobb"],
  /* "GPIO" */ ["GPIO", "GPIO", "GPIO", "Viigud", "Ben"],
  /* "Motors" */ ["Motors", "Motors", "Motores", "Mootorid", "Motorer"],
  /* "Servo" */ ["Servo", "Servo", "Servo", "Servo", "Servon"],
  /* "DC" */ ["DC", "DC", "CC", "DC", "DC"],
  /* "Sensors" */ ["Sensors", "Sensors", "Sensores", "Andurid", "Sensorer"],
  /* "Sonar" */ ["Sonar", "Sonar", "Sonar", "Kajalood", "Ekolod"],
  /* "Joystick" */ ["Joystick", "Joystick", "Palanca de mando", "Juhtkang", "Joystick"],
  /* "Control" */ ["Control", "Control", "Control", "Kontroll", "Kontroll"],
  /* "Math" */ ["Math", "Math", "Matemática", "Aritmeetika", "Matematik"],
  /* "Variables" */ ["Variables", "Variables", "Variables", "Muutujad", "Variabler"],
  /* "Functions" */ ["Functions", "Functions", "Funciones", "Arvutused", "Funktioner"],
  /* "Comments" */ ["Comments", "Comments", "Comentarios", "Kommentaarid", "Kommentarer"],
  /* "Procedures" */ ["Procedures", "Procedures", "Procedimientos", "Toimingud", "Procedurer"],
  /* "Configure DC motors..." */ ["Configure DC motors...", "Configure DC motors...", "Configurar motores CC...", "Seadista mootorid...", "Konfigurera motorer..."],
  /* "Configure sonars..." */ ["Configure sonars...", "Configure sonars...", "Configurar sonares...", "Seadista kajalood...", "Konfigurera ekolod..."],
  /* "square root" */ ["square root", "square root", "raíz cuadrada", "ruutjuur", "roten ur"],
  /* "absolute" */ ["absolute", "absolute", "valor absoluto", "absoluutväärtus", "absolutvärde"],
  /* "logical operator =" */ ["==", "=", "=", "=", "="],
  /* "logical operator ≠" */ ["!=", "≠", "≠", "≠", "≠"],
  /* "logical operator <" */ ["<", "<", "<", "<", "<"],
  /* "logical operator ≤" */ ["<=", "≤", "≤", "≤", "≤"],
  /* "logical operator >" */ [">", ">", ">", ">", ">"],
  /* "logical operator ≥" */ [">=", "≥", "≥", "≥", "≥"],
  /* "constant π" */ ["3.141592653589793", "π", "π", "π (pii)", "π"],
  /* "constant ℯ" */ ["2.718281828459045", "ℯ", "ℯ", "ℯ", "ℯ"],
  /* "constant φ" */ ["1.61803398875", "φ", "φ", "φ (kuldlõige)", "φ"],
  /* "constant √2" */ ["1.4142135623730951", "√2", "√2", "√2 (ruutjuur kahest)", "√2"],
  /* "constant √½" */ ["0.7071067811865476", "√½", "√½", "√½ (ruutjuur poolest)", "√½"],
  /* "constant ∞" */ ["Infinity", "∞", "∞", "∞ (lõpmatus)", "∞"],
  /* "ln" */ ["ln", "ln", "ln", "ln", "ln"],
  /* "log10" */ ["log10", "log10", "log10", "log10", "log10"],
  /* "e^" */ ["e^", "e^", "e^", "e^", "e^"],
  /* "10^" */ ["10^", "10^", "10^", "10^", "10^"],
  /* "sin" */ ["sin", "sin", "seno", "sin", "sin"],
  /* "cos" */ ["cos", "cos", "coseno", "cos", "cos"],
  /* "tan" */ ["tan", "tan", "tangente", "tan", "tan"],
  /* "asin" */ ["asin", "asin", "arcoseno", "asin", "asin"],
  /* "acos" */ ["acos", "acos", "arcocoseno", "acos", "acos"],
  /* "atan" */ ["atan", "atan", "arcotangente", "atan", "atan"],
  /* "round" */ ["round", "round", "redondear", "ümardamine", "avrunda"],
  /* "round up" */ ["ceil", "round up", "redondear hacia arriba", "ümarda üles", "avrunda uppåt"],
  /* "round down" */ ["floor", "round down", "redondear hacia abajo", "ümarda alla", "avrunda nedåt"],
  /* "Configure variables..." */ ["Configure variables...", "Configure variables...", "Configurar variables...", "Seadista muutujaid...", "Konfigurera variabler..."],
  /* "Variable name" */ ["Variable name", "Variable name", "Nombre de variable", "Muutuja nimi", "Variabelns namn"],
  /* "Initial value (if global)" */ ["Initial value (if global)", "Initial value (if global)", "Valor inicial (en caso de global)", "Algväärtus (kui on globaalne)", "Initialvärde (om globalt)"],
  /* "Sound" */ ["Sound", "Sound", "Sonido", "Heli", "Ljud"],
  /* "Buttons" */ ["Buttons", "Buttons", "Botones", "Lülitid", "Knappar"],
  /* "button pressed" */ ["isPressed", "pressed", "presionado", "vajutatud", "nedtryckt"],
  /* "button released" */ ["isReleased", "released", "suelto", "vabastatud", "släppt"],
  /* "button waitForPress" */ ["waitForPress", "press", "presione", "vajutamist", "tryck"],
  /* "button waitForRelease" */ ["waitForRelease", "press and release", "presione y suelte", "vajutamist ja vabastamist", "tryck och släpp"],
  /* "button mode INPUT" */ ["INPUT", "INPUT", "ENTRADA", "SISEND", "INGÅNG"],
  /* "button mode OUTPUT" */ ["OUTPUT", "OUTPUT", "SALIDA", "VÄLJUND", "UTGÅNG"],
  /* "button mode INPUT PULLUP" */ ["INPUT PULLUP", "INPUT PULLUP", "ENTRADA PULLUP", "PULLUP-SISEND", "PULLUP-INGÅNG"],

  /* "turn state on" */ ["On", "turn on", "encender", "lülita sisse", "slå på"],
  /* "turn state off" */ ["Off", "turn off", "apagar", "lülita välja", "slå av"],
  /* "pin state on" */ ["isOn", "on", "encendido", "sees", "på"],
  /* "pin state off" */ ["isOff", "off", "apagado", "väljas", "av"],
  /* "milliseconds" */ ["milliseconds", "milliseconds", "milisegundos", "millisekundit", "millisekunder"],
  /* "delay in milliseconds" */ ["delayMs", "milliseconds", "milisegundos", "millisekundit", "millisekunder"],
  /* "seconds" */ ["seconds", "seconds", "segundos", "sekundit", "sekunder"],
  /* "delay in seconds" */ ["delayS", "seconds", "segundos", "sekundit", "sekunder"],
  /* "minutes" */ ["minutes", "minutes", "minutos", "minutit", "minuter"],
  /* "delay in minutes" */ ["delayM", "minutes", "minutos", "minutit", "minuter"],
  /* "ticking scale second" */ ["s", "second", "segundo", "sekund", "sekund"],
  /* "ticking scale minute" */ ["m", "minute", "minute", "minut", "minut"],
  /* "ticking scale hour" */ ["h", "hour", "hour", "tund", "timme"],
  /* "running" */ ["running", "started", "iniciado", "käivitatud", "startad"],
  /* "stopped" */ ["stopped", "stopped", "detenido", "peatatud", "stoppad"],
  /* "forward" */ ["forward", "forward", "adelante", "edasi", "framåt"],
  /* "backward" */ ["backward", "backward", "atrás", "tagasi", "bakåt"],
  /* "distance_mm" */ ["distance_mm", "mm", "mm", "millimeeter", "mm"],
  /* "distance_cm" */ ["distance_cm", "cm", "cm", "sentimeeter", "cm"],
  /* "distance_m" */ ["distance_m", "m", "m", "meeter", "m"],
  /* "while" */ ["while", "while", "mientras que", "nii kaua kui kehtib", "medan"],
  /* "until" */ ["until", "until", "hasta que", "nii kaua kuni kehtib", "tills"],
  /* "true" */ ["true", "true", "verdadero", "tõene", "sann"],
  /* "false" */ ["false", "false", "falso", "väär", "falsk"],
  /* "logical and" */ ["&&", "and", "y", "ja", "och"],
  /* "logical or" */ ["||", "or", "o", "või", "eller"],
];
