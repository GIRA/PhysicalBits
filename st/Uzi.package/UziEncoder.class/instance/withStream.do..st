private
withStream: aWriteStream do: aBlock
	"Temporary replace the stream inst var, evaluate the block and return the stream contents.
	After evaluating this, the stream returns to its older value."
	| old |
	old := stream.
	stream := aWriteStream.
	aBlock ensure: [stream := old].
	^ aWriteStream contents