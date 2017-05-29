accessing
primitiveForSpecialVariable: variableName
	| primName validNames |
	validNames := #(seconds millis coroutine).
	primName := (validNames includes: variableName)
		ifTrue: [variableName]
		ifFalse: nil.
	^ primName ifNotNil: [UziPrimitive named: primName]