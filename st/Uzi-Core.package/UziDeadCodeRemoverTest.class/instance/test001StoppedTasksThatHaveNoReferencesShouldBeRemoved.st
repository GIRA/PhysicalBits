as yet unclassified
test001StoppedTasksThatHaveNoReferencesShouldBeRemoved
	| actual expected original remover |
	original := Uzi parse: '
		task alive() running { toggle(D13); }
		task dead() stopped { toggle(D12); }'.
	expected := Uzi parse: '
		task alive() running { toggle(D13); }'.
	remover := UziDeadCodeRemover new.
	actual := remover applyOn: original.
	self assert: expected equals: actual
