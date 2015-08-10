encoding scripts
writeScriptHeader: anUziScript
	| header |
	"The header has 4 bytes.
	1 bit : isTicking (1 true / 0 false)
	31 bits : delay (ulong)"
	header := ((anUziScript isTicking 
				ifTrue: [1] 
				ifFalse: [0]) 
					bitShift: 31) 
					bitOr: anUziScript delay rounded.
	3 to: 0 by: -1 do: [:i |
		stream nextPut: ((header bitShift: -8 * i) bitAnd: 16rFF)]