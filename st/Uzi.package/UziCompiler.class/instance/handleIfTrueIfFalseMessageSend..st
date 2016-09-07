visiting
handleIfTrueIfFalseMessageSend: node
	| labelEnd labelFalse |
	self visit: node receiver.
	labelFalse := self newLabel.
	labelEnd := self newLabel.
	script jz: labelFalse.
	"True block"
	self visit: node arguments first.
	script jmp: labelEnd.
	script label: labelFalse.
	"False block"
	self visit: node arguments second.
	script label: labelEnd