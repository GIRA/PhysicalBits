tests
test022FunctionArgumentShouldNotBeRegisteredAsGlobal
	self assertInvalid: '
	task main() running 1 / s{
		toggle(getPin(pin));
	}

	func getPin(pin){
		return pin;
	}'.