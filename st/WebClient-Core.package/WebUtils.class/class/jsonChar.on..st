json-encode
jsonChar: aCharacter on: stream
	"Writes a single encoded character"

	| ascii |
	ascii := aCharacter asciiValue.

	ascii < 32 ifTrue:["Control character"
		ascii caseOf: {
			[13]	-> [^stream nextPutAll: '\r'].
			[12]	-> [^stream nextPutAll: '\f'].
			[10]	-> [^stream nextPutAll: '\n'].
			[9]		-> [^stream nextPutAll: '\t'].
			[8]		-> [^stream nextPutAll: '\b'].
		} otherwise:[
			^stream nextPutAll: '\u'; nextPutAll: 
				((ascii printStringBase: 16) padded: #left to: 4 with: $0)
		].
	].

	(ascii >= 32 and:[ascii <=127]) ifTrue:["Ascii character"
		(ascii = 34 or:[ascii = 92 or:[ascii = 47]]) ifTrue:[stream nextPut: $\].
		^stream nextPut: aCharacter
	].

	"Encode other characters (control chars, accents, umlauts, unicode)"
	stream nextPutAll:  
		'\u', (((ascii bitAnd: 16rFFFF) printStringBase: 16) padded: #left to: 4 with: $0).
