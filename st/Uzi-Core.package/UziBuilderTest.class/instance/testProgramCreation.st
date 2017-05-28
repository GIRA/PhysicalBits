tests
testProgramCreation
	| script instructions program |
	program := Uzi program: [:p | p
		script: #main
		ticking: true
		delay: 1000
		instructions: [:uzi | uzi
			push: 9;
			push: 10;
			prim: #read;
			prim: #write]].
	self assert: 1 equals: program scripts size.
	self assert: #(9 10) equals: (program globals collect: #value).
	script := program scripts first.
	self assert: script name = #main.
	self assert: script isTicking.
	self assert: script delay = 1000.
	instructions := script instructions.
	self assert: instructions size = 4.
	self assert: instructions = {
		push arg: program globals first.
		push arg: program globals second.
		prim arg: (UziPrimitive named: #read).
		prim arg: (UziPrimitive named: #write)
	}
