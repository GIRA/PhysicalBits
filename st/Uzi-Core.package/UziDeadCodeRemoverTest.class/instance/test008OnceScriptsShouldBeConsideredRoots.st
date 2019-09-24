as yet unclassified
test008OnceScriptsShouldBeConsideredRoots
	| actual expected original remover |
	original := Uzi parse: '
		task foo() { toggle(D12); }'.
	expected := Uzi parse: '
		task foo() { toggle(D12); }'.
	remover := UziDeadCodeRemover new.
	actual := remover applyOn: original.
	self assert: expected equals: actual
