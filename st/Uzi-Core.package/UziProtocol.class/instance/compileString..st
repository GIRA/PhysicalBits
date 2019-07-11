compiling
compileString: aString
	| program encoded |
	[program := Uzi compileString: aString.
	encoded :=  Uzi encode: program.
	Uzi logger cr; 
		log: 'Program size (bytes): {1}' format: { encoded size };
		log: encoded printString;
		success: 'Compilation successful!';
		flush.
	currentProgram := program.
	self triggerEvent: UziProtocolEvents programUpdate.
	^ program]
			on: UziParseError , UziCompileError 
			do: [:err | Uzi logger cr; error: err asString; flush.
				err signal]