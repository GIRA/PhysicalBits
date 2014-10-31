encoding programs
writeProgramInputs: program
	| inputs |
	inputs := 0.
	1 to: 32 do: [:pin |
		inputs := inputs bitOr: ((program inputs includes: pin) 
			ifTrue: [1]
			ifFalse: [0]) << (32 - pin)].
	8*3 to: 0 by: -8 do: [:i |
		stream nextPut: ((inputs >> i) bitAnd: 16rFF)]	