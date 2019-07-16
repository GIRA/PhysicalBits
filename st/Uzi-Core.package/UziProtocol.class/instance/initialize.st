initializing
initialize
	super initialize.
	scriptWatchers := Dictionary new.
	globals := Dictionary new.
	pins := Dictionary new.
	debugger := UziDebugger on: self.
	pinsReporting := Set new.
	globalsReporting := Set new