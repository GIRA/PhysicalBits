tests
test060Abs
	| program |	
	program := Uzi compileString: '
	task main() running 1/s {
		write(D13, abs(-1));
	}'.
	self writeFile: thisContext method selector capitalized
		contents: (UziProtocol new run: program)