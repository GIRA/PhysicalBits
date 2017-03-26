accessing
primitiveForSpecialVariable: variableName
	| primName validNames |
	validNames := #(seconds).
	primName := (validNames includes: variableName)
		ifTrue: [variableName]
		ifFalse: nil.
	^ primName ifNotNil: [UziPrimitive named: primName]