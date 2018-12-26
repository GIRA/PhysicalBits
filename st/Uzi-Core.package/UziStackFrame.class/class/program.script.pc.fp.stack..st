instance creation
program: anUziProgram script: anUziScript pc: programCounter fp: framePointer stack: stackArray
	^ self basicNew 
		initializeWithProgram: anUziProgram
		script: anUziScript 
		pc: programCounter 
		fp: framePointer 
		stack: stackArray