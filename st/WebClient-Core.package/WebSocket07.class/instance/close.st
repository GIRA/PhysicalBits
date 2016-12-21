control requests
close
	"Send a close control request"

	closing ifFalse:[
		closing := true.
		self send: #[] code: 8.
	].