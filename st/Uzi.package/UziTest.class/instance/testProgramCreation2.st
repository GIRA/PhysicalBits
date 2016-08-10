tests
testProgramCreation2
	| program script |
	program := Uzi program: [:p | p
		script: #main 
		ticking: true delay: 1000
		bytecodes: [:s | s
			push: 9;
			prim: #getValue;
			jnz: 4;
			push: 9;
			push: 255;
			prim: #setValue;
			jmp: 3;
			push: 9;
			push: 0;
			prim: #setValue;
			end	]].
	self assert: 1 equals: program scripts size.
	self assert: #(9 255 0) equals: (program variables collect: #value).
	script := program scripts first.
	self assert: script name = #main.
	self assert: script isTicking.
	self assert: script delay = 1000.
	self assert: script bytecodes = {
		push arg: program variables first.
		prim arg: (UziPrimitive named: #getValue).
		jnz arg: 4.
		push arg: program variables first.
		push arg: program variables second.
		prim arg: (UziPrimitive named: #setValue).
		jmp arg: 3.
		push arg: program variables first.
		push arg: program variables third.
		prim arg: (UziPrimitive named: #setValue).
		end	
	}
