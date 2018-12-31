debugging
openDebugger
	"Only open a debugger if we have a current program"
	^ self currentProgram ifNotNil: [UziDebugger on: self]