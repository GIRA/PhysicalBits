visiting
visitNumberNode: node
	| instruction variable |
	"First we register the variable (if not registered already)"
	variable := self registerVariable: node.
	"Then we create the push instruction.
	The argument is the index on the variables array (zero based)"
	instruction := UziPushInstruction arg: variable.
	"Finally, we register the instruction"
	instructions add: instruction 
	