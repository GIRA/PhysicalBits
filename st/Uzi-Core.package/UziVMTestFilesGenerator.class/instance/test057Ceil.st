tests
test057Ceil
	| program |	
	program := Uzi compileString: '
	task main() running 1/s {
		write(D10, ceil(0.25));
		write(D11, ceil(0.5));
		write(D12, ceil(0.75));
	}'.
	self writeFile: thisContext method selector capitalized
		contents: (UziProtocol new run: program)