utils
check: aString
	^ UziTypeChecker check: (UziOldParser parse: aString)