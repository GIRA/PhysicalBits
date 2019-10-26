initialize-release
initialize 
	super initialize.
	primitivesDict := Dictionary new.
	primitives do: [:prim | primitivesDict at: prim alias put: prim]