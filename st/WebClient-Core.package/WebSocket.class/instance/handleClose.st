private
handleClose
	"Handle a connection close"

	^self handle: 'close' arguments: (Array with: self)
