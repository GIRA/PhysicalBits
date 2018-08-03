tests
test058Floor
	| program |	
	program := Uzi compileString: '
	task main() running 1/s {
		write(D10, floor(0.25));
		write(D11, floor(0.5));
		write(D12, floor(0.75));
	}'.
	self writeFile: thisContext method selector capitalized
		contents: (UziProtocol new run: program)