json-decode
jsonCharFrom: stream
	"Decodes a backslash-escaped character"

	| ch |
	^(ch := stream next) caseOf: {
		[$u]		->	[Unicode value: 
						(Integer readFrom: (stream next: 4) readStream base: 16)].
		[$r] 	-> 	[Character cr].
		[$n] 	-> 	[Character lf].
		[$t] 	-> 	[Character tab].
		[$b] 	->	[Character backspace].
		[$f] 	->	[Character newPage].
	} otherwise:[ch].
