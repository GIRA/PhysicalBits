instruction creation
label: arg
	(labels includesKey: arg) ifTrue: [UziDuplicatedLabelError signal: arg].
	labels at: arg put: instructions size.