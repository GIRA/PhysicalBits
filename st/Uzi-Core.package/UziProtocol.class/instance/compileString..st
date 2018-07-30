compiling
compileString: aString
	| program encoded |
	[program := Uzi compileString: aString.
	encoded :=  Uzi encode: program.
	UziTranscript cr; 
		log: 'Compilation successful!';
		log: 'Program size (bytes): {1}' format: { encoded size };
		log: encoded printString.
	^ program]
			on: UziParseError , UziCompileError 
			do: [:err | UziTranscript cr; log: err asString.
				err signal]