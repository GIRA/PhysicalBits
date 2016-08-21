encoding programs
writeVariables: program
	| grouped |
	variables := program variables.
	stream nextPut: variables size.
	grouped := variables groupBy: #size having: [:ign | true].
	grouped keysAndValuesDo: [:size :group |
		"The first byte or each group says how many variables and the size.
		- 6 bits: var count
		- 2 bits: size
			00 -> 1 byte
			01 -> 2 bytes
			10 -> 3 bytes
			11 -> 4 bytes"
		stream nextPut: ((group size bitShift: 2) bitOr: size - 1).
		group do: [:var | var writeOn: stream]]