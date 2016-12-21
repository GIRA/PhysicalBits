private
printOn: aStream
	"Print the receiver"

	aStream nextPutAll: self class name.
	aStream nextPutAll: '(', name, ')'.