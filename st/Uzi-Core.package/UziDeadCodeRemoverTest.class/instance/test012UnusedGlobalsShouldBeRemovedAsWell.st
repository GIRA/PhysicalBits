as yet unclassified
test012UnusedGlobalsShouldBeRemovedAsWell
	| actual expected original remover |
	original := Uzi parse: '
		var a = 0;
		task blink13() running 1/s { toggle(D13); }'.
	expected := Uzi parse: '
		task blink13() running 1/s { toggle(D13); }'.
	remover := UziDeadCodeRemover new.
	actual := remover applyOn: original.
	self assert: expected equals: actual
