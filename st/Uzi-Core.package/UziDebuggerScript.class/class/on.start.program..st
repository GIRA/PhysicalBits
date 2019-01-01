instance creation
on: anUziScript start: startPC program: anUziDebuggerProgram
	^ self basicNew
		initializeWithScript: anUziScript
		start: startPC
		program: anUziDebuggerProgram