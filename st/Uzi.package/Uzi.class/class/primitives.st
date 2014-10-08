primitives
primitives
	"
	primitives := nil.
	"
	^ primitives ifNil: [	primitives := Dictionary new
			at: #getValue put: 0;
			at: #setValue put: 1;
			yourself]