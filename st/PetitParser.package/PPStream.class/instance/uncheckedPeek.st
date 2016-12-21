accessing
uncheckedPeek
	"An unchecked version of peek that throws an error if we try to peek over the end of the stream, even faster than #peek."

	^ collection at: position + 1