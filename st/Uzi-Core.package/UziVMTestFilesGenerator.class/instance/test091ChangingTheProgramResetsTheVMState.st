tests
test091ChangingTheProgramResetsTheVMState
	| program |	
	program := '
task blink13() running { 
	turnOn(D13);
	delayMs(1000);
	turnOff(D13);
	delayMs(1000);
}'.
	self writeFile: thisContext method selector
		program: program