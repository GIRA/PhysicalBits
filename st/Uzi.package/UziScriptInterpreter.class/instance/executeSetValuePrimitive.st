primitives
executeSetValuePrimitive
	| val pin |
	val := stack pop.
	pin := stack pop.
	protocol setPin: pin value: val