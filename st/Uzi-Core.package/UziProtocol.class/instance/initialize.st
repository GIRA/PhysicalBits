initializing
initialize
	super initialize.
	tickingScripts := Set new.
	globals := Dictionary new.
	pins := Dictionary new.
	debugger := UziDebugger on: self