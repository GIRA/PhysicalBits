bytecode creation
label: arg
	(labels includesKey: arg) ifTrue: [Error signal: 'Multiple label definition'].
	labels at: arg put: bytecodes size.