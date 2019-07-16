tests
test090DebuggerBreakpointHaltsAreDeterministic
	| program |
	"For this test I need to be precise in the breakpoint placement, that's
	why I can't rely on the compiler and instead I build the program manually"	
	program := Uzi program: [:p | p
	registerGlobal: #a value: 0;
	script: #meta
	ticking: true
	delay: 0
	instructions: [:s | s
 "0"		push: 17;
 "1"		prim: #read;
 "2"		push: 0;
 "3"		prim: #greaterThan;
 "4"		jz: 2;
 "5"		push: 13;
 "6"		prim: #turnOn];
	script: #increment
	ticking: true
	delay: 10
	instructions: [:s | s
 "7"		push: #a;
 "8"		push: 0.01;
 "9"		prim: #add;
 "10"		pop: #a;
 "11"		push: #a;
 "12"		push: 1;
 "13"		prim: #greaterThan;
 "14"		jz: 2;
 "15"		push: 0;
 "16"		pop: #a];
	script: #blink11
	ticking: true
	delay: 1
	instructions: [:s | s
 "17"		push: 11;
 "18"		prim: #toggle];
	script: #test
	ticking: true
	delay: 0
	instructions: [:s | s
 "19"		push: 9;
 "20"		push: #a;
 "21"		prim: #write;
 "22"		push: 7;
 "23"		push: 0;
 "24"		prim: #write]].

	self writeFile: thisContext method selector capitalized
		program: program