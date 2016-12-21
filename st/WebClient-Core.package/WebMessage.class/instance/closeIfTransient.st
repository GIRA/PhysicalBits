initialize
closeIfTransient
	"Close the underlying connection if it's not persistent"

	self isPersistent ifFalse:[self close].
