as yet unclassified
test010PauseScriptShouldMarkTheScriptAsAlive
	| actual expected original remover |
	original := Uzi parse: '
		task blink13() running 1/s { toggle(D13); pause blink12; }
		task blink12() stopped 1/s { toggle(D12); }'.
	expected := Uzi parse: '
		task blink13() running 1/s { toggle(D13); pause blink12; }
		task blink12() stopped 1/s { toggle(D12); }'.
	remover := UziDeadCodeRemover new.
	actual := remover applyOn: original.
	self assert: expected equals: actual
