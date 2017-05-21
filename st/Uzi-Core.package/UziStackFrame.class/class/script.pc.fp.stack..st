instance creation
script: anUziScript pc: programCounter fp: framePointer stack: stackArray
	^ self basicNew 
		initializeWithScript: anUziScript 
		pc: programCounter 
		fp: framePointer 
		stack: stackArray