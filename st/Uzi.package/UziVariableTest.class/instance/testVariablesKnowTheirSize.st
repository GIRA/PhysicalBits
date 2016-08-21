as yet unclassified
testVariablesKnowTheirSize
	| vars |
	vars := {
		0 -> 1.
		16rFF -> 1.
		16r100 -> 2.
		16rFFFF -> 2.
		16r10000 -> 3.
		16rFFFFFF -> 3.
		16r1000000 -> 4.
		16rFFFFFFFFFF -> 4.
		0.5 -> 4.
		-1 -> 4
	}.
	vars do: [:each || var expectedSize |
		var := UziVariable value: each key.
		expectedSize := each value.
		self assert: expectedSize equals: var size]