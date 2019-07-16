tests
test072Mod
	| program |	
	program := Uzi compileString: '
	var a = 0;
	task main() running 1/s {
		a = a % 3;
		if a == 0 { toggle(D13); }
		if a == 1 { toggle(D12); }
		if a == 2 { toggle(D11); }
		if a == 3 { toggle(D10); }
		a = a + 1;
	}'.
	self writeFile: thisContext method selector capitalized
		program: program