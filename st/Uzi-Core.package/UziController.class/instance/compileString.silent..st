compiling
compileString: aString silent: silentFlag
	| program encoded |
	[program := Uzi compileString: aString.
	encoded :=  Uzi encode: program.
	silentFlag ifFalse: [
		Uzi logger cr; 
			log: 'Program size (bytes): %1' format: { encoded size };
			log: encoded printString;
			success: 'Compilation successful!';
			flush].
	currentProgram := program.
	self triggerEvent: UziEvents programUpdate.
	^ program]
			on: UziError
			do: [:err |
				silentFlag ifFalse: [Uzi logger cr; error: err asString; flush].
				err signal]