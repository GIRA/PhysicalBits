as yet unclassified
test015UnusedGlobalsShouldBeRemovedEvenIfTheyAreHiddenByALocalVariable
	| actual expected original remover |
	original := Uzi parse: '
		var a = 0;
		task blink13() running 1/s { var a = D13; toggle(a); }'.
	expected := Uzi parse: '
		task blink13() running 1/s { var a = D13; toggle(a); }'.
	remover := UziDeadCodeRemover new.
	actual := remover applyOn: original.
	self assert: expected equals: actual
