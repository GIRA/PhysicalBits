testing
isLocal: varName
	^ script 
		ifNil: [false]
		ifNotNil: [(script arguments anySatisfy: [:var | var name = varName])
			or: [(script locals anySatisfy: [:var | var name = varName])]]