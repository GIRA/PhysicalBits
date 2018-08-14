initializing
initialize
	super initialize.
	globals := Dictionary new.
	pinLastAccess := Dictionary new.
	pinValues := Dictionary new.
	pinModes := Dictionary new.
	pinsReporting := Set new.
	globalsReporting := Set new.
	tickingScripts := Set new