tests
testVariablesAndLiteralsDoNotCollide
	| var program |
	program := Uzi program: [:p | p
		script: #foo
		ticking: true
		delay: 0
		instructions: [:s | s
			push: 1;
			pop: #a];
		script: #bar
		ticking: true
		delay: 0
		instructions: [:s | s
			push: 0;
			write: 13]].
	var := program globals detect: [:v | v name = #a].
	self deny: program scripts second instructions first argument = var