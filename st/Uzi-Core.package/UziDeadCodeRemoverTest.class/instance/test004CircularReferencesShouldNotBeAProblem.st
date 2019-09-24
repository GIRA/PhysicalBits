as yet unclassified
test004CircularReferencesShouldNotBeAProblem
	| actual expected original remover |
	original := Uzi parse: '
		task foo() running { toggle(D13); start bar; }
		task bar() stopped { start foo; toggle(D12); }'.
	expected := Uzi parse: '
		task foo() running { toggle(D13); start bar; }
		task bar() stopped { start bar; toggle(D12); }'.
	remover := UziDeadCodeRemover new.
	actual := remover applyOn: original.
	self assert: expected equals: actual
