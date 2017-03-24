visiting
visitSpecialVariableNode: node
	"Special variables are just primitive calls"
	(script prim: node name) node: node