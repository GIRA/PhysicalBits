as yet unclassified
test014UsedGlobalsShouldNotBeRemoved
	| actual expected original remover |
	original := Uzi parse: '
		var a = 0;
		var b = 1;
		task blink13() running 1/s { toggle(b); }
		task test() stopped { a = b + 1; }'.
	expected := Uzi parse: '
		var b = 1;
		task blink13() running 1/s { toggle(b); }'.
	remover := UziDeadCodeRemover new.
	actual := remover applyOn: original.
	self assert: expected equals: actual
