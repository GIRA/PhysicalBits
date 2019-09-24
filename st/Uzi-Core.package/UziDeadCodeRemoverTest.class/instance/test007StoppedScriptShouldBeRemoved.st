as yet unclassified
test007StoppedScriptShouldBeRemoved
	| actual expected original remover |
	original := Uzi parse: '
		task bar() stopped { toggle(D12); }'.
	expected := Uzi parse: ''.
	remover := UziDeadCodeRemover new.
	actual := remover applyOn: original.
	self assert: expected equals: actual
