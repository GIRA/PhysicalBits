accessing
getGlobalValue: globalName
	| number |
	number := protocol getGlobalNumber: globalName.
	^ number ifNotNil: [(protocol getGlobal: number) value]