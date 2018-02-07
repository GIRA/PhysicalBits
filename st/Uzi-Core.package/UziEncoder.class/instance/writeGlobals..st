encoding programs
writeGlobals: anUziProgram
	| toEncode grouped |
	globals := anUziProgram allGlobals.
	"Exclude the default globals, which are not supposed to be encoded"
	toEncode := globals copyWithoutAll: anUziProgram defaultGlobals.
	stream nextPut: toEncode size.
	grouped := toEncode groupBy: [:var | var size] having: [:ign | true].
	(1 to: 4) do: [:size || encode |
		encode := [:group |
			stream nextPut: ((group size bitShift: 2) bitOr: size - 1).
			group do: [:var | var writeOn: stream]].
		grouped at: size ifPresent: [:group || rs |
			"The first byte or each group says how many variables and the size.
			- 6 bits: var count
			- 2 bits: size
				00 -> 1 byte
				01 -> 2 bytes
				10 -> 3 bytes
				11 -> 4 bytes"
			rs := group readStream.
			[rs atEnd] 
				whileFalse: [encode value: (rs next: 2r111111)]]]