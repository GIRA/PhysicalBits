visiting
handleIfTrueMessageSend: node
	| label |
	self visit: node receiver.
	label := self newLabel.
	script jz: label.
	"True block"
	self visit: node arguments first.
	script label: label