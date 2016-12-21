accessing-actions
on: method do: aBlockOrNil
	aBlockOrNil
		ifNil: [actions removeKey: method ifAbsent: ["Do nothing"]]
		ifNotNil: [:block | actions at: method put: block]
	