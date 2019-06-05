compiling
compileJSON: jsonString
	| program encoded |
	[program := Uzi compileJSON: jsonString.
	encoded :=  Uzi encode: program.
	UziOutput cr; 
		log: 'Compilation successful!';
		log: 'Program size (bytes): {1}' format: { encoded size };
		log: encoded printString;
		flush.
	^ program]
			on: UziParseError , UziCompileError 
			do: [:err | UziOutput cr; log: err asString; flush.
				err signal]