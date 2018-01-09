encoding programs
writeGlobals: anUziProgram
	| grouped |
	globals := anUziProgram sortedGlobals 
		copyWithoutAll: anUziProgram defaultGlobals.	
	stream nextPut: globals size.
	grouped := globals groupBy: [:var | var size] having: [:ign | true].
	(1 to: 4) do: [:size |
		grouped at: size ifPresent: [:group |
			"The first byte or each group says how many variables and the size.
			- 6 bits: var count
			- 2 bits: size
				00 -> 1 byte
				01 -> 2 bytes
				10 -> 3 bytes
				11 -> 4 bytes"
			stream nextPut: ((group size bitShift: 2) bitOr: size - 1).
			group do: [:var | var writeOn: stream]]].
	"Add the default globals to the list after encoding in order to simplify indexing all globals later"
	globals := anUziProgram defaultGlobals, globals.