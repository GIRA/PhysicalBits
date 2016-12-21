accessing
detail
	| json |
	json := super detail.
	children keysAndValuesDo: [:key :value |
		json at: key put: value summary].
	^ json