initialize
initializeWithName: nameSymbol state: stateSymbol delay: delayMs bytecodes: bytecodesArray
	name := nameSymbol.
	state := stateSymbol.
	delay := delayMs.
	bytecodes := bytecodesArray ifEmpty: [{UziTerminateBytecode new}].
	self initialize 