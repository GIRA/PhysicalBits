accessing
primitiveForSpecialVariable: variableName
	| primName validNames |
	validNames := #(seconds millis).
	primName := (validNames includes: variableName)
		ifTrue: [variableName]
		ifFalse: nil.
	^ primName ifNotNil: [UziPrimitive named: primName]