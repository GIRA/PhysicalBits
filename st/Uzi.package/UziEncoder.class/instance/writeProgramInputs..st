encoding programs
writeProgramInputs: program
	"Three bytes in which each bit represents a pin.
	Input pins are marked with 1"
	| inputs totalBytes |
	totalBytes := 3.
	inputs := 0.
	1 to: 8 * totalBytes do: [:pin |
		inputs := inputs bitOr: ((program inputs includes: pin) 
			ifTrue: [1]
			ifFalse: [0]) << (8 * totalBytes - pin)].
	totalBytes - 1 to: 0 by: -1 do: [:i |
		stream nextPut: ((inputs bitShift: i * -8) bitAnd: 16rFF)]	