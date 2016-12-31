utils
check: aString
	^ UziTypeChecker check: (UziParser parse: aString)