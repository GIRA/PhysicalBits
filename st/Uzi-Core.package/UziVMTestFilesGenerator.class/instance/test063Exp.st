tests
test063Exp
	| program |	
	program := Uzi compileString: '
	task main() running 1/s {
		write(D7, exp(-13.81551055796427)); "0"
		write(D8, exp(-0.693147180559945)); "0.5"
		write(D9, exp(0)); "1"
	}'.
	self writeFile: thisContext method selector capitalized
		contents: (UziProtocol new run: program)