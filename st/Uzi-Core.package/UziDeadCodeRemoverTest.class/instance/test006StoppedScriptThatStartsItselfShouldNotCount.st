as yet unclassified
test006StoppedScriptThatStartsItselfShouldNotCount
	| actual expected original remover |
	original := Uzi parse: '
		task bar() stopped { start bar; toggle(D12); }'.
	expected := Uzi parse: ''.
	remover := UziDeadCodeRemover new.
	actual := remover applyOn: original.
	self assert: expected equals: actual
