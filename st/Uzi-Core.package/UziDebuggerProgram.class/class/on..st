instance creation
on: anUziProgram
	^ self basicNew initializeWithProgram: (anUziProgram ifNil: [UziProgram empty])