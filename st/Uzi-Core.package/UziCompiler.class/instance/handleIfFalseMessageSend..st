visiting
handleIfFalseMessageSend: node
	| label |
	self visit: node receiver.
	label := self newLabel.
	script jnz: label.
	"False block"
	self visit: node arguments first.
	script label: label