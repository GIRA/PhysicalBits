initialize-release
initialize
	super initialize.
	lastAccess := Time millisecondClockValue.
	history := OrderedCollection new