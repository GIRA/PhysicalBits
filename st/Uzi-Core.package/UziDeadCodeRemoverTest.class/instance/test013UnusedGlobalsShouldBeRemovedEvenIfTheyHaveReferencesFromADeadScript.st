as yet unclassified
test013UnusedGlobalsShouldBeRemovedEvenIfTheyHaveReferencesFromADeadScript
	| actual expected original remover |
	original := Uzi parse: '
		var a = 0;
		task blink13() running 1/s { toggle(D13); }
		task test() stopped { a = 100; }'.
	expected := Uzi parse: '
		task blink13() running 1/s { toggle(D13); }'.
	remover := UziDeadCodeRemover new.
	actual := remover applyOn: original.
	self assert: expected equals: actual
