tests
test065IsCloseTo
	| program |	
	program := '
	"D13 -> ON
	D12 -> ON
	D11 -> OFF
	D10 -> ON"
	task main() running 1/s {
		if isCloseTo(1, 1.00001) { 
			turnOn(D13); 
		} else { 
			turnOff(D13); 
		}
		
		write(D12, isCloseTo(0, 0));
		write(D11, isCloseTo(2, 2.001));
		write(D10, isCloseTo(Infinity, Infinity));
	}'.
	self writeFile: thisContext method selector
		program: program