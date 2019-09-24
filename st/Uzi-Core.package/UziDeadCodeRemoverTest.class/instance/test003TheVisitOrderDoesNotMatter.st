as yet unclassified
test003TheVisitOrderDoesNotMatter
	| actual expected original remover |
	original := Uzi parse: '
		task dead() stopped { toggle(D12); }
		task alive() running { toggle(D13); start dead; }
		task reallyDead() stopped { toggle(D11); }'.
	expected := Uzi parse: '
		task dead() stopped { toggle(D12); }
		task alive() running { toggle(D13); start dead; }'.
	remover := UziDeadCodeRemover new.
	actual := remover applyOn: original.
	self assert: expected equals: actual
