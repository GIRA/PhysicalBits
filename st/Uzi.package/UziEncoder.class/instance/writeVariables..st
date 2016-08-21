encoding programs
writeVariables: program
	| grouped |
	variables := program variables.
	stream nextPut: variables size.
	grouped := variables groupBy: #size having: [:ign | true].
	grouped keysAndValuesDo: [:size :group |
		"The first byte says how many (6 bits) and of what size (2 bits)"
		stream nextPut: ((group size bitShift: 2) bitOr: size - 1).
		group do: [:var | var writeOn: stream]]