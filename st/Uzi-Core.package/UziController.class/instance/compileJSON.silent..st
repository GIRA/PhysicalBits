compiling
compileJSON: jsonString silent: silentFlag
	| program encoded |
	[program := Uzi compileJSON: jsonString.
	encoded :=  Uzi encode: program.
	silentFlag ifFalse: [
		Uzi logger cr; 
			log: 'Program size (bytes): {1}' format: { encoded size };
			log: encoded printString;
			success: 'Compilation successful!';
			flush].
	currentProgram := program.
	self triggerEvent: UziEvents programUpdate.
	^ program]
			on: UziParseError , UziCompileError 
			do: [:err | 
				silentFlag ifFalse: [Uzi logger cr; error: err asString; flush].
				err signal]