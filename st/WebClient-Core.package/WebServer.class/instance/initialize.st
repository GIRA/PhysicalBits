initialize
initialize
	"Initialize the receiver"

	mutex := Mutex new.
	entryPoints := Dictionary new.
	connections := IdentitySet new.
	vault := Dictionary new.
	nonceCache := Dictionary new.
	sessions := Dictionary new.
	self logAction:[:aString| Transcript cr; show: aString].
