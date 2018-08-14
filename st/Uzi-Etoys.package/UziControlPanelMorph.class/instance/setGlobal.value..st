accessing
setGlobal: globalName value: value
	| number |
	number := protocol getGlobalNumber: globalName.
	^ number ifNotNil: [protocol setGlobal: number value: value]