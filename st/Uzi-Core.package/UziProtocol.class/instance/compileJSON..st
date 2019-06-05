compiling
compileJSON: jsonString
	| program encoded |
	[program := Uzi compileJSON: jsonString.
	encoded :=  Uzi encode: program.
	UziTranscript cr; 
		log: 'Compilation successful!';
		log: 'Program size (bytes): {1}' format: { encoded size };
		log: encoded printString;
		flush.
	^ program]
			on: UziParseError , UziCompileError 
			do: [:err | UziTranscript cr; log: err asString.
				err signal]