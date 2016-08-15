tests
testProgramCreation2
	| program script |
	program := Uzi program: [:p | p
		script: #main 
		ticking: true delay: 1000
		bytecodes: [:s | s
			push: 9;
			prim: #read;
			jnz: 4;
			push: 9;
			push: 255;
			prim: #write;
			jmp: 3;
			push: 9;
			push: 0;
			prim: #write]].
	self assert: 1 equals: program scripts size.
	self assert: #(9 255 0) equals: (program variables collect: #value).
	script := program scripts first.
	self assert: script name = #main.
	self assert: script isTicking.
	self assert: script delay = 1000.
	self assert: script bytecodes = {
		push arg: program variables first.
		prim arg: (UziPrimitive named: #read).
		jnz arg: 4.
		push arg: program variables first.
		push arg: program variables second.
		prim arg: (UziPrimitive named: #write).
		jmp arg: 3.
		push arg: program variables first.
		push arg: program variables third.
		prim arg: (UziPrimitive named: #write)
	}
