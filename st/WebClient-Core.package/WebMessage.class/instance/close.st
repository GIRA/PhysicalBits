initialize
close
	"Always closes regardless of connection header"

	stream ifNotNil:[stream close].