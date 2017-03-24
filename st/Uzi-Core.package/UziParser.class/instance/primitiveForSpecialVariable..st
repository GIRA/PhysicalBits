accessing
primitiveForSpecialVariable: variableName
	| primName validNames |
	validNames := #(time).
	primName := (validNames includes: variableName)
		ifTrue: [variableName]
		ifFalse: nil.
	^ primName ifNotNil: [UziPrimitive named: primName]