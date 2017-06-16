building messages
message: selector args: arguments token: token
	^ { selector asSymbol . arguments . token withoutTrailingBlanks }