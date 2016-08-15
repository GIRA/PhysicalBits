tests
testProgramCreation
	| script bytecodes program |
	program := Uzi program: [:p | p
		script: #main
		ticking: true
		delay: 1000
		bytecodes: [:uzi | uzi
			push: 9;
			push: 10;
			prim: #getValue;
			prim: #setValue]].
	self assert: 1 equals: program scripts size.
	self assert: #(9 10) equals: (program variables collect: #value).
	script := program scripts first.
	self assert: script name = #main.
	self assert: script isTicking.
	self assert: script delay = 1000.
	bytecodes := script bytecodes.
	self assert: bytecodes size = 4.
	self assert: bytecodes = {
		push arg: program variables first.
		push arg: program variables second.
		prim arg: (UziPrimitive named: #getValue).
		prim arg: (UziPrimitive named: #setValue)
	}
