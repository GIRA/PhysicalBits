tests
test046ProgramWithPrimitiveDeclaration
	self assertValid: 'prim add;'.
	self assertInvalid: 'prim unaPrimitivaQueNoExisteEnElSpec;'