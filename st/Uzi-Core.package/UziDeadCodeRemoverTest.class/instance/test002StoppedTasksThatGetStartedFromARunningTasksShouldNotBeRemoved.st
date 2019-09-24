as yet unclassified
test002StoppedTasksThatGetStartedFromARunningTasksShouldNotBeRemoved
	| actual expected original remover |
	original := Uzi parse: '
		task alive() running { toggle(D13); start dead; }
		task dead() stopped { toggle(D12); }
		task reallyDead() stopped { toggle(D11); }'.
	expected := Uzi parse: '
		task alive() running { toggle(D13); start dead; }
		task dead() stopped { toggle(D12); }'.
	remover := UziDeadCodeRemover new.
	actual := remover applyOn: original.
	self assert: expected equals: actual
