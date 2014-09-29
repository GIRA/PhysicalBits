initialize
initializeWithName: nameSymbol
state: stateSymbol 
delay: delayMs 
literals: literalsArray
variables: variablesArray
bytecodes: bytecodesArray
	name := nameSymbol.
	state := stateSymbol.
	delay := delayMs.
	literals := literalsArray.
	variables := variablesArray.
	bytecodes := bytecodesArray ifEmpty: [{UziEndBytecode new}].
	self initialize 