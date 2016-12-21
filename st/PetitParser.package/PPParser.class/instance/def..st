operators
def: aParser
	"Redefine the receiver as the argument aParser. This method is useful when defining recursive parsers: instantiate a PPUnresolvedParser and later redefine it with another one."

	^ self becomeForward: (aParser name: self name)