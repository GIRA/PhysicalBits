tests
test056Round
	| program |
	program := Uzi compileString: '
	task main() running 1/s {
		write(D10, round(0.25));
		write(D11, round(0.5));
		write(D12, round(0.75));
	}'.
	self writeFile: thisContext method selector capitalized
		contents: (UziProtocol new run: program)