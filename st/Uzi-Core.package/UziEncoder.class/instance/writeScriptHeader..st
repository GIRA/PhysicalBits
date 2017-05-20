encoding scripts
writeScriptHeader: anUziScript
	| header |
	"The header has 4 bytes.
	1 bit : isTicking (1 true / 0 false)
	1 bit: hasVariables (1 true / 0 false)
	30 bits : delay (ulong)"	
	header := anUziScript delay rounded min: (2 raisedTo: 30) - 1.
	header := header bitOr: ((anUziScript isTicking ifTrue: [1] ifFalse: [0]) bitShift: 31).
	header := header bitOr: ((anUziScript hasVariables ifTrue: [1] ifFalse: [0]) bitShift: 30).
	3 to: 0 by: -1 do: [:i |
		stream nextPut: ((header bitShift: -8 * i) bitAnd: 16rFF)]