visiting
handleIfFalseIfTrueMessageSend: node
	| labelEnd labelTrue |
	self visit: node receiver.
	labelTrue := self newLabel.
	labelEnd := self newLabel.
	script jnz: labelTrue.
	"False block"
	self visit: node arguments first.
	script jmp: labelEnd.
	script label: labelTrue.
	"True block"
	self visit: node arguments second.
	script label: labelEnd