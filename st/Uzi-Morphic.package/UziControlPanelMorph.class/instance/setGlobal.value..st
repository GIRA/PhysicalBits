accessing
setGlobal: globalName value: value
	| number |
	number := controller getGlobalNumber: globalName.
	^ number ifNotNil: [controller setGlobal: number value: value]