tests
test059Sqrt
	| program |	
	program := Uzi compileString: '
	task main() running 1/s {
		write(D9, sqrt(0.25));
	}'.
	self writeFile: thisContext method selector capitalized
		contents: (UziProtocol new run: program)